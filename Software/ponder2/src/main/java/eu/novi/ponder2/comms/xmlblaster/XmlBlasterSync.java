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
 * Created on Feb 8, 2010
 *
 * $Log:$
 *//*

package eu.novi.ponder2.comms.xmlblaster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import net.ponder2.exception.Ponder2RemoteException;

*//**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 *//*
public class XmlBlasterSync {

  Map<String, SyncEntry> entries;
  private static int count = 0;

  public XmlBlasterSync() {
    entries = new HashMap<String, SyncEntry>();
  }

  synchronized String create() {
    String id = String.valueOf(++count);
    SyncEntry se = new SyncEntry(id);
    entries.put(id, se);
    return id;
  }

  private synchronized void remove(String id) {
    entries.remove(id);
  }

  private synchronized SyncEntry get(String id) {
    return entries.get(id);
  }

  String awaitReply(String id) throws Ponder2RemoteException {
    SyncEntry se = get(id);
    se.sem.acquireUninterruptibly();
    remove(id);
    if (se.exception)
      throw new Ponder2RemoteException(se.reply);
    return se.reply;
  }

  void reply(String id, String message) {
    SyncEntry se = get(id);
    if (se == null) {
      System.err.println("XmlBlasterProtocol: Sync, unknown reply id: " + id + " - " + message);
      return;
    }
    se.reply = message;
    se.sem.release();
  }

  void exception(String id, String message) {
    SyncEntry se = get(id);
    if (se == null) {
      System.err.println("XmlBlasterProtocol: Sync, unknown reply id: " + id + " - " + message);
      return;
    }
    se.reply = message;
    se.exception = true;
    se.sem.release();
  }

  static public class SyncEntry {

    Semaphore sem;
    String id;
    String reply;
    boolean exception;

    SyncEntry(String id) {
      this.id = id;
      sem = new Semaphore(0);
      reply = null;
      exception = false;
    }
  }

}
*/