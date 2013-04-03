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

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import eu.novi.framework.IntegrationTesting;

/**
 * Integration tests for Intelligent Resource Mapper.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class FederationIT {
	private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
	private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
	private static final String TESTBED_ALL = null;
	
	private static final String EMBEDDING_SERVICE = "eu.novi.mapping.embedding.EmbeddingAlgorithmInterface";
	
	//@Configuration 
    public static Option[] configuration() throws Exception {
        return IntegrationTesting.createConfigurationWithBundles(
        		"irm-solver", "resource-information-service",
        	"embedding", "embedding-federica", "embedding-planetlab", "jackson-osgi", "feedback");
    }

	//@Test
	public void findAllEmbeddingServices(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found
		assertEquals(2, ctx.getServiceReferences(EMBEDDING_SERVICE, TESTBED_ALL).length);
	}
	
	//@Test
	public void findPlanetLabEmbeddingService(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); //workaround - sometimes service is not found
		assertEquals(1, ctx.getServiceReferences(EMBEDDING_SERVICE, TESTBED_PLANETLAB).length);
	}
	
	//@Test
	public void findFEDERICAEmbeddingService(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); //workaround - sometimes service is not found
		assertEquals(1, ctx.getServiceReferences(EMBEDDING_SERVICE, TESTBED_FEDERICA).length);
	}
	/*
	@Test
	public void findIRM(BundleContext ctx) throws Exception {
		ctx.getServiceReferences(null, null); //workaround - sometimes Resource Discovery service is not found
		assertEquals(1, ctx.getServiceReferences(IRM_SERVICE, TESTBED_ALL).length);
	}
	*/
}
