/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;
import eu.novi.im.core.impl.PathImpl;

/**
 * Dijkstra Link Mapping
 */
public final class DijkstraLinkMapping {
	
	/**
	 * Private constructor
	 */
	private DijkstraLinkMapping(){}
	
	/** Local logging. */
	private static final transient Logger LOG = LoggerFactory.getLogger(DijkstraLinkMapping.class);
	
	public static Map<ResourceImpl, ResourceImpl> glm(SparseMultigraph<NodeImpl, LinkImpl> req, 
			SparseMultigraph<NodeImpl, LinkImpl> sub, List<Node> reqNodeList, List<Node> subNodeList, 
			List<Link> reqLinkList, float[][] capTable, Map<ResourceImpl, ResourceImpl>  nodeMapping, ReportEvent userFeedback, String sessionID ) {
		
		/* Number of substrate nodes */
		int subNodeNum = sub.getVertexCount();
		
		LinkedHashMap<Integer, ArrayList<Integer>> mapping = 
			new LinkedHashMap<Integer, ArrayList<Integer>>();
		
		float[][][] flowReserved = 
			new float[req.getEdgeCount()][subNodeNum][subNodeNum];
		
		//--------------------------------------------------------------------->
		// Denote the shortest path by maximizing the bw capacity of the path
		//--------------------------------------------------------------------->
		LOG.debug("Denoting shortest path by maximizing bw capacity of the path...");
		
		maximizeCapacityTable(subNodeNum,capTable);
				
		/* flag is false if link mapping cannot been done */
		boolean flag=true;
		
		for (int i=0; i<reqLinkList.size(); i++) {
			
			List<Integer> path = new ArrayList<Integer>();
			
			Pair<NodeImpl> nodePair =  req.getEndpoints((LinkImpl) reqLinkList.get(i));
			/* the source virtual node */
			int reqSource = reqNodeList.indexOf(nodePair.getFirst());
			/* the destination virtual node */
			int reqDest = reqNodeList.indexOf(nodePair.getSecond());
			
			/* the source substrate node */
			int subSource = findSubstrateNodeIndex(nodeMapping,reqSource,reqNodeList,subNodeList);
			/* the destination substrate node */
			int subDest = findSubstrateNodeIndex(nodeMapping,reqDest,reqNodeList,subNodeList);
			
			/* The bandwidth demand of the current virtual link */
		    float demand = 1/reqLinkList.get(i).getHasCapacity();
		    LOG.debug("Bw demand of the current virtual link: "+demand);
			
		    LOG.debug("Request source node num: "+reqSource);
		    LOG.debug("Request dest node num: "+reqDest);
		    LOG.debug("Substrate source node num: "+subSource);
		    LOG.debug("Substrate dest node num: "+subDest);
		    
		    // checking bound links
			if (!EmbeddingOperations.isSetEmpty(reqLinkList.get(i).getProvisionedBy())) {
				
				LOG.info(reqLinkList.get(i)+ " is already bound");
				path = processBoundLink((VirtualLink) reqLinkList.get(i),
						subSource,subDest,subNodeList, userFeedback,sessionID);
    			
				if (path == null) {return null;}
			
			} else {
			    
				/* Run the Dijkstra */
			    LOG.debug("Running dijkstra algorithm for link "+reqLinkList.get(i));
				int[] pred = dijkstra(sub, subSource, capTable, demand);
				
				/* Check if Dijkstra returns a valid path if not denial */
				if (pred == null) {
					LOG.error("There are no valid mapping for Link "+reqLinkList.get(i));
					userFeedback.errorEvent(sessionID, 
		    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
							"Error: There are no valid mapping for Link "+reqLinkList.get(i).toString()+". Try to reduce the requested bandwidth of the virtual link", 
							EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
					flag=false;
					break;
				}
				
				/* Contain the substrate path */
				path = (ArrayList<Integer>) returnPath(sub, pred, subSource, subDest);
				
				if (path.get(path.size()-1) != subDest){
					LOG.error("There are no valid mapping for Link "+reqLinkList.get(i));
					userFeedback.errorEvent(sessionID, 
		    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
							"Error: There are no valid mapping for Link "+reqLinkList.get(i).toString()+". Try to reduce the requested bandwidth of the virtual link", 
							EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
					flag=false;
					break;
				}
			}
			
			/* Update flowReserved and update the available capacities of the substrate links */
			for (int g=0; g<path.size()-1; g++) {
				flowReserved[i][path.get(g)][path.get(g+1)]
					=flowReserved[i][path.get(g)][path.get(g+1)]
						+reqLinkList.get(i).getHasCapacity();
				
				/* update the initial remaining capacity */
				capTable[path.get(g)][path.get(g+1)]
					=1/capTable[path.get(g)][path.get(g+1)]-1/Math.abs(demand);
				/* reverse again the updated capacity */
				capTable[path.get(g)][path.get(g+1)]
					=1/capTable[path.get(g)][path.get(g+1)];
				/* take into consideration undirected flows */
				capTable[path.get(g+1)][path.get(g)]
					=capTable[path.get(g)][path.get(g+1)];
			}
			
			/* Store mapping */
			mapping.put(i, (ArrayList<Integer>) path);
			
		}
		if (!flag){
			LOG.error("Link Mapping incomplete. Request could not be satisfied");
			return null;
		}
		
		logPathMapping(reqLinkList,mapping);
		
		LOG.info("Link Mapping done successfully");
		double avgHops = avgHops(mapping);
		LOG.debug("avgHops: "+avgHops);
		
		Map<ResourceImpl, ResourceImpl> linkMapping = 
    		(LinkedHashMap<ResourceImpl, ResourceImpl>) buildLinkMapping(reqLinkList,subNodeList,mapping,req);
		
		return linkMapping;
	}
	
	
	private static double avgHops(Map<Integer, ArrayList<Integer>> pathMapping) {
		
		if (pathMapping.size()==0) {return 0;}
		
		double avgHops =0;
		
		for (int k=0; k< pathMapping.size();k++){
			avgHops+=pathMapping.get(k).size()-1;
		}
		return avgHops/pathMapping.size();
	}	
	
	/**
	 * Process bound link If the constraints are ok it is added to the path mapping.
	 * PRE: vLink has provisionedBy relation.
	 *
	 * @param vLink the virtual link
	 * @param subSourceNodeIndex the sub source node index
	 * @param subDestNodeIndex the sub dest node index
	 * @param subNodeList the sububstrate node list
	 * @param userFeedback the user feedback
	 * @param sessionID the session id
	 * @return the list with the substrate node index in the path
	 */
	private static List<Integer> processBoundLink(VirtualLink vLink, int subSourceNodeIndex, 
			int subDestNodeIndex, List<Node> subNodeList, ReportEvent userFeedback, String sessionID) {
		
		List<Integer> toReturn = new ArrayList<Integer>();
		// adding source node
		toReturn.add(subSourceNodeIndex);
		
		Path path = (Path) vLink.getProvisionedBy().iterator().next();
		Set<Resource> pathElements = path.getContains();
		
		Interface sourceVIface = vLink.getHasSource().iterator().next(); 
		
		if (EmbeddingOperations.isSetEmpty(sourceVIface.getImplementedBy())) {
			LOG.error("There are no valid mapping for the bound Link "+vLink.toString()+". Check implementedBy relations in the endpoints of the virtual link");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
					"Error: There are no valid mapping for the bound Link "+vLink.toString()+". Check implementedBy relations in the endpoints of the virtual link", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		Interface sourcePIface = sourceVIface.getImplementedBy().iterator().next();
		
		while (toReturn.get(toReturn.size()-1)!=subDestNodeIndex) {
			if (sourcePIface==null) {
				LOG.error("There are no valid mapping for the bound link "+vLink.toString()+". Check the correctness of its physical path");
				userFeedback.errorEvent(sessionID, 
	    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
						"Error: There are no valid mapping for the bound link "+vLink.toString()+". Check the correctness of its physical path", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return null;
			}
			sourcePIface = processNextPathNode(sourcePIface,toReturn,pathElements,subNodeList,
					userFeedback,sessionID);
		}
		
		return toReturn;
	}

	/**
	 * Gets the next node position in the path.
	 *
	 * @param sourcePIface the source p iface
	 * @param subPath the substrate path
	 * @param pathElements the path elements
	 * @param subNodeList the sub node list
	 * @param userFeedback the user feedback
	 * @param sessionID the session id
	 * @return the next path node
	 */
	private static Interface processNextPathNode(Interface sourcePIface,
			List<Integer> subPath, Set<Resource> pathElements, List<Node> subNodeList, 
			ReportEvent userFeedback, String sessionID) {
		
		if (EmbeddingOperations.isSetEmpty(sourcePIface.getIsSource())) {
			LOG.error("There is no valid mapping for the bound path "+subPath.toString()+". Check source interface relations");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
					"Error: There is no valid mapping for the bound path "+subPath.toString()+". Check source interface relations", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		Link pLink = (Link) sourcePIface.getIsSource().iterator().next();
		
		if (EmbeddingOperations.isSetEmpty(pLink.getHasSink())) {
			LOG.error("There is no valid mapping for the bound path "+subPath.toString()+". Check "+pLink.toString()+" relations");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
					"Error: There is no valid mapping for the bound path "+subPath.toString()+". Check "+pLink.toString()+" relations", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		Interface targetPIface = pLink.getHasSink().iterator().next();
		
		if (EmbeddingOperations.isSetEmpty(targetPIface.getIsInboundInterfaceOf())) {
			LOG.error("There is no valid mapping for the bound path "+subPath.toString()+". Check "+targetPIface.toString()+" relations");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
					"Error: There is no valid mapping for the bound path "+subPath.toString()+". Check "+targetPIface.toString()+" relations", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		Node nextPathNode = targetPIface.getIsInboundInterfaceOf().iterator().next();
		
		Node subNode = EmbeddingOperations.getNode(nextPathNode.toString(), subNodeList); 
		if (subNode == null) {
			LOG.error("There is no valid mapping for the bound path "+subPath.toString()+". Physical node "+nextPathNode.toString()+" does not exist in the substrate");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Link Mapping incomplete. Request could not be satisfied", 
					"Error: There is no valid mapping for the bound path "+subPath.toString()+". Physical node "+nextPathNode.toString()+" does not exist in the substrate", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		
		subPath.add(subNodeList.indexOf(subNode));
		
		// update ad return sourcePIface
		return getNextInterface(nextPathNode,pathElements);

	}

	/**
	 * Gets the next interface from node. It should be in the pathElements.
	 *
	 * @param node the next path node
	 * @param pathElements the path elements
	 * @return the next interface
	 */
	private static Interface getNextInterface(Node node,
			Set<Resource> pathElements) {
		
		//System.out.println("pathElements = "+pathElements);
		
		if (EmbeddingOperations.isSetEmpty(node.getHasOutboundInterfaces())) {
			return null;
		}
		for (Interface iface : node.getHasOutboundInterfaces()) {
			//System.out.println("iface is ="+iface);
			for (Resource x: pathElements)
			if (x.toString().contains(iface.toString())) {return iface;}
		}
		
		return null;
	}




	private static void maximizeCapacityTable(int subNodeNum, float[][] capTable) {
		for (int i=0;i<subNodeNum;i++){
			for (int j=0;j<subNodeNum;j++){
				if (capTable[i][j]!=0) {
					capTable[i][j]= 1/capTable[i][j];
					LOG.debug("Capacity maximized: "+capTable[i][j]);
				}
			}
		}
	}

	public static int[] dijkstra(SparseMultigraph<NodeImpl, LinkImpl> sub, int subSource, 
			float[][] capTable, float demand) {
		
		/* shortest known distance from "subSource" */
		float[] dist = new float[sub.getVertexCount()];  
		/* preceeding node in path */
		final int[] pred = new int[sub.getVertexCount()];  
		/* all false initially */
		final boolean[] visited = new boolean[sub.getVertexCount()]; 
		 
		for (int i=0; i<dist.length; i++) {
			dist[i] = Integer.MAX_VALUE;
		}
		
		dist[subSource] = 0;
		
		/* Check if the node mapping */
		boolean ctrl = true;
		
		for (int i=0; i<dist.length; i++) {
			int next = minVertex(dist, visited);
			if (next==-1){
				ctrl=false;
				break;
			}
			visited[next] = true;
		 
			/* The shortest path to next is dist[next] and via pred[next]. */
			ArrayList<Integer> n = new ArrayList<Integer>();
			/* Find neighbors of next that satisfy the link requirements */
			for (int j=0; j<sub.getVertexCount(); j++){
				if (capTable[next][j]!=0
						&& capTable[next][j]<Math.abs(demand)){
					n.add(j);
				}
			}
			 
			for (int j=0; j<n.size(); j++) {
				int v = n.get(j);		 
				float d = dist[next] + capTable[next][v];
				if (dist[v] > d) {
					dist[v] = d;
					pred[v] = next;
				}
			}
		}
	
		if (ctrl) {return pred;}
		else {return null;}
 
	}
	
	public static int minVertex(float[] dist, boolean[] v) {
		float x = Integer.MAX_VALUE;
		int y = -1;   /* graph not connected, or no unvisited vertices */
		for (int i=0; i<dist.length; i++) {
			if (!v[i] && dist[i]<x) {y=i; x=dist[i];}
		}
		return y;
	}
	
	public static List<Integer> returnPath(SparseMultigraph<NodeImpl, LinkImpl> sub, int [] pred, int s, int e) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int x = e;
		while (x!=s) {
			path.add(0, x);
			x = pred[x];
		}
		path.add (0, s);
		return path;
	}
	
	/**
	 * Finds which substrate node index correspond to virtual node index
	 *
	 * @param nodeMapping the node mapping
	 * @param requestNode the request node
	 * @param reqNodeList the request node list
	 * @param subNodeList the substrate node list
	 * @return the index
	 */
	private static int findSubstrateNodeIndex(
			Map<ResourceImpl, ResourceImpl> nodeMapping, int requestNode, List<Node> reqNodeList, List<Node> subNodeList) {
		
		int toReturn = 0;
	    Set<Entry<ResourceImpl, ResourceImpl>> entries = nodeMapping.entrySet();
	    Iterator<Entry<ResourceImpl,ResourceImpl>> iterator = entries.iterator();
	    while (iterator.hasNext()) {
	         Map.Entry<ResourceImpl,ResourceImpl> entry = 
	        	 (Map.Entry<ResourceImpl,ResourceImpl>)iterator.next();
	         if (requestNode==reqNodeList.indexOf(entry.getKey())) {
	        	  return subNodeList.indexOf(entry.getValue());
	         }
	    }
		return toReturn;
	}
	
	/**
	 * Logs link to path mapping.
	 *
	 * @param reqLinkList the request link list
	 * @param mapping the link to path mapping
	 */
	private static void logPathMapping(List<Link> reqLinkList,
			LinkedHashMap<Integer, ArrayList<Integer>> mapping) {
		for (int i=0; i<reqLinkList.size(); i++) {
    		LOG.info("Path for link "+reqLinkList.get(i)+": "+mapping.get(i));
    	}
	}
	
	
	private static Map<ResourceImpl, ResourceImpl> buildLinkMapping(
			List<Link> reqLinkList, List<Node> subNodeList,
			Map<Integer, ArrayList<Integer>> pathMapping, 
			SparseMultigraph<NodeImpl, LinkImpl> req) {
		
		if (reqLinkList.size()!=pathMapping.size()) {return null;}
		
		Map<ResourceImpl, ResourceImpl> linkMapping = new LinkedHashMap<ResourceImpl,ResourceImpl>(); // request-real
			
		for (int i=0; i<reqLinkList.size(); i++) {
			
			ArrayList<Integer> pathNodeIDs = pathMapping.get(i);
			if (pathNodeIDs.size()<2) {return null;}
			Path subPath = null;
			
			if (!EmbeddingOperations.isSetEmpty(reqLinkList.get(i).getProvisionedBy())) {
				Path currentPath = (Path) reqLinkList.get(i).getProvisionedBy().iterator().next();
				
				// Do the work for the bound link
				subPath = EmbeddingOperations.buildBoundPath(reqLinkList.get(i),pathNodeIDs,subNodeList,currentPath);
				if (subPath == null) {
					return null;
				}
				
			} else {
				subPath = new PathImpl("path-"+i);
				subPath.setHasCapacity(reqLinkList.get(i).getHasCapacity());
				subPath.setExclusive(reqLinkList.get(i).getExclusive());
				subPath.setHasLifetimes(reqLinkList.get(i).getHasLifetimes());
				subPath.setContains(new HashSet<ResourceImpl>());

	    		// Do the work
				if(!EmbeddingOperations.buildPath(reqLinkList.get(i),pathNodeIDs,subNodeList,subPath)){
					return null;
				}
			}
			
    		linkMapping.put((LinkImpl)reqLinkList.get(i), (ResourceImpl) subPath);
    		LOG.debug("Path "+subPath+" added to the Link Mapping");
    		
    		
    		// Creating reverse path (if needed)
    		Link reverseLink = EmbeddingOperations.getReverseLink(req,reqLinkList.get(i),linkMapping);
    		if (reverseLink == null) { continue; }
    		LOG.debug("Creating reverse Path...");
    		
    		// revert pathNodeIDs
    		Collections.reverse(pathNodeIDs);
    		Path reversePath = null;
    		
    		if (!EmbeddingOperations.isSetEmpty(reverseLink.getProvisionedBy())) {
				Path currentPath = (Path) reverseLink.getProvisionedBy().iterator().next();
				
				// Do the work for the bound reversed link
				reversePath = EmbeddingOperations.buildBoundPath(reverseLink,pathNodeIDs,subNodeList,currentPath);
				if (reversePath == null) {
					return null;
				}
				
			} else {
	    		reversePath = new PathImpl("path-"+i+"-reverse");
	    		reversePath.setHasCapacity(reqLinkList.get(i).getHasCapacity());
	    		reversePath.setExclusive(reqLinkList.get(i).getExclusive());
	    		reversePath.setHasLifetimes(reqLinkList.get(i).getHasLifetimes());
	    		reversePath.setContains(new HashSet<ResourceImpl>());
	
	    		// Do the reverse work
				if(!EmbeddingOperations.buildPath(reverseLink,pathNodeIDs,subNodeList,reversePath)){
					return null;
				}
			}
			
    		linkMapping.put((LinkImpl)reverseLink, (ResourceImpl) reversePath);
    		LOG.debug("Path "+reversePath+" added to the Link Mapping");
    		
    	}

		return linkMapping;
	}

	
}
