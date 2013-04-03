package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicy")
public interface MissionPolicy extends Policy {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasInterface")
	Set<MissionInterface> getHasInterface();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasInterface")
	void setHasInterface(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
Set<? extends MissionInterface> hasInterface);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicy")
	Set<ECAPolicy> getHasPolicy();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicy")
	void setHasPolicy(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
Set<? extends ECAPolicy> hasPolicy);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onStart")
	Set<PolicyAction> getOnStart();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onStart")
	void setOnStart(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
Set<? extends PolicyAction> onStart);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onStop")
	Set<PolicyAction> getOnStop();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onStop")
	void setOnStop(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicyProperties")
Set<? extends PolicyAction> onStop);

}
