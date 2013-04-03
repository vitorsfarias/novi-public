package eu.novi.api.request.handler;

import static org.junit.Assert.assertNotNull;

import java.util.Hashtable;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.novi.api.request.queue.RequestListener;
import eu.novi.feedback.FeedbackHandler;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.framework.IntegrationTesting;

@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class RequestHandlerIT {
	

	@Configuration 
    public static Option[] configuration() throws Exception {

		return IntegrationTesting.createConfigurationWithBundles("information-model","activemq-core","org.apache.servicemix.specs.jsr311-api-1.0",
				"geronimo-jms_1.1_spec","geronimo-jta_1.1_spec","geronimo-j2ee-management_1.1_spec");
    }
	
	
	@Inject
	private BundleContext bundleContext;
	private BrokerService broker;
	private ActiveMQConnectionFactory connectionFactory;

	@Before
	public void setup() throws Exception {
		BrokerService broker = new BrokerService();
		broker.setUseJmx(false);
		broker.addConnector("tcp://localhost:61616");
		broker.setUseShutdownHook(false);
		broker.start();
		connectionFactory = new ActiveMQConnectionFactory();

		bundleContext.registerService(
				javax.jms.ConnectionFactory.class.getName(), connectionFactory,
				new Hashtable());
	}
	
	@After
	public void shutdown() throws Exception {
		if(null != broker) {
			broker.stop();
		
		}
	}
	

	//@Test
	public void findRequestHandler(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(RequestListener.class.getName());
		ctx.ungetService(ref);
		assertNotNull(ref);
		assert(true);

	}
	
	//@Test
	public void findReportEvent(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(ReportEvent.class.getName());
		ctx.ungetService(ref);
		assertNotNull(ref);
		assert(true);
	}
	
	//@Test
	public void findFeedbackHandler(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(FeedbackHandler.class.getName());
		ctx.ungetService(ref);
		assertNotNull(ref);
		assert(true);
	}
	
	
}
 