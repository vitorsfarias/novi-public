'''
Created on Aug 10, 2011

@author: steger, gombos, matuszka
'''

from DataProcessing.MeasurementLevel import Ordinal, Ratio, Interval #Nominal
from math import sqrt
from DataProcessing.DataReader import DataReader
from DataProcessing.DataHeader import DataHeader, DataHeaderCell
from DataProcessing.DataSource import DataSource
from DataProcessing.Data import Data
from DataProcessing.DataError import AggregatorError

#FIXME: docs

class Aggregator(DataSource):
    '''
    classdocs
    @cvar cn_count: the name of the column indicating the set size before aggregation
    '''
    cn_count = 'Count'

    def __init__(self, datasource, cellrequest):
        '''
        Constructor
        @param datasource: table of records to manipulate with
        @type datasource: DataSource
        @param cellrequest: a column wise projection of the table is carried out, this column is kept
        @type cellrequest: CellRequest 
        '''
        if not isinstance(datasource, DataSource):
            raise AggregatorError("Wrong type of datasource %s" % datasource)
        DataSource.__init__(self, dependency = datasource)
        self._inputreader.extract(cellrequest = [cellrequest])
        for c in self._inputreader.headercells():
            break
        if not c.dimension.level(self.dimension_compatible):
            raise AggregatorError("The measurement level of input (%s) is not compatible with %s" % (c.dimension, self.name))
        header = DataHeader("%sAggregate(%s)" % (self.name, datasource.name))
        dimension = c.dimension
        header.addColumn(DataHeaderCell(name = self.cn_count, dimension = dimension.manager["Countable"]))
        self.cn_aggr = '%s(%s)' % (self.name, c.name)
        header.addColumn(DataHeaderCell(name = self.cn_aggr, dimension = dimension, unit = c.unit))
        self._data = Data(datasource.um, header)
        self._record = self._data.getTemplate(size = 1)
#        self.um = datasource.um
        self.source = datasource
        
        self._aggregate = None

#FIXME: ez a ketto cucc tenyleg kell?
    def __len__(self):
        return len(self._data)
    @property
    def writelock(self):
        return self._data.writelock


    @property
    def aggregate(self):
        self.process()
        return self._aggregate

    @property
    def readerClass(self):
        return DataReader

    @property
    def dimension_compatible(self):
        raise AggregatorError("dimension_compatible property is not implemented in %s" % self)
    
    
class Sum(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._aggregate = 0

    @property
    def dimension_compatible(self):
        return Interval
    
    @property
    def name(self):
        return "Sum"
    
    def _process(self):
        if self._inputreader.sourceCleared.isSet():
            self._inputreader.sourceCleared.clear()
            self._aggregate = 0
            self._inputreader.rewind()
            changed = True
        else:
            changed = False
        for (x,) in self._inputreader:
            self._aggregate += float(x)
            changed = True
        if changed:
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            return self.CLEARED | self.EXPANDED
        return self.PASS
        
class Min(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)

    @property
    def dimension_compatible(self):
        return Ordinal
    
    @property
    def name(self):
        return "Min"
    
    def _process(self):
        if self._inputreader.sourceCleared.isSet():
            self._inputreader.sourceCleared.clear()
            self._aggregate = None
            self._inputreader.rewind()
        if self._aggregate is None:
            samples = []
        else:
            samples = [self._aggregate]
        for (x,) in self._inputreader:
            samples.append( float(x) )
        newvalue = min(samples)
        if self._aggregate != newvalue:
            self._aggregate = newvalue
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            return self.CLEARED | self.EXPANDED
        return self.PASS

class Max(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)

    @property
    def dimension_compatible(self):
        return Ordinal
    
    @property
    def name(self):
        return "Max"
    
    def _process(self):
        if self._inputreader.sourceCleared.isSet():
            print "MAX src cleared"
            self._inputreader.sourceCleared.clear()
            self._aggregate = None
            self._inputreader.rewind()
        if self._aggregate is None:
            samples = []
        else:
            samples = [self._aggregate]
        for (x,) in self._inputreader:
            samples.append( float(x) )
        print "SAMPLES", samples
        newvalue = max(samples)
        print "MAX", newvalue, "of", len(self.source)
        if self._aggregate != newvalue:
            print "SETTING"
            self._aggregate = newvalue
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            print "XXX", self._record.record
            return self.CLEARED | self.EXPANDED
        return self.PASS

class Mean(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._sum = 0

    @property
    def dimension_compatible(self):
        return Ratio
    
    @property
    def name(self):
        return "Mean"
    
    def _process(self):
        changed = False
        for (x,) in self._inputreader:
            self._sum += float(x)
            changed = True
        if changed:
            self._aggregate = self._sum / float(len(self.source))
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            return self.CLEARED | self.EXPANDED
        return self.PASS

class Deviation(Aggregator):
    def __init__(self, data, cellrequest):
        Aggregator.__init__(self, data, cellrequest)
        self._emp = True
        
    @property
    def empirical(self):
        return self._emp
    @empirical.setter
    def empirical(self, emp):
        self._emp = bool(emp)

    @property
    def dimension_compatible(self):
        return Ratio
    
    @property
    def name(self):
        return "StdDev"
    
    def _process(self):
        changed = False
        aggr = 0
        data = []
        self._inputreader.rewind()
        for (x,) in self._inputreader:
            x = float(x)
            aggr += x
            data.append(x)
            changed = True
        if changed:
            n = float(len(data))
            avg = aggr / n
            s2 = map(lambda x: (x-avg)*(x-avg), data)
            if self.empirical:
                self._aggregate = sqrt(sum(s2) / (n+1))
            else:
                self._aggregate = sqrt(sum(s2) / n)
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            return self.CLEARED | self.EXPANDED
        return self.PASS

class Percentile(Aggregator):
    def __init__(self, data, cellrequest):
        self._percentile = .75
        Aggregator.__init__(self, data, cellrequest)
        
    @property
    def percentile(self):
        return self._percentile
    @percentile.setter
    def percentile(self, percentile):
        self._percentile = max(0, min(1, float(percentile)))

    @property
    def dimension_compatible(self):
        return Ordinal
    
    @property
    def name(self):
        return "Percentile_%d%%" % int(round(100 * self.percentile))
    
    def _process(self):
        data = []
        self._inputreader.rewind()
        for (x,) in self._inputreader:
            data.append(x)
        data.sort()
        n = len(data)
        p = int((n - 1) * self.percentile) 
        if n % 2:
            val = data[p]
        else:
            val = .5 * (data[p] + data[p+1])
        if self._aggregate != val:
            self._aggregate = val
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
            return self.CLEARED | self.EXPANDED
        return self.PASS
