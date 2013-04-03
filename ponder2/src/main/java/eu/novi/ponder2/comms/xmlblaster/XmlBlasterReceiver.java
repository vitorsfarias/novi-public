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
 * Created on Feb 6, 2010
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms.xmlblaster;

import net.ponder2.exception.Ponder2Exception;

import org.xmlBlaster.client.I_Callback;
import org.xmlBlaster.client.I_XmlBlasterAccess;
import org.xmlBlaster.client.key.UpdateKey;
import org.xmlBlaster.client.qos.UpdateQos;
import org.xmlBlaster.client.qos.UpdateReturnQos;
import org.xmlBlaster.util.Global;
import org.xmlBlaster.util.SessionName;
import org.xmlBlaster.util.XmlBlasterException;
import org.xmlBlaster.util.qos.address.Destination;

import eu.novi.ponder2.comms.Receiver;
import eu.novi.ponder2.comms.XmlblasterProtocol;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class XmlBlasterReceiver implements I_Callback {

  private Global glob;
  private I_XmlBlasterAccess receiver;
  private XmlBlasterSync sync;

  public XmlBlasterReceiver(I_XmlBlasterAccess receiver, XmlBlasterSync sync) {
    this.receiver = receiver;
    this.glob = receiver.getGlobal();
    this.sync = sync;
  }

  
   * (non-Javadoc)
   * 
   * @see org.xmlBlaster.client.I_Callback#update(java.lang.String,
   * org.xmlBlaster.client.key.UpdateKey, byte[],
   * org.xmlBlaster.client.qos.UpdateQos)
   
  public String update(String cbSessionId, UpdateKey updateKey, byte[] content, UpdateQos updateQos)
      throws XmlBlasterException {
    if (updateKey.isInternal())
      return "";
    if (updateQos.isErased())
      return "";
    // Ignore messages that we have sent
    SessionName sender = updateQos.getSender();
    if (receiver.getSessionName().equalsRelative(sender))
      return "";

    String message = new String(content);
    String msgtype = updateQos.getClientProperty("Ponder2msgtype", "");
    String ponder2id = updateQos.getClientProperty("Ponder2id", "");

    // Is this a reply or a new message?
    if (msgtype.equals("request")) {
      // Execute the request and get the reply
      System.out.println("XmlBlasterReceiver: Got request from '" + sender + "'");

      Destination destination = new Destination(sender);

      String reply;
      String type;
      try {
        reply = Receiver.execute(message);
        type = "reply";
      }
      catch (Ponder2Exception e) {
        reply = e.toString();
        type = "exception";
      }
      XmlblasterProtocol.publish(receiver, destination, reply, type, ponder2id);
    }
    else if (msgtype.equals("reply")) {
      // We have a reply. We must match it to the waiting task
      sync.reply(ponder2id, message);
    }
    else if (msgtype.equals("exception")) {
      // We have a remote exception. We must let the matching task know
      sync.exception(ponder2id, message);
    }
    else
      System.out.println("XmlBlasterReceiver: received bad message type: " + updateKey);
    UpdateReturnQos uq = new UpdateReturnQos(glob);
    return uq.toXml();
  }
}
*/