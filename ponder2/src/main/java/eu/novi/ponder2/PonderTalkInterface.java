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
 * Created on Jun 18, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for sending PonderTalk to the SMC. Retruns the result as a string
 * if it is sucessful otherwise it throws a RemoteException
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public interface PonderTalkInterface extends Remote {

	/**
	 * Takes a PonderTalk string, compiles and executes it. Returns the result
	 * at a string.
	 * 
	 * @param ponderTalk
	 *            a string containing one or more PonderTalk statements
	 *            separated by full-stops (periods).
	 * @return the result of the operation as a string
	 * @throws RemoteException
	 *             if something goes wrong
	 */
	public String execute(String ponderTalk) throws RemoteException;

	/**
	 * Takes a PonderTalk string and compiles it to internal XML. The XML is
	 * returned as a string.
	 * 
	 * @param ponderTalk
	 *            a string containing one or more PonderTalk statements
	 *            separated by full-stops (periods).
	 * @return the XML result of the compilation as a string
	 * @throws RemoteException
	 *             if something goes wrong
	 */
	public String compile(String ponderTalk) throws RemoteException;

	/**
	 * Takes a Ponder2 internal XML execution structure as a string, executes it
	 * and returns the result as a string.
	 * 
	 * @param xml
	 *            a string containing a single XML structure to be executed
	 * @return the result of the operation as a string
	 * @throws RemoteException
	 *             if something goes wrong
	 */
	public String executeXML(String xml) throws RemoteException;

}
