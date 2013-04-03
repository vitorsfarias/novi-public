'''
Created on May 18, 2012

@author: steger
'''

from gi.repository import Gtk, Gdk
from widget_link import w_link
from widget_path import w_path

#FIXME: lookup Gdk constants for button.event in button_press/button_release 

class da_topology(Gtk.DrawingArea):
    '''
    '''
    ST_NOTHING = 0
    ST_NODE = 1
    ST_LINE = 2

    def __init__(self, topology, callback):
        Gtk.DrawingArea.__init__(self)
        
        self.callback = callback
        
        self.__len__ = topology.__len__
        self.w_nodes = topology.w_nodes
        self.w_links = topology.w_links
        self.path = topology.path
        self.find_node = topology.find_node
        self.movinglinks = Gtk.ListStore(w_link, bool)

        self.set_size_request(topology.width, topology.height)
        self.connect('draw', self.draw)
        self.add_events(Gdk.EventMask.BUTTON_PRESS_MASK | Gdk.EventMask.BUTTON_RELEASE_MASK | Gdk.EventMask.POINTER_MOTION_MASK)
        self.connect('button-press-event', self.button_press)
        self.connect('button-release-event', self.button_release)
        self.connect('motion-notify-event', self.motion)

        self.nodeNpos = None
        self.pathedited = None
        self._state = self.ST_NOTHING
        
    @property
    def mode(self):
        return self._state

    @mode.setter
    def mode(self, value):
        self._state = value
        self.queue_draw()
        
    def getsize(self):
        #TODO: is it the right way to get width/height?
        return self.get_size_request()
        
    def button_press(self, widget, event):
        x, y = event.x, event.y
        node = self.find_node(x, y)
        if node is None:
            return True
        if event.button == 1 and self.mode == self.ST_NOTHING:
            #left click
            node.on_button_press()
            node.selected = True
            self.nodeNpos = node, x, y
            self.selectlinkstomove(node)
            self.mode = self.ST_NODE
        elif event.button == 3:
            #right click
            node.on_button_press()
            self.nodeNpos = node, x, y
            name = "pathfrom_%s" % node.widgetid
            self.pathedited = w_path(fromx = node.x, fromy = node.y, tox = x, toy = y, label = name)
            self.mode = self.ST_LINE

    def button_release(self, widget, event):
        x, y = event.x, event.y
        if event.button == 1:
            #left click
            if self.mode == self.ST_NODE:
                node, _, _ = self.nodeNpos
                self.drop_node(node)
                self.mode = self.ST_NOTHING
        elif event.button == 3:
            #right click
            if self.mode == self.ST_LINE:
                nodesrc, _, _ = self.nodeNpos
                nodedst = self.find_node(x, y)
                self.pathedited = None
                if nodedst is None or nodedst == nodesrc:
                    # manipulate tasks of nodesrc
                    self.mode = self.ST_NOTHING
                    self.manipulate_node(nodesrc)
                else:
                    # manipulate tasks of link/path spanning between nodesrc -> nodedst
                    self.mode = self.ST_NOTHING
                    self.manipulate_path(nodesrc, nodedst)

    def motion(self, widget, event):
        x, y = event.x, event.y
        if self.mode == self.ST_NOTHING:
            return
        node, ox, oy = self.nodeNpos
        if self.mode == self.ST_NODE:
            X, Y = self.getsize()
            if x < 0 or y < 0 or x > X or y > Y:
                self.drop_node(node)
                self.mode = self.ST_NOTHING
                return
            dx = x - ox
            dy = y - oy
            self.movelinks(x, y)
            node.motion(dx, dy)
            self.nodeNpos = node, x, y
        elif self.mode == self.ST_LINE:
            self.pathedited.motion(x, y)
        self.queue_draw()
        
    def drop_node(self, node):
        self.movelinks(node.x, node.y)
        node.selected = False
        self.nodeNpos = None

    def selectlinkstomove(self, node):
        self.movinglinks.clear()
        for link, nodefrom, nodeto in self.w_links():
            if nodefrom == node:
                self.movinglinks.append([link, False])
            elif nodeto == node:
                self.movinglinks.append([link, True])

    def movelinks(self, x, y):
        it = self.movinglinks.get_iter_first()
        while it:
            link, destinationismoving = self.movinglinks.get(it, 0,1)
            it = self.movinglinks.iter_next(it)
            link.motion(x, y, destinationismoving)

    def draw(self, da, ctx):
        for link, _, _ in self.w_links():
            link.draw(da, ctx)
        if self.pathedited is not None:
            self.pathedited.draw(da, ctx)
        for node in self.w_nodes():
            node.draw(da, ctx)
            
    def manipulate_node(self, node):
        self.callback(node)

    def manipulate_path(self, node1, node2):
        mypath = self.path(node1, node2)
        self.callback(mypath)
