/**
 * Created on Jul 10, 2005
 * Copyright 2005 Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * $Log: AuthorisationPolicy.java,v $
 * Revision 1.5  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.4  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.3  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2.policy;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.Policy;

/**
 * This is an Authorisation Policy. See
 * http://ponder2.net/cgi-bin/moin.cgi/BasicScenario for more information on
 * using authorisation policies.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class AuthorisationPolicy extends Policy implements ManagedObject {

	final static boolean DEBUG = false;
	/*
	 * ****Fields necessary for the new implementation of auth****
	 */
	private P2Block requestCondition = null, replyCondition = null;
	private P2ManagedObject subject = null; /* the subject OID */
	private P2ManagedObject target = null; /* the target OID */
	private String action = null;/* the action to which this policy apples */
	/**
	 * This field defines whether this policy *focuses* on a subject ('s') or a
	 * target ('t') as an authorisation policy
	 */
	private char focus1 = ' ', focus2 = ' ';

	/* true if the authorisation request is negative, false otherwise */
	private boolean isAuthRequestNeg = false;
	/* true if the authorisation reply is negative, false otherwise */
	private boolean isAuthReplyNeg = false;

	private boolean isFinal = false;/* true is this is a final policy */

	/*
	 * ***********************************************************
	 */

	/**
	 * Creates a new authorisation policy between subject and target managed
	 * objects. The authorisation policy applies to the given action and the
	 * given focus. The action is essentially the PonderTalk keywords to be
	 * checked.<br>
	 * e.g. a domain could be monitored with the action "at:put"" Note that the
	 * ':' is necessary for keyword actions.<br>
	 * The focus is 's' for subject authorisation at PEP1 or PEP4, 't' for
	 * target authorisation at PEP2 or PEP3.
	 * 
	 * @param subject
	 *            the subject domain or managed object
	 * @param action
	 *            the PonderTalk action to be regulated
	 * @param target
	 *            the target domain or managed object
	 * @param focus
	 *            the focus 't' or 's' for target or subject authorisation
	 */
	@Ponder2op("subject:action:target:focus:")
	public AuthorisationPolicy(P2Object subject, String action,
			P2Object target, String focus) {
		this.subject = subject.getManagedObject();
		// applying this authorisation policy to the subject
		this.subject.applyPolicy(this);

		this.target = target.getManagedObject();
		this.action = action;

		this.focus1 = focus.charAt(0);
		if (focus.length() > 1)
			this.focus2 = focus.charAt(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.policy.Policy#attach(eu.novi.ponder2.objects.P2Object)
	 */
	@Override
	protected void attach(P2Object aManagedObject) {
		aManagedObject.getManagedObject().applyPolicy(this);
		setAttached(true);
	}

	/**
	 * Sets the policy as a final one. Checking stops at this point and this
	 * policy determines whether access will be granted
	 */
	@Ponder2op("final")
	protected void operation_set_final() {

		isFinal = true;
	}

	/**
	 * sets the policy as a negative authorisation for the request
	 */
	@Ponder2op("reqneg")
	protected void operation_set_inneg() {

		isAuthRequestNeg = true;
	}

	/**
	 * sets the policy as a negative authorisation for the reply
	 */
	@Ponder2op("repneg")
	protected void operation_set_outneg() {

		isAuthReplyNeg = true;
	}

	/**
	 * sets the condition of the policy for the request part of an action. The
	 * arguments to the block are the values given to the action being
	 * performed. e.g. with action:
	 * 
	 * <pre>
	 * &quot;at:put:&quot;
	 * </pre>
	 * 
	 * the condition block might be<br>
	 * 
	 * <pre>
	 * [ :at :put | at == &quot;accounts&quot; ]
	 * </pre>
	 * 
	 * the argument names do not matter, you could also have<br>
	 * 
	 * <pre>
	 * [ :name | name == &quot;accounts&quot; ]
	 * </pre>
	 * 
	 * You can also use the global variables
	 * 
	 * <i> p_source </i>
	 * 
	 * and
	 * 
	 * <i> p_target </i>
	 * 
	 * in the block's PonderTalk, they refer to the source and target objects
	 * associated with the action in question. <br>
	 * Answers aBlock.
	 * 
	 * @param aBlock
	 *            the condition for this policy
	 * @return the block given as an argument
	 */
	@Ponder2op("reqcondition:")
	protected P2Object operation_in_condition(P2Block aBlock) {
		requestCondition = aBlock;

		return aBlock;
	}

	/**
	 * sets the condition of the policy for the reply part of an action. The
	 * argument to the block is the value of the return from the action being
	 * performed. e.g. with action:
	 * 
	 * <pre>
	 * &quot;at:&quot;
	 * </pre>
	 * 
	 * the reply condition block might be<br>
	 * 
	 * <pre>
	 * [ :name | name == &quot;accounts&quot; ]
	 * </pre>
	 * 
	 * You can also use the global variables
	 * 
	 * <i> p_source </i>
	 * 
	 * and
	 * 
	 * <i> p_target </i>
	 * 
	 * in the block's PonderTalk, they refer to the source and target objects
	 * associated with the action in question. <br>
	 * Answers aBlock.
	 * 
	 * @param aBlock
	 *            the condition for this policy
	 * @return the block given as an argument
	 */
	@Ponder2op("repcondition:")
	protected P2Object operation_out_condition(P2Block aBlock) {
		replyCondition = aBlock;

		return aBlock;
	}

	P2ManagedObject getSubject() {
		return subject;
	}

	P2ManagedObject getTarget() {
		return target;
	}

	/**
	 * To check whether this policy is a negative policy for PEP1 and PEP2
	 * 
	 * @return the value of isAuthRequestNeg.
	 */
	boolean isAuthRequestNeg() {
		return isAuthRequestNeg;
	}

	/**
	 * To check whether this policy is a negative policy for PEP3 and PEP4
	 * 
	 * @return the value of isAuthReplyNeg.
	 * 
	 */
	boolean isAuthReplyNeg() {
		return isAuthReplyNeg;
	}

	/**
	 * To retrieve the action for which this policy is defined
	 * 
	 * @return the string representing the target method
	 */
	String getAction() {
		return action;
	}

	/**
	 * To check whether this is a final policy
	 * 
	 * @return true is the policy is final. False otherwise
	 */
	boolean isFinal() {
		return isFinal;
	}

	/**
	 * Set the target OID for this policy
	 * 
	 * @param target
	 *            the target OID
	 */
	void setTarget(P2Object target) {
		this.target = target.getManagedObject();
	}

	/**
	 * To retrieve the focus of this policy
	 * 
	 * @return the char 's' if the policy is a subject policy. Otherwise 't' for
	 *         a target policy
	 */
	boolean hasFocus(char focus) {
		return focus1 == focus || focus2 == focus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */

	@Override
	public String toString() {
		String spolicy = "";
		spolicy += "{" + Integer.toHexString(hashCode()) + ",";
		spolicy += subject.getOID() + ",";
		spolicy += target.getOID() + ",";
		if (isFinal)
			spolicy += "Final,";
		else
			spolicy += "Normal,";
		spolicy += "" + focus1 + focus2 + ",";
		if (isActive())
			spolicy += "active,";
		else
			spolicy += "NOTactive,";

		if (isAuthRequestNeg)
			spolicy += "Request-,";
		else
			spolicy += "Request+,";
		spolicy += action + ",";
		if (isAuthReplyNeg)
			spolicy += "Result-}";
		else
			spolicy += "Result+}";
		return spolicy;
	}

	public boolean checkRequestCondition(P2Object subject, P2Object target,
			P2Object... argAttribute) {

		try {

			return checkCondition(subject, target, "in", argAttribute);
		} catch (Ponder2Exception e) {
			System.err
					.println("AuthorisationPolicy.checkRequestCondition(): a Ponder2Exception occurred. Printing the Stack Trace...");
			e.printStackTrace();
		}
		return false;
	}

	public boolean checkReturnCondition(P2Object subject, P2Object target,
			P2Object... argAttribute) {
		try {
			return checkCondition(subject, target, "out", argAttribute);
		} catch (Ponder2Exception e) {
			System.err
					.println("AuthorisationPolicy.checkReturnCondition(): a Ponder2Exception occurred. Printing the Stack Trace...");
			e.printStackTrace();
		}

		return false;
	}

	protected boolean checkCondition(P2Object subject, P2Object target,
			String conditionType, P2Object... argAttribute)
			throws Ponder2Exception {
		if ("in".equals(conditionType)) {
			if (requestCondition != null) {
				if (DEBUG)
					System.out
							.println("AuthorisationPolicy.checkCondition() type "
									+ conditionType
									+ ": the in condition is NOT NULL."
									+ " Going to evalute with the given argument(s).");
				P2Hash localVars = setLocalVars(subject, target);
				return requestCondition.operation_array(subject, localVars,
						new P2Array(argAttribute)).asBoolean();
			}
		} else if ("out".equals(conditionType)) {
			if (replyCondition != null) {
				if (DEBUG)
					System.out
							.println("AuthorisationPolicy.checkCondition() type "
									+ conditionType
									+ ": the in condition is NOT NULL."
									+ " Going to evalute with the given argument(s).");
				P2Hash localVars = setLocalVars(subject, target);
				return replyCondition.operation_array(subject, localVars,
						new P2Array(argAttribute)).asBoolean();
			}

		}
		return true;
	}

	private P2Hash setLocalVars(P2Object subject, P2Object target) {
		P2Hash localVars = new P2Hash();
		localVars.operation_at_put("p_subject", subject);
		localVars.operation_at_put("p_target", target);
		return localVars;
	}
}
