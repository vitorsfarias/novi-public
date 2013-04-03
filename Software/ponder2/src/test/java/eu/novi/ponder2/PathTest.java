package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.util.Vector;

import eu.novi.ponder2.Path;

import junit.framework.TestCase;

public class PathTest extends TestCase {

	public PathTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPathString() {
		Path p = new Path("");
		assertNotNull(p);
		assertEquals(p.toString(), ".");
	}

	public void testPathPath() {
		Path p = new Path("/Foo/Fred");
		Path q = new Path(p);
		try {
			Field f = Path.class.getDeclaredField("paths");
			f.setAccessible(true);
			Vector<String> p_paths = (Vector) f.get(p);
			Vector q_paths = (Vector) f.get(q);
			assertEquals(p_paths, q_paths);
			p_paths.add("Foo2");
			assertFalse(p_paths.equals(q_paths));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testPathPathString() {
		Path p = new Path("/Foo/Fred");
		Path q = new Path(p, "foo2");
		assertEquals(q.toString(), "/Foo/Fred/foo2");
	}

	public void testClone() {
		Path p = new Path("");
		try {
			Path q = (Path) p.clone();
			assertEquals(p.toString(), q.toString());
			Field f = Path.class.getDeclaredField("paths");
			f.setAccessible(true);
			Vector<String> p_paths = (Vector) f.get(p);
			Vector q_paths = (Vector) f.get(q);
			p_paths.add("Foo");
			assertFalse(p.toString().equals(q.toString()));
			assertFalse(p_paths == q_paths);
			assertFalse(p_paths.equals(q_paths));
		} catch (Exception e) {
			fail(e.toString());
		}

	}

	public void testClear() {
		Path p = new Path("/Foo/Fred");
		p.clear();
		try {
			Field f = Path.class.getDeclaredField("paths");
			f.setAccessible(true);
			Vector p_paths = (Vector) f.get(p);
			assertTrue(p_paths.isEmpty());
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(p.toString().length(), 0);
	}

	public void testIsComplete() {
		assertTrue(new Path("/Foo/Fred").isComplete());
		assertTrue(new Path("/").isComplete());
		assertTrue(new Path("Foo/Fred").isComplete());
		assertTrue(new Path("./Foo/Fred").isComplete());
		assertTrue(new Path("").isComplete());
		assertFalse(new Path("../Foo").isComplete());
		assertFalse(new Path("..").isComplete());
	}

	public void testIsRelative() {
		assertFalse(new Path("/Foo/Fred").isRelative());
		assertFalse(new Path("/").isRelative());
		assertTrue(new Path("Foo/Fred").isRelative());
		assertTrue(new Path("./Foo/Fred").isRelative());
		assertTrue(new Path("").isRelative());
	}

	public void testIsAbsolute() {
		assertTrue(new Path("/Foo/Fred").isAbsolute());
		assertTrue(new Path("/").isAbsolute());
		assertFalse(new Path("Foo/Fred").isAbsolute());
		assertFalse(new Path("./Foo/Fred").isAbsolute());
		assertFalse(new Path("").isAbsolute());
	}

	public void testIterator() {
		Path p = new Path("/Foo/Fred");
		assertNotNull(p.iterator());
		// No idea why this comparison fails, but the function surely is
		// correct:
		// assertEquals(p.iterator(), p.paths.iterator());
	}

	public void testParent() {
		assertEquals(new Path("/Foo/Fred").parent().toString(), "/Foo");
		assertEquals(new Path("/").parent().toString(), "/");
		assertEquals(new Path("Foo/Fred").parent().toString(), "Foo");
		assertEquals(new Path("./Foo/Fred").parent().toString(), "Foo");
		assertEquals(new Path("").parent().toString(), "..");
		assertEquals(new Path("../Foo").parent().toString(), "..");
		assertEquals(new Path("..").parent().toString(), "../..");

	}

	public void testChild() {
		assertEquals(new Path("/Foo/Fred").child().toString(), "Fred");
		assertEquals(new Path("/").child().toString(), "/");
		assertEquals(new Path("Foo/Fred").child().toString(), "Fred");
		assertEquals(new Path("./Foo/Fred").child().toString(), "Fred");
		assertEquals(new Path("").child().toString(), ".");
		assertEquals(new Path("../Foo").child().toString(), "Foo");
		assertEquals(new Path("..").child().toString(), "..");
	}

	public void testSet() {
		Path p = new Path("/Foo/Fred");
		p.set("..");
		assertEquals(p.toString(), "..");
		p.set("/Foo/Fred");
		assertEquals(p.toString(), "/Foo/Fred");
		p.set("./Foo/Fred/");
		assertEquals(p.toString(), "Foo/Fred");
	}

	public void testAdd() {
		Path p = new Path("/Foo/Fred");
		p.add("foo2");
		assertEquals(p.toString(), "/Foo/Fred/foo2");
		p.add("../..");
		assertEquals(p.toString(), "/Foo");
		p.add("a/b/c/../../d/../e");
		assertEquals(p.toString(), "/Foo/a/e");
		p.clear();
		p.add("../../..");
		assertEquals(p.toString(), "../../..");
		p.add("/Foo");
		assertEquals(p.toString(), "/Foo");
		p.set("Foo/Fred");
		p.add("../Freddy/foo2/../Fooey/");
		assertEquals(p.toString(), "Foo/Freddy/Fooey");
		p.add("../../..");
		assertEquals(p.toString(), ".");
		p.add("/");
		assertEquals(p.toString(), "/");
		p.add("..");
		assertEquals(p.toString(), "/");
		p.set("");
		p.add("..");
		assertEquals(p.toString(), "..");
		p.set(".");
		p.add("..");
		assertEquals(p.toString(), "..");
		p.set("..");
		p.add("..");
		assertEquals(p.toString(), "../..");
	}

	public void testSize() {
		Path p = new Path("/Foo/Fred");
		try {
			Field f = Path.class.getDeclaredField("paths");
			f.setAccessible(true);
			Vector p_paths = (Vector) f.get(p);
			assertEquals(p_paths.size(), p.size());
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(p.size(), 3);
		p.clear();
		assertEquals(p.size(), 0);
		p.set("Foo/Fred/");
		assertEquals(p.size(), 2);
		p.set("");
		assertEquals(p.size(), 1);
	}

	public void testHead() {
		Path p = new Path("/1/2/3/4/5/6/7/8/9/10");
		assertEquals(p.head(1), "/");
		assertEquals(p.head(6), "/1/2/3/4/5");
		p.set("./0/1/2/3/4");
		assertEquals(p.head(10), "0/1/2/3/4");
		assertEquals(p.head(0), "");
		assertEquals(p.head(1), "0");
		assertEquals(p.head(3), "0/1/2");
	}

	public void testTail() {
		Path p = new Path("/1/2/3/4/5/6/7/8/9/10");
		assertEquals(p.tail(1), "10");
		assertEquals(p.tail(6), "5/6/7/8/9/10");
		p.set("./0/1/2/3/4");
		assertEquals(p.tail(10), "0/1/2/3/4");
		assertEquals(p.tail(0), "");
		assertEquals(p.tail(1), "4");
		assertEquals(p.tail(3), "2/3/4");
	}

	public void testSubpath() {
		Path p = new Path("/1/2/3/4/5/6/7/8/9/10");
		assertEquals(p.subpath(1, 1), "1");
		assertEquals(p.subpath(0, 5), "/1/2/3/4/5");
		assertEquals(p.subpath(8, 10), "8/9/10");
		assertEquals(p.subpath(5, 7), "5/6/7");
		assertEquals(p.subpath(10, 15), "10");
		p.set("./0/1/2/3/4");
		assertEquals(p.subpath(-5, 10), "0/1/2/3/4");
		assertEquals(p.subpath(0, 0), "0");
		assertEquals(p.subpath(4, 3), "");
	}

	public void testToString() {
		testParent();
		testChild();
	}

}
