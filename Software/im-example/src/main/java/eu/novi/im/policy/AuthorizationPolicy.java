package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.lang.Boolean;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicy")
public interface AuthorizationPolicy extends Policy {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnReply")
	Set<Boolean> getEnforceOnReply();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnReply")
	void setEnforceOnReply(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
Set<? extends Boolean> enforceOnReply);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnRequest")
	Set<Boolean> getEnforceOnRequest();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnRequest")
	void setEnforceOnRequest(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#isEnforcedOn")
Set<? extends Boolean> enforceOnRequest);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnSubject")
	Set<Boolean> getEnforceOnSubject();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnSubject")
	void setEnforceOnSubject(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
Set<? extends Boolean> enforceOnSubject);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnTarget")
	Set<Boolean> getEnforceOnTarget();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#enforceOnTarget")
	void setEnforceOnTarget(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#hasFocus")
Set<? extends Boolean> enforceOnTarget);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicySubject")
	Set<ManagedEntity> getHasPolicySubject();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicySubject")
	void setHasPolicySubject(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
Set<? extends ManagedEntity> hasPolicySubject);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyTarget")
	Set<ManagedEntity> getHasPolicyTarget();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasPolicyTarget")
	void setHasPolicyTarget(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
Set<? extends ManagedEntity> hasPolicyTarget);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onMethod")
	Set<ManagedEntityMethod> getOnMethod();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#onMethod")
	void setOnMethod(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicyObjectProperties")
Set<? extends ManagedEntityMethod> onMethod);

}
