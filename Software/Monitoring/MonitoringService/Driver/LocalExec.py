'''
Created on Feb 4, 2013

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

#TODO: nested command execution is not working properly: e.g.: echo `hostname`

from Driver import Driver
from subprocess import Popen, PIPE

class LocalExec(Driver):
    '''
    @summary: implements a driver to execute local commands
    @ivar command: the default command
    @type command: str
    @ivar p: the process api
    @type p: subprocess.Popen or None
    '''

    def __init__(self, command = "echo -n helloworld"):
        '''
        @summary: save a default command
        @param command: the default command
        @type command: str
        '''
        self.command = command
        self.p = None

    def __del__(self):
        '''
        @summary: an implicit deletion of the driver triggers a kill signal on a running process
        '''
        if self.p:
            self.p.kill()
            self.p = None
        
    def execute(self, command = None):
        '''
        @summary: executes a local command
        @param command: the command to run, if None, the default command is issued
        @type command: str or None
        @return: the standard output of the command run
        @rtype: str
        '''
        if command is None:
            command = self.command
        self.p = Popen(args = command.split(' '), stdout = PIPE, stderr = PIPE)
        stout, sterr = self.p.communicate()
        self.p = None
        self.logger.debug("executed '%s'" % (command))
        if len(sterr):
            self.logger.warning("execution '%s' failed: %s" % (command, sterr))
        return stout
