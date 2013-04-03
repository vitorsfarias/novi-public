'''
Created on Jul 18, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

from Driver import Driver, DriverError
from paramiko import SSHClient, RSAKey, SSHException, AutoAddPolicy
from Credential.credentialtypes import UsernamePassword, UsernameRSAKey
from time import sleep
import socket
import logging

class SshDriver(Driver):
    '''
    @summary: implements a driver to build an SSH connection to a server using paramiko package.
    @cvar timeout: a timeout to set up the connection
    @type timeout: float
    @cvar trials: how many times to retry if a time out event occurs or if the remote server is busy to respond
    @type trials: int
    @cvar wait: how long to wait between two trials
    @type wait: .2
    @ivar client: the api of the ssh client
    @type client: paramiko.SSHClient or None
    @ivar host: the host name of the remote ssh server
    @type host: str or None
    @ivar isconnected: indicates whether the connection is set up
    @type isconnected: bool
    '''
    timeout = 5
    trials = 3
    wait = .2

    def __init__(self):
        '''
        @summary: bind the paramiko loggers to the "NOVI.DRIVER"
        '''
        self.client = None
        self.host = None
        #bind paramiko.transport logger to the same handlers used by NOVI.DRIVER
        l = logging.getLogger("paramiko.transport")
        for hlr in self.logger.handlers:
            l.addHandler(hlr)

    def __del__(self):
        '''
        @summary: close connection upon an implicit deletion of the driver
        '''
        self.close()
        
    def connect(self, host, credential, port = 22, known_host = None):
        '''
        @summary: set up the connection
        @param host: the host name of the remote server
        @type host: str
        @param credential: the secret to use for connection set up
        @type credential: L{Credential}
        @param port: the port remote ssh server is listening
        @type port: int
        @param known_host: a file name containing host signatures to check, if None AutoAddPolicy applies 
        @type known_host: str
        '''
        self.client = SSHClient()
        if known_host is None:
            self.client.set_missing_host_key_policy( AutoAddPolicy() )
        else:
            self.client.load_host_keys(filename = known_host)
        if isinstance(credential, UsernamePassword):
        # use password authentication
            self.client.connect(hostname = host, port = port, 
                                username = credential.username, password =credential.password, 
                                timeout = self.timeout, look_for_keys = False, compress = True)
        elif isinstance(credential, UsernameRSAKey):
        # use the RSA key
            if credential.password:
                pw = credential.password
            else:
                pw = None
            key = RSAKey(password = pw, filename = credential.rsakey)
            n = self.trials
            while n:
                try:
                    self.client.connect(hostname = host, port = port, 
                                username = credential.username, pkey = key, 
                                timeout = self.timeout, look_for_keys = False, compress = True)
                    break
                except SSHException, e:
                    if e.message.startswith("Error reading SSH protocol banner"):
                        n -= 1
                        self.logger.warn("retry %d times to connect @%s in %f seconds" % (n, host, self.wait))
                        sleep(self.wait)
                    else:
                        self.logger.error("cannot connect @%s" % (host))
                        raise DriverError("Cannot connect @%s " % host)
                except socket.timeout:
                    n -= 1
                    self.logger.warn("time out, retry %d times to connect @%s in %f seconds" % (n, host, self.wait))
                    sleep(self.wait)
        if not self.isConnected:
            self.close()
            self.logger.error("cannot connect @%s" % (host))
            raise DriverError("Cannot connect @%s " % host)
        self.host = host
        self.logger.info("ssh connected @ %s:%d" % (self.host, port))

    def close(self):
        '''
        @summary: closes the ssh connection
        '''
        try:
            self.client.close()
            self.logger.info("ssh disconnected @ %s" % (self.host))
        except:
            pass
        finally:
            self.client = None
            self.host = None

    @property
    def isConnected(self):
        try:
            return self.client.get_transport().is_active()
        except:
            return False

class SshExec(SshDriver):
    '''
    @summary: an extension of the L{SshDriver} to execute commands on the remote machine
    @ivar command: the string representation of the command to run
    @type command: str
    '''
    
    def __init__(self, host, credential, port = 22, command = "echo helloworld @ `hostname`", known_host = None):
        '''
        @summary: initializes an ssh connection and stores a default command
        @param host: the host name of the remote server
        @type host: str
        @param credential: the secret to use for connection set up
        @type credential: L{Credential}
        @param port: the port remote ssh server is listening
        @type port: int
        @param command: the default remote command
        @type command: str
        @param known_host: a file name containing host signatures to check, if None AutoAddPolicy applies 
        @type known_host: str
        '''
        SshDriver.__init__(self)
        self.connect(host, credential, port, known_host)
        self.command = command
    
    def execute(self, command = None):
        '''
        @summary: executes a remote command
        @param command: the command to run, if None, the default command is issued
        @type command: str or None
        @return: the standard output of the command run
        @rtype: paramico.ChannelFile
        '''
        if not self.isConnected:
            raise DriverError("Not connected")
        if command is None:
            command = self.command
        _, stout, sterr = self.client.exec_command(command = command)
        e = sterr.read()
        self.logger.debug("executed @%s '%s'" % (self.host, command))
        if len(e):
            self.logger.warning("execution @%s '%s' failed: %s" % (self.host, command, e))
        return stout
