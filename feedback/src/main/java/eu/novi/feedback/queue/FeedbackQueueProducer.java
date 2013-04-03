package eu.novi.feedback.queue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.Event;

/**
 * This is the class responsible for handling event produced by user
 * to be pushed into the feedback queue.
 * @author wibisono
 *
 */
public class FeedbackQueueProducer {
	private static final transient Logger log = 
			LoggerFactory.getLogger(FeedbackQueueProducer.class);
	
	private String DEFAULT_BROKER_URL = "tcp://localhost:61616";

	private String NOVI_FEEDBACK_QUEUE = "NOVI_FEEDBACK_QUEUE";

	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;

	private Session jmsSession;
	private Connection jmsConnection;
	private MessageProducer jmsProducer;
	 
	ActiveMQConnectionFactory amqConnectionFactory;
	
	
	public FeedbackQueueProducer() {
	}
	
	public void initialize() throws JMSException {
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
        jmsConnection.start();
        
        jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
        Destination destinationQueue = jmsSession.createQueue(NOVI_FEEDBACK_QUEUE);
        
        // Setup a producer to produce message for the NOVI Request Handler queue
        jmsProducer = jmsSession.createProducer(destinationQueue);
        jmsProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);           
	
	}
	
	/**
	 * Does what it said it does, reporting to sessionID time line, that the componentName
	 * has some interesting event Information, that happened instantaneously.
	 * @param sessionID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @return
	 * @throws JMSException
	 */
	public void reportInstantEvent(String sessionID, String componentName, String eventInformation, String link) throws JMSException {
		
		MapMessage mapMessage = jmsSession.createMapMessage();
		mapMessage.setString("sessionID", sessionID);
		mapMessage.setString("requester",componentName);
		mapMessage.setString("eventInfo", eventInformation);
		mapMessage.setString("link", link);
		mapMessage.setString("type", Event.INSTANT_EVENT);
		
		// This is an important step to indicate which session this message is generated
		mapMessage.setStringProperty("SESSIONID", sessionID);
		mapMessage.setStringProperty("TYPE", Event.INSTANT_EVENT);
		log.info("Reporting instant event"+mapMessage);
		jmsProducer.send(mapMessage);

	}
	
	/**
	 * This will start an event. A matching stop report is expected to be called to stop this.
	 * @param sessionID
	 * @param eventID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @throws JMSException
	 */
	public void reportStartEvent(String sessionID, String eventID, String componentName, 
			String eventInformation, String link) throws JMSException {
		
		MapMessage mapMessage = jmsSession.createMapMessage();
		mapMessage.setString("sessionID", sessionID);
		mapMessage.setString("eventID", eventID);
		mapMessage.setString("requester",componentName);
		mapMessage.setString("eventInfo", eventInformation);
		mapMessage.setString("link", link);
		
		mapMessage.setString("type", Event.START_EVENT);
		
		// This is an important step to indicate which session this message is generated
		mapMessage.setStringProperty("SESSIONID", sessionID);
		mapMessage.setStringProperty("EVENTID", eventID);
		mapMessage.setStringProperty("TYPE", Event.START_EVENT);
		
		log.info("Reporting start event"+mapMessage);
		jmsProducer.send(mapMessage);
	}
	
	/**
	 * This will stop a previously started event.
	 * @param sessionID
	 * @param eventID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @throws JMSException
	 */
	public void reportStopEvent(String sessionID, String eventID, String componentName, 
			String eventInformation, String link) throws JMSException {
		
		MapMessage mapMessage = jmsSession.createMapMessage();
		mapMessage.setString("sessionID", sessionID);
		mapMessage.setString("eventID", eventID);
		mapMessage.setString("requester",componentName);
		mapMessage.setString("eventInfo", eventInformation);
		mapMessage.setString("link", link);
		mapMessage.setString("type", Event.STOP_EVENT);
		
		// This is an important step to indicate which session this message is generated
		mapMessage.setStringProperty("SESSIONID", sessionID);
		mapMessage.setStringProperty("EVENTID", eventID);
		mapMessage.setStringProperty("TYPE", Event.STOP_EVENT);
		
		log.info("Reporting stop event"+mapMessage);
		jmsProducer.send(mapMessage);
	}
	
	/**
	 * Reporting and error event, almost similar to instant event just that the type for this is Error
	 * @param sessionID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @throws JMSException
	 */
	public void reportErrorEvent(String sessionID, String componentName, String eventInformation, String link) throws JMSException {
		
		MapMessage mapMessage = jmsSession.createMapMessage();
		mapMessage.setString("sessionID", sessionID);
		mapMessage.setString("requester",componentName);
		mapMessage.setString("eventInfo", eventInformation);
		mapMessage.setString("link", link);
		mapMessage.setString("type", Event.ERROR_EVENT);
		
		// This is an important step to indicate which session this message is generated
		mapMessage.setStringProperty("SESSIONID", sessionID);
		mapMessage.setStringProperty("TYPE", Event.ERROR_EVENT);
		
		log.info("Reporting error event"+mapMessage);
		jmsProducer.send(mapMessage);

	}

	/**
	 * @return the dEFAULT_BROKER_URL
	 */
	public String getBrokerURL() {
		return DEFAULT_BROKER_URL;
	}

	/**
	 * @param newBroker the dEFAULT_BROKER_URL to set
	 */
	public void setBroker(String newBroker) {
		DEFAULT_BROKER_URL = newBroker;
	}
}
