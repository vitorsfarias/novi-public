/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
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
 * Created on Feb 22, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.EventListener;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ManagedObject;

/**
 * Forwards event from its attachment point to another part of the proximity
 * event bus in this or another SMC. May also be used to forward events as
 * ordinary messages to Managed Objects, useful for communicating with external
 * event busses.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class EventForwarder implements ManagedObject, EventListener {

	private P2Object myP2Object;
	private Map<P2Object, String> forwardToSet = new HashMap<P2Object, String>();
	private Set<P2Object> forwardEvents = null;
	private boolean active = false;

	public final static String eventCommand = "Ponder2.event:";

	/**
	 * creates an EventForwarder
	 */
	@Ponder2op("create")
	EventForwarder(P2Object myP2Object) {
		this.myP2Object = myP2Object;
	}

	/**
	 * attaches the EventForwarder to anObject. Any events generated at or below
	 * the managed object will be picked up by this event forwarder.
	 * 
	 * @param anObject
	 *            the object to attach this forwarder to
	 * @throws Ponder2Exception
	 */
	@Ponder2op("attachTo:")
	void attachTo(P2Object source, P2Object anObject) throws Ponder2Exception {
		anObject.operation(source, "Ponder2.attach:", myP2Object);
	}

	/**
	 * adds anObject to the list of managed objects to forward events to
	 * 
	 * @param anObject
	 *            the managed object to receive forwarded events
	 */
	@Ponder2op("forwardTo:")
	void forwardTo(P2Object anObject) {
		forwardToSet.put(anObject, eventCommand);
	}

	/**
	 * adds anObject to the list of managed objects to forward events to. The
	 * events are forwarded to anObject using the message aMessage which is
	 * expected to take one parameter, the event. Hence aCommand should be a
	 * keyword parameter. e.g. "event:"
	 * 
	 * @param anObject
	 *            the managed object to receive forwarded events
	 * @param aMessage
	 *            the message keyword to be used to pass the event on
	 */
	@Ponder2op("forwardTo:as:")
	void forwardTo(P2Object anObject, String aMessage) {
		forwardToSet.put(anObject, aMessage);
	}

	/**
	 * removes anObject from the forward-to set
	 * 
	 * @param anObject
	 *            the managed object to be removed
	 */
	@Ponder2op("remove:")
	void remove(P2Object anObject) {
		forwardToSet.remove(anObject);
	}

	/**
	 * Specifies that the forwarder is to forward events of anEventType
	 * 
	 * @param eventType
	 */
	@Ponder2op("forward:")
	void forwardEvent(P2Object eventType) {
		if (forwardEvents == null)
			forwardEvents = new HashSet<P2Object>();
		forwardEvents.add(eventType);
	}

	/**
	 * sets the forwarder to be active if aBoolean is true else inactive
	 * 
	 * @param aBoolean
	 *            true if the forwarder is to be activated
	 */
	@Ponder2op("active:")
	void setActive(boolean aBoolean) {
		this.active = aBoolean;
	}

	/**
	 * answers the active state of the forwarder. true or false
	 * 
	 * @return the active state
	 */
	@Ponder2op("active")
	boolean getActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.policy.EventListener#event(eu.novi.ponder2.Event)
	 */
	public void event(Event event) throws Ponder2Exception {
		if (getActive())
			if (forwardEvents == null
					|| forwardEvents.contains(event.getEventTemplate()))
				for (Map.Entry<P2Object, String> entry : forwardToSet
						.entrySet())
					entry.getKey().operation(myP2Object, entry.getValue(),
							event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.policy.EventListener#getId()
	 */
	public P2ManagedObject getId() {
		return myP2Object.getManagedObject();
	}

}
