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
    @author: steger, jozsef
    @summary: 
    This class represents meta information of a single table column. 
    It stores the following information:
     - the name of the cell,
     - the feature associated to the underlying data,
     - the dimension of the underlying data, 
     - the unit of the underlying data,
    '''
    def  __init__(self):
        self._name = None
        self._dimension = None
        self._unit = None
        self._feature = None

    def _get_name(self):
        if self._name is None:
            raise DataError("name property is not set")
        return self._name
    def _set_name(self, name):
        if not isinstance(name, basestring):
            raise DataError("name is not a string")
        if name.count('.'):
            raise DataError("name must not contain any periods (%s)" % name)
        if self._name is not None and self._name != name:
            raise DataError("name property cannot be modified")
        self._name = name
    
    def _get_dimension(self):
        if not self._dimension:
            raise DataError("dimension property is not set")
        return self._dimension
    def _set_dimension(self, dimension):
        if not isinstance(dimension, DimensionManager.Dimension):
            raise DataError("dimension is invalid")
        if self._unit is not None:
            if not dimension.containsUnit(self._unit):
                raise DataError("unit %s is not in the basin of dimension %s" % (self.unit, dimension))
        self._dimension = dimension
    
    def _get_unit(self):
        if self._unit is None:
            return self.dimension.unit
        else:
            return self._unit
    def _set_unit(self, unit):
        if not isinstance(unit, UnitManager.Unit):
            raise DataError("unit is invalid")
        if self._dimension is not None:
            if not self.dimension.containsUnit(unit):
                raise DataError("unit %s is not in the basin of dimension %s" % (unit, self.dimension))
        self._unit = unit
    
    def _get_feature(self):
        if self._feature is None:
            raise DataError("feature property is not set")
        return self._feature
    def _set_feature(self, feature):
        if self._feature is not None and self._feature != feature:
            raise DataError("feature property cannot be modified")
        self._feature = feature
        
    def __eq__(self, cell):
        if not isinstance(cell, Cell):
            raise DataError("type error expecting Cell for comparison")
        return self._name == cell._name and self._feature == cell._feature and self._unit == cell._unit and self._dimension == cell._dimension 
    
    def __ne__(self, cell):
        '''
        @summary: comparison operator of column types.
        @return: True if column names or their units differ
        @rtype: boolean
        '''
        return not self.__eq__(cell)



    feature = property(_get_feature,_set_feature,None)

    name = property(_get_name,_set_name,None)

    unit = property(_get_unit,_set_unit,None)

    dimension = property(_get_dimension,_set_dimension,None)
class DataHeaderCell(Cell):
    def __init__(self, name, dimension, feature = None, unit = None):
        Cell.__init__(self)
        self.name = name
        self.dimension = dimension
        if unit is not None:
            self.unit = unit
        if feature is not None:
            self.feature = feature

class CellRequest(Cell): pass

class CellRequestByName(CellRequest):
    '''
    @author: steger, jozsef
    @summary: 
    This class represents the user request for a data column matching the name of the column.
    One can specify the requested unit.
    '''
    def __init__(self, name, unit = None):
        '''
        @summary: Constructor
        @param name: the name of the requested column
        @type name: string
        @param unit: the requested unit, default is None, which means no conversion request
        @type unit: string or None 
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
    One can specify the requested unit.
    '''
    def __init__(self, feature, unit = None):
        '''
        @summary: Constructor
        @param feature: the feature of the requested column
        @type feature: string
        @param unit: the requested unit, default is None, which means no conversion request
        @type unit: string or None 
        '''
        Cell.__init__(self)
        self.feature = feature
        if unit is not None:
            self.unit = unit

    def __eq__(self, cell):
        return self.feature == cell.feature
