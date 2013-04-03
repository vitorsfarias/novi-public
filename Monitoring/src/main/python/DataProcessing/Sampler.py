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
        DataSource.__init__(self, datasource)
        self._reader = DataReader(datasource = datasource._data)
        header = DataHeader("%sSample(%s)" % (self.name, self.source.name))
        for c in self._reader.headercells():
            header.addColumn(c)
        self._data = Data(self.um, header)
    
    def _get_header(self):
        return self._data.header

    def __len__(self):
        self.process()
        return len(self._data)

    def __getitem__(self, k):
        return self._data._rawrecords.__getitem__(k)



    header = property(_get_header,None,None)
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

    def _get_name(self):
        return "Head"

    def _get_head(self):
        return self._head
    def _set_head(self, head):
        self._head = int(head)
        self._data.clear()
        self._reader.rewind()
    
    def _process(self):
        if self._reader.sourceCleared.isSet():
            self._reader.sourceCleared.clear()
            self._reader.rewind()
            self._data.clear()
        if len(self._data) == self.head:
            return
        for x in self._reader:
            self._data._rawrecords.append(x)
            if len(self._data) == self.head:
                self._data._onexpand()
                return
        raise SamplerError("Not enough sample %d/%d" % (len(self._data), self.head))
        

    head = property(_get_head,_set_head,None)

    name = property(_get_name,None,None)
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
    
    def _get_name(self):
        return "Tail"

    def _get_tail(self):
        return self._tail
    def _set_tail(self, tail):
        self._tail = int(tail)
        self._data.clear()
        self._reader.rewind()

    def _process(self):
        clear = False
        if self._reader.sourceCleared.isSet():
            self._reader.sourceCleared.clear()
            self._reader.rewind()
            self._data.clear()
        for x in self._reader:
            if len(self._data) == self.tail:
                self._data._rawrecords.pop(0)
                clear = True
            self._data._rawrecords.append(x)
        if clear:
            self._data._onclear()
        if len(self._data) == self.tail:
            self._data._onexpand()
        else:
            raise SamplerError("Not enough sample %d/%d" % (len(self._data), self.tail))


    tail = property(_get_tail,_set_tail,None)

    name = property(_get_name,None,None)
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
    
    def _get_name(self):
        return "Sort"
    
    def _get_ascending(self):
        return self._asc
    def _set_ascending(self, ascending):
        if bool(ascending) != self._asc:
            self._asc = bool(ascending)
            self._data.clear()
            self._reader.rewind()
    
    def _get_key(self):
        return self._key
    def _set_key(self, key):
        self._key = int(key)
    
    def _get_keycell(self):
        raise SamplerError("don't read this property")
    def _set_keycell(self, cellrequest):
        for idx, _ in self.source.header.getCell(cellrequest):
            self._key = idx
            break
    
    def _process(self):
        clear = False
        if self._reader.sourceCleared.isSet():
            self._reader.sourceCleared.clear()
            clear = True
        if self._reader.sourceExpanded.isSet():
            self._reader.sourceExpanded.clear()
            clear = True
        if not clear:
            return
        self._reader.rewind()
        self._data.clear()
        self._data._rawrecords = sorted(self._reader.source._rawrecords, key=lambda r: r[self.key], reverse = not self.ascending)
        if len(self._data):
            self._data._onclear()
            self._data._onexpand()
        else:
            raise SamplerError("Not enough sample...")


    keycell = property(_get_keycell,_set_keycell,None)

    name = property(_get_name,None,None)

    key = property(_get_key,_set_key,None)

    ascending = property(_get_ascending,_set_ascending,None)
