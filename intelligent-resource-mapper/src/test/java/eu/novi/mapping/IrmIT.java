/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

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
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.novi.framework.IntegrationTesting;

/**
 * Integration tests for Intelligent Resource Mapper.
 * 
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class IrmIT {
	
	private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
	private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
	
	private static final String EMBEDDING_SERVICE = "eu.novi.mapping.embedding.EmbeddingAlgorithmInterface";
	private static final String IRM_SERVICE = "eu.novi.mapping.RemoteIRM";
	private static final String RIS_SERVICE = "eu.novi.resources.discovery.IRMCalls";
	private static final String NOVI_API_FEEDBACK_SERVICE = "eu.novi.feedback.event.ReportEvent";
	
	@Configuration 
    public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"communications",
				"irm-solver","resource-information-service","embedding",
				"embedding-federica","embedding-planetlab","jung-osgi",
				"jackson-osgi","feedback", "glpk-java-osgi");
    }
	@Inject
	private BundleContext bundleContext;
	private BrokerService broker;

	@Before
	public void setup() throws Exception {
		BrokerService broker = new BrokerService();
		broker.setUseJmx(false);
		broker.addConnector("tcp://localhost:61616");
		broker.setUseShutdownHook(false);
		broker.start();


		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

		bundleContext.registerService(
				javax.jms.ConnectionFactory.class.getName(), connectionFactory,
				new Hashtable<Object, Object>());
	}
	
	@After
	public void shutdown() throws Exception {
		if(null != broker) {
			broker.stop();
		
		}
	}
	
	@Ignore
	@Test
	public void findPlanetLabEmbeddingService(BundleContext ctx) throws Exception {
		
		// Adding delay to ensure that all the bundles are loaded during configuration
		waitSafe();
		
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(EMBEDDING_SERVICE, TESTBED_PLANETLAB);
		
		assert(serviceReferences != null);
		assertEquals(1, serviceReferences.length);
	}
	@Ignore
	@Test
	 public void findFedericaEmbeddingService(BundleContext ctx) throws Exception {
		
		// Adding delay to ensure that all the bundles are loaded during configuration
		waitSafe();
		
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(EMBEDDING_SERVICE, TESTBED_FEDERICA);
	  
		assert(serviceReferences != null);
		assertEquals(1, serviceReferences.length);
	 }
	@Ignore
	@Test 
	public void findRIS(BundleContext ctx) throws Exception {
		
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(RIS_SERVICE, null);
		assert(serviceReferences != null);
		//assertEquals(1, serviceReferences.length);
		
	}
	
	
	@Ignore
	@Test
	public void findNoviApiFeedback(BundleContext ctx) throws Exception {
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		ServiceReference[] serviceReferences = ctx.getServiceReferences(NOVI_API_FEEDBACK_SERVICE, null);
		assert(serviceReferences != null);
		assertEquals(1, serviceReferences.length);
	}
	
	
	@Ignore
	@Test
	public void findLogservice(BundleContext ctx) {
		// Testing log service
		ServiceTracker loggerTracker = new ServiceTracker(ctx,LogService.class.getName(), null);
		loggerTracker.open();
		LogService logger = (LogService)loggerTracker.getService();
		assert(logger != null);
		logger.log(LogService.LOG_INFO,"First!");
	}
	
	@Ignore
	@Test
	public void callRemoteIRMs(BundleContext ctx) throws Exception {
		
		assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(IRM_SERVICE, null);
		
		assert(serviceReferences != null);
		
			
	}
	
	private static void waitSafe() {
		try {
			 Thread.currentThread();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
