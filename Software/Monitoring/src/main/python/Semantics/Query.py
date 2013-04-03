'''
Created on Feb 21, 2012

@author: steger
'''
from DataProcessing.Parameter import ParameterList
from Resource.resource import resource as coreresource
from DataProcessing.DataFormatter import JsonFormatter, DumbFormatter

class SingleQuery(object):
    '''
    @summary: represents a (feature, resource) pair, representing what and where to measure
    The requested output format is also stored here 
    Optionally some measurement specific parameters can be added and post processing can be applied
    '''
    def __init__(self):
        self._feature = None
        self._resource = None
        self._samplechain = None
        self._formatter = None
        self._parameters = ParameterList()

    def _get_resource(self):
        return self._resource
    def _set_resource(self, (resourceid, resource)):
        if not isinstance(resource, coreresource):
            raise Exception("%s is not a resource type" % resource)
        self._resource = (resourceid, resource)
    
    def _get_feature(self):
        return self._feature
    def _set_feature(self, feature):
        self._feature = feature

    def _get_samplechain(self):
        return self._samplechain
    def _set_samplechain(self, samplechain):
        self._samplechain = samplechain

    def _get_formatter(self):
        return self._formatter
    def _set_formatter(self, uri_formatter):
        if str(uri_formatter).endswith("Formatter_JSON"):
            self._formatter = JsonFormatter
        elif str(uri_formatter).endswith("Formatter_CSV"):
            self._formatter = DumbFormatter
        else:
            raise Exception("%s is not a formatter type" % uri_formatter)

    def _get_paramlist(self):
        return self._parameters
    
    def addParameter(self, parameter):
        self._parameters.append(parameter)


    samplechain = property(_get_samplechain,_set_samplechain,None)

    formatter = property(_get_formatter,_set_formatter,None)

    resource = property(_get_resource,_set_resource,None)

    feature = property(_get_feature,_set_feature,None)

    paramlist = property(_get_paramlist,None,None)
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
        if not isinstance(q, SingleQuery):
            raise Exception("Wrong type")
        self.atoms[reference] = q





#class BundleQueryBundle(QueryBundle):
#    def newQuery(self, key, feature, samplechain, resource, formatter):
#        if self.atoms.has_key(key):
#            raise Exception("Atomic query %s exists" % key)
#        Q = MonitorQuery()
#        Q.resource = resource 
#        Q.feature = feature
#        Q.samplechain = samplechain
#        Q.formatter = formatter
#        self.atoms[key] = Q
            
#    def addParameter(self, key, parameter):
#        if not self.atoms.has_key(key):
#            raise Exception("Atomic query %s does not exist" % key)
#        self.atoms[key].addParameter(parameter)
    

#class AggregatorQuery(Query):
#    def __init__(self):
#        Query.__init__(self)
#        self._processid = None
    
#    def _get_processid(self):
#        return self._processid
#    def _set_processid(self, processid):
#        self._processid = processid



#class SampleQuery(QueryBundle):
#    def newQuery(self, key, processid, feature, samplechain, formatter):
#        if self.atoms.has_key(key):
#            raise Exception("Atomic query %s exists" % key)
#        Q = AggregatorQuery()
#        Q.processid = processid
#        Q.feature = feature
#        Q.samplechain = samplechain
#        Q.formatter = formatter
#        self.atoms[key] = Q

#processid = property(_get_processid,_set_processid,None)
