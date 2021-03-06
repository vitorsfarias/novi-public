from __future__ import with_statement
'''
Created on Nov 19, 2012

@author: steger
'''
from DataProcessing.DataHeader import DataError
from threading import Event
from DataProcessing.DataSource import DataSource

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

    DataReader objects register their ready Event in their Data class in order to catch signal upon new data insertion.
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
        self._seq = 0
        self._extractmap = None
        self._conversionmap = None
        self.sourceCleared = Event()
        self.sourceExpanded = Event()
        datasource.registerReader(self)
        self.extract()

    def __del__(self):
        self.source.deregisterReader(self)

    def _get_processedrecords(self):
        '''
        @summary: This property indicates how many records are processed by this reader
        @return: the index of the record iterator
        @rtype: integer
        @note: the current value may be unreliable if an iteration is currently carried out
        '''
        return self._seq
    def _set_processedrecords(self, index):
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
    def _del_processedrecords(self):
        '''
        @summary: rewind to the first record row
        '''
        self._seq = 0

    def rewind(self):
        '''
        @summary: sets the next row record to the first item.
        '''
        del self.processedrecords
        self.sourceCleared.clear()

    def __iter__(self):
        with self.source.readlock:
            if self.sourceCleared.isSet() and self.sourceExpanded.isSet():
                self.sourceCleared.clear()
            while self._seq < len(self.source):
                if self.sourceCleared.isSet():
                    raise DataError("Data cleared while reading records %s %s" % (self, self.source))
                self._seq += 1
                yield self._extract(self._seq - 1)
            self.sourceExpanded.clear()
        raise StopIteration
    
    def sourcecleared(self):
        self.sourceCleared.set()
    
    def sourceexpanded(self):
        self.sourceExpanded.set()
    
    def headercells(self):
        '''
        @summary: iterator over those columns of the Data which are relevant (i.e. which are extracted)
        @return: generator
        @rtype: DataHeaderCell
        '''
        for i in self._extractmap:
            cellname = self.source.header._cellnames[i]
            yield self.source.header._cells[cellname]
            
    def extract(self, cellrequest = None):
        '''
        @summary: Presets the iterator to the first row record and selects only those columns to show and convert who are referenced in the cell request.
        This method works in a best effort manner, those column names that are not in this data table are silently omitted.
        Also in case the unit requested is not allowed by a unit model that column of data is silently ignored.
        @param cellrequest: the list of the column names and the corresponding unit to show during iteration, default is None which means show all columns without unit conversion
        @type cellrequest: list of CellRequest
        '''
        self._seq = 0
        if cellrequest is None:
            s = len(self.source.header._cellnames[:])
            self._extractmap = range(s)
            self._conversionmap = [(None, None)] * s
        else:
            self._extractmap = []
            self._conversionmap = []
            for cellreq in cellrequest:
                for (colidx, cell) in self.source.header.getCell( cellreq ):
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
        D = self.source
        while i < s:
            c = self._extractmap[i]
            celldata = D[idx][c]
            sourceunit, targetunit = self._conversionmap[i]
            if sourceunit is None:
                ret.append( celldata )
            else:
                ret.append( self.source.um.convert(celldata, sourceunit, targetunit) )
            i += 1
        return ret

    processedrecords = property(_get_processedrecords,_set_processedrecords,_del_processedrecords)
