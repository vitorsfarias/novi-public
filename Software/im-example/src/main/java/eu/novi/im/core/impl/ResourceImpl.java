package eu.novi.im.core.impl;

import java.util.Set;

import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.Group;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Resource;
import eu.novi.im.policy.impl.ManagedEntityImpl;


public class ResourceImpl extends ManagedEntityImpl implements Resource {

	private String URI;
	private Set<Lifetime> hasLifetimes;
	private Boolean exclusive;
	Set<? extends Group> isContainedIn;
	
	public ResourceImpl(String uri)
	{
		super(uri);
		this.URI = Variables.checkURI(uri);
		
	}
	
	
	public Resource copy(Resource toCopy) {
		this.exclusive = toCopy.getExclusive();
		this.hasLifetimes = toCopy.getHasLifetimes();
		this.isContainedIn = toCopy.getIsContainedIn();
		return this;
	}
	
	@Override
	public Set<Lifetime> getHasLifetimes() {
		return hasLifetimes;
	}

	@Override
	public void setHasLifetimes(Set<? extends Lifetime> hasLifetimes) {
		this.hasLifetimes = (Set<Lifetime>) hasLifetimes;

	}

	@Override
	public Boolean getExclusive() {
		return this.exclusive;
	}

	@Override
	public void setExclusive(Boolean exclusive) {
		this.exclusive = exclusive;

	}

	@Override
	public Set<Group> getIsContainedIn() {
		return (Set<Group>) isContainedIn;
	}

	@Override
	public void setIsContainedIn(Set<? extends Group> isContainedIn) {
		this.isContainedIn = isContainedIn;

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
