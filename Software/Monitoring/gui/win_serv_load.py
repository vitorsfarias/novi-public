'''
Created on Oct 20, 2012

@author: steger
@author: gombos
@author: matuszka
'''

from gi.repository import Gtk
from win_skeleton import win_skeleton
from conf import url_slice_service, url_owl_service, ontology
from proxy import proxy

class win_serv_load(win_skeleton):
    '''
    '''
    
    def __init__(self, model):
        '''
        Constructor
        '''
        self._model = model
        self._document = model._topology_owl
        self.whoami = model.keyring.username
        win_skeleton.__init__(self, "windows/window_serv_load.glade", "window_serv_load")
        self.builder.get_object("button_fetch").connect('clicked', self.fetch_owl, False)
        self.builder.get_object("button_save").connect('clicked', self.fetch_owl, True)
        self.get_list_from_service()
        self.show_all()
    
    def get_list_from_service(self):
        treeview = self.builder.get_object("treeview_owl")
        # slice id, slice url
        self.treestore = Gtk.TreeStore(str, str)
        try:
            p = proxy()
            data = { 'user': self.whoami }
            content = p.post(uri = url_slice_service, form = data)
            print content
            for sl in content.split(','):
                self.treestore.append(None, [sl, "%s%s" % (ontology['core'][1], sl.split('.')[-1]) ])
        except Exception, e:
            self.error("Error contacting server @ %s: (%s)" % (url_slice_service, e))
            raise
        treeview.set_model(self.treestore)
        tvcol1 = Gtk.TreeViewColumn('Topology descriptors available')
        treeview.append_column(tvcol1)
        cell1 = Gtk.CellRendererText()
        tvcol1.pack_start(cell1,False)
        tvcol1.add_attribute(cell1, 'text', 0)

    def fetch_owl(self, widget, save = False):
        treeview = self.builder.get_object("treeview_owl")
        model, rows = treeview.get_selection().get_selected_rows()
        if len(rows) != 1:
            self.error("You must select a topology descriptor document")
            return
        if save:
            w = Gtk.FileChooserDialog(title = "Save topology description as ...",
                                      action = Gtk.FileChooserAction.SAVE,
                                      buttons = (Gtk.STOCK_CANCEL, Gtk.ResponseType.CANCEL, Gtk.STOCK_SAVE, Gtk.ResponseType.OK) )
            response = w.run()
            if response == Gtk.ResponseType.OK:
                fn = w.get_filename()
            else:
                fn = None
            w.destroy()
        it = model.get_iter(rows[0])
        owl_url = model.get_value(it, 1)
#TODO: progress bar in loading
        try:
            p = proxy()
            print owl_url
            data = { 'getSlice': owl_url }
#FIXME: this is hard coded!!!
            self._model.slice_name = 'novi_%s' % owl_url.split('#')[-1]
            content = p.post(uri = url_owl_service, form = data)
            self._document = content
            if save and fn:
                try:
                    with open(fn, "w") as fp:
                        fp.write(self._document)
                except Exception, e:
                    self.error("Cannot write content to %s" % fn)
        except Exception, e:
            self.error("Error contacting server @ %s: (%s)" % (url_owl_service, e))
        finally:
            self.destroy(self)
