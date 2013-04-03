package eu.novi.ponder2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.EventTemplateP2Adaptor;

import eu.novi.ponder2.Domain;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;

public class DomainTest extends TestCase {

	public DomainTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDomain() {
		P2Object p2o = P2Object.create();
		Domain d = new Domain(p2o);
		assertNotNull("Domain found to be null after creation", d);
		try {
			Field f = Domain.class.getDeclaredField("myP2Object");
			Field g = Domain.class.getDeclaredField("managedObjects");
			f.setAccessible(true);
			g.setAccessible(true);
			P2Object d_myP2Object = (P2Object) f.get(d);
			Map d_managedObjects = (Map) g.get(d);
			assertEquals(
					"Domain did not initialize field variable myOID correctly",
					d_myP2Object, p2o);
			assertTrue(
					"Domain did not initialize field variable managedObjects",
					d_managedObjects != null && d_managedObjects.isEmpty());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testAdd() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		assertEquals(mo.parentCount(), 0);
		d.add("Foo", mo);
		try {
			Field g = Domain.class.getDeclaredField("managedObjects");
			g.setAccessible(true);
			Map d_managedObjects = (Map) g.get(d);
			assertTrue(d_managedObjects.get("Foo").equals(mo));
			assertEquals(d_managedObjects.size(), 1);
			assertEquals(mo.parentCount(), 1);
			d.add("Fred", mo);
			assertTrue(d_managedObjects.get("Fred").equals(mo));
			assertEquals(d_managedObjects.size(), 2);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(mo.parentCount(), 1);
		mo.removeParent(mo);
		assertEquals(mo.parentCount(), 0);
	}

	public void testRemoveObject() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		// new SelfManagedCell(P2Object.create());
		d.add("Foo", mo);
		d.add("Foo1", mo);
		boolean returnMo = false;
		try {
			returnMo = d.removeObject(mo.getP2Object());
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
		assertTrue("Domain.remove() is expected to return true", returnMo);
		try {
			Field g = Domain.class.getDeclaredField("managedObjects");
			g.setAccessible(true);
			Map d_managedObjects = (Map) g.get(d);
			assertTrue("Domain.managedObjects expected to be empty",
					d_managedObjects.isEmpty());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testRemove() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		// new SelfManagedCell(P2Object.create());
		d.add("Foo", mo);
		P2ManagedObject returnMo = null;
		try {
			returnMo = d.remove("Foo");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
		assertNotNull("Domain.remove() is expected to return an OID != null",
				returnMo);
		try {
			Field g = Domain.class.getDeclaredField("managedObjects");
			g.setAccessible(true);
			Map d_managedObjects = (Map) g.get(d);
			assertTrue("Domain.managedObjects expected to be empty",
					d_managedObjects.isEmpty());
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(returnMo, mo);
	}

	public void testGet() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		d.add("Foo", mo);
		P2ManagedObject checkMo = d.get("Foo");
		assertEquals(checkMo, mo);
		checkMo = d.get("");
		assertNull(checkMo);
	}

	public void testContainsKey() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		d.add("Foo", mo);
		assertTrue(d.contains("Foo"));
		assertFalse(d.contains("foo"));
		try {
			Field g = Domain.class.getDeclaredField("managedObjects");
			g.setAccessible(true);
			Map d_managedObjects = (Map) g.get(d);
			d_managedObjects.remove("Foo");
		} catch (Exception e) {
			fail(e.toString());
		}
		assertFalse(d.contains("Foo"));
	}

	public void testNames() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		Map<String, P2ManagedObject> manObj = new HashMap<String, P2ManagedObject>();
		d.add("One", mo);
		d.add("Two", mo);
		d.add("Three", mo);
		d.add("", mo);
		manObj.put("One", mo);
		manObj.put("Two", mo);
		manObj.put("Three", mo);
		manObj.put("", mo);
		String[] names = d.names();
		String[] check = manObj.keySet().toArray(new String[manObj.size()]);
		for (int i = 0; i < names.length; i++) {
			assertTrue(names[i].equals(check[i]));
		}
		assertEquals(names.length, check.length);
	}

	public void testOperation_listNames() {
		P2ManagedObject mo = new P2ManagedObject(P2Object.create());
		Domain d = new Domain(P2Object.create());
		d.add("One", mo);
		d.add("Two", mo);
		d.add("Three", mo);
		d.add("", mo);
		P2Object[] v = null;
		try {
			v = ((eu.novi.ponder2.objects.P2Array) d.operation_listNames())
					.asArray();
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		}
		assertNotNull(v);
		ArrayList<String> l = new ArrayList<String>();
		try {
			for (P2Object x : v)
				l.add(x.asString());

		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
		assertEquals(v.length, 4);
		assertTrue(l.contains("One"));
		assertTrue(l.contains("Two"));
		assertTrue(l.contains("Three"));
		assertTrue(l.contains(""));
	}

	public void testOperation_listObjects() {
		P2ManagedObject mo1 = P2Object.create(1).getManagedObject();
		P2ManagedObject mo2 = P2Object.create(2).getManagedObject();
		P2ManagedObject mo3 = P2Object.create(3).getManagedObject();
		P2ManagedObject mo4 = P2Object.create(4).getManagedObject();
		assertFalse(mo1.equals(mo2));
		assertFalse(mo1.equals(mo3));
		assertFalse(mo1.equals(mo4));
		assertFalse(mo2.equals(mo3));
		assertFalse(mo2.equals(mo4));
		assertFalse(mo3.equals(mo4));
		Domain d = new Domain(P2Object.create());
		d.add("One", mo1);
		d.add("Two", mo2);
		d.add("Three", mo3);
		d.add("", mo4);
		P2Object[] v = null;
		try {
			v = ((P2Array) d.operation_listObjects()).asArray();
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
		assertNotNull(v);
		ArrayList<P2ManagedObject> l = new ArrayList<P2ManagedObject>();
		for (P2Object x : v)
			l.add(x.getManagedObject());
		assertEquals(v.length, 4);
		assertTrue(l.contains(mo1));
		assertTrue(l.contains(mo2));
		assertTrue(l.contains(mo3));
		assertTrue(l.contains(mo4));
	}

	public void testOperation_asHash() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		d.add("One", mo);
		d.add("Two", mo);
		d.add("Three", mo);
		d.add("", mo);
		P2Hash h = d.operation_asHash();
		assertTrue(h.containsKey("One"));
		assertTrue(h.containsKey("Two"));
		assertTrue(h.containsKey("Three"));
		assertTrue(h.containsKey(""));
		assertEquals(h.size(), 4);

		// this test fails:
		// (is this really meant to create new objects rather than just
		// copying?)
		// assertTrue(h.containsValue(someOID));
		// update - this works now with changed ponder2 code:
		assertTrue(h.containsValue(P2Object.create()));
	}

	public void testOperation_atString() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		d.add("Foo", mo);
		assertTrue(d.operation_at("Foo").getManagedObject().equals(mo));
		assertTrue(d.operation_at("") instanceof eu.novi.ponder2.objects.P2Null);
	}

	public void testOperation_atP2ObjectStringP2Block() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		d.add("Foo", mo);
		try {
			assertEquals(d.operation_at(P2Object.create(mo), "Foo", null)
					.getManagedObject(), mo);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
		try {
			P2Hash map = new P2Hash();
			map.put("myNum", P2Object.create(24));
			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);
			assertEquals(d.operation_at(P2Object.create(mo), "Fred2", b)
					.asInteger(), 3 * 7 + 12 + 24);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_at_add() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		assertEquals(d.operation_at_add("Foo", P2Object.create(mo))
				.getManagedObject(), mo);
		assertTrue(d.contains("Foo"));
		assertEquals(d.get("Foo"), mo);
	}

	public void testOperation_do() {
		try {
			P2Hash map = new P2Hash();
			map.put("myDiet", P2Object.create(3));
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 | myDiet := myDiet + arg2 ]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);

			Domain d = new Domain(P2Object.create());
			P2ManagedObject mo1 = P2Object.create(5).getManagedObject();
			P2ManagedObject mo2 = P2Object.create(7).getManagedObject();
			P2ManagedObject mo3 = P2Object.create(12).getManagedObject();
			d.add("Foo", mo1);
			d.add("Fred", mo2);
			d.add("Fred2", mo3);
			d.operation_do(null, b);
			Field f = P2Block.class.getDeclaredField("variables");
			f.setAccessible(true);
			HashMap<String, P2Object> m = (HashMap) f.get(b);
			assertEquals(m.get("myDiet").asInteger(), 3);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private P2Block translate(String code) throws Ponder2OperationException {
		try {
			Constructor c = PonderTalk.class
					.getDeclaredConstructor(new Class[] { P2Object.class });
			c.setAccessible(true);
			PonderTalk p = (PonderTalk) c.newInstance(new Object[] { P2Object
					.create() });
			return (P2Block) p.executePonderTalk(code);
		} catch (Exception e) {
			fail(e.toString());
		}
		return null;
	}

	public void testOperation_collate() {
		// TODO: comment by Yiannos
		/*
		 * try { P2Block b = translate(
		 * "[ :arg1 :arg2 | arg1 + \" says hello \" + (arg2 + 4) + \" times\" ]"
		 * ); P2ManagedObject mo1 = P2Object.create(-1).getManagedObject();
		 * P2ManagedObject mo2 = P2Object.create(3).getManagedObject();
		 * P2ManagedObject mo3 = P2Object.create(8).getManagedObject(); Domain d
		 * = new Domain(P2Object.create()); d.add("Foo", mo1); d.add("Fred",
		 * mo2); d.add("Fred2", mo3); P2Array res = d.operation_collect(null,
		 * b); Field f = P2Array.class.getDeclaredField("values");
		 * f.setAccessible(true); Vector<P2Object> res_values = (Vector)
		 * f.get(res); assertEquals(res_values.get(2).asString(),
		 * "Foo says hello 3 times"); assertEquals(res_values.get(0).asString(),
		 * "Fred says hello 7 times");
		 * assertEquals(res_values.get(1).asString(),
		 * "Fred2 says hello 12 times"); assertEquals(res_values.size(), 3); }
		 * catch (Exception e) {fail(e.getMessage());}
		 */
	}

	public void testOperation_resolve() {
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"));
		try {
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
					P2Object.create());
			Domain d = new Domain(dom);
			d.operation_at_add("Foo", evtmp);
			dom.objImpl = d;
			new SelfManagedCell(dom);
			assertEquals(dom.operation(null, "at:", P2Object.create("Foo")),
					evtmp);
			P2Object res = d.operation_resolve("Foo");
			assertEquals(res, evtmp);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_remove() {
		P2ManagedObject mo = P2Object.create().getManagedObject();
		Domain d = new Domain(P2Object.create());
		new SelfManagedCell(P2Object.create());
		d.add("Foo", mo);
		try {
			assertEquals((d.operation_remove("Foo")).getManagedObject(), mo);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}

	}

}
