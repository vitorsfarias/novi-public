import site
site.addsitedir('classes/Lib')

import unittest

from ServiceTest import ServiceTest
from FedericaTest import FedericaTest


def suite():
    suite = unittest.TestSuite();
    suite = unittest.makeSuite(Tests,'test')

if __name__ == "__main__":
    unittest.main()

