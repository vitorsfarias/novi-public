package eu.novi.im.policy.impl;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.ManagedEntityProperty;
import eu.novi.im.policy.PolicyCondition;

public class PolicyConditionImpl implements PolicyCondition, RDFObject {
	
	
	private Set<? extends ManagedEntityProperty> PolicyServiceHasCondition;
	private String uri;
	
	public PolicyConditionImpl(String uri)
	{
		this.uri = Variables.checkPolicyURI(uri);
	}
	
	@Override
	public String toString()
	{
		return uri;
		
	}
	
	@Override
	public Set<ManagedEntityProperty> getHasCondition() {
		// TODO Auto-generated method stub
		return (Set<ManagedEntityProperty>) this.PolicyServiceHasCondition;
	}

	@Override
	public void setHasCondition(
			Set<? extends ManagedEntityProperty> HasCondition) {
		this.PolicyServiceHasCondition=HasCondition;

	}

	@Override
	public ObjectConnection getObjectConnection() {
		return null;
	}

	@Override
	public Resource getResource() {
		return new URIResourceImpl(uri);
	}

}
