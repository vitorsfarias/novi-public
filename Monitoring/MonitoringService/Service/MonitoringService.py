'''
Created on Mar 22, 2012

@author: steger
'''
from time import sleep
from Semantics.InformationModel import Ontology
from Semantics.UnitModel import UnitModel
from Semantics.TaskModel import TaskModel
from Semantics.QueryInterpreter import QueryInterpreter
from Task.Task import SubtaskManager, TaskError, STRAT_PERIODICAL,\
    STRAT_ONDEMAND
from DataProcessing.Parameter import ParameterList
from Resource.node import node
from DataProcessing.AggregatorManager import AggregatorManager
from paramiko.ssh_exception import BadAuthenticationType
import logging
from StringIO import StringIO
from DataProcessing.DataFormatter import JsonFormatter
from DataProcessing.DataHeaderCell import CellRequestByFeature,\
    CellRequestByName
from DataProcessing.DataError import SamplerError
from Example.model import owl_unit, owl_param, owl_features, owl_task, owl_query,\
    owl_stat, owl_core
from Service.WatchdogManager import WatchdogManager

ontology = { 
    'unit': (owl_unit, "http://fp7-novi.eu/unit.owl#"),
    'param': (owl_param, "http://fp7-novi.eu/monitoring_parameter.owl#"),
    'feature': (owl_features, "http://fp7-novi.eu/monitoring_features.owl#"),
    'task': (owl_task, "http://fp7-novi.eu/monitoring_task.owl#"),
    'query': (owl_query, "http://fp7-novi.eu/monitoring_query.owl#"),
    'stat': (owl_stat, 'http://fp7-novi.eu/monitoring_stat.owl#'),
    'core': (owl_core, "http://fp7-novi.eu/im.owl#"),
}




class MonitoringService(object):
    '''
    classdocs
    '''
    version = "0.0"

    def __str__(self):
        return "NOVI Monitoring Service v%s @ %s" % (self.version, self.platform)
    
    @property
    def platform(self):
        return self._if.platform
    
    def __init__(self, interface, config_owl):
        '''
        @summary: constructor
        @param interface:
        @type interface:  MSInterface
        @param config_owl: platform specific configuration model
        @type config_owl: str  
        '''
        self._if = interface
        self.logger = logging.getLogger(name = "NOVI.MS.%s" % self.platform)
        self.log = self._if.log         # to be removed
        self.ontology = Ontology()
        for prefix, (owl_url, ns) in ontology.iteritems():
            if owl_url is None:
                continue
            self.ontology.load(prefix, owl_url, ns)
        self.ontology.load('config', config_owl, 'http://fp7-novi.eu/config.owl#')
        self.unitmodel = UnitModel(self.ontology)
        self.stm = SubtaskManager(self.um)
        self.am = AggregatorManager()
        self.wm = WatchdogManager(self.am)
        
        self.taskmodel = TaskModel(self.dm, self.um, self.ontology)
        self.QI = QueryInterpreter(self.taskmodel)

        self._nextID = 0
        self.subtaskIDs = {}
        self.aggregatorIDs = {}
        self.watchdogIDs = {}
        self.formatters = {}

    @property
    def pm(self): return self.unitmodel.pm

    @property
    def um(self): return self.unitmodel.um

    @property
    def dm(self): return self.unitmodel.dm

    
    def newProcessID(self):
        try:
            return "%s:process:%d" % (self.platform, self._nextID)
        finally:
            self._nextID += 1

    def newAggregateID(self):
        try:
            return "%s:aggregate:%d" % (self.platform, self._nextID)
        finally:
            self._nextID += 1

    def newWatchdogID(self):
        try:
            return "%s:watchdog:%d" % (self.platform, self._nextID)
        finally:
            self._nextID += 1


    def measure(self, credential, query):
        #TODO: docs
        '''
        '''
        g = self.ontology.g
        sio = StringIO(query)
        g.parse(source = sio)
        responses = []
        errors = []
        queries = self.QI.inferBundleQueries(qgraph = g)
        self.log(shortmsg = "measurements starting...", message = "Attempt to launch %d measurement threads" % len(queries))
        for q in queries:
            feature_uri = q.feature
            domain = self.ontology.ns('task')['Substrate']
            taskgen = self.taskmodel.inferTasks(domain, feature_uri)
            no_tool = True
            (resource_uri, resource) = q.resource
            #we are ugly here: use the first tool
            for task_uri, _ in taskgen:
                print task_uri
                no_tool = False
                _, task = self.newTask(task = task_uri, cred = credential, resource = resource, parameters = q.paramlist)
                if q.samplechain:
                    task.strategy = STRAT_PERIODICAL
                    task.enable()
                    # we apply some aggregation to the data
                    flow = []
                    for skeleton, parlist in q.samplechain:
                        flow.append((skeleton, parlist.formkeyvaldict()))
                    _, A = self.am.newAggregator(task.datasource, CellRequestByFeature(feature = q.feature), flow)
                    formatter = JsonFormatter(datasource = A)
                    while True:
                        try:
                            task.dataAdded.wait( 15 )
                            responses.append( formatter.serialize() )
                            break
                        except SamplerError:
                            task.dataAdded.clear()
                            sleep(1)
                else:
                    task.strategy = STRAT_ONDEMAND
                    task.enable()
                    task.dataAdded.wait( 15 )
                    formatter = JsonFormatter(datasource = task.datasource)
                    formatter.reader.extract(cellrequest = [
                             CellRequestByName(name = "Run"),
                             CellRequestByFeature(feature = feature_uri)
                         ])
                    responses.append( formatter.serialize() )
                    task.destroy()
            if no_tool:
                err_description = "No tools to measure %s @ %s" % (feature_uri, resource_uri) 
                errors.append(err_description)
                self.log(shortmsg = "Limited result set", message = err_description)
        useful_data = ",\n".join( responses )
        error_data = ",\n".join(errors)
        if len(errors):
            if len(useful_data):
                response = "[%s,\n{\"errors\" : \"%s\"}]" % (useful_data, error_data)
            else:
                response = "[{\"errors\" : \"%s\"}]" % (error_data)
        else:
            response = "[%s]" % useful_data
        return response
    
    def launchTasks(self, credential, query):
        #TODO: many things in common with measure!!!
        g = self.ontology.g
        sio = StringIO(query)
        g.parse(source = sio)
        taskID = self.newProcessID()
        idstore = self.subtaskIDs[taskID] = []
        formatters = self.formatters[taskID] = []
        for q in self.QI.inferBundleQueries(qgraph = g):
            feature_uri = q.feature
            domain = self.ontology.ns('task')['Slice']
            taskgen = self.taskmodel.inferTasks(domain, feature_uri)
            #we are ugly here: use the first tool
            for task_uri, _ in taskgen:
                subtaskID, task = self.newTask(task = task_uri, cred = credential, resource = q.resource[1], parameters = q.paramlist)
                task.strategy = STRAT_PERIODICAL
                task.enable()
                idstore.append(subtaskID)
                f = q.formatter(datasource = task.datasource)
                formatters.append(f)
        if len(idstore):
            print "KONYVELT", taskID
            return taskID
        else:
            self.subtaskIDs.pop(taskID)
            self.formatters.pop(taskID)
        return None

    def newTask(self, task, cred, resource = None, parameters = ParameterList()):
        '''
        @summary: initialize a Task object, which is referenced by a uri
        @param task: the reference to the task description
        @type task: URIRef
        @param cred: an iterable over dictionaries, which are used as input parameters to initialize Credential templates passed to the Task object for authentication, authorization purposes
        @type cred: dict generator
        @param resource: the resource to measure
        @type resource: resource or None
        @param parameters: the parameter list to refresh the default parameters of the Task object
        @type parameters: ParameterList  
        @return: the tuple of taskID and the initialized measurement Task object
        @rtype: int, Task    
        '''
        name = self.ontology._tail(task)
        credset = self.taskmodel.inferCredentialOf(task)
        driver = self.taskmodel.inferDriverOf(task)
        hdr = self.taskmodel.inferDataheaderOf(task)
        hooks = self.taskmodel.inferHookdefinitionsOf(task)
        hookpar = self.taskmodel.inferHookparametersOf(task)
        taskparameters = self.taskmodel.inferParametersOf(task)

        taskparameters.update_by_list(parameters)

        #TODO: maybe better push resource to the Task as an argument        
        if isinstance(resource, node):
            addr, unit = resource.get_ipaddress("eth0")
            taskparameters.update("SourceAddress", addr, unit)          
        else:
            print "EEEEE unhandled resource", resource  
#        print taskparameters

        while len(credset):
            ct = credset.pop()
            for c in cred:
                try:
                    credential = ct(**c)
                except:
                    # credential mismatch go on with the next
                    continue
                try:
                    return self.stm.generate(name = name, driver = driver, dataheader = hdr, 
                                     hookimplementations = hooks, parameters = taskparameters, credential = credential, **hookpar)
                except BadAuthenticationType:
                    pass
        raise TaskError("Cannot initialize the Task with the credential set provided for %s" % name)

    def delTask(self, taskidentifier):
        self.stm.pop( taskidentifier )

    def getTask(self, taskidentifier):
        return self.stm[ taskidentifier ]
    
    def attachAggregators(self, credential, query):
        g = self.ontology.g
        sio = StringIO(query)
        g.parse(source = sio)
        aggregatorID = self.newAggregateID()
        idstore = self.aggregatorIDs[aggregatorID] = []
        formatters = self.formatters[aggregatorID] = []

        for q in self.QI.inferSampleManipulationQueries(qgraph = g):
            _, sourcetype, _ = q.sourceid.split(':')
            if sourcetype == 'process':
                #FIXME: csak egy subprocessre muxik perpill
                sourceid = self.subtaskIDs[q.sourceid][0]

                ds = self.stm[ sourceid ].datasource
            elif sourcetype == 'aggregate':
                sourceid = self.aggregatorIDs[q.sourceid][0]

                ds = self.am[ sourceid ]
            else:
                raise Exception("Unknown source type %s" % sourcetype)
            cr = CellRequestByFeature(feature = q.feature)
            aggID, A = self.am.newAggregator(dataSource = ds, cellrequest = cr, commandflow = q.samplechain)
            idstore.append(aggID)
            f = q.formatter(datasource = A)
            formatters.append(f)
        if len(idstore):
            return aggregatorID
        else:
            self.aggregatorIDs.pop(aggregatorID)
            self.formatters.pop(aggregatorID)
        return None

    def delAggregator(self, aggregatoridentifier):
        self.am.pop( aggregatoridentifier )

    def getAggregator(self, aggregatoridentifier):
        return self.am[ aggregatoridentifier ]
    
    def attachWatchdogs(self, credential, query):
        g = self.ontology.g
        sio = StringIO(query)
        g.parse(source = sio)
        watchdogID = self.newWatchdogID()
        idstore = self.watchdogIDs[watchdogID] = []

        for q in self.QI.inferConditionQueries(qgraph = g):
            _, sourcetype, _ = q.sourceid.split(':')
            if sourcetype == 'process':
                #FIXME: csak egy subprocessre muxik perpill
                sourceid = self.subtaskIDs[q.sourceid][0]

                ds = self.stm[ sourceid ].datasource
            elif sourcetype == 'aggregate':
                sourceid = self.aggregatorIDs[q.sourceid][0]

                ds = self.am[ sourceid ]
            else:
                raise Exception("Unknown source type %s" % sourcetype)
            
            #ITT A TENNIVALO
            cr = CellRequestByFeature(feature = q.feature)
            watchID, _ = self.wm.newConditional(dataSource = ds, cellrequest = cr, conditiontype = q.conditiontype, operation = q.operation)
            
            
            
            
            idstore.append(watchID)
        if len(idstore):
            return watchdogID
        else:
            self.watchdogIDs.pop(watchdogID)
        return None

    def delWatchdog(self, watchdogidentifier):
        self.wm.pop( watchdogidentifier )


    def checkWatchdog(self, watchdogidentifier):
        resp = []
        for watchID in self.watchdogIDs[watchdogidentifier]:
            WD = self.wm[watchID]
            resp.append("\'%s\': %s" % (WD.name, WD.value))
        return "{\n\t%s\n}" % ",\n\t".join(resp)
