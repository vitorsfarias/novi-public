package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicy")
public interface ECAPolicy extends Policy {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyAction")
	Set<PolicyAction> getHasPolicyAction();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyAction")
	void setHasPolicyAction(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
Set<? extends PolicyAction> hasPolicyAction);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyCondition")
	Set<PolicyCondition> getHasPolicyCondition();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyCondition")
	void setHasPolicyCondition(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
Set<? extends PolicyCondition> hasPolicyCondition);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyEvent")
	Set<PolicyEvent> getHasPolicyEvent();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyEvent")
	void setHasPolicyEvent(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicyObjectProperties")
Set<? extends PolicyEvent> hasPolicyEvent);

}
