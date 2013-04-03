package eu.novi.im.core.impl;

import java.net.URI;

import eu.novi.im.core.VirtualNode;

public class VirtualNodeImpl extends NodeImpl implements VirtualNode {
	
	private URI diskImage;
	private String hasOS;
	private String hasVendor;
	private String hasVirtualizationEnvironment;
	private String virtualRole;
	
	public VirtualNodeImpl(String uri) {
		super(uri);
	}
	
	public VirtualNode copy(VirtualNode toCopy) {
		super.copy(toCopy);
		this.diskImage = toCopy.getDiskImage();
		this.hasOS = toCopy.getHasOS();
		this.hasVendor = toCopy.getHasVendor();
		this.hasVirtualizationEnvironment = toCopy.getHasVirtualizationEnvironment();
		this.virtualRole = toCopy.getVirtualRole();
		return this;
	}

	@Override
	public URI getDiskImage() {
		return diskImage;
	}

	@Override
	public void setDiskImage(URI diskImage) {
		this.diskImage = diskImage;

	}

	@Override
	public String getHasOS() {
		return hasOS;
	}

	@Override
	public void setHasOS(String hasOS) {
		this.hasOS = hasOS;

	}

	@Override
	public String getHasVendor() {
		return this.hasVendor;
	}

	@Override
	public void setHasVendor(String hasVendor) {
		this.hasVendor = hasVendor;

	}

	@Override
	public String getHasVirtualizationEnvironment() {
		return this.hasVirtualizationEnvironment;
	}

	@Override
	public void setHasVirtualizationEnvironment(
			String hasVirtualizationEnvironment) {
		this.hasVirtualizationEnvironment = hasVirtualizationEnvironment;

	}

	@Override
	public String getVirtualRole() {
		return this.virtualRole;
	}

	@Override
	public void setVirtualRole(String virtualRole) {
		this.virtualRole = virtualRole;

	}

	

}
