package eu.novi.resources.discovery.database;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.util.IMUtil;


/**
 * 
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
 * It makes the connection to the Database
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ConnectionClass {


	private static Repository myRepository;

	//from allibaba
	private static ObjectRepository  objectRepository;
	private static ObjectConnection objectConnection ; 
	private static ObjectRepositoryFactory myFactory;	
	//if true then it crates http repository else native repository
	private static final boolean IS_HTTP_REPOSITORY = false;

	//if it true then it initialize the objectConnection 
	//otherwise the repositoryConnection
	private static final transient Logger log = 
			LoggerFactory.getLogger(ConnectionClass.class);
	private static final File REPOSITORY_PATH = 
			new File("./src/main/resources/DB/defaultRepository");
	private static final File TEST_REPOSITORY_PATH = 
			new File("./target/DB/testRepository");
	//for the http repository
	private static final String sesameServer = 
			"http://dev.adaptivedisclosure.org/openrdf-sesame";
	private static final String repositoryID = "NOVI_TEST";
	/**
	 * true if the default DB is running. If the DB is stopped
	 * then the value is unexpected
	 */
	private static boolean isDefaultDBRunning  = true;

	//Variables for the memory repository.
	//we use this repository to create object for testing
	//and return back to other services.
	private static Repository memoryRepository;
	private static ObjectRepository memoryObjectRepository;
	private static ObjectConnection memoryObjectConnection;
	private static ObjectRepositoryFactory memoryObjectRepositoryFactory;
	
	//private static final File LOG_ERROR_PATH = 
	//		new File("./src/main/resources/log.error");
	private static PrintWriter logErrPrintWriter;
	
	

	
	protected ConnectionClass()
	{
		
	}


	
	/**
	 * Start the storage service. It creates a repository and an
	 * object connection to the DB.
	 * It initialize the RIS service
	 * @param isDefaultDB if true then start the default
	 * storage service, otherwise the testing
	 * @return
	 * true if succeed
	 */
	public static boolean startStorageService(final boolean isDefaultDB)
	{
		if (checkIfStoreServiceOpen())
		{
			log.warn("The storage service you try to start is allready started");
			return true;
		}
		initErrorLogFile(); //initialize the print write for the error log file
		boolean succed = false;
		if(isDefaultDB)
		{

			if (null != createRepository(REPOSITORY_PATH))
			{
				succed = true;

			}
		}
		else
		{
			if (null != createRepository(TEST_REPOSITORY_PATH))
			{
				succed = true;

			}

		}

		if(succed)
		{
			if(createConnectionFromRepos())
			{
				if (isDefaultDB)
					log.info("The Default storage service was initialized succesfull");
				else
					log.info("The Testing storage service was initialized succesfull");

				isDefaultDBRunning = isDefaultDB;
				createMemoryRepository(); //create the memory repository and connection
				return true;
			}

		}
		log.error("Failed to initialize the storage service");
		return false;

	}
	
	
	/**log the error stack to the log.error file.
	 * I adds also the time
	 * @param exception
	 */
	public static void logErrorStackToFile(Exception exception)
	{

		logErrPrintWriter.println("\n" + Calendar.getInstance().getTime());
		exception.printStackTrace(logErrPrintWriter);
		log.warn(exception.getMessage());
		log.info("Please see in resources/log.error for the error stack");

	}
	
	/**
	 * initialize the log error file
	 */
	private static void initErrorLogFile()
	{
		try {
			//If the file already exists, start writing at the end of it.
			//auto flash print writer
			//logErrPrintWriter = new PrintWriter(new FileWriter(LOG_ERROR_PATH, true), true);
			logErrPrintWriter = new PrintWriter(new File("error.log"));
			log.info("The log error file was created");

		}
		catch (IOException e) {
			log.error("Problem creating the log error file");
			log.error(e.getMessage());
		}

	}
	
	private static void closeErrorLogFile()
	{
		if (logErrPrintWriter != null)
			logErrPrintWriter.close();
		log.info("The error log file was closed");
		
	}
	

	/**
	 * It checks if the storage service is open.
	 * @return
	 * true if the storage service is open
	 */
	protected static boolean checkIfStoreServiceOpen()
	{
		if(myRepository != null && objectConnection != null)
		{
			try {
				if (objectConnection.isOpen())
					return true;
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		return false;

	}
	
	/**
	 * Learn if the default db is running;
	 * @return  true if the default DB is running.
	 * If the DB is stopped then the value is unexpected.
	 */
	protected static boolean isDefaultDBRunning()
	{
		return isDefaultDBRunning;
	}
	

	/**
	 * create and return a  sesame repository. It initialize the local
	 * variable myRepository. If the isHttpRepository is true it creates
	 * a http repository otherwise it creates
	 * a local native repository
	 * @return the Repository or null if an error accur
	 */
	private static Repository createRepository(final File file)
	{
		if (IS_HTTP_REPOSITORY)
		{
			log.debug("Creating http repository...");
			myRepository = new HTTPRepository(sesameServer, repositoryID);
		}
		else
		{
			//create a native repository, with inferencer, that store
			//the data in the disc and is scalable
			log.debug("Creating native local repository...");
			NativeStore nativeStore	= new NativeStore(file);
			myRepository = new SailRepository(nativeStore);
			//new ForwardChainingRDFSInferencer(nativeStore)

		}
		try {
			myRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
			log.error("Failed to initialize the repository");
			return null;
		}
		return myRepository;

	}
	

/*	*//**
	 * create and return a repository to a local(sail) mysql sesame repository
	 * (it is not using know)
	 * @return the Repository or null if an error accur
	 *//*
	private Repository createLocalmySQLRepository()
	{
		MySqlStore sql = null;
        sql = new MySqlStore("sesame");
        sql.setUser("root");
        sql.setPassword("password");
        myRepository = new SailRepository(sql);
        try {
			myRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return null;
		}
        return myRepository;
	}*/

	/**
	 * create an object Connection from a sesame repository
	 * (it uses the local myRepository variable). It initialize the 
	 * variable objectConnection. First You have to create the repository
	 * @return
	 * true if succeed
	 */
	private static boolean createConnectionFromRepos()
	{
		if (myRepository == null)
		{
			log.warn("The Repository from which you try to create " +
					"Connection is null.You have to create the repository first");
			return false;
		}

		myFactory = new ObjectRepositoryFactory();
		try {
			objectRepository =  myFactory.createRepository(
					IMUtil.getRepositoryConfig(), myRepository);
		} catch (RepositoryConfigException e) {
			e.printStackTrace();
			log.error("The object connection to the DB failed");
			return false;
		} catch (RepositoryException e) {
			e.printStackTrace();
			log.error("The object connection to the DB failed");
			return false;
		}


		try {
			objectConnection = objectRepository.getConnection();

		} catch (RepositoryException e) {
			e.printStackTrace();
			log.error("The object connection to the DB failed");
			return false;
		}
	
		log.debug("The object connection to the DB was succesful");
		return true;
	
	}



	/**
	 * stop the storage service.
	 *  Close the repository and the connection to the DB.
	 * @return
	 * true if the service is stopped, or false if fail or the
	 * connection (or repository) is null.
	 */
	public static boolean stopStorageService()
	{
		destroyMemoryRepository();
		closeErrorLogFile();
		
		try {

			if (objectConnection != null)
			{
				if (objectConnection.isOpen())
				{
					objectConnection.close();
					log.debug("The object connection to the DB was closed");
				
				}
				else
				{
					log.debug("The connection is allready closed");
				
				}		
				//the object connection is closed

				//try to shut down the repository
				if (shoutDownRepository())
				{
					log.info("The storage service was stoped succesfully");
					return true;
				}
				else
				{
					log.warn("The stop storage service fail to shut down " +
							"the repository");
					return false;

				}
			}
			else
			{
				//object connection is null
				log.warn("The connection to the DB you tried to stop is null");
				return false;

			}


		} catch (RepositoryException e) {

			log.error("The stop storage service failed");
			e.printStackTrace();
			return false;
		}

	}



	/**
	 * close the object connection
	 */
	/*protected static void closeConnection()
	{
		try {
			if (objectConnection != null && objectConnection.isOpen())
			{
				objectConnection.close();
				log.debug("The object connection to the DB was closed");
				
			}
			else
			{
				log.debug("The connection is allready closed");
			}
		} catch (RepositoryException e) {
			log.warn("Problem closing the connection");
			ConnectionClass.logErrorStackToFile(e);
		}
	}*/
	
	/**
	 * open the connection
	 */
	/*protected static void openConnection()
	{
		if (checkIfStoreServiceOpen())
		{
			log.debug("The connection is already opened");
		}
		else
		{

			try {
				objectConnection = objectRepository.getConnection();
				log.debug("The object connection to the DB was opened");

			} catch (RepositoryException e) {
				e.printStackTrace();
				log.error("The object connection to the DB failed");
			}
		}
	}*/
	
	
	/**create a new object connection to the repository
	 * @return the ObjectConnection or null if a problem occur
	 */
	protected static ObjectConnection getNewConnection()
	{
		ObjectConnection objectConn = null;
		try {
			objectConn = objectRepository.getConnection();
			log.debug("A new object connection to the DB was opened");

		} catch (RepositoryException e) {
			e.printStackTrace();
			log.warn("The new object connection to the DB failed");
		}
		return objectConn;
		
	}
	
	/**close an object connection
	 * @param con the ObjectConnection to close
	 */
	protected static void closeAConnection(ObjectConnection con)
	{
		try {
			if (con != null && con.isOpen())
			{
				con.close();
				log.debug("The object connection to the DB was closed");
				
			}
			else
			{
				log.debug("The connection is allready closed");
			}
		} catch (RepositoryException e) {
			log.warn("Problem closing an object connection");
			ConnectionClass.logErrorStackToFile(e);
		}
	}
	
	

	/**
	 * shout down the repository
	 * @return
	 * true if succeed, false if the repository is null or an error occur
	 */
	private static boolean shoutDownRepository()
	{
		if (myRepository != null)
		{
			try {
				myRepository.shutDown();
				log.debug("The repository has been shouted down");
				return true;
			} catch (RepositoryException e) {
				log.error("The repository has failed to been shouted down");
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			log.warn("The repository you try to shout down is null");
			return false;
		}
	}
	

	/**
	 * get the object connection to the DB
	 * @return
	 * the ObjectConnection
	 */
	/*protected static ObjectConnection getConnection2DB()
	{
		if (objectConnection == null)
			log.warn("The object connection you are going to use is null");
		else
			try {
				if (!objectConnection.isOpen())
					log.warn("The object connection you are going to use is closed");
			} catch (RepositoryException e) {
				log.error("Error in geting the ObjectConnection");
				e.printStackTrace();
			}
		
		return objectConnection;
	}*/


	/**
	 * get the object factory of the connection to the DB.
	 * The objects that is created with this factory is also stored
	 * in the persist DB
	 * @return the ObjectFactory or null if the connection is closed
	 */
	protected static ObjectFactory getObjectFactory()
	{
		if (checkIfStoreServiceOpen())
			return objectConnection.getObjectFactory();

		log.warn("The connection to the DB is closed. " +
				"You can't get the object factory");
		return null;

	}
	
	
	/**get a value factory from an object connection to the repository
	 * @return the ValueFactory object or null, if the connection is closed
	 */
	protected static ValueFactory getValueFactory()
	{
		if (checkIfStoreServiceOpen())
		{
			return objectConnection.getValueFactory();
		}
		else
		{
			log.warn("The object connection is close, I can not get the value factory");
			return null;
		}
	}


	///////////FUNCTIONS FOR MEMORY STORAGE //////////////////////

	/**
	 * it create a memory repository and object connection.
	 * We use this repository to create object for testing and
	 * return back to other services.
	 * If we use the default file repository then it will be 
	 * stored some information there and we don't want that.
	 */
	private static void createMemoryRepository() {

		try {
			memoryRepository = new SailRepository(new MemoryStore());
			memoryRepository.initialize();

			memoryObjectRepositoryFactory = new ObjectRepositoryFactory();


			memoryObjectRepository = memoryObjectRepositoryFactory.createRepository(
					IMUtil.getRepositoryConfig(), memoryRepository);
			memoryObjectConnection = memoryObjectRepository.getConnection();

			log.info("Successfully initialize the memory repository");

		} catch (RepositoryConfigException e) {
			e.printStackTrace();
			log.error("This is not good, failed to initialize memory repository");
		} catch (RepositoryException e) {
			e.printStackTrace();
			e.getMessage();
			log.error("This is not good, failed to initialize memory repository");
		} 
	}
	

	/**
	 * shout down the memory repository
	 */
	private static void destroyMemoryRepository() {
		try {

			if (memoryObjectConnection != null)
				if (memoryObjectConnection.isOpen())
					memoryObjectConnection.close();
			if (memoryObjectRepository != null)
				memoryObjectRepository.shutDown();

		} catch (RepositoryException e) {
			e.printStackTrace();
			log.error("Failed to shutdown memory repository");
		}
		log.info("Successfully shut down memory repository");
	}
	
	

	/**
	 * get the object factory of the memory connection.
	 * We use that to create objects that will not be 
	 * stored in the DB
	 * @return the ObjectFactory or null if the connection is closed
	 */
	public static ObjectFactory getMemoryObjectFactory()
	{
		if (memoryObjectRepository != null)
			try {
				if (memoryObjectConnection.isOpen())
					return memoryObjectConnection.getObjectFactory();
			} catch (RepositoryException e) {
				log.error("Error in accessing the memory object connection");
				e.printStackTrace();
			}

		log.warn("The memory connection is closed. " +
				"You can't get the object factory");
		return null;

	}
	
	/**
	 * get the memory object connection to the DB
	 * We use memory connection to create objects that will not be 
	 * stored in the DB
	 * @return
	 * the ObjectConnection
	 */
	public final static ObjectConnection getConnection2MemoryRepos()
	{
		if (memoryObjectConnection == null)
			log.warn("The memory object connection you are going to use is null");
		else
			try {
				if (!memoryObjectConnection.isOpen())
					log.warn("The memory object connection you are going to use is closed");
			} catch (RepositoryException e) {
				log.error("Error in geting the memory ObjectConnection");
				e.printStackTrace();
			}
		
		return memoryObjectConnection;
	}


}

