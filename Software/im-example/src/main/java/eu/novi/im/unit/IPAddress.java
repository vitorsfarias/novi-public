package eu.novi.im.unit;

import java.lang.String;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/unit.owl#IPAddress")
public interface IPAddress {
	@Iri("http://fp7-novi.eu/unit.owl#hasValue")
	String getHasValue();
	@Iri("http://fp7-novi.eu/unit.owl#hasValue")
	void setHasValue(String hasValue);

}
