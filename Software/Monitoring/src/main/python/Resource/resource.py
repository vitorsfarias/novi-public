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

    def _get_name(self):
        if self._name is None:
            raise Exception("resource name is not set")
        return self._name
    def _set_name(self, name):
        self._name = self.ipret(name)
    def _del_name(self):
        self._name = None

    def _get_resourceid(self):
        if self._resourceid is None:
            raise Exception("resource id is not set")
        return self._resourceid
    def _set_resourceid(self, resourceid):
        self._resourceid = resourceid
    def _del_resourceid(self):
        self._resourceid = None
    resourceid = property(_get_resourceid,_set_resourceid,_del_resourceid)

    name = property(_get_name,_set_name,_del_name)
