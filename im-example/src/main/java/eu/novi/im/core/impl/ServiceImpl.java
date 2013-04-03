package eu.novi.im.core.impl;

import eu.novi.im.core.Location;
import eu.novi.im.core.Service;
import eu.novi.im.policy.impl.ManagedEntityImpl;

import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

public class ServiceImpl extends ManagedEntityImpl implements Service {

	private String URI;
	private Location locatedAt;
	
	
	public ServiceImpl(String uri)
	{
		super(uri);
		this.URI = Variables.checkURI(uri);;
	}
	
	
	@Override
	public Location getLocatedAt() {
		return locatedAt;
	}


	@Override
	public void setLocatedAt(Location locatedAt) {
		this.locatedAt = locatedAt;
	}

	@Override
	public String toString() {
		return URI;
	}
	
	@Override
	public URIResourceImpl getResource()
	{
		return new URIResourceImpl(URI);
	}
	
	public void setResource(URIResourceImpl uriImpl) {}

	@Override
	public ObjectConnection getObjectConnection() {
		return null;
	}
	
	


}
