"""
Created on May, 30, 2012

"""

import unittest

import monitoringmodel.im
import os.path
import pkgutil

from Service.MonSrvImpl import MonSrvImpl
from eu.novi.monitoring.credential import UsernamePassword
from eu.novi.monitoring.credential import UsernameRSAKey

from rdflib import Graph, Namespace, Literal

class FedericaTest(unittest.TestCase):
	def setUp(self):
		self.monSrv = MonSrvImpl()
                self.monSrv.setTestbed("FEDERICA")
		
	def tearDown(self):
		pass


        def test_monq(self):
            cred = UsernamePassword("monitor1", "m/n.t,r1")
            q = self.monSrv.createQuery()
            q.addFeature("measureMemoryInfo", "MemoryUtilization");
            q.addResource("measureMemoryInfo", "smilax1", "Node");
            q.addFeature("measureCPUInfo", "CPUUtilization");
            q.addResource("measureCPUInfo", "smilax1", "Node");
            q.addFeature("measureDiskInfo", "DiskUtilization");
            q.addResource("measureDiskInfo", "smilax1", "Node");
            q.addInterface("smilax1", "ifin", "hasInboundInterface");
            q.addInterface("smilax1", "ifout", "hasOutboundInterface");
            q.defineInterface("ifin","194.132.52.166", "hasIPv4Address");
            q.defineInterface("ifout","194.132.52.166", "hasIPv4Address");
            query = q.serialize()
            print query
            response = self.monSrv.substrate(cred, query)
            print response
            assert len(response.splitlines())> 20 #, "got empty measurement response")

        def test_monq_linkutil(self):
            cred = UsernamePassword("monitor1", "m/n.t,r1")
            q = self.monSrv.createQuery()
            q.addFeature("measureLinkInfo", "LinkUtilization");
            q.addResource("measureLinkInfo", "link1", "Link");
            q.addInterface("link1", "ifin", "hasSource");
            q.addInterface("link1", "ifout", "hasSink");
            q.defineInterface("ifin","194.132.52.2", "hasIPv4Address");
            q.defineInterface("ifout","194.132.52.3", "hasIPv4Address");
            query = q.serialize()
            print query
            response = self.monSrv.substrate(cred, query)
            print response
            assert len(response.splitlines())> 20 #, "got empty measurement response")


if __name__ == "__main__":
	unittest.main()


