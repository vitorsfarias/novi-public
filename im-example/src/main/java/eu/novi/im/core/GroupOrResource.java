package eu.novi.im.core;

import eu.novi.im.owl.unionOf;

import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({Group.class, Resource.class})
public interface GroupOrResource {
	@Iri("http://fp7-novi.eu/im.owl#hasLifetime")
	Set<Lifetime> getHasLifetimes();
	@Iri("http://fp7-novi.eu/im.owl#hasLifetime")
	void setHasLifetimes(Set<? extends Lifetime> hasLifetimes);

}
