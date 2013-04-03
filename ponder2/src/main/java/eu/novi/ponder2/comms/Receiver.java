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

package eu.novi.ponder2.comms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import eu.novi.ponder2.comms.P2Serializable;

import com.twicom.qdparser.TaggedElement;
import com.twicom.qdparser.XMLReader;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;

/**
 * receive methods to match the transmit methods. This is a helper class common
 * to all protocols. If the protocol is capable of handing the transmit
 * arguments straight across then these methods simply have to be called without
 * any further work
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Receiver {

	/**
	 * Returns a managed object to a remote SMC. The object is located by using
	 * the given pathname. The address used to access this SMC is also given.
	 * This address is added to the list of the SMC's addresses in case the
	 * local SMC did not know that it could be addressed this way. Future OIDs
	 * when serialised will include the new address.
	 * 
	 * @param address
	 *            the URI used to talk to this SMC
	 * @param path
	 *            the local pathname for the object to be looked up
	 * @return the object located at the pathname
	 * @throws Ponder2Exception
	 */
	public static P2Object getObject(URI address, String path)
			throws Ponder2Exception {
		System.out.println("Remote getObject called for path " + path);
		// This may be a new address for us, we had better save it to tell
		// people
		// about it
		OID.addAddress(address);
		return Util.resolve("/", path);
	}

	/**
	 * Executes a remotely received command. All the arguments are the basic
	 * types required by the operation method
	 * 
	 * @param address
	 *            the URI that this SMC was addressed with
	 * @param oid
	 *            the OID of the object (target) to receive the command
	 * @param source
	 *            the source object of the command
	 * @param op
	 *            the operation itself
	 * @param args
	 *            an array of operation arguments
	 * @return the result of the operation as an internal SMC object
	 * @throws Ponder2Exception
	 *             if an error is found
	 */
	public static P2Object execute(URI address, OID oid, P2Object source,
			String op, P2Object[] args) throws Ponder2Exception {
		if (SelfManagedCell.SystemTrace)
			System.out.println("Receiver: execute called for oid/op " + oid
					+ " " + op);
		P2Object result = oid.getP2Object().operation(source, op, args);
		return result;
	}

	/**
	 * Executes a remotely received command. All Ponder2 types are expressed as
	 * Ponder2 XML.
	 * 
	 * @param address
	 *            the URI that this SMC was addressed with
	 * @param xoid
	 *            the OID of the object (target) to receive the command in XML
	 *            format
	 * @param xsource
	 *            the source object of the command in XML format
	 * @param op
	 *            the operation itself
	 * @param xargs
	 *            an array of operation arguments in XML format
	 * @return the result of the operation as a Ponder2 object in XML format
	 * @throws Ponder2Exception
	 */
	public static TaggedElement execute(URI address, TaggedElement xoid,
			TaggedElement xsource, String op, TaggedElement[] xargs)
			throws Ponder2Exception {
		if (SelfManagedCell.SystemTrace)
			System.out.println("Receiver: execute called for oid/op " + xoid
					+ " " + op);

		Map<Integer, P2Serializable> read = new HashMap<Integer, P2Serializable>();
		OID oid = P2Object.fromXml(xoid, read).getOID();
		P2Object source = P2Object.fromXml(xsource, read);
		P2Object[] args = new P2Object[xargs.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = P2Object.fromXml(xargs[i], read);
		}
		P2Object result = oid.getP2Object().operation(source, op, args);
		return result.writeXml(new HashSet<P2Object>());
	}

	/**
	 * Executes a remotely received command. All the arguments are rolled into a
	 * single XML structure. This method has to be matched by the method in
	 * TransmitterImpl that rolls the arguments up into the XML structure.
	 * 
	 * @param xml
	 *            a single XML structure containing all the information for the
	 *            execute command as sub-elements
	 * @return the result of the operation as a Ponder2 object in XML format
	 * @throws Ponder2Exception
	 */
	public static TaggedElement execute(TaggedElement xml)
			throws Ponder2Exception {
		try {
			TaggedElement result;
			String action = xml.getAttribute("action", "execute");
			URI address = new URI(xml.getAttribute("address"));
			// The xml could actually be a getObject call
			if (action.equals("getobject")) {
				String path = xml.getAttribute("path");
				result = getObject(address, path).writeXml(
						new HashSet<P2Object>());
			} else {
				TaggedElement target = (TaggedElement) xml.getChild(0);
				TaggedElement source = (TaggedElement) xml.getChild(1);
				String op = xml.getAttribute("op");
				int elements = xml.elements();
				TaggedElement[] args = new TaggedElement[elements - 2];
				for (int i = 2; i < elements; i++) {
					args[i - 2] = (TaggedElement) xml.getChild(i);
				}
				result = execute(address, target, source, op, args);
			}
			return result;
		} catch (URISyntaxException e) {
			throw new Ponder2ArgumentException("Receive: bad URI address - "
					+ e.getMessage());
		}
	}

	/**
	 * Executes a remotely received command. All the arguments are rolled into a
	 * single XML string
	 * 
	 * @param sxml
	 *            a single XML structure, in string form, containing all the
	 *            information for the execute command as sub-elements
	 * @return the result of the operation as a Ponder2 object in XML format
	 * @throws Ponder2Exception
	 */
	public static String execute(String sxml) throws Ponder2Exception {
		TaggedElement xml = XMLReader.parse(sxml);
		return execute(xml).toString();
	}
}
