'''
Created on 08.08.2011

@author: steger
'''
from DataProcessing.DataReader import DataReader
from DataProcessing.Data import Data
from DataProcessing.DataError import DataError

class Formatter(object):
    def __init__(self, datasource):
        '''
        Constructor
        '''
        self.source = datasource
        self.reader = DataReader(datasource)

    def _cell(self):
        raise DataError("Implement _cell() method")

    def header(self):
        raise DataError("Implement header() method")    

    def serialize(self):
        raise DataError("Implement serialize() method")
    
    @property
    def name(self):
        return self.source.name
    
    @property
    def sourceExpanded(self):
        return self.reader.sourceExpanded.isSet()

    @property
    def sourceCleared(self):
        return self.reader.sourceCleared.isSet()
    
class JsonFormatter(Formatter):
    '''
    @summary: 
    Serialize Data in JSON format 
    '''
    
    def _cell(self, c):
        '''
        @summary: serialize a column in JSON format
        '''
        try:
            feature = "\n            \"FEATURE\": \"%s\"," % c.feature
        except:
            feature = ""
        score = c.unit.reference.count('_')
        if score == 0:
            ret = """{%s
            "NAME" : "%s",
            "DIMENTSION" : "%s",
            "UNIT" : "%s"
            }""" % (feature, c.name, c.dimension.name, c.unit.reference)
        elif score == 1:
            prefix, base = c.unit.reference.split('_')
            ret = """{%s
            "NAME" : "%s",
            "DIMENTSION" : "%s",
            "PREFIX" : "%s",
            "UNIT" : "%s"
            }""" % (feature, c.name, c.dimension.name, prefix, base)
        else:
            ret = "ERROR: %s" % c
        return ret

    def header(self):
        '''
        @summary: serialize full header
        '''
        return """{
      "NAME" : "DataHeader %s",
      "HDRINFO" : [
         %s
      ]
      }""" % (id(self.source._data.header), ",\n         ".join([ self._cell(c) for c in self.reader.headercells() ]))
    
    def serialize(self):
        '''
        @summary: serialize the header and the new lines of the table into JSON format
        @return: formatted string representation of the table
        @rtype: string
        '''
        self.source.process()
        if self.sourceCleared:
            self.reader.rewind()
        if not self.sourceExpanded:
            return ""
        r = []
        for rec in self.reader:
            st = []
            for d in rec:
                if isinstance(d, Data):
                    #FIXME:  
                    st.append( d._dump() )
                else:
                    st.append( str(d) )
            r.append("[ %s ]" % ", ".join(st))
        return """{
   "TYPE" : "%s",
   "ID" : %d,
   "HDR" : %s,
   "DATA" : [
      %s
   ]
}""" % (self.source._data.header.name, id(self), self.header(), ",\n      ".join(r))
 
class DumbFormatter(Formatter):
    '''
    @summary: 
    Serialize Data in a trivial format 
    '''
    
    def _cell(self, c):
        '''
        @summary: serialize column
        '''
        try:
            return "%s (%s/%s) [%s]" % (c.name, c.feature, c.dimension.name, c.unit)
        except:
            return "%s (/%s) [%s]" % (c.name, c.dimension.name, c.unit)

    def header(self):
        '''
        @summary: serialize full header
        '''
        return "<DataHeader %s>: {%s: [%s]}" % (id(self.source._data.header), self.name, ", ".join([ self._cell(c) for c in self.reader.headercells() ]))

    def serialize(self):
        '''
        @summary: serialize the header and the new lines of the table
        @return: formatted string representation of the table, if no new data are ready the empty string is returned
        @rtype: string
        '''
        self.source.process()
        if self.sourceCleared:
            self.reader.rewind()
        if not self.sourceExpanded:
            return ""
        r = []
        for rec in self.reader:
            st = []
            for d in rec:
                if isinstance(d, Data):
                    #FIXME:  
                    st.append( d._dump() )
                else:
                    st.append( str(d) )
            r.append("(%s)" % ", ".join(st))
        return "{%s:\nHDR:%s\n DATA:[\n%s\n]}" % (str(self), self.header(), ", \n".join(r))
