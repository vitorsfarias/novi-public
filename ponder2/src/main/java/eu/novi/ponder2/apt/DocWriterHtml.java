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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import eu.novi.ponder2.apt.DocWriter;

import com.sun.mirror.apt.Filer;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class DocWriterHtml extends DocWriter {

	private PrintWriter out;
	private Filer filer;
	private static PrintWriter index = null;

	/**
	 * @param filer
	 * @param className
	 * @throws IOException
	 */
	DocWriterHtml(Filer filer, String className) throws IOException {
		super(filer, className);
		this.filer = filer;
		out = filer.createTextFile(Filer.Location.SOURCE_TREE, "doc", new File(
				className + ".html"), null);

		try {
			// Copy the css file over
			PrintWriter pdocOut = filer.createTextFile(
					Filer.Location.SOURCE_TREE, "doc",
					new File("PonderDoc.css"), null);
			BufferedInputStream pdocIn = new BufferedInputStream(this
					.getClass().getResourceAsStream("PonderDoc.css"));
			while (true) {
				int ch = pdocIn.read();
				if (ch == -1)
					break;
				pdocOut.write(ch);
			}
			pdocOut.close();
			pdocIn.close();
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.adaptor.DocWriter#writeClass(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void writeClass(String className, String superClass, String comment) {
		comment = strip(comment);
		writeIndexFile(filer, className, comment);
		writeHeader(out, className);
		if (!superClass.equals("eu.novi.ponder2.P2ObjectAdaptor"))
			out.println("<h2>Based on " + superClass + "</h2>");
		out.println("<p class='object_description'>");
		out.println(comment);
		out.println("</p>");
	}

	private void writeHeader(PrintWriter pw, String title) {
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<meta http-equiv=Content-Type content=\"text/html; charset=windows-1252\">");
		pw.println("<title>" + title + "</title>");
		pw.println("<link rel='stylesheet' type='text/css' href='PonderDoc.css' />");
		pw.println("</head>");
		pw.println("<body>");
		pw.println("<h1 class='object-title'>" + title + "</h1>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.adaptor.DocWriter#section(int)
	 */
	@Override
	public void section(int section) {
		String text;
		String header = null;
		switch (section) {
		case DocWriter.CONSTRUCTOR:
			text = "Factory Messages";
			header = "<tr><th>Operation</th><th>Description</th>";
			break;
		case DocWriter.OPERATION:
			text = "Operational Messages";
			header = "<tr><th>Operation</th><th>Return</th><th>Description</th>";
			break;
		default:
			text = "Danger Will Robinson, error in Doc Writer!";
		}
		out.println("<table class='message-table'><tr><th colspan='3' class='message-header'>"
				+ text + "</th></tr>");
		out.println(header);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.apt.DocWriter#endSection(int)
	 */
	@Override
	public void endSection(int section) {
		out.println("</table><p/>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.adaptor.DocWriter#writeOperation(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void writeOperation(List<String> operation, String comment,
			List<String> parameters, String returnType) {
		int opSize = operation.size();
		int paramSize = parameters.size();
		String line = "";
		for (int i = 0; i < opSize; i++) {
			String op = operation.get(i);
			line += "<span class='keyword'>" + op + "</span>";
			// Is this a unary operation?
			char ch = op.charAt(0);
			if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
					&& !op.endsWith(":"))
				break;
			line += " ";
			if (i < paramSize)
				line += "<span class='argument'>" + parameters.get(i)
						+ "</span>";
			else
				line += "<b>ERROR PARAM MISSING</b>";
			line += " ";
		}
		out.println("</tr>");
		out.println("<td class='message-prototype'>" + line + "</td>");
		if (returnType != null) {
			int index = returnType.lastIndexOf('.');
			if (index > 0)
				returnType = returnType.substring(index + 1);
			out.println("<td class='message-return'>"
					+ (returnType.equals("void") ? "self" : returnType)
					+ "</td>");
		}
		out.println("<td class='message-description'>" + comment + "</td>");
		out.println("</tr>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.adaptor.DocWriter#close()
	 */
	@Override
	public void close() {
		out.close();
	}

	/**
	 * @param objects
	 * @throws IOException
	 */
	private void writeIndexFile(Filer filer, String className, String comment) {
		try {
			index = filer.createTextFile(Filer.Location.SOURCE_TREE, "doc",
					new File("index.html"), null);
			writeHeader(index, "PonderTalk Object Reference");
			index.println("");
			index.println("<table class='object-table'><tr><th colspan='2' class='object-header'>"
					+ "Managed Object" + "</th></tr>");

		} catch (IOException e) {
		}
		index.println("<tr>");
		index.println("<td><a href=\"" + className + ".html" + "\">"
				+ className + "</a></td>");
		index.println("<td>" + comment + "</td>");
		index.println("</tr>");
		index.flush();
		// index.println("</body>");
		// index.println("</html>");
	}

}
