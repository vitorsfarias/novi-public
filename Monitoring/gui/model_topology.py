'''
Created on May 18, 2012

@author: steger
'''

from gi.repository import Gtk
from random import randint
from Resource.interface import interface
from Resource.node import node
from Resource.path import path
from Resource.link import link
from widget_node import w_node
from widget_link import w_link
from widget_path import w_path
from Resource.slice import slice_pointer
from math import sqrt

class topology(object):
    def __init__(self):
        self.width = 700
        self.height = 500
        self.nodecontainer = Gtk.ListStore(str, w_node)
        self.linkcontainer = Gtk.ListStore(str, w_link, w_node, w_node)
        self.interfaces = {}
        self.pointer = slice_pointer()
        #FIXME: set pointer data dynamically tpologyParser?
        self.pointer.name = "novi_novi"

    def __len__(self):
        return len(self.nodecontainer)
    
    def size(self):
        return len(self.nodecontainer), len(self.linkcontainer)

    def clear(self):
        self.nodecontainer.clear()
        self.linkcontainer.clear()
        self.interfaces.clear()
        #TODO: CLEAR self.pointer.name

    def w_nodes(self):
        it = self.nodecontainer.get_iter_first()
        while it:
            node, = self.nodecontainer.get(it, 1)
            it = self.nodecontainer.iter_next(it)
            yield node

    def w_links(self):
        it = self.linkcontainer.get_iter_first()
        while it:
            link, node_from, node_to = self.linkcontainer.get(it, 1,2,3)
            it = self.linkcontainer.iter_next(it)
            yield link, node_from, node_to

    def find_node_by_id(self, widgetid):
        for node in self.w_nodes():
            if node.widgetid == widgetid:
                return node
        return None

    def find_path_by_widgetid(self, wid):
        for p, _, _ in self.w_links():
            if isinstance(p, w_path) and p.widgetid == wid:
                return p
        return None

    def find_link_by_sourceNdestination(self, w_source, w_destination):
        for p, _, _ in self.w_links():
            if isinstance(p, w_link) and p.resource.source == w_source.resource and p.resource.destination == w_destination.resource:
                return p
        return None
    
    def find_node(self, x, y):
        for node in self.w_nodes():
            if node.is_over(x, y):
                return node
        return None

    def add_interface(self, resourceid, ifname, hostname, address, direction):
        if self.interfaces.has_key(resourceid):
            iface = self.interfaces[resourceid]
            if iface.address != address or iface.hostname != hostname or iface.ifname != ifname:
                print "EE: interface with resource id %s is redeclared and miss match, keeping first" % resourceid
                return
            iface.direction |= direction
            return
        iface = interface(hostname, resourceid)
        iface.address = address
        iface.interface = ifname
        iface.direction = direction
        iface.hostname = hostname
        #TODO: iface.ispublic based on IP type
        self.interfaces[resourceid] = iface

    def add_node(self, resourceid, ifaceid):
        iface = self.interfaces[ifaceid]
        n = self.find_node_by_id(resourceid)
        if n:
            print "EE: node already added, updating interfaces"
            if iface.isvalid:
                n.resource.addinterface(iface)
            return
        if iface.isvalid:
            iface.ispublic = True
            hostname = iface.hostname
            r = node(hostname, resourceid)
            r.addinterface(iface)
        else:
            print "EE: interface is not valid using resource id as a name"
            r = node(resourceid, resourceid)
        x = randint(1, self.width)
        y = randint(1, self.height)
        n = w_node(x, y, r)
        self.nodecontainer.append([ n.label, n ])
        return n

    def add_link(self, resourceid, name, nodeid1, nodeid2, ifid1, ifid2):
        node1 = self.find_node_by_id(nodeid1)
        node2 = self.find_node_by_id(nodeid2)
        if not node1 or not node2:
            raise Exception("Cannot connect %s %s" % (node1, node2))
        
        l = self.find_link_by_sourceNdestination(node1, node2)
        if l is not None:
            print "EE: another link between these two nodes exists, doing nothing"
            return l
        
        if1 = self.interfaces[ifid1]
        if if1.isvalid:
            node1.resource.addinterface(if1)
        if2 = self.interfaces[ifid2]
        if if2.isvalid:
            node2.resource.addinterface(if2)
        r = link(name = name, resourceid = resourceid, source = node1.resource, destination = node2.resource)
        l = w_link(fromx = node1.x, fromy = node1.y, tox = node2.x, toy = node2.y, resource = r, label = name)
        self.linkcontainer.append([l.label, l, node1, node2])
        return l
    
    def path(self, node1, node2):
        if not node1 or not node2:
            raise Exception("Cannot connect %s %s" % (node1, node2))
        name = "path_%s-%s" % (node1.widgetid, node2.widgetid)
        p = self.find_path_by_widgetid(name)
        if p is not None:
            # a path already defined
            print "WW: path %s exists, nothing added" % p.label
            return p
        w = self.find_link_by_sourceNdestination(node1, node2)
        if w is None:
            # a new path
            r = path(name = name, resourceid = name, source = node1.resource, destination = node2.resource)
            p = w_path(fromx = node1.x, fromy = node1.y, tox = node2.x, toy = node2.y, resource = r, label = name)
            self.linkcontainer.append([p.label, p, node1, node2])
            print "II: new path %s" % r.name
            return p
        else:
            # a path over a link
            print "II: path over a link %s, nothing added" % w.resource.name
            return w
    
    def removepath(self, path):
        if not isinstance(path, w_path):
            print "WW: You are allowed to remove w_path instances only, I got %s" % path
            return False
        it = self.linkcontainer.get_iter_first()
        while it:
            p = self.linkcontainer.get_value(it, 1)
            if p == path:
                self.linkcontainer.remove(it)
                return True
            it = self.linkcontainer.iter_next(it)
        print "WW: %s was not found in my list" % path
        return False

    def forceDirectedCalc(self):
        '''
        @author: gombosg
        @author: matuska
        '''
        delta_thresh = 0.00005
        kinect = 1
        old_kinect = 0
        while abs(kinect - old_kinect) > delta_thresh:
            old_kinect = kinect
            kinect = 0
            for node in self.nodecontainer:
                for other_node in self.nodecontainer:
                    if node[1] != other_node[1]:
                        force_x = 0
                        force_y = 0
                        force_xpoz = 0
                        force_ypoz = 0
                        dist = sqrt((node[1].x - other_node[1].x)**2 + (node[1].y - other_node[1].y)**2)/100
                        if node[1].x > other_node[1].x:
                            force_x = (int(self.width/2) - (node[1].x - other_node[1].x))*-0.001
                            force_xpoz = (node[1].x - other_node[1].x)*-0.001*dist
                        else:
                            force_x = (int(self.width/2) - (other_node[1].x - node[1].x))*0.001
                            force_xpoz = (node[1].x - other_node[1].x)*-0.001*dist
                        if node[1].y > other_node[1].y:
                            force_y = (int(self.height/2) - (node[1].y - other_node[1].y))*-0.001
                            force_ypoz = (node[1].y - other_node[1].y)*-0.001*dist
                        else:
                            force_y = (int(self.height/2) - (other_node[1].y - node[1].y))*0.001
                            force_ypoz = (node[1].y - other_node[1].y)*-0.001*dist
                        if self.find_link_by_sourceNdestination(node[1], other_node[1]) != None or self.find_link_by_sourceNdestination(other_node[1], node[1]) != None:
                            #vonzas
                            if force_xpoz<5:
                                force_xpoz*5
                            if force_ypoz<5:
                                force_ypoz*5
                            dx = node[1].x + int(force_xpoz*25)
                            dy = node[1].y + int(force_ypoz*25)
                            kinect = kinect + force_xpoz + force_ypoz
                        else:
                            #taszitas
                            dx = node[1].x - int(force_x*15)
                            dy = node[1].y - int(force_y*15)
                            kinect = kinect + force_x + force_y
        
                        if dx < node[1].SIZE/2:
                            dx = node[1].SIZE/2+1
                        if dy < node[1].SIZE/2:
                            dy = node[1].SIZE/2+1
                        if dx > self.width-(node[1].SIZE/2):
                            dx = self.width-(node[1].SIZE/2)-1
                        if dy > self.height-(node[1].SIZE/2):
                            dy = self.height-(node[1].SIZE/2)-1
                        link = self.find_link_by_sourceNdestination(node[1], other_node[1])
                        link2 = self.find_link_by_sourceNdestination(other_node[1], node[1])
                        node[1].x = int(dx)
                        node[1].y = int(dy)
                        if link != None:
                            link.motion(dx,dy,False)
                            link.motion(other_node[1].x,other_node[1].y,True)
                        if link2 != None:
                            link2.motion(dx,dy,True)
                            link2.motion(other_node[1].x,other_node[1].y,False)
