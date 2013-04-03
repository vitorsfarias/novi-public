package eu.novi.ponder2.queue;

import java.util.Collection;

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

public class Ponder2QueueListener implements MessageListener {

	private static final transient Logger log= LoggerFactory.getLogger(Ponder2QueueListener.class);
	private String DEFAULT_BROKER_URL = "TCP://localhost:61616";
	private Connection jmsConnection;
	ActiveMQConnectionFactory amqConnectionFactory;
	private Session jmsSession;
	private MessageConsumer jmsConsumer;
	
	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private String NOVI_PONDER2_QUEUE = "NOVI_PONDER2_QUEUE";
	
	public Ponder2QueueListener() {}
	
	public void initialize () throws JMSException{
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();
		
		jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
		Destination destinationQueue = jmsSession.createQueue(NOVI_PONDER2_QUEUE);
		
		jmsConsumer = jmsSession.createConsumer(destinationQueue);
		jmsConsumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message incomingEventMessage) {
		
		try {
			log.info("[Ponder2] Message listener receive message ");
			log.info("[Ponder2] Message ID : "+ incomingEventMessage.getJMSMessageID());
			log.info("[Ponder2] Receiving message");
			
			boolean ponder2Action=true;
			//boolean MonitoringEvent=false;
			
			incomingEventMessage.getClass();
			TextMessage transmitEvent= (TextMessage)incomingEventMessage;
			
			if (ponder2Action==true)
			{
				System.out.println("We received the action from activemq");
				
			}
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		
	}
	public String getBrokerURL() {
		return DEFAULT_BROKER_URL;
	}
	public void setBrokerURL(String brokerURL) {
		DEFAULT_BROKER_URL = brokerURL;
	}
	public String getPonder2Queue() {
		return NOVI_PONDER2_QUEUE;
	}
	public void setPonder2Queue(String ponder2Queue) {
		NOVI_PONDER2_QUEUE = ponder2Queue;
	}
	
		

	
}
