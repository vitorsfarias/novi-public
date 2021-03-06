from __future__ import with_statement
'''
Created on Mar 22, 2012

@author: steger
'''
from time import sleep
from DataProcessing.Prefix import PrefixManager
from DataProcessing.Unit import UnitManager
from DataProcessing.Dimension import DimensionManager
from Semantics.InformationModel import Ontology
from Semantics.UnitModel import UnitModel
from Semantics.TaskModel import TaskModel
from Semantics.QueryInterpreter import QueryInterpreter
from Task.Task import SubtaskManager, TaskError, STRAT_PERIODICAL,\
    STRAT_ONDEMAND
from DataProcessing.Parameter import ParameterList
from Resource.node import node
from Resource.link import link
from DataProcessing.AggregatorManager import AggregatorManager
from DataProcessing.MeasurementLevel import lut_level
#from paramiko.ssh_exception import BadAuthenticationType
import logging
from rdflib import Graph
from StringIO import StringIO
from DataProcessing.DataFormatter import JsonFormatter
from DataProcessing.DataHeaderCell import CellRequestByFeature,\
    CellRequestByName
from DataProcessing.DataError import SamplerError
import traceback

class MonitoringService(object):
    '''
    classdocs
    '''
    version = "0.0"

    def __str__(self):
        return "NOVI Monitoring Service v%s @ %s" % (self.version, self.platform)
    
    def _get_platform(self):
        return self._if.platform
    
    def __init__(self, interface, baseurl, config_owl):
        '''
        @summary: constructor
        @param interface:
        @type interface:  MSInterface
        @param baseurl: the location of the ontology files. Either poin to the file system or to a public url
        @type baseurl: str
        @param config_owl: platform specific configuration model
        @type config_owl: str  
        '''
        self._if = interface
        self.logger = logging.getLogger(name = "NOVI.MS.%s" % self.platform)
        self.log = self._if.log         # to be removed
        self.pm = PrefixManager()
        self.um = UnitManager()
        self.dm = DimensionManager(self.um)
        self.stm = SubtaskManager(self.um)
        self.am = AggregatorManager()
        self.domains = []
        self.features = []
        self.ontology = Ontology(baseurl = baseurl, config_owl = config_owl)
        self.unitmodel = UnitModel(self.ontology)
        self.taskmodel = TaskModel(self.dm, self.um, self.ontology)
        um = self.unitmodel

        # infer and store prefixes
        for (p_reference, p_symbol, base, exponent) in um.inferPrefixes():
            self.pm.newPrefix( self.ontology._tail(p_reference), p_symbol, base, exponent )
        
        # infer basic units
        for u_reference, u_symbol in um.inferBaseUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, _, _ in um.inferPowerUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, _ in um.inferProductUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, derivedfrom, scale, offset in um.inferLinearTransformedUnits():
            self.storeLinearTransformedUnit(u_reference, u_symbol, derivedfrom, scale, offset)
        for u_reference, u_symbol, derivedfrom, expr_fwd, expr_inv in um.inferRegexpTransformedUnits():
            uref = self.ontology._tail(u_reference)
            ancestor = self.um[ self.ontology._tail(derivedfrom) ]
            self.um.addRegexpTransformedUnit(uref, u_symbol, ancestor, expr_fwd, expr_inv)

        # infer dimensions
        #FIXME: if there is a reference loop an error is raised...
        for d_reference, u_reference, level in um.inferBaseDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            lref = self.ontology._tail(level)
            level = lut_level[lref]
            unit = self.um[uref]
            self.dm.newBaseDimension(dref, dref, unit, level)
        for d_reference, u_reference, d_derivedfrom in um.inferDifferenceDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.DifferenceDimension)
        for d_reference, u_reference, d_derivedfrom, exponent in um.inferPowerDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.PowerDimension, exponent = exponent)
        for d_reference, u_reference, d_derivedfrom in um.inferProductDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            unit = self.um[uref]
            derivedfrom = tuple( self.dm[self.ontology._tail(x)] for x in d_derivedfrom )
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.ProductDimension)
        for d_reference, u_reference, d_derivedfrom in um.inferRatioDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.RatioDimension)

        # infer domains and features
        for uri_domain in self.taskmodel.inferDomains():
            self.domains.append(uri_domain)
        for uri_feature, _, _ in self.taskmodel.inferFeatures():
            self.features.append(uri_feature)
        
        self.QI = QueryInterpreter(self.taskmodel)

        self._nextID = 0
        self.subtaskIDs = {}
        self.aggregatorIDs = {}
        self.formatters = {}


    def storeBasicUnit(self, u_reference, u_symbol):
        uref = self.ontology._tail(u_reference)
        bu = self.um.newBasicUnit(uref, u_symbol)
        for p_reference in self.unitmodel.inferPossiblePrefixesOf(u_reference):
            p = self.pm[ self.ontology._tail(p_reference) ]
            puref = "%s_%s" % (p.reference, uref)
            symbol = "%s%s" % (p.symbol, bu.symbol)
            self.um.addLinearTransformedUnit(puref, symbol, bu, p.scale)

    def storeLinearTransformedUnit(self, u_reference, u_symbol, derivedfrom, scale, offset):
        uref = self.ontology._tail(u_reference)
        ancestor = self.um[ self.ontology._tail(derivedfrom) ]
        bu = self.um.addLinearTransformedUnit(uref, u_symbol, ancestor, scale, offset)
        for p_reference in self.unitmodel.inferPossiblePrefixesOf(u_reference):
            p = self.pm[ self.ontology._tail(p_reference) ]
            puref = "%s_%s" % (p.reference, uref)
            symbol = "%s%s" % (p.symbol, bu.symbol)
            self.um.addLinearTransformedUnit(puref, symbol, bu, p.scale)

    def newProcessID(self):
        try:
            return "%s:process:%d" % (self.platform, self._nextID)
        finally:
            self._nextID += 1

    def newAggregateID(self, isprocess = True):
        try:
            return "%s:aggregate:%d" % (self.platform, self._nextID)
        finally:
            self._nextID += 1

    def measure(self, credential, query):
        #TODO: docs
        '''
        '''
        g = Graph()
        g += self.ontology.graph
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
              try:
                no_tool = False
                _, task = self.newTask(task = task_uri, cred = credential, resource = resource, parameters = q.paramlist)
                if task is None:
                    continue
                if q.samplechain:
                    task.strategy = STRAT_PERIODICAL
                    # we apply some aggregation to the data
                    flow = []
                    for skeleton, parlist in q.samplechain:
                        flow.append((skeleton, parlist.formkeyvaldict()))
                    aid = self.am.newAggregator(task.data, CellRequestByFeature(feature = q.feature), flow)
                    A = self.am[aid]
                    task.enable()
                    while True:
                        try:
                            task.dataAdded.wait( 15 )
                            formatter = JsonFormatter(datasource = A.data)
                            break
                        except SamplerError:
                            task.dataAdded.clear()
                            sleep(1)
                else:
                    task.strategy = STRAT_ONDEMAND
                    task.enable()
                    task.dataAdded.wait( 15 )
                    formatter = JsonFormatter(datasource = task.data)
                    formatter.extract(cellrequest = [
                             CellRequestByName(name = "Run"),
                             CellRequestByFeature(feature = feature_uri)
                         ])
                t = formatter.serialize()
                try:
                    print "Call task.destroy"
                    task.destroy()
                except:
                    pass
                #print "retek",t
                if t is not None:
                   if len(t)>0:
                      responses.append( "{\"%s\" : %s}" %(feature_uri,t) ) #formatter.serialize() )
              except Exception, e:
                tbe = traceback.format_exc()
                err_desc = "Unexpected exception occured: %s, %s" % (e, tbe)
                errors.append(err_desc)
            if no_tool:
                err_description = "No tools to measure %s @ %s" % (feature_uri, resource_uri) 
                errors.append(err_description)
                self.log(shortmsg = "Limited result set", message = err_description)
        useful_data = ",\n".join( responses )
        error_data = "+".join(errors)
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
        g = Graph()
        g += self.ontology.graph
        sio = StringIO(query)
        g.parse(source = sio)
        taskID = self.newID()
        idstore = self.subtaskIDs[taskID] = []
        formatters = self.formatters[taskID] = []
        for q in self.QI.getBundleQuery(qgraph = g):
            feature_uri = q.feature
            
            print "PPPPP", q.paramlist
            
            domain = self.ontology.ns('task')['Slice']
            taskgen = self.taskmodel.inferTasks(domain, feature_uri)
            #we are ugly here: use the first tool
            for task_uri, _ in taskgen:
                subtaskID, task = self.newTask(task = task_uri, cred = credential, resource = q.resource, parameters = q.paramlist)
                task.strategy = STRAT_PERIODICAL
                task.enable()
                idstore.append(subtaskID)
                f = q.formatter(datasource = task.data)
                formatters.append(f)
        if len(idstore):
            return taskID
        else:
            self.subtaskIDs.pop(taskID)
            self.formatters.pop(taskID)
        return None


    platform = property(_get_platform,None,None)


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
        elif isinstance(resource, link):
            addr, unit = resource.source.address
            taskparameters.update("SourceAddress", addr, unit)
            addr, unit = resource.destination.address
            taskparameters.update("DestinationAddress", addr, unit)

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
                except Exception, e:
                    print "Exception - %s" % e
                    pass
        return None, None
        #raise TaskError("Cannot initialize the Task with the credential set provided for %s" % name)

    def delTask(self, taskidentifier):
        self.stm.pop( taskidentifier )

    def getTask(self, taskidentifier):
        return self.stm[ taskidentifier ]
    
    def attachAggregators(self, credential, query):
        g = Graph()
        g += self.ontology.graph
        sio = StringIO(query)
        g.parse(source = sio)
        aggregatorID = self.newID()
        idstore = self.aggregatorIDs[aggregatorID] = []
        formatters = self.formatters[aggregatorID] = []
        raise Exception("unimplemented")
#        for q in self.QI.getBundleQuery(qgraph = g):
#            feature_uri = q.feature
#            
#            print "PPPPP", q.paramlist
#            
#            domain = self.ontology.ns('task')['Slice']
#            taskgen = self.taskmodel.inferTasks(domain, feature_uri)
#            #we are ugly here: use the first tool
#            for task_uri, _ in taskgen:
#                subtaskID, task = self.newTask(task = task_uri, cred = credential, resource = q.resource, parameters = q.paramlist)
#                task.strategy = STRAT_PERIODICAL
#                task.enable()
#                idstore.append(subtaskID)
#                f = q.formatter(datasource = task.data)
#                formatters.append(f)
        if len(idstore):
            return aggregatorID
        else:
            self.subtaskIDs.pop(aggregatorID)
            self.formatters.pop(aggregatorID)
        return None

    def newAggregator(self):
        pass

    def delAggregator(self, aggregatoridentifier):
        self.am.pop( aggregatoridentifier )

    def getAggregator(self, aggregatoridentifier):
        return self.am[ aggregatoridentifier ]
    
