package eu.novi.ponder2.objects;

import java.math.BigDecimal;

import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class P2NumberTest extends TestCase {

	BigDecimal d41;
	BigDecimal d42;
	BigDecimal d43;
	BigDecimal dm0;
	BigDecimal d0;
	BigDecimal dm1;
	BigDecimal dm42;
	P2Number p2n;

	public P2NumberTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		d41 = new BigDecimal(41);
		d42 = new BigDecimal(42);
		d43 = new BigDecimal(43);
		dm0 = new BigDecimal(-0);
		d0 = new BigDecimal(-0);
		dm1 = new BigDecimal(-1);
		dm42 = new BigDecimal(-42);
		p2n = new P2Number(d42);
	}

	public void testAsNumber() {
		try {
			assertEquals(p2n.asInteger(), 42);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testOperation() {
		try {
			assertEquals(p2n.operation(null, "*", P2Object.create(3))
					.asInteger(), 42 * 3);
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2NumberInt() {
		assertEquals(p2n.number.intValue(), 42);
	}

	public void testP2NumberString() {
		try {
			assertEquals(new P2Number("42").number.intValue(), 42);
			assertEquals(new P2Number("-0").number.intValue(), 0);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
		try {
			// this is meant to throw exception
			assertEquals(new P2Number("").number.intValue(), 0);
			fail("Exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testLt() {
		assertFalse(p2n.lt(d41));
		assertFalse(p2n.lt(d42));
		assertTrue(p2n.lt(d43));
	}

	public void testGt() {
		assertTrue(p2n.gt(d41));
		assertFalse(p2n.gt(d42));
		assertFalse(p2n.gt(d43));
	}

	public void testLe() {
		assertFalse(p2n.le(d41));
		assertTrue(p2n.le(d42));
		assertTrue(p2n.le(d43));
	}

	public void testGe() {
		assertTrue(p2n.ge(d41));
		assertTrue(p2n.ge(d42));
		assertFalse(p2n.ge(d43));
	}

	public void testEq() {
		assertFalse(p2n.eq(d41));
		assertTrue(p2n.eq(d42));
		assertFalse(p2n.eq(d43));
	}

	public void testNe() {
		assertTrue(p2n.ne(d41));
		assertFalse(p2n.ne(d42));
		assertTrue(p2n.ne(d43));
	}

	public void testPlus() {
		assertEquals(p2n.plus(d42).intValue(), 84);
		assertEquals(p2n.plus(dm0).intValue(), 42);
		assertEquals(p2n.plus(dm42).intValue(), 0);
	}

	public void testMinus() {
		assertEquals(p2n.minus(d42).intValue(), 0);
		assertEquals(p2n.minus(dm0).intValue(), 42);
		assertEquals(p2n.minus(dm42).intValue(), 84);
	}

	public void testTimes() {
		assertEquals(p2n.times(dm1).intValue(), -42);
		assertEquals(p2n.times(d0).intValue(), 0);
		assertEquals(p2n.times(d42).intValue(), 42 * 42);
	}

	public void testToString() {
		assertTrue((p2n.toString() + "Foo" + p2n.toString()).equals("42Foo42"));
	}

}
