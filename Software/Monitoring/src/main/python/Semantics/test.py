'''
Created on Aug 10, 2011

@author: steger
'''
import site
site.addsitedir('../site-packages')


import unittest2
from rdflib import Graph
from Example.Metrics import RoundTripDelay
from Example.Tools import sonomashortping
from DataProcessing.Parameter import ParameterList, Parameter
from Example.credentials import ple_credentials
from Example.Platforms import FRAMEWORK
from DataProcessing.DataHeaderCell import CellRequestByName
from DataProcessing.DataError import SamplerError
from DataProcessing.DataReader import DataReader
import pkgutil
import StringIO
import monitoringmodel.im
import os.path


class Test(unittest2.TestCase):
    
    def setUp(self):
        self.MS_planetlab = FRAMEWORK.getService('PlanetLab') 
        self.MS_federica = FRAMEWORK.getService('FEDERICA')

        dim_ipaddress = self.MS_planetlab.dm['IPAddress']
        node = ("157.181.175.243", self.MS_planetlab.um.ipv4dotted)
        self.p_src_eltenode = Parameter(name = "SourceAddress", valuetype = str, unitmanager = self.MS_planetlab.um, dimension = dim_ipaddress, default = node)
        node = ("147.102.22.66", self.MS_planetlab.um.ipv4dotted)
        self.p_dst_ntuanode = Parameter(name = "DestinationAddress", valuetype = str, unitmanager = self.MS_planetlab.um, dimension = dim_ipaddress, default = node)
        node = ("192.168.31.1", self.MS_planetlab.um.ipv4dotted)
        self.p_src_fednode = Parameter(name = "SourceAddress", valuetype = str, unitmanager = self.MS_planetlab.um, dimension = dim_ipaddress, default = node)
        node = ("192.168.31.9", self.MS_planetlab.um.ipv4dotted)
        self.p_dst_fednode = Parameter(name = "DestinationAddress", valuetype = str, unitmanager = self.MS_planetlab.um, dimension = dim_ipaddress, default = node)
        
        self.substrate = self.MS_planetlab.ontology.ns('task')['Substrate']
        self.slice = self.MS_planetlab.ontology.ns('task')['Slice']
        
        self.feat_task = {
              'OnewayDelay': (['SONoMAChirp'], []),
              'RoundtripDelay': (['SONoMAPing'], ['sshpingSlice']), 
              'AvailableBandwidth': ([], ['sshabSlice']),
              'AvailableMemory': (['sshMeminfo'], ['sshMeminfoSlice']),
              'FreeMemory': (['sshMeminfo'], ['sshMeminfoSlice']),
              'MemoryUtilization': (['sshMeminfo'], ['sshMeminfoSlice']),
              'Uptime': (['sshuptime'], ['sshuptimeSlice']),
              'CPULoad': (['sshcpuload'], ['sshcpuloadSlice']),
              'CPUCores': (['sshcpuload'], ['sshcpuloadSlice']),
              'CPUUtilization': (['sshcpuload'], ['sshcpuloadSlice']),
              'FreeDiskSpace': (['sshdiskinfo'], ['sshdiskinfoSlice']),
              'UsedDiskSpace': (['sshdiskinfo'], ['sshdiskinfoSlice'])
        }
        
        dim_nameofsomething = self.MS_planetlab.dm['NameOfSomething']
        self.slicename = Parameter(name = "SliceName", valuetype = str, 
                                   unitmanager = self.MS_planetlab.um, dimension = dim_nameofsomething, 
                                   default = ('novi_novi', self.MS_planetlab.um.unitless))
        dim_countable = self.MS_planetlab.dm['Countable']
        self.count = Parameter(name = 'Count', valuetype = int, 
                               unitmanager = self.MS_planetlab.um, dimension = dim_countable, 
                               default = (5, self.MS_planetlab.um.piece))
        
    def test_managers(self):
        expect = 14
        infer = len(self.MS_planetlab.pm)
        self.assertEqual(infer, expect, "Prefix: got %d expect %d" % (infer, expect))

        expect = 10
        infer = [ s for _, s in self.MS_planetlab.unitmodel.inferBaseUnits() ]
        self.assertEqual(expect, len(infer), "BaseUnit: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 1
        infer = [ d for _, d, _ in self.MS_planetlab.unitmodel.inferProductUnits() ]
        self.assertEqual(expect, len(infer), "ProductUnit: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 1
        infer = [ d for _, d, _, _ in self.MS_planetlab.unitmodel.inferPowerUnits() ]
        self.assertEqual(expect, len(infer), "PowerUnit: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 12
        infer = [ d for _, d, _, _, _ in self.MS_planetlab.unitmodel.inferLinearTransformedUnits() ]
        self.assertEqual(expect, len(infer), "LinearTransformedUnit: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 2
        infer = [ d for _, d, _, _, _ in self.MS_planetlab.unitmodel.inferRegexpTransformedUnits() ]
        self.assertEqual(expect, len(infer), "RegexpTransformedUnit: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 8
        infer = [ d for d, _, _ in self.MS_planetlab.unitmodel.inferBaseDimensions() ]
        self.assertEqual(expect, len(infer), "BaseDimension: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 1
        infer = [ d for d, _, _ in self.MS_planetlab.unitmodel.inferDifferenceDimensions() ]
        self.assertEqual(expect, len(infer), "DifferenceDimension: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 1
        infer = [ d for d, _, _, _ in self.MS_planetlab.unitmodel.inferPowerDimensions() ]
        self.assertEqual(expect, len(infer), "PowerDimension: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 1
        infer = [ d for d, _, _ in self.MS_planetlab.unitmodel.inferProductDimensions() ]
        self.assertEqual(expect, len(infer), "ProductDimension: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))

        expect = 4
        infer = [ d for d, _, _ in self.MS_planetlab.unitmodel.inferRatioDimensions() ]
        self.assertEqual(expect, len(infer), "RatioDimension: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))


        NS = self.MS_planetlab.ontology.ns('unit')
        for expect, uri in [(4, NS['second']), (7, NS['Byte']), (3, NS['bit']), (1, NS['unixtimestamp'])]:
            infer = [s for s in self.MS_planetlab.unitmodel.inferPossiblePrefixesOf(uri)]
            self.assertEqual(expect, len(infer), "inferPossiblePrefixesOf: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))



    def test_IM_domainsfeatures(self):
        expect = set(['Slice', 'Substrate'])
        infer = set([ self.MS_planetlab.ontology._tail(x) for x in self.MS_planetlab.taskmodel.inferDomains() ])
        self.assertEqual(expect, infer, "inferDomains: expect %d, got %d\n%s" % (len(expect), len(infer), str(infer)))
        
        expect = len(self.feat_task) #19 feature van, de nehanynak nincs neve
        infer = [ x for x in self.MS_planetlab.taskmodel.inferFeatures()]
        self.assertEqual(expect, len(infer), "inferFeatures: expect %d, got %d\n%s" % (expect, len(infer), str(infer)))
        
    def test_IM_task(self):
        for feat, (t_subst, t_slice) in self.feat_task.iteritems():
            feature = self.MS_planetlab.ontology.ns('feature')[feat]
            infer_t_subst = [ name for _, name in self.MS_planetlab.taskmodel.inferTasks(self.substrate, feature)]
            infer_t_slice = [ name for _, name in self.MS_planetlab.taskmodel.inferTasks(self.slice, feature)]
            self.assertEqual(infer_t_subst, t_subst, "feature: %s searchtask (substrate): expect %s, got %s" % (feat, t_subst, infer_t_subst))
            self.assertEqual(infer_t_slice, t_slice, "feature: %s searchtask (slice): expect %s, got %s" % (feat, t_slice, infer_t_slice))

        task = self.MS_planetlab.ontology.ns('conf')['T_SONoMAPing']
        infer = self.MS_planetlab.taskmodel.inferCredentialOf(task)
        expect = set(sonomashortping.authtype)
        self.assertEqual(infer, expect, "credentials differ expect: %s got: %s" % (expect, infer))

        infer = self.MS_planetlab.taskmodel.inferDriverOf(task)
        expect = sonomashortping.driver
        self.assertEqual(infer, expect, "drivers differ expect: %s got: %s" % (expect, infer))

        infer = self.MS_planetlab.taskmodel.inferHookparametersOf(task)
        expect = sonomashortping.kwargs
        self.assertEqual(infer, expect, "hook parameters differ expect: %s got: %s" % (expect, infer))

        H = self.MS_planetlab.taskmodel.inferHookdefinitionsOf(task)
        for k, h in H.iteritems():
            exp = sonomashortping.hooks[k].strip()
            h = h.strip()
            self.assertEqual(h, exp, "%s hook differs\nexpect:\n%s\ngot:\n%s" % (k, exp, h))

        #TODO: check feature equality
        infer = [ (c.name, str(c._unit), str(c._dimension)) for c in self.MS_planetlab.taskmodel.inferDataheaderOf(task) ]
        expect = [ (c.name, str(c._unit), str(c._dimension)) for c in sonomashortping.dataheaderdeclaration ]
        self.assertEqual(infer, expect, "output header declarations differ expect:\n%s\ngot:\n%s" % (expect, infer))

        infer = self.MS_planetlab.taskmodel.inferParametersOf(task)
        expect = sonomashortping.parameters
        n_inf, n_exp = set(infer.parameter_names()), set(expect.parameter_names())
        self.assertEqual(n_inf, n_exp, "runtime parameters differ expect: %s got: %s" %(n_exp, n_inf))
        for k, p in expect.parameter.iteritems():
            inf_v = infer.parameter[k].value
            exp_v = p.value
            if exp_v is None:
                self.assertFalse(inf_v, "Expected uninitialized value, got %s" % inf_v)
            else:
                inf_v = (inf_v[0], str(inf_v[1]))
                exp_v = (exp_v[0], str(exp_v[1]))
                self.assertEqual(inf_v, exp_v, "Parameter value differ %s expect:\n%s\ngot:\n%s" % (k, exp_v, inf_v))

        feature = self.MS_planetlab.ontology.ns('feature')['RoundtripDelay']
        expect = RoundTripDelay.p_obligatory
        infer = self.MS_planetlab.taskmodel.inferObligatoryParametersOf(feature)
        self.assertEqual(len(expect), len(infer), "obligatory parameters for %s differ expect: %s got: %s" % (feature, expect.parameter_names(), infer.parameter_names()))
        for k, p in expect.parameter.iteritems():
            inf_v = infer.parameter[k].value
            exp_v = p.value
            inf_v = (inf_v[0], str(inf_v[1]))
            exp_v = (exp_v[0], str(exp_v[1]))
            self.assertEqual(inf_v, exp_v, "Parameter value differ %s expect:\n%s\ngot:\n%s" % (k, exp_v, inf_v))

    def test_taskBYuri(self):
        cases = {
             'T_SSHPingSlice': [self.p_src_eltenode, self.slicename, self.count, self.p_dst_ntuanode], 
             'T_SSHMemInfo': [self.p_src_eltenode], 
             'T_SSHMemInfoSlice': [self.p_src_eltenode, self.slicename], 
             'T_SSHCPULoad': [self.p_src_eltenode], 
             'T_SSHCPULoadSlice': [self.p_src_eltenode, self.slicename], 
             'T_SSHUptime': [self.p_src_eltenode], 
             'T_SSHUptimeSlice': [self.p_src_eltenode, self.slicename],
#             'T_SONoMAPing': [self.p_src_eltenode, self.p_dst_ntuanode],
#             'T_hadesaggregate': [self.p_src_fednode, self.p_dst_fednode],
        }
        for l,p in cases.iteritems():
            task_uri = self.MS_planetlab.ontology.ns('conf')[l]
            _, task = self.MS_planetlab.newTask(task = task_uri, 
                                 cred = ple_credentials, 
                                 resource = None, 
                                 parameters = ParameterList(p))
            task.enable()
            task.dataAdded.wait( 15 )
            self.assertGreater(len(task.data), 0, "measurement %s yielded empty result" % l) 
#            print task.data._rawrecords


    def test_owlexamples(self):
        doc = "../monitoringmodel/monitoringQuery_example.owl" #% self.MS_planetlab.ontology.baseurl
        # JYTHON hack for accessing owl files
        im = monitoringmodel.im.im()
        path = im.path
        loader = pkgutil.get_loader("monitoringmodel.im")
        g = Graph()
        g.parse(source = StringIO.StringIO(loader.get_data(os.path.join(path, "monitoringQuery_example.owl"))))
        print str(g)
        qdict = self.MS_planetlab.QI.inferBundleQueries(qgraph = g)
        self.assertTrue(len(qdict), "Got empty query")
        for q in qdict:
            domain = self.MS_planetlab.ontology.ns('task')['Substrate']
            taskgen = self.MS_planetlab.taskmodel.inferTasks(domain, q.feature)
            #we are ugly here: use the first tool
            for task_uri, _ in taskgen: break
            _, task = self.MS_planetlab.newTask(task = task_uri, 
                                 cred = ple_credentials, 
                                 resource = q.resource,
                                 parameters = q.paramlist)
            del task.strategy   # make sure STRAT_ONDEMAND
            task.enable()
            task.dataAdded.wait( 15 )
            task.dataAdded.clear()
            if q.samplechain:
                flow = []
                for skeleton, parlist in q.samplechain:
                    flow.append((skeleton, parlist.formkeyvaldict()))
                aid = self.MS_planetlab.am.newAggregator(task.data, CellRequestByName(name = 'Free Memory'), flow)
                A = self.MS_planetlab.am[ aid ]
                while True:
                    try:
                        s, a = A.data._rawrecords[0]
                        self.assertEqual(s, len(task.data), "inconsistency in length len(data)=%d, max of %d samples?" % (len(task.data), s))
                        R = DataReader(datasource = task.data)
                        R.extract(cellrequest = [CellRequestByName(name = 'Free Memory')])
                        expect = max( [ float(x) for x, in R ] )
                        self.assertEqual(expect, a, "inconsistency in aggregare %f <> %f" % (expect, a))
                        break
                    except SamplerError:
                        print "MEASURE SOME MORE ..."
                        task.disable()
                        task.enable()
                        task.dataAdded.wait( 15 )
                        task.dataAdded.clear()
            self.assertGreater(len(task.data), 0, "measurement yielded empty result") 
            
            

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.test_IM_domainsfeatures']
    unittest2.main()
