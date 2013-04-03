package eu.novi.im.rdfs;

import java.lang.Object;
import java.lang.String;
import java.net.URI;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://www.w3.org/2000/01/rdf-schema#Resource")
public interface Resource {
	@Iri("http://fp7-novi.eu/im.owl#hasName")
	Set<String> getHasName();
	@Iri("http://fp7-novi.eu/im.owl#hasName")
	void setHasName(Set<? extends String> hasName);

	@Iri("http://fp7-novi.eu/im.owl#id")
	Set<URI> getIds();
	@Iri("http://fp7-novi.eu/im.owl#id")
	void setIds(Set<? extends URI> ids);

	@Iri("http://www.w3.org/2002/07/owl#topDataProperty")
	Set<Object> getOwlTopDataProperty();
	@Iri("http://www.w3.org/2002/07/owl#topDataProperty")
	void setOwlTopDataProperty(Set<?> owlTopDataProperty);

}
