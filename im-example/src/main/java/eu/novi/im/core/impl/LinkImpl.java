package eu.novi.im.core.impl;

import java.util.Set;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;

public class LinkImpl extends NetworkElementImpl implements Link {

	private Set<LinkOrPath> provisionedBy;
	private Set<? extends Interface> hasSink;
	private Set<? extends Interface> hasSource;

	public LinkImpl(String uri)
	{
		super(uri);
	}
	
	public Link copy(Link toCopy) {
		this.provisionedBy = toCopy.getProvisionedBy();
		this.setExclusive(toCopy.getExclusive());
		this.setHasLifetimes(toCopy.getHasLifetimes());
		this.setIsContainedIn(toCopy.getIsContainedIn());
		return this;
	}
	
	@Override
	public Set<LinkOrPath> getProvisionedBy() {
		return  this.provisionedBy;
	}
	
	@Override
	public void setProvisionedBy(Set<? extends LinkOrPath> provisionedBy) {
		this.provisionedBy = (Set<LinkOrPath>) provisionedBy;
		
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
