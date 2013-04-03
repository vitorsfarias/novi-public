package eu.novi.resources.discovery.database;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Link;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeComponentImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ConstructFindResQueryTest {


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML");
		//LocalDbCalls.showAllContentOfDB();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	//@Test
	public void test() {
		ConstructFindResQuery query = new ConstructFindResQuery(6);
		query.printQuery(true);
		query.printQuery(true);
		query.setRdfType(6, "Node");
		query.printQuery(true);
		query.setRdfType(7, "Node");

	}

	@Test
	public void test2() {
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		VirtualNode myVNode = factory.createObject("http://fp7-novi.eu/im.owl#virtualNode12",
				VirtualNode.class);
		//ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#node321", node);


		//set hardware type
		myVNode.setHardwareType("i386");

		ConstructFindResQuery query = new ConstructFindResQuery(1);
		query.setRdfType(1, "Node");
		query.setFunctionalChar(1, myVNode);
		query.setExceptNode(1, "http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr");

		query.finalizeQuery();
		assertEquals(2, query.execQueryPrintResults());

		//test case insensitive
		myVNode = new VirtualNodeImpl("vNode");
		myVNode.setHardwareType("I386");
		query = new ConstructFindResQuery(1);
		query.setRdfType(1, "Node");
		query.setFunctionalChar(1, myVNode);
		query.setExceptNode(1, "http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr");

		query.finalizeQuery();
		assertEquals(2, query.execQueryPrintResults());


	}

	@Test
	public void testAskQuery() {

		URI context = NoviUris.createNoviURI("substrate");
		ConstructFindResQuery query = new ConstructFindResQuery("substrate");
		query.setCheck4OfflineNode(NoviUris.createNoviURI("node+=/#").toString());
		query.finalizeQuery();
		assertFalse(query.execAskQuery());
		
		storeNodeZeroValues("node+=/#", context, 0, 0);
		assertTrue(query.execAskQuery());
		
		storeNodeZeroValues("node+=/#2", context, 1, 0);
		query = new ConstructFindResQuery("substrate");
		query.setCheck4OfflineNode(NoviUris.createNoviURI("node+=/#2").toString());
		query.finalizeQuery();
		assertFalse(query.execAskQuery());
		
		//give the full context uri
		query = new ConstructFindResQuery(context.toString());
		query.setCheck4OfflineNode(NoviUris.createNoviURI("node+=/#").toString());
		query.finalizeQuery();
		assertTrue(query.execAskQuery());




	}

	@Test
	public void testIllegalActions()
	{

		//LocalDbCalls.showAllContentOfDB();
		ConstructFindResQuery query = new ConstructFindResQuery(0);
		String s = query.getQuery();
		query.setRdfType(2, "Node");
		assertEquals(s, query.toString());
		query.setBoundConstrain(2, "nodeURI");
		assertEquals(s, query.toString());

		Link link = new LinkImpl("linkURI");
		query.setFunctionalCharLink(1, link);
		assertEquals(s, query.toString());

	}



	@Test
	public void testNodeComponentNormalMode()
	{
		ConstructFindResQuery query = new ConstructFindResQuery(0);
		String s = query.getQuery();
		query.setNodeComponent(2, new NodeComponentImpl("nodeURI"), false);
		assertEquals(s, query.toString());
		query.setNodeComponent(1, new StorageImpl("nodeURI"), false);
		assertFalse(s.equals(query.toString()));

		query.setNodeComponent(1, new MemoryImpl("memURI"), false);

		//normal mode
		Vector<String> st = query.createCpuComponent(1, new CPUImpl("cpuURI"), true);
		assertEquals(2, st.size());

		ConstructFindResQuery qu = new ConstructFindResQuery(0);
		qu.setRdfType(1, "Node");
		CPU cpu = new CPUImpl("cpu2URI");
		cpu.setHasCores(BigInteger.valueOf(4));
		cpu.setHasAvailableCores(BigInteger.valueOf(4));
		qu.setNodeComponent(1, cpu, true);
		qu.finalizeQuery();
		assertEquals(2, qu.execQueryPrintResults());

		//memory
		qu = new ConstructFindResQuery(0);
		qu.setRdfType(1, "Node");
		Memory mem = new MemoryImpl("mem2URI");
		mem.setHasMemorySize(8.0f);
		mem.setHasAvailableMemorySize(8.0f);
		qu.setNodeComponent(1, mem, true);
		qu.finalizeQuery();
		assertEquals(2, qu.execQueryPrintResults());

		//storage
		qu = new ConstructFindResQuery(0);
		qu.setRdfType(1, "Node");
		Storage sto = new StorageImpl("sto2URI");
		sto.setHasStorageSize(2048.0f);
		sto.setHasAvailableStorageSize(2048.0f);
		qu.setNodeComponent(1, sto, true);
		qu.finalizeQuery();
		assertEquals(1, qu.execQueryPrintResults());




	}

	//@Test
	public void testNodeComponentNotNormalMode()
	{
		ConstructFindResQuery query = new ConstructFindResQuery(1);
		//VirtualNode vNo = new VirtualNodeImpl("aVirtualNode");
		CPU cpu = new CPUImpl("myCpu");
		cpu.setHasCores(BigInteger.valueOf(11));
		cpu.setHasCPUSpeed(2.2f);
		Memory mem = new MemoryImpl("myMemory");

		Set<NodeComponent> comps = new HashSet<NodeComponent>();
		comps.add(cpu);
		comps.add(mem);
		//vNo.setHasComponent(comps);

		query.setRdfType(1, "Node");
		query.setNodeComponents(1, comps);
		query.finalizeQuery();
		query.printQuery(true);


	}


	protected void storeNodeZeroValues(String nodeUri, URI context, float mem, float sto)
	{
		Node node = new NodeImpl(nodeUri);


		///

		//////add node components to the node /////////
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		////////CPU//////////////////
		CPU myCPU = new CPUImpl(nodeUri + "cpu1");
		//myCPU.setHasCPUSpeed(0f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(0));
		nodeComponents.add(myCPU);

		/////////Memory Component///////////
		Memory myMemory = new MemoryImpl(nodeUri + "Memory1");
		myMemory.setHasMemorySize(100f);
		myMemory.setHasAvailableMemorySize(mem);
		nodeComponents.add(myMemory);
		
		Storage mySto = new StorageImpl(nodeUri + "Storage");
		mySto.setHasAvailableStorageSize(sto);
		nodeComponents.add(mySto);


		////////////END OF COMPONENTS/////////////
		node.setHasComponent(nodeComponents);
		
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(context);
		try {
			con.addObject(node);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConnectionClass.closeAConnection(con);
	


	}



}
