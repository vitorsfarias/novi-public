package eu.novi.resources.discovery.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Reservation;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.UrisUtil;
import eu.novi.im.util.Validation;


/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * A class that read information from the DB and it creates OWL files from that.
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class OwlCreator {
	
	private static final transient Logger log =
			LoggerFactory.getLogger(OwlCreator.class);
	
	private final static String HEADER_ONTOLOGY = 
			 "<owl:Ontology rdf:about=\"http://fp7-novi.eu/imcore.owl\">\n" +
			 "<owl:imports rdf:resource=\"https://dl.dropbox.com/u/13290647/novi-im/novi-im.owl\"/>\n" +
			 "</owl:Ontology>";
	
	private final static String HEADER = 
			"xmlns=\"http://fp7-novi.eu/im.owl#\"\n" +
			 "xml:base=\"http://fp7-novi.eu/im.owl\"\n" +
			 "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
			 "xmlns:swrl=\"http://www.w3.org/2003/11/swrl#\"\n" +
			 "xmlns:protege=\"http://protege.stanford.edu/plugins/owl/protege#\"\n" +
			 "xmlns:xsp=\"http://www.owl-ontologies.com/2005/08/07/xsp.owl#\"\n" +
		     "xmlns:unit=\"http://fp7-novi.eu/unit.owl#\"\n" +
			 "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
			 "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
			 "xmlns:swrlb=\"http://www.w3.org/2003/11/swrlb#\"\n" +
			 "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
			 HEADER_ONTOLOGY ;
	

	
	private static final String DIRECTORY_PATH = "./target/owlFiles4slices/";
	
	/**it return the slice description in an OWL/RDF string format.
	 * NOT USED ANTY MORE. AN OLD VERSION
	 * @param sliceURI the sliceURI to get it from the DB
	 * @return the slice description in an rdf/xml format or null if the slice was not found.
	 * It add in the beginning of the string the xmlns and owl declarations 
	 */
	public static String getSliceInfoToString(String sliceURI)
	{
		
		
		Reservation slice = IRMLocalDbCalls.getLocalSlice(sliceURI);
		if (slice == null)
		{
			log.warn("getSliceInfoToFile: The slice : {}, was not found.", sliceURI);
			return null;
		}
		log.info("The slice {} was found", sliceURI);
	
		Validation validation = new Validation();
		if (!validation.checkLinksForSinkSource(slice).equals(""))
		{
			log.warn("There is a problem with the stored bound topology. " +
					"Some relation on a link are missing");
			return null;


		}
		
		IMRepositoryUtil imRepos = new IMRepositoryUtilImpl();
		//Excluding managed entities in output.
		String sliceStr = imRepos.exportIMObjectToStringWithFilter(slice,
				"http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity");
		if (sliceStr == null)
		{
			log.warn("Failed to translate the java objects to String");
			return null;
		}
		 
		Pattern pat = Pattern.compile("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">");   
		Matcher mat = pat.matcher(sliceStr);   
		
		return mat.replaceFirst(HEADER).toString();
		
	}
	
	
	/**it return the slice manifest description in an OWL/XML string format
	 * @param sliceURI the slice URI
	 * @return the slice description in an rdf/xml format or an empty string if the slice was not found.
	 *
	 */
	public static String getSliceManifestInfoToString(String sliceURI)
	{

		String sliceManifStr = exportDBinOwl(NoviUris.getSliceManifestContextUri(sliceURI).toString());
		//TODO exclude policy exportIMObjectToStringWithFilter(slice, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity");
		
		if (sliceManifStr == null)
		{
			log.warn("The slice manifest information was not found for the slice {}", sliceURI);
			return "";
		}
		 		
		return importOntology(sliceManifStr);
		
	}

	/**import in the String the header ontology
	 * (a link that refer to the novi-im ontology)
	 * You need that in order the owl file to be readable by protege
	 * @param rdfStr
	 * @return the new string including the ontology
	 */
	protected static String importOntology(String rdfStr)
	{
		//find he first accurance of > after the <?xml version="1.0" encoding="UTF-8"?>
		int ind = rdfStr.indexOf('>', 38); 
		String start = rdfStr.substring(0, ind+1);
		String end = rdfStr.substring(ind+2);
		return start + "\n" + HEADER_ONTOLOGY + "\n" + end;
	}

	
	/**It exports the DB information in OWL format.
	 * It returns exactly what it reads, it doesn't made any processing in the data
	 * @param context will export the information with this context. 
	 * If it is null, then the context is not set
	 * @return the information in a rdf/owl format or null if it is not found
	 */
	public static String exportDBinOwl(String context) {


		log.info("Exporting in OWL the DB info with context {}", context);
		String answer = null;
		if (context != null)
		{
			int count = LocalDbCalls.execStatementReturnSum(null, null, null, NoviUris.createURI(context));
			if (count == 0)
			{
				log.warn("There is no information in the DB with context {}", context);
				return null;
			}
		}
		
		
		ObjectConnection connection = ConnectionClass.getNewConnection();
		StringWriter stringWriter = new StringWriter();
		RDFXMLWriter rdfwriter = new RDFXMLWriter(stringWriter);
		
		
		try {

			if (context == null)
				connection.export(rdfwriter);
			else
				connection.export(rdfwriter, NoviUris.createURI(context));

			answer = stringWriter.getBuffer().toString();
			
			if (answer.isEmpty())
			{
				log.warn("Was not found any information with context: {}", context);
				answer = null;
			}

		} catch(RepositoryException e) {
			log.warn("Problem in export DB info in OWL :" + e.getMessage());
		} catch (RDFHandlerException e) {
			log.warn("Problem in export DB info in OWL :" + e.getMessage());
		} finally {
			ConnectionClass.closeAConnection(connection);

		}
		return answer;
	}


	/**store the slice information in a OWL/RDF file
	 * the file name is the sliceURN + .owl
	 * @param sliceURI
	 */
	public void storeSliceInfoToFile(String sliceURI)
	{
		
		
		String sliceSt = getSliceInfoToString(sliceURI);   
		createStoreFile(UrisUtil.getURNfromURI(sliceURI) + ".owl", sliceSt);
	}
	
	
	/**it finds all the virtual nodes in the slice and it create an OWL/RDF string 
	 * for each one
	 * @param sliceURI the slice URI
	 * @return a set of OWL/RDF Strings, each String contain one virtual node.
	 * if no one virtual node was found or an error happen then the set is empty
	 */
	public Set<String> getVNodes4SliceToString(String sliceURI)
	{
		log.debug("I will find the virtual nodes in the slice: {}", sliceURI);
		ObjectConnection con = ConnectionClass.getNewConnection();
		Set<String> answer = new HashSet<String>();
		IMRepositoryUtil imRepos = new IMRepositoryUtilImpl();
		IMCopy copier = new IMCopy();
		try {
			
			con.setReadContexts(NoviUris.createURI(sliceURI));
			Result<VirtualNode>  vNodes =  con.getObjects(VirtualNode.class);
			if (!vNodes.hasNext())
			{
				log.warn("getVNodes4SliceToString: I did not find any virtual nodes in the slice: {}",
						sliceURI);

			}
			
			while (vNodes.hasNext())
			{
				VirtualNode vNode = vNodes.next();
				log.debug("I found the virtual node : {}", vNode.toString());
				answer.add(imRepos.exportIMObjectToString(copier.copy(vNode, -1)));
			}
		
		} catch (RepositoryException e) {
			
			ConnectionClass.logErrorStackToFile(e);
		}
		catch (ClassCastException e)
		{
			ConnectionClass.logErrorStackToFile(e);
			
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		}
	
		ConnectionClass.closeAConnection(con);
		return answer;
		
	}
	
	/**store all the virtual nodes in the slice to 
	 * separates OWL/RDF files.
	 * The file name are the sliceURN + virtualNode + number + .owl
	 * @param sliceURI
	 */
	public void storeVNodesToFiles(String sliceURI)
	{
		Set<String> vNodes = getVNodes4SliceToString(sliceURI);
		int i = 1;
		for (String  st : vNodes)
		{
			createStoreFile(UrisUtil.getURNfromURI(sliceURI) + "-VirtualNode" + i + ".owl", st);
			i++;
		}
		
	}
	
	
	/**create and store a file with the given fileName and content.
	 * if the file already exist, then delete it and create the new one
	 * @param fileName the file name. the path is taken from the constant DIRECTORY_PATH
	 * @param content
	 */
	public void createStoreFile(String fileName, String content)
	{
		try {
			File dir = new File(DIRECTORY_PATH);
			dir.mkdir();
			File myfile = new File(DIRECTORY_PATH + fileName);

			myfile.setWritable(true);
			if (myfile.exists())
			{
				log.debug("The file : {} exist, I will delete it", fileName);
				myfile.delete();
			}

			FileWriter fwrite = new FileWriter(myfile);
			fwrite.write(content);
			fwrite.flush();
			fwrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static String getHeaderOntol()
	{
		return HEADER_ONTOLOGY;
	}
	
	

}
