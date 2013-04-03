'''
Created on May 24, 2012

@author: steger
'''

from gi.repository import Gtk
from rdflib import URIRef
from widget_core import w_core
from widget_node import w_node
from widget_path import w_path
from queryGenerator import QueryGenerator

#TODO: handle parameter value change

class taskmanager(object):
    UNKNOWN = 0
    uri_domain = URIRef("http://fp7-novi.eu/monitoring_task.owl#Slice")
    uri_node = URIRef("http://fp7-novi.eu/monitoring_features.owl#NodeResource")
    uri_link = URIRef("http://fp7-novi.eu/monitoring_features.owl#PathResource")

    def __init__(self, model):
        '''
        Constructor
        '''
        self.im = model.im
        self.dm = model.dm
        self.featuremodel = model.featuremodel
        self.call = model.ms.call
        
        self.keyring = model.keyring

        #Metric name, uri_feature, resource type
        self.model_metrics = Gtk.ListStore(str, str, int)
        self.fill_model_metrics()

        #Parameter name, default value, default unit, default unit reference, value range, unit range
        self.model_parameters = Gtk.ListStore(str, str, str, str, Gtk.ListStore, Gtk.ListStore)
        self.fill_model_parameters(self.dm)
        
        #Task name, metric, Task identifier, state, tool tip, resource, Data header, Task data serialized
        self.model_tasks = Gtk.ListStore(str, str, str, bool, str, w_core, Gtk.ListStore, Gtk.ListStore)

    def fill_model_metrics(self):
        for uri_feature, featureName, uri_resource in self.featuremodel.inferFeatures():
            if uri_resource == self.uri_node:
                self.model_metrics.append([ featureName, str(uri_feature),  w_node.typeid])
            elif uri_resource == self.uri_link:
                self.model_metrics.append([ featureName, str(uri_feature),  w_path.typeid])

    def fill_model_parameters(self, dimensionmanager):
        unit_models = {}
        for d in dimensionmanager:
            model = Gtk.ListStore(str, str)
            for u in d.basin:
                model.append([u.reference, u.symbol])
            unit_models[d.reference] = model
        for name, dimension, value, unit in self.featuremodel.inferFeatureMonitoringParameters():
            #Parameter name, default value, default unit, value range, unit range
            valuerange = Gtk.ListStore(str)
            if value != "":
                valuerange.append([value])
            self.model_parameters.append([name, value, unit.symbol, unit.reference, valuerange, unit_models[dimension.reference]])

    def find_parameter_by_name(self, name):
        it = self.model_parameters.get_iter_first()
        while it:
            if self.model_parameters.get_value(it, 0) == name:
                return it
            it = self.model_parameters.iter_next(it)
        return None

    def _find(self, model, item, position = 0):
        it = model.get_iter_first()
        while it:
            model_item = model.get_value(it, position)
            if model_item == item:
                return it
            it = model.iter_next(it)
        print "WW: Cannot find item %s" % item
        return None
            
    def wrongTaskname(self, tn):
        return self._find(self.model_tasks, tn, 0)
            
    def add_task(self, window):
        name = window.taskname
        it = self.wrongTaskname(name)
        if it:
            response = window.choice("The name of your Task is not unique. Shall I drop your former Task with the same name (%s)?" % name)
            if response == Gtk.ResponseType.NO:
                return
            else:
                self.remove_task(it)
        metricname, uri_feature = window.metric
        
        qg = QueryGenerator(informationmodel = self.im, feature = uri_feature)
        qg.addResource(window.ipar.resource)
        for p in window.ipar.parameters():
            qg.addParameter(p)

        taskid = self.call('addTask', qg.query)
        d_hdr = self.call('describeTaskData', taskid)
        
        #[<DataHeader 63042768>: {diskinfo: [Run (Countable) [(1)], Available Disk Size (InformationSize) [bit], Disk Size Used (InformationSize) [kB]]}]
        #TODO: use re
        hdrmodel = Gtk.ListStore(Gtk.TreeViewColumn)
        idx = 0
        #FIXME: we keep only the first table
        h = d_hdr[1:-1].split("\n,\n")[0]
        _, mat = h[:-2].split(": [", 2)
        cols = mat.split(", ")
        for c in cols:
            label, rest = c.split(" (", 2)
            _, unitsymbol = rest[:-1].split("[")
            #TODO: CellRendererText(background = '#FF0F0F') if metric is selected 
            col = Gtk.TreeViewColumn('%s\n%s' % (label, unitsymbol), Gtk.CellRendererText(), text = idx)
            hdrmodel.append([col])
            idx += 1

        declaration = [str] * idx
        model = Gtk.ListStore(*declaration)
        self.model_tasks.append([name, metricname, taskid, True, window.ipar.tooltip, window.ipar.resource_widget, hdrmodel, model])
        print "+ %s (# %d)" % (window.taskname, len(self.model_tasks))

    def toggle_task(self, path):
        taskname, metric, taskid, enabled, _, _, _, _ = self.model_tasks[path]
        try:
            if enabled:
                self.call('disableTask', taskid )
                print "%s (%s) disabled" % (taskname, metric)
#FIXME:
# in case task is disabled, console thread should not call the service coz unnecessary
            else:
                self.call('enableTask', taskid )
                print "%s (%s) enabled" % (taskname, metric)
        finally:
            self.model_tasks[path][3] = self.call('getTaskStatus', taskid ) == "True"

    def remove_task(self, taskname, taskid):
        it = self._find(self.model_tasks, taskname)
        self.model_tasks.remove(it)
        #TODO: ask whether to kill tasks
        self.call('removeTask', taskid )
        print "- %s (# %d)" % (taskname, len(self.model_tasks))
    
    def retrieve_tasklist_of_slice(self, sliceID):
        raise Exception("LLLLLLIIIISSSSTTTT")
        return [ t for t in self.service.stm.tasks_of_slice(sliceID) ]
