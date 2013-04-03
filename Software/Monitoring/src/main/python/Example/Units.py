from __future__ import with_statement
'''
Created on Oct 12, 2011

@author: steger
@summary: Here we declare some unit models to enable parameter conversions  
'''
from DataProcessing.Unit import UnitManager
from Example.Prefixes import PM
from DataProcessing.DataError import PrefixError

UM = UnitManager()

def getPrefixBySymbol(symbol):
    '''
    @summary: look up the prefix in the PrefixManager based on its symbol
    @param symbol: the symbol of the unit prefix
    @type symbol: str
    @return: the unit prefix found
    @rtype: Prefix
    @raise PrefixError: Prefix with symbol not found
    '''
    for prefix in PM.prefixes.values():
        if prefix.symbol == symbol:
            return prefix
    raise PrefixError("Prefix with symbol %s not found" % symbol)


basicunits = [
    ("piece", "(1)", None),
    ("unitless", "", None),
    ("fraction", "", None),
    ("second", "s", ['m', 'mu', 'n', 'p']),
    ("unixtimestamp", "tss", ['n']),
    ("ipv4dotted", "", None),
    ("bit", "bit", ['k', 'M' ]),
    ]

lintransformedunits = [
    ("dozen", "(12)", "piece", 12, None),
    ("Byte", "B", "bit", 8, ['k', 'M' ]),
    ]

def storeprefixes(u, prefixes):
    if prefixes:
        for ps in prefixes:
            p = getPrefixBySymbol(ps)
            nr = "%s_%s" % (p.reference, u.reference)
            ns = "%s%s" % (p.symbol, u.symbol)
            UM.addLinearTransformedUnit(nr, ns, u, p.scale)

for reference, symbol, prefixes in basicunits:
    u = UM.newBasicUnit(reference, symbol)
    storeprefixes(u, prefixes)
    
for reference, symbol, ancientref, scale, prefixes in lintransformedunits:
    u = UM.addLinearTransformedUnit(reference, symbol, UM[ancientref], scale)
    storeprefixes(u, prefixes)


# Some units explicitely referenced
pico_second = UM["pico_second"]
nano_second = UM["nano_second"]
micro_second = UM["micro_second"]
milli_second = UM["milli_second"] 
second = UM["second"]

Byte = UM["Byte"]
kilo_Byte = UM["kilo_Byte"]

piece = UM["piece"]
dozen = UM["dozen"]

unitless = UM["unitless"]

unixtimestamp = UM["unixtimestamp"]
nano_unixtimestamp = UM["nano_unixtimestamp"]

fraction = UM["fraction"]

ipv4dotted = UM["ipv4dotted"]