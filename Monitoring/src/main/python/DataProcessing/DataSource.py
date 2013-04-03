'''
Created on Dec 10, 2012

@author: steger
'''
from DataProcessing.DataHeader import DataError
from DataProcessing.DataError import SamplerError

class DataSource(object):
    def __init__(self, datasource):
        self._readers = set()
        self._source = datasource
        self._data = None
        self.um = datasource.um
    
    def _get_source(self):
        return self._source

    def _get_data(self):
        self.process()
        return self._data

    def _get_name(self):
        raise DataError("%s must implement name property" % self)

    def _get_readlock(self):
        return self.source.readlock

    def _get_writelock(self):
        return self.source.writelock

    def __len__(self):
        raise DataError("%s must implement __len__ method" % self)

    def __getitem__(self, k):
        raise DataError("%s must implement __getitem__ method" % self)

    def process(self):
        '''
        @summary: recursively process data records of the source chain
        '''
        if self != self.source:
            self.source.process()
            self._process()

    def _process(self):
        raise DataError("%s must implement _process method" % self)
    
    def _onclear(self):
        for r in self._readers:
            r.sourcecleared()
    
    def _onexpand(self):
        for r in self._readers:
            r.sourceexpanded()
    
    def registerReader(self, reader):
        '''
        @summary: registers a reader to catch clear and update events
        @param reader: data consumer
        @type reader: DataReader 
        @raise DataError: wrong argument
        '''
        try:
            self._readers.add(reader)
            try:
                if len(self):
                    self._onexpand()
                else:
                    self._onclear()
            except SamplerError:
                pass
        except AttributeError:
            self._readers.remove(reader)
            raise DataError("Expecting a DataReader, got %s" % reader)
    
    def deregisterReader(self, reader):
        '''
        @summary: removes a registered reader
        @param reader: data consumer
        @type reader: DataReader 
        '''
        try:
            self._readers.remove(reader)
        except KeyError:
            pass

    readlock = property(_get_readlock,None,None)

    source = property(_get_source,None,None)

    data = property(_get_data,None,None)

    name = property(_get_name,None,None)

    writelock = property(_get_writelock,None,None)
