package eu.novi.im.core;

import javax.xml.datatype.XMLGregorianCalendar;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Lifetime")
public interface Lifetime {
	@Iri("http://fp7-novi.eu/im.owl#endTime")
	XMLGregorianCalendar getEndTime();
	@Iri("http://fp7-novi.eu/im.owl#endTime")
	void setEndTime(XMLGregorianCalendar endTime);

	@Iri("http://fp7-novi.eu/im.owl#startTime")
	XMLGregorianCalendar getStartTime();
	@Iri("http://fp7-novi.eu/im.owl#startTime")
	void setStartTime(XMLGregorianCalendar startTime);

}
