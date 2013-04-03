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
    
    @property
    def sliceid(self):
        if self._sliceid is None:
            raise Exception("slice id is not set")
        return self._sliceid
    @sliceid.setter
    def sliceid(self, sliceid):
        self._sliceid = sliceid
    @sliceid.deleter
    def sliceid(self):
        self._sliceid = None

    @property
    def name(self):
        return self._name
    @name.setter
    def name(self, name):
        self._name = name
    @name.deleter
    def name(self):
        self._name = ""
        