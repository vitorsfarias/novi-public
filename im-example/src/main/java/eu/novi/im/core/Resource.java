package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import java.lang.Boolean;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Resource")
public interface Resource extends ManagedEntity, GroupOrResource {
	@Iri("http://fp7-novi.eu/im.owl#exclusive")
	Boolean getExclusive();
	@Iri("http://fp7-novi.eu/im.owl#exclusive")
	void setExclusive(Boolean exclusive);

	@Iri("http://fp7-novi.eu/im.owl#isContainedIn")
	Set<Group> getIsContainedIn();
	@Iri("http://fp7-novi.eu/im.owl#isContainedIn")
	void setIsContainedIn(Set<? extends Group> isContainedIn);

}
