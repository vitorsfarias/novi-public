'''
Created on May 31, 2012

@author: steger
'''
from Resource.resource import resource
from Resource.node import node
from Resource.interface import interface

class link(resource):
    def __init__(self, name = None, resourceid = None, source = None, destination = None):
        resource.__init__(self, name, resourceid)
        self._source = source
        self._destination = destination
    
    def _get_source(self):
        return self._source
    def _set_source(self, source):
        if isinstance(source, interface): #laki
            self._source = source
    def _del_source(self):
        self._source = None

    def _get_destination(self):
        return self._destination
    def _set_destination(self, destination):
        if isinstance(destination, interface):  #laki
            self._destination = destination
    def _del_destination(self):
        self._destination = None
    source = property(_get_source,_set_source,_del_source)

    destination = property(_get_destination,_set_destination,_del_destination)
