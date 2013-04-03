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

import java.util.concurrent.Future;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.ArrayList;
import org.osgi.service.log.LogService;
import java.util.concurrent.ScheduledExecutorService;


import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.Group;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.Node;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.exceptions.MappingException;
import eu.novi.mapping.utils.GraphOperations;
import eu.novi.mapping.utils.IMOperations;
import eu.novi.mapping.utils.IRMConstants;
import eu.novi.mapping.utils.IRMOperations;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class responsible for handling slice creation requests.
 * @see IRMEngine
 */
public class CreateEngine extends IRMEngine{
	
    /** The NOVI user. */
    private NOVIUserImpl noviUser;
    
    /** The task list for keeping control of embedding threads. */
    protected List<Future<String>> taskList = new ArrayList<Future<String>>();
    
    /** Local logging. */
	private static final transient Logger log = LoggerFactory.getLogger(CreateEngine.class);
    
    /**
     * Constructor of CreateEngine. It sets all the attributes of the superclass (IRMEngine)
     */
	public CreateEngine(IRMCalls irmCallsFromRIS, List<RemoteIRM> irms, 
			List<EmbeddingAlgorithmInterface> embeddingAlgorithms, 
			ReportEvent userFeedback, String sessionID, 
			LogService logService, NOVIUserImpl noviUser, String testbed, 
			ScheduledExecutorService scheduler) {
		this.irmCallsFromRIS = irmCallsFromRIS;
		this.irms = irms;
		this.userFeedback = userFeedback;
		this.logService = logService;
		this.testbed = testbed;
		this.embeddingAlgorithms = embeddingAlgorithms;
		this.noviUser = noviUser;
		this.scheduler = scheduler;
	}

	/**
	 * Creates the slice.
	 *
	 * @param virtualTopology the virtual topology
	 * @param platforms the platforms
	 * @return the topology
	 * @throws MappingException the mapping exception
	 */
	public Topology createSlice(String sessionID, Topology virtualTopology, Set<Platform> platforms) throws MappingException {
		
		userFeedback.instantInfo(sessionID, 
				"IRM Checking type of virtual request...", 
				"Checking type of virtual request: bound/unbound/partial...", 
				IRMConstants.IRM_FEEDBACK_URL);
				
		Topology fullBoundedTopology = null;
		Topology[] splitResources = IRMOperations.checkBoundUnboundRequest(virtualTopology);
		Topology boundedTopology = splitResources[0];
		Topology unboundedTopology = splitResources[1];
		
		// Showing type of request
		if (!IMOperations.isSetEmpty(boundedTopology.getContains()) 
				&& !IMOperations.isSetEmpty(unboundedTopology.getContains())) {
			logService.log(LogService.LOG_INFO,"Type of request: partial bounded");
			userFeedback.instantInfo(sessionID, 
					"IRM Type Request: partially bounded", 
					"IRM Type Request: partially bounded", 
					IRMConstants.IRM_FEEDBACK_URL);
			IMOperations.analyzeGroup(boundedTopology, logService);
			IMOperations.analyzeGroup(unboundedTopology, logService);
		}
		else if (!IMOperations.isSetEmpty(boundedTopology.getContains())) {
			logService.log(LogService.LOG_INFO,"Type of request: bounded");
			userFeedback.instantInfo(sessionID, 
					"IRM Type Request: bounded", 
					"IRM Type Request: bounded", 
					IRMConstants.IRM_FEEDBACK_URL);
			IMOperations.analyzeGroup(boundedTopology, logService);
		}
		else if (!IMOperations.isSetEmpty(unboundedTopology.getContains())) {
			logService.log(LogService.LOG_INFO,"Type of request: unbounded");
			userFeedback.instantInfo(sessionID, 
					"IRM Type Request: Unbounded", 
					"IRM Type Request: Unbounded", 
					IRMConstants.IRM_FEEDBACK_URL);
			IMOperations.analyzeGroup(unboundedTopology, logService);
						
		}
		else {
			throw new MappingException("There are no valid resources in the virtual request. " +
					"Check the correctness of the requested topology");
		}	

		// Checking if the bounded physical resources exist...
		if (!IMOperations.isSetEmpty(boundedTopology.getContains())) {
			logService.log(LogService.LOG_INFO,"Checking if bounded physical resources exist...");
			userFeedback.instantInfo(sessionID, 
					"IRM Checking bounded physical resources...", 
					"Checking bounded resources...", 
					IRMConstants.IRM_FEEDBACK_URL);
			Set<Resource> failingResources = 
				IRMOperations.checkPhysicalResources(boundedTopology, irmCallsFromRIS, noviUser);
				
			if (!IMOperations.isSetEmpty(failingResources)) {
				throw new MappingException("Physical resource(s) does not exist: "
						+failingResources.toString());
			}
		}

		if (IMOperations.isSetEmpty(unboundedTopology.getContains())){
			// There are no unbounded resources
			fullBoundedTopology = virtualTopology;
		}
		// there are unbound physical resources. Let's do our work
		else {
			
			userFeedback.instantInfo(sessionID, 
					"IRM Checking platforms...", 
					"Checking platform bound resources...", 
					IRMConstants.IRM_FEEDBACK_URL);
			
			/*
			 * Check partially unbound resources (those that only the Platform is
			 * specified). Put platform unbound resources in platformUnboundTopology
			 * for sending them to splitting algorithm.
			 */
			Topology platformUnboundTopology = new TopologyImpl("platformUnboundResources");
			Set<Topology> partialTopologies = 
				checkPlatformBoundResources(unboundedTopology,platforms,platformUnboundTopology);
			
			if (!partialTopologies.isEmpty()) {
				userFeedback.instantInfo(sessionID, 
						"IRM Checking platforms...", 
						"There are platform bounded resources...", 
						IRMConstants.IRM_FEEDBACK_URL);
				for (Topology partialTopology : partialTopologies) {
					IMOperations.analyzeGroup(partialTopology, logService);
				}
				
			}
			
			if (!IMOperations.isSetEmpty(platformUnboundTopology.getContains())) {
				// Call Split Resources for NO PLATFORM bound resources
				/** SPLIT RESOURCES **/
				userFeedback.instantInfo(sessionID, 
						"IRM (Create Slice)", 
						"Splitting resources...", 
						IRMConstants.IRM_FEEDBACK_URL);
				logService.log(LogService.LOG_DEBUG,
						"Splitting resources...");
				
				Vector<FPartCostTestbedResponseImpl>  splittingCosts = irmCallsFromRIS.findPartitioningCost(sessionID, platformUnboundTopology);
				
				logService.log(LogService.LOG_DEBUG,
						"Splitting costs received...");
				
				
				SplittingAlgorithm splitAlgo =  new SplittingAlgorithm();
				SparseMultigraph<NodeImpl, LinkImpl> virtualRequestToSplit = GraphOperations.translateIMToGraph(virtualTopology,true);
				PartitionedRequest subGraphs = new PartitionedRequest();
				try {
					// subGraphs = splitAlgo.split(virtualRequestToSplit,
							// splittingCosts,irms);
					subGraphs = splitAlgo.split(virtualRequestToSplit,
							boundedTopology.getContains(),
							unboundedTopology.getContains(),
							partialTopologies,
							splittingCosts,irms);
							
				 logService.log(LogService.LOG_DEBUG,
						"Splitting finished...");
				} catch(MappingException e){
					logService.log(LogService.LOG_ERROR,
							"Error splitting resources...");
					
					if (e.toString().contains("are not available on the federation")){
						userFeedback.instantInfo(sessionID, 
								"IRM Splitting Request: ",
								e.toString(), 
								IRMConstants.IRM_FEEDBACK_URL);
					}
					throw e;
				}
	
				Set<Platform> partialSplitPlatforms  =  subGraphs.getPartialPlatforms();
				
				// Merge platform bound resources with split results...
				mergeSplittingResults(partialTopologies, partialSplitPlatforms);
				
				/** END SPLIT RESOURCES **/
			}

			/** Detecting and marking federable nodes **/
			IRMOperations.checkAndMarkNodesToFederate(virtualTopology);
			/** End detecting and marking federable nodes **/		
			
			Set<Topology> partialBoundTopologies = new HashSet<Topology>();
			int taskID = 0;
			for (Topology partialTopology : partialTopologies) {
			
				/****************************************************************
				 * *********************** LOCAL ********************************
				 ****************************************************************/
				
				for (RemoteIRM irm : irms) {
				
					if (partialTopology.toString().contains(irm.getTestbed())) {
						log.debug("Mapping on  testbed, " + irm.getTestbed() +"  ready to make the task");
						logService.log(LogService.LOG_INFO,"Mapping on testbed "+irm.getTestbed()+"...");
						userFeedback.instantInfo(sessionID, 
								"IRM Mapping on testbed "+irm.getTestbed()+"...", 
								"Number of resources to map: "
								+partialTopology.getContains().size(), 
								IRMConstants.IRM_FEEDBACK_URL);
						
						/** Calling String mapOnTestbed **/
						// translating to string
						if (IMOperations.isSetEmpty(partialTopology.getContains())) {
							throw new MappingException("Partial Topology sent to "+irm.getTestbed()+" is empty");
						}
						IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
						String partial = repositoryUtil.exportIMObjectToString(partialTopology);
						String stringNoviUser = repositoryUtil.exportIMObjectToString(noviUser);
						taskID++;
						MapOnTestbedCallable mot = new MapOnTestbedCallable(sessionID, partial, partialTopology.toString(),
								stringNoviUser,irm,taskID);
						Future<String> task = scheduler.submit(mot);
						log.debug("Task with ID "+taskID+" submitted");
						taskList.add(task);
						break;
					}
				}
				
				/****************************************************************
				 * ********************* END LOCAL ******************************
				 ****************************************************************/
				
			}
	
			log.debug("Obtaining results from tasks. Number of tasks: "+taskList.size());
			for (Future<String>  futTask : taskList) {
				String partialBound = "";
				try {
					log.debug("Getting task results");
					partialBound = futTask.get();
					log.debug("Results obtained");
				} catch (Exception e) {
					log.error("Error: IRM Mapping on local testbed "+testbed+"... Thread execution error");
					log.error("", e.getCause());
					logService.log(LogService.LOG_ERROR, e.getMessage());
					userFeedback.errorEvent(sessionID, 
							"Error: IRM Mapping on local testbed "+testbed+"...", 
							e.getMessage(), 
							IRMConstants.IRM_FEEDBACK_URL);
					throw new MappingException("Error mapping on local testbed "+testbed+"... Thread execution error");
				}
				IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
				Topology partialBoundTopology = repositoryUtil.getIMObjectFromString(partialBound, 
									Topology.class, IRMConstants.URI_PREFIX+"partialBoundTopology");
				
				if (partialBoundTopology!=null){
					log.debug("topology not null");
					userFeedback.instantInfo(sessionID, 
							"IRM Mapping on local testbed complete", 
							"Number of resources mapped: "
								+partialBoundTopology.getContains().size(), 
							IRMConstants.IRM_FEEDBACK_URL);
					logService.log(LogService.LOG_INFO,"Analyzing partial bound results...");
					IMOperations.analyzeGroup(partialBoundTopology, logService);
				
					Iterator<Resource> vResourceIt = partialBoundTopology.getContains().iterator();
					while (vResourceIt.hasNext()) {
						Resource resource = vResourceIt.next();
						if (resource instanceof Node) {
							for (Group g : resource.getIsContainedIn()) {
								log.debug("Node: "+resource.toString()+" contained in "+g);
							}
						}
					}
				} 
				else {
					log.debug("Error: Error embedding resources");
					throw new MappingException("Error: Error embedding resources. " +
							"The current slice can not be allocated.");
				}
				log.debug("creating partia bound merged");
				partialBoundTopologies.add(partialBoundTopology);
			}
			
			logService.log(LogService.LOG_INFO,"Creating full bounded topology (merge)...");
			userFeedback.instantInfo(sessionID, 
					"IRM Creating full bounded topology...", 
					"Creating full bounded topology...", 
					IRMConstants.IRM_FEEDBACK_URL);
			
			fullBoundedTopology = merge(virtualTopology.toString(), 
					partialBoundTopologies, virtualTopology);
			
		}
		
		if (fullBoundedTopology==null) {
			log.debug("error in creating the topology");
			throw new MappingException("Error: Error creating the bounded topology.");
			
		}
		
		return fullBoundedTopology;
	}

	/**
	 * Separate the resources according the Platform requirements. Put them in
	 * platformUnboundedTopology if Platform is not specified.
	 *
	 * @param unboundedTopology the unbounded topology
	 * @param platforms the platforms
	 * @param platformUnboundTopology the platform unbound topology 
	 * to be filled with no platform bounded resources (unbounded ones)
	 * @return the groups of resources separated by its Platform
	 */
	private Set<Topology> checkPlatformBoundResources(
			Topology unboundedTopology, Set<Platform> platforms, Topology platformUnboundTopology) {
		
		Set<Topology> result = new HashSet<Topology>();
		
		// Create one Topology for each Platform in the request
		for (Platform platform : platforms) {
			if (!IMOperations.isSetEmpty(platform.getContains())) {
				Topology t = new TopologyImpl(IMOperations.getId(platform.toString()));
				Set<Resource> resToAdd = new HashSet<Resource>();
				for (Resource r : platform.getContains()) {
					resToAdd.add(r);
					// remove the resource from the unboundedTopology
					unboundedTopology.getContains().remove(r);
				}
				t.setContains(resToAdd);
				result.add(t);
			}
		}
		
		// Add no platform resources to platformUnboundTopology
		Set<Resource> resToAdd = new HashSet<Resource>();
		for (Resource res : unboundedTopology.getContains()) {
			boolean found=false;
			for (Topology platformT : result) {
				for (Resource platformR : platformT.getContains()) {
					if (IMOperations.getId(res.toString())
							.equals(IMOperations.getId(platformR.toString()))) {
						found=true;
						break;
					}		
				}
			}
			// Links should not be considered as platform unbound resources
			if (!found && !(res instanceof Link)) {
				resToAdd.add(res);
			}
		}
		platformUnboundTopology.setContains(resToAdd);
		
		return result;
		
		
		/**
		 * Solution using getIsContainedIn.
		 */
//		Set<Topology> result = new HashSet<Topology>();
//		
//		// Create one Topology for each Platform in the request
//		for (Platform platform : platforms) {
//			Topology t = new TopologyImpl(IMOperations.getId(platform.toString()));
//			result.add(t);
//		}
//		// Add unboundedTopology resources to the corresponding Topology
//		for (Resource res : unboundedTopology.getContains()) {
//			for (Group gr : res.getIsContainedIn()) {
//				if (gr instanceof Platform) {
//					boolean found = false;
//					for (Topology platformT : result) {
//						if (IMOperations.getId(gr.toString())
//								.equals(IMOperations.getId(platformT.toString()))) {
//							platformT.getContains().add(res);
//							found=true;
//							break;
//						}
//					}
//					if (!found) {platformUnboundTopology.getContains().add(res);}
//				}
//			}
//		}	
//		return result;

	}
	
	/**
	 * Sets into partialTopologies (the ones with the resources already allocated in Platforms,
	 * the results obtained from splitting algorithm.
	 *
	 * @param partialTopologies the partial topologies already platform bounded
	 * @param partialSplitPlatforms the partial split topologies
	 */
	private void mergeSplittingResults(Set<Topology> partialTopologies, 
			Set<Platform> partialSplitPlatforms) {
		
		// Check if Platforms from Splitting already exist in partialTopologies
		for (Platform splitT : partialSplitPlatforms) {
			boolean found = false;
			for (Topology partialT : partialTopologies) {
				if (IMOperations.getId(splitT.toString())
						.equals(IMOperations.getId(partialT.toString()))) {
					addResourcesToTopology(partialT,splitT);
					found = true;
					break;
				}
			}
			if (!found) {
				// Create Topology for new Platform
				Topology partialTopology = new TopologyImpl(IMOperations.getId(splitT.toString()));
				Set<Resource> resToAdd = new HashSet<Resource>();
				for (Resource splitR : splitT.getContains()) {
					resToAdd.add(splitR);
				}
				partialTopology.setContains(resToAdd);
				// Add it to partialTopologies
				partialTopologies.add(partialTopology);
			}
		}
		
	}
	
	/**
	 * Adds resources of topologyToAdd to topology.
	 *
	 * @param topology the topology
	 * @param splitT the topology to add
	 */
	private void addResourcesToTopology(Topology topology, Platform splitT) {
		for (Resource resourceToAdd : splitT.getContains()) {
			boolean found = false;
			for (Resource res : topology.getContains()) {
				if (IMOperations.getId(res.toString())
						.equals(IMOperations.getId(resourceToAdd.toString()))) {
					found = true;
					break;
				}
			}
			if (!found) {
				topology.getContains().add(resourceToAdd);
			}
		}
	}
	
	/**
	 * Merges topologies in combined testbed into a federated topology. 
	 * PRE: All Groups are well formed
	 * @param partialTopologies collection of topologies to be merged
	 * @return federated topology
	 */
	private Topology merge(String topologyName, Set<Topology> partialTopologies, 
			Topology virtualRequest) {
		
		logService.log(LogService.LOG_INFO, "Starting merge operation...");
		log.debug("Starting merge operation...");
		Topology result = null;
		try {
			result= new TopologyImpl(topologyName);
			logService.log(LogService.LOG_INFO, "Topology "+result.toString()+" created");
			log.debug("Topology "+result.toString()+" created");
			
			Set<Resource> resources = new HashSet<Resource>();
			// Adding Intra-domain resources to the merged topology
			for (Topology partialTopology : partialTopologies) {
				logService.log(LogService.LOG_INFO, "Adding resources from "+partialTopology.toString());
				log.debug("Adding resources from "+partialTopology.toString());
				/** adding REMOTE mapped physical resources to the virtualRequest 
				 * and into the resulting merged topology **/
				addRemoteMappedResources(virtualRequest,partialTopology, resources);
			}
			result.setContains(resources);
			log.debug("Adding result");
			// Detecting and creating Request Inter-domain links
			for (Resource res : virtualRequest.getContains()) {
				if (res instanceof VirtualLink) {
					log.debug("link found "+res.toString());
					if (!result.getContains().contains(res)) {
						logService.log(LogService.LOG_INFO, "Creating interdomain path for "+res.toString());
						log.debug("creating interdomain path for "+res.toString());
						Path interPath = IRMOperations.createInterDomainLink((VirtualLink)res);
						if (interPath==null) {
							return null;
						}
						logService.log(LogService.LOG_INFO, "Path "+interPath.toString()+" created");
						log.debug("creating interdomain path for "+res.toString());
						Set<LinkOrPath> provisionedBy = new HashSet<LinkOrPath>();
						provisionedBy.add(interPath);
						((VirtualLink)res).setProvisionedBy(provisionedBy);
						result.getContains().add(res);
						logService.log(LogService.LOG_INFO, res.toString()+" added to the merged topology");
					} 
					else {
						logService.log(LogService.LOG_DEBUG,
								res.toString()+" is an Intra-domain link");
					}
				}
			}
			
			// TODO merge bounded resources with unbounded resources
			// (in case of partial bounded requests)... 
			// TODO Detecting and creating Splitting Algorithm Inter-domain links?
		
		} catch (Exception e) {
			logService.log(LogService.LOG_ERROR, "Error merging topologies", e);
		}
		return result;
	}
	
	/**
	 * Add to the virtualRequest and mergedTopologyResources the mappings done by mapOnTestbed
	 * @param virtualRequest
	 * @param partialTopology
	 * @param mergedTopologyResources 
	 */
	private void addRemoteMappedResources(Topology virtualRequest,
			Topology partialTopology, Set<Resource> mergedTopologyResources) {
		for (Resource partialRes : partialTopology.getContains()) {
			for (Resource requestRes : virtualRequest.getContains()) {
				if (partialRes.toString().equals(requestRes.toString())) {
					log.debug("Adding resources "+requestRes.toString());
					mergedTopologyResources.add(requestRes);
					if ((requestRes instanceof VirtualNode)
							&& (((VirtualNode) requestRes).getImplementedBy()==null)) {
						// Getting implementedBy relation for node
						((VirtualNode) requestRes).setImplementedBy(((VirtualNode) partialRes).getImplementedBy());
						// Getting implementedBy relation for node interfaces
						log.debug("Adding node resources "+requestRes.toString());
						addRemoteMappedInterfaces((VirtualNode) requestRes,(VirtualNode) partialRes);
						break;
					} else if ((requestRes instanceof VirtualLink)
							&& ((VirtualLink) requestRes).getProvisionedBy()==null) {
						log.debug("Adding link resources "+requestRes.toString());
						((VirtualLink) requestRes).setProvisionedBy(((VirtualLink) partialRes).getProvisionedBy());
						break;
					}
				}
			}
		}
	}

	/**
	 * Adds the remote mapped interfaces to the request interfaces
	 *
	 * @param requestNode the request node
	 * @param partialNode the remote node
	 */
	private void addRemoteMappedInterfaces(VirtualNode requestNode,
			VirtualNode partialNode) {
		// out ifaces
		if (!IMOperations.isSetEmpty(partialNode.getHasOutboundInterfaces())) {
			for (Interface partialIface : partialNode.getHasOutboundInterfaces()) {
				if (!IMOperations.isSetEmpty(requestNode.getHasOutboundInterfaces())) {
					for (Interface requestIface : requestNode.getHasOutboundInterfaces()) {
						if (partialIface.toString().equals(requestIface.toString())
								&& IMOperations.isSetEmpty(requestIface.getImplementedBy())
								&& !IMOperations.isSetEmpty(partialIface.getImplementedBy())) {
							requestIface.setImplementedBy(partialIface.getImplementedBy());
							break;
						}
					}
				}
			}
		}
		// in ifaces
		if (!IMOperations.isSetEmpty(partialNode.getHasInboundInterfaces())) {
			for (Interface partialIface : partialNode.getHasInboundInterfaces()) {
				if (!IMOperations.isSetEmpty(requestNode.getHasInboundInterfaces())) {
					for (Interface requestIface : requestNode.getHasInboundInterfaces()) {
						if (partialIface.toString().equals(requestIface.toString())
								&& IMOperations.isSetEmpty(requestIface.getImplementedBy())
								&& !IMOperations.isSetEmpty(partialIface.getImplementedBy())) {
							requestIface.setImplementedBy(partialIface.getImplementedBy());
							break;
						}
					}
				}
			}
		}
	}
}
