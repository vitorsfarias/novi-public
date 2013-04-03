package eu.novi.ponder2;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.EventTemplateP2Adaptor;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Object;

public class OIDTest extends TestCase {

	OID oid;
	P2ManagedObject mo;

	public OIDTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mo = P2Object.create(1).getManagedObject();
		oid = new OID(mo);
		OID.getAddresses().clear();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		oid = null;
	}

	public void testAddAddress() {
		Set<URI> s = OID.getAddresses();
		s.clear();
		assertTrue(s.isEmpty());
		URI[] u = new URI[4];
		u[0] = URI.create("/Fred/Foo");
		u[1] = URI.create("/Fred2");
		u[2] = URI.create("/");
		u[3] = URI.create("");
		for (URI x : u)
			OID.addAddress(x);
		s = OID.getAddresses();
		assertEquals(s.size(), 4);
		for (URI x : u)
			assertTrue(s.contains(x));
		s.clear();
		assertTrue(s.isEmpty());
	}

	public void testGetAddresses() {
		testAddAddress();
	}

	public void testFromXML() {
		URI u1 = URI.create("/Fred/Foo");
		URI u2 = URI.create("/Fred2");
		OID.addAddress(u1);
		OID.addAddress(u2);
		oid.setDomain(true);
		try {
			OID oid2 = OID.fromXML(oid.toXML());
			assertEquals(oid, oid2);
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testOID() {
		assertNotNull(new OID(mo));
	}

	public void testIsDomain() {
		assertFalse(oid.isDomain());
		oid.setDomain(true);
		assertTrue(oid.isDomain());
		oid.setDomain(false);
		assertFalse(oid.isDomain());
	}

	public void testSetDomain() {
		assertFalse(oid.isDomain());
		oid.setDomain(!oid.isDomain());
		assertTrue(oid.isDomain());
		oid.setDomain(!oid.isDomain());
		assertFalse(oid.isDomain());
	}

	public void testGetManagedObject() {
		assertNotNull(oid.getManagedObject());
		assertEquals(oid.getManagedObject(), mo);
	}

	public void testGetP2Object() {
		P2Object o1 = P2Object.create(1);
		P2Object o2 = P2Object.create(2);
		P2ManagedObject mo1 = o1.getManagedObject();
		P2ManagedObject mo2 = o2.getManagedObject();
		OID oidx = new OID(mo1);
		assertNotNull(oidx.getManagedObject());
		assertFalse(mo1.equals(mo2));
		assertFalse(o1.equals(o2));
		assertEquals(oidx.getP2Object(), o1);
		oidx = new OID(mo2);
		assertEquals(oidx.getP2Object(), o2);
	}

	public void testGetRemoteAddresses() {
		Mockery m = new Mockery();
		final ObjectInput in = m.mock(ObjectInput.class);
		URI uri = URI.create("/../Foo");
		Set<URI> s1 = new HashSet<URI>();
		s1.add(uri);
		final Set<URI> s = s1;
		final Sequence read = m.sequence("read");
		try {
			m.checking(new Expectations() {
				{
					one(in).readObject();
					inSequence(read);
					will(returnValue("///Foo/Fred2"));
					one(in).readBoolean();
					inSequence(read);
					will(returnValue(true));
					one(in).readObject();
					inSequence(read);
					will(returnValue(s));
				}
			});
			oid.readExternal(in);
			m.assertIsSatisfied();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		try {
			Field f = OID.class.getDeclaredField("remoteAddresses");
			f.setAccessible(true);
			Set sx = (Set) f.get(oid);
			assertEquals(sx, oid.getRemoteAddresses());
			assertEquals(s, sx);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testToXML() {
		URI u1 = URI.create("/Fred/Foo");
		URI u2 = URI.create("/Fred2");
		OID.addAddress(u1);
		OID.addAddress(u2);
		oid.setUid("./Fred/or/../Foo");
		TaggedElement oid2 = oid.toXML();
		assertEquals(oid2.getAttribute("uid"), "./Fred/or/../Foo");
		assertEquals(oid2.getAttribute("isdomain"), "" + oid.isDomain());
		TaggedElement el1 = (TaggedElement) oid2.getChild(1);
		assertEquals(el1.getName(), "address");
		assertEquals(el1.getAttribute("uri"), "/Fred/Foo");
		TaggedElement el2 = (TaggedElement) oid2.getChild(0);
		assertEquals(el2.getName(), "address");
		assertEquals(el2.getAttribute("uri"), "/Fred2");
	}

	public void testToString() {
		// TODO Yiannos
		/*
		 * String s = oid.getUid(); oid.setUid("./Fred/or/../Foo");
		 * oid.setDomain(true); assertEquals(oid.toString(),
		 * "<oid isdomain='true' uid='./Fred/or/../Foo'/>"); URI u1 =
		 * URI.create("/Fred/Foo"); URI u2 = URI.create("/Fred2");
		 * OID.addAddress(u1); OID.addAddress(u2); oid.setUid(s);
		 * assertEquals(oid.toString(), oid.toXML().toString()); //try { //OID
		 * oid2 = OID.fromXML(new TaggedElement(oid.toString())); //OID oid2 =
		 * OID.fromXML(new TaggedElement(oid.toString())); //assertEquals(oid,
		 * oid2); //OID.fromXML(oid.toXML());
		 * 
		 * //System.out.println(new TaggedElement(oid.toXML().toString()));
		 * 
		 * //assertEquals(OID.fromXML(new
		 * TaggedElement(oid.toXML().toString())), oid);
		 * 
		 * 
		 * //assertEquals(OID.fromXML(oid.toXML()), OID.fromXML(new
		 * TaggedElement(oid.toXML().toString()))); //} catch (Ponder2Exception
		 * e) {fail(e.getMessage());} //TODO doesn't work somehow, waiting for
		 * feedback //update: this does not work because the constructor of
		 * TaggedElement seems to be unable to parse xml documents
		 */}

	public void testReadExternal() {
		Mockery m = new Mockery();
		final ObjectInput in = m.mock(ObjectInput.class);
		URI uri = URI.create("/../Foo");
		// OID.addAddress(uri);
		Set<URI> s1 = new HashSet<URI>();
		s1.add(uri);
		final Set<URI> s = s1;
		final Sequence read = m.sequence("read");
		try {
			m.checking(new Expectations() {
				{
					one(in).readObject();
					inSequence(read);
					will(returnValue("///Foo/Fred2"));
					one(in).readBoolean();
					inSequence(read);
					will(returnValue(true));
					one(in).readObject();
					inSequence(read);
					will(returnValue(s));
				}
			});
			oid.readExternal(in);
			m.assertIsSatisfied();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals(oid.getUid(), "///Foo/Fred2");
		assertTrue(oid.isDomain());
		assertEquals(oid.getRemoteAddresses().size(), 1);
		assertTrue(oid.getRemoteAddresses().contains(uri));
	}

	public void testReadResolve() {
		/*
		 * try { ExternalManagedObjectP2Adaptor adaptor = new
		 * ExternalManagedObjectP2Adaptor(null, "create"); ExternalManagedObject
		 * x = ((ExternalManagedObject)adaptor.getObj()); assertNotNull(x);
		 * assertTrue(x instanceof ExternalManagedObject);
		 * 
		 * Field r = OID.class.getDeclaredField("remoteAddresses");
		 * r.setAccessible(true); r.set(oid, new HashSet<URI>());
		 * System.out.println("works"); x.setExternalOID(oid); } catch
		 * (Exception e) {fail(e.toString());}
		 */

		oid.setUid("Foo");
		OID oid2 = new OID(P2Object.create(12).getManagedObject());
		try {
			Field f = OID.class.getDeclaredField("oidList");
			f.setAccessible(true);
			Map<String, OID> oidList = (Map<String, OID>) f.get(oid);
			oidList.clear();
			DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
					P2Object.create());
			assertEquals(oidList.size(), 1);
			oid2.setUid(dom.getOID().getUid());
			Field r = OID.class.getDeclaredField("remoteAddresses");
			r.setAccessible(true);
			r.set(oid2, new HashSet<URI>());
			assertEquals(oid2.readResolve(), dom.getOID());
			P2Array keys = new P2Array(P2Object.create("Foo2"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			dom.operation(null, "at:put:", P2Object.create("remote"), evtmp);
			new SelfManagedCell(dom);
			oid2.setUid("Blub");
			OID blub = (OID) oid2.readResolve();
			assertEquals(oidList.get(blub.getUid()), blub);
			oid2.setUid("Blah");
			OID blah = (OID) oid2.readResolve();
			assertEquals(oidList.get(blah.getUid()), blah);

		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testWriteExternal() {
		Mockery m = new Mockery();
		final ObjectOutput out = m.mock(ObjectOutput.class);
		oid.setDomain(true);
		oid.setUid("Foo");
		URI uri = URI.create("/Fred/Foo");
		OID.addAddress(uri);
		final Sequence write = m.sequence("write");
		try {
			m.checking(new Expectations() {
				{
					one(out).writeObject("Foo");
					inSequence(write);
					one(out).writeBoolean(true);
					inSequence(write);
					one(out).writeObject(OID.getAddresses());
					inSequence(write);
				}
			});
			oid.writeExternal(out);
			m.assertIsSatisfied();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testGetUid() {
		Double x = Double.parseDouble(oid.getUid());
		oid.setUid("Fred");
		assertEquals(oid.getUid(), "Fred");
		oid.setUid("");
		assertEquals(oid.getUid(), "");
	}

	public void testSetUid() {
		testGetUid();
	}

}
