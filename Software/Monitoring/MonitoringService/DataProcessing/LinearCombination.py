'''
Created on Mar 21, 2013

@author: steger
'''
from DataProcessing.DataSource import DataSource
from DataProcessing.Aggregator import Aggregator
from DataProcessing.DataError import DataError

class LinearCombination(DataSource):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''
        DataSource.__init__(self, dependency = None)
        self._terms = []
        self._value = None
        
    def addTerm(self, factor, aggregate):
        if not isinstance(aggregate, Aggregator):
            raise DataError("Wrong type of term")
        self._terms.append((factor, aggregate))
    
    @property
    def name(self):
        return "BLA"
    
    @property
    def value(self):
        self.process()
        return self._value
    
    def process(self):
        result = 0
        for factor, aggregate in self._terms:
            term = aggregate.aggregate
            result += factor * term
        self._value = result