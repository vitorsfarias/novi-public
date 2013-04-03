from __future__ import with_statement
'''
Created on Feb 27, 2012

@author: steger
'''
from DataProcessing.Unit import UnitManager
from DataProcessing.MeasurementLevel import MeasurementLevel, Interval, Ratio
from DataProcessing.DataError import DimensionError

class DimensionManager(object):
    '''
    @summary: the dimension container
    '''    
    class Dimension(object):
        def __init__(self, dimensionmanager, reference, name, unit, level):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: DimensionManager  
            @param reference: the reference to the dimension
            @type reference: str
            @param symbol: a human readable name of the dimension
            @type symbol: str
            @param unit: the default unit of the dimension
            @type base: Unit
            @param level: the measurement level of the dimension
            @type level: MeasurementLevel
            @note: the level is not a class instance
            @raise DimensionError: Wrong type of unit / Wrong type of level
            '''
            if not isinstance(unit, UnitManager.Unit):
                raise DimensionError("Wrong type of unit %s" % unit)
            try:
                if not issubclass(level, MeasurementLevel):
                    raise DimensionError("Wrong type of level %s" % level)
            except TypeError:
                raise DimensionError("Wrong type of level %s" % level)
            self._data = (dimensionmanager, reference, name, unit)
            self._level = level
        def _get_manager(self):
            return self._data[0]
        def _get_unitmanager(self):
            return self._data[0].unitmanager
        def _get_reference(self):
            return self._data[1]
        def _get_name(self):
            return self._data[2]
        def _get_unit(self):
            return self._data[3]
        def _get_basin(self):
            return self.unitmanager.getBasinByUnit(self.unit)
        def level(self, level):
            '''
            @summary: check measurement level against the given level
            @param level: measurement level
            @type level: MeasurementLevel
            @return: True if the measurement level given as a parameter 
            is the same or looser than the level of the dimension
            @rtype: bool
            @raise DimensionError: Wrong type of level
            '''
            if not issubclass(level, MeasurementLevel):
                raise DimensionError("Wrong type of level %s" % level)
            return issubclass(self._level, level)
        def __str__(self):
            return "%s [%s]" % (self.name, self.unit)
        def __eq__(self, d):
            if not isinstance(d, DimensionManager.Dimension):
                raise DimensionError("wrong type")
            return self._level == d._level and self.containsUnit(d.unit)
        def containsUnit(self, unit):
            '''
            @summary: checks if a given unit is in the basin of this dimension
            @param unit: the unit to check
            @type unit: UnitModel.Unit
            @return: true if the unit is applicable for this dimension
            @rtype: bool
            '''
            return unit in self.unitmanager.getBasinByUnit(self.unit)
    

        name = property(_get_name,None,None)

        reference = property(_get_reference,None,None)

        manager = property(_get_manager,None,None)

        unitmanager = property(_get_unitmanager,None,None)

        basin = property(_get_basin,None,None)

        unit = property(_get_unit,None,None)
    class DerivedDimension(Dimension): 
        def ancestors(self):
            '''
            @summary: iterate over all ancestors this dimension is derived from
            @return: generator over ancestors
            @rtype: Dimension
            '''
            for d in self._ancestor:
                yield d

    class BaseDimension(Dimension): pass

    class DifferenceDimension(DerivedDimension):
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: DimensionManager  
            @param reference: the reference to the dimension
            @type reference: str
            @param symbol: a human readable name of the dimension
            @type symbol: str
            @param unit: the default unit of the dimension
            @type base: Unit
            @param derivedfrom: the ancestor dimension this dimension is derived from
            @type derivedfrom: Dimension
            @note: the level is not a class instance
            @raise DimensionError: Wrong type of derivedfrom
            '''
            if not isinstance(derivedfrom, DimensionManager.Dimension):
                raise DimensionError("Wrong type of derivedfrom")
            if not derivedfrom.level(Interval):
                raise DimensionError("Cannot subtract %s" % derivedfrom)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = derivedfrom
    
    class PowerDimension(DerivedDimension):
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom, exponent):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: DimensionManager  
            @param reference: the reference to the dimension
            @type reference: str
            @param symbol: a human readable name of the dimension
            @type symbol: str
            @param unit: the default unit of the dimension
            @type base: Unit
            @param derivedfrom: the ancestor dimension this dimension is derived from
            @type derivedfrom: Dimension
            @param exponent: dimension is a derivative of the derivedfrom dimension, by raising to power exponent
            @type exponent: int  
            @note: the level is not a class instance
            @raise DimensionError: Wrong type of derivedfrom / Cannot power
            '''
            if not isinstance(derivedfrom, DimensionManager.Dimension):
                raise DimensionError("Wrong type of derivedfrom")
            if not derivedfrom.level(Ratio):
                raise DimensionError("Cannot power %s" % derivedfrom)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = (derivedfrom,)
            self._exponent = exponent
        def _get_exponent(self): return self.__get_exponent


        exponent = property(_get_exponent,None,None)
    class ProductDimension(DerivedDimension):
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: DimensionManager  
            @param reference: the reference to the dimension
            @type reference: str
            @param symbol: a human readable name of the dimension
            @type symbol: str
            @param unit: the default unit of the dimension
            @type base: Unit
            @param derivedfrom: the set of dimensions that compose this dimension
            @type derivedfrom: tuple(Dimension)
            @note: the level is not a class instance
            @raise DimensionError: Wrong type of derivedfrom / ProductDimension is derived from more than 2 Dimensions / Cannot be a factor
            '''
            if not isinstance(derivedfrom, tuple):
                raise DimensionError("Wrong type of derivedfrom")
            if len(derivedfrom) < 2:
                raise DimensionError("ProductDimension is derived from more than 2 Dimensions, got %d instead" % len(derivedfrom))
            for d in derivedfrom:
                if not d.level(Ratio):
                    raise DimensionError("%s cannot be a factor" % d)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = derivedfrom

    class RatioDimension(DerivedDimension):
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: DimensionManager  
            @param reference: the reference to the dimension
            @type reference: str
            @param symbol: a human readable name of the dimension
            @type symbol: str
            @param unit: the default unit of the dimension
            @type base: Unit
            @param derivedfrom: the set of dimensions that compose this dimension
            @type derivedfrom: tuple(Dimension)
            @note: the level is not a class instance
            @raise DimensionError: Wrong type of derivedfrom / Cannot be a factor
            '''
            if not isinstance(derivedfrom, DimensionManager.Dimension):
                raise DimensionError("Wrong type of derivedfrom")
            if not derivedfrom.level(Ratio):
                raise DimensionError("%s cannot be a factor" % derivedfrom)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = (derivedfrom,)
    
    def __init__(self, unitmanager):
        '''
        @summary: constructor
        @param unitmanager: the unit manager needs to be referenced, to check the basins of a unit
        @type unitmanager: UnitManager  
        '''
        self.dimensions = {}
        self.unitmanager = unitmanager
    
    def __len__(self):
        '''
        @summary: the number of dimension known by the DimensionManager
        @return: the number of dimension known by the DimensionManager
        @rtype: int
        '''
        return len(self.dimensions)
    
    def __iter__(self):
        '''
        @summary: an iterator over known dimensions
        @return: the next known dimension
        @rtype: Dimension
        '''
        for d in self.dimensions.values():
            yield d
    
    def newBaseDimension(self, reference, name, unit, level):
        '''
        @summary: generate a new dimension
        @param reference: the reference to the dimension
        @type reference: str
        @param symbol: a human readable name of the dimension
        @type symbol: str
        @param unit: the default unit of the dimension
        @type base: Unit
        @param level: the measurement level of the dimension
        @type level: MeasurementLevel
        @note: the level is not a class instance
        @return: the new dimension
        @rtype: Dimension
        @raise DimensionError: Dimension with reference already exists / Wrong type of unit / Wrong type of level / Wrong type of dimension /
        Expecting derivedfrom set / Wrong number of derived from Dimensions
        '''
        if self.dimensions.has_key(reference):
            raise DimensionError("Dimension with reference %s already exists" % reference)
        dimension = self.BaseDimension(self, reference, name, unit, level)
        self.dimensions[reference] = dimension
        return dimension

    def newDerivedDimension(self, reference, name, unit, derivedfrom, dimtype, **kw):
        '''
        @summary: generate a new dimension
        @param reference: the reference to the dimension
        @type reference: str
        @param symbol: a human readable name of the dimension
        @type symbol: str
        @param unit: the default unit of the dimension
        @type base: Unit
        @note: the level is not a class instance
        @param derivedfrom: the set of dimensions that compose this dimension
        @type derivedfrom: tuple(Dimension) or Dimension
        @param dimtype: possible dimension types are DimensionManager.DifferenceDimension, 
        DimensionManager.PowerDimension, DimensionManager.ProductDimension, DimensionManager.RatioDimension
        @note: dimtype parameter is not an instance, but a class scheme
        @type dimtype: Dimension
        @return: the new dimension
        @rtype: Dimension
        @keyword kw: PowerDimension expects an integer valued parameter: exponent 
        @raise DimensionError: Dimension with reference already exists / Wrong type of dimension
        '''
        if self.dimensions.has_key(reference):
            raise DimensionError("Dimension with reference %s already exists" % reference)
        if issubclass(dimtype, self.DifferenceDimension)or issubclass(dimtype, self.ProductDimension) or issubclass(dimtype, self.RatioDimension):
            dimension = dimtype(self, reference, name, unit, derivedfrom)
        elif issubclass(dimtype, self.PowerDimension):
            dimension = dimtype(self, reference, name, unit, derivedfrom, kw.get('exponent'))
        else:
            raise DimensionError("Wrong type of dimension %s" % dimtype)
        self.dimensions[reference] = dimension
        return dimension
    
    def __getitem__(self, reference):
        '''
        @summary: look up the prefix in the DimensionManager based on its reference
        @param reference: the reference to the dimension
        @type reference: str
        @return: the dimension if found
        @rtype: Dimension
        @raise DimensionError: Dimension with reference not found
        '''
        if not self.dimensions.has_key(reference):
            for k in self.dimensions.keys():
                print k,",",
            print "."
            raise DimensionError("Dimension with reference %s not found" % reference)
        return self.dimensions[reference]
