'''
Created on Aug 10, 2011

@author: steger
'''
from DataProcessing.Parameter import ParameterList, Parameter

class FeatureModel(object):
    '''
    classdocs
    '''
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

    def inferFeatures(self):
        '''
        @summary: extract the monitored features from the information model
        @return: a generator of the list of (feature reference, name, resource type) tuples
        @rtype: (URIRef, str, URIRef)
        '''
        q = """
SELECT ?feature ?name ?resource
WHERE {
 ?feature a owl:NamedIndividual ;
          a ?parent ;
          feature:featureName ?name .
 ?parent  rdfs:subClassOf feature:MonitoredFeature .
 ?resource feature:hasFeature ?feature
}
        """
        for uri_feature, name, uri_resource in self.ontology.query(q):
            yield uri_feature, str(name), uri_resource
    
    def inferObligatoryParametersOf(self, feature):
        '''
        @summary: extract the parameter list for a given feature
        @param feature: reference to the monitored feature
        @type feature: URIRef
        @return: an initialized list of the parameters for this feature
        @rtype: ParameterList
        '''
        q = """
SELECT ?name ?ptype ?dim ?defval ?unit ?prefix
WHERE {
 feature:%s feature:obligatoryParameter ?par .
 ?par param:paramName ?name ;
      param:hasType ?ptype ;
      param:hasDimension ?dim .
 OPTIONAL {
  ?par param:paramValue ?defval .
  OPTIONAL {
   ?par param:hasUnit ?unit .
   OPTIONAL {
    ?par param:hasPrefix ?prefix .
   }
  }
 }
}
        """ % (self.ontology._tail(feature))
        paramlist = ParameterList()
        for name, uri_ptype, uri_dim, default, uri_unit, uri_prefix in self.ontology.query(q):
            p = self.translateParameter(str(name), uri_dim, uri_unit, uri_prefix, uri_ptype, default)
            paramlist.append(p)
        return paramlist

    def inferOptionalParametersOf(self, feature):
        '''
        @summary: extract the parameter list for a given feature
        @param feature: reference to the monitored feature
        @type feature: URIRef
        @return: an initialized list of the parameters for this feature
        @rtype: ParameterList
        '''
        q = """
SELECT ?name ?ptype ?dim ?defval ?unit ?prefix
WHERE {
 feature:%s feature:optionalParameter ?par .
 ?par param:paramName ?name ;
      param:hasType ?ptype ;
      param:hasDimension ?dim .
 OPTIONAL {
  ?par param:paramValue ?defval .
  OPTIONAL {
   ?par param:hasUnit ?unit .
   OPTIONAL {
    ?par param:hasPrefix ?prefix .
   }
  }
 }
}
        """ % (self.ontology._tail(feature))
        paramlist = ParameterList()
        for name, uri_ptype, uri_dim, default, uri_unit, uri_prefix in self.ontology.query(q):
            p = self.translateParameter(str(name), uri_dim, uri_unit, uri_prefix, uri_ptype, default)
            paramlist.append(p)
        return paramlist
    
    def inferFeatureMonitoringParameters(self):
        '''
        @summary: extract parameters declared for feature monitoring
        @return: an iterator over parameters
        @rtype: (parameter name, dimension, value, unit)
        '''
        q = """
SELECT ?name ?dim ?defval ?unit ?prefix
WHERE {
 ?par a feature:FeatureMonitoringParameter ;
      param:paramName ?name ;
      param:hasDimension ?dim .
 OPTIONAL {
  ?par param:paramValue ?defval .
  OPTIONAL {
   ?par param:hasUnit ?unit .
   OPTIONAL {
    ?par param:hasPrefix ?prefix .
   }
  }
 }
}
        """
        for name, uri_dim, default, uri_unit, uri_prefix in self.ontology.query(q):
#FIXME: duplicate (similar thing in translateParameter!!!
            d = self.dm[ self.ontology._tail(uri_dim) ]
            if default is None:
                yield str(name), d, "", d.unit
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
                yield str(name), d, str(default), u
    
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
