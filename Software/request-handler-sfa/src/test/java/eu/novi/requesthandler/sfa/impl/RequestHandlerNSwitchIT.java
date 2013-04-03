/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.novi.framework.IntegrationTesting;

public class RequestHandlerNSwitchIT {
	private static final String NSWITCH_MANAGER = "eu.novi.nswitch.manager.NswitchManager";

	@Configuration
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles("communications","nswitch", "information-model", "nswitch-planetlab",
				"nswitch-federica");
	}


	public void findNswitchService(BundleContext ctx) throws Exception {
		assert(ctx != null);
		ctx.getServiceReferences(null, null); // workaround - sometimes service is not found
		ServiceReference[] serviceReferences = ctx.getServiceReferences(NSWITCH_MANAGER, null);
		assertEquals(1, serviceReferences.length );

	}

}
