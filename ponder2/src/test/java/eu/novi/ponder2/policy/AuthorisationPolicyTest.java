package eu.novi.ponder2.policy;

import java.lang.reflect.Field;
import java.util.HashMap;

import junit.framework.TestCase;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import eu.novi.ponder2.policy.AuthorisationPolicy;

public class AuthorisationPolicyTest extends TestCase {

	AuthorisationPolicy p;

	public AuthorisationPolicyTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		p = new AuthorisationPolicy(P2Object.create(), "Foo",
				P2Object.create(), "ts");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		p = null;
	}

	public void testAuthorisationPolicy() {
		assertNotNull(p);
		assertTrue(p instanceof AuthorisationPolicy);
	}

	public void testOperation_set_final() {
		testIsFinal();
	}

	public void testOperation_set_inneg() {
		testIsAuthRequestNeg();
	}

	public void testOperation_set_outneg() {
		testIsAuthReplyNeg();
	}

	public void testOperation_in_condition() {
		P2Block b = new P2Block(new HashMap<String, P2Object>(),
				new TaggedElement("[ :arg1 | 5 < arg1 ]"));
		assertEquals(p.operation_in_condition(b), b);
		try {
			Field f = AuthorisationPolicy.class
					.getDeclaredField("requestCondition");
			f.setAccessible(true);
			P2Block rq = (P2Block) f.get(p);
			assertEquals(rq, b);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_out_condition() {
		P2Block b = new P2Block(new HashMap<String, P2Object>(),
				new TaggedElement("[ :arg1 | 5 < arg1 ]"));
		assertEquals(p.operation_out_condition(b), b);
		try {
			Field f = AuthorisationPolicy.class
					.getDeclaredField("replyCondition");
			f.setAccessible(true);
			P2Block rp = (P2Block) f.get(p);
			assertEquals(rp, b);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testGetSubject() {
		P2Object o = P2Object.create(2);
		assertEquals(p.getSubject(), P2Object.create().getManagedObject());
		p = new AuthorisationPolicy(o, "Foo", P2Object.create(), "ts");
		assertEquals(p.getSubject(), o.getManagedObject());
	}

	public void testGetTarget() {
		P2Object o1 = P2Object.create(1);
		P2Object o2 = P2Object.create(2);
		assertNotNull(p.getTarget());
		p.setTarget(o1);
		assertEquals(p.getTarget(), o1.getManagedObject());
		p.setTarget(o2);
		assertEquals(p.getTarget(), o2.getManagedObject());
	}

	public void testIsAuthRequestNeg() {
		assertFalse(p.isAuthRequestNeg());
		p.operation_set_inneg();
		assertTrue(p.isAuthRequestNeg());
	}

	public void testIsAuthReplyNeg() {
		assertFalse(p.isAuthReplyNeg());
		p.operation_set_outneg();
		assertTrue(p.isAuthReplyNeg());
	}

	public void testGetAction() {
		assertEquals(p.getAction(), "Foo");
	}

	public void testIsFinal() {
		assertFalse(p.isFinal());
		p.operation_set_final();
		assertTrue(p.isFinal());
	}

	public void testSetTarget() {
		testGetTarget();
	}

	public void testGetFocus() {
		assertTrue(p.hasFocus('t'));
	}

	public void testToString() {
		String s = p.toString();
		assertEquals(s.charAt(0), '{');
		// System.out.println(s);
		// assertEquals(s.substring(8),
		// ","+P2Object.create().getOID()+","+P2Object.create().getOID()+",Normal,F,NOTactive,Request+,Foo,Result+}");
		assertTrue(s.contains("," + P2Object.create().getOID() + ","
				+ P2Object.create().getOID()
				+ ",Normal,ts,NOTactive,Request+,Foo,Result+}"));
	}

	public void testCheckRequestCondition() {
		try {
			P2Object target = null;
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 :arg3 | arg1 & arg2 | arg3 ]");
			P2Block b = (P2Block) new XMLParser(
					P2Object.create(new HashMap<String, P2Object>())).execute(
					P2Object.create(), p2xml);
			// true expected when no condition present
			assertTrue(p.checkRequestCondition(null, target));
			p.operation_in_condition(b);
			assertTrue(p.checkRequestCondition(null, target,
					P2Object.create(true), P2Object.create(false),
					P2Object.create(true)));
			assertTrue(p.checkRequestCondition(null, target,
					P2Object.create(true), P2Object.create(true),
					P2Object.create(false)));
			assertFalse(p.checkRequestCondition(null, target,
					P2Object.create(false), P2Object.create(false),
					P2Object.create(false)));
			assertFalse(p.checkRequestCondition(null, target,
					P2Object.create(true), P2Object.create(false),
					P2Object.create(false)));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testCheckReturnCondition() {
		try {
			P2Object target = null;
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 :arg3 | arg1 & (arg2 | arg3) ]");
			P2Block b = (P2Block) new XMLParser(
					P2Object.create(new HashMap<String, P2Object>())).execute(
					P2Object.create(), p2xml);
			// true expected when no condition present
			assertTrue(p.checkReturnCondition(null, target));
			p.operation_out_condition(b);
			assertTrue(p.checkReturnCondition(null, target,
					P2Object.create(true), P2Object.create(false),
					P2Object.create(true)));
			assertTrue(p.checkReturnCondition(null, target,
					P2Object.create(true), P2Object.create(true),
					P2Object.create(false)));
			assertFalse(p.checkReturnCondition(null, target,
					P2Object.create(false), P2Object.create(true),
					P2Object.create(false)));
			assertFalse(p.checkReturnCondition(null, target,
					P2Object.create(true), P2Object.create(false),
					P2Object.create(false)));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testCheckCondition() {
		try {
			// checking "in"
			P2Object target = null;
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 :arg3 | arg1 & arg2 | arg3 ]");
			P2Block b = (P2Block) new XMLParser(
					P2Object.create(new HashMap<String, P2Object>())).execute(
					P2Object.create(), p2xml);
			// true expected when no condition present
			assertTrue(p.checkCondition(null, target, "in", new P2Object[] {}));
			p.operation_in_condition(b);
			assertTrue(p.checkCondition(
					null,
					target,
					"in",
					new P2Object[] { P2Object.create(true),
							P2Object.create(false), P2Object.create(true) }));
			assertTrue(p.checkCondition(
					null,
					target,
					"in",
					new P2Object[] { P2Object.create(true),
							P2Object.create(true), P2Object.create(false) }));
			assertFalse(p.checkCondition(
					null,
					target,
					"in",
					new P2Object[] { P2Object.create(false),
							P2Object.create(false), P2Object.create(false) }));
			assertFalse(p.checkCondition(
					null,
					target,
					"in",
					new P2Object[] { P2Object.create(true),
							P2Object.create(false), P2Object.create(false) }));

			// checking "out"
			p2xml = P2Compiler
					.parse("[ :arg1 :arg2 :arg3 | arg1 & (arg2 | arg3) ]");
			b = (P2Block) new XMLParser(
					P2Object.create(new HashMap<String, P2Object>())).execute(
					P2Object.create(), p2xml);
			// true expected when no condition present
			assertTrue(p.checkCondition(null, target, "out", new P2Object[] {}));
			p.operation_out_condition(b);
			assertTrue(p.checkCondition(
					null,
					target,
					"out",
					new P2Object[] { P2Object.create(true),
							P2Object.create(false), P2Object.create(true) }));
			assertTrue(p.checkCondition(
					null,
					target,
					"out",
					new P2Object[] { P2Object.create(true),
							P2Object.create(true), P2Object.create(false) }));
			assertFalse(p.checkCondition(
					null,
					target,
					"out",
					new P2Object[] { P2Object.create(false),
							P2Object.create(true), P2Object.create(false) }));
			assertFalse(p.checkCondition(
					null,
					target,
					"out",
					new P2Object[] { P2Object.create(true),
							P2Object.create(false), P2Object.create(false) }));
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

}
