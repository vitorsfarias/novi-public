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

package eu.novi.ponder2.comms.rmi;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.comms.Receiver;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

/**
 * The receiving class that is registered with the rmiregistry. Any messages for
 * this RMI name come through this receiver in its own thread.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class RMIReceiver extends UnicastRemoteObject implements RMIReceiveInterface {
	private static final transient Logger log = LoggerFactory.getLogger(RMIReceiver.class);

  /**
   * creates an RMI receiver. This simply registers the RMI name and the new
   * receiver with the rmi registry
   * 
   * @param RMIName
   * @throws RemoteException
   * @throws MalformedURLException
   */
  public RMIReceiver(String RMIName) throws RemoteException, MalformedURLException {
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
  public void register(String RMIName) {
    try {
      try {
        System.out.println("Reading rmiregistry - " + RMIName);
        log.info("Reading rmiregistry - " + RMIName);
        String names[] = Naming.list(RMIName);
        for (String name : names) {
          System.out.println("Found in rmiregistry - " + name);
          log.info("Found in rmiregistry - " + name);
        }
        Naming.bind(RMIName, this);
      }
      catch (AlreadyBoundException e) {
        System.out.println("Rebinding " + RMIName);
        log.info("Rebinding " + RMIName);
        Naming.rebind(RMIName, this);
      }
    }
    catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Local RMI address is " + RMIName);
    log.info("Local RMI address is " + RMIName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.rmi.RMIReceiveInterface#getObject(java.net.URI,
   *      java.lang.String, net.ponder2.Result)
   */
  public OID getObject(URI location, String path) throws RemoteException, Ponder2Exception {
    return Receiver.getObject(location, path).getOID();
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.rmi.RMIReceiveInterface#execute(com.twicom.qdparser.TaggedElement,
   *      com.twicom.qdparser.TaggedElement, net.ponder2.Result)
   */
  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args)
      throws RemoteException, Ponder2Exception {
    return Receiver.execute(address, target, source, op, args);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.rmi.RMIReceiveInterface#execute(java.lang.String)
   */
  public P2Object executeXml(String xmlString) throws RemoteException, Ponder2Exception {
    System.out.println("Remote RMI execute called for xml " + xmlString);
    log.info("Remote RMI execute called for xml " + xmlString);
    return new XMLParser().execute(null, xmlString);
  }

  public P2Object executePonderTalk(String ponderTalk) throws RemoteException, Ponder2Exception {
    System.out.println("Remote RMI execute called for PonderTalk " + ponderTalk);
    log.info("Remote RMI execute called for PonderTalk " + ponderTalk);
    String xml = P2Compiler.parse(ponderTalk);
    return new XMLParser().execute(null, xml);
  }
}
