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
 * $Log: ObligationPolicy.java,v $
 * Revision 1.18  2006/02/11 16:28:36  kpt
 * Modified Event creation
 *
 * Revision 1.17  2005/12/09 11:18:13  kpt
 * Tidied imports
 *
 * Revision 1.16  2005/12/09 10:34:38  kpt
 * Policy actions sorted out
 *
 * Revision 1.15  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.14  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.13  2005/11/13 23:27:30  kpt
 * Added time manager
 *
 * Revision 1.12  2005/11/12 16:59:55  kpt
 * Added conditionals to Policies
 *
 * Revision 1.11  2005/11/07 22:08:50  kpt
 * Added action to policies
 *
 * Revision 1.10  2005/11/07 12:05:42  kpt
 * Added Result to the parsing
 *
 * Revision 1.9  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.8  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.7  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.6  2005/09/22 08:38:01  kpt
 * Fixes for demo
 *
 * Revision 1.5  2005/09/21 10:13:16  kpt
 * Implemented <local> and context for policies
 *
 * Revision 1.4  2005/09/14 21:32:06  kpt
 * Removed warnings
 *
 * Revision 1.3  2005/09/14 21:01:27  kpt
 * BedStation example added
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2.policy;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Object;
//import eu.novi.ponder2.policy.ThreadAction;

import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.EventListener;
import eu.novi.ponder2.policy.Policy;

/**
 * an obligation policy that performs Event, Condition, Action rules for the SMC
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class ObligationPolicy extends Policy implements ManagedObject,
		EventListener {

	private P2Object eventTemplate = null;
	private List<P2Block> conditions = new Vector<P2Block>();
	private List<P2Block> actions = new Vector<P2Block>();
	private List<P2Block> eactions = new Vector<P2Block>();
	private P2Object myP2Object;
	private P2Block errorBlock = null;

	/**
	 * creates a new obligation policy. The policy is empty until filled by
	 * other messages. The policy is initially not active.
	 * 
	 */
	@Ponder2op("create")
	public ObligationPolicy(P2Object myP2Object) {
		this.myP2Object = myP2Object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.policy.EventListener#event(eu.novi.ponder2.policy.Event)
	 */
	public void event(Event event) throws Ponder2Exception {
		if (operation_canExecute(event) == P2Boolean.True)
			operation_execute(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.policy.EventListener#getId()
	 */
	public P2ManagedObject getId() {
		return myP2Object.getManagedObject();
	}

	/**
	 * answers true if anEvent has the values that satisfy all the policy's
	 * conditions
	 * 
	 * @param anEvent
	 *            the event of the correct type
	 * @return true if the condition returns true
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2Exception
	 */
	@Ponder2op("canExecute:")
	protected P2Boolean operation_canExecute(P2Object anEvent)
			throws Ponder2ArgumentException {
		if (!isActive())
			return P2Boolean.False;
		if (!(anEvent instanceof Event))
			throw new Ponder2ArgumentException(
					"Obligation Policy: execute not given Event");
		Event event = (Event) anEvent;
		// System.err.println("checking event "+event.getEventTemplate().asOID()
		// +"
		// vs policy template "+ eventTemplate);
		if (eventTemplate != event.getEventTemplate())
			return P2Boolean.False;

		for (Iterator<P2Block> iter = conditions.iterator(); iter.hasNext();) {
			P2Block condition = iter.next();
			boolean result;
			try {
				result = condition.operation_valueHash(myP2Object, event)
						.asBoolean();
			} catch (Ponder2Exception e) {
				if (errorBlock == null)
					System.out
							.println("ObligationPolicy: unhandled condition error: "
									+ e.getMessage());
				else
					try {
						errorBlock.operation(myP2Object, "value:",
								P2Object.create(e));
					} catch (Ponder2Exception e1) {
						System.out
								.println("ObligationPolicy: unhandled error when executing condition error block: "
										+ e1.getMessage());
					}
				result = false;
			}
			if (!result)
				return P2Boolean.False;
		}
		return P2Boolean.True;
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

	boolean threads = false;

	/**
	 * Tells the policy to perform all actions as separate threads in parallel
	 * 
	 */
	@Ponder2op("threads")
	void threads() {
		this.threads = true;
	}

	/**
	 * executes all the actions using anEvent. AnEvent is given to every action
	 * defined for the policy. Actions may be executed in parallel. Statements
	 * within an action block are executed serially. Answers the receiver. This
	 * policy is the source for the action blocks
	 * 
	 * @param anEvent
	 *            the event (of the expected event type) to give arguments for
	 *            the actions
	 */
	@Ponder2op("execute:")
	protected void operation_execute(P2Object anEvent) {

		for (P2Block action : actions) {
			try {
				if (!threads)
					action.operation(myP2Object, "valueHash:", anEvent);
				else {
					Thread thread = new ThreadAction(myP2Object, action,
							anEvent);
					thread.start();
				}
			} catch (Ponder2Exception e) {
				if (errorBlock == null)
					System.out
							.println("ObligationPolicy: unhandled action error: "
									+ e.getClass().getSimpleName()
									+ " - "
									+ e.getMessage());
				else
					try {
						errorBlock.operation(myP2Object, "value:",
								P2Object.create(e));
					} catch (Ponder2Exception e1) {
						System.out
								.println("ObligationPolicy: unhandled error when executing action error block: "
										+ e1.getMessage());
					}

			}
		}

		for (P2Block eaction : eactions) {
			try {
				eaction.operation(myP2Object, "value:", anEvent);
			} catch (Ponder2Exception e) {
				if (errorBlock == null)
					System.out
							.println("ObligationPolicy: unhandled action error: "
									+ e.getMessage());
				else
					try {
						errorBlock.operation(myP2Object, "value:",
								P2Object.create(e));
					} catch (Ponder2Exception e1) {
						System.out
								.println("ObligationPolicy: unhandled error when executing action error block: "
										+ e1.getMessage());
					}

			}
		}
	}

	/**
	 * executes all the actions using anEvent. AnEvent is given to every action
	 * defined for the policy. Different actions could be executed in parallel.
	 * Statements within an action block are executed serially. Answers the
	 * receiver. This message makes the policy look like a block with the source
	 * being the caller.
	 * 
	 * @param anEvent
	 *            the event (of the expected event type) to give arguments for
	 *            the actions
	 * @throws Ponder2Exception
	 */
	@Ponder2op("value:")
	protected void operation_value(P2Object source, P2Object anEvent)
			throws Ponder2Exception {

		for (P2Block action : actions) {
			action.operation(source, "valueHash:", anEvent);
		}

		for (P2Block eaction : eactions) {
			eaction.operation(source, "value:", anEvent);
		}
	}

	/**
	 * Sets anEventTemplate to be the type of event that this policy should be
	 * triggered by. Answers anEventTemplate.
	 * 
	 * @param anEventTemplate
	 *            the eventTemplate to be used by this policy
	 * @return the event template given
	 */
	@Ponder2op("event:")
	protected P2Object operation_event(P2Object anEventTemplate) {
		eventTemplate = anEventTemplate;
		return anEventTemplate;
	}

	/**
	 * adds aBlock to the list of conditions of the policy. Answers aBlock.
	 * 
	 * @param aBlock
	 *            a condition block
	 * @return the block given
	 */
	@Ponder2op("condition:")
	protected P2Object operation_condition(P2Block aBlock) {
		conditions.add(aBlock);
		return aBlock;
	}

	/**
	 * adds aBlock to the list of actions of the policy. Answers aBlock.
	 * 
	 * @param aBlock
	 *            an action block
	 * @return the block given
	 */
	@Ponder2op("action:")
	protected P2Object operation_action(P2Block aBlock) {
		actions.add(aBlock);
		return aBlock;
	}

	/**
	 * adds aBlock to the list of actions to be handed the whole event when it
	 * occurs. Answers aBlock.
	 * 
	 * @param aBlock
	 *            an action block
	 * @return the block given
	 */
	@Ponder2op("eaction:")
	protected P2Object operation_eaction(P2Block aBlock) {
		eactions.add(aBlock);
		return aBlock;
	}

	/**
	 * associates anErrorBlock with the receiver. The error block is executed if
	 * a Ponder2 error occurs in the receiver. The error block is given one
	 * P2Error argument which contains the error details.
	 * 
	 * @param anErrorBlock
	 */
	@Ponder2op("onError:")
	protected void onError(P2Block anErrorBlock) {
		this.errorBlock = anErrorBlock;
	}

	class ThreadAction extends Thread {

		private P2Object source;
		private P2Object event;
		private P2Object action;

		ThreadAction(P2Object source, P2Object action, P2Object event) {
			this.source = source;
			this.action = action;
			this.event = event;
		}

		@Override
		public void run() {
			try {
				action.operation(source, "valueHash:", event);
			} catch (Ponder2Exception e) {
				System.out.println("ObligationPolicy: unhandled action error: "
						+ e.getMessage());
			}
		}
	}

}
