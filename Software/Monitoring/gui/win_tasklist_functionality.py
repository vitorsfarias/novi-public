'''
Created on May 31, 2012

@author: steger
'''

from gi.repository import Gtk
from random import randint
from rdflib import URIRef
from model_taskparameter import taskparameter
from widget_node import w_node
from threading import Thread, Event


class tasklist_common(object):
    refresh = 1
    def __init__(self):
        self.ipar = taskparameter(self.model)
        self.keyring = self.model.keyring
#        self.service = self.model.service
        self.resourcecombobox = None
        self.newtaskoutputselected = Event()
        self.t = None
    
    def shutdown(self, widget):
        print "window destroyed", widget
        if self.t:
            print "wait for thread to finish"
            self.newtaskoutputselected.set()
            self.t.join()
        
    @property
    def taskname(self):
        return self.builder.get_object("entry_taskname").get_text()
    @taskname.setter
    def taskname(self, value):
        task = self.builder.get_object("entry_taskname")
        if self.taskmanager.wrongName(value):
            self.error("A task name must be unique. Generating a random...")
            del(self.taskname)
        task.set_text(value)
    @taskname.deleter
    def taskname(self):
        task = self.builder.get_object("entry_taskname")
        taskname = "%s%d" % (self.metric[0][:8], randint(0, 1000))
        task.set_text(taskname)
        
    @property
    def metric(self):
        combobox = self.builder.get_object("combobox_metric")
        model = combobox.get_model()
        path = combobox.get_active()
        metric, uri_feature, _ = model[path]
        return metric, URIRef(uri_feature)
        self.error("Metric is not selected")

    def render_metric(self, model):
        combobox = self.builder.get_object("combobox_metric")
        combobox.set_model(model)
        combobox.set_active(0)
        cell = Gtk.CellRendererText()
        combobox.pack_start(cell, True)
        combobox.add_attribute(cell, 'text', 0)
        combobox.connect('changed', self.change_metric)
        self.change_metric(combobox)   

    def render_paramlist(self):
        treeview = self.builder.get_object("treeview_parameters")
        treeview.set_model(self.ipar._parameters)
        tvcol1 = Gtk.TreeViewColumn('Parameter name', Gtk.CellRendererText(), text = 0, background = 8)
        treeview.append_column(tvcol1)
        cell2 = Gtk.CellRendererCombo()
        cell2.set_properties(text_column = 0, editable = True)
        cell2.connect ('edited', self.paramvalue_changed, False)
        tvcol2 = Gtk.TreeViewColumn('value', cell2, text = 1, model = 4)
        treeview.append_column(tvcol2)
        cell3 = Gtk.CellRendererCombo()
        cell3.set_properties(text_column = 0, editable = True)
        cell3.connect ('edited', self.paramvalue_changed, True)
        tvcol3 = Gtk.TreeViewColumn('unit', cell3, text = 2, model = 5)
        treeview.append_column(tvcol3)
        cell4 = Gtk.CellRendererToggle()
        cell4.connect("toggled", self.activate_parameter)
        tvcol4 = Gtk.TreeViewColumn('Activated', cell4, active = 7)
        treeview.append_column(tvcol4)
        treeview.set_search_column(0)
        tvcol1.set_sort_column_id(0)
        treeview.set_reorderable(True)

    def render_tasklist(self, model):
        treeview = self.builder.get_object("treeview_tasks")
        treeview.set_model(model)
        treeview.set_tooltip_column(4)

        tvcol1 = Gtk.TreeViewColumn('User defined name', Gtk.CellRendererText(), text = 0)
        treeview.append_column(tvcol1)
        tvcol2 = Gtk.TreeViewColumn('Task', Gtk.CellRendererText(), text = 1)
        treeview.append_column(tvcol2)
        cell3 = Gtk.CellRendererToggle()
        cell3.connect("toggled", self.toggle_task)
        tvcol3 = Gtk.TreeViewColumn('Enabled', cell3, active = 3)
        treeview.append_column(tvcol3)

        treeview.set_search_column(0)
        treeview.set_search_column(1)
        tvcol1.set_sort_column_id(0)
        tvcol2.set_sort_column_id(1)
        treeview.set_reorderable(True)

    def render_task(self, model):
        combobox = self.builder.get_object("combobox_task")
        combobox.set_model(model)
        cell1 = Gtk.CellRendererText()
        cell2 = Gtk.CellRendererText()
        combobox.pack_start(cell1, True)
        combobox.pack_start(cell2, True)
        combobox.add_attribute(cell1, 'text', 0)
        combobox.add_attribute(cell2, 'text', 1)
        combobox.connect('changed', self.change_output)

    def change_metric(self, combobox):
        model = combobox.get_model()
        index = combobox.get_active()
        _, uri_feature, resourcetypeid = model[index]
        del(self.taskname)
        self.ipar.metric = uri_feature
        if self.resourcecombobox:
            if resourcetypeid == w_node.typeid:
                self.resourcecombobox.set_model( self.model.topology.nodecontainer )
            else:
                self.resourcecombobox.set_model( self.model.topology.linkcontainer )
        
    def change_output(self, combobox):
        # finish data collecting thread of old metric if any
        if self.t is not None:
            print "wait for thread to finish"
            self.newtaskoutputselected.set()
            self.t.join()
            self.t = None
            
        model = combobox.get_model()
        index = combobox.get_active()
        if index == -1:
            print "nothing selected"
            return

        taskname, _, taskid, _, tip, _, hdrmodel, model = model[index]
        # regenerate header of the table
        treeview = self.builder.get_object("treeview_console")
        columns = treeview.get_columns()
        while len(columns):
            treeview.remove_column(columns.pop())
        it = hdrmodel.get_iter_first()
        while it:
            col = hdrmodel.get_value(it, 0)
            it = hdrmodel.iter_next(it)
            treeview.append_column(col)

        treeview.set_model(model)
        combobox.set_tooltip_text(tip)

        # launch a new thread to collect data
        self.newtaskoutputselected.clear()
        self.t = Thread(target = self.retrieve, kwargs = {'taskname': taskname, 'taskid': taskid, 'model': model})
        self.t.setDaemon(True)
        self.t.start()
        
    def retrieve(self, **kw):
        taskname = kw.get('taskname')
        taskid = kw.get('taskid')
        model = kw.get('model')
        print "TT: thread start for %s" % taskname
        while True:
            if self.newtaskoutputselected.wait(self.refresh):
                break
            result = self.taskmanager.call('fetchTaskData', taskid)
            #[{<DataProcessing.DataFormatter.DumbFormatter object at 0x26de490>: 
            #HDR:<DataHeader 41162320>: {uptimeinfo: [Run (Countable) [(1)], Uptime (TimeInterval) [s]]}
            # DATA:[
            #(4, 1256497.43)
            #]}]
            # FIXME: currently we keep only the first table
            table = result[1:-1].split("\n,\n")[0]
            for rec in table.split("\n")[3:-1]:
                if rec.endswith("), "):
                    model.append(rec[1:-3].split(", "))
                else:
                    model.append(rec[1:-1].split(", "))
        print "TT: thread ended for %s" % taskname

    def toggle_task(self, widget, path):
        try:
            self.taskmanager.toggle_task(path)
        except Exception, e:
            self.error(e)

    def add_task(self, widget):
        try:
            self.taskmanager.add_task(self)
        except Exception, e:
            self.error(e)

    def remove_task(self, widget):
        treeview = self.builder.get_object("treeview_tasks")
        treeselection = treeview.get_selection()
        (model, it) = treeselection.get_selected()
        taskname = model[it][0]
        taskid = model[it][2]
        self.taskmanager.remove_task(taskname, taskid)

    def activate_parameter(self, widget, path):
        model = self.ipar._parameters
        obligatory = model[path][6]
        if obligatory:
            print "WW: this is obligatory, cannot change"
        else:
            print "II: parameter activity toggle"
            activity = not model[path][7]
            model[path][7] = activity
            model[path][8] = self.ipar.col_optional[activity]

#TODO: this function is implemented twice. put it somewhere common
    def _find(self, model, item, position = 0):
        it = model.get_iter_first()
        while it:
            model_item = model.get_value(it, position)
            if model_item == item:
                return it
            it = model.iter_next(it)
        print "WW: Cannot find item %s" % item
        return None

#TODO: rename: change_paramvalue
    def paramvalue_changed(self, widget, path, newtext, unitchange):
        print "arg:", unitchange
        model = self.ipar._parameters
        if not len(newtext):
            self.error("You should provide a parameter value, which is not empty, won't change this time")
            return
        if unitchange:
            if model[path][2] == newtext:
                return
            it = self._find(model = model[path][5], item = newtext)
            if it:
                model[path][2] = newtext
                model[path][3] = model[path][5].get_value(it, 1)
                print "unit change -->", newtext
            else:
                self.error("You cannot define a new unit")
        else:
            if model[path][1] == newtext:
                return
            ran = model[path][4]
            if self._find(model = ran, item = newtext) is None:
                ran.append([newtext])
            model[path][1] = newtext
            print "value change -->", newtext
