package eu.novi.im.policy.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.core.Platform;
import eu.novi.im.policy.Role;

public class NOVIUserImpl implements NOVIUser, RDFObject  {
	private String BelogsToDomain = "none";
	//private Set<? extends Boolean> PolicyServiceHasAuthenticationStatus;
	private Role PolicyServiceHasNoviRole;
	private Set<String> PolicyServiceHasRoleInPlatform;
	private Set<String> PublicKeys=new HashSet<String>();
	//private URI PolicyServiceHasUserID;
	private String First_Name;
	private String Last_Name;
	private Platform PolicyServiceHasUserPlatform;
	private String uri;
	private String SessionKey;
	
	public NOVIUserImpl(String uri)
	{
		this.uri = Variables.checkPolicyURI(uri);
	}
	
	@Override
	public String toString()
	{
		return uri;
	}
	

	@Override
	public Set<String> getHasRoleInPlatform() {
		return  this.PolicyServiceHasRoleInPlatform;
	}
	
	@Override
	public Set<String> getPublicKeys() {
		return  this.PublicKeys;
	}
	
	@Override
	public void setPublicKeys(Set<String> publicKeys) {
		this.PublicKeys=publicKeys;
	}
	
	@Override
	public void addPublicKey(String publicKey) {
		this.PublicKeys.add(publicKey);
	}

	@Override
	public void setHasRoleInPlatform(
			Set<String> HasRoleInPlatform) {
		this.PolicyServiceHasRoleInPlatform=HasRoleInPlatform;

	}

	@Override
	public String getFirstName() {
		return First_Name;
	}
	
	@Override
	public void setFirstName(String fName) {
		First_Name=fName;
	}
	
	@Override
	public String getLastName() {
		return Last_Name;
	}
	
	@Override
	public void setLastName(String lName) {
		Last_Name=lName;
	}

	@Override
	public ObjectConnection getObjectConnection() {
		return null;
	}

	@Override
	public Resource getResource() {
		return new URIResourceImpl(uri);
	}

	@Override
	public Role getHasNoviRole() {
		return this.PolicyServiceHasNoviRole;
	}

	@Override
	public void setHasNoviRole(Role hasNoviRole) {
		this.PolicyServiceHasNoviRole=  hasNoviRole;
		
	}

	@Override
	public String getHasSessionKey() {
		return this.SessionKey;
	}

	@Override
	public void setHasSessionKey(String hasSessionKey) {
		SessionKey=hasSessionKey;
		
	}

	@Override
	public String getBelogsToDomain() {
		return this.BelogsToDomain;
	}

	@Override
	public void setBelongsToDomain(String BelogsToDomain) {
		this.BelogsToDomain=BelogsToDomain;
		
	}
	
	//@Override
	//public URI getHasUserID() {
	//	return PolicyServiceHasUserID;
	//}

	//@Override
	//public void setHasUserID(URI hasUserID) {
	//	PolicyServiceHasUserID= hasUserID;
	//	
	//}

	@Override
	public Platform getHasUserPlatform() {
		return PolicyServiceHasUserPlatform;
	}

	@Override
	public void setHasUserPlatform(Platform hasUserPlatform) {
		 PolicyServiceHasUserPlatform = hasUserPlatform;
	}

//	@Override
	///public void setHasNoviRole(Role hasNoviRole) {
	//	this.PolicyServiceHasNoviRole=  (RoleImpl) hasNoviRole;
		// TODO Auto-generated method stub
		
//	}

}
