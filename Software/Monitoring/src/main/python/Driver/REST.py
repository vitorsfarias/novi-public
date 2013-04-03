'''
Created on Feb 4, 2013

@author: Sandor Laki, rewritten for jython
@organization: ELTE
@contact: laki@complex.elte.hu
'''

from Driver import Driver
from Credential.credentialtypes import UsernamePassword
import urllib2, base64

from javax.net.ssl import TrustManager, X509TrustManager
from jarray import array
from javax.net.ssl import SSLContext

class TrustAllX509TrustManager(X509TrustManager):
     def checkClientTrusted(self, chain, auth):
         pass

     def checkServerTrusted(self, chain, auth):
         pass

     def getAcceptedIssuers(self):
         return None

# It is not threadsafe, since setDefault is a global function...
def setSSLTrusted():
    trust_managers = array([TrustAllX509TrustManager()], TrustManager)
    TRUST_ALL_CONTEXT = SSLContext.getInstance("SSL")
    TRUST_ALL_CONTEXT.init(None, trust_managers, None)
    DEFAULT_CONTEXT = SSLContext.getDefault()
    SSLContext.setDefault(TRUST_ALL_CONTEXT)

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
        self.proxy = urllib2.Request(self.url)
        if isinstance(credential, UsernamePassword):
            base64string = base64.encodestring('%s:%s' % (credential.username, credential.password)).replace('\n', '')
            self.proxy.add_header("Authorization", "Basic %s" % base64string)
#        self.proxy = Http(cache = self.cache, timeout = self.timeout)
#        self.proxy.disable_ssl_certificate_validation = not validate_ssl
#        if isinstance(credential, UsernamePassword):
#            # use password authentication
#            self.proxy.add_credentials(credential.username, credential.password)

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
        try:
            resp = urllib2.urlopen(self.proxy)
            return resp.read()
        except Exception, e:
            print "Error: %s" % e
            return None
#        status, response = self.proxy.request(uri = url, method = "GET")
#        if status.status == 200:
#            return response
#        if status.status == 304:
#            self.logger.warning("remote content @ %s was not changed" % url)
#            return None
#        self.logger.error("%s -- retrieving @%s failed: %s" % (status, url, response))
#        return None
