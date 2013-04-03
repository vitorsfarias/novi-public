package eu.novi.ponder2.policy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.ObligationPolicy;

import junit.framework.TestCase;
import eu.novi.ponder2.EventTemplateP2Adaptor;

public class ObligationPolicyTest extends TestCase {

	public ObligationPolicyTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testObligationPolicy() {
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		assertNotNull(p);
	}

	public void testEvent() {
		System.out.println("ObligationPolicyTest.testEvent():");
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		try {
			// assertFalse(p.operation_event(null).asBoolean());
			SelfManagedCell.RootDomain = P2Object.create();
			p.setActive(true);
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			Event ev = new Event(null, evtmp, P2Object.create(7),
					P2Object.create(12));
			p.operation_event(evtmp);
			String p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (12 == Fred)]");
			P2Block b = (P2Block) new XMLParser().execute(P2Object.create(),
					p2xml);
			assertEquals(p.operation_condition(b), b);
			assertTrue(p.operation_canExecute(ev).asBoolean());
			P2Hash map = new P2Hash();
			map.put("myDiet", P2Object.create(""));
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | myDiet := \"Apple nutrition each day: Foo eats \" + Foo + \", Fred eats \" + Fred + \".\"]");
			P2Block blk = (P2Block) new XMLParser(map).execute(
					P2Object.create(), p2xml);
			assertEquals(p.operation_action(blk), blk);
			p.event(ev);
			assertEquals(blk.getVariable("myDiet").toString(),
					"Apple nutrition each day: Foo eats 7, Fred eats 12.");
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | result := (Foo+Foo)*Fred]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_action(b), b);
			p.event(ev);
			assertEquals(b.getVariable("result").asInteger(), (7 + 7) * 12);
		} catch (Exception e) {
			fail(e.toString());
		}
		System.out.println();
	}

	public void testOperation_canExecute() {
		System.out.println("ObligationPolicyTest.testOperation_canExecute():");
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		assertFalse(p.isActive());
		try {
			assertFalse(p.operation_canExecute(null).asBoolean());
			SelfManagedCell.RootDomain = P2Object.create();
			p.setActive(true);
			P2Array keys = new P2Array(P2Object.create("Foo"),
					P2Object.create("Fred"));
			EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
					"create:", keys);
			Event ev = new Event(null, evtmp, P2Object.create(7),
					P2Object.create(12));
			assertFalse(p.operation_canExecute(ev).asBoolean());
			p.operation_event(evtmp);
			assertTrue(p.operation_canExecute(ev).asBoolean());
			String p2xml = P2Compiler
					.parse("[ :Foo :Fred | (8 > Foo) & (11 < Fred)]");
			P2Block b = (P2Block) new XMLParser().execute(P2Object.create(),
					p2xml);
			assertEquals(p.operation_condition(b), b);
			assertTrue(p.operation_canExecute(ev).asBoolean());
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (12 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_condition(b), b);
			assertTrue(p.operation_canExecute(ev).asBoolean());
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (13 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_condition(b), b);
			assertFalse(p.operation_canExecute(ev).asBoolean());
		} catch (Exception e) {
			fail(e.toString());
		}
		System.out.println();
	}

	public void testOperation_execute() throws Exception {
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		P2Array keys = new P2Array(P2Object.create("Foo"),
				P2Object.create("Fred"));
		EventTemplateP2Adaptor evtmp = new EventTemplateP2Adaptor(null,
				"create:", keys);
		Event ev = new Event(null, evtmp, P2Object.create(7),
				P2Object.create(12));
		String p2xml = P2Compiler
				.parse("[ :Foo :Fred | result := Foo * Fred + 3]");
		P2Block b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
		assertEquals(p.operation_action(b), b);
		p.operation_execute(ev);
		assertEquals(b.getVariable("result").asInteger(), 7 * 12 + 3);
		p2xml = P2Compiler.parse("[ :Foo :Fred | result := Foo + Fred]");
		b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
		assertEquals(p.operation_action(b), b);
		p.operation_execute(ev);
		assertEquals(b.getVariable("result").asInteger(), 7 + 12);
		P2Hash map = new P2Hash();
		map.put("myDiet", P2Object.create(""));
		p2xml = P2Compiler
				.parse("[ :Foo :Fred | myDiet := \"Apple nutrition each day: Foo eats \" + Foo + \", Fred eats \" + Fred + \".\"]");
		P2Block blk = (P2Block) new XMLParser(map).execute(P2Object.create(),
				p2xml);
		assertEquals(p.operation_action(blk), blk);
		p.operation_execute(ev);
		assertEquals(blk.getVariable("myDiet").asString(),
				"Apple nutrition each day: Foo eats 7, Fred eats 12.");
		p2xml = P2Compiler.parse("[ :Foo :Fred | result := (Foo+Foo)*Fred]");
		b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
		assertEquals(p.operation_action(b), b);
		p.operation_execute(ev);
		assertEquals(b.getVariable("result").asInteger(), (7 + 7) * 12);
	}

	public void testOperation_event() {
		P2Object someP2o = P2Object.create();
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		P2Object v = p.operation_event(someP2o);
		try {
			Field f = ObligationPolicy.class.getDeclaredField("eventTemplate");
			f.setAccessible(true);
			P2Object p_eventTemplate = (P2Object) f.get(p);
			assertEquals(p_eventTemplate, someP2o);
			assertEquals(v, someP2o);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testOperation_condition() {
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		try {
			Field f = ObligationPolicy.class.getDeclaredField("conditions");
			f.setAccessible(true);
			List l = (List) f.get(p);
			assertTrue(l.isEmpty());
			String p2xml = P2Compiler
					.parse("[ :Foo :Fred | (8 > Foo) & (11 < Fred)]");
			P2Block b = (P2Block) new XMLParser().execute(P2Object.create(),
					p2xml);
			assertEquals(p.operation_condition(b), b);
			assertEquals(l.size(), 1);
			assertTrue(l.contains(b));
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (12 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_condition(b), b);
			assertEquals(l.size(), 2);
			assertTrue(l.contains(b));
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (13 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_condition(b), b);
			assertEquals(l.size(), 3);
			assertTrue(l.contains(b));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_action() {
		ObligationPolicy p = new ObligationPolicy(P2Object.create());
		try {
			Field f = ObligationPolicy.class.getDeclaredField("actions");
			f.setAccessible(true);
			List l = (List) f.get(p);
			assertTrue(l.isEmpty());
			String p2xml = P2Compiler
					.parse("[ :Foo :Fred | (8 > Foo) & (11 < Fred)]");
			P2Block b = (P2Block) new XMLParser().execute(P2Object.create(),
					p2xml);
			assertEquals(p.operation_action(b), b);
			assertEquals(l.size(), 1);
			assertTrue(l.contains(b));
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (12 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_action(b), b);
			assertEquals(l.size(), 2);
			assertTrue(l.contains(b));
			p2xml = P2Compiler
					.parse("[ :Foo :Fred | (7 == Foo) & (13 == Fred)]");
			b = (P2Block) new XMLParser().execute(P2Object.create(), p2xml);
			assertEquals(p.operation_action(b), b);
			assertEquals(l.size(), 3);
			assertTrue(l.contains(b));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
