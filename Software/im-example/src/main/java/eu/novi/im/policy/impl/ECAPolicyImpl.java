package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.ECAPolicy;
import eu.novi.im.policy.PolicyAction;
import eu.novi.im.policy.PolicyCondition;
import eu.novi.im.policy.PolicyEvent;

public class ECAPolicyImpl extends PolicyImpl implements ECAPolicy {
	
	public ECAPolicyImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<PolicyAction> getHasPolicyAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicyAction(Set<? extends PolicyAction> hasPolicyAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<PolicyCondition> getHasPolicyCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicyCondition(
			Set<? extends PolicyCondition> hasPolicyCondition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<PolicyEvent> getHasPolicyEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicyEvent(Set<? extends PolicyEvent> hasPolicyEvent) {
		// TODO Auto-generated method stub
		
	}

}
