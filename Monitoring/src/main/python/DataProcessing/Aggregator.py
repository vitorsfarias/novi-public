from __future__ import with_statement
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

class Aggregator(DataSource):
    '''
    classdocs
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
        DataSource.__init__(self, datasource)
        self._reader = DataReader(datasource = datasource._data)
        self._reader.extract(cellrequest = [cellrequest])
        for c in self._reader.headercells():
            break
        if not c.dimension.level(self.dimension_compatible):
            raise AggregatorError("The measurement level of input (%s) is not compatible with %s" % (c.dimension, self.name))
        header = DataHeader("%sAggregate(%s)" % (self.name, self.source.name))
        dimension = c.dimension
        header.addColumn(DataHeaderCell(name = self.cn_count, dimension = dimension.manager["Countable"]))
        self.cn_aggr = '%s(%s)' % (self.name, c.name)
        header.addColumn(DataHeaderCell(name = self.cn_aggr, dimension = dimension, unit = c.unit))
        self._data = Data(self.um, header)
        self._record = self._data.getTemplate(size = 1)
    
    def _get_dimension_compatible(self):
        raise AggregatorError("dimension_compatible property is not implemented in %s" % self)
    
    

    dimension_compatible = property(_get_dimension_compatible,None,None)
class Sum(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._aggregate = 0

    def _get_dimension_compatible(self):
        return Interval
    
    def _get_name(self):
        return "Sum"
    
    def _process(self):
        if self._reader.sourceCleared.isSet():
            self._reader.sourceCleared.clear()
            self._aggregate = 0
            self._reader.rewind()
            changed  = True
        else:
            changed = False
        for (x,) in self._reader:
            self._aggregate += float(x)
            changed = True
        if changed:
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)
        

    dimension_compatible = property(_get_dimension_compatible,None,None)

    name = property(_get_name,None,None)
class Min(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._aggregate = None

    def _get_dimension_compatible(self):
        return Ordinal
    
    def _get_name(self):
        return "Min"
    
    def _process(self):
        changed = False
        if self._aggregate is None:
            for (x,) in self._reader:
                changed = True
                self._aggregate = float(x)
                break
        sample = [self._aggregate]
        for (x,) in self._reader:
            sample.append( float(x) )
            changed = True
        if changed:
            self._aggregate = min(sample)
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)


    dimension_compatible = property(_get_dimension_compatible,None,None)

    name = property(_get_name,None,None)
class Max(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._aggregate = None

    def _get_dimension_compatible(self):
        return Ordinal
    
    def _get_name(self):
        return "Max"
    
    def _process(self):
        changed = False
        if self._aggregate is None:
            for (x,) in self._reader:
                changed = True
                self._aggregate = float(x)
                break
        sample = [self._aggregate]
        for (x,) in self._reader:
            sample.append( float(x) )
            changed = True
        if changed:
            self._aggregate = max(sample)
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)


    dimension_compatible = property(_get_dimension_compatible,None,None)

    name = property(_get_name,None,None)
class Mean(Aggregator):
    def __init__(self, datasource, cellrequest):
        Aggregator.__init__(self, datasource, cellrequest)
        self._sum = 0
        self._aggregate = None

    def _get_dimension_compatible(self):
        return Ratio
    
    def _get_name(self):
        return "Mean"
    
    def _process(self):
        changed = False
        for (x,) in self._reader:
            self._sum += float(x)
            changed = True
        if changed:
            self._aggregate = self._sum / float(len(self.source))
            self._data.clear()
            self._record.update(name = self.cn_aggr, values = (self._aggregate,))
            self._record.update(name = self.cn_count, values = (len(self.source),))
            self._data.saveRecord(self._record)


    dimension_compatible = property(_get_dimension_compatible,None,None)

    name = property(_get_name,None,None)
class Deviation(Aggregator):
    def __init__(self, data, cellrequest):
        Aggregator.__init__(self, data, cellrequest)
        self._aggregate = None
        self._emp = True
        
    def _get_empirical(self):
        return self._emp
    def _set_empirical(self, emp):
        self._emp = bool(emp)

    def _get_dimension_compatible(self):
        return Ratio
    
    def _get_name(self):
        return "StdDev"
    
    def _process(self):
        changed = False
        aggr = 0
        data = []
        self._reader.rewind()
        for (x,) in self._reader:
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


    dimension_compatible = property(_get_dimension_compatible,None,None)

    empirical = property(_get_empirical,_set_empirical,None)

    name = property(_get_name,None,None)
class Percentile(Aggregator):
    def __init__(self, data, cellrequest):
        self._aggregate = None
        self._percentile = .75
        Aggregator.__init__(self, data, cellrequest)
        
    def _get_percentile(self):
        return self._percentile
    def _set_percentile(self, percentile):
        self._percentile = max(0, min(1, float(percentile)))

    def _get_dimension_compatible(self):
        return Ordinal
    
    def _get_name(self):
        return "Percentile_%d%%" % int(round(100 * self.percentile))
    
    def _process(self):
        data = []
        self._reader.rewind()
        for (x,) in self._reader:
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

    dimension_compatible = property(_get_dimension_compatible,None,None)

    percentile = property(_get_percentile,_set_percentile,None)

    name = property(_get_name,None,None)
