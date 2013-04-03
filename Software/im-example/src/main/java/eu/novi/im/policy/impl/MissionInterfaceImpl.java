package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.MissionInterface;
import eu.novi.im.policy.PolicyEvent;

public class MissionInterfaceImpl extends ManagedEntityImpl implements
		MissionInterface {
	
	public MissionInterfaceImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<PolicyEvent> getAcceptsEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAcceptsEvent(Set<? extends PolicyEvent> acceptsEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<PolicyEvent> getProvidesEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProvidesEvent(Set<? extends PolicyEvent> providesEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<PolicyEvent> getRaiseEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRaiseEvent(Set<? extends PolicyEvent> raiseEvent) {
		// TODO Auto-generated method stub

	}

}
