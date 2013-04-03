package eu.novi.ponder2.exception;

import eu.novi.ponder2.exception.Ponder2OperationException;
import junit.framework.TestCase;

public class Ponder2OperationExceptionTest extends TestCase {

	public Ponder2OperationExceptionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonder2OperationException() {
		Ponder2OperationException e = new Ponder2OperationException("Foo");
		assertEquals(e.getMessage(), "Foo");
		try {
			throw e;
		} catch (Ponder2OperationException x) {
			assertEquals(e, x);
		}
	}

}
