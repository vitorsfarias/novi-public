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

import eu.novi.ponder2.comms.Receiver;

import eu.novi.ponder2.exception.Ponder2Exception;

/**
 * The receiving class that is registered with the rmiregistry. Any messages for
 * this RMI name come through this receiver in its own thread.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class SocketReceiver implements Runnable {

  Socket socket;
  private DataOutputStream os;
  private DataInputStream is;

  /**
   * creates a socket receiver. 
   * 
   * @param socket
   * @throws IOException
   */
  public SocketReceiver(Socket socket) throws IOException {
    this.socket = socket;
    os = new DataOutputStream(socket.getOutputStream());
    is = new DataInputStream(socket.getInputStream());
    new Thread(this).start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
      try {
        String xml = is.readUTF();
        System.out.println("Socketreceiver: Received an XML request");
        os.writeUTF(Receiver.execute(xml));
      }
      catch (IOException e) {
        if (e.getMessage() != null)
          System.out.println("SocketReceiver: Closing because of IOException:" + e.getMessage());
        try {
          os.close();
          is.close();
          socket.close();
        }
        catch (IOException e1) {
          System.out.println("SocketReceiver: failed to close down:" + e1.getMessage());
        }
      }
      catch (Ponder2Exception e) {
        System.out.println("SocketReceiver: Ponder2Exception:" + e.getMessage());
      }
  }

}
