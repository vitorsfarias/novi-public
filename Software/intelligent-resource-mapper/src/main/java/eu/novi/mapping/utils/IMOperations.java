/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osgi.service.log.LogService;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Memory;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;

/**
 * Information Model Operations
 */
public class IMOperations {

	/**
	 * Private constructor
	 */
	private IMOperations(){}
	
	/**
	 * Gets the Source Physical Node of the Virtual Link.
	 *
	 * @param link the link
	 * @return the Physical Source Node of the Virtual Link
	 */
	public static Node getSourceNode(VirtualLink link) {
		if (!isSetEmpty(link.getHasSource())) {
			Interface iface = link.getHasSource().iterator().next();
			if (!isSetEmpty(iface.getIsOutboundInterfaceOf())) {
				Node vNode = iface.getIsOutboundInterfaceOf().iterator().next();
				return vNode.getImplementedBy().iterator().next();
			}
		}
		return null;
	}
	
	/**
	 * Gets the Target Physical Node of the Virtual Link.
	 *
	 * @param link the link
	 * @return the Physical Target Node of the Virtual Link
	 */
	public static Node getTargetNode(VirtualLink link) {
		if (!isSetEmpty(link.getHasSink())) {
			Interface iface = link.getHasSink().iterator().next();
			if (!isSetEmpty(iface.getIsInboundInterfaceOf())) {
				Node vNode = iface.getIsInboundInterfaceOf().iterator().next();
				return vNode.getImplementedBy().iterator().next();
			}
		}
		return null;
	}
	
	/**
	 * Gets the source interface of the requested link.
	 *
	 * @param link the link to bound
	 * @return the source interface of the requested link. Null if it doesn't exist
	 */
	public static Interface getSourceInterface(Link link) {
		if (!isSetEmpty(link.getHasSource())) {
			return link.getHasSource().iterator().next();
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the target interface of the requested link.
	 *
	 * @param link the link to bound
	 * @return the target interface of the requested link. Null if it doesn't exist
	 */
	public static Interface getTargetInterface(Link link) {
		if (!isSetEmpty(link.getHasSink())) {
			return link.getHasSink().iterator().next();
		} else {
			return null;
		}
	}
	
	/**
	 * Get the resource ID (removing http://fp7-novi.eu/im.owl# prefix and the uri prefix)
	 *
	 * @param resourceUri the resource uri
	 * @return the resource name
	 */
	public static String getResourceName(String resourceUri) {
		String resourceId = getId(resourceUri);
		String[] components = resourceUri.split("\\+");
		if (components.length>1) {
			return components[components.length-1];
		} else {
			return resourceId;
		}
	}
	
	/**
	 * Get the resource ID (removing http://fp7-novi.eu/im.owl# prefix)
	 * @param resource
	 * @return
	 */
	public static String getId(String resource) {
		String[] components = resource.split("\\#");
		return components[components.length-1];
	}
	
	/**
	 * Checks if set is null or empty.
	 *
	 * @param set the set of resources
	 * @return true if set is null or does not contain any elements. Returns
	 * false otherwise.
	 */
	public static boolean isSetEmpty(Set<? extends Object> set) {
		return (set == null || set.isEmpty());
	}
	
	/**
	 * Gets the slice id from its URN.
	 *
	 * @param sliceURN the slice URN
	 * @return the slice ID
	 */
	public static int getSliceID(String sliceURN) {
		String sliceID = sliceURN.replace("slice_", "");
		return Integer.parseInt(sliceID);
	}
	
	/*************************************************************
	***************** Analyze Topology Methods  ******************
	*************************************************************/
	
	/**
	 * Put into the log the given group details
	 * PRE: Group is well formed
	 * @param group
	 */
	public static void analyzeGroup(Group group, LogService logService) {
		logService.log(LogService.LOG_INFO,"Analyzing: "+group.toString());
		
		if (IMOperations.isSetEmpty(group.getContains())) {return;}
		
		Iterator<Resource> vResourceIt = group.getContains().iterator();
		while (vResourceIt.hasNext()) {
			Resource resource = vResourceIt.next();
			if (resource instanceof Node) {
				logService.log(LogService.LOG_INFO,"Node: "+resource.toString());
				if (!IMOperations.isSetEmpty(resource.getIsContainedIn())) {
					for (Group g : resource.getIsContainedIn()) {
						logService.log(LogService.LOG_INFO,
								"Node: "+resource.toString()+" contained in "+g);
					}
				}
				analyzeNode((Node) resource, logService);
				if (((Node) resource).getImplementedBy()==null) {
					logService.log(LogService.LOG_INFO, "	Implemented by: -");
				}
				else {
					Node n = ((Node) resource).getImplementedBy().iterator().next();
					logService.log(LogService.LOG_INFO, "	Implemented by: "+n.toString());
					analyzeNode(n, logService);
				}
			}
			if (resource instanceof Link) {
				logService.log(LogService.LOG_INFO,"Link: "+resource.toString());
				if (((Link) resource).getProvisionedBy()==null) {
					logService.log(LogService.LOG_INFO, "Provisioned by: -");
				}
				else {
					Path p = (Path) ((Link) resource).getProvisionedBy().iterator().next();
					logService.log(LogService.LOG_INFO, "	Provisioned by: "+p.toString());
					logService.log(LogService.LOG_INFO, "	Path capacity: "+p.getHasCapacity());
					if (!IMOperations.isSetEmpty(p.getContains())) {
						logService.log(LogService.LOG_INFO, "	Path resources:");
						for (Resource res : p.getContains()) {
							logService.log(LogService.LOG_INFO, "		"+res.toString());
						}
							
					}
				}
			}
		}
	}
	
	/**
	 * Put into the log the given node details
	 * @param n
	 */
	public static void analyzeNode(Node n, LogService logService) {
		if (n.getHasComponent()!=null) {
			for (Resource comp : n.getHasComponent()) {
				if (comp instanceof CPU) {
					logService.log(LogService.LOG_INFO, 
							"		CPU speed: "+((CPU) comp).getHasCPUSpeed()+"GHz");
					logService.log(LogService.LOG_INFO, 
							"		Available CPU cores: "+((CPU) comp).getHasAvailableCores());
				} else if (comp instanceof Memory) {
					logService.log(LogService.LOG_INFO, 
							"		Available Memory: "+((Memory) comp).getHasAvailableMemorySize()+"GB");
				} else if (comp instanceof Storage) {
					logService.log(LogService.LOG_INFO, 
							"		Available Storage: "+((Storage) comp).getHasAvailableStorageSize()+"GB");
				}
			}
		}
		if (!IMOperations.isSetEmpty(n.getHasOutboundInterfaces())) {
			for (Interface iface : n.getHasOutboundInterfaces()) {
				logService.log(LogService.LOG_INFO,
						"		Node outbound Interface: "+iface.toString());
				analyzeInterface(iface, logService);
			}
		}
		if (!IMOperations.isSetEmpty(n.getHasInboundInterfaces())) {
			for (Interface iface : n.getHasInboundInterfaces()) {
				logService.log(LogService.LOG_INFO,
						"		Node inbound Interface: "+iface.toString());
				analyzeInterface(iface, logService);
			}
		}
	}
	
	/**
	 * Put into the log the given interface details
	 * @param iface
	 */
	public static void analyzeInterface(Interface iface, LogService logService) {
		if (!IMOperations.isSetEmpty(iface.getImplementedBy())) {
			for (Interface pIface : iface.getImplementedBy()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is implemented by: "+pIface.toString());
			}
		}
		else {
			logService.log(LogService.LOG_INFO, "implemented by: -");
		}
		if (!IMOperations.isSetEmpty(iface.getIsSource())) {
			for (LinkOrPath lop : iface.getIsSource()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is source of: "+lop.toString());
			}
		}
		if (!IMOperations.isSetEmpty(iface.getIsSink())) {
			for (LinkOrPath lop : iface.getIsSink()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is sink of: "+lop.toString());
			}
		}
		if (!IMOperations.isSetEmpty(iface.getNexts())) {
			for (NetworkElementOrNode neon : iface.getNexts()) {
				logService.log(LogService.LOG_INFO,
						"		Interface next element: "+neon.toString());
			}
		}
		if (!IMOperations.isSetEmpty(iface.getInPaths())) {
			for (LinkOrPath lop : iface.getInPaths()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is in path: "+lop.toString());
			}
		}
		if (!IMOperations.isSetEmpty(iface.getIsInboundInterfaceOf())) {
			for (Node node : iface.getIsInboundInterfaceOf()) {
				logService.log(LogService.LOG_INFO,
						"		Iface is INbound of node: "+node.toString());
			}
		}
		if (!IMOperations.isSetEmpty(iface.getIsOutboundInterfaceOf())) {
			for (Node node : iface.getIsOutboundInterfaceOf()) {
				logService.log(LogService.LOG_INFO,
						"		Iface is OUTbound of node: "+node.toString());
			}
		}
	}

	/**
	 * Gets the virtual request from the groups collection. If the request is a Reservation 
	 * instance, then it is translated to Topology.
	 *
	 * @param groups the groups where the virtual request is
	 * @return the virtual request
	 */
	public static Topology getVirtualRequest(Collection<GroupImpl> groups) {
		for (Group group : groups) {
			if (isSetEmpty(group.getContains())) {
				continue;
			}
			if (group instanceof Topology) {
				return (Topology) group;
			} else if (group instanceof Reservation) {
				Topology toReturn = new TopologyImpl(getId(group.toString()));
				toReturn.setContains(group.getContains());
				return toReturn;
			}
		}
		return new TopologyImpl("emptyGroup");
	}

	
		/**
	 * Gets the virtual request from the groups collection. If the request is a Reservation 
	 * instance, then it is translated to Topology.
	 *
	 * @param groups the groups where the virtual request is
	 * @return the virtual request
	 */
	public static Topology getVirtualRequest(Collection<GroupImpl> groups,  LogService logService) {
		for (Group group : groups) {
			if (isSetEmpty(group.getContains())) {
				continue;
			}
			if (group instanceof Topology) {
				return (Topology) group;
			} else if (group instanceof Reservation) {
				Topology toReturn = new TopologyImpl(getId(group.toString()));
				toReturn.setContains(group.getContains());
				return toReturn;
			}
		}
		return new TopologyImpl("emptyGroup");
	}
	
	/**
	 * Gets the platforms. It creates new platforms in order to 
	 * have their resources available through getIsContainedIn.
	 *
	 * @param groups the groups
	 * @param virtualTopology the virtual topology
	 * @return the platforms
	 */
	public static Set<Platform> getPlatforms(Collection<GroupImpl> groups, Group virtualTopology) {
		
		Set<Platform> platforms = new HashSet<Platform>();
		
		for (Group group : groups) {
			if (group instanceof Platform && 
					!IMOperations.isSetEmpty(group.getContains())) {
				PlatformImpl platformToAdd = 
					copyPlatformResources((Platform) group, virtualTopology);
				platforms.add(platformToAdd);
			}
		}
		
		return platforms;
	}
	
	/**
	 * Copy into the returned platform the resources belonging t
	 * o platform in the virtualTopolopy. This is done in order to 
	 * have their resources available through getIsContainedIn.
	 *
	 * @param platform the platform
	 * @param virtualTopology the virtual topology request
	 * @return the platform impl
	 */
	private static PlatformImpl copyPlatformResources(Platform platform,
			Group virtualTopology) {
		
		PlatformImpl platformToAdd = new PlatformImpl(IMOperations.getId(platform.toString()));
		Set<Resource> resToAdd = new HashSet<Resource>();
		for (Resource platformRes : platform.getContains()) {
			for (Resource reqRes : virtualTopology.getContains()) {
				if (platformRes.toString().equals(reqRes.toString())) {
					resToAdd.add(reqRes);
					break;
				}
			}
		}
		platformToAdd.setContains(resToAdd);

		return platformToAdd;
	}

	/**
	 * Puts the node mapping of a topology into a string.
	 *
	 * @param fullBoundedTopology the full bounded topology
	 * @return the string
	 */
	public static String mappingToString(Topology topology, LogService logService) {
		String toReturn = "";
		logService.log(LogService.LOG_INFO,"Preparing to print mapping info for created slice");
		if (!isSetEmpty(topology.getContains())) {
			for (Resource res : topology.getContains()) {
				logService.log(LogService.LOG_INFO,"Topology is not empty");
				if (res instanceof Node) {
					if (!isSetEmpty(((Node) res).getImplementedBy())) {
						logService.log(LogService.LOG_INFO, "Virtual Resource: " + res.toString());
						Node pNode = ((Node) res).getImplementedBy().iterator().next();
						logService.log(LogService.LOG_INFO, "Physical Resource: " + pNode.toString());
						toReturn +=" Virtual Node: "+res.toString()+ " allocated to: URN="+pNode.toString()+ "and Hostname = " +pNode.getHostname() + ". \n";
					}
					
				}
			}
		}
		return toReturn;
	}
	
}
