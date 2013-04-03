/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 * @author <a href="mailto:simon.vocella@garr.it">Simon Vocella</a>
 * @author <a href="mailto:fabio.farina@garr.it">Fabio Farina</a>
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 */
package eu.novi.mapping.embedding.federica;


import java.util.*;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;


/**
 * Embedding algorithm for FEDERICA testbed.
 */
public class EmbeddingAlgorithmNCM {
	
	/** Local logging. */
    private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingAlgorithmNCM.class);

	public List<Map<ResourceImpl,ResourceImpl>> embedNCM(SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub,  
							ReportEvent userFeedback, String sessionID, LogService logService, String testbedName) {
		
	
		LOG.debug("Embedding into "+ testbedName+": NCM Algorithm");
		logService.log(LogService.LOG_INFO, "Embedding into "+testbedName+" using NCM Algorithm");
		
		userFeedback.instantInfo(sessionID, 
				"Embedding-"+testbedName+": Using NCM Algorithm...", 
				"Embedding process started using NCM Algorithm", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		

		// node & link mapping hash-map to be filled and returned
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>(); 
	    Map<ResourceImpl, ResourceImpl> nodeMapping =  new LinkedHashMap<ResourceImpl, ResourceImpl>(); 
	    Map<ResourceImpl, ResourceImpl> linkMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>(); 
		
		// set substrate nodes into an arrayList to have an Integer ID for each of them
	    List<Node> subNodeList = EmbeddingOperations.getNodes(sub);	
	    int subNodeCount = subNodeList.size();
	    if (subNodeCount==0) {
	    	EmbeddingOperations.printErrorFeedback(sessionID,logService, userFeedback,testbedName,
	    				"There are no nodes in the substrate", "Error: There are no substrate nodes available");
	    	return null;
	    }
		

		// set request nodes into an arrayList to have an Integer ID for each of them
	    List<Node> reqNodeList = EmbeddingOperations.getNodes(req);
	    int reqNodeCount = reqNodeList.size();
	    if (reqNodeCount==0) {
	    	LOG.info("Nothing to map");
	    	// returning empty mapping
	    	mappings.add(new LinkedHashMap<ResourceImpl, ResourceImpl>());
	    	mappings.add(new LinkedHashMap<ResourceImpl, ResourceImpl>());
	    	return mappings;
	    }
		
		// set request links into an arrayList to have an Integer ID for each of them
	    List<Link> reqLinkList = EmbeddingOperations.getLinks(req);
		int reqLinkCount = reqLinkList.size();
	
		AugSubstrate augSub = new AugSubstrate(req,sub);
		LOG.debug("Node Mapping: NCM MIP");
		NCMNodeMapping ncm  = new NCMNodeMapping(augSub, userFeedback,logService, sessionID, testbedName);
		nodeMapping = ncm.mipnm();
				
		if (nodeMapping==null){
			EmbeddingOperations.printErrorFeedback(sessionID,logService,userFeedback,testbedName,
					"Node Mapping incomplete. Request could not be satisfied", 
					"Error: Node constraints are not satisfied. Try to reduce the requested cpu, memory and/or storage");
			return null;
		} else if (reqLinkCount==0) {
			userFeedback.errorEvent(sessionID, 
    				"Embedding-"+testbedName+":Link Mapping not run.", 
					"There are no requested links", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		} else {
			LOG.debug("NodeMapping is not null and there are links to embed");
		
		
			///this is for djikstra		
			if (EmbeddingConstants.LINK_MAPPING.contains("SHORTEST_PATH")){
			LOG.info("Link Mapping: Shortest Path algorithm");
			float[][] capTable = EmbeddingOperations.getAvailableCapTable(sub,subNodeList);
			linkMapping = DijkstraLinkMapping.glm(req, sub, reqNodeList, subNodeList, reqLinkList, capTable, nodeMapping, userFeedback, sessionID);
			}
			else {
			//this is for mcf	
			LOG.info("Link Mapping: MCF algorithm");
			MCFLinkMapping mcf = new MCFLinkMapping(augSub, nodeMapping, userFeedback, logService, sessionID, testbedName);
			linkMapping = mcf.mcflm();
			}
		}

		mappings.add(nodeMapping);
		mappings.add(linkMapping);
		
		if (linkMapping==null) {
			EmbeddingOperations.printErrorFeedback(sessionID, logService,userFeedback,testbedName,
					"Link Mapping incomplete. Request could not be satisfied", 
					"Error: Link constraints are not satisfied. Try to reduce the requested bandwidth of the virtual links");
			return null;
		}
			
		// Take results
		double cost = EmbeddingOperations.costEmbedding(req); 
		LOG.debug("Embedding cost: " +cost );
	
		return mappings;
	}
	

	
	



	/**
	 * Retrieves the name of the algorithm.
	 * @param 
	 *  @return name as string.
	*/
	public String getAlgorithmName() {
		return  EmbeddingConstants.NCM_ALGORITHM_NAME;
	}



}
