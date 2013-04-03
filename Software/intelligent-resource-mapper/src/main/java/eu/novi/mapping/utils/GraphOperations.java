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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.service.log.LogService;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;

/**
 * Graph Operations
 */
public class GraphOperations {

	/**
	 * Private constructor
	 */
	private GraphOperations(){}
	
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
	 * Translate the mapped resources (using JUNG graphs) to topology.
	 *
	 * @param nodeMapping the node mapping
	 * @param linkMapping the link mapping
	 * @param topologyName the topology name of the result
	 * @return topology with the nodes and links in nodeMapping and linkMapping respectively
	 */
	public static Topology translateToTopology(Map<ResourceImpl, ResourceImpl> nodeMapping, 
			Map<ResourceImpl, ResourceImpl> linkMapping, String topologyName) {

		Topology topology = new TopologyImpl(topologyName);
		Set<Resource> topologyResources = new HashSet<Resource>();
		
		// Adding nodes
		for (Resource requestNode : nodeMapping.keySet()) {
			// linking request nodes to substrate nodes
			Set<Node> implementedBy = new HashSet<Node>();
			Node toAdd = (Node) nodeMapping.get(requestNode);
			implementedBy.add(toAdd);
			((Node) requestNode).setImplementedBy(implementedBy);
			topologyResources.add(requestNode);
		}
		
		// Adding links
		for (Resource requestLink : linkMapping.keySet()) {
			Set<Path> provisionedBy = new HashSet<Path>();
			Path toAdd = (Path) linkMapping.get(requestLink);
			provisionedBy.add(toAdd);
			((Link) requestLink).setProvisionedBy(provisionedBy);
			topologyResources.add(requestLink);
		}
		
		// Adding request nodes and links to the topology
		topology.setContains(topologyResources);
		
		return topology;
	}
	
	/**
	 * Checks if the given graph is partial bounded.
	 *
	 * @param graph the graph to check
	 * @return true, if is partial bound graph
	 */
	public static boolean isPartialBoundGraph(
			SparseMultigraph<NodeImpl, LinkImpl> graph) {
		
		Collection<NodeImpl> nodes = graph.getVertices();
		for (Node current : nodes){
			if (!IMOperations.isSetEmpty(current.getImplementedBy())) {
				return true;
			}
		}
		Collection<LinkImpl> links = graph.getEdges();
		for (LinkImpl current : links) {
			if (!IMOperations.isSetEmpty(current.getProvisionedBy())) {
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
	
}
