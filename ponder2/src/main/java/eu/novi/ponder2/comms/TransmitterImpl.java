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
 * Created on Mar 23, 2006
 *
 * $Log:$
 */

package eu.novi.ponder2.comms;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.comms.Transmitter;

import com.twicom.qdparser.TaggedElement;
import com.twicom.qdparser.XMLReader;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

/**
 * Describes the methods that all Ponder2 comms protocols for inter-SMC
 * communication must supply
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class TransmitterImpl implements Transmitter {

	/**
	 * creates and connects a Transmitter to a remote location. This is called
	 * once every time a new remote address is brought into play. If the
	 * location is important to the Transmitter i.e. a permanent channel is
	 * opened then a new instance of transmitter should be created. If the
	 * remote location does not matter then only one instance of the Transmitter
	 * need be created and this method can return itself.
	 * 
	 * @param address
	 *            the location that this protocol is to be connected to
	 * @return a new communications protocol connected to the appropriate place
	 *         or null if it fails
	 * @throws Ponder2RemoteException
	 */
	public Transmitter connect(URI address) throws Ponder2RemoteException {
		return this;
	}

	/**
	 * Checks to see if the remote service is up and running. Returns true or
	 * false.
	 * 
	 * @param address
	 *            the address of the remote service
	 * @return true if the service is running
	 */
	public boolean ping(URI address) {
		try {
			getObject(address, "root");
			return true;
		} catch (Exception e) {
			if (SelfManagedCell.SystemTrace)
				System.err.println("Ping failed for " + address);
			return false;
		}
	}

	/**
	 * gets a managed object from a remote SMC
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param path
	 *            the full path name of the remote managed object
	 * @return the requested Ponder2 object
	 * @throws Ponder2Exception
	 */
	public abstract P2Object getObject(URI address, String path)
			throws Ponder2Exception;

	/**
	 * gets a managed object from a remote SMC using XML for the communications
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param path
	 *            the full path name of the remote managed object
	 * @return the requested Ponder2 object
	 * @throws Ponder2Exception
	 */
	protected final P2Object getObjectXml(URI address, String path)
			throws Ponder2Exception {
		TaggedElement xml = makeXmlGetObject(address, path);
		TaggedElement reply = execute(address, xml);
		return P2Object.fromXml(reply, new HashMap<Integer, P2Serializable>());
	}

	/**
	 * gets a managed object from a remote SMC using XML as a string for the
	 * communications
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param path
	 *            the full path name of the remote managed object
	 * @return the requested Ponder2 object
	 * @throws Ponder2Exception
	 */
	protected final P2Object getObjectString(URI address, String path)
			throws Ponder2Exception {
		TaggedElement xml = makeXmlGetObject(address, path);
		String sxml = xml.toString(false);
		String reply = execute(address, sxml);
		return P2Object.fromXml(XMLReader.parse(reply),
				new HashMap<Integer, P2Serializable>());
	}

	/**
	 * Rolls the getObject parameters into an XML structure. This method has to
	 * be match by its counterpart in Receiver.
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param path
	 *            the full path name of the remote managed object
	 * @return the newly created XML structure
	 * @throws Ponder2Exception
	 */
	private final TaggedElement makeXmlGetObject(URI address, String path)
			throws Ponder2Exception {
		TaggedElement xml = new TaggedElement("P2Remote");
		xml.setAttribute("action", "getobject");
		xml.setAttribute("address", address.toString());
		xml.setAttribute("path", path);
		return xml;
	}

	/**
	 * executes commands at a remote managed object
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param target
	 *            the remote object's OID
	 * @param source
	 *            the originator of the operation
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	public abstract P2Object execute(URI address, OID target, P2Object source,
			String op, P2Object[] args) throws Ponder2Exception;

	/**
	 * executes commands at a remote managed object. All the information is
	 * rolled into an XML structure which is passed to the remote site
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param target
	 *            the remote object's OID
	 * @param source
	 *            the originator of the operation
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	protected final P2Object executeXml(URI address, OID target,
			P2Object source, String op, P2Object[] args)
			throws Ponder2Exception {
		TaggedElement xml = makeXmlExecute(address, target, source, op, args);
		TaggedElement reply = execute(address, xml);
		return P2Object.fromXml(reply, new HashMap<Integer, P2Serializable>());
	}

	/**
	 * executes commands at a remote managed object. All the information is
	 * rolled into an XML structure which is passed to the remote site as a
	 * string
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param target
	 *            the remote object's OID
	 * @param source
	 *            the originator of the operation
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	protected final P2Object executeString(URI address, OID target,
			P2Object source, String op, P2Object[] args)
			throws Ponder2Exception {
		TaggedElement xml = makeXmlExecute(address, target, source, op, args);
		String sxml = xml.toString(false);
		String reply = execute(address, sxml);
		return P2Object.fromXml(XMLReader.parse(reply),
				new HashMap<Integer, P2Serializable>());
	}

	/**
	 * Rolls the execute arguments into a single XML structure. This method must
	 * be matched by the method in Receiver which unravels the arguments before
	 * executing them.
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param target
	 *            the remote object's OID
	 * @param source
	 *            the originator of the operation
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return the newly created XML structure
	 * @throws Ponder2Exception
	 */
	private final TaggedElement makeXmlExecute(URI address, OID target,
			P2Object source, String op, P2Object[] args)
			throws Ponder2Exception {
		TaggedElement xml = new TaggedElement("P2Remote");
		xml.setAttribute("action", "execute");
		xml.setAttribute("address", address.toString());
		xml.setAttribute("op", op);
		Set<P2Object> written = new HashSet<P2Object>();
		TaggedElement xtarget = target.writeXml(written);
		xml.add(xtarget);
		TaggedElement xsource = source.writeXml(written);
		xml.add(xsource);
		for (int i = 0; i < args.length; i++) {
			xml.add(args[i].writeXml(written));
		}
		return xml;
	}

	// The following classes must be overridden when using one or more of the
	// above,
	// matching execute methods

	/**
	 * Executes a command remotely
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param xml
	 *            all the required arguments rolled into an XML structure
	 * @return the object returned after executing the xmlString
	 * @throws Ponder2Exception
	 */
	protected TaggedElement execute(URI address, TaggedElement xml)
			throws Ponder2Exception {
		throw new Ponder2OperationException(
				"Transmitter execute xml is not valid for class "
						+ this.getClass().getCanonicalName());
	}

	/**
	 * Executes a command remotely
	 * 
	 * @param address
	 *            the address of the remote SMC
	 * @param xmlString
	 *            all the required arguments rolled into an XML structure
	 *            expressed as a string
	 * @return the object returned after executing the xmlString
	 * @throws Ponder2Exception
	 */
	protected String execute(URI address, String xmlString)
			throws Ponder2Exception {
		throw new Ponder2OperationException(
				"Transmitter execute string is not valid for class "
						+ this.getClass().getCanonicalName());
	}

}
