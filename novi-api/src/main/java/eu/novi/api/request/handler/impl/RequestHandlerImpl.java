package eu.novi.api.request.handler.impl;


import java.io.IOException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.api.request.handler.RequestHandler;
import eu.novi.api.request.handler.helpers.FileReader;
import eu.novi.api.request.queue.RequestQueueProducer;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.policyAA.interfaces.InterfaceForAPI;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponse;
import eu.novi.resources.discovery.NoviApiCalls;

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
 *  @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 *  
 *******************************************************************************************/
public class RequestHandlerImpl implements RequestHandler {
	private static final transient Logger log = 
			LoggerFactory.getLogger(RequestHandlerImpl.class);
	
	
	/**
	 * References to services provided from other bundles.
	 */
	private InterfaceForAPI authenticationAPI;

	private NoviApiCalls resourceDiscoveryAPI;
	
	private FederatedTestbed requestHandlerAPI;
	
	private HashMap<String, NOVIUser> sessionKeyToNOVIUser = new HashMap<String, NOVIUser>();

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
	
	
	public RequestQueueProducer getRequestProducer() {
		return requestProducer;
	}

	public void setRequestProducer(RequestQueueProducer requestProducer) {
		this.requestProducer = requestProducer;
	}

	
	
	/**
	 * Provides OWL file content for the given slice id.
	 * @param sliceId
	 * @return OWL file content
	 */
	public String getSlice(String sliceID) throws JMSException, URISyntaxException {
		String fileContent = "";
		fileContent = resourceDiscoveryAPI.getSlice(sliceID);
		return fileContent;
	}

	/**
	 * Provides OWL file content for the given slice id.
	 * @param sliceId
	 * @return OWL file content
	 */
	public String getOwlRequestHandler(String sliceID) {
		String fileContent = "";
		try {
			fileContent = FileReader.readFile("1link.owl");
		} catch (NoClassDefFoundError e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return fileContent;
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
	 * This is the implementation of Request Handler REST service
	 * Currently it only provides an entry point to push the OWL request
	 * Into an ActiveMQ queue that will be listened and consumed by other services.
	 * 
	 * Ideally every request will generate a resource (request) that can be monitored later on.
	 * @throws JMSException 
	 * @throws URISyntaxException 
	 */
	@Override
	public String unboundRequestHandler(String requestTopologyInOWLFormat, String sessionKey) throws JMSException, URISyntaxException{
		if(requestTopologyInOWLFormat == null){
			return EMPTY_CREATE_REQUEST;
		}
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("CXF REST endpoint Accepting ");
		//No longer generating random session ID
		String sessionID = UUID.randomUUID().toString();

		
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (CXF Accept)", "Rest end point accepting request in OWL Format", "http://fp7-novi.eu");
		
		//show owl file content
		log.info("PSNC DEBUG: OWL FILE Content:\n\n"+requestTopologyInOWLFormat);
		log.info("\n\n");
		// Putting this OWL request into the queue, providing sessionID, which will be used as sliceID.
		requestProducer.pushCreateSliceRequest(sessionID, requestTopologyInOWLFormat, sessionKeyToNOVIUser.get(sessionKey));

		String redirectLocation = RequestHandler.SYSTEM_INSTANCE_LOCATION+"/requestHandlerFeedback/feedback/timeline/"+sessionID;
		//Redirecting to the user feedback er, not here, I'll just return the string and redirect done by the GUI
		
		return redirectLocation;
	}

	@Override
	public String boundRequestHandler(String requestTopologyInOWLFormat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String namedRequestHandler(String requestTopologyInOWLFormat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteRequestHandler(String sliceID, String sessionKey) throws JMSException,
			URISyntaxException {
		
		if(sliceID == null){
			return EMPTY_DELETE_REQUEST;
		}
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		
		log.info("CXF REST endpoint Accepting Delete Request");
		String sessionID = UUID.randomUUID().toString();
		//String sessionID = sliceID;

		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (CXF Accept Delete)", "Rest end point accepting delete request for sliceID "+sliceID, "http://fp7-novi.eu");
		
		// Putting this OWL request into the queue, providing sessionID, which will be used as sliceID.
		requestProducer.pushDeleteRequest(sessionID, sliceID,  sessionKeyToNOVIUser.get(sessionKey));

		String redirectLocation = RequestHandler.SYSTEM_INSTANCE_LOCATION+"/requestHandlerFeedback/feedback/timeline/"+sessionID;
		//Redirecting to the user feedback er, not here, I'll just return the string and redirect done by the GUI
		
		//return Response.seeOther(new URI(redirectLocation )).build();
		return redirectLocation;
	}
	
	@Override
	public String listSliceByUser(String user) throws JMSException,
			URISyntaxException {
		
			RHListSlicesResponse response = requestHandlerAPI.listUserSlices(user);
		
			List<String> retrievedSlices = new ArrayList<String>();
			
			if(response != null && response.getSlices() != null) 
				retrievedSlices = response.getSlices();
			else
				log.info("No slices found in list slice by user " + user);
			
			StringBuffer commaSeparatedSlices = new StringBuffer();
			
			for(String currentSlice : retrievedSlices) {
				// Filter the currentSlice, if it is not known to RIS, don't return it.
				// I am interested only on the last part started with slice_
				log.info("List slice user processing slice " + currentSlice);
				
				String sliceIDPart= getSliceIDFromRequestHandler(currentSlice);
				
				if(resourceDiscoveryAPI.checkSliceExist("http://fp7-novi.eu/im.owl#"+sliceIDPart)){
					log.info("Slice "+currentSlice+" existed in both testbed(RH) and in RIS");

					if(commaSeparatedSlices.length() > 0) 
								commaSeparatedSlices.append(",");
					commaSeparatedSlices.append(currentSlice);
				} else {
					log.info("Slice "+currentSlice+" is in the testbed (RH) but not in RIS");
				}
			}
			
			return commaSeparatedSlices.toString();
	}
	
	@Override
	public String listSliceByUserRIS(String user) {

		Set<String> resultset = new HashSet<String>();
		Set <String> allSliceIDs = 	resourceDiscoveryAPI.execStatementReturnResults(null, "rdf:type", "im:Reservation");
		
		for(String sliceID : allSliceIDs){
			Set<String> users = resourceDiscoveryAPI.execStatementReturnResults(null, "rdf:type", "pl:NOVIUser", sliceID);
			if(users.size() == 0) continue;
			String curUser = users.iterator().next();
		
			if(curUser.endsWith(user)){
				resultset.add(sliceID);
			}
		}
		
		String result = "";
		for(String slice : resultset){
			if(result.length() == 0) result = slice;
			else result += ","+slice;
		}
		
		return result;
	}

	@Override
	public String listPlatformSliceByUser(String user) throws JMSException,
			URISyntaxException {
		
		RHListSlicesResponse response = requestHandlerAPI.listUserSlices(user);
		
		List<String> retrievedSlices = response.getSlices();
		
		StringBuffer commaSeparatedSlices = new StringBuffer();
		
		for(String currentSlice : retrievedSlices) {
			if(commaSeparatedSlices.length() > 0) 
				commaSeparatedSlices.append(",");
			commaSeparatedSlices.append(currentSlice);
		}
		
		return commaSeparatedSlices.toString();
	}
	
	@Override
	public String listResourceByUser(String user) throws JMSException,
			URISyntaxException {
		
		RHListResourcesResponseImpl response = requestHandlerAPI.listResources(user);
		
		return response.getPlatformString();
	}
	
	public String getSliceIDFromRequestHandler(String currentSlice) {
		String result = "";
		int indexOfSlice = currentSlice.indexOf("slice_");
		if(indexOfSlice >=0 )
			result = currentSlice.substring(indexOfSlice);
		
		return result;
	}

	/**
	 * We are going to set this producer from blueprint configuration
	 */
	RequestQueueProducer requestProducer ;

	/**
	 * @return the resourceDiscoveryAPI
	 */
	public NoviApiCalls getResourceDiscoveryAPI() {
		return resourceDiscoveryAPI;
	}

	/**
	 * @param resourceDiscoveryAPI the resourceDiscoveryAPI to set
	 */
	public void setResourceDiscoveryAPI(NoviApiCalls resourceDiscoveryAPI) {
		this.resourceDiscoveryAPI = resourceDiscoveryAPI;
	}

	public FederatedTestbed getRequestHandlerAPI() {
		return requestHandlerAPI;
	}

	public void setRequestHandlerAPI(FederatedTestbed requestHandlerAPI) {
		this.requestHandlerAPI = requestHandlerAPI;
	} 
	
	@Override
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

	
	@Override
	public String getStatement(String Subject, String Predicate, String Object,	String Context) {
		
		log.info("Calling with arguments Subject "+Subject+" Predicate "+Predicate+" Object "+Object + " Context "+Context);
		
		if(Subject.equals("NoSubject")) Subject = null;
		if(Predicate.equals("NoPredicate")) Predicate = null;
		if(Object.equals("NoObject")) Object = null;
		
		Set<String> result = null;
		if(Context.equals("NoContext")) {
			log.info("Calling RIS with arguments Subject "+Subject+" Predicate "+Predicate+" Object "+Object );
					
			result = resourceDiscoveryAPI.execStatementReturnResults(Subject, Predicate, Object);
		} else {
			log.info("Calling RIS with arguments Subject "+Subject+" Predicate "+Predicate+" Object "+Object + " Context "+Context);
			result = resourceDiscoveryAPI.execStatementReturnResults(Subject, Predicate, Object, Context);
		}
		
		// Based on interface provided by ris, null means something wrong.
		if(result == null) return "Invalid request";
		if(result.isEmpty()) return "No Result";
		
		StringBuffer csvResult = new StringBuffer();
		for(String r : result){
			if(csvResult.length() > 0) csvResult.append(",");
			csvResult.append("\n"+r);
		}
		
		return csvResult.toString();
	}
	
	@Override
	public String  getPrintStatement(String Subject, String Predicate, String Object, String Context){
		
		if(Subject.equals("NoSubject")) Subject = null;
		if(Predicate.equals("NoPredicate")) Predicate = null;
		if(Object.equals("NoObject")) Object = null;
		if(Context.equals("NoContext")) Context = null;

		return resourceDiscoveryAPI.execStatementPrintResults(Subject, Predicate, Object, Context);
	}
	
	
	@Override
	public String updateFailingSlice(String sessionKey, String sliceID,
			List<String> failingResources) throws JMSException {
		

		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("CXF REST endpoint accepting update slice request ");
		
		String sessionID = UUID.randomUUID().toString();

		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (CXF Accept)", "Rest end point accepting update slice request for failing resources", "http://fp7-novi.eu");
		
		requestProducer.pushUpdateFailingRequest(sessionID,  sessionKeyToNOVIUser.get(sessionKey), sliceID, failingResources);

		String redirectLocation = RequestHandler.SYSTEM_INSTANCE_LOCATION+"/requestHandlerFeedback/feedback/timeline/"+sessionID;
		
		
		return redirectLocation;
		
	}
	
	@Override
	public String updateMappingSlice(String sessionKey, String sliceID,
			String updatedRequest)throws JMSException, URISyntaxException {
		
		if(updatedRequest == null){
			return EMPTY_UPDATE_REQUEST;
		}
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("CXF REST endpoint Accepting Update Request");
		
		
		String sessionID = UUID.randomUUID().toString();

		
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (CXF Accept)", "Rest end point accepting request in OWL Format", "http://fp7-novi.eu");
		
		requestProducer.pushUpdateMappingRequest(sessionID, sliceID, updatedRequest, sessionKeyToNOVIUser.get(sessionKey));

		String redirectLocation = RequestHandler.SYSTEM_INSTANCE_LOCATION+"/requestHandlerFeedback/feedback/timeline/"+sessionID;
		
		return redirectLocation;

	}
	
	@Override
	public String updateSliceExpiration(String sessionKey, String sliceURI, String dateString) {
		String result="";
		
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		NOVIUser currentUser = sessionKeyToNOVIUser.get(sessionKey);
		Date realDate  = new Date(dateString); 
	   
		result = resourceDiscoveryAPI.updateExpirationTime(currentUser, sliceURI, realDate);
		
		return result;
		
	};
	
	@Override
	public String listAvailableResourceUser(String sessionKey) {
		if(!sessionKeyToNOVIUser.containsKey(sessionKey)){
			return SESSION_KEY_NOT_FOUND;
		}
		
		log.info("CXF REST endpoint Accepting list available user for session "+sessionKey);
		
		NOVIUser currentUser = sessionKeyToNOVIUser.get(sessionKey);
		
		Set<Node> nodes = resourceDiscoveryAPI.listResources(currentUser);
		
		IMRepositoryUtil util = new IMRepositoryUtilImpl();
		Platform container = new PlatformImpl("http://fp7-novi.eu/im.owl#availableResources");
		String result = "";
		
		container.setContains(nodes);
		
		result = util.exportIMObjectToStringWithFilter(container, 
				  "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity",
				  "http://fp7-novi.eu/im.owl#Resource",
				  "http://fp7-novi.eu/im.owl#NetworkElement",
				  "http://fp7-novi.eu/im.owl#NodeComponent");
		
		return result;
		
	}
	
	

}
