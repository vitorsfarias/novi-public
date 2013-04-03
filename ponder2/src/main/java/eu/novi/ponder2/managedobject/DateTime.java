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
 * Created on Aug 10, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.managedobject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;


import eu.novi.ponder2.managedobject.DateTime;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class DateTime extends P2Object implements ManagedObject {

	Date date;

	@Ponder2op("create")
	DateTime() {
		date = new Date();
	}

	@Ponder2op("create:")
	DateTime(String aString) throws Ponder2ArgumentException {
		try {
			date = DateFormat.getDateInstance().parse(aString);
		} catch (ParseException e) {
			throw new Ponder2ArgumentException("Bad date format "
					+ e.getMessage());
		}
	}

	DateTime(long secs) {
		date = new Date(secs);
	}

	@Ponder2op("secs")
	public String operation_secs() {
		return "" + date.getTime();
	}

	@Ponder2op("-")
	public P2Object operation_minus(P2Object source, P2Object aDateTime)
			throws Ponder2Exception {
		long secs = Long.parseLong(aDateTime.operation(source, "secs")
				.asString());
		return new DateTime(date.getTime() - secs);
	}

	@Ponder2op("print")
	public String operation_print() {
		return date.toString();
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
			throws Ponder2OperationException, Ponder2ArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
}
