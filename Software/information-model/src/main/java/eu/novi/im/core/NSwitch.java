package eu.novi.im.core;

import eu.novi.im.rdfs.subPropertyOf;

import java.lang.String;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#NSwitch")
public interface NSwitch extends Link {
	@Iri("http://fp7-novi.eu/im.owl#hasGRETunnelID")
	Set<String> getHasGRETunnelID();
	@Iri("http://fp7-novi.eu/im.owl#hasGRETunnelID")
	void setHasGRETunnelID(Set<String> hasGRETunnelID);

	@Iri("http://fp7-novi.eu/im.owl#hasPrivateSinkAddress")
	Set<String> getHasPrivateSinkAddress();
	@Iri("http://fp7-novi.eu/im.owl#hasPrivateSinkAddress")
	void setHasPrivateSinkAddress(Set<String> hasPrivateSinkAddress);

	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://fp7-novi.eu/im.owl#hasPrivateSourceAddress")
	Set<String> getHasPrivateSourceAddress();
	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://fp7-novi.eu/im.owl#hasPrivateSourceAddress")
	void setHasPrivateSourceAddress(	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
Set<String> hasPrivateSourceAddress);

	@Iri("http://fp7-novi.eu/im.owl#hasVLANID")
	Set<String> getHasVLANID();
	@Iri("http://fp7-novi.eu/im.owl#hasVLANID")
	void setHasVLANID(Set<String> hasVLANID);
	
	@Iri("http://fp7-novi.eu/im.owl#hasVXLANID")
	Set<String> getHasVXLANID();
	@Iri("http://fp7-novi.eu/im.owl#hasVXLANID")
	void setHasVXLANID(Set<String> hasVXLANID);

}
