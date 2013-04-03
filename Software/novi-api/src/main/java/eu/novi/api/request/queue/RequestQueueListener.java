package eu.novi.api.request.queue;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.api.request.handler.RequestHandler;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.core.Group;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.Validation;
import eu.novi.mapping.IRMInterface;
import eu.novi.resources.discovery.NoviApiCalls;
import eu.novi.mapping.RemoteIRM;

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
 ******************************************************************************************
 * Queue listener which will listen to requests in the request Queue and performs the 
 * appropriate handling of request.
 * 
 * @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 * 
 ********************************************************************************************/

public class RequestQueueListener implements MessageListener, RequestListener {
	private static final transient Logger log = 
			LoggerFactory.getLogger(RequestQueueListener.class);
	
	private String defaultBrokerURL = "failover://(tcp://localhost:61616,tcp://localhost:61616)?initialReconnectDelay=100";
	private String noviRequestHandlerQUEUE = "NOVI_REQUEST_HANDLER_QUEUE";

	private static final int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private int pollSize = 5;
	
	private Session jmsSession;
	private Connection jmsConnection;
	private MessageConsumer jmsConsumer;

	private ActiveMQConnectionFactory amqConnectionFactory;

	private IMRepositoryUtil repositoryUtil;
	private IRMInterface resourceMappingAPI;
	private RemoteIRM   remoteResourceMappingAPI;
	private NoviApiCalls resourceDiscoveryAPI;

	
	
	private List<RequestProcessor> requestProcessors = new Vector<RequestProcessor>();
	private List<DeleteRequestProcessor> deleteRequestProcessor = new Vector<DeleteRequestProcessor>();

	
	private static HashSet<String> processedMessages = new HashSet<String>();
	
	public RequestQueueListener() {}
	
	/**
	 * This method is configured as init-method in blue print container
	 * This will be called after all the bean setters is configured according to values
	 * provided in blueprint container configuration, if it's in constructor it will be called before setters.
	 * @throws JMSException
	 */
	public void initialize () throws JMSException {
		amqConnectionFactory = new ActiveMQConnectionFactory(defaultBrokerURL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();

		
		for(int i=0;i<pollSize;i++){
			jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
			Destination destinationQueue = jmsSession.createQueue(noviRequestHandlerQUEUE);
	
			// Set up a consumer to consume messages off of the NOVI Request Handler queue
		    jmsConsumer = jmsSession.createConsumer(destinationQueue);
		    jmsConsumer.setMessageListener(this);
		}
	    
	}

	@Override
	public void onMessage(Message incomingRequest)  {
		
		try {

			//Not processing messages which had been processed before, let's assume everything is processed successfully
			if(processedMessages.contains(incomingRequest.getJMSMessageID())){
				return;
			}
			
			processedMessages.add(incomingRequest.getJMSMessageID());
			
			log.info("[NOVI-API] Message listener receive message ");
			log.info("[NOVI-API] Message ID : " +incomingRequest.getJMSMessageID());
			
			log.info("[NOVI-API] Constructing topology request from message");
			
			TextMessage requestMessage = (TextMessage)incomingRequest;
			log.debug(requestMessage.getText());
			
			String requestType = requestMessage.getStringProperty("RequestType");
			
			log.info("Start process "+requestMessage.getStringProperty("sessionID"));			
			if(requestType.equals(RequestHandler.CREATE_SLICE_REQUEST)){
				handleCreateSlice(requestMessage);
			}
			
			if(requestType.equals(RequestHandler.DELETE_SLICE_REQUEST)){
				handleDeleteSlice(requestMessage);
			}
			
			if(requestType.equals(RequestHandler.UPDATE_FAILING_SLICE_REQUEST)){
				handleUpdateFailingSlice(requestMessage);
			}
			
			if(requestType.equals(RequestHandler.UPDATE_MAPPING_SLICE_REQUEST)){
				handleUpdateMappingSlice(requestMessage);
			}
			log.info("Finish process "+requestMessage.getStringProperty("sessionID"));
			
		}
		catch(JMSException e){
			log.error("Failed on handling message");
		}
	}

	public void handleUpdateMappingSlice(TextMessage requestMessage) throws JMSException {
		
		
		String sliceID = requestMessage.getStringProperty("sliceID");
		String sessionID = requestMessage.getStringProperty("sessionID");
		
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener Update Request)", "Request listener receiving update request ", "http://fp7-novi.eu");
		
		NOVIUserImpl noviUser = getUserFromMessage(requestMessage);
		
		String requestText = requestMessage.getText();		
		repositoryUtil = new IMRepositoryUtilImpl();
		
		Collection<Group> groupsFromRequest = repositoryUtil.getGroupImplFromFile(requestText);
		
		Validation groupLinksValidator = new Validation();
		Iterator<Group> groups = groupsFromRequest.iterator();
	
		String errorMessage = "";
		boolean validRequest = true;
		while(validRequest && groups.hasNext()){
			Group 	currentGroup = groups.next();
			errorMessage = groupLinksValidator.checkLinksForSinkSource(currentGroup);
			
			// If the error message is not empty, then there is something wrong in the request a.k.a invalid request.
			validRequest = errorMessage.isEmpty();
		}
		
		if(validRequest){
				eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener Update Request)", "Request listener invoking IRM for update mapping slice", "http://fp7-novi.eu");
				
				// Update slice required groupImpl, while we had validated using Group type.
				// Here we convert Group to GroupImpl
				Collection<GroupImpl>  groupsImpl  = new HashSet<GroupImpl>();
				for (Group group : groupsFromRequest) {
					groupsImpl.add((GroupImpl)group);
				}
				
				resourceMappingAPI.updateSlice(sessionID,sliceID,  groupsImpl, noviUser);
		} else {
			eventReporter.errorEvent(sessionID, "NOVI-API (Request Listener) ", errorMessage + " Request not processed", "http://fp7-novi.eu");
		}
	
		
	}

	public  void handleUpdateFailingSlice(TextMessage requestMessage) throws JMSException {
		
			String sliceID = requestMessage.getStringProperty("sliceID");
			String sessionID = requestMessage.getStringProperty("sessionID");
		
			ReportEvent eventReporter = ReportEventFactory.getReportEvent();
			eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener Update Request)", "Request listener receiving update request ", "http://fp7-novi.eu");
		
			int nResources = requestMessage.getIntProperty("nResources");
			
			Collection<String> failingResources = new ArrayList<String>();
			
			for(int i=0;i<nResources;i++){
				failingResources.add(requestMessage.getStringProperty("failingResource"+i));
			}
			
			eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener Update Request)", "Request listener invoking IRM for update slice", "http://fp7-novi.eu");
			
			remoteResourceMappingAPI.updateSlice(sessionID,sliceID, failingResources);
		
	}

	public boolean handleCreateSlice(TextMessage requestMessage)
			 {
		
		boolean validRequest  = true;
		
		// This has to be somehow delivered to IRM and other subsequent components for them to report their user feedback.
		// IRM Will get this allright since this listener will be used from IRM directly now.
		try {
			String sessionID = requestMessage.getStringProperty("sessionID");
			ReportEvent eventReporter = ReportEventFactory.getReportEvent();
			eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener)", "Request Listener consuming request from NOVI-API Request Queue ", "http://fp7-novi.eu");
				
			String requestText = requestMessage.getText();		
			repositoryUtil = new IMRepositoryUtilImpl();

			Collection<Group> groupsFromRequest = repositoryUtil.getGroupImplFromFile(requestText);
			
			Validation groupLinksValidator = new Validation();
			Iterator<Group> groups = groupsFromRequest.iterator();
		
			String errorMessage = "";

			while(validRequest && groups.hasNext()){
				Group 	currentGroup = groups.next();
				errorMessage = groupLinksValidator.checkLinksForSinkSource(currentGroup);
				
				// If the error message is not empty, then there is something wrong in the request a.k.a invalid request.
				validRequest = errorMessage.isEmpty();
			}
			
			if(validRequest){
				resourceMappingAPI.processGroups(groupsFromRequest, sessionID, getUserFromMessage(requestMessage));
			} else {
				eventReporter.errorEvent(sessionID, "NOVI-API (Request Listener) ", errorMessage + " Request not processed", "http://fp7-novi.eu");
			}
			
		} catch(JMSException e){
			log.error("Failed to handle create slice");
			validRequest = false;
		}
		
		return validRequest;
	}
	
	/**
	 * Reconstruct user object more or less based on what is needed from request-handler-sfa from User object
	 * Providing what is needed in eu.novi.requesthandler.sfa.impl.SFAActions.createSFAUserFromNOVIUser
	 * 
	 * Temporary solution, better would be sending objectMessage
	 * @throws JMSException 
	 */
	private NOVIUserImpl getUserFromMessage(TextMessage m) throws JMSException{
			String userURI = m.getStringProperty("userURI");
			
			//If this is not set, then no user is attached
			if(userURI == null) {
				return null;
			}
			
			String firstName = m.getStringProperty("firstName");
			String lastName = m.getStringProperty("lastName");
			String sessionKey = m.getStringProperty("hasSessionKey");
			
			int nKeys = m.getIntProperty("nKeys");
			
			NOVIUserImpl result = new NOVIUserImpl(userURI);
			
			for(int i=0;i<nKeys;i++){
				result.addPublicKey(m.getStringProperty("pubKey"+i));
			}
			
			result.setFirstName(firstName);
			result.setLastName((lastName));
			result.setHasSessionKey(sessionKey);
			
			return result;
	}
	
	/**
	 * @param requestMessage
	 * @throws JMSException
	 */
	public void handleDeleteSlice(TextMessage requestMessage){
		try{
			String sessionID = requestMessage.getStringProperty("sessionID");
			ReportEvent eventReporter = ReportEventFactory.getReportEvent();

			eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener Delete)", "Request Listener consuming delete request from NOVI-API Request Queue ", "http://fp7-novi.eu");
			eventReporter.instantInfo(sessionID, "NOVI-API (Request Listener)", "Request Listener calling RIS to delete", "http://fp7-novi.eu");
	
			
			String sliceID = requestMessage.getText();
			
			NOVIUserImpl user = getUserFromMessage(requestMessage);
			
			// RIS needs to be modified to use this user, to check for authentication.
			resourceDiscoveryAPI.deleteSlice(user, sliceID, sessionID);
		
		} catch(JMSException e){
			log.error("Failed to handle create slice : " + e.getLocalizedMessage());
		}
	}
	

	/**
	 * @return the resourceMappingAPI
	 */
	public IRMInterface getResourceMappingAPI() {
		return resourceMappingAPI;
	}

	/**
	 * @param resourceMappingAPI the resourceMappingAPI to set
	 */
	public void setResourceMappingAPI(IRMInterface resourceMappingAPI) {
		this.resourceMappingAPI = resourceMappingAPI;
	}

	
	/**
	 * @return the remoteResourceMappingAPI
	 */
	public RemoteIRM getRemoteResourceMappingAPI() {
		return remoteResourceMappingAPI;
	}

	/**
	 * @param remoteResourceMappingAPI the remoteResourceMappingAPI to set
	 */
	public void setRemoteResourceMappingAPI(RemoteIRM remoteResourceMappingAPI) {
		this.remoteResourceMappingAPI = remoteResourceMappingAPI;
	}	
	
	
	/**
	 * @return the resourceInformationAPI
	 */
	public NoviApiCalls getResourceDiscoveryAPI() {
		return resourceDiscoveryAPI;
	}

	/**
	 * @param resourceDiscoveryAPI the resourceInformationAPI to set
	 */
	public void setResourceDiscoveryAPI(NoviApiCalls resourceDiscoveryAPI) {
		this.resourceDiscoveryAPI = resourceDiscoveryAPI;
	}

	public String getBrokerURL() {
		return defaultBrokerURL;
	}
	public void setBrokerURL(String brokerURL) {
		defaultBrokerURL = brokerURL;
	}
	public String getRequestHandlerQueue() {
		return noviRequestHandlerQUEUE;
	}
	public void setRequestHandlerQueue(String requestHandlerQueue) {
		noviRequestHandlerQUEUE = requestHandlerQueue;
	}
	
	public void setRepositoryUtil(IMRepositoryUtil repository){
		repositoryUtil = repository;
	}
	
	public IMRepositoryUtil getRepositoryUtil(){
		return repositoryUtil;
	}

	@Override
	public void addRequestProcessor(RequestProcessor processor) {
			requestProcessors.add(processor);
	}

	@Override
	public void addDeleteRequestProcessor(DeleteRequestProcessor deleteProcessor) {
			deleteRequestProcessor.add(deleteProcessor);
	}

	/**
	 * @return the pollSize
	 */
	public int getPollSize() {
		return pollSize;
	}

	/**
	 * @param pollSize the pollSize to set
	 */
	public void setPollSize(int pollSize) {
		this.pollSize = pollSize;
	}




}
