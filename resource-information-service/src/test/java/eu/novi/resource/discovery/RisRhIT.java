package eu.novi.resource.discovery;


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
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import eu.novi.framework.IntegrationTesting;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;

/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * Integration tests for RIS - Request Handler
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class RisRhIT {


	private static final String REQUEST_HANDLER_SERVICE = 
			"eu.novi.requesthandler.sfa.FederatedTestbed";
	private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
	private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";

	@Configuration 
	public static Option[] configuration() throws Exception {
		// Chariklis configuration:
		return IntegrationTesting.createConfigurationWithBundles(
				"xmlrpc-client-osgi",
				"communications",
				"nswitch",
				"nswitch-planetlab",
				"nswitch-federica",
				"nswitch-manager",
				"alibaba-osgi",
				"policy-manager-AA",
				"monitoring-service",
				"request-handler-sfa",
				"jackson-osgi",
				"information-model",
				"feedback");

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
//	@Ignore
	public void findRequestHandler(BundleContext ctx) throws Exception {
		Thread.sleep(30000);
		assert(ctx != null);
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
        for(ServiceReference ref : allServRef){
        	if(ref.toString().contains("eu.novi"))
        		System.out.println("Check "+ref);
        }
		/*final ServiceReference ref = ctx.getServiceReference(FederatedTestbed.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);*/
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				FederatedTestbed.class.getName(), null);
		// Should the result be "2"?
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		

		// Testing log service
		ServiceTracker loggerTracker = new ServiceTracker(ctx,LogService.class.getName(), null);
		loggerTracker.open();
		LogService logger = (LogService)loggerTracker.getService();
		assert(logger != null);
		logger.log(serviceReferences[0],LogService.LOG_INFO,"First!");

	}
	
	
	
	@Test     @Ignore
	public void getRequestHandlerObject(BundleContext ctx) throws InvalidSyntaxException
	{
		
		assertTrue(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found

		/*System.out.println("Testing Fedeerica");
		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				FederatedTestbed.class.getName(), TESTBED_FEDERICA);
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		final FederatedTestbed requestHandlCall = (FederatedTestbed) 
				ctx.getService(serviceReferences[0]);
		assertNotNull(requestHandlCall);
		//requestHandlCall.createSlice("", "myTopology", (TopologyImpl) createTopology());
		RHListResourcesResponseImpl  response = requestHandlCall.listResources("");
		assertFalse(response.hasError());
		assertTrue(response.getPlatform().getContains().size() > 0);
		System.out.println("Error message : " + response.getErrorMessage());
		
		//test PlanetLab
		System.out.println("Testing Planetlab");
		serviceReferences = ctx.getServiceReferences(
				FederatedTestbed.class.getName(), TESTBED_PLANETLAB);
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		final FederatedTestbed requestHandlCallPl = (FederatedTestbed) 
				ctx.getService(serviceReferences[0]);
		assertNotNull(requestHandlCallPl);
		//requestHandlCall.createSlice("", "myTopology", (TopologyImpl) createTopology());
		response = requestHandlCallPl.listResources("");
		assertFalse(response.hasError());
		assertTrue(response.getPlatform().getContains().size() > 0);
		System.out.println("Error message : " + response.getErrorMessage());*/
		
		ServiceReference[] serviceReferences = ctx.getServiceReferences(
				FederatedTestbed.class.getName(), null);
		// Should the result be "2"?
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		
		//ServiceReference[] serviceReferences = ctx.getServiceReferences(REQUEST_HANDLER_SERVICE, null);
		for (ServiceReference serv : serviceReferences)
		{
			final FederatedTestbed requestHandlCall = (FederatedTestbed) ctx.getService(serv);
			assertNotNull(requestHandlCall);
			//requestHandlCall.createSlice("", "myTopology", (TopologyImpl) createTopology());
			System.out.println("Calling Request Handler list resources...");
			RHListResourcesResponseImpl  response = requestHandlCall.listResources("");
			assertNotNull(response);
			//System.out.println("Error message : " + response.getErrorMessage());
			assertFalse(response.hasError());
			assertTrue(response.getPlatformString() != null);
			IMRepositoryUtil repUtil = new IMRepositoryUtilImpl();
			Platform platformSubs = repUtil.getIMObjectFromString(
					response.getPlatformString(), Platform.class);
			assertNotNull(platformSubs);
			assertTrue(platformSubs.getContains().size() > 0);
			

		}
		   
		
	}
	
	
	
	
	private Topology createTopology()
	{
		Topology topology = new TopologyImpl("myTopology");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		vNode1.setHardwareType("x86");
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		topology.setContains(IMUtil.createSetWithOneValue(vNode1));
		return topology;
	}

}
