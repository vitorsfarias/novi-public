package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.owl.Thing;
import eu.novi.im.owl.unionOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({NetworkElement.class, Node.class})
public interface NetworkElementOrNode extends Thing, ManagedEntity, Resource, GroupOrResource {
	/** Should be reconsidered. */
	@Iri("http://fp7-novi.eu/im.owl#inPath")
	Set<Path> getInPaths();
	/** Should be reconsidered. */
	@Iri("http://fp7-novi.eu/im.owl#inPath")
	void setInPaths(Set<? extends Path> inPaths);

	@Iri("http://fp7-novi.eu/im.owl#next")
	Set<NetworkElementOrNode> getNexts();
	@Iri("http://fp7-novi.eu/im.owl#next")
	void setNexts(Set<? extends NetworkElementOrNode> nexts);

}
