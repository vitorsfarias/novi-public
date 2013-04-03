package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.FactoryObject;
import eu.novi.ponder2.Path;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.BasicAuthModule;

import junit.framework.TestCase;
import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.EventTemplateP2Adaptor;
import eu.novi.ponder2.FactoryObjectP2Adaptor;
import eu.novi.ponder2.managedobject.SampleObjectP2Adaptor;
import eu.novi.ponder2.policy.ObligationPolicyP2Adaptor;

public class UtilTest extends TestCase {

	EventTemplateP2Adaptor evtmp;
	DomainP2Adaptor dom;

	public UtilTest(String name) {
		super(name);
		AuthorisationModule a = new BasicAuthModule(null);
		// P2ObjectAdaptor.setAuthorisation(a);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"));
		evtmp = new EventTemplateP2Adaptor(null, "create:", keys);
		dom = new DomainP2Adaptor(null, "create", P2Object.create());
		SelfManagedCell.RootDomain = null;
		new SelfManagedCell(dom);
		dom.operation(null, "at:put:", P2Object.create("Foo"), evtmp);
	}

	public void testResolveString() {
		try {
			assertEquals(Util.resolve("/", "Foo"), evtmp);
			assertEquals(Util.resolve("/", "Foo"), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testResolveStringString() {
		try {
			assertEquals(Util.resolve("/", "Foo"), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testResolveStringPath() {
		try {
			assertEquals(Util.resolve("/", new Path("/Foo")), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testResolvePath() {
		try {
			assertEquals(Util.resolve(new Path("/Foo")), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testResolveP2ObjectString() {
		try {
			assertEquals(Util.resolve(dom, "/Foo"), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testResolveP2ObjectPath() {
		try {
			assertEquals(Util.resolve(dom, new Path("/Foo")), evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testResolveP2ManagedObjectPath() {
		try {
			assertEquals(
					Util.resolve(dom.getManagedObject(), new Path("/Foo")),
					evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testParseFile() {
		// TODO fail("Not yet implemented");
	}

	public void testLoadFactory() {
		try {
			P2Object o = Util.loadFactory("Domain");
			assertNotNull(o);
			assertTrue(o instanceof FactoryObjectP2Adaptor);
			FactoryObjectP2Adaptor ad = (FactoryObjectP2Adaptor) o;
			assertNotNull(ad.objImpl);
			Field f = FactoryObject.class.getDeclaredField("adaptorClass");
			f.setAccessible(true);
			assertEquals(f.get(ad.objImpl), DomainP2Adaptor.class);
			P2Object dom = ad.operation(null, "create", P2Object.create());
			assertNotNull(dom);
			assertTrue(dom instanceof DomainP2Adaptor);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testGetRemoteObject() {
		try {
			Mockery m = new Mockery();
			final Transmitter t = m.mock(Transmitter.class);
			final Transmitter remote = m.mock(Transmitter.class, "remote");
			final URI uri = new URI("FooScheme://FredProtocol");
			ExternalManagedObject.registerProtocol("fooscheme", t, uri);
			m.checking(new Expectations() {
				{
					one(t).connect(uri);
					will(returnValue(remote));
					one(remote).getObject(uri, "FooScheme");
					will(returnValue(P2Object.create(7)));
				}
			});
			assertEquals(
					Util.getRemoteObject("FooScheme",
							"FooScheme://FredProtocol").asInteger(), 7);
			Field f = ExternalManagedObject.class
					.getDeclaredField("protocolTable");
			f.setAccessible(true);
			((Map) f.get(null)).clear();
			f = ExternalManagedObject.class.getDeclaredField("locationTable");
			f.setAccessible(true);
			((Map) f.get(null)).clear();

			m.assertIsSatisfied();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testGetManagedObjectAdaptor() {
		assertEquals(Util.getManagedObjectAdaptor("Domain"),
				DomainP2Adaptor.class);
		assertEquals(Util.getManagedObjectAdaptor("SampleObject"),
				SampleObjectP2Adaptor.class);
		assertEquals(Util.getManagedObjectAdaptor("ObligationPolicy"),
				ObligationPolicyP2Adaptor.class);
		assertEquals(Util.getManagedObjectAdaptor("P2Number"), null);
	}

	public void testGetInputStream() {
		// TODO: To esbisa ego Yiannos
		/*
		 * try { Util.getInputStream(new URI("Policy.p2")); } catch (Exception
		 * e) {fail(e.toString());}
		 */
	}

}
