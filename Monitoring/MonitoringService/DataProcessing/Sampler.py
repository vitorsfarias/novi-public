'''
Created on Nov 20, 2012

@author: steger
'''
from DataProcessing.DataReader import DataReader
from DataProcessing.DataHeader import DataHeader
from DataProcessing.DataSource import DataSource
from DataProcessing.Data import Data
from DataProcessing.DataError import SamplerError

class Sampler(DataSource):
    '''
    classdocs
    '''

    def __init__(self, datasource):
        '''
        Constructor
        @param datasource: table of records to manipulate with
        @type datasource: DataSource
        '''
        if not isinstance(datasource, DataSource):
            raise SamplerError("Wrong type of datasource %s" % datasource)
        DataSource.__init__(self, dependency = datasource)
        self.source = datasource
        header = DataHeader("%sSample(%s)" % (self.name, self.source.name))
        for c in self._inputreader.headercells():
            header.addColumn(c)
        self.um = self.source.um
        self._data = Data(self.um, header)

    @property
    def readerClass(self):
        return DataReader
    
    @property
    def header(self):
        return self._data.header

    def __len__(self):
        self.process()
        return len(self._data)

    def __getitem__(self, k):
        return self._data._rawrecords.__getitem__(k)
    
    @property
    def writelock(self):
        return self._data.writelock


class Head(Sampler):
    def __init__(self, datasource, head = 10):
        '''
        Constructor
        @param datasource: table of records to manipulate with
        @type datasource: DataSource
        @param head: the top n records of the table
        @type head: int 
        '''
        Sampler.__init__(self, datasource)
        self._head = head

    @property
    def name(self):
        return "Head"

    @property
    def head(self):
        return self._head
    @head.setter
    def head(self, head):
        self._head = int(head)
        self._data.clear()
        self._inputreader.rewind()
    
    def _process(self):
        status = self.PASS
        if self._inputreader.sourceCleared.isSet():
            self._inputreader.sourceCleared.clear()
            self._inputreader.rewind()
            self._data.clear()
            status |= self.CLEARED
        if len(self._data) == self.head:
            return status
        for x in self._inputreader:
            self._data._rawrecords.append(x)
            if len(self._data) == self.head:
                status |= self.EXPANDED
                return status
        raise SamplerError("Not enough sample %d/%d" % (len(self._data), self.head))
        
class Tail(Sampler):
    def __init__(self, datasource, tail = 10):
        '''
        Constructor
        @param datasource: table of records to manipulate with
        @type datasource: DataSource
        @param tail: the last n records of the table
        @type tail: int 
        '''
        Sampler.__init__(self, datasource)
        self._tail = tail
    
    @property
    def name(self):
        return "Tail"

    @property
    def tail(self):
        return self._tail
    @tail.setter
    def tail(self, tail):
        self._tail = int(tail)
        self._data.clear()
        self._inputreader.rewind()

    def _process(self):
        status = self.PASS
        clear = False
        if self._inputreader.sourceCleared.isSet():
            self._inputreader.sourceCleared.clear()
            self._inputreader.rewind()
            self._data.clear()
        for x in self._inputreader:
            if len(self._data) == self.tail:
                self._data._rawrecords.pop(0)
                clear = True
            self._data._rawrecords.append(x)
        if clear:
            status |= self.CLEARED
        if len(self._data) == self.tail:
            status |= self.EXPANDED
            return status
        else:
            raise SamplerError("Not enough sample %d/%d" % (len(self._data), self.tail))

class Sorter(Sampler):
    def __init__(self, datasource, keycell = None, ascending = True):
        '''
        Constructor
        @param datasource: table of records to manipulate with
        @type datasource: DataSource
        @param keycell: the key column to use for sorting
        @type keycell: CellRequest or None
        @param ascending: indicate the sortin order
        @type ascending: bool 
        '''
        Sampler.__init__(self, datasource)
        self._asc = ascending
        self._key = 0
        if keycell:
            self.keycell = keycell
    
    @property
    def name(self):
        return "Sort"
    
    @property
    def ascending(self):
        return self._asc
    @ascending.setter
    def ascending(self, ascending):
        if bool(ascending) != self._asc:
            self._asc = bool(ascending)
            self._data.clear()
            self._inputreader.rewind()
    
    @property
    def key(self):
        return self._key
    @key.setter
    def key(self, key):
        self._key = int(key)
    
    @property
    def keycell(self):
        raise SamplerError("don't read this property")
    @keycell.setter
    def keycell(self, cellrequest):
        for idx, _ in self.source._data.header.getCell(cellrequest):
            self._key = idx
            break
    
    def _process(self):
        status = self.PASS
        if self._inputreader.sourceCleared.isSet():
            self._inputreader.sourceCleared.clear()
            status |= self.CLEARED
        if self._inputreader.sourceExpanded.isSet():
            self._inputreader.sourceExpanded.clear()
            status |= self.CLEARED
        if status == self.PASS:
            return self.PASS
        self._inputreader.rewind()
        self._data.clear()
        self._data._rawrecords = sorted(self._inputreader.source._data._rawrecords, key=lambda r: r[self.key], reverse = not self.ascending)
        if len(self._data):
            status |= self.EXPANDED
            return status
        else:
            raise SamplerError("Not enough sample...")

