'''
Created on Mar 22, 2012

@author: steger, jozsef

@summary: Class representation of the measurement levels (aka measurement scale) defined by Stanley Smith Stevens. 
Stevens proposed his theory in a 1946 Science article titled "On the theory of scales of measurement".
@note: These classes are not meant to be instantiated ever.
'''

class MeasurementLevel: 
    '''
    @summary: It serves as the common scheme for the measurement levels. Only its subclasses have a meaning.
    '''
    pass

class Nominal(MeasurementLevel): 
    '''
    @summary: Values of this kind of measurement are mere elements of a set.
    '''
    pass

class Ordinal(Nominal): 
    '''
    @summary: A ranking is defined between the values of this kind of measurement.
    '''
    pass

class Interval(Ordinal): 
    '''
    @summary: A difference is defined which can be evaluated for any two values of this kind of measurement.
    '''
    pass

class Ratio(Interval): 
    '''
    @summary: There is a reference value defined for this kind of measurement, that is "zero" has a meaning.
    '''
    pass

lut_level = {
   'NominalLevel': Nominal,
   'OrdinalLevel': Ordinal,
   'IntervalLevel': Interval,
   'RatioLevel': Ratio,
}
