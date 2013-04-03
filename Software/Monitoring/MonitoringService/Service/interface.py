'''
Created on 08.08.2011

@author: steger, jozsef
'''
from Service.MonitoringService import MonitoringService
import logging

class InterfaceError(Exception):
    pass

#TODO: add and handle bindings at this level
class MSInterface(object):
    '''
    @summary: Implements a thin service layer on top of the MonitoringService instance 
    to collect methods that need to be exported and mapped in the NOVI API.
    It also provides a reference to the framework to be able to communicate with
    remote MonitoringService instances.  The log() method is a place holder 
    to sink information to be pushed in the NOVI UserFeedback service.
    The emit() method is a place holder to sink signals to be pushed in the NOVI
    Policy Service component installed on top of the same platform.
    '''

    def __init__(self, framework, reference, config_owl):
        '''
        Constructor
        @param framework: a service which provides getService() method to look up MonSrv instances of different reference
        @type framework: Framework   
        @param reference: the name of the platform
        @type reference: str
        @param config_owl: platform specific configuration model
        @type config_owl: str  
        '''
        self._framework = framework
        self.platform = reference
        self._ms = MonitoringService(self, config_owl)
        self.logger = logging.getLogger(name = "NOVI.MSI.%s" % reference)

    @property
    def service(self):
        '''
        @return: the underlying monitoring service component
        @rtype: MonitoringService
        '''
        return self._ms
    
    @property
    def proxy(self):
        '''
        @return: a proxy service to look up the rest of the monitoring service components
        @rtype: Framework
        '''
        return self._framework

    def dispatchID(self, identifier):
        '''
        @summary: this method finds the MonitoringService instance that is responsible for handling an identified Task or Aggregate
        @param identifier: identifier of a task or aggregate, it follows the form: <platform>:<process|aggregate>:<id>
        @type identifier: string
        @return: the monitoring service instance
        @rtype: MonitoringService 
        '''
        try:
            platform, _, _ = identifier.split(':')
            if self.platform == platform:
                return self.service
            return self.framework.getService(platform)
        except ValueError:
            raise InterfaceError("Wrong identifier format")

    def log(self, shortmsg, message):
        # overridden by the JAVA wrapper
        self.logger.info("[%s] %s" % (shortmsg, message))
    
    def emit(self, what):
        # overridden by the JAVA wrapper
        self.framework.getPolicyService(self.platform).trigger(what)

    # Test purpose function
    def echo(self, platform):
        '''
        @summary: An integration tester function (to be exported public)
        @param platform: name of the platform
        @type platform: string
        @return: messages of the platforms taking part in the message flow
        @rtype: string
        '''
        self.logger.info("[echo] calling %s" % platform)
        otherservice = self._framework.getInterface(platform).service
        return "%s -> %s" % (str(self.service), str(otherservice))


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
        #TODO: split query and concatenate results
        return self.service.measure(credential, query)
    
    # Slice monitoring functions
    def sliceTasks(self, credential, query):
        raise InterfaceError("sliceTasks() method is not implemented")
    
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
        return self.service.launchTasks(credential, query)
    
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
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        #TODO: move this in the MonitoringService
        headers = map(lambda x: x.header(), ms.formatters[taskID])
        return "[%s]" % "\n,\n".join(headers)
    
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
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        #TODO: move this in the MonitoringService
        response = []
        try:
            for f in ms.formatters[taskID]:
                response.append( f.serialize() )
        except Exception, e:
            print "EEE", e
            pass
        return "[%s]" % "\n,\n".join(response)
    
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
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        #TODO: move this in the MonitoringService
        try:
            subtaskids = ms.subtaskIDs.pop(taskID)
            ms.formatters.pop(taskID)
            while len(subtaskids):
                subtaskid = subtaskids.pop()
                ms.delTask(taskidentifier = subtaskid)
        except KeyError:
            # the taskID does not belong to me
            pass

    def enableTask(self, credential, query):
        '''
        @summary: Method to enable a slice measurement task (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        try:
            for subtaskid in ms.subtaskIDs[taskID]:
                t = ms.getTask(taskidentifier = subtaskid)
                t.enable()
        except KeyError:
            # the taskID does not belong to me
            pass

    def disableTask(self, credential, query):
        '''
        @summary: Method to disable a slice measurement task temporarily (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        try:
            for subtaskid in ms.subtaskIDs[taskID]:
                t = ms.getTask(taskidentifier = subtaskid)
                t.disable()
        except KeyError:
            # the taskID does not belong to me
            pass
    
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
        taskID = query
        ms = self.dispatchID(identifier = taskID)
        try:
            for subtaskid in ms.subtaskIDs[taskID]:
                t = ms.getTask(taskidentifier = subtaskid)
                if t.state == t.STATE_RUNNING:
                    return True
        except KeyError:
            # the taskID does not belong to me
            pass
        return False
    
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
        return self.service.attachAggregators(credential, query)
    
    def removeAggregator(self, credential, query):
        '''
        @summary: Method to remove data manipulation on slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        aggregatorID = query
        ms = self.dispatchID(identifier = aggregatorID)
        try:
            aggregatorids = ms.aggregatorIDs.pop(aggregatorID)
            ms.formatters.pop(aggregatorID)
            while len(aggregatorids):
                aggregatorid = aggregatorids.pop()
                ms.delAggregator(aggregatorid)
        except KeyError:
            # the aggregatorID does not belong to me
            pass
    
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
        aggregatorID = query
        ms = self.dispatchID(identifier = aggregatorID)
        response = []
        try:
            for f in ms.formatters[aggregatorID]:
                f.source.process()
                print "ALMA", f.source
                print "ALMA", f.source.source
                print "ALMA", f.source.data
                response.append( f.serialize() )
        except Exception, e:
            print "EEE", e
            pass
        return "[%s]" % "\n,\n".join(response)
    
    def addWatchdog(self, credential, query):
        '''
        @summary: 
        @param credential: 
        @type credential:
        @param query: an owl document containing several SampleManipulationQuery instances
        @type query: string 
        @return: watchdog identifier
        @rtype: string
        '''
        #TODO: investigate if the service instance under this interface should be the boss
        return self.service.attachWatchdogs(credential, query)
    
    def removeCondition(self, credential, query):
        '''
        @summary: Method to remove conditions bound to slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        watchdogID = query
        ms = self.dispatchID(identifier = watchdogID)
        try:
            watchdogids = ms.watchdogIDs.pop(watchdogID)
            while len(watchdogids):
                watchdogid = watchdogids.pop()
                ms.delWatchdog(watchdogid)
        except KeyError:
            # the aggregatorID does not belong to me
            pass

    def checkCondition(self, credential, query):
        '''
        @summary: Method to examine a conditions bound to slice monitoring data (to be exported public)
        @param credential: 
        @type credential:
        @param query: 
        @type query: string 
        '''
        watchdogID = query
        ms = self.dispatchID(identifier = watchdogID)
        return ms.checkWatchdog(watchdogID)
