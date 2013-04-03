'''
Created on Sep 2, 2011

@author: steger
'''
import unittest
from DataProcessing.Parameter import Parameter
from random import randint
from Example.Prefixes import prefixes, PM
from Example.Dimensions import DM, timeinterval, countable, ipaddress, cardinal
from Example.Units import UM, milli_second, pico_second, dozen, micro_second,\
    piece, nano_second, second
from DataProcessing.Data import Data
from DataProcessing.Aggregator import Sum, Max, Min, Mean, Deviation, Percentile
from DataProcessing.DataReader import DataReader
from DataProcessing.DataFormatter import JsonFormatter, DumbFormatter
from DataProcessing.Sampler import Head, Tail, Sorter
from DataProcessing.AggregatorManager import AggregatorManager
from DataProcessing.DataHeader import DataHeaderGeneratedByDescription,\
    DataHeader
from DataProcessing.DataHeaderCell import DataHeaderCell, CellRequestByName,\
    CellRequestByFeature
from DataProcessing.DataError import DataError, SamplerError
from DataProcessing.DataSample import DataSample


class Test(unittest.TestCase):
    eps = 1e-15
    
    def different(self, expect, got):
        return abs(expect - got) / float(expect) < self.eps
    
    def setUp(self):
        pass
    
    def test_PM(self):
        for ref, symbol, base, exponent in prefixes:
            scale = base ** exponent
            p = PM[ref]
            self.assertEqual(str(p), symbol, "symbol cannot be read back %s %s" % (p, symbol))
            self.assertEqual(p.scale, scale, "prefix %s scale error got: %f expect: %f" % (p, p.scale, scale))
    
        self.assertTrue('p' in PM, "cannot find symbol")
        self.assertFalse('pico' in PM, "found a symbol, which I shouldn't")
    
    
    def test_UM(self):
        s = randint(1, 10000)
        expect = s * 1e-3
        got = UM.convert(s, milli_second, second)
        self.assertTrue(self.different(expect, got), "Different (%d ms) expect %f s got %f s" % (s, expect, got))
        expect = s * 1e9
        got = UM.convert(s, milli_second, pico_second)
        self.assertTrue(self.different(expect, got), "Different (%d ms) expect %f ps got %f ps" % (s, expect, got))

        kilobit = UM["kilo_bit"]
        megaByte = UM["mega_Byte"]
        b = randint(1, 1000)
        expect = b * 1e-3 / 8.
        got = UM.convert(b, kilobit, megaByte)
        self.assertTrue(self.different(expect, got), "Different (%d kbit) expect %f MB got %f MB" % (b, expect, got))
        
    def test_D(self):
        dim = DM['TimeInterval']
        for u in [second, milli_second]:
            self.assertTrue(dim.containsUnit(u), "piece %s not in dim" % u)
        bu = UM.getBasinByUnit(UM['second'])
        br = UM.getBasinByReference('micro_second')
        self.assertTrue(bu == br, "basins differ")

    def test_parameter(self):
        n = randint(0, 1000)
        parameter = Parameter(name = 'testparameter', valuetype = float, unitmanager = UM, dimension = countable, default = (n, dozen)) 
        v1 = 12 * parameter.value[0] 
        v2 = parameter.convert(piece)
        self.assertTrue(abs(v1 - v2) < self.eps, "%d dozen and %d are not equal (type 1)" % (n, v2))
        n = randint(0, 1000)
        parameter.value = (n, piece)
        v = parameter.convert(dozen)
        self.assertTrue(abs(12 * v - n) < self.eps, "%f dozen and %d are not equal (type 2)" % (v, n))

    def test_addcolumn(self):
        '''
        '''
        c1 = DataHeaderCell(name = "oszlop", dimension = timeinterval, unit = milli_second)
        c2 = DataHeaderCell(name = "oszlop2", dimension = timeinterval, unit = second, feature = "kutyafule")
        h = DataHeader(name = "proba")
        h.addColumn(c1)
        h.addColumn(c2)
        self.assertRaises(DataError, h.addColumn, c1)
        cr1 = CellRequestByName(name = "oszlop2")
        cr2 = CellRequestByFeature(feature = "kutyafule")
        qr1 = [ x for x in h.getCell(cellrequest = cr1) ]
        qr2 = [ x for x in h.getCell(cellrequest = cr2) ]
        self.assertEqual(qr1, qr2, "getCell oopses 1")
        qr = [ x for x in h.getCell(cellrequest = CellRequestByFeature(feature = "macskanyelv")) ]
        self.assertEqual(len(qr), 0, "getCell oopses 2")
        
        
    def test_createheadertemplate(self):
        header = DataHeader(name = "traceroute")
        cell = DataHeaderCell(name = "idx", dimension = cardinal)
        header.addColumn(cell)
        iphdr = DataHeader(name = "info")
        cell = DataHeaderCell(name = "address", dimension = ipaddress)
        iphdr.addColumn(cell)
        rtthdr = DataHeader(name = "rttinfo")
        cell = DataHeaderCell(name = "roundtripdelay", dimension = timeinterval, unit = milli_second)
        rtthdr.addColumn(cell)
        iphdr.addColumn(rtthdr)
        header.addColumn(iphdr)
        header2 = DataHeaderGeneratedByDescription("traceroute", [('idx', cardinal), ("info", [('address', ipaddress), ("rttinfo", [('roundtripdelay', timeinterval, milli_second)])])])
        self.assertTrue(header == header2, "headers differ:\n%s\n%s" % (header, header2))

    def test_complex_table(self):
        '''
        '''
        header = DataHeaderGeneratedByDescription("traceroute", [('idx', cardinal), ("info", [('address', ipaddress), ("rttinfo", [('roundtripdelay', timeinterval, milli_second)])])])
        
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = 2)
        iprec1, iprec2 = hoprecord.getRecordTemplates(name = "info")
        (rttrec1,) = iprec1.getRecordTemplates(name = "rttinfo", sizes = [3,])
        (rttrec2,) = iprec2.getRecordTemplates(name = "rttinfo", sizes = [3,])
        
        rttrec1.update(name = 'roundtripdelay', values = [2.925,  3.279,  3.758], unit = milli_second)
        iprec1.update(name = 'address', values = ['192.168.1.1'])
        
        rttrec2.update(name = 'roundtripdelay', values = [.008634, .008857, .009054], unit = second)
        iprec2.update(name = 'address', values = ['157.181.172.126']) 
        
        hoprecord.update(name = 'idx', values = [1,2])
        
        D.saveRecord(hoprecord)

    def test_iteratorNextractor(self):
        N = 1000
        header = DataHeaderGeneratedByDescription("temptable", [('idx', cardinal), ('RoundTripDelay', timeinterval, milli_second)])
        milli = map(lambda x: randint(1, 100000), range(N))
        micro = map(lambda x: 1000*x, milli)
        nano = map(lambda x: 1000000*x, milli)
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = N)
        hoprecord.update(name = 'RoundTripDelay', values = milli, unit = milli_second)
        hoprecord.update(name = 'idx', values = range(N))
        D.saveRecord(hoprecord)
        
        DS = DataSample(table = D)
        
        DR = DataReader(datasource = DS)
        DR.extract(cellrequest = [CellRequestByName(name = 'RoundTripDelay'), CellRequestByName(name = 'RoundTripDelay', unit = micro_second), CellRequestByName(name = 'RoundTripDelay', unit = nano_second)])
        for x in DR:
            mill, mic, nan = milli.pop(0), micro.pop(0), nano.pop(0)
            delta = [(x[0]-mill)/mill, (x[1]-mic)/mic, (x[2]-nan)/nan]
            mask = map(lambda d: abs(d)< self.eps, delta)
            self.assertFalse((False in mask), "Conversion introduced a huge error GOT: %s EXPECTED: %s %s %s DELTA: %s MASK: %s"  % (x, mill,mic,nan, delta, mask))


    @staticmethod
    def randheader():
        return DataHeaderGeneratedByDescription("temptable", [('idx', cardinal), ('rnd', countable)])
    
    def randtable(self, N = 10):
        n = map(lambda x: randint(1, 100000), range(N))
        D = Data(UM, self.randheader())
        hoprecord = D.getTemplate(size = N)
        hoprecord.update(name = 'rnd', values = n)
        hoprecord.update(name = 'idx', values = range(N))
        D.saveRecord(hoprecord)
        S = DataSample(table = D)
        return n, D, S

    def test_reader(self):
        N = 10
        header = self.randheader()
        n1 = map(lambda x: randint(1, 100000), range(N))
        n2 = map(lambda x: randint(1, 100000), range(N))
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = N)
        hoprecord.update(name = 'rnd', values = n1)
        hoprecord.update(name = 'idx', values = range(N))
        DS = DataSample(table = D)
        DR = DataReader(datasource = DS)
        self.assertFalse(DR.sourceExpanded.isSet(), "dataready, howcome?") 
        D.saveRecord(hoprecord)
        DS.process()
        self.assertTrue(DR.sourceExpanded.isSet(), "data not ready, howcome?") 
        for _ in DR:
            pass
        self.assertFalse(DR.sourceExpanded.isSet(), "data still ready, howcome?")
        hoprecord.update(name = 'rnd', values = n2)
        D.saveRecord(hoprecord)
        DS.process()
        self.assertTrue(DR.sourceExpanded.isSet(), "data not ready, howcome?") 
        DR.rewind()
        got = len([x for x in DR])
        self.assertEqual(2*N, got, "Expected %d items and got %d"  % (2*N, got))
    
    def test_formatter(self):
        _, _, DS = self.randtable()
        DF = DumbFormatter(datasource = DS)
        res = DF.serialize()
        #print res
        self.assertGreater(len(res), 2, "empty? %s" % res)
        JF = JsonFormatter(datasource = DS)
        JF.reader.extract(cellrequest = [CellRequestByName(name = 'rnd')])
        res = JF.serialize()
        #print res
        self.assertGreater(len(res), 2, "empty? %s" % res)
        
    def test_aggregator(self):
        N = 10
        n, _, DS = self.randtable(N)
#        self.assertRaises(AggregatorError, Aggregator(D, CellRequestByName(name = 'rnd')))
        s = Sum(DS, CellRequestByName(name = 'rnd'))
        mn = Min(DS, CellRequestByName(name = 'rnd'))
        mx = Max(DS, CellRequestByName(name = 'rnd'))
        avg = Mean(DS, CellRequestByName(name = 'rnd'))
        S = sum(n)
        self.assertEqual(s.data._rawrecords[0], (N, S), "sum %f != %f" % (s._aggregate, S))
        self.assertEqual(mn.data._rawrecords[0], (N, min(n)), "min %f != %f" % (mn._aggregate, min(n)))
        self.assertEqual(mx.data._rawrecords[0], (N, max(n)), "max %f != %f" % (mx._aggregate, max(n)))
        self.assertEqual(avg.data._rawrecords[0], (N, S/float(N)), "avg %f != %f" % (avg._aggregate, S/N))

    def test_sampler(self):
        header = self.randheader()
        D = Data(UM, header)
        DS = DataSample(table = D)
        T = Tail(datasource = DS, tail = 10)
        self.assertRaises(SamplerError, T.process)

        n, D, DS = self.randtable()

        H = Head(datasource = DS, head = 5)
        DR = DataReader(datasource = H)
        expect = n[:5]
        got = [ x for _, x in DR ]
        self.assertEqual(got, expect, "head %s != %s" % (got, expect)) 
        
        T = Tail(datasource = DS)
        T.tail = 5
        DR = DataReader(datasource = T)
        expect = n[-5:]
        got = [ x for _, x in DR ]
        self.assertEqual(got, expect, "tail %s != %s" % (got, expect)) 
        
        expect = n[:]
        expect.sort()
        S = Sorter(datasource = DS, keycell = CellRequestByName(name = 'rnd'))
        DR = DataReader(datasource = S)
        got = [ x for _, x in DR ]
        self.assertEqual(got, expect, "sort %s != %s" % (got, expect)) 

        expect.reverse()
        S = Sorter(datasource = DS, keycell = CellRequestByName(name = 'rnd'), ascending = False)
        DR = DataReader(datasource = S)
        got = [ x for _, x in DR ]
        self.assertEqual(got, expect, "sort %s != %s" % (got, expect)) 


    def test_DispersionOK(self):
        header = self.randheader()
        items = [55,56,57,63,67,68]
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = len(items))
        hoprecord.update(name = 'rnd', values = items)
        hoprecord.update(name = 'idx', values = range(len(items)))
        D.saveRecord(hoprecord)
        DS = DataSample(table = D)
        a = Deviation(DS, CellRequestByName(name = 'rnd'))
        a.empirical = False
        a.data
        self.assertTrue((5.26 == round(a._aggregate,2) ), "Dispersion FAILED 5.26 = "+str(a._aggregate))
        
    def test_PercentOK(self):
        header = self.randheader()
        items = [4.0,5.0,5.0,4.0]
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = len(items))
        hoprecord.update(name = 'rnd', values = items)
        hoprecord.update(name = 'idx', values = range(len(items)))
        D.saveRecord(hoprecord)
        DS = DataSample(table = D)
        a = Percentile(DS, CellRequestByName(name = 'rnd'))
        a.percentile = .5
        a.data
        self.assertTrue((4.5 == a._aggregate ), "Percent is FAILED 4.5 = "+str(a._aggregate))

    def test_Pipe(self):
        header = self.randheader()
        items = [55,56,57,63,67,68]
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = len(items))
        hoprecord.update(name = 'rnd', values = items)
        hoprecord.update(name = 'idx', values = range(len(items)))
        D.saveRecord(hoprecord)
        DS = DataSample(table = D)
        a = Mean(datasource = Tail(datasource = Head(datasource = DS, head = 4), tail = 2), cellrequest = CellRequestByName(name = 'rnd'))
        a.data
        res = a._aggregate
        self.assertTrue((60 == res ), "Pipe FAILED 60 = "+str(res))

    def test_aggregatorManager(self):
        N = 12
        n, D, DS = self.randtable(N)
        
        AM = AggregatorManager()
        _, A = AM.newAggregator(DS, CellRequestByName(name = 'rnd'), [(Tail, {'tail': 10}), (Head, {'head': 5}), (Sum, {})])
       
        A.process()
        expected = sum(n[-10:][:5])
        got = A._aggregate
        self.assertEqual(expected, got, "sum (exp) %f != (got) %f" % (expected, got))
        
        hoprecord = D.getTemplate(size = N)
        n = map(lambda x: randint(1, 100000), range(N))
        hoprecord.update(name = 'rnd', values = n)
        D.saveRecord(hoprecord)

        A.process()
        
        got = A._aggregate
        expected = sum(n[-10:][:5])
        self.assertEqual(expected, got, "2 sum (exp) %f != (got) %f" % (expected, got))


    def test_aggrformatter(self):
        n, _, DS = self.randtable(15)
        AM = AggregatorManager()
        _, A = AM.newAggregator(DS, CellRequestByName(name = 'rnd'), [(Tail, {'tail': 10}), (Head, {'head': 5}), (Sum, {})])
#        _, A = AM.newAggregator(DS, CellRequestByName(name = 'rnd'), [(Tail, {'tail': 10})])
        DF = JsonFormatter(datasource = A)
        res = DF.serialize()
#        print "_"*10
#        print res
#        print "^"*10
        self.assertGreater(len(res), 2, "empty? %s" % res)
        expected = sum(n[-10:][:5])
        got = A._aggregate
        self.assertEqual(expected, got, "2 sum(head(tail())) (exp) %f != (got) %f" % (expected, got))


    def test_ComplexaggregateOK(self):
        '''
        '''
        header = DataHeaderGeneratedByDescription("traceroute", [('idx', cardinal), ("info", [("rttinfo", countable)])])
        
        D = Data(UM, header)
        hoprecord = D.getTemplate(size = 5)
        inf1, inf2, inf3, inf4, inf5 = hoprecord.getRecordTemplates(name = "info")

        inf1.update(name = 'rttinfo', values = [10])        
        inf2.update(name = 'rttinfo', values = [15])
        inf3.update(name = 'rttinfo', values = [16])        
        inf4.update(name = 'rttinfo', values = [18])
        inf5.update(name = 'rttinfo', values = [20])        
        
        hoprecord.update(name = 'idx', values = [1,2,3,4,5])
        
        D.saveRecord(hoprecord)
        #a = Aggregator(D, ['info','rttinfo'])


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.test_UM']
    unittest.main()