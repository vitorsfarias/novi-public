package eu.novi.im.core.impl;

import java.util.Set;

import eu.novi.im.core.NetworkElement;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Path;

public class NetworkElementImpl extends ResourceImpl implements NetworkElement {

	private Set<? extends Path> inPaths;
	private Set<? extends NetworkElementOrNode> nexts;
	private Float hasAvailableCapacity;
	private Float hasCapacity;
	
	public NetworkElementImpl(String uri)
	{
		super(uri);
	}
	
	
	@Override
	public Set<Path> getInPaths() {
		return (Set<Path>) this.inPaths;
	}

	@Override
	public void setInPaths(Set<? extends Path> inPaths) {
		this.inPaths = inPaths;

	}

	@Override
	public Set<NetworkElementOrNode> getNexts() {
		return (Set<NetworkElementOrNode>) this.nexts;
	}

	@Override
	public void setNexts(Set<? extends NetworkElementOrNode> nexts) {
		this.nexts = nexts;

	}

	@Override
	public Float getHasAvailableCapacity() {
		return this.hasAvailableCapacity;
	}

	@Override
	public void setHasAvailableCapacity(Float hasAvailableCapacity) {

		this.hasAvailableCapacity = hasAvailableCapacity;

	}

	@Override
	public Float getHasCapacity() {
		return this.hasCapacity;
	}

	@Override
	public void setHasCapacity(Float hasCapacity) {

		this.hasCapacity = hasCapacity;
	}

}
