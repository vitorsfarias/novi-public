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

from Task.Passive.Monitoring.Database.DBConnector import DBConnector

class SliceManager(DBConnector):
    def __init__(self, host = "127.0.0.1", port = 5432,
                 db = "dump", user = "user", pwd = "pwd"):
        """
        Constructor of the silce manager.
        @type host:             String
        @param host:            host where the database is running on
                                (default: '127.0.0.1'). Give an IP address,
                                because the DBConnector is validating it.
        @type port:             int
        @param port:            Port number the PostgreSQL database is listening
                                on (default: 5432). Must be in range of 1-65535.
        @type db:               String
        @param db:              Name of the database (default: 'dump').
        @type user:             String
        @param user             Username (default: 'user').
        @type pwd:              String
        @param pwd:             Password for user (default: 'pwd').
        """
        DBConnector.__init__(self, host, port, db, user, pwd)
       
    def validateNode(self, node):
        connection = self.connect()
        statement = ("SELECT oid FROM slice WHERE oid = " + str(node.oid) + ";")
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return False if len(rows) == 0 else True
    
    def updateHashRange(self, range):
        connection = self.connect()
        statement = ("UPDATE slice SET hashrange = " + str(range) + ";")
        self.executeUpdate(connection, statement)
        self.disconnect(connection)
        
    def updateHashFunction(self, function):
        connection = self.connect()
        statement = ("UPDATE slice SET algorithm = '" + function + "';")
        self.executeUpdate(connection, statement)
        self.disconnect(connection)
    
    def selectActivityThreshold(self, nodeOid):
        connection = self.connect()
        statement = ("SELECT activity FROM slice WHERE oid = "
                     + str(nodeOid) + ";") 
        rows = self.executeSelect(connection, statement)
        self.disconnect(connection)
        return rows[0][0]
    
    def updateActivityThreshold(self, timeMSec):
        connection = self.connect()
        statement = ("UPDATE slice SET activity = " + str(timeMSec) + ";")
        self.executeUpdate(connection, statement)
        self.disconnect(connection)