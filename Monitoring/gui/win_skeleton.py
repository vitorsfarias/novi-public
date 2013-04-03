'''
Created on May 24, 2012

@author: steger
'''

from gi.repository import Gtk

class win_skeleton(object):
    
    def __init__(self, gladefile, win_name):
        self.builder = Gtk.Builder()
        self.builder.add_from_file(gladefile)
        self.win_name = win_name

    def show_all(self):
        self.builder.get_object(self.win_name).show_all()

    def destroy(self, widget):
        self.builder.get_object(self.win_name).destroy()

    def connect(self, signal, callback, *args):
        self.builder.get_object(self.win_name).connect(signal, callback, *args)

    def error(self, message):
        '''
        @summary: helper to pop up an error message window
        @param message: text message
        @type message: str
        '''
        dialog = Gtk.MessageDialog(flags = Gtk.DialogFlags.DESTROY_WITH_PARENT,
            type = Gtk.MessageType.ERROR, 
            buttons = Gtk.ButtonsType.OK, 
            message_format = message)
        dialog.set_title("An error set in...")
        dialog.run()
        dialog.destroy()

    def choice(self, message):
        '''
        @summary: helper to pop up a yes or no message window
        @param message: text message
        @type message: str
        @return: user response
        '''
        dialog = Gtk.MessageDialog(flags = Gtk.DialogFlags.DESTROY_WITH_PARENT,
            type = Gtk.MessageType.ERROR, 
            buttons = Gtk.ButtonsType.YES_NO, 
            message_format = message)
        dialog.set_title("You have to make a choice...")
        response = dialog.run()
        dialog.destroy()
        return response
