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
 * Created on Jul 10, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import eu.novi.ponder2.PonderTalk;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;

public class P2HashTest extends TestCase {

	P2Hash p2hash;
	P2Object v;
	P2Hash h;
	HashMap<String, P2Object> hash;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		p2hash = new P2Hash();
		v = P2Object.create();
		hash = new HashMap<String, P2Object>();
		hash.put("Foo", v);
		hash.put("Fred", v);
		h = new P2Hash(hash);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		hash.clear();
		h.clear();
		hash = null;
		h = null;
		v = null;
	}

	private P2Block translate(String code) throws Ponder2OperationException {
		try {
			Constructor c = PonderTalk.class
					.getDeclaredConstructor(new Class[] { P2Object.class });
			c.setAccessible(true);
			PonderTalk p = (PonderTalk) c.newInstance(new Object[] { P2Object
					.create() });
			return (P2Block) p.executePonderTalk(code);
		} catch (Exception e) {
			fail(e.toString());
		}
		return null;
	}

	/**
	 * @param name
	 */
	public P2HashTest(String name) {
		super(name);
	}

	/**
	 * Test method for
	 * {@link eu.novi.ponder2.objects.P2Hash#operation_at_put(java.lang.String, eu.novi.ponder2.objects.P2Value)}
	 * .
	 */
	public void testOperation_at_put() {
		HashMap p2hash_hash = null;
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			p2hash_hash = (HashMap) f.get(p2hash);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertFalse("hash not empty", p2hash_hash.containsKey("Fred"));
		p2hash.operation_at_put("Fred", p2hash);
		assertTrue("Fred not found", p2hash_hash.containsKey("Fred"));
		assertEquals("wrong object", p2hash, p2hash_hash.get("Fred"));
		assertEquals(h.operation_at_put("fooo", v), v);
		assertTrue(h.containsKey("fooo"));
	}

	/**
	 * Test method for
	 * {@link eu.novi.ponder2.objects.P2Hash#operation_remove(java.lang.String)}
	 * .
	 */
	public void testOperation_remove() {
		testRemove();
	}

	/**
	 * Test method for
	 * {@link eu.novi.ponder2.objects.P2Hash#operation_has(java.lang.String)}.
	 */
	public void testOperation_has() {
		testContainsKey();
	}

	/**
	 * Test method for
	 * {@link eu.novi.ponder2.objects.P2Hash#operation_at(java.lang.String)}.
	 */
	public void testOperation_at() {
		try {
			assertEquals(h.operation_at("Foo"), v);
		} catch (eu.novi.ponder2.exception.Ponder2ArgumentException e) {
			fail(e.getMessage());
		}
		try {
			assertFalse(h.operation_at("Freddi").equals(v));
			fail("Exception expected");
		} catch (Ponder2ArgumentException e) {
		}

	}

	public void testAsHash() {
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			HashMap h_hash = (HashMap) f.get(h.asHash());
			assertEquals(h_hash, hash);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation() {
		try {
			assertEquals(h.operation(null, "at:", P2Object.create("Foo")), v);
			assertTrue(h.operation(null, "has:", P2Object.create("Fred"))
					.asBoolean());
			assertFalse(h.operation(null, "has:", P2Object.create("Fred2"))
					.asBoolean());
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testP2Hash() {
		P2Hash myHash = new P2Hash();
		HashMap myHash_hash = null;
		assertNotNull(myHash);
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			myHash_hash = (HashMap) f.get(myHash);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertTrue(myHash_hash.isEmpty());
	}

	public void testP2HashMapOfStringP2Value() {
		assertNotNull(h);
		HashMap h_hash = null;
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			h_hash = (HashMap) f.get(h);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(h_hash, hash);
	}

	public void testAsMap() {
		assertEquals(h.asMap(), hash);
	}

	public void testSet() {
		HashMap h_hash = null;
		HashMap temp_hash = null;
		P2Hash temp = new P2Hash();
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			h_hash = (HashMap) f.get(h);
			temp_hash = (HashMap) f.get(temp);
		} catch (Exception e) {
			fail(e.toString());
		}
		temp.set(h);
		assertEquals(temp_hash, h_hash);
		h.remove("Foo");
		assertFalse(temp.equals(h));
	}

	public void testOperation_do() {
		try {
			P2Hash map = new P2Hash();
			map.put("myDiet", P2Object.create("Apple nutrition each day:"));
			String p2xml = P2Compiler
					.parse("[ :arg1 :arg2 | myDiet := myDiet + \" \" + arg1 + \" eats \" + arg2 + \";\"]");
			P2Block b = (P2Block) new XMLParser(map).execute(P2Object.create(),
					p2xml);
			h.clear();
			h.put("Foo", P2Object.create(3));
			h.put("Fred", P2Object.create(5));
			h.put("Fred2", P2Object.create(7));
			h.operation_do(null, b);
			Field f = P2Block.class.getDeclaredField("variables");
			f.setAccessible(true);
			HashMap<String, P2Object> m = (HashMap) f.get(b);
			System.out.println(m.get("myDiet").asString());
			assertEquals(m.get("myDiet").asString(),
					"Apple nutrition each day:");
			// TODO: order in which elements were added is not maintained
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_collate() {

		try {
			P2Block b = translate("[ :arg1 :arg2 | arg1 + \" says hello \" + (arg2 + 4) + \" times\" ]");
			h.clear();
			h.put("Foo", P2Object.create(-1));
			h.put("Fred", P2Object.create(6));
			h.put("Fred2", P2Object.create(16));
			P2Array res = h.operation_collect(null, b);
			assertEquals(res.values.get(2).asString(),
					"Fred2 says hello 20 times");
			assertEquals(res.values.get(0).asString(), "Foo says hello 3 times");
			assertEquals(res.values.get(1).asString(),
					"Fred says hello 10 times");
			assertEquals(res.values.size(), 3);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testOperation_at_ifAbsent() {
		try {
			assertEquals(h.operation_at_ifAbsent(null, "Foo", null), v);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testClear() {
		HashMap h_hash = null;
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			h_hash = (HashMap) f.get(h);
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(h_hash, hash);
		assertEquals(h.size(), 2);
		h.clear();
		assertTrue(h_hash.isEmpty());
	}

	public void testContainsKey() {
		assertTrue(h.containsKey("Fred"));
		assertTrue(h.containsKey("Foo"));
		assertFalse(h.containsKey(null));
		assertFalse(h.containsKey(""));
		assertFalse(h.containsKey("foo"));
	}

	public void testContainsValue() {
		assertTrue(h.containsValue(v));
		assertFalse(h.containsValue(null));
	}

	public void testEntrySet() {
		assertEquals(h.entrySet(), hash.entrySet());
	}

	public void testGet() {
		assertEquals(h.get("Fred"), h.get("Foo"));
		assertEquals(h.get("Foo"), v);
		assertNull(h.get(""));
	}

	public void testIsEmpty() {
		assertFalse(h.isEmpty());
		assertTrue(new P2Hash().isEmpty());
	}

	public void testKeySet() {
		assertEquals(h.keySet(), hash.keySet());
		assertTrue(h.keySet().contains("Fred"));
		assertTrue(h.keySet().contains("Foo"));
		assertEquals(h.keySet().size(), 2);
		h.clear();
		assertTrue(h.keySet().isEmpty());
	}

	public void testPut() {
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			HashMap h_hash = (HashMap) f.get(h);
			HashMap new1_hash = (HashMap) f.get(new P2Hash(hash));
			assertEquals(h_hash, new1_hash);
			P2Hash temp = new P2Hash();
			temp.putAll(hash);
			h.remove("Fred");
			HashMap<String, P2Object> temp_hash = (HashMap) f.get(temp);
			HashMap new2_hash = (HashMap) f.get(new P2Hash(temp_hash));
			assertFalse(h_hash.equals(new2_hash));
			temp.remove("Fred");
			HashMap new3_hash = (HashMap) f.get(new P2Hash(temp_hash));
			assertEquals(h_hash, new3_hash);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testPutAll() {
		h.clear();
		assertTrue(h.isEmpty());
		h.putAll(hash);
		HashMap h_hash = null;
		HashMap new_hash = null;
		try {
			Field f = P2Hash.class.getDeclaredField("hash");
			f.setAccessible(true);
			h_hash = (HashMap) f.get(h);
			new_hash = (HashMap) f.get(new P2Hash(hash));
		} catch (Exception e) {
			fail(e.toString());
		}
		assertEquals(new_hash, h_hash);
	}

	public void testRemove() {
		assertEquals(2, h.size());
		P2Object entry = h.remove("Fred");
		assertEquals(1, h.size());
		assertEquals(P2Null.Null, entry);
	}

	public void testSize() {
		assertEquals(2, h.size());
		h.remove("Fred");
		assertEquals(1, h.size());
		assertEquals(0, new P2Hash().size());
	}

	public void testValues() {
		assertTrue(h.values().contains(v));
		assertEquals(h.values().size(), 2);
		assertEquals(new P2Hash().values().size(), 0);
	}

	public void testToString() {
		assertNotNull(h.toString());
		h.clear();
		assertEquals(h.toString(), "{ }");
	}

}
