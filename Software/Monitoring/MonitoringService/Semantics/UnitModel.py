'''
Created on Feb 12, 2012

@author: steger
'''
from DataProcessing.Prefix import PrefixManager
from DataProcessing.Unit import UnitManager
from DataProcessing.Dimension import DimensionManager
from DataProcessing.MeasurementLevel import lut_level

class UnitModel(object):
    '''
    @summary: an interface to infer prefix, unit and dimension related information from the model
    '''
    
    def __init__(self, ontology):
        '''
        @summary: constructor
        @param ontology: the basic knowledge
        @type ontology: Ontology 
        '''
        self.ontology = ontology
        self.pm = PrefixManager()
        self.um = UnitManager()
        self.dm = DimensionManager(self.um)

        # infer and store prefixes
        for (p_reference, p_symbol, base, exponent) in self.inferPrefixes():
            self.pm.newPrefix( self.ontology._tail(p_reference), p_symbol, base, exponent )
        
        # infer basic units
        for u_reference, u_symbol in self.inferBaseUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, _, _ in self.inferPowerUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, _ in self.inferProductUnits():
            self.storeBasicUnit(u_reference, u_symbol)
        for u_reference, u_symbol, derivedfrom, scale, offset in self.inferLinearTransformedUnits():
            self.storeLinearTransformedUnit(u_reference, u_symbol, derivedfrom, scale, offset)
        for u_reference, u_symbol, derivedfrom, expr_fwd, expr_inv in self.inferRegexpTransformedUnits():
            uref = self.ontology._tail(u_reference)
            ancestor = self.um[ self.ontology._tail(derivedfrom) ]
            self.um.addRegexpTransformedUnit(uref, u_symbol, ancestor, expr_fwd, expr_inv)

        # infer dimensions
        #FIXME: if there is a reference loop an error is raised...
        for d_reference, u_reference, level in self.inferBaseDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            lref = self.ontology._tail(level)
            level = lut_level[lref]
            unit = self.um[uref]
            self.dm.newBaseDimension(dref, dref, unit, level)
        for d_reference, u_reference, d_derivedfrom in self.inferDifferenceDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.DifferenceDimension)
        for d_reference, u_reference, d_derivedfrom, exponent in self.inferPowerDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.PowerDimension, exponent = exponent)
        for d_reference, u_reference, d_derivedfrom in self.inferProductDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            unit = self.um[uref]
            derivedfrom = tuple( self.dm[self.ontology._tail(x)] for x in d_derivedfrom )
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.ProductDimension)
        for d_reference, u_reference, d_derivedfrom in self.inferRatioDimensions():
            dref = self.ontology._tail(d_reference)
            uref = self.ontology._tail(u_reference)
            daref = self.ontology._tail(d_derivedfrom)
            unit = self.um[uref]
            derivedfrom = self.dm[daref]
            self.dm.newDerivedDimension(dref, dref, unit, derivedfrom, self.dm.RatioDimension)

    def storeBasicUnit(self, u_reference, u_symbol):
        uref = self.ontology._tail(u_reference)
        bu = self.um.newBasicUnit(uref, u_symbol)
        for p_reference in self.inferPossiblePrefixesOf(u_reference):
            p = self.pm[ self.ontology._tail(p_reference) ]
            puref = "%s_%s" % (p.reference, uref)
            symbol = "%s%s" % (p.symbol, bu.symbol)
            self.um.addLinearTransformedUnit(puref, symbol, bu, p.scale)

    def storeLinearTransformedUnit(self, u_reference, u_symbol, derivedfrom, scale, offset):
        uref = self.ontology._tail(u_reference)
        ancestor = self.um[ self.ontology._tail(derivedfrom) ]
        bu = self.um.addLinearTransformedUnit(uref, u_symbol, ancestor, scale, offset)
        for p_reference in self.inferPossiblePrefixesOf(u_reference):
            p = self.pm[ self.ontology._tail(p_reference) ]
            puref = "%s_%s" % (p.reference, uref)
            symbol = "%s%s" % (p.symbol, bu.symbol)
            self.um.addLinearTransformedUnit(puref, symbol, bu, p.scale)
    
    def inferPrefixes(self):
        '''
        @summary: iterate over all prefixes defined in the model.
        @return: a generator of the prefix details: (reference, symbol, base, exponent)
        @rtype: (URIRef, str, int, int) 
        @todo: in case the unit:base is not present in a Prefix individual, 
        we should fall back to the restriction on the base defined for the given sibling of the Prefix.  
        This sibling is referenced ?basegroup in the query.
        '''
        q = """
SELECT ?prefix ?symbol ?base ?exponent
WHERE {
 ?prefix a owl:NamedIndividual ;
         a ?basegroup ;
         unit:exponent ?exponent ;
         unit:base ?base .
 ?basegroup rdfs:subClassOf unit:Prefix .
 OPTIONAL {
  ?prefix unit:symbol ?symbol .
 }
}
        """
        for uri_prefix, symbol, base, exponent in self.ontology.query(q):
            if symbol is None:
                yield uri_prefix, self.ontology._tail(uri_prefix), int(base), int(exponent)
            else:
                yield uri_prefix, str(symbol), int(base), int(exponent)

    def inferPrefixSymbolOf(self, prefixuri):
        '''
        @summary: generates an short written form of a unit prefix if unit:symbol is present in the model,
        otherwise an abbreviation is derived from the tail of the uri (the reference name to the individual).
        @param prefixuri: the uri reference to the unit prefix
        @type prefixuri: URIRef
        @return: the short form
        @rtype: str   
        '''
        try:
            _, _, symbol = self.ontology.triples((prefixuri, self.ontology.ns('unit')['symbol'], None)).next()
            return str(symbol)
        except StopIteration:
            return self.ontology._tail(prefixuri)
    

    def inferBaseUnits(self):
        '''
        @summary: iterate over all BaseUnits defined in the model.
        @return: a generator of the unit details: (reference, symbol)
        @rtype: (URIRef, str) 
        '''
        q = """
SELECT ?unit ?symbol
WHERE {
 ?unit a owl:NamedIndividual ;
       a unit:BaseUnit .
 OPTIONAL {
  ?unit unit:symbol ?symbol .
 }
} 
        """
        for uri_unit, symbol in self.ontology.query(q):
            if symbol is None:
                yield uri_unit, self.ontology._tail(uri_unit)
            else:
                yield uri_unit, str(symbol)

    def inferPowerUnits(self):
        '''
        @summary: iterate over all PowerUnits defined in the model.
        @return: a generator of the unit details: (reference, symbol, powerof, exponent)
        @rtype: (URIRef, str, URIRef, int) 
        '''
        q = """
SELECT ?unit ?symbol ?powerof ?exponent
WHERE {
 ?unit a owl:NamedIndividual ;
       a unit:PowerUnit ;
       unit:exponent ?exponent ;
       unit:derivedFrom ?powerof .
 OPTIONAL {
  ?unit unit:symbol ?symbol .
 }
} 
        """
        for uri_unit, symbol, uri_powerof, exponent in self.ontology.query(q):
            if symbol is None:
                yield uri_unit, self.ontology._tail(uri_unit), uri_powerof, int(exponent)
            else:
                yield uri_unit, str(symbol), uri_powerof, int(exponent)

    def inferProductUnits(self):
        '''
        @summary: iterate over all ProductUnits defined in the model.
        @return: a generator of the unit details: (reference, symbol, productof)
        @rtype: (URIRef, str, set(URIRef)) 
        '''
        q = """
SELECT ?unit ?symbol ?productof
WHERE {
 ?unit a owl:NamedIndividual ;
       a unit:ProductUnit ;
       unit:derivedFrom ?productof
 OPTIONAL {
  ?unit unit:symbol ?symbol .
 }
} 
        """
        container = {}
        for uri_unit, symbol, uri_productof in self.ontology.query(q):
            if symbol is None:
                key = uri_unit, self.ontology_tail(uri_unit)
            else:
                key = uri_unit, str(symbol)
            if not container.has_key(key):
                container[key] = set()
            container[key].add(uri_productof)
        for (uri_unit, symbol), productof in container.iteritems():
            yield uri_unit, symbol, productof

    def inferLinearTransformedUnits(self):
        '''
        @summary: iterate over all LinearTransformedUnits defined in the model.
        @return: a generator of the unit details: (reference, symbol, derivedfrom, scale, offset)
        @rtype: (URIRef, str, URIRef, float, float) 
        '''
        q = """
SELECT ?unit ?symbol ?scale ?offset ?derivedfrom
WHERE {
 ?unit a owl:NamedIndividual ;
       a unit:LinearTransformedUnit ;
       unit:derivedFrom ?derivedfrom ;
       unit:scale ?scale .
 OPTIONAL {
  ?unit unit:offset ?offset .
 }
 OPTIONAL {
  ?unit unit:symbol ?symbol .
 }
} 
        """
        for uri_unit, symbol, scale, offset, uri_derivedfrom in self.ontology.query(q):
            if offset is None:
                offset = 0
            else:
                offset = self.ontology._float(offset)
            if symbol is None:
                yield uri_unit, self.ontology._tail(uri_unit), uri_derivedfrom, self.ontology._float(scale), offset
            else:
                yield uri_unit, str(symbol), uri_derivedfrom, self.ontology._float(scale), offset

    def inferRegexpTransformedUnits(self):
        '''
        @summary: iterate over all RegexpTransformedUnits defined in the model.
        @return: a generator of the unit details: (reference, symbol, derivedfrom, expr_fwd, expr_inv)
        @rtype: (URIRef, str, URIRef, str, str) 
        '''
        q = """
SELECT ?unit ?symbol ?derivedfrom ?fwd ?inv
WHERE {
 ?unit a owl:NamedIndividual ;
       a unit:RegexpTransformedUnit ;
       unit:derivedFrom ?derivedfrom ;
       unit:forwardExpression ?fwd ;
       unit:inverseExpression ?inv .
 OPTIONAL {
  ?unit unit:symbol ?symbol .
 }
} 
        """
        for uri_unit, symbol, uri_derivedfrom, expr_fwd, expr_inv in self.ontology.query(q):
            if symbol is None:
                yield uri_unit, self.ontology._tail(uri_unit), uri_derivedfrom, str(expr_fwd), str(expr_inv)
            else:
                yield uri_unit, str(symbol), uri_derivedfrom, str(expr_fwd), str(expr_inv)
        
    def inferPossiblePrefixesOf(self, uri_unit):
        '''
        @summary: extract possible prefixes for the given unit
        @param unit: reference to the unit
        @type unit: URIRef
        @return: a generator over the references of the possible unit prefixes
        @rtype: URIRef 
        '''
        for _, _, uri_prefix in self.ontology.triples((uri_unit, self.ontology.ns('unit')['possiblePrefix'], None)):
            yield uri_prefix

    def inferBaseDimensions(self):
        '''
        @summary: extract BaseDimensions and their corresponding units from the model
        @return: a generator of the BaseDimension details: (reference, unit, level)
        @rtype: (URIRef, URIRef, str) 
        '''
        q = """
SELECT ?dimension ?unit ?level
WHERE {
 ?dimension rdfs:subClassOf unit:BaseDimension ;
            rdfs:subClassOf ?constraint ;
            rdfs:subClassOf ?level .
 ?constraint owl:onProperty unit:defaultUnit ;
            owl:hasValue ?unit .
 FILTER regex(?level, "Level") .
}
        """
        for uri_dimension, uri_unit, level in self.ontology.query(q):
            yield uri_dimension, uri_unit, level

    def inferDifferenceDimensions(self):
        '''
        @summary: extract DifferenceDimensions and their corresponding units from the model
        @return: a generator of the DifferenceDimension details: (reference, unit, derivedfrom)
        @rtype: (URIRef, URIRef, URIRef) 
        '''
        q = """
SELECT ?dimension ?unit ?derivedFrom
WHERE {
 ?dimension rdfs:subClassOf unit:DifferenceDimension ;
            rdfs:subClassOf ?constraint1 ;
            rdfs:subClassOf ?constraint2 .
 ?constraint1 owl:onProperty unit:defaultUnit ;
              owl:hasValue ?unit .
 ?constraint2 owl:onProperty unit:derivedFrom ;
              owl:onClass ?derivedFrom .
}
        """
        for uri_dimension, uri_unit, uri_derivedfrom in self.ontology.query(q):
            yield uri_dimension, uri_unit, uri_derivedfrom

    def inferPowerDimensions(self):
        '''
        @summary: extract PowerDimensions and their corresponding units from the model
        @return: a generator of the PowerDimension details: (reference, unit, derivedfrom, exponent)
        @rtype: (URIRef, URIRef, URIRef, int) 
        '''
        q = """
SELECT ?dimension ?unit ?derivedFrom ?exponent
WHERE {
 ?dimension rdfs:subClassOf unit:PowerDimension ;
            rdfs:subClassOf ?constraint1 ;
            rdfs:subClassOf ?constraint2 ;
            rdfs:subClassOf ?constraint3 .
 ?constraint1 owl:onProperty unit:defaultUnit ;
              owl:hasValue ?unit .
 ?constraint2 owl:onProperty unit:derivedFrom ;
              owl:onClass ?derivedFrom .
 ?constraint3 owl:onProperty unit:exponent ;
              owl:hasValue ?exponent .
}
        """
        for uri_dimension, uri_unit, uri_derivedfrom, exponent in self.ontology.query(q):
            yield uri_dimension, uri_unit, uri_derivedfrom, int(exponent)

    def inferProductDimensions(self):
        '''
        @summary: extract ProductDimensions and their corresponding units from the model
        @return: a generator of the ProductDimension details: (reference, unit, set of derivedfrom references)
        @rtype: (URIRef, URIRef, tuple(URIRef)) 
        '''
        q = """
SELECT ?dimension ?unit ?derivedFrom
WHERE {
 ?dimension rdfs:subClassOf unit:ProductDimension ;
            rdfs:subClassOf ?constraint1 ;
            rdfs:subClassOf ?constraint2 .
 ?constraint1 owl:onProperty unit:defaultUnit ;
              owl:hasValue ?unit .
 ?constraint2 owl:onProperty unit:derivedFrom ;
              owl:onClass ?derivedFrom .
}
        """
        container = {}
        for uri_dimension, uri_unit, uri_derivedfrom in self.ontology.query(q):
            if not container.has_key(uri_dimension):
                container[uri_dimension] = (uri_unit, set())
            container[uri_dimension][1].add(uri_derivedfrom)
        for uri_dimension, (uri_unit, set_derivedfrom) in container.iteritems():
            yield uri_dimension, uri_unit, tuple(set_derivedfrom)

    def inferRatioDimensions(self):
        '''
        @summary: extract RatioDimensions and their corresponding units from the model
        @return: a generator of the RatioDimension details: (reference, unit, derivedfrom)
        @rtype: (URIRef, URIRef, URIRef) 
        '''
        q = """
SELECT ?dimension ?unit ?derivedFrom
WHERE {
 ?dimension rdfs:subClassOf unit:RatioDimension ;
            rdfs:subClassOf ?constraint1 ;
            rdfs:subClassOf ?constraint2 .
 ?constraint1 owl:onProperty unit:defaultUnit ;
              owl:hasValue ?unit .
 ?constraint2 owl:onProperty unit:derivedFrom ;
              owl:onClass ?derivedFrom .
}
        """
        for uri_dimension, uri_unit, uri_derivedfrom in self.ontology.query(q):
            yield uri_dimension, uri_unit, uri_derivedfrom

