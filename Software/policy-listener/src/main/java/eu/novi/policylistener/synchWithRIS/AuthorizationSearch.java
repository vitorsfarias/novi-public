/**
 * 
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
 * 
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */
package eu.novi.policylistener.synchWithRIS;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.authentication.InterfaceForPS;
import eu.novi.connection.RESTCommunication;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Node;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.mail.mailclient.SSendEmail;
import eu.novi.policylistener.authentication.Basic;
import eu.novi.policylistener.interfaces.InterfaceForRIS;
import eu.novi.policylistener.ponder2comms.TelnetClient;

public class AuthorizationSearch implements InterfaceForRIS {
	String toforward;
	private static final transient Logger log = 
			LoggerFactory.getLogger(AuthorizationSearch.class);
	TelnetClient telclient= new TelnetClient();
	// Will be initialized from blue print
	ReportEvent userFeedback=null;
	private String testbed;
	
	private static InterfaceForPS authenticationComp;
	public static InterfaceForPS getauthenticationComp() {
		return authenticationComp;
	}
	public static void setauthenticationComp(InterfaceForPS authc) {
		AuthorizationSearch.authenticationComp = authc;
	}
	
	
	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}
	
	public void setTelnetClient(TelnetClient tel)
	{
		telclient=tel;
	}

	public void sentEmailWithREST(String receiver, String information){
		//String address="";
		//String failureEvent="Text for the User";
		try {
			HashMap<String, String> requestParameters = new HashMap<String, String>();
			requestParameters.put("receiver", receiver);
			requestParameters.put("information", information);
			new RESTCommunication().executePostMethod("http://localhost:8080/forthemail/TempHandler/sentEmailEvent",requestParameters);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String AuthorizedForResourced(String sessionID, String string1, String string2) {
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();

			userFeedback.instantInfo(sessionID, "Policy Manager AA", "Authorized for request from RIS", "http://www.fp7-novi.eu");
		}
		// TODO Auto-generated method stub
		String result = "AUTH";
		return result;
	}
	
	public int AddPolicyFile(String sessionID, String policyfile) {
		
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();
			userFeedback.instantInfo(sessionID,"Policy Manager AA", "Adding a new policy file (.p2) in the PolicyEngine", "http://www.fp7-novi.eu");
		}

		String tof="read "+ policyfile+"\r\n";
		//String tof="roleassigned create:#(\"a\" \"b\" \"c\").\r\n";
		log.info(tof);
		String re=  telclient.TelnetPonder1(tof);
		return 0;
	}

	public String ManualCallToPonder(String sessionID, String thecall){
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();
			userFeedback.instantInfo(sessionID,"Policy Manager AA", "Making a manual call to the PolicyEngine:" + thecall, "http://www.fp7-novi.eu");
		}
		System.out.println("The call is:"+thecall);
		String fcall=thecall+"\r\n";
		String got= telclient.TelnetPonder3(fcall);
		log.info(got);
		return got;
		
	}

	@Override
	public Map<String, Boolean> AuthorizedForResource(String sessionID, NOVIUserImpl noviUser, Set<String> Resources, Integer NumberOfRequestedResources) {
		//String User="User1";
		String UserRole="PlanetLabUser";
		//String UserRole="";
		String theRealUser=noviUser.toString();
		log.info("The NOVI User is: "+theRealUser);
		if (noviUser.getHasNoviRole() != null)
		{
			String UserRolecomplet=noviUser.getHasNoviRole().toString();
			log.info("WE GOT the NOVIRolecomlet: "+UserRolecomplet);
			UserRole= UserRolecomplet.substring(41);
			log.info("WE GOT the NOVIRole: "+UserRole);
		}
		//log.info("User has NOVIRole: "+noviUser.getHasNoviRole().toString());
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();
			userFeedback.instantInfo(sessionID,"Policy Manager AA", "Check Authorized Resources for user "+theRealUser+". It has NOVI Role "+ UserRole +". Request is for "+NumberOfRequestedResources+" Resources.", "http://www.fp7-novi.eu");
		}
		log.info("I got testbed "+testbed);
		if (testbed== null)
		{
			log.info("Testbed is null!!!");
		//	log.info("I will put PlanetLab");
		//	testbed="fake..PlanetLab";
		}
//		if(testbed.contains("PlanetLab"))
//		{
//			forquota="root/PlanetLab getquota.\r\n";
//		}
//		if(testbed.contains("FEDERICA"))
//		{
//			forquota="root/Federica getquota.\r\n";
//		}
		String forquota="root/"+testbed+" getquota.\r\n";
		log.info(forquota);
		String quota=  telclient.TelnetPonder2(forquota);
		log.info("The quota is:"+ quota);
		log.info("The request was for " + NumberOfRequestedResources+ " and based on the agreement and the mission policies we can get up to : " + quota);
		
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();
			userFeedback.instantInfo(sessionID, "Policy Manager AA", "Mission Policies: The request was for "+NumberOfRequestedResources+" and the quota is " + quota , "http://www.fp7-novi.eu");
		}
	
		Map<String, Boolean> authRes=new HashMap<String, Boolean>();
		Iterator it= Resources.iterator();
		while (it.hasNext())
		{
			//authRes.put(Resources.iterator().toString(), true);
			boolean result=true;
			String current=(String) it.next();
			String toforward="root/Rolesdomain/"+UserRole +" search: (root/Resources at: \""+current+"\").\r\n";
			log.info(toforward);
			String resultb=  telclient.TelnetPonder2(toforward);
			log.info(resultb);
			if (resultb.equals("false"))
				result=false;
			if (resultb.equals("true"))
				result=true;
			System.out.println(result);
			authRes.put(current,result);
			
		}
	    log.info("Print the Resources and their status:");
	    //Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	    String statusonAuth="";
	    for (Map.Entry<String, Boolean> entry : authRes.entrySet()) {
	        System.out.println("Resource = " + entry.getKey() + ", authorized = " + entry.getValue());
	        log.info("Resource = " + entry.getKey() + ", authorized = " + entry.getValue());
	        statusonAuth+="Resource = " + entry.getKey() + ", authorized = " + entry.getValue();
	    }
	    
	   
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();
			userFeedback.instantInfo(sessionID, "Policy Manager AA", "Authorized Resources " + statusonAuth , "http://www.fp7-novi.eu");
		}
		
	    return authRes;
		
	}


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


	@Override
	public int AddTopology(String sessionID, Group topo,String topoID) {
		if(userFeedback != null){
			if(sessionID == null)
				sessionID = userFeedback.getCurrentSessionID();	
			userFeedback.instantInfo(sessionID, "Policy Manager AA", "Adding the new topology in Policy Service (ponder2) ", "http://www.fp7-novi.eu");
		}
		
		String result="";
		String result2="";
		if (topo.getContains() ==null)
		{
			String message = "The topology is empty";
			log.warn(message);
			if(userFeedback != null){
				if(sessionID == null)
					sessionID = userFeedback.getCurrentSessionID();	
				userFeedback.instantInfo(sessionID, "Policy Manager AA", "We tryied to add an empty Topology in Policy Service (ponder2) ", "http://www.fp7-novi.eu");
			}
			
			log.info(message);
			return -2;
		}
		log.info("Not empty topology");
		Set<Resource> kati=topo.getContains();
		Iterator it= kati.iterator();
		System.out.println(topo.getContains().size());
		//System.out.println(it.toString());
		String createtopo="root/event/createTopology create: #(\""+topoID+"\").\r\n";
		System.out.println(createtopo);
		log.info(createtopo);
		String createtopo2="root/event/createRemoteTopology create: #(\""+topoID+"\").\r\n";
		System.out.println(createtopo2);
		log.info(createtopo2);
		result = telclient.TelnetPonder2(createtopo);
		System.out.println(result);
		log.info(result);
		result2 = telclient.TelnetPonder2(createtopo2);
		System.out.println(result2);
		log.info(result2);
		while(it.hasNext())
		{
			System.out.println("OK");
			Resource current=(Resource) it.next();
			System.out.println(current);
			log.info(current.toString());
			System.out.println(current.toString());
			if (current instanceof VirtualNode)
			{
				//TODO Lets call both platforms for Everything!!! will do it only in the proper platform soon
				//if (belong to PlanetLab)
				if (true)
				{
					//We have a VNode and so we have to see if it is local or remote
					//((VirtualNode) current).getImplementedBy();
					System.out.println("Printing the PhysicalNode: "+((VirtualNode) current).getImplementedBy());
					Iterator iter=((VirtualNode) current).getImplementedBy().iterator();
					while (iter.hasNext())
					{
						Node currentNode=(Node) iter.next();
						String cNode=currentNode.toString();
						System.out.println(cNode);
						System.out.println("The Physical Node contains the word federica? (true/false)"+cNode.contains("federica"));
						if (cNode.contains("federica"))
							{
							toforward="root/event/createRemoteVNode create: #(\""+topoID+current+"\" \""+ topoID+"\").\r\n";
							log.info("We have a remote VNode");
							}
						else
						{
							toforward="root/event/createVNode create: #(\""+topoID+current+"\" \""+ topoID+"\").\r\n";
							log.info("We have a local VNode");
						}
						System.out.println(toforward);
						log.info(toforward);
							
					}
				}
					
			//root/event/createVNode create: #("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr" "midtermWorkshopSlice").
			}
			//if (current instanceof VirtualLink)
			//{
			//	String toforward="root/event/createVLink create: #(\""+current+"\" \""+ topoID+"\").\r\n";
			//	System.out.println(toforward);
			//}
			else {
				System.out.println("No Vnode");
				toforward=".\r\n";
			}
				result=  telclient.TelnetPonder2(toforward);
				System.out.println(result);
				log.info(result);
			System.out.println(result);
			log.info("Policy Service is informed about the new Slice");
			
			
		}
		if(userFeedback != null)
			userFeedback.instantInfo(sessionID, "Policy Manager AA", "Finished addition of the new topology in Policy Service (ponder2) ", "http://www.fp7-novi.eu");
		
		return 0;
	}
	
	@Override
	public int RemoveTopology(String sessionID, Topology topo,String topoID) {
		String result="";
		/*if (topo.getContains().size() == 0)
		{
			String message = "The topology is empty";
			log.warn(message);
			return -2;
		}*/
		Set<Resource> kati=topo.getContains();
		Iterator it= kati.iterator();
		System.out.println(topo.getContains().size());
		//System.out.println(it.toString());
		String removetopo="root/event/removeTopology create: #(\""+topoID+"\").\r\n";
		System.out.println(removetopo);
		result = telclient.TelnetPonder2(removetopo);
		System.out.println(result);
		while(it.hasNext())
		{
			System.out.println("OK");
			Resource current=(Resource) it.next();
			System.out.println(current);
			System.out.println(current.toString());
			if (current instanceof VirtualNode)
			{
				toforward="root/event/removeVNode create: #(\""+topoID+current+"\" \""+ topoID+"\").\r\n";
				System.out.println(toforward);
			//root/event/createVNode create: #("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr" "midtermWorkshopSlice").
			}
			//if (current instanceof VirtualLink)
			//{
			//	String toforward="root/event/createVLink create: #(\""+current+"\" \""+ topoID+"\").\r\n";
			//	System.out.println(toforward);
			//}
			else {
				System.out.println("No Vnode");
				toforward=".\r\n";
			}
				result=  telclient.TelnetPonder2(toforward);
				System.out.println(result);
			System.out.println(result);
			
			
		}
		return 0;
	}

	@Override
	public Boolean AuthorizedForDeletion(String sessionID, NOVIUserImpl caller,
			Set<NOVIUserImpl> owners) {
		log.info("The caller is "+ caller.toString());
		Iterator usersiter= owners.iterator();
		while(usersiter.hasNext())
		{
			NOVIUserImpl current=(NOVIUserImpl) usersiter.next();
			log.info(current.toString());
			if (caller.toString().equals(current.toString()))
			{
				log.info(caller.toString()+ " equals to " + current.toString());
				return true;
			}
		}
		log.info(caller.toString()+ " does not equals to any of the owners");
		
		if (caller.getHasNoviRole().toString().equals("http://fp7-novi.eu/NOVIPolicyService.owl#PlanetLabPI"))
			{
			log.info("The User has Role "+ caller.getHasNoviRole().toString());	
			log.info("The User is PI");
				log.info("It belongs to domain(site) "+caller.getBelogsToDomain());
				Iterator usersite= owners.iterator();
				while(usersite.hasNext())
				{
					NOVIUserImpl current=(NOVIUserImpl) usersite.next();
					log.info("The owner "+ current.toString()+ " belongs to domain " +current.getBelogsToDomain());
					if(current.getBelogsToDomain().equals(caller.getBelogsToDomain()))
					{
						log.info("The request came from the PI of the domain!!! return TRUE");
						return true;
					}
				}
				
			}
		return false;
	}

	int initMissions()
	{
		String sent1="";
		//String sent2="";
		if (testbed!=null)
		{
		if (testbed.contains("PlanetLab"))
		{
			//sent1="fedeint :=(root import:\"root/role/PlanetLabUser/interface\" from:\"rmi://194.132.52.214:1113/Ponder2FED\").\r\n";
		//	sent2="root/event/newsmc create:#(\"FedericaUser\" fedeint).\r\n";
			sent1="root/event/newsmc create:#(\"FedericaUser\" (root import:\"root/role/PlanetLabUser/interface\" from:\"rmi://194.132.52.214:1113/Ponder2FED\")).\r\n";
		}
		if (testbed.contains("FEDERICA"))
		{
			//sent1="plint :=(root import:\"root/role/FedericaUser/interface\" from:\"rmi://150.254.160.28:1113/Ponder2PL\").\r\n";
		//	sent2="root/event/newsmc create:#(\"PlanetLabUser\" plint).\r\n";
			sent1="root/event/newsmc create:#(\"PlanetLabUser\" (root import:\"root/role/FedericaUser/interface\" from:\"rmi://150.254.160.28:1113/Ponder2PL\")).\r\n";
		}
		}
		log.info("To call :"+sent1);
	//	log.info("To call :"+sent2);
		String result1 = telclient.TelnetPonder2(sent1);
		//String result2 = telclient.TelnetPonder2(sent2);
		log.info("We tried to init the missions. The result is: "+result1);
		return 0;
	}
	
	@Override
	public int AddAllTopologies(String sessionID, Set<Reservation> slices) {
		try {
			log.info("Sleeping for 10 seconds to give time for the init of Missions");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.initMissions();
		//add time delay
		try {
			log.info("Sleeping for 10 seconds to give time for the init of Missions");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(slices!=null)
		{
		Iterator<Reservation> iter = slices.iterator();
		while (iter.hasNext())
		{
			Reservation curr=iter.next();
			//curr.getHasLifetimes();
			//	curr.getContains().
			
			log.info("Topology = " + curr.toString() + ", to be added ");
			//TopologyImpl currtopo= (TopologyImpl) curr;
		    this.AddTopology(sessionID, curr, curr.toString());
			
		}
		}
		//for (Map.Entry<String, Topology> entry : topologies.entrySet()) {
	     //   System.out.println("Topology = " + entry.getKey() + ", to be added = " + entry.getValue());
	      //  log.info("Topology = " + entry.getKey() + ", to be added = " + entry.getValue());
	      //  this.AddTopology(sessionID, entry.getValue(),entry.getKey());
	       // log.info("OK");
	    //}
		return 0;
	}

	@Override
	public int InformExpirationTime(NOVIUserImpl user, String topoID, Date date) {
		// Sent email to the user with the sliceID and the information that the slice will be deleted in 48 hours
		String receiver="";
		if (user!=null)
		{
			log.info("The User has User name "+user.toString());
			receiver=user.toString().substring(41);
			log.info("So the email address :"+receiver);
		}
		else 
		{
			return 1;
		}
		String slice = "none";
		if (topoID!=null)
		{
			slice=topoID;
		}
		String dates="";
		if (date!=null)
		{
			dates=date.toString();
		}
		log.info("I am sending email to "+ receiver+ "about slice "+ slice+ " for date "+ dates);
		String information ="Your slice " + slice+ " is about to expire on "+ dates;
		// Sent email to the User with the sliceID and the info that it was removed
		this.sentEmailWithREST(receiver, information);
		return 0;
	}

	@Override
	public int InformExpirationHappened(NOVIUserImpl user, String topoID,Date date) {
		// RIS called the delete slice
		String receiver="";
		if (user!=null)
		{
			log.info("The User has User name "+user.toString());
			receiver=user.toString().substring(41);
			log.info("So the email address :"+receiver);
		}
		else 
		{
			return 1;
		}
		String slice = "none";
		if (topoID!=null)
		{
			slice=topoID;
		}
		log.info("I am sending email to "+ receiver+ "about slice "+ slice);
		String information ="Your slice " + slice+ " has expired";
		this.sentEmailWithREST(receiver, information);
		// Sent email to the User with the sliceID and the info that it was removed
		return 0;
	}

	@Override
	public int UpdateExpirationTime(NOVIUserImpl user, String topoID, Date date) {
		//Call authorizeOndelete to see if the user can update the expiration time
		//call the myplc function that performs the update
		String receiver="";
		if (user!=null)
		{
			log.info("The User has User name "+user.toString());
			receiver=user.toString().substring(41);
			log.info("So the email address :"+receiver);
		}
		else 
		{
			return 1;
		}
		String slice = "none";
		if (topoID!=null)
		{
			slice=topoID;
		}
		//Expiration ex=new Expiration();
		int r=-1;
		if (authenticationComp!=null)
		{
		try {
			
				r=authenticationComp.updateExpirationTime(user, topoID, date);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		else {log.info("Authentication Component is null");}
		//sent email to the User with sliceId the the new date that the expiration will happen
		log.info("I am sending email to "+ receiver+ "about slice "+ slice);
		String information ="Your slice " + slice+ " is updated";
		this.sentEmailWithREST(receiver, information);
		
		
		return r;
	}

}
