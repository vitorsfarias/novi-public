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
public class Ponder2PolicyManagerIT {
	
	private static final String POLICY_MANAGER_SERVICE = "eu.novi.policy.interfaces.interfaceforponder2";
	private static final String NOVI_API_REQUEST_SERVICE = "eu.novi.api.request.queue.RequestListener";
	@Configuration
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"org.apache.servicemix.bundles.antlr-runtime",
				"tuprolog-osgi",
				"qdparser-osgi",
				"geronimo-jta_1.1_spec",
				"geronimo-j2ee-management_1.1_spec",
				"information-model",
				"policy-manager",
				"jackson-osgi");
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
	public void findPS(BundleContext ctx) throws Exception {
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_MANAGER_SERVICE, null);
		assert(serviceReferences != null);
		for (ServiceReference serviceReference : serviceReferences) {
		System.out.println(serviceReference.getBundle().getSymbolicName());
		//assertEquals(1, serviceReferences.length);
		}
	}
	
	@Test
	@Ignore
	public void findNoviApiRequest(BundleContext ctx) throws Exception {
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		ServiceReference[] serviceReferences = ctx.getServiceReferences(NOVI_API_REQUEST_SERVICE, null);
		assert(serviceReferences != null);
		for (ServiceReference serviceReference : serviceReferences) {
		System.out.println(serviceReference.getBundle().getSymbolicName());
		
		}
		assertEquals(1, serviceReferences.length);
	}
	
	  @Test
	  @Ignore
		public void findPolicyManagerService(BundleContext ctx) throws Exception {
	    	System.out.println("Checking to find Policy Service ");
	    	//ctx.getServiceReferences(null, null);
	    	//System.out.println("..... ");
	    	assertNotNull(ctx);
			//workaround - sometimes  service is not found
			ServiceReference[] allServRef  = ctx.getServiceReferences(null, null);
	        for(ServiceReference ref : allServRef){
	        //	if(ref.toString().contains("eu.novi"))
	        		System.out.println("Check "+ref.toString());
	        }

	        System.out.println("End looking for Policy Service ");
	   	    ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_MANAGER_SERVICE, null);
	   	    assertNotNull(serviceReferences);
			assertEquals(1, serviceReferences.length);    	
		}
	  
	/*@Test
	public void findPolicyManagerService(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(interfaceforponder2.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);
	}*/
	
	@Test
	@Ignore
	public void findInterfaceForPonder2Service(BundleContext ctx) throws Exception {
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


/*	@Test
	@Ignore
	public void CallstoIRMtest(BundleContext ctx) throws Exception {
		assert (ctx != null);
		ctx.getServiceReferences(null, null); // workaround - sometimes service
												// is not found

		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				interfaceforponder2.class.getName(), null);
		// Should the result be "2"?
		assert (serviceReferences != null);
		assertEquals(1, serviceReferences.length);

		// Testing log service
		ServiceTracker loggerTracker = new ServiceTracker(ctx,
				LogService.class.getName(), null);
		loggerTracker.open();
		LogService logger = (LogService) loggerTracker.getService();
		assert (logger != null);
		logger.log(serviceReferences[0], LogService.LOG_INFO, "First!");
	}*/

	
	/*@Test
	public void callUpdateSlice3(BundleContext ctx)
			throws InvalidSyntaxException {
		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				interfaceforponder2.class.getName(), null);
		for (ServiceReference serv : serviceReferences) {
			final RequestToIRM req = (RequestToIRM) ctx.getService(serv);
			System.out.println("I will try");
			req.callUpdateSliceFP(null, null);
			System.out.println("We called Policy-Manager (we didn't checked that the execution was correct)");
		}
	}
	
	@Ignore
	@Test
	public void callUpdateSlice2(BundleContext ctx)
			throws InvalidSyntaxException {
		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				interfaceforponder2.class.getName(), null);
		for (ServiceReference serv : serviceReferences) {
			final RequestToIRM req = (RequestToIRM) ctx.getService(serv);
			req.callUpdateSliceFP(null, null);
			System.out.println("We called Policy-Manager (we didn't checked that the execution was correct)");
		}
	}*/
	
	/* @Test
		public void callUpdateSlice(BundleContext ctx) throws InvalidSyntaxException
		{
			ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_MANAGER_SERVICE, null);
			for (ServiceReference serv : serviceReferences)
			{
				final interfaceforponder2 polman = (interfaceforponder2) ctx.getService(serv);
				polman.callUpdateSliceFP(null, null);
				System.out.println("We called policy-manager (we didn't checked that the execution was correct (null arguments))");
			}
				
				
			}*/
	
}
