package eu.novi.ponder2.policy;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;

import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.StaticAuthPolicySearch;

public class AuthPolicySearchTest extends TestCase {

	public AuthPolicySearchTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEvaluate() {
		Mockery m = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};
		final AuthPolicyHolder holder = m.mock(AuthPolicyHolder.class); // holder
																		// is
																		// mocked,
																		// now
																		// consider
																		// evaluate
																		// and
																		// search
																		// calls
		final AuthorisationPolicy policy = m.mock(AuthorisationPolicy.class);
		final P2Object subject = P2Object.create(7);
		final P2Object target = P2Object.create(12);
		final P2Object[] args = new P2Object[] { subject, target,
				P2Object.create(24) };
		final Sequence seq = m.sequence("seq");
		m.checking(new Expectations() {
			{
				// 1: PEP1 -> AUTH
				// one (holder).getOutgoingAuthPol(); will(returnValue(policy));
				// inSequence(seq);
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkRequestCondition(subject, target, args);
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthRequestNeg();
				will(returnValue(false));
				inSequence(seq);
				one(holder).setOutgoingAuthPol(policy);
				inSequence(seq);
				// 2: PEP2 -> AUTH
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkRequestCondition(subject, target, args);
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthRequestNeg();
				will(returnValue(false));
				inSequence(seq);
				one(holder).setIncomingAuthPol(policy);
				inSequence(seq);
				// 3: PEP2 -> NOTAUTH
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkRequestCondition(subject, target, args);
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthRequestNeg();
				will(returnValue(true));
				inSequence(seq);
				// 4: PEP1 -> POL_NOT_DEFINED
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkRequestCondition(subject, target, args);
				will(returnValue(false));
				inSequence(seq);
				// 5: PEP3 -> AUTH
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target, args);
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(false));
				inSequence(seq);
				// 6: PEP4 -> NOTAUTH
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target, args);
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(true));
				inSequence(seq);
				// 7: PEP3 -> POL_NOT_DEFINED
				one(policy).isActive();
				will(returnValue(false));
				inSequence(seq);
			}
		});
		// 1: PEP1 -> AUTH
		assertEquals(AuthPolicySearch.AUTH, AuthPolicySearch.evaluate(holder,
				policy, AuthorisationModule.PEP1, subject, target, args));
		// 2: PEP2 -> AUTH
		assertEquals(AuthPolicySearch.AUTH, AuthPolicySearch.evaluate(holder,
				policy, AuthorisationModule.PEP2, subject, target, args));
		// 3: PEP2 -> NOTAUTH
		assertEquals(AuthPolicySearch.NOTAUTH,
				AuthPolicySearch.evaluate(holder, policy,
						AuthorisationModule.PEP2, subject, target, args));
		// 4: PEP1 -> POL_NOT_DEFINED
		assertEquals(AuthPolicySearch.POL_NOT_DEFINED,
				AuthPolicySearch.evaluate(holder, policy,
						AuthorisationModule.PEP1, subject, target, args));
		// 5: PEP3 -> AUTH
		assertEquals(AuthPolicySearch.AUTH, AuthPolicySearch.evaluate(holder,
				policy, AuthorisationModule.PEP3, subject, target, args));
		// 6: PEP4 -> NOAUTH
		assertEquals(AuthPolicySearch.NOTAUTH,
				AuthPolicySearch.evaluate(holder, policy,
						AuthorisationModule.PEP4, subject, target, args));
		// 7: PEP3 -> POL_NOT_DEFINED
		assertEquals(AuthPolicySearch.POL_NOT_DEFINED,
				AuthPolicySearch.evaluate(holder, policy,
						AuthorisationModule.PEP3, subject, target, args));
		m.assertIsSatisfied();
	}

	public void testSearch() {
		AuthPolicySearch aps = new StaticAuthPolicySearch();
		new SelfManagedCell(P2Object.create());
		System.out.println(aps.search(new AuthPolicyHolder(),
				AuthorisationModule.PEP1, P2Object.create(), P2Object.create(),
				"*", 't', new P2Object[] {}, P2Object.create("Foo")));

		// P2Object.create().getManagedObject().
		// TODO fail("Not yet implemented");
	}

	public void testPrintState() {
		assertEquals(AuthPolicySearch.printState((short) 0),
				"The subject is AUTHORIZED");
		assertEquals(AuthPolicySearch.printState((short) 1),
				"The subject is NOTAUTHORIZED");
		assertEquals(AuthPolicySearch.printState((short) 2),
				"NO policy was DEFINED for the subject+target+action combo");
		assertEquals(AuthPolicySearch.printState((short) 3),
				"the value 3 is unknown");
		assertEquals(AuthPolicySearch.printState((short) 4),
				"the value 4 is unknown");
		assertEquals(AuthPolicySearch.printState((short) 5),
				"the value 5 is unknown");
	}

}
