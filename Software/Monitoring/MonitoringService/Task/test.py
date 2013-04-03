'''
Created on Sep 1, 2011

@author: steger
'''
import unittest
from Task import SubtaskManager, STRAT_FUNCTIONAL, STRAT_ONDEMAND
from Example.Tools import sshping, sshtraceroute, sonomashortping, \
    sshmeminfo, sonomashortchirp, sshdf, sshhades
from Example.credentials import noviCredential, sonomaCredential,\
    novisaCredential, novihadesCredential
from Example.Metrics import RoundTripDelay, HopMeasurement, FreeMemory,\
    OnewayDelay, DiskUsage
from Example.Units import UM, unitless
from Resource.node import node
from Resource.path import path
from Example.Resources import PLdict, PLpaths
from time import sleep, time
from random import shuffle
from logging import FileHandler, NullHandler
import logging


class Test(unittest.TestCase):
    cred_novi = noviCredential
    cred_siteadmin = novisaCredential
    cred_sonoma = sonomaCredential
    cred_hades = novihadesCredential

    def setUp(self):
        pass

    def tearDown(self):
        pass
    
    @staticmethod
    def map_resource_to_parameter(tool, metric, iface = "eth0"):
        pl = tool.parameters.copy()
        if issubclass(metric.resourcetype, node):
            resource = PLdict.values()[0]
            v, u = resource.get_ipaddress("eth0")
            pl.update("SourceAddress", v, u)
        elif issubclass(metric.resourcetype, path):
            resource = PLpaths[0]
            v, u = resource.source.get_ipaddress("eth0")
            pl.update("SourceAddress", v, u)
            v, u = resource.destination.get_ipaddress(iface)
            pl.update("DestinationAddress", v, u)
        else:
            raise Exception("Unknown resource type")
        return pl

    def sshPingFg(self, tool, iface):
        tool.parameters.update('Interface', iface, unitless)
        pl = self.map_resource_to_parameter(tool, RoundTripDelay, iface)
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result")
        task.destroy()

    def test_sshPingFgNOVI(self):
        tool = sshping
        self.sshPingFg(tool, 'novi')

    def test_sshPingFgSubstrate(self):
        tool = sshping
        self.sshPingFg(tool, 'eth0')

    def test_sshTracerouteFg(self):
        tool = sshtraceroute
        pl = self.map_resource_to_parameter( tool, HopMeasurement )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_sshMeminfo(self):
        tool = sshmeminfo
        pl = self.map_resource_to_parameter( tool, FreeMemory )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result")
        task.destroy()

    def test_sshDiskinfo(self):
        tool = sshdf
        pl = self.map_resource_to_parameter( tool, DiskUsage )
        pl.update_by_list( DiskUsage.p_obligatory )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_siteadmin)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_sshMeminfoUserDefinedSampling(self):
        def sample5():
            for s in [.1, 1, 2, .5, -10]:
                yield s
        tool = sshmeminfo
        pl = self.map_resource_to_parameter( tool, FreeMemory )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi, samplingfunction = sample5)
        task.strategy = STRAT_FUNCTIONAL
        task.enable()
        while task.state == task.STATE_RUNNING:
            sleep(1)
        task.dataAdded.wait( 1 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_SONoMAShortPing(self):
        #FIXME:
        return
        tool = sonomashortping
        pl = self.map_resource_to_parameter( tool, RoundTripDelay )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_sonoma, **tool.kwargs)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_SONoMAShortChirp(self):
        #FIXME:
        return
        tool = sonomashortchirp
        pl = self.map_resource_to_parameter( tool, OnewayDelay )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_sonoma, **tool.kwargs)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_HADES(self):
        #FIXME:
        return
        tool = sshhades
        pl = self.map_resource_to_parameter( tool, RoundTripDelay )
        pl.update('SourceAddress', '192.168.31.1', self.um.ipv4dotted)
        pl.update('DestinationAddress', '192.168.31.5', self.um.ipv4dotted)
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_hades, **tool.kwargs)
        task.enable()
        task.dataAdded.wait( 15 )
        self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
        task.destroy()

    def test_toggle(self):
        tool = sshmeminfo
        pl = self.map_resource_to_parameter( tool, FreeMemory )
        _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
        n = 10
        to = 1.5
        t = time()
        while n:
            task.enable()
            task.dataAdded.wait( to )
            task.disable()
            task.dataAdded.clear()
            n -= 1
        dt = time()-t
        self.assertGreater(len(task.data), 0, "measurement yielded empty result")
        self.assertGreater(dt, n*to, "measurement lasted longer than expected %f > %f" % (dt, n*to))
        task.destroy()

    def test_tm(self):
        tool = sshmeminfo
        pl = self.map_resource_to_parameter( tool, FreeMemory )
        N = len(TM)
        # deletion by task.destroy()
        n= 10
        tasks = []
        while n:
            _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
            n -= 1
            task.enable()
            tasks.append(task)
        shuffle(tasks)
        while len(tasks):
            t = tasks.pop()
            t.destroy()
        # deletion by task using TM method
        n= 10
        tasks = []
        while n:
            _, task = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
            n -= 1
            task.enable()
            tasks.append(task)
        shuffle(tasks)
        while len(tasks):
            t = tasks.pop()
            TM.removefromtasklist(t)
        # destroy by taskid, these tasks are never enabled, not launching new threads
        n= 10
        taskids = []
        while n:
            taskid, _ = TM.generate(name = tool.name, driver = tool.driver, dataheader = tool.dataheaderdeclaration, 
                                     hookimplementations = tool.hooks, parameters = pl, credential = self.cred_novi)
            n -= 1
            taskids.append(taskid)
        shuffle(taskids)
        while len(taskids):
            tid = taskids.pop()
            TM.pop(tid)
        self.assertEqual(N, len(TM), "some tasks were not removed from the SubtaskManager")


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.test_toggle']
    l = logging.getLogger("NOVI.DRIVER")
    l.addHandler(NullHandler())
    fn = "/tmp/Task_test.log"
    hdlr = FileHandler(filename = fn, mode = 'w')
    l = logging.getLogger("NOVI.MS.STM")
#    l.setLevel(level = logging.DEBUG)
    l.setLevel(level = logging.INFO)
    l.addHandler(hdlr = hdlr)
    l.info("START TEST")
    try:
        TM = SubtaskManager(UM)
        unittest.main()
    finally:
        del(TM)
        l.info("FINISHED TEST")
        hdlr.close()
