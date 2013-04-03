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

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.PonderTalkInterface;

/**
 * A Managed Object that parses and executes PonderTalk. PonderTalk can be
 * supplied as strings from other PonderTalk statements or as strings received
 * over RMI. The PonderTalk string is executed and the result is returned as a
 * string. If an error occurs then a Ponder2Exception or a RemoteException is
 * thrown.
 * 
 * This managed object can also be used as a stand-alone program to send
 * PonderTalk to a remote SMC using RMI.<br>
 * 1. Create this managed object in an SMC giving it a RMI name to use. e.g.
 * MyPonder2<br>
 * 2. Send PonderTalk to the SMC using the Ponder2 JAR file e.g.<br>
 * java -cp ponder2.jar eu.novi.ponder2.PonderTalk MyPonder2 any PonderTalk
 * statement
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class PonderTalk implements ManagedObject {

	private boolean trace = false;
	private final P2Object myP2Object;
	private String rmiName;

	/**
	 * Creates a PonderTalk managed object
	 * 
	 * @param myP2Object
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("create")
	public PonderTalk(P2Object myP2Object) {
		this.myP2Object = myP2Object;
		this.rmiName = null;
	}

	/**
	 * Creates a PonderTalk managed object with rmiName as the RMI name that
	 * will be listened to
	 * 
	 * @param myP2Object
	 * @param rmiName
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("create:")
	PonderTalk(P2Object myP2Object, String rmiName)
			throws Ponder2ArgumentException {
		this.myP2Object = myP2Object;
		this.rmiName = rmiName;
		try {
			System.out.println("Trying");
			Naming.rebind(rmiName, new PonderTalkReceiver());
			System.out.println("NO man");
		} catch (Exception e) {
			throw new Ponder2ArgumentException(e.getMessage());
		}
	}

	/**
	 * Binds this managed object to a new RMI name. Any previous bindings are
	 * maintained.
	 * 
	 * @param rmiName
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("rmi:")
	void bind(String rmiName) throws Ponder2ArgumentException {
		this.rmiName = rmiName;
		try {
			Naming.rebind(rmiName, new PonderTalkReceiver());
		} catch (Exception e) {
			throw new Ponder2ArgumentException(e.getMessage());
		}
	}

	/**
	 * turns tracing on if aBoolean is true else turns it off.
	 * 
	 * @param aBoolean
	 */
	@Ponder2op("trace:")
	public void trace(boolean aBoolean) {
		trace = aBoolean;
	}

	/**
	 * tests the RMI interface without having to run a separate SMC
	 * 
	 * @param aString
	 *            the PonderTalk to be executed
	 * @return the result of the execution as a String
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("test:")
	public String test(String aString) throws Ponder2OperationException {
		String result;

		if (rmiName == null)
			throw new Ponder2OperationException(
					"PonderTalk has not been given an RMI name");
		try {
			PonderTalkInterface pt = (PonderTalkInterface) Naming
					.lookup(rmiName);
			result = pt.execute(aString);
		} catch (Exception e) {
			throw new Ponder2OperationException("remote failure: "
					+ e.getMessage());
		}

		return result;
	}

	private class PonderTalkReceiver extends UnicastRemoteObject implements
			PonderTalkInterface {

		/**
		 * @throws RemoteException
		 */
		protected PonderTalkReceiver()throws RemoteException {
			System.out.println("created the pondertalkreceiver");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see eu.novi.ponder2.PonderTalkInterface#execute(java.lang.String)
		 */
		public String execute(String ponderTalk) throws RemoteException {
			try {

				P2Object value = executePonderTalk(ponderTalk);
				return value.toString();
			} catch (Exception e) {
				throw new RemoteException(e.getMessage());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see eu.novi.ponder2.PonderTalkInterface#compile(java.lang.String)
		 */
		public String compile(String ponderTalk) throws RemoteException {
			try {
				return P2Compiler.parse(ponderTalk);
			} catch (Exception e) {
				throw new RemoteException(e.getMessage());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see eu.novi.ponder2.PonderTalkInterface#executeXML(java.lang.String)
		 */
		public String executeXML(String p2xml) throws RemoteException {
			try {
				P2Object value = new XMLParser().execute(myP2Object, p2xml);
				return value.toString();
			} catch (Exception e) {
				throw new RemoteException(e.getMessage());
			}
		}
	}

	/**
	 * Compiles and executes aPonderTalkString and returns the result.
	 * 
	 * @param aPonderTalkString
	 *            the PonderTalk to be executed
	 * @return the object that results from the PonderTalk being executed
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("execute:")
	public P2Object executePonderTalk(String aPonderTalkString)
			throws Ponder2OperationException {
		try {
			if (trace) {
				System.out.println("PonderTalk executing: ");
				System.out.println("-------------------");
				System.out.println(aPonderTalkString);
				System.out.println("-------------------");
			}
			String p2xml = P2Compiler.parse(aPonderTalkString);
			return new XMLParser().execute(myP2Object, p2xml);
		} catch (Exception e) {
			throw new Ponder2OperationException("Caught P2 Exception: "
					+ e.getMessage());
		}
	}

	public static void main(String args[]) {
		Response response = send(args);
		System.out.println(response.message);
		System.exit(response.ok ? 0 : 1);
	}

	public static Response send(String args[]) {
		Response response = new Response();
		if (args.length < 2) {
			response.message = "Too few arguments. Need: RMIname pondertalk statement.";
			response.ok = false;
			return response;
		}
		String rmiName = args[0];
		String ponderTalk = "";
		for (int i = 1; i < args.length; i++) {
			ponderTalk += " " + args[i];
		}
		try {
			PonderTalkInterface pt = (PonderTalkInterface) Naming
					.lookup(rmiName);
			response.message = pt.execute(ponderTalk);
		} catch (Exception e) {
			response.message = "Remote PonderTalk failure: " + e.getMessage();
			response.ok = false;
		}
		return response;
	}

	public static class Response {
		public boolean ok = true;
		public String message = "";
	}
}
