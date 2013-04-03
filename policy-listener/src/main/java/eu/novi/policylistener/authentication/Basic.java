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
package eu.novi.policylistener.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;	 
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.policylistener.interfaces.InterfaceForAPI;
import eu.novi.authentication.InterfaceForPS;
import eu.novi.policylistener.synchWithRIS.AuthorizationSearch;

public class Basic implements InterfaceForAPI {
	private static final transient Logger log = 
			LoggerFactory.getLogger(Basic.class);
	private String testbed;
	private static InterfaceForPS authenticationComp;
	public String getTestbed() {
		return testbed;
	}
	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}
	public static InterfaceForPS getauthenticationComp() {
		return authenticationComp;
	}
	public static void setauthenticationComp(InterfaceForPS authc) {
		Basic.authenticationComp = authc;
	}
	
	static ReportEvent userFeedback=null;
	 
	    public NOVIUserImpl getAuth(String username, String password) throws Exception {
	    	NOVIUserImpl theUser=null;
	    	if (authenticationComp!=null)
	       {
	    	theUser= authenticationComp.getAuth(username,password);
	        log.info("The User "+username + "returned the NOVIUser object with the session key "+ theUser.getHasSessionKey());
	        System.out.println("Finished.");
	        }
	    	else {log.info("The authentication Component is null");}
	    	return theUser;
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
		public int addPolicyFromFile(String fileURI) {
			AuthorizationSearch au=new AuthorizationSearch();
			return au.AddPolicyFile(null, fileURI);
		}

		public String mCallToPonder2(String thecall)
		{
			AuthorizationSearch au=new AuthorizationSearch();
			return au.ManualCallToPonder(null, thecall);
		}
		

}

