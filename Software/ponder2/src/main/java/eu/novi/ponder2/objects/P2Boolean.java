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
import java.util.Set;


import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;

/**
 * A boolean object.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Boolean extends P2Object implements ManagedObject {

	static public final P2Boolean True = new P2Boolean(true);
	static public final P2Boolean False = new P2Boolean(false);

	/**
	 * constructor for Ponder2 serialisation
	 */
	protected P2Boolean() {
	}

	public static P2Boolean from(String string) throws Ponder2ArgumentException {
		if (string.equals("true"))
			return True;
		if (string.equals("false"))
			return False;
		throw new Ponder2ArgumentException("Bad boolean string '" + string
				+ "'");
	}

	private boolean value;

	/**
	 * @param value
	 */
	private P2Boolean(boolean value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asBoolean()
	 */
	@Override
	public boolean asBoolean() throws Ponder2ArgumentException {
		return value;
	}

	/**
	 * Answers a boolean being the logical NOT operator applied to the receiver
	 * 
	 * @return NOT this
	 */
	@Ponder2op("not")
	protected boolean not() {
		return !value;
	}

	/**
	 * Answers a boolean being the receiver AND aBoolean
	 * 
	 * @param aBoolean
	 *            the value for the AND operation
	 * @return this and aBoolean
	 */
	@Ponder2op("&")
	protected boolean and(boolean aBoolean) {
		return value & aBoolean;
	}

	/**
	 * Answers a boolean being the receiver OR aBoolean
	 * 
	 * @param aBoolean
	 *            the value for the OR operation
	 * @return this or aBoolean
	 */
	@Ponder2op("|")
	protected boolean or(boolean aBoolean) {
		return value | aBoolean;
	}

	/**
	 * Answers with a random boolean value
	 * 
	 * @return a random boolean value
	 */
	@Ponder2op("random")
	protected boolean random() {
		return P2Number.random.nextBoolean();
	}

	/**
	 * Answers a boolean being the receiver AND the value of aBlock if the
	 * receiver is true. The block is not evaluated if the receiver is false.
	 * The block must return a boolean.
	 * 
	 * @param aBlock
	 *            the block to be executed if this is true
	 * @return this and aBlock
	 * @throws Ponder2Exception
	 */
	@Ponder2op("and:")
	protected boolean and(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return value ? aBlock.execute(source).asBoolean() : false;
	}

	/**
	 * Answers a boolean being the receiver OR the value of aBlock if the
	 * receiver is false. The block is not evaluated if the receiver is true.
	 * The block must return a boolean.
	 * 
	 * @param aBlock
	 *            the block to be executed if this is false
	 * @return this or aBlock
	 * @throws Ponder2Exception
	 */
	@Ponder2op("or:")
	protected boolean or(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return value ? true : aBlock.execute(source).asBoolean();
	}

	/**
	 * Executes aBlock if the receiver is true. Answers the value of aBlock or
	 * Nil. No arguments are given to the block.
	 * 
	 * @param aBlock
	 *            the block to be executed if this is true
	 * @return boolean or nil
	 * @throws Ponder2Exception
	 */
	@Ponder2op("ifTrue:")
	protected P2Object ifTrue(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return value ? aBlock.execute(source) : P2Object.create();
	}

	/**
	 * Executes aBlock if the receiver is false. Answers the value of aBlock or
	 * Nil. No arguments are given to the block.
	 * 
	 * @param aBlock
	 *            the block to be executed if this is false
	 * @return boolean or nil
	 * @throws Ponder2Exception
	 */
	@Ponder2op("ifFalse:")
	protected P2Object ifFalse(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return value ? P2Object.create() : aBlock.execute(source);
	}

	/**
	 * Executes aBlockTrue if the receiver is true otherwise aBlockFalse is
	 * executed. Answers the return value of the block that is executed. No
	 * arguments are given to the block to be executed.
	 * 
	 * @param aBlockTrue
	 *            the block to be executed if this is true
	 * @param aBlockFalse
	 *            the block to be executed if this is false
	 * @return the result of the block executed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("ifTrue:ifFalse:")
	protected P2Object ifTrueifFalse(P2Object source, P2Block aBlockTrue,
			P2Block aBlockFalse) throws Ponder2Exception {
		return value ? aBlockTrue.execute(source) : aBlockFalse.execute(source);
	}

	/**
	 * Executes aBlockFalse if the receiver is false otherwise aBlockTrue is
	 * executed. Answers the return value of the block that is executed. No
	 * arguments are given to the block to be executed.
	 * 
	 * @param aBlockFalse
	 *            the block to be executed if this is false
	 * @param aBlockTrue
	 *            the block to be executed if this is true
	 * @return the result of the block executed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("ifFalse:ifTrue:")
	protected P2Object ifFalseifTrue(P2Object source, P2Block aBlockFalse,
			P2Block aBlockTrue) throws Ponder2Exception {
		return value ? aBlockTrue.execute(source) : aBlockFalse.execute(source);
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
		xml.setAttribute("value", toString());
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
			throws Ponder2OperationException {
		boolean bool = Boolean.parseBoolean(xml.getAttribute("value"));
		return bool ? True : False;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Boolean.toString(value);
	}

}
