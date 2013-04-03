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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.comms.Receiver;

/**
 * The receiving class that is registered with the rmiregistry. Any messages for
 * this RMI name come through this receiver in its own thread.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class RmiXmlReceiver extends UnicastRemoteObject implements RmiXmlReceiveInterface {

  /**
   * creates an RMI receiver. This simply registers the RMI name and the new
   * receiver with the rmi registry
   * 
   * @param RMIName
   * @throws RemoteException
   * @throws MalformedURLException
   */
  public RmiXmlReceiver(String RMIName) throws RemoteException, MalformedURLException {
    register(RMIName);
  }

  /**
   * registers the RMI name and this instance with the RMI registry
   * 
   * @param RMIName
   *            the RMI name as a string
   * @throws RemoteException
   *             if the RMI registry is not running or can't be contacted
   * @throws MalformedURLException
   *             if the RMI name does not conform to the specification
   */
  public void register(String RMIName) throws RemoteException, MalformedURLException {
    Naming.rebind(RMIName, this);
    System.out.println("Local RMI XML address is " + RMIName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.rmi.RMIReceiveInterface#getObject(java.net.URI,
   *      java.lang.String, net.ponder2.Result)
   */
  public TaggedElement getObject(URI location, String path) throws RemoteException,
      Ponder2Exception {
    return Receiver.getObject(location, path).writeXml(new HashSet<P2Object>());
  }

  /* (non-Javadoc)
   * @see net.ponder2.comms.rmixml.RmiXmlReceiveInterface#execute(com.twicom.qdparser.TaggedElement)
   */
  public TaggedElement execute(TaggedElement xml) throws RemoteException, Ponder2Exception {
    return Receiver.execute(xml);
  }

 }
