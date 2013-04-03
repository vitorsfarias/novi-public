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
 * Created on Feb 14, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2.mission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
//import eu.novi.ponder2.mission.Command;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Object;

/**
 * Provides an interface to remote systems. Commands may be added to this
 * managed object which are mapped into commands to other, local, managed
 * objects. Objects may also be exported by this interface in which case they
 * are addressed in the normal manner using pathnames through the interface or
 * by using the command "at:"
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Interface implements ManagedObject {

	/**
	 * List of events handles by this interface
	 */
	Map<String, P2Object> events;
	/**
	 * List of accepted events
	 */
	Set<String> acceptsEvents;
	/**
	 * List of events exported by this interface
	 */
	Set<String> providesEvents;
	/**
	 * List of proxy commands exposed by this Interface
	 */
	Map<String, Command> commands;
	/**
	 * List of objects exposed through this Interface
	 */
	Map<String, P2Object> objects;
	/**
	 * The internal domain holding events
	 */
	P2Object eventDomain;

	Set<P2Object> eventSubscribers;
	private P2Object myP2Object;
	private P2Object eventForwarder;
	private static P2Object eventForwarderFactory = null;

	boolean dafaultListen = true;

	/**
	 * Creates a new Interface object
	 * 
	 * @throws Ponder2Exception
	 */
	@Ponder2op("create")
	Interface(P2Object myP2Object) throws Ponder2Exception {
		this.myP2Object = myP2Object;
		events = new HashMap<String, P2Object>();
		acceptsEvents = new HashSet<String>();
		providesEvents = new HashSet<String>();
		objects = new HashMap<String, P2Object>();
		commands = new HashMap<String, Command>();
		eventSubscribers = new HashSet<P2Object>();

		// Put in the default Mission Controller
		try {
			objects.put("missioncontroller",
					Util.resolve("root/mission/missioncontroller"));
		} catch (Ponder2OperationException e) {
			System.out
					.println("Warning: Interface, no default mission controller found");
		}

		// Get a domain for the events and add it as an object
		P2Object domainFactory = SelfManagedCell.RootDomain.operation(
				myP2Object, "load:", P2Object.create("Domain"));
		eventDomain = domainFactory.operation(myP2Object, "create");
		mapToObject("event", eventDomain);

		// Sort out an Event Forwarder
		if (eventForwarderFactory == null) {
			eventForwarderFactory = SelfManagedCell.RootDomain.operation(
					myP2Object, "load:", P2Object.create("EventForwarder"));
		}
		eventForwarder = eventForwarderFactory.operation(myP2Object, "create");
		eventForwarder.operation(myP2Object, "active:", P2Object.create(true));

		// Set up default listener
		eventForwarder.operation(myP2Object, "attachTo:",
				SelfManagedCell.RootDomain);

	}

	@Ponder2op("listenTo:")
	void listenTo(P2Object anObject) throws Ponder2Exception {
		eventForwarder.operation(myP2Object, "attachTo:", anObject);
	}

	/**
	 * Set the Interface's mission controller to be aMissionController.
	 * 
	 * @param aMissionController
	 */
	@Ponder2op("missionController:")
	void setMissionController(P2Object aMissionController) {
		objects.put("missioncontroller", aMissionController);
	}

	/**
	 * Maps aName to anEventType. This name may be used in the
	 * <b>acceptsEvent:</b> or <b>sendsEvent:</b> Interface commands.
	 * <p>
	 * Events appear as objects in the interface under the pseudo domain
	 * <i>event</i>. Thus they may be accessed as
	 * <b>interface/event/eventName</b>
	 * 
	 * @param aName
	 * @param anEventType
	 * @throws Ponder2Exception
	 */
	@Ponder2op("event:is:")
	void eventIs(String aName, P2Object anEventType) throws Ponder2Exception {
		events.put(aName, anEventType);
	}

	/**
	 * Makes sure that the event is mapped and the event is included in the
	 * interface's event domain
	 * 
	 * @param aName
	 * @throws Ponder2Exception
	 */
	private void installEvent(String aName) throws Ponder2Exception {
		if (!events.containsKey(aName)) {
			events.put(aName, Util.resolve("root/event/" + aName));
		}
		P2Object anEventType = events.get(aName);
		eventDomain.operation(myP2Object, "at:put:", P2Object.create(aName),
				anEventType);
	}

	/**
	 * Tells the Interface that it can accept events previously defined with
	 * anEventName
	 * 
	 * @param anEventName
	 * @throws Ponder2Exception
	 */
	@Ponder2op("acceptsEvent:")
	void acceptsEvents(String anEventName) throws Ponder2Exception {
		installEvent(anEventName);
		acceptsEvents(anEventName, SelfManagedCell.RootDomain);
	}

	/**
	 * Tells the Interface that it can accept events previously defined with
	 * anEventName
	 * 
	 * @param anEventName
	 * @throws Ponder2Exception
	 */
	@Ponder2op("acceptsEvent:from:")
	void acceptsEvents(String anEventName, P2Object anObject)
			throws Ponder2Exception {
		installEvent(anEventName);
		acceptsEvents.add(anEventName);
	}

	@Ponder2op("raise:")
	void raise(P2Object source, String eventName) throws Ponder2Exception {
		if (acceptsEvents.contains(eventName)) {
			P2Object event = events.get(eventName);
			event.operation(myP2Object, "create");
		} else
			throw new Ponder2ArgumentException("Interface: unknown event "
					+ eventName);
	}
	@Ponder2op("raise:with:")
	  void raise(P2Object source, String eventName, P2Array anArray) throws Ponder2Exception {
	    if (acceptsEvents.contains(eventName)) {
	      P2Object event = events.get(eventName);
	      event.operation(myP2Object, "create:", anArray);
	    }
	    else
	      throw new Ponder2ArgumentException("Interface: unknown event " + eventName);
	  }
	/**
	 * Tells the Interface that it can propagate events previously defined with
	 * anEventName
	 * 
	 * @param anEventName
	 * @throws Ponder2Exception
	 */
	@Ponder2op("providesEvent:")
	void providesEvent(String anEventName) throws Ponder2Exception {
		installEvent(anEventName);
		providesEvents.add(anEventName);
	}

	@Ponder2op("subscribe:")
	void subscriberTo(P2Object anObject) throws Ponder2Exception {
		eventForwarder.operation(myP2Object, "forwardTo:", anObject);
	}

	/**
	 * Maps aCommand to anObject. aCommand appears as part of the external
	 * interface of this Interface. Answers with the result of the command
	 * 
	 * @param aCommand
	 *            the command to be recognised
	 * @param anObject
	 *            the destination managed object to receive the command
	 */
	@Ponder2op("map:to:")
	void mapTo(String aCommand, P2Object anObject) {
		mapToAs(aCommand, anObject, aCommand);
	}

	/**
	 * Maps aCommand to anObject as anotherCommand. aCommand appears as part of
	 * the external interface of this Interface. Answers with the result of the
	 * command
	 * 
	 * @param aCommand
	 *            the command to be recognised
	 * @param anObject
	 *            the destination managed object to receive the command
	 * @param anotherCommand
	 *            the actual command to sent to anObject
	 */
	@Ponder2op("map:to:as:")
	void mapToAs(String aCommand, P2Object anObject, String anotherCommand) {
		commands.put(aCommand, new Command(anObject, anotherCommand));
	}

	/**
	 * Exports anObject with aName. The Interface appears to be a domain with
	 * aName inside it
	 * 
	 * @param aName
	 *            the name of the object to export
	 * @param anObject
	 *            the object to be exported
	 */
	@Ponder2op("map:toObject:")
	void mapToObject(String aName, P2Object anObject) {
		objects.put(aName, anObject);
	}

	// Commands used by the system to implement the interface

	/**
	 * Returns the exported object called aName
	 * 
	 * @param aName
	 *            the name of the required object
	 * @return the exported object
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("at:")
	P2Object at(String aName) throws Ponder2OperationException {
		P2Object object;
		object = objects.get(aName);
		if (object == null)
			throw new Ponder2OperationException("Interface: object \"" + aName
					+ "\" not found");
		return object;
	}

	/**
	 * For internal use. Maps any command to the correct object and command.
	 * Answers with the answer produced by executing the command. Throws an
	 * error if the command is not found.
	 * 
	 * @param source
	 *            the source object that generated the command
	 * @param op
	 *            the operation (command) to be performed
	 * @param args
	 *            the arguments for the command
	 * @return the result of the command
	 * @throws Ponder2Exception
	 *             if the command is not found
	 */
	@Ponder2op(Ponder2op.WILDCARD)
	protected P2Object obj_operation(P2Object source, String op,
			P2Object... args) throws Ponder2Exception {
		// We have to send something to an object
		Command command = commands.get(op);
		if (command == null)
			throw new Ponder2OperationException(
					"Interface: Received bad operation " + op);
		return command.operation(source, args);
	}

	class Command {

		P2Object object;
		String command;

		/**
		 * @param object
		 * @param command
		 */
		public Command(P2Object object, String command) {
			this.object = object;
			this.command = command;
		}

		P2Object operation(P2Object source, P2Object... args)
				throws Ponder2Exception {
			return object.operation(source, command, args);
		}

	}

}
