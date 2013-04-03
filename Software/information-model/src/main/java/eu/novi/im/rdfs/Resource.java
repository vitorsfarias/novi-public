package eu.novi.im.rdfs;


import java.lang.Object;
import java.lang.String;
import java.net.URI;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://www.w3.org/2000/01/rdf-schema#Resource")
public interface Resource {
	@Iri("http://fp7-novi.eu/im.owl#hasName")
	String getHasName();
	@Iri("http://fp7-novi.eu/im.owl#hasName")
	void setHasName(String hasName);

	@Iri("http://fp7-novi.eu/im.owl#id")
	URI getId();
	@Iri("http://fp7-novi.eu/im.owl#id")
	void setId(URI id);

	@Iri("http://fp7-novi.eu/unit.owl#derivedFrom")
	Set<Object> getDerivedFrom();
	@Iri("http://fp7-novi.eu/unit.owl#derivedFrom")
	void setDerivedFrom(Set<?> derivedFrom);


	@Iri("http://fp7-novi.eu/unit.owl#hasValue")
	Set<Object> getHasValue();
	@Iri("http://fp7-novi.eu/unit.owl#hasValue")
	void setHasValue(Set<?> hasValue);

	@Iri("http://www.w3.org/2002/07/owl#topDataProperty")
	Set<Object> getTopDataProperty();
	@Iri("http://www.w3.org/2002/07/owl#topDataProperty")
	void setTopDataProperty(Set<?> topDataProperty);

}
