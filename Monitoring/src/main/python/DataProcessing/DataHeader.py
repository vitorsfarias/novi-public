from __future__ import with_statement
'''
Created on Sep 1, 2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
@author: laki, sandor
'''

from DataProcessing.Dimension import DimensionManager
from DataProcessing.DataError import DataError
from DataProcessing.DataHeaderCell import DataHeaderCell, CellRequestByName,\
    CellRequestByFeature

class DataHeader(object):
    '''
    @author: steger, jozsef
    @summary: 
    This class represents the full header of a table.
    One can construct the header as a single step, 
    if they provide a header description or they can use
    methods to add new columns.
    
    In order to be able to represent a wide variety of data and relationship
    between them, a column can refer to another table.
    In that latter case a specific column refer to another DataHeader. 
    '''

    def __init__(self, name):
        '''
        @summary: Constructor
        @param name: the name of the table
        @type name: string
        @raise DataError: corrupt header description
        '''
        self._name = name
        self._cellnames = []
        self._cells = {}
    
    def __iter__(self):
        for cn in self._cellnames:
            yield self._cells[cn]
    
    def __len__(self):
        '''
        @summary: Return the number of columns
        @return: the number of columns currently set
        @rtype: integer
        '''
        return len(self._cellnames)

    def __eq__(self, header):
        '''
        @summary: Comparison operator of table headers.
        Two tables are declared equal, if all the columns' names and their unit models are the same. 
        Two headers are still regarded equal if the order of their columns are different 
        or the current unit of the corresponding columns are not the same.
        @raise DataError: if not DataHeader instances are compared
        @return: True if both the header name and all columns match, their order may vary
        @rtype: boolean
        '''
        if not isinstance(header, DataHeader):
            raise DataError("wrong type to compare")
        if self.name != header.name:
            return False
        if len(self._cellnames) != len(header._cellnames):
            return False
        if self._cells.keys() != header._cells.keys():
            return False
        for n in self._cellnames:
            if self._cells[n] != header._cells[n]:
                return False
        return True
    
    def __ne__(self, header):
        '''
        @summary: comparison operator of table headers.
        @return: True if tables headers differ
        @rtype: boolean
        '''
        return not self.__eq__(header)
    
    def _get_name(self):
        return self._name
    
    def has_name(self, name):
        '''
        @summary: Check for the existence of a given column name
        @param name: the name of the column looking for
        @type name: string
        @return: true if such a name exists
        @rtype: boolean
        '''
        return name in self._cellnames
    
    def addColumn(self, cell):
        '''
        @summary: Append a new column at the end of the current table header structure
        @param cell: pointer to the header of the new column
        @type cell: DataHeader or DataHeaderCell
        @raise DataError: cell is of a wrong type  
        '''
        ishdr = isinstance(cell, DataHeader)
        if ishdr or isinstance(cell, DataHeaderCell):
            name = cell.name
            if self.has_name(name):
                raise DataError("attempt to add a column with an already existing name (%s)" % cell.name)
            self._cells[name] = cell
            self._cellnames.append(name)
        else:
            raise DataError("attempt to add a wrong type of header cell")
    
    def removeColumn(self, name):
        '''
        @summary: remove a named column if it exists in the header. Otherwise do silently nothing
        @param name: the name of the column to remove
        @type name: string
        '''
        if self.has_name(name):
            self._cells.pop(name)
            self._cellnames.pop(self._cellnames.index(name))
    
    def getHeader(self, name):
        '''
        @summary: Return a pointer to the named sub header in the naming hierarchy
        @param name: the name of the sub header searched
        @type name: string
        @raise DataError: name not found
        '''
        if name.count('.') == 0:
            if name == self.name:
                return self
            if self.has_name(name) and isinstance(self._cells[name], DataHeader):
                return self._cells[name]
        elif name.count('.') == 1:
            n_pre, n_post = name.split('.', 1)
            if n_pre == self.name and self.has_name(n_post) and isinstance(self._cells[n_post], DataHeader):
                return self._cells[n_post]
        else:
            n_pre, n, n_post = name.split('.', 2)
            if n_pre == self.name and self.has_name(n) and isinstance(self._cells[n], DataHeader):
                return self._cells[n].getHeader(n_post)
        raise DataError("Lost in the naming hierarchy: %s < %s" % (self.name, name))


    name = property(_get_name,None,None)
#FIXME: complex table lookup is not implemented    
    def getCell(self, cellrequest):
        '''
        @summary: Return the index and the cell referenced by a name
        @param cellrequest: 
        @type name: CellRequest
        @return: index and the cell
        @rtype: (int, Cell)
        @raise DataError: name not found
        '''
        if isinstance(cellrequest, CellRequestByName):
            name = cellrequest.name
            try:
                yield (self._cellnames.index(name), self._cells[name])
            except:
                DataError("Cell with name %s not found" % name)
        elif isinstance(cellrequest, CellRequestByFeature):
            for c in self:
                try:
                    if cellrequest == c:
                        yield (self._cellnames.index(c.name), c)
                except DataError:
                    continue
        else:
            raise DataError("wrong request type")



class DataHeaderGeneratedByDescription(DataHeader):
    def __init__(self, name, headerdescription):
        '''
        @summary: Constructor
        @param name: the name of the table
        @type name: string
        @param headerdescription: the description of a full table header
        @param headerdescription: list or None
        @raise DataError: corrupt header description
        '''
        DataHeader.__init__(self, name)
        for item in headerdescription:
            if len(item) == 2:
                name, description = item
                unit = None
            else:
                name, description, unit = item
            if self.has_name(name):
                raise DataError("Duplicate column name declaration (%s)" % name)
            if description is None or isinstance(description, DimensionManager.Dimension):
                cell = DataHeaderCell(name = name, dimension = description, unit = unit)
                self.addColumn(cell)
            elif isinstance(description, list):
                hdr = DataHeaderGeneratedByDescription(name = name, headerdescription = description)
                self.addColumn(hdr)
            else:
                raise DataError("corrupt header description (%s)" % name)
