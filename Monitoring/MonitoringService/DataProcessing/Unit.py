'''
Created on Oct 19, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''
from DataError import UnitError

class UnitManager(object):
    '''
    @summary: the unit container
    
    @note: The relationship between various unit, describing the derivation paths are not stored in this model,
    because this information can be inferred from the dimension derivations, represented in the L{DimensionManager}.
    @note: Units that are formed by prepending a unit prefix (L{Prefix}) are dealt as a L{DerivedUnit}.
    
    @ivar units: container of known units
    @type units: dict(str: L{Unit})
    @ivar conversionpaths: is a map of operations to carry out from a unit to get a different unit
    @type conversionpaths: dict((L{Unit}, L{Unit}): (callable, args))
    @ivar basins: indicates the derivatives of a basic unit
    @type basins: dict(L{BasicUnit}: set(L{Unit}))
    @ivar duplicatesymbols: collection of unit symbols, which more than one unit may bare
    @type duplicatesymbols: set(str)
    '''

    class Unit(object):
        '''
        @summary: common skeleton of all units
        @ivar manager: reference to the unit manager
        @type manager: L{UnitManager}
        @ivar reference: unique reference of the unit
        @ivar symbol: short form of the unit
        @type symbol: str
        '''
        def __init__(self, manager, reference, symbol, ancestor):
            '''
            @summary: bind and store common information of the unit
            @param manager: the unit manager
            @type manager: L{UnitManager}
            @param reference: a unique identifier
            @param symbol: short human readable representation of the unit
            @type symbol: str
            @param ancestor: the ancestor of this unit is deriving from
            @type ancestor: L{Unit}
            '''
            self._data = (manager, reference, symbol)
            self._ancestor = ancestor
        @property
        def manager(self):
            return self._data[0]
        @property
        def reference(self):
            return self._data[1]
        @property
        def symbol(self):
            return self._data[2]
        def __str__(self):
            return self.symbol
        def __eq__(self, u):
            return self._data == u._data

    class BasicUnit(Unit):
        '''
        @summary: a unit axiom
        '''
        def __init__(self, manager, reference, symbol):
            '''
            @summary: constructor
            A BasicUnit is an instance of either set of BaseUnit, ProductUnit and PowerUnit as of the information model.
            @param manager: a reference to the unit manager
            @type manager: L{UnitManager} 
            @param reference: the reference to the unit
            @param symbol: an abbreviation for the unit
            @type symbol: str
            '''
            UnitManager.Unit.__init__(self, manager, reference, symbol, None)
    
    class DerivedUnit(Unit):
        '''
        @summary: a unit deriving from various known units
        '''
        def __init__(self, manager, reference, symbol, ancestor):
            '''
            @summary: constructor
            A DerivedUnit is an instance of either set of LinearTransformedUnit and RegexpScaledUnit as of the information model.
            Also units that have any unit prefix fall in this set.
            @param manager: a reference to the unit manager
            @type manager: L{UnitManager} 
            @param reference: the reference to the unit
            @param symbol: an abbreviation for the unit
            @type symbol: str
            @param ancestor: the neighbor unit, whose derivative this instance is.
            @type ancestor: L{Unit}
            '''
            UnitManager.Unit.__init__(self, manager, reference, symbol, ancestor)
    
    
    def __init__(self):
        '''
        @summary: constructor
        '''
        self.units = {}
        self.conversionpaths = {}
        self.basins = {}
        self.duplicatesymbols = set()
    
    def __contains__(self, item):
        '''
        @summary: check the existence of a unit
        @param item: a unit or its symbol
        @type item: L{Unit} or str
        @return: True if the unit is known by the L{UnitManager}
        @rtype: bool
        @raise L{UnitError}: Wrong item type
        '''
        units = set(self.units.values())
        if isinstance(item, self.Unit):
            return item in units
        elif isinstance(item, str):
            for unit in units:
                if unit.symbol == item:
                    return True
            return False
        else:
            raise UnitError("Wrong item type %s" % item)
    
    def __len__(self):
        '''
        @summary: the number of units known by the L{UnitManager}
        @return: the number of units known by the L{UnitManager}
        @rtype: int
        '''
        return len(self.units)

    @staticmethod
    def intORfloat(x):
        '''
        @summary: a conversion helper to read out a value as a number
        @param x: a number
        @type x: str
        @return: the number converted to integer or floating point decimal
        @rtype: int or float
        '''
        if isinstance(x, str):
            try:
                return int(x)
            except ValueError:
                return float(x)
        else:
            return float(x)

    def __getitem__(self, reference):
        '''
        @summary: look up the unit in the L{UnitManager} using its reference
        @param reference: the reference to the unit
        @return: the unit found
        @rtype: L{Unit}
        @raise L{UnitError}: Unit with reference not found
        '''
        if self.units.has_key(reference):
            return self.units[reference]
        raise UnitError("Unit with reference %s not found" % reference)

    def newBasicUnit(self, reference, symbol):
        '''
        @summary: generate a new basic unit
        @param reference: the reference to the unit
        @param symbol: a short form of the unit
        @type symbol: str
        @return: the new unit
        @rtype: L{BasicUnit}
        @raise L{UnitError}: Unit with reference exists
        '''
        if self.units.has_key(reference): 
            raise UnitError("Unit with reference %s exists" % reference)
        if UnitManager.__contains__(self, symbol):
            self.duplicatesymbols.add(symbol)
        unit = self.BasicUnit(self, reference, symbol)
        self.units[reference] = unit
        self.basins[unit] = set([unit])
        self.__dict__[reference] = unit
        return unit

    def addLinearTransformedUnit(self, reference, symbol, derivedfrom, scale, offset = 0):
        '''
        @summary: generate a derived unit
        @param reference: the reference to the unit
        @param symbol: a short form of the unit
        @type symbol: str
        @param derivedfrom: the neighbor unit
        @type derivedfrom: L{Unit}
        @param scale: scaling factor for the linear transformation
        @type scale: float
        @param offset: the shift in the linear transformation, defaults to 0
        @type offset: float 
        @return: the new unit
        @rtype: L{DerivedUnit}
        @raise L{UnitError}: Wrong type of derivedfrom / Unit not found / Unit with reference exists / Cannot extend basin with unit, because Unit not found
        '''
        if not isinstance(derivedfrom, self.Unit):
            raise UnitError("Wrong type of derivedfrom %s" % derivedfrom)
        if not UnitManager.__contains__(self, str(derivedfrom)):
            raise UnitError("Unit %s not found" % derivedfrom)
        if self.units.has_key(reference): 
            raise UnitError("Unit with reference %s exists" % reference)
        unit = self.DerivedUnit(self, reference, symbol, derivedfrom)
        basic = derivedfrom
        while basic._ancestor:
            basic = basic._ancestor
        if not self.basins.has_key(basic):
            raise UnitError("Cannot extend basin with unit %s, because Unit %s not found" % (unit, basic))
        if UnitManager.__contains__(self, symbol):
            self.duplicatesymbols.add(symbol)
        self.units[reference] = unit
        self.conversionpaths[(unit, derivedfrom)] = (self.op_lt_forward, (scale, offset))
        self.conversionpaths[(derivedfrom, unit)] = (self.op_lt_inverse, (scale, offset))
        self.basins[basic].add(unit)
        self.__dict__[reference] = unit
        return unit

    def addRegexpTransformedUnit(self, reference, symbol, derivedfrom, expr_forward, expr_inverse):
        '''
        @summary: generate a derived unit
        @param reference: the reference to the unit
        @param symbol: a short form of the unit
        @type symbol: str
        @param derivedfrom: the neighbor unit
        @type derivedfrom: L{Unit}
        @param expr_forward: the expression driving the forward transformation
        @type expr_forward: str
        @param expr_inverse: the expression driving the inverse transformation
        @type expr_inverse: str
        @return: the new unit
        @rtype: L{DerivedUnit}
        @raise L{UnitError}: Wrong type of derivedfrom / Unit not found / Unit with reference exists / Cannot extend basin with unit, because Unit not found
        '''
        if not isinstance(derivedfrom, self.Unit):
            raise UnitError("Wrong type of derivedfrom %s" % derivedfrom)
        if not UnitManager.__contains__(self, str(derivedfrom)):
            raise UnitError("Unit %s not found" % derivedfrom)
        if self.units.has_key(reference): 
            raise UnitError("Unit with reference %s exists" % reference)
        unit = self.DerivedUnit(self, reference, symbol, derivedfrom)
        basic = derivedfrom
        while basic._ancestor:
            basic = basic._ancestor
        if not self.basins.has_key(basic):
            raise UnitError("Cannot extend basin with unit %s, because Unit %s not found" % (unit, basic))
        if UnitManager.__contains__(self, symbol):
            self.duplicatesymbols.add(symbol)
        self.units[reference] = unit
        self.conversionpaths[(unit, derivedfrom)] = (self.op_rt_forward, expr_forward)
        self.conversionpaths[(derivedfrom, unit)] = (self.op_rt_inverse, expr_inverse)
        self.basins[basic].add(unit)
        self.__dict__[reference] = unit
        return unit

    def getBasinByUnit(self, unit):
        '''
        @summary: return the set of units, which are compatible with a given unit
        @param unit: the unit to look up
        @type unit: L{Unit}
        @return: the set of compatible units
        @rtype: set(L{Unit})
        @raise L{UnitError}: not found
        '''
        for basin in self.basins.values():
            if unit in basin:
                return basin
        raise UnitError("Basin for unit %s not found" % unit)

    def getBasinByReference(self, reference):
        '''
        @summary: look up the compatible units of a given unit with the calling reference
        @param reference:
        @return: the set of compatible units
        @rtype: set(L{Unit})
        @raise L{UnitError}: not found
        '''
        try:
            unit = self[reference]
            return self.getBasinByUnit(unit)
        except UnitError:
            raise UnitError("Basin for unit reference %s not found" % reference)

    def op_lt_forward(self, value, so):
        (scale, offset) = so
        def op(value):
            return scale * self.intORfloat( value ) + offset
        if isinstance(value, list):
            return map(lambda x: op(x), value)
        return op(value)

    def op_lt_inverse(self, value, so):
        (scale, offset) = so
        def op(value):
            return (self.intORfloat( value ) - offset) / float(scale)
        if isinstance(value, list):
            return map(lambda x: op(x), value)
        return op(value)

    def op_rt_forward(self, value, expression):
        def op(value):
            raise UnitError("not implemented")
        if isinstance(value, list):
            return map(lambda x: op(x), value)
        return op(value)

    op_rt_inverse = op_rt_forward

    def convert(self, value, from_unit, to_unit):
        '''
        @summary: convert a value of one unit to the other
        @param value: input value in from_unit
        @param from_unit: the original unit of the input value
        @type from_unit: L{Unit}
        @param to_unit: the requested new unit
        @type to_unit: L{Unit}
        @raise L{UnitError}: unknown unit / incompatible units
        '''
        if not UnitManager.__contains__(self, str(from_unit)):
            raise UnitError("Unknown from_unit")
        if not UnitManager.__contains__(self, str(to_unit)):
            raise UnitError("Unknown to_unit")
        if from_unit == to_unit:
            return value

        while from_unit._ancestor:
            op, oparg = self.conversionpaths[(from_unit, from_unit._ancestor)]
            value = op(value, oparg)
            from_unit = from_unit._ancestor
        heap = []
        while to_unit._ancestor:
            op, oparg = self.conversionpaths[(to_unit._ancestor, to_unit)]
            heap.append((op, oparg))
            to_unit = to_unit._ancestor
        if from_unit != to_unit:
            raise UnitError("Different base units %s %s" % (from_unit, to_unit))
        while len(heap):
            op, oparg = heap.pop(0)
            value = op(value, oparg)
        return value

