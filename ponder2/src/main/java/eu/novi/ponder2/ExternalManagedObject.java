/**
 * Created on Sep 20, 2005
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
 * $Log: ExternalManagedObject.java,v $
 * Revision 1.18  2005/11/19 21:11:33  kpt
 * Shows remote OID for e.g. ls -l
 *
 * Revision 1.17  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.16  2005/11/03 04:23:46  kpt
 * More restore and tidying done
 *
 * Revision 1.15  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.14  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.13  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.12  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.11  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.10  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.9  2005/10/11 10:11:40  kpt
 * First full version of the demo
 *
 * Revision 1.8  2005/10/10 04:28:59  kpt
 * Improved external use. Assume local unless remote specified
 *
 * Revision 1.7  2005/10/09 22:03:04  kpt
 * First complete version of demo with a few fixes.
 Local evaluation added on demand before shipping xml for export.
 *
 * Revision 1.6  2005/10/06 10:59:17  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.5  2005/10/01 20:50:52  kpt
 * Changed result type to new Result class
 *
 * Revision 1.4  2005/09/30 11:04:21  kpt
 * Tidied up a little
 *
 * Revision 1.3  2005/09/21 15:54:56  kpt
 * USE now works through external domains
 *
 * Revision 1.2  2005/09/21 13:46:40  kpt
 * Can now create and send events using XML and the event templates
 *
 * Revision 1.1  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 */

package eu.novi.ponder2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.Protocol;
import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.OID;

/**
 * A managed object that represents a remote managed object. It acts as a proxy,
 * rerouting any requests that it receives to the remote object. An external
 * managed object is not imported and used in the normal way, it is
 * automatically instantiated and filled in by the <use> clause when it
 * recognises a remote address.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class ExternalManagedObject implements ManagedObject {

	/**
	 * Map of protocol names to that actual protocol code to perform I/O using
	 * that protocol
	 */
	private static Map<String, Transmitter> protocolTable = new HashMap<String, Transmitter>();
	/**
	 * Cache of locations to external protocol methods
	 */
	private static Map<URI, Transmitter> locationTable = new HashMap<URI, Transmitter>();

	/**
	 * Registers an external protocol so that it can be used as a transport
	 * mechanism. This is called from the install method of the protocol being
	 * installed
	 * 
	 * @param name
	 *            the name of the protocol
	 * @param protocol
	 *            instance of the protocol interface allowing messages to be
	 *            sent
	 * @param address
	 *            the address of a remote SMC
	 */
	public static void registerProtocol(String name, Transmitter protocol,
			URI address) {
		protocolTable.put(name, protocol);
		if (address != null)
			OID.addAddress(address);
	}

	/**
	 * sees if a protocol has already been loaded
	 * 
	 * @param name
	 *            the name of the protocol to be checked
	 * @return true if the protocol has been loaded
	 */
	public static boolean hasProtocol(String name) {
		return protocolTable.containsKey(name);
	}

	/**
	 * gets the protocol code for the given URI. if the protocol is not already
	 * loaded then it is located and loaded on the fly.
	 * 
	 * @param location
	 *            the remote address as a URI
	 * @return the comms class for the given protocol
	 * @throws Ponder2RemoteException
	 */
	public static Transmitter getRemote(URI location)
			throws Ponder2RemoteException {
		if (location == null)
			throw new Ponder2RemoteException(
					"No known remote address for object");
		String scheme = location.getScheme().toLowerCase();
		// We had better make sure that we have the right protocol drivers
		// loaded
		if (!hasProtocol(scheme)) {
			loadProtocol(scheme, null, location);
		}
		Transmitter protocol = protocolTable.get(scheme);
		Transmitter remote = locationTable.get(location);
		if (remote == null) {
			// We have to find the remote for this location
			remote = protocol.connect(location);
			locationTable.put(location, remote);
		}
		return remote;
	}

	/**
	 * Loads a new communications protocol for interworking with other Ponder2
	 * SMCs
	 * 
	 * @param protocol
	 *            protocol name
	 * @param address
	 *            address for others to reach us
	 * @param remote
	 *            remote address that kicked off this request
	 * @throws Ponder2RemoteException
	 */
	@SuppressWarnings("unchecked")
	public static void loadProtocol(String protocol, String address, URI remote)
			throws Ponder2RemoteException {
		URI uri = null;
		try {
			if (address != null)
				uri = new URI(address);
		} catch (URISyntaxException e) {
			throw new Ponder2RemoteException(e.getMessage());
		}
		if (protocol == null && uri != null)
			protocol = uri.getScheme();
		if (protocol == null)
			throw new Ponder2RemoteException(
					"Load protocol: protocol cannot be determined from "
							+ address);

		// Make the protocol starts with uppercase, rest lower
		// and construct the Class name
		protocol = protocol.toLowerCase();
		String start = protocol.substring(0, 1);
		protocol = start.toUpperCase() + protocol.substring(1) + "Protocol";

		// Now load and instantiate the class
		Class<Protocol> cl = null;
		try {
			cl = (Class<Protocol>) Class.forName(protocol);
		} catch (ClassNotFoundException e) {
			// Ignore and try again
			try {
				cl = (Class<Protocol>) Class.forName("eu.novi.ponder2.comms."
						+ protocol);
			} catch (ClassNotFoundException e1) {
				throw new Ponder2RemoteException(
						"Load protocol: protocol not found: " + protocol);
			}
		}
		try {
			// cl must have a value by this point
			Protocol option = cl.newInstance();
			option.install(uri, remote);
		} catch (Exception e) {
			throw new Ponder2RemoteException("Load protocol failure for: "
					+ protocol + " - " + e.getMessage());
		}
	}

	/**
	 * The real external OID of the object
	 */
	OID externalOID;

	/**
	 * Valid addresses for the remote object
	 */
	List<URI> addresses;

	/**
	 * Preferred protocol
	 */
	String protocol;

	/**
	 * Preferred address
	 */
	URI address;
	private P2Object myP2Object;

	/**
	 * Creates a new external Managed Object that acts as a proxy for the real
	 * remote object. The remote protocol is checked to see that it exists and
	 * the driver is kept handy for speedy communications. NB This message is
	 * used internally by Ponder2.
	 * 
	 */
	@Ponder2op("create")
	public ExternalManagedObject(P2Object myP2Object) {
		this.myP2Object = myP2Object;
	}

	public void setExternalOID(OID externalOID) {
		this.externalOID = externalOID;
		// TODO Is this ok? we still have the old uid in the global OID table.
		myP2Object.getOID().setUid(externalOID.getUid());
		myP2Object.getOID().setDomain(externalOID.isDomain());
		// Get the valid addresses for the object
		addresses = new Vector<URI>();
		for (URI uri : externalOID.getRemoteAddresses()) {
			addresses.add(uri);
			String scheme = uri.getScheme();
			if (protocolTable.containsKey(scheme)) {
				protocol = scheme;
				address = uri;
			}
		}

	}

	/**
	 * All operations are sent to the external Managed Object. Answers with the
	 * answer from the external Managed Object.
	 * 
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            an array of the arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	@Ponder2op(Ponder2op.WILDCARD)
	protected P2Object obj_operation(P2Object source, String op,
			P2Object... args) throws Ponder2Exception {
		// We have to send something to the remote object
		Transmitter remote = getRemote(address);
		P2Object value = remote.execute(address, externalOID, source, op, args);
		// Patch up the result structure
		// System.out.println("Remote execute returns: " + value);
		return value;
	}

}
