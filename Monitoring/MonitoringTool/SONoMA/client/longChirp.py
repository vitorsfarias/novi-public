'''
Created on Dec 2, 2011

@author: steger
'''

import suds
from base64 import b64decode
from time import sleep

class proxy(suds.client.Client):
    wsdl = "http://complex.elte.hu/~steger/sonoma/user.wsdl"
    MA_port = 11122
    
    def __init__(self, username, password, zipresult = False, outputformat = 'CSV'):
        suds.client.Client.__init__(self, self.wsdl)
        self.uname = username
        self.pw = password
        self.sessionId = self.service.requestSession(username, password, outputformat, zipresult)
    
    def __del__(self):
        self.service.closeSession(self.sessionId, self.uname, self.pw)
        
    def generate_delayList(self, delaylist):
        dlt = self.factory.create("delayList")
        dlt.gap = delaylist
        return dlt
    
    def socket(self, address):
        return "%s:%d" % (address, self.MA_port)
    
    def shortChirp(self, source, destination, sport = 7777, dport = 7777, count = 10, delay_ms = 100, packetsize = 64, delaylist = [100000,100000]):
        return b64decode( self.service.shortChirp(self.sessionId, 
               self.socket(source), sport, 
               self.socket(destination), dport,
               count, delay_ms, packetsize, self.generate_delayList(delaylist)) )

    def longChirp(self, source, destination, sport = 7777, dport = 7777, count = 10, delay_ms = 100, packetsize = 64, delaylist = [100000,100000]):
        return self.service.longChirp(self.sessionId, 
               self.socket(source), sport, 
               self.socket(destination), dport,
               count, delay_ms, packetsize, self.generate_delayList(delaylist))
    
    def processInfo(self, processID):
        return self.service.getProcessInfo(self.sessionId, processID)

    def getData(self, processID):
        return b64decode( self.service.getResult(self.sessionId, processID, False) ) 
                          
if __name__ == '__main__':
    service = proxy(username = 'guest', password = 'guest')
    
    # an example how to run a short synchronous measurement
    print "result\n", service.shortChirp(source = "157.181.175.243", destination = "150.254.160.19")
    
    # an example how to run a background measurement
    pid = service.longChirp(source = "157.181.175.243", destination = "150.254.160.19")
    print "processID", pid
    I = service.processInfo(pid)
    print "status", I
    print "do something useful = sleep()"
    sleep(I.expectedDuration)
    print "status", service.processInfo(pid)
    print "result\n", service.getData(pid)
