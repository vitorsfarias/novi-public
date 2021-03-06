from __future__ import with_statement
'''
Created on Oct 19, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''
from DataProcessing.DataError import UnitError

class UnitManager(object):
    '''
    @summary: the unit container
    The web that describes the derivation path between different units are not stored,
    because most of that information can be inferred from the dimension derivations.
    The UnitManager differentiates between BasicUnit and DerivedUnit.
    BasicUnits form the set of BaseUnit, ProductUnit, PowerUnit as referred to in the information model.
    Whereas DerivedUnits are made up of LinearTransformedUnit and RegexpScaledUnit as referenced in the information model.
    Units that are formed by prepending a unit prefix are also DerivedUnits.
    '''

    class Unit(object):
        def __init__(self, manager, reference, symbol, ancestor):
            self._data = (manager, reference, symbol)
            self._ancestor = ancestor
        def _get_manager(self):
            return self._data[0]
        def _get_reference(self):
            return self._data[1]
        def _get_symbol(self):
            return self._data[2]
        def __str__(self):
            return self.symbol
        def __eq__(self, u):
            return self._data == u._data


        manager = property(_get_manager,None,None)

        symbol = property(_get_symbol,None,None)

        reference = property(_get_reference,None,None)
    class BasicUnit(Unit):
        def __init__(self, manager, reference, symbol):
            '''
            @summary: constructor
            A BasicUnit is an instance of either set of BaseUnit, ProductUnit and PowerUnit as of the information model.
            @param manager: a reference to the unit manager
            @type manager: UnitManager 
            @param reference: the reference to the unit
            @type reference: str
            @param symbol: an abbreviation for the unit
            @type symbol: str
            '''
            UnitManager.Unit.__init__(self, manager, reference, symbol, None)
    
    class DerivedUnit(Unit):
        def __init__(self, manager, reference, symbol, ancestor):
            '''
            @summary: constructor
            A DerivedUnit is an instance of either set of LinearTransformedUnit and RegexpScaledUnit as of the information model.
            Also units that have any unit prefix fall in this set.
            @param manager: a reference to the unit manager
            @type manager: UnitManager 
            @param reference: the reference to the unit
            @type reference: str
            @param symbol: an abbreviation for the unit
            @type symbol: str
            @param ancestor: the neighbor unit, whose derivative this instance is.
            @type ancestor: Unit
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
        @type item: Unit or str
        @return: True if the unit is known by the UnitManager
        @rtype: bool
        @raise UnitError: Wrong item type
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
        @summary: the number of units known by the UnitManager
        @return: the number of units known by the UnitManager
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
        @summary: look up the unit in the UnitManager using its reference
        @param reference: the reference to the unit
        @type reference: str
        @return: the unit found
        @rtype: Unit
        @raise UnitError: Unit with reference not found
        '''
        if self.units.has_key(reference):
            return self.units[reference]
        raise UnitError("Unit with reference %s not found" % reference)

    def newBasicUnit(self, reference, symbol):
        '''
        @summary: generate a new basic unit
        @param reference: the reference to the unit
        @type reference: str
        @param symbol: a short form of the unit
        @type symbol: str
        @return: the new unit
        @rtype: BasicUnit
        @raise UnitError: Unit with reference exists
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
        @summary: generate a derived basic unit
        @param reference: the reference to the unit
        @type reference: str
        @param symbol: a short form of the unit
        @type symbol: str
        @param derivedfrom: the neighbor unit
        @type derivedfrom: Unit
        @param scale: scaling factor for the linear transformation
        @type scale: float
        @param offset: the shift in the linear transformation, defaults to 0
        @type offset: float 
        @return: the new unit
        @rtype: DerivedUnit
        @raise UnitError: Wrong type of derivedfrom / Unit not found / Unit with reference exists / Cannot extend basin with unit, because Unit not found
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
        @summary: generate a new basic unit
        @param reference: the reference to the unit
        @type reference: str
        @param symbol: a short form of the unit
        @type symbol: str
        @param derivedfrom: the neighbor unit
        @type derivedfrom: Unit
        @param expr_forward: the expression driving the forward transformation
        @type expr_forward: str
        @param expr_inverse: the expression driving the inverse transformation
        @type expr_inverse: str
        @return: the new unit
        @rtype: DerivedUnit
        @raise UnitError: Wrong type of derivedfrom / Unit not found / Unit with reference exists / Cannot extend basin with unit, because Unit not found
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
        for basin in self.basins.values():
            if unit in basin:
                return basin
        raise UnitError("Basin for unit %s not found" % unit)

    def getBasinByReference(self, reference):
        try:
            unit = self[reference]
            return self.getBasinByUnit(unit)
        except UnitError:
            raise UnitError("Basin for unit reference %s not found" % reference)

    def op_lt_forward(self, value, (scale, offset)):
        def op(value):
            return scale * self.intORfloat( value ) + offset
        if isinstance(value, list):
            return map(lambda x: op(x), value)
        return op(value)

    def op_lt_inverse(self, value, (scale, offset)):
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

