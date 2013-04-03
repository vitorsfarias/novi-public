package eu.novi.api.request.queue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.api.request.handler.RequestHandler;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.policy.NOVIUser;

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
 * 
 ****************************************************************************************** 
 * 
 * Request Queue Producer implementation. 
 * This class is responsible to setup connection to ActiveMQ broker
 * REST end point accepting request topology will use this to produce entry in RequestQueue 
 * 
 * @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 * 
 *******************************************************************************************/


public class RequestQueueProducer {
	private static final transient Logger log = 
			LoggerFactory.getLogger(RequestQueueProducer.class);
	
	private String DEFAULT_BROKER_URL = "tcp://localhost:61616";
	private String NOVI_REQUEST_HANDLER_QUEUE="NOVI_REQUEST_HANDLER_QUEUE";

	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;

	private Session jmsSession;
	private Connection jmsConnection;
	private MessageProducer jmsProducer;
	 
	ActiveMQConnectionFactory amqConnectionFactory;
	
	public RequestQueueProducer() {}
	
	/**
	 * This method is configured as init-method in blue print container
	 * This will be called after all the bean setters is configured according to values
	 * provided in blueprint container configuration, if it's in constructor it will be called before setters.
	 * @throws JMSException
	 */

	public void initialize() throws JMSException {
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
        jmsConnection.start();
        
        jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
        Destination destinationQueue = jmsSession.createQueue(NOVI_REQUEST_HANDLER_QUEUE);
        
        // Setup a producer to produce message for the NOVI Request Handler queue
        jmsProducer = jmsSession.createProducer(destinationQueue);
        jmsProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);           
        
	}
	
	/**
	 * 
	 * This is where we push a requestText into the queue. This request queue producer is only producing request for current session.
	 * @param requestText
	 * @return
	 * @throws JMSException
	 */
	public String pushCreateSliceRequest(String sessionID, String requestText, NOVIUser user) throws JMSException {
		
		TextMessage textMessage = createRequestMessage(sessionID, RequestHandler.CREATE_SLICE_REQUEST, requestText, user);
		
		log.info("Pushing a request from user");
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (Request Producer)", "Request Producer pushing request to NOVI-API Request Queue", "http://fp7-novi.eu");
		
		jmsProducer.send(textMessage);
		
		return textMessage.getJMSMessageID();
	}
	


	public String pushDeleteRequest(String sessionID, String sliceID, NOVIUser user) throws JMSException{
		
		TextMessage textMessage = createRequestMessage(sessionID, RequestHandler.DELETE_SLICE_REQUEST, sliceID, user);
		
		log.info("Pushing a delete request from user");
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (Request Producer Delete Request)", "Request Producer pushing delete request to NOVI-API Request Queue", "http://fp7-novi.eu");
		
		jmsProducer.send(textMessage);
		
		return textMessage.getJMSMessageID();		
	}
	
	
	public TextMessage pushUpdateFailingRequest(String sessionID, NOVIUser noviUser,
			String sliceID, List<String> failingResources) throws JMSException {
		
		TextMessage updateSliceMessage =  createRequestMessage(sessionID,RequestHandler.UPDATE_FAILING_SLICE_REQUEST,sliceID, noviUser);
		
		updateSliceMessage.setStringProperty("sliceID", sliceID);
		updateSliceMessage.setIntProperty("nResources", failingResources.size());
		
		for(int i=0;i< failingResources.size();i++){
			updateSliceMessage.setStringProperty("failingResource"+i, failingResources.get(i));
		}
		
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (Request Producer Update Request)", "Request Producer pushing update request to NOVI-API Request Queue", "http://fp7-novi.eu");
	
		jmsProducer.send(updateSliceMessage);
		
		return updateSliceMessage;
	}

	public TextMessage pushUpdateMappingRequest(String sessionID, String sliceID,
			String updatedRequest, NOVIUser noviUser) throws JMSException {
		
		TextMessage updateMappingMessage = createRequestMessage(sessionID, RequestHandler.UPDATE_MAPPING_SLICE_REQUEST, updatedRequest, noviUser);
		updateMappingMessage.setStringProperty("sliceID", sliceID);
		updateMappingMessage.setStringProperty("sessionID", sessionID);
		
		
		log.info("Pushing an update mapping request from user");
		ReportEvent eventReporter = ReportEventFactory.getReportEvent();
		eventReporter.instantInfo(sessionID, "NOVI-API (Request Producer)", "Request Producer pushing update mapping request to NOVI-API Request Queue", "http://fp7-novi.eu");
		
		jmsProducer.send(updateMappingMessage);
		
		return updateMappingMessage;

	}
	
	// Original version without user
	public TextMessage createRequestMessage(String sessionID, String requestType, String requestText) throws JMSException{
		TextMessage textMessage = jmsSession.createTextMessage();
		textMessage.setStringProperty("sessionID", sessionID);
		textMessage.setStringProperty("RequestType", requestType);
		textMessage.setText(requestText);
		return textMessage;
	}
	
	/**
	 * Create a message with main message contained in requestText
	 * @param sessionID  current feedback session ID
	 * @param requestType request type which can be CreateSlice, DeleteSlice, or UpdateSlice
	 * @param requestText owlFile for createSlice, sliceID for DeleteSlice
	 * @param user
	 * @return
	 * @throws JMSException
	 */
	public TextMessage createRequestMessage(String sessionID, String requestType, String requestText, NOVIUser user) throws JMSException{
		TextMessage textMessage = jmsSession.createTextMessage();
		textMessage.setStringProperty("sessionID", sessionID);
		textMessage.setStringProperty("RequestType", requestType);
		textMessage.setText(requestText);
		
		if(user == null) 
			return textMessage;
		
		//setup user
		textMessage.setStringProperty("userURI", user.toString());
		textMessage.setStringProperty("firstName", user.getFirstName());
		textMessage.setStringProperty("lastName", user.getLastName());
		textMessage.setStringProperty("hasSessionKey", user.getHasSessionKey());
		Set<String> keys = user.getPublicKeys();
		
		int nKeys = 0;
		if(keys!=null)
			nKeys = keys.size();
		
		Iterator<String> keysIterator= keys.iterator();
		
		textMessage.setIntProperty("nKeys", nKeys);
		for(int i=0;i<nKeys;i++)
			textMessage.setStringProperty("pubKey"+i, keysIterator.next());
		
		return textMessage;
	}
	
	public String getBrokerURL() {
		return DEFAULT_BROKER_URL;
	}
	
	public void setBrokerURL(String brokerURL) {
		log.info("Setting up beans");
		DEFAULT_BROKER_URL = brokerURL;
	}
	
	public String getRequestHandlerQueue() {
		return NOVI_REQUEST_HANDLER_QUEUE;
	}
	
	public void setRequestHandlerQueue(String requestHandlerQueue) {
		NOVI_REQUEST_HANDLER_QUEUE = requestHandlerQueue;
	}



}
