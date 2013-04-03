package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.ECAPolicy;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.MissionInterface;
import eu.novi.im.policy.MissionPolicy;
import eu.novi.im.policy.PolicyAction;

public class MissionPolicyImpl extends PolicyImpl implements MissionPolicy {
	
	public MissionPolicyImpl(String uri)
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

	@Override
	public Set<MissionInterface> getHasInterface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasInterface(Set<? extends MissionInterface> hasInterface) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ECAPolicy> getHasPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicy(Set<? extends ECAPolicy> hasPolicy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<PolicyAction> getOnStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOnStart(Set<? extends PolicyAction> onStart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<PolicyAction> getOnStop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOnStop(Set<? extends PolicyAction> onStop) {
		// TODO Auto-generated method stub
		
	}

}
