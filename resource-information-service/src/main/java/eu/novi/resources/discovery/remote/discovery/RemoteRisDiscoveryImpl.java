package eu.novi.resources.discovery.remote.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.resources.discovery.database.LockSession;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.NoviRisValues;
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
 * The implementation class of the RemoteRisDiscovery
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class RemoteRisDiscoveryImpl implements RemoteRisDiscovery {

	private static final transient Logger log = 
			LoggerFactory.getLogger(RemoteRisDiscoveryImpl.class);

	//private RemoteRisServe remoteRis;
	private String testbed; //get from constructor
	// static final String REMOTE_RIS_SERVICE = 
	//	"eu.novi.resources.discovery.remote.serve.RemoteRisServe";

	//private RemoteRisServe remoteRisServe; //this is for internal JUnit testing


	static List<RemoteRisServe> remoteRISList = new ArrayList<RemoteRisServe>();

	public RemoteRisDiscoveryImpl(){

		this.testbed = Testbeds.takeCurrentTestbed();

	}


	/**
	 * @param testbed the local testbed that the RIS is running.
	 */
	public RemoteRisDiscoveryImpl(String testbed)
	{
		this.testbed = testbed;
	}

	@Override
	public void lockUnlockRemoteResources(final LockSession lockSession,
			final boolean isLock) {
		log.info("I am going to " + ((isLock) ? "lock" : "unlock") + " the remote resources for" +
				" the slice ID {}", lockSession.getSessionID());
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not lock/unlock the remote resources. " +
					"Remore RIS serve is null");
			return;
		}

		List<Future<Boolean>>  answers = new Vector<Future<Boolean>>();
		//create n-1 threads to execute the remote RISs. 
		for (int i = 0; i < remoteRis.size() - 1; i++)
		{
			final RemoteRisServe remoteF = remoteRis.get(i);
			answers.add(PeriodicUpdate.executeNewThread(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					remoteF.lockUnlockLocalResources(lockSession, isLock);
					return true;
				}
			}));

		}
		//use the current thread to execute the last remote RIS
		remoteRis.get(remoteRis.size() - 1).lockUnlockLocalResources(lockSession, isLock);
		if (!answers.isEmpty())
			log.info("Now I will wait for the threads answers");
		for (Future<Boolean> answ : answers)
		{
			try {
				answ.get();
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			}


		}
		log.info("The lock/unlock was done");

	}

	@Override
	public void storeRemoteSlice(ReservationImpl slice)
	{
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not store the information about slice {} to the remote platform. " +
					"Remore RIS serve is null", slice.toString());
			return;
		}
		log.debug("Translating the slice information to string format...");
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String sliceXmlRdf = repositoryUtil.exportIMObjectToString(slice);
		log.info("Sending the slice information to remote testbed");
		remoteRis.get(0).storeLocallyRemoteSliceInfo(sliceXmlRdf, testbed);
		log.info("The slice information was sent");

	}

	@Override
	public Reservation getRemoteSlice(final String uri)
	{
		log.info("I will contact the remote RISs to get the slice {}", uri);
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("Remote RIS not available. I can not get the information about slice " +
					"{} from the the remote platform",
					uri);
			return null;
		}
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		//////
		List<Future<String>>  answers = new Vector<Future<String>>();
		//create n-1 threads to execute the remote RISs. 
		for (int i = 0; i < remoteRis.size() - 1; i++)
		{
			final RemoteRisServe remoteF = remoteRis.get(i);
			answers.add(PeriodicUpdate.executeNewThread(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return remoteF.getLocalSlice(uri, false);
				}
			}));

		}
		//use the current thread to execute the last remote RIS
		String remSlice = remoteRis.get(remoteRis.size() - 1).getLocalSlice(uri, false);
		if (remSlice != null)
		{
			log.info("I found the remote slice");
			Set<Reservation> objects = repositoryUtil.getIMObjectsFromString(
					remSlice, Reservation.class);
			if (objects != null && objects.size() > 0)
			{
				return objects.iterator().next();
			}
			else
			{
				log.warn("I did not find reservation object in the slice");
				return null;
			}

		}

		if (!answers.isEmpty())
			log.info("Now I will wait for the threads answers");
		for (Future<String> answ : answers)
		{
			try {
				String slice = answ.get();
				if (slice != null)
				{
					log.info("I found the remote slice");
					Set<Reservation> objects = repositoryUtil.getIMObjectsFromString(
							remSlice, Reservation.class);
					if (objects != null && objects.size() > 0)
					{
						return objects.iterator().next();
					}
					else
					{
						log.warn("I did not find reservation object in the slice");
						return null;
					}
				}
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			}


		}

		log.warn("I did not find the slice {}", uri);
		return null;
	}



	@Override
	public String getRemoteSliceOwl(final String uri) {
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("Remote RIS not available. I can not get the rdf information about slice " +
					"{} from the the remote platform",
					uri);
			return null;
		}
		/////
		List<Future<String>>  answers = new Vector<Future<String>>();
		//create n-1 threads to execute the remote RISs. 
		for (int i = 0; i < remoteRis.size() - 1; i++)
		{
			final RemoteRisServe remoteF = remoteRis.get(i);
			answers.add(PeriodicUpdate.executeNewThread(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return remoteF.getLocalSlice(uri, true);
				}
			}));

		}
		//use the current thread to execute the last remote RIS
		String remSlice = remoteRis.get(remoteRis.size() - 1).getLocalSlice(uri, true);
		if (remSlice != null)
		{
			log.info("I found the remote slice");
			return remSlice;

		}

		if (!answers.isEmpty())
			log.info("Now I will wait for the threads answers");
		for (Future<String> answ : answers)
		{
			try {
				remSlice = answ.get();
				if (remSlice != null)
				{
					log.info("I found the remote slice");
					return remSlice;
				}
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			}


		}

		log.warn("I did not find the slice {}", uri);
		return null;
	}



	@Override
	public Vector<FPartCostTestbedResponseImpl> findRemotePartitioningCost(
			TopologyImpl requestedTopology) {


		log.info("Calling remote ris for partitioning cost");
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		Vector<FPartCostTestbedResponseImpl> results = 
				new Vector<FPartCostTestbedResponseImpl>();
		if (remoteRis.isEmpty())
		{
			log.warn("Problem to get the remote partitioning costs, the remote ris is null");
			return results;
		}

		//federica or planetlab
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		final String st = imRepo.exportIMObjectToString(requestedTopology);
		if (st == null)
		{
			log.warn("findRemotePartitioningCost: I can not get the xml/rdf string from the Topology {}",
					requestedTopology.toString());
			return results;
		}
		///////call all the remote platforms
		int size = remoteRis.size();
		final int[] counter = new int[size];
		for (int i = 0; i<counter.length; i++)
		{
			counter[i] = 0;
			
		}
		final String WAITING_FLAG = "WAIT_FOR_REMOTE_PARTITIONING";

		
		final String[] currentFlag  = new String[size]; 
		for (int i =0; i<size; i++)
		{
			currentFlag[i] = remoteRis.get(i).giveLocalPartitioningCost(st).getTestbedURI();;
		}
		
		List<Future<FPartCostTestbedResponseImpl>>  answers = 
				new Vector<Future<FPartCostTestbedResponseImpl>>();
		//create n-1 threads to execute the remote RISs. 
		for (int j = 0; j < remoteRis.size() - 1; j++)
		{
			final int i = j;
			final RemoteRisServe remoteF = remoteRis.get(i);
			answers.add(PeriodicUpdate.executeNewThread(new Callable<FPartCostTestbedResponseImpl>() {

				@Override
				public FPartCostTestbedResponseImpl call() throws Exception {
					while(counter[i] < 100 && currentFlag[i].equals(WAITING_FLAG)){
						try {
							Thread.sleep(3000);
							counter[i] ++;
							currentFlag[i]  = remoteF.giveLocalPartitioningCost(st).getTestbedURI();

						} catch (InterruptedException e) {

							e.printStackTrace();
						}
					}

					if(!currentFlag[i].equals(WAITING_FLAG))
					{
						log.info("I got the answer from the a remote ris about partitioning cost");
						return remoteF.giveLocalPartitioningCost(st);
					}
					else {
						log.warn("Attempted 100 times 3 secs to get remote partitioning cost, giving up");
						return null;
					}

					

				}
			}));

		}
		
		//use the current thread to execute the last remote RIS
		while(counter[size - 1] < 100 && currentFlag[size - 1].equals(WAITING_FLAG)){
			try {
				Thread.sleep(3000);
				counter[size - 1] ++;
				currentFlag[size - 1]  = remoteRis.get(size - 1).
						giveLocalPartitioningCost(st).getTestbedURI();

			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		if(!currentFlag[size - 1].equals(WAITING_FLAG))
		{
			log.info("I got the answer from the a remote ris about partitioning cost");
			results.add(remoteRis.get(remoteRis.size() - 1).giveLocalPartitioningCost(st));
		}
		else {
			log.warn("Attempted 100 times 3 secs to get remote partitioning cost, giving up");
		}
		
		
		if (!answers.isEmpty())
			log.info("Now I will wait for the threads answers");
		for (Future<FPartCostTestbedResponseImpl> answ : answers)
		{
			try {
				FPartCostTestbedResponseImpl returnObject = answ.get();
				if (returnObject != null)
				{
					results.add(returnObject);
				}
				
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
				log.warn("Problem to read the answer from a thread");
			}


		}

		return results;
		

		
	}



	@Override
	public ResourceImpl getRemoteResource(String uri) {
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not get the resource {} from the the remote platform",
					uri);
			return null;
		}
		
		
		for (RemoteRisServe remote : remoteRis)
		{
			String rdfString = remote.getLocalResource(uri);
			if (rdfString != null)
			{
				log.info("I found the remote resources {}", uri);
				IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
				return (ResourceImpl) imRepo.getIMObjectFromString(rdfString, Resource.class, uri);
			}

			
		}
		log.warn("getRemoteResource: I did not find the resource {}", uri);
		return null;
		
	}

	
	@Override
	public Set<ResourceImpl> getRemoteResources(Set<String> uris) {
		log.info("GetRemoteResource call for {} resources : {}", uris.size(), uris.toString());
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not get the resources {} from the the remote platform. " +
					"Remote ris service is unavailable", uris.toString());
			return null;
		}

		///
		Set<String> rdfStrings = new HashSet<String>(); 
		for (RemoteRisServe remote : remoteRis)
		{
			Set<String> testbedRdfStrings = remote.getLocalResources(uris);
			if (testbedRdfStrings == null)
			{
				log.warn("getRemoteResources: I got a null xml/rdf string from a remote testbed");
				
			}
			else
			{
				rdfStrings.addAll(testbedRdfStrings);
			}
			
			
		}
		///////
		if (rdfStrings.isEmpty())
		{
			log.warn("I did not find any remote resources");
		}
		
		log.info("I got {} resources back. Therefore {} resource were not found.",
				rdfStrings.size(), uris.size() - rdfStrings.size());
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		Set<ResourceImpl> answer = new HashSet<ResourceImpl>();
		Set<String> notReturnUris = new HashSet<String>();
		for(String s: uris)
		{//copy initial set of URIs
			notReturnUris.add(s);
		}

		for (String rdfSt : rdfStrings)
		{//for each returned resource

			for (String uri : notReturnUris)
			{//check which resource is exactly
				ResourceImpl retRes = (ResourceImpl) 
						imRepo.getIMObjectFromString(rdfSt, Resource.class, uri);
				if (retRes != null)
				{
					log.info("The resource {} was found remotely", uri);
					answer.add(retRes);
					notReturnUris.remove(uri);
					break;
				}
			}


		}
		if (notReturnUris.size() > 0 )
		{
			log.warn("The following {} resources was not found remotely: {}",
					notReturnUris.size(), notReturnUris.toString());
		}
		return answer;
	}



	@Override
	public Set<String> checkRemoteResources(Set<String> uris, String user) {
		log.info("checkRemoteResource call for {} resources : {}", uris.size(), uris.toString());
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not check the resources {} from the the remote platform. " +
					"Remote ris service is unavailable", uris.toString());
			return null;
		}
		
		Set<String> foundURIs = new HashSet<String>();
		for (RemoteRisServe remote : remoteRis)
		{
			Set<String> foundURIsTestbed = remote.checkLocalResources(uris, user);
			if (foundURIsTestbed == null)
			{
				log.warn("checkRemoteResources: I got a null Set from remote testbed");
			}
			else
			{
				foundURIs.addAll(foundURIsTestbed);
			}
			
		}
		
		
		
		
		//////////
		log.info("I got {} resources back. Therefore {} resource were not found or are not authorized.",
				foundURIs.size(), uris.size() - foundURIs.size());
		log.info("The following {} resources were found remotely {}", foundURIs.size(),
				foundURIs.toString());

		Set<String> notFound = new HashSet<String>();
		//copy initial list
		for(String s: uris)
		{
			notFound.add(s);
		}
		//remove the found resources
		notFound.removeAll(foundURIs);
		log.info("The following {} resources were not found remotely {}", notFound.size(),
				notFound.toString());
		return foundURIs;
	}
	

	@Override
	public boolean deleteRemoteSlice(String sliceURI, NOVIUser user, 
			ReportEvent userFeedback, String sessionID) {

		log.info("deleteRemoteSlice call for {}", sliceURI);
		List<RemoteRisServe> remoteRis = getRemoteRisServe();
		if (remoteRis.isEmpty())
		{
			log.warn("I can not delete the slice {} from the the remote platform. " +
					"Remote ris service is unavailable", sliceURI);
			return false;
		}

		List<Boolean> answers = new Vector<Boolean>();
		for (RemoteRisServe remote : remoteRis)
		{
			answers.add(remote.deleteLocalSlice(sliceURI, user, userFeedback, sessionID));
		}
		
		for (Boolean answ : answers)
		{
			if (answ != null && answ == true)
			{
				log.info("The slice information was deleted from a remote platform and from the testbeds");
				return true;
			}
		}
		log.warn("The slice information was not found in any remote platform");
		return false;

	}



	/**it return the remote ris serve. If the variable was not set by the
	 * setRemoteRisServe then it creates it from service mix
	 * @return the RemoteRisServe service or an empty list if a problem occur 
	 */
	public List<RemoteRisServe> getRemoteRisServe() {

		// This is assuming only one remote, when we're scaling up, think about what we actually want
		// We need to process the whole list with exception of local RIS.
		List<RemoteRisServe> remoteRis = new Vector<RemoteRisServe>();
		if(remoteRISList != null){
			for(RemoteRisServe remoteRIS : remoteRISList) {
				if (remoteRIS != null){
					if(!remoteRIS.getTestbed().equals(testbed)){
						remoteRis.add(remoteRIS);
					}
				}	
			}	
		}
		if (remoteRis.isEmpty())
		{
			log.warn("The remoteRisList does not contain RemoteRIS on other testbed. Getting RemoteRis from bundle context");
			return getRemoteRisServeFromBundleContext();
		}
		else
		{
			log.info("Where found {} remote RISs", remoteRis.size());
			return remoteRis;
		}



	}




	/**
	 * @return the list of remote RISs or an empty list if none were found
	 */
	private List<RemoteRisServe> getRemoteRisServeFromBundleContext() {
		List<RemoteRisServe> remoteRis = new Vector<RemoteRisServe>();
		try {
			Bundle bundle = null;
			bundle = FrameworkUtil.getBundle(RemoteRisDiscoveryImpl.class);
			BundleContext ctx =  bundle.getBundleContext();
			ctx.getServiceReferences(null, null); //just to work around
			ServiceReference [] remoteRisReferences = ctx.getServiceReferences(
					RemoteRisServe.class.getName(), null);
			if(remoteRisReferences == null || remoteRisReferences.length == 0)
			{
				log.error("Cannot get RemoteRisServe from bundle context. ServiceReferences is null or empty");
				return remoteRis;
			}
			else
			{
				for(int i = 0; i < remoteRisReferences.length; i++)
				{
					ServiceReference serviceReference = (ServiceReference)remoteRisReferences[i];
					RemoteRisServe remoteRisService =(RemoteRisServe) ctx.getService(serviceReference);
					if( !remoteRisService.getTestbed().equals(this.getTestbed()) )
					{
						remoteRis.add(remoteRisService);
						//return remoteRisService;
					}
				}

			}
		} catch (NoClassDefFoundError e1) {
			log.error("Problem to get the bundle of class: "+RemoteRisDiscoveryImpl.class.getName());
			e1.printStackTrace();
			return remoteRis;
		} catch (InvalidSyntaxException e) {
			log.error("Problem to get service reference from context");
			e.printStackTrace();
			return remoteRis;
		}
		if (remoteRis.isEmpty())
		{
			log.error("Cannot get RemoteRisServe. There is no service on another testbed");

		}
		else
		{
			log.info("Where found {} remote RISs", remoteRis.size());

		}
		return remoteRis;
	}



	/**for junit testing
	 * @param remoteRisServe
	 */
	/*public void setRemoteRisServe(RemoteRisServe remoteRisServe) {
		this.remoteRisServe = remoteRisServe;
	}*/

	@Override
	public void setTestbed(String testbed) {
		// TODO Auto-generated method stub
		this.testbed = testbed;
	}

	@Override
	public String getTestbed() {
		// TODO Auto-generated method stub
		return testbed;
	}

	@Override
	public List<RemoteRisServe> getRemoteRISList() {
		return remoteRISList;
	}

	@Override
	public void setRemoteRISList(List<RemoteRisServe> remoteRISList) {
		this.remoteRISList = remoteRISList;
	}

	public static void staticSetRemoteRISList(List<RemoteRisServe> remoteRISList) {
		RemoteRisDiscoveryImpl.remoteRISList = remoteRISList;
	}

	@Override
	public List<String> getRemoteRisServeTestbed() {
		List<RemoteRisServe> remoteRIS = getRemoteRisServe();
		List<String> testbeds = new Vector<String>();
		for (RemoteRisServe remote : remoteRIS)
		{
			testbeds.add(remote.getTestbed());
		}
		return testbeds;
	}

}
