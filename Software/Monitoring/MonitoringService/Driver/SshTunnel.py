'''
Created on Jan 14, 2013

@author: steger
'''

import select
import SocketServer
from Driver import Driver
from SshExec import SshDriver
from threading import Thread
from SshExec import SshExec

class SshTunnel(SshDriver):
    '''
    @summary: this class extends L{SshDriver} and establishes a connection 
    to the requested SSH server and sets up local port
    forwarding (the openssh -L option) from a local port through a tunneled
    connection to a destination reachable from the SSH server machine.
    @ivar t: the thread container
    @type t: threading.Thread or None
    '''

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
                Driver.logger.debug('Incoming request to %s:%d failed: %s' % (self.chain_host,
                                                                  self.chain_port,
                                                                  repr(e)))
                return
            if chan is None:
                Driver.logger.debug('Incoming request to %s:%d was rejected by the SSH server.' %
                        (self.chain_host, self.chain_port))
                return
    
            Driver.logger.debug('Tunnel open %r -> %r -> %r' % (self.request.getpeername(),
                                                                chan.getpeername(), (self.chain_host, self.chain_port)))
            while True:
                r, _, _ = select.select([self.request, chan], [], [])
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
            Driver.logger.debug('Tunnel closed from %r' % (self.request.getpeername(),))
    
    def __init__(self):
        '''
        @summary: allocates thread pointer container
        '''
        SshDriver.__init__(self)
        self.t = None
        
    def connect(self, host, credential, localport, port, remoteserver, remoteport, known_host = None):
        '''
        @summary: set up the tunnel connection
        @param host: the host name of the remote server acting a port forwarder
        @type host: str
        @param credential: the secret to use for connection set up
        @type credential: L{Credential}
        @param localport: the local port entry mapped to the remoteport
        @type localport: int
        @param port: the port of the forwarder ssh server
        @type port: int
        @param remoteserver: the sink of the tunnel
        @type remoteserver: str
        @param remoteport: the port of the tunnel sink
        @type remoteport: int
        @param known_host: a file name containing host signatures to check, if None AutoAddPolicy applies 
        @type known_host: str
        '''
        SshDriver.connect(self, host, credential, port, known_host)
        self.logger.info('Now forwarding port %d to %s:%d ...' % (localport, remoteserver, remoteport))
        self.t = Thread(target = self._tran, kwargs = {'localport': localport, 'remoteserver': remoteserver, 'remoteport': remoteport})
        self.t.daemon = True
        self.t.start()

    def _tran(self, localport, remoteserver, remoteport):
        '''
        @summary: thread worker to transport data over the tunnel
        @param localport: the local port entry mapped to the remoteport
        @type localport: int
        @param remoteserver: the sink of the tunnel
        @type remoteserver: str
        @param remoteport: the port of the tunnel sink
        @type remoteport: int
        '''
        try:
            # this is a little convoluted, but lets me configure things for the Handler
            # object.  (SocketServer doesn't give Handlers any way to access the outer
            # server normally.)
            class SubHander (self.Handler):
                chain_host = remoteserver
                chain_port = remoteport
                ssh_transport = self.client.get_transport()
            self.service = self.ForwardServer(('', localport), SubHander)
            self.service.serve_forever()
        except KeyboardInterrupt:
            self.logger.debug('C-c: Port forwarding stopped.')
            self.close()
    
    def close(self):
        '''
        @summary: stops the thread and tears down the tunnel
        '''
        if self.t is None:
            return
        self.t.join(timeout = self.timeout)
        self.t = None
        self.service.shutdown()
        self.logger.info('Port forwarding stopped @ %s.' % self.host)
        SshDriver.close(self)


class SshExecTunnel(SshTunnel):
    '''
    @summary: an extension of the L{SshTunnel} driver to execute commands 
    on the remote machine accessed via the tunnel
    @ivar command: the string representation of the command to run
    @type command: str
    @ivar localdriver: the representation of an ssh client connecting over an existing ssh tunnel
    @type localdriver: L{SshExec}
    '''

    def __init__(self, host, credential, localport, port, remoteserver, remoteport, remotecredential = None, command = "echo helloworld @ `hostname`", known_host = None):
        '''
        @summary: initializes an ssh connection and stores a default command
        @param host: the host name of the remote server acting a port forwarder
        @type host: str
        @param credential: the secret to use for tunnel set up
        @type credential: L{Credential}
        @param localport: the local port entry mapped to the remoteport
        @type localport: int
        @param port: the port of the forwarder ssh server
        @type port: int
        @param remoteserver: the sink of the tunnel
        @type remoteserver: str
        @param remoteport: the port of the tunnel sink
        @type remoteport: int
        @param remotecredential: the secret to use for connection set up, if None then we fall back to the credential
        @type remotecredential: L{Credential} or None
        @param command: the default remote command
        @type command: str
        @param known_host: a file name containing host signatures to check, if None AutoAddPolicy applies 
        @type known_host: str
        '''
        SshTunnel.__init__(self)
        self.connect(host, credential, localport, port, remoteserver, remoteport, known_host)
        self.command = command
        if remotecredential is None:
            remotecredential = credential
        self.localdriver = SshExec(host = 'localhost', credential = remotecredential, port = localport, command = command, known_host = None)
        self.logger.info("connected over tunnel")
    
    def execute(self, command = None):
        '''
        @summary: executes a remote command
        @param command: the command to run, if None, the default command is issued
        @type command: str or None
        @return: the standard output of the command run
        @rtype: paramico.ChannelFile
        '''
        return self.localdriver.execute(command)
