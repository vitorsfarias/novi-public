/**
 * Copyright 2006 Imperial College, London, England.
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
 * Created on Jan 6, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.objects;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;
import eu.novi.ponder2.objects.P2XML;

import com.twicom.qdparser.Element;
import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * Holds a sequence of characters
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2String extends P2Object implements ManagedObject {

	private String string;

	private boolean booleanSet = false;

	private boolean booleanValue = false;

	private boolean intSet = false;

	private BigDecimal intValue;

	/**
	 * constructor for Ponder2 serialisation
	 */
	public P2String() {
		string = null;
	}

	/**
	 * Creates a string with an initial value
	 * 
	 * @param value
	 *            the initial value for this string
	 */
	public P2String(String value) {
		super();
		this.string = value != null ? value : "";
	}

	/**
	 * Returns itself
	 * 
	 * @return the string itself
	 */
	@Override
	@Ponder2op("asString")
	public String asString() throws Ponder2ArgumentException {
		return string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asBoolean()
	 */
	@Override
	public boolean asBoolean() throws Ponder2ArgumentException {
		if (booleanSet)
			return booleanValue;
		booleanValue = P2Boolean.from(string).asBoolean();
		booleanSet = true;
		return booleanValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asNumber()
	 */
	@Override
	public BigDecimal asNumber() throws Ponder2ArgumentException {
		if (intSet)
			return intValue;
		intValue = new P2Number(string).asNumber();
		intSet = true;
		return intValue;
	}

	/**
	 * Answers true if this string is the empty string
	 * 
	 * @return true is this string is empty
	 */
	@Ponder2op("isEmpty")
	public boolean isEmpty() {
		return string == null || string.length() == 0;
	}

	/**
	 * Answers this string and aString concatenated together
	 * 
	 * @param aString
	 *            the string to be concatenated after this
	 * @return this + aString
	 */
	@Ponder2op("+")
	public String add(String aString) {
		return string + aString;
	}

	/**
	 * Answers true if this string and aString are the same
	 * 
	 * @param aString
	 *            the string to be compared with
	 * @return this == aString
	 */
	@Ponder2op("==")
	public boolean equals(String aString) {
		return string.equals(aString);
	}

	/**
	 * Answers true if this string and aString are different
	 * 
	 * @param aString
	 *            the string to be compared with
	 * @return this != aString
	 */
	@Ponder2op("!=")
	public boolean nequals(String aString) {
		return !string.equals(aString);
	}

	/**
	 * Answers aNumber copies of the original string concatenated together
	 * 
	 * @param aNumber
	 *            the number of copies required
	 * @return this string as aNumber copies
	 */
	@Ponder2op("*")
	public String times(int aNumber) {
		StringBuffer buf = new StringBuffer(string.length() * aNumber);
		for (int i = 0; i < aNumber; i++)
			buf.append(string);
		return buf.toString();
	}

	/**
	 * Answers an XML representation of the receiver
	 * 
	 * @return the string as an XML structure
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("asXML")
	public P2XML asXML() throws Ponder2ArgumentException {
		return new P2XML(string);
	}

	/**
	 * Answers the managed object referred to by the pathname in the receiver
	 * 
	 * @return the object found by using this as a pathname
	 * @throws Ponder2Exception
	 */
	@Ponder2op("asObject")
	public P2Object asObject() throws Ponder2Exception {
		return Util.resolve(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#writeXml()
	 */
	@Override
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		TaggedElement xml = super.writeXml(written);
		// Add quotes
		xml.add("\"" + Element.quote(string) + "\"");
		return xml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.objects.P2Object#readXml(com.twicom.qdparser.TaggedElement
	 * )
	 */
	@Override
	public P2Object readXml(TaggedElement xml, Map<Integer, P2Serializable> read) {
		string = Element.unquote(xml.getChild(0).toString());
		// Remove quotes
		string = string.substring(1, string.length() - 1);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof P2String))
			return false;
		return string.equals(((P2String) obj).string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return string.hashCode();
	}

}
