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

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.StartStopPonder2SMC;
import eu.novi.ponder2.exception.Ponder2Exception;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Ponder2OperationException extends Ponder2Exception {
	
	private static final transient Logger log = LoggerFactory.getLogger(Ponder2OperationException.class);
	public Ponder2OperationException(String message) {
		super(message);
		log.info(message);
		
	}

}
