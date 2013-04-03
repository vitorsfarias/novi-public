package eu.novi.ponder2.policy;

import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.Policy;
import junit.framework.TestCase;

public class PolicyTest extends TestCase {

	public PolicyTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPolicy() {
		// this calls super():
		assertNotNull(new AuthorisationPolicy(P2Object.create(), "Foo",
				P2Object.create(), "Fred"));
	}

	private void SetIsActiveTest() {
		new SelfManagedCell(P2Object.create());
		AuthorisationPolicy p = new AuthorisationPolicy(P2Object.create(),
				"Foo", P2Object.create(), "Fred");
		assertFalse(p.isActive());
		p.setActive(true);
		assertTrue(p.isActive());
		p.setActive(false);
		assertFalse(p.isActive());
	}

	public void testSetActive() {
		System.out.println("PolicyTest.testSetActive():");
		SetIsActiveTest();
		System.out.println();
	}

	public void testIsActive() {
		System.out.println("PolicyTest.testIsActive():");
		SetIsActiveTest();
		System.out.println();
	}

	public void testGetDefaultDomain() {
		assertEquals(Policy.getDefaultDomain(), "/policy");
	}

	public void testOperation_active() {
		System.out.println("PolicyTest.testOperation_active():");
		new SelfManagedCell(P2Object.create());
		AuthorisationPolicy p = new AuthorisationPolicy(P2Object.create(),
				"Foo", P2Object.create(), "Fred");
		try {
			assertFalse(p.isActive());
			assertTrue(p.operation_active(true).asBoolean());
			assertTrue(p.isActive());
			assertFalse(p.operation_active(false).asBoolean());
			assertFalse(p.isActive());
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		}
		System.out.println();
	}

}
