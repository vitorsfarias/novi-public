package eu.novi.requesthandler.sfa.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import eu.novi.requesthandler.sfa.rspecs.SAXValidator;

public class SAXValidatorTest {

	@Test
	public void createSAXValidator() {
		SAXValidator handler = new SAXValidator();
		assertNotNull(handler);
	}
	
	@Test
	public void resetSAXValidator() {
		SAXValidator handler = new SAXValidator();
		handler.reset();
		assertTrue(handler.isValid());
	}
	
	@Test
	public void setWarning() {
		LocatorImpl locator = new LocatorImpl();
		locator.setColumnNumber(1);
		locator.setLineNumber(12);
		SAXParseException exception = new SAXParseException("unitTest exception", locator);
		SAXValidator handler = new SAXValidator();
		handler.warning(exception);
		assertFalse(handler.isValid());
		assertEquals("Warning: unitTest exception at line 12 column 1", handler.getErrors().get(0));
	}
	
	@Test
	public void setFatalError() {
		LocatorImpl locator = new LocatorImpl();
		locator.setColumnNumber(5);
		locator.setLineNumber(9);
		SAXParseException exception = new SAXParseException("unitTest fatal error", locator);
		SAXValidator handler = new SAXValidator();
		handler.fatalError(exception);
		assertFalse(handler.isValid());
		assertEquals("Fatal Error: unitTest fatal error at line 9 column 5", handler.getErrors().get(0));
	}
	
	@Test
	public void setErrorAndReset() {
		LocatorImpl locator = new LocatorImpl();
		locator.setColumnNumber(9);
		locator.setLineNumber(23);
		SAXParseException exception = new SAXParseException("unitTest error", locator);
		SAXValidator handler = new SAXValidator();
		handler.error(exception);
		assertFalse(handler.isValid());
		assertEquals("Error: unitTest error at line 23 column 9", handler.getErrors().get(0));
		
		handler.reset();
		assertTrue(handler.isValid());
		assertEquals(1, handler.getErrors().size());
	}
	
	@Test
	public void setListOfErrors() {
		List<String> errors = new ArrayList<String>();
		errors.add("error num 1");
		errors.add("error num 2");
		errors.add("error num 3");
		SAXValidator handler = new SAXValidator();
		handler.setErrors(errors);
		
		assertNotNull(handler.getErrors());
		assertEquals(3, handler.getErrors().size());
		assertTrue(handler.getErrors().contains("error num 2"));
		
	}
	
}
