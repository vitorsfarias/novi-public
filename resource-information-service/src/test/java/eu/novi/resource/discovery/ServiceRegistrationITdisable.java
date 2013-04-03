package eu.novi.resource.discovery;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Set;

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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import eu.novi.framework.IntegrationTesting;
import eu.novi.im.core.Group;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.resources.discovery.IRMCalls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class ServiceRegistrationITdisable {

	@Ignore
	@Configuration
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"alibaba-osgi",
				"topology",
				"policy-manager-AA",
				"request-handler-sfa",
				"information-model",
				"feedback");
	}

	@Test
	@Ignore
	public void findResourceInformationService(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(IRMCalls.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);
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

	@Test @Ignore
	public void findIMRU(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		final ServiceReference ref = ctx.getServiceReference(IMRepositoryUtil.class.getName());
		IMRepositoryUtil myUtil = (IMRepositoryUtil) ctx.getService(ref);
		
		Bundle bundle = FrameworkUtil.getBundle(ServiceRegistrationITdisable.class);
		String requestedOWL = readStream(bundle.getEntry("MidtermWorkshopRequest.owl").openStream());
		
		Set<Group> groupies = myUtil.getGroupImplFromFile(requestedOWL);
		assert(groupies != null);
		assertEquals(3,groupies.size());
		ctx.ungetService(ref);
		assertNotNull(ref);
		assert(true);
	}

	private static String readStream(InputStream input)	throws java.io.IOException {
    	StringBuffer fileData = new StringBuffer(1000);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(input) );
    	String line = reader.readLine();
    	while(line !=null){
    		fileData.append(line);
    		line = reader.readLine();
    	}
    	return fileData.toString();
	}
}
