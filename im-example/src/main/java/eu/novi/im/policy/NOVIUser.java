package eu.novi.im.policy;

import eu.novi.im.owl.Thing;
import eu.novi.im.policy.impl.RoleImpl;
import eu.novi.im.rdfs.subPropertyOf;
import eu.novi.im.core.Platform;
import java.lang.String;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUser")
public interface NOVIUser extends Thing {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasNoviRole")
	Role getHasNoviRole();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasNoviRole")
	void setHasNoviRole(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
Role hasNoviRole);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasRoleInPlatform")
	Set<String> getHasRoleInPlatform();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasRoleInPlatform")
	void setHasRoleInPlatform(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
Set<String> hasRoleInPlatform);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasSessionKey")
	String getHasSessionKey();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasSessionKey")
	void setHasSessionKey(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
String hasSessionKey);

	//@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	//@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasUserID")
	//String getHasUserID();
	//@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
	//@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasUserID")
	//void setHasUserID(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#RoleProperties")
//String hasUserID);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasUserPlatform")
	Platform getHasUserPlatform();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasUserPlatform")
	void setHasUserPlatform(@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUserProperties")
			Platform hasUserPlatform);
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#firstName")
	String getFirstName();
	
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#firstName")
	void setFirstName(String fName);
	
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#lastName")
	String getLastName();
	
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#lastName")
	void setLastName(String lName);
	
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#publicKeys")
	Set<String> getPublicKeys();
	
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#publicKeys")
	void setPublicKeys(Set<String> publicKeys);
	
	//@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#publicKeys")
	void addPublicKey(String publicKey);
	String getBelogsToDomain();
	void setBelongsToDomain(String BelogsToDomain);
	//void setHasNoviRole(Role hasNoviRole);

}
