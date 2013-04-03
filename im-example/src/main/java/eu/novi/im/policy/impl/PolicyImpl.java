package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.Policy;
import eu.novi.im.policy.PolicyAction;
import eu.novi.im.policy.PolicyCondition;
import eu.novi.im.policy.PolicyEvent;

public class PolicyImpl extends ManagedEntityImpl implements Policy {
	private Set<? extends PolicyAction> PolicyServiceHasPolicyAction;
	private Set<? extends PolicyCondition> PolicyServiceHasPolicyCondition;
	
	
	public PolicyImpl(String uri)
	{
		super(uri);
	}


	@Override
	public Set<ManagedEntity> getHasAttached() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setHasAttached(Set<? extends ManagedEntity> hasAttached) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Set<Boolean> getIsEnabled() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setIsEnabled(Set<? extends Boolean> isEnabled) {
		// TODO Auto-generated method stub
		
	}
	
	

}
