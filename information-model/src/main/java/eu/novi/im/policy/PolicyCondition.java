package eu.novi.im.policy;

import eu.novi.im.owl.Thing;
import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyCondition")
public interface PolicyCondition extends Thing {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasCondition")
	Set<ManagedEntityProperty> getHasCondition();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasCondition")
	void setHasCondition(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
Set<? extends ManagedEntityProperty> hasCondition);

}
