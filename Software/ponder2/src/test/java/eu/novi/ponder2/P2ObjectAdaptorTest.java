package eu.novi.ponder2;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;

import junit.framework.TestCase;

import eu.novi.ponder2.EventTemplateP2Adaptor;

import org.jmock.Expectations;
import org.jmock.Mockery;

import eu.novi.ponder2.EventTemplate;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.BasicAuthModule;

public class P2ObjectAdaptorTest extends TestCase {

	P2ObjectAdaptor ad;

	public P2ObjectAdaptorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		ad = new P2ObjectAdaptor();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ad = null;
	}

	public void testP2ObjectAdaptor() {
		assertNotNull(ad);
		assertTrue(ad instanceof P2ObjectAdaptor);
	}

	public void testP2ObjectAdaptorP2ObjectStringP2ObjectArray() {
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"), P2Object.create("Fred2"));
		try {
			// this calls the method to test: super(source, operation, args)
			EventTemplateP2Adaptor evtmpad = new EventTemplateP2Adaptor(null,
					"create:", keys);
			assertNotNull(evtmpad);
			assertTrue(evtmpad instanceof P2ObjectAdaptor);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testSetObj() {
		Mockery m = new Mockery();
		ManagedObject m1 = m.mock(ManagedObject.class, "m1");
		ManagedObject m2 = m.mock(ManagedObject.class, "m2");
		assertFalse(m1.equals(m2));
		assertNull(ad.objImpl);
		ad.setObj(m1);
		assertEquals(ad.objImpl, m1);
		ad.setObj(m2);
		assertEquals(ad.objImpl, m2);
	}

	public void testGetObj() {
		Mockery m = new Mockery();
		ManagedObject m1 = m.mock(ManagedObject.class, "m1");
		ManagedObject m2 = m.mock(ManagedObject.class, "m2");
		assertFalse(m1.equals(m2));
		assertNull(ad.getObj());
		ad.setObj(m1);
		assertEquals(ad.getObj(), m1);
		ad.setObj(m2);
		assertEquals(ad.getObj(), m2);
	}

	public void testSetAuthorisation() {
		try {
			Field f = P2ObjectAdaptor.class
					.getDeclaredField("authorisationModule");
			f.setAccessible(true);
			assertNull(f.get(null));
			AuthorisationModule a = new BasicAuthModule(null);
			P2ObjectAdaptor.setAuthorisation(a);
			assertNotNull(f.get(null));
			assertEquals(f.get(null), a);
			AuthorisationModule b = new BasicAuthModule(null);
			assertFalse(a.equals(b));
			P2ObjectAdaptor.setAuthorisation(b);
			assertEquals(f.get(null), b);
			f.set(null, null);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testCreateP2ObjectStringP2ObjectArray() {
		// this method is called by the constructor
		testP2ObjectAdaptorP2ObjectStringP2ObjectArray();
	}

	public void testOperation() {
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"), P2Object.create("Fred2"));
		try {
			// use subclass to test the method
			EventTemplateP2Adaptor evtmpad = new EventTemplateP2Adaptor(null,
					"create:", keys);
			assertNull(((EventTemplate) evtmpad.objImpl).argList
					.getArg("Fred27"));
			evtmpad.operation(null, "arg:", P2Object.create("Fred27"));
			assertEquals(
					((EventTemplate) evtmpad.objImpl).argList.getArg("Fred27").name,
					"Fred27");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testTrace() {
		System.err
				.println("***Little error testing - P2ObjectAdaptor.trace()***");
		System.err.println("expected: create: Foo Foo2 3 7");
		System.err.print("actual:   ");
		P2ObjectAdaptor.trace("create", "Foo", "Foo2", P2Object.create(3),
				P2Object.create(7));
		System.err.println("***End of error testing***\n");
	}

	public void testGetCreateOperation() {
		try {
			assertNull(ad.getCreateOperation("Foo"));
			fail("Exception expected");
		} catch (Ponder2OperationException e) {
			assertEquals(
					e.toString(),
					"eu.novi.ponder2.exception.Ponder2OperationException: Object P2ObjectAdaptor unknown constructor 'Foo'");
		}

	}

	public void testGetInstanceOperation() {
		try {
			assertNull(ad.getInstanceOperation("Fred"));
			fail("Exception expected");
		} catch (Ponder2OperationException e) {
			assertEquals(
					e.toString(),
					"eu.novi.ponder2.exception.Ponder2OperationException: Object class eu.novi.ponder2.P2ObjectAdaptor unknown operation 'Fred'");
		}
	}

	public void testReadExternal() {
		Mockery m = new Mockery();
		final ObjectInput in = m.mock(ObjectInput.class);
		final OID oid = P2Object.create().getOID();
		try {
			m.checking(new Expectations() {
				{
					one(in).readObject();
					will(returnValue(oid));
				}
			});
			Field f = P2ObjectAdaptor.class.getDeclaredField("myExternalOID");
			f.setAccessible(true);
			assertNull(f.get(ad));
			ad.readExternal(in);
			assertNotNull(f.get(ad));
			assertEquals(f.get(ad), oid);
		} catch (Exception e) {
			fail(e.toString());
		}
		m.assertIsSatisfied();
	}

	public void testReadResolve() {
		Mockery m = new Mockery();
		final ObjectInput in = m.mock(ObjectInput.class);
		final OID oid = P2Object.create(144000).getOID();
		try {
			m.checking(new Expectations() {
				{
					one(in).readObject();
					will(returnValue(oid));
				}
			});
			ad.readExternal(in);
			assertEquals(((P2Object) ad.readResolve()).asInteger(), 144000);
		} catch (Exception e) {
			fail(e.toString());
		}
		m.assertIsSatisfied();
	}

	public void testWriteExternal() {
		Mockery m = new Mockery();
		final ObjectOutput out = m.mock(ObjectOutput.class);
		try {
			m.checking(new Expectations() {
				{
					one(out).writeObject(ad.getOID());
				}
			});
			ad.writeExternal(out);
		} catch (Exception e) {
			fail(e.toString());
		}
		m.assertIsSatisfied();
	}

}
