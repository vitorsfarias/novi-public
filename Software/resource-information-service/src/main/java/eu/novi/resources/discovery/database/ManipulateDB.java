package eu.novi.resources.discovery.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Platform;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;


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
 * It has some functions to manipulate the DB.
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ManipulateDB {
	private static final transient Logger log = 
			LoggerFactory.getLogger(ManipulateDB.class);
	
	
	
	/**
	 * the testbed context string with out the novi base address
	 */
	protected static final String TESTBED_CONTEXTS_STR = "testebedSubstrateConexts";
	
	
	/**
	 * all the information that are got from the testbed
	 * are stored with this contexts URI
	 */
	public static final URI TESTBED_CONTEXTS =
			NoviUris.createNoviURI(TESTBED_CONTEXTS_STR);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConnectionClass.startStorageService(true);
		ManipulateDB.cleanDBandLoadOWLFile("PLEtopologyModified.owl");
		ConnectionClass.stopStorageService();
	}
	
	
	/**update the testbed information in the DB
	 * @param testbedCom
	 * @return true if succed otherwise false
	 */
	public static boolean updateDBfromTestbed()
	{
		log.info("Updating the testbed information in the DB...");
		ObjectConnection con = ConnectionClass.getNewConnection(); //get an new connection to the repository
		Platform platf = TestbedCommunication.getTestbedSubstrate();
		if (platf == null)
		{
			log.warn("Problem in updating the DB from the testbed advertisment. " +
					"The answer from Request handler is null");
			ConnectionClass.closeAConnection(con);
			return false;
		}
		//log.info(platf.getContains().toString());
			
		//log.debug("I get : " + top.getContains().toString());
		boolean succ = false;
		try {
			//clear the old data from testbed
			con.clear(TESTBED_CONTEXTS);
			//set the testbed contexts
			con.setAddContexts(TESTBED_CONTEXTS);
			//int numBefore = LocalDbCalls.execStatementReturnSum(null, null, null, TESTBED_CONTEXTS);
			//add the new data
			con.addObject(platf);
			log.info("The DB was updated successfully with the new data from testbed");
			succ = true;
			log.info("Printing all the content of the DB...");
			LocalDbCalls.execPrintStatement(null, null, null, false, TESTBED_CONTEXTS);
			/*int numAfter = LocalDbCalls.execStatementReturnSum(null, null, null, TESTBED_CONTEXTS);
			if (numBefore == numAfter)
			{
				log.warn("The new data was not inserted to the DB");
			}
			else
			{
				log.info("The DB was updated successfully with the new data from testbed");
				
			}*/
		} catch (RepositoryException e) {
			log.error("Repository error in updating the DB");
			ConnectionClass.logErrorStackToFile(e);
		}
		finally 
		{

			ConnectionClass.closeAConnection(con);
			
			
		}
		return succ;
	}



	/**
	 * it clean the sesame DB from all triples;
	 * It works only if the testing db is running.
	 * @return true if succeed otherwise false
	 */
	public static final boolean clearTribleStoreTestDB()
	{
		if (ConnectionClass.isDefaultDBRunning())
		{
			log.warn("The default DB is running. You can not " +
					"execute clearTribleStoreTestDB");
			return false;
		}
		else
		{
			return clearTripleStore();
		}
	}

	/**
	 * it clean the sesame DB from all triples.
	 * @return true if succeed otherwise false
	 */
	protected static final boolean clearTripleStore()
	{
		/*if (!ConnectionClass.checkIfStoreServiceOpen())
		{

			log.warn("The storage service is stopped. " +
					"The clear trible store can't be executed");
			return false;
		}*/
		ObjectConnection con = ConnectionClass.getNewConnection();

		try {

			if (con.isEmpty())
			{
				log.info("The DB you try to empty is already empty");

			}
			else
			{
				con.clear();
				log.info("The triples from the DB were removed succesfully");
			}
			ConnectionClass.closeAConnection(con);
			return true;
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
			log.error("Failed to remove the triples from the DB");
			ConnectionClass.closeAConnection(con);
			return false;
		}

	}
	
	
	
	/**
	 * it clean the memory repository from all triples.
	 * @return true if succeed otherwise false
	 */
	public static final boolean clearTripleStoreMemory()
	{
		if (ConnectionClass.getConnection2MemoryRepos() == null)
		{

			log.warn("The memory repository object is null. " +
					"The clear trible store can't be executed");
			return false;
		}

		try {

			if (ConnectionClass.getConnection2MemoryRepos().isEmpty())
			{
				log.info("The memory repository you try to empty is already empty");
				return true;

			}
			else
			{
				ConnectionClass.getConnection2MemoryRepos().clear();
				log.info("The triples from the memory repository were removed succesfully");
				return true;
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Failed to remove the triples from the memory repository");
			return false;
		}

	}


	/**
	 * Clean the DB and after that load the file.
	 * @param file the OWL file
	 * @return true id succeed
	 */
	protected static boolean cleanDBandLoadOWLFile(final String file)
	{

		if (clearTripleStore())
		{
			return loadOWLFile(file, "RDFXML" );

		}
		else
		{
			log.warn("The clean up of DB was not succesfull. " +
					"The insert owl file was canceled");
			return false;
		}

	}


	/**
	 * Loading owl file created from editor;
	 * It works only if the testing db is running.
	 * @param file the owl file
	 * @return true if succeed otherwise false
	 */
	public static boolean loadOwlFileTestDB(final String file, URI...contexts)
	{
		if (ConnectionClass.isDefaultDBRunning())
		{
			log.warn("The default DB is running. You can not " +
					"execute loadOwlFileTestDB");
			return false;
		}
		else
		{
			return loadOWLFile(file, "RDFXML", contexts);
		}
		
	}

	/**
	 * Loading owl file created from editor;
	 * it use the objectConnection in the ConnectionClass
	 * @param file the owl file
	 * @param type the type of the file, can be RDFXML or NTRIPLES.
	 * the default is RDFXML
	 * @return true if succeed otherwise false
	 */
	protected static boolean loadOWLFile(final String file, String type, URI... contexts) {

		/*if (!ConnectionClass.checkIfStoreServiceOpen())
		{

			log.warn("The storage service is stopped. " +
					"The load OWL file can't be executed");
			return false;
		}*/

		Bundle bundle = null;
		try {
			bundle = FrameworkUtil.getBundle(ManipulateDB.class);
		} catch (NoClassDefFoundError e1) {
			log.error("Problem to get the bundle. Probaply is not running in" +
					"Service Mix");
			e1.printStackTrace();
			System.out.println();
		}
		BufferedReader fileReader = null;
		if (bundle != null) {
			InputStream resourceInputStream = null;
			try {
				resourceInputStream = bundle.getEntry(file).openStream();
				fileReader = new BufferedReader(new InputStreamReader(resourceInputStream));
				log.info("This one is getting from bundle");
			} catch (IOException e) {
				log.error("Bundle can't find resource, no file for RIS");
				return false;
			}
		} 
		else {
			URL url = ManipulateDB.class.getResource("/"+file);
			if (url == null)
			{
				log.warn("The file " + file + " was not found");
				return false;
			}
			try {
				fileReader = new BufferedReader(new FileReader(url.getPath()));
			} catch (FileNotFoundException e) {
				log.error("Failed to open local file :" + file);
				return false;
			}
			log.info("The file " + file + " was found");
		}


		ObjectConnection con = ConnectionClass.getNewConnection();
		String baseURI = "";
		try {

			if (fileReader != null)
			{
				if (contexts.length > 0 )
				{
					con.setAddContexts(contexts);
				}
				
				if (type.equals("NTRIPLES"))
				{
					con.add(fileReader, baseURI, RDFFormat.NTRIPLES);
				}
				else
				{
					con.add(fileReader, baseURI, RDFFormat.RDFXML);
				}
				
				log.info("The OWL file " + file + " was successfully inserted to the DB");
				ConnectionClass.closeAConnection(con);
				return true;
			}

		} catch (RDFParseException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);	
		} catch (IOException e) {
			ConnectionClass.logErrorStackToFile(e);
		}
		log.info("The OWL file " + file + " was failed to be inserted to the DB");
		ConnectionClass.closeAConnection(con);
		return false;

	} //end of loadOwlFile	
	
	
	/**load in the DB a file in triples format
	 * @param file
	 * @param contexts
	 * @return
	 */
	public static boolean loadTripleOWLFile(final String file, URI... contexts) {
		
		return loadOWLFile(file, "NTRIPLES", contexts);
		
	}
	
	
	
	/**
	 * Loading owl file created from editor to the memory repository;
	 * @param file the owl file
	 * @return true if succeed otherwise false
	 */
	protected static boolean loadOWLFileMemory(final String file, URI... contexts) {

		BufferedReader fileReader = null;
	
			URL url = ManipulateDB.class.getResource("/"+file);
			if (url == null)
			{
				log.warn("The file " + file + " was not found");
				return false;
			}
			try {
				fileReader = new BufferedReader(new FileReader(url.getPath()));
			} catch (FileNotFoundException e) {
				log.error("Failed to open local file :" + file);
				return false;
			}
			log.info("The file " + file + " was found");
		


		String baseURI = "";
		try {

			if (fileReader != null)
			{
				if (contexts.length > 0 )
				{
					ConnectionClass.getConnection2MemoryRepos().setAddContexts(contexts);
				}
				ConnectionClass.getConnection2MemoryRepos().add(fileReader, baseURI, RDFFormat.RDFXML);
				log.info("The OWL file " + file + " was successfully inserted to the memory repository");
				return true;
			}

		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			if (contexts.length > 0 )
			{
				ConnectionClass.getConnection2MemoryRepos().setAddContexts();
			}
			
		}
		log.info("The OWL file " + file + " was failed to be inserted to the memory repository");
		return false;

	} //end of loadOwlFile
	
	/**get the testbed contexts URI
	 * all the information that are got from the testbed
	 * are stored with this contexts URI
	 * @return
	 */
	public static URI getTestbedContextURI()
	{
		return TESTBED_CONTEXTS;
	}
	
	
	/**add IM objects to the DB.
	 * Testing DB must be running
	 * @param object the IM object
	 * @param contexts the contexts to be added
	 */
	public static void addObjectToTestDB(Object object, URI... contexts)
	{
		if (ConnectionClass.isDefaultDBRunning())
		{
			log.warn("The default DB is running. I can not add object");
			return ;
		}
		
		ObjectConnection con = ConnectionClass.getNewConnection();
		try {
			con.setAddContexts(contexts);
			con.addObject(object.toString(), object);
			log.debug("The resource {} was added successfuly to the DB. Conetxts : {}", object, contexts);
		} catch (RepositoryException e) {
			log.warn("Problem in addObjectToTestDB");
			e.printStackTrace();
		} finally {
			ConnectionClass.closeAConnection(con);
		}
		
	}

}
