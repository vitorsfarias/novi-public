package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.owl.Thing;
import eu.novi.im.owl.unionOf;
import org.openrdf.annotations.Iri;

@unionOf({Service.class, Node.class})
public interface NodeOrService extends Thing, ManagedEntity {
	@Iri("http://fp7-novi.eu/im.owl#locatedAt")
	Location getLocatedAt();
	@Iri("http://fp7-novi.eu/im.owl#locatedAt")
	void setLocatedAt(Location locatedAt);

}
