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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.P2Serializable;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.parser.XMLParser;

/**
 * Acts as a closure encompassing PonderTalk statements. Any variables used by
 * the statements within the block are untouched once the block is created.
 * Blocks may have arguments that are handed in when they are executed. When a
 * block is executed it returns returns the value of the last statement executed
 * within the block. c.f. function calls.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class P2Block extends P2Object implements ManagedObject {

	private Map<String, P2Object> variables;
	private Map<String, P2Object> testVariables;
	private P2Hash extraVars;
	private TaggedElement block;

	// Optimisation information. We only need to work these values out once. We
	// can do them when the block is first executed.
	private boolean firstTime;
	private int argCount;
	private String[] argName;
	private TaggedElement code;
	private P2Block errorBlock;

	/**
	 * constructor for Ponder2 serialisation
	 */
	protected P2Block() {
	}

	public P2Block(Map<String, P2Object> variables, TaggedElement block) {
		this.variables = new HashMap<String, P2Object>(variables);
		this.testVariables = null;
		this.block = block;
		this.firstTime = true;
		this.errorBlock = null;
		this.extraVars = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.parser.P2Value#asString()
	 */
	@Override
	public P2Block asBlock() throws Ponder2ArgumentException {
		return this;
	}

	/**
	 * Returns true if aHash contains entries that match all the names of the
	 * arguments that the block requires. Otherwise returns false.
	 * 
	 * @param aHash
	 *            the named arguments for the block
	 * @return a Ponder2 boolean, true if the arguments match the hash
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("hasArgs:")
	protected boolean operation_hasArgs(P2Object source, P2Object aHash)
			throws Ponder2Exception {
		setup();
		for (int i = 0; i < argName.length; i++) {
			if (!(aHash.operation(source, "has:", argName[i]).asBoolean()))
				return false;
		}
		return true;
	}

	/**
	 * The block is executed with the values of its arguments being taken by
	 * name from aHash. Answers the value of the last statement executed by the
	 * block. Throws an error if aHash does not satisfy the block's arguments.
	 * 
	 * @param aHash
	 *            the named arguments for the block
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("valueHash:")
	public P2Object operation_valueHash(P2Object source, P2Object aHash)
			throws Ponder2Exception {
		setup();
		P2Object[] args = new P2Object[argCount];
		try {
			for (int i = 0; i < argCount; i++) {
				args[i] = aHash.operation(source, "at:", argName[i]);
			}
			return execute(source, args);
		} catch (Ponder2ArgumentException e) {
			e.addXML(block);
			throw e;
		}
	}

	/**
	 * The block is executed with the values in aHash being added to the block's
	 * environment variables. Variables in aHash will overwrite environment
	 * variables with the same name. The values are added for this call only and
	 * do not persist. Answers the value of the last statement executed by the
	 * block.
	 * 
	 * @param aHash
	 *            the named arguments for the block
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("valueVars:")
	public P2Object operation_valueVars(P2Object source, P2Object aHash)
			throws Ponder2Exception {
		setup();
		try {
			if (aHash instanceof P2Hash) {
				extraVars = (P2Hash) aHash;
			} else {
				P2Array array = (P2Array) aHash.operation(source, "asArray");
				extraVars = array.asHash();
			}
			return execute(source);
		} catch (Ponder2ArgumentException e) {
			e.addXML(block);
			throw e;
		}
	}

	/**
	 * executes the block with no arguments. Answers with the result of the last
	 * statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("value")
	public P2Object operation_value0(P2Object source) throws Ponder2Exception {
		return execute(source);
	}

	/**
	 * Executes the block with one argument: arg0. Answers with the result of
	 * the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param arg0
	 *            the first argument
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("value:")
	public P2Object operation_value1(P2Object source, P2Object arg0)
			throws Ponder2Exception {
		return execute(source, arg0);
	}

	/**
	 * Executes the block with two arguments: arg0 and arg1. Answers with the
	 * result of the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param arg0
	 *            the first argument
	 * @param arg1
	 *            the second argument
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("value:value:")
	public P2Object operation_value2(P2Object source, P2Object arg0,
			P2Object arg1) throws Ponder2Exception {
		return execute(source, arg0, arg1);
	}

	/**
	 * Executes the block with three arguments: arg0, arg1 and arg2. Answers
	 * with the result of the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param arg0
	 *            the first argument
	 * @param arg1
	 *            the second argument
	 * @param arg2
	 *            the third argument
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("value:value:value:")
	public P2Object operation_value3(P2Object source, P2Object arg0,
			P2Object arg1, P2Object arg2) throws Ponder2Exception {
		return execute(source, arg0, arg1, arg2);
	}

	/**
	 * Executes the block with anArray of arguments. Answers with the result of
	 * the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param anArray
	 *            the Ponder2 array of arguments to be used
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	@Ponder2op("values:")
	public P2Object operation_array(P2Object source, P2Array anArray)
			throws Ponder2Exception {
		return execute(source, anArray.asArray());
	}

	/**
	 * Executes the block with anArray of arguments. Answers with the result of
	 * the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param vars
	 *            extra vars to be added to the environment
	 * @param anArray
	 *            the Ponder2 array of arguments to be used
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	public P2Object operation_array(P2Object source, P2Hash vars,
			P2Array anArray) throws Ponder2Exception {
		extraVars = vars;
		return execute(source, anArray.asArray());
	}

	/**
	 * While the receiver is true, keep executing aBlock with no arguments
	 * 
	 * @param source
	 *            the originator of the command
	 * @param aBlock
	 *            the block to be executed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("whileTrue:")
	protected void whileTrue(P2Object source, P2Object aBlock)
			throws Ponder2Exception {
		while (execute(source) == P2Boolean.True)
			aBlock.operation(source, "value");
	}

	/**
	 * While the receiver is false, keep executing aBlock with no arguments
	 * 
	 * @param source
	 *            the originator of the command
	 * @param aBlock
	 *            the block to be executed
	 * @throws Ponder2Exception
	 */
	@Ponder2op("whileFalse:")
	protected void whileFalse(P2Object source, P2Object aBlock)
			throws Ponder2Exception {
		while (execute(source) == P2Boolean.False)
			aBlock.operation(source, "value");
	}

	/**
	 * Executes the block with anArray of arguments. Answers with the result of
	 * the last statement executed by the block.
	 * 
	 * @param source
	 *            the originator of the command
	 * @param args
	 *            the array of arguments to be used
	 * @return the result of the block execution
	 * @throws Ponder2Exception
	 */
	protected P2Object execute(P2Object source, P2Object... args)
			throws Ponder2Exception {
		setup();
		// Copy the closure information
		P2Hash variables = new P2Hash(this.variables);
		testVariables = variables;
		variables.put("Variables", variables);
		// Add extra variables, if any
		if (extraVars != null) {
			variables.putAll(extraVars);
			// only use them once
			extraVars = null;
		}
		// Get the args (if any) for the xml code
		if (argCount > 0) {
			// We should have args, grab them
			if (args.length < argCount)
				throw new Ponder2ArgumentException(
						"Not enough arguments for block. " + args.length
								+ " given, " + argCount + " expected.");
			// Set the args up as variables for the parser
			for (int i = 0; i < argCount; i++) {
				variables.put(argName[i], args[i]);
			}
		}
		P2Object result;
		try {
			result = new XMLParser(variables).execute(source, code);
		} catch (Ponder2Exception e) {
			if (errorBlock == null)
				throw e;
			result = errorBlock.operation(source, "value:", P2Object.create(e));
		}
		return result;
	}

	/**
	 * associates anErrorBlock with the receiver. The error block is executed if
	 * a Ponder2 error occurs in the receiver. The error block is given one
	 * P2Error argument which contains the error details.
	 * 
	 * @param anErrorBlock
	 */
	@Ponder2op("onError:")
	protected P2Object onError(P2Block anErrorBlock) {
		this.errorBlock = anErrorBlock;
		return this;
	}

	/**
	 * Sets up the local fields for faster future execution. Is only called when
	 * the block is to be executed. If it is never executed then this method
	 * need never be called.
	 * 
	 */
	private void setup() {
		if (!firstTime)
			return;
		firstTime = false;

		// Get and save the code block
		code = (TaggedElement) block.getChild(1);

		// Ok, grab the argument block
		TaggedElement child = (TaggedElement) block.getChild(0);
		argCount = child.elements();
		if (argCount == 0)
			return;

		// We have some args, get the names into an array for fast access later
		argName = new String[argCount];
		for (int i = 0; i < argCount; i++) {
			TaggedElement arginfo = (TaggedElement) child.getChild(i);
			argName[i] = arginfo.getAttribute("name");
		}
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
		xml.add(P2Object.create(variables).writeXml(written));
		xml.add(block);
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
		variables = new HashMap<String, P2Object>();
		TaggedElement child = (TaggedElement) xml.getChild(0);
		P2Object obj = P2Object.fromXml(child, read);
		variables = obj.asHash().asMap();
		block = (TaggedElement) xml.getChild(1);
		firstTime = true;
		return this;
	}

	/**
	 * Used for testing. Returns the value of the named variable.
	 * 
	 * @param var
	 *            the name of the variable
	 * @return the value of the named variable
	 */
	public P2Object getVariable(String var) {
		return testVariables.get(var);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Need to work out how to dump the variables properly without stack
		// overflow
		// return variables.toString() + block.toString();
		// Need toString(Set<P2Object> track);
		return block.toString();
	}

}
