package eu.novi.federation;

/**
 * Unit test case for slice configuration.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 * 
 */
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.novi.resources.Resource;

public class SliceConfigurationTest {

	protected Slice slice;
	protected Resource resource1, resource2;
	protected FederatedTestbed federatedTestbed; 
	
	@Before
	public void givenSliceWithResources() throws Exception {
		slice = new Slice();
		resource1 = mock(Resource.class);
		resource2 = mock(Resource.class);
		slice.addResource(resource1);
		slice.addResource(resource2);
		
		federatedTestbed = mock(FederatedTestbed.class);
		slice.configure(federatedTestbed);
	}

	@After
	public void tearDown() throws Exception {
		slice = null;
		resource1 = null;
		resource2 = null;
		federatedTestbed = null;
	}

	@Test
	public void checkFederatedTestbedConfigured() {
		verify(federatedTestbed).configure(slice);
	}
	
	@Test
	public void checkResource1Configured() {
		verify(resource1).configure();
	}
	
	@Test
	public void checkResource2Configured() {
		verify(resource2).configure();
	}
}
