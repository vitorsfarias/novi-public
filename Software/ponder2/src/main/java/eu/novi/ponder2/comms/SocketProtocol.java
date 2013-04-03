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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import eu.novi.ponder2.comms.socket.SocketReceiver;
import eu.novi.ponder2.comms.socket.SocketTransmitter;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.Shell;
import eu.novi.ponder2.exception.Ponder2RemoteException;

/**
 * RMI protocol implementation for inter-SMC chit-chat
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class SocketProtocol implements Protocol, Runnable {

  public static final int portbase = 23570;
  public static int port = portbase;
  public static final int maxport = 24000;
  protected ServerSocket serverSocket;

  /*
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Protocol#install(java.net.URI,
   * uk.ac.ic.doc.dse.ponder2.Result)
   */
  public void install(URI address, URI remote) throws Ponder2RemoteException {
    serverSocket = null;
    // If we don't have a local address, we had better make one up
    if (address == null) {
      String host = null;
      boolean gotPort = false;
      while (!gotPort) {
        try {
          System.out.println("SocketProtocol: trying port " + port);
          serverSocket = Shell.setup(port);
          gotPort = true;
          host = Utilities.getLocalIpAddress();
          String newaddress = "socket://" + host + ":" + port;
          address = new URI(newaddress);
          System.out.println("SocketProtocol: opened port: " + newaddress);
        }
        catch (IOException e) {
          port++;
          if (port > maxport)
            throw new Ponder2RemoteException("SocketProtocol: Cannot assign any ports: "
                + e.getMessage());
        }
        catch (URISyntaxException e) {
          throw new Ponder2RemoteException("SocketProtocol URI syntax error " + "socket://" + host
              + ":" + port + " - " + e.getMessage());
        }
      }
    }
    else {
      HostPort hostport = new HostPort(address);
      String host = hostport.host;

      // Try a few times to get this port, may be in time-wait state
      int tries = 33;
      IOException error = null;
      while (tries-- > 0) {
        try {
          serverSocket = Shell.setup(hostport.port);
          break;
        }
        catch (IOException e) {
          error = e;
          if (!e.getMessage().contains("Address already in use"))
            break;
          System.out.println("SocketProtocol: waiting for port " + port + " - " + tries
              + " tries left: " + e.getMessage());
          try {
            Thread.sleep(2000);
          }
          catch (InterruptedException e1) {
          }
        }
      }
      if (serverSocket == null) {
        throw new Ponder2RemoteException("SocketProtocol: Cannot assign port " + host + ":"
            + hostport.port + " (" + address + "): " + error.getMessage());
      }
      System.out.println("Remote Socket address is " + address);
      // OK we have the port set up
      try {
        if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1")) {
          host = Utilities.getLocalIpAddress();
          address = new URI("socket://" + host + ":" + port);
        }
      }
      catch (URISyntaxException e) {
        throw new Ponder2RemoteException("SocketProtocol URI syntax error " + "socket://" + host
            + ":" + port + " - " + e.getMessage());
      }
    }
    new Thread(this).start();
    String protocol = address.getScheme().toLowerCase();
    // Now register the protocol and let the external managed object use it
    ExternalManagedObject.registerProtocol(protocol, new SocketTransmitter(), address);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    while (true) {
      try {
        Socket clientSocket = serverSocket.accept();
        new SocketReceiver(clientSocket);
      }
      catch (IOException e) {
        System.out.print("SocketProtocol: lost connection");
      }
    }
  }

  public static class HostPort {

    public String host;
    @SuppressWarnings("hiding")
    public int port;

    public HostPort(URI address) {
      String hostport = address.getSchemeSpecificPart();
      // remove leading '/'s
      while (hostport.length() > 0 && hostport.charAt(0) == '/')
        hostport = hostport.substring(1);
      // New style with a ':' or old with a '/'?
      int index = hostport.indexOf(':');
      if (index < 0)
        index = hostport.indexOf('/');
      host = hostport.substring(0, index);
      port = Integer.parseInt(hostport.substring(index + 1));
    }
  }
}
