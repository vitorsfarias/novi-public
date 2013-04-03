'''
Created on May 29, 2012

@author: steger
'''

from gi.repository import Gtk
from DataProcessing.Parameter import ParameterList, Parameter
from rdflib import URIRef
from widget_core import w_core

class taskparameter(object):
#FIXME: ugly
    ref_dim_address = "http://fp7-novi.eu/unit.owl#"
    ref_dim_nameofsomething = "NameOfSomething" #http://fp7-novi.eu/unit.owl#
#FIXME: ugly to redefine coloring here, cant it be rendered? 
    col_compulsory = '#ff0000'
    col_optional = {True: '#00ff00', False:'#009900'}

    def __init__(self, model):
        '''
        Constructor
        '''
        self.taskmanager = model.taskmanager
        self.um = model.um
        self.dm = model.dm
        
        print "TASKPAR INIT", len(model.dm)
        for d in model.dm:
            print "DIM", d, d.reference
        
        self.featuremodel = model.featuremodel
        self.slicename = model.topology.pointer.name
        self._resourcewidget = None
        self._metric = None
        #Parameter name
        #Parameter current value
        #Parameter current unit
        #Parameter current unit reference
        #Parameter value range
        #Parameter unit range
        #Parameter obligatory
        #Parameter active
        #Background color
        self._parameters = Gtk.ListStore(str, str, str, str, Gtk.ListStore, Gtk.ListStore, bool, bool, str)
        self._parlist = ParameterList()

    def clear(self):
        self._parameters.clear()
        self._parlist.clear()

    @property
    def metric(self):
        return self._metric
    @metric.setter
    def metric(self, uri_feature):
        self.clear()
        uri_feature = URIRef(uri_feature)
        self._metric = uri_feature
        par_obligatory = self.featuremodel.inferObligatoryParametersOf(uri_feature)
        par_optional = self.featuremodel.inferOptionalParametersOf(uri_feature)
        for p in par_obligatory:
            self._parlist.append(p)
            it = self.taskmanager.find_parameter_by_name(p.name)
            valuestr, symbol, reference, valuerange, unitrange = self.taskmanager.model_parameters.get(it, 1,2,3,4,5)
            self._parameters.append([p.name, valuestr, symbol, reference, valuerange, unitrange, True, True, self.col_compulsory])
        for p in par_optional:
            self._parlist.append(p)
            it = self.taskmanager.find_parameter_by_name(p.name)
            valuestr, symbol, reference, valuerange, unitrange = self.taskmanager.model_parameters.get(it, 1,2,3,4,5)
            self._parameters.append([p.name, valuestr, symbol, reference, valuerange, unitrange, False, True, self.col_optional[True]])
    @metric.deleter
    def metric(self):
        self.clear()
        self._metric = None
        
    @property
    def resource(self):
        if self._resourcewidget:
            return self._resourcewidget.resource
        raise Exception("resource_widget is not set")

    @property
    def resource_widget(self):
        return self._resourcewidget
    @resource_widget.setter
    def resource_widget(self, w):
        if not isinstance(w, w_core):
            raise Exception("resource_widget must have a resource attribute")
        self._resourcewidget = w
    @resource_widget.deleter
    def resource_widget(self):
        self._resourcewidget = None

    def parameters(self):
        dim_nameofsomething = self.dm[ self.ref_dim_nameofsomething ]
        yield Parameter(name = "SliceName", valuetype = str, unitmanager = self.um, 
                        dimension = dim_nameofsomething, default = (self.slicename, self.um.unitless))
        it = self._parameters.get_iter_first()
        while it is not None:
            pname, pvalue, puref, active = self._parameters.get(it, 0,1,3,7)
            it = self._parameters.iter_next(it)
            if active:
                # either an obligatory parameter
                # or an optional parameter, which the user opted to use
                p = self._parlist.parameter[pname]
                unit = self.um.getUnitByReference(puref)
                p.value = (pvalue, unit)
                yield p
    
    @property
    def tooltip(self):
        tooltip = ["Resource:\t<b>%s</b>" % self.resource.name ]
        for p in self.parameters():
            pvalue, punit = p.value
            tooltip.append("%s=\t<b>%s</b> %s" % (p.name, pvalue, punit))
        return "\n".join(tooltip)

