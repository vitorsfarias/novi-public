package eu.novi.im.core.impl;

import eu.novi.im.core.Location;

import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

public class LocationImpl implements Location, RDFObject {
	
	private String uri;
	private Float latitude;
	private Float longitude;
	
	public LocationImpl(String uri)
	{
		this.uri = Variables.checkURI(uri);;
	}

	@Override
	public Float getLatitude() {
		return this.latitude;
	}

	@Override
	public void setLatitude(Float latitude) {
		this.latitude = latitude;

	}

	@Override
	public Float getLongitude() {
		return this.longitude;
	}

	@Override
	public void setLongitude(Float longitude) {
		this.longitude = longitude;

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
