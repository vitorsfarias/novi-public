package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.owl.Thing;
import eu.novi.im.owl.unionOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({Link.class, Path.class})
public interface LinkOrPath extends Thing, NetworkElement, ManagedEntity, Resource, GroupOrResource {
	@Iri("http://fp7-novi.eu/im.owl#hasSink")
	Set<Interface> getHasSink();
	@Iri("http://fp7-novi.eu/im.owl#hasSink")
	void setHasSink(Set<? extends Interface> hasSink);

	@Iri("http://fp7-novi.eu/im.owl#hasSource")
	Set<Interface> getHasSource();
	@Iri("http://fp7-novi.eu/im.owl#hasSource")
	void setHasSource(Set<? extends Interface> hasSource);

}
