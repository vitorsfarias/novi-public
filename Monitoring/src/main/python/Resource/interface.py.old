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
    
    @property
    def ispublic(self):
        if not self._iface:
            raise Exception("No interface name defined yet for %s" % self.resourceid)
        if not self._address:
            raise Exception("No address defined yet for %s" % self.resourceid)
        return self._public
    @ispublic.setter
    def ispublic(self, ispublic):
        if isinstance(ispublic, bool):
            self._public = ispublic
        else:
            self._public = False  
    
    @property
    def hostname(self):
        return self._hostname
    @hostname.setter
    def hostname(self, hostname):
        self._hostname = self.ipret(hostname)
            
    @property
    def address(self):
        return self._address
    @address.setter
    def address(self, address):
        if isinstance(address, tuple):
            self._address = address
        else:
            self._address = self.ipret(address)

    @property
    def interface(self):
        return self._iface
    @interface.setter
    def interface(self, iface):
        self._iface = self.ipret(iface)

    @property
    def direction(self):
        return self._direction
    @direction.setter
    def direction(self, direction):
        self._direction = direction & (self.INGRESS | self.EGRESS)
    
    @property
    def isvalid(self):
        return self.address and self.interface
