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
 * Created on Feb 9, 2010
 *
 * $Log:$
 */

package eu.novi.ponder2.comms;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Utilities {

  /**
   * Finds the local IP address. In the normal Java call returns the localhost
   * number then the interfaces are checked to see if a nonlocal IP number can
   * be obtained. If not the localhost number is ised.
   * 
   * @return the local IP address
   */
  public static String getLocalIpAddress() {
    try {
      String host = InetAddress.getLocalHost().getHostAddress();
      if (!host.startsWith("127.")) {
        System.out.println("Ponder2Comms: local host is: " + host);
        return host;
      }
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          System.out.println("Ponder2Comms: Looking at " + inetAddress.getHostAddress());
          if (!inetAddress.isLoopbackAddress()) {
            host = inetAddress.getHostAddress().toString();
            System.out.println("Ponder2Comms: local host is: " + host);
            return host;
          }
        }
      }
    }
    catch (SocketException e) {
      System.err.println("Ponder2Comms: Trying to find local IP address " + e.getMessage());
    }
    catch (UnknownHostException e) {
      System.err.println("Ponder2Comms: Trying to find local IP address " + e.getMessage());
    }
    System.out.println("Ponder2Comms: no external IP number found - using localhost");
    return "127.0.0.1";
  }

}
