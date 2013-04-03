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
 * $Log: Domain.java,v $
 * Revision 1.23  2006/03/15 13:36:42  kpt
 * Lots of testing stuff and new XMLSaver managed object
 *
 * Revision 1.22  2005/11/19 17:26:17  kpt
 * Error messages for add
 *
 * Revision 1.21  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.20  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.19  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.18  2005/11/14 13:59:00  kpt
 * Improved results, fixed TickManager
 *
 * Revision 1.17  2005/11/07 12:05:42  kpt
 * Added Result to the parsing
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
 * Revision 1.13  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.12  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.11  2005/10/22 16:49:46  kpt
 * Returns results through Reply. Shell does rm properly
 *
 * Revision 1.10  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.9  2005/10/21 17:15:47  kpt
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
 * Revision 1.4  2005/09/21 15:54:56  kpt
 * USE now works through external domains
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

package eu.novi.ponder2;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.Util;

/**
 * Domain is the basic managed object for Ponder2 that supports hierarchies.
 * View it like a directory or a folder in a filesystem
 * 
 * @author Kevin Twidle
 * @version $Id: Domain.java,v 1.23 2006/03/15 13:36:42 kpt Exp $
 */
public class Domain implements ManagedObject {

	/**
	 * the managed objects belonging to this domain
	 */
	private final Map<String, P2ManagedObject> managedObjects;

	private P2Object myP2Object;

	/**
	 * creates a new Domain managed object
	 * 
	 */
	@Ponder2op("create")
	protected Domain(P2Object myP2Object) {
		this.myP2Object = myP2Object;
		myP2Object.getOID().setDomain(true);
		managedObjects = new HashMap<String, P2ManagedObject>();
	}

	/**
	 * adds a new managed object to this domain
	 * 
	 * @param name
	 *            the name of the new object to be added
	 * @param mo
	 *            the OID of the object to be added
	 */
	protected void add(String name, P2ManagedObject mo) {
		mo.addParent(myP2Object.getManagedObject());
		managedObjects.put(name, mo);
		// sendEvent("/event/domain/add", this.myOID.toXML(), new
		// TextElement(name), oid.toXML());
		// try {
		// Event event = new Event(P2Object.create(myOID), "/event/domain/add",
		// P2Object.create(name),
		// P2Object.create(oid));
		// myOID.sendEvent(event);
		// }
		// catch (Ponder2ResolveException e) {
		// }
		// catch (Ponder2ArgumentException e) {
		// }
		// catch (Ponder2OperationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * removes a managed object from a domain
	 * 
	 * @param name
	 *            the name of the object to be removed
	 * @throws Ponder2Exception
	 */
	protected P2ManagedObject remove(String name) throws Ponder2Exception {
		P2ManagedObject mo = managedObjects.remove(name);
		// The oid may be in this domain more than once under different names
		if (mo != null && !managedObjects.containsValue(mo)) {
			mo.removeParent(myP2Object.getManagedObject());
			// // sendEvent("/event/domain/remove", this.myOID.toXML(), new
			// // TextElement(name), oid.toXML());
			// try {
			// Event event = new Event(myP2Object, "/event/domain/remove",
			// P2Object.create(name), P2Object
			// .create(mo));
			// myP2Object.getManagedObject().sendEvent(event);
			// }
			// catch (Ponder2ResolveException e) {
			// }
			// catch (Ponder2ArgumentException e) {
			// }
			// catch (Ponder2OperationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		return mo;
	}

	/**
	 * gets the P2ManagedObject of a named managed object
	 * 
	 * @param name
	 *            the name of the object to be located
	 * @return the P2ManagedObject of the named object or null if not found
	 */
	public P2ManagedObject get(String name) {
		return managedObjects.get(name);
	}

	/**
	 * returns true if the named object exists within the domain
	 * 
	 * @param aName
	 *            the name of the object to be located
	 * @return true if the object is found
	 */
	@Ponder2op("has:")
	public boolean contains(String aName) {
		return managedObjects.containsKey(aName);
	}

	/**
	 * returns true if anObject exists within the domain
	 * 
	 * @param anObject
	 *            the object to be located
	 * @return true if the object is found
	 */
	@Ponder2op("hasObject:")
	public boolean contains(P2Object anObject) {
		return managedObjects.containsValue(anObject.getManagedObject());
	}

	/**
	 * Answer the number of elements in the receiver.
	 * 
	 * @return the number of elements held
	 */
	@Ponder2op("size")
	public int size() {
		return managedObjects.size();
	}

	/**
	 * returns an array of the names of the objects in this domain
	 * 
	 * @return a String array with the object names
	 */
	protected String[] names() {
		int size = managedObjects.size();
		return managedObjects.keySet().toArray(new String[size]);
	}

	// Ponder2 operations

	/**
	 * answers an array containing the names of all the domain's entries.
	 * 
	 * @return a Ponder2 array with the object names
	 */
	@Ponder2op("listNames")
	protected P2Object operation_listNames() {
		String[] names = names();
		P2Object[] values = new P2Object[names.length];
		for (int i = 0; i < names.length; i++) {
			values[i] = new P2String(names[i]);
		}
		return new P2Array(values);
	}

	/**
	 * answers an array of Managed Object names containing all the entries in
	 * the domain.
	 * 
	 * @return an array containing all the object in this domain
	 */
	@Ponder2op("listObjects")
	protected P2Object operation_listObjects() {
		Collection<P2ManagedObject> mos = managedObjects.values();
		P2Object[] values = new P2Object[mos.size()];
		int i = 0;
		for (Iterator<P2ManagedObject> iter = mos.iterator(); iter.hasNext();) {
			P2ManagedObject mo = iter.next();
			values[i++] = mo.getP2Object();
		}
		return new P2Array(values);
	}

	/**
	 * Returns a hash containing all the entries in the domain as name->object
	 * pairs
	 * 
	 * @return a hash containing all the object in this domain as name,object
	 *         pairs
	 */
	@Ponder2op("asHash")
	protected P2Hash operation_asHash() {
		Map<String, P2Object> hash = new HashMap<String, P2Object>();
		for (Map.Entry<String, P2ManagedObject> entry : managedObjects
				.entrySet()) {
			hash.put(entry.getKey(), entry.getValue().getP2Object());
		}
		return new P2Hash(hash);
	}

	/**
	 * answers the P2Object of the Managed Object at aName. Answers NIL if the
	 * object does not exist.
	 * 
	 * @param aName
	 *            the name of the object to be returned
	 * @return the object located by aName
	 */
	@Ponder2op("at:")
	protected P2Object operation_at(String aName) {
		P2ManagedObject mo = get(aName);
		return mo != null ? mo.getP2Object() : P2Object.create();
	}

	/**
	 * Answer the P2Object associated with aName. If not found evaluate aBlock
	 * (with no arguments) and return its result
	 * 
	 * @param aName
	 *            the name of the object to be returned
	 * @param aBlock
	 *            a block to be executed if aName is not found
	 * @return the named object or the result of executing the block
	 * @throws Ponder2Exception
	 */
	@Ponder2op("at:ifAbsent:")
	protected P2Object operation_at(P2Object source, String aName,
			P2Block aBlock) throws Ponder2Exception {
		return operation_asHash().operation_at_ifAbsent(source, aName, aBlock);
	}

	/**
	 * add anOid into the domain with aName. Answers anOid.
	 * 
	 * @param aName
	 *            the name to be used
	 * @param p2Object
	 *            the object to be added
	 * @return the object added to the domain
	 */
	@Ponder2op("at:put:")
	protected P2Object operation_at_add(String aName, P2Object p2Object) {
		add(aName, p2Object.getManagedObject());
		return p2Object;
	}

	/**
	 * Calls aBlock with name/value pairs for each entry in the domain. Answers
	 * with the the receiver
	 * 
	 * @param aBlock
	 *            the block to be executed. It must take up to two arguments,
	 *            the first being name and the second being value
	 * @throws Ponder2Exception
	 */
	@Ponder2op("do:")
	protected void operation_do(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		operation_asHash().operation_do(source, aBlock);
	}

	/**
	 * Calls aBlock with name/value pairs for each entry in the domain. Answers
	 * with an array with all the answers from the executions
	 * 
	 * @param aBlock
	 *            the block to be executed
	 * @return an array with the results of the block executions
	 * @throws Ponder2Exception
	 */
	@Ponder2op("collect:")
	protected P2Array operation_collect(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return operation_asHash().operation_collect(source, aBlock);
	}

	/**
	 * Answers with the object from aPath relative to this domain
	 * 
	 * @param aPath
	 *            the relative path to be resolved
	 * @return the object found at the path location
	 * @throws Ponder2Exception
	 */
	@Ponder2op("resolve:")
	protected P2Object operation_resolve(String aPath) throws Ponder2Exception {
		return Util.resolve(myP2Object, aPath);
	}

	/**
	 * Removes aName and its ManagedObject from the domain. Answers the object
	 * removed. TODO Error checks?
	 * 
	 * @param aName
	 *            the name of the object to be removed
	 * @return the object removed or <code>nil</code>
	 * @throws Ponder2Exception
	 */
	@Ponder2op("remove:")
	protected P2Object operation_remove(String aName) throws Ponder2Exception {
		P2ManagedObject mo = remove(aName);
		return mo != null ? mo.getP2Object() : P2Object.create();
	}

	/**
	 * Removes anObject from the receiver. All copies of anObject will be
	 * removed. Answers true if one or more were removed.
	 * 
	 * @param anObject
	 *            the object to be removed
	 * @return true if an object was removed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("removeObject:")
	protected boolean removeObject(P2Object anObject) throws Ponder2Exception {
		boolean result = false;
		// The object may be here more than once
		Set<Entry<String, P2ManagedObject>> entrySet = new HashSet<Entry<String, P2ManagedObject>>(
				managedObjects.entrySet());
		for (Entry<String, P2ManagedObject> entry : entrySet) {
			if (entry.getValue().getP2Object().equals(anObject)) {
				remove(entry.getKey());
				result = true;
			}
		}
		return result;
	}

	/**
	 * Removes all objects from the domain. Answers the receiver.
	 * 
	 * @throws Ponder2Exception
	 */
	@Ponder2op("removeAll")
	protected void removeAll() throws Ponder2Exception {
		Set<String> names = new HashSet<String>(managedObjects.keySet());
		for (String name : names)
			remove(name);
	}

}
