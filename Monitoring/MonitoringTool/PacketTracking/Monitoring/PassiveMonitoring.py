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

from Task.Passive.Monitoring.Aggregator import Aggregator

class PassiveMonitoring(object):
    
    def __init__(self):
        self.__Aggregator = Aggregator()
            
    
    # PT Actions
    # 1.
    def getPtVolumeStats(self, startTime, stopTime, nodeList):
        """
        Function to retrieve the observed traffic in the time intervall defined
        between startTime and stopTime. Traffic means, packet/byte count for
        each node in the list.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList:      list
        @param nodeList:     List of nodes, whereby a node is one installed
                             monitoring probe.
        @rtype:              PtVolumeStats (list)
        @return:             (At least) number of packets and bytes observed by
                             a node.
        """
        return self.__Aggregator.getPtVolumeStats(startTime, stopTime, nodeList)
    
    # 2.
    def getPtHopStats(self, startTime, stopTime, nodeList2D):
        """
        Function to retrieve the observed traffic statistics in the time
        intervall defined between startTime and stopTime. Statistics mean
        packet/byte count and min/max/avg delay between each adjacent node/node
        pair.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type StopTime:      struct_time
        @param StopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList2D:    list
        @param nodeList2D:   A List of <first, second> Node pairs.
        @rtype:              PtHopStats (list)
        @return:             Packet tracking Hop statistics, i.e. delay/volume
                             statistics between two neighbor PT probes
        """
        return self.__Aggregator.getPtHopStats(startTime, stopTime, nodeList2D)
      
    # 3.
    def getPtPathStats(self, startTime, stopTime, nodeTuple):
        """
        Function to retrieve the observed traffic statistics in the time
        intervall defined between StartTime and StopTime. Statistics mean
        packet/byte count and min/max/avg delay between each node/node pair.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeTuple:     tuple
        @param nodeTuple:    A pair of nodes represented as a 'tuple' datatype
        @rtype:              PtPathStats (list)
        @return:             Packet tracking Hop statistics, i.e. delay/volume
                             statistics between any two PT probes
        """
        return self.__Aggregator.getPtPathStats(startTime, stopTime, nodeTuple)
        
    # 4.
    def getPtActivity(self, startTime, stopTime, nodeList):
        """
        Returns the 'activity' for each node in the list. Activity is defined
        as a percentage [0-100%] of interval startTime to stopTime . If no
        packet is observed in this defined time interval the function returns
        zero, otherwise the function returns a positive non-zero value. It also
        depends on the configured activity threshold (timeout), which is set by
        the function 'setPtActivityThreshold'.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList:      list
        @param nodeList:     List of nodes, whereby a node is one installed
                             monitoring probe.
        @rtype:              PtActivity
        @return:             Amount of time when there was live traffic
                             observed at a node.
        """
        return self.__Aggregator.getPtActivity(startTime, stopTime, nodeList)
    
    # Pt Management (Impd4e + Collector)
    # 1.
    def restartPtCollector(self):
        """
        Method to restart the Collector.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.restartPtCollector()
    
    # 2.
    def restartPtProbes(self, nodeList):
        """
        Method to restart the probes on all the selected Nodes.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.restartPtProbes(nodeList)
    
    # 3.
    def purgePtDatabase(self, startTime, stopTime):
        """
        Function to purge the observed track information from the database
        (Only for administrative purposes).
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.purgePtDatabase(startTime, stopTime)
     
    # 4.
    def setPtHashRange(self, range):
        """
        Sets the hash range for the hashing algorithm, value must be between 1
        and 100. The higher, the more packets are selected by the hashing
        function. 100 - Means, all packets are selected.
        @type range:         int
        @param range:        A value between 1 and 100.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.setPtHashRange(range)
    
    # 5.
    def setPtHashFunction(self, hashFunction):
        """
        The PT probe provides different hashing functions to do the packet
        selection. Possible parameters are: "BOB", "OAAT", "TWMX", "HSIEH
        @type hashFunction:  str
        @param hashFunction: A short string representing the hashing function.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.setPtHashFunction(hashFunction)
    
    # 6.
    def setPtActivityThreshold(self, timeMSec):
        """
        Function to adjust the activity threshold. Increasing this value in
        effect reduces the probability to receive a "0 - no activity" from the
        function 'getPtActivity'.
        @type timeMSec:        int
        @param timeMSec:       A time value in milliseconds.
        @rtype:                Status
        @return:               Placeholder for return error handling, i.e.
                               "Success" or specific Exception.
        """
        return self.__Aggregator.setPtActivityThreshold(timeMSec)
    
    # PT Visualization (Netview)
    # 1.
    def viewPtPathTracks(self, startTime, stopTime, nodeList):
        """
        Starts a GUI application and displays the path defined by the nodeList,
        in the time interval, defined by startTime and stopTime.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList:      list
        @param nodeList:     List of nodes, whereby a node is one installed
                             monitoring probe.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.viewPtPathTracks(startTime, stopTime, nodeList)
    
    # 2.
    def viewPtHopTracks(self, startTime, stopTime, nodeList2D):
        """
        Starts a GUI application and displays the hop defined by the node
        vector, in the time interval, defined by startTime and stopTime.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param StopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList2D:    list
        @param nodeList2D:   A List of node tuples (src, dst).
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.viewPtHopTracks(startTime, stopTime, nodeList2D)

    # 3.
    def plotPtPathStats(self, startTime, stopTime, nodeList):
        """
        Plots the delay stats in a X/Y-plot for a hop between two nodes given
        by the nodeList, in the time interval defined by startTime and stopTime.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList:      list
        @param nodeList:     List of nodes, whereby a node is one installed
                             monitoring probe.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.plotPtPathStats(startTime, stopTime, nodeList)
    
    # 4.
    def plotPtHopStats(self, startTime, stopTime, nodeList2D):
        """
        Plots the delay stats in a X/Y-plot for a path between multiple nodes
        given by the nodeList, in the time interval defined by startTime and
        stopTime.
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type nodeList2D:    list
        @param nodeList2D:   A List of <first, second> Node pairs.
        @rtype:              Status
        @return:             Placeholder for return error handling, i.e.
                             "Success" or specific Exception.
        """
        return self.__Aggregator.plotPtHopStats(startTime, stopTime, nodeList2D)