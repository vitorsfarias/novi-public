'''
Created on Aug 10, 2011

@author: steger
'''
from Credential.credentialtypes import UsernamePassword, UsernameRSAKey
from DataProcessing.Data import DataHeader, DataHeaderCell
from DataProcessing.Parameter import ParameterList, Parameter
from Driver.SOAPClient import SOAPClient
from Driver.SshExec import SshExec
from Driver.LocalExec import LocalExec
from Driver.REST import RESTDriver

class TaskModelError(Exception):
    pass

class TaskModel(object):
    '''
    classdocs
    '''
    hooklookup = {
        'hasPreHook' : 'prehook', 
        'hasStartHook' : 'starthook',
        'hasRetrieveHook' : 'retrievehook',
        'hasStopHook' : 'stophook',
        'hasPostHook' : 'posthook',
        }
    typelookup = {
        'Integer': int,
        'Float': float,
        'String': str
        }

    def __init__(self, dimensionmanager, unitmanager, ontology):
        '''
        @summary: constructor
        @param dimensionmanager: the container to form a cell's dimension
        @type dimensionmanager: DimensionManager 
        @param unitmanager: the container to form a cell's unit
        @type unitmanager: UnitManager 
        @param ontology: the basic knowledge
        @type ontology: Ontology 
        '''
        self.ontology = ontology
        self.dm = dimensionmanager
        self.um = unitmanager
        
    def inferDomains(self):
        '''
        @summary: extract the monitoring domains from the information model
        @return: generator of the list of domains
        @rtype: URIRef 
        '''
        for uri_domain, _, _ in self.ontology.triples((None, self.ontology.ns('rdf')['type'], self.ontology.ns('task')['MonitoringDomain'])):
            yield uri_domain

    def inferTasks(self, domain, feature):
        '''
        @summary: provides a generator to crawl over the tasks that can measure a given feature in the given domain of interest
        @param domain: domain of interest
        @type domain: URIRef
        @param feature: the feature to measure
        @type feature: URIRef
        @return: a generator of the list of (task reference, task name) pairs 
        @rtype: (URIRef, str)
        '''
        q = """
SELECT ?task ?name
WHERE {
 ?task a owl:NamedIndividual ;
       a task:MonitoringTask ;
       task:name ?name ;
       task:hasMonitoringDomain task:%s ;
       task:hasOutputTableFormat ?data .
?data  task:hasColumn ?col .
?col   task:hasMonitoredFeature feature:%s
}
        """ % (self.ontology._tail(domain), self.ontology._tail(feature))
        for uri_task, tname in self.ontology.query(q):
            yield uri_task, str(tname)

    def inferCredentialOf(self, task):
        '''
        @summary: extracts the set of acceptable credential templates the given task accepts
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: a set of an uninitialized Credential classes
        @rtype: set(Credential)
        @raise IMError: Unknown authentication type
        '''
        creds = set()
        for (_, _, auth) in self.ontology.triples((task, self.ontology.ns('task')['hasAuthenticationType'], None)):
            if auth == self.ontology.ns('task')["UsernamePassword"]:
                creds.add(UsernamePassword)
            elif auth == self.ontology.ns('task')["UsernameRSAKey"]:
                creds.add(UsernameRSAKey)
            else:
                raise TaskModelError("Unknown authentication type %s" % auth)
        return creds

    def inferDriverOf(self, task):
        '''
        @summary: extarcts the driver of the task
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: the appropriate driver class uninstantiated
        @rtype: Driver
        @raise IMError: Unknown driver type / hasDriver missing
        '''
        try:
            _, _, driver = self.ontology.triples((task, self.ontology.ns('task')['hasDriver'], None)).next()
            if driver == self.ontology.ns('task')["SOAPClient"]:
                return SOAPClient
            elif driver == self.ontology.ns('task')["SSH"]:
                return SshExec
            elif driver == self.ontology.ns('task')["LocalExec"]:
                return LocalExec
            elif driver == self.ontology.ns('task')["REST"]:
                return RESTDriver
            else:
                raise TaskModelError("Unknown driver type %s" % driver)
        except StopIteration:
            raise TaskModelError("hasDriver is missing for task %s" % task)

    def inferHookparametersOf(self, task):
        '''
        @summary: extract the necessary control parameters for task initialization
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: a lookup table of arguments, which are passed to the Task object's prehook method as keyword arguments
        @rtype: dict
        '''
        q = """
SELECT ?name ?value ?type
WHERE {
 config:%s task:hasHookParameter ?p .
 ?p param:paramName ?name ;
    a owl:NamedIndividual ;
    rdf:type task:HookParameter ;
    unit:hasValue ?value ;
    param:hasType ?type .
}
        """ % (self.ontology._tail(task))
        d = {}
        for pname, pvalue, ptype in self.ontology.query(q):
            pname = str(pname)
            if ptype == self.ontology.ns('param')["Integer"]:
                d[pname] = int(pvalue)
            elif ptype == self.ontology.ns('param')["Float"]:
                d[pname] = float(pvalue)
            else:
                d[pname] = str(pvalue)
        return d
    
    def inferHookdefinitionsOf(self, task):
        '''
        @summary: extract the hook implementation details for task initialization
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: a lookup table of hook definitions
        @rtype: dict
        '''
        q = """
SELECT ?rel ?value
WHERE {
 config:%s ?rel ?h .
 ?h task:hookCode ?value .
}
        """ % (self.ontology._tail(task))
        d = {}
        for hrel, hvalue in self.ontology.query(q):
            hook = self.ontology._tail(uriref = hrel)
            d[self.hooklookup[hook]] = str(hvalue).replace('\\n', '\n').replace('\\t', '\t').replace('\\\\', '\\').strip()
        return d
    
    def inferDataheaderOf(self, task):
        '''
        @summary: extract the data header declaration the for task
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: an initialized DataHeader instance
        @rtype: DataHeader
        '''
        q = """
SELECT ?tablename ?colname ?dim ?feature ?unit ?prefix
WHERE {
 config:%s task:hasOutputTableFormat ?hdr .
 ?hdr task:name ?tablename .
 ?hdr task:hasColumn ?col .
 ?col task:name ?colname ;
      a owl:NamedIndividual ;
      a ?dim ;
      task:sequenceNumber ?seqno .
 {
  ?dim rdfs:subClassOf unit:BaseDimension .
 } UNION {
  ?dim rdfs:subClassOf ?p .
  ?p rdfs:subClassOf unit:DerivedDimension .
 }
 OPTIONAL {
  ?col task:hasMonitoredFeature ?feature .
 }
 OPTIONAL {
  ?col unit:hasUnit ?unit .
  OPTIONAL {
   ?col unit:hasPrefix ?prefix .
  }
 }
}
ORDER BY ?seqno
        """ % (self.ontology._tail(task))
        datahdr = None
        for tablename, colname, uri_dim, uri_feature, uri_unit, uri_prefix in self.ontology.query(q):
            if datahdr is None:
                datahdr = DataHeader(str(tablename))
            if uri_unit is None:
                u = None
            elif uri_prefix is None:
                u = self.um[ self.ontology._tail(uri_unit) ]
            else:
                ref = "%s_%s" % (self.ontology._tail(uri_prefix), self.ontology._tail(uri_unit))
                u = self.um[ref]
            d = self.dm[ self.ontology._tail(uri_dim) ]
            if uri_feature is None:
                cell = DataHeaderCell(name = str(colname), dimension = d, unit = u)
            else:
                cell = DataHeaderCell(name = str(colname), dimension = d, feature = uri_feature, unit = u)
            datahdr.addColumn(cell)
        return datahdr

    def inferParametersOf(self, task):
        '''
        @summary: extract the parameter list for the given task
        @param task: reference to the monitoring task
        @type task: URIRef
        @return: an initialized list of the parameters of the task
        @rtype: ParameterList
        '''
        q = """
SELECT ?name ?ptype ?dim ?defval ?unit ?prefix
WHERE {
 config:%s task:hasExecutionParameter ?par .
 ?par param:paramName ?name ;
      param:hasType ?ptype ;
      a ?dim .
 {
  ?dim rdfs:subClassOf unit:BaseDimension .
 } UNION {
  ?dim rdfs:subClassOf ?p .
  ?p rdfs:subClassOf unit:DerivedDimension .
 }      
 OPTIONAL {
  ?par unit:hasValue ?defval .
  OPTIONAL {
   ?par unit:hasUnit ?unit .
   OPTIONAL {
    ?par unit:hasPrefix ?prefix .
   }
  }
 }
}
        """ % (self.ontology._tail(task))
        paramlist = ParameterList()
        for name, uri_ptype, uri_dim, default, uri_unit, uri_prefix in self.ontology.query(q):
            p = self.translateParameter(str(name), uri_dim, uri_unit, uri_prefix, uri_ptype, default)
            paramlist.append(p)
        return paramlist

    def translateParameter(self, name, uri_dim, uri_unit, uri_prefix, uri_ptype, default = None):
        '''
        @summary: helper method to instantiate a Parameter
        @param name: the reference name of the parameter
        @type name: str 
        @param uri_dim: the dimension of the parameter
        @type uri_dim: URIRef 
        @param uri_unit: the unit of the parameter, if None we fall back to the unit of the dimension
        @type uri_unit: URIRef 
        @param uri_prefix: accounts only if uri_unit is not None
        @type uri_prefix: URIRef 
        @param uri_ptype: the type of the parameter to use for serialization
        @type uri_ptype: URIRef 
        @param default: the parameter value to initialize with, if None, parameter won't hol a value
        @type default: Literal 
        @return: a parameter
        @rtype: Parameter
        '''
        vt = self.typelookup[ self.ontology._tail(uri_ptype) ]
        d = self.dm[ self.ontology._tail(uri_dim) ]
        if default is None:
            return Parameter(name = name, valuetype = vt, unitmanager = self.um, dimension = d)
        else:
            if uri_unit is None:
                if uri_prefix is None:
                    u = d.unit
                else:
                    ref = "%s_%s" % (self.ontology._tail(uri_prefix), d.unit.reference)
                    u = self.um[ref]
            else:
                if uri_prefix is None:
                    u = self.um[ self.ontology._tail(uri_unit) ]
                else:
                    ref = "%s_%s" % (self.ontology._tail(uri_prefix), self.ontology._tail(uri_unit))
                    u = self.um[ref]
            return Parameter(name = name, valuetype = vt, unitmanager = self.um, dimension = d, default = (vt(default), u))
