/**
 * Copyright 2007 Imperial College, London, England.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

import com.twicom.qdparser.Element;
import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * Implements a dictionary that stores objects indexed by keys. The keys are
 * string types.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Hash extends P2Object implements ManagedObject,
		Map<String, P2Object> {

	// This is held as a Java Map because it is essentially the same thing
	private Map<String, P2Object> hash;

	/**
	 * Creates an instance of a P2Hash with no contents
	 */
	public P2Hash() {
		this.hash = new HashMap<String, P2Object>();
	}

	/**
	 * Creates an instance of a P2Hash with initials value(s)
	 * 
	 * @param hash
	 *            a Java Map containing P2Values
	 */
	public P2Hash(Map<String, P2Object> hash) {
		this.hash = new HashMap<String, P2Object>(hash);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.parser.P2Value#asString()
	 */
	@Override
	public P2Hash asHash() throws Ponder2ArgumentException {
		return this;
	}

	/**
	 * gets the complete arguments in name, value pairs
	 * 
	 * @return a Map of the named arguments
	 */
	public Map<String, P2Object> asMap() {
		return hash;
	}

	/**
	 * sets the hash with a copy of the contents of another P2Hash
	 * 
	 * @param hash
	 *            the hash to be copied
	 */
	protected void set(P2Hash hash) {
		clear();
		putAll(hash.asMap());
	}

	/**
	 * Takes a block and executes the block once for each entry in the hash. The
	 * arguments to the block are the name of the entry and the value of the
	 * entry. Answers with the receiver.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("do:")
	public void operation_do(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		for (Iterator<Entry<String, P2Object>> iter = hash.entrySet()
				.iterator(); iter.hasNext();) {
			Entry<String, P2Object> value = iter.next();
			aBlock.execute(source, new P2String(value.getKey()),
					value.getValue());
		}
	}

	/**
	 * Takes a block and executes the block once for each entry in the hash. The
	 * arguments to the block are the name of the entry and the value of the
	 * entry. The result of each block is collected and returned in an array.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("collect:")
	public P2Array operation_collect(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		P2Array result = new P2Array();
		for (Iterator<Entry<String, P2Object>> iter = hash.entrySet()
				.iterator(); iter.hasNext();) {
			Entry<String, P2Object> value = iter.next();
			result.add(aBlock.execute(source, new P2String(value.getKey()),
					value.getValue()));
		}
		return result;
	}

	/**
	 * returns an array of the names of the objects in this domain
	 * 
	 * @return a String array with the object names
	 */
	protected String[] names() {
		int size = hash.size();
		return hash.keySet().toArray(new String[size]);
	}

	// Ponder2 operations

	/**
	 * answers an array containing the names of all the domain's entries.
	 * 
	 * @return a Ponder2 array with the object names
	 */
	@Ponder2op("listNames")
	protected P2Object operation_listNames() {
		String[] names = names();
		P2Object[] values = new P2Object[names.length];
		for (int i = 0; i < names.length; i++) {
			values[i] = new P2String(names[i]);
		}
		return new P2Array(values);
	}

	/**
	 * answers an array of Managed Object names containing all the entries in
	 * the domain.
	 * 
	 * @return an array containing all the object in this domain
	 */
	@Ponder2op("listObjects")
	protected P2Object operation_listObjects() {
		Collection<P2Object> mos = hash.values();
		P2Object[] values = new P2Object[mos.size()];
		int i = 0;
		for (Iterator<P2Object> iter = mos.iterator(); iter.hasNext();) {
			values[i++] = iter.next();
		}
		return new P2Array(values);
	}

	/**
	 * answers an array containing name, values, name, value ... entries from
	 * the receiver
	 * 
	 * @return a Ponder2 array containing the hash
	 */
	@Ponder2op("asArray")
	protected P2Array asPonder2Array() {
		P2Array array = new P2Array();
		for (Map.Entry<String, P2Object> entry : hash.entrySet()) {
			array.add(P2Object.create(entry.getKey()));
			array.add(entry.getValue());
		}
		return array;
	}

	/**
	 * Answer anObject. Store anObject in the table with aKey. If aKey already
	 * exists the previous value is overridden.
	 */
	@Ponder2op("at:put:")
	public P2Object operation_at_put(String aKey, P2Object anObject) {
		put(aKey, anObject);
		return anObject;
	}

	/**
	 * Answer the value associated with the given key and remove it from the
	 * table. Answer Null if it is not found. TODO Should fail if not found
	 */
	@Ponder2op("remove:")
	protected P2Object operation_remove(String aKey) {
		return remove(aKey);
	}

	/**
	 * Removes anObject from the receiver. All copies of anObject will be
	 * removed. Answers true if one or more were removed.
	 * 
	 * @param anObject
	 *            the object to be removed
	 * @return true if an object was removed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("removeObject:")
	protected boolean removeObject(P2Object anObject) throws Ponder2Exception {
		boolean result = false;
		// The object may be here more than once
		Set<Entry<String, P2Object>> entrySet = new HashSet<Entry<String, P2Object>>(
				hash.entrySet());
		for (Entry<String, P2Object> entry : entrySet) {
			if (entry.getValue().equals(anObject)) {
				remove(entry.getKey());
				result = true;
			}
		}
		return result;
	}

	/**
	 * Removes all objects stored in the receiver. Answers self.
	 */
	@Ponder2op("removeAll")
	protected void operation_removeAll() {
		clear();
	}

	/**
	 * Answer true if the given key exists otherwise false
	 */
	@Ponder2op("has:")
	protected boolean operation_has(String aKey) {
		return containsKey(aKey);
	}

	/**
	 * Answer true if anObject is in the receiver
	 */
	@Ponder2op("hasObject:")
	protected boolean operation_has(P2Object anObject) {
		return containsValue(anObject);
	}

	/**
	 * Answer the value associated with the given key. If not found evaluate
	 * block (with no arguments) and return its result
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("at:ifAbsent:")
	public P2Object operation_at_ifAbsent(P2Object source, String aKey,
			P2Block aBlock) throws Ponder2Exception {
		if (containsKey(aKey))
			return get(aKey);
		return aBlock.operation_value0(source);
	}

	/**
	 * Answer the value associated with the given key. Throws a
	 * Ponder2ArgumentException error if not found.
	 * 
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("at:")
	protected P2Object operation_at(String aKey)
			throws Ponder2ArgumentException {
		P2Object value = get(aKey);
		if (value == null)
			throw new Ponder2ArgumentException("hash key '" + aKey
					+ "' not found");
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		hash.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return hash.containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return hash.containsValue(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<String, P2Object>> entrySet() {
		return hash.entrySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public P2Object get(Object key) {
		return hash.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return hash.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return hash.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public P2Object put(String key, P2Object value) {
		return hash.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends P2Object> map) {
		hash.putAll(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public P2Object remove(Object key) {
		return hash.remove(key);
	}

	/**
	 * Answer the number of elements in the receiver.
	 * 
	 * @return the number of elements held
	 */
	@Ponder2op("size")
	public int size() {
		return hash.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection<P2Object> values() {
		return hash.values();
	}

	/**
	 * To be overridden by P2Hash subclasses. Affects whether the hash is copied
	 * remotely or a reference is sent
	 * 
	 * @return true if mutable
	 */
	protected boolean isMutable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#writeXml()
	 */
	@Override
	public TaggedElement writeXml(Set<P2Object> written)
			throws Ponder2OperationException {
		if (isMutable())
			return getOID().writeXml(written);

		// Immutable object, write it all out
		TaggedElement xml = super.writeXml(written);
		if (xml.getAttribute("written") != null) {
			return xml;
		}
		for (Entry<String, P2Object> entry : hash.entrySet()) {
			TaggedElement xmlEntry = new TaggedElement("entry");
			xmlEntry.setAttribute("name", entry.getKey());
			TaggedElement xmlValue = entry.getValue().writeXml(written);
			xmlEntry.add(xmlValue);
			xml.add(xmlEntry);
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
		if (isMutable())
			throw new Ponder2OperationException("P2Hash cannot call readXml");

		// Immutable object, read it all in
		for (Element element : xml.getElements()) {
			TaggedElement xmlEntry = (TaggedElement) element;
			if (xmlEntry.getName().equals("entry")) {
				String name = xmlEntry.getAttribute("name");
				P2Object obj = P2Object.fromXml(
						(TaggedElement) xmlEntry.getChild(0), read);
				hash.put(name, obj);
			}
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ ");
		for (Entry<String, P2Object> entry : hash.entrySet()) {
			buf.append(entry.getKey());
			buf.append("=\"");
			if (entry.getValue() == this) {
				buf.append("this");
			} else {
				buf.append(entry.getValue());
			}
			buf.append("\" ");
		}
		buf.append("}");
		return buf.toString();
	}

}
