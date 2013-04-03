package eu.novi.ponder2.objects;

import eu.novi.ponder2.objects.P2Class;
import eu.novi.ponder2.objects.P2Null;
import junit.framework.TestCase;

public class P2ClassTest extends TestCase {

	public P2ClassTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAsClass() {
		try {
			assertEquals(new P2Class(P2Null.class).asClass(), P2Null.class);
		} catch (eu.novi.ponder2.exception.Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

	public void testP2Class() {
		testAsClass();
	}

	public void testToString() {
		assertEquals(new P2Class(P2Null.class).toString(), "P2Null");
		assertEquals(
				new P2Class(eu.novi.ponder2.DomainP2Adaptor.class).toString(),
				"Domain");
	}

}
