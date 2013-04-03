/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.planetlab;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.planetlab.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.planetlab.utils.EmbeddingOperations;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Group;
import eu.novi.im.core.Topology;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.core.impl.GroupImpl;

/* For user feedback */
import eu.novi.feedback.event.ReportEvent;
/* Remote interface imports*/
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRFailedMess;
import eu.novi.resources.discovery.response.FRResponse;

/**
 * Embedding algorithm for PlanetLab testbed (Greedy Node Mapping).
 * 
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
public class EmbeddingAlgorithmGNM implements EmbeddingAlgorithmInterface {

	/** Main interface to report user feedback, initialized in blueprint based on service provided by novi-api. */
	private ReportEvent userFeedback;

	/** The session id. Temporary to keep the value obtained from NOVI-API,
	  * to be used when reporting user feedback.*/
	private String sessionID; 
	
    /** Local logging. */
	private static final transient Logger LOG = LoggerFactory.getLogger(EmbeddingAlgorithmGNM.class);
    
    /** The log service. */
    private LogService logService;
	
		 /** The Interface for calling RIS service. */
    private IRMCalls resourceDiscovery;
		/**
	 * Embedding algorithm for PlanetLab testbed (Greedy Node Mapping).
	 *
	 * @param partialTopology the request
	 * @param noviUser the requester
	 * @return a list of mappings: nodes only
	 */
	@Override
	public List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {
		String testbed = getTestbedName();
    	if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
		LOG.debug("sessionID "+ sessionID+ " embedding into "+testbed);
		logService.log(LogService.LOG_INFO,"Embedding into "+testbed);		
		
		 if (partialTopology!=null) {
    		logService.log(LogService.LOG_INFO, "Request: "+partialTopology);
    	} else {
    		logService.log(LogService.LOG_ERROR, "Request is null");
    		userFeedback.errorEvent(sessionID, 
    				"Embedding-"+testbed+": Request received is null", 
					"Error: Request received is null", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    		return null;
    	}
		
		 
		SparseMultigraph<NodeImpl, LinkImpl> req = EmbeddingOperations.translateIMToGraph(partialTopology,true);
			
		if (EmbeddingOperations.analyzeGraph(req, logService)==-1){
			logService.log(LogService.LOG_ERROR, "Request has links - is not supported by: "+ testbed);
    		userFeedback.errorEvent(sessionID, 
    				"Embedding-"+testbed+": Request not supported", 
					"Error: Request contains Link resources", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    		return null;
		}

		 
		 
		SparseMultigraph<NodeImpl, LinkImpl> sub = resourceDiscovery(sessionID, partialTopology, noviUser);
		
		if (sub!=null) {
    		logService.log(LogService.LOG_INFO, "Substrate: "+sub);
    	} else {
    		logService.log(LogService.LOG_ERROR, "Resource discovery returned no appropriate response");
    		return null;
    	}
		

			
      	userFeedback.instantInfo(sessionID, 
				"Embedding-"+testbed+": Using Greddy Node Mapping...", 
				"Embedding process started using Gredy Node Mapping", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    	
    	
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>();
		Map<ResourceImpl, ResourceImpl> phi = new LinkedHashMap<ResourceImpl, ResourceImpl>(); // request-real
		
		LOG.info("Checking bound resources...");
		if (!processBoundResources(req, sub, phi)) { return null;}
		LOG.info(phi.size()+ " Resources are already bound");
		
		int reqNodeNum=req.getVertexCount();
		LOG.info("Embedding into "+getTestbedName()+"...");
		for (int i=0;i<reqNodeNum;i++) {
			if (!embedResource(req, sub, phi)) {return null;}
		}
		
		LOG.info("Mapping done successfully");
		mappings.add(phi);
		
		return mappings;
		
		
	}
	

	/**
	 * Embedding algorithm for PlanetLab testbed (Greedy Node Mapping).
	 *
	 * @param req the request
	 * @param sub the substrate
	 * @return the list of mapped resources
	 */
    @Override
	public List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID, SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub) {
    	
    	/* Init userFeedback sessionID */
    	if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
    	
    	logService.log(LogService.LOG_INFO, 
				"Embedding into "+getTestbedName()+" using Greddy Node Mapping");
    	
    	userFeedback.instantInfo(sessionID, 
				"Embedding-"+getTestbedName()+": Using Greddy Node Mapping...", 
				"Embedding process started using Gredy Node Mapping", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    	
    	if (req!=null) {
    		logService.log(LogService.LOG_INFO, "Request: "+req);
    	} else {
    		logService.log(LogService.LOG_ERROR, "Request is null");
    		userFeedback.errorEvent(sessionID, 
    				"Embedding-"+getTestbedName()+": Request received is null", 
					"Error: Request received is null", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    		return null;
    	}
    	if (sub!=null) {
    		logService.log(LogService.LOG_INFO, "Substrate: "+sub);
    	} else {
    		logService.log(LogService.LOG_ERROR, "Substrate is null");
    		userFeedback.errorEvent(sessionID, 
    				"Embedding-"+getTestbedName()+": Testbed substrate is null", 
					"Error: Testbed substrate is null", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
    		return null;
    	}
    	
		List<Map<ResourceImpl, ResourceImpl>> mappings =  new LinkedList<Map<ResourceImpl, ResourceImpl>>();
		Map<ResourceImpl, ResourceImpl> phi = new LinkedHashMap<ResourceImpl, ResourceImpl>(); // request-real
		
		LOG.info("Checking bound resources...");
		if (!processBoundResources(req, sub, phi)) { return null;}
		LOG.info(phi.size()+ " Resources are already bound");
		
		int reqNodeNum=req.getVertexCount();
		LOG.info("Embedding into "+getTestbedName()+"...");
		for (int i=0;i<reqNodeNum;i++) {
			if (!embedResource(req, sub, phi)) {return null;}
		}
		
		LOG.info("Mapping done successfully");
		mappings.add(phi);
		
		return mappings;
	}
	
	
	 public SparseMultigraph<NodeImpl, LinkImpl> resourceDiscovery(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {
		
		userFeedback.instantInfo(sessionID, 
				"Embedding Calling RIS...", 
				"Calling RIS - find resources...", 
				EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
		 
		LOG.debug("sessionID: " + sessionID + " " + noviUser.toString() + " " + partialTopology.toString());
		FRResponse resp = resourceDiscovery.findResources(sessionID, (Topology) partialTopology, noviUser);
		
		if (resp.hasError()) {
			userFeedback.errorEvent(sessionID, 
					"Embedding Error in find resources response", 
					"There was error finding resources", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			LOG.debug("There was error finding resources");	
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
							LOG.debug("Embedding Error in find resources responses");
			}
			
			if (!resp.getUserFeedback().isEmpty()) {
				userFeedback.errorEvent(sessionID, 
						"RIS detailed information/suggestions:", 
						resp.getUserFeedback(), 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
						LOG.debug("RIS detailed information/suggestions");
			}
			
			return null;
		}
		
		Group availableResources = resp.getTopology();
		 logService.log(LogService.LOG_INFO, 
				 "Analyzing Topology returned by RIS");
		
		if (EmbeddingConstants.DEBUG_EMBEDDING){
		EmbeddingOperations.analyzeGroup(availableResources, logService);
		}
		
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
	 * Process bound resources. If a virtual node is bounded, then the constraints are checked.
	 * If the bound is OK, then the virtual resource is removed from the request and the physical
	 * node is removed from the substrate.
	 *
	 * @param req the request
	 * @param sub the substrate
	 * @param phi the mapping
	 * @return true, if no errors
	 */
	private boolean processBoundResources(
			SparseMultigraph<NodeImpl, LinkImpl> req,
			SparseMultigraph<NodeImpl, LinkImpl> sub,
			Map<ResourceImpl, ResourceImpl> phi) {
		
		for (Node vnode : req.getVertices()) {
			if (EmbeddingOperations.isSetEmpty(vnode.getImplementedBy())) {continue;}
			String pnodeId = vnode.getImplementedBy().iterator().next().toString();
			Node pnode = EmbeddingOperations.getNode(pnodeId,sub);
			if (pnode == null) {
				// bound is not correct
				LOG.error("Mapping incomplete. Bound physical resource "+pnodeId+" does not exist in the substrate");
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+": Bound physical resource "+pnodeId+" does not exist in the substrate", 
						"Error: There are no substrate node with ID: "+pnodeId+". Check the node ID and try again", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return false;
			}
			if (!EmbeddingOperations.checkConstraints(vnode, pnode)) {
				// constraints are not satisfied
				LOG.error("Mapping incomplete. Constraints are not satisfied for the bound physical resource "+pnodeId);
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+": Constraints are not satisfied for the bound physical resource "+pnodeId, 
						"Error: Constraints are not satisfied for the bound physical resource "+pnodeId+". Try to reduce the requested cpu, memory and/or storage or switch to another substrate node", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return false;
			}
			
			phi.put((NodeImpl)vnode, (NodeImpl)pnode);
		}
		
		// removing bound resources
		for (Entry<ResourceImpl, ResourceImpl> entry : phi.entrySet()) {
			req.removeVertex((NodeImpl) entry.getKey());
			sub.removeVertex((NodeImpl) entry.getValue());
		}
		
		return true;
	}

	/**
	 * Map of a virtual resource with a physical resource.
	 *
	 * @param req virtual request
	 * @param sub physical substrate
	 * @param phi mapping
	 * @return true if success; false otherwise
	 */
	private boolean embedResource(SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub,
			Map<ResourceImpl, ResourceImpl> phi) {
		
		LOG.info("Finding the virtual node with maximum resource requirement...");
		Node virtMax = findMaximumVirtualNode(req);
		
		/* It is necessary to check that because already used sub nodes are deleted
		from the sub graph */
		if (sub.getVertexCount()==0) {
			LOG.error("There are no substrate nodes available");
			userFeedback.errorEvent(sessionID, 
    				"Embedding-"+getTestbedName()+": There are no substrate nodes available", 
					"Error: There are no substrate nodes available", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		
		LOG.info("Finding the substrate node with the maximum residual capacity...");
		logService.log(LogService.LOG_INFO, "Number of substrate nodes: "+sub.getVertexCount());
		Node subMax = findMaximumSubstrateNode(sub);
		
		LOG.debug("Checking substrate node with maximum residual capacity...");
		if (subMax == null) {
			LOG.error("Mapping incomplete. Request could not be satisfied");
			userFeedback.errorEvent(sessionID, 
    				"Embedding-"+getTestbedName()+": There are no substrate nodes with available resources", 
					"Error: There are no substrate nodes with available resources", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		
		LOG.debug("Checking request/substrate constraints...");
		if (!EmbeddingOperations.checkConstraints(virtMax,subMax)) {
			LOG.error("Mapping incomplete. Request could not be satisfied");
			userFeedback.errorEvent(sessionID, 
    				"Embedding-"+getTestbedName()+": Constraints are not satisfied. Try to reduce the requested cpu, memory and/or storage", 
					"Error: Constraints are not satisfied. Try to reduce the requested cpu, memory and/or storage", 
					EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
			return false;
		}
		
		LOG.info("Mapping Node...");
		phi.put((NodeImpl)virtMax, (NodeImpl)subMax);
					
		req.removeVertex((NodeImpl) virtMax);
		sub.removeVertex((NodeImpl) subMax);
		
		return true;
	}

	/**
	 * Obtain the maximum virtual node.
	 *
	 * @param req virtual request
	 * @return maximum virtual node
	 */
	private Node findMaximumVirtualNode(SparseMultigraph<NodeImpl, LinkImpl> req) {
		
		Node virtMax=null;
		
		float max=0;
		float virtCost;
		float cpuSpeedReq;
		float cpuCoresReq;
		float memReq;
		float diskReq;
		String cpuSpeed = "";
		String mem = "";
		String disk = "";
		
		for (Node node : req.getVertices()){

			cpuCoresReq = EmbeddingOperations.getCPUCores(node);
			cpuSpeedReq = EmbeddingOperations.getCPUSpeed(node);
			memReq = EmbeddingOperations.getMemory(node);
			diskReq = EmbeddingOperations.getStorage(node);
			
			virtCost = cpuCoresReq+cpuSpeedReq+memReq+diskReq;
			
			if (virtCost>=max){
				max=virtCost;
				virtMax=node;
				cpuSpeed = EmbeddingOperations.nodeComponentValueToString(cpuSpeedReq);
				mem = EmbeddingOperations.nodeComponentValueToString(memReq);
				disk = EmbeddingOperations.nodeComponentValueToString(diskReq);
			}
		}
		
		/* max virtual node */
		EmbeddingOperations.printNodeInfo(virtMax,cpuSpeed,mem,disk);
		
		return virtMax;
	}

	/**
	 * Obtain the maximum physical node.
	 *
	 * @param sub physical substrate
	 * @return maximum physical node
	 */
	private Node findMaximumSubstrateNode(SparseMultigraph<NodeImpl, LinkImpl> sub) {
		
		Node subMax=null;
		
		float max=0;
		String cpuSpeedSubstrate= "";
		String memSubstrate = "";
		String diskSubstrate = "";
		
		logService.log(LogService.LOG_DEBUG, "Number of substrate nodes: "+sub.getVertexCount());
		
		for (Node node:sub.getVertices()){
			float realCost=0; float cpuCoresSub =0; float cpuSpeedSub =0; float memSub = 0; float diskSub =0;
			
			
			logService.log(LogService.LOG_INFO,"Processing substrate Node: "+node.toString());		

			Set<NodeComponent> ncs = node.getHasComponent();
			if (ncs==null) {
				LOG.warn("Substrate Node: "+node.toString()+" has no components defined");
				continue;
			}/* ignoring Nodes with no components */
					
			Iterator<NodeComponent> testIterator = ncs.iterator();
			while (testIterator.hasNext()){
				NodeComponent curComponent = testIterator.next();
				cpuCoresSub = checkCPUcores(curComponent,node);
				realCost +=cpuCoresSub;
				cpuSpeedSub = checkCPUspeed(curComponent,node);
				realCost +=cpuSpeedSub;
				memSub = checkMEM(curComponent,node);
				realCost +=memSub;
				diskSub=checkSTO(curComponent,node);
				realCost +=diskSub;
			}

			if (realCost>=max){
				max=realCost;
				subMax=node;
				cpuSpeedSubstrate = EmbeddingOperations.nodeComponentValueToString(cpuSpeedSub);
				memSubstrate = EmbeddingOperations.nodeComponentValueToString(memSub);
				diskSubstrate = EmbeddingOperations.nodeComponentValueToString(diskSub);
			}
		}
		
		EmbeddingOperations.printNodeInfo(subMax,cpuSpeedSubstrate,memSubstrate,diskSubstrate);
		
		return subMax;

	}
	
	public float  checkCPUcores (NodeComponent curComponent, Node node){
		float cpuCoresSub =0; 
		
		if(curComponent instanceof CPU){
			BigInteger cores = ((CPU) curComponent).getHasAvailableCores();
			if (cores==null) {
				LOG.error("Mapping incomplete. Error: Available CPU cores is not set for "+node.toString());
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+": Error on substrate node "+node.toString(), 
						"Error: Available CPU cores is not set", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return -1;
			}
			
			cpuCoresSub=cores.floatValue();
			LOG.debug("cpu cores="+cpuCoresSub);		
		}
		
		
		return cpuCoresSub;
	}
	
	public float  checkCPUspeed(NodeComponent curComponent, Node node){
		float cpuSpeedSub =0; 
		
		if(curComponent instanceof CPU){
			if (((CPU)curComponent).getHasCPUSpeed()==null) {	
				LOG.error("Mapping incomplete. Error: CPU speed is not set for "+node.toString());
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+": Error on substrate node "+node.toString(), 
						"Error: CPU speed is not set", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return -1;
			}
			
			cpuSpeedSub = ((CPU)curComponent).getHasCPUSpeed();
			LOG.debug("cpu="+cpuSpeedSub);
			
		}
		
		
		return cpuSpeedSub;
	}

	
	public float  checkMEM(NodeComponent curComponent, Node node){
		float memSub = 0; 
		
		if(curComponent instanceof Memory){
			if (((Memory)curComponent).getHasAvailableMemorySize()==null) {
				LOG.error("Mapping incomplete. Error: Available memory is not set for "+node.toString());
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+":  Error on substrate node "+node.toString(), 
						"Error: Available memory is not set", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return -1;
			}
			memSub = ((Memory)curComponent).getHasAvailableMemorySize();
			LOG.debug("mem="+memSub);
		}
		
		
		return memSub;
	}
	
	public float  checkSTO(NodeComponent curComponent, Node node){
		float diskSub =0;
		
		if(curComponent instanceof Storage){
			if (((Storage)curComponent).getHasAvailableStorageSize()==null) {
				LOG.error("Mapping incomplete. Error: Available storage is not set for "+node.toString());
				userFeedback.errorEvent(sessionID, 
	    				"Embedding-"+getTestbedName()+": There are an error on substrate node "+node.toString(), 
						"Error: Available storage is not set", 
						EmbeddingConstants.EMBEDDING_FEEDBACK_URL);
				return -1;
			}
			diskSub = ((Storage)curComponent).getHasAvailableStorageSize();
			LOG.debug("disk="+diskSub);
		}
		
		return diskSub;
	}
	
	

	@Override
	public String getTestbedName() {
		return EmbeddingConstants.TESTBED_NAME;
	}
	
	@Override
	public String getAlgorithmName() {
		return EmbeddingConstants.GNM_ALGORITHM_NAME;
	}
	
	@Override
	public String getTestbedTopLeveAuthority(){
		return EmbeddingConstants.TOP_LEVEL_AUTHORITY;
	};

	/*************************************************************
	************************ Testing methods *********************
	*************************************************************/
	@Override
	public String whoAreYou (String question) {
		logService.log(LogService.LOG_INFO, "Message: "+question);
		return "I am the Greedy Node Mapping algorithm in "+getTestbedName();
	}
	
	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/
	
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
