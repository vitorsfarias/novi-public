package eu.novi.ponder2;

import java.lang.reflect.Constructor;

import eu.novi.ponder2.DomainP2Adaptor;

import eu.novi.ponder2.Shell;

import junit.framework.TestCase;

public class ShellTest extends TestCase {

	public ShellTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testInitialise() {
		System.out.println("ShellTest.testInitialise():");
		DomainP2Adaptor dp2a = new DomainP2Adaptor();
		int i = Shell.initialise(dp2a, 13570, false);
		assertTrue(i == 13570 || i == 0);
		assertEquals(Shell.initialise(dp2a, 13570, false), 0);
		System.out.println();
	}

	public void testRun() {
		System.out.println("ShellTest.testRun():");
		try {
			Constructor c = Shell.class.getDeclaredConstructor(int.class,
					boolean.class);
			c.setAccessible(true);
			DomainP2Adaptor dp2a = new DomainP2Adaptor();
			int i = Shell.initialise(dp2a, 13575, false);
			Shell s = (Shell) c.newInstance(13575, false);
			s.run();
		} catch (Exception e) {
			fail(e.toString());
		}
		System.out.println();
	}

}
