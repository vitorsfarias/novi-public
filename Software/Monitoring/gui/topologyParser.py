'''
Created on May 21, 2012

@author: steger
'''
from rdflib import Graph
from StringIO import StringIO
from Resource.interface import interface
#from urllib2 import URLError

class TopologyParser(object):

    def __init__(self, informationmodel, topology_owl):
        self.graph = informationmodel.g
        sio = StringIO(topology_owl)
        self.graph += Graph().parse(source = sio)
        self.ipret = informationmodel.ipret
    
    @property
    def myns(self):
        return dict(self.graph.namespaces())
    
    @staticmethod
    def _iface(iface):
        # TODO: check interface naming scheme
        return "-".join( str(iface).split('#')[-1].split(':')[-1].split('.')[-1].split("-")[:-1] )

    def interfaces(self):
        #FIXME: nem muuxik a namespace lookup a hasValue-ra, de mieert
        #FIXME: itt kell a unitot feltenni!
        q = """
SELECT ?if ?hn ?ip 
WHERE {
 ?if a core:Interface ;
     core:hasIPv4Address ?ipo .
 ?ipo a unit:IPAddress ;
      unit:hasValue ?ip .
 OPTIONAL {
  ?if core:connectedTo ?n .
  ?n core:hostname ?hn .
 }    
}
        """
        for uri_iface, hostname, ip in self.graph.query(q, initNs = self.myns):
            if str(uri_iface).endswith("-out"):
                direction = interface.EGRESS
            elif str(uri_iface).endswith("-in"):
                direction = interface.INGRESS
            else:
                direction = interface.INGRESS | interface.EGRESS
            #FIXME: ugly here
            IP = self.ipret(ip)#, self.model.um.ipv4dotted
            yield (uri_iface, self._iface(uri_iface), self.ipret(hostname), IP, direction)


    def nodes(self):
        q = """
SELECT ?vn ?if
WHERE {
 ?t a core:Topology ;
    core:contains ?vn .
 ?vn a core:VirtualNode ;
     core:implementedBy ?n .
 OPTIONAL {
   ?n core:hasInboundInterface ?if .
 }
}
        """
        for uri_node, uri_iface in self.graph.query(q, initNs = self.myns):
            yield (uri_node, uri_iface)

    def links(self):
        q = """
SELECT ?vnsrc ?vndst ?vl ?ifs ?ifd
WHERE {
 ?t a core:Topology ;
    core:contains ?vl .
 ?vl a core:VirtualLink ;
     core:hasSource ?ifs ;
     core:hasSink ?ifd .
 ?ifs a core:Interface .
 ?ifd a core:Interface .
 ?vnsrc a core:VirtualNode ;
        core:hasOutboundInterface ?ifs .
 ?vndst a core:VirtualNode ;
        core:hasInboundInterface ?ifd .
}
        """
        for uri_source, uri_destination, uri_link, uri_ifsrc, uri_ifdst in self.graph.query(q, initNs = self.myns):
            yield uri_link, uri_source, uri_destination, uri_ifsrc, uri_ifdst

if __name__ == '__main__':
#    fn = './sample/MidtermWorkshopRequest_bound_slice2_v4.owl'
#    fn = './sample/fullBoundRequest.owl'
    #fn = './sample/slice_1992460072.owl'
    #fn = './sample/1node.owl'
    #fn = './sample/1link.owl'
    #fn = "./sample/slice_1909186741.owl"
    fn = "./sample/moreLink.owl"
    with open(fn) as fp:
        owl = fp.read()
    t = TopologyParser(baseurl = '../information-model/monitoring-model', topology_owl = owl)
    print "IFACES"
    for i in t.interfaces():
        print i
    print "NODES"
    for n in t.nodes():
        print n
    print "LINKS"
    for l in t.links():
        print l
