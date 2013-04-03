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

from IPy import IP

import psycopg2

class DBConnector(object):
    def __init__(self, host = "127.0.0.1", port = 5432,
                 db = "dump", user = "user", pwd = "pwd"):
        self.host = host
        self.port = port
        self.db = db
        self.user = user
        self.pwd = pwd
        
    @property
    def port(self):
        return self.__port
    
    @port.setter
    def port(self, port):
        """
        Method to check whether the port is in the valid port range of 1-65535,
        if not it raises a ValueError
        @type port:             int
        @param port:            Port on which the PostgreSQL database is
                                listening
        """
        if port in range(1, 65535):
            self.__port = port
        else:
            raise ValueError("Port must be in range of 1 to 65535")
        
    @property
    def host(self):
        return self.__host
    
    @host.setter
    def host(self, host):
        """
        Method to check whether the given host name is a valid IP address. If
        not the IP address is set to localhost (127.0.0.1).
        @type host:             str
        @param port:            IP on which the PostgreSQL database is
                                reachable
        """
        try:
            IP(host)
            self.__host = host
        except ValueError:
            print "Invalid IP address, setting host to 127.0.0.1"
            self.__host = "127.0.0.1"
        
    def __str__(self):
        return ("Connected to DB " + self.db  + " on " + self.host +
        ":" + str(self.port) + " as " + self.user)
        
    def connect(self):
        """
        Method to connect to a PostgreSQL
        @type Connection:             connection  
        @param Connection:            The connection to the PostgreSQL database
                                      created by 'psycopg2.connect()'
        @type Statement:              string
        @param Statement:             String describing the select statement,
                                      e.g. 'SELECT * from table;'
        """       
        try:
            connection = psycopg2.connect("dbname='" + self.db + "' user='" + 
                                          self.user + "' host='" + self.host +
                                          "' password='" + self.pwd + "'")
            return connection
        except psycopg2.Error, msg:
            print "Error: ", msg
    
    def executeSelect(self, connection, statement):
        """
        Method that executes a select statement on a PosgreSQL database.
        @type connection:             connection  
        @param connection:            The connection to the PostgreSQL database
                                      created by 'psycopg2.connect()'
        @type statement:              str
        @param statement:             String describing the select statement,
                                      e.g. 'SELECT * from table;'
        """
        st = connection.cursor()
        rows = []
        try:
            st.execute(statement)
            rows = st.fetchall()
        except Exception:
            print "Statement '" + statement + "' can not be executed"
        st.close()
        return rows
    
    def executeUpdate(self, connection, statement):
        """
        Method that executes an update statement on a PosgreSQL database.
        @type connection:             connection  
        @param connection:            The connection to the PostgreSQL database
                                      created by 'psycopg2.connect()'
        @type statement:              str
        @param statement:             String describing the update statement,
                                      e.g. 'DELETE FROM table WHERE x=3;'
        """
        st = connection.cursor()
        try:
            st.execute(statement)
            connection.commit()
        except Exception:
            print "Statement '" + statement + "' can not be executed"
        st.close()
        
    def disconnect(self, connection):
        """
        Method to close a given connection
        @type connection:             connection
        @param connection:            A connection to a PostgreSQL that should
                                      be closed database (created by
                                      'psycopg2.connect()')
        """
        connection.close()
