package eu.novi.federation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import eu.novi.framework.IntegrationTesting;

/**
 * Integration tests for federated resources in PlanetLab and FEDERICA.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class FederatedResourcesIT {
	private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
	private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
	private static final String TESTBED_ALL = null;
	
	private static final String RESOURCE_SERVICE = "eu.novi.resources.Resource";
	private static final String ROUTER_SERVICE = "eu.novi.resources.Router";
	private static final String NODE_SERVICE = "eu.novi.resources.Node";
	
	@Configuration 
    public static Option[] configuration() throws Exception {
        return IntegrationTesting.createConfigurationWithBundles(
        		"resources-federica", "resources-planetlab");
    }

	/*IT tests temporary commented
	@Test
	public void findAllResources(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null);
		assertEquals(3, ctx.getServiceReferences(RESOURCE_SERVICE, TESTBED_ALL).length);
	}
	
	@Test
	public void findPlanetLabSliver(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null);
		assertEquals(1, ctx.getServiceReferences(NODE_SERVICE, TESTBED_PLANETLAB).length);
	}
	
	@Test
	public void findFEDERICAVM(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null);
		assertEquals(1, ctx.getServiceReferences(NODE_SERVICE, TESTBED_FEDERICA).length);
	}
	
	@Test
	public void findFEDERICARouter(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null);
		assertEquals(1, ctx.getServiceReferences(ROUTER_SERVICE, TESTBED_FEDERICA).length);
	}
	*/
}
