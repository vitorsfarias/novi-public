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
 * Created on Feb 13, 2010
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms.ssc;

import java.io.IOException;

import eu.novi.ponder2.exception.Ponder2Exception;

import com.twicom.comms.singlesocket.Message;
import com.twicom.comms.singlesocket.MessageInterface;
import com.twicom.comms.singlesocket.client.Client;
import com.twicom.comms.singlesocket.client.ClientCallback;

import eu.novi.ponder2.comms.Receiver;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class SscReceiver implements ClientCallback {

  public SscReceiver(Client client) {
    client.register(this);
  }

  
   * (non-Javadoc)
   * 
   * @see
   * com.twicom.comms.singlesocket.ClientCallback#broadcast(com.twicom.comms
   * .singlesocket.MessageInterface)
   
  public void broadcast(MessageInterface message) {
    // TODO Auto-generated method stub

  }

  
   * (non-Javadoc)
   * 
   * @seecom.twicom.comms.singlesocket.ClientCallback#notify(com.twicom.comms.
   * singlesocket.MessageInterface)
   
  public void notify(MessageInterface message) {
    // TODO Auto-generated method stub

  }

  
   * (non-Javadoc)
   * 
   * @seecom.twicom.comms.singlesocket.ClientCallback#request(com.twicom.comms.
   * singlesocket.MessageInterface)
   
  public Message request(MessageInterface message) throws IOException {
    try {
      String reply = Receiver.execute(message.asString());
      return new Message(reply);
    }
    catch (Ponder2Exception e) {
      throw new IOException(e.getMessage());
    }
  }

}
*/