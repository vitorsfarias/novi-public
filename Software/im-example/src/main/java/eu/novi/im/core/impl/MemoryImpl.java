package eu.novi.im.core.impl;

import eu.novi.im.core.Memory;

public class MemoryImpl extends NodeComponentImpl implements Memory {

	private Float hasAvailableMemorySize;
	private Float hasMemorySize;
	
	
	public MemoryImpl(String uri)
	{
		super(uri);
	}
	
	
	@Override
	public Float getHasAvailableMemorySize() {
		return this.hasAvailableMemorySize;
	}

	@Override
	public void setHasAvailableMemorySize(Float hasAvailableMemorySize) {
		this.hasAvailableMemorySize = hasAvailableMemorySize;

	}

	@Override
	public Float getHasMemorySize() {
		return this.hasMemorySize;
	}

	@Override
	public void setHasMemorySize(Float hasMemorySize) {
		this.hasMemorySize = hasMemorySize;

	}

}
