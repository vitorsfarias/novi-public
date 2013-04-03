package eu.novi.resources.discovery.database;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.resources.discovery.database.communic.MonitoringInfo;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.util.RisSystemVariables;

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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FindLocalResourcesTest {
	
	private static final transient Logger log = 
		LoggerFactory.getLogger(FindLocalResourcesTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);
		
		ManipulateDB.loadOWLFile("PLEtopologyModified3_v1.owl", "RDFXML",
				NoviUris.createNoviURI("secondContext"));

		ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML",
				NoviUris.createNoviURI("thirdContext"));
		//LocalDbCalls.showAllContentOfDB();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}
	
	
	@Test
	public void testBoundRequest()
	{
		
		Topology topo = new TopologyImpl("boundTopo");
		VirtualNode vNode1 = new VirtualNodeImpl("vNode1");
		CPU cpu1 = new CPUImpl("cpu1");
		cpu1.setHasCores(BigInteger.valueOf(2)); //in monitoring is set to 4
		vNode1.setHasComponent(IMUtil.createSetWithOneValue(cpu1));
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(
				new NodeImpl("smilax2.man.poznan.pl")));
		
		VirtualNode vNode2 = new VirtualNodeImpl("vNode2");
		CPU cpu2 = new CPUImpl("cpu2");
		cpu2.setHasCores(BigInteger.valueOf(2)); //netmode is set to 2 in monitoring
		vNode2.setHasComponent(IMUtil.createSetWithOneValue(cpu2));
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(
				new NodeImpl("planetlab1-novi.lab.netmode.ece.ntua.gr")));
		
		Set<Resource> res = new HashSet<Resource>();
		res.add(vNode1);
		res.add(vNode2);
		topo.setContains(res);
		NOVIUser user = new NOVIUserImpl("noviUser7");
		//TODO mock policy service
		PolicyServCommun.setPolicyServiceCallsStatic(null);
		FindLocalResources findResources = new FindLocalResources();
		FRResponse response = findResources.findLocalResources(topo, user);
		assertNotNull(response.getTopology().getContains());
		assertFalse(response.hasError());
		assertEquals(2, findResources.getNumberOfReqResources());
		assertTrue(response.getTopology().getContains().toString().
				contains("planetlab1-novi.lab.netmode.ece.ntua.gr"));
		assertTrue(response.getTopology().getContains().toString().
				contains("smilax2.man.poznan.pl"));
		
	}
	
	@Test
	public void testUnBoundRequest()
	{//it uses the hardcoded values for monitoring 
		//create topology
		Topology topo = new TopologyImpl("unboundTopo");
		VirtualNode vNode1 = new VirtualNodeImpl("vNode1");
		//CPU cpu1 = new CPUImpl("cpu1");
		//cpu1.setHasCores(BigInteger.valueOf(4)); //in monitoring is set to 4
	//	cpu1.setHasCPUSpeed(2.0f);
		//Memory mem1 = new MemoryImpl("mem1");
		//mem1.setHasMemorySize(8.0f);
		//Set<NodeComponent> comp1 = new HashSet<NodeComponent>();
		//comp1.add(cpu1);
		//comp1.add(mem1);
		//vNode1.setHasComponent(comp1);

		
		VirtualNode vNode2 = new VirtualNodeImpl("vNode2");
		// CPU cpu2 = new CPUImpl("cpu2");
		// cpu2.setHasCores(BigInteger.valueOf(4));
		// cpu2.setHasCPUSpeed(2.5f);
		// Memory mem2 = new MemoryImpl("mem2");
		// mem2.setHasMemorySize(4.0f);
		// Storage sto2 = new StorageImpl("sto2");
		// sto2.setHasStorageSize(1000.0f);
		// Set<NodeComponent> comp2 = new HashSet<NodeComponent>();
		// comp2.add(cpu2);
		// comp2.add(mem2);
		// comp2.add(sto2);
		// vNode2.setHasComponent(comp2);
		
		Set<Resource> res = new HashSet<Resource>();
		res.add(vNode1);
		res.add(vNode2);
		topo.setContains(res);
		
		NOVIUser user = new NOVIUserImpl("noviUser7");
		
		//TODO mock policy service
		PolicyServCommun.setPolicyServiceCallsStatic(null);
		FindLocalResources findResources = new FindLocalResources();
		FRResponse response = findResources.findLocalResources(topo, user);
		assertNotNull(response.getTopology().getContains());
		assertFalse(response.hasError());
		assertEquals(2, findResources.getNumberOfReqResources());
		System.out.println(response.getTopology().getContains().toString());
		assertEquals(4, response.getTopology().getContains().size());
		assertTrue(response.getTopology().getContains().toString().
				contains("planetlab2-novi.lab.netmode.ece.ntua.gr"));
		 assertTrue(response.getTopology().getContains().toString().
				 contains("smilax2.man.poznan.pl"));
		 

		 ///////////////call again but with out monitoring call/////////////////////////
		 RisSystemVariables.setUpdateMonValuesPeriodic(true);

		 CPU cpu1 = new CPUImpl("cpu1");
		 cpu1.setHasCores(BigInteger.valueOf(2)); //in monitoring is set to 4
		 cpu1.setHasCPUSpeed(4.0f);
		 Memory mem1 = new MemoryImpl("mem1");
		 mem1.setHasMemorySize(4.0f);
		 Set<NodeComponent> comp1 = new HashSet<NodeComponent>();
		 comp1.add(cpu1);
		 comp1.add(mem1);
		 vNode1.setHasComponent(comp1);



		 CPU cpu2 = new CPUImpl("cpu2");
		 cpu2.setHasCores(BigInteger.valueOf(6));
		 cpu2.setHasCPUSpeed(2f);
		 Memory mem2 = new MemoryImpl("mem2");
		 mem2.setHasMemorySize(8.0f);
		 Storage sto2 = new StorageImpl("sto2");
		 sto2.setHasStorageSize(2000.0f);
		 Set<NodeComponent> comp2 = new HashSet<NodeComponent>();
		 comp2.add(cpu2);
		 comp2.add(mem2);
		 comp2.add(sto2);
		 vNode2.setHasComponent(comp2);

		 findResources = new FindLocalResources();
		 response = findResources.findLocalResources(topo, user);
		 RisSystemVariables.setUpdateMonValuesPeriodic(false);
		 
		 
		 assertNotNull(response.getTopology().getContains());
		 assertFalse(response.hasError());
		 assertEquals(2, findResources.getNumberOfReqResources());
		 System.out.println(response.getTopology().getContains().toString());
		 assertEquals(2, response.getTopology().getContains().size());
		 assertTrue(response.getTopology().getContains().toString().
				 contains("planetlab1-novi.lab.netmode.ece.ntua.gr"));
		 assertTrue(response.getTopology().getContains().toString().
				 contains("smilax1.man.poznan.pl"));



		
	}
	
	@Test
	public void testUnBoundRequestFeedback()
	{
		//create topology
		Topology topo = new TopologyImpl("unboundTopo");


		//find one available machine
		VirtualNode vNode = UserFeedbackTest.createVirtualNode("vNode", 2, 2, 4, 1000);
		
		Set<Resource> res = new HashSet<Resource>();
		res.add(vNode);
		topo.setContains(res);
		
		NOVIUser user = new NOVIUserImpl("noviUser7");
		
		PolicyServCommun.setPolicyServiceCallsStatic(null);
		FindLocalResources findResources = new FindLocalResources();
		FRResponse response = findResources.findLocalResources(topo, user);
		assertTrue(response.getUserFeedback().equals(""));
		assertNotNull(response.getTopology().getContains());
		assertFalse(response.hasError());
		assertEquals(1, findResources.getNumberOfReqResources());
		System.out.println(response.getTopology().getContains().toString());
		assertEquals(1, response.getTopology().getContains().size());
		assertTrue(response.getTopology().getContains().toString().
				contains("smilax1.man.poznan.pl"));
		
		//no available machines
		vNode = UserFeedbackTest.createVirtualNode("vNode", 2, 4, 4, 1000);
		
		res = new HashSet<Resource>();
		res.add(vNode);
		topo.setContains(res);
		
		

		FindLocalResources findResources2 = new FindLocalResources();
		response = findResources2.findLocalResources(topo, user);
		assertFalse(response.getUserFeedback().equals(""));
		System.out.println(response.getUserFeedback());
		assertNotNull(response.getTopology().getContains());
		assertTrue(response.hasError());
		assertEquals(1, findResources2.getNumberOfReqResources());
		assertEquals(0, response.getTopology().getContains().size());

		
	}

	
	@Test
	public void testPolicyAndBoundReq() throws RepositoryException {
		InterfaceForRIS policyMock = mock(InterfaceForRIS.class);
		//PolicyServCommun policyMock = mock(PolicyServCommun.class);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		map.put("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr",
				true);
		map.put("http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl",
				false);
		when(policyMock.AuthorizedForResource(any(String.class), any(NOVIUserImpl.class), any(Set.class), 
				any(Integer.class))).thenReturn(map);
		
		NOVIUser user = new NOVIUserImpl("noviUSer13");
		FindLocalResources findResources = new FindLocalResources();
		PolicyServCommun.setPolicyServiceCallsStatic(policyMock);
		//findResources.setPolicyService(policyMock);
		FRResponse responce = findResources.findLocalResources(createTopology(), user);
		//System.out.println(responce.getTopology().getContains().toString());
		assertEquals(1, responce.getTopology().getContains().size());
		assertFalse(responce.hasError());
		assertEquals(1, findResources.getNumberOfReqResources());
		assertTrue(responce.getTopology().getContains().toString().
				contains("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr"));
		//log.debug(responce.getTopology().getContains().toString());
		///////////////////////

		/*Collection<Set<Resource>> list = responce.getSucceedResources().values();
		Iterator it = list.iterator();
		while (it.hasNext())
		{
			Set<Resource> resources = (Set<Resource>) it.next();
			for (Resource r : resources)
			{
				log.debug("node {}",r);
				Node node = (Node) r;
				IRMLocalDbCalls.execPrintStatement( 
						LocalDbCalls.createURI(node.toString()), null, null); //for testing
				node.setHostnames(null);
				IRMLocalDbCalls.execPrintStatement( 
						LocalDbCalls.createURI(node.toString()), null, null); //for testing
			}
		}*/
		
	
	}
	
	
	@Test
	public void testFindResUpdate() throws RepositoryException
	{
		FindLocalResources findResources = new FindLocalResources();
		FRResponse responce = findResources.findLocalResourcesUpdate(createUpdTopology(),
				IMUtil.createSetWithOneValue("http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl"));
		PolicyServCommun.setPolicyServiceCallsStatic(null);
		assertFalse(responce.hasError());
		assertEquals(2, responce.getTopology().getContains().size());
		assertEquals(0, findResources.getNumberOfReqResources());
		// assertFalse(responce.getTopology().getContains().toString().
				// contains("http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl"));
		assertFalse(responce.getTopology().getContains().toString().
				contains("http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr"));
		
		
		responce = findResources.findLocalResourcesUpdate(new ReservationImpl("a topology"),
				IMUtil.createSetWithOneValue("http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl"));
		assertTrue(responce.hasError());
		
	}
	
	
	@Test
	public void testUpdateMonitCacheNoCompon() throws RepositoryException, QueryEvaluationException
	{
		FindLocalResources findResources = new FindLocalResources();
		URI s = NoviUris.createNoviURI("testNodeForMonitoring");
		Node node = new NodeImpl(s.toString());
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts( ManipulateDB.getTestbedContextURI());
		con.addObject(node);
		
		
		Set<MonitoringInfo> monitoring = new HashSet<MonitoringInfo>();
		MonitoringInfo mon = new MonitoringInfo(s.toString(), 2, 2.0f, 30, 4);
		monitoring.add(mon);
		
		MonitoringInfo mon2 = new MonitoringInfo(
				NoviUris.createNoviURI("not_exist").toString(), 2, 2, 3, 4);
		monitoring.add(mon2);
		
		//update the values
		//findResources.startConnection();
		UpdateAvailability.updateMonitoringValues(monitoring);
		//findResources.stopConnection();
		
		
		Node nod = con.getObject(Node.class, s.toString());
		assertEquals(3, nod.getHasComponent().size());
		
		ConnectionClass.closeAConnection(con);
		
		
		
		
	}
	
	
	
	@Test
	public void testUpdateMonitCacheCreateComp()
	{
		//ManipulateDB.clearTripleStore();
		FindLocalResources findResources = new FindLocalResources();
		findResources.startConnection();
		URI s = NoviUris.createNoviURI("testNodeForMonitoring2343");
		Node node = new NodeImpl(s.toString());
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts( ManipulateDB.getTestbedContextURI());
		try {
			con.addObject(node);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionClass.closeAConnection(con);
		///CREATE THE COMPONENTS//////
		Set<MonitoringInfo> monitoring = new HashSet<MonitoringInfo>();
		MonitoringInfo mon = new MonitoringInfo(s.toString(), 2, 2.1f, 30.5f, 4.5f);
		monitoring.add(mon);
		
		//LocalDbCalls.showAllContentOfDB();
		//Log.debug("////////////////");
		UpdateAvailability.updateMonitoringValues(monitoring);
		findResources.stopConnection();
		//LocalDbCalls.showAllContentOfDB();
		
		con = ConnectionClass.getNewConnection();
		log.debug("GETTING THE NODE {}", s.toString());
		Node nod=null;
		try {
			nod = con.getObject(Node.class, s.toString());
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<NodeComponent> nodeComp = nod.getHasComponent();
		log.debug("I GOT IT");
		assertEquals(3, nodeComp.size());
		for (NodeComponent comp: nodeComp)
		{
			log.debug("Checking {}", comp.toString());
			if (comp instanceof CPU)
			{
				log.debug("It is CPU");
				CPU cpu = (CPU)comp;
				log.debug("BEFORE ASSERT");
				assertEquals(2, cpu.getHasAvailableCores().intValue());
				assertEquals(2.1f, ((CPU)comp).getHasCPUSpeed(), 0.1);
			}
			else if (comp instanceof Memory)
			{
				assertEquals(4.5f, ((Memory)comp).getHasAvailableMemorySize(), 0.1);
			} 
			else
			{
				assertEquals(30.5f, ((Storage)comp).getHasAvailableStorageSize(), 0.1);
			}
		}
		
		ConnectionClass.closeAConnection(con);
		
		///UPDATE THE COMPONENTS////
		log.debug("UPDATE THE COMPONENTS");
		
		
		mon = new MonitoringInfo(s.toString(), 3, 1.5f, 35.6f, 3.6f);
		monitoring = new HashSet<MonitoringInfo>();
		monitoring.add(mon);
		
		//LocalDbCalls.showAllContentOfDB();
		//Log.debug("////////////////");
		//findResources.startConnection();
		UpdateAvailability.updateMonitoringValues(monitoring);
		//findResources.stopConnection();
		//LocalDbCalls.showAllContentOfDB();
		

		con = ConnectionClass.getNewConnection();
		log.debug("GETTING THE NODE {}", s.toString());
		nod=null;
		try {
			nod = con.getObject(Node.class, s.toString());
		} catch (RepositoryException e) {
	
			e.printStackTrace();
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		}
		nodeComp = nod.getHasComponent();
		log.debug("I GOT IT");
		assertEquals(3, nodeComp.size());
		for (NodeComponent comp: nodeComp)
		{
			log.debug("Checking {}", comp.toString());
			if (comp instanceof CPU)
			{
				log.debug("It is CPU");
				CPU cpu = (CPU)comp;
				log.debug("BEFORE ASSERT");
				assertEquals(3, cpu.getHasAvailableCores().intValue());
				assertEquals(1.5f, ((CPU)comp).getHasCPUSpeed(), 0.1);
			}
			else if (comp instanceof Memory)
			{
				assertEquals(3.6f, ((Memory)comp).getHasAvailableMemorySize(), 0.1);
			} 
			else
			{
				assertEquals(35.6f, ((Storage)comp).getHasAvailableStorageSize(), 0.1);
			}
		}

		ConnectionClass.closeAConnection(con);
		///UPDATE THE COMPONENTS with -1////
		log.debug("UPDATE THE COMPONENTS with -1");


		mon = new MonitoringInfo(s.toString(), -1, -1f, -1f, -1f);
		monitoring = new HashSet<MonitoringInfo>();
		monitoring.add(mon);

		//LocalDbCalls.showAllContentOfDB();
		//Log.debug("////////////////");
		//findResources.startConnection();
		UpdateAvailability.updateMonitoringValues(monitoring);
		//findResources.stopConnection();
		LocalDbCalls.showAllContentOfDB();


		con = ConnectionClass.getNewConnection();
		log.debug("GETTING THE NODE {}", s.toString());
		nod=null;
		try {
			nod = con.getObject(Node.class, s.toString());
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		nodeComp = nod.getHasComponent();
		log.debug("I GOT IT");
		assertEquals(3, nodeComp.size());
		for (NodeComponent comp: nodeComp)
		{
			log.debug("Checking {}", comp.toString());
			if (comp instanceof CPU)
			{
				log.debug("It is CPU");
				CPU cpu = (CPU)comp;
				assertEquals(0, cpu.getHasAvailableCores().intValue());
				assertEquals(0f, ((CPU)comp).getHasCPUSpeed(), 0.1);
			}
			else if (comp instanceof Memory)
			{
				assertEquals(0f, ((Memory)comp).getHasAvailableMemorySize(), 0.1);
			} 
			else
			{
				assertEquals(0f, ((Storage)comp).getHasAvailableStorageSize(), 0.1);
			}
		}

		log.debug("THIS IS THE END");
		ConnectionClass.closeAConnection(con);

	}
	
	
	//@Test
	public void testQuery() throws RepositoryException
	{

		System.out.println("\n/////////////test query$$$$$$$$$$$$$$$$");
		
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		VirtualNode myVNode = factory.createObject("http://fp7-novi.eu/im.owl#virtualNode12",
        		VirtualNode.class);
        
        Node node = factory.createObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr",
        		Node.class);
        ConnectionClass.getConnection2MemoryRepos().addObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr", node);
        

        //set hardware type
        myVNode.setHardwareType("i386");

        myVNode.setImplementedBy(IMUtil.createSetWithOneValue(node));

		ConstructFindResQuery query = new ConstructFindResQuery(1);
		query.setRdfType(1, "Node");
		query.setFunctionalChar(1, myVNode);
		query.setBoundConstrain(1, node.toString());
		query.finalizeQuery();
		query.execQueryPrintResults();
		
	}
	
	//@Test
	public void testGetWrongObject()
	{
		Node node = new NodeImpl("anode");
		ObjectConnection con = ConnectionClass.getNewConnection();
		try {
			con.addObject(node);
			log.debug("before 1");
			assertNotNull(con.getObject(Node.class, NoviUris.createNoviURI("anode")));
			log.debug("before 2");
			assertNotNull(con.getObject(NoviUris.createNoviURI("anode")));
			log.debug("before 3");
			assertNull(con.getObject(NoviUris.createNoviURI("notexistasdfasdfasfasfasdf333")));
			log.debug("before 4");
			assertNull(con.getObject(Node.class, NoviUris.createNoviURI("notexist333")));

		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		log.debug("before close");
		ConnectionClass.closeAConnection(con);


	}

	
	/**
	 * it create a bound topology with a single node for testing
	 * @return
	 * @throws RepositoryException 
	 */
	private static Topology createTopology() throws RepositoryException
	{
		// Another example of creating a Node, and setting their Hardware Type and components
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		Topology myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#topology123", Topology.class);
		
		
        VirtualNode myVNode = factory.createObject("http://fp7-novi.eu/im.owl#virtualNode12",
        		VirtualNode.class);
        myTopology.setContains(IMUtil.createSetWithOneValue(myVNode));
        
        Node node = factory.createObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr",
        		Node.class);
        ConnectionClass.getConnection2MemoryRepos().addObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr", node);
        ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#virtualNode12", myVNode);
        

        //set hardware type
        myVNode.setHardwareType("i386");
        //myVNode.setImplementedBy(IMUtil.createSetWithOneValue(node)); //TODO it has error here
        
        //////////add my node to topology ///////////

		
		
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
	    //////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#cpu1", CPU.class);
        ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#cpu1", myCPU);
		//myCPU.setHasCPUSpeed(IMUtil.createSetWithOneValue(2.3f));
		//myCPU.setHasCores(IMUtil.createSetWithOneValue(BigInteger.valueOf(8)));
		myCPU.setHasCores(BigInteger.valueOf(4));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
        Memory myMemory = factory.createObject("http://fp7-novi.eu/im.owl#Memory1", Memory.class);
        ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#Memory1", myMemory);
        //myMemory.setHasMemorySize(IMUtil.createSetWithOneValue(100f));
        myMemory.setHasMemorySize(4f);
        nodeComponents.add(myMemory);
        
        /////////storage///////////
        Storage storage = factory.createObject("http://fp7-novi.eu/im.owl#storage1", Storage.class);
        ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#storage1", storage);
        //myMemory.setHasMemorySize(IMUtil.createSetWithOneValue(100f));
        storage.setHasStorageSize(999f);
        nodeComponents.add(storage);
        

    	////////////END OF COMPONENTS/////////////

        
        myVNode.setHasComponent(nodeComponents);
        
        
        
        
        return myTopology;
		
	}
	
	
	
	
	/**
	 * it create a topology with a single node for testing
	 * @return
	 * @throws RepositoryException 
	 */
	private static Reservation createUpdTopology() throws RepositoryException
	{
		// Another example of creating a reservation with two virtual nodes implement by
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		Reservation myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#reservation123", Reservation.class);
		
		Set<VirtualNode> resources = new HashSet<VirtualNode>();
        VirtualNode myVNode = factory.createObject("http://fp7-novi.eu/im.owl#virtualNode122",
        		VirtualNode.class);
        resources.add(myVNode);
        VirtualNode myVNode2 = factory.createObject("http://fp7-novi.eu/im.owl#virtualNode123",
        		VirtualNode.class);
        resources.add(myVNode2);
        myTopology.setContains(resources);
        
        Node node1 = factory.createObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr",
        		Node.class);
        ConnectionClass.getConnection2MemoryRepos().addObject(
        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr", node1);
        
        Node node2 = factory.createObject(
        		"http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl",
        		Node.class);
        ConnectionClass.getConnection2MemoryRepos().addObject(
        		"http://fp7-novi.eu/im.owl#smilax2.man.poznan.pl", node2);

        //set hardware type
        myVNode.setHardwareType("i386");
        myVNode.setImplementedBy(IMUtil.createSetWithOneValue(node1)); 
        
        myVNode2.setImplementedBy(IMUtil.createSetWithOneValue(node2)); 
        
        //////////add my node to topology ///////////

		
		
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
	    //////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#cpu1", CPU.class);
        ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#cpu1", myCPU);
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(2));
		//myCPU.setHasAvailableCores(BigInteger.valueOf(8));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
     /*   Memory myMemory = factory.createObject("http://fp7-novi.eu/im.owl#Memory1", Memory.class);
        myMemory.setHasMemorySize(IMUtil.createSetWithOneValue(100f));
        myMemory.setHasAvailableMemorySize(IMUtil.createSetWithOneValue(100f));
        nodeComponents.add(myMemory);*/

    	////////////END OF COMPONENTS/////////////

        
        myVNode2.setHasComponent(nodeComponents);
        
        
        
        
        return myTopology;
		
	}

}
