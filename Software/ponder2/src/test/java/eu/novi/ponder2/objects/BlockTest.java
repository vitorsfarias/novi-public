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
 * Created on Jul 29, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.objects;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;
import junit.framework.TestCase;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class BlockTest extends TestCase {

	/**
	 * @param name
	 */
	public BlockTest(String name) {
		super(name);
	}

	public void testBlock() {
		P2Hash map = new P2Hash();
		map.put("myNum", P2Object.create(7));
		try {

			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);

			assertEquals(b.execute(P2Object.create()).asInteger(),
					3 * 7 + 12 + 7);
		} catch (Ponder2Exception e) {
			fail(e.getMessage());
		}
	}

}
