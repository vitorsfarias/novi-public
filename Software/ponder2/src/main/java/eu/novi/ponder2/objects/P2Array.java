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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * Holds a collection of Ponder2 objects. An array may be returned from sending
 * a message to an object or or may be created in PonderTalk with the
 * 
 * <pre>
 * #( obj1 obj2 obj3)
 * </pre>
 * 
 * syntax.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Array extends P2Object implements ManagedObject {

	// This is held as a list because we might want to alter it eg. add:
	Vector<P2Object> values;

	/**
	 * creates an empty P2Array
	 */
	public P2Array() {
		this.values = new Vector<P2Object>();
	}

	/**
	 * creates a P2Array initialised with values
	 * 
	 * @param values
	 *            the values to use to initialise the array
	 */
	public P2Array(P2Object... values) {
		this.values = new Vector<P2Object>(Arrays.asList(values));
	}

	/**
	 * creates a P2Array initialised with string values
	 * 
	 * @param values
	 *            the values to use to initialise the array
	 */
	public P2Array(String... values) {
		this.values = new Vector<P2Object>();
		for (String value : values)
			this.values.add(P2Object.create(value));
	}

	/**
	 * creates a P2Array initialised with int values
	 * 
	 * @param values
	 *            the values to use to initialise the array
	 */
	public P2Array(int... values) {
		this.values = new Vector<P2Object>();
		for (int value : values)
			this.values.add(P2Object.create(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.parser.P2Value#asString()
	 */
	@Override
	public P2Object[] asArray() throws Ponder2ArgumentException {
		return values.toArray(new P2Object[values.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asP2Array()
	 */
	@Override
	public P2Array asP2Array() throws Ponder2ArgumentException {
		return this;
	}

	/**
	 * Returns a Hash comprising of the array contents taken as key, value pairs
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asHash()
	 */
	@Override
	@Ponder2op("asHash")
	public P2Hash asHash() throws Ponder2ArgumentException,
			Ponder2OperationException {
		if ((values.size() % 2) != 0)
			throw new Ponder2ArgumentException(
					"P2Array cannot convert to P2Hash, odd number of elements");
		P2Hash hash = new P2Hash();
		for (Iterator<P2Object> it = values.iterator(); it.hasNext();) {
			P2Object name = it.next();
			P2Object value = it.next();
			hash.operation_at_put(name.asString(), value);
		}
		return hash;
	}

	/**
	 * for each object in the array executes aBlock with the object given as an
	 * argument to the block. Answers the receiver
	 * 
	 * @param source
	 *            the originator of the command
	 * @param aBlock
	 *            the block to be executed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("do:")
	public void operation_do(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		for (Iterator<P2Object> iter = values.iterator(); iter.hasNext();) {
			P2Object value = iter.next();
			aBlock.execute(source, value);
		}
	}

	/**
	 * for each entry in the array executes aBlock with the entry given as an
	 * argument to the block. Answers an array of answers built from each
	 * execution of the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param aBlock
	 *            the block to be executed
	 * @return an array with the results of the block executions
	 * @throws Ponder2Exception
	 */
	@Ponder2op("collect:")
	public P2Array operation_collect(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		P2Array result = new P2Array();
		for (Iterator<P2Object> iter = values.iterator(); iter.hasNext();) {
			P2Object value = iter.next();
			result.add(aBlock.execute(source, value));
		}
		return result;
	}

	/**
	 * adds a aP2Object to the receiver. Answers the receiver
	 * 
	 * @param aP2Object
	 *            the object to be added to the array
	 */
	@Ponder2op("add:")
	public void add(P2Object aP2Object) {
		values.add(aP2Object);
	}

	/**
	 * adds all objects in anArray to the receiver. Answers the receiver
	 * 
	 * @param anArray
	 *            the array to be added to this object
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("addAll:")
	public void addAll(P2Array anArray) throws Ponder2ArgumentException {
		P2Object[] array = anArray.asArray();
		for (int i = 0; i < array.length; i++) {
			values.add(array[i]);
		}

	}

	/**
	 * returns the object at anIndex
	 * 
	 * @param anIndex
	 *            the index of the required object
	 * @return the object located by anIndex
	 */
	@Ponder2op("at:")
	public P2Object at(int anIndex) {
		return values.elementAt(anIndex);
	}

	/**
	 * inserts aP2Object at anIndex posiion in the array. Returns the object
	 * added.
	 * 
	 * @param anIndex
	 *            the index of the object to be added
	 * @param aP2Object
	 *            the object to be added
	 * @return the object that was added
	 */
	@Ponder2op("at:put:")
	public P2Object atput(int anIndex, P2Object aP2Object) {
		if (anIndex < 0)
			anIndex = 0;
		if (anIndex > values.size())
			anIndex = values.size();
		values.set(anIndex, aP2Object);
		return aP2Object;
	}

	/**
	 * returns the number of elements in the receiver
	 * 
	 * @return the number of elements held
	 */
	@Ponder2op("size")
	public int size() {
		return values.size();
	}

	/**
	 * returns true if the array an element at anIndex. This is really here to
	 * compliment the other collections and the remove: operations.
	 * 
	 * @param anIndex
	 *            the object to be checked
	 * @return true if the object is in the array
	 */
	@Ponder2op("has:")
	public boolean has(int anIndex) {
		return (anIndex >= 0) && (anIndex < values.size());
	}

	/**
	 * returns true if the array contains aP2Object
	 * 
	 * @param aP2Object
	 *            the object to be checked
	 * @return true if the object is in the array
	 */
	@Ponder2op("hasObject:")
	public boolean hasObject(P2Object aP2Object) {
		return values.contains(aP2Object);
	}

	/**
	 * Answer the value associated with the given index and remove it from the
	 * receiver.
	 */
	@Ponder2op("remove:")
	protected P2Object remove(int anIndex) {
		return values.remove(anIndex);
	}

	/**
	 * Removes anObject from the receiver. All copies of anObject will be
	 * removed. Answers true if one or more were removed.
	 * 
	 * @param anObject
	 *            the object to be removed
	 * @return true if the an object was removed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("removeObject:")
	protected boolean removeObject(P2Object anObject) throws Ponder2Exception {
		// The object may be here more than once
		boolean removed = values.remove(anObject);
		boolean result = removed;
		while (removed) {
			removed = values.remove(anObject);
		}
		return result;
	}

	/**
	 * Removes all objects stored in the receiver. Answers self.
	 */
	@Ponder2op("removeAll")
	protected void operation_removeAll() {
		values.clear();
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
		if (xml.getAttribute("written") != null) {
			return xml;
		}
		xml.setAttribute("size", "" + values.size());
		for (Iterator<P2Object> iter = values.iterator(); iter.hasNext();) {
			P2Object value = iter.next();
			xml.add(value.writeXml(written));
		}
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
			throws Ponder2OperationException, Ponder2ArgumentException {
		int size = Integer.parseInt(xml.getAttribute("size"));
		values = new Vector<P2Object>(size);
		for (int i = 0; i < size; i++) {
			TaggedElement e = (TaggedElement) xml.getChild(i);
			values.add(P2Object.fromXml(e, read));
		}
		return this;
	}

	/**
	 * Returns all the values in the array as a Vector
	 * 
	 * @return the values
	 */
	public Vector<P2Object> getValues() {
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return values.toString();
	}
}
