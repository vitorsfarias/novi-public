package eu.novi.im.policy.impl;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.PolicyEvent;

public class PolicyEventImpl implements PolicyEvent, RDFObject {
	private Set<?> PolicyServiceHasEvent;
	private String uri;
	
	
	public PolicyEventImpl(String uri)
	{
		this.uri = Variables.checkPolicyURI(uri);
	}
	
	@Override
	public String toString()
	{
		return uri;
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
	public void setHasEvent(Set<? extends ManagedEntity> hasEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ManagedEntity> getHasEvent() {
		// TODO Auto-generated method stub
		return null;
	}

}
