'''
Created on Feb 21, 2012

@author: steger
'''
from DataProcessing.Parameter import ParameterList
from Resource.resource import resource as coreresource
from DataProcessing.DataFormatter import JsonFormatter, DumbFormatter, Formatter

class Query(object):
    '''
    @summary: the common skeleton of all queries
    '''
    pass

class InformationSource(object):
    '''
    @summary: represents a (feature, resource) pair, standing for what and where to measure
    @ivar resource: pointer to the resource
    @ivar feature: ponter to the feature
    '''
    def __init__(self):
        self._feature = None
        self._resource = None

    @property
    def resource(self):
        return self._resource
    @resource.setter
    def resource(self, (resourceid, resource)):
        if not isinstance(resource, coreresource):
            raise Exception("%s is not a resource type" % resource)
        self._resource = (resourceid, resource)
    
    @property
    def feature(self):
        return self._feature
    @feature.setter
    def feature(self, feature):
        self._feature = feature

class Identifier(object):
    '''
    @summary: represents a task, an aggregate or condition identifier
    @ivar sourceid: pointer to the task, aggregate or condition
    @type sourceid: str
    '''
    def __init__(self):
        self._sourceid = None
    
    @property
    def sourceid(self):
        return self._sourceid
    @sourceid.setter
    def sourceid(self, sourceid):
        self._sourceid = sourceid

class SingleQuery(Query, InformationSource):
    '''
    @summary: an extension of L{InformationSource} to store:
    - the requested output format and
    - some optional parameters L{ParameterList}
    @ivar samplechain: data manipulation chain
    @ivar formatter: a pointer to the requested formatter
    @type formatter: L{Formatter}
    @ivar paramlist: L{ParameterList}
    '''
    def __init__(self):
        InformationSource.__init__(self)
        self._samplechain = None
        self._formatter = None
        self._parameters = ParameterList()

    @property
    def samplechain(self):
        return self._samplechain
    @samplechain.setter
    def samplechain(self, samplechain):
        self._samplechain = samplechain

    @property
    def formatter(self):
        return self._formatter
    @formatter.setter
    def formatter(self, uri_formatter):
        if str(uri_formatter).endswith("Formatter_JSON"):
            self._formatter = JsonFormatter
        elif str(uri_formatter).endswith("Formatter_CSV"):
            self._formatter = DumbFormatter
        elif issubclass(uri_formatter, Formatter):
            self._formatter = uri_formatter
        else:
            raise Exception("%s is not a formatter type" % uri_formatter)

    @property
    def paramlist(self):
        return self._parameters
    
    def addParameter(self, parameter):
        '''
        @summary: append a new parameter to the list
        @param parameter: the parameter to append to the list
        @type parameter: L{Parameter} 
        '''
        self._parameters.append(parameter)

class SingleSampleQuery(SingleQuery, Identifier):
    '''
    @summary: represents a (feature, resource, source identifier) triplet to fully identify a data generator
    and binds a chain of operations to it.
    The requested output format is also stored here 
    '''
    def __init__(self):
        SingleQuery.__init__(self)
        Identifier.__init__(self)
        
class SingleConditionQuery(SingleQuery, Identifier):
    def __init__(self):
        SingleQuery.__init__(self)
        Identifier.__init__(self)

class QueryBundle(object):
    '''
    @summary: represents a collection of SingleQueries
    '''
    def __init__(self):
        self.atoms = {}

    def __len__(self):
        return len(self.atoms)

    def has_key(self, key):
        return self.atoms.has_key(key)

    def __iter__(self):
        for q in self.atoms.itervalues():
            yield q
    
    def getResource(self, resourceid):
        for q in self:
            if q.resource[0] == resourceid:
                return q.resource[1]
        return None

    def add(self, reference, q):
        if self.atoms.has_key(reference):
            raise Exception("Duplicate MonitoringQuery entry")
        if not isinstance(q, Query):
            raise Exception("Wrong type")
        self.atoms[reference] = q
