'''
Created on Oct 20, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

from DataProcessing.Dimension import DimensionManager
from DataProcessing.DataError import ParameterError

class Parameter(object):
    '''
    @author: steger, jozsef
    @summary: 
    This class represents the control parameters of a monitoring task. 
    '''

    def __init__(self, name, valuetype, unitmanager, dimension, default = None):
        '''
        @summary: Constructor
        @param name: the name of the parameter
        @type name: str
        @param valuetype: the type of the parameter (used when reading value information)
        @type valuetype: type
@fixme: docs
        @param default: the preset unit aware value of the parameter 
        @type default: a tuple of value and unit
        '''
        self.um = unitmanager
        if not isinstance(dimension, DimensionManager.Dimension):
            raise ParameterError("wrong type of dimension")
        self._data = (name, valuetype, dimension)
        self._value = None
        if default is not None:
            self.value = default

    def __str__(self):
        if self._value is None:
            return "%s (%s)" % (self.name, self.dimension)
        else:
            return "%s (%s) = %s [%s] as %s" % (self.name, self.dimension.name, self._value[0], self._value[1], self.valuetype)
 
    @property
    def name(self):
        return self._data[0]

    @property
    def valuetype(self):
        return self._data[1]
    
    @property
    def dimension(self):
        return self._data[2]

    @property
    def value(self):
        return self._value
    @value.setter
    def value(self, value):
        _, unit = value
        if not self.dimension.containsUnit(unit):
            raise ParameterError("Unit %s is not in the basin of the dimension %s" % (unit, self.dimension))
        self._value = tuple(value)
    @value.deleter
    def value(self):
        self._value = None
    
    def copy(self):
        return Parameter(name = self.name, valuetype = self.valuetype, unitmanager = self.um, dimension = self.dimension, default = self.value)
    
    def convert(self, unit):
        '''
        @summary: returns the value of the given parameter in the required unit
        @param unit: the requested unit, which must adhere to the unit model of this parameter
        @type unit: Unit
        @return: the parameter value represented in the requested units
        @rtype: 
        @raise ParameterError: Unit not in dimension basin / Unit is not initialized
        '''
        if not self.dimension.containsUnit(unit):
            raise ParameterError("Unit %s is not in the basin of the dimension %s" % (unit, self.dimension))
        if self._value is None:
            raise ParameterError("%s is not initialized" % self)
        val, un = self._value
        if unit == un:
            return self.valuetype(val)
        else:
            return self.valuetype( self.um.convert(value = val, from_unit = un, to_unit = unit) )
    
    def convertToReferencedUnit(self, unitreference):
        '''
        @summary: returns the parameter value in units, where the unit is referenced
        @param unitreference: the reference to the requested unit, which must adhere to the unit model of this parameter
        @type unit: str
        '''
        return self.convert( self.um[unitreference] )



























class ParameterList(object):
    '''
    @author: steger, jozsef
    @summary: 
    This class represents a list of control parameters of a monitoring task. 
    '''

    def __init__(self, parameterlist = []):
        '''
        @summary: Constructor
        @param parameterlist: a list of parameters to handle together
        @type parameterlist: list(Parameter) or ParameterList
        '''
        self.parameter = {}
        self.extend(parameterlist)
    
    def __str__(self):
        '''
        '''
        return "<ParameterList> [%s\n\t]" % "\n\t\t".join([ "%s," % (p) for p in self.parameter.values() ])
    
    def __len__(self):
        '''
        @summary: return the size of the parameter list
        @return: the size of the parameter list
        @rtype: integer
        '''
        return len(self.parameter)
    
    def __iter__(self):
        '''
        @summary: provide an iterator over all the parameter elements
        @return: the next parameter
        @rtype: Parameter
        '''
        for p in self.parameter.values():
            yield p
            
    def __getitem__(self, key):
        '''
        @summary: provide the value of a parameter without unit conversion
        @return: current value
        @rtype: (str, unit)
        '''
        return self.parameter[key].value
    
    def append(self, p):
        '''
        @summary: append a new Parameter to the parameter list. If a wrong type of parameter is given, silently discard it. 
        In case a parameter with the same name exists overwrite its value only.
        @param p: a new parameter to add or an existing parameter to update former values 
        @type p: Parameter
        '''
        if not isinstance(p, Parameter):
            print "WW: %s is not a parameter" % str(p)
            return
        if self.has_key(p.name):
            print "WW: parameter with name %s is updated" % p.name
            self.parameter[p.name].value = p.value
        else:
            self.parameter[p.name] = p
    
    def has_key(self, name):
        '''
        @summary: Check if a parameter with a given name is already in the list
        @param name: the name of the parameter looking for
        @type name: str
        '''
        return self.parameter.has_key(name)
    
    def get(self, name, unit):
        '''
        @summary: Read the parameter pointed by a given name in the required unit
        @param name: the name of the parameter
        @type name: str
        @param unit: the target unit the caller wants the named parameter to be expressed in
        @type unit: Unit
        @raise ParameterError: no such parameter name
        '''
        if not self.has_key(name):
            raise ParameterError("No Parameter with name: %s" % name)
        return self.parameter[name].convert(unit)

    def getInReferencedUnits(self, name, unitreference):
        '''
        @summary: Read the parameter pointed by a given name in the required unit
        @param name: the name of the parameter
        @type name: str
        @param unitreference: the target unit the caller wants the named parameter to be expressed in
        @type unitreference: str
        @raise ParameterError: no such parameter name
        '''
        if not self.has_key(name):
            raise ParameterError("No Parameter with name: %s" % name)
        return self.parameter[name].convertToReferencedUnit(unitreference)
    
    def update(self, name, value, unit):
        '''
        @summary: reset the value of the parameter with the given name
        @param name: the name of the parameter to update
        @type name: str
        @param value: the new value
        @type value: depends on the Parameter.type
        @param unit: the new unit
        @type unit: Unit
        '''
        self.parameter[name].value = value, unit

    def updateInReferencedUnits(self, name, value, unitreference):
        '''
        @summary: reset the value of the parameter with the given name
        @param name: the name of the parameter to update
        @type name: str
        @param value: the new value
        @type value: depends on the Parameter.type
        @param unitreference: the new unit
        @type unitreference: str
        '''
        p = self.parameter[name]
        p.value = value, p.um[unitreference]
        
    def update_by_list(self, p_updating):
        '''
        @summary: update parameter list with matching elements of another parameter list
        @param p_updating: parameter list, whose matching elements update the element of this list
        @type p_updating: ParameterList
        @raise ParameterError: wrong argument type 
        '''
        if not isinstance(p_updating, ParameterList):
            raise ParameterError("wrong argument type")
        for name in p_updating.parameter_names():
            if self.has_key(name):
                v = p_updating.parameter[name].value
                if v is not None:
                    self.parameter[name].value = v
    
    def clear(self):
        '''
        @summary: Empty the parameter list
        '''
        self.parameter.clear()
    
    def copy(self):
        return ParameterList( map(lambda p: p.copy(), self) )
    
    def extend(self, parameterlist):
        '''
        @summary: extends this parameter list with the items of another parameter list
        @param paramlist: the list of parameter items to extend with
        @type paramlist: ParameterList 
        '''
        for p in parameterlist:
            self.append(p)
        
    def parameter_names(self):
        '''
        @summary: List the names of the currently hold parameters
        @return: list of Parameter.name
        @rtype: list 
        '''
        return self.parameter.keys()
    
    def formkeyvaldict(self):
        return dict( [ (name, p.value[0]) for (name, p) in self.parameter.iteritems() ] ) 
