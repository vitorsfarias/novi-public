package eu.novi.api.request.handler.impl;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.api.request.handler.MonitoringService;
//import eu.novi.api.request.handler.helpers.FileReader;
//import eu.novi.api.request.queue.RequestQueueProducer;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.policyAA.interfaces.InterfaceForAPI;
//import eu.novi.requesthandler.sfa.SFAFederatedTestbed;
//import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
//import eu.novi.requesthandler.sfa.response.RHListSlicesResponse;
//import eu.novi.resources.discovery.NoviApiCalls;
import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.*;


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
 *******************************************************************************************
 * Request handler service implementation.
 * 
 * Creation of slice and deletion of slice are asynchronous via queue.
 * 
 * Authentication and getSlice are synchronous.
 * 
  * 
 *  @author <a href="mailto:lakis@inf.elte.hu">Sandor Laki</a>
 *  
 *******************************************************************************************/
import static eu.novi.api.request.handler.RequestHandler.*;
public class MonitoringServiceImpl implements MonitoringService {
	private static final transient Logger log = 
			LoggerFactory.getLogger(MonitoringServiceImpl.class);
	
	
	/**
	 * References to services provided from other bundles.
	 */
	private InterfaceForAPI authenticationAPI;

//	private NoviApiCalls resourceDiscoveryAPI;
	
//	private SFAFederatedTestbed requestHandlerAPI;

	private MonSrv monitoringServiceAPI;
	
	private HashMap<String, NOVIUser> sessionKeyToNOVIUser = new HashMap<String, NOVIUser>();

	
	public MonitoringServiceImpl() {
		sessionKeyToNOVIUser.put("dummy",new NOVIUserImpl("dummy")); // TODO: to be removed in the final version 		
	}
	
	/**
	 * @return the monitoringServiceAPI
	 */
	public MonSrv getMonitoringServiceAPI() {
		return monitoringServiceAPI;
	}
	
	
	/**
	 * @param the monitoringServiceAPI to be set
	 */
	public void setMonitoringServiceAPI(MonSrv monitoringServiceAPI) {
		this.monitoringServiceAPI = monitoringServiceAPI;
	}
	
	/**
	 * @return the authorizationAPI
	 */
	public InterfaceForAPI getAuthenticationAPI() {
		return authenticationAPI;
	}

	/**
	 * @param authenticationAPI the authorizationAPI to set
	 */
	public void setAuthenticationAPI(InterfaceForAPI authenticationAPI) {
		this.authenticationAPI = authenticationAPI;
	}
	
		
	
	@Override
	public String authenticateUser(String userName, String password) {
		NOVIUser noviUser = null;
		
		try {
			noviUser   = authenticationAPI.getAuth(userName, password);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		if(noviUser  == null){
			log.info("NOVI USER "+ USER_DOES_NOT_EXIST);
			return USER_DOES_NOT_EXIST;
		}
		if(noviUser.getHasSessionKey() == null){
			log.info("Failed authentication NOVI user "+noviUser.getFirstName()+" "+noviUser.getLastName()+" "+noviUser.getHasSessionKey());
			
			return  USER_IS_NOT_AUTHENTICATED;
		}
		
		String sessionKey = noviUser.getHasSessionKey();
		sessionKeyToNOVIUser.put(sessionKey, noviUser);		
		
		log.info("Successfulll authentication NOVI user "+noviUser.getFirstName()+" "+noviUser.getLastName()+" "+noviUser.getHasSessionKey());
		
		log.info("Session keys stored so far : " +sessionKeyToNOVIUser.size());
		
		return sessionKey;
	}



	/**
	 * Test call for monitoring service
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String echo(String testparam)  throws JMSException, URISyntaxException {
		
//		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
//			return SESSION_KEY_NOT_FOUND;
//		}
//		
		log.info("Monitoring CXF REST endpoint Accepting ");
		
		List<String> response = monitoringServiceAPI.echo(testparam);
		
		return response.get(0) + response.get(1); // dummy implementation
				
	}
	
	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String sliceTasks(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
		
		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String addTask(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
	
		UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

		String result = monitoringServiceAPI.addTask( dummy, query );
		
	
		return result;	
		
	}
	
	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String describeTaskData(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.describeTaskData( dummy, query );


                return result;
//		return "Not forwarded yet";	
		
	}
	
	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String modifyTask(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.modifyTask( dummy, query );


                return result;
//		return "Not forwarded yet";	
		
	}
	
	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String removeTask(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.removeTask( dummy, query );

                return result;		

//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String enableTask(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");

                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.enableTask( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String disableTask(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.disableTask( dummy, query );


                return result;		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String getTaskStatus(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
		UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                boolean result = monitoringServiceAPI.getTaskStatus( dummy, query );


		if (result) return "[{ \"status\" : \"True\" }]";
		else return "[{ \"status\" : \"False\" }]";
                
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String addAggregator(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.addAggregator( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String removeAggregator(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.removeAggregator( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String fetchAggregator(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

				String result= "";
            //    String result = monitoringServiceAPI.fetchAggregator( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String fetchTaskData(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.fetchTaskData( dummy, query );


                return result;

//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String addCondition(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.addCondition( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String removeCondition(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.removeCondition( dummy, query );


                return result;

//		return "Not forwarded yet";	
		
	}

	/**
	 * 
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String modifyCondition(String sliceID, String query, String sessionKey)  throws JMSException, URISyntaxException {
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("Monitoring CXF REST endpoint Accepting ");
                UsernameRSAKey dummy = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

                String result = monitoringServiceAPI.modifyCondition( dummy, query );


                return result;
		
//		return "Not forwarded yet";	
		
	}

	
/*	@Override
	public String checkSession(String key) {
		if(sessionKeyToNOVIUser.size() == 0)
			return SESSION_MAP_EMPTY;
		if(!sessionKeyToNOVIUser.containsKey(key))
			return SESSION_KEY_NOT_FOUND;
		
		NOVIUser user = sessionKeyToNOVIUser.get(key);
		
		String name = ""+ user.getFirstName();
		name += user.getLastName();
		
		return name;
	}
*/
	
	

}
