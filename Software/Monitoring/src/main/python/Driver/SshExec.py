from __future__ import with_statement
'''
Created on Feb 29, 2012

@summary: A Jython compatible ssh driver
@author: Sandor Laki
@organization: ELTE
@contact: lakis@inf.elte.hu
'''


from java.io import BufferedReader
from java.io import IOException
from java.io import InputStream
from java.io import InputStreamReader
from java.io import File
from jarray import zeros
from java.lang import String
from com.jcraft.jsch import JSch
from StringIO import StringIO
#import libssh2
#import socket
from tempfile import mkstemp
from os import close, write, unlink, path, access, R_OK
#from SshKeygen import SshKeygen
from threading import Lock
from Credential.credentialtypes import Credential, UsernameRSAKey,\
    UsernamePassword
from Driver import Driver
import org.python.core.util.FileUtil as FileUtil
import java.lang.Exception
#from org.slf4j import Logger
#from org.slf4j import LoggerFactory
#import org.python.core.PyFile as PyFile
#driverlock = Lock()

class SshExec(Driver):
    '''
    @summary: this class handles control of a monitoring tool over an ssh channel
    @author: steger, jozsef 
    @todo: get rid of global lock if possible
    @note: if no global lock is there, a lot os segmentation faults occur in a concurrent session opening and program execution
    '''
    #lock = Lock() #driverlock
#    log = LoggerFactory.getLogger("eu.novi.monitoring.Driver.SshExec")

    def __init__(self, host, credential, port = 22, command = "echo helloworld @ `hostname`", known_host = None):
        '''
        @summary: initiates a class to execute a single remote command via ssh protocol, tekes care of opening ssh session
        @param host: name of the hos machine
        @type host: string
        @param credential: authentication details
        @type credential: Credential
        @param port: port of the ssh service
        @type port: integer
        @param command: the remote command to execute later
        @type command: string
        @raise Exception: wrong authentication type
        
        @note: only a single command can be run by the class
        
        
        @todo: check what happens with commands run in the background         
        '''
        self.lock = Lock()
        self.session = None
        self.channel = None
        if host is None: return

        if not isinstance(credential, Credential):
            raise Exception("wrong type of credential")
        with self.lock:
            self._result = ""
#            self.session = libssh2.Session()
#            self.session.set_banner()
            self.command = command
            self.fn_pub = None
#            self.pemfile = None
            
            try:
                self.jsch = JSch()
#                self.log.info("Host:%s Username:%s Port:%s Command:%s" % (host, credential.username, port, self.command))
                print "h:%s un:%s p:%s" % (host, credential.username, port)
		self.session = self.jsch.getSession(credential.username, host, port)
		#self.jsch.setKnownHosts("/home/maven/.ssh/known_hosts")

                if isinstance(credential, UsernameRSAKey):
                    privatekey = credential.rsakey
#                    self.log.info("Credential: %s" % privatekey)
                    self.jsch.addIdentity(privatekey)
                    self.session.setConfig("StrictHostKeyChecking", "no")
                    self.session.setTimeout(5000);
                    print "identity file %s\n" % privatekey
                    PATH=privatekey
                    if path.exists(PATH) and path.isfile(PATH) and access(PATH, R_OK):
                        print "File exists and is readable"
#                        self.log.info("Privatekey exists and is readable")
                    else:
#                        self.log.info("RSA key is missing: %s" % PATH)
                        raise Exception("RSA key file is missing or not readable: %s" % PATH)

#                    publickey_srt = SshKeygen.convert_key_from_file(privatekey)
#                    fd, publickey = mkstemp(suffix = ".pub", prefix = "rsa", text = True)
#                    write(fd, "ssh-rsa %s" % publickey_srt)
#                    close(fd)
#                    self.fn_pub = publickey
#                    self.session._session.userauth_publickey_fromfile(credential.username, publickey, privatekey, credential.password)
                elif isinstance(credential, UsernamePassword):
                    self.session.setPassword( credential.password )
                else:
                    raise Exception("wrong type of credential")
                
                self.session.connect()
            except java.lang.Exception, e:
#                self.log.info("Connection error")
                print "Connection Error"
                print "Exc:%s" % e
                self.session = None
                self.channel = None
                #raise e
            
#            self.channel = self.session.open_session()
        
    def execute(self):
        '''
        @summary: invokes the remote command to run. The standard output of the command is stored in the result variable.
        '''
        with self.lock:
#            self.log.info("Execute:%s" % self.command)
            if self.session is None: return StringIO("")
            self.channel = self.session.openChannel("exec")
            self.channel.setCommand(self.command)
            self.channel.setInputStream(None)

            stdo = self.channel.getInputStream()
#            br = BufferedReader( InputStreamReader( stdo ) )
            self.channel.connect()

            return FileUtil.wrap( stdo )

#            buffer = 4096
#            buf = zeros(1024,'b')
#            while True:
#                while stdo.available()>0:
#                    i=stdo.read(buf,0,1024)
#                    if i<0: break
#                    self._result += str(String(buf,0,i))
#                if channel.isClosed(): break
#            channel.disconnect()
#            return StringIO(self._result)

#    def _get_result(self):
#        '''
#        @summary: the copy of the standard output of the remote command
#        @return: the standard output of the remote command
#        @rtype: string
#        '''
#        return str(self._result)

    def close(self):
        '''
        @summary: the destructor takes care of closing the session and removing the public key file stored temporary
        '''
        with self.lock:
            if self.channel is not None:
                self.channel.disconnect()
                self.channel = None
            if self.session is not None:
                self.session.disconnect()
#            if self.fn_pub is not None:
#                unlink(self.fn_pub)

#    result = property(_get_result,None,None)

    def __del__(self):
        self.close()

    def _isConnected(self):
        try:
            if self.channel is not None: return True
            else: return False
        except:
            return False

    isConnected = property(_isConnected,None, None)


