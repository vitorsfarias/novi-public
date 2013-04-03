package eu.novi.feedback;

import javax.jms.JMSException;

import eu.novi.feedback.queue.FeedbackQueueProducer;
import junit.framework.TestCase;

public class FeedbackQueueProducerTest extends TestCase {


	static final String VMBroker ="vm://localhost?broker.persistent=false"; 
	
	public final void testInitialize() throws JMSException {
		FeedbackQueueProducer fqp = new FeedbackQueueProducer();
		assert(fqp != null);
		fqp.setBroker(VMBroker);
		fqp.initialize();
	}

	public final void testGettingBrokerURL() throws JMSException {
		FeedbackQueueProducer fqp = new FeedbackQueueProducer();
		assert(fqp != null);
		fqp.setBroker(VMBroker);
		assert(fqp.getBrokerURL().equals(VMBroker));
	}
	
	public final void testReportingEvent() throws JMSException {
		FeedbackQueueProducer fqp = new FeedbackQueueProducer();
		assert(fqp != null);
		fqp.setBroker(VMBroker);
		fqp.initialize();
		fqp.reportErrorEvent("someSession", "Some Component", "Something Happened","Nothing");
		fqp.reportInstantEvent("someSession", "Some Component", "Something Happened","Nothing");
		fqp.reportStartEvent("startEvent", "someID", "Some Component", "some event", "some link");
		fqp.reportStopEvent("startEvent", "someID", "Some Component", "some event", "some link");
		
		
	}

}
