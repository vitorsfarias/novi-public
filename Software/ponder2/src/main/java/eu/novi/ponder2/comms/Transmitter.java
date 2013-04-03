/**
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
 * Created on Mar 23, 2006
 *
 * $Log:$
 */

package eu.novi.ponder2.comms;

import java.net.URI;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.comms.Transmitter;

/**
 * Describes the methods that all Ponder2 comms protocols for inter-SMC
 * communication must supply
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface Transmitter {

	/**
	 * creates and connects a Transmitter to a remote location. This is called
	 * once every time a new remote address is brought into play. If the
	 * location is important to the Transmitter i.e. a permanent channel is
	 * opened then a new instance of transmitter should be created. If the
	 * remote location does not matter then only one instance of the Transmitter
	 * need be created and this method can return itself.
	 * 
	 * @param address
	 *            the location that this protocol is to be connected to
	 * @return a new communications protocol connected to the appropriate place
	 *         or throw an error if it fails
	 * @throws Ponder2RemoteException
	 */
	public Transmitter connect(URI address) throws Ponder2RemoteException;

	/**
	 * Checks to see if the remote service is up and running. Returns true or
	 * false.
	 * 
	 * @param address
	 *            the address of the remote service
	 * @return true if the service is running
	 */
	public boolean ping(URI address);

	/**
	 * gets a managed object from a remote SMC
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param path
	 *            the full path name of the remote managed object
	 * @return the result of the operation.
	 * @throws Ponder2Exception
	 *             if an exception occurs in the remote system
	 */
	public P2Object getObject(URI address, String path) throws Ponder2Exception;

	/**
	 * executes commands at a remote managed object. The command will either be
	 * a create or use clause.
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param target
	 *            the remote object's OID
	 * @param source
	 *            the originator of the operation
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return the result of the operation as a Ponder2 object
	 * @throws Ponder2Exception
	 *             if an exception occurs in the remote system
	 */
	public P2Object execute(URI address, OID target, P2Object source,
			String op, P2Object[] args) throws Ponder2Exception;

}
