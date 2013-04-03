package eu.novi.im.policy.impl;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.ManagedEntityMethod;
import eu.novi.im.policy.ManagedEntityProperty;

public class ManagedEntityImpl implements ManagedEntity, RDFObject  {
	private Set<? extends ManagedEntityMethod> PolicyServiceMethods;
	private Set<? extends ManagedEntityProperty> PolicyServiceProperties;
	private String uri;
	
	public ManagedEntityImpl(String uri)
	{
		this.uri = Variables.checkPolicyURI(uri);
	}
	
	@Override
	public String toString()
	{
		return uri;
	}
	
	@Override
	public Set<ManagedEntityMethod> getHasMethods() {
		return (Set<ManagedEntityMethod>) this.PolicyServiceMethods;
	}

	@Override
	public void setHasMethods(
			Set<? extends ManagedEntityMethod> HasMethods) {
		this.PolicyServiceMethods=HasMethods;

	}

	@Override
	public Set<ManagedEntityProperty> getHasProperties() {
		return (Set<ManagedEntityProperty>) this.PolicyServiceProperties;
	}

	@Override
	public void setHasProperties(
			Set<? extends ManagedEntityProperty> HasProperties) {
		this.PolicyServiceProperties=HasProperties;

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
