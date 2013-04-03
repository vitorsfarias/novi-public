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
 * Created on Feb 24, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2.policy;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.policy.Event;

/**
 * Listens to events produced by managed objects
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface EventListener {

	/**
	 * called to deal with an event when one occurs
	 * 
	 * @param event
	 *            the event to be handled
	 */
	void event(Event event) throws Ponder2Exception;

	/**
	 * Called to get an ID to keep track of events
	 * 
	 * @return the managed object of the event listener
	 */
	P2ManagedObject getId();

}
