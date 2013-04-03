package eu.novi.ponder2.apt;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;

import junit.framework.TestCase;


import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;

import com.sun.mirror.apt.Filer;

import eu.novi.ponder2.apt.DocWriter;
import eu.novi.ponder2.apt.DocWriterHtml;

public class DocWriterHtmlTest extends TestCase {

	Mockery context = new Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	final Filer filer = context.mock(Filer.class);

	StringWriter htmlWriter = new StringWriter();
	final PrintWriter htmlPrinter = new PrintWriter(htmlWriter);

	StringWriter cssWriter = new StringWriter();
	final PrintWriter cssPrinter = new PrintWriter(cssWriter);

	StringWriter indexWriter = new StringWriter();
	final PrintWriter indexPrinter = new PrintWriter(indexWriter);

	DocWriterHtml dwh;

	@Override
	public void setUp() throws IOException {
		context.checking(new Expectations() {
			{
				one(filer).createTextFile(Filer.Location.SOURCE_TREE, "doc",
						new File("PonderDoc.css"), null);
				will(returnValue(cssPrinter));
				one(filer).createTextFile(Filer.Location.SOURCE_TREE, "doc",
						new File("Foo.html"), null);
				will(returnValue(htmlPrinter));
			}
		});

		dwh = new DocWriterHtml(filer, "Foo");
	}

	public void testDocWriterHtml() throws Exception {
		context.assertIsSatisfied();
		Field f = DocWriterHtml.class.getDeclaredField("out");
		f.setAccessible(true);
		assertEquals(f.get(dwh), htmlPrinter);
		f = DocWriterHtml.class.getDeclaredField("filer");
		f.setAccessible(true);
		assertEquals(f.get(dwh), filer);
	}

	public void testWriteClass() throws Exception {
		final Sequence seq = context.sequence("seq");

		context.checking(new Expectations() {
			{
				one(filer).createTextFile(Filer.Location.SOURCE_TREE, "doc",
						new File("index.html"), null);
				will(returnValue(indexPrinter));
			}
		});
		dwh.writeOperation("OpFoo:OpFred:Op3:", "FooComment",
				asList("Foo1", "Foo2", "Fred1"), "return");
		dwh.writeClass("MyClass", "SuperClass", "comment");
		assertTrue(indexWriter.toString().contains(
				"PonderTalk Object Reference"));
		assertTrue(indexWriter.toString().contains("MyClass.html"));
		assertTrue(htmlWriter.toString().contains("MyClass"));
		assertTrue(htmlWriter.toString().contains("Based on SuperClass"));
		context.assertIsSatisfied();
	}

	public void testSection() throws Exception {
		dwh.section(DocWriter.CONSTRUCTOR);
		assertTrue(htmlWriter.toString().contains("Factory Messages"));
		dwh.section(DocWriter.OPERATION);
		assertTrue(htmlWriter.toString().contains("Operational Messages"));
		dwh.section(2);
		assertTrue(htmlWriter.toString().contains(
				"Danger Will Robinson, error in Doc Writer!"));
	}

	public void testWriteOperationListOfStringStringListOfString()
			throws Exception {
		LinkedList<String> operation = new LinkedList<String>();
		LinkedList<String> parameters = new LinkedList<String>();
		operation.add("OpFoo:");
		operation.add("OpFred:");
		operation.add("Op3:");
		parameters.add("Foo1, Foo2");
		parameters.add("Fred1");
		dwh.writeOperation(operation, "FooComment", parameters, "return");
		assertTrue(htmlWriter.toString().contains("OpFoo:"));
		assertTrue(htmlWriter.toString().contains("OpFred:"));
		assertTrue(htmlWriter.toString().contains("Op3:"));
		assertTrue(htmlWriter.toString().contains("Foo1, Foo2"));
		assertTrue(htmlWriter.toString().contains("Fred1"));
		assertTrue(htmlWriter.toString().contains("ERROR PARAM MISSING"));
	}

	public void testClose() {
		dwh.close();
		context.assertIsSatisfied();
	}

}
