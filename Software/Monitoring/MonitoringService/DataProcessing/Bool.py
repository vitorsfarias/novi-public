'''
Created on Mar 22, 2013

@author: steger
'''
from DataProcessing.DataError import DataError

#FIXME: this is a DataSource?
class Comparator(object):
    '''
    classdocs
    '''
    def __init__(self, datasource):
        self._datasource = datasource
    
    @property
    def value(self):
        raise DataError("Implement value property")
        
class IsPositive(Comparator):
    '''
    '''
    @property
    def name(self):
        return "IsPositive(%s)" % self._datasource.name
    
    @property
    def value(self):
        return self._datasource.value > 0
        
class IsNegative(Comparator):
    '''
    '''
    @property
    def name(self):
        return "IsNegative(%s)" % self._datasource.name
    
    @property
    def value(self):
        return self._datasource.value < 0
            
class IsNotPositive(Comparator):
    '''
    '''
    @property
    def name(self):
        return "IsNotPositive(%s)" % self._datasource.name
    
    @property
    def value(self):
        return self._datasource.value <= 0
            
class IsNotNegative(Comparator):
    '''
    '''
    @property
    def name(self):
        return "IsNotNegative(%s)" % self._datasource.name
    
    @property
    def value(self):
        return self._datasource.value >= 0