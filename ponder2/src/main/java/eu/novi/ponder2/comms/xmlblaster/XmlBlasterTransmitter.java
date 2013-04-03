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
 * Created on Feb 6, 2010
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms.xmlblaster;

import java.net.URI;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

import org.xmlBlaster.client.I_XmlBlasterAccess;
import org.xmlBlaster.util.Global;
import org.xmlBlaster.util.SessionName;
import org.xmlBlaster.util.XmlBlasterException;
import org.xmlBlaster.util.qos.address.Destination;

import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.comms.TransmitterImpl;
import eu.novi.ponder2.comms.XmlblasterProtocol;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class XmlBlasterTransmitter extends TransmitterImpl implements Transmitter {

  private I_XmlBlasterAccess transmitter;
  private Global glob;
  private XmlBlasterSync sync;

  public XmlBlasterTransmitter(I_XmlBlasterAccess transmitter, XmlBlasterSync sync) {
    this.glob = transmitter.getGlobal();
    this.transmitter = transmitter;
    this.sync = sync;
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#connect(java.net.URI)
   
  @Override
  public Transmitter connect(URI address) throws Ponder2RemoteException {
    return this;
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI,
   * java.lang.String)
   
  @Override
  public P2Object getObject(URI address, String path) throws Ponder2Exception {
    return getObjectString(address, path);
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#execute(java.net.URI, net.ponder2.OID,
   * net.ponder2.objects.P2Object, java.lang.String,
   * net.ponder2.objects.P2Object[])
   
  @Override
  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args)
      throws Ponder2Exception {
    return executeString(address, target, source, op, args);
  }

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#execute(java.net.URI,
   * java.lang.String)
   
  @Override
  protected String execute(URI address, String xmlString) throws Ponder2Exception {
    try {
      // Get name and knock off leading '/'
      String remote = address.getPath();
      remote = remote.substring(1);
      remote = "Ponder2_" + remote;

      Destination destination = new Destination(new SessionName(glob, remote));
      String ponder2id = sync.create();
      XmlblasterProtocol.publish(transmitter, destination, xmlString, "request", ponder2id);
      return sync.awaitReply(ponder2id);
    }
    catch (XmlBlasterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new Ponder2OperationException("XmlBlasterTransmitter: " + e);
    }
  }
}
*/