'''
Created on Dec 10, 2012

@author: steger
'''
from DataProcessing.LinearCombination import LinearCombination
from DataProcessing.Bool import IsPositive, IsNotPositive, IsNegative,\
    IsNotNegative

class WatchdogManager(object):
    def __init__(self, am):
        self._id = 0;
        self._conditionals = {}
        self.am = am
        self._dep = {}

    def newConditional(self, dataSource, cellrequest, conditiontype, operation):
        deps = []
        if conditiontype in [ IsPositive, IsNegative, IsNotNegative, IsNotPositive ]:
            lincomb = LinearCombination()
            for factor, commandflow in operation:
                Aid, A = self.am.newAggregator(dataSource, cellrequest, commandflow)
                lincomb.addTerm(factor, A)
                deps.append(Aid)
            DS = conditiontype(lincomb)
        self._id += 1
        self._conditionals[ self._id ] = DS
        self._dep[ self._id ] = deps[:]
        return self._id, DS

    def __getitem__(self, watchdogid):
        try:
            return self._conditionals[ watchdogid ]
        except:
            raise Exception("Watchdog with id %s not found" % watchdogid)

    def pop(self, watchdogid):
        try:
            self._conditionals.pop( watchdogid )
            deps = self._dep.pop(watchdogid)
            while len(deps):
                Aid = deps.pop()
                self.am.pop(Aid)
        except KeyError:
            print "WW: Watchdog with id %s not found" % watchdogid
        