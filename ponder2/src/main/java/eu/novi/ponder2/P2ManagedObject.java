/**
 * Created on Jul 4, 2005
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
 * $Log: P2ManagedObject.java,v $
 * Revision 1.22  2006/03/15 14:52:18  kpt
 * Improved handling of visited interaction also now thread safe
 *
 * Revision 1.21  2006/03/15 14:37:38  kpt
 * Fixed multiple and looping event paths
 *
 * Revision 1.20  2006/03/15 13:36:42  kpt
 * Lots of testing stuff and new XMLSaver managed object
 *
 * Revision 1.19  2006/02/11 16:28:35  kpt
 * Modified Event creation
 *
 * Revision 1.18  2005/11/23 14:47:07  kpt
 * Removed Iterable from QDParser.
 *
 * Revision 1.17  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.16  2005/11/15 15:00:51  kpt
 * ExecuteSetup introduced, more result feedback
 *
 * Revision 1.15  2005/11/13 23:27:30  kpt
 * Added time manager
 *
 * Revision 1.14  2005/11/03 04:23:46  kpt
 * More restore and tidying done
 *
 * Revision 1.13  2005/10/29 20:22:56  kpt
 * Tidied up managed object command execution a little
 *
 * Revision 1.12  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.11  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.10  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.9  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.8  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.7  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.6  2005/10/09 22:03:04  kpt
 * First complete version of demo with a few fixes.
 Local evaluation added on demand before shipping xml for export.
 *
 * Revision 1.5  2005/10/06 10:59:17  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.4  2005/10/01 20:50:52  kpt
 * Changed result type to new Result class
 *
 * Revision 1.3  2005/09/21 13:46:40  kpt
 * Can now create and send events using XML and the event templates
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.HashSet;
import java.util.Set;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.EventListener;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.Ponder2Message;
import eu.novi.ponder2.SelfManagedCell;

/**
 * The base class for all Managed Objects within the system. The parent and
 * policy sets are maintained here. All RPC calls come through this interface,
 * the RPC is checked against the policies and then propagated to the actual
 * object to perform the action.
 * 
 * @author Kevin Twidle
 * @version $Id: P2ManagedObject.java,v 1.22 2006/03/15 14:52:18 kpt Exp $
 */
public class P2ManagedObject implements Ponder2Message, Externalizable {

	private Set<P2ManagedObject> parentSet;
	private Set<EventListener> eventListenerSet = null;
	private Set<AuthorisationPolicy> authorisationPolicySet = null;
	private Set<P2Object> remoteEventSet = null;
	private OID oid;
	private P2Object p2Object;

	/**
	 * The base class for all Managed Objects within the system. The parent and
	 * policy sets are maintained here. All RPC calls come through this
	 * interface, the RPC is checked against the policies and then propagated to
	 * the actual object to perform the action.
	 */
	public P2ManagedObject(P2Object p2Object) {
		parentSet = new HashSet<P2ManagedObject>();
		oid = new OID(this);
		this.p2Object = p2Object;
	}

	/**
	 * Constructor for serialisation
	 */
	public P2ManagedObject() {
	}

	/**
	 * gets the object identifier for this object
	 * 
	 * @return this object's identifier
	 */
	public OID getOID() {
		return oid;
	}

	public boolean isDomain() {
		return getOID().isDomain();
	}

	/**
	 * gets the object identifier for this object
	 * 
	 * @return this object's identifier
	 */
	public P2Object getP2Object() {
		return p2Object;
	}

	/**
	 * adds a parent managed object as a parent of this one
	 * 
	 * @param mo
	 *            the parent to be added
	 */
	protected void addParent(P2ManagedObject mo) {
		parentSet.add(mo);
	}

	/**
	 * removes a parent object from this object's parent set
	 * 
	 * @param mo
	 *            the parent to be removed
	 */
	protected void removeParent(P2ManagedObject mo) {
		parentSet.remove(mo);
	}

	public Set<P2ManagedObject> getParentSet() {
		return parentSet;
	}

	/**
	 * returns the number of parents that this managed object has
	 * 
	 * @return the number of parents
	 */
	public int parentCount() {
		return parentSet.size();
	}

	/**
	 * get all the obligation policies applying to this managed object
	 * 
	 * @return a Set containing the policies
	 */
	protected Set<EventListener> getEventListeners() {
		if (eventListenerSet == null)
			eventListenerSet = new HashSet<EventListener>();
		return eventListenerSet;
	}

	/**
	 * get all the authorisation policies applying to this managed object
	 * 
	 * @return a Set containing the policies
	 */
	public Set<AuthorisationPolicy> getAuthorisationPolicies() {
		if (authorisationPolicySet == null)
			authorisationPolicySet = new HashSet<AuthorisationPolicy>();
		return authorisationPolicySet;
	}

	/**
	 * @return the remoteEventSet
	 */
	private Set<P2Object> getRemoteEventSet() {
		if (remoteEventSet == null)
			remoteEventSet = new HashSet<P2Object>();
		return remoteEventSet;
	}

	/**
	 * applies a policy to this managed object
	 * 
	 * @param policy
	 *            the policy to be applied
	 */
	public void applyPolicy(EventListener policy) {
		getEventListeners().add(policy);
	}

	/**
	 * applies a policy to this managed object
	 * 
	 * @param policy
	 *            the policy to be applied
	 */
	public void applyPolicy(AuthorisationPolicy policy) {
		getAuthorisationPolicies().add(policy);
	}

	/**
	 * removes a policy from this managed object's policy set
	 * 
	 * @param policy
	 *            the policy to be removed
	 */
	public void removePolicy(EventListener policy) {
		getEventListeners().remove(policy);
	}

	/**
	 * removes a policy from this managed object's policy set
	 * 
	 * @param policy
	 *            the policy to be removed
	 */
	public void removePolicy(AuthorisationPolicy policy) {
		getAuthorisationPolicies().remove(policy);
	}

	public void attachRemotePolicy(P2Object remote) {
		getRemoteEventSet().add(remote);
	}

	/**
	 * sends an event to policies attached to this managed object and above
	 * 
	 * @param event
	 *            the event to be sent to the policies
	 * @throws Ponder2Exception
	 */
	public void sendEvent(Event event) {
		// Have we already seen this event?
		if (SelfManagedCell.SystemTrace) {
			System.err.println();
			System.err.print("Event " + event + " " + this + " ");
		}
		if (event.setVisited(this)) {
			if (eventListenerSet != null) {
				Set<EventListener> eventListeners = new HashSet<EventListener>(
						getEventListeners());
				for (EventListener listener : eventListeners) {
					if (SelfManagedCell.SystemTrace)
						System.err.print("listener " + listener + " ");
					if (event.setVisited(listener.getId()))
						try {
							listener.event(event);
						} catch (Ponder2Exception e) {
							System.out.println("Event caused unhandled error: "
									+ e.getMessage());
						}
				}
			}
			if (remoteEventSet != null) {
				Set<P2Object> remoteEvents = new HashSet<P2Object>(
						getRemoteEventSet());
				for (P2Object remote : remoteEvents) {
					try {
						remote.operation(event.getSource(), "Ponder2.event:",
								event);
					} catch (Ponder2Exception e) {
						System.out
								.println("Event (remote) caused unhandled error: "
										+ e.getMessage());
					}
				}
			}
			Set<P2ManagedObject> parents = new HashSet<P2ManagedObject>(
					parentSet);
			for (P2ManagedObject mo : parents) {
				mo.sendEvent(event);
			}
		}
		if (SelfManagedCell.SystemTrace)
			System.err.println("Event end");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.Ponder2Message#create(eu.novi.ponder2.objects.P2OID,
	 * java.lang.String, eu.novi.ponder2.objects.P2Object[])
	 */
	public P2Object create(P2Object source, String operation, P2Object... args)
			throws Ponder2Exception {
		return p2Object.create(source, operation, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.Ponder2Message#operation(eu.novi.ponder2.objects.P2OID,
	 * java.lang.String, eu.novi.ponder2.objects.P2Object[])
	 */
	public P2Object operation(P2Object source, String operation,
			P2Object... args) throws Ponder2Exception {
		return p2Object.operation(source, operation, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.Ponder2Message#operation(eu.novi.ponder2.objects.P2Object
	 * , java.lang.String, java.lang.String, java.lang.String[])
	 */
	public P2Object operation(P2Object source, String operation, String arg1,
			String... args) throws Ponder2Exception {
		return p2Object.operation(source, operation, arg1, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException {
		// Nothing to read
	}

	/**
	 * Called by the serialization routines after readExternal as been called.
	 * 
	 * @return the newly read in OID, actually probably an old one.
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException {
		// Just return null, this object will be created elsewhere if necessary
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput arg0) throws IOException {
		// Nothing to write, we don't actually send this object
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof P2ManagedObject))
			return false;
		return getP2Object().equals(((P2ManagedObject) obj).getP2Object());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getP2Object().hashCode();
	}

}
