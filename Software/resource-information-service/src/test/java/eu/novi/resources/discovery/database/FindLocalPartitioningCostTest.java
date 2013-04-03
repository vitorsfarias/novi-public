package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.Credential;
import eu.novi.monitoring.util.MonitoringQuery;
import eu.novi.resources.discovery.database.communic.MonitoringAvarInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.database.communic.MonitoringServCommunTest;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.remote.serve.RemoteRisServeImpl;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponse;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.util.NoviRisValues;
import eu.novi.resources.discovery.util.Testbeds;


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
public class FindLocalPartitioningCostTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("PLEtopologyModified3.2.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Test
	public void testFindPartCost() throws RepositoryException, IOException {
		
		//set up the mocks for monitoring
		//we don't care here about the average utilization from monitoring here
		mockMonitoringForUtilValues();


		FindLocalPartitioningCost findLPCost = new FindLocalPartitioningCost("PlanetLab");
		FPartCostTestbedResponseImpl response = findLPCost.findLocalPartitioningCost(createTopology("i386"));
		assertEquals(1, response.getLinkCosts().size());
		assertEquals(1, response.getNodeCosts().size());
		assertEquals(2, response.getNodeCosts().get(0).getResourceURIs().size());
		assertEquals(3, response.getNodeCosts().get(0).takeAvailResNumber());
		assertEquals(2, response.getNodeCosts().get(0).takeFedeResNumb());
		assertEquals(0, response.getLinkCosts().get(0).takeAvailResNumber());
		assertEquals(1, response.getLinkCosts().size());
		assertEquals(0, response.getLinkCosts().get(0).getAverUtil(), 0.01);
		//test type
		assertEquals("i386", response.getNodeCosts().get(0).getHardwType());
		assertEquals("2.35", response.getLinkCosts().get(0).getHardwType());
		
		
		//set the monitoring service again to null
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(null);
	}
	
	
	@Test
	public void testAvarUtilization() throws RepositoryException
	{
		FindLocalPartitioningCost findLPCost = new FindLocalPartitioningCost("PlanetLab");
		//test with one physical node
		FPartCostRecordImpl record = new FPartCostRecordImpl();
		//physical nodes
		Set<String> physNodes = new HashSet<String>();
		physNodes.add("physical node 1");
		record.setPhysicalNodeURIs(physNodes);
		//monitoring answer
		MonitoringAvarInfo monInfo1 = new MonitoringAvarInfo("physical node 1", 0.1f, 0.2f, 0.3f);
		Set<MonitoringAvarInfo> monAvarInfo = new HashSet<MonitoringAvarInfo>();
		monAvarInfo.add(monInfo1);
		
		assertEquals(0.20, findLPCost.calculateComputNodesAvarUtil(record, monAvarInfo), 0.01);
		
		//test with two physical node
		record = new FPartCostRecordImpl();
		//physical nodes
		physNodes = new HashSet<String>();
		physNodes.add("physicalNode1");
		physNodes.add("physicalNode2");
		physNodes.add("physicalNode3"); //Omit  the mon values for this one
		physNodes.add("physicalNode4"); //give not valid mon values for this one
		record.setPhysicalNodeURIs(physNodes);
		//monitoring answer
		monInfo1 = new MonitoringAvarInfo("physicalNode1", 0.2f, 0.3f, 0.4f);
		MonitoringAvarInfo monInfo2 = new MonitoringAvarInfo("physicalNode2", 0.5f, 0.6f, 0.7f);
		MonitoringAvarInfo monInfo3 = new MonitoringAvarInfo("physicalNode4", -1, 0.6f, 0.7f);//invalid
		monAvarInfo = new HashSet<MonitoringAvarInfo>();
		monAvarInfo.add(monInfo1);
		monAvarInfo.add(monInfo2);
		monAvarInfo.add(monInfo3);


		assertEquals(4, record.getPhysicalNodeURIs().size());
		//(0.35 + 0.45 + 0.55)/3 = 0.45
		assertEquals(0.45, findLPCost.calculateComputNodesAvarUtil(record, monAvarInfo), 0.01);
		assertEquals(2, record.getPhysicalNodeURIs().size());
		
		
		///AGGREGATION CALL///
		//the record has 2 nodes
		monInfo1 = new MonitoringAvarInfo("physical node 1", 0.15f, 0.26f, 0.37f);
		assertEquals(0.26, findLPCost.calculateComputNodesAvarAggregUtil(monInfo1), 0.01);
	}
	
	
	@Test
	public void testRoutersAvarUtilization() throws RepositoryException
	{
		FindLocalPartitioningCost findLPCost = new FindLocalPartitioningCost("PlanetLab");
		//test with one physical router
		FPartCostRecordImpl record = new FPartCostRecordImpl();
		//physical nodes
		Set<String> physNodes = new HashSet<String>();
		physNodes.add(NoviUris.createNoviURI("router1").toString());
		record.setPhysicalNodeURIs(physNodes);
		record.setHardwType(NoviRisValues.getRouterHardType());
		//store routers to db
		storeRouter("router1", 10, 4);
		
		assertEquals(0.60, findLPCost.setNodesAvarUtil(record), 0.01);
		assertEquals(1, record.getPhysicalNodeURIs().size());
		
		//more complex test
		physNodes.add(NoviUris.createNoviURI("router2").toString()); //valid
		physNodes.add(NoviUris.createNoviURI("router3").toString()); //stored but not valid
		physNodes.add(NoviUris.createNoviURI("router4").toString()); //not stored in DB
		record.setPhysicalNodeURIs(physNodes);
		
		//store it in DB
		storeRouter("router2", 11, 6);
		storeRouter("router3", null, 4); //invalid
		//I don't store the router 4
		
		assertEquals(0.52, findLPCost.setNodesAvarUtil(record), 0.01);
		assertEquals(0.52, record.getAverUtil(), 0.01);
		assertEquals(2, record.getPhysicalNodeURIs().size());
		assertTrue(record.getPhysicalNodeURIs().contains(NoviUris.createNoviURI("router2").toString()));
		assertTrue(record.getPhysicalNodeURIs().contains(NoviUris.createNoviURI("router1").toString()));
		
		
	}
	
	
	@Test
	public void testCanFederate() throws RepositoryException
	{
		FindLocalPartitioningCost findLPCost = new FindLocalPartitioningCost("PlanetLab");
		//test with one physical router
		FPartCostRecordImpl record = new FPartCostRecordImpl();
		//physical nodes
		storeNodeFederable("node1", "x86--", true);
		storeNodeFederable("node2", "x86--", true);
		storeNodeFederable("node3", "x86--", false);
		storeNodeFederable("node4", "x86-32", true);
		storeNodeFederable("node4", "x86-32", false);
	
		FPartCostTestbedResponseImpl response = findLPCost.findLocalPartitioningCost(createTopology("x86--"));
		assertEquals(1, response.getNodeCosts().size());
		assertEquals(2, response.getNodeCosts().get(0).takeFedeResNumb());
		assertEquals(2, response.getNodeCosts().get(0).getFederableResourceURIs().size());
		
		assertTrue(response.getNodeCosts().get(0).getFederableResourceURIs().
				contains(NoviUris.createNoviURI("node1").toString()));
		assertTrue(response.getNodeCosts().get(0).getFederableResourceURIs().
				contains(NoviUris.createNoviURI("node2").toString()));

		
		
	}
	
	@Test
	public void testRemoteFPCost() throws RepositoryException
	{
		//set up the mocks for monitoring
		//we don't care here about the average utilization from monitoring here
		mockMonitoringForUtilValues();

		RemoteRisDiscoveryImpl remoteRis = new RemoteRisDiscoveryImpl("PlanetLab");
		remoteRis.setRemoteRISList(createRemoteList());
		Vector<FPartCostTestbedResponseImpl> responses = 
				remoteRis.findRemotePartitioningCost(((TopologyImpl)createTopology("i386")));
		FPartCostTestbedResponseImpl response = responses.get(0);
		assertEquals(1, response.getLinkCosts().size());
		assertEquals(1, response.getNodeCosts().size());
		assertEquals(2, response.getNodeCosts().get(0).getResourceURIs().size());
		assertEquals(3, response.getNodeCosts().get(0).takeAvailResNumber());
		assertEquals(0, response.getLinkCosts().get(0).takeAvailResNumber());

		assertEquals("i386", response.getNodeCosts().get(0).getHardwType());

		//set the monitoring service again to null
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(null);
	}
	
	public final static void mockMonitoringForUtilValues()
	{
		//set up the mocks for monitoring
		MonSrv monServ = mock(MonSrv.class);
		try {
			when(monServ.substrate(any(Credential.class), any(String.class))).
			thenReturn(MonitoringServCommunTest.readFile("src/test/resources/monitoringUtil.json"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MonitoringQuery quer = mock(MonitoringQuery.class);
		when(monServ.createQuery()).thenReturn(quer);
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(monServ);

	}
	
	
	private List<RemoteRisServe> createRemoteList() {
		RemoteRisServe plRIS = new RemoteRisServeImpl();
		plRIS.setTestbed(Testbeds.PLANETLAB);
		RemoteRisServe fdRIS = new RemoteRisServeImpl();
		fdRIS.setTestbed(Testbeds.FEDERICA);
		
		List<RemoteRisServe> remotes = new ArrayList<RemoteRisServe>();
		remotes.add(plRIS); 
		remotes.add(fdRIS);
		return remotes;
	}
	
	
	
	/**two virtual node and one link
	 * @return
	 * @throws RepositoryException
	 */
	public static Topology createTopology(String hwType) throws RepositoryException
	{
		// Another example of creating a Node, and setting their Hardware Type and components

		Topology myTopology = new TopologyImpl("topology123");
		
		
        VirtualNode myVNode = new VirtualNodeImpl("virtualNode12");

        VirtualNode vNode2 = new VirtualNodeImpl("virtualNode123");

        Link myLink = new LinkImpl("link324");
        
        Set<Resource> resources = new HashSet<Resource>();
        resources.add(myVNode);
        resources.add(vNode2);
        resources.add(myLink);
        myTopology.setContains(resources);
        //set hardware type
        myVNode.setHardwareType(hwType);
        vNode2.setHardwareType(hwType);
        myLink.setHasCapacity(2.35f);

        return myTopology;
		
	}
	
	protected static void storeRouter(String name, Integer totalLogRout, Integer availLogRouter)
	{
		Node node = new NodeImpl(name);

		node.setHardwareType(NoviRisValues.getRouterHardType());
		node.setHasLogicalRouters(totalLogRout);
		node.setHasAvailableLogicalRouters(availLogRouter);
		
		UserFeedbackTest.storeNodeDB(node);
		
		
	}
	
	protected static void storeNodeFederable(String name, String hwType, boolean federable)
	{
		Node node = new NodeImpl(name);

		node.setHardwareType(hwType);
		Interface interf = new InterfaceImpl(name + "-interface-in");

		interf.setCanFederate(federable);
		node.setHasInboundInterfaces(IMUtil.createSetWithOneValue(interf));
		
		UserFeedbackTest.storeNodeDB(node);
		
		
	}

}
