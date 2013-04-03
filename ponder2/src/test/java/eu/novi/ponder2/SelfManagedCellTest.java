/*package eu.novi.ponder2;

import java.lang.reflect.Field;

import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.objects.P2Object;

import junit.framework.TestCase;

public class SelfManagedCellTest extends TestCase {

	SelfManagedCell smc;

	public SelfManagedCellTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SelfManagedCell.RootDomain = null;
		smc = new SelfManagedCell(P2Object.create());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		smc = null;
		SelfManagedCell.RootDomain = null;
		Field f = P2ObjectAdaptor.class.getDeclaredField("authorisationModule");
		f.setAccessible(true);
		f.set(null, null);
	}

	public void testSelfManagedCell() {
		assertNotNull(smc);
		assertNotNull(SelfManagedCell.RootDomain);
		assertEquals(SelfManagedCell.RootDomain, P2Object.create());
	}

	public void testTraceBoolean() {
		SelfManagedCell smc = new SelfManagedCell(P2Object.create());
		assertTrue(smc.trace(true));
		assertTrue(SelfManagedCell.SystemTrace);
		assertFalse(smc.trace(false));
		assertFalse(SelfManagedCell.SystemTrace);
	}

	public void testPrint() {
		System.out.println("SelfManagedCellTest.testPrint():");
		assertEquals(smc.print("Foo"), "Foo");
		System.out.println();
	}

	public void testGetDefaulAuthPolicy() {
		System.out.println("SelfManagedCellTest.testGetDefaultAuthPolicy():");
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.main(new String[] { "-boot", "-", "-auth", "allow",
				"-port", "0" });
		assertTrue(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.main(new String[] { "-boot", "-", "-auth", "Foo",
				"-port", "0" });
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		System.out.println();
	}

	public void testStart() {
		System.out.println("SelfManagedCellTest.testStart():");
		SelfManagedCell.start(new String[] { "-multiple", "-boot", "-",
				"-port", "60000" });
		assertTrue(SelfManagedCell.port == 60000);
		SelfManagedCell.start(new String[] { "-multiple", "-boot", "-",
				"-trace", "-port", "0" });
		assertTrue(SelfManagedCell.SystemTrace);
		smc.trace(false);
		SelfManagedCell.start(new String[] { "-port", "0", "-multiple",
				"-boot", "-" });
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.start(new String[] { "-boot", "-", "-auth", "allow",
				"-port", "0" });
		assertTrue(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.start(new String[] { "-boot", "-", "-auth", "Foo",
				"-port", "0" });
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		System.out.println();
	}

	public void testMain() {
		System.out.println("SelfManagedCellTest.testMain():");
		SelfManagedCell.main(new String[] { "-multiple", "-boot", "-", "-port",
				"60005" });
		assertTrue(SelfManagedCell.port == 60005);
		SelfManagedCell.main(new String[] { "-multiple", "-boot", "-",
				"-trace", "-port", "0" });
		assertTrue(SelfManagedCell.SystemTrace);
		smc.trace(false);
		SelfManagedCell.main(new String[] { "-port", "0", "-multiple", "-boot",
				"-" });
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.main(new String[] { "-boot", "-", "-auth", "allow",
				"-port", "0" });
		assertTrue(SelfManagedCell.getDefaulAuthPolicy());
		SelfManagedCell.main(new String[] { "-boot", "-", "-auth", "Foo",
				"-port", "0" });
		assertFalse(SelfManagedCell.getDefaulAuthPolicy());
		System.out.println();
	}

}
*/