from __future__ import with_statement
'''
Created on Oct 12, 2011

@author: steger
@summary: Here we define some monitoring tools and dress them up with parameters and work flow description
'''
from DataProcessing.Parameter import Parameter, ParameterList
from Example.Resources import PLdict
from Credential.credentialtypes import UsernamePassword, UsernameRSAKey
from Driver.SOAPClient import SOAPClient
from Driver.SshExec import SshExec
from Example.Units import UM, Byte, micro_second, piece, milli_second,\
    nano_second, unitless, nano_unixtimestamp, unixtimestamp, fraction,\
    kilo_Byte, second
from Example.Dimensions import cardinal, countable, ipaddress, timeinterval,\
    informationsize, pointintime, nameofsomething, probability
from DataProcessing.DataHeader import DataHeaderGeneratedByDescription

DOM_SUBSTRATE = 1
DOM_SLICE = 2

sonoma_url = "http://complex.elte.hu/~steger/sonoma/user.wsdl"

nodes = map(lambda x:(x.get_hostname("eth0"), unitless), PLdict.values())

class sonomashortping:
    driver = SOAPClient
    name = "SONoMAPing"
    domain = DOM_SUBSTRATE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('ping', [('Run', countable), 
                                                ('Sequence Number', countable), 
                                                ('Source Address', ipaddress), 
                                                ('Destination Address', ipaddress), 
                                                ('Packet Size', informationsize, Byte), 
                                                ('Time To Live', countable), 
                                                ('Round Trip Delay', timeinterval, micro_second)])    
    
    authtype = (UsernamePassword, )
    kwargs = { "url": sonoma_url, "MAserviceport": 11123 }
    hooks = {
        "prehook" : """
from base64 import b64decode
self.decode = b64decode
self.pattern = re.compile('^(\d+)\s+(\d+\.\d+\.\d+\.\d+)\s+(\d+\.\d+\.\d+\.\d+)\s+(\d+)\s+(\d+)\s+(\d+)$')
self.username=self.credential.username
self.password=self.credential.password
self.client = self.driver(kw.get('url'))
self.sessionId = self.client.service.requestSession(self.username, self.password, 'CSV', False)
self.port = kw.get('MAserviceport')
self.template = self.data.getTemplate(size = 1)
        """,
        "retrievehook" : """
source = "%s:%d" % (self.parameters.get('SourceAddress', self.um.ipv4dotted), self.port)
res = self.client.service.shortPing(self.sessionId, 
  source, self.parameters.get('DestinationAddress', self.um.ipv4dotted), self.parameters.get('Count', self.um.piece), 
  self.parameters.get('Delay', self.um.micro_second), self.parameters.get('PacketSize', self.um.Byte))
rec = self.decode(res).splitlines()
for r in rec:
    if self.pattern.match(r):
        self.template.clear()
        ex = self.pattern.split(r)[:-1]
        ex[0] = self.runcount
        self.template.updateMany( ('Run', 'Sequence Number', 'Source Address', 'Destination Address', 'Packet Size', 'Time To Live', 'Round Trip Delay'), [ex,] )
        self.data.saveRecord(self.template)
return True
        """,
        "posthook": "self.client.service.closeSession(self.username, self.password, self.sessionId)"}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "DestinationAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)),
                   Parameter(name = "Delay", valuetype = int, unitmanager = UM, dimension = timeinterval, default = (100, milli_second)),
                   Parameter(name = "PacketSize", valuetype = int, unitmanager = UM, dimension = informationsize, default = (64, Byte)) ])

class sonomashortchirp:
    driver = SOAPClient
    name = "SONoMAChirp"
    domain = DOM_SUBSTRATE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('onewaydelay', [('Run', countable), 
                                                ('SequenceNumber', countable), 
                                                ('SourceAddress', ipaddress), 
                                                ('DestinationAddress', ipaddress), 
                                                ('TimestampSend', pointintime, nano_unixtimestamp), 
                                                ('OnewayDelay', timeinterval, nano_second) ])    
    authtype = (UsernamePassword, )
    kwargs = { "url": sonoma_url, "MAserviceport": 11123 }
    hooks = {
        "prehook" : """
from base64 import b64decode
self.decode = b64decode
self.pattern = re.compile('^(\d+)\s+(\d+\.\d+\.\d+\.\d+)\s+(\d+\.\d+\.\d+\.\d+)\s+(\d+)\s+(\d+)$')
self.username=self.credential.username
self.password=self.credential.password
self.client = self.driver(kw.get('url'))
self.sessionId = self.client.service.requestSession(self.username, self.password, 'CSV', False)
self.port = kw.get('MAserviceport')
self.template = self.data.getTemplate(size = 1)
self.delaylist = self.client.factory.create("delayList")
self.delaylist.gap = [100000,100000]
        """,
        "retrievehook" : """
source = "%s:%d" % (self.parameters.get('SourceAddress', self.um.ipv4dotted), self.port)
destination = "%s:%d" % (self.parameters.get('DestinationAddress', self.um.ipv4dotted), self.port)
res = self.client.service.shortChirp(self.sessionId, 
  source, self.parameters.get('SourcePort', self.um.unitless), 
  destination, self.parameters.get('DestinationPort', self.um.unitless),
  self.parameters.get('Count', self.um.piece), self.parameters.get('Delay', self.um.milli_second), 
  self.parameters.get('PacketSize', self.um.Byte), self.delaylist)
rec = self.decode(res).splitlines()
data = []
for r in rec:
    if self.pattern.match(r):
        self.template.clear()
        ex = self.pattern.split(r)[:-1]
        ex[0] = self.runcount
        ex[-1] = int(ex[-1])-int(ex[-2])
        data.append( ex )
self.template.clear(size = len(data))
self.template.updateMany( ('Run', 'SequenceNumber', 'SourceAddress', 'DestinationAddress', 'TimestampSend', 'OnewayDelay'), data )
self.data.saveRecord(self.template)
return True
        """,
        "posthook": "self.client.service.closeSession(self.username, self.password, self.sessionId)"}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "DestinationAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)),
                   Parameter(name = "Delay", valuetype = int, unitmanager = UM, dimension = timeinterval, default = (100, milli_second)),
                   Parameter(name = "PacketSize", valuetype = int, unitmanager = UM, dimension = informationsize, default = (64, Byte)),
                   Parameter(name = "SourcePort", valuetype = int, unitmanager = UM, dimension = cardinal, default = (7777, unitless)),
                   Parameter(name = "DestinationPort", valuetype = int, unitmanager = UM, dimension = cardinal, default = (7777, unitless)), ])


class sshping:
    driver = SshExec
    name = "sshPing"
    domain = DOM_SLICE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('ping', [('Run', cardinal), 
                                                ('TimeReceived', pointintime), 
                                                ('PacketSize', informationsize), 
                                                ('DestinationAddress', ipaddress), 
                                                ('SequenceNumber', countable), ('TimeToLive', countable), 
                                                ('RoundTripDelay', timeinterval, milli_second)])
    authtype = (UsernameRSAKey, UsernamePassword)  
    kwargs = {} 
    hooks = {
        "prehook" : """
self.pattern = re.compile('^\[(\d+\.?\d*)\]\s*(\d+)\s*bytes\s*from\s*(\d+\.\d+\.\d+\.\d+):\s*icmp_req=(\d+)\s*ttl=(\d+)\s*time=(\d+\.?\d*)\s*(\w*)')
self.template = self.data.getTemplate(size = self.parameters.get('Count', self.um.piece))
command = "ping -D -n -c %d -i %f -t %d -I %s %s" % (
    self.parameters.get('Count', self.um.piece), self.parameters.get('Delay', self.um.second), 
    self.parameters.get('TimeToLive', self.um.piece), self.parameters.get('Interface', self.um.unitless), 
    self.parameters.get('DestinationAddress', self.um.ipv4dotted))
self.client = self.driver(host = self.parameters.get('SourceAddress', self.um.ipv4dotted), credential = self.credential, command = command)
        """,
        "retrievehook" : """
data = []
for r in self.client.execute().readlines():
    if self.pattern.match(r):
        ex = self.pattern.split(r)[:-2]
        ex[0] = self.runcount
        data.append( ex )
self.template.clear(size = len(data))
self.template.updateMany( ('Run', 'TimeReceived', 'PacketSize', 'DestinationAddress', 'SequenceNumber', 'TimeToLive', 'RoundTripDelay'), data )
self.data.saveRecord(self.template)
return True
        """}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "DestinationAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)),
                   Parameter(name = "Delay", valuetype = float, unitmanager = UM, dimension = timeinterval, default = (200, milli_second)), 
                   Parameter(name = "TimeToLive", valuetype = int, unitmanager = UM, dimension = countable, default = (32, piece)), 
                   Parameter(name = "Interface", valuetype = str, unitmanager = UM, dimension = nameofsomething, default = ("eth0", unitless)) ] )

class sshmeminfo:
    driver = SshExec
    name = "sshMeminfo"
    domain = DOM_SLICE | DOM_SUBSTRATE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('meminfo', [('Run', cardinal), 
                                                   ('AvailableMemory', informationsize), 
                                                   ('FreeMemory', informationsize)])
    authtype = (UsernameRSAKey, UsernamePassword)  
    kwargs = {} 
    hooks = {
        "prehook" : """
self.pattern = re.compile('^(.*):\s*(\d+)\s+(.B)$')
self.template = self.data.getTemplate(size = 1)
command = "cat /proc/meminfo"
self.client = self.driver(host = self.parameters.get('SourceAddress', self.um.ipv4dotted), credential = self.credential, command = command)
        """,
        "retrievehook" : """
self.template.clear()
self.template.update('Run', (self.runcount,))
for r in self.client.execute().readlines():
    if self.pattern.match(r):
        n, v, u = self.pattern.split(r)[1:-1]
        if n == 'MemTotal' and u == 'kB':
            self.template.update('AvailableMemory', (v,))
        elif n == 'MemFree' and u == 'kB':
            self.template.update('FreeMemory', (v,))
self.data.saveRecord(self.template)
return True
        """}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress), ])

class sshdf:
    driver = SshExec
    name = "sshDiskinfo"
    domain = DOM_SLICE | DOM_SUBSTRATE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('diskinfo', [('Run', cardinal), 
                                                   ('Available', informationsize, kilo_Byte), 
                                                   ('Used', informationsize, kilo_Byte)])
    authtype = (UsernameRSAKey, UsernamePassword)  
    kwargs = {} 
    hooks = {
        "prehook" : """
self.pattern = re.compile('^.*\s+\d+\s+(\d+)\s+(\d+)\s+\d+%\s+.*$')
self.template = self.data.getTemplate(size = 1)
command = "df %s" % self.parameters.get('Directory', self.um.unitless)
self.client = self.driver(host = self.parameters.get('SourceAddress', self.um.ipv4dotted), credential = self.credential, command = command)
        """,
        "retrievehook" : """
self.template.clear()
self.template.update('Run', (self.runcount,))
for r in self.client.execute().readlines():
    if self.pattern.match(r):
        u, a = self.pattern.split(r)[1:-1]
        self.template.update('Available', (a,))
        self.template.update('Used', (u,))
self.data.saveRecord(self.template)
return True
        """}
    parameters = ParameterList([ 
            Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress), 
            Parameter(name = "Directory", valuetype = str, unitmanager = UM, dimension = nameofsomething), 
        ])

    
class sshtraceroute:
    driver = SshExec
    name = "sshTraceroute"
    domain = DOM_SLICE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('traceroute', [('Run', cardinal),
                                                      ('Hop', countable), 
                                                      ('Raw', nameofsomething)])
    authtype = (UsernameRSAKey, UsernamePassword)  
    kwargs = {} 
    hooks = {
        "prehook" : """
self.pattern = re.compile('^\s*(\d+)\s+(.*)$')
self.template = self.data.getTemplate(size = 1)
command = "traceroute -n %s" % (self.parameters.get('DestinationAddress', self.um.ipv4dotted))
self.client = self.driver(host = self.parameters.get('SourceAddress', self.um.ipv4dotted), credential = self.credential, command = command)
        """,
        "retrievehook" : """
data = []
for r in self.client.execute().readlines():
    if self.pattern.match(r):
        ex = self.pattern.split(r)[:-1]
        ex[0] = self.runcount
        data.append( ex )
self.template.clear(size = len(data))
self.template.updateMany( ('Run', 'Hop', 'Raw'), data )
self.data.saveRecord(self.template)
return True
        """}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "DestinationAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "Count", valuetype = int, unitmanager = UM, dimension = countable, default = (5, piece)), ])
    
class sshhades:
    driver = SshExec
    name = "HADESaggregates"
    domain = DOM_SUBSTRATE
    dataheaderdeclaration = DataHeaderGeneratedByDescription('hadestable',  [('Run', cardinal),
                                                       ('Time', pointintime, unixtimestamp), 
                                                       ('MinDelay', timeinterval, second), 
                                                       ('MedianDelay', timeinterval, second), 
                                                       ('MaxDelay', timeinterval, second), 
                                                       ('Loss', probability, fraction),
                                                       ])
    authtype = (UsernameRSAKey, UsernamePassword)  
    kwargs = { 'repository': '194.132.52.212', 'samplecount': 9 } 
    hooks = {
        "prehook" : """
self.repository = kw.get('repository')
self.pattern = re.compile('^(\d+)\s+(-?\d+\.?\d*)\s+(-?\d+\.?\d*)\s+(-?\d+\.?\d*)\s+(\d+)\s+.*$')
self.template = self.data.getTemplate(size = 1)
lookup = { '192.168.31.1': 'PSNC_FED', '192.168.31.5': 'DFN_FED', '192.168.31.9': 'GARR_FED' }
root = "/home/novi-monitoring"
source = lookup[ self.parameters.get('SourceAddress', self.um.ipv4dotted) ]
destination = lookup[ self.parameters.get('DestinationAddress', self.um.ipv4dotted) ]
lookupcommand = "echo %s/data/hades/novi/www/*/*/*/%s.%s.0.qos_ai.dat" % (root, source, destination)
self.client = self.driver(host = self.repository, credential = self.credential)
files = self.client.execute(lookupcommand).read().split()
self.command = "%s/hades/bin/hades-show-data.pl --config=novi %s"  % (root, files[-1])
self.nsamples = int(kw.get('samplecount'))
        """,
        "retrievehook" : """
data = []
for r in self.client.execute(self.command).readlines():
    print r
    if self.pattern.match(r):
        ts, dtmin, dtmed, dtmax, loss = self.pattern.split(r)[1:-1]
        data.append( [ self.runcount, ts, dtmin, dtmed, dtmax, float(loss)/self.nsamples ] )
self.template.clear(size = len(data))
self.template.updateMany( ('Run', 'Time', 'MinDelay', 'MedianDelay', 'MaxDelay', 'Loss'), data )
self.data.saveRecord(self.template)
return True
        """}
    parameters = ParameterList([ Parameter(name = "SourceAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   Parameter(name = "DestinationAddress", valuetype = str, unitmanager = UM, dimension = ipaddress),
                   ])
