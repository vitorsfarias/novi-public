'''
Created on 08.08.2011

@author: Sandor Laki
'''

from __future__ import with_statement
from rdflib import Graph
from StringIO import StringIO
from Service.interface import MSInterface
from eu.novi.monitoring import MonDiscoveryImpl
import sys
from Util.MonitoringQueryImpl import MonitoringQueryImpl
from threading import Lock
from org.slf4j import Logger
from org.slf4j import LoggerFactory
from os import path, access, R_OK
#import eu.novi.feedback.event.ReportEvent
import traceback
import java.lang.StackOverflowError
import java.lang.Error

try:
    import site
    site.addsitedir('../site-packages')
except ImportError, e:
    sys.stderr.write("[EXCEPTION] import Site -> %s\n" % e)



try:
    from eu.novi.monitoring import MonSrv
except ImportError:
    MonSrv = object

try:
    from eu.novi.monitoring import Wiring
except ImportError:
    Wiring = object

try:
    from eu.novi.im.core import Resource
except ImportError, e:
    sys.stderr.write("[EXCEPTION] Resource -> %s\n" % e)
    Resource = None


class MonSrvImpl(MonSrv,Wiring):
    testbed = "Undefined"
    userFeedback = None
    lock = Lock()

    log = LoggerFactory.getLogger("eu.novi.monitoring.MonSrv")

    def __init__(self):
        #self.testbed = "Unknown"
        self._msi = None
        self.framework = MonDiscoveryImpl()
        self.log.info("MonSrvImpl has started... Testbed=%s" % self.testbed)

    def createQuery(self):
        return MonitoringQueryImpl(self.getMSI()._ms)

    def getMSI(self):
        print "getMSI %s" % self.getTestbed()
        self.log.info("getMSI %s" % self.getTestbed())
        tbname = self.getTestbed()
        with self.lock:
            if self._msi is None:
               baseurl = ""
               config_owl = "config_%s.owl" % (tbname.lower())
               self.log.info("Testbed specific configuration: %s" % config_owl)
               #config_owl = "config_planetlab.owl"
               try:
                   self._msi = MSInterface(self.framework, self.getTestbed(), baseurl, config_owl)
               except:
                   self.log.info("Error occured at %s" % config_owl)
                   config_owl = "config_planetlab.owl"
                   self._msi = MSInterface(self.framework, self.getTestbed(), baseurl, config_owl)
               self.log.info("MSInterface has been instanciated... Testbed=%s" % self.getTestbed() )
        return self._msi


    def setPolicy(self, policy):
        self.policy = policy

    def getPolicy(self):
        return self.policy

    def getPlatform(self):
        return self.testbed

    def setResource(self, resource):
        self.resource = resource

    def getResource(self):
        return self.resource

    def getTestbed(self):
        return self.testbed

    def setTestbed(self, testbed):
        self.testbed = testbed

    def getUserFeedback(self):
        return self.userFeedback

    def setUserFeedback(self, userFeedback):
        self.userFeedback = userFeedback

   # Test purpose function
    def echo(self, platform):
        '''
        @summary: An integration tester function (to be exported public)
        @param platform: name of the platform
        @type platform: string
        @return: messages of the platforms taking part in the message flow
        @rtype: string
        '''
        return self.getMSI().echo(platform)

    def extractCredential(self, credential):
        cred = []
        if credential.getType()=="UsernamePassword": cred=[{'username' : credential.username, 'password' : credential.password}]
        elif credential.getType()=="UsernameRSAKey": cred=[{'username' : credential.username, 'password' : credential.password, 'rsakey' : credential.RSAKey}]
        else: return "Error - unknown credential...."

        # Hardcoded credential - TODO: FIX IT ASAP!!!
        PATH="/home/novi/apache-servicemix-4.4.1-fuse-01-06/instances/system-tests/etc/root_planetlab_rsa"
 
        try:
            #PATH="/home/novi/apache-servicemix-4.4.1-fuse-01-06/instances/system-tests/etc/sfademo_key"
            if path.exists(PATH) and path.isfile(PATH) and access(PATH, R_OK):
                cred=[{'username' : "root", 'password' : "", 'rsakey' : PATH}]
                self.log.info("root path exists and readable")
        except:
            self.log.info("root key cannot be accessed at %s" % PATH)
            if not path.exists(PATH):
                self.log.info("path doesn't exists")
            if not path.isfile(PATH):
                self.log.info("path is not a file")
            if not access(PATH, R_OK):
                self.log.info("file cannot be accessed, permission issue?")
            #pass
        cred.append({'username':'monitor1','password':'m/n.t,r1'}) # G3 Access
        return cred


    # Substrate monitoring function
    def measure(self, credential, query):
        '''
        @summary: Method to handle substrate monitoring queries (to be exported public)
        @param credential: 
        @type credential:
        @param query: an owl document containing several BundleQuery instances
        @type query: string 
        @return: response to the query
        @rtype: string
        '''
        cred = self.extractCredential( credential )
        self.log.info("New substrate monitoring query has arrived: %s" % query)
        try:
            print "Call measure"
            #TODO: split query and concatenate results
            return self.getMSI().measure(cred, query)
        except Exception, e:
            self.log.info("Exception %s %s" % (e, traceback.format_exc()))
        except java.lang.StackOverflowError, se:
            se.printStackTrace()
            self.log.info("unknown %s" % se.toString())
        except java.lang.Error, er:
            er.printStackTrace()
        return "[]"

    def substrateFB(self, credential, query, sessionID):
        try:
            self.getUserFeedback().instantInfo(sessionID, "MS", "A substrate monitoring task has been submitted.", "http://fp7-novi.eu");
        except:
            self.log.info("Feedback thrown an exception")
        return self.measure(credential, query)
    
    def substrate(self, credential, query):
        return self.measure(credential, query)

    # Slice monitoring functions
    def sliceTasks(self, credential, query):
        return "sliceTasks() method is not implemented"
    
    def addTask(self, credential, query):
        '''
        @summary: Method to start slice monitoring tasks (to be exported public)
        @param credential: 
        @type credential:
        @param query: an owl document containing several BundleQuery instances
        @type query: string 
        @return: process identifier
        @rtype: string
        '''
        #TODO: investigate if the service instance under this interface should be the boss
        cred = self.extractCredential( credential )
        return self.getMSI().launchTasks(cred, query)
    
    def describeTaskData(self, credential, query):
        '''
        @summary: Method to retrieve meta data of task data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        @return: serialize the header of the data tables
        @rtype: string
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().describeTaskData(cred, query)
        
   
    def fetchTaskData(self, credential, query):
        '''
        @summary: Method to retrieve task data collected since last fetch or the start (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        @return: serialize the appended content of the data tables
        @rtype: string
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().fetchTaskData(cred, query)
   
    def modifyTask(self, credential, query):
        raise InterfaceError("modifyTask() method is not implemented")
    
    def removeTask(self, credential, query):
        '''
        @summary: Method to remove a slice measurement task (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().removeTask(cred, query)
 
    def enableTask(self, credential, query):
        '''
        @summary: Method to enable a slice measurement task (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().enableTask(cred, query)
 
    def disableTask(self, credential, query):
        '''
        @summary: Method to disable a slice measurement task temporarily (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().disableTask(cred, query)
   
    def getTaskStatus(self, credential, query):
        '''
        @summary: Method to check the state of a slice measurement task (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        @return: True if the tasks are running
        @rtype: boolean
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().getTaskStatus(cred, query)
   
    def addAggregator(self, credential, query):
        '''
        @summary: Method to define new data manipulation on slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: an owl document containing several SampleManipulationQuery instances
        @type query: string 
        @return: aggregator identifier
        @rtype: string
        '''
        #TODO: investigate if the service instance under this interface should be the boss
        cred = self.extractCredential( credential )
        return self.getMSI().addAggregator(cred, query)
    
    def removeAggregator(self, credential, query):
        '''
        @summary: Method to remove data manipulation on slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().removeAggregator(cred, query)
   
    def fetchAggregatorData(self, credential, query):
        '''
        @summary: Method to refresh and serialize results of data manipulation on slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        @return: result of aggregators
        @rtype: string
        '''
        cred = self.extractCredential( credential )
        return self.getMSI().fetchAggregatorData(cred, query)
   
    def addCondition(self, credential, query):
        raise InterfaceError("addCondition() method is not implemented")
    
    def modifyCondition(self, credential, query):
        raise InterfaceError("modifyCondition() method is not implemented")
    
    def removeCondition(self, credential, query):
        raise InterfaceError("removeCondition() method is not implemented")


