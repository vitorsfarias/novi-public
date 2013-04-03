prefix = /usr/
bindir = $(prefix)/bin
sharedir = $(prefix)/share
pysrcdir = $(sharedir)/novi-ms-gui
gladedir = $(sharedir)/novi-ms-gui/windows

install:
	mkdir -p $(DESTDIR)$(pysrcdir) $(DESTDIR)$(bindir) $(DESTDIR)$(gladedir)
	cp -r *py  $(DESTDIR)$(pysrcdir)
	cp windows/*glade $(DESTDIR)$(gladedir)
	cp novigui $(DESTDIR)$(bindir)/
	chmod +x $(DESTDIR)$(bindir)/novigui