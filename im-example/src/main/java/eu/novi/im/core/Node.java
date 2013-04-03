package eu.novi.im.core;

import java.lang.String;
import java.math.BigInteger;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Node")
public interface Node extends NodeOrService, NetworkElementOrNode, InterfaceOrNode, Resource {
	@Iri("http://fp7-novi.eu/im.owl#hardwareType")
	String getHardwareType();
	@Iri("http://fp7-novi.eu/im.owl#hardwareType")
	void setHardwareType(String hardwareType);

	@Iri("http://fp7-novi.eu/im.owl#hasAvailableLogicalRouters")
	Integer getHasAvailableLogicalRouters();
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableLogicalRouters")
	void setHasAvailableLogicalRouters(Integer hasAvailableLogicalRouters);

	@Iri("http://fp7-novi.eu/im.owl#hasComponent")
	Set<NodeComponent> getHasComponent();
	@Iri("http://fp7-novi.eu/im.owl#hasComponent")
	void setHasComponent(Set<? extends NodeComponent> hasComponent);

	@Iri("http://fp7-novi.eu/im.owl#hasInboundInterface")
	Set<Interface> getHasInboundInterfaces();
	
	/**the reverse function setIsInboundInterfaceOf is set automatically
	 * @param hasInboundInterfaces
	 */
	@Iri("http://fp7-novi.eu/im.owl#hasInboundInterface")
	void setHasInboundInterfaces(Set<? extends Interface> hasInboundInterfaces);

	@Iri("http://fp7-novi.eu/im.owl#hasLogicalRouters")
	Integer getHasLogicalRouters();
	@Iri("http://fp7-novi.eu/im.owl#hasLogicalRouters")
	void setHasLogicalRouters(Integer hasLogicalRouters);

	@Iri("http://fp7-novi.eu/im.owl#hasOutboundInterface")
	Set<Interface> getHasOutboundInterfaces();
	
	
	/**the reverse function setIsOutboundInterfaceOf is set automatically
	 * @param hasOutboundInterfaces
	 */
	@Iri("http://fp7-novi.eu/im.owl#hasOutboundInterface")
	void setHasOutboundInterfaces(Set<? extends Interface> hasOutboundInterfaces);

	@Iri("http://fp7-novi.eu/im.owl#hasService")
	Set<Service> getHasService();
	@Iri("http://fp7-novi.eu/im.owl#hasService")
	void setHasService(Set<? extends Service> hasService);

	@Iri("http://fp7-novi.eu/im.owl#hostname")
	String getHostname();
	@Iri("http://fp7-novi.eu/im.owl#hostname")
	void setHostname(String hostname);

	@Iri("http://fp7-novi.eu/im.owl#hrn")
	String getHrn();
	@Iri("http://fp7-novi.eu/im.owl#hrn")
	void setHrn(String hrn);
	
	@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	Set<Node> getImplementedBy();
	
	/**the reverse property implements are set automatically
	 * @param implementedBy
	 */
	@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	void setImplementedBy(Set<? extends Node> implementedBy);

	@Iri("http://fp7-novi.eu/im.owl#implements")
	Set<Node> getImplements();
	@Iri("http://fp7-novi.eu/im.owl#implements")
	void setImplements(Set<? extends Node> _implements);

	/*
	 * The following constans say what physical type of resource this Node represents
	 */
	public static final String HARDWARE_TYPE_ROUTER="genericNetworkDevice";
	public static final String HARDWARE_TYPE_PLANETLAB_NODE="plab-pc";

}
