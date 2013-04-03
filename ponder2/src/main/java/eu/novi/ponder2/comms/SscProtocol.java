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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2RemoteException;

import com.twicom.comms.singlesocket.client.Client;

import eu.novi.ponder2.comms.ssc.SscReceiver;
import eu.novi.ponder2.comms.ssc.SscTransmitter;

*//**
 * Single Socket Protocol implementation for inter-SMC chit-chat
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class SscProtocol implements Protocol {

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Protocol#install(java.net.URI,
   * uk.ac.ic.doc.dse.ponder2.Result)
   
  public void install(URI address, URI remote) throws Ponder2RemoteException {
    try {
      String host;
      int port;
      String name;
      String protocol;

      if (address != null) {
        protocol = address.getScheme().toLowerCase();
        host = address.getHost();
        port = address.getPort();
        name = address.getPath();
      }
      else {
        if (remote == null)
          throw new Ponder2RemoteException("Invalid empty address");

        protocol = remote.getScheme().toLowerCase();
        // We need the host:port because that is the address of the server
        host = remote.getHost();
        port = remote.getPort();
        // We need to make up a unique name, don't use the remote's
        name = null;
      }

      // get a unique string ready
      String unique = SelfManagedCell.RootDomain.getOID().getUid();
      if (unique.charAt(0)=='-')
        unique = unique.substring(1);
      
      // Verify name
      if (name == null || name.length() == 0) {
        // We need to make up a unique name
        name = unique;
      }

      // Check we don't have a / at the beginning
      if (name.charAt(0) == '/')
        name = name.substring(1);

      // Check we still have a name
      if (name.length() == 0) {
        // We need to make up a unique name
        name = unique;
      }
      
      // See if we need to add a unique identifier
      if (name.endsWith("-")) {
        name = name + unique;
      }

      Client client = new Client(name, host, port);

      new SscReceiver(client);

      // Now register the protocol and let the external managed object use it
      try {
        address = new URI(protocol, null, host, port, "/" + name, null, null);
        ExternalManagedObject.registerProtocol(protocol, new SscTransmitter(client), address);
      }
      catch (URISyntaxException e) {
        throw new Ponder2RemoteException("SscProtocol: failed to create new URI: " + e.getMessage());
      }

    }
    catch (IOException e) {
      throw new Ponder2RemoteException(e.getMessage());
    }
  }
}
*/