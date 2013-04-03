package eu.novi.ponder2.exception;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;

public class Ponder2ArgumentExceptionTest extends TestCase {

	public Ponder2ArgumentExceptionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonder2ArgumentException() {
		testGetMessage();
	}

	public void testAddXML() {
		Ponder2ArgumentException e = new Ponder2ArgumentException("Foo");
		TaggedElement t = new TaggedElement("<xml>Fred</xml>");
		e.addXML(t);
		try {
			throw e;
		} catch (Ponder2ArgumentException x) {
			assertEquals(e, x);
			try {
				Field f = Ponder2Exception.class.getDeclaredField("xml");
				f.setAccessible(true);
				TaggedElement x_xml = (TaggedElement) f.get(x);
				assertEquals(x_xml, t);
			} catch (Exception exc) {
				fail(exc.toString());
			}
		}
	}

	public void testAddSource() {
		Ponder2ArgumentException e = new Ponder2ArgumentException("Foo");
		e.addSource("Fred");
		try {
			throw e;
		} catch (Ponder2ArgumentException x) {
			assertEquals(e, x);
			try {
				Field f = Ponder2Exception.class.getDeclaredField("source");
				f.setAccessible(true);
				String x_source = (String) f.get(x);
				assertEquals(x_source, "Fred");
			} catch (Exception exc) {
				fail(exc.toString());
			}
		}
	}

	public void testGetLineInfo() {
		Ponder2ArgumentException e = new Ponder2ArgumentException("Foo");
		try {
			throw e;
		} catch (Ponder2ArgumentException x) {
			assertEquals(e, x);
			assertEquals(x.getMessage(), "Foo");
			assertEquals(x.getLineInfo(), "");
		}
		e.addXML(new TaggedElement("<xml>Fred</xml>"));
		e.addSource("Fred");
		assertEquals(e.getLineInfo(), "Fred:unknown line - ");
	}

	public void testGetMessage() {
		Ponder2ArgumentException e = new Ponder2ArgumentException("Foo");
		assertEquals(e.getMessage(), "Foo");
		try {
			throw e;
		} catch (Ponder2ArgumentException x) {
			assertEquals(e, x);
			assertEquals(x.getMessage(), "Foo");
		}
	}

	public void testToString() {
		Ponder2ArgumentException e = new Ponder2ArgumentException("Foo");
		try {
			throw e;
		} catch (Ponder2ArgumentException x) {
			assertEquals(e, x);
			assertEquals(x.toString(),
					"eu.novi.ponder2.exception.Ponder2ArgumentException: Foo");
		}
	}

}
