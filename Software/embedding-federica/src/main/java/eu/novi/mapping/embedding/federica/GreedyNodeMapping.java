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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;

/**
 * Greedy Node Mapping
 */
public final class GreedyNodeMapping {

	/**
	 * Private constructor
	 */
	private GreedyNodeMapping(){}
	
	/** Local logging. */
	private static final transient Logger LOG = LoggerFactory.getLogger(GreedyNodeMapping.class);
	
	public static Map<ResourceImpl, ResourceImpl> gnm(SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub,
			List<Node> reqNodeList, List<Node> subNodeList, float[][] capTable, ReportEvent userFeedback, String sessionID) {
		
		Map<ResourceImpl, ResourceImpl> nodeMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>(); // request-real
		
		LOG.debug("Creating costs for Substrate Nodes");
    	float[] average = createCosts(subNodeList,capTable);
    	
        ///////////////////////////////////////////////////////////////
		// Build arrays for the different functional characteristics //
    	// (e.g. cpu, memory , disk for each servers                 //
		///////////////////////////////////////////////////////////////
    	
    	LOG.debug("Getting functional characteristics...");
    	float[][] subCost = EmbeddingOperations.getAvailableCapacities(subNodeList);
		float[][] reqCost = EmbeddingOperations.getCapacities(reqNodeList);
		
    	for (int h=0; h<reqNodeList.size();h++) {
  
    		// checking bound nodes
    		if (!EmbeddingOperations.isSetEmpty(reqNodeList.get(h).getImplementedBy())) {
    			LOG.info(reqNodeList.get(h)+ " is already bound");
    			if (!processBoundNode((VirtualNode) reqNodeList.get(h),h,subNodeList,
    					nodeMapping,reqCost,subCost,userFeedback,sessionID)) {return null;}
    			else {continue;}
    		}
    		
    		Node selected=null;
    		LOG.debug("Virtual Role for the current Node: "+((VirtualNode) reqNodeList.get(h)).getVirtualRole());
    		
    		if (EmbeddingOperations.isVirtualMachine((VirtualNode)reqNodeList.get(h))) {
    			selected = mapVirtualMachine(reqNodeList.get(h),h,subNodeList,nodeMapping,reqCost,subCost,average);
    		}
    		else if (EmbeddingOperations.isVirtualRouter((VirtualNode)reqNodeList.get(h))) {
    			selected = mapVirtualRouter(reqNodeList.get(h),subNodeList,nodeMapping,average);
    		}
    			
    		/* update mapping and remove from pool of available substrate resources */
    		if (selected!=null){
    			LOG.debug("Request Node "+reqNodeList.get(h)+" mapped into "+selected);
    			nodeMapping.put((ResourceImpl)reqNodeList.get(h),(ResourceImpl)selected);
    		}
    		else {
    			userFeedback.errorEvent(sessionID, 
        				"Embedding error: Node Mapping incomplete. Request Node "+reqNodeList.get(h)+" can not be mapped", 
    					"Error: Node constraints are not satisfied. Try to reduce the requested cpu, memory and/or storage", 
    					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    			break;
    		} 
    	}
    	
    	if (nodeMapping.size()==req.getVertexCount()) {
    		LOG.info("Node Mapping done successfully");
    		return nodeMapping;
    	}
    	else {
    		LOG.error("Node Mapping incomplete. Request could not be satisfied");
    		return null;
    	}
	}


	/**
	 * Process bound node If the constraints are ok it is added to the node mapping.
	 * PRE: vNode has implemtedBy relation.
	 *
	 * @param vNode the virtual node
	 * @param vNodeIndex the vNode index
	 * @param subNodeList the substrate node list
	 * @param nodeMapping the node mapping
	 * @param reqCost the request cost
	 * @param subCost the substrate cost
	 * @param userFeedback the user feedback
	 * @param sessionID the session id
	 * @return true, if successful
	 */
	private static boolean processBoundNode(VirtualNode vNode, int vNodeIndex,
			List<Node> subNodeList,
			Map<ResourceImpl, ResourceImpl> nodeMapping, float[][] reqCost,
			float[][] subCost, ReportEvent userFeedback, String sessionID) {
		
		String pNodeId = vNode.getImplementedBy().iterator().next().toString();
		Node pNode = EmbeddingOperations.getNode(pNodeId,subNodeList);
		if (pNode == null) {
			// bound is not correct. Physical node does not exist
			LOG.error("Mapping incomplete. Bound physical resource "+pNodeId+" does not exist in the substrate");
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Bound physical resource "+pNodeId+" does not exist in the substrate", 
					"Error: There are no substrate node with ID: "+pNodeId+". Check the node ID and try again", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		if (!EmbeddingOperations.areCompatible(vNode, pNode)) {
			// bound is not correct. Not compatible resources
			LOG.error("Mapping incomplete. Bound physical resource "+pNodeId+" is not compatible with "+vNode.toString());
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Bound physical resource "+pNodeId+" is not compatible with "+vNode.toString(), 
					"Error: Virtual role of vNode ("+vNode.getVirtualRole()+") does not correspond with Hardware Type of the physical node ("+pNode.getHardwareType()+")", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		if (!checkConstraints(vNode, vNodeIndex, pNode, subNodeList.indexOf(pNode), reqCost, subCost)) {
			// bound is not correct. Constraints error
			LOG.error("Mapping incomplete. Constraints error between "+pNodeId+" and "+vNode.toString());
			userFeedback.errorEvent(sessionID, 
    				"Embedding error: Node Mapping incomplete. Bound Request Node "+vNode.toString()+" can not be mapped", 
					"Error: Node constraints are not satisfied. Try to reduce the requested cpu, memory and/or storage or use another physical router", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		
		// Adding bound node to node mapping
		nodeMapping.put((NodeImpl)vNode, (NodeImpl)pNode);
		
		return true;
	}
	
	/**
	 * Check constraints between a virtual node and its physical node
	 *
	 * @param vNode the virtual node
	 * @param vNodeIndex the virtual node index
	 * @param pNode the physical node
	 * @param pNodeIndex the physical node index
	 * @param reqCost the request cost
	 * @param subCost the substrate cost
	 * @return true, if successful
	 */
	private static boolean checkConstraints(VirtualNode vNode, int vNodeIndex,
			Node pNode, int pNodeIndex, float[][] reqCost, float[][] subCost) {
		
		if (!EmbeddingOperations.checkFederableConstraint(vNode,pNode)) {
			LOG.error("Federation constraints error");
			return false;
		}
		
		if (EmbeddingOperations.isVirtualMachine(vNode)) {
			LOG.debug("Checking out VM "+vNode.toString()+" for Server "+pNode.toString()+"...");
			int counter2=0;
			int funcTypes  = EmbeddingConstants.NON_FUNCTIONAL_VALUES;
			for (int j=0;j<funcTypes;j++){
				if (subCost[j][pNodeIndex]>=reqCost[j][vNodeIndex]){
					LOG.debug("Capacity constraint ok");
					counter2++;
				}
			}
			if (counter2==funcTypes) {
				return true;
			}
		} else if (EmbeddingOperations.isVirtualRouter(vNode)) {
			if (pNode.getHasAvailableLogicalRouters()>0) {
				LOG.error("Avaiable logical routers constraints error");
				return true;
			}
		}
		return false;
	}


	/**
	 * Maps a virtual machine.
	 *
	 * @param vNode the virtual node
	 * @param vNodeIndex the vitual node index
	 * @param subNodeList the substrate node list
	 * @param nodeMapping the node mapping
	 * @param reqCost the request cost
	 * @param subCost the substrate cost
	 * @param average the average
	 * @return the selected substrate node
	 */
	private static Node mapVirtualMachine(Node vNode, int vNodeIndex, List<Node> subNodeList,
			Map<ResourceImpl, ResourceImpl> nodeMapping, float[][] reqCost,
			float[][] subCost, float[] average) {
		
		Node selected = null;
		
		int funcTypes  = EmbeddingConstants.NON_FUNCTIONAL_VALUES;
		double max=0;
		int counter2=0;
		
		for (int i=0; i<subNodeList.size();i++) {
			if (EmbeddingOperations.isServer(subNodeList.get(i))
					&& (!nodeMapping.containsValue(subNodeList.get(i)))
					&& EmbeddingOperations.checkFederableConstraint((VirtualNode) vNode,
							subNodeList.get(i))) {
				/* if substrate node not selected */
				LOG.debug("Checking out VM "+vNode+" for Server "+subNodeList.get(i)+"...");
				counter2=0;
				//check that capacity constraints are satisfied for every functional type in the node.
				for (int j=0;j<funcTypes;j++){
					if (subCost[j][i]>=reqCost[j][vNodeIndex]){
						LOG.debug("Capacity constraints ok");
						counter2++;
					}
				}
				/* if capacity constraints are satisfied check if it has the biggest average */
				if (counter2==funcTypes
						&& average[i]>max) {
					LOG.debug("Server: "+subNodeList.get(i)+" has average capacity of "+average[i]);
					LOG.debug("Max average ok");
					max=average[i];
					selected=subNodeList.get(i);
				}
			}
		}
		
		return selected;
	}

	/**
	 * Maps a virtual router.
	 *
	 * @param vNode the virtual node
	 * @param subNodeList the substrate node list
	 * @param nodeMapping the node mapping
	 * @param average the average
	 * @return the selected physical node
	 */
	private static Node mapVirtualRouter(Node vNode, List<Node> subNodeList,
			Map<ResourceImpl, ResourceImpl> nodeMapping, float[] average) {
		Node selected = null;
		double max=0;
		
		for (int i=0; i<subNodeList.size();i++) {
			if (EmbeddingOperations.isRouter(subNodeList.get(i))
					&& !nodeMapping.containsValue(subNodeList.get(i))
					&& subNodeList.get(i).getHasAvailableLogicalRouters()>0
					&& average[i]> max
					&& EmbeddingOperations.checkFederableConstraint((VirtualNode) vNode,
							subNodeList.get(i))) {
				/* if substrate node not selected check that capacity constraints are satisfied for the node. */
				LOG.debug("Checking out VirtualRouter "+vNode+" for Router "+subNodeList.get(i)+"...");
				LOG.debug("Max average ok");
				max=average[i];
				selected=subNodeList.get(i);
			}
		}
		return selected;
	}

	/**
	 * Create cost for physical nodes
	 * @param subNodeList physical node list
	 * @param capTable capacity table
	 * @return costs
	 */
	private static float[] createCosts(List<Node> subNodeList, float[][] capTable) {
		/* number of nodes */
		int initSubNodeNum = subNodeList.size();
		
		LOG.info("Number of substrate nodes: "+initSubNodeNum);
			
		float[] average = new float[initSubNodeNum];
		float[] adjBandwidth = new float[initSubNodeNum];
		
		/* Find the average of every substrate node regarding available resources */
		for (int i=0;i<initSubNodeNum;i++) {
			
			LOG.debug("Creating cost for node: "+subNodeList.get(i).toString());
			
			if (EmbeddingOperations.isServer(subNodeList.get(i))) {
				LOG.debug(subNodeList.get(i).toString()+" is a server");
				average[i] = getNodeComponentAvailableCost(subNodeList.get(i));
			}
			if (EmbeddingOperations.isRouter(subNodeList.get(i))) {
				LOG.debug(subNodeList.get(i).toString()+" is a router");
				average[i] = subNodeList.get(i).getHasAvailableLogicalRouters().floatValue();
			}

			/* apart form node capacity take into consideration link capacity of links leaving the node
			 * Multiply the capacities with sum of adjacent substrate links of every substrate node */
			adjBandwidth[i] = getAdjacentLinksCapacity(initSubNodeNum,i,capTable);
			average[i]=average[i]*adjBandwidth[i];
			LOG.debug("Average value for Node "+subNodeList.get(i)+": "+average[i]);
		}
		return average;
	}

	/**
	 * Gets the node component available cost.
	 *
	 * @param node the node
	 * @return the node component available cost
	 */
	private static float getNodeComponentAvailableCost(Node node) {
		float toReturn = 0;
		for (NodeComponent nc : node.getHasComponent()) {
			LOG.debug("Current node component: "+nc.toString());
			if (nc instanceof CPU) {
				LOG.debug("NC is CPU component");
				if (((CPU) nc).getHasCPUSpeed()==null) {
					LOG.debug("CPU speed is null");
				}
				toReturn += ((CPU) nc).getHasCPUSpeed();
				toReturn += ((CPU) nc).getHasAvailableCores().intValue();
			} 
			else if (nc instanceof Memory) {
				LOG.debug("NC is Memory component");
				toReturn += ((Memory) nc).getHasAvailableMemorySize();
			} else if (nc instanceof Storage) {
				LOG.debug("NC is Storage component");
				toReturn += ((Storage) nc).getHasAvailableStorageSize();
			}
		}
		return toReturn;
	}
	
	/**
	 * Gets the adjacent links capacity for a node.
	 *
	 * @param initSubNodeNum the init sub node num
	 * @param nodeIndex the node index
	 * @param capTable the cap table
	 * @return the adjacent links capacity
	 */
	private static float getAdjacentLinksCapacity(int initSubNodeNum,
			int nodeIndex, float[][] capTable) {
		float toReturn = 0;
		for (int j=0;j<initSubNodeNum;j++) {
			if (capTable[nodeIndex][j]!=0){
				toReturn += capTable[nodeIndex][j];
			}			
		}
		return toReturn;
	}
}
