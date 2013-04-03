package eu.novi.ponder2.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;

public class P2ArrayTest extends TestCase {

	P2Array a;
	P2String foo = new P2String("Foo");
	P2String fred = new P2String("Fred");
	P2Null n = P2Null.Null;

	public P2ArrayTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		a = new P2Array(foo, fred, n);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		a = null;
	}

	public void testAsArray() {
		try {
			assertEquals(a.asArray().length, 3);
			assertEquals(a.asArray()[0], foo);
			assertEquals(a.asArray()[1], fred);
			assertEquals(a.asArray()[2], n);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testAsP2Array() {
		try {
			assertEquals(
					a.asP2Array().asP2Array().asP2Array().asArray().length, 3);
			assertEquals(a.asP2Array().asP2Array().asP2Array().asArray()[0],
					foo);
			assertEquals(a.asP2Array().asP2Array().asP2Array().asArray()[1],
					fred);
			assertEquals(a.asP2Array().asP2Array().asP2Array().asArray()[2], n);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testAsHash() {
		a.values.remove(n);
		try {
			P2Hash temp = new P2Hash(a.asHash());
			assertTrue(temp.containsKey("Foo"));
			assertEquals(temp.get("Foo").asString(), "Fred");
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testOperation() {
		a.values.remove(n);
		try {
			P2Object res = a.operation(null, "asHash");
			assertTrue(res instanceof P2Hash);
			assertTrue(res.asHash().containsKey("Foo"));
			assertEquals(res.asHash().get("Foo").asString(), "Fred");
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2Array() {
		assertNotNull(a);
		testAsArray(); // tests whether array is created correctly
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

	public void testOperation_do() {
		try {
			P2Hash map = new P2Hash();
			map.put("myNum", P2Object.create(7));
			String p2xml = P2Compiler.parse("[ :arg1 | myNum := arg1 + myNum]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);
			a.values.clear();
			a.add(P2Object.create(5));
			a.add(P2Object.create(7));
			a.add(P2Object.create(12));
			a.operation_do(null, b);
			Field f = P2Block.class.getDeclaredField("variables");
			f.setAccessible(true);
			HashMap<String, P2Object> m = (HashMap) f.get(b);
			assertEquals(7, m.get("myNum").asInteger());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testOperation_collate() {
		try {
			P2Block b = translate("[ :arg1 | arg1 * 3 * 7 + 12 ]");
			a.values.clear();
			a.add(P2Object.create(5));
			a.add(P2Object.create(7));
			a.add(P2Object.create(12));
			P2Array res = a.operation_collect(null, b);
			assertEquals(res.values.get(0).asInteger(), 5 * 3 * 7 + 12);
			assertEquals(res.values.get(1).asInteger(), 7 * 3 * 7 + 12);
			assertEquals(res.values.get(2).asInteger(), 12 * 3 * 7 + 12);
			assertEquals(res.values.size(), 3);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testAdd() {
		a.add(foo);
		a.add(fred);
		try {
			assertEquals(a.asArray().length, 5);
			assertEquals(a.asArray()[0], foo);
			assertEquals(a.asArray()[1], fred);
			assertEquals(a.asArray()[2], n);
			assertEquals(a.asArray()[3], foo);
			assertEquals(a.asArray()[4], fred);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testToString() {
		a.values.remove(n);
		assertEquals("[Foo, Fred]", a.toString());
	}

}
