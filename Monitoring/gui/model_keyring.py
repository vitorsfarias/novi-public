'''
Created on May 22, 2012

@author: steger
'''

from gi.repository import Gtk

class keyring(Gtk.ListStore):
    '''
    classdocs
    '''
    UNAMEPW, UNAMEPKEY = 0, 1

    def __init__(self, username):
        '''
        Constructor
        '''
        self._username = username
        Gtk.ListStore.__init__(self, int, str, str, str)
    
    @property
    def username(self):
        return self._username
    @username.setter
    def username(self, username):
        if username is None or not len(username):
            raise Exception("Empty username")
        self._username = username

    def add_credential(self, cred_type, username, password = None, keyfilename = None):
        it = self.get_iter_first()
        while it:
            ct, un, pw, kf = self.get(it, 0,1,2,3)
            it = self.iter_next(it)
            if cred_type == ct and username == un:
                if cred_type == self.UNAMEPW and password == pw:
                    raise Exception("Username(%s) password already in the key ring" % username)
                if cred_type == self.UNAMEPKEY and keyfilename == kf:
                    raise Exception("Username(%s) keyfile already in the key ring" % username)
        self.append([cred_type, username, password, keyfilename])

    def __iter__(self):
        it = self.get_iter_first()
        while it:
            cred_type, username, password, keyfile = self.get(it, 0,1,2,3)
            it = self.iter_next(it)
            if cred_type == self.UNAMEPW:
                yield { 'username': username, 'password': password }
            elif cred_type == self.UNAMEPKEY:
                yield { 'username': username, 'rsakey': keyfile, 'password': password }
