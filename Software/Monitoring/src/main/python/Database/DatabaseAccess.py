'''
Created on 08.08.2011

@author: csc
'''

from ConnectionPool import ConnectionPool

class DatabaseAccess():
    '''
    classdocs
    '''


    def __init__(self, parent):
        '''
        Constructor
        '''
        self.parent = parent
        self.pool = ConnectionPool(params = "foo")