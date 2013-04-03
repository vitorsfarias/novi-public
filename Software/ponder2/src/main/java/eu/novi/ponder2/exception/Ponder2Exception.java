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

package eu.novi.ponder2.exception;

import com.twicom.qdparser.TaggedElement;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class Ponder2Exception extends Exception {

	private TaggedElement xml;
	private String source;

	/**
	 * @param message
	 */
	public Ponder2Exception(String message) {
		super(message);
		this.xml = null;
		this.source = null;
	}

	public void addXML(TaggedElement xml) {
		if (this.xml == null)
			this.xml = xml;
	}

	public void addSource(String source) {
		if (this.source == null && source != null
				&& !source.equals("UnknownFile"))
			this.source = source;
	}

	public String getSource() {
		return source == null ? "unknown file" : source;
	}

	public String getLine() {
		String result = null;
		if (xml != null)
			result = xml.getAttribute("line");
		if (result == null)
			result = "unknown line";
		return result;
	}

	public String getLineInfo() {
		StringBuffer info = new StringBuffer();
		// TODO stack backtrace
		if (xml != null) {
			info.append(getSource());
			info.append(":");
			info.append(getLine());
			info.append(" - ");
		}
		return info.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return getLineInfo() + super.getMessage();// +" - "+xml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return getLineInfo() + super.toString();// +" - "+xml;
	}

}
