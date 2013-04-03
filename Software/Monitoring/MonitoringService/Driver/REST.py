'''
Created on Feb 4, 2013

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

from Driver import Driver
from Credential.credentialtypes import UsernamePassword
from httplib2 import Http

class RESTDriver(Driver):
    '''
    @summary: implements REST driver to fetch using http GET
    @cvar timeout: timeout of connection
    @type timeout: float
    @cvar cache: a cache directory
    @type cache: str
    @ivar url: a default document locator to be reused
    @type url: str
    @ivar proxy: an interface to the http server
    @type proxy: httplib2.Http
    '''
    timeout = 10
    cache = "/tmp/.cache"
    
    def __init__(self, url, credential = None, validate_ssl = False):
        '''
        @summary: initializes a proxy to the http service and saves a default document locator
        @param url: the default document locator
        @type url: str
        @param credential: an authentication secret
        @type credential: L{Credential} or None
        @param validate_ssl: whether to apply strick certificate validation, default is False
        @type validate_ssl: bool 
        '''
        self.url = url
        self.proxy = Http(cache = self.cache, timeout = self.timeout)
        self.proxy.disable_ssl_certificate_validation = not validate_ssl
        if isinstance(credential, UsernamePassword):
            # use password authentication
            self.proxy.add_credentials(credential.username, credential.password)

    def fetch(self, url = None):
        '''
        @summary: retrieve the document
        @param url: the document locator, if not present the default is used
        @type url: str or None
        @return: the remote document
        @rtype: str or None
        @note: if the remote content cached is not changed, None is returned
        '''
        if url is None:
            url = self.url
        status, response = self.proxy.request(uri = url, method = "GET")
        if status.status == 200:
            return response
        if status.status == 304:
            self.logger.warning("remote content @ %s was not changed" % url)
            return None
        self.logger.error("%s -- retrieving @%s failed: %s" % (status, url, response))
        return None
