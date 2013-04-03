'''
Created on Oct 6, 2011

@author: steger
'''

from gi.repository import Gtk, Gdk, GLib
from model_topology import topology
from model_taskmanager import taskmanager
from win_main import win_main
from proxy import msproxy
from Semantics.InformationModel import Ontology
from Semantics.UnitModel import UnitModel
from Semantics.FeatureModel import FeatureModel
from conf import ontology, credential
from model_keyring import keyring


class MonitorApp(object):
    ms = msproxy()
    
    def __init__(self):
        self.ontology = Ontology()
        for prefix, (owl_url, ns) in ontology.iteritems():
            if owl_url is None:
                continue
            self.ontology.load(prefix, owl_url, ns)
        self.unitmodel = UnitModel(self.ontology)
        self.featuremodel = FeatureModel(self.dm, self.um, self.ontology)

        self._topology_owl = None
        self.slice_name = ''
        cred_id, cred_list = credential
        self.keyring = keyring(cred_id)
        for c in cred_list:
            self.keyring.add_credential(**c)
        self.topology = topology()
        self.taskmanager = taskmanager(self)
        self.winmain = win_main(self)
        self.winmain.show_all()

    @property
    def pm(self): return self.unitmodel.pm

    @property
    def um(self): return self.unitmodel.um

    @property
    def dm(self): return self.unitmodel.dm

    @property
    def im(self): return self.ontology

if __name__ == "__main__":
    app = MonitorApp()
    try:
        GLib.threads_init()
        Gdk.threads_init()
        Gdk.threads_enter()
        Gtk.main()
        Gdk.threads_leave()
    except Exception, e:
        print e
    print "exited"
