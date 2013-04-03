package eu.novi.resources.discovery.remote.serve;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.resources.discovery.database.DeleteSlice;
import eu.novi.resources.discovery.database.FindLocalPartitioningCost;
import eu.novi.resources.discovery.database.IRMLocalDbCalls;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.LockResources;
import eu.novi.resources.discovery.database.LockSession;
import eu.novi.resources.discovery.database.OwlCreator;
import eu.novi.resources.discovery.database.ReserveSlice;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
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
 * The implementation class of the RemoteRisServe 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class RemoteRisServeImpl implements RemoteRisServe {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(RemoteRisServeImpl.class);
	
	private String testbed; //get for service mix blueprint

	HashMap<String, FPartCostTestbedResponseImpl> cachedResult = new HashMap<String, FPartCostTestbedResponseImpl>();
	
	@Override
	public String dummyCall(String input) {
		if (input.equals("letSee"))
			return "good";
		else
			return "bad";
	}
	
	
	@Override
	public void lockUnlockLocalResources(LockSession lockSession, boolean isLock) {
		log.info("I got a lock/unlock call from a remote platform");
		LockResources lockRes = new LockResources();
		lockRes.lockUnlockLocalResources(lockSession, isLock, false);
		
	}

	@Override
	public String getLocalSlice(String uri, boolean getManifest)
	{
		log.info("I got a remote call for the getLocalSlice");
		if (!LocalDbCalls.checkSliceExist(uri))
		{
			log.warn("I can not find the slice {}.", uri);
			return null;
			
		}
		
		log.info("The slice is stored locally");
	
		if (getManifest)
		{
			log.info("I will return the manifest owl description");
			return OwlCreator.getSliceManifestInfoToString(uri);
			
		}
		else
		{
			log.info("I will return the normal slice decription, not the manifest");
			Reservation reservationObject =  (ReservationImpl) IRMLocalDbCalls.getLocalSlice(uri);
			IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
			return repositoryUtil.exportIMObjectToString(reservationObject);
			
		}

	}
	
	

	@Override
	public void storeLocallyRemoteSliceInfo(String slice, String testbed)
	{
		log.info("I got a remote call from {}, for the storeLocallyRemoteSliceInfo", 
				testbed);
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		Set<Reservation> sliceObjects = repositoryUtil.getIMObjectsFromString(slice, Reservation.class);
		Reservation sliceObject;
		if (sliceObjects.size() > 0)
		{
			sliceObject = sliceObjects.iterator().next();
			
		}
		else
		{
			log.warn("I can not get any reservation object from the xml/rdf string :\n {}", slice);
			return ;
		}
		ReserveSlice reservation = new ReserveSlice();
		reservation.storeRemoteSliceInfo(sliceObject, testbed);
		
	}

	@Override
	public FPartCostTestbedResponseImpl giveLocalPartitioningCost(
			String requestedTopology) {
		
		log.info("I got a remote call for the giveLocalPartitioningCost");
		
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		Topology top = imRepo.getIMObjectFromString(requestedTopology, Topology.class);
		log.info("I translated the String topology to objects");
		if (top == null)
		{
			log.warn("giveLocalPartitioningCost: there is a problem geting the topology object " +
					"from the xml/rdf string : {}", requestedTopology);
			FPartCostTestbedResponseImpl answ = new FPartCostTestbedResponseImpl();
			answ.setTestbedURI(testbed);
			
			return answ;
			
		}
		
		String topologyKey = getMD5Sum(requestedTopology);
		log.info("I got the MD5SUM key");
		
		//If we have results for this exact topology, immediately return.
		//This can means returning a flag result that we are actuall waiting.
		if(cachedResult.containsKey(topologyKey)){
			return cachedResult.get(topologyKey);
		}
		
		//First call, this part of code would only be reachable if we don't have waiting flag stored in cache:
		/// We will fill cache with a waiting flag, and then we will set a timer to start the real find partitioning task
		
		FPartCostTestbedResponseImpl waitingFLAG = new FPartCostTestbedResponseImpl();
		waitingFLAG.setTestbedURI("WAIT_FOR_REMOTE_PARTITIONING");
		
		cachedResult.put(topologyKey, waitingFLAG);
		
		
		
		// Spawn this timer just so that we can return
		final Topology finalTop = top;
		final String finalKey = topologyKey;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				FindLocalPartitioningCost findLocal = new FindLocalPartitioningCost(testbed);
				cachedResult.put(finalKey, findLocal.findLocalPartitioningCost(finalTop));
				log.info("To be sure, this line should appear once, and only ONCE for this request key"+finalKey);
			}
		}, 1000);
		
		
		log.info("I am done, I am going to return back the results");
		return  cachedResult.get(topologyKey);
		
	}
	
	@Override
	public String getLocalResource(String uri) {
		
		Resource res = LocalDbCalls.getLocalResource(uri);
		if (res == null)
		{
			return null;
		}
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		return imRepo.exportIMObjectToString(res);
	}
	
	
	@Override
	public Set<String> getLocalResources(Set<String> uris) {
		log.info("I get a remote getResources request for {} resources: {}", uris.size(),
				uris.toString());
		Set<String> answer = new HashSet<String>();
		Set<String> foundUris = new HashSet<String>();
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		for (String uri : uris)
		{
			Resource res = LocalDbCalls.getLocalResource(uri);
			if (res != null)
			{
				log.debug("The resource {} from remote getResources call, is found", uri);
				answer.add(imRepo.exportIMObjectToString(res));
				foundUris.add(uri);
			}
			else
			{
				log.debug("The resource {} from remote getResources call, is not found", uri);
			}
			
			
		}
		log.info("The following {} resources were found: {}", foundUris.size(), foundUris.toString());
		log.info("The answer contains {} resources", answer.size());

		return answer;
	}

	
	@Override
	public Set<String> checkLocalResources(Set<String> uris, String user) {
		log.info("I get a remote checkResources request for {} resources: {}", uris.size(),
				uris.toString());
		Set<String> foundUris = new HashSet<String>();
		Set<String> notFound = new HashSet<String>();
		for (String uri : uris)
		{
			Resource res = LocalDbCalls.getLocalResource(uri);
			if (res != null)
			{
				log.debug("The resource {} from remote getResources call, is found", uri);
				foundUris.add(uri);
			}
			else
			{
				log.debug("The resource {} from remote getResources call, is not found", uri);
				notFound.add(uri);
			}
			
			
		}
		NOVIUserImpl user1  = new NOVIUserImpl(user);
		log.info("The following {} resources were found: {}", foundUris.size(), foundUris.toString());
		log.info("The following {} resources were not found: {}", notFound.size(), notFound.toString());
		//Note: we actually need to pass sessionID to Policy instead of NULL
		Map<String, Boolean>  policyAnswer = PolicyServCommun.getAuthorizedResources(null,
				user1, foundUris, foundUris.size());
		Set<String> validRes = PolicyServCommun.getAuthResourcesFromMap(policyAnswer);

		return validRes;

	}
	
	@Override
	public boolean deleteLocalSlice(String sliceURI, NOVIUser user, 
			ReportEvent userFeedback, String sessionID) {
		log.info("I got a deleteLocalSlice call for slice {}", sliceURI);
		if (LocalDbCalls.checkSliceExist(sliceURI))
		{
			log.info("The slice is in this platform");
			boolean answ = DeleteSlice.deleteSlice(sliceURI, user, userFeedback, sessionID);
			if (answ == true)
			{
				log.info("The slice information was found and was deleted");
			}
			else
			{
				log.warn("The slice information was not found");
			}
			return answ;
		}
		else
		{
			log.warn("I can not find the slice {}", sliceURI);
			return false;
		}
		
	}

	

	///////////////////////////////////////////////
	/////GETTERS AND SETTERS///
	/////////////////////////////////////////////
	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}

	private String getMD5Sum(String text){
		String signature = null;
		try {
			   MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			   md5Digest.update(text.getBytes(),0,text.length());
			   signature = new BigInteger(1,md5Digest.digest()).toString(16);
			} catch (final NoSuchAlgorithmException e) {
			   e.printStackTrace();
		}
		
		return signature;
	}

	
	
}
