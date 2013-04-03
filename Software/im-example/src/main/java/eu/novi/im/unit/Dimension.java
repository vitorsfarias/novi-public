package eu.novi.im.unit;

import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/unit.owl#Dimension")
public interface Dimension {
	@Iri("http://fp7-novi.eu/unit.owl#hasUnit")
	Unit getHasUnit();
	@Iri("http://fp7-novi.eu/unit.owl#hasUnit")
	void setHasUnit(Unit hasUnit);

}
