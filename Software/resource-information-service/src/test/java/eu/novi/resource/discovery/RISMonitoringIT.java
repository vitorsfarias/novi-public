package eu.novi.resource.discovery;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import eu.novi.framework.IntegrationTesting;

import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.UsernameRSAKey;
import eu.novi.monitoring.util.MonitoringQuery;
import eu.novi.resources.discovery.database.communic.MonitoringAvarInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;

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
 * Integration tests for RIS - Monitoring
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class RISMonitoringIT {

	private static void waitSafe() {
		try {
			 Thread.currentThread().sleep(8000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	@Configuration 
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
				"xmlrpc-client-osgi",
				"policy-manager-AA",
				"monitoring-service",
				"communications",
				"request-handler-sfa",
				"jackson-osgi",
				"information-model",
				"feedback"
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


	
	/*@Test
	public void monitoringTest(BundleContext ctx) throws Exception {
		
		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);
        for(ServiceReference ref : allServRef){
        	if(ref.toString().contains("eu.novi"))
        		System.out.println("Check "+ref);
        }

        System.out.println("Checing find monitoring service ");
        MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
        UsernameRSAKey cred = new UsernameRSAKey("novi_novi","/home/maven/.ssh/id_dsa","");
        assertNotNull(service);
        assertNotNull(cred);
        final String query = ContructMonQuery.getTestQuery();
        assertNotNull(query);
        String result = service.substrate(cred, query);
        assertNotNull(result);
        System.out.println("The results:\n" + result);
        

	}*/
	
	
	@Test
	public void testMonitoringQuery(BundleContext ctx) throws Exception {
		waitSafe();
		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);


		MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");
		assertNotNull(service);
		assertNotNull(cred);


		MonitoringQuery q = service.createQuery();
		q.addFeature("measureMemoryInfo", "FreeMemory");
		q.addResource("measureMemoryInfo", "smilax1", "Node");
		q.addInterface("smilax1", "ifin", "hasInboundInterface");
		q.addInterface("smilax1", "ifout", "hasOutboundInterface");
		q.defineInterface("ifin","150.254.160.19", "hasIPv4Address");
		q.defineInterface("ifout","150.254.160.19", "hasIPv4Address");
		String query = q.serialize();
		System.out.println("The query is:\n" + query);
		String res = service.substrate(cred, query);
		System.out.println("The results are:\n" + res);
		assertTrue( 20 < res.split("\n").length);        

	}
	
	@Test
	public void testComplexMonitoringQuery(BundleContext ctx) throws Exception {
		waitSafe();
		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);


		MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");
		assertNotNull(service);
		assertNotNull(cred);

		String node = "smilax1";

		MonitoringQuery q = service.createQuery();
		//free memory
		q.addFeature("measureMemoryInfo", "FreeMemory");
		q.addResource("measureMemoryInfo", node, "Node");
		//free disk  space
		q.addFeature("measureDiskInfo", "FreeDiskspace");
		q.addResource("measureDiskInfo", node, "Node");
		//CPU load
		q.addFeature("measureCPULoad", "CPULoad");
		q.addResource("measureCPULoad", node, "Node");
		//CPU cores
		q.addFeature("measureCPUcores", "CPUCores");
		q.addResource("measureCPUcores", node, "Node");
		q.addInterface(node, "ifin", "hasInboundInterface");
		q.addInterface(node, "ifout", "hasOutboundInterface");
		q.defineInterface("ifin","150.254.160.19", "hasIPv4Address");
		q.defineInterface("ifout","150.254.160.19", "hasIPv4Address");
		String query = q.serialize();
		assertNotNull(query);
		System.out.println("The query is:\n" + query);
		String res = service.substrate(cred, query);
		System.out.println("The results are : \n" + res);
		assertNotNull(res);       

	}
	
	
	//@Test
	//Commented to be fixed tomorrow, fail due to some latest changes in monitoring 
	public void testAvarageUtilMonitoringQuery(BundleContext ctx) throws Exception {
		System.out.println("Testing monitoring average utilization");
		waitSafe();
		assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);


		MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");
		assertNotNull(service);
		assertNotNull(cred);

		String node = "urn:publicid:IDN+novipl:novi+node+smilax1.man.poznan.pl";
		
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(service);
		mon.setPlSfademoKeyPath("/home/jenkins/.ssh/sfademo_key");

		Set<String> nodes = new HashSet<String>();
		nodes.add(node);
		Set<MonitoringAvarInfo> answer = mon.getNodesMonUtilData(nodes);
		assertEquals(1, answer.size());
		MonitoringAvarInfo monInfo = answer.iterator().next();
		assertTrue(monInfo.getCpuUtil() != -1);
		assertTrue(monInfo.getMemoryUtil() != -1);
		assertTrue(monInfo.getStorageUtil() != -1);
	     

	}
	
	/**read a file for IT or JUnit test
	 * @param file
	 * @return
	 */
	private String readFile(String file)
	{
		Bundle bundle = null;
		try {
			bundle = FrameworkUtil.getBundle(RISMonitoringIT.class);
		} catch (NoClassDefFoundError e1) {
			System.out.println("Problem to get the bundle. Probaply is not running in" +
					"Service Mix");
			e1.printStackTrace();
			System.out.println();
		}
		BufferedReader fileReader = null;
		InputStream resourceInputStream = null;
		if (bundle != null) {
			try {
				resourceInputStream = bundle.getEntry(file).openStream();
				System.out.println("This one is getting from bundle");
	
			} catch (IOException e) {
				System.out.println("Bundle can't find resource, no file for RIS");
				return null;
			}
		} 
		else
		{
			System.out.println("Bundle is null");
			//for JUit test
			try {
				resourceInputStream = new FileInputStream(
						RISMonitoringIT.class.getResource("/" + file).getPath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		fileReader = new BufferedReader(new InputStreamReader(resourceInputStream));
		return fileReader.toString();

	}
	

}
