package eu.novi.api.request.queue;

import static org.junit.Assert.*;

import javax.jms.JMSException;

import org.junit.Test;

public class RequestQueueProducerTest {
	static final String VMBroker ="vm://localhost?broker.persistent=false"; 
	
	@Test
	public final void testRequestQueueProducer() throws JMSException {
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		//rqp.initialize();
		assertEquals(rqp.getBrokerURL(), VMBroker);
	}


}
