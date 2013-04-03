package eu.novi.im.core.impl;

import eu.novi.im.core.Storage;

public class StorageImpl extends NodeComponentImpl implements Storage {

	private Float hasAvailableStorageSize;
	private Float hasStorageSize;
	
	
	public StorageImpl(String uri)
	{
		super(uri);
	}
	
	
	@Override
	public Float getHasAvailableStorageSize() {
		return hasAvailableStorageSize;
	}
	
	@Override
	public void setHasAvailableStorageSize(Float hasAvailableStorageSize) {
		this.hasAvailableStorageSize = hasAvailableStorageSize;
	}
	
	@Override
	public Float getHasStorageSize() {
		return hasStorageSize;
	}
	
	@Override
	public void setHasStorageSize(Float hasStorageSize) {
		this.hasStorageSize = hasStorageSize;
	}
	
	

}
