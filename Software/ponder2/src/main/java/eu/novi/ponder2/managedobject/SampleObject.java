/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
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
 * Created on Mar 8, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.managedobject;

import java.util.HashMap;
import java.util.Map;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.objects.P2Object;

/**
 * Implements a hash or dictionary. This object holds name/object pairs. Objects
 * may be added and removed.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class SampleObject implements ManagedObject {

	private Map<String, P2Object> data;

	/**
	 * Creates an empty hash
	 */
	@Ponder2op("create")
	SampleObject() {
		data = new HashMap<String, P2Object>();
	}

	/**
	 * Creates a hash with a particular minimum size
	 * 
	 * @param size
	 *            the size with which to create the hash
	 */
	@Ponder2op("size:")
	SampleObject(int size) {
		data = new HashMap<String, P2Object>(size);
	}

	/**
	 * add an object to the hash
	 * 
	 * @param name
	 *            the name of the object to be added
	 * @param oid
	 *            the object to be added
	 * @return the object added
	 */
	@Ponder2op("at:put:")
	P2Object p2_operation_at_put(String name, P2Object oid) {
		data.put(name, oid);
		return oid;
	}

	/**
	 * retrieves an object by name
	 * 
	 * @param name
	 *            the name of the object to be found
	 * @return the named object
	 */
	@Ponder2op("at:")
	P2Object p2_operation_at(String name) {
		return data.get(name);
	}

	/**
	 * removes the named object from the hash
	 * 
	 * @param name
	 *            the name of the object to be removed
	 * @return the object removed
	 */
	@Ponder2op("remove:")
	P2Object p2_operation_remove(String name) {
		return data.remove(name);
	}

	/**
	 * get the number of objects in the hash
	 * 
	 * @return the number of objects in the hash
	 */
	@Ponder2op("size")
	int p2_operation_size() {
		return data.size();
	}

}
