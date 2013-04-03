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

from time import mktime

from Task.Passive.Monitoring.Database.DBConnector import DBConnector

class DBAccess(DBConnector):
    def __init__(self, host = "127.0.0.1", port = 5432,
                 db = "dump", user = "user", pwd = "pwd"):
        DBConnector.__init__(self, host, port, db, user, pwd)
    
    def selectDelayEntrys(self, startTime, stopTime, nodeOid):
        """
        Function to select a set of delay information from the database. The
        statement calls 'SELECT num, sumbytes, sumdelay, mindelay, maxdelay FROM
        delays WHERE (ts >= startTime AND ts <= stopTime) AND (src = nodeOid OR
        dst = nodeOid);'.
        @type startTime:    struct_time
        @param startTime:   A sequence of 9 integers, as returned from
                            gmtime(), localtime() or strptime().
        @type stopTime:        struct_time
        @param stopTime:    A sequence of 9 integers, as returned from
                            gmtime(), localtime() or strptime().
        @rtype:             list
        @return:            List of rows that match the time interval and also
                            the nodes OID
        """
        startTimeString = str(int(mktime(startTime)*1000*1000)) # sec->musec
        stopTimeString = str(int(mktime(stopTime)*1000*1000)) # sec->musec
        statement = ("SELECT num, sumbytes, sumdelay, mindelay, maxdelay "
                     "FROM hop_delays WHERE (ts >= " + startTimeString +
                     " AND ts <= " + stopTimeString + ") AND (src = " +
                     str(nodeOid) + " OR dst = " + str(nodeOid) + ");")
        connection = self.connect()
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return rows
    
    def selectActivityEntries(self, startTime, stopTime, nodeOid):
        """
        Method to get the activity of a node (number of packets in the defined
        time interval).
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @rtype:              list
        @return:             List of rows that match the time interval and also
                             the nodes OID
        """
        startTimeString = str(int(mktime(startTime)*1000*1000)) # sec->musec
        stopTimeString = str(int(mktime(stopTime)*1000*1000)) # sec->musec
        statement = ("SELECT num FROM hop_delays WHERE (ts >= " + 
                     startTimeString + " AND ts <= " + stopTimeString + ") "
                     "AND (src = " + str(nodeOid) + " OR dst = "
                     + str(nodeOid) + ");")
        connection = self.connect()
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return rows
    
    def selectPathDelays(self, startTime, stopTime, srcOid, dstOid):
        startTimeString = str(int(mktime(startTime)*1000*1000)) # sec->musec
        stopTimeString = str(int(mktime(stopTime)*1000*1000)) # sec->musec
        statement = ("SELECT num, sumbytes, sumdelay, mindelay, maxdelay, path "
                     "FROM path_delays WHERE (ts >= " + startTimeString + 
                     " AND ts <= " + stopTimeString + ") AND (src = " +
                     str(srcOid) + " AND dst = " + str(dstOid) + ") "
                     "ORDER BY path ASC;")     
        connection = self.connect()
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return rows
    
    def selectHopDelays(self, startTime, stopTime, srcOid, dstOid):
        startTimeString = str(int(mktime(startTime)*1000*1000)) # sec->musec
        stopTimeString = str(int(mktime(stopTime)*1000*1000)) # sec->musec
        statement = ("SELECT num, sumbytes, sumdelay, mindelay, maxdelay, ts "
                     "FROM hop_delays WHERE (ts >= " + startTimeString +
                     " AND ts <= " + stopTimeString + ") AND (src = " +
                     str(srcOid) + " AND dst = " + str(dstOid) + ");")
        connection = self.connect()
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return rows
    
    def purgeDelayDB(self, startTime, stopTime):
        """
        Function to remove a set of observed tracks from the database in the
        interval between 'StartTime' and 'StopTime'. The statement calls 'DELETE
        FROM delays WHERE (ts >= startTime AND ts <= stopTime);'
        @type startTime:     struct_time
        @param startTime:    A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @type stopTime:      struct_time
        @param stopTime:     A sequence of 9 integers, as returned from
                             gmtime(), localtime() or strptime().
        @rtype:              Integer
        @return:             Returns the number of purged rows on success -1
                             otherwise.
        """
        startTimeString = str(int(mktime(startTime)*1000*1000)) # sec->musec
        stopTimeString = str(int(mktime(stopTime)*1000*1000)) # sec->musec
        statement = ("DELETE FROM hop_delays WHERE (ts >= " + startTimeString +
                    " AND ts <= " + stopTimeString + ");")
        connection = self.connect()
        self.executeUpdate(connection, statement)
        self.disconnect(connection)
        
