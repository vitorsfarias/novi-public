/**
 * Created on Jul 4, 2005
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
 * $Log: SelfManagedCell.java,v $
 * Revision 1.20  2006/03/15 13:36:42  kpt
 * Lots of testing stuff and new XMLSaver managed object
 *
 * Revision 1.19  2006/02/22 18:15:57  kpt
 * Tidied up
 *
 * Revision 1.18  2006/02/22 16:35:58  kpt
 * First version of Proxy integration
 *
 * Revision 1.17  2005/11/28 16:02:02  kpt
 * Added Event to external invocation
 *
 * Revision 1.16  2005/11/20 13:47:50  kpt
 * prints return values for boot files
 *
 * Revision 1.15  2005/11/20 13:06:17  kpt
 * Import returns error if not found
 *
 * Revision 1.14  2005/11/19 17:00:07  kpt
 * Fixed bootfile param and prints shell port when ready
 *
 * Revision 1.13  2005/11/19 12:12:34  kpt
 * use -rmi - to turn RMI off
 *
 * Revision 1.12  2005/11/18 15:17:29  kpt
 * Added - boot option and . to terminate XML input
 *
 * Revision 1.11  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.10  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.9  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.8  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.7  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.6  2005/10/21 14:31:40  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.5  2005/10/11 10:11:40  kpt
 * First full version of the demo
 *
 * Revision 1.4  2005/09/22 08:38:01  kpt
 * Fixes for demo
 *
 * Revision 1.3  2005/09/21 15:54:56  kpt
 * USE now works through external domains
 *
 * Revision 1.2  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import eu.novi.ponder2.policy.BasicAuthModule;

import eu.novi.ponder2.Domain;
import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.SelfManagedCellP2Adaptor;
import eu.novi.ponder2.Shell;
import eu.novi.ponder2.Util;

/**
 * The top level root domain of the whole system. Contains main() to start the
 * whole thing off
 * 
 * @author Kevin Twidle
 * @version $Id: SelfManagedCell.java,v 1.20 2006/03/15 13:36:42 kpt Exp $
 */
public class SelfManagedCell extends Domain implements ManagedObject {

	public static final String SVNDate = "$Date: 2010-03-23 14:45:20 +0100 (Tue, 23 Mar 2010) $";

	public static final String SVNRevision = "$Rev: 3570 $";

	public static boolean SystemTrace = false;

	public static final String EventDomain = "/event";

	public static final String PolicyDomain = "/policy";

	public static P2Object RootDomain = null;

	public static int port = 13570;

	public static Map<String, P2Object> startupArgs;

	public static String[] startupArgsArray;

	/**
	 * The default value for authorization. Setting its value to true means that
	 * all actions are allowed by default, otherwise as default no actions are
	 * authorised. If no value is set by the user then the default value will be
	 * set to false.
	 */
	static boolean defaulAuthPolicy = false;

	/**
	 * This flag if set to true means that an authorisation module has been
	 * loaded. false otherwise.
	 */
	static boolean isAuthModSet = false;

	/**
	 * creates a new SelfManagedCell domain. This is normally the root domain of
	 * an SMC. It contains code for loading other objects, printing etc.
	 */
	@Ponder2op("create")
	public SelfManagedCell(P2Object myP2Object) {
		super(myP2Object);
		P2Object.setSMC(this);

		if (RootDomain == null)
			RootDomain = myP2Object;

		if (SystemTrace) {
			System.out.println("SelfManagedCell.SelfManagedCell(): my OID is "
					+ myP2Object.getOID());
			System.out
					.println("SelfManagedCell.SelfManagedCell(): my myP2Object is  "
							+ myP2Object);
		}
	}

	/**
	 * answers with a hash containing the user supplied arguments to the SMC.
	 * User supplied arguments are those after an argument of '-' by itself.
	 * <p>
	 * The arguments are interpreted as key=>value pairs where the key is an
	 * argument starting with a '-'. A value cannot start with a '-', it is
	 * taken to be the next key instead. The hash is indexed with the names of
	 * the keys without the '-'. If a value follows a key then it is included as
	 * the value in the hash. If a value is found without a preceding key, it is
	 * ignored. In the following example "copper" will be lost.
	 * <p>
	 * -a -b gold -c silver copper -d
	 * <p>
	 * becomes
	 * 
	 * <pre>
	 * a =&gt; &quot;&quot;
	 * b =&gt; gold
	 * c =&gt; silver
	 * d =&gt; &quot;&quot;
	 * </pre>
	 * 
	 * @return a hash containing the user, run-time arguments
	 */
	@Ponder2op("argsAsHash")
	protected P2Object argsAsHash() {
		return P2Object.create(startupArgs);
	}

	/**
	 * answers with an array containing all the user supplied arguments to the
	 * SMC. User supplied arguments are those after an argument of '-' by
	 * itself. All arguments are added to the array as-is, any '-' characters
	 * are left intact.
	 * 
	 * @return an array containing all the user, run-time arguments
	 */
	@Ponder2op("argsAsArray")
	protected P2Object argsAsArray() {
		return P2Object.create(startupArgsArray);
	}

	/**
	 * Sleeps for secs seconds
	 * 
	 * @param secs
	 */
	@Ponder2op("sleep:")
	protected void sleep(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted " + e.getMessage());
		}
	}

	/**
	 * Terminates the Ponder2 SMC with a zero status
	 */
	@Ponder2op("exit")
	protected void exit() {
		System.exit(0);
	}

	/**
	 * Terminates the Ponder2 SMC with exitStatus
	 * 
	 * @param exitStatus
	 */
	@Ponder2op("exit:")
	protected void exit(int exitStatus) {
		System.exit(exitStatus);
	}

	/**
	 * Loads a communications protocol and sets the SMC's local address to
	 * anAddress. This is functionally the same as using the -address argument
	 * when running Ponder2, however, it means that communications can be
	 * delayed until the domain structure is setup correctly
	 * 
	 * @param anAddress
	 *            the new address as a string in the correct format for the
	 *            protocol chosen
	 * @throws Ponder2RemoteException
	 *             if the protocol cannot be loaded properly
	 */
	@Ponder2op("address:")
	protected void address(String anAddress) throws Ponder2RemoteException {
		if (anAddress.contains(":"))
			ExternalManagedObject.loadProtocol(null, anAddress, null);
		else
			ExternalManagedObject.loadProtocol(anAddress, null, null);
	}

	/**
	 * Loads the code necessary for creating an instance of anObject. Answers
	 * the factory for creating instances of anObject.
	 * 
	 * @throws Ponder2Exception
	 */
	@Ponder2op("load:")
	protected P2Object operation_load(String anObject) throws Ponder2Exception {
		return Util.loadFactory(anObject);
	}

	/**
	 * Pings remote SMC to see if it is alive. Answers true if it is.
	 * 
	 * @param location
	 *            address of remote site
	 * @return true if the remote is alive
	 * @throws Ponder2Exception
	 */
	@Ponder2op("ping:")
	public static boolean ping(String location) throws Ponder2Exception {
		try {
			if (location == null)
				throw new Ponder2ArgumentException(
						"Ping error: location cannot be null");

			URI uri = new URI(location);
			String scheme = uri.getScheme();
			if (scheme == null)
				throw new Ponder2ArgumentException(
						"Ping: missing scheme/protocol in " + location);

			// Ok, let's get for the remote proxy
			Transmitter remote = ExternalManagedObject.getRemote(uri);
			// Finally ask for the remote object
			return remote != null && remote.ping(uri);
		} catch (URISyntaxException e) {
			throw new Ponder2RemoteException("getRemote: " + e.getMessage());
		}
	}

	/**
	 * Answers the remote managed object which has aName belonging to an SMC at
	 * aLocation.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("import:from:")
	protected P2Object operation_import_from(String aName, String aLocation)
			throws Ponder2Exception {
		return Util.getRemoteObject(aName, aLocation);
	}

	/**
	 * answers with the amount of free memory in the Java VM
	 * 
	 * @return the amount of free memory
	 */
	@Ponder2op("memory")
	protected long memory() {
		Runtime runtime = Runtime.getRuntime();
		long max = runtime.maxMemory();
		long free = runtime.freeMemory();
		System.out.println("Max : " + max + ". Free: " + free);
		runtime.gc();
		free = runtime.freeMemory();
		System.out.println("Max : " + max + ". Free: " + free);
		return free;
	}

	/**
	 * Reads and executes PonderTalk from aUrl.
	 * 
	 * @param source
	 * @param aUrl
	 * @throws Ponder2Exception
	 */
	@Ponder2op("read:")
	public void read(P2Object source, String aUrl) throws Ponder2Exception {
		Util.parseFile(source, aUrl);
	}

	/**
	 * Reads and executes PonderTalk from aUrl. The parser is initialized with
	 * the variables in the P2Hash varHash
	 * 
	 * @param source
	 * @param aUrl
	 * @throws Ponder2Exception
	 */
	@Ponder2op("read:withVars:")
	public void read(P2Object source, String aUrl, P2Hash varHash)
			throws Ponder2Exception {
		Util.parseFile(source, aUrl, varHash);
	}

	/**
	 * Reads and executes PonderTalk from aPonderTalkString.
	 * 
	 * @throws Ponder2Exception
	 */
	@Ponder2op("readString:")
	public P2Object readString(P2Object source, String aPonderTalkString)
			throws Ponder2Exception {
		String p2xml = P2Compiler.parse(aPonderTalkString);
		return new XMLParser().execute(source, p2xml);
	}

	/**
	 * Reads and executes PonderTalk from aPonderTalkString. The parser is
	 * initialised with the variables in the P2Hash varHash
	 * 
	 * @throws Ponder2Exception
	 */
	@Ponder2op("readString:withVars:")
	public P2Object readString(P2Object source, String aPonderTalkString,
			P2Hash varHash) throws Ponder2Exception {
		String p2xml = P2Compiler.parse(aPonderTalkString);
		return new XMLParser(varHash).execute(source, p2xml);
	}

	/**
	 * Reads a text file called aFileName into a string and returns it.
	 * 
	 * @param aFileName
	 * @return the contents of the file
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("getFile:")
	public String getFile(String aFileName) throws Ponder2OperationException {
		String content;
		try {
			InputStream is = Util.getInputStream(new URI(aFileName));
			int x = is.available();
			byte b[] = new byte[x];
			is.read(b);
			content = new String(b);
		} catch (Exception e) {
			throw new Ponder2OperationException(e.getMessage());
		}
		return content;
	}

	/**
	 * Answers aBoolean. Set system tracing on or off.
	 * 
	 */
	@Ponder2op("trace:")
	protected boolean trace(boolean aBoolean) {
		SystemTrace = aBoolean;
		return aBoolean;
	}

	/**
	 * Answers aString. Displays aString on the console.
	 * 
	 */
	@Ponder2op("print:")
	protected String print(String aString) {
		if (SystemTrace)
			System.out.print("Print: ");
		System.out.println(aString);
		return aString;
	}

	/**
	 * Executes aBlock with no arguments. Answers with the answer from aBlock.
	 * Used to force execution of a block within a particular SMC.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("execute:")
	protected P2Object execute(P2Object source, P2Block aBlock)
			throws Ponder2Exception {
		return aBlock.operation_value0(source);
	}

	/**
	 * Executes aBlock with the values of its arguments being taken by name from
	 * aHash. Answers the value of the last statement executed by the block.
	 * Throws an error if aHash does not satisfy the block's arguments. Used to
	 * force execution of a block within a particular SMC.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("execute:withHash:")
	protected P2Object executeHash(P2Object source, P2Block aBlock, P2Hash aHash)
			throws Ponder2Exception {
		return aBlock.operation_valueHash(source, aHash);
	}

	/**
	 * Executes aBlock with anArray of arguments. Answers with the result of the
	 * last statement executed by the block. Used to force execution of a block
	 * within a particular SMC.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("execute:args:")
	protected P2Object executeArgs(P2Object source, P2Block aBlock,
			P2Array anArray) throws Ponder2Exception {
		return aBlock.operation_array(source, anArray);
	}

	static public boolean getDefaulAuthPolicy() {
		return defaulAuthPolicy;
	}

	/**
	 * starts the whole system running and returns the root OID. The root OID
	 * can be useful for starting the SMC from within a Java VM.
	 * 
	 * @param args
	 *            see doc for argument values and meanings
	 */
	public static void start(String[] args) {

		// Set the default policy to off unless changed with -auth option
		String defaultPolicyValue = null;
		// the name of prolog file that implements the strategy for conflict
		// resolution.
		String confStrgFile = null;

		List<String> bootFiles = new Vector<String>();
		int portBase = port = 13570;
		boolean multipleSMCs = false;
		bootFiles.add("boot.p2");
		SelfManagedCellP2Adaptor smc;
		try {
			smc = new SelfManagedCellP2Adaptor(null, "create");
		} catch (Exception e) {
			System.out.println("Failed to start SMC: ");
			e.printStackTrace();
			return;
		}
		int i;
		for (i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("-")) {
				// Have we reached the end of the system arguments? i.e. a "-"
				// by itself
				if (arg.length() == 1) {
					// Consume this arg and drop out
					i++;
					break;
				}
				if (arg.equals("-port"))
					port = portBase = Integer.parseInt(args[++i]);
				else if (arg.equals("-boot")) {
					String bootFile = args[++i];
					if (bootFile.equals("-"))
						bootFiles.clear();
					else {
						String[] file = bootFile.split(",");
						for (int j = 0; j < file.length; j++) {
							bootFiles.add(file[j]);
						}
					}
				} else if (arg.equals("-multiple")) {
					multipleSMCs = true;
				} else if (arg.equals("-trace")) {
					SystemTrace = true;
				} else if (arg.equals("-loadpath")) {
					String paths = args[++i];
					if (paths.equals("-"))
						Util.setLoadPath(new String[0]);
					else {
						String[] path = paths.split(",");
						for (int j = 0; j < path.length; j++) {
							Util.addLoadPath(path[j]);
						}
					}
				} else if (arg.equals("-address")) {
					// This is an address where we can be contacted
					// Load the protocol and start a receiver
					String address = args[++i];
					try {
						if (address.contains(":"))
							ExternalManagedObject.loadProtocol(null, address,
									null);
						else
							ExternalManagedObject.loadProtocol(address, null,
									null);
					} catch (Ponder2RemoteException e) {
						System.err.println("Failed to allocate address "
								+ address + ": " + e.getMessage());
					}
				} else if (arg.equals("-auth")) {
					defaultPolicyValue = args[++i];
				}

				else if (arg.equals("-strategy")) {
					confStrgFile = args[++i];
				} else if (arg.equals("-version") || arg.equals("--version")) {
					System.out.print("Ponder2 Core ");
					System.out.print(SVNRevision.substring(1,
							SVNRevision.length() - 2));
					System.out.print(" ");
					System.out
							.print(SVNDate.substring(1, SVNDate.length() - 2));
					System.out.println();
					System.exit(0);
				}
				// else if (flag.equals("-path")) {
				// Path path = new Path(args[++i]);
				// DomainP2Adaptor parent = RootDomain;
				// for (int p = 1; p < path.size(); p++) {
				// String pname = path.head(p);
				// OID newDom = null;
				// try {
				// newDom = Util.resolve("/", pname).asOID();
				// }
				// catch (Ponder2ArgumentException e) {
				// }
				// catch (Ponder2ResolveException e) {
				// }
				// if (newDom == null) {
				// DomainP2Adaptor dom = new DomainP2Adaptor();
				// parent.add(path.subpath(p - 1, p - 1), dom.getOID());
				// newDom = dom.getOID();
				// }
				// parent = (DomainP2Adaptor)newDom.getManagedObject();
				// }
				// parent.add(path.tail(1), smc.getOID());
				// }
			}
		}

		// Move to next argument
		int argsleft = args.length - i;
		startupArgsArray = new String[argsleft];
		startupArgs = new HashMap<String, P2Object>();
		// Now deal with user arguments, if any.
		int j = 0;
		while (i < args.length) {
			String arg = args[i];
			startupArgsArray[j] = arg;
			i++;
			j++;
			// We only deal with flagged arguments
			if (arg.startsWith("-")) {
				String name = arg.substring(1);
				String value = "";
				// Does this arg have a value with it?
				if (i < args.length && !args[i].startsWith("-")) {
					value = args[i];
					startupArgsArray[j] = value;
					i++;
					j++;
				}
				// Store the arg name and value, if any
				startupArgs.put(name, P2Object.create(value));
			}

		}

		if (portBase != 0) {
			port = Shell.initialise(smc, portBase, multipleSMCs);
			if (port == 0) {
				System.err.println("Failed to open port " + portBase + ".");
				System.err.println("Use -multiple for multiple SMCs");
				System.err.println("Use -port 0 for no shell access");
				System.exit(1);
			}
		}

		if (defaultPolicyValue != null) {

			isAuthModSet = true; // the authorisation module is going to be
									// loaded!

			// Set the default policy true otherwise the ponder interpreter
			// structure cannot be loaded.
			SelfManagedCell.defaulAuthPolicy = true;
			BasicAuthModule bAuthMod = new BasicAuthModule(confStrgFile);
			bAuthMod.setRootDomain(RootDomain);
			P2ObjectAdaptor.setAuthorisation(bAuthMod);
		}

		for (String bootfile : bootFiles) {
			try {
				Util.parseFile(RootDomain, bootfile);
			} catch (Ponder2OperationException e) {
				System.out.println("Ponder2 operation exception: "
						+ e.getMessage());
				if (SelfManagedCell.SystemTrace)
					e.printStackTrace();
			} catch (Ponder2ArgumentException e) {
				System.out.println("Ponder2 argument exception: "
						+ e.getMessage());
				if (SelfManagedCell.SystemTrace)
					e.printStackTrace();
			} catch (Ponder2Exception e) {
				System.out.println("Ponder2 exception: " + e.getMessage());
				if (SelfManagedCell.SystemTrace)
					e.printStackTrace();
			}
		}

		if (defaultPolicyValue != null) {
			// After building of domain structure we can re-set the default
			// policy
			SelfManagedCell.defaulAuthPolicy = defaultPolicyValue
					.equals("allow");
			System.out.println("SMC, the default authorization policy is "
					+ (defaulAuthPolicy ? "positive" : "negative"));
		}

		if (port != 0)
			System.out.println("Shell port " + port + " ready");
	}

	/**
	 * starts the whole system running
	 * 
	 * @param args
	 *            see doc for argument values and meanings
	 */
	public static void main(String[] args) {
		SelfManagedCell.start(args);
	}

	public static Map<String, P2Object> getStartupArgs() {
		return startupArgs;
	}

	public static void setStartupArgs(Map<String, P2Object> startupArgs) {
		SelfManagedCell.startupArgs = startupArgs;
	}

	public static String[] getStartupArgsArray() {
		return startupArgsArray;
	}

	public static void setStartupArgsArray(String[] startupArgsArray) {
		SelfManagedCell.startupArgsArray = startupArgsArray;
	}

	public static boolean isSystemTrace() {
		return SystemTrace;
	}

	public static void setSystemTrace(boolean systemTrace) {
		SystemTrace = systemTrace;
	}

	public static P2Object getRootDomain() {
		return RootDomain;
	}

	public static void setRootDomain(P2Object rootDomain) {
		RootDomain = rootDomain;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		SelfManagedCell.port = port;
	}

	public static boolean isAuthModSet() {
		return isAuthModSet;
	}

	public static void setAuthModSet(boolean isAuthModSet) {
		SelfManagedCell.isAuthModSet = isAuthModSet;
	}

	public static String getSvndate() {
		return SVNDate;
	}

	public static String getSvnrevision() {
		return SVNRevision;
	}

	public static String getEventdomain() {
		return EventDomain;
	}

	public static String getPolicydomain() {
		return PolicyDomain;
	}

	public static void setDefaulAuthPolicy(boolean defaulAuthPolicy) {
		SelfManagedCell.defaulAuthPolicy = defaulAuthPolicy;
	}

}
