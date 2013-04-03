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

package eu.novi.ponder2.comms;

import java.net.URI;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2RemoteException;

import org.xmlBlaster.client.I_XmlBlasterAccess;
import org.xmlBlaster.client.key.PublishKey;
import org.xmlBlaster.client.key.SubscribeKey;
import org.xmlBlaster.client.qos.ConnectQos;
import org.xmlBlaster.client.qos.ConnectReturnQos;
import org.xmlBlaster.client.qos.PublishQos;
import org.xmlBlaster.client.qos.SubscribeQos;
import org.xmlBlaster.util.Global;
import org.xmlBlaster.util.MsgUnit;
import org.xmlBlaster.util.SessionName;
import org.xmlBlaster.util.XmlBlasterException;
import org.xmlBlaster.util.property.Property;
import org.xmlBlaster.util.qos.address.Address;
import org.xmlBlaster.util.qos.address.Destination;
import org.xmlBlaster.util.qos.storage.ClientQueueProperty;

import eu.novi.ponder2.comms.xmlblaster.XmlBlasterReceiver;
import eu.novi.ponder2.comms.xmlblaster.XmlBlasterSync;
import eu.novi.ponder2.comms.xmlblaster.XmlBlasterTransmitter;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class XmlblasterProtocol implements Protocol {

  private I_XmlBlasterAccess con;
  public static final String P2TOPIC = "Ponder2Message";

  
   * (non-Javadoc)
   * 
   * @see net.ponder2.comms.Protocol#install(java.net.URI)
   
  public void install(URI address, URI remote) throws Ponder2RemoteException {

    try {
      // Set up semaphore block for this connection
      // we need to sync the requests and replies
      XmlBlasterSync sync = new XmlBlasterSync();

      // Sort out our name
      String host = null;
      int port = -1;
      String clientName;
      if (address == null) {
        clientName = SelfManagedCell.RootDomain.getOID().getUid();
      }
      else {
        host = address.getHost();
        port = address.getPort();
        // Get name and knock off leading '/'
        clientName = address.getPath();
        clientName = clientName.substring(1);
      }
      clientName = "Ponder2_" + clientName;

      final Global glob = new Global();
      con = glob.getXmlBlasterAccess();

      Property property = glob.getProperty();
      property.set("protocol", "SOCKET");
      property.set("persistence/defaultPlugin", "RAM");
      property.set("queue/defaultPlugin", "RAM");
      if (host != null)
        property.set("dispatch/connection/plugin/socket/hostname", host);
      if (port > 0)
        property.set("dispatch/connection/plugin/socket/port", "" + port);

      ClientQueueProperty prop = new ClientQueueProperty(glob, null);
      // Client side queue up to 100 entries if not connected
      prop.setMaxEntries(100);

      Address channel = new Address(glob);
      channel.setType("SOCKET"); // force SOCKET protocol
      channel.setDelay(4000L); // retry connecting every 4 sec
      channel.setRetries(-1); // -1 == forever
      // channel.setPingInterval(2000L); // ping every 2 sec
      prop.setAddress(channel);

      // Give name and password
      ConnectQos qos = new ConnectQos(glob);
      qos.setSessionName(new SessionName(glob, clientName));
      qos.setPersistent(true);
      qos.setPtpAllowed(true);
      qos.addClientQueueProperty(prop);

      // Login to xmlBlaster, register for updates
      ConnectReturnQos conRetQos = con.connect(qos, new XmlBlasterReceiver(con, sync));

      System.out.println("connection name is :" + con.getSessionName().getRelativeName());
      System.out.println(("Sender connected to xmlBlaster " + conRetQos.getSessionName()
          .getRelativeName()));

      // // Erase our topic
      // EraseKey ek = new EraseKey(glob, P2TOPIC);
      // EraseQos eq = new EraseQos(glob);
      // con.erase(ek, eq);

      // Subscribe to our topic
      SubscribeKey sk = new SubscribeKey(glob, P2TOPIC);
      SubscribeQos sq = new SubscribeQos(glob);
      // Suppress duplicate updates
      //sq.setMultiSubscribe(false);
      sq.setPersistent(true);

      con.subscribe(sk, sq);

      ExternalManagedObject.registerProtocol("xmlblaster", new XmlBlasterTransmitter(con, sync),
          address);
    }
    catch (XmlBlasterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void publish(I_XmlBlasterAccess connection, Destination destination,
      String message, String type, String ponder2id) throws XmlBlasterException {
    Global glob = connection.getGlobal();
    // PublishKey pk = new PublishKey(glob, XmlblasterProtocol.P2TOPIC);
    // No need to use a topic if using PtP
    PublishKey pk = new PublishKey(glob);
    PublishQos pq = new PublishQos(glob);
    destination.forceQueuing(true);
    pq.addDestination(destination);
    pq.addClientProperty("Ponder2msgtype", type);
    pq.addClientProperty("Ponder2id", ponder2id);
    MsgUnit msgUnit = new MsgUnit(pk, message, pq);
    connection.publish(msgUnit);
  }

}
*/