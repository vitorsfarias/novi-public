'''
Created on May 22, 2012

@author: steger
'''

from gi.repository import Gtk
from os import path
from win_skeleton import win_skeleton

class win_keyring(win_skeleton):
    def __init__(self, model):
        '''
        Constructor
        '''
        self.keyring = model.keyring
        win_skeleton.__init__(self, "windows/window_keyring.glade", "window_keyring")
        self.builder.get_object("button_add_credential").connect('clicked', self.add_credential)
        self.builder.get_object("button_cancel_cred").connect('clicked', self.destroy)
        self.builder.get_object("notebook_credential").set_current_page(1)
        self.show_all()

    def add_credential(self, widget):
        page = self.builder.get_object("notebook_credential").get_current_page()
        if page == 0:
            # Username Password
            cred_type = self.keyring.UNAMEPW
            keyfilename = ""
            username = self.builder.get_object("entry_uname0").get_text()
            password = self.builder.get_object("entry_passwd0").get_text()
        elif page == 1:
            # Username Private key
            cred_type = self.keyring.UNAMEPKEY
            keyfilename = self.builder.get_object("filechooserbutton1").get_filename()
            if keyfilename is None:
                self.error("Please, provide a private key file")
            else:
                if not path.exists(keyfilename):
                    self.error("File %s does not exist" % keyfilename)
            username = self.builder.get_object("entry_uname1").get_text()
            password = self.builder.get_object("entry_passwd1").get_text()
        else:
            return
        if username is None:
            self.error("Please, provide a valid user name")
            return
        try:
            self.keyring.add_credential(cred_type, username, password, keyfilename)
        except:
            self.error("This credential is already in your key ring")
        self.destroy(widget)
