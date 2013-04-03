'''
Created on Oct 12, 2011

@author: steger
@summary: Here we declare monitorable metrics and combine them with tools that are measuring them 
'''
from Example.Tools import sshping, sonomashortping, sshtraceroute, sshmeminfo,\
    sonomashortchirp
from DataProcessing.Parameter import ParameterList, Parameter
from Example.Units import UM, unitless, milli_second, Byte, piece
from Example.Dimensions import nameofsomething, informationsize,\
    timeinterval, cardinal, countable
from Resource.node import node
from Resource.path import path

class FreeMemory(object):
    name = 'Free Memory'
    resourcetype = node
    p_obligatory = ParameterList()
    p_optional = ParameterList()

class DiskUsage(object):
    name = 'Disk Usage'
    resourcetype = node
    p_obligatory = ParameterList([
        Parameter(name = "Directory", valuetype = str, unitmanager = UM, dimension = nameofsomething, default = ('/dev/mapper/planetlab-vservers', unitless))
        ])
    p_optional = ParameterList()

class RoundTripDelay(object):
    name = 'Round Trip Delay'
    resourcetype = path
    p_obligatory = ParameterList()
    p_optional = ParameterList([
        Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)),
        Parameter(name = "PacketSize", valuetype = int, unitmanager = UM, dimension = informationsize, default = (64, Byte)),
        Parameter(name = "Delay", valuetype = float, unitmanager = UM, dimension = timeinterval, default = (200, milli_second)), 
        Parameter(name = "TimeToLive", valuetype = int, unitmanager = UM, dimension = countable, default = (32, piece)), 
        Parameter(name = "Interface", valuetype = str, unitmanager = UM, dimension = nameofsomething, default = ("eth0", unitless)), 
        ])

class OnewayDelay(object):
    name = 'One Way Delay'
    resourcetype = path
    p_obligatory = ParameterList()
    p_optional = ParameterList([
        Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)),
        Parameter(name = "Delay", valuetype = int, unitmanager = UM, dimension = timeinterval, default = (200, milli_second)), 
        Parameter(name = "TimeToLive", valuetype = int, unitmanager = UM, dimension = countable, default = (32, piece)), 
#        Parameter(name = "Interface", valuetype = str, unitmanager = UM, dimension = nameofsomething, default = (novi_iface, unitless)), 
        Parameter(name = "PacketSize", valuetype = int, unitmanager = UM, dimension = informationsize, default = (64, Byte)),
        Parameter(name = "SourcePort", valuetype = int, unitmanager = UM, dimension = cardinal, default = (7777, unitless)),
        Parameter(name = "DestinationPort", valuetype = int, unitmanager = UM, dimension = cardinal, default = (7777, unitless)),
        ])

class HopMeasurement(object):
    name = 'Hop Measurement'
    resourcetype = path    
    p_obligatory = ParameterList()
    p_optional = ParameterList()


MonitorMetrics = {
    FreeMemory: [sshmeminfo], 
    RoundTripDelay: [sshping, sonomashortping], 
    OnewayDelay: [sonomashortchirp],
    HopMeasurement: [sshtraceroute]
}
