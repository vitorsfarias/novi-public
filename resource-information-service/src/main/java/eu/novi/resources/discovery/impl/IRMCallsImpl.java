package eu.novi.resources.discovery.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.database.FindLocalPartitioningCost;
import eu.novi.resources.discovery.database.FindLocalResources;
import eu.novi.resources.discovery.database.IRMLocalDbCalls;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.ReserveSlice;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscovery;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.FRResponseImp;
import eu.novi.resources.discovery.response.ReserveResponse;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.Testbeds;

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
 * ******************************************************************************
 * 
 * it implements the resource discovery for IRM
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class IRMCallsImpl implements IRMCalls {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(IRMCallsImpl.class);
	
    /**
     * Testbed name on which the service is running e.g. PlanetLab, FEDERICA.
     */
    private String testbed; //get for service mix blueprint
	//private TestbedCommunication testbedComm;//get from service mix
	private RemoteRisDiscovery remoteRis = new RemoteRisDiscoveryImpl(testbed);
	//private PolicyServCommun policyService; //get from service mix 
	

	//List<RemoteRisServe> remoteRISList; // for junit testing

	ReportEvent userFeedback;

	/**
	 * @see eu.novi.resources.discovery.IRMCalls#findResources(Topology)
	 */
	@Override
	public FRResponse findResources(String sessionID, Topology topology,  NOVIUserImpl noviUser)
	{
		// fall back only if sessionID is null
		if(sessionID == null){
			sessionID = userFeedback.getCurrentSessionID();
		}
		userFeedback.instantInfo(sessionID, "RIS (findResources)", 
				"RIS receiving find request for topology"+topology.toString(), "http://www.ris.com");
		
		//this is a special case only for FEDERICA, all the other platforms are working differently
		// if (Testbeds.isFederica(testbed))
		// {
			// Platform platform = IRMLocalDbCalls.getSubstrate(Testbeds.FEDERICA);
			// FRResponseImp response = new FRResponseImp();
			// response.setFedericaTopology(platform);
			// return response;
			
			
		// }
		
		
		FindLocalResources findLocalRes = new FindLocalResources();
		//findLocalRes.setPolicyService(policyService);
		return findLocalRes.findLocalResources(topology, noviUser);
		
	}
	
	

	@Override
	public FRResponse getSubstrateAvailability(String sessionID) {
		userFeedback.instantInfo(sessionID, "RIS (getSubstrateAvailability)", 
				"RIS getting the platform " + testbed + " substrate", "http://www.ris.com");
		
		Platform platform = IRMLocalDbCalls.getSubstrate(testbed);
		FRResponseImp response = new FRResponseImp();
		response.setFedericaTopology(platform);
		return response;
	}
	
	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#findLocalResourcesUpdate(Reservation, Set)
	 */
	@Override
	public FRResponse findLocalResourcesUpdate(String sessionID, Reservation slice,
			Set<String> failedMachinesURIs) {
		
		// fall back only if sessionID is null
		if(sessionID == null){
			sessionID = userFeedback.getCurrentSessionID();
		}
		userFeedback.instantInfo(sessionID, "RIS (findResourcesUpdate)", 
				"RIS receiving find  resources update request for slice" + 
		slice.toString(), "http://www.ris.com");

		FindLocalResources findLocalRes = new FindLocalResources();
		//findLocalRes.setPolicyService(policyService);
		return findLocalRes.findLocalResourcesUpdate(slice, failedMachinesURIs);
	}
	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#findPartitioningCost(Topology)
	 */
	@Override
	public Vector<FPartCostTestbedResponseImpl> findPartitioningCost(String sessionID,
			final Topology requestedTopology) {
		log.info("I got a find partitioning cost call");

		// fall back only if sessionID is null
		if(sessionID == null){
			sessionID = userFeedback.getCurrentSessionID();
		}
		userFeedback.instantInfo(sessionID, "RIS (findPartitioningCost)", 
				"RIS Find partitioning cost", "http://www.ris.com");

		Vector<FPartCostTestbedResponseImpl> response = new Vector<FPartCostTestbedResponseImpl>();

		//use threads and concurrency//

		////call to local database///
		Future<FPartCostTestbedResponseImpl> localResults = PeriodicUpdate.executeNewThread(
				new Callable<FPartCostTestbedResponseImpl>() {

					@Override
					public FPartCostTestbedResponseImpl call() throws Exception {
						log.info("Calling the local DB using new thread");
						FindLocalPartitioningCost findLocal = new FindLocalPartitioningCost(testbed);
						return findLocal.findLocalPartitioningCost(requestedTopology);

					}

				});


		////call remote platforms///
		Future<List<FPartCostTestbedResponseImpl>> remoteResults = PeriodicUpdate.executeNewThread(
				new Callable<List<FPartCostTestbedResponseImpl>>() {

					@Override
					public List<FPartCostTestbedResponseImpl> call() throws Exception {
						//here is the actual code
						log.info("Calling the remote platform using new thread");
						RemoteRisDiscovery remoteRIS = new RemoteRisDiscoveryImpl(testbed);
						//remoteRIS.setRemoteRISList(remoteRISList);
						List<FPartCostTestbedResponseImpl> remotAnsw = remoteRIS.findRemotePartitioningCost(
								((TopologyImpl) requestedTopology));
						if (remotAnsw.isEmpty())
						{
							String st = "There is a problem to get the find partitioning cost from the remote platform";
							log.error(st);
							userFeedback.errorEvent(userFeedback.getCurrentSessionID(), "RIS (findPartitioningCost)", 
									st, "http://www.ris.com");

						} else
						{
							log.info("I got back the answer from the remote platform");

						}
						return remotAnsw;
					}

				});

		//gather the results
		try {
			//to be sure that all the threads are started
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			log.warn(e1.getMessage());
			log.warn("Was not able to sleep");
		}

		//local
		try {
			response.add(localResults.get());
			log.info("I got the local results");
		} catch (InterruptedException e) {
			log.warn(e.getMessage());
			log.warn("Find partitioning cost: I was not able to get the local information");
		} catch (ExecutionException e) {
			log.warn(e.getMessage());
			log.warn("Find partitioning cost: I was not able to get the local information");
		}

		//remote
		try {
			response.addAll(remoteResults.get());
			log.info("I got the remote results");
		} catch (InterruptedException e) {
			log.warn(e.getMessage());
			log.warn("Find partitioning cost: I was not able to get the remote information");
		} catch (ExecutionException e) {
			log.warn(e.getMessage());
			log.warn("Find partitioning cost: I was not able to get the remote information");
		}
		
		return response;
		
		/////////////////FAKE FEDERICA RESPONSE///////
		/*FPartCostTestbedResponseImpl fedeRes = new FPartCostTestbedResponseImpl(
				"http://fp7-novi.eu/im.owl#federica");
		Set<Resource> resources = requestedTopology.getContains();
		for (Resource r : resources)
		{
		
			if (r instanceof VirtualNode)
			{
				FPartCostRecordImpl vNode = new FPartCostRecordImpl(r.toString());
				vNode.setAvailResNumber(1);
				vNode.setAverUtil(2.5f);
				fedeRes.setNodeCosts(vNode);
				
			}
			else if (r instanceof Link)
			{
				FPartCostRecordImpl link = new FPartCostRecordImpl(r.toString());
				link.setAvailResNumber(3);
				link.setAverUtil(1.5f);
				fedeRes.setNodeCosts(link);
				
			}
		}
		response.add(fedeRes);*/
		///////////////////////////////////////////////
		
		
	}
	
	

	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#reserveSlice(Topology)
	 */
	@Override
	public ReserveResponse reserveSlice(String sessionID, final Topology boundRequest, Integer sliceID, NOVIUserImpl user)
	{
		// fall back only if sessionID is null
		if(sessionID == null){
			sessionID = userFeedback.getCurrentSessionID();
		}
		userFeedback.instantInfo(sessionID, "RIS (reserveSlice)", 
				"RIS reserving slice"+boundRequest.toString(), "http://www.ris.com");
		ReserveSlice reserve = new ReserveSlice();
		
		
		///reserve.setTestbedComm(testbedComm);
		log.info("RIS recieved a reserveSlice call from IRM, for a sliceID: {}...", sliceID);

		ReserveResponse response = reserve.reserveLocalSlice(boundRequest, sliceID, user);
		if (!response.hasError())
		{
			userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "RIS (reserveSlice)", 
					"The reservation was succesful in request handler and in the local NOVI layer. \n" +
					"The login info is : " + response.getUserLoginInfo()
					, "http://www.ris.com");
			//don't store anything in the federica site
			/*Reservation limitedSlice = ((ReserveResponseImp) response).getLimitedSliceInfo();
			if (limitedSlice != null)
			{
				userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "RIS (reserveSlice)", 
						"Contacting remote RIS..., ", "http://www.ris.com");
				log.info("Contacting remote RIS to store some slice information for {}...",
						limitedSlice.toString());
				remoteRis.storeRemoteSlice( ((ReservationImpl) limitedSlice));
				
			}
			else
			{
				log.warn("The slice object that should be stored to the remote platform is null");
			}
			*/
		}
		else
		{
			log.warn("The response of the reservation has error : {}", response.getErrorMessage());
			userFeedback.errorEvent(userFeedback.getCurrentSessionID(), "RIS (reserveSlice)", 
						"The reservation has Error : " + response.getErrorMessage(),
						"http://www.ris.com");
		}

		return response;
		
	}
	
	@Override
	public ReserveResponse updateSlice(String sessionID, Topology boundRequest, Integer sliceID) {
		// fall back only if sessionID is null
		if(sessionID == null){
			sessionID = userFeedback.getCurrentSessionID();
		}
		userFeedback.instantInfo(sessionID, "RIS (updateSlice)", 
				"RIS updating slice"+boundRequest.toString(), "http://www.ris.com");
		ReserveSlice reserve = new ReserveSlice();
		///reserve.setTestbedComm(testbedComm);

		ReserveResponse response = reserve.updateLocalSlice(boundRequest, sliceID);
		if (!response.hasError())
		{
			userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "RIS (updateSlice)", 
					"The slice update was succesful in request handler and in the local NOVI layer." +
					"\nThe User login info is: " + response.getUserLoginInfo()
					, "http://www.ris.com");
			
		}
		else
		{
			log.warn("The response of the update slice has error : {}. ", response.getErrorMessage());
			userFeedback.errorEvent(userFeedback.getCurrentSessionID(), "RIS (updateSlice)", 
						"The update slice has Error : " + response.getErrorMessage(),
						"http://www.ris.com");
		}

		return response;
	}
	
	
	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#getSlice(String)
	 */
	@Override
	public Reservation getSlice(String uri)
	{
		Reservation slice = IRMLocalDbCalls.getLocalSlice(uri);
		if (slice == null || slice.getContains() == null || slice.getContains().isEmpty())
		{
			log.info("Contacting Remote RIS to get the slice information");
			return remoteRis.getRemoteSlice(uri);
			
		}
		else
		{
			log.info("The slice information was stored locally");
			return slice;
		}
		
	}

	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#getResource(String)
	 */
	@Override
	public Resource getResource(String uri)
	{
		Resource res = LocalDbCalls.getLocalResource(uri);
		if (res != null)
		{
			return res;
		}
		else 
		{
			log.info("Contacting remote platform to get the resource {}", uri);
			return remoteRis.getRemoteResource(uri);
		}
		
	}
	
	
	@Override
	public Set<Resource> getResources(Set<String> uris)
	{
		Set<Resource> answer = new HashSet<Resource>();
		Set<String> lookRemotely = new HashSet<String>();
		
		
		for (String uri : uris)
		{
			Resource res = LocalDbCalls.getLocalResource(uri);
			if (res != null)
			{//the resources was found locally
				answer.add(res);
			}
			else 
			{
				lookRemotely.add(uri);
				
			}
			
		}
		
		
		if (lookRemotely.isEmpty())
		{
			log.info("All the resources were found locally");
		}
		else
		{
			log.info("Contacting remote platform to get the resources {}", lookRemotely.toString());
			Set<ResourceImpl>  remoteRes = remoteRis.getRemoteResources(lookRemotely);
			if (remoteRes != null)
			{
				answer.addAll(remoteRes);
			}
			
		}
		
		return answer;
		
		
	}
	
	
	@Override
	public Set<String> checkResources(String sessionID, Set<String> uris, NOVIUserImpl user)
	{
		Set<String> answer = new HashSet<String>();
		Set<String> lookRemotely = new HashSet<String>();
		Set<String> localRes = new HashSet<String>();
		log.info("I got a checkResources call for user {} and resources {}",
				user.toString(), uris.toString());
		
		for (String uri : uris)
		{
			Resource res = LocalDbCalls.getLocalResource(uri);
			if (res != null)
			{//the resources was found locally
				localRes.add(uri);
			}
			else 
			{
				lookRemotely.add(uri);
				
			}
			
		}
		
		
		//contact local policy for the authorized resources
		Map<String, Boolean>  policyAnswer = PolicyServCommun.
				getAuthorizedResources(sessionID, user, localRes, localRes.size());
		Set<String> validLocalRes = PolicyServCommun.getAuthResourcesFromMap(policyAnswer);
		answer.addAll(validLocalRes);
		
		if (lookRemotely.isEmpty())
		{
			log.info("All the resources were found locally");
		}
		else
		{
			String userName  =  user.toString();
			log.info("Contacting remote platform to check the resources {}", lookRemotely.toString());
			Set<String>  remoteRes = remoteRis.checkRemoteResources(lookRemotely, userName);
			if (remoteRes != null)
			{
				log.info("I got back the valid resources: {}", remoteRes.toString());
				answer.addAll(remoteRes);
			}
			
		}
		
		return answer;
		
		
	}


	/**
	 * @see eu.novi.resources.discovery.IRMCalls#getSubstrate(java.lang.String)
	 * 
	 */
	@Override
	public Platform getSubstrate(String uri) {
		return IRMLocalDbCalls.getSubstrate(uri);
	}

	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#getNSwitchAddress(Node node, String sliceName)
	 */
	@Override
	public String getNSwitchAddress(Node node, String sliceName) throws IllegalArgumentException {
		if(node == null){
			throw new IllegalArgumentException("The Node parametr is null!");
		}
		if(sliceName == null || sliceName.trim().equals("")){
			throw new IllegalArgumentException("Slice name is empty!");
		}
		
		/*HARDCODING* -> When hardwaraType is router return nswitch federica addresss */
		if(node.getHardwareType() != null && node.getHardwareType().trim().equals(Node.HARDWARE_TYPE_ROUTER)){
			return "194.132.52.217";
		}
		
		return null;
	}
	
	
	/**
	 * @see eu.novi.resources.discovery.IRMCalls#getPlanetlabPrivateAddressForNSwitchEndpoint(Node node, String string)
	 */
	@Override
	public String getPlanetlabPrivateAddressForNSwitchEndpoint(Node node, String sliceName)
			throws IllegalArgumentException {
		
		if(node == null){
			throw new IllegalArgumentException("The Node parametr is null!");
		}
		if(sliceName == null || sliceName.trim().equals("")){
			throw new IllegalArgumentException("Slice name is empty!");
		}
		
		if(node.getHardwareType() == null  || node.getHardwareType().trim().equals("")){
			throw new IllegalArgumentException("Node has no hardwaraType setup");
		}
		
		if(node.getHardwareType() != null && !node.getHardwareType().trim().equals(Node.HARDWARE_TYPE_PLANETLAB_NODE)){
			throw new IllegalArgumentException("Node has wrong hardwareType: " + node.getHardwareType() + " . It should be:" + Node.HARDWARE_TYPE_PLANETLAB_NODE);
		}
		if(node.getHardwareType() != null && node.getHardwareType().trim().equals(Node.HARDWARE_TYPE_PLANETLAB_NODE)){
			if(sliceName.equals("slice1")){
				return "192.168.0.1";
			}
		}
		
		return null;
	}
	
	
	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/


	/*public TestbedCommunication getTestbedComm() {
		return testbedComm;
	}


	public void setTestbedComm(TestbedCommunication testbedComm) {
		this.testbedComm = testbedComm;
	}*/
	
	public String getTestbed() {
		return testbed;
	}


	public void setTestbed(String testbed) {
		remoteRis.setTestbed(testbed);
		this.testbed = testbed;
	}
	


	public RemoteRisDiscovery getRemoteRis() {
		return remoteRis;
	}


	//this is not used from service mix
	public void setRemoteRis(RemoteRisDiscovery remoteRis) {
		this.remoteRis = remoteRis;
	}
	
  /*  public PolicyServCommun getPolicyService() {
		return policyService;
	}


	public void setPolicyService(PolicyServCommun policyService) {
		this.policyService = policyService;
	}*/


	/**
	 * @return the userFeedback
	 */
	public ReportEvent getUserFeedback() {
		return userFeedback;
	}


	/**
	 * @param userFeedback the userFeedback to set
	 */
	public void setUserFeedback(ReportEvent userFeedback) {
		this.userFeedback = userFeedback;
	}


	/**
	 * @return the remoteRISList
	 */
	/*public List<RemoteRisServe> getRemoteRISList() {
		return remoteRISList;
	}


	*//**
	 * @param remoteRISList the remoteRISList to set
	 *//*
	public void setRemoteRISList(List<RemoteRisServe> remoteRISList) {
		this.remoteRISList = remoteRISList;
		remoteRis.setRemoteRISList(remoteRISList);
	}*/






    

}
