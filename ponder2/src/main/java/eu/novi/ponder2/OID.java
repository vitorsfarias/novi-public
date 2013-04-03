/**
 * Created on Jul 4, 2005
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
 * $Log: OID.java,v $
 * Revision 1.12  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.11  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.10  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.9  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.8  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.7  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.6  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.5  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.4  2005/10/06 10:59:17  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.3  2005/09/21 15:54:56  kpt
 * USE now works through external domains
 *
 * Revision 1.2  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.WriteAbortedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import eu.novi.ponder2.Domain;
import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.ExternalManagedObjectP2Adaptor;
import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Util;

import com.twicom.qdparser.Element;
import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Object;

/**
 * This is the fundamental object identifier for all the managed objects in the
 * system. Every managed object will have a single, unique OID associated with
 * it on a one to one, reversible mapping. The OID also contains all the known
 * ways of remotely contacting the SMC where the managed object resides.
 * 
 * @author Kevin Twidle
 * @version $Id: OID.java,v 1.12 2005/10/28 21:58:00 kpt Exp $
 */
public class OID implements P2Serializable, Externalizable {

	/**
	 * Table of all OIDs at this location (SMC)
	 */
	private static Map<String, OID> oidList = Collections
			.synchronizedMap(new HashMap<String, OID>());

	/**
	 * Set of our this SMC's addresses for different protocols
	 */
	private static Set<URI> myAddresses = Collections
			.synchronizedSet(new HashSet<URI>());

	private static Domain remoteDomain = null;

	private static int externalNumber = 0;

	private static Random random = new Random(System.currentTimeMillis());

	/**
	 * adds a new address to the set of addresses for this SMC
	 * 
	 * @param address
	 *            the address to be added
	 */
	public static void addAddress(URI address) {
		if (address != null)
			myAddresses.add(address);
		else
			System.err.println("OID: Warning null address added - ignoring");
	}

	public static Set<URI> getAddresses() {
		return myAddresses;
	}

	/**
	 * generates an OID from an XML structure. If the OID is already known to
	 * the SMC then the original OID is returned, otherwise a new OID is created
	 * and added to the SMC's set of OIDs.
	 * 
	 * @param xml
	 *            the XML structure containing the OID
	 * @return the OID generated from the XML structure
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	public static OID fromXML(TaggedElement xml)
			throws Ponder2OperationException, Ponder2ArgumentException {
		String uid = xml.getAttribute("uid");
		OID oid = oidList.get(uid);
		if (oid == null)
			try {
				// Get the OID info that we need
				boolean isDomain = xml.getAttribute("isDomain", "false")
						.equals("true");
				Set<URI> remoteAddresses = new HashSet<URI>();
				for (int i = 0; i < xml.elements(); i++) {
					Element el = xml.getChild(i);
					if (el instanceof TaggedElement) {
						TaggedElement child = (TaggedElement) el;
						if (child.getName().equals("address")) {
							try {
								URI uri = new URI(child.getAttribute("uri"));
								remoteAddresses.add(uri);
							} catch (URISyntaxException e) {
								throw new Ponder2OperationException(
										"OID: failed to parse URI ("
												+ child.getAttribute("uri")
												+ ")correctly" + e.getMessage());
							}
						}
					}
				}
				OID externalOID = new OID(uid, isDomain, remoteAddresses);
				oid = makeNewExternalObject(externalOID);
			} catch (Exception e) {
				throw new Ponder2OperationException("OID: failed to read OID"
						+ e.getMessage());
			}
		return oid;
	}

	//
	// End of static declarations
	//
	/**
	 * the immutable unique ID for this OID
	 */
	private String uid;

	/**
	 * whether this OID is associated with a domain managed object
	 */
	private boolean isDomain;

	/**
	 * remote addresses held for external OIDs only. Used exclusively by
	 * ExternalManagedObject
	 */
	private Set<URI> remoteAddresses = null;

	/**
	 * the managed object associated with this OID
	 */
	private P2ManagedObject managedObject;

	/**
	 * Dummy required for serialisation
	 */
	public OID() {
	}

	private OID(String uid, boolean isDomain, Set<URI> remoteAddresses) {
		this.uid = uid;
		this.isDomain = isDomain;
		this.remoteAddresses = remoteAddresses;
	}

	/**
	 * creates a new OID, initialises its fields and adds it to the SMC's list
	 * of OIDs
	 * 
	 */
	public OID(P2ManagedObject managedObject) {
		synchronized (random) {
			uid = "" + random.nextLong();
		}
		// address = CommsManager.getAddress();
		this.managedObject = managedObject;
		isDomain = false;
		oidList.put(uid, this);
	}

	/**
	 * checks whether this OID is associated with a domain managed object
	 * 
	 * @return true if this OID is associated with a domain managed object
	 */
	public boolean isDomain() {
		return isDomain;
	}

	/**
	 * sets whether this OID is associated with a domain managed object
	 * 
	 * @param isDomain
	 *            true if this OID is to be associated with a domain managed
	 *            object
	 */
	protected void setDomain(boolean isDomain) {
		this.isDomain = isDomain;
	}

	/**
	 * returns the actual managed object associated with this OID
	 * 
	 * @return the OID's managed object
	 */
	public P2ManagedObject getManagedObject() {
		return managedObject;
	}

	/**
	 * returns the actual managed object associated with this OID
	 * 
	 * @return the OID's managed object
	 */
	public P2Object getP2Object() {
		return managedObject.getP2Object();
	}

	protected Set<URI> getRemoteAddresses() {
		return remoteAddresses;
	}

	protected Set<URI> getAddressSet() {
		if (remoteAddresses != null)
			return remoteAddresses;
		return myAddresses;
	}

	/**
	 * returns the OID as an XML structure.
	 * 
	 * @return a description of the OID as an XML structure
	 */
	public TaggedElement toXML() {
		TaggedElement oid = new TaggedElement("oid");
		oid.setAttribute("uid", uid);
		oid.setAttribute("isdomain", Boolean.toString(isDomain));
		Set<URI> addresses = getAddressSet();
		if (!addresses.isEmpty()) {
			for (URI entry : addresses) {
				TaggedElement addr = new TaggedElement("address");
				addr.setAttribute("uri", entry.toString());
				oid.add(addr);
			}
		}
		return oid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toXML().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		uid = (String) in.readObject();
		setDomain(in.readBoolean());
		remoteAddresses = (Set<URI>) in.readObject();
	}

	/**
	 * Called by the serialization routines after readExternal as been called.
	 * 
	 * @return the newly read in OID, actually probably an old one.
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException {
		// Does this OID already exist?
		OID oid = oidList.get(uid);
		if (oid == null)
			try {
				oid = makeNewExternalObject(this);
			} catch (Exception e) {
				throw new WriteAbortedException("OID: failed to read OID", e);
			}
		return oid;
	}

	private static OID makeNewExternalObject(OID externalOID)
			throws Ponder2RemoteException {
		try {
			// We need to create an External Managed Object for the OID
			// First check that it is not really our object i.e. one we could
			// not find e.g. an old object from a previous incarnation
			for (URI uri : externalOID.getRemoteAddresses()) {
				if (myAddresses.contains(uri))
					throw new Ponder2RemoteException(
							"this OID does not exist here: "
									+ externalOID.getUid());
			}
			ExternalManagedObjectP2Adaptor adaptor = new ExternalManagedObjectP2Adaptor(
					SelfManagedCell.RootDomain, "create");
			((ExternalManagedObject) adaptor.getObj())
					.setExternalOID(externalOID);
			OID oid = adaptor.getOID();
			oidList.put(externalOID.uid, oid);
			// Make sure we have a domain to put it into
			if (remoteDomain == null) {
				try {
					OID remote = Util.resolve("/", "/remote").getOID();
					if (!remote.isDomain())
						throw new Ponder2RemoteException(
								"OID: Fatal error /remote is not a domain");
					remoteDomain = (Domain) ((DomainP2Adaptor) remote
							.getP2Object()).getObj();
				} catch (Ponder2Exception e) {
					// root/remote does not exist
					DomainP2Adaptor domAdaptor = new DomainP2Adaptor(
							SelfManagedCell.RootDomain, "create");
					remoteDomain = (Domain) domAdaptor.getObj();
					SelfManagedCell.RootDomain.operation(
							SelfManagedCell.RootDomain, "at:put:",
							P2Object.create("remote"), domAdaptor);
				}
			}
			externalNumber++;
			while (remoteDomain.get("external" + externalNumber) != null) {
				externalNumber++;
			}
			remoteDomain.add("external" + externalNumber,
					oid.getManagedObject());
			return oid;

		} catch (Ponder2Exception e) {
			throw new Ponder2RemoteException("OID: failed to read OID"
					+ e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(uid);
		out.writeBoolean(isDomain());
		out.writeObject(getAddresses());
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	protected void setUid(String uid) {
		this.uid = uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.comms.P2Serializable#writeXml()
	 */
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		TaggedElement xml = new TaggedElement("P2Object");
		xml.setAttribute("class", this.getClass().getCanonicalName());
		xml.setAttribute("uid", getUid());
		xml.setAttribute("isdomain", isDomain() ? "true" : "false");
		Set<URI> addresses = getAddressSet();
		for (URI uri : addresses) {
			TaggedElement address = new TaggedElement("address");
			address.setAttribute("uri", uri.toString());
			xml.add(address);
		}
		return xml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.comms.P2Serializable#readXml(com.twicom.qdparser.
	 * TaggedElement)
	 */
	public P2Object readXml(TaggedElement xml, Map<Integer, P2Serializable> read)
			throws Ponder2OperationException, Ponder2ArgumentException {
		uid = xml.getAttribute("uid");
		String isdomain = xml.getAttribute("isdomain");
		setDomain(isdomain.equals("true"));
		remoteAddresses = new HashSet<URI>();
		for (Element element : xml.getElements()) {
			TaggedElement address = (TaggedElement) element;
			try {
				URI uri = new URI(address.getAttribute("uri"));
				remoteAddresses.add(uri);
			} catch (URISyntaxException e) {
				throw new Ponder2OperationException("Bad URI in OID address: "
						+ address.getAttribute("uri"));
			}
		}
		// Does this OID exist?
		OID oid = oidList.get(uid);
		if (oid == null)
			try {
				oid = makeNewExternalObject(this);
			} catch (Exception e) {
				throw new Ponder2OperationException(
						"OID: failed to make new external OID for " + uid
								+ ": " + e.getMessage());
			}
		return oid.getP2Object();
	}

}
