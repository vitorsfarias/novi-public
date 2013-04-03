package eu.novi.ponder2.policy;

import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.BasicAuthModule;
import junit.framework.TestCase;

public class AuthorisationModuleTest extends TestCase {

	public AuthorisationModuleTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSetRootDomain() {
		AuthorisationModule a = new BasicAuthModule(null);
		assertNull(a.rootDomain);
		a.setRootDomain(P2Object.create(7));
		assertNotNull(a.rootDomain);
		assertEquals(a.rootDomain.toString(), "7");
	}

}
