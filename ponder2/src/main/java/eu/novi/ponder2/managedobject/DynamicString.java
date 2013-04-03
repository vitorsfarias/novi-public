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
 * Created on Oct 9, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.managedobject;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;

/**
 * Implements a dynamic string. This string can be appended to and otherwise
 * used as a String.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class DynamicString implements ManagedObject {

	private StringBuffer buffer;

	/**
	 * Creates a new Dynamic String
	 */
	@Ponder2op("create")
	public DynamicString() {
		buffer = new StringBuffer();
	}

	/**
	 * Creates a new Dynamic String initialised with aString
	 * 
	 * @param aString
	 *            the string to initialise the DynamicString
	 */
	@Ponder2op("create:")
	public DynamicString(String aString) {
		buffer = new StringBuffer(aString);
	}

	/**
	 * Appends anObject to the receiver. Answers anObject.
	 * 
	 * @param aString
	 *            the string to be appended
	 */
	@Ponder2op("add:")
	public void add(String aString) {
		buffer.append(aString);
	}

	/**
	 * Appends a newline sequence to the receiver.
	 */
	@Ponder2op("cr")
	public void add_cr() {
		buffer.append("\r\n");
	}

	/**
	 * Appends a tab character to the receiver.
	 */
	@Ponder2op("tab")
	public void add_tab() {
		buffer.append('\t');
	}

	/**
	 * Returns the receiver as a String
	 * 
	 * @return the value of this object as a string
	 */
	@Override
	@Ponder2op("asString")
	public String toString() {
		return buffer.toString();
	}
}
