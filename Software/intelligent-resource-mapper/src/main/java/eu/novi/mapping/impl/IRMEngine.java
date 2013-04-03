/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.impl;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.im.core.Group;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.exceptions.MappingException;
import java.util.concurrent.ScheduledExecutorService;
/* Remote interface imports*/
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRFailedMess;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveResponse;
import eu.novi.mapping.IRMInterface;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.utils.GraphOperations;
import eu.novi.mapping.utils.IMOperations;
import eu.novi.mapping.utils.IRMConstants;
import eu.novi.mapping.utils.IRMOperations;

/* For user feedback */
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.util.IMCopy;
/* Logging imports */
import org.osgi.service.log.LogService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * IRM solver will select the appropriate strategy for dealing with 
 * the various sub-problems of the Inter-domain VNE. On solving the VN 
 * request splitting problem the VN request along with the retrieved 
 * ResourceSet are passed as arguments to the appropriate algorithm. 
 * The algorithm should return the sub-graphs to be allocated at every 
 * domain (partialVirtualTopology) along with the corresponding partial 
 * resource set (partialResourceSet). On solving the intra-domain VNE 
 * problem, the VN partial topology request along with the partial 
 * resource set are passed as arguments to the appropriate embedding 
 * algorithm. The mapped resources list (MResourceSet ) is returned 
 * (resources to be scheduled for reservation).
 */
public class IRMEngine implements IRMInterface, RemoteIRM {

	/** Testbed name on which the service is running e.g. PlanetLab, FEDERICA. */
	protected String testbed;
	
	/** The splitting algorithms available for the IRM Engine. */
	protected List<SplittingAlgorithm> splittingAlgorithms
			= new ArrayList<SplittingAlgorithm>();
	
	/** The embedding algorithms available for the IRM Engine. */
	protected List<EmbeddingAlgorithmInterface> embeddingAlgorithms
			= new ArrayList<EmbeddingAlgorithmInterface>();
    
    /** The Interface for calling RIS service. */
    protected IRMCalls irmCallsFromRIS;
	
	/** The scheduler service. */
    protected ScheduledExecutorService scheduler;
    
    /** The remote IRMs, needed for talking between them in a distributed way. */
    protected List<RemoteIRM> irms = new ArrayList<RemoteIRM>();
    
    /** The log service. */
    protected LogService logService;
	
    /** Main interface to report user feedback, initialized in blueprint based on service provided by novi-api. */
    protected ReportEvent userFeedback;
   
	
    /** Local logging */
	//private static final transient Logger log = LoggerFactory.getLogger(IRMEngine.class);
	
    /**
     * @see eu.novi.mapping.IRMInterface#processGroups(Collection, String, NOVIUserImpl)
     */
    @Override
    public void processGroups(Collection<Group> groups, String sessionID, NOVIUserImpl noviUser){
    	
		userFeedback.instantInfo(sessionID, "IRM (ProcessGroups)", 
				"IRM callback which is invoked because there is incoming request in NOVI-API", 
				IRMConstants.IRM_FEEDBACK_URL);
				
		Collection<GroupImpl>  groupsImpl  = new HashSet<GroupImpl>();

		for (Group group : groups) {
			groupsImpl.add((GroupImpl)group);
		}
		
		logService.log(LogService.LOG_INFO,"Getting groups/topologies from API, size of group " + groupsImpl.size());

		createSlice(sessionID, groupsImpl, noviUser);
    }
    
    /**
     * PRE: It is assumed that the input is well formed.
	 * PRE: It is assumed that resource names are unique.
     * 
	 * @see eu.novi.mapping.IRMInterface#createSlice(Collection, NOVIUserImpl)
	 */
    public int createSlice(String sessionID, Collection<GroupImpl> groups, NOVIUserImpl noviUser)  {
		
    	if (!checkIRMServices(sessionID)) {
    		return -1;
    	}

		int sliceID = -1;
		// Get slice ID from NOVI-API (sessionID)
		try {
			sliceID = IRMOperations.getUUID(sessionID);
		} catch (NoSuchAlgorithmException e) {
			userFeedback.errorEvent(sessionID, 
					"IRM Slice ID error", 
					"Error generating sliceID", 
					IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		}
		
		logService.log(LogService.LOG_INFO,"Resulting slice ID: "+sliceID);
		logService.log(LogService.LOG_INFO,"Number of Groups in the request: "+groups.size());
		
		Set<Platform> platforms = null;
		Topology virtualTopology = null;
		
		// getting virtual request
		virtualTopology = IMOperations.getVirtualRequest(groups);

		logService.log(LogService.LOG_INFO,"Getting virtual Topology from the groups, see what it contains " + virtualTopology.getContains());
		
		userFeedback.instantInfo(sessionID, 
				"IRM (Creating Slice) Checking virtual topology...", 
				"Checking virtual topology...", 
				IRMConstants.IRM_FEEDBACK_URL);
		if (IMOperations.isSetEmpty(virtualTopology.getContains())) {
			 userFeedback.errorEvent(sessionID, 
					 "IRM No resources", 
					 "There are no resources in the virtual request "
					 +virtualTopology.toString(), 
					 IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		} else {
			IMOperations.analyzeGroup(virtualTopology, logService);
		}
	
		// getting platforms
		platforms = IMOperations.getPlatforms(groups, virtualTopology);
		
		// Delegating the operation to CreateEngine
		Topology fullBoundedTopology;
		CreateEngine ce = new CreateEngine(irmCallsFromRIS, irms, embeddingAlgorithms, 
				userFeedback, sessionID, logService, noviUser, getTestbed(), scheduler);
		try {
			fullBoundedTopology = ce.createSlice(sessionID, virtualTopology, platforms);
		} catch (MappingException e) {
			logService.log(LogService.LOG_ERROR, e.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error in Create Slice", 
					e.getMessage(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		}
			
		logService.log(LogService.LOG_INFO,"Analyzing full bounded topology for slice with id: " + sliceID);
		IMOperations.analyzeGroup(fullBoundedTopology, logService);
		
		return reserveSliceToRIS(sessionID, fullBoundedTopology,sliceID,noviUser);
		
	}

	/**
	 * PRE: currentSlice is well formed
	 * PRE: failing resources are bounded
	 * 
	 * @see eu.novi.mapping.IRMInterface#updateSlice(String, Collection)
	 */
	@Override
	public Collection<String> updateSlice(String sessionID, String currentSliceID, Collection<String> failingResourcesIDs) {
		
		if (!checkIRMServices(sessionID)) {
    		return new HashSet<String>();
    	}
		
		userFeedback.instantInfo(sessionID, 
				"IRM (Update Slice)", 
				"Getting slice and failing resources from RIS...", 
				IRMConstants.IRM_FEEDBACK_URL);
					
		logService.log(LogService.LOG_INFO, "Getting slice from sliceID...");
		
		Reservation currentSlice = irmCallsFromRIS.getSlice(currentSliceID);
		IMOperations.analyzeGroup(currentSlice, logService);
		IMCopy imc = new IMCopy();
		Reservation sliceCopy = (Reservation) imc.copy(currentSlice, -1);
		
		
		logService.log(LogService.LOG_INFO, "Getting failing physical resources from their IDs...");
		Group failingTopology = new GroupImpl("failingTopology");
		Set<Resource> failingResources = new HashSet<Resource>();
		Set<String> failingPhysicalResourcesIDs = new HashSet<String>();
		Iterator<String> failingResourcesIt = failingResourcesIDs.iterator();
		while (failingResourcesIt.hasNext()) {
			String resourceID = failingResourcesIt.next();
			logService.log(LogService.LOG_INFO, "Getting physical resource ID for failing resource "+resourceID+"...");
			String physicalResourceID = IRMOperations.getPhysicalResourceID(currentSlice,resourceID);
			if (physicalResourceID == null) {
				logService.log(LogService.LOG_ERROR, "IRM Error obtaining physical resource ID");
				userFeedback.errorEvent(sessionID, 
						"IRM Error obtaining physical resource ID", 
						"Error: The failing virtual resource does not belong to the current slice...", 
						IRMConstants.IRM_FEEDBACK_URL);
				new HashSet<String>();
			}
			failingPhysicalResourcesIDs.add(physicalResourceID);
			logService.log(LogService.LOG_INFO, "Physical resource ID: "+physicalResourceID);
			logService.log(LogService.LOG_INFO, "Calling RIS...");
			Resource failingResource = irmCallsFromRIS.getResource(physicalResourceID);
			if (failingResource==null) {
				logService.log(LogService.LOG_ERROR, "IRM Error obtaining resources from RIS");
				userFeedback.errorEvent(sessionID, 
						"IRM Error obtaining resources from RIS", 
						"Error: error obtaining resource from RIS...", 
						IRMConstants.IRM_FEEDBACK_URL);
				return new HashSet<String>();
			}
			logService.log(LogService.LOG_INFO, "ID of the failing resource: "
					+failingResource.toString());
			failingResources.add(failingResource);
		}
		failingTopology.setContains(failingResources);
	
		
		// Delegating the operation to FailureUpdateEngine
		Collection<String> result=new HashSet<String>();
		logService.log(LogService.LOG_INFO, "IRM Delegating the operation to FailureUpdateEngine YK1");
		FailureUpdateEngine fue = new FailureUpdateEngine(irmCallsFromRIS, irms, embeddingAlgorithms, 
				userFeedback, sessionID, logService, getTestbed(), scheduler);
		logService.log(LogService.LOG_INFO, "IRM Delegating the operation to FailureUpdateEngine YK2");
		try {
			logService.log(LogService.LOG_INFO, "IRM Delegating the operation to FailureUpdateEngine YK3");
			result = fue.updateSlice(sessionID, sliceCopy, failingResources, failingPhysicalResourcesIDs);
			//result = fue.updateSlice(sessionID, currentSlice, failingResources, failingPhysicalResourcesIDs);
			logService.log(LogService.LOG_INFO, "IRM Delegating the operation to FailureUpdateEngine YK4");
		} catch (MappingException e) {
			logService.log(LogService.LOG_ERROR, e.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error in Update Slice", 
					e.getMessage(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return new HashSet<String>();
		}
		
		logService.log(LogService.LOG_INFO,"New physical resources: "+result);

		logService.log(LogService.LOG_INFO,"Analyzing reallocated slice...");
		IMOperations.analyzeGroup(currentSlice, logService);
		
		// Obtaining slice URN
		String sliceURN = IMOperations.getId(currentSlice.toString());
		// Obtaining slice ID from slice URN.
		Integer sliceID;
		try {
			sliceID = IMOperations.getSliceID(sliceURN);
		} catch (NumberFormatException e) {
			logService.log(LogService.LOG_ERROR,"Slice ID error",e);
			userFeedback.errorEvent(sessionID, 
					"IRM Error in Update Slice", 
					"Slice ID error",
					IRMConstants.IRM_FEEDBACK_URL);
			return new HashSet<String>();
		}
		
		/** Translating Reservation to Topology **/
		Topology topologySlice = new TopologyImpl(sliceURN);
		topologySlice.setContains(currentSlice.getContains());
		
		if (updateSliceToRIS(sessionID, topologySlice, sliceID)) {
			return result;
		} else {
			return new HashSet<String>();
		}

	}

	/**
	 * PRE: currentSlice is well formed
	 * PRE: updateSliceGroups is well formed
	 * 
	 * @see eu.novi.mapping.IRMInterface#updateSlice(String, Collection, NOVIUserImpl)
	 */
	@Override
	public int updateSlice(String sessionID, String sliceID,
			Collection<GroupImpl> updatedSliceGroups, NOVIUserImpl noviUser) {
		
		if (!checkIRMServices(sessionID)) {
    		return -1;
    	}
		
		// Getting Slice from RIS
		Reservation currentSlice = irmCallsFromRIS.getSlice(sliceID);
		IMOperations.analyzeGroup(currentSlice, logService);
		
		Set<Platform> platforms = null;
		Topology virtualTopology = null;
		
		// getting virtual request
		virtualTopology = IMOperations.getVirtualRequest(updatedSliceGroups);
		
		userFeedback.instantInfo(sessionID, 
				"IRM (Update Slice) Checking virtual topology...", 
				"Checking virtual topology...", 
				IRMConstants.IRM_FEEDBACK_URL);
		if (IMOperations.isSetEmpty(virtualTopology.getContains())) {
			 userFeedback.errorEvent(sessionID, 
					 "IRM No resources", 
					 "There are no resources in the virtual request", 
					 IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		} else {
			IMOperations.analyzeGroup(virtualTopology, logService);
		}
		
		// getting platforms
		platforms = IMOperations.getPlatforms(updatedSliceGroups, virtualTopology);
		
		// Delegating the operation to UpdateEngine
		Topology fullBoundedTopology;
		UpdateEngine ue = new UpdateEngine(irmCallsFromRIS, irms, embeddingAlgorithms,
				userFeedback, sessionID, logService, noviUser, getTestbed(), scheduler);
		try {
			fullBoundedTopology = ue.updateSlice(sessionID, currentSlice, virtualTopology, platforms);
		} catch (MappingException e) {
			logService.log(LogService.LOG_ERROR, e.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error in Create Slice", 
					e.getMessage(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		}

		logService.log(LogService.LOG_INFO,"Analyzing full bounded topology for slice with id: " + sliceID);
		IMOperations.analyzeGroup(fullBoundedTopology, logService);
		
		// TODO reservation
		
		return 0;
	}
	
	/**
	 * Calls RIS in order to reserve the bounded slice for the create slice process.
	 *
	 * @param fullBoundedTopology the full bounded topology
	 * @param sliceID the slice ID
	 * @param noviUser the NOVI user
	 * @return the slice ID id successfull; -1 otherwise
	 */
	private int reserveSliceToRIS(String sessionID, Topology fullBoundedTopology, int sliceID,
			NOVIUserImpl noviUser) {
		
		logService.log(LogService.LOG_INFO,"Calling RIS  -  reserving resources...");
		userFeedback.instantInfo(sessionID, 
				"IRM (Calling RIS)", 
				"Calling RIS - reserving resources...", 
				IRMConstants.IRM_FEEDBACK_URL);
		IMCopy copier = new IMCopy();
		Topology returnSlice = (Topology) copier.copy(fullBoundedTopology, -1);
	
		ReserveResponse resp = irmCallsFromRIS.reserveSlice(sessionID, fullBoundedTopology, sliceID, noviUser);
		if (resp.hasError()) {

			logService.log(LogService.LOG_ERROR,"Error reserving slice: "+resp.getErrorMessage());
			logService.log(LogService.LOG_ERROR,"Error details: "+resp.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error reserving slice", 
					"Error reserving slice: "+resp.getErrorMessage(),
					IRMConstants.IRM_FEEDBACK_URL);
			return -1;
		}
		String msg = "With ID: " + resp.getSliceID();
		String stringMapping = IMOperations.mappingToString(returnSlice,logService);
		logService.log(LogService.LOG_INFO,stringMapping);
		logService.log(LogService.LOG_INFO,"Slice successfully created" );			
		userFeedback.instantInfo(sessionID, 
		"IRM Slice created", msg+". and mapping  "+stringMapping, 
		IRMConstants.IRM_FEEDBACK_URL);
					
		return sliceID;	
	}
	
	/**
	 * Calls RIS in order to update the reservation if the given slice.
	 *
	 * @param topologySlice the updated slice
	 * @param sliceID the slice ID
	 * @return true if successfully updated in RIS; false otherwise
	 */
	private boolean updateSliceToRIS(String sessionID, Topology topologySlice, Integer sliceID) {
		logService.log(LogService.LOG_INFO,"Calling RIS - reserving resources...");
		userFeedback.instantInfo(sessionID, 
				"IRM Calling RIS", 
				"Calling RIS - reserving resources...", 
				IRMConstants.IRM_FEEDBACK_URL);
		ReserveResponse reservResp = irmCallsFromRIS.updateSlice(sessionID,
				topologySlice, sliceID);
		if (reservResp.hasError()) {
			logService.log(LogService.LOG_ERROR,"Error reserving slice: "+reservResp.getErrorMessage());
			logService.log(LogService.LOG_ERROR,"Error details: "+reservResp.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error reserving slice", 
					"Error reserving slice: "+reservResp.getErrorMessage(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return false;
		}	

		logService.log(LogService.LOG_INFO,"Slice successfully updated");
		userFeedback.instantInfo(sessionID, 
				"IRM Slice updated", 
				"Slice successfully updated", 
				IRMConstants.IRM_FEEDBACK_URL);
		
		return true;
	}
	
	/**
	 * @see eu.novi.mapping.IRMInterface#mapOnTestbed(GroupImpl, NOVIUser)
	 */
	@Override
	public GroupImpl mapOnTestbed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {

		logService.log(LogService.LOG_INFO,"Number of embedding algorithms: "+embeddingAlgorithms.size());
		logService.log(LogService.LOG_INFO,"Selecting appropriate embedding algorithm...");
		
		EmbeddingAlgorithmInterface algorithm = selectEmbeddingAlgorithm();
		
		if (algorithm==null) {
			logService.log(LogService.LOG_ERROR, "There are no embedding algorithms available");
			userFeedback.errorEvent(sessionID, 
					"IRM Error embedding resources", 
					"There are no embedding algorithms available", 
					IRMConstants.IRM_FEEDBACK_URL);
			return null;
		}
		
		logService.log(LogService.LOG_INFO,"Embedding with: "+algorithm.getAlgorithmName());
		List<Map<ResourceImpl, ResourceImpl>> mappings = null;
		if (partialTopology==null) {
			return null;
		}
		mappings = algorithm.embed(sessionID, partialTopology, noviUser);
		
		if (mappings==null) {
			return null;
		}
		LinkedHashMap<ResourceImpl, ResourceImpl> nodeMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>();
		LinkedHashMap<ResourceImpl, ResourceImpl> linkMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>();
		if (mappings.size()==1) {
			nodeMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(0);
		} else if (mappings.size()==2) {
			nodeMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(0);
			linkMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(1);
		} else {return null;}

		return (GroupImpl)GraphOperations.translateToTopology(nodeMapping, 
				linkMapping, "partialBoundTopology");

	}
	/**
	 * @see eu.novi.mapping.IRMInterface#mapOnTestbed(GroupImpl, NOVIUser)
	 *//*
	@Override
	public GroupImpl mapOnTestbed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser) {
		
		userFeedback.instantInfo(sessionID, 
				"IRM Calling RIS...", 
				"Calling RIS - find resources...", 
				IRMConstants.IRM_FEEDBACK_URL);
		 
		FRResponse resp = irmCallsFromRIS.findResources(sessionID, (Topology) partialTopology, noviUser);
		if (resp.hasError()) {
			userFeedback.errorEvent(sessionID, 
					"IRM Error in find resources response", 
					"There was error finding resources", 
					IRMConstants.IRM_FEEDBACK_URL);
			Iterator<Entry<Resource, FRFailedMess>> errorIt = 
					resp.getFailedResources().entrySet().iterator();
			while (errorIt.hasNext()) {
				Entry<Resource, FRFailedMess> errorEntry = errorIt.next();
				logService.log(LogService.LOG_ERROR, 
						"Resource "+errorEntry.getKey().toString()+" with error: "+errorEntry.getValue());
				userFeedback.errorEvent(sessionID, 
						"IRM Error in find resources response", 
						"Resource "+errorEntry.getKey()+" with error: "+
							errorEntry.getValue(), 
						IRMConstants.IRM_FEEDBACK_URL);
			}
			if (!resp.getUserFeedback().isEmpty()) {
				userFeedback.errorEvent(sessionID, 
						"RIS detailed information/suggestions:", 
						resp.getUserFeedback(), 
						IRMConstants.IRM_FEEDBACK_URL);
			}
			return null;
		}
		Group availableResources = resp.getTopology();
		logService.log(LogService.LOG_INFO, 
				"Analyzing Topology returned by RIS");
		IMOperations.analyzeGroup(availableResources, logService);
		
		logService.log(LogService.LOG_INFO,"Checking available resources...");
		if (IMOperations.isSetEmpty(availableResources.getContains())) {
			userFeedback.errorEvent(sessionID, 
					"IRM Error getting available resources", 
					"Error There are no available resources" +
						" for the request in "+availableResources.toString(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return null;
		}
		
		Set<Resource> contained = availableResources.getContains();
		logService.log(LogService.LOG_DEBUG, 
				"Num of available resources: "+contained.size());
		
		logService.log(LogService.LOG_INFO,"Translating topology for using the embedding alorithm...");
		SparseMultigraph<NodeImpl, LinkImpl> virtualRequest = 
			GraphOperations.translateIMToGraph(partialTopology,true);
		GraphOperations.analyzeGraph(virtualRequest, logService);
		SparseMultigraph<NodeImpl, LinkImpl> resources = 
			GraphOperations.translateIMToGraph(availableResources,true);
		GraphOperations.analyzeGraph(resources, logService);
		
		userFeedback.instantInfo(sessionID, 
				"IRM Embedding...", 
				"Calling embedding algorithm..." +
				" for the request in "+availableResources.toString(), 
				IRMConstants.IRM_FEEDBACK_URL);
		Group partialBoundTopology = embed(sessionID, virtualRequest, resources);
		
		return (GroupImpl)partialBoundTopology;
	
	}*/
	
	/**
	 * @see eu.novi.mapping.IRMInterface#updateOnTestbed(GroupImpl, GroupImpl, Collection)
	 */
	@Override
	public GroupImpl updateOnTestbed(String sessionID, GroupImpl slice, GroupImpl partialTopology, Collection<String> failingResources) {

		// Adding testbed virtual resources needed for RIS
		logService.log(LogService.LOG_INFO,"Adding testbed virtual resources needed for RIS...");
		Reservation partialReservation = new ReservationImpl(IMOperations.getId(slice.toString()));
		Set<Resource> ress = new HashSet<Resource>();
		for (Resource sliceRes : slice.getContains()) {
			if (!IMOperations.isSetEmpty(sliceRes.getIsContainedIn())) {
				for (Group group : sliceRes.getIsContainedIn()) {
					if (group.toString().contains(getTestbed())) {
						logService.log(LogService.LOG_INFO,"Adding resource "+sliceRes.toString()+"...");
						ress.add(sliceRes);
					}
				}
			}
		}
		partialReservation.setContains(ress);
		userFeedback.instantInfo(sessionID, 
				"IRM Calling RIS...", 
				"Calling RIS - find resources update...", 
				IRMConstants.IRM_FEEDBACK_URL);
		FRResponse resp = irmCallsFromRIS.findLocalResourcesUpdate(sessionID, partialReservation, (Set<String>) failingResources);
		if (resp.hasError()) {
			userFeedback.errorEvent(sessionID, 
					"IRM Error in find resources update response", 
					"There was error finding resources", 
					IRMConstants.IRM_FEEDBACK_URL);
			Iterator<Entry<Resource, FRFailedMess>> errorIt = 
					resp.getFailedResources().entrySet().iterator();
			while (errorIt.hasNext()) {
				Entry<Resource, FRFailedMess> errorEntry = errorIt.next();
				logService.log(LogService.LOG_ERROR, 
						"Resource "+errorEntry.getKey().toString()+" with error: "+errorEntry.getValue());
				userFeedback.errorEvent(sessionID, 
						"IRM Error in find resources update response", 
						"Resource "+errorEntry.getKey()+" with error: "+
							errorEntry.getValue(), 
						IRMConstants.IRM_FEEDBACK_URL);
			}
			if (!resp.getUserFeedback().isEmpty()) {
				userFeedback.errorEvent(sessionID, 
						"RIS detailed information/suggestions:", 
						resp.getUserFeedback(), 
						IRMConstants.IRM_FEEDBACK_URL);
			}
			return null;
		}

		Group availableResources = resp.getTopology();
		
		logService.log(LogService.LOG_INFO,"Checking available resources...");
		if (IMOperations.isSetEmpty(availableResources.getContains())) {
			userFeedback.errorEvent(sessionID, 
					"IRM Error getting available resources", 
					"Error There are no available resources" +
						" for the request in "+availableResources.toString(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return null;
		}
		
		logService.log(LogService.LOG_INFO,"Result topology with available resources: "+availableResources);
		Set<Resource> contained = availableResources.getContains();
		logService.log(LogService.LOG_DEBUG, 
				"Num of available resources "+contained.size());
		
		logService.log(LogService.LOG_INFO,"Translating topology for using the embedding alorithm...");
		SparseMultigraph<NodeImpl, LinkImpl> virtualRequest = 
			GraphOperations.translateIMToGraph(partialTopology,true);
		GraphOperations.analyzeGraph(virtualRequest, logService);
		SparseMultigraph<NodeImpl, LinkImpl> resources = 
			GraphOperations.translateIMToGraph(availableResources,true);
		GraphOperations.analyzeGraph(resources, logService);
		
		userFeedback.instantInfo(sessionID, 
				"IRM Embedding...", 
				"Calling embedding algorithm..." +
				" for the request in "+availableResources.toString(), 
				IRMConstants.IRM_FEEDBACK_URL);
		Group partialBoundTopology = embed(sessionID, virtualRequest, resources);
		
		return (GroupImpl)partialBoundTopology;
	}
	
	/**
	 * Select and use the appropriate local embedding algorithm
	 * @param problem to solve
	 * @param resources to allocate
	 * @param availableResources - available physical resources
	 * @return allocated topology. Null if there is an error
	 */
	private Topology embed(String sessionID, SparseMultigraph<NodeImpl, LinkImpl> problem, 
			SparseMultigraph<NodeImpl, LinkImpl> resources) {
		
		logService.log(LogService.LOG_INFO,"Number of embedding algorithms: "+embeddingAlgorithms.size());
		logService.log(LogService.LOG_INFO,"Selecting appropriate embedding algorithm...");
		
		EmbeddingAlgorithmInterface algorithm = selectEmbeddingAlgorithm(problem);
		
		
		if (algorithm==null) {
			logService.log(LogService.LOG_ERROR, "There are no embedding algorithms available");
			userFeedback.errorEvent(sessionID, 
					"IRM Error embedding resources", 
					"There are no embedding algorithms available", 
					IRMConstants.IRM_FEEDBACK_URL);
			return null;
		}
		
		logService.log(LogService.LOG_INFO,"Embedding with: "+algorithm.getAlgorithmName());
		List<Map<ResourceImpl, ResourceImpl>> mappings = null;
		mappings = algorithm.embed(sessionID,problem, resources);
		if (mappings==null) {
			return null;
		}
		LinkedHashMap<ResourceImpl, ResourceImpl> nodeMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>();
		LinkedHashMap<ResourceImpl, ResourceImpl> linkMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>();
		if (mappings.size()==1) {
			nodeMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(0);
		} else if (mappings.size()==2) {
			nodeMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(0);
			linkMapping = (LinkedHashMap<ResourceImpl, ResourceImpl>) mappings.get(1);
		} else {return null;}

		return (TopologyImpl)GraphOperations.translateToTopology(nodeMapping, 
				linkMapping, "partialBoundTopology");

	}

	/**
	 * Select the appropriate embedding algorithm in function of
	 * the testbed & the given problem
	 * @param problem
	 * @return the selected algorithm
	 */
	private EmbeddingAlgorithmInterface selectEmbeddingAlgorithm(
			SparseMultigraph<NodeImpl, LinkImpl> problem) {
		
		logService.log(LogService.LOG_INFO,"Selecting "+getTestbed()+" algorithm...");
		
		for (EmbeddingAlgorithmInterface algorithm : embeddingAlgorithms) {
			if (algorithm.getTestbedName().equals(this.getTestbed())) {
				return algorithm;
			}
		}
				
		logService.log(LogService.LOG_INFO,"unable to select embedding algorithm");
		return null;
	}
	
			/**
	 * Select the appropriate embedding algorithm in function of
	 * the testbed & the given problem
	 * @param problem
	 * @return the selected algorithm
	 */
	private EmbeddingAlgorithmInterface selectEmbeddingAlgorithm() {
		
		logService.log(LogService.LOG_INFO,"Selecting "+getTestbed()+" algorithm...");
		
		for (EmbeddingAlgorithmInterface algorithm : embeddingAlgorithms) {
			if (algorithm.getTestbedName().equals(this.getTestbed())) {
				System.out.println(algorithm.getAlgorithmName());
				return algorithm;
			}
		}
				
		logService.log(LogService.LOG_INFO,"unable to select embedding algorithm");
		return null;
	}
	
	/**
	 * Check IRM services. Return false if some o them are null.
	 *
	 * @return true, if all IRM Services were found successfully. False, otherwise
	 */
	private boolean checkIRMServices(String sessionID) {
		if (logService==null) {
			if (userFeedback!=null) {
				userFeedback.errorEvent(sessionID, 
						"IRM LogService is null",
						"LogService Service is null", 
						IRMConstants.IRM_FEEDBACK_URL);
			}
			return false;
		}
		if (userFeedback==null) {
			logService.log(LogService.LOG_ERROR, "ReportEvent (user feedback) Service is null");
			return false;
		}
		if (scheduler==null) {
			logService.log(LogService.LOG_ERROR, "Threads Scheduler Service is null");
			return false;
		}
		if (irmCallsFromRIS==null) {
			logService.log(LogService.LOG_ERROR, "IRMCalls (RIS communication) Service is null");
			userFeedback.errorEvent(sessionID, 
					"IRM IRMCalls is null",
					"IRMCalls (RIS communication) Service is null", 
					IRMConstants.IRM_FEEDBACK_URL);
			return false;
		}
		if (irms==null) {
			logService.log(LogService.LOG_ERROR, "Remote IRMs Service is null");
			userFeedback.errorEvent(sessionID, 
					"IRM Remote IRMs is null",
					"Remote IRMs Service is null", 
					IRMConstants.IRM_FEEDBACK_URL);
			return false;
		}
		if (embeddingAlgorithms.size()==0) {
			logService.log(LogService.LOG_ERROR, "Embedding Algorithms Service is empty");
			userFeedback.errorEvent(sessionID, 
					"IRM Embedding Algorithms Service is empty",
					"Embedding Algorithms Service is empty", 
					IRMConstants.IRM_FEEDBACK_URL);
			return false;
		}
		return true;
	}
	
	/**
	 * Select the appropriate splitting algorithm in function of
	 * the given virtual topology.
	 *
	 * @param virtualTopology the virtual topology
	 * @return splitting algorithm
	 */
//	private SplittingAlgorithm selectSplittingAlgorithm(Topology virtualTopology) {
//		return null;
//	}
	
	/*************************************************************
	******************* Cross Testbed Methods  *******************
	*************************************************************/
	
	/**
     * @see eu.novi.mapping.IRMInterface#mapOnTestbed(String, String, String)
     */
	@Override
	public String mapOnTestbed(String sessionID, String partial, String id, String stringNoviUser ) {
		// translating to IM
		
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		Topology partialTopology;
		partialTopology = repositoryUtil.getIMObjectFromString(partial, Topology.class, id);
		NOVIUserImpl noviUser = (NOVIUserImpl) repositoryUtil.getIMObjectFromString(stringNoviUser, NOVIUser.class);
		// calling mapOnTestbed
		GroupImpl partialBoundTopology = mapOnTestbed(sessionID, (GroupImpl) partialTopology, noviUser);
		
		if (partialBoundTopology==null) {return null;}
		
		// translating to string
		return repositoryUtil.exportIMObjectToString(partialBoundTopology);
	}
	
	/**
     * @see eu.novi.mapping.IRMInterface#updateOnTestbed(String, String, String, Collection)
     */
	@Override
	public String updateOnTestbed(String sessionID, String slice, String partial, String sliceID, String partialTopologyID,
			Collection<String> failingResourceIDs) {
		// translating to IM
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		Group sliceGroup = repositoryUtil.getIMObjectFromString(slice, Group.class, sliceID);
		Group partialGroup = repositoryUtil.getIMObjectFromString(partial, Group.class, partialTopologyID);
//		Collection<String> failingResources = new HashSet<String>();
//		for (String failingRes : failingResourceIDs) {
//			Resource failingResource = repositoryUtil.getIMObjectFromString(slice, Resource.class, failingRes);
//			failingResources.add(failingResource.toString());
//		}
		// calling UpdateOnTestbed
		GroupImpl partialBoundTopology = updateOnTestbed(sessionID, (GroupImpl)sliceGroup, 
				(GroupImpl)partialGroup, failingResourceIDs);
		
		if (partialBoundTopology==null) {return null;}
		
		// translating to string
		return repositoryUtil.exportIMObjectToString(partialBoundTopology);
	}
	
	/*************************************************************
	************************ Testing methods *********************
	*************************************************************/
		
	/**
	 * @see eu.novi.mapping.IRMInterface#mapOnTestbed(TopologyImpl, PlatformImpl)
	 */
	@Override
	public String mapOnTestbed(TopologyImpl req, PlatformImpl subs) {
		logService.log(LogService.LOG_INFO, "Dummy mapOnTestbed reached on "+getTestbed());
		logService.log(LogService.LOG_INFO, "Request: "+req.toString());
		logService.log(LogService.LOG_INFO, "Substrate: "+subs.toString());
		for (EmbeddingAlgorithmInterface algo : embeddingAlgorithms) {
			logService.log(LogService.LOG_INFO, 
					"Calling local algorithm: "+algo.getTestbedName());
			String result = algo.whoAreYou("Who are you?");
			logService.log(LogService.LOG_INFO, 
					"Response of the algorithm: "+result);
		}
		return "Hi";
	}

	@Override	
	public  String getTestbedTopLeveAuthority() {
		for (EmbeddingAlgorithmInterface algo : this.embeddingAlgorithms) {
			return algo.getTestbedTopLeveAuthority();
		}
	return null;
	}

	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/
	
	public List<SplittingAlgorithm> getSplittingAlgorithms() {
		return splittingAlgorithms;
	}

	public void setSplittingAlgorithms(List<SplittingAlgorithm> splittingAlgorithms) {
		this.splittingAlgorithms = splittingAlgorithms;
	}

	public List<EmbeddingAlgorithmInterface> getEmbeddingAlgorithms() {
		return embeddingAlgorithms;
	}

//	public void setEmbeddingAlgorithms(List<EmbeddingAlgorithmInterface> embeddingAlgorithms) {
//		this.embeddingAlgorithms = embeddingAlgorithms;
//	}
	
	public void addEmbedding(EmbeddingAlgorithmInterface embedding) {
		this.embeddingAlgorithms.add(embedding);
	}
	
	public void removeEmbedding(EmbeddingAlgorithmInterface embedding) {
		this.embeddingAlgorithms.remove(embedding);
	}
	
	public void addRemoteIRM(RemoteIRM remote) {
		this.irms.add(remote);
	}
	
	public void removeRemoteIRM(RemoteIRM remote) {
		this.irms.remove(remote);
	}
	
	// RIS
	public IRMCalls getIrmCallsFromRIS() {
		return irmCallsFromRIS;
	}

	public void setIrmCallsFromRIS(IRMCalls irmCallsFromRIS) {
		this.irmCallsFromRIS = irmCallsFromRIS;
	}

	// IRM's
	public List<RemoteIRM> getIrms() {
		return irms;
	}
	
	// scheduler's
	public ScheduledExecutorService  getScheduler(){
		return scheduler;
	}

	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}

	// Testbed
	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}
	
	/**
     * Getters and setters for seting up report event
     */
    public ReportEvent getReportUserFeedback() {
		return userFeedback;
    }

    public void setReportUserFeedback(ReportEvent reportUserFeedback) {
		this.userFeedback = reportUserFeedback;
    }


	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void addEmbedding(
			List<EmbeddingAlgorithmInterface> plcEmbeddingAlgorithms) {
		this.embeddingAlgorithms.addAll(plcEmbeddingAlgorithms);
	}

	public void addIrms(ArrayList<RemoteIRM> irms) {
		this.irms.addAll(irms);
	}

	public void clearIrms() {
		this.irms.clear();
	}
    
}
