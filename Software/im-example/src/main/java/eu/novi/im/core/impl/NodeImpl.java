package eu.novi.im.core.impl;

import java.math.BigInteger;
import java.util.Set;

import eu.novi.im.core.Interface;
import eu.novi.im.core.InterfaceOrNode;
import eu.novi.im.core.Location;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Path;
import eu.novi.im.core.Service;
import eu.novi.im.util.IMUtil;

public class NodeImpl extends ResourceImpl implements Node {

	private Location locatedAt;
	private Set<? extends Node> implementedBy;
	private Set<? extends Node> _implements;
	private Set<? extends Path> inPaths;
	private Set<? extends NetworkElementOrNode> nexts;
	private Set<? extends InterfaceOrNode> connectedTo;
	private String hardwareType;
	private Integer hasAvailableLogicalRouters;
	private Set<? extends NodeComponent> hasComponent;
	private Set<? extends Interface> hasInboundInterfaces;
	private Integer hasLogicalRouters;
	private Set<? extends Interface> hasOutboundInterfaces;
	private Set<? extends Service> hasServices;
	private String hostname;
	private String hrn;
	
	

	public NodeImpl(String uri)
	{
		super(uri);
		
	}
	
/*	public Node copy(Node toCopy) {
		super.copy(toCopy);
		this.locatedAt = toCopy.getLocatedAt();
		this.implementedBy = toCopy.getImplementedBy();
		this.inPaths = toCopy.getInPaths();
		this.nexts = toCopy.getNexts();
		this.connectedTo = toCopy.getConnectedTo();
		this.hardwareType = toCopy.getHardwareType();
		this.hasAvailableLogicalRouters = toCopy.getHasAvailableLogicalRouters();
		this.hasComponent = toCopy.getHasComponent();
		this.hasInboundInterfaces = toCopy.getHasInboundInterfaces();
		this.hasOutboundInterfaces = toCopy.getHasOutboundInterfaces();
		this.hasLogicalRouters = toCopy.getHasLogicalRouters();
		this.hasServices = toCopy.getHasServices();
		this.hostname = toCopy.getHostname();
		this.hrn = toCopy.getHrn();
		return this;
	}*/
	
	@Override
	public Location getLocatedAt() {
		return this.locatedAt;
	}

	@Override
	public void setLocatedAt(Location locatedAt) {
		this.locatedAt = locatedAt;
	}

	
	@Override
	public Set<Node> getImplementedBy() {
		return (Set<Node>) this.implementedBy;
	}

	
	@Override
	public void setImplementedBy(Set<? extends Node> implementedBy) {
		IMUtil.reverseProperty(implementedBy, this.implementedBy, this, "Implements");
		this.implementedBy = implementedBy;

	}

	@Override
	public Set<Path> getInPaths() {
		return (Set<Path>) this.inPaths;
	}

	@Override
	public void setInPaths(Set<? extends Path> inPaths) {
		this.inPaths = inPaths;

	}

	@Override
	public Set<NetworkElementOrNode> getNexts() {
		return (Set<NetworkElementOrNode>) this.nexts;
	}

	@Override
	public void setNexts(Set<? extends NetworkElementOrNode> nexts) {
		this.nexts = nexts;
	}

	@Override
	public Set<InterfaceOrNode> getConnectedTo() {
		return (Set<InterfaceOrNode>) this.connectedTo;
	}

	@Override
	public void setConnectedTo(Set<? extends InterfaceOrNode> connectedTo) {
		this.connectedTo = connectedTo;
	}

	@Override
	public String getHardwareType() {
		return this.hardwareType;
	}

	@Override
	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;

	}

	@Override
	public Integer getHasAvailableLogicalRouters() {
		return this.hasAvailableLogicalRouters;
	}

	@Override
	public void setHasAvailableLogicalRouters(
			Integer hasAvailableLogicalRouters) {
		this.hasAvailableLogicalRouters = hasAvailableLogicalRouters;

	}

	@Override
	public Set<NodeComponent> getHasComponent() {
		return (Set<NodeComponent>) this.hasComponent;
	}

	@Override
	public void setHasComponent(Set<? extends NodeComponent> hasComponent) {
		this.hasComponent = hasComponent;

	}

	@Override
	public Set<Interface> getHasInboundInterfaces() {
		return (Set<Interface>) this.hasInboundInterfaces;
	}

	
	@Override
	public void setHasInboundInterfaces(
			Set<? extends Interface> hasInboundInterfaces) {
		
		
		IMUtil.reverseProperty(hasInboundInterfaces, this.hasInboundInterfaces, this, "IsInboundInterfaceOf");
		this.hasInboundInterfaces = hasInboundInterfaces;

	}

	@Override
	public Integer getHasLogicalRouters() {
		return this.hasLogicalRouters;
	}

	@Override
	public void setHasLogicalRouters(Integer hasLogicalRouters) {
		this.hasLogicalRouters = hasLogicalRouters;

	}

	@Override
	public Set<Interface> getHasOutboundInterfaces() {
		return (Set<Interface>) this.hasOutboundInterfaces;
	}


	@Override
	public void setHasOutboundInterfaces(
			Set<? extends Interface> hasOutboundInterfaces) {
		
		IMUtil.reverseProperty(hasOutboundInterfaces, this.hasOutboundInterfaces,
				this, "IsOutboundInterfaceOf");
		this.hasOutboundInterfaces = hasOutboundInterfaces;

	}

	@Override
	public Set<Service> getHasService() {
		return (Set<Service>) this.hasServices;
	}

	@Override
	public void setHasService(Set<? extends Service> hasServices) {
		this.hasServices = hasServices;

	}

	@Override
	public String getHostname() {
		return this.hostname;
	}

	@Override
	public void setHostname(String hostname) {
		this.hostname = hostname;

	}

	@Override
	public String getHrn() {
		return this.hrn;
	}

	@Override
	public void setHrn(String hrn) {
		this.hrn = hrn;

	}

	@Override
	public Set<Node> getImplements() {
		return (Set<Node>)_implements;
	}

	@Override
	public void setImplements(Set<? extends Node> _implements) {
		this._implements = _implements;
		
	}

}
