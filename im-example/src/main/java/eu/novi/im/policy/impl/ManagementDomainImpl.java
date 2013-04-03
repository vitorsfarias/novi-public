package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.ManagementDomain;

public class ManagementDomainImpl extends ManagedEntityImpl implements
		ManagementDomain {
	private Set<? extends ManagedEntity> PolicyServiceIncludes;
	
	public ManagementDomainImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<ManagedEntity> getIncludes() {
		return (Set<ManagedEntity>) this.PolicyServiceIncludes;
	}

	@Override
	public void setIncludes(
			Set<? extends ManagedEntity> NOVIPolicyServiceIncludes) {
		this.PolicyServiceIncludes = NOVIPolicyServiceIncludes;
	}

}
