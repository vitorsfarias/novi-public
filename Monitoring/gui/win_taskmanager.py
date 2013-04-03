'''
Created on May 24, 2012

@author: steger
'''

from win_skeleton import win_skeleton
from win_tasklist_functionality import tasklist_common
from widget_node import w_node
from widget_terminal import SshTerminal

class win_taskmanager(win_skeleton, tasklist_common):
    col_optional = {True: '#00ff00', False:'#009900'}

    def __init__(self, model, w_resource):
        '''
        Constructor
        '''
        self.model = model
        tasklist_common.__init__(self)
        self.ipar.resource_widget = w_resource
        wintitle = "Resource: %s" % w_resource.label
        self.taskmanager = model.taskmanager
        self.terminal = None
        
        win_skeleton.__init__(self, "windows/window_taskmanager.glade", "window_taskmanager")

        self.builder.get_object("button_addtask").connect('clicked', self.add_task)
        self.builder.get_object("button_removetask").connect('clicked', self.remove_task)
        model = self.taskmanager.model_metrics.filter_new()
        model.set_visible_func(self.cb_filter_model_metrics, self.ipar.resource_widget)
        self.render_metric(model)
        self.render_paramlist()
        model = self.taskmanager.model_tasks.filter_new()
        model.set_visible_func(self.cb_filter_model_tasks, self.ipar.resource_widget)
        self.render_tasklist(model)
        self.render_task(model)
        notebook = self.builder.get_object("notebook_taskmanager")
        notebook.set_current_page(1)
        
        if isinstance(w_resource, w_node):
            box = self.builder.get_object("box_terminal")
            self.terminal = SshTerminal()
            box.add(self.terminal)
            notebook.connect('switch_page', self.tabchange, w_resource)
        else:
            notebook.remove_page(0)

        window = self.builder.get_object("window_taskmanager")
        window.set_title(wintitle)
        window.connect("destroy", self.cancel, model)
        window.connect("destroy", self.shutdown)
        self.show_all()
    
    def tabchange(self, widget, page, pos, w_resource):
        print w_resource.label
        if pos == 0 and not self.terminal.isConnected:
            try:
                self.terminal.connectssh_ring(w_resource.resource.get_ipaddress("eth0"), self.model.keyring)
            except:
                for dif, dip, _, _, _ in w_resource.resource.interfaces():
                    print dif, dip
                    try:
                        self.terminal.connectssh_federica_ring(dip, "194.132.52.221", self.model.keyring)
                        break
                    except:
                        print "Not available"
                    
    
    def cb_filter_model_metrics(self, model, it, r):
        return model.get_value(it, 2) & r.typeid

    def cb_filter_model_tasks(self, model, it, r):
        return model.get_value(it, 5) == r

    def cancel(self, widget, model):
        if not len(model):
            if self.model.topology.removepath(self.ipar.resource_widget):
                try:
                    self.model.winmain.drawingarea.queue_draw()
                except:
                    pass
