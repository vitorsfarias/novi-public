package eu.novi.nswitch.manager;

import eu.novi.im.core.Node;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;

public class Federation {
	private String plInterface;
	private String nodeIp;
	private String vlan;
	private String sliverIp;
	private String netmask;
	private String sliceName;
	private Node plNode;
	private boolean configuredPlanetlab;
	private boolean configuredFederica;

	public Federation(){
		
	}
	
	public String getVlan() {
		return vlan;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public void setVlan(String vlan) {
		this.vlan = vlan;
	}

	public String getSliverIp() {
		return sliverIp;
	}

	public void setSliverIp(String sliverIp) throws IncorrectTopologyException {		
		if(!sliverIp.contains("/")){
			throw new IncorrectTopologyException("Sliver ip " + sliverIp + " has wrong fromat. Should be IP/NETMASK ");
		}
		
		String ip = sliverIp.substring(0, sliverIp.lastIndexOf("/"));
		String netmask = sliverIp.substring(sliverIp.lastIndexOf("/") + 1, sliverIp.length());
		setNetmask(netmask);
		this.sliverIp = ip;	

//BP to fix issues with IM
//		this.sliverIp = sliverIp;
//		if(sliverIp.contains("37")){
//			setNetmask("29");
//		}else{
//			setNetmask("24");
//		}
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public String getSliceName() {
		return sliceName;
	}

	public void setSliceName(String sliceName) {
		this.sliceName = "novi_"+sliceName;
	}

	
	public Node getPlNode() {
		return plNode;
	}

	public void setPlNode(Node plNode) {
		this.plNode = plNode;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	public boolean isPlanetlabConfigured() {
		return configuredPlanetlab;
	}

	public void setPlanetlabConfigured(boolean configured) {
		this.configuredPlanetlab = configured;
	}
	
	public boolean isFedericaConfigured() {
		return configuredFederica;
	}

	public void setFedericaConfigured(boolean configured) {
		this.configuredFederica = configured;
	}

	public String getPlInterface() {
		return plInterface;
	}

	public void setPlInterface(String plInterface) {
		this.plInterface = plInterface;
	}
}

