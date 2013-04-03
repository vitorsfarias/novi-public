package eu.novi.policysender.queue;
/*package eu.novi.policy.queue;

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

//import eu.novi.ponder2.queue.PolicyQueueProducer;

public class Ponder2QueueProducer {

	private static final transient Logger log = 
			LoggerFactory.getLogger(Ponder2QueueProducer.class);
	private String DEFAULT_BROKER_URL = "tcp://localhost:61616";
	private String NOVI_PONDER2_QUEUE = "NOVI_PONDER2_QUEUE";
	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private Connection jmsConnection;
	ActiveMQConnectionFactory amqConnectionFactory;
	private Session jmsSession;
	private MessageProducer jmsProducer;
	public Ponder2QueueProducer() {}
	
	public void initialize() throws JMSException{
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();
		
		jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
		Destination destinationQueue = jmsSession.createQueue(NOVI_PONDER2_QUEUE);
		
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
	
	public String getPonder2Queue() {
		return NOVI_PONDER2_QUEUE;
	}
	
	public void setPonder2Queue(String ponder2Queue) {
		NOVI_PONDER2_QUEUE = ponder2Queue;
	}
	
	
}
*/