'''
Created on Oct 27, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

class Credential(object):
    '''
    @summary: an empty credential to serve as an ancient class
    @author: steger, jozsef 
    '''
    pass

class UsernamePassword(Credential):
    '''
    @summary: container for a pair of user name and password
    @author: steger, jozsef 
    '''

    def __init__(self, username, password):
        '''
        @summary: Constructor
        @param username: the username
        @type username: string
        @param password: the password
        @type password: string
        '''
        self.username = username
        self.password = password

class UsernameRSAKey(Credential):
    '''
    @summary: container for a triple of user name, private key and an optional password for the key
    @author: steger, jozsef 
    '''

    def __init__(self, username, rsakey, password = ""):
        '''
        @summary: Constructor
        @param username: the username
        @type username: string
        @param rsakey: the private key
        @type rsakey: string 
        @param password: the optional password to unlock the private key, default: ""
        @type password: string
        '''
        self.username = username
        self.rsakey = rsakey
        self.password = password
        