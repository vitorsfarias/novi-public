package eu.novi.im.unit;

import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/unit.owl#SimpleUnit")
public interface SimpleUnit extends DerivedUnit {
	@Iri("http://fp7-novi.eu/unit.owl#derivedFrom")
	Unit getDerivedFrom();
	@Iri("http://fp7-novi.eu/unit.owl#derivedFrom")
	void setDerivedFrom(Unit derivedFrom);

}
