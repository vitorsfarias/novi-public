'''
Created on Oct 6, 2011

@author: steger
'''

from gi.repository import Gtk
from win_keyring import win_keyring
from win_serv_load import win_serv_load
from win_taskmanager import win_taskmanager
from win_skeleton import win_skeleton
from win_tasklist_functionality import tasklist_common
from topologyParser import TopologyParser
from drawingarea_topology import da_topology
from conf import defaultfolder, preload_topology

class win_main(win_skeleton, tasklist_common):

    def __init__(self, model):
        self.model = model
        tasklist_common.__init__(self)
        self.taskmanager = model.taskmanager
        
        win_skeleton.__init__(self, "windows/window_main.glade", "window")
        self.builder.get_object("window").connect('destroy', lambda w: Gtk.main_quit())
        self.builder.get_object("button_quit").connect('clicked', lambda w: Gtk.main_quit())

        # My keyring tab
        self.render_credentials()
        self.builder.get_object("button_credential_dialog").connect('clicked', self.show_cred_window)
        self.builder.get_object("button_removecredential").connect('clicked', self.remove_credential)

        # Topology tab
        #FIXME: if no file is selected the last topology name is dropped
        #FIXME: current directory is changed after loading document
        self.builder.get_object("notebook").set_current_page(1)
        self.drawingarea = None
        topo_butt_local = self.builder.get_object("filechooserbutton_owl")
        topo_butt_local.set_current_folder(defaultfolder)
        topo_butt_local.connect('file-set', self.load_topo_from_file)
        self.builder.get_object("button_serv_load").connect('clicked', self.show_serv_load_window)

        # Tasklist tab
        self.render_metric(self.model.taskmanager.model_metrics)
        self.render_resource()
        self.render_paramlist()
        self.render_tasklist(self.model.taskmanager.model_tasks)
        #FIXME: disable combobox_resource when no resource is selected
        self.builder.get_object("combobox_resource").connect('changed', self.changed_resource)
        self.builder.get_object("button_addtask").connect('clicked', self.add_task)
        self.builder.get_object("button_removetask").connect('clicked', self.remove_task)

        # Console tab
        self.render_task(self.taskmanager.model_tasks)

        class fw:
            @staticmethod
            def get_filename(): return preload_topology
        self.load_topo_from_file(fw)

        self.show_all()
        
    def __del__(self):
        Gtk.main_quit()
        
# My keyring related methods
    def render_credentials(self):
        treeview = self.builder.get_object("treeview_keyring")
        treeview.set_model(self.model.keyring)
        tvcol1 = Gtk.TreeViewColumn('User name', Gtk.CellRendererText(), text = 1)
        treeview.append_column(tvcol1)

    def show_cred_window(self, widget):
        #TODO: disable credential buttons
        win_keyring(self.model)

    def remove_credential(self, widget):
        treeview = self.builder.get_object("treeview_keyring")
        treeselection = treeview.get_selection()
        (model, it) = treeselection.get_selected()
        if it is not None:
            model.remove(it)

# Topology related methods
    def show_serv_load_window(self, widget):
        win = win_serv_load(self.model)
        win.connect('destroy', self.load_topo_from_service, win)

    def load_topo_from_service(self, widget, win):
        self.model._topology_owl = win._document
        self.render_topology(widget)
    
    def load_topo_from_file(self, widget):
        fn = widget.get_filename()
        with open(fn) as fp:
            self.model._topology_owl = fp.read()
        self.render_topology(widget)

    def render_topology(self, widget):
        try:
            tp = TopologyParser(informationmodel = self.model.im, topology_owl = self.model._topology_owl)
            topo = self.model.topology
            topo.clear()
            da = da_topology(topo, self.selected_resource)
            print "LOAD TOPOLOGY"
            for uri_iface, ifname, hostname, ip, direction in tp.interfaces():
                topo.add_interface(uri_iface, ifname, hostname, ip, direction)
            print "***", len(topo.interfaces), "interfaces"
            for uri_node, uri_iface in tp.nodes():
                topo.add_node(uri_node, uri_iface)
            print "***", len(topo.nodecontainer), "nodes"
            for uri_link, uri_source, uri_destination, uri_if1, uri_if2 in tp.links():
                topo.add_link(uri_link, uri_link, uri_source, uri_destination, uri_if1, uri_if2)
            print "***", len(topo.linkcontainer), "links"

            print "nodes and their valid interfaces"
            for n in topo.w_nodes():
                print "N:", n.label
                for iface, address, ispublic, hostname, direction in n.resource.interfaces():
                    print "  --", iface, address, ispublic, hostname, direction
                
        except Exception:
            self.error("Cannot read or parse topology document")
            raise
        if not len(topo):
            self.error("Document defines an empty topology" )
            return
        topo.forceDirectedCalc();
        da.queue_draw()
        vp = self.builder.get_object("viewport_topo")
        if self.drawingarea:
            tasks = self.model.taskmanager.retrieve_tasklist_of_slice(-1)
            if len(tasks):
                response = self.choice("Warning: %d tasks are still running. If you select Yes, they are terminating, and the requested topology is loaded" % len(tasks))
                if response == Gtk.ResponseType.NO:
                    # nothing is done
                    #TODO: set filename if necessary
                    print "REVERT"
                    return
                print "stopping tasks"
                try:
                    for t in tasks:
                        del(t)
                finally:
                    self.taskmanager.model_tasks.clear()
            print "CHANGING TOPOLOGY"
            vp.remove(self.drawingarea)
        print "finish"
        self.drawingarea = da
        vp.add(da)
        vp.show_all()
    
    def cancel_loadtopology(self, widget):
        #FIXME: file select dialog window associated with filechooser button...
        print widget.get_window()

    # activate/manipulate measurements
    def selected_resource(self, resource):
        win_taskmanager(self.model, resource)

    def render_resource(self):
        combobox = self.builder.get_object("combobox_resource")
        self.resourcecombobox = combobox
        combobox.set_active(0)
        cell = Gtk.CellRendererText()
        combobox.pack_start(cell, True)
        combobox.add_attribute(cell, 'text', 0)
        self.change_metric(self.builder.get_object("combobox_metric"))

    def changed_resource(self, combobox):
        model = combobox.get_model()
        index = combobox.get_active()
        if index >= 0:
            self.ipar.resource_widget = model[index][1]
            sensitive = True
        else:
            sensitive = False
        self.builder.get_object("button_addtask").set_sensitive(sensitive)