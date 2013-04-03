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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;

import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.LinkImpl;

/* For user feedback */
import eu.novi.feedback.event.ReportEvent;

/**
 * Embedding algorithm for FEDERICA testbed: Greedy Power Shuffle Algorithm
 */
public class EmbeddingAlgorithmGSP {
	
	/** Local logging. */
	private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingAlgorithmGSP.class);
	
	public List<Map<ResourceImpl,ResourceImpl>> embedGSP(SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub,  
							ReportEvent userFeedback, String sessionID, LogService logService, String testbedName) {
		
		// Init userFeedback sessionID
    	if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
		
		LOG.info("Embedding into "+testbedName+": GSP Algorithm");
		logService.log(LogService.LOG_INFO, 
				"Embedding into "+testbedName+" using GSP Algorithm");
		
		userFeedback.instantInfo(sessionID, 
				"Embedding-"+testbedName+": Using GSP Algorithm...", 
				"Embedding process started using GSP Algorithm", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		
		// node & link mapping hash-map to be filled and returned
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>();
		 
	    // set substrate nodes into an arrayList 
	    // to have an Integer ID for each of them
	    List<Node> subNodeList = EmbeddingOperations.getNodes(sub);
	    
	    // set req nodes into an arrayList 
	    // to have an Integer ID for each of them
	    // bound resources are set in the first positions of the arrayList
	    List<Node> reqNodeList = EmbeddingOperations.getNodes(req);
	    
	    // set req links into an arrayList 
	    // to have an Integer ID for each of them
	    // bound resources are set in the first positions of the arrayList
	    List<Link> reqLinkList = EmbeddingOperations.getLinks(req);
	    
	    //get bandwidth on substrate links.
	    LOG.debug("Getting bw substrate capacity table...");
	    float[][] capTable = EmbeddingOperations.getAvailableCapTable(sub,subNodeList);
	
	    /***************** Phase 1: Node mapping **************/
	    
	    LOG.info("Phase 1: Mapping Nodes...");
	    Map<ResourceImpl, ResourceImpl> nodeMapping = GreedyNodeMapping.gnm(req, sub, 
	    		reqNodeList, subNodeList, capTable, userFeedback, sessionID);
	    
	    if (nodeMapping == null) {
	    	LOG.error("Node Mapping incomplete. Request could not be satisfied");
	    	return null;
	    }
	    
	    LOG.debug("Adding Node Mapping to the response...");	
    	mappings.add(nodeMapping);
    	
    	/***************** Phase 2: Link mapping **************/
    	
    	LOG.info("Phase 2: Mapping Links...");
    	
    	Map<ResourceImpl, ResourceImpl> linkMapping = DijkstraLinkMapping.glm(req, sub, 
    			reqNodeList, subNodeList, reqLinkList, capTable, 
    			nodeMapping, userFeedback, sessionID);
    
    	
    	if (linkMapping == null) {
    		LOG.error("Link Mapping incomplete. Error building physical path");
    		userFeedback.errorEvent(sessionID, 
    				"Embedding-"+testbedName+": Link Mapping incomplete. Error building physical path", 
					"Error: Path build failed. Check the correctness of the requested topology", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    		return null;
    	}
    	
    	LOG.debug("Adding Link Mapping to the response...");
    	
    	mappings.add(linkMapping);
    	
    	LOG.debug("Taking results...");
    	double cost = EmbeddingOperations.costEmbedding(req);  
		LOG.debug("cost: "+cost);
    	
	    return mappings;
	}

	

	
	public String getAlgorithmName() {
		return  EmbeddingConstants.GSP_ALGORITHM_NAME;
	}
	
}
