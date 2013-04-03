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

class PtVolumeStats(object):
    """
    (at least) number of packets and bytes observed by a Node
    """
    def __init__(self, startTime = time(), stopTime = time(),
                 node = Node.Node(), number = 0, avgPktSize = 0):
        self.startTime = mktime(startTime) 
        self.stopTime = mktime(stopTime)
        self.node = node
        self.number = number
        self.avgPktSize = avgPktSize
        
    def __str__(self):
        startTime = strftime("%a, %d %b %Y %H:%M:%S", localtime(self.startTime))
        stopTime = strftime("%a, %d %b %Y %H:%M:%S", localtime(self.stopTime))
        Str = ("PtVolumeStats:\n"
               "  Node:            " + str(self.node) + "\n" +                                                       
               "  StartTime:       " + startTime + "\n" +
               "  StopTime:        " + stopTime + "\n" +
               "  Packets:         " + str(self.number) + "\n" +
               "  Avg. Pktsize:    " + str(self.avgPktSize) + " Bytes")  
        return Str

        
    def set_StartTime(self, StartTime):
        """
        Overwritten setter functions to convert the time_struct into a real
        timestamp.
        @type StartTime:            struct_time
        @param StartTime            Time that defines the start of the observation
        """
        self.StartTime = mktime(StartTime)
      
    def set_StopTime(self, StopTime):
        """
        Overwritten setter functions to convert the time_struct into a real
        timestamp.
        @type StartTime:            struct_time
        @param StartTime            Time that defines the end of the observation
        """  
        self.StopTime = mktime(StopTime)