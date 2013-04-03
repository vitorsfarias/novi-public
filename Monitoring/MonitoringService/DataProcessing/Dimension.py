'''
Created on Feb 27, 2012

@author: steger
'''
from Unit import UnitManager
from DataProcessing.MeasurementLevel import MeasurementLevel, Interval, Ratio
from DataProcessing.DataError import DimensionError

class DimensionManager(object):
    '''
    @summary: the dimension container
    @ivar dimenstions: the container of the known dimensions
    @type dimensions: dict(str: L{Dimension})
    @ivar unitmanager: reference to the unit manager
    @type unitmanager: L{UnitManager}
    '''    
    class Dimension(object):
        '''
        @summary: a skeleton class for all the dimensions handled by L{DimensionManager}
        @ivar manager: back reference to the dimension manager
        @type manager: L{DimensionManager}
        @ivar unitmanager: a reference to the unit manager
        @type unitmanager: L{UnitManager}
        @ivar reference: the unique identifier of the dimension
        @ivar name: the name of the dimension
        @type name: str
        @ivar unit: the default unit of the dimension
        @type unit: L{Unit}
        @ivar basin: the set of units which are valid for this dimension
        @type basin: set(L{Unit})
        '''
        def __init__(self, dimensionmanager, reference, name, unit, level):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: L{DimensionManager}  
            @param reference: the reference to the dimension
            @type reference: str
            @param unit: the default unit of the dimension
            @type unit: L{Unit}
            @param level: the measurement level of the dimension
            @type level: L{MeasurementLevel}
            @note: the level is not a class instance
            @raise L{DimensionError}: Wrong type of unit / Wrong type of level
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
        @property
        def manager(self):
            return self._data[0]
        @property
        def unitmanager(self):
            return self._data[0].unitmanager
        @property
        def reference(self):
            return self._data[1]
        @property
        def name(self):
            return self._data[2]
        @property
        def unit(self):
            return self._data[3]
        @property
        def basin(self):
            return self.unitmanager.getBasinByUnit(self.unit)
        def level(self, level):
            '''
            @summary: check measurement level against the given level
            @param level: measurement level
            @type level: L{MeasurementLevel}
            @return: True if the measurement level given as a parameter 
            is the same or looser than the level of the dimension
            @rtype: bool
            @raise L{DimensionError}: Wrong type of level
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
            @type unit: L{Unit}
            @return: true if the unit is applicable for this dimension
            @rtype: bool
            '''
            return unit in self.unitmanager.getBasinByUnit(self.unit)
    
    class BaseDimension(Dimension): 
        '''
        @summary: a dimension axiom
        '''
        pass

    class DerivedDimension(Dimension): 
        '''
        @summary: a skeleton for  dimensions, which are deriving from other already known dimensions
        '''
        def ancestors(self):
            '''
            @summary: iterate over all ancestors this dimension is derived from
            @return: generator over ancestors
            @rtype: L{Dimension}
            '''
            for d in self._ancestor:
                yield d

    class DifferenceDimension(DerivedDimension):
        '''
        @summary: a dimension defined by subtracting two individuals of a known dimension 
        '''
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: L{DimensionManager}  
            @param reference: the reference to the dimension
            @param unit: the default unit of the dimension
            @type unit: L{Unit}
            @param derivedfrom: the ancestor dimension this dimension is derived from
            @type derivedfrom: L{Dimension}
            @raise L{DimensionError}: Wrong type of derivedfrom
            '''
            if not isinstance(derivedfrom, DimensionManager.Dimension):
                raise DimensionError("Wrong type of derivedfrom")
            if not derivedfrom.level(Interval):
                raise DimensionError("Cannot subtract %s" % derivedfrom)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = derivedfrom
    
    class PowerDimension(DerivedDimension):
        '''
        @summary: a dimension defined by raising an existing dimension to a given power
        @ivar exponent: the power
        @type exponent: int
        '''
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom, exponent):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: L{DimensionManager}  
            @param reference: the reference to the dimension
            @param unit: the default unit of the dimension
            @type unit: L{Unit}
            @param derivedfrom: the ancestor dimension this dimension is derived from
            @type derivedfrom: L{Dimension}
            @param exponent: dimension is a derivative of the derivedfrom dimension, by raising to power exponent
            @type exponent: int
            @raise DimensionError: Wrong type of derivedfrom / Cannot power
            '''
            if not isinstance(derivedfrom, DimensionManager.Dimension):
                raise DimensionError("Wrong type of derivedfrom")
            if not derivedfrom.level(Ratio):
                raise DimensionError("Cannot power %s" % derivedfrom)
            DimensionManager.Dimension.__init__(self, dimensionmanager, reference, name, unit, Ratio)
            self._ancestor = (derivedfrom,)
            self._exponent = exponent
        @property
        def exponent(self):
            return self._exponent

    class ProductDimension(DerivedDimension):
        '''
        @summary: dimension defined by multiplying at least two known different dimensions
        '''
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: L{DimensionManager}  
            @param reference: the reference to the dimension
            @param unit: the default unit of the dimension
            @type unit: L{Unit}
            @param derivedfrom: the set of dimensions that compose this dimension
            @type derivedfrom: tuple(L{Dimension})
            @raise L{DimensionError}: Wrong type of derivedfrom / ProductDimension is derived from more than 2 Dimensions / Cannot be a factor
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
        '''
        @summary: dimension defined by dividing two known dimensions
        '''
        def __init__(self, dimensionmanager, reference, name, unit, derivedfrom):
            '''
            @summary: constructor
            @param dimensionmanager: reference to the dimension manager
            @type dimensionmanager: L{DimensionManager}  
            @param reference: the reference to the dimension
            @param unit: the default unit of the dimension
            @type unit: L{Unit}
            @param derivedfrom: the set of dimensions that compose this dimension
            @type derivedfrom: tuple(L{Dimension})
            @raise L{DimensionError}: Wrong type of derivedfrom / Cannot be a factor
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
        @type unitmanager: L{UnitManager}  
        '''
        self.dimensions = {}
        self.unitmanager = unitmanager
    
    def __len__(self):
        '''
        @summary: the number of dimension known by the L{DimensionManager}
        @return: the number of dimension known by the L{DimensionManager}
        @rtype: int
        '''
        return len(self.dimensions)
    
    def __iter__(self):
        '''
        @summary: an iterator over known dimensions
        @return: the next known dimension
        @rtype: L{Dimension}
        '''
        for d in self.dimensions.values():
            yield d
    
    def newBaseDimension(self, reference, name, unit, level):
        '''
        @summary: generate a new dimension
        @param reference: the reference to the dimension
        @type reference: str
        @param unit: the default unit of the dimension
        @type unit: L{Unit}
        @param level: the measurement level of the dimension
        @type level: L{MeasurementLevel}
        @note: the level is not a class instance
        @return: the new dimension
        @rtype: L{Dimension}
        @raise L{DimensionError}: Dimension with reference already exists / Wrong type of unit / Wrong type of level / Wrong type of dimension /
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
        @param unit: the default unit of the dimension
        @type unit: L{Unit}
        @param derivedfrom: the set of dimensions that compose this dimension
        @type derivedfrom: tuple(L{Dimension}) or L{Dimension}
        @param dimtype: possible dimension types are L{DifferenceDimension}, L{PowerDimension}, L{ProductDimension}, L{RatioDimension}
        @note: dimtype parameter is not an instance, but a class scheme
        @type dimtype: L{Dimension}
        @return: the new dimension
        @rtype: L{Dimension}
        @keyword kw: L{PowerDimension} expects an integer valued parameter: exponent 
        @raise L{DimensionError}: Dimension with reference already exists / Wrong type of dimension
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
        @return: the dimension if found
        @rtype: L{Dimension}
        @raise L{DimensionError}: Dimension with reference not found
        '''
        if not self.dimensions.has_key(reference):
            raise DimensionError("Dimension with reference %s not found" % reference)
        return self.dimensions[reference]
