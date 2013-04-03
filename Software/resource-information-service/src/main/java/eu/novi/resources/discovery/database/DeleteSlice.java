package eu.novi.resources.discovery.database;

import java.util.Set;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;

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
 * It delete the slice from RIS DB and from the Testbeds. It also calls policy
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class DeleteSlice {

	private static final transient Logger log = 
			LoggerFactory.getLogger(DeleteSlice.class);




	/**delete the slice from the RIS DB and the testbeds.
	 * It call also policy to check the  user authorization
	 * @param sliceUri
	 * @param user the user that request the deletion. If it is null then it doesn't call policy
	 * @param userFeedback it can be null
	 * @param sessionID
	 * @return true if the deletion was succesful, otherwise false
	 */
	public static boolean deleteSlice(String sliceUri, NOVIUser user, ReportEvent userFeedback, String sessionID)
	{
		log.info("I am going to delete the slice {}", sliceUri);
		String st = "";
		

		if (user != null)
		{//check the user authorization
			log.info("Checking the user authorization: {}", user.toString());
			//get the owner user
			NOVIUserImpl ownerUser = NOVIUserClass.getNoviUserSlice(sliceUri);
			if (ownerUser == null)
			{
				st = "I can not find the owner user for the slice " + sliceUri;
				log.warn(st);
				if (userFeedback != null)
					userFeedback.errorEvent(sessionID, "RIS", 
							st, "http://fp7-novi.eu");
				return false;	
			}
			
			//check authorization
			if (PolicyServCommun.checkUserDelAuth(sessionID, (NOVIUserImpl)user, IMUtil.createSetWithOneValue(ownerUser)))
			{
				st = "The user " + user.toString() + " is authorized to delete the slice "
						+ sliceUri;
				log.info(st);
				if (userFeedback != null)
					userFeedback.instantInfo(sessionID, "RIS", 	st, "http://fp7-novi.eu");
			}
			else 
			{
				st = "The user " + user.toString() + " is not authorized to delete the slice "
						+ sliceUri;
				log.warn(st);
				if (userFeedback != null)
					userFeedback.errorEvent(sessionID, "RIS", 
							st, "http://fp7-novi.eu");
				return false;
			}
			
		}
		else
		{
			log.info("I don't have the user, I will not call policy for the authorization");
		}
		


		//call request handler
		st = "I will call Request Handler to delete the slice";
		log.info(st);
		if (userFeedback != null)
			userFeedback.instantInfo(sessionID, "RIS", 	st, "http://fp7-novi.eu");

		Set<String> platformURIs = ReserveSlice.getPlatformsFromSlice(sliceUri);
		if (platformURIs.size() == 0)
		{
			st = "I can not find the platforms for the slice " + sliceUri + ". I will not call request handler";
			log.warn(st);
			if (userFeedback != null)
				userFeedback.errorEvent(sessionID, "RIS", 
						st, "http://fp7-novi.eu");
			return false;
		}
		else
		{
			log.debug("The platforms from the slice {} are : {}", sliceUri, platformURIs.toString());


			log.info("calling the Request Handler for deleting slice");
			//get the slice description
			Reservation res = IRMLocalDbCalls.getLocalSlice(sliceUri);
			TopologyImpl top = new TopologyImpl(res.toString());
			top.setAutoUpdateOnfailure(res.getAutoUpdateOnfailure());
			top.setContains(res.getContains());
			boolean answer = TestbedCommunication.deleteSlice(sliceUri, platformURIs, top);

			if (!answer)
			{
				st = "Delete slice: The deletion to the testbed was failed";
				log.warn(st);
				if (userFeedback != null)
					userFeedback.errorEvent(sessionID, "RIS", 
							st, "http://fp7-novi.eu");
				return false;

			}

		}

		//delete the slice information in the RIS DB
		if (deleteLocalSliceInfo(sliceUri))
		{
			st = "The slice information was deleted succesfully from the  NOVI side";
			if (userFeedback != null)
				userFeedback.instantInfo(sessionID, "RIS", 
						st, "http://fp7-novi.eu");
			return true;

		}
		else
		{
			st = "The slice information was not found in the NOVI DB"+ 
					" or there is a problem on delete the slice info";
			log.warn(st);
			if (userFeedback != null)
				userFeedback.errorEvent(sessionID, "RIS", 
						st, "http://fp7-novi.eu");
			return false;

		}
		
	}

	
	
	/**delete all the stored local info for this slice.
	 * It deletes also the manifest info.
	 * It doesn't delete the slice from the testbeds
	 * @param uri the URI of the slice
	 * @return true if the slice was found and the info was deleted or 
	 * false if the slice was not found or a problem occur in the delete
	 */
	public static boolean deleteLocalSliceInfo(final String uri)
	{
		log.info("I will delete from the local NOVI DB the slice {}", uri);
		LocalDbCalls.printGetCurrentSlices();

		if (LocalDbCalls.execStatementReturnSum(NoviUris.createURI(uri), null, null) > 0)
		{

			//give the deleted slice info to policy
			///log.info("Giving the deleted slice info to policy");
			//Note: we actually need to pass sessionID to Policy instead of NULL
			PolicyServCommun.deleteSlicePolicy(null, IRMLocalDbCalls.getLocalSlice(uri), uri);

			ObjectConnection con = ConnectionClass.getNewConnection();
			try {

				log.info("Checking createURI to be deleted : {}", NoviUris.createURI(uri)); 
				con.clear(NoviUris.createURI(uri));
				con.clear(NoviUris.getSliceManifestContextUri(uri));
				con.commit();
				log.info("The slice {} was found in the DB and was deleted", uri);
				LocalDbCalls.printGetCurrentSlices();
				ConnectionClass.closeAConnection(con);
				return true;
			} catch (RepositoryException e) {
				log.warn("Problem in removing the slice {} from the DB", uri);
				ConnectionClass.logErrorStackToFile(e);
				ConnectionClass.closeAConnection(con);
				return false;
			}
		}
		else
		{
			//log.warn("The slice {} doesn't exist in the DB", uri);
			return false;
		}

	}

}
