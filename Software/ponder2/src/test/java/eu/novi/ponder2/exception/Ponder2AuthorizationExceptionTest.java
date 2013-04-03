package eu.novi.ponder2.exception;

import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import junit.framework.TestCase;

public class Ponder2AuthorizationExceptionTest extends TestCase {

	public Ponder2AuthorizationExceptionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonder2AuthorizationException() {
		Ponder2AuthorizationException e = new Ponder2AuthorizationException(
				"Foo");
		assertEquals(e.getMessage(), "Foo");
		try {
			throw e;
		} catch (Ponder2AuthorizationException x) {
			assertEquals(e, x);
		}
	}

}
