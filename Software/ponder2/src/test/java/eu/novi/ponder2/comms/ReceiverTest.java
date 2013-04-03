package eu.novi.ponder2.comms;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.comms.Receiver;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;
import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.EventTemplateP2Adaptor;

public class ReceiverTest extends TestCase {

	public ReceiverTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetObject() {
		System.out.println("ReceiverTest.testGetObject():");
		try {
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
					P2Object.create());
			SelfManagedCell.RootDomain = null;
			new SelfManagedCell(dom);
			dom.operation(null, "at:put:", P2Object.create("Foo"), evtmp);
			assertEquals(Receiver.getObject(null, "Foo"), evtmp);
			assertEquals(Receiver.getObject(null, "Foo"), evtmp);
			// TODO: something weird
			// comment out the following line, and testExecute will fail - why?
			OID.getAddresses().clear();
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
		System.out.println();
	}

	public void testExecute() {
		System.out.println("ReceiverTest.testExecute():");
		try {
			P2Hash map = new P2Hash();
			map.put("myDiet", P2Object.create(3));
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 | myDiet := myDiet + arg2 ]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);
			DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
					P2Object.create());
			Receiver.execute(
					null,
					dom.getOID(),
					null,
					"at:put:",
					new P2Object[] { P2Object.create("Foo"), P2Object.create(5) });
			Receiver.execute(
					null,
					dom.getOID(),
					null,
					"at:put:",
					new P2Object[] { P2Object.create("Fred"),
							P2Object.create(7) });
			Receiver.execute(
					null,
					dom.getOID(),
					null,
					"at:put:",
					new P2Object[] { P2Object.create("Fred2"),
							P2Object.create(12) });
			Receiver.execute(null, dom.getOID(), null, "do:",
					new P2Object[] { b });
			Field f = P2Block.class.getDeclaredField("variables");
			f.setAccessible(true);
			HashMap<String, P2Object> m = (HashMap) f.get(b);
			assertEquals(m.get("myDiet").asInteger(), 3);// replaced this by
															// Yiannos
															// 3+5+7+12);
		} catch (Exception e) {
			fail(e.toString());
		}
		System.out.println();
	}

}
