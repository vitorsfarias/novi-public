/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.rspecs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.osgi.service.log.LogService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Memory;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.LoginComponentImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAActions;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.exceptions.RHBadInputException;
import eu.novi.requesthandler.utils.RHUtils;
import eu.novi.im.unit.IPAddress;
import eu.novi.im.unit.impl.IPAddressImpl;

/**
 * FEDERICA RSpec 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class FedericaRSpec extends RSpecSchema {
	
	private Element root;
	private NOVIUserImpl user;
	private final static String inSufix = "-in";
	private final static String outSufix = "-out";

	public FedericaRSpec(){
		super();
	}
	
	/**
	 * Adds the information from the vnode to the RSpec request.
	 * 
	 * @param: vnode. Node coming from the Topology requested by the user. Its information
	 *  is added to the request RSpec.
	 * @throws Exception 
	 */
	public void addNodeToRequestRSpec(VirtualNode node) throws RHBadInputException {
		String virtualRole = node.getVirtualRole();	
		
		if (virtualRole.equals("router")) {
			processRouterNode(node);			
		} else if (virtualRole.equals("vm")) {
			processVmNode(node);
		} else {
			logService.log(LogService.LOG_INFO, "Unknown virtual role for FEDERICA: "+virtualRole);
		}
	}

	private void processVmNode(VirtualNode node) throws RHBadInputException {
		String parent = getVMServer(node);
		
		Element nodeElement = createVMNodeElement(node.toString());
		nodeElement.setAttribute(SFAConstants.COMPONENT_ID, SFAConstants.FED_COMPONENT_NODE_PREFIX + parent);
		
		Element hwElement = doc.createElement(SFAConstants.HW_TYPE);
		hwElement.setAttribute(SFAConstants.NAME, "pc");
		nodeElement.appendChild(hwElement);
		
		Element sliverElement = createSliverVmElement(node);	
		nodeElement.appendChild(sliverElement);
		
		Element servicesElement = createServiceElement();
		nodeElement.appendChild(servicesElement);

		Set<Interface> inboundInterfaces = node.getHasInboundInterfaces();
		if(!RHUtils.isSetEmpty(inboundInterfaces)) {
			for (Interface iface : inboundInterfaces) {
				Element interfaceElement = createVmInterface(iface);
				nodeElement.appendChild(interfaceElement);
			}
		}

		root.appendChild(nodeElement);
	}

	private Element createVMNodeElement(String nodeName) {
		logService.log(LogService.LOG_INFO, "RH - Received VM in FEDERICA: " + nodeName);
		
		Element nodeElement = doc.createElement(SFAConstants.NODE);
		nodeElement.setAttribute(SFAConstants.CLIENT_ID, RHUtils.removeNOVIURIprefix(nodeName));
		nodeElement.setAttribute(SFAConstants.EXCLUSIVE, String.valueOf(false));
		nodeElement.setAttribute(SFAConstants.COMPONENT_MANAGER_ID, SFAConstants.FED_COMPONENT_MANAGER_ID);
		
		return nodeElement;
	}

	private String getVMServer(Node node) throws RHBadInputException {
		Set<Node> implementedBySet = node.getImplementedBy();
		String nodeName = node.toString();

		if (implementedBySet != null) {
			Iterator<Node> it = implementedBySet.iterator();
			if (it.hasNext()) {
				Node physicalServer = (Node)it.next();
				return physicalServer.getHostname();
			} else {
				logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with implementedBy empty");
				throw new RHBadInputException("Node " + nodeName + " has implementedBy empty. Only bound request can arrive to FEDERICA.");
			}
		} else {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with implementedBy = null");
			throw new RHBadInputException("Node " + nodeName + " has implementedBy = null. Only bound request can arrive to FEDERICA.");
		}
	}

	private Element createVmInterface(Interface iface) throws RHBadInputException {
		Element interfaceElement = doc.createElement(SFAConstants.INTERFACE);
		
		String interfaceName = RHUtils.removeInterfacePrefixAndSufix(iface.toString());
		interfaceElement.setAttribute(SFAConstants.CLIENT_ID, interfaceName);
		
		if (RHUtils.isSetEmpty(iface.getImplementedBy())) {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding interface " + iface.toString() + " with implementedBy empty or null");
			throw new RHBadInputException("Interface " + iface.toString() + " has implementedBy empty or null. Only bound request can arrive to FEDERICA.");			
		} else {
			for (Interface phyIface : iface.getImplementedBy()) {
				String physicalInterfaceName = RHUtils.removeInterfacePrefixAndSufix(phyIface.toString());
			
				interfaceElement.setAttribute(SFAConstants.COMPONENT_ID, SFAConstants.FED_COMPONENT_INTERFACE_PREFIX+physicalInterfaceName);
			}
		}
		interfaceElement.setAttribute("cc:"+SFAConstants.EXCLUSIVE, String.valueOf(false));

		return interfaceElement;
	}

	private Element createSliverVmElement(VirtualNode node) throws RHBadInputException {
		Element sliverElement = doc.createElement(SFAConstants.SLIVER_TYPE);
		
		if(!RHUtils.isSetEmpty(node.getHasComponent())) {
			Element computeCapacityElement = createComputeCapacityElement(node);
			sliverElement.setAttribute(SFAConstants.NAME, SFAConstants.VM);
			sliverElement.appendChild(computeCapacityElement);			
		} else {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + node.toString() + " with getHasComponent = null");
			throw new RHBadInputException("Node " + node.toString() + " has components = null. CPU speed, num CPU cores, RAM size and disk size information are needed for FEDERICA.");	
		}
		
		return sliverElement;
	}

	private Element createComputeCapacityElement(VirtualNode node) throws RHBadInputException {
		String nodeName = node.toString();
		String cpuSpeed = "";
		String numCpuCores = "";
		String ramSize = "";
		String diskSize = "";
		String guestOS = "";
		
		Set<NodeComponent> nodeComponentSet = node.getHasComponent();		
		for (NodeComponent nc : nodeComponentSet) {
			if (nc instanceof CPU){
				numCpuCores = getNumCPUCores(nc, nodeName);
				cpuSpeed = getCPUSpeed(nc, nodeName);
			} else if (nc instanceof Storage) {
				diskSize = getDiskSize(nc, nodeName);
			} else if (nc instanceof Memory) {
				ramSize = getRamSize(nc, nodeName);
			}
		}		
		guestOS = getOSFromNode(node);
	
		return createComputeCapacityElementWithValues(cpuSpeed, numCpuCores, ramSize, diskSize, guestOS);
	}

	private String getDiskSize(NodeComponent nc, String nodeName) throws RHBadInputException {
		logService.log(LogService.LOG_INFO, "VMs in FEDERICA with Storage");
		Float ram = ((Storage) nc).getHasStorageSize();
		if (ram == null) {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with getHasStorageSize = null");
			throw new RHBadInputException("Node " + nodeName + " has getHasStorageSize = null. Storage size is missing for FEDERICA.");					
		}
		
		return String.valueOf(ram.intValue());
	}

	private String getOSFromNode(VirtualNode node) throws RHBadInputException {	
		String guestOS = node.getHasOS();			
		if(guestOS == null || guestOS.isEmpty()) {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + node.toString() + " with getHasOS = null");
			throw new RHBadInputException("Node " + node.toString() + " has getHasOS = null. OS is missing for FEDERICA.");					
		}
		return guestOS;
	}

	private Element createComputeCapacityElementWithValues(String cpuSpeed,
			String numCpuCores, String ramSize, String diskSize, String guestOS) {
		
		Element computeCapacityElement = doc.createElement("cc:compute_capacity");
		computeCapacityElement.setAttribute(SFAConstants.CPU_SPEED, cpuSpeed);
		computeCapacityElement.setAttribute(SFAConstants.NUM_CPUS, numCpuCores);
		computeCapacityElement.setAttribute(SFAConstants.RAM_SIZE, ramSize);
		computeCapacityElement.setAttribute(SFAConstants.DISK_SIZE, diskSize);
		computeCapacityElement.setAttribute(SFAConstants.GUEST_OS, guestOS);		
		return computeCapacityElement;
	}

	private String getRamSize(NodeComponent nc, String nodeName) throws RHBadInputException {
		logService.log(LogService.LOG_INFO, "VMs in FEDERICA with Memory");
		
		Float disk = ((Memory) nc).getHasMemorySize(); 
		
		if (disk == null){
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with getHasMemorySize = null");
			throw new RHBadInputException("Node " + nodeName + " has getHasMemorySize = null. Memory size is missing for FEDERICA.");					
		}
		
		return String.valueOf(disk.intValue());
	}

	private String getCPUSpeed(NodeComponent nc, String nodeName) throws RHBadInputException {
		logService.log(LogService.LOG_INFO, "VMs in FEDERICA with CPU data");
		Float cpu = ((CPU) nc).getHasCPUSpeed();
		if(cpu == null) {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with getHasCPUSpeed = null");
			throw new RHBadInputException("Node " + nodeName + " has getHasCPUSpeed = null. CPU speed is missing for FEDERICA.");					
		}

		return String.valueOf(cpu.intValue());
	}

	private String getNumCPUCores(NodeComponent nc, String nodeName) throws RHBadInputException {
		BigInteger cores = ((CPU) nc).getHasCores();
		if (cores == null) {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + nodeName + " with getHasCores = null");
			throw new RHBadInputException("Node " + nodeName + " has getHasCores = null. Number of CPU cores is missing for FEDERICA.");					
		}
		
		return String.valueOf(cores.intValue());
	}

	protected void processRouterNode(VirtualNode node) throws RHBadInputException {
		String physicalRouterName = getPhysicalRouterName(node);

		Element nodeElement = createRouterNodeElement(node, physicalRouterName);
		Element hwElement = getRouterHWElement();
		nodeElement.appendChild(hwElement);
		
		Element sliverElement = doc.createElement(SFAConstants.SLIVER_TYPE);
		sliverElement.setAttribute(SFAConstants.NAME, SFAConstants.ROUTER);
		nodeElement.appendChild(sliverElement);

		Element serviceElement = createServiceElement();
		nodeElement.appendChild(serviceElement);

		Set<Interface> interfacesSet = node.getHasOutboundInterfaces();
		Iterator<Interface> itInt = interfacesSet.iterator();
		while (itInt.hasNext()) {				
			Interface logicalInterface = (InterfaceImpl)itInt.next();

			Element interfaceElement = createLogicalRouterInterfaceElement(logicalInterface, physicalRouterName);
			nodeElement.appendChild(interfaceElement);
		}
		root.appendChild(nodeElement);
	}

	private Element createRouterNodeElement(VirtualNode node, String physicalRouterName) {
		Element nodeElement = doc.createElement(SFAConstants.NODE);
		String routerName = RHUtils.removeNOVIURIprefix(node.toString());
		nodeElement.setAttribute(SFAConstants.CLIENT_ID, routerName);
		nodeElement.setAttribute(SFAConstants.COMPONENT_ID, SFAConstants.FED_COMPONENT_NODE_PREFIX + physicalRouterName);
		nodeElement.setAttribute(SFAConstants.EXCLUSIVE, String.valueOf(false));
		nodeElement.setAttribute(SFAConstants.COMPONENT_MANAGER_ID, SFAConstants.FED_COMPONENT_MANAGER_ID);
		
		return nodeElement;
	}

	private Element getRouterHWElement() throws RHBadInputException {
		Element hwElement = doc.createElement(SFAConstants.HW_TYPE);
		hwElement.setAttribute(SFAConstants.NAME, "genericNetworkDevice");
		return hwElement;
	}
	
	private String getPhysicalRouterName(VirtualNode node) throws RHBadInputException {
		String parentRouter = "";
		Set<Node> implementedBySet = node.getImplementedBy();
		if (implementedBySet != null) {
			Iterator<Node> it = implementedBySet.iterator();
			if (it.hasNext()) {
				Node physicalRouter = (Node)it.next();
				parentRouter = physicalRouter.getHostname();
			} else {
				logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + node.toString() + " with implementedBy empty");
				throw new RHBadInputException("Node " + node.toString() + " has implementedBy epmty. Only bound request can arrive to FEDERICA.");
			}
		} else {
			logService.log(LogService.LOG_INFO, "FedericaRSpec: Not adding node " + node.toString() + " with implementedBy = null");
			throw new RHBadInputException("Node " + node.toString() + " has implementedBy = null. Only bound request can arrive to FEDERICA.");
		}
		return parentRouter;
	}

	private Element createServiceElement() {
		Element serviceElement = doc.createElement(SFAConstants.SERVICES);
		String userID = SFAActions.getSFAUserIDFromNOVIUser(user);
		Set<String> userPublicKeys = user.getPublicKeys();
		for (String publicKey : userPublicKeys) {
			Element loginElement = doc.createElement(SFAConstants.LOGIN);	
			loginElement.setAttribute(SFAConstants.AUTHENTICATION, "ssh-keys");
			loginElement.setAttribute("cc:user", userID);
			loginElement.setAttribute("cc:key", publicKey);
			serviceElement.appendChild(loginElement);
		}
		return serviceElement;
	}
	

	private Element createLogicalRouterInterfaceElement(
			Interface logicalInterface, String routerName) throws RHBadInputException {
		Element interfaceElement = doc.createElement(SFAConstants.INTERFACE);
		
		String interfaceName = RHUtils.removeInterfacePrefixAndSufix(logicalInterface.toString());
		interfaceElement.setAttribute(SFAConstants.CLIENT_ID, interfaceName);
		
		Set<Interface> physicalInterfacesSet = logicalInterface.getImplementedBy();
		String physicalPort = getPhysicalPortWithoutPrefix(physicalInterfacesSet, interfaceName);

		interfaceElement.setAttribute(SFAConstants.COMPONENT_ID, SFAConstants.FED_COMPONENT_INTERFACE_PREFIX + physicalPort);
		interfaceElement.setAttribute(SFAConstants.CC_EXCLUSIVE, String.valueOf(false));

		Element ipElement = createIPElement(logicalInterface);

		interfaceElement.appendChild(ipElement);
		return interfaceElement;
	}

	private Element createIPElement(Interface logicalInterface) {
		Element ipElement = doc.createElement(SFAConstants.IP);
		String ip = "";
		String netmask = "";
		Set<IPAddress> ipSet = logicalInterface.getHasIPv4Address();
		if (ipSet != null && !ipSet.isEmpty()) {
			ip = ipSet.iterator().next().getHasValue();
			if (ip != null && ip.contains("/")) {
				String[] ipAndNetmask = cidrToDotNotation(ip);
				ip = ipAndNetmask[0];
				netmask = ipAndNetmask[1];
			} else {
				netmask = logicalInterface.getHasNetmask();
			}
		}

		ipElement.setAttribute(SFAConstants.ADDRESS, ip);
		ipElement.setAttribute(SFAConstants.NETMASK, netmask);
		ipElement.setAttribute(SFAConstants.TYPE, "ipv4");
		
		return ipElement;
	}
	
	/**
	 * Changes from cidr notation to dot.
	 * @param ifaceIP
	 * @return IP in dot form
	 */
	private String[] cidrToDotNotation(String ifaceIP) {
		SubnetUtils utils = new SubnetUtils(ifaceIP);
		String ip = utils.getInfo().getAddress();
		String netmask = utils.getInfo().getNetmask();
		return new String[]{ip, netmask};

	}

	private String getPhysicalPortWithoutPrefix(
			Set<Interface> physicalInterfacesSet, String interfaceName) throws RHBadInputException {
		if (physicalInterfacesSet != null && physicalInterfacesSet.iterator() != null) {

			Iterator<Interface> physicalInterfaceIt = physicalInterfacesSet.iterator();
			return RHUtils.removeInterfacePrefixAndSufix(physicalInterfaceIt.next().toString());
		} else {
			logService.log(LogService.LOG_ERROR, "Logical interface " + interfaceName + " doesn't have any physical interface.\n logicalInterface.getImplementedBy() returned null or empty");
			throw new RHBadInputException("Logical interface " + interfaceName + " doesn't have any physical interface.\n logicalInterface.getImplementedBy() returned null or empty");
		}

	}
	
	public boolean isExistingVirtualLink(VirtualLink vLink) throws RHBadInputException {
		if (RHUtils.isSetEmpty(vLink.getProvisionedBy())) {
			throw new RHBadInputException("Virtual link " + vLink.toString() + " is not provisioned by any path");
		}
		
		Path path = (Path) vLink.getProvisionedBy().iterator().next();
		if(RHUtils.isSetEmpty(path.getContains())) {
			throw new RHBadInputException("Path " + path.toString() + " doesn't contain resources");
		}	
		
		int numResourcesNewLink = (path.getContains().size())/3;
		if (numResourcesNewLink == 1) {
			for (Resource resource : path.getContains()) {
				if((resource instanceof NSwitch)) {
					return true;
				}
			}
		}
		
		String sourceNodeInterfaceNewLink = getSourceInterface(vLink);
		String sinkNodeInterfaceNewLink = getSinkInterface(vLink);
		
		NodeList linksRSpec = doc.getElementsByTagName(SFAConstants.LINK);
		for (int i=0; i<linksRSpec.getLength(); i++) {
			Element linkElement = (Element) linksRSpec.item(i);
			NodeList componentHops = linkElement.getElementsByTagName(SFAConstants.COMPONENT_HOP);
			int hopsInExistingLink = componentHops.getLength();
			List<String> existingNodesInterfaces = getListOfInterfacesFromLinkElement(linkElement);
			
			if(existingNodesInterfaces.contains(sourceNodeInterfaceNewLink) &&
					existingNodesInterfaces.contains(sinkNodeInterfaceNewLink)) {
				//&& hopsInExistingLink == numResourcesNewLink
				return true;
			}
		}
		return false;
	}
	
	private List<String> getListOfInterfacesFromLinkElement(Element linkElement) {
		List<String> interfacesFromLink = new ArrayList<String>();
		NodeList interfaceRefElements = linkElement.getElementsByTagName(SFAConstants.INTERFACE_REF);
		
		for(int i=0; i<interfaceRefElements.getLength(); i++) {
			Element interfaceRef = (Element) interfaceRefElements.item(i);
			interfacesFromLink.add(interfaceRef.getAttribute(SFAConstants.COMPONENT_ID));
		}
		
		return interfacesFromLink;

	}
	
	
	public void addLinkToRequestRSpec(VirtualLink vLink) {
		Path path = (Path) vLink.getProvisionedBy().iterator().next();
		Element linkElement = createLinkElement(RHUtils.removeInterfacePrefixAndSufix(vLink.toString()));
		setComponentHops(linkElement, path.getContains());
		setInterfacesReferences(linkElement, vLink);
		
		root.appendChild(linkElement);
	}
	
	private void setInterfacesReferences(Element linkElement, VirtualLink vLink) {
		Element sourceInterfaceElement = getSourceInterfaceElement(vLink);
		linkElement.appendChild(sourceInterfaceElement);

		Element sinkInterfaceElement = getSinkInterfaceElement(vLink);
		linkElement.appendChild(sinkInterfaceElement);
	}
	
	private Element getSourceInterfaceElement(VirtualLink vLink) {
		Element sourceInterfaceElement = doc.createElement(SFAConstants.INTERFACE_REF);
		sourceInterfaceElement.setAttribute(SFAConstants.CLIENT_ID, getSourceInterface(vLink));
		
		return sourceInterfaceElement;
	}
	
	private String getSourceInterface(VirtualLink vLink) {
		Interface virtualInterfaceSource = vLink.getHasSource().iterator().next();
		Interface physicalInterfaceSource = virtualInterfaceSource.getImplementedBy().iterator().next();
				
		if(physicalInterfaceSource.toString().contains(SFAConstants.NOVI_PL)) {
			return "nswitch:if0";
		} else {
			return RHUtils.removeInterfacePrefixAndSufix(vLink.getHasSource().iterator().next().toString());
		}
	}
	

	private Element getSinkInterfaceElement(VirtualLink vLink) {
		Element sinkInterfaceElement = doc.createElement(SFAConstants.INTERFACE_REF);
		sinkInterfaceElement.setAttribute(SFAConstants.CLIENT_ID, getSinkInterface(vLink));
		return sinkInterfaceElement;
	}
	
	private String getSinkInterface(VirtualLink vLink) {
		Interface virtualInterfaceSink = vLink.getHasSink().iterator().next();
		Interface physicalInterfaceSink = virtualInterfaceSink.getImplementedBy().iterator().next();	
		if(physicalInterfaceSink.toString().contains(SFAConstants.NOVI_PL)) {
			return "nswitch:if0";
		} else {
			return RHUtils.removeInterfacePrefixAndSufix(vLink.getHasSink().iterator().next().toString());
		}
	}


	private void setComponentHops(Element linkElement, Set<Resource> pathResources) {
		for (Resource resource : pathResources) {
			if(resource instanceof Link) {
				if(!(resource instanceof NSwitch)) {
					Link link = (Link) resource;	
					Element componentHopElement = getHopElement(link);
					linkElement.appendChild(componentHopElement);
				}
			}
		}		
	}

	private Element getHopElement(Link link) {
		Element componentHopElement = doc.createElement(SFAConstants.COMPONENT_HOP);
		componentHopElement.setAttribute(SFAConstants.COMPONENT_ID, 
				RHUtils.removeInterfacePrefixAndSufix(link.toString()));
		
		Interface sinkInterface = link.getHasSink().iterator().next();
		Interface sourceInterface = link.getHasSource().iterator().next();
		
		Element sourceInterfaceElement = doc.createElement(SFAConstants.INTERFACE_REF);
		sourceInterfaceElement.setAttribute(SFAConstants.COMPONENT_ID, RHUtils.removeInterfacePrefixAndSufix(sourceInterface.toString()));
		sourceInterfaceElement.setAttribute(SFAConstants.COMPONENT_MANAGER_ID, SFAConstants.FED_COMPONENT_MANAGER_ID);
		componentHopElement.appendChild(sourceInterfaceElement);
		
		Element sinkInterfaceElement = doc.createElement(SFAConstants.INTERFACE_REF);
		sinkInterfaceElement.setAttribute(SFAConstants.COMPONENT_ID,RHUtils.removeInterfacePrefixAndSufix(sinkInterface.toString()));
		sinkInterfaceElement.setAttribute(SFAConstants.COMPONENT_MANAGER_ID, SFAConstants.FED_COMPONENT_MANAGER_ID);
		componentHopElement.appendChild(sinkInterfaceElement);		
		
		return componentHopElement;
	}

	private Element createLinkElement(String vLinkName) {
		Element linkElement = doc.createElement(SFAConstants.LINK);
		linkElement.setAttribute(SFAConstants.CLIENT_ID, vLinkName);
		
		Element componentManagerElement = doc.createElement(SFAConstants.COMPONENT_MANAGER);
		componentManagerElement.setAttribute(SFAConstants.NAME, SFAConstants.FED_COMPONENT_MANAGER_ID);
		
		linkElement.appendChild(componentManagerElement);
		return linkElement;
	}

	public void addUAG() {		
		Element uagElement = doc.createElement("cc:slicelogin");		

		Element loginElement = doc.createElement("cc:"+SFAConstants.LOGIN);

		loginElement.setAttribute("username", SFAActions.getSFAUserIDFromNOVIUser(user));
		
		Set<String> userPublicKeys = user.getPublicKeys();
		for (String publicKey : userPublicKeys) {
			loginElement.setAttribute("key", publicKey);
		}	
		
		uagElement.appendChild(loginElement);
		
		root.appendChild(uagElement);
	}
	
	public void createEmptyRequestRSpec() {
		root = doc.createElement(SFAConstants.RSPEC);
		root.setAttribute(SFAConstants.XMLNS, SFAConstants.FED_XMLNS);
		root.setAttribute(SFAConstants.XMLNS_CC, SFAConstants.FED_XMLNS_CC);
		root.setAttribute(SFAConstants.XMLNS_XSI, SFAConstants.FED_XMLNS_XSI);
		root.setAttribute(SFAConstants.XSI_SCHEMA_LOCATION, SFAConstants.FED_XSI_SCHEMA_LOCATION_REQ);
		root.setAttribute(SFAConstants.TYPE, "request");
		doc.appendChild(root);
	}

	/**
	 * gets the nodes and links from the RSpec as Resource objects;
	 * @return the set of Resource objects
	 */
	public Set<Resource> getResourceSet() {
		Set<Node> nodeSet = getNodeSet();
		
		Set<Link> linkSet = getLinkSet(nodeSet);
		
		Set<Resource> resources = new HashSet<Resource>();
		
		resources.addAll(nodeSet);
		resources.addAll(linkSet);
		return resources;		
	}
	
	/**
	 * returns the set of nodes from the RSpec
	 * @return set of Node objects
	 */
	private Set<Node> getNodeSet() {
		//get the list of nodes by tag

		NodeList nodesRSpec = doc.getElementsByTagName(SFAConstants.NODE);		
		
		int nItems = nodesRSpec.getLength();
		
		Set<Node> nodeSet = new HashSet<Node>();
		//go through the list of nodes from the RSpec sequentially and add them to the list
		for (int i = 0; i < nItems; i++) {
			Element crtNode = (Element) nodesRSpec.item(i);
			
			Node nn = getNodeFromElement(crtNode);			
			if (nn != null) {
				nodeSet.add(nn); 
			}
		}
		return nodeSet;
	}
	
	/**
	 * converts an RSpec Element to a Node object from the NOVI IM
	 * @param rspecElement: an element from a RSpec 
	 * @return Node object that contains the information extracted from the rspecElement
	 */
	private Node getNodeFromElement(Element rspecElement) {
		if (rspecElement.getAttributes().getNamedItem(SFAConstants.COMPONENT_NAME) == null) {
			return null;
		}
		String componentName = rspecElement.getAttributes().getNamedItem(SFAConstants.COMPONENT_NAME).getNodeValue();
		String nodeURN = getElementURN(rspecElement);									

		Node nodeToReturn = new NodeImpl(nodeURN);		
		nodeToReturn.setLocatedAt(getNodeLocation(componentName, rspecElement));
		nodeToReturn.setHardwareType(getHardwareType(rspecElement));
	
		if (getSliverType(rspecElement).equals(SFAConstants.ROUTER)) {
			int availableRouters = getHasAvailableLogicalRouters(rspecElement);
			nodeToReturn.setHasAvailableLogicalRouters(availableRouters);
			nodeToReturn.setHasLogicalRouters(SFAConstants.MAX_LOGICAL_ROUTERS - availableRouters);
		} else if (getSliverType(rspecElement).equals(SFAConstants.VM)) {
			nodeToReturn.setHasComponent(getNodeComponents(componentName, rspecElement));
		}

		nodeToReturn.setHasInboundInterfaces(getInterfaces(rspecElement, "in"));
		nodeToReturn.setHasOutboundInterfaces(getInterfaces(rspecElement, "out"));
		nodeToReturn.setHostname(componentName);
		nodeToReturn.setHrn(getHRN(nodeURN));

		return nodeToReturn;
	}
	
	
	/**
	 * returns the sliver type (e.g. router, vm) of the RSpec element given as input
	 * @param rspecElement: an element from a RSpec
	 * @return a String containing the type of the sliver
	 */
	protected String getSliverType(Element rspecElement) {
		String sliverType = "";
		
		NodeList sliverNodes = rspecElement.getElementsByTagName(SFAConstants.SLIVER_TYPE);
		if (sliverNodes.getLength() > 0) {
			org.w3c.dom.Node crtChild = sliverNodes.item(0);
			sliverType = crtChild.getAttributes().getNamedItem(SFAConstants.NAME).getNodeValue();
		}		
		return sliverType;
	}

	/**
	 * returns the number of logical routers available at the RSpec element given as input
	 * @param rspecElement: an element from a RSpec
	 * @return the number of available logical routers
	 */
	protected int getHasAvailableLogicalRouters(Element rspecElement) {
		NodeList availableElements = rspecElement.getElementsByTagName(SFAConstants.AVAILABLE);
		if (availableElements.getLength() > 0) {
			String tag = SFAConstants.FEDERICA_ADVERTISEMENT + ":" + SFAConstants.AVAILABLE_LOGICAL_ROUTERS;
			return Integer.parseInt(availableElements.item(0).getAttributes().getNamedItem(tag).getNodeValue());
		}
		return 0;
	}
	
	/**
	 * returns a set of the components available at the RSpec element specified as input 
	 * @param componentName: string with the name of the component
	 * 							used for creating the object URI
	 * @param rspecElement: an element from a RSpec
	 * @return a set containing the components available at the node (e.g. CPU, memory, storage)
	 */
	protected Set<NodeComponent> getNodeComponents(String componentName, Element rspecElement) {
		Set<NodeComponent> nodeComponents  = new HashSet<NodeComponent>();
		
		NodeList availableElements = rspecElement.getElementsByTagName(SFAConstants.AVAILABLE);
		
		for (int i = 0; i < availableElements.getLength(); i++) {
			
			org.w3c.dom.Node n = availableElements.item(i);
			
			CPU cpu = getCPU(componentName, n);
			if (cpu != null) {
				nodeComponents.add(cpu);
			}
			
			Memory mem = getMemory(componentName, n);
			if (mem != null) {
				nodeComponents.add(mem);
			}
			
			Storage storage = getStorage(componentName, n);
			if (storage != null) {
				nodeComponents.add(storage);
			}
		}
		return nodeComponents;
	}
	
	/**
	 * converts an RSpec element to a CPU object from the NOVI IM
	 * @param componentName: string with the name of the component;
	 * 							used for creating the object URI
	 * @param availableElement: an RSpec element
	 * @return CPU object
	 */
	protected CPU getCPU(String componentName, org.w3c.dom.Node availableElement) {
		String tag = SFAConstants.FEDERICA_ADVERTISEMENT + ":" + SFAConstants.NUM_CPUS;
		String speedTag = SFAConstants.FEDERICA_ADVERTISEMENT + ":" + SFAConstants.CPU_SPEED;
		
		org.w3c.dom.Node nCpu = availableElement.getAttributes().getNamedItem(tag);
		
		String uri = componentName + "." + SFAConstants.CPU;
		CPU cpu = null;
		if (nCpu != null) {
			cpu =  new CPUImpl(uri);
			cpu.setHasAvailableCores(new BigInteger(nCpu.getNodeValue()));
		} 
		
        org.w3c.dom.Node cpuSpeed = availableElement.getAttributes().getNamedItem(speedTag);
        
        if (cpuSpeed != null) {
        	if (cpu == null) {
        		cpu  = new CPUImpl(uri);
        	}
            cpu.setHasCPUSpeed(Float.valueOf(getNumericCharacters(cpuSpeed.getNodeValue())));

        }
		return cpu;
	}
	
	
	/**
	 * converts an RSpec element to a Memory object from the NOVI IM
	 * @param componentName: string with the name of the component; 
	 * 							used for creating the object URI 
	 * @param availableElement: an RSpec element
	 * @return a Memory object
	 */
	protected Memory getMemory(String componentName, org.w3c.dom.Node availableElement) {
		
		String tag = SFAConstants.FEDERICA_ADVERTISEMENT + ":" + SFAConstants.RAM_SIZE;
		
		org.w3c.dom.Node nMem = availableElement.getAttributes().getNamedItem(tag);

		if (nMem != null) {
			String uri = componentName + "." + SFAConstants.MEM;
			Memory mem = new MemoryImpl(uri);
			//the "GB" are dropped
			mem.setHasAvailableMemorySize(new Float(getNumericCharacters(nMem.getNodeValue())));
			return mem;
		}

		return null;
	}
	
	/**
	 * extract characters representing a real number from the string passed as input
	 * @param s: the initial string
	 * @return a substring with the numeric characters
	 */
	private String getNumericCharacters(String s) {
		//if s = "240,19Gb", the method should return 240.19  
		String res = "";
		for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!Character.isLetter(ch)) {
                res += ch;
            }
        }
		//the line below fixes the issue with values from FEDERICA Adv
		res = res.replaceAll(",", ".");
		return res;
	}
	
	/**
	 * converts an RSpec element to Storage object from the NOVI IM
	 * @param componentName: string with the name of the component;
	 * 							used for creating the object URI
	 * @param availableElement: an RSpec element
	 * @return a Storage object
	 */
	protected Storage getStorage(String componentName, org.w3c.dom.Node availableElement) {
		String tag = SFAConstants.FEDERICA_ADVERTISEMENT + ":" + SFAConstants.DISK_SIZE;
		
		org.w3c.dom.Node nStorage = availableElement.getAttributes().getNamedItem(tag);

		if (nStorage != null) {
			String uri = componentName + "." + SFAConstants.STORAGE;
			Storage st = new StorageImpl(uri);
			//the "Gb" are dropped
			String s = getNumericCharacters(nStorage.getNodeValue());
			st.setHasAvailableStorageSize(new Float(s));
			return st;
		}

		return null;
	}
	

	
	/**
	 * returns a set of interfaces available for the RSpec element specified as input
	 * @param rspecElement: an RSpec element
	 * @return a set of Interface objects
	 */
	private Set<Interface> getInterfaces(Element rspecElement, String direction) {
		Set<Interface> ifaces = new HashSet<Interface>();
		
		NodeList interfaceElements = rspecElement.getElementsByTagName(SFAConstants.INTERFACE);
				
		for (int i = 0; i < interfaceElements.getLength(); i++) {
			String uri = getElementURN((Element) interfaceElements.item(i)) + "-" + direction;
			InterfaceImpl newInterface = new InterfaceImpl(uri);
			boolean canFederate = getCanFederate((Element) interfaceElements.item(i));
			newInterface.setCanFederate(canFederate);
			ifaces.add(newInterface);
		}
		return ifaces;
	}
	
	private boolean getCanFederate(Element interfaceElement) {
		String canFederate = interfaceElement.getAttributes().getNamedItem(SFAConstants.CAN_FEDERATE).getNodeValue();
		
		return Boolean.valueOf(canFederate);
	}
	
	/**
	 * returns the set of links from the RSpec
	 * @param nodeSet: the set of nodes in the RSpec;
	 * 					needed in order to setup the links correctly
	 * @return a set of Link objects
	 */
	private Set<Link> getLinkSet(Set<Node> nodeSet) {
		NodeList linksRSpec = doc.getElementsByTagName(SFAConstants.LINK);
		
		int nItems = linksRSpec.getLength();
		
		Set<Link> linkSet = new HashSet<Link>();
		//go through the list of links read from the RSpec sequentially and add them to the list
		for (int i = 0; i < nItems; i++) {
		
			Element crtLink = (Element) linksRSpec.item(i);
			
			Set<Link> links = getLinksFromElement(crtLink, nodeSet);
			
			if (!links.isEmpty()) {
				linkSet.addAll(links);
			}
		}
		
		return linkSet;
	}

	/**
	 * converts an RSpec element to two Link objects from the NOVI IM
	 * (two Link objects in order to model bidirectional links - one for each direction) 
	 * @param crtLink: the RSpec element
	 * @param nodeSet: the set of nodes in the RSpec;
	 * 					needed in order to setup the links correctly
	 * @return a set of two Link objects
	 */
	protected Set<Link> getLinksFromElement(Element crtLink, Set<Node> nodeSet) {
		String uri = getElementURN(crtLink);
		Link link = new LinkImpl(uri);
		
		String capacity = crtLink.getElementsByTagName(SFAConstants.FEDERICA_ADVERTISEMENT+":"+"linkAvailability").item(0).getAttributes().getNamedItem("capacity").getNodeValue();
		String numericCapacity = getNumericCharacters(capacity);
		link.setHasCapacity(new Float(numericCapacity));
		link.setHasAvailableCapacity(new Float(numericCapacity));
		
		NodeList ifaces = crtLink.getElementsByTagName(SFAConstants.INTERFACE_REF);
		
		Set<Link> links = new HashSet<Link>();
		
		if (ifaces.getLength() == 2) {
		
			String ifaceFirst = getElementURN((Element) ifaces.item(0));
			String ifaceSecond = getElementURN((Element) ifaces.item(1));
		
			//go through the list of nodes and find the ones that have these interfaces
			Interface ifc1in = getNodeInterface(nodeSet, ifaceFirst + inSufix);
			Interface ifc1out = getNodeInterface(nodeSet, ifaceFirst + outSufix);
			Interface ifc2in = getNodeInterface(nodeSet, ifaceSecond + inSufix);
			Interface ifc2out = getNodeInterface(nodeSet, ifaceSecond + outSufix);

			if (ifc1in != null && ifc1out != null && ifc2in != null && ifc2out != null) {
				Link linkRev = new LinkImpl(uri);
				linkRev.setHasCapacity(new Float(numericCapacity));
				linkRev.setHasAvailableCapacity(new Float(numericCapacity));
				
				//forward link
				addInterfaceToLinkSources(link, ifc1out);
				addInterfaceToLinkSinks(link, ifc2in);
			
				//backward link
				addInterfaceToLinkSources(linkRev, ifc2out);
				addInterfaceToLinkSinks(linkRev, ifc1in);
			
				links.add(link);
				links.add(linkRev);
				
				Set<Link> linkSetInInterface = new HashSet<Link>();
				linkSetInInterface.add(link);
				Set<Link> linkRevSetInInterface = new HashSet<Link>();
				linkSetInInterface.add(linkRev);
				
				ifc1in.setIsSink(linkRevSetInInterface);
				ifc1out.setIsSource(linkSetInInterface);
				ifc2in.setIsSink(linkSetInInterface);
				ifc2out.setIsSource(linkRevSetInInterface);
			}
		}
		
		return links;
	}
	

	/**
	 * searches in a set of Node objects given as input, to find the Interface object 
	 * that has the name specified as input parameter
	 * @param nodeSet: the set of nodes from the RSpec
	 * @param ifaceName: the name of the interface
	 * @return the Interface object with the specified name
	 */
	private Interface getNodeInterface(Set<Node> nodeSet, String ifaceName) {
		for (Node node : nodeSet) {
			Set<Interface> ifaces = new HashSet<Interface>();
			if (ifaceName.endsWith("in")) {
				ifaces = node.getHasInboundInterfaces(); 
			} else {
				ifaces = node.getHasOutboundInterfaces();
			} 
			for (Interface iface : ifaces) {
				if (RHUtils.removeInterfacePrefixAndSufix(iface.toString()).equals(ifaceName)) {
					return iface;
				}
			}
		}
		return null;
	} 
	
	/**
	 * adds an Interface object to the set of sources of the given link 
	 * @param link: Link object
	 * @param ifc: Interface object
	 */
	protected void addInterfaceToLinkSources(Link link, Interface ifc) {
		Set<Interface> hasSources = link.getHasSource();
		if (hasSources == null) {
			hasSources = new HashSet<Interface>();
		}
		if (hasSources.add(ifc)) {
			link.setHasSource(hasSources);
		}
	}
	

	
	/**
	 * adds an Interface object to the set of sinks of the given link
	 * @param link: Link object
	 * @param ifc: Interface object
	 */
	protected void addInterfaceToLinkSinks(Link link, Interface ifc) {
		Set<Interface> hasSinks = link.getHasSink();
		if (hasSinks == null) {
			hasSinks = new HashSet<Interface>();
		}
		if (hasSinks.add(ifc)) {
			link.setHasSink(hasSinks);
		}
	}

	
	public NodeList getNodeListFromDocument() {
		return doc.getElementsByTagName(SFAConstants.NODE);
	}
	
	public NodeList getLinkListFromDocuemtn() {
		return doc.getElementsByTagName(SFAConstants.LINK);
	}
	
	public List<Element> getRoutersFromElement() {
		NodeList allNodes = doc.getElementsByTagName(SFAConstants.NODE);
		List<Element> routerElements = new ArrayList<Element>();
		for (int i=0; i<allNodes.getLength(); i++) {
			Element node = (Element) allNodes.item(i);
			String sliverType = getSliverType(node);
			
			if(sliverType.equalsIgnoreCase(SFAConstants.ROUTER)) {
				routerElements.add(node);
			}
		}
		return routerElements;
	}
	
	public List<Element> getComputesFromElement() {
		NodeList allNodes = doc.getElementsByTagName(SFAConstants.NODE);
		List<Element> computeElements = new ArrayList<Element>();
		for (int i=0; i<allNodes.getLength(); i++) {
			Element node = (Element) allNodes.item(i);
			String sliverType = getSliverType(node);
			
			if(sliverType.equalsIgnoreCase(SFAConstants.VM)) {
				computeElements.add(node);
			}
		}
		return computeElements;
	}
	
	public List<Element> getNodesFromElement() {
		NodeList allNodes = doc.getElementsByTagName(SFAConstants.NODE);
		List<Element> computeElements = new ArrayList<Element>();
		for (int i=0; i<allNodes.getLength(); i++) {
			Element node = (Element) allNodes.item(i);
			computeElements.add(node);
		}
		return computeElements;
	}
	
	public LoginComponentImpl getLoginFromElement(Element router) {
		String routerName = router.getAttribute(SFAConstants.CLIENT_ID);
		NodeList serviceElements = router.getElementsByTagName(SFAConstants.SERVICES);
		LoginComponentImpl loginComponent = new LoginComponentImpl(routerName+"-login");

		for(int i=0; i<serviceElements.getLength(); i++) {
			Element services = (Element) serviceElements.item(i);
			NodeList loginElements = services.getElementsByTagName(SFAConstants.LOGIN);
			
			for(int j= 0; j<loginElements.getLength(); j++) {
				Element login = (Element) loginElements.item(j);
				String hostname = login.getAttribute(SFAConstants.HOSTNAME);
				IPAddress ip = new IPAddressImpl(hostname);
				ip.setHasValue(hostname);
				loginComponent.setHasLoginIPv4Address(ip);
				String port = login.getAttribute(SFAConstants.PORT);
				loginComponent.setHasLoginPort(new Integer(port));
				String user = login.getAttribute(SFAConstants.USER);
				loginComponent.setHasLoginUsername(user);
				String password = login.getAttribute("cc:key");
				loginComponent.setHasLoginPassword(password);
			}
		}
		
		return loginComponent;
	}
	

	public NOVIUserImpl getUser() {
		return user;
	}

	public void setUser(NOVIUserImpl user) {
		this.user = user;
	}

	public static boolean isFedericaLink(VirtualLink vLink) throws RHBadInputException {
		if(RHUtils.isSetEmpty(vLink.getHasSource())) {
			throw new RHBadInputException("virtual link " + vLink.toString() + " has no source interface");
		} else if(RHUtils.isSetEmpty(vLink.getHasSource().iterator().next().getImplementedBy())) {
			throw new RHBadInputException("sourceInterface " + vLink.getHasSource().toString() + " has no physical interface");
		}
		
		if(RHUtils.isSetEmpty(vLink.getHasSink())) {
			throw new RHBadInputException("virtual link " + vLink.toString() + " has no sink interface");
		} else if(RHUtils.isSetEmpty(vLink.getHasSink().iterator().next().getImplementedBy())) {
			throw new RHBadInputException("sinkInterface " + vLink.getHasSink().toString() + " has no physical interface");
		}
		
		if (vLink.getHasSink().iterator().next().getImplementedBy().iterator().next().toString().contains("federica") ||
				vLink.getHasSource().iterator().next().getImplementedBy().iterator().next().toString().contains("federica")) {
			return true;
		}
		
		return false;
	}
	
	
}
