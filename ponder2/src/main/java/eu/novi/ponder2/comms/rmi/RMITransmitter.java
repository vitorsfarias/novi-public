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
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.comms.TransmitterImpl;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.StartStopPonder2SMC;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;

/**
 * This class embodies the sending mechanism for inter-SMC communications using
 * the RMI protocol. When instantiated it locks itself to one particular
 * destination. One RMITransmitter is created for each RMI destination that is
 * used.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class RMITransmitter extends TransmitterImpl implements Transmitter {
	private static final transient Logger log = LoggerFactory.getLogger(RMITransmitter.class);
  /**
   * the receiving interface of the remote SMC
   */
  private RMIReceiveInterface remote = null;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#connect(java.net.URI)
   */
  @Override
  public Transmitter connect(URI address) {
    RMITransmitter tx = new RMITransmitter();
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
      Object obj = Naming.lookup(location.getSchemeSpecificPart());
      remote = (RMIReceiveInterface)obj;
    }
    catch (MalformedURLException e) {
      System.out.println("RMIReceiveInterface: bad form of address - " + location);
      log.info("RMIReceiveInterface: bad form of address - " + location);
    }
    catch (RemoteException e) {
      System.out.println("RMIReceiveInterface: remote exception - " + location + " - "
          + e.getMessage());
      log.info("RMIReceiveInterface: remote exception - " + location + " - "
              + e.getMessage());
    }
    catch (NotBoundException e) {
      System.out.println("RMIReceiveInterface: remote RMI name not bound - " + location);
      log.info("RMIReceiveInterface: remote RMI name not bound - " + location);
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
    RemoteException error = null;
    int tries = 0;
    while (tries++ < 2) {
      try {
        OID oid = remote.getObject(location, path);
        return oid.getP2Object();
      }
      catch (ConnectException e) {
        error = e;
        System.out.println("RMI Transmitter: lost remote. Reconnecting to " + location);
        log.info("RMI Transmitter: lost remote. Reconnecting to " + location);
        
        lookup(location);
      }
      catch (RemoteException e) {
        error = e;
        break;
      }
    }
    throw new Ponder2OperationException(error.getMessage());
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
    RemoteException error = null;
    int tries = 0;
    while (tries++ < 2) {
      try {
        return remote.execute(address, target, source, op, args);
      }
      catch (ConnectException e) {
        error = e;
        System.out.println("RMI Transmitter: lost remote. Reconnecting to " + address);
        log.info("RMI Transmitter: lost remote. Reconnecting to " + address);
        lookup(address);
      }
      catch (RemoteException e) {
        error = e;
        break;
      }
    }
    throw new Ponder2OperationException(error.getMessage());
  }

}
