/**
 * Created on Aug 16, 2005
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
 * $Log: EventTemplate.java,v $
 * Revision 1.16  2005/12/09 10:34:38  kpt
 * Policy actions sorted out
 *
 * Revision 1.15  2005/11/23 14:47:08  kpt
 * Removed Iterable from QDParser.
 *
 * Revision 1.14  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.13  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.12  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.11  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.10  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.9  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.8  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.7  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.6  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.5  2005/10/06 10:59:18  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.4  2005/10/01 20:50:53  kpt
 * Changed result type to new Result class
 *
 * Revision 1.3  2005/09/21 13:46:40  kpt
 * Can now create and send events using XML and the event templates
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.ArgumentList;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.SelfManagedCell;

/**
 * Managed object to act as a template for notifications (events). Each instance
 * of the template contains a set of named arguments. These argument names can
 * be retrieved for checking against Policies that expect to be activated by the
 * events. When an event is created using this template the named arguments are
 * packed into a map and sent around the system inside the event.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class EventTemplate implements ManagedObject {

	protected ArgumentList argList;

	public static String defaultDomain = SelfManagedCell.EventDomain;

	private P2Object myP2Object;

	/**
	 * creates an empty template. Further messages must be sent to give the
	 * named arguments.
	 * 
	 * @param myP2Object
	 */
	@Ponder2op("create")
	protected EventTemplate(P2Object myP2Object) {
		argList = new ArgumentList();
		this.myP2Object = myP2Object;
	}

	/**
	 * creates an event template with anArray containing the argument names.
	 * 
	 * @param myP2Object
	 * @param anArray
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("create:")
	protected EventTemplate(P2Object myP2Object, P2Array anArray)
			throws Ponder2ArgumentException, Ponder2OperationException {
		this(myP2Object);
		P2Object[] argNames = anArray.asArray();
		for (int i = 0; i < argNames.length; i++) {
			argList.add(argNames[i].asString());
		}
	}

	/**
	 * INTERNAL OPERATION. Takes a list of objects and packages them into a hash
	 * indexed with the argument names. Answers the new hash.
	 * 
	 * @param args
	 *            the arguments to be taken as values
	 * @return a hash containing named values from args
	 * @throws Ponder2ArgumentException
	 */
	public P2Hash packageArgs(P2Object... args) throws Ponder2ArgumentException {
		P2Hash value = new P2Hash();
		if (argList.size() > args.length)
			throw new Ponder2ArgumentException(
					"too few arguments for event, got " + args.length
							+ " wanted " + argList.size());
		int count = 0;
		for (ArgumentList.Entry entry : argList) {
			value.operation_at_put(entry.name, args[count]);
			count++;
		}
		return value;
	}

	@Ponder2op("packageArgs:")
	public P2Hash packageArgs(P2Array anArray) throws Ponder2ArgumentException {
		return packageArgs(anArray.asArray());
	}

	public P2Hash packageHash(P2Hash args) throws Ponder2ArgumentException {
		P2Hash value = new P2Hash();
		for (ArgumentList.Entry entry : argList) {
			P2Object arg = args.get(entry.name);
			if (arg == null)
				throw new Ponder2ArgumentException(
						"EventTemplate: missing argument  " + entry.name);
			value.operation_at_put(entry.name, arg);
		}
		return value;
	}

	/**
	 * adds an argument field to this event definition. Answers the receiver
	 * 
	 * @param arg
	 *            the name of the argument to add
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("arg:")
	protected void operation_arg(String arg) throws Ponder2ArgumentException {
		argList.add(arg);
	}

	/**
	 * adds anArray of argument fields to this event template. Answers the
	 * receiver.
	 * 
	 * @param anArray
	 *            the names of the arguments to become attributes of the event
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("args:")
	protected void operation_args(P2Object anArray)
			throws Ponder2ArgumentException, Ponder2OperationException {
		P2Object[] strings = anArray.asArray();
		for (int i = 0; i < strings.length; i++) {
			argList.add(strings[i].asString());
		}
	}

	/**
	 * creates and sends an event of this type with no values. Answers the
	 * receiver
	 * 
	 * @param source
	 *            the ID of the object initiating the event
	 * @throws Ponder2Exception
	 */
	@Ponder2op("create")
	protected void operation_create(P2Object source) throws Ponder2Exception {
		Event event = new Event(source, myP2Object, new P2Array());
		source.getManagedObject().sendEvent(event);
	}

	/**
	 * creates and sends an event of this type with values from anArray. Answers
	 * the receiver
	 * 
	 * @param source
	 *            the ID of the object initiating the event
	 * @param anArray
	 *            the values to be filled in as attributes of the event
	 * @throws Ponder2Exception
	 */
	@Ponder2op("create:")
	protected void operation_create(P2Object source, P2Array anArray)
			throws Ponder2Exception {
		Event event = new Event(source, myP2Object, anArray);
		source.getManagedObject().sendEvent(event);
	}

	/**
	 * creates and sends an event of this type with values from aHash. Answers
	 * the receiver
	 * 
	 * @param source
	 *            the ID of the object initiating the event
	 * @param aHash
	 *            the named values to be filled in as attributes of the event
	 * @throws Ponder2Exception
	 */
	@Ponder2op("fromHash:")
	protected void operation_create(P2Object source, P2Hash aHash)
			throws Ponder2Exception {
		Event event = new Event(source, myP2Object, packageHash(aHash));
		source.getManagedObject().sendEvent(event);
	}

}
