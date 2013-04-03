package eu.novi.ponder2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.OID;
import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

public class ExternalManagedObjectTest extends TestCase {

	ExternalManagedObject emo;

	public ExternalManagedObjectTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		emo = new ExternalManagedObject(P2Object.create(7));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		try {
			Field f = ExternalManagedObject.class
					.getDeclaredField("protocolTable");
			f.setAccessible(true);
			((Map) f.get(null)).clear();
			f = ExternalManagedObject.class.getDeclaredField("locationTable");
			f.setAccessible(true);
			((Map) f.get(null)).clear();
		} catch (Exception e) {
			fail(e.toString());
		}
		emo = null;
	}

	public void testRegisterProtocol() {
		Mockery m = new Mockery();
		Transmitter t1 = m.mock(Transmitter.class, "t1");
		Transmitter t2 = m.mock(Transmitter.class, "t2");
		Transmitter t3 = m.mock(Transmitter.class, "t3");
		assertFalse(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t2.equals(t3));
		try {
			Field f = ExternalManagedObject.class
					.getDeclaredField("protocolTable");
			f.setAccessible(true);
			Map map = (Map) f.get(emo);
			assertTrue(map.isEmpty());
			ExternalManagedObject.registerProtocol("Foo", t1, null);
			assertEquals(map.size(), 1);
			assertEquals(map.get("Foo"), t1);
			ExternalManagedObject.registerProtocol("Fred", t2, null);
			assertEquals(map.size(), 2);
			assertEquals(map.get("Fred"), t2);
			URI uri = new URI("Fred2URI");
			ExternalManagedObject.registerProtocol("Fred2", t3, uri);
			assertEquals(map.size(), 3);
			assertEquals(map.get("Fred2"), t3);
			assertTrue(OID.getAddresses().contains(uri));
		} catch (Exception e) {
			fail(e.toString());
		}
		OID.getAddresses().clear();
	}

	public void testHasProtocol() {
		Mockery m = new Mockery();
		Transmitter t = m.mock(Transmitter.class);
		assertFalse(ExternalManagedObject.hasProtocol("Foo"));
		assertFalse(ExternalManagedObject.hasProtocol("Fred"));
		assertFalse(ExternalManagedObject.hasProtocol("Fred2"));
		ExternalManagedObject.registerProtocol("Foo", t, null);
		assertTrue(ExternalManagedObject.hasProtocol("Foo"));
		ExternalManagedObject.registerProtocol("Fred", t, null);
		assertTrue(ExternalManagedObject.hasProtocol("Fred"));
		ExternalManagedObject.registerProtocol("Fred2", t, null);
		assertTrue(ExternalManagedObject.hasProtocol("Fred2"));
	}

	public void testGetRemote() {
		try {
			Mockery m = new Mockery();
			final Transmitter t = m.mock(Transmitter.class, "t");
			final Transmitter remote = m.mock(Transmitter.class, "remote");
			ExternalManagedObject.registerProtocol("fooscheme", t, new URI(
					"FooScheme://FredProtocol"));
			m.checking(new Expectations() {
				{
					one(t).connect(new URI("FooScheme://FredProtocol"));
					will(returnValue(remote));
				}
			});
			assertEquals(ExternalManagedObject.getRemote(new URI(
					"FooScheme://FredProtocol")), remote);
			m.assertIsSatisfied();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testLoadProtocol() {
		try {
			ExternalManagedObject.loadProtocol("classNotFound", "Fred", null);
			fail("classNotFound was somehow loaded");
		} catch (Ponder2RemoteException e) {
		}
		try {
			ExternalManagedObject.loadProtocol("my", "http://Fred/Foo", null);
		} catch (Ponder2RemoteException e) {
			fail(e.toString());
		}
	}

	public void testExternalManagedObject() {
		assertNotNull(emo);
		try {
			Field f = ExternalManagedObject.class
					.getDeclaredField("myP2Object");
			f.setAccessible(true);
			assertEquals(((P2Object) f.get(emo)).asInteger(), 7);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testSetExternalOID() {
		Mockery m = new Mockery();
		final Set s = m.mock(Set.class);
		try {
			Constructor c = OID.class.getDeclaredConstructor(String.class,
					boolean.class, Set.class);
			c.setAccessible(true);
			OID oid = (OID) c.newInstance("Foo2", true, s);
			Mockery iterMock = new Mockery();
			final Iterator iter = iterMock.mock(Iterator.class);
			iterMock.checking(new Expectations() {
				{
					one(iter).hasNext();
					will(returnValue(false));
				}
			});
			m.checking(new Expectations() {
				{
					one(s).iterator();
					will(returnValue(iter));
				}
			});
			emo.setExternalOID(oid);
			iterMock.assertIsSatisfied();
			m.assertIsSatisfied();
			Field f = ExternalManagedObject.class
					.getDeclaredField("myP2Object");
			f.setAccessible(true);
			P2Object o = (P2Object) f.get(emo);
			assertTrue(o.getOID().isDomain());
			assertEquals(o.getOID().getUid(), "Foo2");
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testObj_operation() {
		System.out.println("ExternalManagedObjectTest.testObj_operation():");
		try {
			Mockery m = new Mockery();
			final Transmitter t = m.mock(Transmitter.class, "t");
			final Transmitter remote = m.mock(Transmitter.class, "remote");
			final URI uri = new URI("mocked://uri");
			final OID oid = P2Object.create(3).getOID();
			emo.externalOID = oid;
			emo.address = uri;
			ExternalManagedObject.registerProtocol("mocked", t, uri);
			m.checking(new Expectations() {
				{
					one(t).connect(uri);
					will(returnValue(remote));
					one(remote).execute(uri, oid, null, "mocked",
							new P2Object[] {});
					will(returnValue(P2Object.create(12)));
				}
			});
			assertEquals(emo.obj_operation(null, "mocked").asInteger(), 12);
			m.assertIsSatisfied();
		} catch (Exception e) {
			fail(e.toString());
		}
		System.out.println();
	}

}
