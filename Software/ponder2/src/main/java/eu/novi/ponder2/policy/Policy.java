/**
 * Created on Jul 10, 2005
 * Copyright 2005 Imperial College, London, England.
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
 * $Log: Policy.java,v $
 * Revision 1.23  2005/12/09 10:34:38  kpt
 * Policy actions sorted out
 *
 * Revision 1.22  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.21  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.20  2005/11/15 15:00:51  kpt
 * ExecuteSetup introduced, more result feedback
 *
 * Revision 1.19  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.18  2005/11/13 23:27:30  kpt
 * Added time manager
 *
 * Revision 1.17  2005/11/12 18:02:23  kpt
 * Sorted out return values from managed objects. MO conditions now work
 *
 * Revision 1.16  2005/11/12 16:59:55  kpt
 * Added conditionals to Policies
 *
 * Revision 1.15  2005/11/07 22:08:50  kpt
 * Added action to policies
 *
 * Revision 1.14  2005/11/03 04:23:46  kpt
 * More restore and tidying done
 *
 * Revision 1.13  2005/10/29 20:22:56  kpt
 * Tidied up managed object command execution a little
 *
 * Revision 1.12  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.11  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.10  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.9  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.8  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.7  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.6  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.5  2005/10/08 23:05:44  kpt
 * Airline Demonstration almost complete
 *
 * Revision 1.4  2005/10/06 10:59:18  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.3  2005/10/01 20:50:53  kpt
 * Changed result type to new Result class
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2.policy;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.objects.P2Object;

/**
 * This class implements policies within the SMC. It is the base class for more
 * specialised policy classes and manages all the common work.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class Policy implements ManagedObject {

	/**
	 * true if this policy is active and can recognise events
	 */
	private boolean active = false;
	/**
	 * true if this policy has been attached to a managed object
	 */
	private boolean attached = false;

	public static String defaultDomain = SelfManagedCell.PolicyDomain;

	/**
	 * initialises the base class for the underlying specific policy class
	 */
	protected Policy() {
	}

	/**
	 * makes the policy active if aBoolean is true, else makes it inactive. Once
	 * active it can receive events of the correct type. If the policy has not
	 * been attached anywhere (see <code>attach:</code> it will be attached to
	 * the root domain.
	 * 
	 * @param active
	 *            true if the policy should be activated
	 */
	public void setActive(boolean active) {
		this.active = active;
		if (active && !isAttached())
			attach(SelfManagedCell.RootDomain);
		System.out.println("Policy: active is set to " + active);
	}

	/**
	 * checks to see if this policy is active
	 * 
	 * @return true if this policy is active
	 */
	public boolean isActive() {
		return active;
	}

	public static String getDefaultDomain() {
		return defaultDomain;
	}

	/**
	 * makes the policy active if aBoolean is true else makes the policy
	 * inactive. Returns aBoolean.
	 * 
	 * @param aBoolean
	 *            true to make the policy active
	 * @return true if this policy is now active
	 */
	@Ponder2op("active:")
	protected P2Object operation_active(boolean aBoolean) {
		setActive(aBoolean);
		return P2Object.create(aBoolean);
	}

	/**
	 * attaches this policy to aManagedObject. This policy may be attached to
	 * more than one managed object. This command forms part of the proximity
	 * event bus.
	 * 
	 * @param aManagedObject
	 *            the managed object that the policy should be attached to
	 */
	@Ponder2op("attach:")
	protected abstract void attach(P2Object aManagedObject);

	/**
	 * @return the attached
	 */
	protected boolean isAttached() {
		return attached;
	}

	/**
	 * @param attached
	 *            the attached to set
	 */
	protected void setAttached(boolean attached) {
		this.attached = attached;
	}

}
