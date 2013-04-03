'''
Created on Dec 10, 2012

@author: steger
'''
from DataProcessing.Aggregator import AggregatorError, Aggregator
from DataProcessing.Sampler import Sampler
from DataProcessing.Parameter import ParameterList

class AggregatorManager(object):
    def __init__(self):
        self._id = 0;
        self._aggregators = {}

    def newAggregator(self, dataSource, cellrequest, commandflow):
        for c, ca in commandflow:
            if issubclass(c, Aggregator):
                dataSource = c(dataSource, cellrequest)
                if isinstance(ca, ParameterList):
                    for p in ca:
                        dataSource.__setattr__(p.name, p.value[0])
                else:
                    for k, v in ca.iteritems():
                        dataSource.__setattr__(k, v)
            elif issubclass(c, Sampler):
                dataSource = c(dataSource)
                if isinstance(ca, ParameterList):
                    for p in ca:
                        dataSource.__setattr__(p.name, p.value[0])
                else:
                    for k, v in ca.iteritems():
                        dataSource.__setattr__(k, v)
        self._id += 1
        self._aggregators[ self._id ] = dataSource
        return self._id, dataSource

    def __getitem__(self, aggregatorid):
        try:
            return self._aggregators[ aggregatorid ]
        except:
            raise AggregatorError("Aggregator with id %s not found" % aggregatorid)

    def pop(self, aggregatorid):
        try:
            self._aggregators.pop( aggregatorid )
        except KeyError:
            print "WW: Aggregator with id %s not found" % aggregatorid
        