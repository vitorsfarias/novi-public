package eu.novi.im.core.impl;


import java.util.Set;

import eu.novi.im.core.Interface;
import eu.novi.im.core.InterfaceOrNode;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Node;
import eu.novi.im.unit.IPAddress;
import eu.novi.im.util.IMUtil;

public class InterfaceImpl extends NetworkElementImpl implements Interface {

	private Set<? extends InterfaceOrNode> connectedTo;
	private Set<? extends IPAddress> hasIPv4Address;
	private String hasNetmask;
	private Set<? extends LinkOrPath> isSink;
	private Set<? extends LinkOrPath> isSource;
	private Set<? extends Interface> switchedTo;
	private Set<? extends Interface> implementedBy;
	private Set<? extends Interface> _implements;
	private Set<? extends Node> isInboundInterfaceOf;
	private Set<? extends Node> isOutboundInterfaceOf;
	private Boolean canFederate = false;
	
	
	public InterfaceImpl(String uri)
	{
		super(uri);
		
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
	public Set<IPAddress> getHasIPv4Address() {
		return (Set<IPAddress>) this.hasIPv4Address;
	}

	@Override
	public void setHasIPv4Address(Set<? extends IPAddress> hasIPv4Address) {
		this.hasIPv4Address = hasIPv4Address;

	}

	@Override
	public String getHasNetmask() {
		return this.hasNetmask;
	}

	@Override
	public void setHasNetmask(String hasNetmask) {
		this.hasNetmask = hasNetmask;

	}

	@Override
	public Set<LinkOrPath> getIsSink() {
		return (Set<LinkOrPath>) this.isSink;
	}

	
	/**
	 *for each LinkOrPath in the Set it sets also the reverse relation
	 * LinkOrPath hasSink thisInterface. If you want to add new linkOrPath then
	 * you have to add the linkOrPath to the set and to call again the setIsSink, 
	 * otherwise the data will be inconsistent
	 * @param isSink
	 */
	@Override
	public void setIsSink(Set<? extends LinkOrPath> isSink) {
		//To make sure that this IsSink relation and link/hasSink it is synchronized
		//Whenever we set a new linkOrPath as isSink here, 
		//we make sure hasSink is also filled with this value.

		/*if (this.isSink != null)
		{
			//the set is not null, remove the links from the
			//previous Link/Path
			for (LinkOrPath r : this.isSink)
			{
				Set<Interface> interf = r.getHasSink();
				if (interf != null)
				{
					interf.remove(this);
				}
			}
		}

		if (isSink != null)
		{
			for(LinkOrPath lp : isSink){
				Set<Interface> inter = lp.getHasSink();
				if(inter == null){
					inter = new HashSet<Interface>();
				}
				inter.add(this);
				lp.setHasSink(inter);

			}

		}*/
		IMUtil.reverseProperty(isSink, this.isSink, this, "HasSink");
		this.isSink = isSink;
	}

	@Override
	public Set<LinkOrPath> getIsSource() {
		return (Set<LinkOrPath>) this.isSource;
	}

	
	/**
	 * for each LinkOrPath in the Set it sets also the reverse relation
	 * LinkOrPath hasSource thisInterface. If you want to add new linkOrPath then
	 * you have to add the linkOrPath to the set and to call again the setIsSource, 
	 * otherwise the data will be inconsistent
	 * @param isSource
	 */
	@Override
	public void setIsSource(Set<? extends LinkOrPath> isSource) {

		//To make sure that this IsSource relation and link/hasSource it is synchronized
		//Whenever we set a new linkOrPath as isSource here, 
		//we make sure hasSource is also filled with this value.
		
		IMUtil.reverseProperty(isSource, this.isSource, this, "HasSource");
		this.isSource = isSource;

		/*if (this.isSource != null)
		{
			//the set is not null, remove the links from the
			//previous Link/Path
			for (LinkOrPath r : this.isSource)
			{
				Set<Interface> interf = r.getHasSource();
				if (interf != null)
				{
					interf.remove(this);
				}
			}
		}

		if (isSource != null)
		{
			for(LinkOrPath lp : isSource){
				Set<Interface> inter = lp.getHasSource();
				if(inter == null){
					inter = new HashSet<Interface>();
				}
				inter.add(this);
				lp.setHasSource(inter);

			}

		}

		this.isSource = isSource;*/
	}
	
	

	
	

	@Override
	public Set<Interface> getSwitchedTo() {
		return (Set<Interface>) this.switchedTo;
	}

	@Override
	public void setSwitchedTo(Set<? extends Interface> switchedTo) {
		this.switchedTo = switchedTo;

	}
	
	@Override
	public Set<Interface> getImplementedBy() {
		return (Set<Interface>) this.implementedBy;
	}

	@Override
	public void setImplementedBy(Set<? extends Interface> implementedBy) {
		IMUtil.reverseProperty(implementedBy, this.implementedBy, this, "Implements");
		this.implementedBy = implementedBy;
		
	}

	@Override
	public Set<Interface> getImplements() {
		return (Set<Interface>)_implements;
	}

	@Override
	public void setImplements(Set<? extends Interface> _implements) {
		this._implements = _implements;
		
	}

	@Override
	public Set<Node> getIsInboundInterfaceOf() {
		return (Set<Node>) isInboundInterfaceOf;
	}

	@Override
	public void setIsInboundInterfaceOf(Set<? extends Node> isInboundInterfaceOf) {
		this.isInboundInterfaceOf = isInboundInterfaceOf;
		
	}

	@Override
	public Set<Node> getIsOutboundInterfaceOf() {
		return (Set<Node>) isOutboundInterfaceOf;
	}

	@Override
	public void setIsOutboundInterfaceOf(
			Set<? extends Node> isOutboundInterfaceOf) {
		this.isOutboundInterfaceOf = isOutboundInterfaceOf;
		
	}

	@Override
	public Boolean getCanFederate() {
		return this.canFederate;
	}

	@Override
	public void setCanFederate(Boolean canFederate) {
		this.canFederate = canFederate;
		
	}

}
