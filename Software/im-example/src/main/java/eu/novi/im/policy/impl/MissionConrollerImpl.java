package eu.novi.im.policy.impl;

import java.util.Set;

import eu.novi.im.policy.MissionConroller;
import eu.novi.im.policy.MissionPolicy;

public class MissionConrollerImpl extends ManagedEntityImpl implements
		MissionConroller {
	
	public MissionConrollerImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<MissionPolicy> getHasLoaded() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasLoaded(Set<? extends MissionPolicy> hasLoaded) {
		// TODO Auto-generated method stub

	}

}
