package eu.novi.im.unit.impl;


import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.impl.URIResourceImpl;
import eu.novi.im.core.impl.Variables;
import eu.novi.im.unit.IPAddress;

public class IPAddressImpl implements IPAddress, RDFObject {
	
	private String uri;
	private String hasValue;
	
	
	public IPAddressImpl(String uri)
	{
		this.uri = Variables.checkUnitURI(uri);;
	}

	@Override
	public String getHasValue() {
		return this.hasValue;
	}

	@Override
	public void setHasValue(String hasValue) {
		this.hasValue = hasValue;

	}
	
	@Override
	public String toString() {
		return uri;
	}
	
	
	@Override
	public ObjectConnection getObjectConnection() {
		
		return null;
	}


	@Override
	public URIResourceImpl getResource() {
		return new URIResourceImpl(uri);
	}
	
	public void setResource(URIResourceImpl uriImpl) {}

}
