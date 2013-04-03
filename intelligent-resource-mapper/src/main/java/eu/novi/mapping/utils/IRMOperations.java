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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NSwitchImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.mapping.impl.CreateEngine;
import eu.novi.resources.discovery.IRMCalls;

/**
 * IRM Operations
 */
public final class IRMOperations {
	 /** Local logging. */
	private static final transient Logger log = LoggerFactory.getLogger(IRMOperations.class);
	/**
	 * Private constructor
	 */
	private IRMOperations(){}
	
	/**
	 * Translates the sessionID to int.
	 *
	 * @param sessionID the session id
	 * @return the uuid
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public static int getUUID(String sessionID) throws NoSuchAlgorithmException {
	    SecureRandom srA = SecureRandom.getInstance("SHA1PRNG");
	    srA.setSeed(sessionID.getBytes());
	    return Math.abs(Integer.valueOf(srA.nextInt()));
	}
	
	
	/**
	 * Gets the source nSwitch interface of the node. If more than one, it gets the
	 * first one.
	 *
	 * @param node the node
	 * @return the source nSwitch interface
	 */
	public static Interface getSourceNSwitchInterface(Node node) {
		Set<Interface> ifaces = node.getHasOutboundInterfaces();
		for (Interface iface : ifaces) {
			if (iface.getCanFederate()) {
				return iface;
			} 
		}
		return null;
	}
	
	/**
	 * Gets the target nSwitch interface of the node. If more than one, it gets the
	 * first one.
	 *
	 * @param node the node
	 * @return the target nSwitch interface
	 */
	public static Interface getTargetNSwitchInterface(Node node) {
		Set<Interface> ifaces = node.getHasInboundInterfaces();
		for (Interface iface : ifaces) {
			if (iface.getCanFederate()) {
				return iface;
			} 
		}
		return null;
	}

	/**
	 * PRE: currentSlice is well formed
	 * PRE: resourceID is the ID of a VirtualNode
	 * 
	 * Get the host of the vnode with resourceID ID.
	 *
	 * @param currentSlice the current slice
	 * @param resourceID the resource ID
	 * @return Physical node ID
	 */
	public static String getPhysicalResourceID(Group currentSlice, String resourceID) {
		for (Resource res : currentSlice.getContains()) {
			if (res.toString().equals(resourceID)) {
				return ((Node) res).getImplementedBy()
					.iterator().next().toString();
			}
		}
		return null;
	}
	
	/**
	 * PRE: currentSlice is well formed
	 * Unbound from the currentSlice the failing resources
	 * @param currentSlice
	 * @param failingPhysicalResourcesIDs
	 * @return copy of the Slice with the failing resources unbounded
	 */
	public static Topology disconnectFailingResources(Group currentSlice,
			Set<String> failingPhysicalResourcesIDs) {
		
		// Making a copy of the slice and translating it to Topology
		IMCopy imc = new IMCopy();
		Reservation sliceCopy = (Reservation) imc.copy(currentSlice, -1);
		Topology result = new TopologyImpl(sliceCopy.toString());
		result.setContains(sliceCopy.getContains());
		
		Node pNode = null;
		Path path = null;
		
		for (String pResID : failingPhysicalResourcesIDs) {
			for (Resource vRes : result.getContains()) {
				if (vRes instanceof Node) {
					pNode = ((Node) vRes).getImplementedBy().iterator().next();
					if (pResID.equals(pNode.toString())) {
						// Disconnect the Node
						disconnectNode((Node) vRes);
						break;
					}
				}
				else if (vRes instanceof Link) {
					// could be already disconnected
					if (!IMOperations.isSetEmpty(((Link) vRes).getProvisionedBy())) {
						path = (Path) ((Link) vRes).getProvisionedBy().iterator().next();
						if (pResID.equals(path)) {
							((Link) vRes).setProvisionedBy(null);
							break;
						}
					}
				}
			}
		}
		return result;
	}
	 
	/**
	 * Disconnect virtual node from its physical node.
	 *
	 * @param vNode the virtual node
	 */
	private static void disconnectNode(Node vNode) {
		((Node) vNode).setImplementedBy(null);
		// Disconnect adjacent Links
		if (!IMOperations.isSetEmpty(((Node) vNode).getHasInboundInterfaces())) {
			for (Interface inIface : ((Node) vNode).getHasInboundInterfaces()) {
				if (!IMOperations.isSetEmpty(inIface.getIsSink())) {
					Link inLink = (Link) inIface.getIsSink().iterator().next();
					inLink.setProvisionedBy(null);
				}
			}
		}
		if (!IMOperations.isSetEmpty(((Node) vNode).getHasOutboundInterfaces())) {
			for (Interface outIface : ((Node) vNode).getHasOutboundInterfaces()) {
				if (!IMOperations.isSetEmpty(outIface.getIsSource())) {
					Link outLink = (Link) outIface.getIsSource().iterator().next();
					outLink.setProvisionedBy(null);
				}
			}
		}
	}

	/**
	 * Checks and marks nodes to federate setting the property canFederate on the
	 * needed interfaces.
	 *
	 * @param virtualTopology the virtual topology
	*/
	public static void checkAndMarkNodesToFederate(Topology virtualTopology) {	
		for (Resource res : virtualTopology.getContains()) {
			if (res instanceof Link) {
				Interface sinkIface = ((Link) res).getHasSink().iterator().next();
				Node sinkNode= sinkIface.getIsInboundInterfaceOf().iterator().next();
				Interface sourceIface = ((Link) res).getHasSource().iterator().next();
				Node sourceNode= sourceIface.getIsOutboundInterfaceOf().iterator().next();
				Platform sourcePlatform = null;
				Platform targetPlatform = null;
				for (Group group : sinkNode.getIsContainedIn()) {
					if (group instanceof Platform) {
						targetPlatform = (Platform) group;
						break;
					}
				}
				for (Group group : sourceNode.getIsContainedIn()) {
					if (group instanceof Platform) {
						sourcePlatform = (Platform) group;
						break;
					}
				}
				if (!sourcePlatform.toString().equals(targetPlatform.toString())) {
					//mark interfaces with canFederate
					sinkIface.setCanFederate(true);
					sourceIface.setCanFederate(true);
				}
			}
		}
	}

	/**
	 * Return the virtual topology split into bounded and unbounded resources.
	 * Platform bounded resources are set as unbounded.
	 * 
	 * PRE: It is assumed that the virtualTopology is well formed.
	 * 
	 * @param virtualTopology the virtual topology to split
	 * @return the virtual topology split into bounded and unbounded resources
	 */
	public static Topology[] checkBoundUnboundRequest(Topology virtualTopology) {
		Topology[] toReturn = new Topology[2];
		Topology unboundTopology = new TopologyImpl("unboundResources");
		Topology boundTopology = new TopologyImpl("boundResources");
		Set<Resource> unboundResources = new HashSet<Resource>();
		Set<Resource> boundResources = new HashSet<Resource>();
		Iterator<Resource> reqResourceIt = virtualTopology.getContains().iterator();
		while (reqResourceIt.hasNext()) {
			Resource resource = reqResourceIt.next();
			if (resource instanceof VirtualNode) {
				if (IMOperations.isSetEmpty(((VirtualNode) resource).getImplementedBy())) {
					unboundResources.add(resource);
				}
				else {
					boundResources.add(resource);
				}
			} else if (resource instanceof VirtualLink) {
				if (IMOperations.isSetEmpty(((VirtualLink) resource).getProvisionedBy())) {
					unboundResources.add(resource);
				}
				else {
					boundResources.add(resource);
				}
			}
		}
		unboundTopology.setContains(unboundResources);
		boundTopology.setContains(boundResources);
		toReturn[0] = boundTopology;
		toReturn[1] = unboundTopology;
		return toReturn;
	}

	/**
	 * Gather all the physical resources and check if the bounded physical resources exists.
	 *
	 * @param boundedTopology the bounded topology
	 * @param virtualTopology the virtual topology
	 * @param noviUser the novi user
	 * @return the non existing resource. Null otherwise
	 */
	public static Set<Resource> checkPhysicalResources(Topology boundedTopology, IRMCalls irmCallsFromRIS, 
			NOVIUserImpl noviUser) {
		Set<Resource> physicalResourcesToCheck = new HashSet<Resource>();
		Iterator<Resource> vResourceIt = boundedTopology.getContains().iterator();
		while (vResourceIt.hasNext()) {
			Resource resource = vResourceIt.next();
			/** 
			 * NOTE: Physical Interfaces and Physical Links are are obtained through VirtualNode
			 * and next relations. They would be also obtained from 
			 * VirtualLink -> provisionedBy Path -> getContains
			 */
			if (resource instanceof Node) {
				Node pNode = ((Node) resource).getImplementedBy().iterator().next();
				// Adding Physical Node to check list
				physicalResourcesToCheck.add(pNode);
				// Adding Physical Node Interfaces and connected Links to check list
				Set<Interface> ifaces = new HashSet<Interface>();
				if (!IMOperations.isSetEmpty(pNode.getHasInboundInterfaces())) {
					ifaces.addAll(pNode.getHasInboundInterfaces());
				}
				if (!IMOperations.isSetEmpty(pNode.getHasOutboundInterfaces())) {
					ifaces.addAll(pNode.getHasOutboundInterfaces());
				}
				for (Interface iface : ifaces) {
					physicalResourcesToCheck.addAll(ifaces);
					// Adding "next" resources to check list
					NetworkElementOrNode currentNE = iface;
					while (!IMOperations.isSetEmpty(currentNE.getNexts())) {
						currentNE = currentNE.getNexts().iterator().next();
						// Do not check NSwitch Links
						if (!(currentNE instanceof NSwitch)) {
							physicalResourcesToCheck.add(currentNE);
						}
					}
				}
			}
		}
		
		return checkPhysicalResourcesAgainstRIS(physicalResourcesToCheck,irmCallsFromRIS,noviUser);
		
	}
	
	/**
	 * Check physical resources against RIS component.
	 *
	 * @param physicalResourcesToCheck the physical resources to check
	 * @param noviUser the novi user
	 * @return the non existing resource. Null otherwise
	 */
	private static Set<Resource> checkPhysicalResourcesAgainstRIS(
			Set<Resource> physicalResourcesToCheck, IRMCalls irmCallsFromRIS, NOVIUserImpl noviUser) {
		
		// Checking resources calling RIS (checkResources)
		Set<String> physicalResourceIDs = new HashSet<String>();
		for (Resource pRes : physicalResourcesToCheck) {
			physicalResourceIDs.add(pRes.toString());
		}
		
		Set<String> existingResourceIDs = irmCallsFromRIS.checkResources(null, physicalResourceIDs,noviUser);
		if (existingResourceIDs.size()!=physicalResourceIDs.size()) {
			// There are resources not found in the NOVI DB
			Set<Resource> unexistingResources = new HashSet<Resource>();
			boolean found;
			for (Resource pRes : physicalResourcesToCheck) {
				found=false;
				for (String eResID : existingResourceIDs) {
					if (pRes.toString().equals(eResID)) {
						found=true;
						break;
					}
				}
				if (!found) {
					unexistingResources.add(pRes);
				}
			}
			return unexistingResources;
		}
		
		// All resources exist in the RIS DB
		return null;
	}
	
	/**
	 * Create an Inter Domain Link
	 * @param linkToBound
	 * @param virtualRequest
	 * @return
	 */
	public static Path createInterDomainLink(VirtualLink linkToBound) {
		
		// getting physical endpoints
		Node sourceNode = IMOperations.getSourceNode(linkToBound);
		Node targetNode = IMOperations.getTargetNode(linkToBound);
		log.debug("sourceNode "+sourceNode.toString()+" targetNode" + targetNode.toString());
		if (sourceNode==null || targetNode==null) {
			return null;
		}
		
		// obtaining NSwitch interfaces
		Interface sourcePInterface = IRMOperations.getSourceNSwitchInterface(sourceNode);
		Interface targetPInterface = IRMOperations.getTargetNSwitchInterface(targetNode);
		log.debug("sourcePInterface "+sourcePInterface.toString()+" targetPInterface" + targetPInterface.toString());
		if (sourcePInterface==null || targetPInterface==null) {
			return null;
		}
		
		return createNSwitchPath(sourcePInterface, 
				targetPInterface, linkToBound);
		
	}
	
	/**
	 * Create a NSwitch Path between sourceInterface and targetInterface
	 * for the virtual link linkToBound
	 * @param sourcePInterface
	 * @param targetPInterface
	 * @return NSwitch Path
	 */
	public static Path createNSwitchPath(Interface sourcePInterface,
			Interface targetPInterface, VirtualLink linkToBound) {
		
		// creating Path
		Path interPath = new PathImpl(IRMConstants.PATH_PREFIX+
				IMOperations.getResourceName(sourcePInterface.toString())+"-"+
				IMOperations.getResourceName(targetPInterface.toString()));
		interPath.setHasCapacity(linkToBound.getHasCapacity());
		
		// creating NSwitch
		NSwitch nSwitch = new NSwitchImpl(IRMConstants.LINK_PREFIX+
				IMOperations.getResourceName(sourcePInterface.toString())+"-"+
				IMOperations.getResourceName(targetPInterface.toString()));
		
		// Adding implementedBy from virtual interfaces to physical interfaces
		Interface sourceVIface = IMOperations.getSourceInterface(linkToBound);
		Interface targetVIface = IMOperations.getTargetInterface(linkToBound);
		if (sourceVIface==null || targetVIface==null) {
			return null;
		}
		Set<Interface> sourceIface = new HashSet<Interface>();
		sourceIface.add(sourcePInterface);
		sourceVIface.setImplementedBy(sourceIface);
		Set<Interface> targetIface = new HashSet<Interface>();
		targetIface.add(targetPInterface);
		targetVIface.setImplementedBy(targetIface);
		
		// connecting interfaces with NSwitch
		if(sourcePInterface.getIsSource() == null){
			Set<LinkOrPath> sourceSet = new HashSet<LinkOrPath>();
			sourcePInterface.setIsSource(sourceSet);
		}
		sourcePInterface.getIsSource().add(nSwitch);
		
		if(targetPInterface.getIsSink() == null){
			Set<LinkOrPath> sinkSet = new HashSet<LinkOrPath>();
			targetPInterface.setIsSink(sinkSet);
		}
		targetPInterface.getIsSink().add(nSwitch);
			
		// connecting NSwitch with interfaces
		Set<Interface> sourceIfaces = new HashSet<Interface>();
		sourceIfaces.add(sourcePInterface);
		nSwitch.setHasSource(sourceIfaces);
		Set<Interface> targetIfaces = new HashSet<Interface>();
		targetIfaces.add(targetPInterface);
		nSwitch.setHasSink(targetIfaces);
		
		// adding elements to the path
		Set<Resource> toAdd = new HashSet<Resource>();
		toAdd.add(sourcePInterface);
		toAdd.add(nSwitch);
		toAdd.add(targetPInterface);
		interPath.setContains(toAdd);
		
		// adding path to the elements
		// source interface
		if(sourcePInterface.getInPaths() == null){
			Set<Path> sourcePathSet = new HashSet<Path>();
			sourcePInterface.setInPaths(sourcePathSet);
		}
		sourcePInterface.getInPaths().add(interPath);
		// target interface
		if(targetPInterface.getInPaths() == null){
			Set<Path> targetPathSet = new HashSet<Path>();
			targetPInterface.setInPaths(targetPathSet);
		}
		targetPInterface.getInPaths().add(interPath);
		// nswitch
		Set<Path> pathToAdd = new HashSet<Path>();
		pathToAdd.add(interPath);
		nSwitch.setInPaths(pathToAdd);
		
		// adding next connections
		// from out iface to NSwitch
		if(sourcePInterface.getNexts() == null){
			Set<NetworkElementOrNode> sourceNextSet = 
				new HashSet<NetworkElementOrNode>();
			sourcePInterface.setNexts(sourceNextSet);
		}
		sourcePInterface.getNexts().add(nSwitch);
		
		// from NSwitch to in iface
		Set<NetworkElementOrNode> nSwitchToIface = new HashSet<NetworkElementOrNode>();
		nSwitchToIface.add(targetPInterface);
		nSwitch.setNexts(nSwitchToIface);
		
		return interPath;
		
	}
	
	/**
	 * Remove from the interfaces the Path in the given virtual link
	 * PRE: Path contains also reverse relations
	 * @param sourceInterface
	 * @param targetInterface
	 * @param linkToChange
	 */
	public static void removeNSwitchPath(VirtualLink linkToChange) {
		
		if (IMOperations.isSetEmpty(linkToChange.getProvisionedBy())) {
			return;
		}
		Path toRemove = (Path) linkToChange.getProvisionedBy().iterator().next();
		
		for (Resource res : toRemove.getContains()) {
			if (res instanceof LinkImpl) {	
				if (!IMOperations.isSetEmpty(((LinkImpl) res).getHasSource())) {
					// removing source iface to nswitch
					Interface sourceInterface = 
						((LinkImpl) res).getHasSource().iterator().next();
					sourceInterface.getIsSource().remove(res);
					// removing source iface next to nswitch
					sourceInterface.getNexts().remove(res);
				}
				// removing target iface to nswitch
				if (!IMOperations.isSetEmpty(((LinkImpl) res).getHasSink())) {
					Interface targetInterface = 
						((LinkImpl) res).getHasSink().iterator().next();
					targetInterface.getIsSink().remove(res);
				}
				
			} else if (res instanceof Interface 
					&& !IMOperations.isSetEmpty(((Interface) res).getInPaths())) {
				// remove interface to path
				((Interface) res).getInPaths().remove(toRemove);
			}
		}
		
	}
	
}
