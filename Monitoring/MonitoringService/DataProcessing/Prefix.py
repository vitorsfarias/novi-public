'''
Created on Feb 27, 2012

@author: steger
'''
from DataProcessing.DataError import PrefixError

class PrefixManager(object):
    '''
    @summary: acts a unit prefix container
    @ivar prefixes: the container of the known prefixes
    @type prefixes: dict(str: L{Prefix})
    @ivar duplicatesymbols: the set of prefixes, which map to the same symbol
    @type duplicatesymbols: set(str)
    '''
    class Prefix(object):
        '''
        @summary: represents a unit prefix and the scaling information
        @ivar reference: a uniq prefix identifier
        @ivar symbol: a short representation of the prefix
        @type symbol: str
        @ivar base: the base of the scaling factor
        @type base: int
        @ivar exponent: the exponent of the scaling factor
        @type exponent: int
        @ivar scale: the scaling factor, which is base ** exponent
        @type scale: float
        '''
        def __init__(self, reference, symbol, base, exponent):
            '''
            @summary: constructor
            @param reference: the reference to the unit prefix
            @type reference: str
            @param symbol: a short form of the unit prefix
            @type symbol: str
            @param base: the base of the unit prefix, typically 2 or 10
            @type base: int
            @param exponent: the exponent of the unit prefix
            @type exponent: int
            '''
            scale = base ** exponent
            self._data = (reference, symbol, base, exponent, scale)
        def __str__(self):
            return self.symbol
        @property
        def reference(self):
            return self._data[0]
        @property
        def symbol(self):
            return self._data[1]
        @property
        def base(self):
            return self._data[2]
        @property
        def exponent(self):
            return self._data[3]
        @property
        def scale(self):
            return self._data[4]

    def __init__(self):
        '''
        @summary: constructor
        '''
        self.prefixes = {}
        self.duplicatesymbols = set()

    def __contains__(self, item):
        '''
        @summary: check the existence of a unit prefix
        @param item: a prefix or a prefix symbol
        @type item: L{Prefix} or str
        @return: True if the prefix is known by the L{PrefixManager}
        @rtype: bool
        @raise L{PrefixError}: Wrong item type
        '''
        if isinstance(item, self.Prefix):
            return item in self.prefixes.values()
        elif isinstance(item, str):
            for prefix in self.prefixes.values():
                if prefix.symbol == item:
                    return True
            return False
        else:
            raise PrefixError("Wrong item type %s" % item)
    
    def __len__(self):
        '''
        @summary: the number of prefixes known by the L{PrefixManager}
        @return: the number of prefixes known by the L{PrefixManager}
        @rtype: int
        '''
        return len(self.prefixes)
    
    def newPrefix(self, reference, symbol, base, exponent):
        '''
        @summary: generate a new unit prefix
        @param reference: the reference to the unit prefix
        @type reference: str
        @param symbol: a short form of the unit prefix
        @type symbol: str
        @param base: the base of the unit prefix, typically 2 or 10
        @type base: int
        @param exponent: the exponent of the unit prefix
        @type exponent: int
        @return: the new unit prefix
        @rtype: L{Prefix}
        @raise L{PrefixError}: Prefix with reference exists
        '''
        if self.prefixes.has_key(reference): 
            raise PrefixError("Prefix with reference %s already exists" % reference)
        if PrefixManager.__contains__(self, symbol):
            self.duplicatesymbols.add(symbol)
        prefix = self.Prefix(reference, symbol, base, exponent)
        self.prefixes[reference] = prefix
        return prefix

    def __getitem__(self, reference):
        '''
        @summary: look up the prefix in the L{PrefixManager} based on its reference
        @param reference: the reference to the unit prefix
        @type reference: str
        @return: the unit prefix found
        @rtype: L{Prefix}
        @raise L{PrefixError}: Prefix with reference not found
        '''
        if self.prefixes.has_key(reference):
            return self.prefixes[reference]
        raise PrefixError("Prefix with reference %s not found" % reference)
