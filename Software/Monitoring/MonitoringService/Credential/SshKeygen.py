'''
Created on Jul 20, 2011

@author: steger
'''
from M2Crypto import RSA
from base64 import b64encode
from os import chmod, path
import stat

# paramiko provides this functionality, so maybe we don't need this class. see paramiko.PKey

class CannotSet(Exception):
    pass

class SshKeygen(object):
    '''
    Generates a pair of RSA keys.
    Enables saving the keys to the file system.
    '''
    def __init__(self, bits = 1024, e = 65337):
        '''
        Initiates the pair of RSA keys
        @param bits: the length of the keys in bits
        @type bits: integer  
        @param e: the exponent
        @type e: integer  
        '''
        self.rsa = RSA.gen_key(bits, e, lambda: None)
        
    def _check_filename(self, filename):
        if path.exists(filename):
            raise Exception("File exists: %s" % filename)

    @property
    def private(self):
        '''
        @summary: return the private key in PEM format
        @return: the private key in PEM format
        @rtype: string
        '''
        return self.rsa.as_pem(cipher = None)

    @private.setter
    def private(self, value):
        raise CannotSet

    @private.deleter
    def private(self):
        raise CannotSet
    
    @staticmethod
    def _convert(rsa):
        return b64encode('\x00\x00\x00\x07ssh-rsa%s%s' % (rsa.pub()[0], rsa.pub()[1]))

    @property
    def public(self):
        '''
        @summary: return the public key in base64 format conforming to the content of authorized_keys
        @return: the public key in base64 format
        @rtype: string
        '''
        return self._convert(self.rsa)

    @public.setter
    def public(self, value):
        raise CannotSet

    @public.deleter
    def public(self):
        raise CannotSet

    def save_private_key(self, filename):
        '''
        @summary: save the private key in the file system in a named file.
        @param filename: the filename to store the private key.
        @type filename: string  
        '''
        self._check_filename(filename)
        self.rsa.save_key(filename, cipher = None)
        chmod(filename, stat.S_IRUSR)
                
    def save_public_key(self, filename):
        '''
        @summary: save the public key in the file system in a named file.
        @param filename: the filename to store the public key.
        @type filename: string  
        '''
        self._check_filename(filename)
        with open(filename, "w") as f:
            f.write("ssh-rsa %s" % self.public)

    @staticmethod            
    def convert_key_from_file(filename):
        '''
        @summary: convert a private key stored in a file in PEM format and return the public key in base64 format conforming to the content of authorized_keys
        @return: the public key in base64 format
        @rtype: string
        '''
        return SshKeygen._convert( RSA.load_key(file = filename) )
