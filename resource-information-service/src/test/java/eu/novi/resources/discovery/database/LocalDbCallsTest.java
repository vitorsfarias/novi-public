package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.cglib.core.Local;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.im.util.UrisUtil;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.impl.IRMCallsImpl;

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
public class LocalDbCallsTest {
	
	private String base = "http://fp7-novi.eu/im.owl#";
	private String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private String unit = "http://fp7-novi.eu/unit.owl#";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//LocalDbCalls.showAllContentOfDB();
		ConnectionClass.stopStorageService();
	}

	@Test
	public void testURI() {
		//test NOVI URI
		assertTrue("http://fp7-novi.eu/im.owl#testName".
				equals(NoviUris.createNoviURI("testName").toString()));
		assertFalse("http://fp7-novi.eu/im.owl#testName".
				equals(NoviUris.createNoviURI("testname").toString()));
		
		//test RDF URI
		assertTrue("http://www.w3.org/1999/02/22-rdf-syntax-ns#rdfTest".
				equals(NoviUris.createRdfURI("rdfTest").toString()));
		assertFalse("http://www.w3.org/1999/02/22-rdf-syntax-ns#rdfTest".
				equals(NoviUris.createRdfURI("rdftest").toString()));
		
		assertEquals("testName", UrisUtil.getURNfromURI("http://fp7-novi.eu/im.owl#testName"));
		assertEquals("testName", UrisUtil.getURNfromURI("testName"));
		
	}
	
	@Test
	public void testExecStatementReturnRes()
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		addSt(con, base+"node1", rdf+"type", base+"Node", base+"context1");
		addSt(con, base+"node2", rdf+"type", base+"Node", base+"context2");
		addSt(con, base+"node1", base+"implementedBy", base+"node2", base+"context1");
		
		addSt(con, base+"node3", rdf+"type", base+"Node", null);
		
		addSt(con, base+"slice1", rdf+"type", base+"Slice", base+"context1");
		addSt(con, base+"slice2", rdf+"type", base+"Slice", base+"context1");
		addSt(con, base+"interf1", rdf+"type", unit+"IPAddress", base+"context1");
		
		//should be one and only one null
		assertNull(LocalDbCalls.execStatementReturnRes(base,base, base));
		assertNull(LocalDbCalls.execStatementReturnRes(null, null, base));
		assertNull(LocalDbCalls.execStatementReturnRes(base, null, null));
		assertNull(LocalDbCalls.execStatementReturnRes(null, base, null));
		assertNull(LocalDbCalls.execStatementReturnRes(null, null, null));
		
		assertEquals(0, LocalDbCalls.execStatementReturnRes(null, base, base).size());
		assertEquals(0, LocalDbCalls.execStatementReturnRes(base, null, base).size());
		assertEquals(0, LocalDbCalls.execStatementReturnRes(base, base, null).size());
		
		//subject
		assertEquals(3, 
				LocalDbCalls.execStatementReturnRes(null, rdf+"type", base+"Node").size());
		
		//System.out.println(LocalDbCalls.execPrintStatement(null, "rdf:type", base+"Node", null));
		
		assertEquals(1, 
				LocalDbCalls.execStatementReturnRes(null, rdf+"type", base+"Node", base+"context1").size());
		assertTrue(LocalDbCalls.execStatementReturnRes(
				null, rdf+"type", base+"Node", base+"context1").contains(base+"node1"));
		
		Set<String> result =  LocalDbCalls.execStatementReturnRes(
				null, rdf+"type", base+"Node", base+"context1", base+"context2");
		
		assertEquals(2, result.size());
		assertTrue(result.contains(base+"node1"));
		assertTrue(result.contains(base+"node2"));
		assertFalse(result.contains(base+"node3"));
		
		
		//test abbreviation
		//subject
		assertEquals(3, 
				LocalDbCalls.execStatementReturnRes(null, "rdf:type", "im:Node").size());
		assertEquals(1, 
				LocalDbCalls.execStatementReturnRes(null, "rdf:type", "im:Node", "im:context1").size());
		
		assertEquals(1, 
				LocalDbCalls.execStatementReturnRes(null, "rdf:type", "unit:IPAddress", "im:context1").size());
		assertTrue(LocalDbCalls.execStatementReturnRes(
				null, "rdf:type", "im:Node", "im:context1").contains(base+"node1"));
		
		assertTrue(LocalDbCalls.execStatementReturnRes(
				null, "rdff:type", "im:Node", "im:context1", "im:context2").isEmpty());
		

		
		
		//object
		assertEquals(1, 
				LocalDbCalls.execStatementReturnRes(base+"node1", base+"implementedBy", null).size());
		assertTrue(LocalDbCalls.execStatementReturnRes(base+"node1", base+"implementedBy", null).
				contains(base+"node2"));
		assertEquals(1, LocalDbCalls.execStatementReturnRes(
				base+"node1", base+"implementedBy", null, base+"context1").size());

		assertEquals(0, LocalDbCalls.execStatementReturnRes(
				base+"node1", base+"implementedBy", null, base+"context2").size());
		
		//predicate
		assertTrue(LocalDbCalls.execStatementReturnRes(base+"node1", null, base+"node2", base+"context1").
				contains(base+"implementedBy"));
		
		ConnectionClass.closeAConnection(con);
		
		
	}
	
	private void addSt(ObjectConnection con, String sub, String pred, String obj, String cont)
	{
		try {
			if (cont != null)
				con.add(NoviUris.createURI(sub), NoviUris.createURI(pred), 
						NoviUris.createURI(obj), NoviUris.createURI(cont));
			else
				con.add(NoviUris.createURI(sub), NoviUris.createURI(pred), 
						NoviUris.createURI(obj));

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLifetimes()
	{

		Lifetime lifetime = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#lifetime1", Lifetime.class);
		lifetime.setStartTime(LocalDbCalls.getDate(0, -3, 0));
		lifetime.setEndTime(LocalDbCalls.getDate(0, 3, 0));
		assertFalse(LocalDbCalls.checkIfLifetimeIsNotValid(lifetime));
	
		Lifetime lifetime2 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#lifetime2", Lifetime.class);
		lifetime2.setStartTime(LocalDbCalls.getDate(0, -3, 0));
		lifetime2.setEndTime(LocalDbCalls.getDate(0, -1, 0));
		assertTrue(LocalDbCalls.checkIfLifetimeIsNotValid(lifetime2));
		
		//////
		Lifetime lifetime3 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#lifetime3", Lifetime.class);
		lifetime3.setStartTime(LocalDbCalls.getDate(0, -3, 0));
		lifetime3.setEndTime(LocalDbCalls.getDate(0, -1, 0));
		assertFalse(LocalDbCalls.checkIfLifetimeIsValid(lifetime3));
		
		Lifetime lifetime4 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#lifetime4", Lifetime.class);
		lifetime4.setStartTime(LocalDbCalls.getDate(0, 1, 0));
		lifetime4.setEndTime(LocalDbCalls.getDate(0, 3, 0));
		assertFalse(LocalDbCalls.checkIfLifetimeIsValid(lifetime4));
		
		Lifetime lifetime5 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#lifetime4", Lifetime.class);
		lifetime5.setStartTime(LocalDbCalls.getDate(0, -1, 0));
		lifetime5.setEndTime(LocalDbCalls.getDate(0, 1, 0));
		assertTrue(LocalDbCalls.checkIfLifetimeIsValid(lifetime5));
		assertFalse(LocalDbCalls.checkIfLifetimeIsValid(lifetime5, 0, 1, 1));
		assertFalse(LocalDbCalls.checkIfLifetimeIsValid(lifetime5, 0, 1, 0));
		assertFalse(LocalDbCalls.checkIfLifetimeIsValid(lifetime5, 0, 0, 1440));
		assertTrue(LocalDbCalls.checkIfLifetimeIsValid(lifetime5, 0, 0, 1439));
		
		lifetime4.setStartTime(LocalDbCalls.getDate(0, 0, 0));
		lifetime4.setEndTime(LocalDbCalls.getDate(0, 1, 0));
		assertTrue(LocalDbCalls.checkIfLifetimeIsValid(lifetime4));
		
	}


	@Test
	public void testGetLocalResource() throws RepositoryException
	{
		URI vNodeURI = NoviUris.createNoviURI("virtualNode1223213");
		//create a virtual node and store it
		ObjectConnection con = ConnectionClass.getNewConnection();
		//VirtualNode vNode = ConnectionClass.getObjectFactory().createObject(
		//		vNodeURI, VirtualNode.class);
		VirtualNode vNode = new VirtualNodeImpl(vNodeURI.toString());
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);
		
		vNode.setHardwareType("x86_32");
		vNode.setHasLifetimes(IMUtil.createSetWithOneValue((Lifetime) new LifetimeImpl("lifetime23")));
		con.addObject( vNode);
		//context 2
		con.setAddContexts(NoviUris.createNoviURI("context2"));
		con.addObject(new VirtualNodeImpl("virtualnode2"));
		
		//get virtual node
		Resource resource = LocalDbCalls.getLocalResource(vNodeURI.toString());
		assertEquals(vNodeURI.toString(), resource.toString());
		assertTrue((resource instanceof VirtualNode));
		VirtualNode getVnode = (VirtualNode) resource;
		assertEquals(getVnode.getHardwareType(), "x86_32");
		assertEquals(1, getVnode.getHasLifetimes().size());
		
		//get not exist resource
		assertNull(LocalDbCalls.getLocalResource(
				NoviUris.createNoviURI("not_esxist-resourcesss").toString()));
		
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);
		//test again
		URI vNodeURI2 = NoviUris.createNoviURI("virtualNode122321444");
		//create a virtual node and store it
		VirtualNode vNode2 = new VirtualNodeImpl(vNodeURI2.toString());
		vNode2.setHardwareType("x86_64");
		con.addObject(vNode2);
		
		//get virtual node
		Resource resource2 = LocalDbCalls.getLocalResource(vNodeURI2.toString());
		assertEquals(vNodeURI2.toString(), resource2.toString());
		assertTrue((resource2 instanceof VirtualNode));
		VirtualNode getVnode2 = (VirtualNode) resource2;
		assertEquals(getVnode2.getHardwareType(), "x86_64");
		
		ConnectionClass.closeAConnection(con);
		
		
	}
	
	@Test
	public void testGetRerource()
	{
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadOwlFileTestDB("PLETopology.owl", ManipulateDB.TESTBED_CONTEXTS);
		IRMCalls irm = new IRMCallsImpl();
		assertNotNull(irm.getResource("http://fp7-novi.eu/im.owl#PlanetLab_smilax1.man.poznan.pl"));
		
		//ManipulateDB.clearTribleStoreTestDB();
		//ManipulateDB.loadOwlFileTestDB("FEDERICATopology.owl", ManipulateDB.TESTBED_CONTEXTS);
		//assertNotNull(irm.getResource("http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.router1"));
		
	}
	
	//@Test
	public void testGetRerourceFederica()
	{

		
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadOwlFileTestDB("FEDERICATopology.owl", 
				ManipulateDB.getTestbedContextURI());
		IRMCalls irm = new IRMCallsImpl();
		assertNotNull(irm.getResource("http://fp7-novi.eu/im.owl#dfn.erl.router1.ge-0/1/0-in"));
		assertNotNull(irm.getResource("http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.router1"));
		
		
	}
	
	@Test
	public void testMemoRepoExe() throws RepositoryException
	{
		ConnectionClass.getConnection2MemoryRepos().clear();
		ConnectionClass.getConnection2MemoryRepos().addObject(new NodeImpl("nodeURI"));
		LocalDbCalls.execPrintStatementMemoryRepos(null, null, null);
		LocalDbCalls.showAllContentOfMemoryRepos();
		
	}

}
