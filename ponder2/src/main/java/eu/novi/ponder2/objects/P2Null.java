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

import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * Class representing the null value. Can be used to test for null.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Null extends P2Object {

	public static P2Null Null = new P2Null();

	/**
	 * constructor for Ponder2 serialisation
	 */
	public P2Null() {
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
		return Null;
	}

}
