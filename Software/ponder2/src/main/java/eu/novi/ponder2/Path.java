/**
 * Created on Jul 7, 2005
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
 * $Log: Path.java,v $
 * Revision 1.3  2005/10/01 14:35:03  kpt
 * Improved toString
 *
 * Revision 1.2  2005/09/30 11:04:21  kpt
 * Tidied up a little
 *
 * Revision 1.1  2005/09/12 10:47:08  kpt
 * Initial Checkin
 *
 */

package eu.novi.ponder2;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import eu.novi.ponder2.Path;

/**
 * A path manipulation library. Handles Unix style path names and can index
 * separate parts of the path, extract parts of the path and normalise
 * pathnames.
 * 
 * @author Kevin Twidle
 * @version $Id: Path.java,v 1.3 2005/10/01 14:35:03 kpt Exp $
 */
public class Path implements Iterable<String>, Cloneable {

	/**
	 * Default string for the current directory/domain
	 */
	public static final String DOT = ".";

	/**
	 * Default string for a path separator
	 */
	public static final String SLASH = "/";

	/**
	 * Default string for the root
	 */
	public static final String ROOT = "/";

	/**
	 * Default string for a parent directory/domain
	 */
	public static final String DOTDOT = "..";

	private Vector<String> paths;

	/**
	 * creates a new Path based on a String value
	 * 
	 * @param path
	 *            the String pathname
	 */
	public Path(String path) {
		super();
		paths = new Vector<String>();
		set(path);
	}

	/**
	 * creates a new Path as a copy of another Path
	 * 
	 * @param path
	 *            the Path to be copied
	 */
	public Path(Path path) {
		super();
		paths = new Vector<String>(path.paths);
	}

	/**
	 * creates a new Path as a concatenation of a Path and a String joined with
	 * a SLASH
	 * 
	 * @param path
	 *            the first component of the pathname
	 * @param extra
	 *            the last component of the pathname
	 */
	public Path(Path path, String extra) {
		this(path);
		add(extra);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() throws CloneNotSupportedException {
		Path clone = (Path) super.clone();
		clone.paths = (Vector<String>) paths.clone();
		return clone;
	}

	/**
	 * clears the content of the Path
	 * 
	 */
	public void clear() {
		paths.clear();
	}

	/**
	 * Checks whether the path is complete or goes up from the current element
	 * 
	 * @return true if the path is complete
	 */
	public boolean isComplete() {
		return (paths.size() > 0 && !paths.get(0).equals(DOTDOT));
	}

	/**
	 * Checks whether the path starts from the current domain
	 * 
	 * @return true if the path is relative
	 */
	public boolean isRelative() {
		return (paths.size() > 0 && !paths.get(0).equals(SLASH));
	}

	/**
	 * Checks whether the path starts from the root
	 * 
	 * @return true if the path starts from the root
	 */
	public boolean isAbsolute() {
		return (paths.size() > 0 && paths.get(0).equals(SLASH));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<String> iterator() {
		return paths.iterator();
	}

	/**
	 * Returns a new Path being the parent of the current one
	 * 
	 * @return the Path of the parent
	 */
	public Path parent() {
		Path path = null;
		try {
			path = (Path) clone();
			path.add(DOTDOT);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}

	/**
	 * Returns the child part of the Path as a String
	 * 
	 * @return the child of the Path
	 */
	public String child() {
		return paths.lastElement();
	}

	/**
	 * sets the Path to be the same as a String
	 * 
	 * @param path
	 *            the new full pathname
	 */
	public void set(String path) {
		clear();
		add(path);
	}

	/**
	 * adds a component to the end of the Path. A SLASH is also added as a
	 * Separator if necessary.
	 * 
	 * @param path
	 */
	public void add(String path) {
		StringTokenizer st = new StringTokenizer(path, SLASH);
		if (path.startsWith(SLASH)) {
			paths.clear();
			paths.add(SLASH);
		}
		while (st.hasMoreTokens()) {
			String bit = st.nextToken();
			if (bit != null && !bit.equals(DOT)) {
				// Check for climbing up the path
				if (!bit.equals(DOTDOT)) {
					paths.add(bit);
				} else {
					// We have DOTODT
					// Special case if PATH is DOT
					if (paths.size() == 1 && paths.get(0).equals(DOT)) {
						paths.set(0, DOTDOT);
					} else
					// Ignore if we have we hit root
					if (!(paths.size() == 1 && paths.get(0).equals(SLASH))) {
						// Do we remove or add to the elements?
						if (paths.size() > 0
								&& !paths.lastElement().equals(DOTDOT)) {
							paths.remove(paths.size() - 1);
						} else {
							paths.add(bit);
						}
					}
				}
			}
		}
		if (paths.isEmpty()) {
			paths.add(DOT);
		}
	}

	/**
	 * returns the number of elements in the path
	 * 
	 * @return the number of elements in the path
	 */
	public int size() {
		return paths.size();
	}

	/**
	 * returns beginning n elements of the path. If the index is negative,
	 * returns all but the first n elements.
	 * 
	 * @param index
	 *            the number of elements to return
	 * @return the required part of the path as a string
	 */
	public String head(int index) {
		if (index < 0)
			return tail(paths.size() + index);
		return subpath(0, index - 1);
	}

	/**
	 * returns the last n elements of the Path. If the index is negative,
	 * returns all but the last n elements.
	 * 
	 * @param index
	 *            the number of elements to return
	 * @return the beginning of the Path as a String
	 */
	public String tail(int index) {
		if (index < 0)
			return head(paths.size() + index);
		return subpath(paths.size() - index, paths.size() - 1);
	}

	/**
	 * returns a subset of the path from the n'th element to the end.
	 * 
	 * @param start
	 *            the first element to copy
	 * @return a string containing the subset of the Path
	 */
	public String subpath(int start) {
		return subpath(start, paths.size() - 1);
	}

	/**
	 * returns a subset of the path indexed by the start element and the end
	 * element
	 * 
	 * @param start
	 *            the index of the first element of the substring
	 * @param end
	 *            the index of the last element of the substring
	 * @return the substring
	 */
	public String subpath(int start, int end) {
		StringBuffer result = new StringBuffer();
		if (start < 0)
			start = 0;
		if (end >= paths.size())
			end = paths.size() - 1;
		if (end < start)
			return "";
		boolean addSlash = false;
		while (start <= end) {
			if (addSlash)
				result.append('/');
			String s = paths.elementAt(start++);
			result.append(s);
			addSlash = !s.equals(SLASH);
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean addSlash = isRelative();
		for (String part : paths) {
			buf.append(part);
			if (addSlash)
				buf.append(SLASH);
			addSlash = true;
		}
		if (buf.length() > 1 && buf.charAt(buf.length() - 1) == '/')
			buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}

}
