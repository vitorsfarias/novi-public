package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.util.HashMap;

import eu.novi.ponder2.EventTemplateP2Adaptor;

import eu.novi.ponder2.EventTemplate;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class EventTemplateTest extends TestCase {

	EventTemplateP2Adaptor evtmpad;
	EventTemplate evtmp;

	public EventTemplateTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"), P2Object.create("Fred2"));
		evtmpad = new EventTemplateP2Adaptor(null, "create:", keys);
		evtmp = (EventTemplate) evtmpad.objImpl;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		evtmpad = null;
		evtmp = null;
	}

	public void testEventTemplateP2Object() {
		P2Object o = P2Object.create(7);
		EventTemplate et = new EventTemplate(o);
		assertNotNull(et.argList);
		assertTrue(et.argList.isEmpty());
		try {
			Field f = EventTemplate.class.getDeclaredField("myP2Object");
			f.setAccessible(true);
			assertEquals(f.get(et), o);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(EventTemplate.defaultDomain, SelfManagedCell.EventDomain);
	}

	public void testEventTemplateP2ObjectP2Array() {
		P2Object o = P2Object.create(7);
		P2Array a = new P2Array(o);
		try {
			EventTemplate et = new EventTemplate(o, a);
			Field f = EventTemplate.class.getDeclaredField("myP2Object");
			f.setAccessible(true);
			assertEquals(f.get(et), o);
			assertNotNull(et.argList);
			assertEquals(et.argList.size(), 1);
			assertEquals(et.argList.get(0).name, "7");
			// and once a field is created, why not testing this...
			assertEquals(f.get(evtmp), evtmpad);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(EventTemplate.defaultDomain, SelfManagedCell.EventDomain);
	}

	public void testPackageArgs() {
		try {
			P2Hash h = evtmp.packageArgs(P2Object.create("Fooarg"),
					P2Object.create("Fredarg"), P2Object.create("Fred2arg"));
			assertEquals(h.get("Foo").asString(), "Fooarg");
			assertEquals(h.get("Fred").asString(), "Fredarg");
			assertEquals(h.get("Fred2").asString(), "Fred2arg");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testPackageHash() {
		HashMap<String, P2Object> map = new HashMap<String, P2Object>();
		map.put("Foo", P2Object.create(24));
		map.put("Fred2", P2Object.create("-.-"));
		map.put("Fred", P2Object.create());
		P2Hash args = P2Object.create(map);
		try {
			P2Hash h = evtmp.packageHash(args);
			assertEquals(h.get("Foo").asInteger(), 24);
			assertEquals(h.get("Fred"), P2Object.create());
			assertEquals(h.get("Fred2").asString(), "-.-");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testDefaultDomain() {
		assertEquals(EventTemplate.defaultDomain, SelfManagedCell.EventDomain);
	}

	public void testOperation_arg() {
		assertNull(evtmp.argList.getArg("Fred3"));
		assertEquals(evtmp.argList.size(), 3);
		try {
			evtmp.operation_arg("Fred3");
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		}
		assertNotNull(evtmp.argList.getArg("Fred3"));
		assertEquals(evtmp.argList.getArg("Fred3").name, "Fred3");
		assertEquals(evtmp.argList.size(), 4);
		// try {
		// evtmp.operation_arg("Fred3");
		// } catch (Ponder2ArgumentException e) {fail(e.toString());}
		// assertEquals(evtmp.argList.size(), 4);
		// TODO: ask whether args are meant to occur twice
		// TODO: find out what I meant where args would occurs twice :S
	}

	public void testOperation_args() {
		assertEquals(evtmp.argList.size(), 3);
		assertNull(evtmp.argList.getArg("Fred3"));
		assertNull(evtmp.argList.getArg("Fred4"));
		assertNull(evtmp.argList.getArg("Fred5"));
		assertNull(evtmp.argList.getArg("Fred6"));
		try {
			evtmp.operation_args(P2Object.create(P2Object.create("Fred3"),
					P2Object.create("Fred4"), P2Object.create("Fred5")));
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		} catch (Ponder2OperationException e) {
			fail(e.toString());
		}
		assertEquals(evtmp.argList.size(), 6);
		assertNotNull(evtmp.argList.getArg("Fred3"));
		assertNotNull(evtmp.argList.getArg("Fred4"));
		assertNotNull(evtmp.argList.getArg("Fred5"));
		assertNull(evtmp.argList.getArg("Fred6"));
		assertEquals(evtmp.argList.getArg("Fred3").name, "Fred3");
	}

	public void testOperation_createP2Object() {
		try {
			P2Array keys2 = new P2Array(P2Object.create("Foo"));
			EventTemplateP2Adaptor eta = new EventTemplateP2Adaptor(null,
					"create:", keys2);
			EventTemplate et = (EventTemplate) eta.objImpl;
			et.operation_create(eta, new P2Array("value"));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_createP2ObjectP2Array() {
		try {
			evtmp.operation_create(evtmpad, new P2Array(P2Object.create(3),
					P2Object.create(7), P2Object.create(12)));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_createP2ObjectP2Hash() {
		HashMap<String, P2Object> map = new HashMap<String, P2Object>();
		map.put("Foo", P2Object.create(24));
		map.put("Fred2", P2Object.create("-.-"));
		map.put("Fred", P2Object.create());
		P2Hash args = P2Object.create(map);
		try {
			evtmp.operation_create(evtmpad, args);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

}
