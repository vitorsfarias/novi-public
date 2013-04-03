package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.ManipulateDB;
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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ManipulateDbTest {
	
	private static final transient Logger log =
			LoggerFactory.getLogger(ManipulateDbTest.class);
	
	//TestbedCommunication mockTestbed;

	
	@BeforeClass 
	public static void setUpBeforeClass() 
	{
		ConnectionClass.startStorageService(false);
		
	}

	@AfterClass 
	public static void tearDownAfterClass()
	{
		TestbedCommunication.assignCalls2TestbedFromRHStatic(null);
		ConnectionClass.stopStorageService();
	}
	
	@Test
	public void testUpdate() throws RepositoryException
	{
		//ConnectionClass.startStorageService(true);
		ManipulateDB.clearTripleStore();
		ObjectConnection con = ConnectionClass.getNewConnection();
		//add a node in different context
		con.setAddContexts(NoviUris.createNoviURI("context3"));
		Node nod = new NodeImpl("stayNode");
		con.addObject(nod);
		// Setting up local environment
		log.debug("Setting up local environment...");
		//mockTestbed = mock(TestbedCommunication.class);

		//stubbing mockRIS
		log.debug("stubbing mockRIS...");
		//create a simple planetlab platform
		Platform platform = new PlatformImpl("planetlab");
		Node node = new NodeImpl("myNode");
		node.setHardwareType("x86");
		platform.setContains(IMUtil.createSetWithOneValue(node));
		//when(mockTestbed.getTestbedSubstrate()).
		//thenReturn(platform);
		////////
		RHListResourcesResponseImpl testbedResponse =  mock(RHListResourcesResponseImpl.class);
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		when(testbedResponse.getPlatformString()).thenReturn(imRepo.exportIMObjectToString(platform));
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.listResources(any(String.class))).
		thenReturn(testbedResponse);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		//////
		
		//LocalDbCalls.showAllContentOfDB();
		ManipulateDB.updateDBfromTestbed();
		//LocalDbCalls.showAllContentOfDB();
		
		Platform dbPlatf = IRMLocalDbCalls.getSubstrate("planetlab");
		assertEquals("x86", ((Node)dbPlatf.getContains().toArray()[0]).getHardwareType());

		//onother node with no context
		Node nod2 = new NodeImpl("stayNode22");
		con.addObject(nod2);

		//updated version of planetlab
		Platform platform2 = new PlatformImpl("planetlab");
		Node node2 = new NodeImpl("myNode");
		node2.setHardwareType("x86-32");
		platform2.setContains(IMUtil.createSetWithOneValue(node2));
		//when(mockTestbed.getTestbedSubstrate()).
		//thenReturn(platform2);
		///
		when(testbedResponse.getPlatformString()).thenReturn(imRepo.exportIMObjectToString(platform2));
		when(calls2TestbedFromRHMock.listResources(any(String.class))).
		thenReturn(testbedResponse);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		///
		
		ManipulateDB.updateDBfromTestbed();
		//LocalDbCalls.showAllContentOfDB();
		Platform dbPlatf2 = IRMLocalDbCalls.getSubstrate("planetlab");
		assertEquals("x86-32", ((Node)dbPlatf2.getContains().toArray()[0]).getHardwareType());

		//TODO get back the substrate
		ConnectionClass.closeAConnection(con);
		//ConnectionClass.startStorageService(true);
	}
	
	@Test
	public void testUpdateGetSubstrate() throws RepositoryException
	{
		//ConnectionClass.startStorageService(true);
		ManipulateDB.clearTripleStore();
		ObjectConnection con = ConnectionClass.getNewConnection();
		//get the federica platform in memory objects
		Platform memPlatform = createPlatformTopology();
		//add a node in different context
		con.setAddContexts(NoviUris.createNoviURI("context3"));
		Node nod = new NodeImpl("stayNode");
		con.addObject(nod);
		// Setting up local environment
		log.debug("Setting up local environment...");
		//mockTestbed = mock(TestbedCommunication.class);

		//stubbing mockRIS
		log.debug("stubbing mockRIS...");

		//when(mockTestbed.getTestbedSubstrate()).
		//thenReturn(memPlatform);
		///
		RHListResourcesResponseImpl testbedResponse =  mock(RHListResourcesResponseImpl.class);
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		when(testbedResponse.getPlatformString()).thenReturn(imRepo.exportIMObjectToString(memPlatform));
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.listResources(any(String.class))).
		thenReturn(testbedResponse);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		
		//LocalDbCalls.showAllContentOfDB();
		ManipulateDB.updateDBfromTestbed();
		//LocalDbCalls.showAllContentOfDB();
		
		Platform dbPlatf = IRMLocalDbCalls.getSubstrate("FEDERICA");
		assertEquals(33, dbPlatf.getContains().size());
		log.debug("contains : " + dbPlatf.getContains().size());

		ConnectionClass.closeAConnection(con);

		//TODO get back the substrate
		

	}
	

	@Test	
	public void testCalls() {
		
		
		try {
			ObjectConnection con = ConnectionClass.getNewConnection();
			ManipulateDB.clearTripleStore();
			assertTrue(con.isEmpty());
			ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML");
			assertFalse(con.isEmpty());
			ManipulateDB.clearTripleStore();
			assertTrue(con.isEmpty());
			ManipulateDB.cleanDBandLoadOWLFile("PLEtopologyModified3.owl");
			assertFalse(con.isEmpty());
			assertFalse(ManipulateDB.loadOWLFile("file_not_exist", "RDFXML"));
			ConnectionClass.closeAConnection(con);
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
			
		}
	}
	
	
	/**create the federica platform topology with memory objects
	 * at the end it cleans the DB
	 * @return
	 * @throws RepositoryException 
	 */
	private Platform createPlatformTopology() throws RepositoryException
	{
		ManipulateDB.clearTripleStoreMemory();
		ManipulateDB.loadOWLFileMemory("FEDERICA_substrate.owl", ManipulateDB.TESTBED_CONTEXTS);
		Platform platform = (Platform) ConnectionClass.getConnection2MemoryRepos().
				getObject(NoviUris.createNoviURI("FEDERICA"));
		IMCopy copier = new IMCopy();
		Platform platformImpl = (Platform) copier.copy(platform, -1);
		ManipulateDB.clearTripleStoreMemory();
		
		return platformImpl;
		
	}
	


	@Test
	public void testloadFileWhileDefaultDBRunning() 
	{
		ConnectionClass.stopStorageService();
		ConnectionClass.startStorageService(true);

		assertFalse(ManipulateDB.loadOwlFileTestDB("PLEtopologyModified3.owl"));
		assertFalse(ManipulateDB.clearTribleStoreTestDB());
		
		ConnectionClass.stopStorageService();

	}
	
	@Test
	public void testloadFileWhileDefaultDBNotRunning() 
	{
		ConnectionClass.stopStorageService();
		ConnectionClass.startStorageService(false);

		assertTrue(ManipulateDB.loadOwlFileTestDB("PLEtopologyModified3.owl"));
		assertTrue(ManipulateDB.clearTribleStoreTestDB());
		
		ConnectionClass.stopStorageService();

	}
	@Test
	public void testUnexistentFile(){
		assertFalse(ManipulateDB.loadOWLFileMemory("NonExistentFile"));
	}
	
	@Test
	public void testUpdateDBFromTestBed(){
		//TestbedCommunication tbc = mock(TestbedCommunication.class);
		//when(tbc.getTestbedSubstrate()).thenReturn(null);
		
		///
		RHListResourcesResponseImpl testbedResponse =  mock(RHListResourcesResponseImpl.class);
		when(testbedResponse.getPlatformString()).thenReturn(null);
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.listResources(any(String.class))).
		thenReturn(testbedResponse);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		//
		ConnectionClass.startStorageService(true);

		ManipulateDB.updateDBfromTestbed();

		assertNotNull(ManipulateDB.getTestbedContextURI());
		ConnectionClass.stopStorageService();

	}

	@Test
	public void testClearTripleStoreWhenDefaultIsRunning(){
		ConnectionClass.stopStorageService();
		ConnectionClass.startStorageService(true);
		assertFalse(ManipulateDB.clearTribleStoreTestDB());
	}

}
