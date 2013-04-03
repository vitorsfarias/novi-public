package eu.novi.im.policy.impl;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.ManagedEntityMethod;
import eu.novi.im.policy.PolicyAction;

public class PolicyActionImpl implements PolicyAction, RDFObject {
	private Set<?> PolicyServiceHasAction;
	private String uri;
	
	public PolicyActionImpl(String uri)
	{
		this.uri = Variables.checkPolicyURI(uri);
	}
	
	@Override
	public String toString()
	{
		return uri;
	}
	@Override
	public Set<Object> getHasAction() {
		// TODO Auto-generated method stub
		return (Set<Object>) this.PolicyServiceHasAction;
	}

	@Override
	public void setHasAction(Set<?> HasAction) {
		this.PolicyServiceHasAction=HasAction;

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
