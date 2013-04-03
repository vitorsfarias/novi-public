/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Memory;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.Group;
import org.osgi.service.log.LogService;

/**
 * Common Embedding Operations for FEDERICA
 */
public final class EmbeddingOperations {

	/** logging. */
	private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingOperations.class);
	
	/**
	 * Private constructor
	 */
	private EmbeddingOperations(){}
	
	/**
	 * Gets the nodes from the graph. Bound Nodes are set
	 * in the first positions.
	 *
	 * @param graph the graph
	 * @return the nodes
	 */
	public static List<Node> getNodes(SparseMultigraph<NodeImpl, LinkImpl> graph) {
		ArrayList<Node> nodeList =new ArrayList<Node>();
		ArrayList<Node> boundNodes =new ArrayList<Node>();
		ArrayList<Node> unboundNodes =new ArrayList<Node>();
		for (Node node: graph.getVertices()) {
			if (!EmbeddingOperations.isSetEmpty(node.getImplementedBy())) {
				boundNodes.add(node);
			} else {
				unboundNodes.add(node);
			}
		}
		nodeList.addAll(boundNodes);
		nodeList.addAll(unboundNodes);
		return nodeList;
	}
	
	/**
	 * Gets the links from the graph. Bound links are set 
	 * in the first positions.
	 *
	 * @param graph the graph
	 * @return the links
	 */
	public static List<Link> getLinks(SparseMultigraph<NodeImpl, LinkImpl> graph) {
		ArrayList<Link> linkList =new ArrayList<Link>();
		ArrayList<Link> boundLinks =new ArrayList<Link>();
		ArrayList<Link> unboundLinks =new ArrayList<Link>();
		for (Link link: graph.getEdges()) {
			if (!EmbeddingOperations.isSetEmpty(link.getProvisionedBy())) {
				boundLinks.add(link);
			} else {
				unboundLinks.add(link);
			}
		}
		linkList.addAll(boundLinks);
		linkList.addAll(unboundLinks);
		return linkList;
	}
	
	/**
	 * Obtain capacities of the virtual resources
	 * @param nodeList
	 * @return capacities of the virtual resources
	 */
	public static float[][] getCapacities (List<Node> nodeList) {
		
		float[][] result =  new float[EmbeddingConstants.NON_FUNCTIONAL_VALUES][nodeList.size()];

		for (int i=0;i<nodeList.size();i++) {  
			if (EmbeddingOperations.isVirtualMachine((VirtualNode) nodeList.get(i))
					&& !EmbeddingOperations.isSetEmpty(nodeList.get(i).getHasComponent())) {
				for (NodeComponent nc : nodeList.get(i).getHasComponent()) {
					if (nc instanceof CPU) {
						BigInteger cores = ((CPU) nc).getHasCores();
						result[0][i] = cores.intValue();
					} else if (nc instanceof Memory) {
						result[1][i] = ((Memory) nc).getHasMemorySize();
					} else if (nc instanceof Storage) {
						result[2][i] = ((Storage) nc).getHasStorageSize();
					}
				}
			}
			if (EmbeddingOperations.isVirtualRouter((VirtualNode) nodeList.get(i))) {
				result[0][i] = 1;//nodeList.get(i).getHasLogicalRouters().floatValue();
			}
			
			/*log.info("Capacities for Node :"+nodeList.get(i));
			log.info("Capacity 0: "+result[0][i]);
			log.info("Capacity 1: "+result[1][i]);
			log.info("Capacity 2: "+result[2][i]);*/
		}
		return result;
	}
	
	/**
	 * Obtain available capacities of the physical resources
	 * @param nodeList
	 * @return available capacities of the physical resources
	 */
	public static float[][] getAvailableCapacities (List<Node> nodeList) {
		
		float[][] result =  new float[EmbeddingConstants.NON_FUNCTIONAL_VALUES][nodeList.size()];

		for (int i=0;i<nodeList.size();i++) {  
			if (EmbeddingOperations.isServer(nodeList.get(i))) {
				for (NodeComponent nc : nodeList.get(i).getHasComponent()) {
					if (nc instanceof CPU) {
						BigInteger cores = ((CPU) nc).getHasAvailableCores();
						result[0][i] = cores.intValue();
					} else if (nc instanceof Memory) {
						result[1][i] = ((Memory) nc).getHasAvailableMemorySize();
					} else if (nc instanceof Storage) {
						result[2][i] = ((Storage) nc).getHasAvailableStorageSize();
					}
				}
			}
			else if (EmbeddingOperations.isRouter(nodeList.get(i))) {
				result[0][i] = nodeList.get(i).getHasAvailableLogicalRouters().floatValue();
			}
			/*log.info("Capacities for Node :"+nodeList.get(i));
			log.info("Capacity 0: "+result[0][i]);
			log.info("Capacity 1: "+result[1][i]);
			log.info("Capacity 2: "+result[2][i]);*/
		}
		return result;
	}
	
	
	/**
	 * Check if the node is a virtual router
	 * @param virtualNode
	 * @return true if it is a virtual router
	 */
	public static boolean isVirtualRouter(VirtualNode virtualNode) {
		return virtualNode.getVirtualRole().equals(EmbeddingConstants.ROUTER_VIRTUAL_ROLE);
	}

	/**
	 * Check if the node is a virtual machine
	 * @param virtualNode
	 * @return true if it is a virtual machine
	 */
	public static boolean isVirtualMachine(VirtualNode virtualNode) {
		return virtualNode.getVirtualRole().equals(EmbeddingConstants.VM_VIRTUAL_ROLE);
	}
	
	/**
	 * Check if the node is a VM server
	 * @param node
	 * @return true if it is a VM server
	 */
	public static boolean isServer(Node node) {
		return node.getHardwareType().equals(EmbeddingConstants.VM_HW_TYPE_1)
		|| node.getHardwareType().equals(EmbeddingConstants.VM_HW_TYPE_2);
	}
	
	/**
	 * Check if the node is a router
	 * @param node
	 * @return true if it is a router
	 */
	public static boolean isRouter(Node node) {
		return node.getHardwareType().equals(EmbeddingConstants.ROUTER_HW_TYPE);
	}

	/**
	 * Chek that a given pair or req/sub nodes are compatible or not
	 * @param reqNode
	 * @param subNode
	 * @return compatible or not
	 */
	public static boolean areCompatible(VirtualNode reqNode, Node subNode) {
		return ((isVirtualRouter(reqNode)&&isRouter(subNode)) 
				|| (isVirtualMachine(reqNode)&&isServer(subNode)));
	}
	
	/**
	 * Build available capacity table for the given graph and nodes
	 * @param sub
	 * @param nodes
	 * @return
	 */
	public static float[][] getAvailableCapTable(SparseMultigraph<NodeImpl, LinkImpl> sub, List<Node> nodes, Map<Node, Integer> fakeIDs ) {
		
		int num = sub.getVertexCount();
		Collection<LinkImpl> edges =  sub.getEdges();
		float[][] tab =  new float[num][num];
		
		for (LinkImpl current : edges) {
			Pair<NodeImpl> currentNodes = sub.getEndpoints(current);
			
			int node1 = fakeIDs.get(currentNodes.getFirst());
			//int node1 = nodes.indexOf(currentNodes.getFirst());
			LOG.debug("Node "+currentNodes.getFirst()+" is identified by: "+node1);
			int node2 = fakeIDs.get(currentNodes.getSecond());
			//int node2 = nodes.indexOf(currentNodes.getSecond());
			LOG.debug("Node "+currentNodes.getSecond()+" is identified by: "+node2);
			
			LOG.debug("Link "+current+" has available capacity: "+current.getHasAvailableCapacity());
			float cap = current.getHasAvailableCapacity();
			
			tab[node1][node2] += cap;
			tab[node2][node1] += cap;
		}
		
		return tab;
	}
	
	
		/**
	 * Gets the capacity table.
	 *
	 * @param g the graph
	 * @return the capacity table
	 */
	public static float[][] getCapTable(SparseMultigraph<NodeImpl, LinkImpl> g) {
		
		List<Node> nodes = getNodes(g);
		int num = g.getVertexCount();
		Collection<LinkImpl> edges =  g.getEdges();
		float[][] tab =  new float[num][num];
		
		for (LinkImpl current : edges) {
			Pair<NodeImpl> currentNodes = g.getEndpoints(current);
				
			int node1 = nodes.indexOf(currentNodes.getFirst());
			LOG.debug("Node "+currentNodes.getFirst()+" is identified by: "+node1);
			int node2 = nodes.indexOf(currentNodes.getSecond());
			LOG.debug("Node "+currentNodes.getSecond()+" is identified by: "+node2);
			
			LOG.debug("Link "+current+" has capacity: "+current.getHasCapacity());
			float cap = current.getHasCapacity();
			
			tab[node1][node2] += cap;
			tab[node2][node1] += cap;
		}
		
		return tab;
	}
	
	public static List<int[]>  getSourceDest(SparseMultigraph<NodeImpl, LinkImpl> req, List<Link> reqLinkList, Map<Node, Integer> fakeIDs, Map<Link, Integer> fakeLinkIDs) {
		List<int[]> sdList = new ArrayList<int[]>();
		int[] o = new int[reqLinkList.size()];
		int[] d = new int[reqLinkList.size()];
	
		for (Link link :  reqLinkList){
			LOG.debug("checking link with id: " +fakeLinkIDs.get(link));
			Node reqSourceNode = req.getEndpoints((LinkImpl) link).getFirst();
			Node reqTargetNode = req.getEndpoints((LinkImpl) link).getSecond();			
			int sourceNodeID = fakeIDs.get(reqSourceNode);
			int targetNodeID = fakeIDs.get(reqTargetNode);
			LOG.debug("source : " + sourceNodeID + " dest " + targetNodeID);
			o[fakeLinkIDs.get(link)]=sourceNodeID;
			d[fakeLinkIDs.get(link)]=targetNodeID;
		}	
		sdList.add(o);
		sdList.add(d);
	
	return sdList;
	}
	
		public static float[][] getAvailableCapTable(SparseMultigraph<NodeImpl, LinkImpl> sub, List<Node> nodes) {
		
		int num = sub.getVertexCount();
		Collection<LinkImpl> edges =  sub.getEdges();
		float[][] tab =  new float[num][num];
		
		for (LinkImpl current : edges) {
			Pair<NodeImpl> currentNodes = sub.getEndpoints(current);
			
			
			int node1 = nodes.indexOf(currentNodes.getFirst());
			LOG.debug("Node "+currentNodes.getFirst()+" is identified by: "+node1);
			
			int node2 = nodes.indexOf(currentNodes.getSecond());
			LOG.debug("Node "+currentNodes.getSecond()+" is identified by: "+node2);
			
			LOG.debug("Link "+current+" has available capacity: "+current.getHasAvailableCapacity());
			float cap = current.getHasAvailableCapacity();
			
			tab[node1][node2] += cap;
			tab[node2][node1] += cap;
		}
		
		return tab;
	}
	
	/**
	 * Estimate the cost of the given request
	 */
	public static double costEmbedding(SparseMultigraph<NodeImpl, LinkImpl> req) {
		double cost=0;
		
		for (Link link : req.getEdges()) {
			cost+=link.getHasCapacity();
		}
		  
		if (cost != 0){
			for (Node x: req.getVertices()){
				if (isVirtualMachine((VirtualNode)x)
						&& !EmbeddingOperations.isSetEmpty(x.getHasComponent())) {
					cost += getNodeComponentCosts(x);
				}
				if (isVirtualRouter((VirtualNode)x)) {
					cost += x.getHasLogicalRouters().doubleValue();
				}
			}
		}

		return cost;
	}
		
	/**
	 * Gets the node component costs.
	 *
	 * @param node the node
	 * @return the node component costs
	 */
	private static double getNodeComponentCosts(Node node) {
		double toReturn = 0;
		for (NodeComponent nc : node.getHasComponent()) {
			
			if (nc instanceof CPU) {
				toReturn += ((CPU) nc).getHasCPUSpeed();
				toReturn += ((CPU) nc).getHasCores().floatValue();
			}
			
			if (nc instanceof Memory) {
				toReturn += ((Memory) nc).getHasMemorySize();
			}
			
			if (nc instanceof Storage) {
				toReturn += ((Storage) nc).getHasStorageSize();
			}
		}
		return toReturn;
	}

	/**
	 * Checks if set is null or empty.
	 * 
	 * @param set
	 * @return true if set is null or does not contain any elements. Returns
	 *         false otherwise.
	 */
	public static boolean isSetEmpty(Set<? extends Object> set) {
		return (set == null || set.isEmpty());
	}
	
	/**
	 * Builds the path of the given link.
	 *
	 * @param link the link
	 * @param pathNodeIDs the path node i ds
	 * @param subNodeList the sub node list
	 * @param subPath the sub path
	 * @return true, if successful
	 */
	public static boolean buildPath(Link link, ArrayList<Integer> pathNodeIDs,
			List<Node> subNodeList, Path subPath) {
		
		// Getting virtual interface endpoints
		Interface sourceVInterface = getSourceInterface(link);
		Interface targetVInterface = getTargetInterface(link);
		
		for (int j=0; j<pathNodeIDs.size()-1; j++) {
		
			Node sourceNode = subNodeList.get(pathNodeIDs.get(j));
			Node destNode = subNodeList.get(pathNodeIDs.get(j+1));
			
			// Adding target node to the path (if it an intermediate node)
			if (!destNode.equals(subNodeList.get(pathNodeIDs.get(pathNodeIDs.size()-1)))) {
				addResourceToPath(destNode, subPath);
				LOG.debug("Substrate Node "+destNode+" added to the Path");
			}
				
			if (!buildPathIteration(sourceNode,
					destNode,subPath,subNodeList,pathNodeIDs,
					sourceVInterface,targetVInterface)) {return false;}
			
		}
		return true;
	}
	
	/**
	 * Fill the given Path with the needed information between
	 * source and destination nodes.
	 *
	 * @param sourceNode the source node
	 * @param destNode the dest node
	 * @param subPath the sub path
	 * @param subNodeList the sub node list
	 * @param pathNodeIDs the path node i ds
	 * @param sourcePathInterface the source path interface to be detected
	 * @param targetPathInterface the target path interface to be detected
	 * @return true, if successful
	 */
	public static boolean buildPathIteration(Node sourceNode, 
			Node destNode, Path subPath, List<Node> subNodeList, 
			List<Integer> pathNodeIDs, 
			Interface sourceVInterface, Interface targetVInterface) {
		
		Set<Interface> sourceIfaces = sourceNode.getHasOutboundInterfaces();
		Set<Interface> targetIfaces = destNode.getHasInboundInterfaces();
		Link selectedLink = null;
		Interface selectedSourceIface = null;
		Interface selectedTargetIface = null;
		if (!EmbeddingOperations.isSetEmpty(sourceIfaces)) {
			for (Interface sourceIface : sourceIfaces) {
				//Ignoring NSwitch interfaces (Federable interfaces)
				if (sourceIface.getCanFederate()) {continue;}
				Set<LinkOrPath> sourceLinks = sourceIface.getIsSource();
				if (!EmbeddingOperations.isSetEmpty(sourceLinks)) {					
					for (LinkOrPath sourceLink : sourceLinks) {
						if (sourceLink instanceof Link 
								&& !EmbeddingOperations.isSetEmpty(targetIfaces)) {
							for (Interface targetIface : targetIfaces) {
								if (targetIface.getCanFederate()) {continue;}
								Set<LinkOrPath> targetLinks = targetIface.getIsSink();
								if (!EmbeddingOperations.isSetEmpty(targetLinks)) {
									for (LinkOrPath targetLink : targetLinks) {
				    					if (targetLink instanceof Link
				    							&& sourceLink.equals(targetLink)
				    							&& (selectedLink == null || 
				    									sourceLink.getHasAvailableCapacity()
				    									>selectedLink.getHasAvailableCapacity())) {					
											if (selectedLink != null) {
												LOG.debug("Removing previous assignation...");
												removeMapping(selectedLink,destNode,selectedSourceIface,
														selectedTargetIface,subPath);
											}
											
											selectedLink = ((Link) sourceLink);
											selectedSourceIface = sourceIface;
											selectedTargetIface = targetIface;
											
											addToPath(sourceNode, destNode, 
													selectedLink,sourceIface,
													targetIface,subPath);
														    				
						    				break;
				    					}
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (selectedLink==null) {return false;}
		
		// Checking source and target Path Interfaces
		if (sourceNode.equals(subNodeList.get(pathNodeIDs.get(0)))) {
			// Adding implementedBy to the source virtual interfaces
    		Set<Interface> sourceIface = new HashSet<Interface>();
    		sourceIface.add(selectedSourceIface);
    		sourceVInterface.setImplementedBy(sourceIface);
    		
		}
		if (destNode.equals(subNodeList.get(pathNodeIDs.get(pathNodeIDs.size()-1)))) {
			// Adding implementedBy to the target virtual interfaces
			Set<Interface> targetIface = new HashSet<Interface>();
    		targetIface.add(selectedTargetIface);
    		targetVInterface.setImplementedBy(targetIface);
		}
		
		return true;
	}
	
	/**
	 * Add the subPath to the sourceIface, link and targetIface
	 * @param sourceNode
	 * @param destNode
	 * @param link
	 * @param sourceIface
	 * @param targetIface
	 * @param subPath
	 */
	public static void addToPath(Node sourceNode, Node destNode, Link link, Interface sourceIface,
			Interface targetIface, Path subPath) {
		
		// Adding source interface to the Path
		addResourceToPath(sourceIface,subPath);
		LOG.debug("Substrate Source iface "+sourceIface+" added to the Path");
		
		// Adding next from the source node to its interface (if it is in path)
		if (!isSetEmpty(sourceNode.getInPaths())
				&& sourceNode.getInPaths().contains(subPath)) {
			addNextRelation(sourceNode,sourceIface);
			LOG.debug("Next connection from "+sourceNode+" to "+sourceIface);
		}
		
		// Adding next from source iface to link
		addNextRelation(sourceIface,link);
		LOG.debug("Next connection from "+sourceIface+" to "+link);
		
		// Adding link to the Path
		addResourceToPath(link,subPath);
		LOG.debug("Substrate Link "+link+" added to the Path");
		
		// Adding next from link to target iface
		addNextRelation(link,targetIface);
		LOG.debug("Next connection from "+link+" to "+targetIface);
		
		// Adding target interface to the Path
		addResourceToPath(targetIface,subPath);
		LOG.debug("Substrate Target iface "+targetIface+" added to the Path");

		// Adding next to the dest node (if it is in path)
		if (destNode.getInPaths()!=null
				&& destNode.getInPaths().contains(subPath)) {
			addNextRelation(targetIface,destNode);
			LOG.debug("Next connection from "+targetIface+" to "+destNode);
		}
	}
	
	/**
	 * Builds the bound path.
	 *
	 * @param link the virtual link
	 * @param pathNodeIDs the path node ids
	 * @param subNodeList the substrate node list
	 * @param currentPath the current path (the one in the request)
	 * @return the path created using the substrate resources
	 */
	public static Path buildBoundPath(Link link,
			ArrayList<Integer> pathNodeIDs, List<Node> subNodeList, Path currentPath) {
		
		Path subPath = new PathImpl(currentPath.toString());
		subPath.setContains(new HashSet<Resource>());
		
		// Getting virtual interface endpoints
		Interface sourceVInterface = getSourceInterface(link);
		Interface targetVInterface = getTargetInterface(link);
		
		for (int j=0; j<pathNodeIDs.size()-1; j++) {
		
			Node sourceNode = subNodeList.get(pathNodeIDs.get(j));
			Node destNode = subNodeList.get(pathNodeIDs.get(j+1));
			
			// Adding target node to the path (if it an intermediate node)
			if (!destNode.equals(subNodeList.get(pathNodeIDs.get(pathNodeIDs.size()-1)))) {
				addResourceToPath(destNode, subPath);
				LOG.debug("Substrate Node "+destNode+" added to the Path");
			}
				
			if (!buildBoundPathIteration(sourceNode,
					destNode,subPath,subNodeList,pathNodeIDs,
					sourceVInterface,targetVInterface,currentPath)) {return null;}
			
		}
		
		return subPath;
	}
	
	/**
	 * Builds the bound path iteration.
	 *
	 * @param sourceNode the source node
	 * @param destNode the destination node
	 * @param subPath the substrate path to build
	 * @param subNodeList the substrate node list
	 * @param pathNodeIDs the path node ids
	 * @param sourceVInterface the source virtual interface
	 * @param targetVInterface the target virtual interface
	 * @param currentPath the current path
	 * @return true, if successful
	 */
	private static boolean buildBoundPathIteration(Node sourceNode,
			Node destNode, Path subPath, List<Node> subNodeList,
			ArrayList<Integer> pathNodeIDs, Interface sourceVInterface,
			Interface targetVInterface, Path currentPath) {

		Set<Interface> sourceIfaces = sourceNode.getHasOutboundInterfaces();
		Set<Interface> targetIfaces = destNode.getHasInboundInterfaces();
		
		// getting source substrate interface
		Interface selectedSourceIface = getPathInterface(sourceIfaces,currentPath);
		// getting target substrate interface
		Interface selectedTargetIface = getPathInterface(targetIfaces,currentPath);
		
		if (selectedSourceIface==null || selectedTargetIface==null) {
			LOG.error("Resources in the bound path do not correspond with the substrate resources. " +
					"Please, Check the correctness of the requested topology");
			return false;
		}

		// getting substrate link
		Link selectedLink = getPathLink(selectedSourceIface,selectedTargetIface,currentPath);
		
		if (selectedLink == null) {
			LOG.error("Resources in the bound path do not correspond with the substrate resources. " +
					"Please, Check the correctness of the requested topology");
			return false;
		}
		
		addToPath(sourceNode, destNode, 
				selectedLink,selectedSourceIface,
				selectedTargetIface,subPath);
		
		// Checking source and target Path Interfaces
		if (sourceNode.equals(subNodeList.get(pathNodeIDs.get(0)))) {
			// Adding implementedBy to the source virtual interfaces
    		Set<Interface> sourceIface = new HashSet<Interface>();
    		sourceIface.add(selectedSourceIface);
    		sourceVInterface.setImplementedBy(sourceIface);
    		
		}
		if (destNode.equals(subNodeList.get(pathNodeIDs.get(pathNodeIDs.size()-1)))) {
			// Adding implementedBy to the target virtual interfaces
			Set<Interface> targetIface = new HashSet<Interface>();
    		targetIface.add(selectedTargetIface);
    		targetVInterface.setImplementedBy(targetIface);
		}
		
		return true;
	}

	/**
	 * Gets the link in the currentPath with the given source and target ifaces.
	 *
	 * @param sourceIface the source iface
	 * @param targetIface the target iface
	 * @param currentPath the current path
	 * @return the link in the path
	 */
	private static Link getPathLink(Interface sourceIface,
			Interface targetIface, Path currentPath) {
		
		Link toReturn = null;
		
		if (isSetEmpty(sourceIface.getIsSource())) {return null;}
		if (isSetEmpty(targetIface.getIsSink())) {return null;}
		
		for (LinkOrPath lop : sourceIface.getIsSource()) {
			if (lop instanceof Link && isInPath(lop.toString(), currentPath)) {
				toReturn = (Link) lop;
				break;
			}
		}
		
		if (toReturn == null) {return null;}
		
		for (LinkOrPath lop : targetIface.getIsSink()) {
			if (lop.toString().equals(toReturn.toString())) {
				return toReturn;
			}
		}
		
		return null;
	}

	/**
	 * Gets the interface contained in the currentPath.
	 *
	 * @param sourceIfaces the source ifaces
	 * @param currentPath the current path
	 * @return the path interface
	 */
	private static Interface getPathInterface(Set<Interface> sourceIfaces,
			Path currentPath) {
		if (!isSetEmpty(sourceIfaces)) {
			for (Interface iface : sourceIfaces) {
				if (isInPath(iface.toString(),currentPath)) {return iface;}
			}
		}
		return null;
	}

	/**
	 * Checks if a resource with resId is in the currentPath.
	 *
	 * @param resId the resource id
	 * @param currentPath the current path
	 * @return true, if is in path
	 */
	private static boolean isInPath(String resId, Path currentPath) {
		if (isSetEmpty(currentPath.getContains())) {return false;}
		for (Resource res : currentPath.getContains()) {
			if (resId.equals(res.toString())) {return true;}
		}
		return false;
	}

	/**
	 * Adds the resource to path.
	 *
	 * @param resource the resource
	 * @param path the path
	 */
	public static void addResourceToPath(NetworkElementOrNode resource, Path path) {
		Set<Path> inPaths = null;
		if (!isSetEmpty(resource.getInPaths())) {inPaths = resource.getInPaths();} 
		else {inPaths = new HashSet<Path>();}
		inPaths.add(path);
		resource.setInPaths(inPaths);
		// path setContains
		path.getContains().add(resource);
	}

	/**
	 * Adds the next relation from source to target.
	 *
	 * @param source the source
	 * @param target the target
	 */
	private static void addNextRelation(NetworkElementOrNode source, NetworkElementOrNode target) {
		Set<NetworkElementOrNode> nexts = null;
		if (!isSetEmpty(source.getNexts())) {nexts = source.getNexts();}
		else {nexts = new HashSet<NetworkElementOrNode>();}
		nexts.add(target);
		source.setNexts(nexts);
	}
	
	/**
	 * Remove a previous assignation for the subPath
	 * @param link
	 * @param destNode
	 * @param sourceIface
	 * @param targetIface
	 * @param subPath
	 */
	public static void removeMapping(Link link, Node destNode,
			Interface sourceIface, Interface targetIface, 
			Path subPath) {
		
		// Removing source interface to the Path
		Set<Path> inPaths = sourceIface.getInPaths();
		inPaths.remove(subPath);
		sourceIface.setInPaths(inPaths);
		LOG.debug("Substrate Source iface "+sourceIface+" removed from the Path");
		
		// Removing next from source iface to link
		Set<NetworkElementOrNode> nexts = sourceIface.getNexts();
		nexts.remove(link);
		sourceIface.setNexts(nexts);
		LOG.debug("Next connection from "+sourceIface+" to "+link+" removed");
		
		// Removing link from the Path
		inPaths = link.getInPaths();
		inPaths.remove(subPath);
		link.setInPaths(inPaths);
		LOG.debug("Substrate Link "+link+" removed from the Path");
		
		// Adding next from link to target iface
		nexts = link.getNexts();
		nexts.remove(targetIface);
		link.setNexts(nexts);
		LOG.debug("Next connection from "+link+" to "+targetIface+" removed");
		
		// Removing target interface from the Path
		inPaths = targetIface.getInPaths();
		inPaths.remove(subPath);
		targetIface.setInPaths(inPaths);
		LOG.debug("Substrate Target iface "+targetIface+" removed from the Path");
		
		// Removing next to the dest node (if it is in path)
		if (!EmbeddingOperations.isSetEmpty(destNode.getInPaths())) {
			inPaths = destNode.getInPaths();
			if (inPaths.contains(subPath)) {
				nexts = targetIface.getNexts();
				nexts.remove(destNode);
				targetIface.setNexts(nexts);
				LOG.debug("Next connection from "+targetIface+" to "+destNode+" removed");
			}
		}
	}
	
	/**
	 * Get the inverse direction of the given link (In the IM)
	 * @param req
	 * @param link
	 * @param linkMapping
	 * @return
	 */
	public static Link getReverseLink(SparseMultigraph<NodeImpl, LinkImpl> req, Link link, Map<ResourceImpl, ResourceImpl> linkMapping) {
		NodeImpl sourceNode = req.getEndpoints((LinkImpl) link).getFirst();
		NodeImpl targetNode = req.getEndpoints((LinkImpl) link).getSecond();
		
		if (!isSetEmpty(targetNode.getHasOutboundInterfaces())) {
			for (Interface targetIface : targetNode.getHasOutboundInterfaces()) {
				if (!isSetEmpty(targetIface.getIsSource())) {
					for (LinkOrPath targetLink : targetIface.getIsSource()) {
						if (targetLink instanceof Link 
								&& !isSetEmpty(sourceNode.getHasInboundInterfaces())) {
							Link result = getReverseLinkFromSourceNode(sourceNode,targetLink,linkMapping);
							if (result!=null) {return result;}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets the reverse link from source node.
	 *
	 * @param sourceNode the source node
	 * @param targetLink the target link
	 * @param linkMapping the link mapping
	 * @return the reverse link from source node
	 */
	private static Link getReverseLinkFromSourceNode(Node sourceNode,
			LinkOrPath targetLink, Map<ResourceImpl, ResourceImpl> linkMapping) {
		
		for (Interface sourceIface : sourceNode.getHasInboundInterfaces()) {
			if (!isSetEmpty(sourceIface.getIsSink())) {
				for (LinkOrPath sourceLink : sourceIface.getIsSink()) {
					if (sourceLink instanceof Link 
							&& targetLink.equals(sourceLink) && !linkMapping.containsKey(targetLink)) {
						return (Link) targetLink;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check Federable constraint. If the virtualNode is federable, the physical
	 * node should have federable capacities.
	 *
	 * @param virtualNode the virtual node
	 * @param physicalNode the physical node
	 * @return true, if successful
	 */
	public static boolean checkFederableConstraint(VirtualNode virtualNode, Node physicalNode) {
			
		if (isVirtualMachine(virtualNode)) {return true;}
		
		/* check inbound ifaces */
		if (!EmbeddingOperations.isSetEmpty(virtualNode.getHasInboundInterfaces())) {
			for (Interface iface : virtualNode.getHasInboundInterfaces()) {
				if (iface.getCanFederate()) {
					/* node should have federable capacities */
					return isFederable(physicalNode);
				}
			}
		}
		
		/* check outbound ifaces */
		if (!EmbeddingOperations.isSetEmpty(virtualNode.getHasOutboundInterfaces())) {
			for (Interface iface : virtualNode.getHasOutboundInterfaces()) {
				if (iface.getCanFederate()) {
					/* node should have federable capacities */
					return isFederable(physicalNode);
				}
			}
		}

		return true;
	}
	
    public static boolean checkFederable(Node node) {
		
		/* check inbound ifaces */
		if (!EmbeddingOperations.isSetEmpty(node.getHasInboundInterfaces())) {
			for (Interface iface : node.getHasInboundInterfaces()) {
				if (iface.getCanFederate()) {
					/* node should have federable capacities */
					return true;
				}
			}
		}
		/* check outbound ifaces */
		if (!EmbeddingOperations.isSetEmpty(node.getHasOutboundInterfaces())) {
			for (Interface iface : node.getHasOutboundInterfaces()) {
				if (iface.getCanFederate()) {
					/* node should have federable capacities */
					return true;
				}
			}
		}
	
		return false;
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
	 * Gets the target interface if the requested link.
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
	 * Checks if the physical node is federable or not.
	 *
	 * @param physicalNode the physical node
	 * @return true, if is federable
	 */
	private static boolean isFederable(Node physicalNode) {
		// Doing the checking with outbound interfaces
		if (EmbeddingOperations.isSetEmpty(physicalNode.getHasOutboundInterfaces())) {
			return false;
		}
		for (Interface iface : physicalNode.getHasOutboundInterfaces()) {
			if (iface.getCanFederate()) {return true;} 
		}
		return false;
	}

	
	/**
	 * Gets the node with nodeId from the nodeList.
	 *
	 * @param nodeId the node id
	 * @param nodeList the node list
	 * @return the node
	 */
	public static Node getNode(String nodeId, List<Node> nodeList) {
		for (Node node : nodeList) {
			if (nodeId.equals(node.toString())) {
				return node;
			}
		}
		return null;
	}
	
		/**
	 * Move Nodes and Links of a virtual topology or platform in a JUNG graph
	 * This method ignore Paths
	 * @return translated graph
	 */
	public synchronized static SparseMultigraph<NodeImpl, LinkImpl> translateIMToGraph(Group availableResources, boolean bidirectional) {
		// Graph to return
		SparseMultigraph<NodeImpl, LinkImpl> graph = new SparseMultigraph<NodeImpl, LinkImpl>();
		// HashMap for links and its endpoints
		HashMap<Link,Node[]> linksAndEndpoints = new HashMap<Link, Node[]>();
		
		try {
			Iterator<Resource> resourceIt = ((Group) availableResources).getContains().iterator();
			while (resourceIt.hasNext()) {
				Resource resource = resourceIt.next();
				if (resource instanceof Node) {
					// Add vertex to the graph
					graph.addVertex((NodeImpl) resource);
					Set<Interface> outIfaces = new HashSet<Interface>();
					Set<Interface> inIfaces = new HashSet<Interface>();
					// go through out-bound interfaces
					if (((Node) resource).getHasOutboundInterfaces()!=null) {
						outIfaces.addAll(((Node) resource).getHasOutboundInterfaces());
					}
					for (Interface iface : outIfaces) {
						Set<LinkOrPath> links = new HashSet<LinkOrPath>();
						if (iface.getIsSource()!=null) {
							links.addAll(iface.getIsSource());
						}
						for (LinkOrPath link : links) {
							if (link instanceof Link) {
								if (linksAndEndpoints.containsKey(link)) {
									Node[] pair = linksAndEndpoints.get(link);
									pair[0] = (Node)resource;
								} else {
									Node pair[] = new Node[2];
									pair[0] = (Node)resource;
									linksAndEndpoints.put((Link)link, pair);
								}
							}
						}
					}
					// go through in-bound interfaces
					if (((Node) resource).getHasInboundInterfaces()!=null) {
						inIfaces.addAll(((Node) resource).getHasInboundInterfaces());
					}
					for (Interface iface : inIfaces) {
						Set<LinkOrPath> links = new HashSet<LinkOrPath>();
						if (iface.getIsSink()!=null) {
							links.addAll(iface.getIsSink());
						}
						for (LinkOrPath link : links) {
							if (link instanceof Link) {
								if (linksAndEndpoints.containsKey(link)) {
									Node[] pair = linksAndEndpoints.get(link);
									pair[1] = (Node)resource;
								} else {
									Node pair[] = new Node[2];
									pair[1] = (Node)resource;
									linksAndEndpoints.put((Link)link, pair);
								}
							}
						}
					}
				}
				// Nothing to do with links. No nodes or interfaces reachable.
			}
			
			// Add edges to the graph
			Set<Entry<Link,Node[]>> set = linksAndEndpoints.entrySet();
			for (Entry<Link,Node[]> entry : set) {
				// Discard link if some endpoint is null 
				// or if there is already set the inverse link in the graph
				if (entry.getValue()[0]!=null && entry.getValue()[1]!=null) {
					if (bidirectional) {
						if (!existsInverseEdge(graph, entry.getValue()[0], entry.getValue()[1])) {
							graph.addEdge((LinkImpl)entry.getKey(),
									(NodeImpl)entry.getValue()[0],(NodeImpl)entry.getValue()[1],
									EdgeType.UNDIRECTED);
						}
						// Link discarded
					} else {
						graph.addEdge((LinkImpl)entry.getKey(),
								(NodeImpl)entry.getValue()[0],(NodeImpl)entry.getValue()[1],
								EdgeType.UNDIRECTED);
					}
				}
				// Link discarded
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return graph;
	}
	
	
		/**
	 * Detect if inverse edge exists in the given graph.
	 *
	 * @param graph the graph
	 * @param first the first node
	 * @param second the second node
	 * @return true, if exists; false otherwise
	 */
	private static boolean existsInverseEdge(SparseMultigraph<NodeImpl, LinkImpl> graph, Node first, Node second) {
		for (LinkImpl link : graph.getEdges()) {
			Pair<NodeImpl> pair = graph.getEndpoints(link);
			if (pair.getFirst().equals(second) && pair.getSecond().equals(first)) {
				return true;
			}
		}
		return false;
	}
	
		/**
	 * Put into the log the given graph details
	 * @param graph
	 */
	public static void analyzeGraph(SparseMultigraph<NodeImpl, LinkImpl> graph, 
			LogService logService) {
		
		Collection<NodeImpl> nodes = graph.getVertices();
		Collection<LinkImpl> links = graph.getEdges();
		
		logService.log(LogService.LOG_INFO,"********** Nodes **********");
		
		for (Node current : nodes){
			logService.log(LogService.LOG_INFO,"Node name: "+current.toString());	
		}
		
		logService.log(LogService.LOG_INFO,"********** Links **********");
		
		for (LinkImpl current : links) {
			logService.log(LogService.LOG_INFO,"Link name: "+current.toString());
			Pair<NodeImpl> pair = graph.getEndpoints(current);
			logService.log(LogService.LOG_INFO,"--> Link endpoint1: "+pair.getFirst().toString());
			logService.log(LogService.LOG_INFO,"--> Link endpoint2: "+pair.getSecond().toString());
		}
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
		
		if (isSetEmpty(group.getContains())) {return;}
		
		Iterator<Resource> vResourceIt = group.getContains().iterator();
		while (vResourceIt.hasNext()) {
			Resource resource = vResourceIt.next();
			if (resource instanceof Node) {
				logService.log(LogService.LOG_INFO,"Node: "+resource.toString());
				if (!isSetEmpty(resource.getIsContainedIn())) {
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
					if (!isSetEmpty(p.getContains())) {
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
		if (!isSetEmpty(n.getHasOutboundInterfaces())) {
			for (Interface iface : n.getHasOutboundInterfaces()) {
				logService.log(LogService.LOG_INFO,
						"		Node outbound Interface: "+iface.toString());
				analyzeInterface(iface, logService);
			}
		}
		if (!isSetEmpty(n.getHasInboundInterfaces())) {
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
		if (!isSetEmpty(iface.getImplementedBy())) {
			for (Interface pIface : iface.getImplementedBy()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is implemented by: "+pIface.toString());
			}
		}
		else {
			logService.log(LogService.LOG_INFO, "implemented by: -");
		}
		if (!isSetEmpty(iface.getIsSource())) {
			for (LinkOrPath lop : iface.getIsSource()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is source of: "+lop.toString());
			}
		}
		if (!isSetEmpty(iface.getIsSink())) {
			for (LinkOrPath lop : iface.getIsSink()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is sink of: "+lop.toString());
			}
		}
		if (!isSetEmpty(iface.getNexts())) {
			for (NetworkElementOrNode neon : iface.getNexts()) {
				logService.log(LogService.LOG_INFO,
						"		Interface next element: "+neon.toString());
			}
		}
		if (!isSetEmpty(iface.getInPaths())) {
			for (LinkOrPath lop : iface.getInPaths()) {
				logService.log(LogService.LOG_INFO,
						"		Interface is in path: "+lop.toString());
			}
		}
		if (!isSetEmpty(iface.getIsInboundInterfaceOf())) {
			for (Node node : iface.getIsInboundInterfaceOf()) {
				logService.log(LogService.LOG_INFO,
						"		Iface is INbound of node: "+node.toString());
			}
		}
		if (!isSetEmpty(iface.getIsOutboundInterfaceOf())) {
			for (Node node : iface.getIsOutboundInterfaceOf()) {
				logService.log(LogService.LOG_INFO,
						"		Iface is OUTbound of node: "+node.toString());
			}
		}
	}
	
	public static void printErrorFeedback(String sessionID, LogService logService, 
							ReportEvent userFeedback, String testbedName, String msg1, String msg2){
		LOG.error(msg1);
		logService.log(LogService.LOG_ERROR, msg1);
		userFeedback.errorEvent(sessionID, 
				"Embedding-"+testbedName+"::"+ msg1, 
				msg2, 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		}
	

}
