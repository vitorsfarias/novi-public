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
 * Created on Jan 10, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.objects;

import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * Object containing error details created when an error is thrown in the code.
 * Attributes include an error message and the PonderTalk source file name, line
 * number and character number.
 * <p>
 * This is the argument received by the block onError: message.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Error extends P2Object implements ManagedObject {

	private String message;
	private String source;
	private String line;
	private String character;

	/**
	 * constructor for Ponder2 serialisation
	 */
	protected P2Error(Ponder2Exception exception) {
		message = exception.getMessage();
		source = exception.getSource();
		String linechar = exception.getLine();
		int colon = linechar.indexOf(':', 0);
		if (colon >= 0) {
			line = linechar.substring(0, colon);
			character = linechar.substring(colon + 1);
		} else {
			line = linechar;
			character = "";
		}
	}

	/**
	 * Returns the source file name
	 * 
	 * @return the source file name
	 */
	@Ponder2op("source")
	protected String source() {
		return source;
	}

	/**
	 * returns the source line number
	 * 
	 * @return the source line number
	 */
	@Ponder2op("line")
	protected String line() {
		return line;
	}

	/**
	 * returns the source character number
	 * 
	 * @return the source character number
	 */
	@Ponder2op("char")
	protected String character() {
		return character;
	}

	/**
	 * returns the error message
	 * 
	 * @return the error message
	 */
	@Ponder2op("message")
	protected String message() {
		return message;
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
		xml.setAttribute("source", source);
		xml.setAttribute("line", line);
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
	public P2Object readXml(TaggedElement xml, Map<Integer, P2Serializable> read)
			throws Ponder2OperationException {
		source = xml.getAttribute("value");
		line = xml.getAttribute("line");
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return source + ":" + line + ":" + character + ": " + message;
	}
}
