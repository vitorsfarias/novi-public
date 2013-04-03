'''
Created on Dec 20, 2012

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

class DataError(Exception):
    pass

class PrefixError(DataError):
    pass

class UnitError(DataError):
    pass

class DimensionError(DataError):
    pass

class ParameterError(DataError):
    pass

class SamplerError(DataError):
    pass

class AggregatorError(DataError):
    pass

