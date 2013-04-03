'''
Created on Sep 2, 2011

@author: laki, sandor
@organization: ELTE
@contact: laki@complex.elte.hu
@author: steger, jozsef
'''

#TODO: catch exception using the .service attribute and log it in the Driver log
from suds import transport, client, wsse
from Driver import Driver

class SOAPClient(Driver, client.Client):
    '''
    @summary: implements SOAP driver to access remote procedures
    '''
    pass

SOAPSecurity=wsse.Security
SOAPUsernameToken=wsse.UsernameToken
SOAPHttpAuthenticated=transport.http.HttpAuthenticated
