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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Class;
import eu.novi.ponder2.objects.P2Error;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.OID;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.Ponder2Message;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class P2Object implements Ponder2Message, P2Serializable,
		Serializable {

	private static SelfManagedCell mysmc;
	private P2ObjectAdaptor p2Adaptor = null;

	public static void setSMC(SelfManagedCell smc) {
		mysmc = smc;
	}

	public static SelfManagedCell getSMC() {
		return mysmc;
	}

	public static P2Null create() {
		return P2Null.Null;
	}

	public static P2Number create(BigDecimal number) {
		return new P2Number(number);
	}

	public static P2Number create(long number) {
		return new P2Number(new BigDecimal(number));
	}

	public static P2Number create(int number) {
		return new P2Number(new BigDecimal(number));
	}

	public static P2Number create(double number) {
		return new P2Number(new BigDecimal(number));
	}

	public static P2Number create(float number) {
		return new P2Number(new BigDecimal(number));
	}

	public static P2String create(String string) {
		return new P2String(string);
	}

	public static P2Hash create(Map<String, P2Object> hash) {
		return new P2Hash(hash);
	}

	public static P2Object create(P2ManagedObject mo) {
		return mo.getP2Object();
	}

	public static P2Array create(P2Object... values) {
		return new P2Array(values);
	}

	public static P2Array create(String... values) {
		return new P2Array(values);
	}

	public static P2Array create(int... values) {
		return new P2Array(values);
	}

	public static P2Class create(Class<?> name) {
		return new P2Class(name);
	}

	public static P2Error create(Ponder2Exception exception) {
		return new P2Error(exception);
	}

	public static P2Boolean create(boolean value) {
		return value ? P2Boolean.True : P2Boolean.False;
	}

	public static P2Block create(Map<String, P2Object> variables,
			TaggedElement block) {
		return new P2Block(variables, block);
	}

	private static HashMap<String, Class<P2Object>> classTable = null;

	@SuppressWarnings("unchecked")
	public static P2Object fromXml(TaggedElement xml,
			Map<Integer, P2Serializable> read)
			throws Ponder2OperationException, Ponder2ArgumentException {
		if (classTable == null)
			classTable = new HashMap<String, Class<P2Object>>();
		// Have we already read this in?
		String className = xml.getAttribute("class");
		try {
			Class<P2Object> objectClass = classTable.get(className);
			if (objectClass == null) {
				objectClass = (Class<P2Object>) Class.forName(className);
				classTable.put(className, objectClass);
			}
			String p2idstring = xml.getAttribute("p2id");
			int p2id = 0;
			if (p2idstring != null)
				p2id = Integer.parseInt(p2idstring);
			if (p2idstring != null && xml.getAttribute("written") != null) {
				P2Serializable object = read.get(p2id);
				if (object == null) {
					System.err.println("From XML, bad p2id of " + p2idstring);
					throw new Ponder2OperationException(
							"FromXML: internal error, p2id not found");
				}
				return (P2Object) object;
			}
			P2Serializable newInstance = objectClass.newInstance();
			// The new object may be different from the newInstance e.g.
			// P2Boolean
			P2Object newObject = newInstance.readXml(xml, read);
			if (p2idstring != null)
				read.put(p2id, newObject);
			return newObject;

		} catch (ClassNotFoundException e) {
			throw new Ponder2OperationException("FromXML: Cannot locate class "
					+ className);
		} catch (InstantiationException e) {
			throw new Ponder2OperationException(
					"FromXML: Cannot instantiate class " + className);
		} catch (IllegalAccessException e) {
			throw new Ponder2OperationException("FromXML: Cannot access class "
					+ className);
		} catch (ClassCastException e) {
			throw new Ponder2OperationException("FromXML:  " + className
					+ " is not of type P2Object");
		}
	}

	/**
	 * The domain managed object associated with this managed object
	 */
	private P2ManagedObject mo = null;

	/**
	 * Empty constructor used for input serialisation
	 */
	protected P2Object() {
	};

	public P2ManagedObject getManagedObject() {
		if (mo == null) {
			mo = new P2ManagedObject(this);
		}
		return mo;
	}

	/**
	 * Returns the Ponder2 Object Identifier of this object
	 * 
	 * @return the OID
	 */
	public OID getOID() {
		return getManagedObject().getOID();
	}

	/**
	 * Returns itself as an Integer
	 * 
	 * @return the integer value
	 * @throws Ponder2ArgumentException
	 */
	public int asInteger() throws Ponder2ArgumentException {
		try {
			return asNumber().intValue();
		} catch (ArithmeticException e) {
			throw new Ponder2ArgumentException("not an integer "
					+ e.getMessage());
		}
	}

	/**
	 * Returns itself as a Long integer
	 * 
	 * @return the long value
	 * @throws Ponder2ArgumentException
	 */
	public long asLong() throws Ponder2ArgumentException {
		try {
			return asNumber().longValue();
		} catch (ArithmeticException e) {
			throw new Ponder2ArgumentException("not a long " + e.getMessage());
		}
	}

	/**
	 * Returns itself as a Float
	 * 
	 * @return the float value
	 * @throws Ponder2ArgumentException
	 */
	public float asFloat() throws Ponder2ArgumentException {
		return asNumber().floatValue();
	}

	/**
	 * Returns itself as a Double
	 * 
	 * @return the double value
	 * @throws Ponder2ArgumentException
	 */
	public double asDouble() throws Ponder2ArgumentException {
		return asNumber().doubleValue();
	}

	/**
	 * Returns itself as a BigDecimal number
	 * 
	 * @return the BigDecimal value
	 * @throws Ponder2ArgumentException
	 */
	public BigDecimal asNumber() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not a number");
	}

	/**
	 * Returns itself as a String
	 * 
	 * @return the String value
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	public String asString() throws Ponder2ArgumentException,
			Ponder2OperationException {
		return toString();
	}

	/**
	 * Returns itself as an array of Ponder2 objects
	 * 
	 * @return the array value
	 * @throws Ponder2ArgumentException
	 */
	public P2Object[] asArray() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not an array");
	}

	/**
	 * Returns itself as a Ponder2 Array
	 * 
	 * @return the array value
	 * @throws Ponder2ArgumentException
	 */
	public P2Array asP2Array() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not an array");
	}

	/**
	 * Returns itself as a Ponder2 block
	 * 
	 * @return the block value
	 * @throws Ponder2ArgumentException
	 */
	public P2Block asBlock() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not a block");
	}

	/**
	 * Returns itself as a Ponder2 hash
	 * 
	 * @return the hash value
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	public P2Hash asHash() throws Ponder2ArgumentException,
			Ponder2OperationException {
		throw new Ponder2ArgumentException("not a hash");
	}

	/**
	 * Returns itself as a boolean
	 * 
	 * @return the boolean value
	 * @throws Ponder2ArgumentException
	 */
	public boolean asBoolean() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not a boolean");
	}

	/**
	 * Returns itself as a Ponder2 class object
	 * 
	 * @return a class value
	 * @throws Ponder2ArgumentException
	 */
	public Class<?> asClass() throws Ponder2ArgumentException {
		throw new Ponder2ArgumentException("not a class");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.comms.P2Serializable#writeXml()
	 */
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		TaggedElement xml = new TaggedElement("P2Object");
		xml.setAttribute("class", this.getClass().getCanonicalName());
		xml.setAttribute("p2id", "" + this.hashCode());
		if (!written.add(this)) {
			xml.setAttribute("written", "true");
		}
		return xml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.comms.P2Serializable#readXml(com.twicom.qdparser.
	 * TaggedElement)
	 */
	public abstract P2Object readXml(TaggedElement xml,
			Map<Integer, P2Serializable> read)
			throws Ponder2OperationException, Ponder2ArgumentException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.Ponder2Message#create(eu.novi.ponder2.objects.P2OID,
	 * java.lang.String, eu.novi.ponder2.objects.P2Object[])
	 */
	public P2Object create(P2Object source, String operation, P2Object... args)
			throws Ponder2Exception {
		throw new Ponder2OperationException(this.getClass()
				+ ": No such create operation - " + operation);
	}

	/**
	 * Performs operations on behalf of basic managed objects. If this operation
	 * is called we check that we have an adaptor and call that. If we don't
	 * have one we instantiate one and hang on to it.
	 * 
	 * @see eu.novi.ponder2.Ponder2Message#operation(eu.novi.ponder2.objects.P2Object,
	 *      java.lang.String, eu.novi.ponder2.objects.P2Object[])
	 */
	@SuppressWarnings("unchecked")
	public P2Object operation(P2Object source, String operation,
			P2Object... args) throws Ponder2Exception {
		if (p2Adaptor == null)
			try {
				Class<P2ObjectAdaptor> p2AdaptorClass = (Class<P2ObjectAdaptor>) Class
						.forName(this.getClass().getName() + "P2Adaptor");
				if (SelfManagedCell.SystemTrace)
					System.out.println("loading " + this.getClass().getName()
							+ "P2Adaptor");
				p2Adaptor = p2AdaptorClass.newInstance();
				p2Adaptor.setObj((ManagedObject) this);
			} catch (ClassNotFoundException e) {
				throw new Ponder2OperationException(this.getClass()
						+ ": cannot find adaptor class - " + e.getMessage());
			} catch (InstantiationException e) {
				throw new Ponder2OperationException(
						"Factory create: InstantiationException "
								+ e.getMessage());
			} catch (IllegalAccessException e) {
				throw new Ponder2OperationException(
						"Factory create: IllegalAccessException "
								+ e.getMessage());
			}
		return p2Adaptor.operation(source, operation, args);
	}

	public P2Object operation(P2Object source, String operation, String arg1,
			String... args) throws Ponder2Exception {
		P2Object[] p2args = new P2Object[1 + args.length];
		p2args[0] = P2Object.create(arg1);
		for (int i = 0; i < args.length; i++) {
			p2args[i + 1] = P2Object.create(args[i]);
		}
		return operation(source, operation, p2args);
	}

}
