'''
Created on May 18, 2012

@author: steger
'''

import cairo
from Resource.node import node
from widget_core import w_core

class w_node(w_core):
    typeid = 1
    SIZE = 20
    LABELOFFSET = 8
    FONTSIZE = 14
    TOLERANCE = 3
    def __init__(self, x, y, resource, label = None):
        w_core.__init__(self, resource = resource, label = label)
        if not isinstance(resource, node):
            raise Exception("expecting node, got %s" % resource)
        self.x = x
        self.y = y
        self.selected = False
        self.showlabel = True

    def path(self, ctx):
        ctx.new_path()
        ctx.move_to(-self.SIZE/2, -self.SIZE/2)
        ctx.rel_line_to(self.SIZE, 0)
        ctx.rel_line_to(0, self.SIZE)
        ctx.rel_line_to(-self.SIZE, 0)
        ctx.close_path()

    def drawlabel(self, ctx):
        label = self.label
        ctx.set_source_rgba(1, 0.2, 0.2, 0.6);
        ctx.select_font_face ("Georgia", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
        ctx.set_font_size(self.FONTSIZE)
        width = ctx.text_extents(label)[2]
        d = self.LABELOFFSET + self.SIZE / 2
        ctx.move_to(d, -d)
        ctx.show_text(label)
        ctx.new_path()
        ctx.move_to(self.SIZE/2, -self.SIZE/2)
        ctx.rel_line_to(self.LABELOFFSET - 3, 3 - self.LABELOFFSET)
        ctx.rel_line_to(3 + width, 0)
        ctx.stroke()

    def draw(self, da, ctx):
        ctx.set_dash([], 0)
        ctx.set_line_width(3)
        ctx.set_tolerance(0.1)
        ctx.set_line_join(cairo.LINE_JOIN_ROUND)
        ctx.save()
        ctx.translate(self.x, self.y)
        ctx.set_source_rgb(50, 50, 50)
        self.path(ctx)
        ctx.fill()
        if self.selected:
            ctx.set_source_rgb(200, 0, 0)
        else:
            ctx.set_source_rgb(0, 0, 0)
        self.path(ctx)
        ctx.stroke()
        if self.showlabel:
            self.drawlabel(ctx)
        ctx.restore()
    
    def on_button_press(self):
        print "I'm", self.widgetid
    
    def motion(self, dx, dy):
        self.x += dx
        self.y += dy
    
    def is_over(self, x, y):
        r = self.SIZE / 2 + self.TOLERANCE
        horizontal = abs(x - self.x) <= r
        vertical = abs(y - self.y) <= r
        return horizontal and vertical
