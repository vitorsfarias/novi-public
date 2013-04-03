package eu.novi.ponder2;

import eu.novi.ponder2.ArgumentList;
import junit.framework.TestCase;

public class ArgumentListTest extends TestCase {

	public ArgumentListTest(String name) {
		super(name);
	}

	public void testAddString() {
		ArgumentList a = new ArgumentList();
		assertTrue("ArgumentList must be empty after creation", a.isEmpty());
		a.add("Foo");
		assertFalse(
				"ArgumentList is expected to be non-empty but found to be empty",
				a.isEmpty());
		a.add("");
		assertEquals("ArgumentList is expected to contain 2 entries", a.size(),
				2);
		assertTrue("Item insertion failed to work properly",
				a.get(0).name.equals("Foo") && a.get(1).name.equals(""));
		a.add("");
		assertEquals("Failed to add 2 empty strings", a.size(), 3);

	}

	public void testGetArg() {
		ArgumentList a = new ArgumentList();
		assertNull("ArgumentList is empty and is expected to return null",
				a.getArg(""));
		a.add("");
		assertTrue("ArgumentList is expected to return empty string",
				a.getArg("").name.equals(""));
		a.add("Foo");
		assertTrue("ArgumentList is expected to return Foo",
				a.getArg("Foo").name.equals("Foo"));
		assertTrue("ArgumentList is expected to return empty string",
				a.getArg("").name.equals(""));
	}

}
