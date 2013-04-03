package eu.novi.im.policy;

import eu.novi.im.owl.Thing;
import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyEvent")
public interface PolicyEvent extends Thing {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasEvent")
	Set<ManagedEntity> getHasEvent();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasEvent")
	void setHasEvent(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
Set<? extends ManagedEntity> hasEvent);

}
