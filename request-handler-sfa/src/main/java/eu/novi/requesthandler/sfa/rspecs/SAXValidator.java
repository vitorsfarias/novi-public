/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.rspecs;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Class used for validating if the XML created is aligned to the schema.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 *
 */
public class SAXValidator implements ErrorHandler {
	  
	// Flag to check whether any errors have been spotted.
	private boolean valid = true;
	// List of warnings and errors
	private List<String> errors = new ArrayList<String>();
	  
	public boolean isValid() {
		return valid; 
	}
	  
	// If this handler is used to parse more than one document, 
	// its initial state needs to be reset between parses.
	public void reset() {
		// Assume document is valid until proven otherwise
		valid = true; 
	}
	  
	public void warning(SAXParseException exception) {
	    
		errors.add("Warning: "+exception.getMessage()+
				" at line "+exception.getLineNumber()+
				" column "+exception.getColumnNumber());
		valid = false;
	    
	}
	  
	public void error(SAXParseException exception) {
	     
		errors.add("Error: "+exception.getMessage()+
				" at line "+exception.getLineNumber()+
				" column "+exception.getColumnNumber()); 
	    valid = false;
	    
	}
	  
	public void fatalError(SAXParseException exception) {
	     
		errors.add("Fatal Error: "+exception.getMessage()+
				" at line "+exception.getLineNumber()+
				" column "+exception.getColumnNumber());
	    valid = false;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	
	
}
