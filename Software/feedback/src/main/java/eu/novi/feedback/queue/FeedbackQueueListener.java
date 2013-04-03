package eu.novi.feedback.queue;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.UserFeedbackData;
import eu.novi.feedback.event.Event;

/**
 * This is the listener of the feedback queue.
 * Whenever a message arrives about an event on this feedback queue,
 * This listener will parse the message and store it in the UserFeedback.
 * @author wibisono
 *
 */
public class FeedbackQueueListener implements MessageListener{
	private static final transient Logger logger = 
			LoggerFactory.getLogger(FeedbackQueueListener.class);
	
	// This will be initialized from the blueprint
	private String defaultBrokerURL = "failover://(tcp://localhost:61616,tcp://localhost:61616)?initialReconnectDelay=100";
	
	private String noviFeedbackQUEUE = "NOVI_FEEDBACK_QUEUE";

	private static final int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private static final int MIN_TIME_UNIT=2000;
	
	private Session jmsSession;
	private Connection jmsConnection;
	private MessageConsumer jmsConsumer;

	private ActiveMQConnectionFactory amqConnectionFactory;

	// Should be initialized via blueprint/container.
	private UserFeedbackData userFeedbackData=new UserFeedbackData();
	
	
	
	//To make the graph nicer, I always increase by 5000 miliseconds
	private long niceCurrentTime = System.currentTimeMillis();
	/**
	 * Notice that in initialization of this feedback queueListener, we use the session ID
	 * as selector for the queue. 
	 * @throws JMSException
	 */
	
	
	public void initialize () throws JMSException {
		amqConnectionFactory = new ActiveMQConnectionFactory(defaultBrokerURL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();

		jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
		Destination destinationQueue = jmsSession.createQueue(noviFeedbackQUEUE);

	    jmsConsumer = jmsSession.createConsumer(destinationQueue);
	    jmsConsumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message genericMessage) {
		MapMessage mapMessage = (MapMessage)genericMessage;
		logger.info("Retrieve message " + mapMessage);
		
		// Somehow must remember here we initialize eventID to "" not null
		String sessionID, requester, eventInfo, link, type, eventID="";
		try {
			
			sessionID = mapMessage.getString("sessionID");
			requester = mapMessage.getString("requester");
			eventInfo = mapMessage.getString("eventInfo");
			eventID   = mapMessage.getString("eventID");
			link 	  = mapMessage.getString("link");
			type	  = mapMessage.getStringProperty("TYPE");
			
			
			/**
			 * A little hack to make the timeline nicer.
			 */
			long currentTime = System.currentTimeMillis();
			if(currentTime >= niceCurrentTime+MIN_TIME_UNIT){
				niceCurrentTime = currentTime;
			}
			else{
				niceCurrentTime += MIN_TIME_UNIT;
			}
			
			userFeedbackData.updateEventsForSession(sessionID, 
					new Event(sessionID, requester, eventInfo, link, eventID, type,
							new Date(niceCurrentTime), new Date(niceCurrentTime)));
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the userFeedbacks
	 */
	public UserFeedbackData getUserFeedbackData() {
		return userFeedbackData;
	}

	/**
	 * @param userFeedbacks the userFeedbacks to set
	 */
	public void setUserFeedbackData(UserFeedbackData userFeedbackData) {
		this.userFeedbackData = userFeedbackData;
	}

	/**
	 * @return the dEFAULT_BROKER_URL
	 */
	public String getBrokerURL() {
		return defaultBrokerURL;
	}

	/**
	 * @param brokerURL the dEFAULT_BROKER_URL to set
	 */
	public void setBrokerURL(String brokerURL) {
		defaultBrokerURL = brokerURL;
	}
}
