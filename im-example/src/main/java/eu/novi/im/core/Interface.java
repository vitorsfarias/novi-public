package eu.novi.im.core;


import java.lang.String;
import java.util.Set;
import org.openrdf.annotations.Iri;

import eu.novi.im.unit.IPAddress;

@Iri("http://fp7-novi.eu/im.owl#Interface")
public interface Interface extends NetworkElement, InterfaceOrNode {
	
	@Iri("http://fp7-novi.eu/im.owl#canFederate")
	Boolean getCanFederate();
	@Iri("http://fp7-novi.eu/im.owl#canFederate")
	void setCanFederate(Boolean canFederate);
	
	@Iri("http://fp7-novi.eu/im.owl#hasIPv4Address")
	Set<IPAddress> getHasIPv4Address();
	@Iri("http://fp7-novi.eu/im.owl#hasIPv4Address")
	void setHasIPv4Address(Set<? extends IPAddress> hasIPv4Address);

	@Iri("http://fp7-novi.eu/im.owl#hasNetmask")
	String getHasNetmask();
	@Iri("http://fp7-novi.eu/im.owl#hasNetmask")
	void setHasNetmask(String hasNetmask);

	@Iri("http://fp7-novi.eu/im.owl#isInboundInterfaceOf")
	Set<Node> getIsInboundInterfaceOf();
	@Iri("http://fp7-novi.eu/im.owl#isInboundInterfaceOf")
	void setIsInboundInterfaceOf(Set<? extends Node> isInboundInterfaceOf);

	@Iri("http://fp7-novi.eu/im.owl#isOutboundInterfaceOf")
	Set<Node> getIsOutboundInterfaceOf();
	@Iri("http://fp7-novi.eu/im.owl#isOutboundInterfaceOf")
	void setIsOutboundInterfaceOf(Set<? extends Node> isOutboundInterfaceOf);

	@Iri("http://fp7-novi.eu/im.owl#isSink")
	Set<LinkOrPath> getIsSink();
	@Iri("http://fp7-novi.eu/im.owl#isSink")
	void setIsSink(Set<? extends LinkOrPath> isSink);

	@Iri("http://fp7-novi.eu/im.owl#isSource")
	Set<LinkOrPath> getIsSource();
	@Iri("http://fp7-novi.eu/im.owl#isSource")
	void setIsSource(Set<? extends LinkOrPath> isSource);

	@Iri("http://fp7-novi.eu/im.owl#switchedTo")
	Set<Interface> getSwitchedTo();
	@Iri("http://fp7-novi.eu/im.owl#switchedTo")
	void setSwitchedTo(Set<? extends Interface> switchedTo);
	
	@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	Set<Interface> getImplementedBy();
	
	/**the reverse property implements are set automatically
	 * @param implementedBy
	 */
	@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	void setImplementedBy(Set<? extends Interface> implementedBy);

	@Iri("http://fp7-novi.eu/im.owl#implements")
	Set<Interface> getImplements();
	@Iri("http://fp7-novi.eu/im.owl#implements")
	void setImplements(Set<? extends Interface> _implements);

}
