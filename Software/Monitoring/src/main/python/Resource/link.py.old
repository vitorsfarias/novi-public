'''
Created on May 31, 2012

@author: steger
'''
from Resource.resource import resource
from Resource.node import node

class link(resource):
    def __init__(self, name = None, resourceid = None, source = None, destination = None):
        resource.__init__(self, name, resourceid)
        self._source = source
        self._destination = destination
    
    @property
    def source(self):
        return self._source
    @source.setter
    def source(self, source):
        if isinstance(source, node):
            self._source = source
    @source.deleter
    def source(self):
        self._source = None

    @property
    def destination(self):
        return self._destination
    @destination.setter
    def destination(self, destination):
        if isinstance(destination, node):
            self._destination = destination
    @destination.deleter
    def destination(self):
        self._destination = None