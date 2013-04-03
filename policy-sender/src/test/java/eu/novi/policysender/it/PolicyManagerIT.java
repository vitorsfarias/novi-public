package eu.novi.policysender.it;
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
 * 
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import eu.novi.framework.IntegrationTesting;
//import eu.novi.mail.mailclient.SSendEmail;
import eu.novi.mapping.RemoteIRM;
import eu.novi.policysender.interfaces.interfaceforponder2;
import eu.novi.policysender.requestToIRM.RequestToIRM;


@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class PolicyManagerIT {

	private static final String IRM_SERVICE = "eu.novi.mapping.RemoteIRM";
//	private static final String mail_SERVICE = "eu.novi.mail.mailclient.InterfaceForMail";
	
	@Configuration 
    public static Option[] configuration() throws Exception {
        return IntegrationTesting.createConfigurationWithBundles(
        	//	"mail-osgi",
  //      		"geronimo-j2ee-management_1.1_spec",
   //     		"geronimo-jms_1.1_spec",
        		//"activemq-core",
        		"communications",
        		"intelligent-resource-mapper",
        		"resource-information-service",
        		"information-model",
        		"qdparser-osgi",
        		"request-handler-sfa",
        		"xmlrpc-client-osgi",
        	//	"org.apache.commons.codec",
        		"feedback"
        		);
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
	public void findLogservice(BundleContext ctx) {
		// Testing log service
		ServiceTracker loggerTracker = new ServiceTracker(ctx,LogService.class.getName(), null);
		loggerTracker.open();
		LogService logger = (LogService)loggerTracker.getService();
		assert(logger != null);
		logger.log(LogService.LOG_INFO,"First!");
	}
	
    @Test
    @Ignore
	public void findIntelligentResourceMapperService(BundleContext ctx) throws Exception {
    	System.out.println("Checking find IRM service ");
    	assertNotNull(ctx);
		//workaround - sometimes  service is not found
		ServiceReference [] allServRef  = ctx.getServiceReferences(null, null);
        for(ServiceReference ref : allServRef){
      //  	if(ref.toString().contains("eu.novi"))
        		System.out.println("Check "+ref);
        }

        System.out.println("Checking find IRM service ");
   	    ServiceReference[] serviceReferences = ctx.getServiceReferences(IRM_SERVICE, null);
   	    assertNotNull(serviceReferences);
		assertEquals(1, serviceReferences.length);    	
	}
        
	
	
    @Test
 //   @Ignore
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
	
	 @Test
	 @Ignore
		public void callUpdateSlice(BundleContext ctx) throws InvalidSyntaxException
		{
			ServiceReference[] serviceReferences = ctx.getServiceReferences(IRM_SERVICE, null);
			for (ServiceReference serv : serviceReferences)
			{
				System.out.println("Cheking IRM get object...");
				final RemoteIRM irm = (RemoteIRM) ctx.getService(serv);
				assertNotNull(irm);
				Set<String> failingResources = new HashSet<String>();
				failingResources.add("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr");
				Collection<String> result = irm.updateSlice("sessionID","midtermWorkshopSlice", failingResources);
				System.out.println("We called IRM for updateslice: the result is "+ result);
			}
				
				
			}
	 
/*	 @Test
	 @Ignore
	 	public void callSendEmail(BundleContext ctx) {
		    SSendEmail sentToUser = new SSendEmail();
			String theUser="ykryftis@netmode.ece.ntua.gr";
			String ResourcesText="Einai apo to IT";
			sentToUser.SendEmail(theUser,ResourcesText);
		}*/
		
}
