package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.util.Set;

import eu.novi.ponder2.EventTemplateP2Adaptor;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.ObligationPolicy;

import junit.framework.TestCase;

public class P2ManagedObjectTest extends TestCase {

	P2ManagedObject mo;

	public P2ManagedObjectTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mo = new P2ManagedObject(P2Object.create(12));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mo = new P2ManagedObject(P2Object.create(12));
	}

	public void testP2ManagedObject() {
		assertNotNull(mo);
		assertTrue(mo instanceof P2ManagedObject);
		try {
			Field f = P2ManagedObject.class.getDeclaredField("parentSet");
			f.setAccessible(true);
			assertNotNull(f.get(mo));
			assertTrue(((Set) f.get(mo)).isEmpty());
			f = P2ManagedObject.class.getDeclaredField("eventListenerSet");
			f.setAccessible(true);
			assertNull(f.get(mo));
			f = P2ManagedObject.class
					.getDeclaredField("authorisationPolicySet");
			f.setAccessible(true);
			assertNull(f.get(mo));
			f = P2ManagedObject.class.getDeclaredField("oid");
			f.setAccessible(true);
			assertNotNull(f.get(mo));
			assertEquals(((OID) f.get(mo)).getManagedObject(), mo);
			f = P2ManagedObject.class.getDeclaredField("p2Object");
			f.setAccessible(true);
			assertNotNull(f.get(mo));
			assertEquals(((P2Object) f.get(mo)).asInteger(), 12);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testGetOID() {
		assertNotNull(mo.getOID());
		assertTrue(mo.getOID() instanceof OID);
		assertEquals(mo.getOID().getManagedObject(), mo);
	}

	public void testIsDomain() {
		assertFalse(mo.isDomain());
		mo.getOID().setDomain(true);
		assertTrue(mo.isDomain());
		mo.getOID().setDomain(false);
		assertFalse(mo.isDomain());
	}

	public void testGetP2Object() {
		assertNotNull(mo.getP2Object());
		assertTrue(mo.getP2Object() instanceof P2Number);
		try {
			assertEquals(mo.getP2Object().asInteger(), 12);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testAddParent() {
		// normal behaviour
		P2ManagedObject p1 = P2Object.create(1).getManagedObject();
		P2ManagedObject p2 = P2Object.create(2).getManagedObject();
		P2ManagedObject p3 = P2Object.create(3).getManagedObject();
		assertFalse(p1.equals(p2));
		assertFalse(p1.equals(p3));
		assertFalse(p2.equals(p3));
		assertTrue(mo.getParentSet().isEmpty());
		mo.addParent(p1);
		assertEquals(mo.parentCount(), 1);
		assertTrue(mo.getParentSet().contains(p1));
		mo.addParent(p2);
		mo.addParent(p3);
		assertEquals(mo.parentCount(), 3);
		assertTrue(mo.getParentSet().contains(p2));
		assertTrue(mo.getParentSet().contains(p3));
		mo.addParent(p1);
		assertEquals(mo.parentCount(), 3);
		mo.removeParent(p1);
		assertEquals(mo.parentCount(), 2);
		assertFalse(mo.getParentSet().contains(p1));
		mo.removeParent(p2);
		mo.addParent(p2);
		assertEquals(mo.parentCount(), 2);
		assertTrue(mo.getParentSet().contains(p2));
		mo.removeParent(p2);
		mo.removeParent(p3);
		assertEquals(mo.parentCount(), 0);

		// some special cases
		mo.removeParent(null);
		mo.removeParent(p1);
		assertEquals(mo.parentCount(), 0);
	}

	public void testRemoveParent() {
		testAddParent();
	}

	public void testGetParentSet() {
		testAddParent();
	}

	public void testParentCount() {
		testAddParent();
	}

	public void testApplyPolicy() {
		AuthorisationPolicy auth1 = new AuthorisationPolicy(P2Object.create(),
				"Foo", P2Object.create(), "Fred");
		AuthorisationPolicy auth2 = new AuthorisationPolicy(P2Object.create(),
				"Foo2", P2Object.create(), "Fred2");
		ObligationPolicy obl1 = new ObligationPolicy(P2Object.create(1));
		ObligationPolicy obl2 = new ObligationPolicy(P2Object.create(2));
		assertTrue(mo.getAuthorisationPolicies().isEmpty());
		assertTrue(mo.getEventListeners().isEmpty());
		mo.applyPolicy(auth1);
		assertTrue(mo.getAuthorisationPolicies().contains(auth1));
		assertTrue(mo.getEventListeners().isEmpty());
		mo.applyPolicy(obl1);
		mo.applyPolicy(obl2);
		assertEquals(mo.getAuthorisationPolicies().size(), 1);
		assertTrue(mo.getAuthorisationPolicies().contains(auth1));
		assertEquals(mo.getEventListeners().size(), 2);
		assertTrue(mo.getEventListeners().contains(obl1));
		assertTrue(mo.getEventListeners().contains(obl2));
		mo.removePolicy(auth1);
		assertTrue(mo.getAuthorisationPolicies().isEmpty());
		assertEquals(mo.getEventListeners().size(), 2);
		mo.removePolicy(obl1);
		assertEquals(mo.getEventListeners().size(), 1);
		assertTrue(mo.getEventListeners().contains(obl2));
		mo.removePolicy(obl2);
		assertTrue(mo.getEventListeners().isEmpty());
		assertTrue(mo.getAuthorisationPolicies().isEmpty());
	}

	public void testRemovePolicy() {
		testApplyPolicy();
	}

	public void testGetObligationPolicies() {
		testApplyPolicy();
	}

	public void testGetAuthoriisationPolicies() {
		testApplyPolicy();
	}

	public void testSendEvent() {
		try {
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			Event ev = new Event(null, evtmp, P2Object.create(7),
					P2Object.create(12));
			mo.applyPolicy(new ObligationPolicy(P2Object.create()));
			mo.addParent(P2Object.create().getManagedObject());
			mo.sendEvent(ev);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testCreate() {
		try {
			mo.create(null, "createFoo");
			fail("Exception expected");
		} catch (Ponder2Exception e) {
			assertEquals(
					e.toString(),
					"eu.novi.ponder2.exception.Ponder2OperationException: class eu.novi.ponder2.objects.P2Number: No such create operation - createFoo");
		}
	}

	public void testOperation() {
		try {
			assertEquals(mo.operation(null, "*", P2Object.create(3))
					.asInteger(), 3 * 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

}
