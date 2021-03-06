'''
Created on Nov 20, 2012

@author: steger
'''
from Service.interface import MSInterface
import logging
from logging.handlers import TimedRotatingFileHandler
from os import path, unlink

class FrameworkError(Exception):
    pass

class PolicyMock(object):
    def __init__(self):
        fn = "/tmp/ps.log"
        if path.exists(fn):
            unlink(fn)
        hdlr = TimedRotatingFileHandler(filename = fn)
        self.logger = logging.getLogger("NOVI.PS")
        self.logger.setLevel(level = logging.DEBUG)
        self.logger.addHandler(hdlr = hdlr)

    def trigger(self, what):
        self.logger.info(what)

class Framework(object):
    '''
    This class mimics the integration framework. It helps to look up remote Monitoring Service instances
    '''

    def __init__(self, baseurl, conf):
        '''
        Constructor
        '''
        self._if = {}
        self._pol = PolicyMock()
        for platform, config_owl in conf.iteritems():
            fn = "/tmp/ms_%s.log" % platform
            if path.exists(fn):
                unlink(fn)
            hdlr = TimedRotatingFileHandler(filename = fn)
            l = logging.getLogger("NOVI.MS.%s" % platform)
            l.setLevel(level = logging.DEBUG)
            l.addHandler(hdlr = hdlr)
            l = logging.getLogger("NOVI.MSI.%s" % platform)
            l.setLevel(level = logging.DEBUG)
            l.addHandler(hdlr = hdlr)
            self._if[platform] = MSInterface(self, platform, baseurl, config_owl)
    
    def _getInterface(self, platform):
        try:
            return self._if[platform]
        except:
            print "EE: %s platform not found" % platform
            raise FrameworkError

    def getService(self, platform):
        try:
            return self._if[platform]
        except:
            print "EE: %s platform not found" % platform
            raise FrameworkError
    
    def _getPolicyService(self, platform):
        return self._pol
        
    def _serviceList(self):
        return self._if.values()
