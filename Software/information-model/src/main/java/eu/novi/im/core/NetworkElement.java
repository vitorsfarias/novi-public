package eu.novi.im.core;

import java.lang.Float;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#NetworkElement")
public interface NetworkElement extends NetworkElementOrNode, Resource {
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableCapacity")
	Float getHasAvailableCapacity();
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableCapacity")
	void setHasAvailableCapacity(Float hasAvailableCapacity);

	@Iri("http://fp7-novi.eu/im.owl#hasCapacity")
	Float getHasCapacity();
	@Iri("http://fp7-novi.eu/im.owl#hasCapacity")
	void setHasCapacity(Float hasCapacity);

}
