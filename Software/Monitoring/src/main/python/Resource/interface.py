'''
Created on Jul 11, 2012

@author: steger
'''
from Resource.resource import resource

class interface(resource):
    '''
    classdocs
    '''
    UNDEFINED = 0
    INGRESS = 1
    EGRESS = 2

    def __init__(self, name = None, resourceid = None):
        resource.__init__(self, name, resourceid)
        self._public = False
        self._direction = self.UNDEFINED
        self._iface = None
        self._address = None
        self._hostname = None

    def setvalues(self, ifacename, address, ispublic = False, direction = 0, hostname = None):
        self.interface = ifacename
        self.address = address
        self.ispublic = ispublic
        self.direction = direction
        self.hostname = hostname
    
    def _get_ispublic(self):
        if not self._iface:
            raise Exception("No interface name defined yet for %s" % self.resourceid)
        if not self._address:
            raise Exception("No address defined yet for %s" % self.resourceid)
        return self._public
    def _set_ispublic(self, ispublic):
        if isinstance(ispublic, bool):
            self._public = ispublic
        else:
            self._public = False  
    
    def _get_hostname(self):
        return self._hostname
    def _set_hostname(self, hostname):
        self._hostname = self.ipret(hostname)
            
    def _get_address(self):
        return self._address
    def _set_address(self, address):
        if isinstance(address, tuple):
            self._address = address
        else:
            self._address = self.ipret(address)

    def _get_interface(self):
        return self._iface
    def _set_interface(self, iface):
        self._iface = self.ipret(iface)

    def _get_direction(self):
        return self._direction
    def _set_direction(self, direction):
        self._direction = direction & (self.INGRESS | self.EGRESS)
    
    def _get_isvalid(self):
        return self.address and self.interface

    direction = property(_get_direction,_set_direction,None)

    ispublic = property(_get_ispublic,_set_ispublic,None)

    isvalid = property(_get_isvalid,None,None)

    hostname = property(_get_hostname,_set_hostname,None)

    address = property(_get_address,_set_address,None)

    interface = property(_get_interface,_set_interface,None)
