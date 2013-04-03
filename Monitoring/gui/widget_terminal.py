'''
Created on Oct 26, 2012

@author: steger
'''

from gi.repository import Gtk, Gdk, GLib, Vte
import paramiko
import socket
import select

import threading
import traceback
from Driver.SshExec import SshDriver
from Credential.credentialtypes import UsernameRSAKey, UsernamePassword


class SshTerminal(Vte.Terminal):
    '''
    classdocs
    '''
    port = 22

    def __init__(self):
        '''
        Constructor
        '''
        Vte.Terminal.__init__(self)
        self.connect("commit", self.listen_terminal)
        self.driver = SshDriver()
        self.channel = None
        self.t = None
    
    @property
    def isConnected(self):
        return self.driver.isConnected
   
    # TODO: fix this...
    def connectssh_federica(self, hostname, uag, credential):
        try:
            print '*** Connecting... %s@%s' % (credential.username, uag)
            self.driver.connect(uag, credential)
            self.channel = self.driver.client.invoke_shell()
            self.channel.settimeout(0.0)
            if self.channel:
                self.channel.send("ssh %s\n" % hostname)
            self.t = threading.Thread(target = self.listen_ssh)
            self.t.setDaemon(True)
            self.t.start()
        except Exception, e:
            print '*** Caught exception: %s: %s' % (e.__class__, e)
            self.close()
            raise

    def connectssh(self, hostname, credential):
        try:
            print '*** Connecting... %s@%s' % (credential.username, hostname)
            self.driver.connect(hostname, credential)
            self.channel = self.driver.client.invoke_shell()
            self.channel.settimeout(0.0)
            self.t = threading.Thread(target = self.listen_ssh)
            self.t.setDaemon(True)
            self.t.start()
        except Exception, e:
            print '*** Caught exception: %s: %s' % (e.__class__, e)
            self.close()
            raise

    def connectssh_ring(self, hostname, model):
        for cred_details in model:
            try:
                if cred_details.has_key("rsakey"):
                    credential = UsernameRSAKey(**cred_details)
                else:
                    credential = UsernamePassword(**cred_details)
                self.connectssh(hostname, credential)
                break
            except Exception, e:
                print e
                pass
        if not self.isConnected:
            raise Exception("Cannot connect to %s" % hostname)

    def connectssh_federica_ring(self, hostname, uag, model):
        for cred_details in model:
            try:
                if cred_details.has_key("rsakey"):
                    credential = UsernameRSAKey(**cred_details)
                else:
                    credential = UsernamePassword(**cred_details)
                self.connectssh_federica(hostname, uag, credential)
                break
            except Exception, e:
                print e
                pass
        if not self.isConnected:
            raise Exception("Cannot connect to %s" % hostname)



    def __del__(self):
        if self.t:
            self.t.join()
        self.close()
    
    def close(self):
        try:
            self.channel.close()
        except:
            pass
        finally:
            self.channel = None
        self.driver.close()

    def listen_terminal(self, terminal, *args):
        (text, length) = args
        if length and self.channel:
            if self.channel.send(text) == 0:
                print "*** Remote end gave up listening???"
    
    def listen_ssh(self):
        try:    
            while True:
                select.select([self.channel], [], [])
                try:
                    data = self.channel.recv(1024)
                    if len(data) == 0:
                        break
                    try:
                        self.feed(data, len(data))
                    except:
                        self.feed(data)
                except socket.timeout:
                    pass
        except Exception, e:
            print "HIBA", e
        finally:
            self.close()

