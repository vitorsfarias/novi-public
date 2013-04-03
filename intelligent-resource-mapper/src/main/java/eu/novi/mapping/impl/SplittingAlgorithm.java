package eu.novi.mapping.impl;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.LinkImpl;
import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.mapping.RemoteIRM;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.mapping.utils.IRMConstants;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.Topology;
import eu.novi.mapping.exceptions.MappingException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Splits the federated slice problem into testbed specific embedding problems.
 * 
 *@author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 *
 */
public class SplittingAlgorithm {
	// Local logging
    private static final transient Logger LOG = LoggerFactory.getLogger(SplittingAlgorithm.class);
    	
	/**
	 * 
	 * @param topology
	 * @param resources
	 * @return
	 */
	public PartitionedRequest split(SparseMultigraph<NodeImpl, LinkImpl> virtualTopology, Set<Resource> boundResources, Set<Resource> unboundResources, Set<Topology> partialTopologies,
	Vector<FPartCostTestbedResponseImpl> splittingCosts, List<RemoteIRM> irms) throws MappingException {
		LinkedHashMap<Integer, String> testbeds =new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, FPartCostTestbedResponseImpl> testbedsCost =new LinkedHashMap<Integer, FPartCostTestbedResponseImpl>();		
	
		if (irms==null){
			LOG.debug("Error in fetching list of irms: null");
			throw new MappingException("Error in fetching list of irms: null");
		}
		if (virtualTopology==null){
			LOG.debug("Error in provided topology: null");
			throw new MappingException("Error in provided topology: null");
		}
		if (splittingCosts==null){
			LOG.debug("No partitioning costs provided by RIS");
			throw new MappingException("No partitioning costs provided by RIS! ");
		}
		
		Integer id = 0;
		for (RemoteIRM irm : irms) {
			testbeds.put(id, irm.getTestbed());
			id++;
		}
		
		for (FPartCostTestbedResponseImpl tcost:splittingCosts ){
			for( FPartCostRecordImpl tlinkcost: tcost.getLinkCosts()){
				if (tlinkcost.hasError()) {
					throw new MappingException("Error in providing link partitioning costs from testbed " +tcost.getTestbedURI() );
				}
			}
			for( FPartCostRecordImpl tnodecost: tcost.getNodeCosts()){
				if (tnodecost.hasError()) {
					throw new MappingException("Error in providing node partitioning costs from testbed " +tcost.getTestbedURI() );
				}
			}
			
		}
		
		
		testbedsCost=(LinkedHashMap<Integer, FPartCostTestbedResponseImpl>)updatePartitioningCosts(splittingCosts,testbeds);
		
		
		
		Set<Resource> missingResources = new HashSet<Resource>();
		missingResources = checkNodeAvailability(unboundResources, splittingCosts);
		if (missingResources.size()>0) {
			String missingResURIs = "";
			for (Resource res : missingResources) {
			missingResURIs =  missingResURIs+ res.toString() + " : " ;
			}
		LOG.debug ("The types of resources :" + missingResURIs+ " are not available on the federation");
		throw new MappingException("The types of resources: " + missingResURIs+ " are not available on the federation");		
		}
		
		PartitionedRequest pr = new PartitionedRequest();
		pr = runILS(virtualTopology,testbeds,testbedsCost, boundResources,unboundResources,partialTopologies);


		return pr;
	}
	
	/**
	 * Check the platform of the bound  node.
	 * @param current: the bound virtual node.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @return the testbed id, null otherwise.
	 */
	private Integer checkPlatform(VirtualNode current, Map<Integer, String> testbeds) {
	
			Iterator<Node> iter=((VirtualNode) current).getImplementedBy().iterator();
			while (iter.hasNext())
			{
						Node currentNode=(Node) iter.next();
						String cNode=currentNode.toString();
						for (Integer entry : testbeds.keySet()) {
							if (cNode.toLowerCase().contains(testbeds.get(entry).toLowerCase())) {							
								return entry;
							}
						}
						
			}
		return null;
	}
	
	
	/**
	 * Check the platform of the platform bound  node.
	 * @param current: the platform bound virtual node.
	 * @param partialTopologies: the platforms as topologies containing the platform bound resources.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @return the testbed id, null otherwise.
	 */	
	private Integer checkPlatform(VirtualNode current, Set<Topology> partialTopologies, Map<Integer, String> testbeds) {
			Iterator<Topology> iteratorTop = partialTopologies.iterator();
			
			while(iteratorTop.hasNext()) {
			Topology platform = iteratorTop.next();
			Set<Resource> pboundResources  = platform.getContains();
				if (pboundResources.contains((Resource)current)){
					for (Integer entry : testbeds.keySet()) {
						if (platform.toString().toLowerCase().contains(testbeds.get(entry).toLowerCase())) {						
						return entry;
						}
					}	
				}
			}


			return null;
	}
	
	
	/**
	 * Initialize and run ILS algorithm.
	 * @param virtualTopology: the virtual topology.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @param testbedsCost: the partitioning costs reurned by RIS
	 * @param  boundResources: set of bound requested resources
     * @param  unboundResources: set of unboundResources requested resources
	 * @param partialTopologies: the platforms as topologies containing the platform bound resources.
	 * @return PartitionedRequest - partialPlatforms.
	 */
	private PartitionedRequest runILS(SparseMultigraph<NodeImpl, LinkImpl> virtualTopology,Map<Integer, String> testbeds, 
															Map<Integer, FPartCostTestbedResponseImpl> testbedsCost,
															Set<Resource> boundResources, Set<Resource> unboundResources, Set<Topology> partialTopologies ) {
		double cost = Double.MAX_VALUE; 
		double bestCost=Double.MAX_VALUE;
		PartitionedRequest pr = new PartitionedRequest();
		
		LinkedHashMap<Node, Integer> bestMapping = new LinkedHashMap<Node, Integer>();
		LinkedHashMap<Node, Integer> mapping = new LinkedHashMap<Node, Integer>();
		
		for (int i=0;i<IRMConstants.IRM_PARAMETER_ILS;i++){		
		LOG.info("create initial mapping - RANDOM ASSIGNMENT OF NODES TO TESTBEDS");
		mapping =  (LinkedHashMap<Node, Integer>)createInitialMapping(virtualTopology,partialTopologies, boundResources, unboundResources,testbeds);

		printFinalSplittingInfo(virtualTopology,mapping,testbeds);
	
		cost = calculateCost(mapping, virtualTopology, testbedsCost,testbeds);	
	    int trials = IRMConstants.IRM_PARAMETER_LS * virtualTopology.getVertexCount() * testbeds.size();
		
		LOG.info("Running ILS for  " + trials + " times" );
		mapping = (LinkedHashMap<Node, Integer>)runLS(trials, virtualTopology,testbeds,testbedsCost, cost, mapping, unboundResources);
		
		
		cost = calculateCost(mapping, virtualTopology, testbedsCost,testbeds);			

			if (((Double)cost).compareTo((Double)bestCost)<0){
				bestMapping=mapping;
				bestCost = cost;
			}
		LOG.info("Estimate new cost for mapping: " +bestCost);
		printFinalSplittingInfo(virtualTopology,bestMapping,testbeds);
		} //end ILS
		
		pr.createSubNets(bestMapping, testbeds, virtualTopology);
		printFinalSplittingInfo(virtualTopology,bestMapping,testbeds);
		
		
		return pr;
	
	}
	
	 /**
	 * Get testbed partitioning cost from  splittingCosts and associate them with internal testbed id (testbedsCost)
	 * @param splittingCosts: the vector of tesbeds cost by RIS.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @return testbedsCost.
	 * @throws MappingException the mapping exception
	 */
	
	private Map<Integer, FPartCostTestbedResponseImpl> updatePartitioningCosts (Vector<FPartCostTestbedResponseImpl> splittingCosts,Map<Integer, String> testbeds) throws MappingException 
	{
		LinkedHashMap<Integer, FPartCostTestbedResponseImpl> tmpTestbedsCost =new LinkedHashMap<Integer, FPartCostTestbedResponseImpl>();
		
		// put the testbeds available and costs in the structure - read fromm RIS
		for (Enumeration<FPartCostTestbedResponseImpl> e = splittingCosts.elements(); e.hasMoreElements();)
		{
			FPartCostTestbedResponseImpl costs = (FPartCostTestbedResponseImpl) e.nextElement();
			
			for (Integer key : testbeds.keySet()) {
				String val = testbeds.get(key);
				if ((costs.getTestbedURI().toLowerCase()).contains(val.toLowerCase())){
					if ((costs.getNodeCosts()==null)&&(costs.getLinkCosts()==null)){
					throw new MappingException("The testbed with URI: " + val+ " returned null costs from RIS");
					}
					else {
					tmpTestbedsCost.put(key,costs);
					LOG.info("The testbed with URI: " + val+  "was added to testbeds cost " + costs.getNodeCosts().size() + " " +costs.getLinkCosts() );
					}
				}
			}
			
		}
		
		Iterator<Integer> ie = testbeds.keySet().iterator();
		while (ie.hasNext()) {
			Integer id = (Integer)ie.next();
			if (tmpTestbedsCost.get(id)==null){
				LOG.info("The testbed with ID : " + id + " is not known to the RIS");
				throw new MappingException("The testbed with ID : " + testbeds.get(id) + " is not known to the RIS");
			}
		}
		
		return tmpTestbedsCost;
	}
	
	/**
	 * Get random unbound node from virtual topology 
	 * @param virtualTopology: the virtual topology.
	 * @param  unboundResources: set of unboundResources requested resources
	 * @return unbound node.
	 */
	private Node getRandomNode (SparseMultigraph<NodeImpl, LinkImpl> virtualTopology, Set<Resource> unboundResources){
			
		Resource updatedNode = null;
		
		while (!unboundResources.contains(updatedNode)) {
			int randomNode=(int) (Math.random()* (virtualTopology.getVertexCount()));		
			List<NodeImpl>  nodeList = new ArrayList<NodeImpl>(virtualTopology.getVertices());
			updatedNode = (Resource) nodeList.get(randomNode);
			
		}
			
		return (Node)updatedNode;
	}
	
	/**
	 * Prints the splitting info
	 * @param virtualTopology: the virtual topology.
	 * @param  mapping: mapping solution.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 */
	private void printFinalSplittingInfo (SparseMultigraph<NodeImpl, LinkImpl> virtualTopology, Map<Node, Integer> mapping, Map<Integer, String> testbeds){

		for (Node reqNode: virtualTopology.getVertices()){
				Integer belongsTo = mapping.get(reqNode);
				LOG.info("Node: " +reqNode.toString()+ "  in testbed " + testbeds.get(belongsTo) );
		}
		
	}

	/**
	 * Initialize and run LS algorithm.
	 * @param virtualTopology: the virtual topology.
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @param testbedsCost: the partitioning costs reurned by RIS
	 * @param cost: cost value of previous LS execution
	 * @param mapping:  previous node asignement solution
     * @param  unboundResources: set of unboundResources requested resources
	 * @return mapping: new node asignement solution.
	 */
   private Map<Node, Integer> runLS (int trials,SparseMultigraph<NodeImpl, LinkImpl> virtualTopology, 
						Map<Integer, String> testbeds, Map<Integer, FPartCostTestbedResponseImpl> testbedsCost,
						double cost,Map<Node, Integer> mapping, Set<Resource> unboundResources   ) {
		
		for (int i=0;i<trials;i++){
			LinkedHashMap<Node, Integer> nodeMapping = new LinkedHashMap<Node, Integer>();
			double newcost = 0;
			Node updatedNode = getRandomNode(virtualTopology,unboundResources);
			
			int randomINP=(int)(Math.random()*testbeds.size());
		
		
			nodeMapping = (LinkedHashMap<Node, Integer> ) mapping;
			nodeMapping.put(updatedNode, randomINP);

			newcost=calculateCost(nodeMapping, virtualTopology,testbedsCost,testbeds);
			if (((Double)newcost).compareTo((Double)cost)<0){
				mapping = (LinkedHashMap<Node, Integer>)nodeMapping;
				cost = newcost;
			}
			
			

		}
		return (LinkedHashMap<Node, Integer> )mapping;
	
	}
	

	 /**
	 * Initialize mapping solution
	 * @param virtualTopology: the virtual topology.
	 * @param partialTopologies: the platforms as topologies containing the platform bound resources.
     * @param  boundResources: set of bound requested resources
     * @param  unboundResources: set of unboundResources requested resources
	 * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @return mapping: initial random mapping solution.
	 */
	private Map<Node, Integer> createInitialMapping (SparseMultigraph<NodeImpl,LinkImpl> virtualTopology, Set<Topology> partialTopologies,
																  Set<Resource> boundResources, Set<Resource> unboundResources, Map<Integer, String> testbeds ) {
																  
		LinkedHashMap<Node, Integer> mapping = new LinkedHashMap<Node, Integer>();
		
			for (Node reqNode: virtualTopology.getVertices()){
				if (unboundResources.contains((Resource)reqNode)) {
					int initial=(int) (Math.random()* testbeds.size());
					mapping.put(reqNode,  initial);
				} 
				else if (boundResources.contains((Resource)reqNode)) {
					int pid = checkPlatform((VirtualNode)reqNode, testbeds);
					mapping.put(reqNode,  pid);
				} else {
					int pid = checkPlatform((VirtualNode)reqNode, partialTopologies, testbeds);
					mapping.put(reqNode,  pid);
				}
			}
		
		return mapping;
		}
	
	
	/**
	 * Calculate cost of mapping solution based on partitioning cost information.
	 * @param mapping:  previous node asignement solution
	 * @param virtualTopology: the virtual topology.
	 * @param testbedsCost: the partitioning costs reurned by RIS
     * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @param cost: cost value of previous LS execution
	 * @return cost: new  asignement cost.
	 */
	
	private double calculateCost(Map<Node, Integer> mapping, SparseMultigraph<NodeImpl,LinkImpl> virtualTopology,
				Map<Integer, FPartCostTestbedResponseImpl> testbedsCost, Map<Integer, String> testbeds){
		
		double cost = 0;
		LinkedHashMap<Node, Integer> interDomainEndPoints =  new LinkedHashMap<Node, Integer> ();
	
		cost = calculateNodeAssigningCost(mapping,virtualTopology,testbedsCost,testbeds);
	
		double penalty = 1; 
		for (LinkImpl  reqLink: virtualTopology.getEdges()){
			Pair<NodeImpl> linkNodes =virtualTopology.getEndpoints(reqLink);
			NodeImpl src =  linkNodes.getFirst();
			Integer belongsToSrc = mapping.get(src);

			NodeImpl dst =  linkNodes.getSecond();
			Integer belongsToDst = mapping.get(dst);
			
			LOG.info("printing for source " + belongsToSrc);
			double costSrc = linkCostperInP(testbedsCost,reqLink,belongsToSrc);
			
			if (!belongsToSrc.equals(belongsToDst)){	
				
				
				double interDomainLinkCost = 0.0;		
				LOG.info("printing for dst " + belongsToDst);
				double costDst = linkCostperInP(testbedsCost,reqLink,belongsToDst );
				
				
				if ( ((Double.compare(costSrc ,IRMConstants.IRM_PARAMETER_ILS_LINK_PENALTY)) ==0) || ((Double.compare(costDst ,IRMConstants.IRM_PARAMETER_ILS_LINK_PENALTY)) ==0)){
				interDomainLinkCost = Math.min(costSrc,costDst);
				} else {
				interDomainLinkCost = Math.max(costSrc,costDst);
				}
				
			
				FPartCostTestbedResponseImpl tmpCostsSrc = testbedsCost.get(belongsToSrc);
				FPartCostTestbedResponseImpl tmpCostsDst = testbedsCost.get(belongsToDst);		
			
				interDomainEndPoints = (LinkedHashMap<Node, Integer>)add(interDomainEndPoints, (Node)src, belongsToSrc);
				interDomainEndPoints = (LinkedHashMap<Node, Integer>)add(interDomainEndPoints, (Node)dst, belongsToDst);
				
				if  ((isFederable(src,tmpCostsSrc)) && (isFederable(dst,tmpCostsDst))){			
					if ( (countValues(interDomainEndPoints,belongsToSrc).compareTo(getNumberOfFederableNodes(src,tmpCostsSrc))>0) ||
					    (countValues(interDomainEndPoints,belongsToDst).compareTo(getNumberOfFederableNodes(dst,tmpCostsDst))>0))
					{
					cost += interDomainLinkCost*IRMConstants.IRM_PARAMETER_ILS_LINK_PENALTY; 
					}
					else {
					cost += interDomainLinkCost*penalty;
					}			
				}
				else {
					LOG.debug("Node: " +src.toString()+ "  is not federable on testbed" + testbeds.get(belongsToSrc)+ " "  + belongsToSrc+ "  " + isFederable(src,tmpCostsSrc) );
					LOG.debug("Node: " +dst.toString()+ "  is not federable on testbed"  +testbeds.get(belongsToDst)+ " " + belongsToDst + " " + isFederable(dst,tmpCostsSrc) );		
					cost += (interDomainLinkCost*IRMConstants.IRM_PARAMETER_ILS_LINK_PENALTY);
				}
				LOG.debug("Added interdomain link cost: "+cost);
				
			}
			else {
				cost += costSrc;
				LOG.debug("Added link cost: "+reqLink.toString() + " " +cost);
				
			}
		}
		 return cost;
	 }
	 
	 	/**
	 * Calculate cost of node mapping solution based on partitioning cost information.
	 * @param mapping:  previous node asignement solution
	 * @param virtualTopology: the virtual topology.
	 * @param testbedsCost: the partitioning costs reurned by RIS
     * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @param cost: cost value of previous LS execution
	 * @return cost: new  asignement cost.
	 */
	 private double calculateNodeAssigningCost (Map<Node, Integer> mapping, SparseMultigraph<NodeImpl,LinkImpl> virtualTopology,
				Map<Integer, FPartCostTestbedResponseImpl> testbedsCost, Map<Integer, String> testbeds){
	 	
		double cost = 0;
		for (Node reqNode: virtualTopology.getVertices()){
			Integer belongsTo = mapping.get(reqNode);
			 
			FPartCostTestbedResponseImpl tmpCosts = testbedsCost.get(belongsTo);
		    LOG.info("Checking node : " + reqNode.toString() + " in testbed : "+ testbeds.get(belongsTo));
	
			for (FPartCostRecordImpl nodeCost: tmpCosts.getNodeCosts() )
			{
				
				Set<String>  uris = nodeCost.getResourceURIs();
				for (String resourceURI:  uris){
					if (resourceURI.equalsIgnoreCase(reqNode.toString())){
						//if ((Integer.valueOf(nodeCost.takeAvailResNumber())).equals(0)){			
						if (nodeCost.takeAvailResNumber()==0){		
						cost+= IRMConstants.IRM_PARAMETER_ILS_NODE_PENALTY;
						LOG.info("Updated penalty node cost: " + cost);
						} else {
							//if ((testbeds.get(belongsTo).equalsIgnoreCase("FEDERICA") )&& (!(role.contains("router"))) ){
							//cost+= IRMConstants.IRM_PARAMETER_ILS_NODE_PENALTY;
							//to be removed when federica works.
							//LOG.info("FEDERICA NODE penalty role: " + role + " and testbed " +testbeds.get(belongsTo));
							//}
							//else {
							cost+= ((double)nodeCost.getAverUtil()/((double)nodeCost.takeAvailResNumber()));
							LOG.info("Updated node cost: " + cost + " utilization : "+ nodeCost.getAverUtil() + " residual num: "+ nodeCost.takeAvailResNumber() );
							//}
						}
					}
				}
			}		
		}
		

		
		return cost;
	 
	 }
	 
	 
	 /**
	 * Check if the requested nodes can be facilitated by testbeds
	 * @param virtualTopology: the virtual topology.
	 * @param testbedsCost: the partitioning costs reurned by RIS
     * @param testbeds: structure with testbeds and their internal ids - splitting algortihm specific.
	 * @return cost: nodes that cannot be facilitated.
	 */
	 private Set<Resource> checkNodeAvailability (SparseMultigraph<NodeImpl,LinkImpl> virtualTopology, Vector<FPartCostTestbedResponseImpl> splittingCosts){
		Set<Resource> missingResources = new HashSet<Resource>();

		for (Node reqNode: virtualTopology.getVertices()){
		int matching = 0;
			for (FPartCostTestbedResponseImpl tmpCosts :splittingCosts) {
			
		    LOG.info("Checking node : " + reqNode.toString() + " in testbed : "+ tmpCosts.getTestbedURI());
	
				for (FPartCostRecordImpl nodeCost: tmpCosts.getNodeCosts() )
				{
					Set<String>  uris = nodeCost.getResourceURIs();
					for (String resourceURI:  uris){
						if (resourceURI.equalsIgnoreCase(reqNode.toString())){
							if (!(Integer.valueOf(nodeCost.takeAvailResNumber())).equals(0)){			
							matching++;
							}
						}
					}	
				}
			}
			if (matching==0) missingResources.add((Resource)reqNode);
		}
	 
	 return missingResources;
	 }
	 
	 
	 private Set<Resource> checkNodeAvailability (Set<Resource> unboundNodes, Vector<FPartCostTestbedResponseImpl> splittingCosts){
			Set<Resource> missingResources = new HashSet<Resource>();

			for (Resource reqRes: unboundNodes){
			int matching = 0;
				for (FPartCostTestbedResponseImpl tmpCosts :splittingCosts) {
				
			    LOG.info("Checking node : " + reqRes.toString() + " in testbed : "+ tmpCosts.getTestbedURI());
		
					for (FPartCostRecordImpl nodeCost: tmpCosts.getNodeCosts() )
					{
						Set<String>  uris = nodeCost.getResourceURIs();
						for (String resourceURI:  uris){
							if (resourceURI.equalsIgnoreCase(reqRes.toString())){
								if (!(Integer.valueOf(nodeCost.takeAvailResNumber())).equals(0)){			
								matching++;
								}
							}
						}	
					}
					
				// to be removed when charicklis fixes the problem with links
					for (FPartCostRecordImpl linkCost: tmpCosts.getLinkCosts() )
					{
						Set<String>  uris = linkCost.getResourceURIs();
						for (String resourceURI:  uris){
							if (resourceURI.equalsIgnoreCase(reqRes.toString())){
								if (!(Integer.valueOf(linkCost.takeAvailResNumber())).equals(0)){			
								matching++;
								}
							}
						}	
					}
					
				}
				if (matching==0) missingResources.add((Resource)reqRes);
			}
		 
		 return missingResources;
		 }
	/**
	 * Add nodes to interDomainEndPoints LinkedHashMap - no duplicates (SHOULD FIX THAT)
	 * @param interDomainEndPoints: LinkedHashMap containing (NSwitch) endpoints and the matching testbed internal id
	 * @param node: (NSwitch) endpoint to be added in the interDomainEndPoints.
	 * @param testbed internal id.
	 * @return updated interDomainEndPoints
	 */ 
	private  Map<Node, Integer>  add( Map<Node, Integer> interDomainEndPoints, Node node, Integer testbed) {
	
		if (!interDomainEndPoints.containsKey(node)) {
			interDomainEndPoints.put(node, testbed);
		}
		
		return (LinkedHashMap<Node, Integer> )interDomainEndPoints;
	}
	 
	/**
	 * Count how many (NSwitch) endpoints have been assigned to a testbed (SHOULD FIX THAT)
	 * @param interDomainEndPoints: LinkedHashMap containing (NSwitch) endpoints and the matching testbed internal id
	 * @param testbed: testbed internal id.
	 * @return occurences
	 */ 
		
	private Integer countValues(Map<Node, Integer> interDomainEndPoints, Integer testbed) {
	Integer occurences = 0;
	Collection<Integer> c = interDomainEndPoints.values();
	Iterator<Integer> iterator = c.iterator();
		while (iterator.hasNext()) {
		Object number = iterator.next();
			if (testbed.equals((Integer)number)) {
			occurences++;
			}

		}
	return occurences;
	}
	
	/**
	 * Estimate the cost of specific link for InP
	 * @param testbedsCost: the partitioning costs reurned by RIS
	 * @param reqLink: requested link.
	 * @param testbed: testbed internal id.
	 * @return occurences
	 */ 
	private double linkCostperInP( Map<Integer, FPartCostTestbedResponseImpl> testbedsCost, Link reqLink, Integer testbed){
			double linkCostVal = 0;
			

			FPartCostTestbedResponseImpl tmpCosts = testbedsCost.get(testbed);
			LOG.info(" getLinkCosts size " +tmpCosts.getTestbedURI() + " " +
"getNodeCosts().size( )" + tmpCosts.getNodeCosts().size() + " getLinkCosts().size())" + tmpCosts.getLinkCosts().size());
			for (FPartCostRecordImpl  linkCost: tmpCosts.getLinkCosts())
			{
				Set<String>  uris = linkCost.getResourceURIs();
					LOG.info(" checking for  testbed " +tmpCosts. getTestbedURI() + " linkCosts");
					for (String resourceURI:  uris){
						LOG.info(" virtual resource " +resourceURI);
						if (resourceURI.equalsIgnoreCase(reqLink.toString())){
							LOG.info(" Link avail  for  testbed " +tmpCosts. getTestbedURI() + " value" + linkCost.takeAvailResNumber());
							if (linkCost.takeAvailResNumber()==0){
							//if ((Integer.valueOf(linkCost.takeAvailResNumber())).equals(0)){
							linkCostVal = IRMConstants.IRM_PARAMETER_ILS_LINK_PENALTY;
							LOG.info(" Link penalty  for  testbed " +tmpCosts. getTestbedURI() + " value" + linkCostVal);
							} else {
							linkCostVal=((double)linkCost.getAverUtil()/(double)linkCost.takeAvailResNumber());
							LOG.info(" Link cost  for  testbed " +tmpCosts. getTestbedURI() + " value" + linkCostVal + " " +(double)linkCost.getAverUtil() +" " + (double)linkCost.takeAvailResNumber() );
							}
					}
				}
			}
		return linkCostVal;
	}
	
	/**
	 * Check if candidate mapping nodes are federables
	 * @param node: requested node
	 * @param tmpCosts: the partitioning costs reurned by RIS for the node's testbed.
	 * @return true
	 */ 
	private boolean isFederable (NodeImpl node, FPartCostTestbedResponseImpl tmpCosts) {
	 
			for (FPartCostRecordImpl nodeCost: tmpCosts.getNodeCosts() )
			{
				Set<String>  uris = nodeCost.getResourceURIs();
				for (String resourceURI:  uris){
					if ((resourceURI.equalsIgnoreCase(node.toString())) && (nodeCost.takeFedeResNumb()>0)){
						return true;	
					}
				}
			}

	  
	  return false;
	
	 }
	 
	 /**
	 * Check if candidate mapping nodes are federable and the cardinality of the set
	 * @param node: requested node
	 * @param tmpCosts: the partitioning costs reurned by RIS for the node's testbed.
	 * @return number
	 */ 
	 private Integer getNumberOfFederableNodes (NodeImpl node, FPartCostTestbedResponseImpl tmpCosts ) {
			for (FPartCostRecordImpl nodeCost: tmpCosts.getNodeCosts() )
			{
				Set<String>  uris = nodeCost.getResourceURIs();
				for (String resourceURI:  uris){
					if (resourceURI.equalsIgnoreCase(node.toString())){
						return nodeCost.takeFedeResNumb();	
					}
				}
			}

	  
	  return 0;
	
	 }
	

}
