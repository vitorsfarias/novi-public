'''
Created on Aug 10, 2011

@author: steger
'''
import unittest
from rdflib import Graph, Namespace, Literal
from Service.mock_framework import start_servers, stop_servers
import httplib2
from Example.Platforms import federation
from urllib import urlencode
from time import sleep

fw, t = start_servers()
from MonitoringService import ontology

class Test(unittest.TestCase):
    headers = {'Content-type': 'application/x-www-form-urlencoded'}
    proxy = httplib2.Http(cache = "/tmp/ms_client_cache", timeout = 10)
    
    def setUp(self):
        self.MSI_planetlab = fw.getInterface('PlanetLab')
        self.PL_O = self.MSI_planetlab.service.ontology
        NS = self.ns
        self.S = NS('stat')['UnmodifiedExtractOfFeatureSamples']
        self.F = NS('query')['Formatter_JSON']
        sleep (1)

    def tearDown(self):
        pass

    def test_echo(self):
        p1 = "PlanetLab"
        p2 = "FEDERICA"
        
        h = httplib2.Http(cache = "/tmp/ms_client_cache", timeout = 10)
        url = "http://localhost:%d/echo" % federation[p1][0]
        data = urlencode( {'platform': p2} )
        resp, response = h.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service @%s responded with status %s" % (p1, resp.status))
        i, o = response.split("->")
        got = (i.split("@")[-1].strip(), o.split("@")[-1].strip())
        expect = (p1, p2)
        self.assertEquals(expect, got, "Echo reply differs from expected (%s): %s" % (expect, response))

        data = urlencode( {'platform': p1} )
        resp, response = h.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service @%s responded with status %s" % (p1, resp.status))
        i, o = response.split("->")
        got = (i.split("@")[-1].strip(), o.split("@")[-1].strip())
        expect = (p1, p1)
        self.assertEquals(expect, got, "Echo reply differs from expected (%s): %s" % (expect, response))

    @staticmethod
    def q_enc(q):
        return urlencode( {'query': q} )
    
    @property
    def mns(self):
        return Namespace("http://foo.bar/req.owl#")

    @property
    def ns(self):
        return self.PL_O.ns
    
    @property
    def ns_type(self):
        return self.ns('rdf')['type']

    def measure(self, q, expect = 26):
        url = "http://localhost:%d/measure" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.tlen(response, expect)
        return response
    
    def addtask(self, q):
        url = "http://localhost:%d/addTask" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertTrue(response.startswith('PlanetLab:process:'), "wrong process id %s" % response)
        return response

    def fetchtaskdata(self, q, expect = 26):
        url = "http://localhost:%d/fetchTaskData" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.tlen(response, expect)
        return response

    def deltask(self, q):
        url = "http://localhost:%d/removeTask" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertEqual(response, "OK", "Got: %s" % response)

    def addaggregator(self, q):
        url = "http://localhost:%d/addAggregator" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertTrue(response.startswith('PlanetLab:aggregate:'), "wrong process id %s" % response)
        return response

    def fetchaggregate(self, q, expect = 26):
        url = "http://localhost:%d/fetchAggregatorData" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.tlen(response, expect)
        return response

    def delaggregator(self, q):
        url = "http://localhost:%d/removeAggregator" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertEqual(response, "OK", "Got: %s" % response)

    def addcondition(self, q):
        url = "http://localhost:%d/addCondition" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertTrue(response.startswith('PlanetLab:watchdog:'), "wrong process id %s" % response)
        return response

    def delcondition(self, q):
        url = "http://localhost:%d/removeCondition" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertEqual(response, "OK", "Got: %s" % response)

    def checkcondition(self, q):
        url = "http://localhost:%d/checkCondition" % federation['PlanetLab'][0]
        data = self.q_enc(q)
        resp, response = self.proxy.request(uri = url, method = "POST", body = data, headers = self.headers)
        self.assertTrue(resp.status == 200, "Service responded with status %s" % (resp.status))
        self.assertGreater(len(response.splitlines()), 2, "Got: %s" % response)
        return response


    def tlen(self, response, expect = 26):
#        print response
        self.assertTrue(response, "Got nothing due to former errors")
        if expect > 1:
            self.assertGreater(len(response.splitlines()), expect, "got empty measurement response")
        else:
            self.assertGreater(len(response), 2, "got empty measurement response")
        return response



    def new_g(self):
        g = Graph()
        for k, (_, ns) in ontology.iteritems():
            g.bind(k, Namespace(ns))
        mns = self.mns
        g.bind('q', mns)

        return g

    def save(self, fn, q):
        try:
            with open(fn, 'w') as f:
                f.write(q)
            with open("%s.ue" % fn, 'w') as f:
                f.write( self.q_enc(q) )
        except:
            pass

    def test_measure(self):
        doc = "../../information-model/monitoring-model//monitoringQuery_example.owl"
        with open(doc) as fp:
            q = fp.read()
        self.measure(q)
    
    def addnode(self, g, resname = 'smilax1', address = '150.254.160.19'):
        mns = self.mns
        NS = self.ns
        TYPE = self.ns_type
        R = mns[resname]
        I1 = mns['ifin']
        I2 = mns['ifout']
        IPADDR = Literal(address)
        ADDR = mns['%s_address' % resname]
        g.add((R, TYPE, NS('core')['Node']))
        g.add((R, TYPE, NS('core')['Resource']))
        g.add((R, TYPE, NS('owl')['NamedIndividual']))
 
        g.add((R, NS('core')['hasInboundInterface'], I1))
        g.add((R, NS('core')['hasOutboundInterface'], I1))
        g.add((I1, TYPE, NS('core')['Interface']))
        g.add((I2, TYPE, NS('core')['Interface']))
        g.add((I1, NS('core')['hasIPv4Address'], ADDR))
        g.add((I2, NS('core')['hasIPv4Address'], ADDR))
        g.add((ADDR, TYPE, NS('owl')['NamedIndividual']))
        g.add((ADDR, TYPE, NS('unit')['IPAddress']))
        g.add((ADDR, NS('unit')['hasValue'], IPADDR))
        return R

    def addPar(self, g, pname = 'partition', pval = '/', ptype = 'String', pdim = 'NameOfSomething'):
        P = self.mns['par_%s' % pname]
        NS = self.ns
        TYPE = self.ns_type
        g.add((P, TYPE, NS('owl')['NamedIndividual']))
        g.add((P, TYPE, NS('query')['QueryParameter']))
        g.add((P, NS('param')['paramName'], Literal(pname)))
        g.add((P, NS('unit')['hasValue'], Literal(pval)))
        g.add((P, NS('param')['hasType'], NS('param')[ptype]))
        g.add((P, TYPE, NS('unit')[pdim]))
        return P
    
    def bindNode(self, g, Q, R):
        g.add((Q, self.ns('query')['hasResource'], R))

    def bindPar(self, g, Q, P):
        g.add((Q, self.ns('param')['hasParameter'], P))

    def newQ(self, g, what = 'MemoryUtilization'):
        Q = self.mns['measure_%s' % what]
        NS = self.ns
        TYPE = NS('rdf')['type']
        g.add((Q, TYPE, NS('owl')['NamedIndividual']))
        g.add((Q, TYPE, NS('query')['BundleQuery']))
        g.add((Q, NS('feature')['hasFeature'], NS('feature')[what]))

        g.add((Q, NS('stat')['hasSample'], self.S))
        g.add((Q, NS('query')['hasFormatter'], self.F))
        return Q

    def createaggregatorquery(self, pid, what = 'MemoryUtilization'):
        g = self.new_g()
        R = self.addnode(g)
        Q = self.mns['aggr_%s' % what]
        NS = self.ns
        TYPE = NS('rdf')['type']
        g.add((Q, TYPE, NS('owl')['NamedIndividual']))
        g.add((Q, TYPE, NS('query')['SampleManipulationQuery']))
        g.add((Q, NS('feature')['hasFeature'], NS('feature')[what]))

        g.add((Q, NS('query')['hasFormatter'], self.F))
        g.add((Q, NS('query')['hasProcessid'], Literal(pid)))
        g.add((Q, NS('query')['hasResource'], R))

        P = self.mns['par_last5']
        NS = self.ns
        TYPE = self.ns_type
        g.add((P, TYPE, NS('owl')['NamedIndividual']))
        g.add((P, TYPE, NS('query')['SOP_tail']))
        g.add((P, NS('param')['paramName'], Literal('tail')))
        g.add((P, NS('unit')['hasValue'], Literal('5')))
        g.add((P, NS('param')['hasType'], NS('param')['Integer']))
        g.add((P, TYPE, NS('unit')['Countable']))
        
        L5 = self.mns['last5']
        g.add((L5, TYPE, NS('owl')['NamedIndividual']))
        g.add((L5, TYPE, NS('stat')['Tail']))
        g.add((L5, NS('stat')['hasSample'], self.S))
        g.add((L5, NS('param')['hasParameter'], P))
        ML5 = self.mns['maxlast5']
        g.add((ML5, TYPE, NS('owl')['NamedIndividual']))
        g.add((ML5, TYPE, NS('stat')['Maximum']))
        g.add((ML5, NS('stat')['hasSample'], L5))
        g.add((Q, NS('stat')['hasSample'], ML5))

        return g.serialize()


    def createconditionquery(self, pid, what = 'MemoryUtilization'):
        g = self.new_g()
        R = self.addnode(g)
        NS = self.ns
        TYPE = self.ns_type

        P = self.mns['par_last']
        g.add((P, TYPE, NS('owl')['NamedIndividual']))
        g.add((P, TYPE, NS('query')['SOP_tail']))
        g.add((P, NS('param')['paramName'], Literal('tail')))
        g.add((P, NS('unit')['hasValue'], Literal('1')))
        g.add((P, NS('param')['hasType'], NS('param')['Integer']))
        g.add((P, TYPE, NS('unit')['Countable']))

        L = self.mns['last']
        g.add((L, TYPE, NS('owl')['NamedIndividual']))
        g.add((L, TYPE, NS('stat')['Tail']))
        g.add((L, NS('stat')['hasSample'], self.S))
        g.add((L, NS('param')['hasParameter'], P))

        MIN = self.mns['minall']
        g.add((MIN, TYPE, NS('owl')['NamedIndividual']))
        g.add((MIN, TYPE, NS('stat')['Minimum']))
        g.add((MIN, NS('stat')['hasSample'], self.S))

        ML = self.mns['maxlast1']
        g.add((ML, TYPE, NS('owl')['NamedIndividual']))
        g.add((ML, TYPE, NS('stat')['Maximum']))
        g.add((ML, NS('stat')['hasSample'], L))

        T1 = self.mns['lastitem_%s' % what]
        g.add((T1, TYPE, NS('owl')['NamedIndividual']))
        g.add((T1, TYPE, NS('stat')['SampleTerm']))
#        g.add((T, NS('stat')['hasScale'], Literal(1)))
        g.add((T1, NS('stat')['hasSample'], ML))

        T2 = self.mns['minitem_%s' % what]
        g.add((T2, TYPE, NS('owl')['NamedIndividual']))
        g.add((T2, TYPE, NS('stat')['SampleTerm']))
        g.add((T2, NS('stat')['hasScale'], Literal('-1.5')))
        g.add((T2, NS('stat')['hasSample'], MIN))

        LCS = self.mns['lcs_%s' % what]
        g.add((LCS, TYPE, NS('owl')['NamedIndividual']))
        g.add((LCS, TYPE, NS('stat')['LinearCombinedSample']))
        g.add((LCS, NS('stat')['hasTerm'], T1))
        g.add((LCS, NS('stat')['hasTerm'], T2))

        # condition: "last measurement" > "1.5 * min(all measurements)"
        C = self.mns['cond_%s' % what]
        g.add((C, TYPE, NS('owl')['NamedIndividual']))
        g.add((C, TYPE, NS('stat')['IsPositive']))
        g.add((C, NS('stat')['hasSample'], LCS))
        
        Q = self.mns['condq_%s' % what]
        g.add((Q, TYPE, NS('owl')['NamedIndividual']))
        g.add((Q, TYPE, NS('query')['ConditionQuery']))
        g.add((Q, NS('feature')['hasFeature'], NS('feature')[what]))
        g.add((Q, NS('stat')['hasCondition'], C))
        g.add((Q, NS('query')['hasProcessid'], Literal(pid)))
        g.add((Q, NS('query')['hasResource'], R))

        return g.serialize()


    def test_genq_mem(self):
        g = self.new_g()
        Q = self.newQ(g)
        R = self.addnode(g)
        self.bindNode(g, Q, R)
        query = g.serialize()
        self.save(fn = "/tmp/genq_mem.owl", q = query)
        self.measure(query, 20)

    def test_genq_cpu(self):
        g = self.new_g()
        Q = self.newQ(g, what= 'CPUUtilization')
        R = self.addnode(g)
        self.bindNode(g, Q, R)
        query = g.serialize()
        self.save(fn = "/tmp/genq_cpu.owl", q = query)
        self.measure(query, 16)

    def test_genq_err(self):
        g = self.new_g()
        Q = self.newQ(g, what = 'AlmaFa')
        R = self.addnode(g)
        self.bindNode(g, Q, R)
        query = g.serialize()
        self.save(fn = "/tmp/genq_mem_err.owl", q = query)
        response = self.measure(query, 1)
        self.assertTrue("error" in response, "no error message! got %s" % response)

    def test_genq_complex(self):
        g = self.new_g()
        R = self.addnode(g)
        P = self.addPar(g)
        for feature in ['FreeMemory', 'CPULoad', 'FreeDiskSpace', 'AlmaFa']:
            Q = self.newQ(g, what = feature)
            self.bindNode(g, Q, R)
            if feature == 'FreeDiskSpace':  #FIXME: ugly
                self.bindPar(g, Q, P)
        
        query = g.serialize()
        self.save(fn = "/tmp/genq_complex.owl", q = query)
        response = self.measure(query, 26)
        #print response
        self.assertTrue("error" in response, "no error message! got %s" % response)

    def test_genq_memslice(self):
        g = self.new_g()
        Q = self.newQ(g)
        R = self.addnode(g)
        P = self.addPar(g, pname = 'SliceName', pval = 'novi_novi')
        self.bindNode(g, Q, R)
        self.bindPar(g, Q, P)
        query = g.serialize()
        self.save(fn = "/tmp/genq_memslice.owl", q = query)
        pid = self.addtask(query)
        query = self.createaggregatorquery(pid)
        self.save(fn = "/tmp/genq_memaggr.owl", q = query)
        aid = self.addaggregator(query)
        print "COLLECTING DATA WAIT FOR 10 SECS"
        sleep(10)
        self.fetchaggregate(q = aid, expect = 21)
        print "COLLECTING SOME MORE DATA WAIT FOR 10 SECS"
        sleep(10)
        self.fetchaggregate(q = aid, expect = 21)

        self.delaggregator(q = aid)
        self.deltask(q = pid)





    def test_condition(self):
        g = self.new_g()
        Q = self.newQ(g)
        R = self.addnode(g)
        P = self.addPar(g, pname = 'SliceName', pval = 'novi_novi')
        self.bindNode(g, Q, R)
        self.bindPar(g, Q, P)
        query = g.serialize()
        self.save(fn = "/tmp/genq_memslice_c.owl", q = query)
        pid = self.addtask(query)
        query = self.createconditionquery(pid)
        self.save(fn = "/tmp/genq_cond.owl", q = query)
        cid = self.addcondition(query)

        sleep(3)        
        print self.checkcondition(q = cid)
        
        self.delcondition(q = cid)
        self.deltask(q = pid)







if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.test_genq']
    try:
        unittest.main()
    finally:
        stop_servers(t)
