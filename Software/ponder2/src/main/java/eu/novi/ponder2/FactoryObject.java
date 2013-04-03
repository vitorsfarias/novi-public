/**
 * Created on Sep 6, 2005
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
 * $Log: FactoryObject.java,v $
 * Revision 1.14  2005/11/18 18:16:57  kpt
 * Added better info for ls -l
 *
 * Revision 1.13  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.12  2005/11/14 13:59:00  kpt
 * Improved results, fixed TickManager
 *
 * Revision 1.11  2005/11/03 04:23:46  kpt
 * More restore and tidying done
 *
 * Revision 1.10  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.9  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.8  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.7  2005/10/22 16:49:46  kpt
 * Returns results through Reply. Shell does rm properly
 *
 * Revision 1.6  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.5  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.4  2005/10/06 10:59:17  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.3  2005/10/01 20:50:52  kpt
 * Changed result type to new Result class
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.SelfManagedCell;

/**
 * This managed object class is used to hold the factory classes for other
 * managed objects and to instantiate new managed objects. This is the only way
 * managed object types get into the system and the only way instances of those
 * types are created (unless they are being restored).
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class FactoryObject implements ManagedObject {

	/**
	 * the factory to create the specialised managed object
	 */
	private Class<P2ObjectAdaptor> adaptorClass;

	/**
	 * creates a managed object that acts as a factory for creating instances of
	 * the underlying managed object
	 * 
	 * @param adaptorClass
	 *            the P2Adaptor class that will be creating the correct object
	 *            type later
	 */
	@SuppressWarnings("unchecked")
	@Ponder2op("create:")
	public FactoryObject(Class<?> adaptorClass) {
		super();
		this.adaptorClass = (Class<P2ObjectAdaptor>) adaptorClass;
	}

	/**
	 * all messages are sent to the Managed Object being created. See the
	 * object's factory documentation for commands accepted.
	 * 
	 * @param op
	 *            the operation to be performed
	 * @param args
	 *            an array of arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	@Ponder2op(Ponder2op.WILDCARD)
	protected P2Object operation_create(P2Object source, String op,
			P2Object... args) throws Ponder2Exception {
		try {
			P2ObjectAdaptor adaptor = adaptorClass.newInstance();
			P2Object value = adaptor.create(source, op, args);
			if (SelfManagedCell.SystemTrace)
				System.out.println("Factory create returns " + value.getOID());
			return value;
		} catch (InstantiationException e) {
			throw new Ponder2OperationException(
					"Factory create: InstantiationException " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new Ponder2OperationException(
					"Factory create: IllegalAccessException " + e.getMessage());
		}
	}

}
