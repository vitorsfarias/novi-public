package eu.novi.im.core;

import java.lang.Float;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Location")
public interface Location {
	@Iri("http://fp7-novi.eu/im.owl#latitude")
	Float getLatitude();
	@Iri("http://fp7-novi.eu/im.owl#latitude")
	void setLatitude(Float latitude);

	@Iri("http://fp7-novi.eu/im.owl#longitude")
	Float getLongitude();
	@Iri("http://fp7-novi.eu/im.owl#longitude")
	void setLongitude(Float longitude);

}
