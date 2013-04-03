package eu.novi.resources.discovery.database;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


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
 *  test the connection to the DB
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ConnectionTest {
	

	
	public void testGetEmptyConnection2MemoryRepo(){
		assert(ConnectionClass.getConnection2MemoryRepos() == null);
	}
	
	public void testGetEmptyMemoryObjectFactory(){
		assert(ConnectionClass.getMemoryObjectFactory() == null);
	}
	
	public void testGetEmptyObjectFactory(){
		assert(ConnectionClass.getObjectFactory() == null);
	}
	

	
	@Test
	public void testStartStopDB()
	{

		assertTrue(ConnectionClass.startStorageService(true));
		assertTrue(ConnectionClass.checkIfStoreServiceOpen());
		assertTrue(ConnectionClass.stopStorageService());
		assertFalse(ConnectionClass.checkIfStoreServiceOpen());
		
		assertTrue(ConnectionClass.startStorageService(false));
		assertTrue(ConnectionClass.checkIfStoreServiceOpen());
		assertTrue(ConnectionClass.stopStorageService());
		assertFalse(ConnectionClass.checkIfStoreServiceOpen());
		
	}
	

	
}
