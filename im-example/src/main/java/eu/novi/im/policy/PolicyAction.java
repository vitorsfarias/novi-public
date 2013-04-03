package eu.novi.im.policy;

import eu.novi.im.owl.Thing;
import eu.novi.im.rdfs.subPropertyOf;
import java.lang.Object;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyAction")
public interface PolicyAction extends Thing {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasAction")
	Set<Object> getHasAction();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasAction")
	void setHasAction(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyActionProperties")
Set<?> hasAction);

}
