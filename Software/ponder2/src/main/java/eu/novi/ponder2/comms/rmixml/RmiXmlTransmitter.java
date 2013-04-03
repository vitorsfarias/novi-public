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

package eu.novi.ponder2.comms.rmixml;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.comms.TransmitterImpl;

/**
 * This class embodies the sending mechanism for inter-SMC communications using
 * the RMI protocol. When instantiated it locks itself to one particular
 * destination. One RMITransmitter is created for each RMI destination that is
 * used.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class RmiXmlTransmitter extends TransmitterImpl implements Transmitter {

  /**
   * the receiving interface of the remote SMC
   */
  private RmiXmlReceiveInterface remote = null;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#connect(java.net.URI)
   */
  @Override
  public Transmitter connect(URI address) {
    RmiXmlTransmitter tx = new RmiXmlTransmitter();
    if (tx.lookup(address))
      return tx;
    return null;
  }

  /**
   * looks up the interface for the remote SMC
   * 
   * @param location
   *            the remote RMI name
   * @return true if the lookup succeeded
   */
  private boolean lookup(URI location) {
    try {
      Object obj = Naming.lookup("rmi:"+location.getSchemeSpecificPart());
      remote = (RmiXmlReceiveInterface)obj;
    }
    catch (MalformedURLException e) {
      System.out.println("RmiReceiveInterface: bad form of address - " + location);
    }
    catch (RemoteException e) {
      System.out.println("RmiReceiveInterface: remote exception - " + location + " - "
          + e.getMessage());
    }
    catch (NotBoundException e) {
      System.out.println("RmiReceiveInterface: remote RMI name not bound - " + location);
    }
    return (remote != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI,
   *      java.lang.String)
   */
  @Override
  public P2Object getObject(URI location, String path) throws Ponder2Exception {
    try {
      TaggedElement reply = remote.getObject(location, path);
      return P2Object.fromXml(reply, new HashMap<Integer,P2Serializable>());
    }
    catch (RemoteException e) {
      throw new Ponder2OperationException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#execute(java.net.URI, net.ponder2.OID,
   *      net.ponder2.objects.P2Object, java.lang.String,
   *      net.ponder2.objects.P2Object[])
   */
  @Override
  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args)
      throws Ponder2Exception {
    return executeXml(address, target, source, op, args);
   }
  
  @Override
  protected TaggedElement execute(URI address, TaggedElement xml) throws Ponder2Exception {
    try {
      return remote.execute(xml);
    }
    catch (RemoteException e) {
      throw new Ponder2OperationException(e.getMessage());
    }
   }

}
