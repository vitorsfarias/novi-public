package eu.novi.resource.discovery;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
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

import eu.novi.framework.IntegrationTesting;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.policyAA.interfaces.InterfaceForRIS;

/**
 * 
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
 * Integration tests for RIS - Policy
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class RisPolicyIT {


	private static final String POLICY_SERVICE_AA = 
			"eu.novi.policyAA.interfaces.InterfaceForRIS";

	@Configuration 
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"communications",
				"jackson-osgi",
				"feedback",
				"information-model",
				"monitoring-service",
				"policy-manager-AA",
				"request-handler-sfa"
				);
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

	
	public static void waitSafe() {
		try {
			 Thread.currentThread().sleep(8000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Test
	public void findPolicyServiceAA(BundleContext ctx) throws Exception {
		
		waitSafe(); 

		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);
        for(ServiceReference ref : allServRef){
        	if(ref.toString().contains("eu.novi"))
        		System.out.println("Check "+ref);
        }

        System.out.println("Checing find policy service ");
		ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_SERVICE_AA, null);
		// Should the result be "2"?
		assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);
		
		/*assert(ctx != null);
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found

		final ServiceReference ref = ctx.getServiceReference(InterfaceForRIS.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);*/


	}
	
	
	@Test
	@Ignore
	public void getPolicyServiceObject(BundleContext ctx) throws InvalidSyntaxException
	{
		waitSafe();
		ServiceReference[] serviceReferences = ctx.getServiceReferences(POLICY_SERVICE_AA, null);
		for (ServiceReference serv : serviceReferences)
		{
			System.out.println("Cheking policy get object...");
			final InterfaceForRIS policyCall = (InterfaceForRIS) ctx.getService(serv);
			assertNotNull(policyCall);
			NOVIUserImpl user = new NOVIUserImpl("sfademo@barcelona.com");
			assertNotNull(policyCall.AuthorizedForResource(null, user,
					new HashSet<String>(Arrays.asList(
							"http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr")), 
							new Integer(0)));
			System.out.println("Checking the addTopology");
			
			Topology top = new TopologyImpl("topology");
			top.setContains(IMUtil.createSetWithOneValue(new VirtualNodeImpl("vNode")));
			assertEquals(0, policyCall.AddTopology(null, top, "http://fp7-novi.eu/im.owl#slice_uri"));
			assertEquals(0, policyCall.RemoveTopology(null, top, "http://fp7-novi.eu/im.owl#slice_uri"));

			
		}
		
		/*final ServiceReference ref = ctx.getServiceReference(InterfaceForRIS.class.getName());
		final InterfaceForRIS policyCall = (InterfaceForRIS) ctx.getService(ref);
		assertNotNull(policyCall);
		assertNotNull(policyCall.AuthorizedForResource("User1",
				new HashSet<String>(Arrays.asList("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr"))));*/
		   
		
	}
	

}

