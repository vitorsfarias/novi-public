package eu.novi.nswitch.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Interface;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.unit.IPAddress;
import eu.novi.nswitch.Nswitch;

import eu.novi.nswitch.exceptions.FederationException;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;
import eu.novi.nswitch.manager.Federation;
import eu.novi.nswitch.manager.SliceFederations;
import eu.novi.nswitch.manager.NswitchManager;

/**
 * NSwitch manager that gets topology from the Request-Handler
 * 
 * @author pikusa
 * 
 */
public class NswitchManagerImpl implements NswitchManager {

	private Nswitch nswitchFederica;
	private Nswitch nswitchPlanetlab;
	private final Logger logger = LoggerFactory.getLogger(NswitchManager.class);
	private SliceTagsManager sliceTagsManager = new SliceTagsManager(); 
	private Map<String, SliceFederations> federatedSlicesMap = new HashMap<String, SliceFederations>();
	private List<String> federatedSlicesNames = new ArrayList<String>();

	/**
	 * Create federation for the given topology and slice of given sliceName
	 * 
	 * @param topology
	 *            that includes resources to federate
	 * @param sliceName
	 *            of the slice under creation
	 */
	public void createFederation(TopologyImpl topology, String sliceName) throws FederationException, Exception {
		if (nswitchFederica == null) {
			throw new FederationException("nswitchFederica object is null ! ");
		}
		if (nswitchPlanetlab == null) {
			throw new FederationException("nswitchPlanetlab object is null ! ");
		}
		if (topology == null) {
			throw new Exception("Topology is null!");
		}
		if (isSetEmpty(topology.getContains())) {
			throw new IncorrectTopologyException("Topology is empty!");
		}

		if (sliceName == null || sliceName.trim().equals("")) {
			throw new IncorrectTopologyException("SliceName is null or empty in topology:" + topology.toString());
		}

		sliceName = sliceName.replace("-", "");
		logger.info("Slice name is " + sliceName + " for topology: " + topology.toString());

		SliceFederations sliceFederations = findFederations(topology);
		sliceFederations.setSliceName(sliceName);
		if (sliceFederations.size() == 0) {
			throw new IncorrectTopologyException("Given topology does not contain any federations");
		}
		for (Iterator iterator = sliceFederations.iterator(); iterator.hasNext();) {
			Federation federation = (Federation) iterator.next();
			federation.setSliceName(sliceName);
		}

		createFederationInTestbeds(sliceFederations);
		federatedSlicesMap.put(sliceName, sliceFederations);
		federatedSlicesNames.add(sliceName);
	}

	/**
	 * Calls nswitch-federica and nswitch-planetlab to create federation in
	 * testbeds using drivers
	 * 
	 * @param fedList
	 *            List with federations information needed by drivers
	 * @throws Exception
	 */
	protected void createFederationInTestbeds(List<Federation> fedList) throws FederationException, Exception {
		boolean first = false;
		logger.info("PSNC DEBUG: createFederationInTestbeds");
		
		for (Iterator iterator = fedList.iterator(); iterator.hasNext();) {
			Federation federation = (Federation) iterator.next();
			
			if(!first){
				first = true;
				sliceTagsManager.setupTags(federation);
			}
			logger.info("Createing federation in slice " + federation.getSliceName() + " on node " + federation.getNodeIp());
			
			//try{
				try{
					String plInterface = nswitchPlanetlab.federate(federation.getNodeIp(), 
									federation.getVlan(), 
									federation.getVlan(),
									federation.getSliceName(), 
									federation.getSliverIp(), 
									federation.getNetmask());
				}catch(NullPointerException e){
					throw new FederationException("nswitch-planetlab is null!: " + e.getMessage());
				}
			//}catch(Exception e){
				//e.getClass().
				//logger.error("Error occured during Planetlab federation in slice " + federation.getSliceName() + " on node " + federation.getNodeIp() + ": " + e.getMessage());
				//revertFederation(fedList);
				//throw new FederationException(e.getMessage());
			//}
			federation.setPlanetlabConfigured(true);
			//federation.setPlInterface(plInterface);
			
			try{
				nswitchFederica.federate(federation.getNodeIp(), 
									federation.getVlan(), 
									federation.getVlan(),
									federation.getSliceName(), 
									federation.getSliverIp(), 
									federation.getNetmask());
			}catch(NullPointerException e){
				throw new FederationException("nswitch-federica is null!: " + e.getMessage());
			}
			federation.setFedericaConfigured(true);
		}
	}

	/**
	 * In case when an exception occurs during the federation creation, then all
	 * configuration changes must be reverted
	 * 
	 * @param fedList
	 *            with the federation details included in Federation objects
	 */
	protected void revertFederation(List<Federation> fedList) {
		// TODO Auto-generated method stub

	}

	
	
	@Override
	/**
	 * Release federation in the slice 
	 * @param slice name of the slice to defederate
	 * @throws Exception
	 */
	public void releaseFederation(String sliceName) throws Exception {
		if (sliceName == null | sliceName.trim().equals("")) {
			throw new IncorrectTopologyException("Slice name is null or empty!");
		}

		List<Federation> fedList = federatedSlicesMap.get(sliceName);
		if (fedList == null) {
			throw new IncorrectTopologyException("There is no information about federation for this slice: "
					+ sliceName);

		}
		for (Iterator iterator = fedList.iterator(); iterator.hasNext();) {
			Federation federation = (Federation) iterator.next();
			nswitchPlanetlab.defederate(federation.getNodeIp(), federation.getVlan(), federation.getVlan(),
					federation.getSliceName(), federation.getSliverIp(), federation.getNetmask());

			nswitchFederica.defederate(federation.getNodeIp(), federation.getVlan(), federation.getVlan(),
					federation.getSliceName(), federation.getSliverIp(), federation.getNetmask());
		}
	}

	/**
	 * Just checks if set is null or empty
	 * 
	 * @param set
	 *            to check
	 * @return true if set is null or does not contain any elements. Returns
	 *         false otherwise.
	 */
	protected boolean isSetEmpty(Set<? extends Object> set) {
		if (set == null || set.isEmpty())
			return true;
		else
			return false;
	}

	/**
	 * Method finds federations in the given topology. It discovers all the
	 * topology and search for the resources to federate. It collects all needed
	 * data for the federation
	 * 
	 * @param topology
	 * @return list with the federations that contains elements with all the
	 *         needed information to create federation on particular resources
	 * @throws IncorrectTopologyException
	 */
	protected SliceFederations findFederations(TopologyImpl topology) throws IncorrectTopologyException {
		SliceFederations federationList = new SliceFederationsImpl();

		for (Resource r : topology.getContains()) {
			if (r instanceof VirtualNode) {
				VirtualNode vnode = (VirtualNode) r;

				if (isSetEmpty(vnode.getImplementedBy())) {
					throw new IncorrectTopologyException("No phisycal node for virtual node " + vnode.toString());
				}
				Node srcNode = vnode.getImplementedBy().iterator().next();
				if (srcNode == null) {
					throw new IncorrectTopologyException("Phisycal node for virtual node " + vnode.toString()
							+ " is null");
				}
				if (!srcNode.getHardwareType().equals(Node.HARDWARE_TYPE_ROUTER)) {
					continue;
				}
				collectFederationsData(topology, srcNode.getHasInboundInterfaces(), federationList);
				logger.info("PSNC DEBUG: after collectFederations");
			}
		}
		logger.info("PSNC DEBUG: before setting the  topology");
		federationList.setTopology(topology);
		return federationList;
	}

	/**
	 * Collects drivers data for all founded federations. Firt it founds nswitch
	 * links and for each of it, it collects needed information
	 * 
	 * @param topology
	 *            that is given in request
	 * @param interfaces
	 *            that are connected to node that implements vnode
	 * @param federationList
	 *            to add federation information
	 * @throws IncorrectTopologyException
	 */
	protected void collectFederationsData(TopologyImpl topology, Set<Interface> interfaces,
			List<Federation> federationList) throws IncorrectTopologyException {

		// Just for logging
		logger.info("Number of interfaces: " + interfaces.size());
		int i = 0;
		for (Interface intf : interfaces) {
			logger.info("PSNC Interface " + i + " : " + intf.toString());
			i++;
		}
		// end of logging

		Set<NSwitch> nswitchLinksSink = findNswitchLinksInInterfacesSink(interfaces);
		if (!isSetEmpty(nswitchLinksSink)) {
			for (NSwitch nswitchLink : nswitchLinksSink) {
				logger.info("Found nswitchLinkSink" + nswitchLink.toString());
				getFederationData(topology, nswitchLink, federationList);
				logger.info("PSNC DEBUG: afterGetFederation sink");
			}
		}
		Set<NSwitch> nswitchLinksSource = findNswitchLinksInInterfacesSource(interfaces);
		if (!isSetEmpty(nswitchLinksSource)) {
			for (NSwitch nswitchLink : nswitchLinksSource) {
				logger.info("Found nswitchLinkSource" + nswitchLink.toString());
				getFederationData(topology, nswitchLink, federationList);
				logger.info("PSNC DEBUG: after get federation source");
			}
		}
	}

	/**
	 * Collects information needed by nswitch drivers for particular federation
	 * 
	 * @param topology
	 *            that is given in request
	 * @param nswitchLink
	 *            that links resources from two remote testbeds
	 * @param federationList
	 *            to add federation information
	 * @throws IncorrectTopologyException
	 */
	protected void getFederationData(TopologyImpl topology, NSwitch nswitchLink, List<Federation> federationList)
			throws IncorrectTopologyException {
		if (isSetEmpty(nswitchLink.getHasVLANID())) {
			throw new IncorrectTopologyException(" VLAN ID set is empty in nswitch!");
		}
		String vlanId = nswitchLink.getHasVLANID().iterator().next();
		if (vlanId.trim().equals("")) {
			throw new IncorrectTopologyException(" VLAN ID is empty in nswitch!");
		}
		Federation federation = new Federation();
		federation.setVlan(vlanId);

		Set<Interface> nodeInterfaces = nswitchLink.getHasSource();

		if (isSetEmpty(nodeInterfaces)) {
			throw new IncorrectTopologyException("Nswitch with vlanID" + federation.getVlan()
					+ " has empty set of sources");
		}

		Interface nswitchSource = nodeInterfaces.iterator().next();
		// Set<InterfaceOrNode> connectedTo = nswitchSource.getConnectedTo();
		Set<Node> dstSet;
		Set<Node> inboundSet = nswitchSource.getIsInboundInterfaceOf();
		if (isSetEmpty(inboundSet)) {
			dstSet = nswitchSource.getIsOutboundInterfaceOf();
		} else {
			dstSet = inboundSet;
		}

		if (isSetEmpty(dstSet)) {
			throw new IncorrectTopologyException("Interface " + nswitchSource.toString() + " linekd to nswitch "
					+ nswitchLink.toString()
					+ " has empty both InboundInterfaceOf and OutboundInterfaceOf sets null or empty");
		}
		Node dstNode = dstSet.iterator().next();
		federation.setNodeIp(getIpOfNode(dstNode));
		federation.setPlNode(dstNode);

		String ip = getIpOfNode(getVirtualNodeHostedByNode(topology, federation.getPlNode()));
		logger.info("Sliver on node " + dstNode.toString() + " has ip: " + ip);
		federation.setSliverIp(ip);
		logger.info("PSNC DEBUG: After setSliverIp");
		federationList.add(federation);
	}

	/**
	 * Finds all the nswitch links that are connected to sink of the given
	 * interfaces.
	 * 
	 * @param interfaces
	 * @return list with nswitch links
	 * @throws IncorrectTopologyException
	 */
	protected Set<NSwitch> findNswitchLinksInInterfacesSink(Set<Interface> interfaces)
			throws IncorrectTopologyException {
		Set<NSwitch> nswitchLinks = new HashSet<NSwitch>();
		if (!isSetEmpty(interfaces)) {
			for (Interface intf : interfaces) {
				if (intf == null) {
					throw new IncorrectTopologyException("One of the interfaces is null for Interfaces set: "
							+ interfaces.toString());
				}
				if (!isSetEmpty(intf.getIsSink())) {
					for (LinkOrPath link : intf.getIsSink()) {
						addLinkToListWhenIsNSwitch(link, nswitchLinks);
					}
				}
			}
		}
		return nswitchLinks;
	}

	/**
	 * Finds all the nswitch links that are connected to source of the given
	 * interfaces.
	 * 
	 * @param interfaces
	 * @return list with nswitch links
	 * @throws IncorrectTopologyException
	 */
	protected Set<NSwitch> findNswitchLinksInInterfacesSource(Set<Interface> interfaces)
			throws IncorrectTopologyException {
		Set<NSwitch> nswitchLinks = new HashSet<NSwitch>();
		if (!isSetEmpty(interfaces)) {
			for (Interface intf : interfaces) {
				if (intf == null) {
					throw new IncorrectTopologyException("One of the interfaces is null for Interfaces set: "
							+ interfaces.toString());
				}
				if (!isSetEmpty(intf.getIsSource())) {
					for (LinkOrPath link : intf.getIsSource()) {
						addLinkToListWhenIsNSwitch(link, nswitchLinks);
					}
				}
			}
		}
		return nswitchLinks;
	}

	/**
	 * Checks if the given link is nswitch link and add it to the set if it is.
	 * 
	 * @param link
	 *            to check
	 * @param set
	 *            to add nswitch link
	 * @throws IncorrectTopologyException
	 */
	protected void addLinkToListWhenIsNSwitch(LinkOrPath link, Set<NSwitch> set) throws IncorrectTopologyException {
		if (link == null) {
			throw new IncorrectTopologyException("One of the  links  in interface is null");
		}
		if (link instanceof NSwitch) {
			NSwitch nswitchLink = (NSwitch) link;
			if (isSetEmpty(link.getHasSink())) {
				throw new IncorrectTopologyException("nSwitchLink sink is empty for nswitchLink "
						+ nswitchLink.toString());
			}
			if (isSetEmpty(link.getHasSource())) {
				throw new IncorrectTopologyException("nSwitchLink source is empty! for nswitchLink "
						+ nswitchLink.toString());
			}
			set.add(nswitchLink);
		}
	}

	/**
	 * Returns ip of the sliver
	 * 
	 * @param vnode
	 *            sliver to find the ip
	 * @return slvier ip
	 * @throws IncorrectTopologyException
	 */
	protected String getIpOfNode(Node node) throws IncorrectTopologyException {
		if (isSetEmpty(node.getHasInboundInterfaces())) {
			throw new IncorrectTopologyException("No Inbound interface for virtual node: " + node.toString());
		}

		if (isSetEmpty(node.getHasInboundInterfaces().iterator().next().getHasIPv4Address())) {
			throw new IncorrectTopologyException("HasIPv4Address set is null for virtual node: " + node.toString());
		}
		
		IPAddress ip = node.getHasInboundInterfaces().iterator().next().getHasIPv4Address().iterator().next();
		if (ip.getHasValue()== null | ip.getHasValue().trim().equals("")) {
			throw new IncorrectTopologyException("IPAdress value is null or empty for virtual node "
					+ node.toString());
		}
		return ip.getHasValue();
	}

	/**
	 * Returns virtual node implemented by the given node.
	 * 
	 * @param topology
	 *            to search on
	 * @param node
	 *            that implements the virtual node to be founds
	 * @return virtual node implemented by the given node
	 * @throws IncorrectTopologyException
	 */
	protected VirtualNode getVirtualNodeHostedByNode(TopologyImpl topology, Node node)
			throws IncorrectTopologyException {
		for (Resource r : topology.getContains()) {
			if (r instanceof VirtualNode) {
				if (isSetEmpty(((VirtualNode) (r)).getImplementedBy())) {
					throw new IncorrectTopologyException("No phisycal node for virtual node "
							+ ((VirtualNode) (r)).toString());
				}
				Node fNode = ((VirtualNode) (r)).getImplementedBy().iterator().next();
				if (fNode.getHostname().equals(node.getHostname())) {
					return (VirtualNode) (r);
				}
			}
		}
		throw new IncorrectTopologyException("No VirtualNode hosted by node: " + node.toString());
	}

	public Nswitch getNswitchFederica() {
		return nswitchFederica;
	}

	public void setNswitchFederica(Nswitch nswitchFederica) {
		this.nswitchFederica = nswitchFederica;
	}

	public Nswitch getNswitchPlanetlab() {
		return nswitchPlanetlab;
	}

	public void setNswitchPlanetlab(Nswitch nswitchPlanetlab) {
		this.nswitchPlanetlab = nswitchPlanetlab;
	}

	/**
	 * Private data class storing information required by drivers to federate
	 * resources
	 * 
	 * @author Piotr Pikusa
	 * 
	 */
	public SliceTagsManager getSliceTagsManager() {
		return sliceTagsManager;
	}

	public void setSliceTagsManager(SliceTagsManager sliceTagsManager) {
		this.sliceTagsManager = sliceTagsManager;
	}

}
