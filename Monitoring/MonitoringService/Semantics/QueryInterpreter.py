'''
Created on Feb 21, 2012

@author: steger
'''

from Semantics.Query import QueryBundle, SingleQuery, SingleSampleQuery,\
    SingleConditionQuery
from Resource.node import node
from Resource.interface import interface
from DataProcessing.Aggregator import Max, Min, Percentile, Mean, Deviation
from DataProcessing.Sampler import Tail, Head
from DataProcessing.Parameter import ParameterList
from DataProcessing.Bool import IsPositive, IsNegative

class QueryInterpreter(object):
    '''
    classdocs
    '''
    samplesource = 'UnmodifiedExtractOfFeatureSamples'
    lut_skeleton = {
        'Maximum': Max,
        'Minimum': Min,
        'Percentile': Percentile,
        'Average': Mean,
        'Variance': Deviation,
        'Tail': Tail,
        'Head': Head
    }
    lut_condition = {
        'IsPositive': IsPositive,
        'IsNegative': IsNegative,
        #FIXME: IsNotNegative, IsNotPositive
        'AndExpression': '',
        'OrExpression': '',
    }

    def __init__(self, model):
        '''
        @summary: constructor
        @param model: the task model to resolve the tools
        @type model: TaskModel
        '''
        self.model = model
    
    def getUnitOfDimension(self, ref_dim):
        return self.model.dm[ref_dim].unit

    def getUnit(self, uri_prefix, uri_unit):
        if uri_prefix is None:
            uref = self.model._tail(uri_unit)
        else:
            uref = "%s_%s" % (self.model._tail(uri_prefix), self.model._tail(uri_unit))
        return self.um[uref]

    @property
    def myns(self):
        return self.model.ontology.ns_dict

    def inferInterfacesOf(self, qgraph, uri_node):
        q = """
SELECT ?ifin ?address ?unit ?prefix
WHERE {
 <%s> core:hasInboundInterface ?ifin ;
    core:hasOutboundInterface ?ifout .
 ?ifin a core:Interface ;
       core:hasIPv4Address ?addressobj .
 ?ifout a core:Interface ;
        core:hasIPv4Address ?addressobj .
 ?addressobj a owl:NamedIndividual ;
             a unit:IPAddress ;
             unit:hasValue ?address .
 OPTIONAL {
  ?addressobj unit:hasUnit ?unit .
 }
 OPTIONAL {
  ?addressobj unit:hasPrefix ?prefix .
 }
}
        """ % uri_node
        for uri_ifin, address, uri_unit, uri_prefix in qgraph.query(q, initNs = self.myns):
            name = self.model.ontology._tail(uri_ifin)
            iface = interface(name, resourceid = uri_ifin)
            if uri_unit is not None:
                iface.address = str(address), self.getUnit(uri_prefix, uri_unit)
            else:
                iface.address = str(address), self.getUnitOfDimension('IPAddress')
            iface.direction = iface.EGRESS | iface.INGRESS
            #FIXME: this info should come from the model
            iface.interface = "eth0"
            iface.ispublic = True
            yield iface
        #TODO: similarly look up uni directional interfaces of the node and yield them as well

    def inferBundleQueries(self, qgraph):
        '''
        @summary: 
        '''
        q = """
SELECT ?query ?resource ?feature ?sample ?formatter
WHERE {
 ?query a owl:NamedIndividual ;
        a query:BundleQuery ;
        feature:hasFeature ?feature ;
        stat:hasSample ?sample ;
        query:hasResource ?resource ;
        query:hasFormatter ?formatter .
}
        """
        Q = QueryBundle()
        for uri_query, uri_resource, uri_feature, uri_sample, uri_formatter in qgraph.query(q, initNs = self.myns):
            r = Q.getResource(uri_resource)
            if r is None: 
                r = self.translateResource(qgraph, uri_resource)
            sq = SingleQuery()
            sq.feature = uri_feature
            sq.resource = (uri_resource, r)
            sq.formatter = uri_formatter
            sq.samplechain = self.inferSampleChain(qgraph, uri_sample)
            for p in self.inferParameters(qgraph, uri_query):
                sq.addParameter(parameter = p)
            Q.add(uri_query, sq)
        return Q

    def inferSampleManipulationQueries(self, qgraph):
        '''
        @summary: 
        '''
        q = """
SELECT ?query ?resource ?feature ?sourceid ?sample ?formatter
WHERE {
 ?query a owl:NamedIndividual ;
        a query:SampleManipulationQuery ;
        query:hasResource ?resource ;
        feature:hasFeature ?feature ;
        query:hasProcessid ?sourceid ;
        stat:hasSample ?sample ;
        query:hasFormatter ?formatter .
}
        """
        Q = QueryBundle()
        for uri_query, uri_resource, uri_feature, uri_sourceid, uri_sample, uri_formatter in qgraph.query(q, initNs = self.myns):
            r = Q.getResource(uri_resource)
            if r is None: 
                r = self.translateResource(qgraph, uri_resource)
            aq = SingleSampleQuery()
            aq.feature = uri_feature
            aq.resource = (uri_resource, r)
            aq.formatter = uri_formatter
            aq.samplechain = self.inferSampleChain(qgraph, uri_sample)
            aq.sourceid = str(uri_sourceid)
            Q.add(uri_query, aq)
        return Q

    def inferConditionQueries(self, qgraph):
        '''
        @summary: 
        '''
        q = """
SELECT ?query ?resource ?sourceid ?feature ?cond 
WHERE {
 ?query a owl:NamedIndividual ;
        a query:ConditionQuery ;
        query:hasResource ?resource ;
        query:hasProcessid ?sourceid ;
        feature:hasFeature ?feature ;
        stat:hasCondition ?cond ;
}
        """
        Q = QueryBundle()
        for uri_query, uri_resource, uri_sourceid, uri_feature, uri_cond in qgraph.query(q, initNs = self.myns):
            r = Q.getResource(uri_resource)
            if r is None: 
                r = self.translateResource(qgraph, uri_resource)
            C, op = self.inferCondition(qgraph, uri_cond)
            print uri_query, uri_resource, uri_sourceid, uri_cond
            
            cq = SingleConditionQuery()
            cq.feature = uri_feature
            cq.resource = (uri_resource, r)
            cq.operation = op
            cq.conditiontype = C
            cq.sourceid = str(uri_sourceid)
            Q.add(uri_query, cq)
        return Q

    def inferCondition(self, qgraph, uri_cond):
        q = """
SELECT ?what ?sample
WHERE {
 <%s> a owl:NamedIndividual ;
      a ?what ;
      stat:hasSample ?sample ;
}
        """ % uri_cond
        for what, uri_sample in qgraph.query(q, initNs = self.myns):
            tail = self.model.ontology._tail(what)
            if tail in [ 'NamedIndividual' ]:
                continue
            C = self.lut_condition[tail]
            if C in [IsPositive, IsNegative]: #FIXME: IsNotNegative, IsNotPositive
                return C, self.inferLineraCombinedSample(qgraph, uri_sample)
            else:
                print "BALHE"
                raise Exception("QI NOT IMPLMENTED")

    def inferLineraCombinedSample(self, qgraph, uri_sample):
        q = """
SELECT ?sample ?factor
WHERE {
 <%s> a owl:NamedIndividual ;
      a stat:LinearCombinedSample ;
      stat:hasTerm ?term .
 ?term stat:hasSample ?sample .
 OPTIONAL {
  ?term stat:hasScale ?factor .
 }
}
        """ % uri_sample
        terms = []
        for uri_sample, factor in qgraph.query(q, initNs = self.myns):
            try:
                factor = float(factor)
            except:
                factor = 1
            op = self.inferSampleChain(qgraph, uri_sample)
            terms.append( ( factor, op) ) 
        return terms
    
    def inferSampleChain(self, qgraph, uri_sample):
        tail = self.model.ontology._tail(uri_sample)
        if tail == self.samplesource:
            return []
        q = """
SELECT ?nextsample ?sampleop
WHERE {
 <%s> a owl:NamedIndividual ;
      stat:hasSample ?nextsample ;
      a ?sampleop
}
        """ % uri_sample
        for uri_sample_next, uri_sampleop in qgraph.query(q, initNs = self.myns):
            tail = self.model.ontology._tail(uri_sampleop)
            if tail in [ 'NamedIndividual' ]:
                continue
            op = self.inferSampleChain(qgraph, uri_sample_next)
            break
        skeleton = self.lut_skeleton[tail]
        parlist = ParameterList([ p for p in self.inferParameters(qgraph, uri_sample) ])
        op.append( (skeleton, parlist) )
        return op

    def inferParameters(self, qgraph, uri_query):
        q = """
SELECT ?name ?type ?dim ?defval ?unit ?prefix
WHERE {
 <%s> param:hasParameter ?par .
 ?par a owl:NamedIndividual ;
      param:paramName ?name ;
      param:hasType ?type ;
      a ?dim .
 OPTIONAL {
  ?par unit:hasValue ?defval .
  OPTIONAL {
   ?par unit:hasUnit ?unit .
  }
  OPTIONAL {
   ?par unit:hasPrefix ?prefix .
  }
 }
}
        """ % uri_query
        for uri_name, uri_type, uri_dim, uri_default, uri_unit, uri_prefix in qgraph.query(q, initNs = self.myns):
            tail = self.model.ontology._tail(uri_dim)
#FIXME: query should include the filter, but rdflib has a bug and only the spelt out form would work
# FILTER ( ?dim != owl:NamedIndividual ) 
# FILTER ( ?dim != query:QueryParameter ) 
# FILTER ( ?dim != stat:SampleOperatorParameter ) 
#
# like:
# FILTER ( ?dim != <http://www.w3.org/2002/07/owl#NamedIndividual> ) 
            if tail in [ 'QueryParameter', 'SOP_tail', 'SOP_head', 'SOP_order', 'NamedIndividual' ]:
                continue
            yield self.model.translateParameter(str(uri_name), uri_dim, uri_unit, uri_prefix, uri_type, uri_default)
    
    def translateResource(self, qgraph, uri_resource):
        resource_name = self.model.ontology._tail(uri_resource)
        q = """
SELECT ?resourcetype
WHERE {
 <%s> a owl:NamedIndividual ;
      a core:Resource ;
      a ?resourcetype ;
}
        """ % uri_resource
        for uri_rtype, in qgraph.query(q, initNs = self.myns):
            tail = self.model.ontology._tail(uri_rtype)
            if tail in [ 'Resource', 'NamedIndividual' ]:
                continue
            if tail == "Node":
                r = node(name = resource_name, resourceid = uri_resource)
                for iface in self.inferInterfacesOf(qgraph, uri_resource):
                    r.addinterface(iface)
                return r
            else:
                print "WW: unhandled rtype", uri_rtype
                continue
            