/**
 * Created on Jul 27, 2005
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
 * $Log: Util.java,v $
 * Revision 1.19  2006/03/15 13:36:42  kpt
 * Lots of testing stuff and new XMLSaver managed object
 *
 * Revision 1.18  2006/03/01 22:57:32  kpt
 * Added remote execution of external xml via RMI
 *
 * Revision 1.17  2006/01/19 22:28:44  kpt
 * Allowed parameter substitution with any element
 *
 * Revision 1.16  2005/12/09 10:34:38  kpt
 * Policy actions sorted out
 *
 * Revision 1.15  2005/11/23 14:47:07  kpt
 * Removed Iterable from QDParser.
 *
 * Revision 1.14  2005/11/20 13:06:17  kpt
 * Import returns error if not found
 *
 * Revision 1.13  2005/11/19 18:05:10  kpt
 * eval tries resource then file
 *
 * Revision 1.12  2005/11/19 16:58:52  kpt
 * Only call execute if the use has attributes or child elements for the MO
 *
 * Revision 1.11  2005/11/19 12:12:54  kpt
 * Added error returns for <use>
 *
 * Revision 1.10  2005/11/18 10:44:55  kpt
 * Added trace. Tweaked boot.xml
 *
 * Revision 1.9  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.8  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.7  2005/11/15 15:00:51  kpt
 * ExecuteSetup introduced, more result feedback
 *
 * Revision 1.6  2005/11/14 21:02:21  kpt
 * Made Result much more pervasive
 *
 * Revision 1.5  2005/11/13 23:27:30  kpt
 * Added time manager
 *
 * Revision 1.4  2005/11/12 18:02:23  kpt
 * Sorted out return values from managed objects. MO conditions now work
 *
 * Revision 1.3  2005/11/07 12:05:42  kpt
 * Added Result to the parsing
 *
 * Revision 1.2  2005/10/29 15:52:19  kpt
 * Restore almost done
 *
 * Revision 1.1  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.21  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.20  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.19  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.18  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.17  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.16  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.15  2005/10/21 14:31:41  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.14  2005/10/19 21:58:19  kpt
 * Removed NormalEOFException
 *
 * Revision 1.13  2005/10/17 14:55:35  kpt
 * Fixed problem where eval did not return an OID
 *
 * Revision 1.12  2005/10/12 15:20:00  kpt
 * Logs sensible error to console if file to be read is missing
 *
 * Revision 1.11  2005/10/11 10:10:00  kpt
 * Fixed null return from parsing.
 Added -name argument substitution
 *
 * Revision 1.10  2005/10/10 04:28:59  kpt
 * Improved external use. Assume local unless remote specified
 *
 * Revision 1.9  2005/10/09 22:03:04  kpt
 * First complete version of demo with a few fixes.
 Local evaluation added on demand before shipping xml for export.
 *
 * Revision 1.8  2005/10/06 10:59:18  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.7  2005/09/22 08:38:01  kpt
 * Fixes for demo
 *
 * Revision 1.6  2005/09/21 15:54:56  kpt
 * USE now works through external domains
 *
 * Revision 1.5  2005/09/21 13:46:40  kpt
 * Can now create and send events using XML and the event templates
 *
 * Revision 1.4  2005/09/21 10:13:15  kpt
 * Implemented <local> and context for policies
 *
 * Revision 1.3  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import eu.novi.ponder2.comms.Transmitter;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2RemoteException;
import eu.novi.ponder2.exception.Ponder2ResolveException;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthorisationModule;

import eu.novi.ponder2.ExternalManagedObject;
import eu.novi.ponder2.ExternalManagedObjectP2Adaptor;
import eu.novi.ponder2.FactoryObjectP2Adaptor;
import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.P2ObjectAdaptor;
import eu.novi.ponder2.Path;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Util;

/**
 * Utility routines to support the Ponder2 framework
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Util {
	// Used to allow libraries e.g. Android to override resource location
	protected static Util util = new Util();

	public static P2Object resolve(String name) throws Ponder2Exception {
		return resolve("/", name);
	}

	public static P2Object resolve(String defaultDomain, String name)
			throws Ponder2Exception {
		Path path;
		if (name == null)
			return null;
		if (name.startsWith("/"))
			path = new Path(name);
		else
			path = new Path(defaultDomain + "/" + name);
		return resolve(path);
	}

	/**
	 * resolves a pathname of a managed object into an OID
	 * 
	 * @param defaultDomain
	 *            the starting point if path is relative
	 * @param path
	 *            the name of the managed object
	 * @return the P2ManagedObject of the managed object or null if not found
	 * @throws Ponder2Exception
	 */
	public static P2Object resolve(String defaultDomain, Path path)
			throws Ponder2Exception {
		if (!path.isAbsolute())
			path = new Path(defaultDomain + "/" + path.toString());
		return resolve(path);
	}

	/**
	 * resolves a pathname of a managed object into an OID
	 * 
	 * @param path
	 *            the name of the managed object
	 * @return the P2ManagedObject of the managed object or null if not found
	 * @throws Ponder2Exception
	 */
	public static P2Object resolve(Path path) throws Ponder2Exception {
		if (!path.isAbsolute()) {
			throw new Ponder2ResolveException("Resolve: relative path'" + path
					+ "' not found");
		}
		return resolve(SelfManagedCell.RootDomain.getManagedObject(), path);
	}

	public static P2Object resolve(P2Object dom, String name)
			throws Ponder2Exception {
		return resolve(dom.getManagedObject(), new Path(name));
	}

	public static P2Object resolve(P2Object dom, Path path)
			throws Ponder2Exception {
		return resolve(dom.getManagedObject(), path);
	}

	/**
	 * resolves a pathname of a managed object into an OID
	 * 
	 * @param dom
	 *            the starting point if name is relative
	 * @param path
	 *            the name of the managed object
	 * @return the P2ManagedObject of the managed object or null if not found
	 * @throws Ponder2Exception
	 */
	public static P2Object resolve(P2ManagedObject dom, Path path)
			throws Ponder2Exception {
		if (SelfManagedCell.SystemTrace)
			System.out.print("Util: resolve path " + path + " => ");

		P2ManagedObject mo = dom;
		if (!(path.isRelative() || path.isAbsolute())) {
			return mo.getP2Object();
		}
		int count = 0;
		// Work our way down the path
		for (String bit : path) {
			count++;
			// If dot then we have finished
			if (bit.equals(".")) {
				mo = dom;
				break;
			}
			// TODO Sort out mo and dom, probably do not need both
			// KPT to allow root at the start
			if (bit.equals(Path.ROOT) || (count == 2 && bit.equals("root"))) {
				mo = dom = SelfManagedCell.RootDomain.getManagedObject();
			} else {
				// If we have a remote domain we can ask it to resolve the path
				if (dom.isDomain()
						&& dom.getP2Object() instanceof ExternalManagedObjectP2Adaptor) {
					// This is external, it will be completely resolved remotely
					String rest = rest(path, count - 1);
					if (!rest.equals("")) {
						P2Object result = dom.operation(
								SelfManagedCell.RootDomain, "resolve:",
								P2Object.create(rest));
						mo = result.getManagedObject();
					}
					break;
				}
				P2Object obj = dom.operation(SelfManagedCell.RootDomain, "at:",
						P2Object.create(bit));
				if (obj == P2Null.Null)
					throw new Ponder2ResolveException("Resolve: path'" + path
							+ "' not found");
				dom = mo = obj.getManagedObject();
			}
		}
		if (SelfManagedCell.SystemTrace)
			System.out.println(mo);
		return mo.getP2Object();
	}

	// Return path - count bits from the front
	private static String rest(Path path, int count) {
		String result = "";
		boolean slash = false;
		for (String bit : path) {
			if (count-- <= 0) {
				result += (slash ? "/" : "") + bit;
				slash = true;
			}
		}
		return result;
	}

	public static void parseFile(P2Object source, String name)
			throws Ponder2Exception {
		System.out.println("Reading " + name);
		parseFile(source, name, new XMLParser());
	}

	public static void parseFile(P2Object source, String name, P2Hash variables)
			throws Ponder2Exception {
		System.out.println("Reading (with variables) " + name);
		parseFile(source, name, new XMLParser(variables));
	}

	public static void parseFile(P2Object source, String name, XMLParser parser)
			throws Ponder2Exception {
		try {
			InputStream is = getInputStream(new URI(name));
			String xml = P2Compiler.parse(name, is);
			parser.execute(source, xml);
		} catch (MalformedURLException e) {
			throw new Ponder2OperationException(e.getMessage());
		} catch (IOException e) {
			throw new Ponder2OperationException(e.getMessage());
		} catch (URISyntaxException e) {
			throw new Ponder2OperationException(e.getMessage());
		}
	}

	/**
	 * loads Managed Object code into this VM and returns a Ponder2 factory
	 * object
	 * 
	 * @param name
	 *            the URL of the object to be loaded. Currently the scheme can
	 *            only be blank or 'resource'. e.g.
	 * 
	 *            <pre>
	 * MyObjectName
	 * resource:MyObjectName
	 * </pre>
	 * 
	 * @return the factory object of the new managed object type
	 * @throws Ponder2Exception
	 *             it it can't be found or there is a syntax error
	 */
	public static P2Object loadFactory(String name) throws Ponder2Exception {
		P2Object p2Object = null;
		URI uri;
		try {
			uri = new URI(name);
			String scheme = uri.getScheme();
			if (scheme == null)
				scheme = "resource";
			if (!scheme.equals("resource")) {
				throw new Ponder2ArgumentException(
						"Load Managed Object - Scheme '" + scheme
								+ "' not understood");
			}

			Class<P2ObjectAdaptor> adaptorClass = Util
					.getManagedObjectAdaptor(uri.getSchemeSpecificPart());
			if (adaptorClass == null) {
				throw new Ponder2ArgumentException(
						"Load Managed Object - Adaptor for '"
								+ uri.getSchemeSpecificPart() + "' not found");
			}

			p2Object = new FactoryObjectP2Adaptor(null, "create:",
					P2Object.create(adaptorClass));
		} catch (URISyntaxException e) {
			throw new Ponder2ArgumentException(
					"Load Managed Object - Bad URI syntax " + e.getReason());
		}
		return p2Object;
	}

	private static List<String> loadPath;
	private static final String[] initialLoadPath = new String[] { "resource",
			"eu.novi.ponder2", "eu.novi.ponder2.policy",
			"eu.novi.ponder2.managedobject" };
	static {
		setLoadPath(initialLoadPath);
	}

	public static void setLoadPath(String[] paths) {
		List<String> newPath = new Vector<String>();
		for (String path : paths) {
			newPath.add(path);
		}
		loadPath = newPath;
	}

	public static void addLoadPath(String path) {
		loadPath.add(0, path);
	}

	protected static Class<P2ObjectAdaptor> getManagedObjectAdaptor(String name) {
		Class<P2ObjectAdaptor> cl = null;
		String adaptorName = '.' + name + "P2Adaptor";
		for (String path : loadPath) {
			cl = getFactory(path + adaptorName);
			if (cl != null)
				break;
		}
		return cl;
	}

	@SuppressWarnings("unchecked")
	private static Class<P2ObjectAdaptor> getFactory(String name) {
		Class<P2ObjectAdaptor> cl = null;
		try {
			cl = (Class<P2ObjectAdaptor>) Class.forName(name);
		} catch (ClassNotFoundException e) {
			// ignore any errors here, we simple return null
		}
		return cl;
	}

	/**
	 * Imports a remote Managed Object from another SMC using the protocol
	 * defined in the URI
	 * 
	 * @param name
	 *            the pathname of the remote object
	 * @param location
	 *            the URI of the remote SMC
	 * @return the imported object. This is actually an External Managed Object
	 * @throws Ponder2Exception
	 *             if the object cannot be found or imported
	 */
	public static P2Object getRemoteObject(String name, String location)
			throws Ponder2Exception {
		try {
			if (name == null || location == null)
				throw new Ponder2ArgumentException(
						"Get remote object error: name " + name + " from "
								+ location);

			URI uri = new URI(location);
			String scheme = uri.getScheme();
			if (scheme == null)
				throw new Ponder2ArgumentException(
						"Get remote object: missing scheme/protocol in "
								+ location);

			// Ok, let's get for the remote proxy
			Transmitter remote = ExternalManagedObject.getRemote(uri);
			// Finally ask for the remote object
			return remote.getObject(uri, name);
		} catch (URISyntaxException e) {
			throw new Ponder2RemoteException("getRemote: " + e.getMessage());
		}
	}

	private static final String STRING_SCHEME = "string";
	private static final String RESOURCE_SCHEME = "resource";

	// private static final String FILE_SCHEME = "file";

	public static InputStream getInputStream(URI uri)
			throws MalformedURLException, IOException, URISyntaxException {
		String scheme = uri.getScheme();
		boolean emptyScheme = false;
		if (scheme == null) {
			emptyScheme = true;
			scheme = RESOURCE_SCHEME;
		}

		if (scheme.equals(STRING_SCHEME)) {
			byte[] bytes = uri.getSchemeSpecificPart().getBytes();
			return new ByteArrayInputStream(bytes);
		}

		InputStream istream;

		String resource = uri.getSchemeSpecificPart();
		if (scheme.equals(RESOURCE_SCHEME)) {
			String fullResourceName = resource;
			istream = util.openResource(fullResourceName);
			if (istream == null && emptyScheme) {
				URI fileUri = new URI("file", resource, uri.getFragment());
				try {
					istream = fileUri.toURL().openStream();
				} catch (FileNotFoundException e) {
				}
			}
			if (istream == null && !resource.startsWith("/")) {
				fullResourceName = "resource/" + resource;
				istream = util.openResource(fullResourceName);
			}
			if (istream == null && !resource.startsWith("/")) {
				fullResourceName = "/" + resource;
				istream = util.openResource(fullResourceName);
			}
			if (istream == null && !resource.startsWith("/")) {
				fullResourceName = "/resource/" + resource;
				istream = util.openResource(fullResourceName);
			}
			if (istream == null)
				throw new IOException("resource " + resource + " not found");
			return istream;
		}

		// Nothing found above, just try to open the URI
		// it might be file:// jar:// etc
		return uri.toURL().openStream();

	}

	protected InputStream openResource(String resource) {
		return this.getClass().getResourceAsStream(resource);
	}

	/**
	 * Check if an object has permission to perform an operation on another
	 * object. Returns true or false.
	 * 
	 * @param source
	 *            the source object
	 * @param target
	 *            the target object
	 * @param operation
	 *            the operation to be performed
	 * @param args
	 *            the arguments for the operation
	 * @return true if the operation would be allowed
	 */
	public static boolean canPerform(P2Object source, P2Object target,
			String operation, P2Object... args) {
		AuthorisationModule authorisationModule = P2ObjectAdaptor
				.getAuthorisation();

		if (authorisationModule != null) {
			AuthPolicyHolder holder = new AuthPolicyHolder();
			try {
				authorisationModule.request(AuthorisationModule.PEP1, 's',
						holder, source, target, operation, args);
				// authorisationModule.request(AuthorisationModule.PEP2, 't',
				// holder,
				// source, target,
				// operation, args);
			} catch (Ponder2AuthorizationException e) {
				return false;
			}
		}
		return true;
	}

}
