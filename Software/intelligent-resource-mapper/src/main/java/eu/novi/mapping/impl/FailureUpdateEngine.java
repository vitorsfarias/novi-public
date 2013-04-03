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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.util.IMCopy;
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

/**
 * The Class responsible for handling updating of slices triggered
 * by resource failures.
 * @see IRMEngine
 */
public class FailureUpdateEngine extends IRMEngine {

	/** The task list for keeping control of embedding threads. */
    protected List<Future<String>> taskList = new ArrayList<Future<String>>();
    
    /** Local logging. */
	private static final transient Logger log = LoggerFactory.getLogger(FailureUpdateEngine.class);
	
	/**
     * Constructor of FailureUpdateEngine. It sets all the attributes of the superclass (IRMEngine)
     */
	public FailureUpdateEngine(IRMCalls irmCallsFromRIS, List<RemoteIRM> irms, 
			List<EmbeddingAlgorithmInterface> embeddingAlgorithms, 
			ReportEvent userFeedback, String sessionID, 
			LogService logService, String testbed, ScheduledExecutorService scheduler) {
		this.irmCallsFromRIS = irmCallsFromRIS;
		this.irms = irms;
		this.userFeedback = userFeedback;
		this.logService = logService;
		this.testbed = testbed;
		this.embeddingAlgorithms = embeddingAlgorithms;
		this.scheduler = scheduler;
	}
	
	/**
	 * Update slice for failing of resources.
	 *
	 * @param currentSlice the current slice
	 * @param failingResources the failing resources
	 * @param failingPhysicalResourcesIDs the failing physical resources i ds
	 * @return the collection
	 * @throws MappingException the mapping exception
	 */
	public Collection<String> updateSlice(String sessionID, Reservation currentSlice, 
			Set<Resource> failingResources,
			Set<String> failingPhysicalResourcesIDs) throws MappingException {
		
		Collection<String> result = new HashSet<String>();
		
		// doing a copy of currentSlice
		IMCopy imc = new IMCopy();
		Reservation sliceCopy = (Reservation) imc.copy(currentSlice, -1);
		IMOperations.analyzeGroup(sliceCopy, logService);
		Reservation sliceCopyRIS = (Reservation) imc.copy(currentSlice, -1);
		
		
		/** SPLIT FAILING RESOURCES **/
		userFeedback.instantInfo(sessionID, 
				"IRM update failing resources", 
				"Unmap failing resources to reallocate in the same testbed...", 
				IRMConstants.IRM_FEEDBACK_URL);

		// Set partialTopologies without splitting algorithm (In the same platform they was located) 
		
/*		Set<Group> partialTopologies = updateFailingResourcesTestbed(sliceCopy,failingResources,logService);
		if (partialTopologies == null) {
			throw new MappingException("Error in retrieving the user's slice information: failing resources cannot be updated");
		}
			*/
		 Set<Group> partialTopologies  =  new HashSet<Group>();
		 for (RemoteIRM irm : irms) {
			Reservation sliceTMP= (Reservation) imc.copy(currentSlice, -1);
		
			Group  partialTopology =  updateFailingResourcesTestbed(sliceCopy,failingResources, irm, logService);
			if (partialTopology == null) {
			 throw new MappingException("Error in retrieving the user's slice information: failing resources cannot be updated");
			 }
			 if (partialTopology.getContains().size()>0) {
			 log.debug("partialTopology.getContains().size()>0 " + irm.getTestbed()); 
			 partialTopologies.add(partialTopology);
			 }
		 }
		
			
		if (partialTopologies.isEmpty()) {
			throw new MappingException("Unable to split failing resources in testbeds");
		} else {
			disconnectFailingResources(sliceCopy,failingResources, logService);
		}
		
		for (Group partialTopology : partialTopologies) {
			IMOperations.analyzeGroup(partialTopology, logService);
		}
		
		boolean errorEmbedding = false;
		Set<Group> partialBoundTopologies = new HashSet<Group>();
		int taskID = 0;
		for (Group partialTopology : partialTopologies) {
		
			/****************************************************************
			 * *********************** LOCAL ********************************
			 ****************************************************************/	
			
			userFeedback.instantInfo(sessionID, 
					"IRM Mapping on local testbeds...", 
					"Mapping on local testbeds...", 
					IRMConstants.IRM_FEEDBACK_URL);

			// Select the appropriate IRM to call	
			for (RemoteIRM irm : irms) {
				if (partialTopology.toString().contains(irm.getTestbed())) {
					logService.log(LogService.LOG_INFO,"Mapping on testbed "+irm.getTestbed()+"...");
					userFeedback.instantInfo(sessionID, 
							"IRM Mapping on testbed "+irm.getTestbed()+"...", 
							"Number of resources to map: "
								+partialTopology.getContains().size(), 
							IRMConstants.IRM_FEEDBACK_URL);

					/** Calling String updateOnTestbed **/
					// translating to string
					if (IMOperations.isSetEmpty(partialTopology.getContains())) {
						throw new MappingException("Partial Topology sent to "+irm.getTestbed()+" is empty");
					}
					IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
					String partial = repositoryUtil.exportIMObjectToString(partialTopology);
				//	String slice = repositoryUtil.exportIMObjectToString(sliceCopy);
					String slice = repositoryUtil.exportIMObjectToString(sliceCopyRIS);
					
					taskID++;
					UpdateOnTestbedCallable uot = new UpdateOnTestbedCallable(sessionID,slice,partial,sliceCopyRIS.toString(), 
							partialTopology.toString(),failingPhysicalResourcesIDs, irm, taskID);
					
					Future<String> task = scheduler.submit(uot);
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
			// translating to IM
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
				errorEmbedding = true;
				break;
			}
			partialBoundTopologies.add(partialBoundTopology);
		}
		
		
		// If embed process fails, call partitioning algorithm and embed again
		if (errorEmbedding) {
			
			logService.log(LogService.LOG_INFO,"Embedding failed. " +
					"Calling partitioning algorithm and trying again...");
			userFeedback.instantInfo(sessionID, 
					"IRM Updating slice", 
					"Embedding failed. Calling partitioning algorithm and trying again...", 
					IRMConstants.IRM_FEEDBACK_URL);
			
			partialBoundTopologies = 
				updateSliceWithPartitioningAlgorithm(sessionID, currentSlice,failingPhysicalResourcesIDs);
			
			if (partialBoundTopologies==null) {
				logService.log(LogService.LOG_ERROR,"Error re-embedding resources");
				userFeedback.errorEvent(sessionID, 
						"IRM Error re-embedding resources", 
						"Error: Error embedding resources after splitting process. " +
							"The current slice can not be allocated.", 
						IRMConstants.IRM_FEEDBACK_URL);
				throw new MappingException("IRM Error re-embedding resources (after splitting process). " +
						"The current slice can not be allocated.");
			}
		}
		
		logService.log(LogService.LOG_INFO,"Updating slice with the reallocation of failing resources...");
		userFeedback.instantInfo(sessionID, 
				"IRM Updating slice", 
				"Updating slice with the reallocation of failing resources...", 
				IRMConstants.IRM_FEEDBACK_URL);
		
		result = reallocateSlice(currentSlice, partialBoundTopologies);
		if (result.size()==0) {
			logService.log(LogService.LOG_ERROR,
					"Slice can not be reallocated. There are inconsistence with failing resources");
			userFeedback.errorEvent(sessionID, 
					"IRM Slice can not be reallocated", 
					"Slice can not be reallocated. There are inconsistence with failing resources", 
					IRMConstants.IRM_FEEDBACK_URL);
			throw new MappingException("Slice can not be reallocated. There are inconsistence with failing resources");
			
		}
		
		return result;
	}
	
	/**
	 * Split the failing resources into platforms, putting them
	 * in the same one they was before and disconnecting the resources
	 * for the failing ones.
	 *
	 * @param currentSlice the current slice
	 * @param failingResources the failing resources
	 * @return failing resources split into platforms
	 // */
	private Group updateFailingResourcesTestbed(Group currentSlice, Set<Resource> failingResources, RemoteIRM irm, LogService logService) {
		Set<Group> result = new HashSet<Group>();
		Group T = new GroupImpl(irm.getTestbed());
		Set<Resource> R = new HashSet<Resource>();
		Node pNode = null;
		Path path = null;
		String TestbedTopLeveAuthority = irm.getTestbedTopLeveAuthority();


		logService.log(LogService.LOG_INFO,"In updateFailingResourcesTestbed..." + irm.getTestbed() + "  "+ TestbedTopLeveAuthority);
		for (Resource vRes : currentSlice.getContains()) {
			
			if (vRes instanceof Node) {
				logService.log(LogService.LOG_INFO,vRes.toString());
				if(((Node) vRes).getImplementedBy()!=null) {
				pNode = ((Node) vRes).getImplementedBy().iterator().next();
				logService.log(LogService.LOG_INFO,pNode.toString());
				for (Resource pRes : failingResources) {
				 log.debug("Failing resources: " + pRes.toString());
				 log.debug("checking mapped resource: " + pNode.toString());
				 log.debug("checking virtual resource: " + vRes.toString());
				 log.debug("TestbedTopLeveAuthority : " + TestbedTopLeveAuthority);
					if (pRes.toString().equals(pNode.toString())) {	
					logService.log(LogService.LOG_INFO,"found failing resources");
					log.debug("found failing resource");
					logService.log(LogService.LOG_INFO,TestbedTopLeveAuthority.toLowerCase());
						if  (pNode.toString().toLowerCase().contains(TestbedTopLeveAuthority.toLowerCase())) {
							logService.log(LogService.LOG_INFO,"match TestbedTopLeveAuthority");
							R.add(vRes);
						} 
					}
				}
				}
			}
			else if (vRes instanceof Link) {
				if(((Link) vRes).getProvisionedBy()!=null){
				path = (Path) ((Link) vRes).getProvisionedBy().iterator().next();
					for (Resource pRes : failingResources) {
						if (pRes.equals(path)) {
							if  (path.toString().toLowerCase().contains(TestbedTopLeveAuthority.toLowerCase())) {
							R.add(vRes);
							} 
						}
					}
				}
			}
		}
		T.setContains(R);
		if (T.getContains().size()>0) {result.add(T);}
		Platform P = new PlatformImpl(irm.getTestbed());
		P.setContains(R);

		
		return T;
	}
	
	private void disconnectFailingResources(Group currentSlice, Set<Resource> failingResources, LogService logService){
		Node pNode = null;
		Path path = null;
		for (Resource vRes : currentSlice.getContains()) {
			logService.log(LogService.LOG_INFO,vRes.toString());
			if (vRes instanceof Node) {
				pNode = ((Node) vRes).getImplementedBy().iterator().next();
				logService.log(LogService.LOG_INFO,pNode.toString());
				for (Resource pRes : failingResources) {
					if (pRes.toString().equals(pNode.toString())) {	
					logService.log(LogService.LOG_INFO,"found failing resources");
						((Node) vRes).setImplementedBy(null);
					}
				}
			}
			else if (vRes instanceof Link) {
				path = (Path) ((Link) vRes).getProvisionedBy().iterator().next();
				for (Resource pRes : failingResources) {
					//Default
					if (pRes.equals(path)) {
						//Disconnect the Link
						((Link)vRes).setProvisionedBy(null);
					}
					
				}
			}
		}
	}
	
	
	/**
	 * Split the failing resources into platforms, putting them
	 * in the same one they was before and disconnecting the resources
	 * for the failing ones.
	 *
	 * @param currentSlice the current slice
	 * @param failingResources the failing resources
	 * @return failing resources split into platforms
	 // */
	private Set<Group> updateFailingResourcesTestbed(Group currentSlice, Set<Resource> failingResources, LogService logService) {
		Set<Group> result = new HashSet<Group>();
		Group plT = new GroupImpl(IRMConstants.PLANETLAB);
		Group fedeT = new GroupImpl(IRMConstants.FEDERICA);
		Set<Resource> plR = new HashSet<Resource>();
		Set<Resource> fedeR = new HashSet<Resource>();
		Node pNode = null;
		Path path = null;
		logService.log(LogService.LOG_INFO,"In updateFailingResourcesTestbed...");
		for (Resource vRes : currentSlice.getContains()) {
			logService.log(LogService.LOG_INFO,vRes.toString());
			if (vRes instanceof Node) {
				pNode = ((Node) vRes).getImplementedBy().iterator().next();
				logService.log(LogService.LOG_INFO,pNode.toString());
				for (Resource pRes : failingResources) {
					if (pRes.toString().equals(pNode.toString())) {	
					logService.log(LogService.LOG_INFO,"found failing resources");
						if  (pNode.getHardwareType().equals(IRMConstants.PLANETLAB_HW_TYPE)) {
							plR.add(vRes);
						} 
						else if (pNode.getHardwareType().equals(IRMConstants.FEDERICA_HW_TYPE_ROUTER)
								|| pNode.getHardwareType().equals(IRMConstants.FEDERICA_HW_TYPE_SERVER)) {
							fedeR.add(vRes);
						} 
					//	Default
						else {	
							plR.add(vRes);
						}
					//	Disconnect the Node
						((Node) vRes).setImplementedBy(null);
					}
				}
			}
			else if (vRes instanceof Link) {
				path = (Path) ((Link) vRes).getProvisionedBy().iterator().next();
				for (Resource pRes : failingResources) {
					//Default
					if (pRes.equals(path)) {
						fedeR.add(vRes);
					}
					//Disconnect the Link
					((Link)vRes).setProvisionedBy(null);
				}
			}
		}
		plT.setContains(plR);
		fedeT.setContains(fedeR);
		if (plT.getContains().size()>0) {result.add(plT);}
		if (fedeT.getContains().size()>0) {result.add(fedeT);}
		//Setting Platforms in the resources
		Platform plP = new PlatformImpl(IRMConstants.PLANETLAB);
		Platform fedeP = new PlatformImpl(IRMConstants.FEDERICA);
		plP.setContains(plR);
		fedeP.setContains(fedeR);
		
		return result;
	}
	
	/**
	 * Update Slice resources using splitting algorithm.
	 *
	 * @param currentSlice the current slice
	 * @param failingPhysicalResourcesIDs the failing physical resources ID's
	 * @return partialBoundTopologies
	 */
	private Set<Group> updateSliceWithPartitioningAlgorithm(String sessionID,
			Group currentSlice, Set<String> failingPhysicalResourcesIDs) {
		
		logService.log(LogService.LOG_INFO,"Updating resources using splitting algorithm...");
		userFeedback.instantInfo(sessionID, 
				"IRM Updating slice", 
				"Updating slice with the reallocation of failing resources using splitting algorithm...", 
				IRMConstants.IRM_FEEDBACK_URL);
		
		Topology partialBoundTopologyToSplit = IRMOperations.disconnectFailingResources(currentSlice,failingPhysicalResourcesIDs);
		Topology[] splitResources = IRMOperations.checkBoundUnboundRequest(partialBoundTopologyToSplit);
		Topology boundedTopology = splitResources[0];
		Topology unboundedTopology = splitResources[1];
		
		/** SPLIT RESOURCES **/
		Vector<FPartCostTestbedResponseImpl>  splittingCosts = irmCallsFromRIS.findPartitioningCost(sessionID, partialBoundTopologyToSplit);
		SplittingAlgorithm splitAlgo =  new SplittingAlgorithm();
		SparseMultigraph<NodeImpl, LinkImpl> virtualRequestToSplit =  GraphOperations.translateIMToGraph(partialBoundTopologyToSplit,true);
		PartitionedRequest subGraphs = new PartitionedRequest();
		try{
			subGraphs = splitAlgo.split(virtualRequestToSplit, 
					boundedTopology.getContains(),
					unboundedTopology.getContains(),
					new HashSet<Topology>(),
					splittingCosts,irms);			
		}catch(MappingException e){
			logService.log(LogService.LOG_ERROR,"Spltting resources failed: "+ e.getMessage());
			userFeedback.errorEvent(sessionID, 
					"IRM Error splitting request", 
					"Error splitting request: " + e.getMessage(), 
					IRMConstants.IRM_FEEDBACK_URL);
			return null;
		}

		Set<Platform> partialSplitPlatforms  =  subGraphs.getPartialPlatforms();
		/** END SPLIT RESOURCES **/
		
		Set<Group> partialBoundTopologies = new HashSet<Group>();
		taskList = new ArrayList<Future<String>>();
		int taskID = 0;
		for (Group partialTopology : partialSplitPlatforms) {
		
			/****************************************************************
			 * *********************** LOCAL ********************************
			 ****************************************************************/	
			
			userFeedback.instantInfo(sessionID, 
					"IRM Mapping on local testbeds...", 
					"Mapping on local testbeds...", 
					IRMConstants.IRM_FEEDBACK_URL);		
			
			// Select the appropriate IRM to call	
			for (RemoteIRM irm : irms) {
				if (partialTopology.toString().contains(irm.getTestbed())) {
					logService.log(LogService.LOG_INFO,"Mapping on testbed "+irm.getTestbed()+"...");
					userFeedback.instantInfo(sessionID, 
							"IRM Mapping on testbed "+irm.getTestbed()+"...", 
							"Number of resources to map: "
								+partialTopology.getContains().size(), 
							IRMConstants.IRM_FEEDBACK_URL);

					/** Calling String updateOnTestbed **/
					// translating to string
					if (IMOperations.isSetEmpty(partialTopology.getContains())) {
						logService.log(LogService.LOG_ERROR,"Partial Topology sent to "+irm.getTestbed()+" is empty");
						userFeedback.errorEvent(sessionID, 
								"IRM Error embedding resources", 
								"Partial Topology sent to "+irm.getTestbed()+" is empty", 
								IRMConstants.IRM_FEEDBACK_URL);
						return null;
					}
					IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
					String partial = repositoryUtil.exportIMObjectToString(partialTopology);
					String slice = repositoryUtil.exportIMObjectToString(currentSlice);
					taskID++;
					UpdateOnTestbedCallable uot = new UpdateOnTestbedCallable(sessionID, slice,partial,currentSlice.toString(), 
							partialTopology.toString(),failingPhysicalResourcesIDs,irm,taskID);
					
					Future<String> task = scheduler.submit(uot);
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
						"IRM Error embedding resources", 
						"Error: IRM Mapping on local testbed "+testbed+"... Thread execution error", 
						IRMConstants.IRM_FEEDBACK_URL);
				return null;
			}
			// translating to IM
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
				logService.log(LogService.LOG_ERROR,"Error embedding resources");
				userFeedback.errorEvent(sessionID, 
						"IRM Error embedding resources", 
						"Error: Error embedding resources", 
						IRMConstants.IRM_FEEDBACK_URL);
				return null;
			}
			partialBoundTopologies.add(partialBoundTopology);
		}

		return partialBoundTopologies;
	}
	
	/**
	 * Update the currentSlice with the resources in partialBoundTopologies.
	 *
	 * @param currentSlice slice to update
	 * @param partialBoundTopologies reallocated resources
	 * @return the IDs of the reallocated physical resoruces, if successful empty set otherwise
	 */
	private Collection<String> reallocateSlice(Group currentSlice,
			Set<Group> partialBoundTopologies) {
		
		logService.log(LogService.LOG_INFO,"Reallocating resources of slice "+currentSlice.toString());
		
		Collection<String> result = new HashSet<String>();
		
		for (Group groupToAllocate : partialBoundTopologies) {
			
			logService.log(LogService.LOG_INFO,"Resources to reallocate: "+groupToAllocate.getContains().size());
			
			for (Resource resourceToAllocate : groupToAllocate.getContains()) {
				
				logService.log(LogService.LOG_INFO,"Checking resource "+resourceToAllocate.toString());
				
				for (Resource resourceToChange : currentSlice.getContains()) {
					if (resourceToAllocate.toString().equals(resourceToChange.toString())) {
						
						logService.log(LogService.LOG_INFO,"Resource to change: "+resourceToChange.toString());
						
						if (resourceToChange instanceof VirtualNode) {
							
							logService.log(LogService.LOG_INFO,"Resource to change is a Virtual Node");
							
							Node pNodeToAllocate = ((VirtualNode) resourceToAllocate).getImplementedBy().iterator().next();
							
							logService.log(LogService.LOG_INFO,"Physical Node to reallocate: "+pNodeToAllocate.toString());
							
							// Replacing IN connections
							if (!IMOperations.isSetEmpty(((VirtualNode) resourceToChange).getHasInboundInterfaces())) {
								for (Interface inIfaceToChange : ((VirtualNode) resourceToChange).getHasInboundInterfaces()) {
									
									logService.log(LogService.LOG_INFO,"Replacing IN connection to iface "+inIfaceToChange.toString());
									
									Set<LinkOrPath> linksToChange = inIfaceToChange.getIsSink();
									if (!IMOperations.isSetEmpty(linksToChange)) {
										for (LinkOrPath linkToChange : linksToChange) {
											if (linkToChange instanceof VirtualLink) {
												
												logService.log(LogService.LOG_INFO,"Reallocating link "+linkToChange.toString());
												
												// TODO reallocation of intra-domain links? It is done by the embedding algorithm
												// getting source virtual node
												Node pSourceNode = IMOperations.getSourceNode((VirtualLink) linkToChange);
												if (pSourceNode==null) {
													return new HashSet<String>();
												}
												
												logService.log(LogService.LOG_INFO,"Source Physical Node "+pSourceNode.toString());
												
												Interface sourceInterface = 
													IRMOperations.getSourceNSwitchInterface(pSourceNode);
												Interface targetInterface = 
													IRMOperations.getTargetNSwitchInterface(pNodeToAllocate);
												if (sourceInterface==null || targetInterface==null) {
													return new HashSet<String>();
												}
												
												// Remove previous assignation 
												// Or completely remove the path and nswitch objects
												
												logService.log(LogService.LOG_INFO,"Removing old NSwitch Path...");
												
												IRMOperations.removeNSwitchPath((VirtualLink) linkToChange);
												
												logService.log(LogService.LOG_INFO,"Creating new NSwitch Path...");
												
												Path interPath = IRMOperations.createNSwitchPath(sourceInterface, 
														targetInterface, (VirtualLink) linkToChange);
												
												logService.log(LogService.LOG_INFO,"Setting new Path to the vlink...");
		
												// Setting new Path to the vlink
												Set<LinkOrPath> provisionedBy = new HashSet<LinkOrPath>();
												provisionedBy.add(interPath);
												((VirtualLink) linkToChange).setProvisionedBy(provisionedBy);
												
											}	
										}
									}
								}
							}
							
							// Replacing OUT connections
							if (!IMOperations.isSetEmpty(((VirtualNode) resourceToChange).getHasOutboundInterfaces())) {
								for (Interface outIfaceToChange : ((VirtualNode) resourceToChange).getHasOutboundInterfaces()) {
									
									logService.log(LogService.LOG_INFO,"Replacing OUT connection to iface "+outIfaceToChange.toString());
									
									Set<LinkOrPath> linksToChange = outIfaceToChange.getIsSource();
									if (!IMOperations.isSetEmpty(linksToChange)) {
										for (LinkOrPath linkToChange : linksToChange) {
											if (linkToChange instanceof VirtualLink) {
												
												logService.log(LogService.LOG_INFO,"Reallocating link "+linkToChange.toString());
												
												// TODO reallocation of intra-domain links? It is done by the embedding algorithm
												// getting target virtual node
												Node pTargetNode = IMOperations.getTargetNode((VirtualLink) linkToChange);
												if (pTargetNode==null) {
													return new HashSet<String>();
												}
												
												logService.log(LogService.LOG_INFO,"Target Physical Node "+pTargetNode.toString());
												
												Interface sourceInterface = 
													IRMOperations.getSourceNSwitchInterface(pNodeToAllocate);
												Interface targetInterface = 
													IRMOperations.getTargetNSwitchInterface(pTargetNode);
												if (sourceInterface==null || targetInterface==null) {
													return new HashSet<String>();
												}
												
												logService.log(LogService.LOG_INFO,"Removing old NSwitch Path...");
												
												// Remove previous assignation
												// Or completely remove the path and nswitch objects
												IRMOperations.removeNSwitchPath((VirtualLink) linkToChange);
												
												logService.log(LogService.LOG_INFO,"Creating new NSwitch Path...");
												
												Path interPath = IRMOperations.createNSwitchPath(sourceInterface, 
														targetInterface, (VirtualLink) linkToChange);
	
												logService.log(LogService.LOG_INFO,"Setting new Path to the vlink...");
												
												// Setting new Path to the vlink
												Set<LinkOrPath> provisionedBy = new HashSet<LinkOrPath>();
												provisionedBy.add(interPath);
												((VirtualLink) linkToChange).setProvisionedBy(provisionedBy);
												
											}	
										}
									}
								}
							}

							logService.log(LogService.LOG_INFO,"Replacing failing physical node with the new one");
							Set<Node> implementedBy = new HashSet<Node>();
							implementedBy.add(pNodeToAllocate);
							((VirtualNode) resourceToChange).setImplementedBy(implementedBy);
							result.add(pNodeToAllocate.toString());
							
						} else if (resourceToChange instanceof VirtualLink) {
							// TODO reallocate connections for the link? Currently, the failing resources are Nodes
							logService.log(LogService.LOG_DEBUG,"TODO: reallocate connections for the link");
						} else {return new HashSet<String>();}
					}
				}
			}
		}
		return result;
	}
}
