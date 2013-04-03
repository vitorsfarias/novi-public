from __future__ import with_statement
'''
Created on Aug 10, 2011

@author: steger
'''

import site
site.addsitedir('../site-packages')

import unittest2
from random import randint
from Example.Resources import PLnodes
from threading import Thread
from Example.credentials import noviCredential, sonomaCredential
from SshExec import SshExec
from SOAPClient import SOAPClient

class Test(unittest2.TestCase):
    testnodes = map(lambda x: x.get_ipaddress("eth0")[0], PLnodes)
    cred_novi = noviCredential
    url_sonoma = "http://complex.elte.hu/~steger/sonoma/user.wsdl"
    cred_sonoma = sonomaCredential
    

    def setUp(self):
        pass

    def tearDown(self):
        pass
    
    def gettestnode(self):
        '''
        @summary: Return a test node IP address chosen random
        @return: ip address
        @rtype: string 
        '''
        return self.testnodes[randint(1, len(self.testnodes))-1]

    def test_helloworldWithMaster(self):
        '''
        @summary: Execute remote command in the name of the testuser authenticated with the master key
        '''
        proc = SshExec(host = self.gettestnode(), credential = self.cred_novi)
        result = proc.execute().read()
        print "alma %s" % result
        self.assertTrue(result.startswith("helloworld @ "), "Remote command output differs from expected")

    def echoWithMaster(self, address):
        '''
        @summary: Execute remote echo command in the name of the testuser authenticated with the master key
        @param address: ip address of the remote machine
        @type address: string  
        '''
        try:
            n = randint(0, 10000)
            command = "echo %d" % n
            proc = SshExec(host = address, credential = self.cred_novi, command = command)
            result = proc.execute().read()
            self.assertTrue(result.strip() == str(n), "Remote command @%s output differs from expected: (%s != %d)" % (address, result, n))
        except Exception, e:
            self.assertFalse(True, "Got an error %s" % e)

    def test_echoWithMaster(self):
        '''
        @summary: Execute remote echo command in the name of the testuser authenticated with the master key
        '''
        self.echoWithMaster(self.gettestnode())
        
    def test_distributedEcho(self):
        '''
        @summary: Execute parallel remote echo commands in a distributed fashion
        '''
        threads = []
        for n in self.testnodes:
            t = Thread(target = self.echoWithMaster, args = (n,))
            t.daemon = True
            t.start()
            threads.append(t)
        while len(threads):
            t = threads.pop()
            t.join(5)

    def test_parallelEcho(self):
        '''
        @summary: Execute parallel remote echo commands in a test node
        '''
        N = 20
        n = self.gettestnode()
        threads = []
        while N:
            N -= 1
            t = Thread(target = self.echoWithMaster, args = (n,))
            t.daemon = True
            t.start()
            threads.append(t)
        while len(threads):
            t = threads.pop()
            t.join(5)

    def test_stress(self):
        '''
        @summary: Consecutively execute parallel remote echo commands in a distributed fashion
        '''
        threads = []
        for n in self.testnodes:
            N = randint(5, 20)
            while N:
                N -= 1
                t = Thread(target = self.echoWithMaster, args = (n,))
                t.daemon = True
                t.start()
                threads.append(t)
        while len(threads):
            t = threads.pop()
            t.join(5)
    
    def test_soap(self):
        '''
        @summary: Run SONoMA getNodeList
        '''
        client = SOAPClient(self.url_sonoma)
        resources = client.service.getNodeList(filter = "AVAILABLE")
        self.assertGreater(len(resources), 0, "sonoma reports no nodes") 

if __name__ == "__main__":
    import sys;sys.argv = ['', 'Test.test_stress']
    unittest2.main()
