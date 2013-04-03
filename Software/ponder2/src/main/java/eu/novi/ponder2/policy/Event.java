/**
 * Created on Sep 2, 2005
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
 * $Log: Event.java,v $
 * Revision 1.23  2006/03/15 14:52:17  kpt
 * Improved handling of visited interaction also now thread safe
 *
 * Revision 1.22  2006/03/15 14:37:38  kpt
 * Fixed multiple and looping event paths
 *
 * Revision 1.21  2006/03/15 13:36:42  kpt
 * Lots of testing stuff and new XMLSaver managed object
 *
 * Revision 1.20  2006/02/22 16:35:58  kpt
 * First version of Proxy integration
 *
 * Revision 1.19  2006/02/11 16:28:35  kpt
 * Modified Event creation
 *
 * Revision 1.18  2005/12/13 17:33:53  kpt
 * Lots of improvements done for xmlBlaster
 *
 * Revision 1.17  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.16  2005/11/15 15:00:51  kpt
 * ExecuteSetup introduced, more result feedback
 *
 * Revision 1.15  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.14  2005/10/29 20:22:56  kpt
 * Tidied up managed object command execution a little
 *
 * Revision 1.13  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.12  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.11  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.10  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.9  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.8  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.7  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.6  2005/10/06 10:59:17  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.5  2005/10/01 20:50:52  kpt
 * Changed result type to new Result class
 *
 * Revision 1.4  2005/10/01 17:40:07  kpt
 * Tidied up the dialox code a little
 *
 * Revision 1.3  2005/09/21 13:46:40  kpt
 * Can now create and send events using XML and the event templates
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2.policy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.twicom.qdparser.Element;
import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

/**
 * The actual notification that is sent through the system. An Event is created
 * using an EventTemplate which packages up named arguments and includes them in
 * the Event. Several static methods aid in the creation and sending of Events.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Event extends P2Hash implements ManagedObject {

	private P2Object source;
	private P2Object eventTemplate;
	private Set<P2ManagedObject> visited;

	/**
	 * Creates an instance of an Event with no contents. The create is necessary
	 * for the compiler to generate the correct code, it is not supposed to be
	 * used in PonderTalk.
	 */
	@Ponder2op("create")
	public Event() {
	}

	public Event(P2Object source, String eventTemplate, P2Object... values)
			throws Ponder2Exception {
		this(source, Util.resolve(eventTemplate), values);
	}

	public Event(P2Object source, P2Object eventTemplate, P2Array values)
			throws Ponder2Exception {
		P2Hash hash = (eventTemplate.operation(source, "packageArgs:", values)
				.asHash());
		initialise(source, eventTemplate, hash);
	}

	public Event(P2Object source, P2Object eventTemplate, P2Object... values)
			throws Ponder2Exception {
		P2Hash hash = (eventTemplate.operation(source, "packageArgs:",
				P2Object.create(values)).asHash());
		initialise(source, eventTemplate, hash);
	}

	public Event(P2Object source, P2Object eventTemplate, P2Hash values)
			throws Ponder2ArgumentException, Ponder2OperationException {
		initialise(source, eventTemplate, values);
	}

	private void initialise(P2Object source, P2Object eventTemplate,
			P2Hash values) {
		this.source = source;
		this.eventTemplate = eventTemplate;
		visited = new HashSet<P2ManagedObject>();
		set(values);
	}

	/**
	 * gets the OID of the original event template
	 * 
	 * @return the event template's OID
	 */
	public P2Object getEventTemplate() {
		return eventTemplate;
	}

	/**
	 * gets the source OID of this event
	 * 
	 * @return the source OID
	 */
	public P2Object getSource() {
		return source;
	}

	/**
	 * sets and checks the visited status of the current OID. As the event
	 * traverses the managed object tree, each OID is set here. It returns
	 * whether it was already set.
	 * 
	 * @param mo
	 *            the newly visited P2Object
	 * @return true if the P2Object had already been visited
	 */
	public synchronized boolean setVisited(P2ManagedObject mo) {
		return visited.add(mo);
	}

	/**
	 * To be overridden by P2Hash subclasses. Affects whether the hash is copied
	 * remotely or a reference is sent
	 * 
	 * @return true if mutable
	 */
	@Override
	protected boolean isMutable() {
		return false;
	}

	// External stuff

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Hash#writeXml(java.util.Set)
	 */
	@Override
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		TaggedElement xml = super.writeXml(written);
		if (xml.getAttribute("written") != null) {
			return xml;
		}

		TaggedElement element;
		element = new TaggedElement("source");
		element.add(source.writeXml(written));
		xml.add(element);

		element = new TaggedElement("eventTemplate");
		element.add(eventTemplate.writeXml(written));
		xml.add(element);

		for (P2ManagedObject visit : visited) {
			element = new TaggedElement("visited");
			element.add(visit.getP2Object().writeXml(written));
			xml.add(element);
		}

		return xml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.objects.P2Hash#readXml(com.twicom.qdparser.TaggedElement,
	 * java.util.Map)
	 */
	@Override
	public P2Object readXml(TaggedElement xml, Map<Integer, P2Serializable> read)
			throws Ponder2OperationException, Ponder2ArgumentException {
		super.readXml(xml, read);
		visited = new HashSet<P2ManagedObject>();
		for (Element element : xml.getElements()) {
			TaggedElement xmlEntry = (TaggedElement) element;
			if (xmlEntry.getName().equals("source")) {
				source = P2Object.fromXml((TaggedElement) xmlEntry.getChild(0),
						read);
			}
			if (xmlEntry.getName().equals("eventTemplate")) {
				eventTemplate = P2Object.fromXml(
						(TaggedElement) xmlEntry.getChild(0), read);
			}
			if (xmlEntry.getName().equals("visited")) {
				P2Object obj = P2Object.fromXml(
						(TaggedElement) xmlEntry.getChild(0), read);
				visited.add(obj.getManagedObject());
			}
		}
		return this;
	}

}
