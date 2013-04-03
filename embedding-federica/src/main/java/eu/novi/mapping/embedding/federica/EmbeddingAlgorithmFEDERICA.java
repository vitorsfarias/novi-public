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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Group;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.core.impl.GroupImpl;


/* Remote interface imports*/
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRFailedMess;
import eu.novi.resources.discovery.response.FRResponse;


/**
 * Embedding algorithm for FEDERICA testbed.
 */
public class EmbeddingAlgorithmFEDERICA implements EmbeddingAlgorithmInterface {
	
	/** Main interface to report user feedback, initialized in blueprint based on service provided by novi-api. */
	private ReportEvent userFeedback;

	
	/** Local logging. */
    private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingAlgorithmFEDERICA.class);
    
    /** The log service. */
    private LogService logService;
	
	/** The Interface for calling RIS service. */
    private IRMCalls resourceDiscovery;
	
	@Override
	public List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID, SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub) {
		
		// Init userFeedback sessionID
    	if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
		
		LOG.debug("Embedding into "+getTestbedName());
		logService.log(LogService.LOG_INFO,"Embedding into "+getTestbedName());
		userFeedback.instantInfo(sessionID, "Embedding-"+ getTestbedName(),"Selecting Algorithm", EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>();

		logService.log(LogService.LOG_INFO, "Selecting NCM if there are links to map");
		logService.log(LogService.LOG_INFO, "Selecting GSP if there are no links to map or request is partial bound");
		
	
		if (req.getEdgeCount()>0 && !isPartialBoundGraph(req)) {
			logService.log(LogService.LOG_INFO, "NCM algorithm selected");
			EmbeddingAlgorithmNCM ncm = new EmbeddingAlgorithmNCM();
			mappings = ncm.embedNCM(req,sub,userFeedback,sessionID,logService,getTestbedName());
		} else {
			logService.log(LogService.LOG_INFO, "GSP algorithm selected");
			EmbeddingAlgorithmGSP gsp = new EmbeddingAlgorithmGSP();
			mappings = gsp.embedGSP(req,sub,userFeedback,sessionID,logService,getTestbedName());
			
		}
		
		if (mappings==null){
			logService.log(LogService.LOG_ERROR,"unable to select and use FEDERICA embedding algorithm");
		}
		
		return mappings;
	}
	
	@Override
	public List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {
		
		// Init userFeedback sessionID
    	if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
		
		if (partialTopology==null) {
			EmbeddingOperations.printErrorFeedback(sessionID, logService, userFeedback, getTestbedName(), 
					" Request received is null", "Error: Request received is null");
    		return null;
    	}
			
		LOG.debug("Embedding into "+getTestbedName());
		logService.log(LogService.LOG_INFO,"Embedding into "+getTestbedName());		
		
		SparseMultigraph<NodeImpl, LinkImpl> sub = resourceDiscovery(sessionID, partialTopology, noviUser);
		
    	if (sub==null) {
    		EmbeddingOperations.printErrorFeedback(sessionID, logService, userFeedback, getTestbedName(), 
					"Resource discovery returned no appropriate response", "Error: Substrate response is null");
    		return null;
    	}

		SparseMultigraph<NodeImpl, LinkImpl> req = EmbeddingOperations.translateIMToGraph(partialTopology,true);
		EmbeddingOperations.analyzeGraph(req, logService);
			
		userFeedback.instantInfo(sessionID, 
				"Embedding-"+ getTestbedName(),
				"Selecting Algorithm", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		
		
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>();
		logService.log(LogService.LOG_INFO, "Selecting NCM if there are links to map");
		logService.log(LogService.LOG_INFO, "Selecting GSP if there are no links to map or request is partial bound");
		

		if (req.getEdgeCount()>0 && !isPartialBoundGraph(req)) {
			logService.log(LogService.LOG_INFO, "NCM algorithm selected");
			EmbeddingAlgorithmNCM ncm = new EmbeddingAlgorithmNCM();
			mappings = ncm.embedNCM(req,sub,userFeedback,sessionID,logService,getTestbedName());
		} else {
			logService.log(LogService.LOG_INFO, "GSP algorithm selected");
			EmbeddingAlgorithmGSP gsp = new EmbeddingAlgorithmGSP();
			mappings = gsp.embedGSP(req,sub,userFeedback,sessionID,logService,getTestbedName());
			
		}
		
		if (mappings==null){
			EmbeddingOperations.printErrorFeedback(sessionID, logService, userFeedback, getTestbedName(), 
					"Unable to select and use FEDERICA embedding algorithm", 
					"Error: Mappings response is null");
		}
		
		return mappings;
	}
	
	
	
	
	public SparseMultigraph<NodeImpl, LinkImpl> resourceDiscovery(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {
	
		userFeedback.instantInfo(sessionID, 
				"Embedding Calling RIS...", 
				"Calling RIS - find resources...", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		 
		LOG.debug("sessionID: " + sessionID + " " + noviUser.toString() + " " + partialTopology.toString());
		
		FRResponse resp = resourceDiscovery.getSubstrateAvailability(sessionID);
		
		if (resp.hasError()) {
			userFeedback.errorEvent(sessionID, 
					"Embedding Error in find resources response", 
					"There was error finding resources", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
					
			Iterator<Entry<Resource, FRFailedMess>> errorIt = resp.getFailedResources().entrySet().iterator();
			while (errorIt.hasNext()) {
				Entry<Resource, FRFailedMess> errorEntry = errorIt.next();
				logService.log(LogService.LOG_ERROR, 
						"Resource "+errorEntry.getKey().toString()+" with error: "+errorEntry.getValue());
				userFeedback.errorEvent(sessionID, 
						"Embedding Error in find resources response", 
						"Resource "+errorEntry.getKey()+" with error: "+
							errorEntry.getValue(), 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			}
			if (!resp.getUserFeedback().isEmpty()) {
				userFeedback.errorEvent(sessionID, 
						"RIS detailed information/suggestions:", 
						resp.getUserFeedback(), 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			}
			
			return null;
		}
		
		Group availableResources = resp.getTopology();
		 logService.log(LogService.LOG_INFO, 
				 "Analyzing Topology returned by RIS");
		 EmbeddingOperations.analyzeGroup(availableResources, logService);
		 
		logService.log(LogService.LOG_INFO,"Checking available resources...");
		if (EmbeddingOperations.isSetEmpty(availableResources.getContains())) {
			userFeedback.errorEvent(sessionID, 
					"Embedding Error getting available resources", 
					"Error There are no available resources" +
						" for the request in "+availableResources.toString(), 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return null;
		}
		
		Set<Resource> contained = availableResources.getContains();
		logService.log(LogService.LOG_DEBUG,"Num of available resources: "+contained.size());	
		logService.log(LogService.LOG_INFO,"Translating topology for using the embedding alorithm...");
		SparseMultigraph<NodeImpl, LinkImpl> sub = EmbeddingOperations.translateIMToGraph(availableResources,true);
		EmbeddingOperations.analyzeGraph(sub, logService);
		
		return  sub;
	
	}
	
	
	/**
	 * Checks if the given graph is partial bounded.
	 *
	 * @param graph the graph to check
	 * @return true, if is partial bound graph
	 */
	public static boolean isPartialBoundGraph(SparseMultigraph<NodeImpl, LinkImpl> graph) {	
		Collection<NodeImpl> nodes = graph.getVertices();
		for (Node current : nodes){
			if (!EmbeddingOperations.isSetEmpty(current.getImplementedBy())) {
				return true;
			}
		}
		Collection<LinkImpl> links = graph.getEdges();
		for (LinkImpl current : links) {
			if (!EmbeddingOperations.isSetEmpty(current.getProvisionedBy())) {
				return true;
			}
		}
		return false;
	}
	
	/*************************************************************
	************************ Testing methods *********************
	*************************************************************/
	@Override
	public String whoAreYou (String question) {
		logService.log(LogService.LOG_INFO, "Message: "+question);
		return "I am rVine algorithm in "+getTestbedName();
	}
	
	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/
	
	@Override
	public String getTestbedName() {
		return EmbeddingConstants.TESTBED_NAME;
	}
	
	@Override
	public String getAlgorithmName() {
		return EmbeddingConstants.FED_ALGORITHM_NAME;
	}
	
	@Override
	public String getTestbedTopLeveAuthority(){
		return EmbeddingConstants.TOP_LEVEL_AUTHORITY;
	};
	
	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}
	
	public ReportEvent getUserFeedback() {
		return userFeedback;
    }
    
    public void setUserFeedback(ReportEvent userFeedback) {
		this.userFeedback = userFeedback;
    }
	
			// RIS
	public IRMCalls getResourceDiscovery() {
		return resourceDiscovery;
	}

	public void setResourceDiscovery(IRMCalls resourceDiscovery) {
		this.resourceDiscovery = resourceDiscovery;
	}

}
