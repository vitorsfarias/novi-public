package eu.novi.im.core;

import eu.novi.im.rdfs.subPropertyOf;

import java.lang.Float;
import java.math.BigInteger;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#CPU")
public interface CPU extends NodeComponent {
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableCores")
	BigInteger getHasAvailableCores();
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableCores")
	void setHasAvailableCores(BigInteger hasAvailableCores);

	@Iri("http://fp7-novi.eu/im.owl#hasCPUSpeed")
	Float getHasCPUSpeed();
	@Iri("http://fp7-novi.eu/im.owl#hasCPUSpeed")
	void setHasCPUSpeed(Float hasCPUSpeed);

	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://fp7-novi.eu/im.owl#hasCores")
	BigInteger getHasCores();
	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://fp7-novi.eu/im.owl#hasCores")
	void setHasCores(	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
BigInteger hasCores);

}
