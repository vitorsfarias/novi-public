'''
Created on Dec 31, 2012

@author: steger
'''
from DataProcessing.DataReader import DataReader

class DataIndex(DataReader):
    '''
    classdocs
    '''

    def __init__(self, datasource, key):
        '''
        Constructor
        '''
        DataReader.__init__(self, datasource)
        self.indexmap = {}
        self.extract(cellrequest = key)
    
    def buildindex(self):
        i = len(self.indexmap)
        for k in self:
            self.indexmap[tuple(k)] = i
            i += 1
    
    def __getitem__(self, k):
        if self.sourceCleared.isSet():
            self.sourceCleared.clear()
            self.indexmap.clear()
            self.buildindex()
        try:
            iter(k)
        except TypeError:
            k = (k,)
        if not self.indexmap.has_key(k) and self.sourceExpanded.isSet():
            self.sourceExpanded.clear()
            self.buildindex()
        return self.source._rawrecords[ self.indexmap[k] ]