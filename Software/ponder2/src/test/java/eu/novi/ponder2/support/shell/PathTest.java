/**
 * Created on Sep 30, 2005
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
 * $Log: PathTest.java,v $
 * Revision 1.1  2005/09/30 11:04:21  kpt
 * Tidied up a little
 *
 */

package eu.novi.ponder2.support.shell;

import eu.novi.ponder2.Path;
import junit.framework.TestCase;

public class PathTest extends TestCase {

	Path path1;

	@Override
	protected void setUp() throws Exception {
		path1 = new Path("/a/b/c/d/e");
	}

	public void testSubpath() {
		assertEquals("/", path1.subpath(0, 0));
		assertEquals("/a/b", path1.subpath(0, 2));
		assertEquals("c", path1.subpath(3, 3));
		assertEquals("c/d/e", path1.subpath(3, 5));
		assertEquals("b/c/d", path1.subpath(2, 4));
		// Work out of bounds
		assertEquals("/a", path1.subpath(-3, 1));
		assertEquals("", path1.subpath(5, 3));
		assertEquals("c/d/e", path1.subpath(3, 22));
	}

	public void testHead() {
		assertEquals("/", path1.head(1));
		assertEquals("/a/b", path1.head(3));
		assertEquals("/a/b/c/d/e", path1.head(10));
		assertEquals("a/b/c/d/e", path1.head(-1));
		assertEquals("c/d/e", path1.head(-3));
		assertEquals("", path1.head(-6));
	}

	public void testTail() {
		assertEquals("e", path1.tail(1));
		assertEquals("c/d/e", path1.tail(3));
		assertEquals("/a/b/c/d/e", path1.tail(10));
		assertEquals("/a/b/c/d", path1.tail(-1));
		assertEquals("/a/b", path1.tail(-3));
		assertEquals("", path1.tail(-30));
	}

}
