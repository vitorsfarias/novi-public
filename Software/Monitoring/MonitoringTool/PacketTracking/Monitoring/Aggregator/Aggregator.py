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

# System imports
from time import strptime, strftime, localtime, mktime
from sys import maxint

# Project imports
from DBAccess import DBAccess
from DelayPlotter import DelayPlotter
from Task.Passive.Monitoring.SliceManager.SliceManager import SliceManager
from Task.Passive.Monitoring.DataTypes import Node, Status
from Task.Passive.Monitoring.DataTypes import PtVolumeStats
from Task.Passive.Monitoring.DataTypes import PtHopStats
from Task.Passive.Monitoring.DataTypes import PtPathStats
from Task.Passive.Monitoring.DataTypes import PtActivity

# Constants DO NOT EDIT!
NUM = 0
SUMBYTES = 1
SUMDELAY = 2
MINDELAY = 3
MAXDELAY = 4
PATH = 5
TS = 5
# Constants

class Aggregator(object):
    def __init__(self):
        self.__noviDB = DBAccess("127.0.0.1", 5432, "noviDB",
                                 "novi_user","novi123")
        self.__sliceDB = SliceManager("127.0.0.1", 5432, "noviDB",
                                      "novi_user", "novi123")
        self.__delayPlotter = DelayPlotter()
    
    def __checkHashFuntion(self, function):
        """
        Method to check if the given 'function' is a valid PT hashing function
        @type Function:             str
        @param Function:            String that contains the name of the
                                    hashing function.
        @rtype                      boolean
        @return                     Returns 'true' if the function is a valid
                                    hashing function, 'false' otherwise
        """
        return function in ["BOB", "OAAT", "TWMX", "HSIEH"]
            
    def __checkDateTime(self, timeString):
        """
        Method that checks if a given time string is valid, according to the
        ISO time format ("YYYY-MM-DDThh:mm:ss"). If its valid the string is
        converted to Pythons specific 9 floating values format (time_struct)
        and returned to the caller.
        @type timeString:         str
        @param timeString:        The string that contains the time, which
                                  should be verified.
        @rtype:                   struct_time
        @return:                  Time in Python specific 9 floating values
                                  format.
        """
        timeTuple = localtime()
        try:
            timeTuple= strptime(timeString, "%Y-%m-%dT%H:%M:%S")
        except ValueError:
            print ("Wrong date/time format, time must be provided in the"
                   " following format 'YYYY-MM-DDThh:mm:ss'")
            print ("Time set to: " + strftime("%Y-%m-%dT%H:%M:%S", timeTuple))       
        return timeTuple
        
    def __checkNodeList(self, nodeList):
        """
        Check the validity of the given nodeList, if one of the nodes is neither
        of type int nor of type Node the method raises an exception. For int
        values in the list the method converts them to a Node with Oid from the
        int value. The method also checks if all nodes are in the booked slice,
        if not the concerning node is removed from the list.
        @type nodeList:         list
        @param nodeList:        List of nodes that should be validated.
        @rtype:                 list
        @return:                New list with only valid nodes.
        """
        for i in range(len(nodeList)):
            nodeList[i] = self.__makeNodeFromOid(nodeList[i])  
        return [node for node in nodeList if self.__sliceDB.validateNode(node)]

    def __checkNodeList2D(self, nodeList2D):
        """
        Check the validity of the given nodeList2D, if one of the nodes tuples
        is not of type tuple with the length of 2 the method raises an
        exception. The method also checks if all nodes are in the booked slice,
        if not the concerning node tuple is removed from the list.
        @type nodeList2D:       list
        @param nodeList2D:      List of nodes that should be validated.
        @rtype:                 list
        @return:                New list with only valid node tuples.
        """        
        for i in range(len(nodeList2D)):
            nodeList2D[i] = self.__makeNodeTupleFromOid(nodeList2D[i])    
        return [nodeTuple for nodeTuple in nodeList2D if
                (self.__sliceDB.validateNode(nodeTuple[0]) and
                 self.__sliceDB.validateNode(nodeTuple[1]))]

    def __checkNodeTuple(self, nodeTuple):
        """
        Checks if both tuple entries are valid nodes.
        @type nodeTuple:        tuple
        @param nodeTuple        Tuple that should be checked for validity
        @type                   tuple
        @return:                The old tuple if everything is all right or
                                an empty tuple if the tuple containes invalid
                                datatypes or is not in the booked slice       
        """
        nodeTuple = self.__makeNodeTupleFromOid(nodeTuple)
        return nodeTuple if (self.__sliceDB.validateNode(nodeTuple[0]) and
                 self.__sliceDB.validateNode(nodeTuple[1])) else ()

    def __makeNodeFromOid(self, value):
        """
        This method checks whether the given 'value' is of type Node or int
        (OID), if it is an int, it returns a new Node, with the given
        'value' as OID, otherwise it just returns the Node.
        @type value:             int/Node
        @param value:            The value that is checked if it is of type int
                                 or Node.
        @rtype                   Node
        @return                  Newly created node if 'value' was int, or just
                                 returning the given value if the type already
                                 was Node.
        """
        if type(value).__name__ == "int":
            return Node.Node(value)
        elif type(value).__name__ == "Node":
            return value
        else:
            raise TypeError("'node' must be of type 'int' or 'Node'")

    def __makeNodeTupleFromOid(self, nodeTuple):
        if type(nodeTuple).__name__ == "tuple" and len(nodeTuple) == 2:
            return (self.__makeNodeFromOid(nodeTuple[0]),
                    self.__makeNodeFromOid(nodeTuple[1]))
        else:
            raise TypeError("'nodeTuple' must be of type 'tuple' and length "
                            "must be 2")

    def getPtVolumeStats(self, startTime, stopTime, nodeList):
        # Check if all given parameters are valid
        startTime = self.__checkDateTime(startTime) # struct_time
        stopTime = self.__checkDateTime(stopTime) # struct_time
        nodeList = self.__checkNodeList(nodeList)
        stats = []
              
        for node in nodeList:
            maxDelay = 0
            minDelay = maxint
            byteCount = 0
            avgPktSize = 0
            number = 0
            
            rows = self.__noviDB.selectDelayEntrys(startTime, stopTime,
                                                   node.oid)
            if len(rows) != 0:
                for row in rows:
                    if (row[MAXDELAY] > maxDelay):
                        maxDelay = row[MAXDELAY]
                    if (row[MINDELAY] < minDelay):
                        minDelay = row[MINDELAY]
                    byteCount += row[SUMBYTES]
                    number += row[NUM]
                
                if number != 0:
                    avgPktSize = byteCount/number
            stats.append(PtVolumeStats.PtVolumeStats(startTime, stopTime, node,
                                                     number, avgPktSize))
        return stats

    def getPtHopStats(self, startTime, stopTime, nodeList2D):
        # Check if all given parameters are valid
        startTime = self.__checkDateTime(startTime) # struct_time
        stopTime = self.__checkDateTime(stopTime) # struct_time
        nodeList2D = self.__checkNodeList2D(nodeList2D)
        stats = []
                
        for nodeTuple in nodeList2D:
            src = nodeTuple[0]
            dst = nodeTuple[1]
            
            maxDelay = 0
            minDelay = maxint
            byteCount = 0
            avgPktSize = 0
            delayCount = 0
            avgDelay = 0
            number = 0
            
            rows = self.__noviDB.selectHopDelays(startTime, stopTime,
                                                 src.oid, dst.oid)
            if len(rows) != 0:
                for row in rows:
                    if (row[MAXDELAY] > maxDelay):
                        maxDelay = row[MAXDELAY]
                    if (row[MINDELAY] < minDelay):
                        minDelay = row[MINDELAY]
                    byteCount += row[SUMBYTES]
                    delayCount += row[SUMDELAY]
                    number += row[NUM]
                
                if number != 0:
                    avgPktSize = byteCount/number
                    avgDelay = delayCount/number
            
            stats.append(PtHopStats.PtHopStats(startTime, stopTime, src, dst,
                                               number, avgDelay, avgPktSize))
        return stats
    
    def getPtPathStats(self, startTime, stopTime, nodeTuple):
        # Check if all given parameters are valid
        startTime = self.__checkDateTime(startTime) # struct_time
        stopTime = self.__checkDateTime(stopTime) # struct_time
        nodeTuple = self.__checkNodeTuple(nodeTuple)
        stats = []
          
        if len(nodeTuple) == 0:
            return stats
        src = nodeTuple[0]
        dst = nodeTuple[1]
        path = ""
   
        rows = self.__noviDB.selectPathDelays(startTime, stopTime,
                                              src.oid, dst.oid)
        if len(rows) != 0:
            for row in rows:
                if len(path) == 0:
                    maxDelay = 0
                    minDelay = maxint
                    byteCount = 0
                    avgPktSize = 0
                    delayCount = 0
                    avgDelay = 0
                    number = 0
                    path = row[PATH]
                if row[PATH] != path:
                    stats.append(PtPathStats.PtPathStats(startTime, stopTime,
                                                         src, dst, number,
                                                         avgPktSize, avgDelay,
                                                         path))
                    path = ""
                else:
                    if (row[MAXDELAY] > maxDelay):
                        maxDelay = row[MAXDELAY]
                    if (row[MINDELAY] < minDelay):
                        minDelay = row[MINDELAY]
                    byteCount += row[SUMBYTES]
                    delayCount += row[SUMDELAY]
                    number += row[NUM]
                
                    if number != 0:
                        avgPktSize = byteCount/number
                        avgDelay = delayCount/number
        
        stats.append(PtPathStats.PtPathStats(startTime, stopTime, src, dst,
                                             number, avgPktSize, avgDelay, 
                                             path))
        return stats

    def getPtActivity(self, startTime, stopTime, nodeList):
        # Check if all given parameters are valid
        startTime = self.__checkDateTime(startTime) # struct_time
        stopTime = self.__checkDateTime(stopTime) # struct_time
        nodeList = self.__checkNodeList(nodeList)
        stats = []
        
        for node in nodeList:
            activity = 0
            packets = 0
            percentile = 0
            
            rows = self.__noviDB.selectActivityEntries(startTime, stopTime,
                                                       node.oid)
            if len(rows) != 0:
                for row in rows:
                    packets += row[NUM]
                    
            activity = packets*self.__sliceDB.selectActivityThreshold(node.oid)
            
            if(mktime(stopTime) - mktime(startTime) != 0):
                percentile = activity*100 /((mktime(stopTime) -
                                             mktime(startTime)) *1000*1000)
            
            if percentile > 100:
                percentile = 100
            stats.append(PtActivity.PtActivity(startTime, stopTime, node,
                                               percentile))
        return stats
        
    def restartPtCollector(self):
        # Carsten/Jens
        print "restartPtCollector"
        return Status.Status()
    
    def restartPtProbes(self, nodeList):
        # Carsten/Jens
        print "restartPtProbes"
        return Status.Status()
    
    def purgePtDatabase(self, startTime, stopTime):
        #TODO: Fix the return type to a real Status
        self.__noviDB.purgeDelayDB(startTime, stopTime)
        return Status.Status()
    
    def setPtHashRange(self, range):
        #TODO: Fix the return type to a real Status
        try:
            if int(range) <= 100 and int(range) > 0:
                self.__sliceDB.updateHashRange(range)
                return Status.Status()
            else:
                raise ValueError
        except ValueError:
            print "Invalid range, value must an integer between 1 and 100."         
        return Status.Status(-1)
    
    def setPtHashFunction(self, hashFunction):
        #TODO: Fix the return type to a real Status
        if self.__checkHashFuntion(hashFunction):
            self.__sliceDB.updateHashFunction(hashFunction)
            return Status.Status()
        else:
            print hashFunction + ": Unknown hashing function."
            return Status.Status(-1)
        
            
    def setPtActivityThreshold(self, timeMSec):
        #TODO: Fix the return type to a real Status
        self.__sliceDB.updateActivityThreshold(timeMSec)
        return Status.Status()
        
    def viewPtPathTracks(self, startTime, stopTime, nodeList):
        # Spaeter
        print "viewPtPathTracks"
        return Status.Status()
    
    def viewPtHopTracks(self, startTime, stoptime, nodeList2D):
        # Spaeter
        print "viewPtHopTracks"
        return Status.Status()

    def plotPtPathStats(self, startTime, stopTime, nodeList):
        print "plotPtPathStats"
        return Status.Status()
    
    def plotPtHopStats(self, startTime, stopTime, nodeList2D):
        # Check if all given parameters are valid
        startTime = self.__checkDateTime(startTime) # struct_time
        stopTime = self.__checkDateTime(stopTime) # struct_time
        nodeList2D = self.__checkNodeList2D(nodeList2D)

        for nodeTuple in nodeList2D:
            src = nodeTuple[0]
            dst = nodeTuple[1]
            delaysY = []
            timeX = []
            
            rows = self.__noviDB.selectHopDelays(startTime, stopTime,
                                                 src.oid, dst.oid)
            if len(rows) != 0:
                for row in rows:
                    delaysY.append(row[SUMDELAY]/row[NUM])
                    timeX.append(row[TS])
                self.__delayPlotter.plotStats(timeX, delaysY)
            else:
                return Status.Status(-1)
            
        return Status.Status()
