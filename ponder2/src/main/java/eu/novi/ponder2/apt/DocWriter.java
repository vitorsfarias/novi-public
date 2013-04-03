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
 * Created on Mar 29, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.apt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.mirror.apt.Filer;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class DocWriter {

	public final static int CONSTRUCTOR = 0;
	public final static int OPERATION = 1;

	DocWriter(Filer filer, String className) throws IOException {
	}

	public abstract void writeClass(String className, String superClass,
			String comment);

	protected abstract void writeOperation(List<String> operations,
			String comment, List<String> parameterList, String returnType);

	public abstract void close();

	public void writeOperation(String operation, String comment,
			List<String> parameterList, String returnType) {
		writeOperation(splitOperation(operation), strip(comment),
				parameterList, returnType);
	}

	public static List<String> splitOperation(String operation) {
		List<String> result = new Vector<String>();
		if (operation.indexOf(':') < 0) {
			result.add(operation);
		} else {
			StringTokenizer st = new StringTokenizer(operation, ":");
			while (st.hasMoreTokens()) {
				result.add(st.nextToken() + ":");
			}
		}
		return result;
	}

	/**
	 * strips comment chars from comments
	 * 
	 * @param comment
	 * @return the stripped comment
	 */
	protected String strip(String comment) {
		String result = "";
		if (comment == null)
			return result;
		StringReader sr = new StringReader(comment);
		BufferedReader in = new BufferedReader(sr);
		try {
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				line = line.trim();
				while (line.length() > 0
						&& (line.charAt(0) == '/' || line.charAt(0) == '*'))
					line = line.substring(1);
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) == '@')
					break;
				result += " " + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Tells the writer which section we are in
	 * 
	 * @param section
	 */
	public abstract void section(int section);

	public abstract void endSection(int section);

}
