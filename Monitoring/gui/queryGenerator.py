'''
Created on Aug 10, 2011

@author: steger
'''

from rdflib import Namespace
from rdflib import Literal
from Resource.node import node
from conf import savequery

class QueryGenerator(object):
    mns = Namespace("http://foo.bar/req.owl#")

    def __init__(self, informationmodel, feature):
        # Q a BundleQuery
        # Q a NamedIndividual
        # Q hasFeature feature
        # Q hasFormatter Formatter_CSV
        f = feature.split("#")[-1]
        self.graph = informationmodel.emptygraph()
        self.ns = informationmodel.ns
        self.graph.bind('q', self.mns)
        self.TYPE = self.ns('rdf')['type']
        Q = self.mns['measure%sInformation' % f]
        self.Q = Q
        self.graph.add((Q, self.TYPE, self.ns('owl')['NamedIndividual']))
        self.graph.add((Q, self.TYPE, self.ns('query')['BundleQuery']))
        self.graph.add((Q, self.ns('feature')['hasFeature'], feature))
        self.graph.add((Q, self.ns('query')['hasFormatter'], self.ns('query')['Formatter_CSV']))
        self.graph.add((Q, self.ns('stat')['hasSample'], self.ns('stat')['UnmodifiedExtractOfFeatureSamples'] ))
        self.graph.add((Q, self.ns('query')['hasFormatter'], self.ns('query')['Formatter_CSV'] ))

    def _addNode(self, r):
        # Q hasResource R
        #  R a Node
        #  R a Resource
        #  R hasInboundInterface I1
        #  R hasOutboundInterface I2
        #   I1 a Interface
        #   I1 hasIPv4Address ADDR
        #   I2 a Interface
        #   I2 hasIPv4Address ADDR
        #    ADDR a IPAddress
        #    ADDR a NamedIndividual
        #    ADDR hasValue address
        iface = 'eth0'
        try:
            address = r.get_ipaddress(iface)
        except:
            print "EE: fall back to another resource HACK"
            address = '157.181.175.243'
        R = self.mns[r.name]
        I1 = self.mns['ifin']
        I2 = self.mns['ifout']
        IPADDR = Literal(address)
        ADDR = self.mns['IPAddress_%s' % r.name]
        self.graph.add((R, self.TYPE, self.ns('core')['Node']))
        self.graph.add((R, self.TYPE, self.ns('core')['Resource']))
        self.graph.add((R, self.TYPE, self.ns('owl')['NamedIndividual']))
        self.graph.add((R, self.ns('core')['hasInboundInterface'], I1))
        self.graph.add((R, self.ns('core')['hasOutboundInterface'], I2))
        self.graph.add((I1, self.TYPE, self.ns('core')['Interface']))
        self.graph.add((I2, self.TYPE, self.ns('core')['Interface']))
        self.graph.add((I1, self.ns('core')['hasIPv4Address'], ADDR))
        self.graph.add((I2, self.ns('core')['hasIPv4Address'], ADDR))
        self.graph.add((ADDR, self.TYPE, self.ns('owl')['NamedIndividual']))
        self.graph.add((ADDR, self.TYPE, self.ns('unit')['IPAddress']))
        self.graph.add((ADDR, self.ns('unit')['hasValue'], IPADDR))
        self.graph.add((self.Q, self.ns('query')['hasResource'], R))
        print "NODE ADDED"


    def addResource(self, r):
        if isinstance(r, node):
            self._addNode(r)

    def addParameter(self, p):
        # Q hasParameter P
        #  P a D
        #  P a QueryParameter
        #  P hasType T
        #  P hasUnit U
        #  P hasPrefix PR
        #  P paramName parname
        #  P paramValue parvalue
        P = self.mns[p.name]
        self.graph.add((P, self.TYPE, p.dimension.reference))
        self.graph.add((P, self.TYPE, self.ns('owl')['NamedIndividual']))
        self.graph.add((P, self.TYPE, self.ns('query')['QueryParameter']))
        value, unit = p.value
        cast_value = p.valuetype(value)
        if isinstance(cast_value, str):
            self.graph.add((P, self.ns('param')['hasType'], self.ns('param')['String']))
        elif isinstance(cast_value, int):
            self.graph.add((P, self.ns('param')['hasType'], self.ns('param')['Integer']))
        if isinstance(cast_value, float):
            self.graph.add((P, self.ns('param')['hasType'], self.ns('param')['Float']))
        #FIXME: implement it
        if unit != p.dimension.unit:
            if unit.prefix:
                pass
        self.graph.add((P, self.ns('param')['paramName'], Literal(p.name)))
        self.graph.add((P, self.ns('unit')['hasValue'], Literal(cast_value)))
        self.graph.add((self.Q, self.ns('param')['hasParameter'], P))

    @property
    def query(self):
        with open(savequery, 'w') as f:
            f.write(self.graph.serialize())
        return self.graph.serialize()

if __name__ == "__main__":
    pass
