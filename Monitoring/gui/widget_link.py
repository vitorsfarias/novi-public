'''
Created on May 18, 2012

@author: steger
'''

import cairo
from math import atan2, sqrt, sin, cos, pi
from widget_core import w_core

class w_link(w_core):
    typeid = 2
    TOLERANCE = 3
    ARROWSIZE = 20
    ARROWANGLE = .18
    ARROWTIP = 10
    
    def __init__(self, fromx, fromy, tox, toy, resource = None, label = None):
        w_core.__init__(self, resource = resource, label = label)
        self.fromx = fromx
        self.fromy = fromy
        self.dx = tox - fromx
        self.dy = toy - fromy
    
    def myangle(self):
        return atan2(self.dy, self.dx)
    
    def __len__(self):
        return sqrt(self.dx ** 2 + self.dy ** 2 )

    def draw(self, da, ctx):
        ctx.set_line_width(3)
        ctx.set_source_rgb(.1, .1, .1)
        ctx.set_dash([], 0)
        ctx.set_tolerance(0.1)
        ctx.set_line_join(cairo.LINE_JOIN_ROUND)
        ctx.save()
        ctx.translate(self.fromx, self.fromy)
        ctx.new_path()
        ctx.move_to(0, 0)
        ctx.rel_line_to(self.dx, self.dy)
        alpha = self.myangle()
        self.drawarrowhead(ctx, alpha)
        ctx.stroke()
        ctx.restore()
    
    def drawarrowhead(self, ctx, alpha):
        alpha += pi
        atipdx = self.ARROWTIP * cos(alpha)
        atipdy = self.ARROWTIP * sin(alpha)
        ctx.rel_move_to(atipdx, atipdy)
        gamma = alpha - self.ARROWANGLE
        dx = self.ARROWSIZE * cos(gamma)
        dy = self.ARROWSIZE * sin(gamma)
        ctx.rel_line_to(dx, dy)
        ctx.rel_move_to(-dx, -dy)
        gamma = alpha + self.ARROWANGLE
        dx = self.ARROWSIZE * cos(gamma)
        dy = self.ARROWSIZE * sin(gamma)
        ctx.rel_line_to(dx, dy)
    
    def on_button_press(self):
        print "I'm", self.label
    
    def motion(self, x, y, destinationmoved = True):
        if destinationmoved:
            self.dx = x - self.fromx
            self.dy = y - self.fromy
        else:
            tox = self.fromx + self.dx
            toy = self.fromy + self.dy
            self.fromx = x
            self.fromy = y
            self.dx = tox - x
            self.dy = toy - y
    
    def is_over(self, x, y):
#        horizontal = (x >= self.x - self.TOLERANCE) and (x <= self.x + self.SIZE + self.TOLERANCE)
#        vertical = (y >= self.y - self.TOLERANCE) and (y <= self.y + self.SIZE + self.TOLERANCE)
#        return horizontal and vertical
        return False
