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

package eu.novi.ponder2.comms.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.comms.TransmitterImpl;
import eu.novi.ponder2.comms.SocketProtocol.HostPort;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

/**
 * This class embodies the sending mechanism for inter-SMC communications using
 * the Socket protocol. When instantiated it locks itself to one particular
 * destination. One SocketTransmitter is created for each Socket destination
 * that is used.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class SocketTransmitter extends TransmitterImpl implements Transmitter {

  Socket socket = null;
  DataOutputStream os;
  DataInputStream is;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Transmitter#connect(java.net.URI)
   */
  @Override
  public Transmitter connect(URI address) throws Ponder2RemoteException {
    // We always open a new socket to cope with parallel threads
    return this;
  }

  /**
   * looks up the interface for the remote SMC
   * 
   * @param address
   *            the remote IP address
   * @return true if the lookup succeeded
   * @throws Ponder2RemoteException
   */
  private boolean open(URI address) throws Ponder2RemoteException {
    HostPort hostport = new HostPort(address);
    try {
      socket = new Socket(hostport.host, hostport.port);
      os = new DataOutputStream(socket.getOutputStream());
      is = new DataInputStream(socket.getInputStream());
    }
    catch (IOException e) {
      System.out.println("SocketTransmitter: failed to open " + address + " (" + hostport.host + ","
          + hostport.port + ") - " + e.getMessage());
    }
    return (socket != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#getObject(java.net.URI,
   *      java.lang.String)
   */
  @Override
  public P2Object getObject(URI location, String path) throws Ponder2Exception {
    return getObjectString(location, path);
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
    return executeString(address, target, source, op, args);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.TransmitterImpl#execute(java.lang.String)
   */
  @Override
  protected String execute(URI address, String xmlString) throws Ponder2RemoteException {
    IOException error = null;
    int tries = 0;
    open(address);
    while (tries++ < 3) {
      try {
        if (os==null) throw new IOException("Socket transmitter not open");
        os.writeUTF(xmlString);
        return is.readUTF();
      }
      catch (IOException e) {
        error = e;
        System.out.println("Socket Transmitter: Lost remote. Reconnecting to " + address);
        if (tries > 0)
          try {
            Thread.sleep(2000);
          }
          catch (InterruptedException e1) {
          }
        open(address);
      }
    }
    throw new Ponder2RemoteException(error.getMessage());
  }

}
