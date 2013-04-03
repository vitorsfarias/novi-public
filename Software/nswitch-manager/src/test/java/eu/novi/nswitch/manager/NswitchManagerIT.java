package eu.novi.nswitch.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

//import eu.novi.framework.IntegrationTesting;

@RunWith(JUnit4TestRunner.class)
public class NswitchManagerIT {

	private static final String NSWITCH_MANAGER = "eu.novi.nswitch.manager.NswitchManager";
	private static final String NSWITCH = "eu.novi.nswitch.Nswitch";

	private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
	private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
	private static final String TESTBED_ALL = null;

	

	//@Configuration
	/* static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles("nswitch", "information-model", "nswitch-planetlab",
				"nswitch-federica");
	}*/

	//@Test
	public void findNswitchManager(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service
												// is not found

		ServiceReference[] serviceReferences = ctx.getServiceReferences(NSWITCH_MANAGER, null);
		assertEquals(1, serviceReferences.length );
	}

	//@Test
	public void findNswitch(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service
												// is not found

		ServiceReference[] serviceReferences = ctx.getServiceReferences(NSWITCH, TESTBED_ALL);
		assertEquals(2, serviceReferences.length );
	}

	
	//@Test
	public void findNswitchPlanetlab(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service
												// is not found

		ServiceReference[] serviceReferences = ctx.getServiceReferences(NSWITCH, TESTBED_PLANETLAB);
		assertEquals(1, serviceReferences.length );
	}
	
	//@Test
	public void findNswitchFederica(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); // workaround - sometimes service
												// is not found

		ServiceReference[] serviceReferences = ctx.getServiceReferences(NSWITCH, TESTBED_FEDERICA);
		assertEquals(1, serviceReferences.length );
	}


}
