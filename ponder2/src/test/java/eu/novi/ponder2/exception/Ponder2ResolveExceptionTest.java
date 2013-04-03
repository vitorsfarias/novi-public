package eu.novi.ponder2.exception;

import eu.novi.ponder2.exception.Ponder2ResolveException;
import junit.framework.TestCase;

public class Ponder2ResolveExceptionTest extends TestCase {

	public Ponder2ResolveExceptionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonder2ResolveException() {
		Ponder2ResolveException e = new Ponder2ResolveException("Foo");
		assertEquals(e.getMessage(), "Foo");
		try {
			throw e;
		} catch (Ponder2ResolveException x) {
			assertEquals(e, x);
		}
	}

}
