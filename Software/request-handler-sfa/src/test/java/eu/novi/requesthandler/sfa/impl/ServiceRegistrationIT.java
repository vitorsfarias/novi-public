/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.novi.framework.IntegrationTesting;
import eu.novi.requesthandler.sfa.FederatedTestbed;

import static org.junit.Assert.assertNotNull;

@RunWith(org.ops4j.pax.exam.junit.JUnit4TestRunner.class)
public class ServiceRegistrationIT {

	@Configuration
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles(
//				"geronimo-servlet_2.5_spec",
				"xmlrpc-client-osgi",
				"information-model",
				"request-handler",
				"nswitch-manager");
	}
	
	
	
	public void findRequestHandlerSFAService(BundleContext ctx) throws Exception {
		ServiceReference[] s = ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		   for(ServiceReference ref : s){
	        	if(ref.toString().contains("eu.novi"))
	        		System.out.println("Check "+ref);
	        }
		final ServiceReference ref = ctx.getServiceReference(FederatedTestbed.class.getName());
		assertNotNull(ref);
		ctx.ungetService(ref);
	}
}
