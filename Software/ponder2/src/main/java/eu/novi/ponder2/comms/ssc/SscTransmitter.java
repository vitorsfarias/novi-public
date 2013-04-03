/**
 * Copyright 2008 Kevin Twidle, Imperial College, London, England.
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
 * Created on Feb 13, 2010
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms.ssc;

import java.io.IOException;
import java.net.URI;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.comms.singlesocket.client.Client;

import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.comms.TransmitterImpl;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class SscTransmitter extends TransmitterImpl implements Transmitter {

  private Client client;

  public SscTransmitter(Client client) {
    this.client = client;
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#execute(java.net.URI,
   * net.ponder2.OID, net.ponder2.objects.P2Object, java.lang.String,
   * net.ponder2.objects.P2Object[])
   
  @Override
  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args)
      throws Ponder2Exception {
    return executeString(address, target, source, op, args);
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#getObject(java.net.URI,
   * java.lang.String)
   
  @Override
  public P2Object getObject(URI address, String path) throws Ponder2Exception {
    return getObjectString(address, path);
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#execute(java.net.URI,
   * java.lang.String)
   
  @Override
  protected String execute(URI address, String xmlString) throws Ponder2Exception {
    try {
      String destination = address.getPath();
      destination = destination.substring(1);
      return client.request(destination, xmlString);
    }
    catch (IOException e) {
      throw new Ponder2RemoteException(e.getMessage());
    }
  }

}
*/