'''
Created on Nov 20, 2012

@author: steger
'''
from Service.interface import MSInterface
import logging
from logging.handlers import TimedRotatingFileHandler
from os import path, unlink
from flask import Flask, request
from threading import Thread
from Example.Platforms import federation
from select import select
from sys import stdin
import httplib2
from os import urandom

#FIXME: DIRTY!
from Example.credentials import novisaCredentialIARGS
import traceback
import sys

class FrameworkError(Exception):
    pass

class PolicyMock(object):
    def __init__(self):
        fn = "/tmp/ps.log"
        if path.exists(fn):
            unlink(fn)
        hdlr = TimedRotatingFileHandler(filename = fn)
        self.logger = logging.getLogger("NOVI.PS")
        self.logger.setLevel(level = logging.DEBUG)
        self.logger.addHandler(hdlr = hdlr)

    def trigger(self, what):
        self.logger.info(what)

class Framework(object):
    '''
    This class mimics the integration framework. It helps to look up remote Monitoring Service instances
    '''

    def __init__(self):
        '''
        Constructor
        '''
        self._if = {}
        self._pol = PolicyMock()
    
    def add(self, platform, config_owl):
        fn = "/tmp/ms_%s.log" % platform
        if path.exists(fn):
            unlink(fn)
        hdlr = TimedRotatingFileHandler(filename = fn)
        l = logging.getLogger("NOVI.MS.%s" % platform)
        l.setLevel(level = logging.DEBUG)
        l.addHandler(hdlr = hdlr)
        l = logging.getLogger("NOVI.MSI.%s" % platform)
        l.setLevel(level = logging.DEBUG)
        l.addHandler(hdlr = hdlr)
        iface = MSInterface(self, platform, config_owl)
        self._if[platform] = iface
        return iface
    
    def getInterface(self, platform):
        try:
            return self._if[platform]
        except:
            print "EE: %s platform not found" % platform
            raise FrameworkError

    def getPolicyService(self, platform):
        return self._pol
        
    def serviceList(self):
        return self._if.values()

def app_launch(plif, port):
    app = Flask(__name__)
    app.secret_key = urandom(24)
    t = Thread(target = run, args = (app, port))
    t.start()

    # these are here for test and control
    @app.route("/", methods = ['GET'])
    def hello():
        return "Hello world"

    @app.route('/shutdown', methods = ['POST'])
    def shutdown():
        shutdown_server()
        return 'Server shutting down...'


    # these are the real service interfaces
    @app.route("/echo", methods = ['POST'])
    def echo():
        platform = request.form['platform']
        try:
            return plif.echo(platform)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "="*60
            return "Error %s" % e

    @app.route("/measure", methods = ['POST'])
    def measure():
        q = request.form['query']
        try:
            return plif.measure(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "="*60
            return "Error %s" % e

    @app.route("/addTask", methods = ['POST'])
    def addTask():
        try:
            q = request.form['query']
            return plif.addTask(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "-"*60
            return "Error %s" % e
                    
    @app.route("/fetchTaskData", methods = ['POST'])
    def fetchTaskData():
        q = request.form['query']
        return plif.fetchTaskData(credential = [novisaCredentialIARGS], query = q)

    @app.route("/removeTask", methods = ['POST'])
    def removeTask():
        try:
            q = request.form['query']
            plif.removeTask(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "Error %s" % e
        return "OK"    
            
    @app.route("/describeTaskData", methods = ['POST'])
    def describeTaskData():
        q = request.form['query']
        return plif.describeTaskData(credential = [novisaCredentialIARGS], query = q)

    @app.route("/getTaskStatus", methods = ['POST'])
    def getTaskStatus():
        try:
            q = request.form['query']
            return str( plif.getTaskStatus(credential = [novisaCredentialIARGS], query = q) )
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "-"*60
            return "Error %s" % e

    @app.route("/enableTask", methods = ['POST'])
    def enableTask():
        q = request.form['query']
        plif.enableTask(credential = [novisaCredentialIARGS], query = q)
        return "OK"

    @app.route("/disableTask", methods = ['POST'])
    def disableTask():
        q = request.form['query']
        plif.disableTask(credential = [novisaCredentialIARGS], query = q)
        return "OK"

    @app.route("/addAggregator", methods = ['POST'])
    def addAggregator():
        try:
            q = request.form['query']
            return plif.addAggregator(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "-"*60
            return "Error %s" % e
                    
    @app.route("/fetchAggregatorData", methods = ['POST'])
    def fetchAggregatorData():
        q = request.form['query']
        return plif.fetchAggregatorData(credential = [novisaCredentialIARGS], query = q)

    @app.route("/removeAggregator", methods = ['POST'])
    def removeAggregator():
        try:
            q = request.form['query']
            plif.removeAggregator(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "Error %s" % e
        return "OK"    

    @app.route("/addCondition", methods = ['POST'])
    def addCondition():
        try:
            q = request.form['query']
            return plif.addWatchdog(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "-"*60
            return "Error %s" % e

    @app.route("/removeCondition", methods = ['POST'])
    def removeCondition():
        try:
            q = request.form['query']
            plif.removeCondition(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "Error %s" % e
        return "OK"    

    @app.route("/checkCondition", methods = ['POST'])
    def checkCondition():
        try:
            q = request.form['query']
            return plif.checkCondition(credential = [novisaCredentialIARGS], query = q)
        except Exception, e:
            print "-"*60
            traceback.print_exc(file=sys.stdout)
            print "-"*60
            return "Error %s" % e
    


    return t


def run(app, port):
    app.run(debug = False, port = port)

def shutdown_server():
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()

def emit(port):
    try:
        h = httplib2.Http(timeout = 10)
        url = "http://localhost:%d/shutdown" % port
        resp, _ = h.request(uri = url, method = "POST")
        if resp.status != 200:
            print "Service responded with status %s" % resp.status
    except Exception, e:
        print "Error contacting server @ localhost:%d: (%s)" % (port, e)

def start_servers():
    fw = Framework()
    t = []
    for platform, (port, config_owl) in federation.iteritems():
        plif = fw.add(platform, config_owl)
        t.append( app_launch(plif, port) )
    return fw, t

def stop_servers(t):
    # POST the shutdown methods
    for port, _ in federation.values():
        emit(port)
        
    # join threads
    while len(t):
        st = t.pop()
        st.join()
        
if __name__ == "__main__":
    print "INIT"
    # start services as separate threads
    _, t = start_servers()
    
    # wait for a keyboard interrupt
    print "PRESS ^C to stop"
    while True:
        try:
            select([stdin],[],[])
        except KeyboardInterrupt:
            break
    
    stop_servers(t)
    print "OK"