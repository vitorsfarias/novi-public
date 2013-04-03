package eu.novi.feedback;

import javax.jms.JMSException;

import eu.novi.feedback.queue.FeedbackQueueListener;
import eu.novi.feedback.queue.FeedbackQueueProducer;
import junit.framework.TestCase;

public class FeedbackQueueListenerTest extends TestCase {


	static final String VMBroker ="vm://localhost?broker.persistent=false"; 
	
	public final void testInitialize() throws JMSException {
		FeedbackQueueListener fql = new FeedbackQueueListener ();
		fql.setBrokerURL(VMBroker);
		fql.initialize();
		assert(fql != null);
	}

	public final void testGettingDefaultBrokerURL() throws JMSException {
		FeedbackQueueListener fql = new FeedbackQueueListener ();
		assert(fql != null);
		fql.setBrokerURL(VMBroker);
		assertEquals(fql.getBrokerURL(), VMBroker);
		
	}
	
	public final void testReportingErrorEvent() throws JMSException {
		FeedbackQueueListener fql = new FeedbackQueueListener ();
		assert(fql != null);
		fql.setBrokerURL(VMBroker);
		fql.setUserFeedbackData(new UserFeedbackData());
		assert(fql.getUserFeedbackData() != null);
	}


}
