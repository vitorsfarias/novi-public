package eu.novi.ponder2.policy;

import java.lang.reflect.Field;

import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthorisationPolicy;

import junit.framework.TestCase;

public class AuthPolicyHolderTest extends TestCase {

	AuthPolicyHolder aph;
	AuthorisationPolicy p1;
	AuthorisationPolicy p2;

	public AuthPolicyHolderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		aph = new AuthPolicyHolder();
		p1 = new AuthorisationPolicy(P2Object.create(), "Foo",
				P2Object.create(), "Fred");
		p2 = new AuthorisationPolicy(P2Object.create(), "/Foo",
				P2Object.create(), "Fred2");
	}

	public void tearDown() throws Exception {
		super.tearDown();
		aph = null;
		p1 = null;
		p2 = null;
	}

	public void testAuthPolicyHolder() {
		assertNotNull(aph);
		assertTrue(aph instanceof AuthPolicyHolder);
	}

	public void testGetIncomingAuthPol() {
		aph.setIncomingAuthPol(p1);
		assertEquals(aph.getIncomingAuthPol(), p1);
		aph.setIncomingAuthPol(p2);
		assertEquals(aph.getIncomingAuthPol(), p2);
		assertFalse(p1.equals(p2));
	}

	public void testSetIncomingAuthPol() {
		try {
			Field f = AuthPolicyHolder.class
					.getDeclaredField("incomingAuthPol");
			f.setAccessible(true);
			aph.setIncomingAuthPol(p1);
			AuthorisationPolicy px = (AuthorisationPolicy) f.get(aph);
			assertEquals(px, p1);
			aph.setIncomingAuthPol(p2);
			px = (AuthorisationPolicy) f.get(aph);
			assertEquals(px, p2);
			assertFalse(p1.equals(p2));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testGetOutgoingAuthPol() {
		aph.setOutgoingAuthPol(p1);
		assertEquals(aph.getOutgoingAuthPol(), p1);
		aph.setOutgoingAuthPol(p2);
		assertEquals(aph.getOutgoingAuthPol(), p2);
		assertFalse(p1.equals(p2));
	}

	public void testSetOutgoingAuthPol() {
		try {
			Field f = AuthPolicyHolder.class
					.getDeclaredField("outgoingAuthPol");
			f.setAccessible(true);
			aph.setOutgoingAuthPol(p1);
			AuthorisationPolicy px = (AuthorisationPolicy) f.get(aph);
			assertEquals(px, p1);
			aph.setOutgoingAuthPol(p2);
			px = (AuthorisationPolicy) f.get(aph);
			assertEquals(px, p2);
			assertFalse(p1.equals(p2));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
