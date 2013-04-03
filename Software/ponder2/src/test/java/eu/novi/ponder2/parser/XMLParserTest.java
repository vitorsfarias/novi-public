package eu.novi.ponder2.parser;

import java.io.StringReader;
import java.util.HashMap;

import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.parser.P2Compiler;
import eu.novi.ponder2.parser.XMLParser;

import junit.framework.TestCase;

public class XMLParserTest extends TestCase {

	public XMLParserTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testXMLParser() {
		XMLParser p = new XMLParser();
		assertNotNull(p);
		assertTrue(p instanceof XMLParser);
		assertNotNull(p.variables);
		assertTrue(p.variables instanceof P2Hash);
		assertEquals(p.variables.size(), 1);
		assertEquals(p.variables.get("Variables"), p.variables);
	}

	public void testXMLParserP2Hash() {
		P2Hash h = new P2Hash();
		h.put("Foo", P2Object.create(7));
		XMLParser p = new XMLParser(h);
		assertNotNull(p);
		assertTrue(p instanceof XMLParser);
		assertEquals(p.variables, h);
		assertEquals(p.variables.size(), 1);
		try {
			assertEquals(p.variables.get("Foo").asInteger(), 7);
		} catch (Ponder2ArgumentException e) {
			fail(e.toString());
		}
	}

	public void testExecuteP2ObjectString() {
		try {
			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			HashMap<String, P2Object> map = new HashMap<String, P2Object>();
			map.put("myNum", P2Object.create(7));
			P2Block b = (P2Block) new XMLParser(new P2Hash(map)).execute(null,
					p2xml);
			assertEquals(b.operation_value0(null).asInteger(), 3 * 7 + 12 + 7);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}

	}

	public void testExecuteP2ObjectReader() {
		try {
			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			HashMap<String, P2Object> map = new HashMap<String, P2Object>();
			map.put("myNum", P2Object.create(7));
			P2Block b = (P2Block) new XMLParser(new P2Hash(map)).execute(null,
					new StringReader(p2xml));
			assertEquals(b.operation_value0(null).asInteger(), 3 * 7 + 12 + 7);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testExecuteP2ObjectTaggedElement() {
		try {
			String p2xml = P2Compiler.parse("[ 3 * 7 + 12 + myNum]");
			HashMap<String, P2Object> map = new HashMap<String, P2Object>();
			map.put("myNum", P2Object.create(7));
			P2Block b = (P2Block) new XMLParser(new P2Hash(map)).execute(null,
					XMLParser.readXML(new StringReader(p2xml)));
			assertEquals(b.operation_value0(null).asInteger(), 3 * 7 + 12 + 7);
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testReadXML() {
		testExecuteP2ObjectTaggedElement();
	}

}
