package eu.novi.im.core.impl;

import java.util.Set;

import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Path;

public class NSwitchImpl extends LinkImpl implements NSwitch {
	private Set<String> hasGRETunnelID;
	private Set<String> hasPrivateSinkAddress;
	private Set<String> hasPrivateSourceAddress;
	private Set<String> hasVLANID;
	private Set<String> hasVXLANID;
	
	public NSwitchImpl(String uri)
	{
		super(uri);
	}

	@Override
	public Set<String> getHasGRETunnelID() {
		return this.hasGRETunnelID;
	}

	@Override
	public void setHasGRETunnelID(Set<String> hasGRETunnelID) {
		this.hasGRETunnelID = hasGRETunnelID;
		
	}

	@Override
	public Set<String> getHasPrivateSinkAddress() {
		return this.hasPrivateSinkAddress;
	}

	@Override
	public void setHasPrivateSinkAddress(
			Set<String> hasPrivateSinkAddress) {
		this.hasPrivateSinkAddress = hasPrivateSinkAddress;
		
	}

	@Override
	public Set<String> getHasPrivateSourceAddress() {
		return this.hasPrivateSourceAddress;
	}

	@Override
	public void setHasPrivateSourceAddress(
			Set<String> hasPrivateSourceAddress) {
		this.hasPrivateSourceAddress = hasPrivateSourceAddress;
		
	}

	@Override
	public Set<String> getHasVLANID() {
		return this.hasVLANID;
	}

	@Override
	public void setHasVLANID(Set<String> hasVLANID) {
		this.hasVLANID = hasVLANID;
		
	}

	@Override
	public Set<String> getHasVXLANID() {
		return this.hasVXLANID;
	}

	@Override
	public void setHasVXLANID(Set<String> hasVXLANID) {
		this.hasVXLANID = hasVXLANID;
		
	}

	
}
