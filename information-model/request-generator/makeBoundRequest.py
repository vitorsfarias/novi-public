#!/usr/bin/python
import rdflib
RDF = rdflib.Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
RDFS = rdflib.Namespace("http://www.w3.org/2000/01/rdf-schema#")
OWL = rdflib.Namespace("http://www.w3.org/2002/07/owl#")
NOVI = rdflib.Namespace("http://fp7-novi.eu/im.owl#")
UNIT = rdflib.Namespace("http://fp7-novi.eu/unit.owl#")

GRAPH = None


class NoviObject(object):
    """The generic Novi Object
    
    This object stores the name and generates a URI based on the NOVI prefix."""
    def __init__(self, name):
        self.name = name
        self.prefix = NOVI
        self.uri = self.prefix[self.name]
    
    def __str__(self):
        return "%s (%s)" % (self.name,self.uri)
    
    def toRDF(self):
        "Convert the information in the object to the RDF GRAPH object."
        GRAPH.add((self.uri,RDF.type,OWL.NamedIndividual))
        GRAPH.add((self.uri,NOVI.name,rdflib.Literal(self.name)))

class Topology(NoviObject):
    """A NOVI Request Topology object"""
    def __init__(self, name):
        super(Topology, self).__init__(name)
        self._links = []
        self._nodes = []
    
    def addLinks(self,links):
        self._links += links
    def addNodes(self,nodes):
        self._nodes += nodes
    def toRDF(self):
        super(Topology, self).toRDF()
        for x in self._nodes:
            GRAPH.add((self.uri,NOVI.contains,x.uri))
            x.toRDF()
        for x in self._links:
            GRAPH.add((self.uri,NOVI.contains,x.uri))
            x.toRDF()
            
        
class Node(NoviObject):
    """A NOVI Node object
    
    This is a generic Node, used for representing virtual hosts. 
    Stores the host information, and contains pointers to the interfaces of this Node."""
    def __init__(self, name):
        super(Node, self).__init__(name)
        self._intf_index = 0
        self._interfaces = []
        self._hostname = None
        self._intfprefix = "eth"
        self._implementedBy = None

    def setPrefixName(self,name):
        self._intfprefix = name

    def toRDF(self):
        super(Node,self).toRDF()
        GRAPH.add((self.uri,RDF.type,NOVI.Node))
        for i in self._interfaces:
            i.toRDF()
        if self._implementedBy:
            GRAPH.add((self.uri,NOVI.implementedBy,self._implementedBy.uri))
            self._implementedBy.toRDF()
        if self._hostname:
            GRAPH.add((self.uri,NOVI.hostname,rdflib.Literal(self._hostname)))

    def addInterface(self,name=None,target=None,ip=None,netmask=None,impl=None):
        """Create a new Interface attached to this Node.
        
        <name> is optional, if not given a new name will be made using the intf prefix and an index
        <target> is optional, if given a Link is created also to that target
        <ip> and <netmask> are used to set the ip and netmask for the interface.
        <impl> specifies the URI the Interface is implemented by. """
        if not name:
            name = self._intfprefix + str(self._intf_index)
            self._intf_index += 1
        intf = Interface(name,self,target,ip=ip,netmask=netmask,impl=impl)
        self._interfaces.append(intf)
        return intf
    
    def implementedBy(self,implnode):
        self._implementedBy = implnode
    
    def setHostname(self,hostname):
        self._hostname = hostname

class ImplNode(Node):
    """A hardware-based NOVI Node
    
    Additionally store the hardware type, and the addInterface takes a full URI."""
    def __init__(self, uri):
        super(ImplNode, self).__init__(uri)
        self.uri = rdflib.URIRef(uri)
        self._hardwaretype = None
        
    def addInterface(self,uri):
        "This method takes a full URI to use as the interface name."
        intf = Interface(uri,self,uri=uri)
        self._interfaces.append(intf)

    def setHardwareType(self,hwtype):
        self._hardwaretype = hwtype

    def toRDF(self):
        super(ImplNode,self).toRDF()
        if self._hardwaretype:
            GRAPH.add((self.uri,NOVI.hardwareType,rdflib.Literal(self._hardwaretype)))
            
class Link(NoviObject):
    """A NOVI Link object
    
    The Link object is unidirectional.
    It stores the name of the link, as well as the source and sink (unidirectional) Interfaces."""
    def __init__(self, name, src=None, snk=None):
        super(Link, self).__init__(name)
        self._src = src
        self._snk = snk
        self._provisionedBy = None
    
    def provisionedBy(self,noviobj):
        "Define that a (virtual) Link is implemented by another Link."
        self._provisionedBy = noviobj
    
    def toRDF(self):
        super(Link,self).toRDF()
        if self._provisionedBy:
            GRAPH.add((self.uri,RDF.type,NOVI.VirtualLink))
            GRAPH.add((self.uri,NOVI.provisionedBy,self._provisionedBy.uri))
            self._provisionedBy.toRDF()
        else:
            GRAPH.add((self.uri,RDF.type,NOVI.Link))
            
        
        
class Interface(NoviObject):
    """A NOVI Interface object
    
    This object contains the most logic, with references to both its Device and the underlying Link.
    The Interface object represents a single interface, with two unidirectional components, each with a separate Link.
    
    The name and device parameters are required, the rest is optional."""
    def __init__(self, name, device, target=None,ip=None,netmask=None,uri=None,impl=None):
        super(Interface, self).__init__(name)
        self._device = device
        self._ip = ip
        self._netmask = netmask
        self._implementedBy = impl
        if uri:
            self.uri = rdflib.URIRef(uri)
        else:
            self.uri = self.prefix[self._device.name+"-"+self.name]
        if target:
            self.addTarget(target)
        else:
            self._target = None
    
    def getDevIntname(self):
        return self._device.name+":"+self.name
    
    def addTarget(self,target):
        self._target = target
        self._inLink = Link(self._target.getDevIntname()+"-"+self.getDevIntname(),snk=self)
        self._outLink = Link(self.getDevIntname()+"-"+self._target.getDevIntname(),src=self)
    
    def linkProvisionedBy(self,link):
        self._inLink.provisionedBy(link)
        self._outLink.provisionedBy(link)
    
    def getLinks(self):
        return [self._inLink, self._outLink]
    
    def toRDF(self):
        super(Interface,self).toRDF()
        GRAPH.add((self.uri,RDF.type,NOVI.BidirectionalInterface))
        if self._ip:
            # Make an IPv4Address object to store the value.
            adr = rdflib.URIRef(self.uri+"-address")
            GRAPH.add((self.uri,NOVI.hasIPv4Address,adr))
            GRAPH.add((adr,RDF.type,OWL.NamedIndividual))
            GRAPH.add((adr,RDF.type,UNIT.IPAddress))
            GRAPH.add((adr,UNIT.hasUnit, UNIT.ipv4dotted))
            GRAPH.add((adr,UNIT.hasValue, rdflib.Literal(self._ip)))
            if self._netmask:
                GRAPH.add((adr,UNIT.hasNetmask,rdflib.Literal(self._netmask)))
        for x in ("-in","-out"):
            # For both the in and out going interfaces, create the RDF statements.
            x = rdflib.URIRef(self.uri+x)
            GRAPH.add((x,RDF.type,OWL.NamedIndividual))
            GRAPH.add((x,RDF.type,NOVI.Interface))
            GRAPH.add((self.uri,NOVI.contains,x))
            if x.endswith("-in"):
                GRAPH.add((self._device.uri,NOVI.hasInboundInterface,x))
                if self._target:
                    GRAPH.add((x,NOVI.isSink,self._inLink.uri))
                    self._inLink.toRDF()
                if self._implementedBy:
                    GRAPH.add((x,NOVI.implementedBy,rdflib.URIRef(self._implementedBy+"-in")))
            else:
                GRAPH.add((self._device.uri,NOVI.hasOutboundInterface,x))
                if self._target:
                    GRAPH.add((x,NOVI.isSource,self._outLink.uri))
                    self._outLink.toRDF()
                if self._implementedBy:
                    GRAPH.add((x,NOVI.implementedBy,rdflib.URIRef(self._implementedBy+"-out")))
                
        
class NSwitch(NoviObject):
    # TODO: not compeletely sure of this syntax yet.
    def __init__(self, name, uri=None, vlan=None):
        super(NSwitch, self).__init__(name)
        if uri:
            self.uri = rdflib.URIRef(uri)
        if vlan:
            self._vlan = vlan
        self._interfaces = []
    
    def addInterface(self,intf):
        self._interfaces.append(intf)

    def toRDF(self):
        super(NSwitch,self).toRDF()
        GRAPH.add((self.uri,RDF.type,NOVI.NSwitch))
        if self._vlan:
            GRAPH.add((self.uri,NOVI.hasVLANID,rdflib.Literal(self._vlan)))
        

def connect(x,y,ipX=None,netmaskX=None,ipY=None,netmaskY=None,nswitch=None,implX=None,implY=None):
    """Short-hand function to connect two Interfaces, possibly both with IP addresses on them.
    The nswitch parameter is used to describe that the link is actually implemented by an NSwitch"""
    portX = x.addInterface(ip=ipX,netmask=netmaskX,impl=implX)
    portY = y.addInterface(ip=ipY,netmask=netmaskY,impl=implY)
    portX.addTarget(portY)
    portY.addTarget(portX)
    if nswitch:
        portX.linkProvisionedBy(nswitch)
        portY.linkProvisionedBy(nswitch)
    return portX.getLinks() + portY.getLinks()
    
def addImplDetails(implNode):
    """Create specific hardware implementation details."""
    if implNode.name == "http://fp7-novi.eu/im.owl#FEDERICA_psnc.poz.router1":
        implNode.setHostname("psnc.poz.router1")
        implNode.addInterface("http://fp7-novi.eu/im.owl#psnc.poz.router1.ge-0/0/0")
        implNode.addInterface("http://fp7-novi.eu/im.owl#psnc.poz.router1.ge-0/2/9")
        implNode.setHardwareType("router")
    elif implNode.name == "http://fp7-novi.eu/im.owl#PlanetLab_smilax1.man.poznan.pl":
        implNode.setHostname("smilax1.man.poznan.pl")
        implNode.addInterface("http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl:eth0")
        implNode.setHardwareType("i386")
    elif implNode.name == "http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.vserver1":
        implNode.setHostname("vserver1.erl.de")
        implNode.addInterface("http://fp7-novi.eu/im.owl#dfn.erl.vserver1.vmnic1")
    elif implNode.name == "http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.router1":
        implNode.setHostname("router1.erl.de")
        implNode.addInterface("http://fp7-novi.eu/im.owl#dfn.erl.router1.ge-0/1/0")
        implNode.addInterface("http://fp7-novi.eu/im.owl#dfn.erl.router1.ge-0/1/2")
    elif implNode.name == "http://fp7-novi.eu/im.owl#PlanetLab_planetlab2-novi.lab.netmode.ece.ntua.gr":
        implNode.setHostname("planetlab2-novi.lab.netmode.ece.ntua.gr")
        implNode.addInterface("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr:eth0")
        implNode.setHardwareType("i386")
    


def generateTopology():
    topDict = {}
    for x in ["lrouter","lrouter2","sliver1","sliver2","vm1"]:
        topDict[x] = Node(x)
    implDict = {"lrouter":  "http://fp7-novi.eu/im.owl#FEDERICA_psnc.poz.router1",
                "lrouter2": "http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.router1",
                "sliver1":  "http://fp7-novi.eu/im.owl#PlanetLab_planetlab2-novi.lab.netmode.ece.ntua.gr",
                "sliver2":  "http://fp7-novi.eu/im.owl#PlanetLab_smilax1.man.poznan.pl",
                "vm1":      "http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.vserver1"}
    for x in implDict.keys():
        implDict[x] = ImplNode(implDict[x])
        addImplDetails(implDict[x])
    topDict["lrouter"].setPrefixName("if")
    topDict["lrouter2"].setPrefixName("if")
    ns = NSwitch("http://fp7-novi.eu/im.owl#nswitch-fed-pl-3701",
                vlan=3701,uri="http://fp7-novi.eu/im.owl#nswitch-fed-pl-3701")
    links = []
    links.extend(connect(topDict["lrouter"],topDict["sliver1"],
            implX="http://fp7-novi.eu/im.owl#psnc.poz.router1.ge-0/2/9",
            ipX="192.168.37.1",netmaskX="255.255.255.248",
            implY="http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr:eth0",
            ipY="192.168.37.3",netmaskY="255.255.255.248",
            nswitch=ns))
    links.extend(connect(topDict["lrouter"],topDict["sliver2"],
            implX="http://fp7-novi.eu/im.owl#psnc.poz.router1.ge-0/2/9",
            ipX="192.168.37.2",netmaskX="255.255.255.248",
            implY="http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl:eth0",
            ipY="192.168.37.4",netmaskY="255.255.255.248",
            nswitch=ns))
    links.extend(connect(topDict["lrouter"],topDict["lrouter2"],
            implX="http://fp7-novi.eu/im.owl#psnc.poz.router1.ge-0/0/0",
            implY="http://fp7-novi.eu/im.owl#dfn.erl.router1.ge-0/1/0"))
    links.extend(connect(topDict["lrouter2"],topDict["vm1"]))
    for x in topDict.keys():
        # This iterates over the virtual topology nodes and corresponds them to their hardware nodes.
        topDict[x].implementedBy(implDict[x])
    # Finally add the (virtual) nodes and links to the request topology object
    # And ask the topology object to map everything to RDF.
    topo = Topology("RequestTopology2")
    topo.addNodes(topDict.values())
    topo.addLinks(links)
    topo.toRDF()
    
def main():
    global GRAPH
    GRAPH = rdflib.Graph()
    GRAPH.bind("novi",NOVI)
    GRAPH.bind("unit",UNIT)
    GRAPH.bind("owl",OWL)
    generateTopology()
    # print GRAPH.serialize(format="n3")
    GRAPH.serialize(destination="bound-request.n3",format="n3")
    GRAPH.serialize(destination="bound-request.owl",format="xml")
    print "Created/overwritten bound-request.n3 and bound-requestl.owl"
    
if __name__ == '__main__':
    main()