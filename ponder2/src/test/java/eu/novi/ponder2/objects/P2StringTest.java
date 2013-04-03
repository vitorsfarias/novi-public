package eu.novi.ponder2.objects;

import java.io.PrintStream;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;

import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

public class P2StringTest extends TestCase {

	public P2StringTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAsNumber() {
		try {
			assertEquals(new P2String("42").asInteger(), 42);
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testAsString() {
		try {
			assertTrue(new P2String("Foo").asString().equals("Foo"));
			assertTrue(new P2String("").asString().equals(""));
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
	}

	public void testAsBoolean() {
		try {
			assertTrue(new P2String("true").asBoolean());
			assertFalse(new P2String("false").asBoolean());
		} catch (Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
		try {
			// this is meant to throw an exception
			assertTrue(new P2String("").asBoolean());
			fail("Exception expected");
		} catch (Ponder2ArgumentException e) {
		}
	}

	public void testAsXML() {
		try {
			assertEquals(new P2String("<xml/>").asXML().asString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml/>");
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testOperation() {
		P2String s = P2Object.create("Foo");
		try {
			assertEquals(s.operation(null, "*", P2Object.create(3)).asString(),
					"FooFooFoo");
			assertTrue(s.operation(null, "==", P2Object.create("Foo"))
					.asBoolean());
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2String() {
		testAsString();
	}

	public void testAdd() {
		assertEquals(new P2String("").add(""), "");
		assertEquals(new P2String("Foo").add("!"), "Foo!");
		assertEquals(new P2String(" ").add(""), new P2String("").add(" "));
	}

	public void testEqualsString() {
		assertTrue(new P2String("Foo").equals("Foo"));
		assertTrue(new P2String("").equals(""));
		assertFalse(new P2String(" ").equals("  "));
	}

	public void testNequals() {
		assertFalse(new P2String("Foo").nequals("Foo"));
		assertFalse(new P2String("").nequals(""));
		assertTrue(new P2String(" ").nequals("  "));
	}

	public void testTimes() {
		assertEquals(new P2String("Foo").times(3), "FooFooFoo");
		assertEquals(new P2String("").times(1000), "");
		assertEquals(new P2String("a").times(2), "aa");
		assertEquals(new P2String("Foo").times(1), "Foo");
		assertEquals(new P2String("Foo").times(0), "");
	}

	public void testToString() {
		assertEquals(new P2String("Foo").toString(), "Foo");
		assertEquals(new P2String("").toString(), "");
	}

}
