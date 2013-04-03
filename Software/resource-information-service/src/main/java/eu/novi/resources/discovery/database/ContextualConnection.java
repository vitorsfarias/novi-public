package eu.novi.resources.discovery.database;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.util.IMUtil;

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
 * 
 * a quick solution for a problem in reading context of alibaba.
 * it is not used any more
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ContextualConnection {
	URI currentContext;	
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(ContextualConnection.class);
	
	private static Repository memoryRepository;
	private static ObjectRepository memoryObjectRepository;
	private static ObjectConnection memoryObjectConnection;
	private static ObjectRepositoryFactory memoryObjectRepositoryFactory;
	
	public ContextualConnection() {
		createMemoryRepository();
	}
	
	public void setContext(URI context){
		this.currentContext = context;
		setupInternalMemoryToCurrentContext();
	
	}
	
	public void closeConnection()
	{
		
		try {
			memoryObjectConnection.close();
			memoryObjectRepository.shutDown();
		} catch (RepositoryException e) {
			log.warn("Problem in closing the contextual connection");
		}
		
	}

	private void setupInternalMemoryToCurrentContext() {
		ObjectConnection con = null;
		try {
			memoryObjectConnection.clear();
			con = ConnectionClass.getNewConnection();
			memoryObjectConnection.add(con.
					getStatements(null,null,null,true, currentContext));
		} catch (RepositoryException e) {
			log.warn("Problem in loading all the triples to the contextual connection");
		}
		finally
		{
			ConnectionClass.closeAConnection(con);
		}
	}
	
	
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
	
	public ObjectConnection getObjectConnection(){
		return memoryObjectConnection;
	}
		
}
