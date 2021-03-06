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
package eu.novi.policylistener.monitoringInteractions;

import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.policylistener.interfaces.InterfaceForMonitoring;
import eu.novi.policylistener.ponder2comms.TelnetClient;
import eu.novi.policylistener.synchWithRIS.AuthorizationSearch;

public class MonitoringEvents implements InterfaceForMonitoring{
    String toforward;
	private static final transient Logger log = LoggerFactory.getLogger(MonitoringEvents.class);
	TelnetClient telclient= new TelnetClient();
	// Will be initialized from blue print
	ReportEvent userFeedback=null;
	
	public void setTelnetClient(TelnetClient tel)
	{
		telclient=tel;
	}	

	
	public int ResourceFailure(String topo,String vnodeID) {
		
		String result="";
		String vnodefailure="root/event/virtualnodefailure create: #(\""+vnodeID+"\").\r\n";
		System.out.println(vnodefailure);
		if(userFeedback != null)
			userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "Monitoring Event received :"+vnodefailure, " .");
		result = telclient.TelnetPonder2(vnodefailure);
		System.out.println(result);
		return 0;
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



}
