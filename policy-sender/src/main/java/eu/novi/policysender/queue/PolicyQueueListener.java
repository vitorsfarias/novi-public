package eu.novi.policysender.queue;
/*package eu.novi.policy.queue;

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

import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.policy.RequestToIRM.RequestToIRM;
import eu.novi.policy.monitoringevents.MEReturn;
import eu.novi.policy.monitoringevents.MonitoringEvents;

public class PolicyQueueListener implements MessageListener {
	private static final transient Logger log= LoggerFactory.getLogger(PolicyQueueListener.class);
	private String DEFAULT_BROKER_URL = "TCP://localhost:61616";
	private Connection jmsConnection;
	ActiveMQConnectionFactory amqConnectionFactory;
	private Session jmsSession;
	private MessageConsumer jmsConsumer;
	
	private static int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	private static final boolean TRANSACTED = false;
	private String NOVI_POLICY_MANAGER_QUEUE = "NOVI_POLICY_MANAGER_QUEUE";
	
	public PolicyQueueListener() {}
	
	public void initialize () throws JMSException{
		amqConnectionFactory = new ActiveMQConnectionFactory(DEFAULT_BROKER_URL);
		jmsConnection = amqConnectionFactory.createConnection();
		jmsConnection.start();
		
		jmsSession = jmsConnection.createSession(TRANSACTED, ACK_MODE);
		Destination destinationQueue = jmsSession.createQueue(NOVI_POLICY_MANAGER_QUEUE);
		
		jmsConsumer = jmsSession.createConsumer(destinationQueue);
		jmsConsumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message incomingEventMessage) {
		
		try {
			log.info("[Policy Manager] Message listener receive message ");
			log.info("[Policy Manager] Message ID : "+ incomingEventMessage.getJMSMessageID());
			log.info("[Policy Manager] Transmitting the message to Ponder2 component");
			
			boolean ponder2Action=true;
			boolean MonitoringEvent=false;
			
			incomingEventMessage.getClass();
			TextMessage transmitEvent= (TextMessage)incomingEventMessage;
			
			if (ponder2Action==true)
			{
				System.out.println("Mas esteile to ponder event gia na kanoume to update");
				String topo="topology";
				Collection<String> failingResources = null;
				RequestToIRM reqToIRM=new RequestToIRM();
				reqToIRM.callUpdateSliceFP(topo,failingResources);
			}
			
			if (MonitoringEvent==true)
			{
				MonitoringEvents MEvent= new MonitoringEvents();
				MEvent.setMonitoringEvent(transmitEvent);
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
	public String getPolicyManagerQueue() {
		return NOVI_POLICY_MANAGER_QUEUE;
	}
	public void setPolicyManagerQueue(String policyManagerQueue) {
		NOVI_POLICY_MANAGER_QUEUE = policyManagerQueue;
	}
	
		

}
*/