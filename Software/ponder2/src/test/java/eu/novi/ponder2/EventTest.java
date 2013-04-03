package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.EventTemplateP2Adaptor;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.Event;

import junit.framework.TestCase;

public class EventTest extends TestCase {

	public EventTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEventP2ObjectStringP2ObjectArray() {
		try {
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
					P2Object.create());
			new SelfManagedCell(dom);
			SelfManagedCell.RootDomain = dom;
			dom.operation(null, "at:put:", P2Object.create("Foo"), evtmp);
			Event ev = new Event(null, "Foo", P2Object.create(124),
					P2Object.create(144));
			assertEquals(ev.size(), 2);
			assertEquals(ev.get("Foo").asInteger(), 124);
			assertEquals(ev.get("Fred").asInteger(), 144);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testEventP2ObjectP2ObjectP2ObjectArray() {
		try {
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"), P2Object.create("Fred2"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			Event ev = new Event(null, evtmp, P2Object.create(3),
					P2Object.create(7), P2Object.create(12));
			assertEquals(ev.size(), 3);
			assertEquals(ev.get("Foo").asInteger(), 3);
			assertEquals(ev.get("Fred").asInteger(), 7);
			assertEquals(ev.get("Fred2").asInteger(), 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testEventP2ObjectP2ObjectP2Hash() {
		P2Object source = P2Object.create(7);
		P2Object eventTemplate = P2Object.create(12);
		HashMap<String, P2Object> hash = new HashMap<String, P2Object>();
		hash.put("Foo", P2Object.create(-1));
		hash.put("Fred", P2Object.create(3));
		hash.put("Fred2", P2Object.create(8));
		P2Hash h = new P2Hash(hash);
		Event e = null;
		try {
			e = new Event(source, eventTemplate, h);
		} catch (Ponder2Exception x) {
			fail(x.toString());
		}
		assertNotNull(e);
		// test of getEventTemplate():
		assertEquals(e.getEventTemplate(), eventTemplate);
		// test of getSource():
		assertEquals(e.getSource(), source);
		try {
			Field f = Event.class.getDeclaredField("visited");
			f.setAccessible(true);
			Set vis = (Set) f.get(e);
			assertNotNull(vis);
			assertTrue(vis.isEmpty());
		} catch (Exception x) {
			fail(e.toString());
		}
	}

	public void testGetEventTemplate() {
		testEventP2ObjectP2ObjectP2Hash();
	}

	public void testGetSource() {
		testEventP2ObjectP2ObjectP2Hash();
	}

	public void testSetVisited() {
		P2Hash h = new P2Hash(new HashMap<String, P2Object>());
		Event e = null;
		try {
			e = new Event(P2Object.create(), P2Object.create(), h);
		} catch (Ponder2Exception x) {
			fail(x.toString());
		}
		assertNotNull(e);
		try {
			Field f = Event.class.getDeclaredField("visited");
			f.setAccessible(true);
			Set vis = (Set) f.get(e);
			assertNotNull(vis);
			assertTrue(vis.isEmpty());
			P2ManagedObject mo = new P2ManagedObject(P2Object.create());
			e.setVisited(mo);
			assertFalse(vis.isEmpty());
			assertTrue(vis.contains(mo));
		} catch (Exception x) {
			fail(e.toString());
		}
	}

}
