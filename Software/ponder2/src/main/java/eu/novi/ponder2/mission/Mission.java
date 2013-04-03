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
 * Created on Apr 2, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2.mission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.objects.P2Object;

/**
 * Describes a mission which can be instantiated at a Mission Controller with a
 * set of given interfaces.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Mission implements ManagedObject {

	@SuppressWarnings("unused")
	private String name;
	Map<String, P2Object> policies;
	P2Object startBlock;
	P2Object stopBlock;
	String pname = "policy";
	int pnumber = 1;
	private Set<String> interfaces;

	/**
	 * Creates a new mission with no values set
	 * 
	 */
	@Ponder2op("create")
	public Mission() {
		name = "unknown";
		policies = new HashMap<String, P2Object>();
		interfaces = new HashSet<String>();
		startBlock = stopBlock = P2Object.create();
	}

	/**
	 * Names the mission aName
	 * 
	 * @param aName
	 */
	@Ponder2op("name:")
	void name(String aName) {
		name = aName;
	}

	/**
	 * Adds an interface requirement to the mission
	 * 
	 * @param anInterface
	 */
	@Ponder2op("interface:")
	void addInterface(String anInterface) {
		interfaces.add(anInterface);
		;
	}

	/**
	 * Returns the set of interfaces specified to the mission
	 * 
	 * @return
	 */
	@Ponder2op("interfaces")
	P2Object getInterfaces() {
		return P2Object
				.create(interfaces.toArray(new String[interfaces.size()]));
	}

	/**
	 * Adds an unnamed policy in the form of aBlock to the mission
	 * 
	 * @param aBlock
	 */
	@Ponder2op("policy:")
	void policy(P2Object aBlock) {
		policyIs(pname + pnumber++, aBlock);
	}

	/**
	 * Adds a policy block, aBlock, with the name aName
	 * 
	 * @param aName
	 * @param aBlock
	 */
	@Ponder2op("policy:is:")
	protected void policyIs(String aName, P2Object aBlock) {
		policies.put(aName, aBlock);
	}

	/**
	 * Returns a hash of policies given to the mission. The has is indexed by
	 * the policy name
	 * 
	 * @return
	 */
	@Ponder2op("policies")
	P2Object policies() {
		return P2Object.create(policies);
	}

	/**
	 * executes aBlock when the mission is started
	 * 
	 * @param aBlock
	 */
	@Ponder2op("onStart:")
	void startBlock(P2Object aBlock) {
		startBlock = aBlock;
	}

	/**
	 * returns the start block
	 * 
	 * @return
	 */
	@Ponder2op("onStart")
	P2Object startBlock() {
		return startBlock;
	}

	/**
	 * executes aBlock when the mission is stopped
	 * 
	 * @param aBlock
	 */
	@Ponder2op("onStop:")
	void stopBlock(P2Object aBlock) {
		stopBlock = aBlock;
	}

	/**
	 * returns the stop block
	 * 
	 * @return
	 */
	@Ponder2op("onStop")
	P2Object stopBlock() {
		return stopBlock;
	}

	/**
	 * Returns the policy with the name aName
	 * 
	 * @param aName
	 * @return
	 */
	@Ponder2op("at:")
	P2Object at(String aName) {
		return policies.get(aName);
	}

}
