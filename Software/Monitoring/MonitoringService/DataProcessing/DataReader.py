'''
Created on Nov 19, 2012

@author: steger
'''
from threading import Event, Lock
from DataProcessing.DataHeader import DataError
from DataProcessing.DataSource import DataSource

#FIXME: docs
class DataReader(object):
    '''
    This class is an extension to the DataSource class. 
    It provides an iterator over the rows / records of the DataSource.
    When the iterator is invoked several times only new records are yielded.
    In order to access rows, which have already been iterated, use the rewind() method to move the pointer to the first record.
    
    By default iteration yields all columns.
    In case user is interested in a specific slice of the table (or wants to retrieve row records on a different column order), 
    they can do so by invoking the extract method, which expects an ordered list of the interesting column names.
    Besides the column names user may indicate the requested unit, in which case iteration will yield properly transformed data.

    DataReader objects register clear and expand events in the underlying DataSource class instance in order to catch signal upon 
    new data insertion or deletion.
    '''

    def __init__(self, datasource):
        '''
        Constructor
        @param datasource: the 
        @type datasource: DataSource 
        '''
        if not isinstance(datasource, DataSource):
            raise DataError("Expect DataSource, got %s" % datasource)
        self.source = datasource
        self.sourceCleared = Event()
        self.sourceExpanded = Event()
        self.readlock = Lock()
        datasource.registerReader(self)
        self._seq = 0
        self._extractmap = None
        self._conversionmap = None
        self.extract()

    def __del__(self):
        self.source.deregisterReader(self)

    @property
    def processedrecords(self):
        '''
        @summary: This property indicates how many records are processed by this reader
        @return: the index of the record iterator
        @rtype: integer
        @note: the current value may be unreliable if an iteration is currently carried out
        '''
        return self._seq
    @processedrecords.setter
    def processedrecords(self, index):
        '''
        @summary: set the iterator to a given position. A negative index means rewinding by that many rows
        @param index: position description
        @type index: integer
        '''
        index = int(index)
        if index < 0:
            self._seq = max(0, self._seq + index)
        else:
            self._seq = min(index, len(self.source))
    @processedrecords.deleter
    def processedrecords(self):
        '''
        @summary: rewind to the first record row
        '''
        self._seq = 0

    def rewind(self):
        '''
        @summary: sets the next row record to the first item.
        '''
        del self.processedrecords
#        self.sourceCleared.clear()

#FIXME: DataSampleReader!!!
    def __iter__(self):
        with self.readlock:
            self.sourceCleared.clear()
            while self._seq < len(self.source):
                if self.sourceCleared.isSet():
                    raise DataError("Data cleared while reading records %s %s" % (self, self.source))
                self._seq += 1
                yield self._extract(self._seq - 1)
            self.sourceExpanded.clear()
            raise StopIteration
    
    def sourcecleared(self):
        with self.source.writelock:
            self.sourceCleared.set()
    
    def sourceexpanded(self):
        with self.source.writelock:
            self.sourceExpanded.set()
    
#FIXME: Sample specifik
    def headercells(self):
        '''
        @summary: iterator over those columns of the Data which are relevant (i.e. which are extracted)
        @return: generator
        @rtype: DataHeaderCell
        '''
        meta = self.source._data.header
        for i in self._extractmap:
            cellname = meta._cellnames[i]
            yield meta._cells[cellname]
            
    def extract(self, cellrequest = None):
        '''
        @summary: Presets the iterator to the first row record and selects only those columns to show and convert who are referenced in the cell request.
        This method works in a best effort manner, those column names that are not in this data table are silently omitted.
        Also in case the unit requested is not allowed by a unit model that column of data is silently ignored.
        @param cellrequest: the list of the column names and the corresponding unit to show during iteration, default is None which means show all columns without unit conversion
        @type cellrequest: list of CellRequest
        '''
        self._seq = 0
        meta = self.source._data.header
        if cellrequest is None:
            s = len(meta._cellnames[:])
            self._extractmap = range(s)
            self._conversionmap = [(None, None)] * s
        else:
            self._extractmap = []
            self._conversionmap = []
            for cellreq in cellrequest:
                for (colidx, cell) in meta.getCell( cellreq ):
                    try:
                        unit = cell.unit
                        dimension = cell.dimension
                        if cellreq.unit == unit:
                            unitmap = (None, None)
                        elif dimension.containsUnit(cellreq.unit):
                            unitmap = (unit, cellreq.unit)
                        else:
                            raise Exception("unit %s is not in the basin of dimension %s" % (unit, cell.dimension))
                    except DataError:
                        unitmap = (None, None)
                    self._extractmap.append( colidx )
                    self._conversionmap.append( unitmap )
        
    def _extract(self, idx):
        '''
        @summary: an internal helper method that takes care of extracting and ordering the columns in the order predefined by calling the extract method.
        @param idx: the row index
        @type idx: integer
        @return: a list of the cell data slice from the row pointed by the row index
        @rtype: list
        '''
        ret = []
        i = 0
        s = len(self._extractmap)
        D = self.source._data
        while i < s:
            c = self._extractmap[i]
            celldata = D[idx][c]
            sourceunit, targetunit = self._conversionmap[i]
            if sourceunit is None:
                ret.append( celldata )
            else:
                ret.append( D.um.convert(celldata, sourceunit, targetunit) )
            i += 1
        return ret
