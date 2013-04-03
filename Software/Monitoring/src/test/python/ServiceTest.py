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

class ServiceTest(unittest.TestCase):
	def setUp(self):
		self.monSrv = MonSrvImpl()
		
	def tearDown(self):
		pass

	def tet_echo(self):
        	message = "foo"
		_, response = self.monSrv.echo(message)
		assert response==message #, "Echo reply differs from expected: %s" % response)


	def test_Substrate(self):
		cred = UsernameRSAKey("novi_novi", "/home/jenkins/.ssh/sfademo_key", "")
                doc = "monitoringQuery_example.owl"
                im = monitoringmodel.im.im()
                loader = pkgutil.get_loader("monitoringmodel.im")
                q = loader.get_data(os.path.join(im.path,doc))
		s = self.monSrv.substrate(cred, q) 
		print s
		assert len(s.splitlines())>26 #self.monSrv.substrate(cred, self.sampleQuery) == self.sampleResult

        def test_monq(self):
            cred = UsernameRSAKey("novi_novi", "/home/jenkins/.ssh/sfademo_key", "")
            q = self.monSrv.createQuery()
            q.addFeature('measureMemoryInfo', 'FreeMemory')
            q.addResource('measureMemoryInfo', 'smilax1', 'Node')
            q.addInterface('smilax1', 'ifin', 'hasInboundInterface')
            q.addInterface('smilax1', 'ifout', 'hasOutboundInterface')
            q.defineInterface('ifin','150.254.160.19', 'hasIPv4Address')
            q.defineInterface('ifout','150.254.160.19', 'hasIPv4Address')
            query = q.serialize()
            print query
            response = self.monSrv.substrate(cred, query)
            print response
            assert len(response.splitlines())> 20 #, "got empty measurement response")

        def tet_genq(self):
                cred = UsernameRSAKey("novi_novi", "/home/jenkins/.ssh/sfademo_key", "")
                g = Graph()
                for k, ns in self.monSrv.ontology.binddict.iteritems():
                        g.bind(k, ns)
                mns = Namespace("http://foo.bar/req.owl#")
                g.bind('q', mns)
                NS = self.monSrv.ontology.ns
                TYPE = NS('rdf')['type']
                Q = mns['measureMemoryInformation']
                R = mns['smilax1']
                I1 = mns['ifin']
                I2 = mns['ifout']
                ADDR = Literal('150.254.160.19')
                g.add((Q, TYPE, NS('owl')['NamedIndividual']))
                g.add((Q, TYPE, NS('query')['BundleQuery']))
                g.add((Q, NS('query')['hasFeature'], NS('feature')['FreeMemory']))
                g.add((Q, NS('query')['hasResource'], R))
                g.add((R, TYPE, NS('core')['Node']))
                g.add((R, NS('core')['hasInboundInterface'], I1))
                g.add((R, NS('core')['hasOutboundInterface'], I2))
                g.add((I1, TYPE, NS('core')['Interface']))
                g.add((I2, TYPE, NS('core')['Interface']))
                g.add((I1, NS('core')['hasIPv4Address'], ADDR))
                g.add((I2, NS('core')['hasIPv4Address'], ADDR))
                query = g.serialize()
                #print query
                response = self.monSrv.substrate(cred, query)
                #print response
                assert len(response.splitlines())>26
#                self.assertTrue(response, "got nothing")
#                self.assertGreater(len(response.splitlines()), 26, "got empty measurement response")


if __name__ == "__main__":
	unittest.main()


