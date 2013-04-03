package eu.novi.ponder2.objects;

import java.lang.reflect.Constructor;

import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class P2BooleanTest extends TestCase {

	P2Block block_true;
	P2Block block_false;
	P2Block block_7;
	P2Block block_12;

	public P2BooleanTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		block_true = translate("[ true | false ]");
		block_false = translate("[ true & false ]");
		block_7 = translate("[ (1 + 2) * 2 + 1]");
		block_12 = translate("[ 2 + (2 * 5) ]");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		block_true = null;
		block_false = null;
		block_7 = null;
		block_12 = null;
	}

	public void testAsBoolean() {
		testFrom();
	}

	public void testOperation() {
		try {
			assertTrue(P2Boolean.True.operation(null, "&", P2Boolean.True)
					.asBoolean());
			assertTrue(P2Boolean.False.operation(null, "|", P2Boolean.True)
					.asBoolean());
			assertTrue(P2Boolean.True.operation(null, "|", P2Boolean.False)
					.asBoolean());
			assertFalse(P2Boolean.True.operation(null, "&", P2Boolean.False)
					.asBoolean());
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testFrom() {
		try {
			assertTrue(P2Boolean.from("true").asBoolean());
			assertFalse(P2Boolean.from("false").asBoolean());
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
		try {
			// this is meant to throw an exception
			assertTrue(P2Boolean.from("").asBoolean());
			fail("exception expected");
		} catch (Ponder2Exception e) {
		}
	}

	public void testNotBoolean() {
		assertTrue(P2Boolean.False.not());
		assertFalse(P2Boolean.True.not());
	}

	public void testAndBoolean() {
		assertTrue(P2Boolean.True.and(true));
		assertFalse(P2Boolean.True.and(false));
		assertFalse(P2Boolean.False.and(true));
		assertFalse(P2Boolean.False.and(false));
	}

	public void testOrBoolean() {
		assertTrue(P2Boolean.True.or(true));
		assertTrue(P2Boolean.True.or(false));
		assertTrue(P2Boolean.False.or(true));
		assertFalse(P2Boolean.False.or(false));
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

	public void testAndP2ObjectP2Block() {
		try {
			assertTrue(P2Boolean.True.and(null, block_true));
			assertFalse(P2Boolean.True.and(null, block_false));
			assertFalse(P2Boolean.False.and(null, block_true));
			assertFalse(P2Boolean.False.and(null, block_false));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOrP2ObjectP2Block() {
		try {
			assertTrue(P2Boolean.True.or(null, block_true));
			assertTrue(P2Boolean.True.or(null, block_false));
			assertTrue(P2Boolean.False.or(null, block_true));
			assertFalse(P2Boolean.False.or(null, block_false));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testIfTrue() {
		try {
			assertEquals(P2Boolean.True.ifTrue(null, block_7).asInteger(), 7);
			assertEquals(P2Boolean.True.ifTrue(null, block_12).asInteger(), 12);
			assertTrue(P2Boolean.False.ifTrue(null, block_7) instanceof P2Null);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testIfFalse() {
		try {
			assertEquals(P2Boolean.False.ifFalse(null, block_7).asInteger(), 7);
			assertEquals(P2Boolean.False.ifFalse(null, block_12).asInteger(),
					12);
			assertTrue(P2Boolean.True.ifFalse(null, block_7) instanceof P2Null);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testIfTrueifFalse() {
		try {
			assertEquals(P2Boolean.True.ifTrueifFalse(null, block_7, block_12)
					.asInteger(), 7);
			assertEquals(P2Boolean.False.ifTrueifFalse(null, block_7, block_12)
					.asInteger(), 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testIfFalseifTrue() {
		try {
			assertEquals(P2Boolean.True.ifFalseifTrue(null, block_7, block_12)
					.asInteger(), 12);
			assertEquals(P2Boolean.False.ifFalseifTrue(null, block_7, block_12)
					.asInteger(), 7);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testToString() {
		assertEquals(P2Boolean.True.toString(), "" + true);
		assertEquals(P2Boolean.False.toString(), "" + false);
	}

}
