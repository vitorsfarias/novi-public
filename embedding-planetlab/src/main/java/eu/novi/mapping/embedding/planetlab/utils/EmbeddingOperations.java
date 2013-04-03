/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.planetlab.utils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.Group;
import org.osgi.service.log.LogService;
/**
 * Common Embedding Operations for PLANETLAB
 */
public final class EmbeddingOperations {

	// logging
	private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingOperations.class);
	
	/**
	 * Private constructor
	 */
	private EmbeddingOperations(){}

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
	 * Prints the node info.
	 *
	 * @param node the node
	 * @param cpuSpeed the cpu speed
	 * @param mem the memory
	 * @param disk the disk size
	 */
	public static void printNodeInfo(Node node, String cpuSpeed,
			String mem, String disk) {
		if (node!=null) {
			LOG.info("Node: "+node);
			LOG.info("Node description: "+cpuSpeed+
					" CPU, "+mem+" Memory, "+disk+" Disk");
		}
	}

	/**
	 * Node component value to string.
	 *
	 * @param nc the node component
	 * @return the string
	 */
	public static String nodeComponentValueToString(float nc) {
		if (nc!=0) {return Float.toString(nc);}
		else {return "Not defined";}
	}
	
	/**
	 * Check non functional values for the mapping between vNode and pNode.
	 *
	 * @param vNode the v node
	 * @param pNode the node
	 * @return true if success; false otherwise
	 */
	public static boolean checkConstraints(Node vNode, Node pNode) {
		
		if (getCPUCores(vNode)>getAvailableCPUCores(pNode)) {
			LOG.error("Invalid CPU cores");
			return false;
		}
		if (getCPUSpeed(vNode)>getCPUSpeed(pNode)) {
			LOG.error("Invalid CPU speed");
			return false;
		}
		if (getMemory(vNode)>getAvailableMemory(pNode)) {
			LOG.error("Invalid Memory");
			return false;
		}
		if (getStorage(vNode)>getAvailableStorage(pNode)) {
			LOG.error("Invalid Storage size");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get storage size.
	 *
	 * @param node the node
	 * @return storage size
	 */
	public static float getStorage(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof Storage) {
				return ((Storage)nc).getHasStorageSize();
			}
		}
		return 0;
	}

	/**
	 * Get available storage size.
	 *
	 * @param node the node
	 * @return available storage size
	 */
	private static float getAvailableStorage(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof Storage) {
				return ((Storage)nc).getHasAvailableStorageSize();
			}
		}
		return 0;
	}

	/**
	 * Get memory.
	 *
	 * @param node the node
	 * @return memory
	 */
	public static float getMemory(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof Memory) {
				return ((Memory)nc).getHasMemorySize();
			}
		}
		return 0;
	}

	/**
	 * Get available memory.
	 *
	 * @param node the node
	 * @return available memory
	 */
	private static float getAvailableMemory(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof Memory) {
				return ((Memory)nc).getHasAvailableMemorySize();
			}
		}
		return 0;
	}
	
	/**
	 * Get CPU speed.
	 *
	 * @param node the node
	 * @return CPU speed
	 */
	public static float getCPUSpeed(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof CPU) {
				return ((CPU)nc).getHasCPUSpeed();
			}
		}
		return 0;
	}

	/**
	 * Get CPU cores.
	 *
	 * @param node the node
	 * @return CPU cores
	 */
	public static int getCPUCores(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof CPU) {
				BigInteger cores = ((CPU) nc).getHasCores();
				return cores.intValue();
			}
		}
		return 0;
	}

	/**
	 * Get available CPU cores.
	 *
	 * @param node the node
	 * @return available CPU cores
	 */
	private static int getAvailableCPUCores(Node node) {
		if (node.getHasComponent()==null) {return 0;}
		for (NodeComponent nc : node.getHasComponent()) {
			if (nc instanceof CPU) {
				BigInteger cores = ((CPU) nc).getHasAvailableCores();
				return cores.intValue();
			}
		}
		return 0;
	}

	public static Node getNode(String nodeId,
			SparseMultigraph<NodeImpl, LinkImpl> graph) {
		for (Node node : graph.getVertices()) {
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
		HashMap<Link, Node[]> linksAndEndpoints = new HashMap<Link, Node[]>();
		
		try {
				Iterator<Resource> resourceIt = ((Group) availableResources).getContains().iterator();
				while (resourceIt.hasNext()) {
					Resource resource = resourceIt.next();
					if (resource instanceof Node) {
					graph.addVertex((NodeImpl) resource);	
					linksAndEndpoints = processNode((Node)resource, linksAndEndpoints);
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
				} 
			}
			catch (Exception e) {
			e.printStackTrace();
			return null;
			}
		
		return graph;
	}
	
	public static HashMap<Link,Node[]> processNode(Node resource, HashMap<Link,Node[]> linksAndEndpoints){
		
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
		
		
		
		return linksAndEndpoints;
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
	public static int analyzeGraph(SparseMultigraph<NodeImpl, LinkImpl> graph, 
			LogService logService) {
		
		Collection<NodeImpl> nodes = graph.getVertices();
		Collection<LinkImpl> links = graph.getEdges();
		
		logService.log(LogService.LOG_INFO,"********** Nodes **********");
		
		for (Node current : nodes){
			logService.log(LogService.LOG_INFO,"Node name: "+current.toString());	
		}
		
		logService.log(LogService.LOG_INFO,"********** Links **********");
		
		for (LinkImpl current : links) {
			logService.log(LogService.LOG_INFO,"Found Link: "+current.toString());
			return -1;
		}
		return 0;
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
	
}
