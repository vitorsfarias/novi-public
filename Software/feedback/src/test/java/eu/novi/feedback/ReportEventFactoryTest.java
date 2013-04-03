package eu.novi.feedback;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;

public class ReportEventFactoryTest {
	static final String VMBroker ="vm://localhost?broker.persistent=false"; 

	@Test
	public final void testGetReportEvent() {
		ReportEvent eventReporter = ReportEventFactory.getReportEventBrokerURL(VMBroker);
		assertNotNull(eventReporter);
	}

}
