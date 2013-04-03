from time import time

class d(object):
	def __init__(self, n = None):
		self.n = n
		self.c = 0
		self.t = time()
	def __str__(self):
		t = time()
		self.c += 1
		r = "%s %d %f" % (self.n, self.c, t-self.t)
		self.t = t
		return r
