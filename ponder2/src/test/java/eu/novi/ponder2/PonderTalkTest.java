package eu.novi.ponder2;

import java.lang.reflect.Field;

import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class PonderTalkTest extends TestCase {

	public PonderTalkTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonderTalkP2Object() {
		P2Object o = P2Object.create(7);
		PonderTalk pt = new PonderTalk(o);
		try {
			Field trace = PonderTalk.class.getDeclaredField("trace");
			Field myP2Object = PonderTalk.class.getDeclaredField("myP2Object");
			Field rmiName = PonderTalk.class.getDeclaredField("rmiName");
			trace.setAccessible(true);
			myP2Object.setAccessible(true);
			rmiName.setAccessible(true);
			assertFalse(trace.getBoolean(pt));
			assertEquals(myP2Object.get(pt), o);
			assertNull(rmiName.get(pt));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testPonderTalkP2ObjectString() {
		/*
		 * Removed by Yiannos P2Object o = P2Object.create(7); try { PonderTalk
		 * pt = new PonderTalk(o, "//localhost:123/Foo"); Field trace =
		 * PonderTalk.class.getDeclaredField("trace"); Field myP2Object =
		 * PonderTalk.class.getDeclaredField("myP2Object"); Field rmiName =
		 * PonderTalk.class.getDeclaredField("rmiName");
		 * trace.setAccessible(true); myP2Object.setAccessible(true);
		 * rmiName.setAccessible(true); assertFalse(trace.getBoolean(pt));
		 * assertEquals(myP2Object.get(pt), o); assertEquals(rmiName.get(pt),
		 * "Foo"); } catch (Ponder2ArgumentException e) {fail(e.toString());}
		 * catch (Exception e) {fail(e.toString());}
		 */
	}

	public void testBind() {
		// PonderTalk pt = new PonderTalk(P2Object.create());
		// try {
		// SelfManagedCell.main(new String[]{});
		// pt.bind("//127.0.0.1:13570/");
		// } catch (Exception e) {fail(e.toString());}
		// TODO fail("Not yet implemented");
	}

	public void testTrace() {
		PonderTalk pt = new PonderTalk(P2Object.create());
		try {
			Field trace = PonderTalk.class.getDeclaredField("trace");
			trace.setAccessible(true);
			assertFalse(trace.getBoolean(pt));
			pt.trace(true);
			assertTrue(trace.getBoolean(pt));
			pt.trace(false);
			assertFalse(trace.getBoolean(pt));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testTest() {
		// TODO fail("Not yet implemented");
	}

	public void testExecutePonderTalk() {
		P2Hash map = new P2Hash();
		map.put("myNum", P2Object.create(24));
		PonderTalk pt = new PonderTalk(map);
		try {
			P2Block b = pt.executePonderTalk("[ :arg1 | 3 * 7 + 12 + arg1]")
					.asBlock();
			assertEquals(b.operation_value1(null, P2Object.create(144000))
					.asInteger(), 3 * 7 + 12 + 144000);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

}
