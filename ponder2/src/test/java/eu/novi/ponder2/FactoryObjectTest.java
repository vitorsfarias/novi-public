package eu.novi.ponder2;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Vector;

import eu.novi.ponder2.DomainP2Adaptor;

import eu.novi.ponder2.FactoryObject;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;

public class FactoryObjectTest extends TestCase {

	public FactoryObjectTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFactoryObject() {
		FactoryObject o = new FactoryObject(String.class);
		assertNotNull(o);
		try {
			Field f = FactoryObject.class.getDeclaredField("adaptorClass");
			f.setAccessible(true);
			Class c = (Class) f.get(o);
			assertEquals(c, String.class);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_create() {
		FactoryObject o = new FactoryObject(DomainP2Adaptor.class);
		try {
			P2Object num = P2Object.create();
			P2Object dom = o.operation_create(null, "create", num);
			dom.operation(null, "at:put:", P2Object.create("Foo"),
					P2Object.create(7));
			dom.operation(null, "at:put:", P2Object.create("Fred"),
					P2Object.create(12));
			P2Hash map = new P2Hash();
			map.put("myNum", P2Object.create(3));
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 | myNum := myNum * arg2 ]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);
			P2Object[] result = dom.operation(null, "collect:", b).asArray();
			assertEquals(3 * 7 + 3 * 12,
					result[0].asInteger() + result[1].asInteger());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
