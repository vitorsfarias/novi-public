'''
Created on Jun 15, 2012

@author: steger
'''

from gi.repository import Gtk
from Resource.resource import resource

class w_core(Gtk.Widget):
    def __init__(self, resource = None, label = None):
        Gtk.Widget.__init__(self)
        self._resource = resource
        self._label = label
        self.connect('draw', self.draw)
        self.queue_draw()
    
    @property
    def label(self):
        if self._label:
            return self._label
        elif self._resource:
            return self._resource.name
        raise Exception("label property is not set")
    @label.setter
    def label(self, label):
        self._label = label
    @label.deleter
    def label(self):
        if self._resource:
            self._label = self.resource.name
        else:
            self._label = None
    
    @property
    def resource(self):
        return self._resource
    @resource.setter
    def resource(self, r):
        if not isinstance(r, resource):
            raise Exception("A resource must be a Resource.resource type, I got %s" % r)
        self._resource = r
    @resource.deleter
    def resource(self):
        self._resource = None
        
    @property
    def widgetid(self):
        if self._resource:
            return self._resource.resourceid
        elif self._label:
            return self._label
        raise Exception("label and resource properties are not set")
    @widgetid.setter
    def widgetid(self, nodeid):
        raise Exception("widgetid is read only property")
    @widgetid.deleter
    def widgetid(self):
        raise Exception("widgetid is read only property")
        
    def draw(self):
        raise Exception("draw method not implemented")