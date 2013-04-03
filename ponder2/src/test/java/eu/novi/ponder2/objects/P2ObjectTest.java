package eu.novi.ponder2.objects;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

public class P2ObjectTest extends TestCase {

	public P2ObjectTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSetSMC() {
		P2Object.setSMC(null);
		assertNull(P2Object.getSMC());
		SelfManagedCell smc = new SelfManagedCell(P2Object.create());
		P2Object.setSMC(smc);
		assertEquals(P2Object.getSMC(), smc);
	}

	public void testGetSMC() {
		testSetSMC();
	}

	@SuppressWarnings("cast")
	public void testCreate() {
		assertTrue(P2Object.create() instanceof P2Null);
	}

	public void testCreateInt() {
		P2Object v = P2Object.create(42);
		assertTrue(v instanceof P2Number);
		assertEquals(((P2Number) v).number.intValue(), 42);
	}

	public void testCreateString() {
		P2Object v = P2Object.create("Foo");
		assertTrue(v instanceof P2String);
		assertTrue(((P2String) v).equals("Foo"));
	}

	public void testCreateMapOfStringP2Object() {
		Map<String, P2Object> hash = new HashMap<String, P2Object>();
		HashMap new_hash = null;
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			new_hash = (HashMap) f.get((P2Hash) P2Object.create(hash));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertTrue(new_hash.equals(hash));
	}

	public void testCreateP2ManagedObject() {
		assertNotNull((P2Object.create(new P2ManagedObject(P2Object.create()))));
	}

	public void testCreateP2ObjectArray() {
		assertTrue(P2Object.create(P2Object.create(), P2Object.create()) instanceof P2Array);
	}

	public void testCreateClassOfQ() {
		try {
			assertEquals(P2Object.create(P2ObjectTest.class).asClass(),
					P2ObjectTest.class);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testCreateBoolean() {
		try {
			assertTrue(P2Object.create(true).asBoolean());
			assertFalse(P2Object.create(false).asBoolean());
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}

	}

	public void testCreateMapOfStringP2ObjectTaggedElement() {
		HashMap<String, P2Object> map = new HashMap<String, P2Object>();
		map.put("value", P2Object.create(3));
		P2Object block = P2Object.create(map, new TaggedElement(
				"[ :value | 5 < value ]"));
		assertNotNull(block);
		assertTrue(block instanceof P2Block);
	}

	// public void testCreateTaggedElement() {
	// TaggedElement t = new TaggedElement("<Foo>");
	// P2XML xml = P2Object.create(t);
	// assertEquals(xml.asXML(), t);
	// }

	public void testP2Object() {
		// TODO constructor of abstract class P2Object not implemented
	}

	public void testAsNumber() {
		try {
			assertNull(P2Object.create().asNumber());
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsString() {
		try {
			assertNotNull(P2Object.create().asString());
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testGetOID() {
		assertNotNull(P2Object.create().getOID());
	}

	public void testGetManagedObject() {
		assertNotNull(P2Object.create().getManagedObject());
	}

	public void testAsArray() {
		try {
			assertNotNull(P2Object.create().asArray());
			fail("exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsP2Array() {
		try {
			assertNotNull(P2Object.create().asP2Array());
			fail("exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsBlock() {
		try {
			assertNotNull(P2Object.create().asBlock());
			fail("exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsHash() {
		try {
			assertNotNull(P2Object.create().asHash());
			fail("exception expected");
		} catch (Ponder2Exception e) {
		}
	}

	public void testAsBoolean() {
		try {
			assertNotNull(P2Object.create().asBoolean());
			fail("exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsClass() {
		try {
			assertNotNull(P2Object.create().asClass());
			fail("exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	// public void testAsXML() {
	// try {
	// assertNotNull(P2Object.create().asXML());
	// fail("exception expected");
	// } catch (Ponder2ArgumentException e) {}
	// }

	public void testCreateP2ObjectStringP2ObjectArray() {
		try {
			assertNotNull(P2Object.create().create(null, null, new P2Object[5]));
			fail("exception expected");
		} catch (Ponder2Exception e) {
		}
	}

	public void testOperation() {
		try {
			assertNotNull(P2Object.create().operation(null, null,
					new P2Object[5]));
			fail("exception expected");
		} catch (Ponder2Exception e) {
		}
	}

	/*
	 * private testBaseClass() { Base b = new Base }
	 * 
	 * private abstract class Base<T extends Base>{ public Base create(Class<T>
	 * aClass, Object... args) throws Exception { if (args.length == 0) return
	 * new Nil(); Class[] argClasses = new Class[args.length]; for (int i = 0;
	 * i<args.length; i++) argClasses[i] = args[i].getClass(); return
	 * aClass.getConstructor(argClasses).newInstance(args); //return b; }
	 * //public Object getAs(Class<T> aClass) throws Ponder2Exception { // if
	 * (aClass = this.getClass()) return getValue(); // throw new
	 * Ponder2OperationException(this) //} //protected
	 * 
	 * }
	 * 
	 * 
	 * private class IntNum extends Base { int num; IntNum(Integer i) {num = i;}
	 * }
	 * 
	 * private class Nil extends Base { Nil(){} } private class Str extends Base
	 * { String s; Str(String x) {s = x;} }
	 * 
	 * 
	 * /*Don't ask what this was for :)
	 * 
	 * public void testBlah() { //Blahh<P2Object> x = new Blahh<P2Object>();
	 * //assertTrue(x.getAs(Blah.class)); Blah x = new Blah();
	 * 
	 * 
	 * //IntNum i = (IntNum) x.create(IntNum.class, new Object[]{5});
	 * //assertEquals(i.num, 5);
	 * 
	 * Blah blub = x.create(Nil.class); assertTrue(blub instanceof Nil);
	 * 
	 * //assertNotNull(x.getAs(Sub.class)); } private class Blah{ public Blah
	 * create(Class<?> t, Object... args) { Class[] c = new Class[args.length];
	 * for (int i=0; i<args.length; i++) c[i] = args[i].getClass(); try { return
	 * (Blah) t.getConstructor(c).newInstance(args); } catch (Exception e)
	 * {System.err.println(e.toString());} return null; } public Blah
	 * create2(Class<?> t, Class[] args, Object[] args2) { try { return (Blah)
	 * t.getConstructor(args).newInstance(args2); } catch (Exception e)
	 * {System.err.println(e.toString());} return null; } public Blah
	 * getAs(Class<?> t) { //if (t.isInstance(this)) return this; if
	 * (t.isInstance(this)) return this; else {
	 * System.err.println("this is not an instance of " + t.getName()); return
	 * null; } } }
	 * 
	 * private class IntNum extends Blah { int num; IntNum(Integer i) {num = i;}
	 * }
	 * 
	 * private class Nil extends Blah { Nil(){} } private class Str extends Blah
	 * { String s; Str(String x) {s = x;} }//
	 */

}
