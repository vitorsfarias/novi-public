'''
Created on May 18, 2012

@author: steger
'''

import cairo
from math import sin, cos, pi
from widget_link import w_link

class w_path(w_link):
    BEND_ANGLE = .4
    def draw(self, da, ctx):
        ctx.set_line_width(5)
        ctx.set_dash([4.0, 4.0], 0)
        ctx.set_source_rgb(.1, .1, .1)
        ctx.set_tolerance(0.1)
        ctx.set_line_join(cairo.LINE_JOIN_ROUND)
        ctx.save()
        ctx.translate(self.fromx, self.fromy)
        ctx.new_path()
        ctx.move_to(0, 0)
        alpha = self.myangle() - self.BEND_ANGLE
        l = len(self) / 2
        dx1 = l * cos(alpha) 
        dy1 = l * sin(alpha)
        alpha = pi + self.myangle() + self.BEND_ANGLE
        dx2 = self.dx + l * cos(alpha)
        dy2 = self.dy + l * sin(alpha)
        ctx.rel_curve_to(dx1, dy1, dx2, dy2, self.dx, self.dy)
        alpha = self.myangle() + self.BEND_ANGLE
        self.drawarrowhead(ctx, alpha)
        ctx.stroke()
        ctx.restore()
