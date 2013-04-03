package eu.novi.policysender.requestToIRM;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Resource;
import eu.novi.mapping.RemoteIRM;
import eu.novi.policysender.emailclient.SimpleSendEmail;
import eu.novi.resources.discovery.PolicyCalls;

public class RequestToIRM2 {
	private static RemoteIRM irmMapperInterface;
	private static PolicyCalls policyCallsToRIS;
	private static String theUser="yiannosk@gmail.com";
	private static final transient Logger log = 
			LoggerFactory.getLogger(RequestToIRM2.class);
	public static Collection<String> callUpdateSliceFP2(String currentTopology, Collection<String> failingResources ) {
		if (policyCallsToRIS==null)
		{
			log.warn("The object from IRM service is null");
			return null;
		}
		else
		{
			log.info("Policy Manager is sending the query to get the NOVIUser owner with the Slice that has a failure. The sliceURI is: " +currentTopology);
			theUser=policyCallsToRIS.getNoviUser(currentTopology);
			log.info("The Slice: "+ currentTopology+" belongs to User "+ theUser);
			//log.info("disabled the call to RIS");
			theUser="ykryftis@netmode.ece.ntua.gr";
		
		}
		
		if (irmMapperInterface == null)
		{
			log.warn("The object from (ponder2) IRM service is null");
			return null;
		}
		else
		{
			log.info("Policy Manager is sending the request to update slice");
			log.info("Testing Email First");
			String ResourcesText="";
			SimpleSendEmail sentToUser = new SimpleSendEmail();
			sentToUser.sentEmailWithREST(theUser,"From Policy Service at Update init");
		//	int emailres=sentToUser.SendEmail(theUser,"I am sending the email before the update");
		
		//	if (emailres==1)
	//		{
	//			log.info("Email Sent");
//			}
		//	else{
	//			log.info("Error about the email");
	//		}
			Collection<String> changedResources= new HashSet<String>();
			changedResources= irmMapperInterface.updateSlice("updateSession",currentTopology,  failingResources);
			log.info("PS called IRM and it returned");
			if (changedResources!=null)
			{
			Iterator it= changedResources.iterator();
			ResourcesText="The Resources that failed are:";
			while(it.hasNext())
			{
				String current=(String)it.next();
				System.out.println("Resource "+ current+" failed");
				log.info("Resource "+ current+" failed");
				ResourcesText+=current+" ";
			}
			}
			else 
			{
				log.info("IRM returned null!!!");
				ResourcesText="Problem in the update!!!";
			}
			log.info("Policy Manager is sending the email to inform the User");
//			SimpleSendEmail sentToUser = new SimpleSendEmail();
		  //  sentToUser.SendEmail(theUser,ResourcesText);
			//SimpleSendEmail.SendEmail(theUser, ResourcesText);
		//	log.info("The email should be sent");
			sentToUser.sentEmailWithREST(theUser,ResourcesText);
			return changedResources;
		    
		}
	}
	
	public RemoteIRM getIrmMapperInterface() {
		return irmMapperInterface;
	}

	public void setIrmMapperInterface(RemoteIRM irmMapperInterface) {
		this.irmMapperInterface = irmMapperInterface;
	}

	public PolicyCalls getPolicyCallsToRIS() {
		return policyCallsToRIS;
	}

	public void setPolicyCallsToRIS(PolicyCalls policyCallsToRIS) {
		this.policyCallsToRIS = policyCallsToRIS;
	}


}
