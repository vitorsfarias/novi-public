package eu.novi.ponder2.apt;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;


import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import com.sun.mirror.apt.Filer;

import eu.novi.ponder2.apt.DocWriter;
import eu.novi.ponder2.apt.DocWriterHtml;

public class DocWriterTest extends TestCase {
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

	public void testSplitsOperationIntoListOfLexicalElements() {
		assertEquals(DocWriter.splitOperation("OpFoo:OpFred:Op3:"),
				asList("OpFoo:", "OpFred:", "Op3:"));
	}

	public void testWriteOperationStringStringListOfString() throws Exception {
		dwh.writeOperation("OpFoo:OpFred:Op3:", "FooComment",
				asList("Foo1, Foo2", "Fred1"), "return");

		assertTrue(htmlWriter.toString().contains("ERROR PARAM MISSING"));

		context.assertIsSatisfied();
	}

	public void testStrip() throws Exception {
		assertEquals(dwh.strip(null), "");
		assertEquals(dwh.strip(""), "");
		assertEquals(dwh.strip("    foo    "), " foo");
		assertEquals(dwh.strip("  foo  \n  fred  \n  "), " foo fred ");
		assertEquals(dwh.strip("foo\n@bla\nfred"), " foo");
		assertEquals(dwh.strip("foo\n/bla\nfred\n*fred2"),
				" foo bla fred fred2");
	}

}
