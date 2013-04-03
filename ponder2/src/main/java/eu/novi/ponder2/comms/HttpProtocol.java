/**
 * Copyright 2006 Imperial College, London, England.
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
 * Created on Mar 20, 2006
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms;

import java.net.URI;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.comms.http.HTTPTransmitter;
import eu.novi.ponder2.exception.Ponder2RemoteException;

*//**
 * Web Service protocol implementation for inter-SMC chit chat
 *
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class HttpProtocol implements Protocol {

    (non-Javadoc)
   * @see net.ponder2.comms.Protocol#install(java.net.URI)
   
  public void install(URI address, URI remote) throws Ponder2RemoteException {
    // If we don't have a local address we have no idea what we should be, just
    // let it go through blank. It simply means that the other end can't
    // initiate messages to send to us
    ExternalManagedObject.registerProtocol("http", new HTTPTransmitter(), address);
  }
}
*/