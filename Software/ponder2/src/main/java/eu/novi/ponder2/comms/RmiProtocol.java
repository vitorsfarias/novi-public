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
 */

package eu.novi.ponder2.comms;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.comms.rmi.RMIReceiver;
import eu.novi.ponder2.comms.rmi.RMITransmitter;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.Path;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.StartStopPonder2SMC;
import eu.novi.ponder2.exception.Ponder2RemoteException;

/**
 * RMI protocol implementation for inter-SMC chit-chat
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class RmiProtocol implements Protocol {
	private static final transient Logger log = LoggerFactory.getLogger(RmiProtocol.class);
  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Protocol#install(java.net.URI,
   *      uk.ac.ic.doc.dse.ponder2.Result)
   */
  public void install(URI address, URI remote) throws Ponder2RemoteException {
    // Assign a security manager
    // Is this necessary here?
     if (System.getSecurityManager() == null){
    	 log.info("Assign a security manager if it does not exist(DISABLED)");
    //	 System.setSecurityManager(new RMISecurityManager());
    	// log.info("System.setSecurityManager(new RMISecurityManager())");
    	 
     }
     
    // If we don't have a local address, we had better make one up
    if (address == null) {
      try {
        String host = InetAddress.getLocalHost().getHostAddress();
        int rand = new Random().nextInt();
        address = new URI("rmi://" + host + "/Ponder2-" + rand);
        System.out.println("Remote RMI address is " + address);
        log.info("Remote RMI address is " + address);
      }
      catch (UnknownHostException e) {
        // This should not happen! That's what they all say.
        throw new Ponder2RemoteException("RmiProtocol: unknown host " + address + ": "
            + e.getMessage());
      }
      catch (URISyntaxException e) {
        // Nor this!
        throw new Ponder2RemoteException("RmiProtocol URI syntax error " + address + ": "
            + e.getMessage());
      }
    }
    Path path = new Path(address.getSchemeSpecificPart());
    String RMIName = path.tail(1);
    try {
      // Register a new name for this SMC with the rmiregistry
      new RMIReceiver(address.getSchemeSpecificPart());
      System.out.println("Remote RMI address is " + address);
      log.info("Remote RMI address is " + address);
    }
    catch (RemoteException e) {
      throw new Ponder2RemoteException("RmiProtocol cannot contact RMI registry for " + RMIName
          + ". Is it running? " + (SelfManagedCell.SystemTrace ? e.getMessage() : ""));
    }
    catch (MalformedURLException e) {
      throw new Ponder2RemoteException("RmiProtocol: bad RMI name " + RMIName + ": "
          + e.getMessage());
    }
    String protocol = address.getScheme().toLowerCase();
    // Now register the protocol and let the external managed object use it
    ExternalManagedObject.registerProtocol(protocol, new RMITransmitter(), address);
  }
}
