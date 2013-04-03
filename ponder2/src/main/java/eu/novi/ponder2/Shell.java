/**
 * Created on Jul 9, 2005
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
 * $Log: Shell.java,v $
 * Revision 1.38  2006/03/01 22:50:58  kpt
 * New Version
 *
 * Revision 1.37  2006/02/11 16:28:35  kpt
 * Modified Event creation
 *
 * Revision 1.36  2006/01/19 22:28:44  kpt
 * Allowed parameter substitution with any element
 *
 * Revision 1.35  2006/01/15 14:24:03  kpt
 * New Version Number
 *
 * Revision 1.34  2005/12/16 18:01:32  kpt
 * Fixed new method of send and receive
 *
 * Revision 1.33  2005/12/14 12:41:35  kpt
 * New version number
 *
 * Revision 1.32  2005/12/09 10:46:46  kpt
 * Fixed unknown command bug
 *
 * Revision 1.31  2005/11/23 14:47:08  kpt
 * Removed Iterable from QDParser.
 *
 * Revision 1.30  2005/11/20 23:20:54  kpt
 * Improved startup message
 *
 * Revision 1.29  2005/11/20 23:19:06  kpt
 * Added Id to version string
 *
 * Revision 1.28  2005/11/20 13:39:39  kpt
 * improved the reply mechanism for the shell
 *
 * Revision 1.27  2005/11/20 10:58:59  kpt
 * Upped version number
 *
 * Revision 1.26  2005/11/20 10:57:20  kpt
 * Added managed object commands
 *
 * Revision 1.25  2005/11/18 15:17:29  kpt
 * Added - boot option and . to terminate XML input
 *
 * Revision 1.24  2005/11/17 11:36:28  kpt
 * Added context to many ops for arg substitution
 *
 * Revision 1.23  2005/11/14 21:02:22  kpt
 * Made Result much more pervasive
 *
 * Revision 1.22  2005/11/14 13:59:00  kpt
 * Improved results, fixed TickManager
 *
 * Revision 1.21  2005/11/07 12:05:42  kpt
 * Added Result to the parsing
 *
 * Revision 1.20  2005/11/03 04:23:46  kpt
 * More restore and tidying done
 *
 * Revision 1.19  2005/10/29 15:52:19  kpt
 * Restore almost done
 *
 * Revision 1.18  2005/10/28 21:58:00  kpt
 * Restore almost completed
 *
 * Revision 1.17  2005/10/27 16:57:38  kpt
 * OID separated from P2ManagedObject
 *
 * Revision 1.16  2005/10/27 09:25:15  kpt
 * Dump almost completed. Everything still works!
 *
 * Revision 1.15  2005/10/24 20:53:02  kpt
 * Dump part of dump and restore working
 *
 * Revision 1.14  2005/10/22 16:49:46  kpt
 * Returns results through Reply. Shell does rm properly
 *
 * Revision 1.13  2005/10/22 08:53:33  kpt
 * Changed TaggedElement getAtt to getAttribute
 *
 * Revision 1.12  2005/10/21 17:15:48  kpt
 * Renamed XML Element types
 *
 * Revision 1.11  2005/10/21 17:12:55  kpt
 * Tidied up and made sure demo still worked
 *
 * Revision 1.10  2005/10/21 14:31:41  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.9  2005/10/12 13:45:13  kpt
 * Changed mkdom to use XML rather than talk straight to a domain
 *
 * Revision 1.8  2005/10/11 10:11:40  kpt
 * First full version of the demo
 *
 * Revision 1.7  2005/10/08 23:05:44  kpt
 * Airline Demonstration almost complete
 *
 * Revision 1.6  2005/10/06 10:59:18  kpt
 * Complete reorganisation of return values using Result.
 Commands handed to MOs one at a time.
 *
 * Revision 1.5  2005/09/22 15:07:25  kpt
 * Trimed lines on input from telnet session
 *
 * Revision 1.4  2005/09/21 08:21:03  kpt
 * First cut at external objects
 *
 * Revision 1.3  2005/09/15 21:02:48  kpt
 * Improved JAR receipe
 *
 * Revision 1.2  2005/09/14 21:22:36  kpt
 * Added flat bedstation XML
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2ResolveException;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.DomainP2Adaptor;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.Shell;
import eu.novi.ponder2.ShellInstance;
import eu.novi.ponder2.Util;

/**
 * The shell is responsible for all the interactions with an interactive user.
 * The shell parses commands, converts them into XML structures and sends them
 * to managed objects to be executed.
 * 
 * @author Kevin Twidle
 * @version $Id: Shell.java 762 2008-05-22 22:17:15Z Kevin Twidle $
 */
public class Shell implements Runnable {

	/**
	 * the socket that the shell communicates through
	 */
	private ServerSocket ssc;
	/**
	 * the root domain act as a default for many commands
	 */
	private static P2Object root;

	/**
	 * the IP port number of this SMC
	 */
	private int port;

	/**
	 * The location of the /shell domain for shell instances
	 */
	private P2Object shellDomain = null;

	/**
	 * The count of shells that have connected
	 */
	private int shellNumber = 0;
	private static final transient Logger log = LoggerFactory.getLogger(Shell.class);
	/**
	 * instantiates and sets the shell up to receive connections on the given
	 * port. The shell is run as a separate thread.
	 * 
	 * @param root
	 *            the root domain for this SMC
	 * @param port
	 *            the preferred IP port number to listen on for connections
	 * @return the actual IP port number that was opened for connections
	 */
	public static int initialise(P2Object root, int port, boolean multipleSMCs) {
		Shell.root = root;
		Shell shell = new Shell(port, multipleSMCs);
		if (shell.port == 0)
			return 0;
		Thread tshell = new Thread(shell);
		tshell.start();
		return shell.port;
	}

	/**
	 * creates an instance of the main shell listener. An attempt is made to
	 * open the given IP port but if it is not available then the next port up
	 * is tried. this continues until an available port is opened successfully.
	 * 
	 * @param port
	 *            the preferred IP port number to listen on
	 */
	private Shell(int port, boolean multipleSMCs) {
		super();
		boolean gotPort = false;
		while (!gotPort) {
			try {
				System.out.println("Shell: trying port " + port);
				log.info("Shell: trying port " + port);
				ssc = setup(port);
				gotPort = true;
			} catch (IOException e) {
				if (!multipleSMCs) {
					port = 0;
					break;
				}
				port++;
			}
		}
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (ssc == null) {
			System.out.println("No socket for shell");
			return;
		}
		while (true) {
			Socket sc;
			try {
				sc = ssc.accept();
				// Check /shell exists else create it
				if (shellDomain == null) {
					try {
						shellDomain = Util.resolve("/", "/shell");
					} catch (Ponder2ResolveException e) {
						shellDomain = new DomainP2Adaptor(root, "create");
						SelfManagedCell.RootDomain.operation(root, "at:put:",
								P2Object.create("shell"), shellDomain);
						// if an authorisation module is loaded
						// then we need to read the file that specifies the
						// authorisation policies for the shell instances
						if (SelfManagedCell.isAuthModSet)
							Util.parseFile(SelfManagedCell.RootDomain,
									"shell-auth.p2");
					}
				}
				Thread thread = new Thread(new ShellInstance(sc, root,
						shellDomain, ++shellNumber));
				thread.start();
			} catch (Ponder2Exception e) {
				System.err.println("Shell:  " + e.getMessage());
				break;
			} catch (IOException e) {
				System.err.println("Shell: socket accept error: "
						+ e.getMessage());
				break;
			}
		}

	}

	/**
	 * opens and binds the server-socket channel
	 * 
	 * @param port
	 *            the IP port number to be opened
	 * @return the new socket channel
	 * @throws IOException
	 *             if the port is not available
	 */
	public static ServerSocket setup(int port) throws IOException {
		ServerSocket ssc = new ServerSocket(port);
		return ssc;
	}

}
