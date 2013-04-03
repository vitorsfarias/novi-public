'''
Created on Jan 14, 2013

@author: steger
'''

import select
import SocketServer
import sys

import paramiko

#FIXME: let an SshTunnel instance launch a new thread;
#TODO: docs
#TODO: localport could be retrieved from a pool 

class ForwardServer (SocketServer.ThreadingTCPServer):
    daemon_threads = True
    allow_reuse_address = True
    

class Handler (SocketServer.BaseRequestHandler):

    def handle(self):
        try:
            chan = self.ssh_transport.open_channel('direct-tcpip',
                                                   (self.chain_host, self.chain_port),
                                                   self.request.getpeername())
        except Exception, e:
            print('Incoming request to %s:%d failed: %s' % (self.chain_host,
                                                              self.chain_port,
                                                              repr(e)))
            return
        if chan is None:
            print('Incoming request to %s:%d was rejected by the SSH server.' %
                    (self.chain_host, self.chain_port))
            return

        print('Connected!  Tunnel open %r -> %r -> %r' % (self.request.getpeername(),
                                                            chan.getpeername(), (self.chain_host, self.chain_port)))
        while True:
            r, w, x = select.select([self.request, chan], [], [])
            if self.request in r:
                data = self.request.recv(1024)
                if len(data) == 0:
                    break
                chan.send(data)
            if chan in r:
                data = chan.recv(1024)
                if len(data) == 0:
                    break
                self.request.send(data)
        chan.close()
        self.request.close()
        print ('Tunnel closed from %r' % (self.request.getpeername(),))




class SshTunnel(object):
    '''
This class establishes a connection to the requested SSH server and sets up local port
forwarding (the openssh -L option) from a local port through a tunneled
connection to a destination reachable from the SSH server machine.
    '''
    def __init__(self):
        pass

    def setup(self, localport = 4000, username = 'root', server = 'localhost', serverport = 22, remoteserver = 'localhost', remoteport = 22, keyfile = None, password = None,
                 look_for_keys = False):
        '''
        Constructor
        '''
        client = paramiko.SSHClient()
        client.load_system_host_keys()
        client.set_missing_host_key_policy(paramiko.WarningPolicy())

        print ('Connecting to ssh host %s:%d ...' % (server, serverport))
        try:
            client.connect(server, serverport, username = username, key_filename = keyfile,
                       look_for_keys = look_for_keys, password = password)
        except Exception, e:
            print '*** Failed to connect to %s:%d: %r' % (server, serverport, e)
            sys.exit(1)

        print ('Now forwarding port %d to %s:%d ...' % (localport, remoteserver, remoteport))

        try:
            # this is a little convoluted, but lets me configure things for the Handler
            # object.  (SocketServer doesn't give Handlers any way to access the outer
            # server normally.)
            class SubHander (Handler):
                chain_host = remoteserver
                chain_port = remoteport
                ssh_transport = client.get_transport()
            self.service = ForwardServer(('', localport), SubHander)
            self.service.serve_forever()
        except KeyboardInterrupt:
            print 'C-c: Port forwarding stopped.'
            sys.exit(0)
    
    def teardown(self):
        print "ENDE"
        self.service.shutdown()


if __name__ == '__main__':
    T = SshTunnel()
    from threading import Timer
    t = Timer(5.0, T.teardown)
    t.start() # after 30 seconds, "hello, world" will be printed
    T.setup(username = 'novi_novi', server = 'novilab.elte.hu', remoteserver = 'complex.elte.hu', keyfile = '/home/steger/Private/ssh/novi_rsa')
    