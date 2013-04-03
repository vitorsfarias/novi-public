package eu.novi.ponder2.integrationtests;

import static org.junit.Assert.*;

import java.util.Hashtable;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import eu.novi.framework.IntegrationTesting;
import eu.novi.mapping.IRMInterface;
import eu.novi.policy.RequestToIRM.RequestToIRM;
import eu.novi.policy.interfaces.interfaceforponder2;
import eu.novi.ponder2.SMCStartInterface;

/*
 * Integration Test for ponder2 communication with policy-manager
 */
@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class PolicyManagerIT {
	
	private static final String POLICY_MANAGER_SERVICE = "eu.novi.policy.interfaces.interfaceforponder2";
	@Configuration
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"communications",
				"jackson-osgi",
				"org.apache.servicemix.bundles.antlr-runtime",
				"tuprolog-osgi",
				"qdparser-osgi",
				"activemq-core",
				"information-model",
				"policy-manager",
				"resource-information-service",
				"intelligent-resource-mapper");
	}
	
	@Inject
	private BundleContext bundleContext;
	private BrokerService broker;
	private ActiveMQConnectionFactory connectionFactory;
	
	@Before
	public void setup() throws Exception {
		System.out.println("Before");
		BrokerService broker = new BrokerService();
		broker.setUseJmx(false);
		broker.addConnector("tcp://localhost:61616");
		broker.setUseShutdownHook(false);
		broker.start();

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

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
	
	@Test
	@Ignore
	public void findPolicyManagerService(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(interfaceforponder2.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);
	}
	
	@Test
	//@Ignore
	public void findInterfaceForPonder2Service(BundleContext ctx) throws Exception {
		waitSafe();
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		ServiceReference[] serviceReferences = ctx.getServiceReferences(null, null);
		int i = 0;
		for (ServiceReference serviceReference : serviceReferences) {
			System.out.println(i++);
			System.out.println(serviceReference.getBundle().getSymbolicName());
			System.out.println(serviceReference.getBundle().getBundleId());
		}
		//assertEquals(1, serviceReferences.length);
	}


	@Test
	@Ignore
	public void findPolicyService(BundleContext ctx) throws Exception {
		
		waitSafe(); 

		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);
        for(ServiceReference ref : allServRef){
        	if(ref.toString().contains("eu.novi"))
        		System.out.println("Check "+ref);
        }

        System.out.println("Checking find policy service ");
		ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_MANAGER_SERVICE, null);
		// Should the result be "2"?
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		
		/*assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found

		final ServiceReference ref = ctx.getServiceReference(InterfaceForRIS.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);*/


	}
	
	public static void waitSafe() {
		try {
			 Thread.currentThread().sleep(18000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
