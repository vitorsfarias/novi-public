'''
Created on Oct 12, 2011

@author: steger
@summary: Here we declare some unit models to enable parameter conversions  
'''
from DataProcessing.Prefix import PrefixManager

prefixes = [
        ('pico', 'p', 10, -12),
        ('nano', 'n', 10, -9),
        ('micro', 'mu', 10, -6),
        ('milli', 'm', 10, -3),
        ('deco', 'd', 10, 0),
        ('hecto', 'h', 10, 2),
        ('kilo', 'k', 10, 3),
        ('mega', 'M', 10, 6),
        ('giga', 'G', 10, 9),
]

PM = PrefixManager()
for reference, symbol, base, exponent in prefixes:
    PM.newPrefix(reference, symbol, base, exponent)
