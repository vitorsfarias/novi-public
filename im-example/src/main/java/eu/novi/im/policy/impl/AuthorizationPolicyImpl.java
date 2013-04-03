package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.AuthorizationPolicy;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.ManagedEntityMethod;

public class AuthorizationPolicyImpl extends PolicyImpl implements
		AuthorizationPolicy {
	
	public AuthorizationPolicyImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<Boolean> getEnforceOnReply() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnforceOnReply(Set<? extends Boolean> enforceOnReply) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Boolean> getEnforceOnRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnforceOnRequest(Set<? extends Boolean> enforceOnRequest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Boolean> getEnforceOnSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnforceOnSubject(Set<? extends Boolean> enforceOnSubject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Boolean> getEnforceOnTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnforceOnTarget(Set<? extends Boolean> enforceOnTarget) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ManagedEntity> getHasPolicySubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicySubject(
			Set<? extends ManagedEntity> hasPolicySubject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ManagedEntity> getHasPolicyTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasPolicyTarget(Set<? extends ManagedEntity> hasPolicyTarget) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ManagedEntityMethod> getOnMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOnMethod(Set<? extends ManagedEntityMethod> onMethod) {
		// TODO Auto-generated method stub
		
	}

}
