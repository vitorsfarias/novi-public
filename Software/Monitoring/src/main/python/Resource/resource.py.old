'''
Created on May 31, 2012

@author: steger
'''

class resource(object):
    def __init__(self, name = None, resourceid = None):
        self._name = name
        self._resourceid = resourceid

    @staticmethod
    def ipret(x):
        if not x:
            return None
        x = str(x)
        if len(x):
            return x
        else:
            return None

    @property
    def name(self):
        if self._name is None:
            raise Exception("resource name is not set")
        return self._name
    @name.setter
    def name(self, name):
        self._name = self.ipret(name)
    @name.deleter
    def name(self):
        self._name = None

    @property
    def resourceid(self):
        if self._resourceid is None:
            raise Exception("resource id is not set")
        return self._resourceid
    @resourceid.setter
    def resourceid(self, resourceid):
        self._resourceid = resourceid
    @resourceid.deleter
    def resourceid(self):
        self._resourceid = None