/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.rspecs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.osgi.service.log.LogService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.novi.im.core.Location;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.LocationImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAConstants;

/**
 * Parent class for all RSpecs.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public abstract class RSpecSchema {

	protected DocumentBuilderFactory dbfac;
	protected DocumentBuilder docBuilder;
	protected Document doc;

	private String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	public abstract void createEmptyRequestRSpec();
	//protected abstract Node getNextNode();
	public abstract Set<Resource> getResourceSet();
    // LogService to get for service mix blueprint
    protected LogService logService;
	/**
	 * All RSpec schemas will have a DocumentBuilder factory
	 */
	protected RSpecSchema () {
		// Creating an empty RSpec Document
		dbfac = DocumentBuilderFactory.newInstance();
		
		// Setting schema validation...
		dbfac.setNamespaceAware(true);
//		dbfac.setValidating(true);
		try {
			dbfac.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			// Creating document builder
			docBuilder = dbfac.newDocumentBuilder();
		} catch (final ParserConfigurationException e1) {
			logService.log(LogService.LOG_ERROR, "RH - Creating RSpec schema: " + e1.getMessage());
		} catch (final IllegalArgumentException x) {
			logService.log(LogService.LOG_ERROR, "RH - Creating RSpec schema: " + x.getMessage());
		} 
		doc = docBuilder.newDocument();
	}
	
	/**
	 * Creates a document with the RSpec received as a string
	 * @param: rspecString. The RSpec to analyze in a string form.
	 */
	public void readRSpec(String rspecString) {
        try {
        	dbfac.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
        			"http://www.w3.org/2001/XMLSchema");
			InputStream rspecIS = new ByteArrayInputStream(rspecString.getBytes("UTF-8"));
			docBuilder = dbfac.newDocumentBuilder();
			// parsing
			doc = docBuilder.parse(rspecIS);
			
		} catch (SAXException e) {
			logService.log(LogService.LOG_ERROR, "RH - Reading RSpec schema: " + e.getMessage());
		} catch (IOException e) {
			logService.log(LogService.LOG_ERROR, "RH - Reading RSpec schema: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			logService.log(LogService.LOG_ERROR, "RH - Reading RSpec schema: " + e.getMessage());
		}
	}
	
	/**
	 * Checks that the schema of the RSpec is valid against the .xsd 
	 * @param rspecString
	 * @return true if the RSpec is valid, false if not or error occurs
	 */
	public  boolean validateSchema (String rspecString) {
		DocumentBuilder validator = null;
		try {
			final InputStream rspecIS = new ByteArrayInputStream(rspecString.getBytes("UTF-8"));
			validator = dbfac.newDocumentBuilder();
			final SAXValidator handler = new SAXValidator();
			validator.setErrorHandler(handler);
			validator.parse(rspecIS);
			if (handler.isValid()) {
				return true;
			}
			return false;	
		} catch (final SAXException e) {
			logService.log(LogService.LOG_ERROR, 
    				"RH - Validanting RSpec schema: " + e.toString());
			return false;
		} catch (final IOException e) {
			logService.log(LogService.LOG_ERROR, 
    				"RH - Validanting RSpec schema: " + e.toString());
			return false;
		} catch (final ParserConfigurationException e) {
			logService.log(LogService.LOG_ERROR, 
    				"RH - Validanting RSpec schema: " + e.toString());
			return false;
		}
	}
	
	
	/**
	 * Gets the document builder and parses it to String. Hence, the RSpec 
	 * will be defined in a string
	 */
	public String toString(){
		// set up a transformer
		final TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = null;
		try {
			trans = transfac.newTransformer();
		} catch (final TransformerConfigurationException e) {
			logService.log(LogService.LOG_ERROR, 
    				"RH - Parsing RSpec to String: " + e.toString());
		}
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		//create string from DOM tree
		final StringWriter sw = new StringWriter();
		final StreamResult resultString = new StreamResult(sw);		  
		final DOMSource source = new DOMSource(doc);
		
		try {
			trans.transform(source, resultString);
		} catch (final TransformerException e) {
			logService.log(LogService.LOG_ERROR, 
    				"RH - Parsing RSpec to String: " + e.toString());
			}
		String rspecString = sw.toString();
		return rspecString;
	}
	
	/**
	 * extracts the information about the location of a node
	 * @param componentName: the name of the component, taken from the RSpec
	 * @param rspecElement: the element from the RSpec
	 * @return Location Object, with latitude and longitude information 
	 */
	protected Location getNodeLocation(String componentName, Element rspecElement) {
		Location location = new LocationImpl(componentName + "-" + SFAConstants.LOCATION);
		Float latitude = null; 
		Float longitude = null;
		
		NodeList locNodes = rspecElement.getElementsByTagName(SFAConstants.LOCATION); //this is actually just one node
		
		if (locNodes.getLength() > 0) {
			org.w3c.dom.Node crtChild = locNodes.item(0);
			latitude = new Float(crtChild.getAttributes().getNamedItem(SFAConstants.LATITUDE).getNodeValue());
			longitude = new Float(crtChild.getAttributes().getNamedItem(SFAConstants.LONGITUDE).getNodeValue());
		}
		
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		
		return location;
	}
	
	/**
	 * returns the information related to the hardware type of a node
	 * @param rspecElement: the element from the RSpec
	 * @return a string containing the hardware type
	 */
	protected String getHardwareType(Element rspecElement) {
		String hwType = "";
		
		NodeList hwNodes = rspecElement.getElementsByTagName(SFAConstants.HW_TYPE);
		if (hwNodes.getLength() > 0) {
			org.w3c.dom.Node crtChild = hwNodes.item(0);
			hwType = crtChild.getAttributes().getNamedItem(SFAConstants.NAME).getNodeValue();
		}
		
		return hwType;
	}
	
	/**
	 * creates the HRN from the componentId given in the RSpec
	 * @param componentId: string taken from the RSpec
	 * @return HRN 
	 */
	protected String getHRN(String componentId){ 
		String[] splitStr = componentId.split("\\+"); 
		String hrn = splitStr[1] + "." + splitStr[3]; 
		hrn = hrn.replaceAll(":", "."); 
		return hrn; 
	}
	
	/**
	 * extracts the interface URN from an item of a specified RSpec element, 
	 * where the items have been filtered by the specified tag
	 * @param element: an RSpec element
	 * @return string with the element URN (name)
	 */
	protected String getElementURN(Element element) {
		
		String elementComponentId = element.getAttribute(SFAConstants.COMPONENT_ID);
		return elementComponentId;
		
	} 
	
	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}
	
	/** 
	 * @return the number of resources available in the RSpec 
	 */
	public int getNumberOfResources() {
		return getResourceSet().size();
	}


}
