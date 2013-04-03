'''
Created on Feb 21, 2012

@author: steger
'''

from Semantics.Query import QueryBundle, SingleQuery
from Resource.node import node
from Resource.interface import interface
from Resource.link import link
from DataProcessing.Aggregator import Max, Min, Percentile, Mean, Deviation
from DataProcessing.Sampler import Tail, Head
from DataProcessing.Parameter import ParameterList


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

    def _get_myns(self):
        return dict(self.model.ontology.graph.namespaces())


    myns = property(_get_myns,None,None)


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
 ?addressobj a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
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

    def inferInterfacesOfLink(self, qgraph, uri_node, iftype):
        q = """
SELECT ?ifin ?address ?unit ?prefix
WHERE {
 <%s> core:%s ?ifin .
 ?ifin a core:Interface ;
       core:hasIPv4Address ?addressobj .
 ?addressobj a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
             a unit:IPAddress ;
             unit:hasValue ?address .
 OPTIONAL {
  ?addressobj unit:hasUnit ?unit .
 }
 OPTIONAL {
  ?addressobj unit:hasPrefix ?prefix .
 }
}
        """ % (uri_node, iftype)
        for uri_ifin, address, uri_unit, uri_prefix in qgraph.query(q, initNs = self.myns):
            name = self.model.ontology._tail(uri_ifin)
            iface = interface(name, resourceid = uri_ifin)
            if uri_unit is not None:
                iface.address = str(address), self.getUnit(uri_prefix, uri_unit)
            else:
                iface.address = str(address), self.getUnitOfDimension('IPAddress')
            if iftype=="hasSource": iface.direction = iface.EGRESS
            else: iface.direction = iface.INGRESS
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
SELECT ?query ?feature ?sample ?resource ?formatter
WHERE {
 ?query a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
        a query:BundleQuery ;
        feature:hasFeature ?feature ;
        stat:hasSample ?sample ;
        query:hasResource ?resource ;
        query:hasFormatter ?formatter .
}
        """
        Q = QueryBundle()
        for uri_query, uri_feature, uri_sample, uri_resource, uri_formatter in qgraph.query(q, initNs = self.myns):
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

    def getSampleManipulationQuery(self, qgraph):
        '''
        @summary: 
        '''
        q = """
SELECT ?query ?feature ?sample ?formatter
WHERE {
 ?query a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
        a query:SampleManipulationQuery ;
        feature:hasFeature ?feature ;
        stat:hasSample ?sample ;
        query:hasFormatter ?formatter .
}
        """
        Q = SampleQuery()
        resources = {}
        for uri_query, uri_feature, uri_sample, uri_resource, uri_formatter in qgraph.query(q, initNs = self.myns):
            resource_name = self.model.ontology._tail(uri_resource)
            if not resources.has_key(resource_name):
                resources[resource_name] = self.translateResource(qgraph, uri_resource)
            if not Q.has_key(uri_query):
                samplechain = self.inferSampleChain(qgraph, uri_sample)
                Q.newQuery(key = uri_query, feature = uri_feature, samplechain = samplechain, resource = resources[resource_name], formatter = uri_formatter)
            for p in self.inferParameters(qgraph, uri_query):
                Q.addParameter(key = uri_query, parameter = p)
        return Q

    
    def inferSampleChain(self, qgraph, uri_sample):
        tail = self.model.ontology._tail(uri_sample)
        if tail == self.samplesource:
            return []
        q = """
SELECT ?nextsample ?sampleop
WHERE {
 <%s> a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
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
 ?par a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
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
# FILTER ( ?dim != <http://www.w3.org/2002/07/owl#NamedIndividual> ) 
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
 <%s> a <http://www.w3.org/2002/07/owl#NamedIndividual> ;
      a core:Resource ;
      a ?resourcetype ;
}
        """ % uri_resource
        for uri_rtype, in qgraph.query(q, initNs = self.myns):
            tail = self.model.ontology._tail(uri_rtype)
            if tail in [ 'Resource', 'NamedIndividual', 'NetworkElement' ]:
                continue
            if tail == "Node":
                r = node(name = resource_name, resourceid = uri_resource)
                for iface in self.inferInterfacesOf(qgraph, uri_resource):
                    r.addinterface(iface)
                return r
            elif tail == "Link":
                r = link(name = resource_name, resourceid = uri_resource)
                for iface in self.inferInterfacesOfLink(qgraph, uri_resource, "hasSource"):
                    r.source = iface
                    break
                for iface in self.inferInterfacesOfLink(qgraph, uri_resource, "hasSink"):
                    r.destination = iface
                    break
                return r
            else:
                print "WW: unhandled rtype", uri_rtype
                continue
            
