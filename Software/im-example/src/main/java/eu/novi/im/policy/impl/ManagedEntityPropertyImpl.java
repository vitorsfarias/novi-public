package eu.novi.im.policy.impl;


import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.policy.ManagedEntityProperty;

public class ManagedEntityPropertyImpl implements ManagedEntityProperty, RDFObject {
	private String uri;

	public ManagedEntityPropertyImpl(String uri)
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

}
