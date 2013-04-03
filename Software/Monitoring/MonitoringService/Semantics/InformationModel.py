'''
Created on Aug 10, 2011

@author: steger
'''
from rdflib import Graph, Namespace, URIRef, plugin
from rdflib.query import Processor, Result

class IMError(Exception):
    pass

class Ontology(object):
 
    def __init__(self):

        plugin.register(
            'sparql', Processor,
            'rdfextras.sparql.processor', 'Processor')
        plugin.register(
            'sparql', Result,
            'rdfextras.sparql.query', 'SPARQLQueryResult')
        self._graph = Graph()
        self._graph.bind('owl', Namespace("http://www.w3.org/2002/07/owl#"))
    
    def load(self, prefix, owl_url, namespace_url = None):
        '''
        @summary: load owl file and bind name space
        @param prefix: an abbreviation of the name space to be used in sparql queries
        @type prefix: str
        @param owl_url: the location of the owl document to load
        @type owl_url: str
        @param namespace_url: the name space if None a # is added to the owl_url
        @type namespace_url: str or None
        '''
        if namespace_url is None:
            ns = Namespace("%s#" % namespace_url)
        else:
            ns = Namespace(namespace_url)
        try:
            self._graph += Graph().parse(source = owl_url)
        #except URLError:
        except:
            raise IMError("URLError: Cannot read model %s" % owl_url)
        try:
            self._graph.bind(prefix, ns)
        except:
            pass

    @staticmethod
    def _float(f):
        if '.' in f or 'e' in f or 'E' in f:
            return float(f)
        else:
            return int(f)

    @staticmethod
    def ipret(x):
        if not x:
            return None
        x = str(x)
        if len(x):
            return x
        else:
            return None

    @staticmethod
    def _tail(uriref):
        if not isinstance(uriref, URIRef):
            raise IMError("Wrong uriref %s" % uriref)
        return str(uriref).split("#")[-1]
    
    def query(self, query):
        return self._graph.query(query, initNs = dict(self._graph.namespaces()))

    def triples(self, spo_tuple):
        return self._graph.triples(spo_tuple)

    def ns(self, prefix):
        for p, ns in self._graph.namespaces():
            if p == prefix:
                return Namespace(ns)
        raise IMError("Unknown prefix: %s" % prefix)
    
    @property
    def ns_dict(self):
        return dict(self._graph.namespaces())

    def emptygraph(self):
        g = Graph()
        # bind name spaces
        for prefix, ns in self.ns_dict.iteritems():
            g.bind(prefix, ns)
        return g

    @property
    def g(self):
        g = Graph()
        g += self._graph
        for prefix, ns in self.ns_dict.iteritems():
            g.bind(prefix, ns)
        return g
    
#    @property
#    def graph(self):
#        return self._graph

    def dump(self):
        for t in self._graph.triples((None, None, None)):
            print t
