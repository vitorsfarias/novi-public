/**
 * Created on Sep 19, 2005
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
 * $Log: RMIReceiveInterface.java,v $
 * Revision 1.2  2006/03/01 22:57:33  kpt
 * Added remote execution of external xml via RMI
 *
 * Revision 1.1  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 */

package eu.novi.ponder2.comms.rmi;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;

/**
 * Interface description of the class handed over to the RMI registry. The RMI
 * registry hand this description off to SMCs that look up the address. The
 * methods below are operations that can be made through RMI with all
 * serialisation and rebuilding done by the RMI system.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface RMIReceiveInterface extends Remote {

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI,
   *      java.lang.String, net.ponder2.Result)
   */
  public OID getObject(URI location, String path) throws RemoteException, Ponder2Exception;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#execute(java.net.URI,
   *      com.twicom.qdparser.TaggedElement, com.twicom.qdparser.TaggedElement,
   *      net.ponder2.Result)
   */
  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args)
      throws RemoteException, Ponder2Exception;

  /**
   * executes the given XML string. Used for external, non-SMC applications to
   * inject XML into the system
   * 
   * @param xmlString
   *            the XML commands to be executed
   * @return the standard result structure as an XML string
   * @throws RemoteException
   *             because it has to be a possibility
   * @throws Ponder2OperationException
   * @throws Ponder2ArgumentException
   */
  public P2Object executeXml(String xmlString) throws RemoteException, Ponder2Exception;

  public P2Object executePonderTalk(String ponderTalk) throws RemoteException, Ponder2Exception;

}
