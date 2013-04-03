'''
Created on Oct 28, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''
import logging

class Driver(object):
    '''
    @summary: an empty driver to serve as an ancient class
    @author: steger, jozsef
    @cvar logger: an interface to the logger named "NOVI.DRIVER"
    @type logger: logging.Logger 
    '''
    logger = logging.getLogger("NOVI.DRIVER")

class DriverError(Exception):
    pass