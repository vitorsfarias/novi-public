'''
Created on Dec 20, 2012

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

from DataProcessing.Dimension import DimensionManager
from DataProcessing.DataError import DataError
from DataProcessing.Unit import UnitManager

class Cell(object):
    '''
    @summary: This class is a skeleton to represent the meta information of a single table column.
    It combines the following information:
     - the name of the cell,
     - the feature associated to the underlying data,
     - the dimension of the underlying data, 
     - the unit of the underlying data,
     @ivar name: the name of the cell
     @type name: str
     @ivar dimension: the dimension of the cell
     @type dimension: L{Dimension}
     @ivar unit: the unit of a cell, if not set, the default unit of the dimension is applied
     @type unit: L{Unit}
     @ivar feature: the metric of the column
     @type feature: str 
    '''
    def  __init__(self):
        self._name = None
        self._dimension = None
        self._unit = None
        self._feature = None

    @property
    def name(self):
        if self._name is None:
            raise DataError("name property is not set")
        return self._name
    @name.setter
    def name(self, name):
        if not isinstance(name, basestring):
            raise DataError("name is not a string")
        if name.count('.'):
            raise DataError("name must not contain any periods (%s)" % name)
        if self._name is not None and self._name != name:
            raise DataError("name property cannot be modified")
        self._name = name
    
    @property
    def dimension(self):
        if not self._dimension:
            raise DataError("dimension property is not set")
        return self._dimension
    @dimension.setter
    def dimension(self, dimension):
        if not isinstance(dimension, DimensionManager.Dimension):
            raise DataError("dimension is invalid")
        if self._unit is not None:
            if not dimension.containsUnit(self._unit):
                raise DataError("unit %s is not in the basin of dimension %s" % (self.unit, dimension))
        self._dimension = dimension
    
    @property
    def unit(self):
        if self._unit is None:
            return self.dimension.unit
        else:
            return self._unit
    @unit.setter
    def unit(self, unit):
        if not isinstance(unit, UnitManager.Unit):
            raise DataError("unit is invalid")
        if self._dimension is not None:
            if not self.dimension.containsUnit(unit):
                raise DataError("unit %s is not in the basin of dimension %s" % (unit, self.dimension))
        self._unit = unit
    
    @property
    def feature(self):
        if self._feature is None:
            raise DataError("feature property is not set")
        return self._feature
    @feature.setter
    def feature(self, feature):
        if self._feature is not None and self._feature != feature:
            raise DataError("feature property cannot be modified")
        self._feature = feature
        
    def __eq__(self, cell):
        '''
        @summary: comparison operator of two columns' meta
        @return: True if column names, features, units and dimensions match
        @rtype: bool
        '''
        if not isinstance(cell, Cell):
            raise DataError("type error expecting Cell for comparison")
        return self._name == cell._name and self._feature == cell._feature and self._unit == cell._unit and self._dimension == cell._dimension 
    
    def __ne__(self, cell):
        '''
        @summary: comparison operator of two columns' meta
        @return: True if column names or their units differ
        @rtype: bool
        '''
        return not self.__eq__(cell)

class DataHeaderCell(Cell):
    '''
    @summary: represents meta information of a single column
    '''
    def __init__(self, name, dimension, feature = None, unit = None):
        '''
        @summary: constructor
        @param name: the nema of the cell
        @type name: str
        @param dimension: the dimension of the cell
        @type dimension: L{Dimension}
        @param feature: pointer if it is a monitoring feature
        @param unit: indicates the unit of a column if it is different from the default
        @type unit: L{Unit}
        '''
        Cell.__init__(self)
        self.name = name
        self.dimension = dimension
        if unit is not None and unit != dimension.unit:
            self.unit = unit
        if feature is not None:
            self.feature = feature

class CellRequest(Cell):
    '''
    @summary: skeleton, which is used to search the among meta information. It is basically a cell with missing certain details
    '''
    pass

class CellRequestByName(CellRequest):
    '''
    @summary: This class represents the user request for a data column matching the name of the column.
    One can specify the requested unit.
    '''
    def __init__(self, name, unit = None):
        '''
        @summary: Constructor
        @param name: the name of the requested column
        @type name: str
        @param unit: the requested unit, default is None, which means no conversion request
        @type unit: L{Unit} or None 
        '''
        Cell.__init__(self)
        self.name = name
        if unit is not None:
            self.unit = unit
    
    def __eq__(self, cell):
        return self.name == cell.name

class CellRequestByFeature(CellRequest):
    '''
    @author: steger, jozsef
    @summary: 
    This class represents the user request for a data column(s) matching the feature of the column.
    One can also specify the requested unit.
    '''
    def __init__(self, feature, unit = None):
        '''
        @summary: Constructor
        @param feature: the feature of the requested column
        @type feature: str
        @param unit: the requested unit, default is None, which means no conversion request
        @type unit: L{Unit} or None 
        '''
        Cell.__init__(self)
        self.feature = feature
        if unit is not None:
            self.unit = unit

    def __eq__(self, cell):
        return self.feature == cell.feature
