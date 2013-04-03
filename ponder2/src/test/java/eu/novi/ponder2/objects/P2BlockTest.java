package eu.novi.ponder2.objects;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import junit.framework.TestCase;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

public class P2BlockTest extends TestCase {

	P2Block b;
	HashMap<String, P2Object> map;

	public P2BlockTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		map = new HashMap<String, P2Object>();
		map.put("myNum", P2Object.create(7));
		b = new P2Block(map, new TaggedElement("[ :arg1 | 5 < arg1 ]"));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		map = null;
		b = null;
	}

	private P2Block translate(String code) throws Ponder2OperationException {
		try {
			Constructor<PonderTalk> c = PonderTalk.class
					.getDeclaredConstructor(new Class[] { P2Object.class });
			c.setAccessible(true);
			PonderTalk p = c.newInstance(new Object[] { P2Object.create() });
			return (P2Block) p.executePonderTalk(code);
		} catch (Exception e) {
			fail(e.toString());
		}
		return null;
	}

	public void testAsBlock() {
		try {
			assertEquals(b.asBlock(), b);
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		}
	}

	public void testOperation() {
		try {
			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			b = (P2Block) new XMLParser(new P2Hash(map)).execute(
					P2Object.create(), p2xml);
			assertEquals(b.execute(P2Object.create()).asInteger(),
					3 * 7 + 12 + 7);

			b = translate("[ :arg12 :arg299 | arg12 * arg299]");
			assertEquals(
					b.operation(null, "value:value:", P2Object.create(7),
							P2Object.create(12)).asInteger(), 7 * 12);
			b = translate("[ :value | 5 < value ]");
			assertTrue(b.operation(null, "value:", P2Object.create(6))
					.asBoolean());
			assertFalse(b.operation(null, "value:", P2Object.create(2))
					.asBoolean());

		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testP2Block() {
		assertNotNull(b);
		// TODO print hashes out properly
		// assertEquals("{myNum=7}<[ :arg1 | 5 < arg1 ]/>", b.toString());
		assertEquals("<[ :arg1 | 5 < arg1 ]/>", b.toString());
	}

	public void testOperation_hasArgs() {
		map.clear();
		map.put("arg12", P2Object.create(13));
		map.put("arg299", P2Object.create(300));
		try {
			b = translate("[ :arg12 :arg299 | arg12 * arg299]");
			assertTrue(b.operation_hasArgs(null, new P2Hash(map)));
			b = translate("[ :arg12 :arg299 :arg3 | arg12 * arg299]");
			assertFalse(b.operation_hasArgs(null, new P2Hash(map)));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_valueHash() {
		try {
			b = translate("[ :foo :fred :fred2 | foo * fred + fred2 ]");
			P2Hash hash = new P2Hash();
			hash.put("foo", P2Object.create(3));
			hash.put("fred", P2Object.create(7));
			hash.put("fred2", P2Object.create(24));
			assertEquals(b.operation_valueHash(P2Object.create(), hash)
					.asInteger(), 3 * 7 + 24);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_value0() {
		try {
			b = translate("[ 3 * 7 + 12 ]");
			assertEquals(b.operation_value0(P2Object.create()).asInteger(),
					3 * 7 + 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}

	}

	public void testOperation_value1() {
		try {
			b = translate("[ :arg1 | arg1 * 3 * 7 + 12 ]");
			assertEquals(
					b.operation_value1(P2Object.create(), P2Object.create(27))
							.asInteger(), 27 * 3 * 7 + 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_value2() {
		try {
			b = translate("[ :arg1 :arg3 | arg1 * 3 * 7 + arg3 ]");
			assertEquals(
					b.operation_value2(P2Object.create(), P2Object.create(20),
							P2Object.create(-144)).asInteger(),
					20 * 3 * 7 - 144);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_value3() {
		try {
			b = translate("[ :arg1 :arg2 :arg3 | arg1 * arg2 + arg3 ]");
			assertEquals(
					b.operation_value3(P2Object.create(), P2Object.create(12),
							P2Object.create(7), P2Object.create(3)).asInteger(),
					12 * 7 + 3);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_array() {
		try {
			b = translate("[ :arg1 :arg2 :arg3 | arg1 * arg2 + arg3 ]");
			assertEquals(
					b.operation_array(
							P2Object.create(),
							new P2Array(P2Object.create(12),
									P2Object.create(7), P2Object.create(3)))
							.asInteger(), 12 * 7 + 3);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testExecute() {
		try {
			b = translate("[ 3 * 7 + 12]");
			assertEquals(b.execute(P2Object.create()).asInteger(), 3 * 7 + 12);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testToString() {
		map.put("bool", P2Object.create(true));
		b = null;
		b = new P2Block(map, new TaggedElement("[ :arg1 | 2 < arg1 ]"));
		// TODOCan't print hashes at the moment
		// assertEquals("{myNum=7, bool=true}<[ :arg1 | 2 < arg1 ]/>",
		// b.toString());
		assertEquals("<[ :arg1 | 2 < arg1 ]/>", b.toString());
	}

}
