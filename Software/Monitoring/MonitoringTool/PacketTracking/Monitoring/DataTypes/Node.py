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

class Node(object):
    def __init__(self, oid = 0, ip = "0.0.0.0",
                 latitude = 0.0, longitude = 0.0):
        self.oid = oid
        self.ip = ip
        self.latitude = latitude
        self.longitude = longitude
    
    def set_ip(self, ip):
        try:
            IP(ip)
            self.ip = ip
        except ValueError:
            self.ip = "0.0.0.0"
    
    def __str__(self):
        return (str(self.oid) + " (" + str(self.ip) + ")")
