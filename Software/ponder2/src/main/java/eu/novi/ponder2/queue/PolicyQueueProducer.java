package eu.novi.ponder2.queue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyQueueProducer {
	private static final transient Logger log = 
			LoggerFactory.getLogger(PolicyQueueProducer.class);
	private String DEFAULT_BROKER_URL = "tcp://localhost:61616";
	private String NOVI_POLICY_MANAGER_QUEUE = "NOVI_POLICY_MANAGER_QUEUE";
	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private Connection jmsConnection;
	ActiveMQConnectionFactory amqConnectionFactory;
	private Session jmsSession;
	private MessageProducer jmsProducer;
	public PolicyQueueProducer() {}
	
	public void initialize() throws JMSException{
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();
		
		jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
		Destination destinationQueue = jmsSession.createQueue(NOVI_POLICY_MANAGER_QUEUE);
		
		jmsProducer = jmsSession.createProducer(destinationQueue);
		jmsProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		
		
	}
	public String pushRequest(String requestText) throws JMSException {
		TextMessage txtMessage = jmsSession.createTextMessage();
		txtMessage.setText(requestText);
		
		jmsProducer.send(txtMessage);
		
		return "Sending : "+txtMessage.getJMSMessageID();
	}
	
	public String getBrokerURL() {
		return DEFAULT_BROKER_URL;
	}
	
	public void setBrokerURL(String brokerURL) {
		log.info("Setting up beans");
		DEFAULT_BROKER_URL = brokerURL;
	}
	
	public String getPolicyManagerQueue() {
		return NOVI_POLICY_MANAGER_QUEUE;
	}
	
	public void setPolicyManagerQueue(String policyManagerQueue) {
		NOVI_POLICY_MANAGER_QUEUE = policyManagerQueue;
	}
	
	

}
