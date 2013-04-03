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

package eu.novi.ponder2.comms.rmixml;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import eu.novi.ponder2.exception.Ponder2Exception;

import com.twicom.qdparser.TaggedElement;

/**
 * Interface description of the class handed over to the RMI registry. The RMI
 * registry hand this description off to SMCs that look up the address. The
 * methods below are operations that can be made through RMI with all
 * serialisation and rebuilding done by the RMI system.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface RmiXmlReceiveInterface extends Remote {

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI,
   *      java.lang.String, net.ponder2.Result)
   */
  public TaggedElement getObject(URI location, String path) throws RemoteException,
      Ponder2Exception;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#execute(java.net.URI,
   *      com.twicom.qdparser.TaggedElement, com.twicom.qdparser.TaggedElement,
   *      net.ponder2.Result)
   */
  public TaggedElement execute(TaggedElement xml) throws RemoteException, Ponder2Exception;

}
