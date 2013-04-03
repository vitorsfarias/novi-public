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
 * Created on Feb 1, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;

/**
 * Interface defining the methods that anything able to receive a Ponder2
 * message must implement. This allows the object to recognise the command and
 * execute the command.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface Ponder2Message {

	// public P2Object getP2Object();
	//
	// public P2ManagedObject getDomainObject();
	//
	// public OID getOID();

	public P2Object create(P2Object source, String operation, P2Object... args)
			throws Ponder2Exception;

	public P2Object operation(P2Object source, String operation,
			P2Object... args) throws Ponder2Exception;

	public P2Object operation(P2Object source, String operation, String arg1,
			String... args) throws Ponder2Exception;

}