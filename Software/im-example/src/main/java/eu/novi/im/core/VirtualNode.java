package eu.novi.im.core;

import java.lang.String;
import java.net.URI;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#VirtualNode")
public interface VirtualNode extends Node {
	@Iri("http://fp7-novi.eu/im.owl#diskImage")
	URI getDiskImage();
	@Iri("http://fp7-novi.eu/im.owl#diskImage")
	void setDiskImage(URI diskImage);

	@Iri("http://fp7-novi.eu/im.owl#hasOS")
	String getHasOS();
	@Iri("http://fp7-novi.eu/im.owl#hasOS")
	void setHasOS(String hasOS);

	@Iri("http://fp7-novi.eu/im.owl#hasVendor")
	String getHasVendor();
	@Iri("http://fp7-novi.eu/im.owl#hasVendor")
	void setHasVendor(String hasVendor);

	@Iri("http://fp7-novi.eu/im.owl#hasVirtualizationEnvironment")
	String getHasVirtualizationEnvironment();
	@Iri("http://fp7-novi.eu/im.owl#hasVirtualizationEnvironment")
	void setHasVirtualizationEnvironment(String hasVirtualizationEnvironment);

	@Iri("http://fp7-novi.eu/im.owl#virtualRole")
	String getVirtualRole();
	@Iri("http://fp7-novi.eu/im.owl#virtualRole")
	void setVirtualRole(String virtualRole);

}
