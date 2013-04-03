DOC = docs
SRC = MonitoringService
GUISRC = gui
IM = ../information-model/
TESTS = $(wildcard $(SRC)/*/test.py)
PY_SOURCES = $(filter-out $(TESTS), $(wildcard $(SRC)/*/*py))
GUI_VERSION = 1.0
GUI_PACKAGE = novi-ms-gui
GUI_DEBIAN = packaging/debian
GUI_INSTALL = packaging/Makefile
GUI_RUNNER = packaging/novigui

GUI_DIRNAME = $(GUI_PACKAGE)-$(GUI_VERSION)
GUI_TARBALL = $(GUI_PACKAGE)_$(GUI_VERSION).orig.tar.gz
GUI_INSTALLDIR = /usr/share/novi-ms-gui
GUI_BUILDDIR = /tmp/guibuild

PYTHON = /usr/bin/python2.7
EPYDOC = /usr/bin/epydoc

all:
	make test
	make $(DOC)

$(DOC): $(PY_SOURCES)
	mkdir -p $@ && \
	test -x $(EPYDOC) && $(EPYDOC) -v -o $@ -n MonSrv --graph all $(PY_SOURCES)

distclean:
	make clean
	rm -rf $(DOC)
	rm -rf $(GUI_BUILDDIR)
	
test: $(TESTS)
	for t in $(TESTS) ; do \
		PYTHONPATH=$(SRC) $(PYTHON) $$t ; \
	done

clean:
	find -name \*pyc | xargs rm -f
	rm -rf $(GUIDST)

$(GUI_BUILDDIR):
	mkdir -p $@

$(GUI_BUILDDIR)/$(GUI_TARBALL): $(GUI_BUILDDIR)
	make clean
	cp -a $(GUISRC) $</$(GUI_DIRNAME)
	cd $< && tar zfc $@ $(GUI_DIRNAME)
	cp -a $(GUI_DEBIAN) $</$(GUI_DIRNAME)
	cp $(GUI_INSTALL) $</$(GUI_DIRNAME)
	cp $(GUI_RUNNER) $</$(GUI_DIRNAME)
	cd $</$(GUI_DIRNAME) && debuild -us -uc 

standalonegui:
	make $(GUI_BUILDDIR)/$(GUI_TARBALL)
#	cp -a $(GUISRC) $(SRC) $(IM) $(GUIDST)
