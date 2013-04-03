package eu.novi.ponder2.objects;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import com.twicom.qdparser.TaggedElement;

import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2XML;

public class P2XMLTest extends TestCase {

	public P2XMLTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	
	/* public void testP2XMLTaggedElement() { try {
	 TaggedElement t_xml = new TaggedElement("xml"); 
	 TaggedElement t_foo = new TaggedElement("Foo"); 
	 t_foo.add("Fred1=Fred2"); t_xml.add(t_foo); 
	 P2XML xml = new P2XML(t_xml); 
	 assertEquals(xml.asString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>\n<Foo>Fred1=Fred2</Foo>\n</xml>");
	 } catch (Ponder2Exception e) 
	 {fail(e.toString());
	 } 
	 }*/
	 

	public void testP2XMLString() {
		try {
			P2XML xml = new P2XML("<xml><Foo>Fred1=Fred2</Foo></xml>");
			assertEquals(
					xml.asString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><Foo>Fred1=Fred2</Foo></xml>");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testP2XMLNode() {
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			P2XML xml = new P2XML(builder.parse(new ByteArrayInputStream(
					"<xml><Foo>Fred1=Fred2</Foo></xml>".getBytes())));
			assertEquals(
					xml.asString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><Foo>Fred1=Fred2</Foo></xml>");
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testXPathNodeSet() {
		// fail("Not yet implemented");
	}

	public void testXPathNode() {
		// fail("Not yet implemented");
	}

	public void testXPathString() {
		try {
			P2XML xml = new P2XML("<xml><Foo>Fred1=Fred2</Foo></xml>");
			assertEquals(xml.xPathString("xml"), "Fred1=Fred2");
			xml = new P2XML(
					"<xml><Foo>Fred1=Fred2</Foo><Foo2>Fred3=Fred4</Foo2></xml>");
			assertEquals(xml.xPathString("xml"), "Fred1=Fred2Fred3=Fred4");
			xml = new P2XML(
					"<xml><Foo>Fred1=Fred2</Foo><Foo2>Fred3=Fred4</Foo2></xml>");
			assertEquals(xml.xPathString("//Foo2"), "Fred3=Fred4");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testXPathBoolean() throws Exception {
		P2XML xml = new P2XML("<xml><Foo>true()</Foo></xml>");
		assertTrue(xml.xPathBoolean("xml"));
		xml = new P2XML("<xml>true()</xml>");
		assertFalse(xml.xPathBoolean("xmll"));
		xml = new P2XML("<xml><Foo>0</Foo><Foo2>false()</Foo2></xml>");
		assertTrue(xml.xPathBoolean("//Foo"));
	}

	public void testXPathNumber() throws Exception {
		P2XML xml = new P2XML("<xml><Foo>7</Foo></xml>");
		assertEquals(7, xml.xPathNumber("xml"));
	}

	public void testOperation() {
		try {
			P2XML xml = new P2XML("<xml><Foo>Fred1=Fred2</Foo></xml>");
			assertEquals(
					xml.operation(null, "asString").asString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><Foo>Fred1=Fred2</Foo></xml>");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testAsString() {
		try {
			P2XML xml = new P2XML("<xml><Foo>Fred1=Fred2</Foo></xml>");
			assertEquals(
					xml.asString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><Foo>Fred1=Fred2</Foo></xml>");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

	public void testToString() {
		try {
			P2XML xml = new P2XML("<xml><Foo>Fred1=Fred2</Foo></xml>");
			assertEquals(
					xml.toString(),
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><Foo>Fred1=Fred2</Foo></xml>");
		} catch (Ponder2Exception e) {
			fail(e.toString());
		}
	}

}
