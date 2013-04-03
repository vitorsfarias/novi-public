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

from time import mktime, time, strftime, localtime

from Task.Passive.Monitoring.DataTypes import Node

class PtPathStats(object):
    def __init__(self, startTime = time(), stopTime = time(), src = Node.Node(),
                 dst = Node.Node(), number = 0, avgPktSize = 0,
                 avgDelay = 0, path = ""):
        self.startTime = mktime(startTime) 
        self.stopTime = mktime(stopTime)
        self.src = src
        self.dst = dst 
        self.number = number
        self.avgPktSize = avgPktSize
        self.avgDelay = avgDelay
        self.path = path
        
    def __str__(self):
        startTime = strftime("%a, %d %b %Y %H:%M:%S",
                             localtime(self.startTime))
        stopTime = strftime("%a, %d %b %Y %H:%M:%S", 
                            localtime(self.stopTime))
        return ("PtPathStats:\n"
                "  Src. Node:       " + str(self.src) + "\n" +
                "  Dst. Node:       " + str(self.dst) + "\n" + 
                "  Path:            " + self.path + "\n" +                                          
                "  StartTime:       " + startTime + "\n" +
                "  StopTime:        " + stopTime + "\n" +
                "  Packets:         " + str(self.number) + " Pkts\n"
                "  Avg. Pktsize:    " + str(self.avgPktSize) + " Bytes\n" +
                "  Avg. Delay:      " + str(self.avgDelay) + " ms")
    
    def set_startTime(self, startTime):
        """
        Overwritten setter functions to convert the time_struct into a real
        timestamp.
        @type StartTime:            struct_time
        @param StartTime            Time that defines the start of the observation
        """
        self.startTime = mktime(startTime)
      
    def set_stopTime(self, stopTime):
        """
        Overwritten setter functions to convert the time_struct into a real
        timestamp.
        @type StartTime:            struct_time
        @param StartTime            Time that defines the end of the observation
        """  
        self.stopTime = mktime(stopTime)