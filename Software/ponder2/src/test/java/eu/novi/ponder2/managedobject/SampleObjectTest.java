package eu.novi.ponder2.managedobject;

import java.lang.reflect.Field;
import java.util.Map;

import eu.novi.ponder2.managedobject.SampleObject;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class SampleObjectTest extends TestCase {

	public SampleObjectTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSampleObject() {
		SampleObject o = new SampleObject();
		assertNotNull(o);
		try {
			Field f = SampleObject.class.getDeclaredField("data");
			f.setAccessible(true);
			Map o_data = (Map) f.get(o);
			assertTrue(o_data.isEmpty());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testSampleObjectInt() {
		SampleObject o = new SampleObject(3);
		assertNotNull(o);
		try {
			Field f = SampleObject.class.getDeclaredField("data");
			f.setAccessible(true);
			Map o_data = (Map) f.get(o);
			assertTrue(o_data.isEmpty());
			assertEquals(o_data.size(), 0);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2_operation_at_put() {
		testP2_operation_at();
	}

	public void testP2_operation_at() {
		SampleObject o = new SampleObject();
		P2Object oid = P2Object.create();
		P2Object oid2 = P2Object.create();
		assertEquals(o.p2_operation_at_put("Foo", oid), oid);
		assertEquals(o.p2_operation_at_put("Fred", oid2), oid2);
		assertEquals(o.p2_operation_at("Foo"), oid);
		assertEquals(o.p2_operation_at("Fred"), oid2);
		try {
			Field f = SampleObject.class.getDeclaredField("data");
			f.setAccessible(true);
			Map o_data = (Map) f.get(o);
			assertEquals(o_data.size(), 2);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2_operation_remove() {
		SampleObject o = new SampleObject();
		P2Object oid = P2Object.create();
		P2Object oid2 = P2Object.create();
		assertEquals(o.p2_operation_at_put("Foo", oid), oid);
		assertEquals(o.p2_operation_at_put("Fred", oid2), oid2);
		assertEquals(o.p2_operation_remove("Foo"), oid);
		assertEquals(o.p2_operation_remove("Fred"), oid2);
		try {
			Field f = SampleObject.class.getDeclaredField("data");
			f.setAccessible(true);
			Map o_data = (Map) f.get(o);
			assertTrue(o_data.isEmpty());
			assertEquals(o_data.size(), 0);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2_operation_size() {
		SampleObject o = new SampleObject();
		P2Object oid = P2Object.create();
		P2Object oid2 = P2Object.create();
		assertEquals(o.p2_operation_size(), 0);
		assertEquals(o.p2_operation_at_put("Foo", oid), oid);
		assertEquals(o.p2_operation_size(), 1);
		assertEquals(o.p2_operation_at_put("Fred", oid2), oid2);
		assertEquals(o.p2_operation_size(), 2);
		assertEquals(o.p2_operation_remove("Foo"), oid);
		assertEquals(o.p2_operation_size(), 1);
		assertEquals(o.p2_operation_remove("Fred"), oid2);
		assertEquals(o.p2_operation_size(), 0);
	}

}
