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
 * Created on Jul 10, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.policy;

import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class AuthorisationModule {
	public final static short PEP1 = 1;
	public final static short PEP2 = 2;
	public final static short PEP3 = 3;
	public final static short PEP4 = 4;

	protected P2Object rootDomain;

	public void setRootDomain(P2Object rootDomain) {
		this.rootDomain = rootDomain;
	}

	/**
	 * Method that intercepts request calls at PEP1 and PEP2 points.
	 * 
	 * @param pepType
	 *            either PEP1 or PEP2
	 * @param focus
	 *            's' or 't' for source or target
	 * @param holder
	 *            the holder containing the policy for this operation
	 * @param source
	 *            the ID of the subject
	 * @param target
	 *            the ID of the target
	 * @param operation
	 *            the operation invoked
	 * @param args
	 *            the arguments of the operation
	 * @throws Ponder2AuthorizationException
	 *             if the operation is not authorised
	 */
	abstract public void request(short pepType, char focus,
			AuthPolicyHolder holder, P2Object source, P2Object target,
			String operation, P2Object[] args)
			throws Ponder2AuthorizationException;

	/**
	 * Method that intercepts calls at PEP3 and PEP4 points.
	 * 
	 * @param pepType
	 *            either PEP3 or PEP4
	 * @param focus
	 *            's' or 't' for source or target
	 * @param holder
	 *            the holder containing the policy for this operation
	 * @param source
	 *            the ID of the subject
	 * @param target
	 *            the ID of the target
	 * @param operation
	 *            the operation invoked
	 * @param args
	 *            the arguments of the operation
	 * @param returnedValue
	 *            the value returned by the operation execution
	 * @throws Ponder2AuthorizationException
	 *             if the operation is not authorised
	 */
	abstract public void reply(short pepType, char focus,
			AuthPolicyHolder holder, P2Object source, P2Object target,
			String operation, P2Object[] args, P2Object returnedValue)
			throws Ponder2AuthorizationException;

}
