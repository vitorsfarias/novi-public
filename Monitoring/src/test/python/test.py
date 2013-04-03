'''
Created on Aug 10, 2011

@author: steger
'''
import unittest
from Service.MonSrvImpl import MonSrvImpl

class Test(unittest.TestCase):

    def setUp(self):
        self.monSrv = MonSrvImpl()
    
    def tearDown(self):
        pass

    def test_echo(self):
        message = "foo"
        _, response = self.monSrv.echo(message)
        self.assertEquals(response, message, "Echo reply differs from expected: %s" % response)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
