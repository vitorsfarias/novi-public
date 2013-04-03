/**
 * Created on Aug 18, 2005
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
 * $Log: ArgumentList.java,v $
 * Revision 1.6  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.5  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.4  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.3  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.2  2005/09/22 08:38:01  kpt
 * Fixes for demo
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.util.Vector;

//import eu.novi.ponder2.Entry;

import eu.novi.ponder2.ArgumentList;

/**
 * Simple class to manage argument handling for Events and Policies
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class ArgumentList extends Vector<ArgumentList.Entry> {

	/**
	 * adds a named argument to the list
	 * 
	 * @param name
	 *            name of string type argument
	 */
	public void add(String name) {
		add(new Entry(name, null));
	}

	/**
	 * get a named argument from the list
	 * 
	 * @param name
	 *            the name of the argument to retreive
	 * @return a name, type pair if found else null
	 */
	public Entry getArg(String name) {
		for (Entry entry : this) {
			if (entry.name.equals(name))
				return entry;
		}
		return null;
	}

	/**
	 * The entries for the argument list. Each entry comprises the name and the
	 * type
	 * 
	 * @author Kevin Twidle
	 * @version $Id:$
	 */
	public class Entry implements Comparable<Entry> {

		/**
		 * The argument name
		 */
		String name;
		/**
		 * The arguement type
		 */
		String type;

		/**
		 * creats an entry for the argument list
		 * 
		 * @param name
		 *            the argument name
		 * @param type
		 *            the argument type
		 */
		public Entry(String name, String type) {
			this.name = name;
			if (type == null)
				type = "string";
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry other) {
			int value = name.compareTo(other.name);
			if (value == 0)
				value = type.compareTo(other.type);
			return value;
		}
	}
}