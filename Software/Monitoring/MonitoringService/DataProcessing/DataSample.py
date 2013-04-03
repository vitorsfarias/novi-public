'''
Created on Mar 4, 2013

@author: steger
'''
from DataProcessing.DataSource import DataSource
from DataProcessing.DataReader import DataReader

class DataSample(DataSource):
    pass

    def __init__(self, table):
        '''
        Constructor
        '''
        DataSource.__init__(self)
        self._data = table
    
    def __len__(self):
        return len(self._data)

    def __getitem__(self, k):
        return None

    @property
    def name(self):
        return "Original(%s)" % self._data.name

    @property
    def readerClass(self):
        return DataReader

    def _process(self):
        status = 0
        with self._data.readlock:
            if self._data.evCleared.isSet():
                self._sourcecleared()
                self._data.evCleared.clear()
                status |= self.CLEARED
            if self._data.evExpanded.isSet():
                self._sourceexpanded()
                self._data.evExpanded.clear()
                status |= self.EXPANDED
        return status
    
    @property
    def writelock(self):
        return self._data.writelock
    
    
    @property
    def um(self):
        return self._data.um
    
