/**
 * Copyright 2008 Kevin Twidle, Imperial College, London, England.
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
 * Created on Feb 18, 2010
 *
 * $Log:$
 */

package eu.novi.ponder2;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;

/**
 * A semaphore object to enable synchronisation between managed objects.
 * Typically used when interacting with GUIs
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Semaphore implements ManagedObject {

	private java.util.concurrent.Semaphore semaphore;

	/**
	 * Creates a new semaphore with zero permits available. The first acquire
	 * will block the task.
	 */
	@Ponder2op("create")
	Semaphore() {
		this.semaphore = new java.util.concurrent.Semaphore(0);
	}

	/**
	 * Creates a new semaphore with anInteger permits available.
	 * 
	 * @param initial
	 */
	@Ponder2op("create:")
	Semaphore(int initial) {
		this.semaphore = new java.util.concurrent.Semaphore(initial);
	}

	/**
	 * Tries to acquire a permit. If none are available then the task will hang
	 * until one becomes available
	 */
	@Ponder2op("wait")
	protected void acquire() {
		semaphore.acquireUninterruptibly();
	}

	/**
	 * Releases the semaphore ay incrementing the number of permits available.
	 * If a task is blocked on this semaphore then that task is released instead
	 * of increasing the number of permits.
	 */
	@Ponder2op("release")
	protected void release() {
		semaphore.release();
	}

}
