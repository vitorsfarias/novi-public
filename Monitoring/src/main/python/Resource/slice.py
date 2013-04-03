'''
Created on Oct 30, 2012

@author: steger
'''

class slice_pointer(object):
    '''
    classdocs
    '''

    def __init__(self, sliceid = None, slicename = ""):
        '''
        Constructor
        '''
        self._sliceid = sliceid
        self._name = slicename
    
    def _get_sliceid(self):
        if self._sliceid is None:
            raise Exception("slice id is not set")
        return self._sliceid
    def _set_sliceid(self, sliceid):
        self._sliceid = sliceid
    def _del_sliceid(self):
        self._sliceid = None

    def _get_name(self):
        return self._name
    def _set_name(self, name):
        self._name = name
    def _del_name(self):
        self._name = ""
        
    sliceid = property(_get_sliceid,_set_sliceid,_del_sliceid)

    name = property(_get_name,_set_name,_del_name)
