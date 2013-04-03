package eu.novi.ponder2.exception;

import eu.novi.ponder2.exception.Ponder2RemoteException;
import junit.framework.TestCase;

public class Ponder2RemoteExceptionTest extends TestCase {

	public Ponder2RemoteExceptionTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPonder2RemoteException() {
		Ponder2RemoteException e = new Ponder2RemoteException("Foo");
		assertEquals(e.getMessage(), "Foo");
		try {
			throw e;
		} catch (Ponder2RemoteException x) {
			assertEquals(e, x);
		}
	}

}
