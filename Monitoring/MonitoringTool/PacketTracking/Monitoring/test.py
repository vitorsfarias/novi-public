"""
Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
Copyright according to BSD License
For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt

@author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
@author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
@author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
@author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
@author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
@author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
@author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
@author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
"""

import unittest

from PassiveMonitoring import PassiveMonitoring

class Test(unittest.TestCase):

    def setUp(self):
        self.pM = PassiveMonitoring()
        self.nodeList = [1,2,3,4,5,42,46]
        self.hashFunctions = ["BOB", "OAAT", "TWMX", "HSIEH"]

    def testGetPtVolumeStats(self):
        ret = self.pM.getPtVolumeStats("2010-10-10T10:10:10",
                                       "2012-10-10T10:10:10",
                                       self.nodeList)
        for r in ret:
            print r
        
    def testGetPtHopStats(self):
        ret = self.pM.getPtHopStats("2010-10-10T10:10:10",
                                    "2012-10-10T10:10:10",
                                    [(1,2), (3,46), (5,42)])
        for r in ret:
            print r
        
    def testGetPtPathStats(self):
        ret = self.pM.getPtPathStats("2010-10-10T10:10:10",
                                     "2012-10-10T10:10:10",
                                     (46,42))
        for r in ret:
            print r
        
    def testGetPtActivity(self):
        ret = self.pM.getPtActivity("2010-10-10T10:10:10",
                                    "2012-10-10T10:10:10",
                                    self.nodeList)
        for r in ret:
            print r
        
    def testRestartPtCollector(self):
        pass
    
    def testRestartPtProbes(self):
        pass
    
    def testPurgePtDatabase(self):
        pass
    
    def testSetPtHashRange(self):
        ret = 0
        for i in range(1,100):
            ret += self.pM.setPtHashRange(i).status
        print "setPtHashRange " + str(ret)
        
    def testSetPtHashFunction(self):
        ret = 0
        for function in self.hashFunctions:
            ret += self.pM.setPtHashFunction(function).status
        print "setPtHashFunction " + str(ret)
        
    def testSetPtActivityThreshold(self):
        ret = self.pM.setPtActivityThreshold(100).status
        print "setPtActivityThreshold " + str(ret)
        
    def testViewPtPathTracks(self):
        self.pM.viewPtPathTracks("2010-10-10T10:10:10",
                                 "2012-10-10T10:10:10",
                                 self.nodeList)
        
    def testViewPtHopTracks(self):
        self.pM.viewPtHopTracks("2010-10-10T10:10:10",
                                "2012-10-10T10:10:10",
                                [(1,2), (3,46), (5,42)])
        
    def testPlotPtPathStats(self):
        self.pM.plotPtPathStats("2010-10-10T10:10:10",
                                "2012-10-10T10:10:10",
                                self.nodeList)
        
    def testPlotPtHopStats(self):
        self.pM.plotPtHopStats("2010-10-10T10:10:10",
                               "2012-10-10T10:10:10",
                               [(1,2), (3,46), (5,42)])

if __name__ == "__main__":
    unittest.main()