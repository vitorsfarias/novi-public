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

package eu.novi.ponder2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.novi.ponder2.ExternalManagedObjectP2Adaptor;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.SelfManagedCell;

import com.twicom.qdparser.TaggedElement;

//import eu.novi.ponder2.CreateOperation;
//import eu.novi.ponder2.CreateOrStaticOperation;
//import eu.novi.ponder2.InstanceOperation;
//import eu.novi.ponder2.StaticOperation;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.EventListener;

/**
 * An object adaptor manages the interface between the ponder2 system and the
 * actual managed object itself
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2ObjectAdaptor extends P2Object implements Externalizable {

	/**
	 * The instance of the managed object to be called
	 */
	protected ManagedObject objImpl = null;
	private static AuthorisationModule authorisationModule = null;

	/**
	 * Creates an empty object adaptor. Used only by factory object to create a
	 * new object. The create call must be made afterwards. See the other
	 * constructor.
	 */
	public P2ObjectAdaptor() {
	}

	public P2ObjectAdaptor(P2Object source, String operation, P2Object... args)
			throws Ponder2Exception {
		create(source, operation, args);
	}

	public void setObj(ManagedObject obj) {
		this.objImpl = obj;
	}

	public ManagedObject getObj() {
		return objImpl;
	}

	/**
	 * Turns authorisation checking on by setting the authorisation module that
	 * will be checking commands. If an authorisation is blocked then an error
	 * will be thrown.
	 * 
	 * @param auth
	 *            the authorisation module that will perform the checks
	 */
	public static void setAuthorisation(AuthorisationModule auth) {
		authorisationModule = auth;
	}

	/**
	 * Returns the authorisation module in use, if any.
	 * 
	 * @return the current authorisation module
	 */
	public static AuthorisationModule getAuthorisation() {
		return authorisationModule;
	}

	/**
	 * called as a result of an operation on the factory managed object for this
	 * managed object
	 * 
	 * @param source
	 *            the subject OID of the operation
	 * @param operation
	 *            the name of the operation
	 * @param args
	 *            arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	@Override
	// public P2Object create(P2Object source, String operation, P2Object...
	// args)
	// throws Ponder2Exception {
	// CreateOperation op = getCreateOperation(operation);
	// if (SelfManagedCell.SystemTrace)
	// trace("Create", this.getClass().getSimpleName(), operation, args);
	// objImpl = op.call(this, source, operation, args);
	// return this;
	// }
	public P2Object create(P2Object source, String operation, P2Object... args)
			throws Ponder2Exception {
		CreateOrStaticOperation opInfo = new CreateOrStaticOperation(operation);
		getCreateOrStaticOperation(opInfo);
		if (SelfManagedCell.SystemTrace)
			trace("Create/Static call: ", this.getClass().getSimpleName(),
					operation, args);
		return opInfo.call(this, source, operation, args);
	}

	/**
	 * called as a result of an operation on the instantiated managed object
	 * 
	 * @param source
	 *            the subject OID of the operation
	 * @param operation
	 *            the name of the operation
	 * @param args
	 *            arguments for the operation
	 * @return the result of the operation
	 * @throws Ponder2Exception
	 */
	@Override
	public P2Object operation(P2Object source, String operation,
			P2Object... args) throws Ponder2Exception {
		InstanceOperation op = getInstanceOperation(operation);
		if (SelfManagedCell.SystemTrace)
			trace("Operation", this.getClass().getSimpleName(), operation, args);

		AuthPolicyHolder holder = null;

		boolean checkAuth = authorisationModule != null
				&& getManagedObject().getParentSet() != null
				&& !getManagedObject().getParentSet().isEmpty();

		if (checkAuth) { // && (source != null)) {
			holder = new AuthPolicyHolder();
			authorisationModule.request(AuthorisationModule.PEP1, 's', holder,
					source, this, operation, args);
			if (!(objImpl instanceof ExternalManagedObjectP2Adaptor))
				authorisationModule.request(AuthorisationModule.PEP2, 't',
						holder, source, this, operation, args);
		}
		P2Object value = op.call(this, objImpl, source, operation, args);
		if (SelfManagedCell.SystemTrace)
			if (value != null)
				System.err.println(" => " + value);
			else
				System.err.println(" => *** Error: return value is null ***");
		if (checkAuth) {
			if (!(objImpl instanceof ExternalManagedObjectP2Adaptor))
				authorisationModule.reply(AuthorisationModule.PEP3, 't',
						holder, source, this, operation, args, value);
			authorisationModule.reply(AuthorisationModule.PEP4, 's', holder,
					source, this, operation, args, value);
		}
		return value;
	}

	/**
	 * Prints a command trace line on stderr.
	 * 
	 * @param opType
	 *            the type of the operation, mainly "P2Op"
	 * @param className
	 *            the class receiving the operation
	 * @param operation
	 *            the name of the operation
	 * @param args
	 *            the arguments for the operation
	 */
	protected static void trace(String opType, String className,
			String operation, P2Object... args) {
		if (className.endsWith("P2Adaptor"))
			className = className.substring(0, className.length() - 9);
		System.err.print(opType + ": " + className + " " + operation + " ");
		for (int i = 0; i < args.length; i++) {
			P2Object value = args[i];
			System.err.print(value + " ");
		}
		System.err.println();
	}

	public void getCreateOrStaticOperation(CreateOrStaticOperation opInfo)
			throws Ponder2OperationException {
		throw new Ponder2OperationException("Object "
				+ this.getClass().getSimpleName()
				+ " unknown constructor or static operation: '" + opInfo.opName
				+ "'");
	}

	public CreateOperation getCreateOperation(String opName)
			throws Ponder2OperationException {
		throw new Ponder2OperationException("Object "
				+ this.getClass().getSimpleName() + " unknown constructor '"
				+ opName + "'");
	}

	/**
	 * The map of instance operations to methods
	 */
	private final static Map<String, InstanceOperation> operation;

	// Create the default call tables when the class is loaded
	static {
		operation = new HashMap<String, InstanceOperation>();

		// Ponder2 call to add a remote event listener
		operation.put("Ponder2.attach:", new InstanceOperation() {

			@Override
			public P2Object call(P2Object thisObj, ManagedObject obj,
					P2Object source, String operation, P2Object... args)
					throws Ponder2Exception {
				P2Object object = args[0];
				if (object instanceof P2ObjectAdaptor) {
					P2ObjectAdaptor listener = (P2ObjectAdaptor) object;
					if (listener.getObj() instanceof EventListener) {
						thisObj.getManagedObject().applyPolicy(
								(EventListener) listener.getObj());
					} else if (listener instanceof ExternalManagedObjectP2Adaptor) {
						thisObj.getManagedObject().attachRemotePolicy(listener);
					} else
						throw new Ponder2ArgumentException(
								"Ponder2.attach: Can only attach an Event Listener or External Managed Object");
				}
				return thisObj;
			}
		});

		// Ponder2 call to inject an event
		operation.put("Ponder2.event:", new InstanceOperation() {

			@Override
			public P2Object call(P2Object thisObj, ManagedObject obj,
					P2Object source, String operation, P2Object... args)
					throws Ponder2Exception {
				P2Object event = args[0];
				if (event instanceof Event) {
					thisObj.getManagedObject().sendEvent((Event) event);
				} else
					throw new Ponder2ArgumentException(
							"Ponder2.event: Object is not of type Event - "
									+ event.getClass());
				return thisObj;
			}
		});

		// Ponder2 call to return the class name
		operation.put("class", new InstanceOperation() {

			@Override
			public P2Object call(P2Object thisObj, ManagedObject obj,
					P2Object source, String operation, P2Object... args)
					throws Ponder2Exception {
				String name = ((P2ObjectAdaptor) thisObj).getObj().getClass()
						.getSimpleName();
				if (name.startsWith("P2")) {
					name = name.substring(2);
				}
				return P2Object.create(name);
			}
		});
	}

	public InstanceOperation getInstanceOperation(String opName)
			throws Ponder2OperationException {
		InstanceOperation op = operation.get(opName);
		if (op == null)
			throw new Ponder2OperationException("Object " + this.getClass()
					+ " unknown operation '" + opName + "'");
		return op;
	}

	public static class CreateOrStaticOperation {

		private CreateOperation create = null;
		private StaticOperation staticop = null;
		private String opName;

		CreateOrStaticOperation(String opName) {
			this.opName = opName;
		}

		public boolean findOp(Map<String, CreateOperation> createMap,
				Map<String, StaticOperation> staticMap) {
			create = createMap.get(opName);
			if (create == null) {
				staticop = staticMap.get(opName);
				if (staticop == null)
					return false;
			}
			return true;
		}

		public P2Object call(P2ObjectAdaptor adaptor, P2Object source,
				String operation, P2Object... args) throws Ponder2Exception {
			if (create != null) {
				adaptor.objImpl = create.call(adaptor, source, operation, args);
				return adaptor;
			}
			if (staticop != null)
				return staticop.call(source, operation, args);

			throw new Ponder2OperationException("Object "
					+ this.getClass().getSimpleName()
					+ " unknown constructor or static operation: '" + operation
					+ "'");

		}

	}

	/**
	 * Base class to map an operation with known arguments to a specific call
	 * within a managed object
	 * 
	 * @author Kevin Twidle
	 * @version $Id:$
	 */
	public static abstract class StaticOperation {

		/**
		 * maps an operation to a static method
		 * 
		 * @param source
		 *            the subject OID of the operation
		 * @param args
		 *            arguments for the operation
		 * @return the new managed object
		 * @throws Ponder2ArgumentException
		 */
		public abstract P2Object call(P2Object source, String operation,
				P2Object... args) throws Ponder2Exception;
	}

	/**
	 * Base class to map an operation with known arguments to a specific call
	 * within a managed object
	 * 
	 * @author Kevin Twidle
	 * @version $Id:$
	 */
	public static abstract class CreateOperation {

		/**
		 * creates a managed object
		 * 
		 * @param source
		 *            the subject OID of the operation
		 * @param args
		 *            arguments for the operation
		 * @return the new managed object
		 * @throws Ponder2ArgumentException
		 */
		public abstract ManagedObject call(P2Object obj, P2Object source,
				String operation, P2Object... args) throws Ponder2Exception;
	}

	/**
	 * Base class to map an operation with known arguments to a specific call
	 * within a managed object
	 * 
	 * @author Kevin Twidle
	 * @version $Id:$
	 */
	public static abstract class InstanceOperation {

		/**
		 * calls an operation in a managed object
		 * 
		 * @param obj
		 *            the instance of the managed object to be called
		 * @param source
		 *            the subject OID of the operation
		 * @param args
		 *            arguments for the operation
		 * @return the result of the operation
		 * @throws Ponder2Exception
		 */
		public abstract P2Object call(P2Object thisobj, ManagedObject obj,
				P2Object source, String operation, P2Object... args)
				throws Ponder2Exception;
	}

	// --------------------------------------------------------------

	// This code is for serialising Java Managed Objects. All that we need do is
	// serialise this object as an OID because the objects themselves do not
	// travel around.
	// When reading back in, we read in an OID and return the object associated
	// with the OID. If the OID is external then we will be returning an
	// ExternalManagedObject, if internal then we return the correct
	// P2ObjectObjectAdaptor.

	// Place holder for keeping our OID when reading this object from an
	// external
	// source
	private OID myExternalOID = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// Just read an OID and save it
		myExternalOID = (OID) in.readObject();
	}

	/**
	 * Resolves instances being deserialised to the preexisting objects.
	 * 
	 * @return the intended object
	 * @throws ObjectStreamException
	 *             if something happens
	 */
	protected Object readResolve() throws ObjectStreamException {
		// Return the object associated with the read OID.
		return myExternalOID.getP2Object();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getOID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#writeXml()
	 */
	@Override
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		return getOID().writeXml(written);
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
		throw new Ponder2OperationException(
				"P2ObjectAdaptor cannot be read in as a serialised object");
	}

}
