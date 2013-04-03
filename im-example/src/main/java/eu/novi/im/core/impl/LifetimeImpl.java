package eu.novi.im.core.impl;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.Lifetime;

public class LifetimeImpl implements Lifetime, RDFObject {
	private String uri;
	private XMLGregorianCalendar endTime;
	private XMLGregorianCalendar startTime;

	public LifetimeImpl(String uri)
	{
		this.uri = Variables.checkURI(uri);;
	}
	
	
	@Override
	public XMLGregorianCalendar getEndTime() {
		return this.endTime;
	}

	@Override
	public void setEndTime(XMLGregorianCalendar endTime) {
		this.endTime = endTime;

	}

	@Override
	public XMLGregorianCalendar getStartTime() {
		return this.startTime;
	}

	@Override
	public void setStartTime(XMLGregorianCalendar startTime) {
		this.startTime = startTime;

	}
	
	@Override
	public String toString() {
		return uri;
	}

	public void copy(Lifetime lt) {
		this.endTime = lt.getEndTime();
		this.startTime = lt.getStartTime();
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
