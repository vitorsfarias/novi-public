"""
#
# created by sandor laki, 31th august, 2012
#
"""

try:
    from eu.novi.monitoring.util import MonitoringQuery
except ImportError:
    MonitoringQuery = object

from rdflib import Graph, Namespace, Literal

class MonitoringQueryImpl(MonitoringQuery):
	"""
	A helper class to assemble monitoring queries in other novi components
	"""
	def __init__(self, monSrv):
		self.monSrv = monSrv
                self.addrs = {}
		self.g = Graph()
                for k, (_, ns) in self.monSrv.ontology.ontology.iteritems():
                        self.g.bind(k, Namespace(ns))
		self.mns = Namespace("http://foo.bar/req.owl#")
		self.g.bind('q', self.mns)
		self.NS = self.monSrv.ontology.ns
		self.TYPE = self.NS('rdf')['type']
                self.S = self.NS('stat')['UnmodifiedExtractOfFeatureSamples']
                self.F = self.NS('query')['Formatter_JSON']


	def addFeature(self, queryName, feature):
		Q = self.mns[queryName]
		self.g.add((Q, self.TYPE, self.NS('owl')['NamedIndividual']))
		self.g.add((Q, self.TYPE, self.NS('query')['BundleQuery']))
		self.g.add((Q, self.NS('feature')['hasFeature'], self.NS('feature')[feature]))
                self.g.add((Q, self.NS('stat')['hasSample'], self.S))
                self.g.add((Q, self.NS('query')['hasFormatter'], self.F))



	def addResource(self, queryName, resourceName, resourceType):
		Q = self.mns[queryName]
		R = self.mns[resourceName]
		self.g.add((Q, self.NS('query')['hasResource'], R))
		self.g.add((R, self.TYPE, self.NS('core')[resourceType]))
                self.g.add((R, self.TYPE, self.NS('core')['Resource']))
                self.g.add((R, self.TYPE, self.NS('owl')['NamedIndividual']))

	def addInterface(self, resourceName, interfaceName, interfaceType):
		R = self.mns[resourceName]
		I = self.mns[interfaceName]
		self.g.add((R, self.NS('core')[interfaceType], I))

	def defineInterface(self, interfaceName, address, addressType):
		I = self.mns[interfaceName]
                if address in self.addrs:
                    A = self.addrs[address]
                else:                    
		    IPA = Literal(address)
                    A = self.mns[ '%s_address' % interfaceName ]
                    self.g.add((A, self.TYPE, self.NS('owl')['NamedIndividual']))
                    self.g.add((A, self.TYPE, self.NS('unit')['IPAddress']))
                    self.g.add((A, self.NS('unit')['hasValue'], IPA))
                    self.addrs[address] = A
		self.g.add((I, self.TYPE, self.NS('core')['Interface']))
		self.g.add((I, self.NS('core')[addressType], A))
        
	def serialize(self):
		return self.g.serialize()

