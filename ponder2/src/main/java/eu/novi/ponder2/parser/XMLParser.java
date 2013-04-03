/**
 * Copyright 2006 Imperial College, London, England.
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
 * Created on Jan 5, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import com.twicom.qdparser.Element;
import com.twicom.qdparser.TaggedElement;
import com.twicom.qdparser.XMLParseException;
import com.twicom.qdparser.XMLReader;

import eu.novi.ponder2.OID;
import eu.novi.ponder2.Path;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.exception.Ponder2ResolveException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class XMLParser {

	public static boolean trace = false;

	P2Hash variables;

	public XMLParser() {
		variables = new P2Hash();
		variables.put("Variables", variables);

	}

	/**
	 * @param variables
	 */
	public XMLParser(P2Hash variables) {
		this.variables = variables;
	}

	public P2Object execute(P2Object source, String xmlString)
			throws Ponder2Exception {
		Reader reader = new StringReader(xmlString);
		return execute(source, reader);
	}

	public P2Object execute(P2Object source, Reader reader)
			throws Ponder2Exception {
		P2Object value = P2Object.create();
		TaggedElement element = readXML(reader);
		if (element != null) {
			value = execute(source, element);
		}
		return value;
	}

	public P2Object execute(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		return executeObject(source, xml);
	}

	private P2Object executeObject(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		P2Object value = P2Object.create();
		try {
			String command = xml.getTag();
			if (trace) {
				System.err.print("Command is " + command);
				if (xml.getAttribute("name") != null)
					System.err.print(" " + xml.getAttribute("name"));
				System.err.println();
			}
			if (command.equals("ponder2"))
				value = executeXML(source, xml);
			else if (command.equals("assign"))
				value = executeAssign(source, xml);
			else if (command.equals("send"))
				value = executeSend(source, xml);
			else if (command.equals("use"))
				value = executeUse(xml);
			else if (command.equals("string"))
				value = new P2String(xml.getAttribute("value"));
			else if (command.equals("boolean"))
				value = P2Boolean.from(xml.getAttribute("value"));
			else if (command.equals("number"))
				value = new P2Number(xml.getAttribute("value"));
			else if (command.equals("block"))
				value = new P2Block(variables, xml);
			else if (command.equals("array"))
				value = createArray(source, xml);
			else if (command.equals("oid"))
				value = executeOID(xml);
			else
				throw new Ponder2ArgumentException("Unknown element type: "
						+ command);
		} catch (Ponder2Exception e) {
			e.addXML(xml);
			throw e;
		}
		return value;
	}

	/**
	 * @param xml
	 * @throws Ponder2Exception
	 */
	private P2Object executeXML(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		P2Object value = null;
		for (Iterator<?> iter = xml.iterator(); iter.hasNext();) {
			Element element = (Element) iter.next();
			if (!(element instanceof TaggedElement))
				System.out.println(element);
			else
				try {
					value = executeObject(source, (TaggedElement) element);
				} catch (Ponder2Exception e) {
					e.addSource(xml.getAttribute("source"));
					throw e;
				}
		}
		return value;
	}

	/**
	 * @param xml
	 * @throws Ponder2Exception
	 */
	private P2Object executeAssign(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		String name = xml.getAttribute("name");
		P2Object value = executeObject(source, (TaggedElement) xml.getChild(0));
		variables.put(name, value);
		return value;
	}

	/**
	 * Executes <send><oid/><command/>...</send>. Each command is evaluated and
	 * sent one by one to the OID.
	 * 
	 * @param xml
	 * @throws Ponder2Exception
	 */
	private P2Object executeSend(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		Iterator<Element> iter = xml.iterator();
		TaggedElement objxml = (TaggedElement) iter.next();
		P2Object obj = executeObject(source, objxml);
		P2Object value = obj;
		// We need to remember what we are working on, in case an error is
		// thrown
		TaggedElement currentXML = xml;
		try {
			while (iter.hasNext()) {
				Element element = iter.next();
				if (!(element instanceof TaggedElement))
					// Just a string, print it out, useful for debugging and
					// tracing
					System.out.println(element);
				else {
					// We have to parse and resolve the XML before handing it to
					// the
					// Managed Object
					TaggedElement message = (TaggedElement) element;
					currentXML = message;
					// The following should have the tag "message" - it is the
					// only valid
					// value
					String name = message.getAttribute("name");
					int size = Integer.parseInt(message.getAttribute("args",
							"0"));
					P2Object args[] = new P2Object[size];
					for (int i = 0; i < size; i++) {
						TaggedElement arg = (TaggedElement) message.getChild(i);
						currentXML = (TaggedElement) arg.getChild(0);
						args[i] = executeObject(source, currentXML);
					}
					if (name.equals("self"))
						value = obj;
					else {
						// TODO The variables should be made available to the
						// managed object
						value = obj.operation(source, name, args);
					}
				}
			}
		} catch (Ponder2ArgumentException e) {
			e.addXML(currentXML);
			throw e;
		}
		return value;
	}

	/**
	 * Evaluates an XML <use> clause. If the name can be found in the variables
	 * Map then simply return the contents.
	 * 
	 * @param xml
	 * @throws Ponder2Exception
	 */
	private P2Object executeUse(TaggedElement xml) throws Ponder2Exception {
		String name = xml.getAttribute("name");
		P2Object value = variables.get(name);
		if (value != null)
			return value;
		Path path = new Path(name);
		try {
			if (path.isRelative()) {
				value = variables.get(path.head(1));
				// System.err.println("Path head is " + path.head(1));
			}
			if (value != null)
				return Util.resolve(value, path.subpath(1));
			return Util.resolve("/", path);
		} catch (Ponder2ResolveException e) {
			throw new Ponder2ArgumentException("path not found '" + name + "'");
		}

	}

	/**
	 * @param xml
	 * @return
	 * @throws Ponder2Exception
	 */
	private P2Object createArray(P2Object source, TaggedElement xml)
			throws Ponder2Exception {
		int children = xml.elements();
		P2Object[] values = new P2Object[children];
		for (int i = 0; i < children; i++) {
			Element element = xml.getChild(i);
			if (!(element instanceof TaggedElement))
				throw new Ponder2ArgumentException(
						"Array element of incorrect type: " + element);

			values[i] = executeObject(source, (TaggedElement) element);
		}
		return new P2Array(values);
	}

	/**
	 * @param xml
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	private P2Object executeOID(TaggedElement xml)
			throws Ponder2ArgumentException, Ponder2OperationException {
		OID oid = OID.fromXML(xml);
		if (oid == null)
			throw new Ponder2ArgumentException("OID not found: "
					+ xml.toString());
		return oid.getP2Object();
	}

	/**
	 * reads and parses XML
	 * 
	 * @param input
	 *            the input stream to read from
	 * @return the parsed XML structure or null if a failure occured
	 */
	public static TaggedElement readXML(Reader input) {
		TaggedElement element = null;
		try {
			XMLReader reader = new XMLReader("xml", input);
			element = reader.parse();
		} catch (XMLParseException e) {
			// TODO Auto-generated catch block
			System.out.println("XMLParser: readXML");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("XMLParser: readXML");
			e.printStackTrace();
		}
		return element;
	}

}
