package eu.novi.im.core.impl;

import java.util.Set;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;

public class PathImpl extends NetworkElementImpl implements Path {

	private Set<? extends Resource> contains;
	private Set<? extends Interface> hasSink;
	private Set<? extends Interface> hasSource;
	
	public PathImpl(String uri)
	{
		super(uri);
	}
	
	@Override
	public Set<Resource> getContains() {
		return (Set<Resource>) this.contains;
	}

	@Override
	public void setContains(Set<? extends Resource> contains) {
		this.contains = contains;

	}

	@Override
	public Set<Interface> getHasSink() {
		return (Set<Interface>) this.hasSink;
	}

	@Override
	public void setHasSink(Set<? extends Interface> hasSink) {
		this.hasSink = hasSink;
		
	}

	@Override
	public Set<Interface> getHasSource() {
		return (Set<Interface>) this.hasSource;
	}

	@Override
	public void setHasSource(Set<? extends Interface> hasSource) {
		this.hasSource = hasSource;
		
	}

}
