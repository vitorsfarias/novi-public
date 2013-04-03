package eu.novi.resources.discovery.database.communic;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Reservation;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.resources.discovery.response.FPartCostTestbedResponse;





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
 * the communication with the policy service
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class PolicyServCommun {

	private static final transient Logger log = 
			LoggerFactory.getLogger(PolicyServCommun.class);
	
	private static InterfaceForRIS policyServiceCalls;



	/**
	 * contact policy service and get the authorized resources in find resources function
	 * @param user the NOVI User
	 * @param resources the set of resources
	 * @param reqResNumb the number of virtual resources that the user requested
	 * @return
	 */
	public static Map<String, Boolean> getAuthorizedResources(String sessionID, NOVIUser user, Set<String> resources,
			int reqResNumb)
	{
		Map<String, Boolean> results;

		if (policyServiceCalls == null)
		{
			results = new HashMap<String, Boolean>();
			log.warn("The object from policy service is null,  I will not call policy");
			for (String r : resources)
			{

				results.put(r, true);

			}
		}
		else
		{
			log.info("Calling policy manager to get the authorized resources for {}", resources.toString());
			results = policyServiceCalls.AuthorizedForResource(sessionID, ((NOVIUserImpl)user), resources,
					new Integer(reqResNumb));
			if (results == null)
			{
				log.warn("The response from policy is null");
			}
			
		}

		return results;
	}
	
	/**send the information about the new slice to policy service
	 * @param topology the bound topology
	 * @param SliceURI the URI of the slice
	 */
	public static void sendNewSliceInfo(String sessionID, Topology topology, String SliceURI)
	{
		
		if (topology.getContains() == null)
		{
			log.warn("The topology to be sent to policy is empty. I will not send it");
			return ;
		}
		

		if (policyServiceCalls != null)
		{
			log.info("Contacting policy service to send the new objects...");
			policyServiceCalls.AddTopology(sessionID, topology, SliceURI);
		}
		else
		{
			log.warn("sendNewSliceInfo : the policy service object is null. " +
					"I will not send the information");
		}
		
	}
	
	
	/**it sends to policy all the slices in the RIS DB
	 * @param slices
	 */
	public static void sendAllSlices(Set<Reservation> slices)
	{
		if (slices == null)
		{
			log.warn("The slices to be sent to policy is null. I will not send it");
			return ;
		}
		//if there is no slice, then send an empty list

		if (policyServiceCalls != null)
		{
			log.info("Contacting policy service to send all the slices...");
			policyServiceCalls.AddAllTopologies(null, slices);
			//TODO contact policy
		}
		else
		{
			log.warn("sendAllSlices : the policy service object is null. " +
					"I will not send the information");
		}
		
	}
	
	
	/**check if the user is authorize to delete a slice
	 * @param userRequest the user that make the delete request
	 * @param owners the owners of the slice
	 * @return true if the user is authorize to delete the slice, otherwise false
	 */
	public static boolean checkUserDelAuth(String sessionID, NOVIUserImpl userRequest, Set<NOVIUserImpl> owners)
	{
		
		if (policyServiceCalls != null)
		{
			log.info("Contacting policy service to ask for user delete slice authorization");
			return policyServiceCalls.AuthorizedForDeletion(sessionID, userRequest, owners);
			
		}
		else
		{
			log.warn("checkUserDelAuth : the policy service object is null. " +
					"I will just check if the user is the owner");
			
			for (NOVIUserImpl us: owners)
			{
				if (us.toString().equals(userRequest.toString()))
				{
					log.info("The request user is the owner, therefore I will procced with the delete slice");
					return true;
					
				}
				
			}
			//the request user is not in the owner list
			log.info("The request user is not the owner, therefore I will not procced with the delete slice");
			return false;
		}
	
	}
	
	
	/**
	 * send the deleted slice info to policy service 
	 * @param sliceTopology the slice topology that will be deleted
	 * @param sliceURI the URI of the slice
	 */
	public static void deleteSlicePolicy(String sessionID, Reservation sliceTopology, String sliceURI)
	{
		if (sliceTopology == null)
		{
			throw new IllegalArgumentException("The given topology is null");
		}
		else if (sliceURI == null)
		{
			throw new IllegalArgumentException("The slice URI is null");
		}
		
		if (policyServiceCalls != null)
		{
			Topology top = new TopologyImpl(sliceURI);
			top.setContains(sliceTopology.getContains());
			sliceTopology.setContains(null);
			log.info("Contacting policy service to delete a slice...");
			policyServiceCalls.RemoveTopology(sessionID, top, sliceURI);
		}
		else
		{
			log.warn("deleteSlicePolicy : the policy service object is null. " +
					"I will not send the information");
		}

	}
	
	
	
	/**
	 * it get the partitioning cost for all the remote testbeds.
	 * Policy service call all the remote RIS and get that information
	 * @param requestedTopology
	 * @return
	 */
	public static Vector<FPartCostTestbedResponse> getRemotePartitioningCosts(
			TopologyImpl requestedTopology) {



		Vector<FPartCostTestbedResponse> results = 
				new Vector<FPartCostTestbedResponse>();
		if (policyServiceCalls == null)
		{
			log.warn("Problem to get the remote partitioning costs." +
					" The policy service reference is null");
			return null;
		}
		else
		{
			
			IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
			String st = imRepo.exportIMObjectToString(requestedTopology);
			if (st == null)
			{
				log.warn("findRemotePartitioningCost: I can not get the xml/rdf string from the Topology {}",
						requestedTopology.toString());
			}//TODO enable this
			/*else
			{
				results.add(policyServiceCalls.findRemotePartitioningCost(st));
			}*/

		}


		return results;
	}
	
	
	/**return the authorized resources from the map
	 * @param map the Map contains the string and boolean
	 * @return the valid resources (has true)
	 */
	public static Set<String> getAuthResourcesFromMap(Map<String, Boolean> map)
	{
		Set<String> validRes = new HashSet<String>();
		Iterator<Entry<String, Boolean>> it = map.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Boolean> pair = it.next();
			if (pair.getValue() == true)
			{
				log.info("The resources {} is authorized", pair.getKey());
				validRes.add(pair.getKey());
			}
			else
			{
				log.info("The resources {} is not authorized ", pair.getKey());
			}
		}
		return validRes;
	}
	
	
	/**call policy for a slice that expired
	 * @param user
	 * @param sliceURI
	 * @param date
	 */
	public static void call4SliceExpiration(NOVIUserImpl user, String sliceURI, Date date)
	{
		log.info("Calling policy to inform about the expired slice {}", sliceURI);
		policyServiceCalls.InformExpirationHappened(user, sliceURI, date);
	}
	
	
	/**call policy to renew the slice
	 * @param user
	 * @param sliceURI
	 * @param date
	 */
	public static void call4SliceRenew(NOVIUserImpl user, String sliceURI, Date date)
	{
		log.info("Calling policy to renew the slice {}, new date: {}", sliceURI, date.toString());
		policyServiceCalls.UpdateExpirationTime(user, sliceURI, date);
	}
	
	/**call policy to notify that a slice is expiring soon
	 * @param user
	 * @param sliceURI
	 * @param date
	 */
	public static void call4SliceExpirationNotif(NOVIUserImpl user, String sliceURI, Date date)
	{
		log.info("Calling policy to inform that the slice {} is expiring soon", sliceURI);
		policyServiceCalls.InformExpirationTime(user, sliceURI, date);
	}
	

	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/
	
	public InterfaceForRIS getPolicyServiceCalls() {
		return policyServiceCalls;
	}

	public void setPolicyServiceCalls(InterfaceForRIS policyServiceCalls) {
		PolicyServCommun.policyServiceCalls = policyServiceCalls;
	}
	
	public static void setPolicyServiceCallsStatic(InterfaceForRIS policyServiceCalls) {
		PolicyServCommun.policyServiceCalls = policyServiceCalls;
	}

}
