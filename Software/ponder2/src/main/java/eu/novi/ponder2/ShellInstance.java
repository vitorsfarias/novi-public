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
 * Created on Jul 20, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import eu.novi.ponder2.Commands;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2ResolveException;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import eu.novi.ponder2.policy.EventListener;

import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.Path;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.ShellInstanceP2Adaptor;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.policy.PolicyP2Adaptor;

/**
 * The actual class that an interactive user interacts with. One ShellInstance
 * is spawned off in a new thread for each new connection received.
 * 
 * @author Kevin Twidle
 * @version $Id: Shell.java 468 2007-07-11 09:51:47Z kpt $
 */
public class ShellInstance implements Runnable, ManagedObject {
	private static final transient Logger log = LoggerFactory.getLogger(ShellInstance.class);
	/**
	 * the internal commands recognised by the shell
	 */
	protected static enum Commands {
		help, read, ls, cd, mkdom, mkdir, md, rm, rmdom, rmdir, rd, mkpolicy, mv, ln, lp, apply, ap, policy, event;
	}

	/**
	 * the current domain cf current directory in Unix
	 */
	private P2Object currentDomain;
	/**
	 * the full path name used to get to the current domain
	 */
	private Path currentPath;
	/**
	 * the current command being executed
	 */
	private String currentCommand;
	/**
	 * The managed object that represents this shell in the domain structure
	 */
	private P2Object shellmo;
	private PrintWriter out;
	private BufferedReader in;
	private P2Hash variables;
	private P2Object root;

	/**
	 * creates a new shell instance with a particular socket to be used for
	 * communication
	 * 
	 * @param sc
	 *            the socket to be used by this shell instance
	 * @param shellDomain
	 *            the default domains for shells
	 * @param shellNumber
	 *            the number of this shell connection
	 * @throws Ponder2OperationException
	 * @throws Ponder2ArgumentException
	 */
	ShellInstance(Socket socket, P2Object root, P2Object shellDomain,
			int shellNumber) {

		this.root = root;

		// Sort out the I/O streams
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the Shell connection");
		}

		// Add this new shell to the domain hierarchy
		ShellInstanceP2Adaptor shellAdaptor = new ShellInstanceP2Adaptor();
		shellAdaptor.setObj(this);
		shellmo = shellAdaptor;

		try {
			shellDomain.operation(SelfManagedCell.RootDomain, "at:put:",
					P2Object.create("shell" + shellNumber), shellmo);
		} catch (Ponder2Exception e) {
			System.err.println("Shell setup error - Should never happen!");
			e.printStackTrace();
		}

		// Set up our initial values
		currentDomain = root;
		currentPath = new Path("/");

		// Persistent variables for this instance of the shell
		variables = new P2Hash();
		variables.put("Variables", variables);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {

			out.print("Ponder2 Shell "
					+ SelfManagedCell.SVNRevision.substring(1,
							SelfManagedCell.SVNRevision.length() - 2)
					+ " "
					+ SelfManagedCell.SVNDate.substring(1,
							SelfManagedCell.SVNDate.length() - 2)
					+ "\r\nNo wild chars yet\r\n");
			log.info("Ponder2 Shell "
					+ SelfManagedCell.SVNRevision.substring(1,
							SelfManagedCell.SVNRevision.length() - 2)
					+ " "
					+ SelfManagedCell.SVNDate.substring(1,
							SelfManagedCell.SVNDate.length() - 2)
					+ "\r\nNo wild chars yet\r\n");

			StringBuffer buffer = new StringBuffer();
			while (true) {
				buffer.setLength(0);
				out.print(currentPath + " $ ");
				out.flush();
				String line = in.readLine();
				if (line == null)
					break;
				line = trim(line);

				String result;
				try {
					try {
						result = doCommand(line);
					} catch (IllegalArgumentException e) {
						result = doPonderTalk(line);
					}
				} catch (Ponder2Exception e) {
					System.out.println("Ponder2 Exception: " + e.getMessage());
					log.info("Ponder2 Exception: " + e.getMessage());
					if (SelfManagedCell.SystemTrace)
						e.printStackTrace();
					
					Writer writer = new StringWriter();
					PrintWriter printWriter = new PrintWriter(writer);
					e.printStackTrace(printWriter);
					log.info("Command failed:" + e);
					log.info("Failure is:"+ writer.toString());
					result = "Command failed:" + e;
				} catch (Exception e) {
					//e.printStackTrace();
					Writer writer = new StringWriter();
					PrintWriter printWriter = new PrintWriter(writer);
					e.printStackTrace(printWriter);
					log.info("Command failed:" + e);
					log.info("Failure is:"+writer.toString());
					result = "Command failed:" + e;
				}

				if (result != null && result.length() > 0
						&& result.charAt(result.length() - 1) != '\n')
					result = result + "\r\n";
				out.print(result);
				log.info(result);
				out.flush();
			}
		} catch (Exception e) {
			System.out.println("Shell exception - " + e.getMessage());
			log.info("Shell exception - " + e.getMessage());
		}
		System.out.println("Lost connection");
		log.info("Lost connection");
		try {
			in.close();
		} catch (IOException e1) {
		}
		out.close();
	}

	private String doPonderTalk(String line) throws Ponder2Exception {
		StringBuffer buffer = new StringBuffer(line);
		while (buffer.charAt(buffer.length() - 1) != '.') {
			try {
				out.print(currentPath + " [PonderTalk or . ] $ ");
				log.info(currentPath + " [PonderTalk or . ] $ ");
				out.flush();
				line = in.readLine();
				System.out.println(line);
				log.info(line + "\n");
				buffer.append('\n');
				buffer.append(trim(line));
			} catch (IOException e) {
				log.info(e.getMessage());
				throw new Ponder2ArgumentException("Caught IO Exception: "
						+ e.getMessage());
			}
		}
		// We have reached the end the PonderTalk
		String p2xml = P2Compiler.parse(buffer.toString());
		P2Object value = new XMLParser(variables).execute(shellmo, p2xml);
		return value != null ? value.toString() : "";
	}

	private String trim(String line) throws IOException {
		StringBuffer buf = new StringBuffer();
		int lindex = 0;
		while (lindex < line.length()) {
			char ch = line.charAt(lindex);
			// System.out.print((0 + ch) + " ");
			if (ch > 512) {
				throw new IOException("bad character");
			} else if (ch == 0x03 || ch == 0x04 || ch == 0x1a)
				throw new IOException("EOF Character");
			else if (ch == 0x08) {
				// Backspace
				if (buf.length() > 0)
					buf.setLength(buf.length() - 1);
			} else {
				buf.append(ch);
			}
			lindex++;
		}
		return buf.toString().trim();
	}

	/**
	 * executes a command once it has been read in
	 * 
	 * @param line
	 * @return
	 * @throws Ponder2Exception
	 */
	private String doCommand(String line) throws IllegalArgumentException,
			Ponder2Exception {
		String output = "";
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() <= 0)
			return output;
		currentCommand = st.nextToken();
		if (currentCommand.startsWith("#"))
			return output;
		System.out.println("Command is " + currentCommand);
		log.info("Command is " + currentCommand);
		switch (Commands.valueOf(currentCommand)) {
		// List
		case ls:
			output = doCommandLS(st);
			break;
		// Change Directory
		case cd:
			output = doCommandCD(st);
			break;
		// Make domain
		case mkdom:
		case mkdir:
		case md:
			output = doCommandMKDOM(st);
			break;
		// Remove domain or object
		case rmdom:
		case rmdir:
		case rd:
		case rm:
			output = doCommandRM(st);
			break;
		// Move
		// case mv:
		// result = doCommandMV(st);
		// break;
		// Apply policy
		case ap:
		case apply:
			output = doCommandAP(st);
			break;
		// List Policies
		case lp:
			output = doCommandLP(st);
			break;
		case event:
			output = doCommandEvent(st);
			break;
		case read:
			output = doCommandRead(st);
			break;
		// Help
		case help:
			output = doCommandHelp(st);
			break;
		default:
			output = currentCommand + ": Unimplemented command";
		}
		return output;
	}

	/**
	 * applies a named policy to a named managed object
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return any errors generated as a string
	 * @throws Ponder2Exception
	 */
	private String doCommandAP(StringTokenizer st) throws Ponder2Exception {
		// TODO should use result
		StringBuffer output = new StringBuffer();
		if (st.countTokens() < 2) {
			return currentCommand + ": not enough arguments";
		}

		// Find the policy
		String policyName = st.nextToken();
		P2Object pvalue;
		try {
			pvalue = Util.resolve(currentDomain, policyName);
		} catch (Ponder2ResolveException e) {
			// It may be in the Policy domain
			try {
				pvalue = Util.resolve(currentDomain, "/policy/" + policyName);
			} catch (Ponder2ResolveException e1) {
				return policyName + ": policy not found";
			}
		}

		// We do actually have a policy, don't we?
		if (!(pvalue instanceof PolicyP2Adaptor)) {
			return policyName + ": not a policy";
		}

		PolicyP2Adaptor policy = (PolicyP2Adaptor) pvalue;

		// Ok, now deal with the objects that this policy is to be applied to
		while (st.hasMoreTokens()) {
			String arg = st.nextToken();
			P2Object mo = Util.resolve(currentDomain, arg);
			if (mo != null)
				mo.getManagedObject().applyPolicy(
						(EventListener) policy.getObj());
			else {
				output.append(arg + ": not found\r\n");
			}
		}
		return output.toString();
	}

	/**
	 * changes the curent domain. The current domain is used as a starting point
	 * for many other commands involving names of managed objects.
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return any errors that might be flagged
	 * @throws Ponder2Exception
	 */
	private String doCommandCD(StringTokenizer st) throws Ponder2Exception {
		// TODO should use result
		String output = "";
		switch (st.countTokens()) {
		case 0:
			currentDomain = root;
			break;
		case 1:
			String arg = st.nextToken();
			P2Object p2Object = Util.resolve(currentPath.toString(), arg);
			if (p2Object == null) {
				output = currentCommand + ": " + arg + " does not exist";
			} else if (p2Object.getOID().isDomain()) {
				currentDomain = p2Object;
				currentPath.add(arg);
			} else {
				output = currentCommand + ": " + arg + " is not a domain";
			}
			break;
		default:
			output = currentCommand + ": too many parameters";
		}
		return output;
	}

	/**
	 * creates an event with arguments
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @return any errors that may have occured
	 * @throws Ponder2Exception
	 */
	private String doCommandEvent(StringTokenizer st) throws Ponder2Exception {
		StringBuffer pondertalk = new StringBuffer();
		if (!st.hasMoreTokens()) {
			throw new Ponder2ArgumentException(
					"usage: event <event name> args ...");
		}
		String eventTemplate = st.nextToken();
		while (st.hasMoreTokens()) {
			pondertalk.append(' ');
			pondertalk.append(st.nextToken());
		}
		return doPonderTalk("root/event/" + eventTemplate + " create: #( "
				+ pondertalk + " ).");
	}

	/**
	 * lists the command help information for interactive use with the shell
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @return the help information as a printable string
	 */
	private String doCommandHelp(StringTokenizer st) {
		String result = "";
		result += "cd [dom]\r\n";
		result += "   Change Directory\r\n";
		result += "help\r\n";
		result += "   Help\r\n";
		result += "ln obj ... dom   or  ln obj obj\r\n";
		result += "   Link\r\n";
		result += "ls [-l] [obj ...]\r\n";
		result += "   List\r\n";
		result += "mkdom dom ...\r\n";
		result += "   Make Domain\r\n";
		result += "rm [-f] obj ...\r\n";
		result += "   Remove\r\n";
		result += "ap policy obj ...\r\n";
		result += "   Apply Policy\r\n";
		result += "lp [obj ...]\r\n";
		result += "   List Policy\r\n";
		result += "event <template name> [arg ...]\r\n";
		result += "   Create event\r\n";
		result += "policy +|-[event] [policy ...]\r\n";
		result += "   Manipulate policy\r\n";
		result += "<objPathName> [att=val ...] [sub command [att=val ...]]\r\n";
		result += "   Send command to object\r\n";
		return result;
	}

	/**
	 * executes the list managed object command
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return the output string ready for display
	 * @throws Ponder2Exception
	 */
	private String doCommandLS(StringTokenizer st) throws Ponder2Exception {
		StringBuffer output = new StringBuffer();
		boolean longForm = false;
		boolean doneOne = false;
		while (st.hasMoreTokens()) {
			String arg = st.nextToken();
			if (arg.equals("-l")) {
				longForm = true;
				continue;
			}
			try {
				P2Object oid = Util.resolve(currentDomain, arg);
				doCommandLS(output, arg, oid, longForm);
			} catch (Ponder2ResolveException e) {
				output.append(arg + ": not found");
				output.append("\r\n");
			}
			doneOne = true;
		}
		if (!doneOne) {
			doCommandLS(output, currentPath.toString(), currentDomain, longForm);
		}
		return output.toString();
	}

	/**
	 * list information about a managed object. If the managed object is a
	 * domain the all the domain's managed objects are listed instead.
	 * 
	 * @param output
	 *            the buffer for adding printable output to
	 * @param name
	 *            the name of the managed object being listed
	 * @param oid
	 *            the OID of the managed object being listed
	 * @param longForm
	 *            true if longform (-l) output is required
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @throws Ponder2Exception
	 */
	private void doCommandLS(StringBuffer output, String name, P2Object oid,
			boolean longForm) throws Ponder2Exception {
		if (!oid.getOID().isDomain()) {
			doCommandLSwrite(output, name, oid, longForm);
		} else {
			// Run through the domain, it may be remote
			P2ObjectAdaptor domain = (P2ObjectAdaptor) oid;
			P2Object[] names = domain.operation(shellmo, "listNames").asArray();
			for (int i = 0; i < names.length; i++) {
				P2Object child = names[i];
				P2Object object = domain.operation(shellmo, "at:", child);
				doCommandLSwrite(output, child.asString(), object, longForm);
			}
		}
	}

	/**
	 * writes information about a managed object as a string
	 * 
	 * @param output
	 *            the buffer for adding printable output to
	 * @param name
	 *            the name of the managed object being listed
	 * @param oid
	 *            the OID of the managed object being listed
	 * @param longForm
	 *            true if longform (-l) output is required
	 */
	private void doCommandLSwrite(StringBuffer output, String name,
			P2Object oid, boolean longForm) {
		if (!longForm) {
			output.append(name);
			if (oid.getOID().isDomain())
				output.append("/");
		} else {
			output.append(name);
			output.append(": ");
			output.append(oid.getManagedObject().getP2Object().getClass()
					.toString());
			output.append("\r\n");
			output.append(oid.getOID().toXML());
		}
		output.append("\r\n");
	}

	/**
	 * lists the policies applying to events generated by a managed object
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return a list of policies in printable form
	 * @throws Ponder2Exception
	 */
	private String doCommandLP(StringTokenizer st) throws Ponder2Exception {
		StringBuffer output = new StringBuffer();
		boolean doneOne = false;
		while (st.hasMoreTokens()) {
			String name = st.nextToken();
			try {
				P2Object oid = Util.resolve(currentDomain, name);
				doCommandLP(output, name, oid);
			} catch (Ponder2ResolveException e) {
				output.append(name + ": not found");
				output.append("\r\n");
			}
			doneOne = true;
		}

		if (!doneOne) {
			doCommandLP(output, currentPath.toString(), currentDomain);
		}
		return output.toString();
	}

	/**
	 * lists a single policy and its contents
	 * 
	 * @param output
	 *            the buffer for adding printable output to
	 * @param name
	 *            the name of the policy being listed
	 * @param oid
	 *            the OID of the policy being listed
	 */
	private void doCommandLP(StringBuffer output, String name, P2Object oid) {
		output.append(name);
		output.append(": ");
		output.append(oid.getClass().toString());
		for (EventListener policy : oid.getManagedObject().getEventListeners()) {
			output.append("\r\n" + "    -> ");
			output.append(policy.toString());
		}
		output.append("\r\n");
	}

	/**
	 * creates a new domain and places it into the domain structure
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return
	 * @throws Ponder2Exception
	 */
	private String doCommandMKDOM(StringTokenizer st) throws Ponder2Exception {
		String output = "";
		while (st.hasMoreTokens()) {
			String arg = st.nextToken();
			Path path = new Path(arg);
			P2Object p2Object;
			try {
				p2Object = Util.resolve(currentDomain, path);
				output = output + arg + " already exists\r\n";
				continue;
			} catch (Ponder2ResolveException e) {
			}
			Path parent = path.parent();
			try {
				p2Object = Util.resolve(currentDomain, parent);
			} catch (Ponder2ResolveException e) {
				output = output + arg + ": cannot access parent domain\r\n";
				continue;
			}
			if (!p2Object.getOID().isDomain()) {
				output += path.toString() + "is not a domain\r\n";
				continue;
			}

			DomainP2Adaptor adaptor = new DomainP2Adaptor(shellmo, "create");
			p2Object.operation(shellmo, "at:put:",
					P2Object.create(path.child()), adaptor);
		}
		return output;
	}

	/**
	 * reads XML structure commands from a file or resource
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @return any errors returned in the result structure or and empty string
	 * @throws Ponder2Exception
	 */
	private String doCommandRead(StringTokenizer st) throws Ponder2Exception {
		while (st.hasMoreTokens()) {
			String file = st.nextToken();
			Util.parseFile(shellmo, file);
		}
		return "";
	}

	/**
	 * removes an object from a domain. Domains are only removed if the -f flag
	 * is given.
	 * 
	 * @param st
	 *            the string tokenizer to enable the arguments to be read and
	 *            consumed
	 * @param result
	 *            the standard result structure for OIDs and errors
	 * @return any errors that might be flagged
	 * @throws Ponder2Exception
	 */
	private String doCommandRM(StringTokenizer st) throws Ponder2Exception {
		String output = "";
		while (st.hasMoreTokens()) {
			String arg = st.nextToken();
			Path path = new Path(arg);
			P2Object mo;
			try {
				mo = Util.resolve(currentDomain, path);
			} catch (Ponder2ResolveException e) {
				output = output + arg + " not found\r\n";
				continue;
			}
			// Check we can access the parent
			Path parent = path.parent();
			try {
				mo = Util.resolve(currentDomain, parent);
			} catch (Ponder2ResolveException e) {
				output = output + arg + ": cannot access parent domain\r\n";
				continue;
			}
			// Check that the parent is a domain
			if (!mo.getOID().isDomain()) {
				output += path.toString() + "is not a domain\r\n";
				continue;
			}
			// Ok, send the unlink command
			mo.operation(shellmo, "remove:", P2Object.create(path.child()));
		}
		return output;
	}

	/**
	 * Dummy operation to make sure Eclipse gives this file to the APT compiler
	 */
	@Ponder2op("no op")
	public void dummyop() {
	};

}