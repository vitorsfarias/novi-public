'''
Created on May 31, 2012

@author: steger
'''
from Resource.resource import resource
from Resource.interface import interface

class node(resource):
    def __init__(self, name = None, resourceid = None):
        resource.__init__(self, name, resourceid)
        self._public = False
        self._interfaces = {}
    
    @property
    def ispublic(self):
        if not len(self._interfaces):
            raise Exception("No interfaces defined yet for %s" % self.resourceid)
        return self._public
    
    def addinterface(self, iface):
        if not isinstance(iface, interface):
            raise Exception("Wrong resource type %s is not an interface" % iface)
        self._interfaces[iface.interface] = iface
        self._public |= iface.ispublic
    
    def interfaces(self):
        for iface in self._interfaces.itervalues():
            if not iface.isvalid:
                print "WW: invalid interface:", iface.resourceid
                continue
            yield iface.interface, iface.address, iface.ispublic, iface.hostname, iface.direction
    
    def get_ipaddress(self, interfacename):
        for ifname, address, _, _, _ in self.interfaces():
            if ifname == interfacename:
                return address
        raise Exception("%s has no interface %s" % (self.resourceid, interfacename))

    def get_hostname(self, interfacename):
        for ifname, address, _, hostname, _ in self.interfaces():
            if ifname != interfacename:
                continue
            if hostname:
                return hostname
            else:
                return address
        raise Exception("%s has no interface %s" % (self.resourceid, interfacename))
