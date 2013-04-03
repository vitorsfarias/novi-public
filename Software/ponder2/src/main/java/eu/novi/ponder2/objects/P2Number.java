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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;

/**
 * An object used for comparing and counting
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Number extends P2Object implements ManagedObject {

	BigDecimal number;

	/**
	 * constructor for Ponder2 serialisation
	 */
	protected P2Number() {
	}

	/**
	 * constructs a P2Number with value as its initial value
	 * 
	 * @param value
	 *            the initial value of this number
	 */
	public P2Number(BigDecimal value) {
		this.number = value;
	}

	/**
	 * constructs a P2Number with its initial value expressed as a string
	 * 
	 * @param value
	 *            the initial value of this number
	 * @throws Ponder2ArgumentException
	 */
	public P2Number(String value) throws Ponder2ArgumentException {
		try {
			number = new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw new Ponder2ArgumentException("cannot parse '" + value
					+ "' as a number");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.objects.P2Object#asNumber()
	 */
	@Override
	public BigDecimal asNumber() throws Ponder2ArgumentException {
		return number;
	}

	/**
	 * Answer true if the receiver is less than aNumber else answer false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this < aNumber
	 */
	@Ponder2op("<")
	protected boolean lt(BigDecimal aNumber) {
		return number.compareTo(aNumber) < 0;
	}

	/**
	 * Answer true if the receiver is greater than aNumber else answer false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this > aNumber
	 */
	@Ponder2op(">")
	protected boolean gt(BigDecimal aNumber) {
		return number.compareTo(aNumber) > 0;
	}

	/**
	 * Answer true if the receiver is less than or equal to aNumber else answer
	 * false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this <= aNumber
	 */
	@Ponder2op("<=")
	protected boolean le(BigDecimal aNumber) {
		return number.compareTo(aNumber) <= 0;
	}

	/**
	 * Answer true if the receiver is greater than or equal to aNumber else
	 * answer false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this >= aNumber
	 */
	@Ponder2op(">=")
	protected boolean ge(BigDecimal aNumber) {
		return number.compareTo(aNumber) >= 0;
	}

	/**
	 * Answer true if the receiver is equal to aNumber else answer false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this == aNumber
	 */
	@Ponder2op("==")
	protected boolean eq(BigDecimal aNumber) {
		return number.compareTo(aNumber) == 0;
	}

	/**
	 * Answer true if the receiver is not equal to aNumber else answer false
	 * 
	 * @param aNumber
	 *            the value to be compared
	 * @return this != aNumber
	 */
	@Ponder2op("!=")
	protected boolean ne(BigDecimal aNumber) {
		return number.compareTo(aNumber) != 0;
	}

	/**
	 * Answer the result of adding the receiver and aNumber
	 * 
	 * @param aNumber
	 *            the value to be used
	 * @return this + aNumber
	 */
	@Ponder2op("+")
	protected BigDecimal plus(BigDecimal aNumber) {
		return number.add(aNumber);
	}

	/**
	 * Answer the result of subtracting aNumber from the receiver
	 * 
	 * @param aNumber
	 *            the value to be used
	 * @return this - aNumber
	 */
	@Ponder2op("-")
	protected BigDecimal minus(BigDecimal aNumber) {
		return number.subtract(aNumber);
	}

	/**
	 * Answer the result of multiplying the receiver and aNumber
	 * 
	 * @param aNumber
	 *            the value to be used
	 * @return this * aNumber
	 */
	@Ponder2op("*")
	protected BigDecimal times(BigDecimal aNumber) {
		return number.multiply(aNumber);
	}

	/**
	 * Answer the result of dividing the receiver by aNumber
	 * 
	 * @param aNumber
	 *            the value to be used
	 * @return this / aNumber
	 */
	@Ponder2op("/")
	protected BigDecimal divide(BigDecimal aNumber) {
		return number.divide(aNumber, 5, RoundingMode.HALF_UP);
	}

	public final static Random random = new Random();

	/**
	 * Answer a random number depending upon the value of the receiver: <br>
	 * 0 => random long value<br>
	 * n => random integer >=0 and < n<br>
	 * n.m => random double >= 0.0 and < 1.0<br>
	 * 
	 * @return a random number
	 */
	@Ponder2op("random")
	protected BigDecimal random() {
		BigDecimal result;
		if (number.compareTo(BigDecimal.ZERO) == 0)
			result = new BigDecimal(random.nextLong());
		else if (number.scale() == 0)
			result = new BigDecimal(random.nextInt(number.intValue()));
		else
			result = new BigDecimal(random.nextDouble());
		return result;
	}

	/**
	 * Execute aBlock this number of times. C.f. a for loop. The iteration
	 * number is given as an argument to aBlock. The number starts at 0. Answers
	 * receiver
	 * 
	 * @param aBlock
	 *            the block to be executed. It may take one argument
	 * @throws Ponder2Exception
	 */
	@Ponder2op("do:")
	protected void op_do(P2Object source, P2Object aBlock)
			throws Ponder2Exception {
		for (int i = 0; i < number.intValue(); i++)
			aBlock.operation(source, "value:", P2Object.create(i));
	}

	/**
	 * Execute aBlock this number of times. C.f. a for loop. The iteration
	 * number is given as an argument to aBlock. The number starts at 0. Answers
	 * an array with a collection of all the results of the block executions.
	 * 
	 * @param aBlock
	 *            the block to be executed. It may take one argument
	 * @throws Ponder2Exception
	 */
	@Ponder2op("collect:")
	protected P2Array op_collect(P2Object source, P2Object aBlock)
			throws Ponder2Exception {
		P2Array result = new P2Array();
		for (int i = 0; i < number.intValue(); i++)
			result.add(aBlock.operation(source, "value:", P2Object.create(i)));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + number;
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
		number = new BigDecimal(xml.getAttribute("value"));
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof P2Number))
			return false;
		return number.equals(((P2Number) obj).number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return number.hashCode();
	}

}
