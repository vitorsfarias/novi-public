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
 * Created on Mar 24, 2006
 *
 * $Log:$
 */

package eu.novi.ponder2.comms;

import java.net.URI;

import eu.novi.ponder2.exception.Ponder2RemoteException;

/**
 * defines the methods that all protocols must implement to be able to
 * automatically install themselves into an SMC
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface Protocol {

	/**
	 * Installs a new communications address as an address that this SMC can be
	 * referred to
	 * 
	 * @param address
	 *            the local address as a URI comprising
	 *            <code>protocol://host/protocol-dependent<code>
	 * @param remote
	 *            the remote address that initiated this call to install
	 * @throws Ponder2RemoteException
	 */
	public void install(URI address, URI remote) throws Ponder2RemoteException;
}
