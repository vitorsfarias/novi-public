package eu.novi.ponder2.policy;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;

import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.BasicAuthModule;

public class BasicAuthModuleTest extends TestCase {

	BasicAuthModule b;

	Mockery m;
	AuthPolicySearch aps;
	P2Object rd;
	AuthPolicyHolder holder;
	AuthorisationPolicy policy;

	@Override
	protected void setUp() throws Exception {
		m = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};
		b = new BasicAuthModule(null);
		rd = P2Object.create("Foo");
		b.setRootDomain(rd);
		aps = m.mock(AuthPolicySearch.class);
		b.aps = aps;
		holder = m.mock(AuthPolicyHolder.class); // holder is mocked, now
													// consider evaluate and
													// search calls
		policy = m.mock(AuthorisationPolicy.class);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		b = null;
	}

	public void testBasicAuthModule() {
		assertNotNull(b);
		assertNotNull(b.aps);
	}

	public void testRequestOutgoing() {
		final P2Object subject = P2Object.create(7);
		final P2Object target = P2Object.create(12);
		final P2Object[] args = new P2Object[] { subject, target,
				P2Object.create(24) };
		m.checking(new Expectations() {
			{
				one(aps).search(holder, AuthorisationModule.PEP1, subject,
						target, "Fred2", 's', args, null);
				will(returnValue(AuthPolicySearch.AUTH));
				one(aps).search(holder, AuthorisationModule.PEP1, subject,
						target, "Foo", 's', args, null);
				will(returnValue(AuthPolicySearch.NOTAUTH));
				one(aps).search(holder, AuthorisationModule.PEP1, subject,
						target, "foo2", 's', args, null);
				will(returnValue(AuthPolicySearch.POL_NOT_DEFINED));
			}
		});

		try {
			b.request(AuthorisationModule.PEP1, 's', null, rd, null, "Fred",
					null);
			b.request(AuthorisationModule.PEP1, 's', holder, subject, target,
					"Fred2", args);
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.request(AuthorisationModule.PEP1, 's', holder, subject, target,
					"Foo", args);
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo"))
				fail(e.toString());
		}
		try {
			b.request(AuthorisationModule.PEP1, 's', holder, subject, target,
					"foo2", args);
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("foo2"))
				fail(e.toString());
		}
		m.assertIsSatisfied();
	}

	public void testRequestIncoming() {
		final P2Object subject = P2Object.create(7);
		final P2Object target = P2Object.create(12);
		final P2Object[] args = new P2Object[] { subject, target,
				P2Object.create(24) };
		m.checking(new Expectations() {
			{
				one(aps).search(holder, AuthorisationModule.PEP2, subject,
						target, "Fred2", 't', args, null);
				will(returnValue(AuthPolicySearch.AUTH));
				one(aps).search(holder, AuthorisationModule.PEP2, subject,
						target, "Foo", 't', args, null);
				will(returnValue(AuthPolicySearch.NOTAUTH));
				one(aps).search(holder, AuthorisationModule.PEP2, subject,
						target, "foo2", 't', args, null);
				will(returnValue(AuthPolicySearch.POL_NOT_DEFINED));
			}
		});

		try {
			b.request(AuthorisationModule.PEP2, 't', null, rd, null, "Fred",
					null);
			b.request(AuthorisationModule.PEP2, 't', holder, subject, target,
					"Fred2", args);
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.request(AuthorisationModule.PEP2, 't', holder, subject, target,
					"Foo", args);
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo"))
				fail(e.toString());
		}
		try {
			b.request(AuthorisationModule.PEP2, 't', holder, subject, target,
					"foo2", args);
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("foo2"))
				fail(e.toString());
		}
		m.assertIsSatisfied();
	}

	public void testReplyOutgoing() {
		final P2Object subject = P2Object.create(7);
		final P2Object target = P2Object.create(12);
		final P2Object[] args = new P2Object[] { subject, target,
				P2Object.create(24) };
		final Sequence seq = m.sequence("seq");
		m.checking(new Expectations() {
			{
				// 1: == rd -> AUTH
				// 2: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == AUTH
				one(holder).getIncomingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target,
						P2Object.create());
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(false));
				inSequence(seq);
				// 3: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == NOAUTH
				one(holder).getIncomingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target,
						P2Object.create());
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(true));
				inSequence(seq);
				// 4: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == POL_NOT_DEFINED
				one(holder).getIncomingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(false));
				inSequence(seq);
				// 5: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == AUTH
				one(holder).getIncomingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP3, subject,
						target, "Foo5", 't', args, P2Object.create());
				will(returnValue(AuthPolicySearch.AUTH));
				inSequence(seq);
				// 6: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == NOAUTH
				one(holder).getIncomingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP3, subject,
						target, "Foo6", 't', args, P2Object.create());
				will(returnValue(AuthPolicySearch.NOTAUTH));
				inSequence(seq);
				// 7: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == POL_NOT_DEFINED
				one(holder).getIncomingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP3, subject,
						target, "Foo7", 't', args, P2Object.create());
				will(returnValue(AuthPolicySearch.POL_NOT_DEFINED));
				inSequence(seq);
			}
		});

		try {
			b.reply(AuthorisationModule.PEP3, 't', null, rd, null, "Fred",
					null, P2Object.create());
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo2", args, P2Object.create());
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo3", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo3"))
				fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo4", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo4"))
				fail(e.toString());
		}

		try {
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo5", args, P2Object.create());
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo6", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo6"))
				fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP3, 't', holder, subject, target,
					"Foo7", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo7"))
				fail(e.toString());
		}
		m.assertIsSatisfied();
	}

	public void testReplyIncoming() {
		final P2Object subject = P2Object.create(7);
		final P2Object target = P2Object.create(12);
		final P2Object[] args = new P2Object[] { subject, target,
				P2Object.create(24) };
		final Sequence seq = m.sequence("seq");
		m.checking(new Expectations() {
			{
				// 1: == rd -> AUTH
				// 2: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == AUTH
				one(holder).getOutgoingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target,
						P2Object.create());
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(false));
				inSequence(seq);
				// 3: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == NOAUTH
				one(holder).getOutgoingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(true));
				inSequence(seq);
				one(policy).checkReturnCondition(subject, target,
						P2Object.create());
				will(returnValue(true));
				inSequence(seq);
				one(policy).isAuthReplyNeg();
				will(returnValue(true));
				inSequence(seq);
				// 4: != rd -> holder.getIncomingAuthPol() != null ->
				// aps.evaluate == POL_NOT_DEFINED
				one(holder).getOutgoingAuthPol();
				will(returnValue(policy));
				inSequence(seq);
				one(policy).isActive();
				will(returnValue(false));
				inSequence(seq);
				// 5: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == AUTH
				one(holder).getOutgoingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP4, subject,
						target, "Foo5", 's', args, P2Object.create());
				will(returnValue(AuthPolicySearch.AUTH));
				inSequence(seq);
				// 6: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == NOAUTH
				one(holder).getOutgoingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP4, subject,
						target, "Foo6", 's', args, P2Object.create());
				will(returnValue(AuthPolicySearch.NOTAUTH));
				inSequence(seq);
				// 7: != rd -> holder.getIncomingAuthPol() == null -> aps.search
				// == POL_NOT_DEFINED
				one(holder).getOutgoingAuthPol();
				will(returnValue(null));
				inSequence(seq);
				one(aps).search(holder, AuthorisationModule.PEP4, subject,
						target, "Foo7", 's', args, P2Object.create());
				will(returnValue(AuthPolicySearch.POL_NOT_DEFINED));
				inSequence(seq);
			}
		});

		try {
			b.reply(AuthorisationModule.PEP4, 's', null, rd, null, "Fred",
					null, P2Object.create());
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo2", args, P2Object.create());
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo3", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo3"))
				fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo4", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo4"))
				fail(e.toString());
		}

		try {
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo5", args, P2Object.create());
		} catch (Ponder2AuthorizationException e) {
			fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo6", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo6"))
				fail(e.toString());
		}
		try {
			b.reply(AuthorisationModule.PEP4, 's', holder, subject, target,
					"Foo7", args, P2Object.create());
			fail("Exception expected!");
		} catch (Ponder2AuthorizationException e) {
			if (!e.getMessage().equals("Foo7"))
				fail(e.toString());
		}
		m.assertIsSatisfied();
	}

}
