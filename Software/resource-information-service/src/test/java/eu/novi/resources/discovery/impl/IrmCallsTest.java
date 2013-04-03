package eu.novi.resources.discovery.impl;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.FindLocalPartitioningCostTest;
import eu.novi.resources.discovery.database.ManipulateDB;
import eu.novi.resources.discovery.database.NoviUris;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.remote.serve.RemoteRisServeImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveResponse;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.Testbeds;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class IrmCallsTest {
	
	private static final transient Logger log = LoggerFactory.getLogger(IrmCallsTest.class);
	
	
	
	//TestbedCommunication mockTestbed;
	FederatedTestbed calls2TestbedFromRHMock;
	ReportEvent userFeedback;
	RemoteRisDiscoveryImpl remoteRis;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadOwlFileTestDB("PLEtopologyModified3.owl", ManipulateDB.TESTBED_CONTEXTS);
		ManipulateDB.loadOwlFileTestDB("FEDERICA_substrate.owl", ManipulateDB.TESTBED_CONTEXTS);
		
		PeriodicUpdate update = new PeriodicUpdate();
		update.setScheduler(Executors.newScheduledThreadPool(5));


	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Before
	public void setUp() throws Exception {
		log.debug("Initializing ReservationSliceTest...");

		// Setting up local environment
		log.debug("Setting up local environment...");
		///mockTestbed = mock(TestbedCommunication.class);

		//stubbing mockRIS
		log.debug("stubbing mockRIS...");
		RHCreateDeleteSliceResponseImpl mockCreateSliceResp = 
				mock(RHCreateDeleteSliceResponseImpl.class);
		when(mockCreateSliceResp.hasError()).thenReturn(false);
		when(mockCreateSliceResp.getSliceID()).thenReturn("sliceID1213");
		
		calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.createSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).
		thenReturn(mockCreateSliceResp);
		
		//user feedback
		userFeedback =  mock(ReportEvent.class);
		
		remoteRis = new RemoteRisDiscoveryImpl("PlanetLab");
		RemoteRisServeImpl remoteServe = new RemoteRisServeImpl();
		remoteServe.setTestbed("FEDERICA");
		List<RemoteRisServe> remoteRISList = new ArrayList<RemoteRisServe>();
		remoteRISList.add(remoteServe);
		remoteRis.setRemoteRISList(remoteRISList);
		
		
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testReservation()
	{
		IRMCallsImpl irmC = new IRMCallsImpl();
		irmC.setTestbed("PlanetLab");
		///irmC.setTestbedComm(mockTestbed);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		irmC.setUserFeedback(userFeedback);
		irmC.setRemoteRis(remoteRis);
		
		irmC.reserveSlice(null, testCreateTopology2Nodes(), 3333, new NOVIUserImpl("noviUser233"));
		assertNotNull(irmC.getSlice(NoviUris.createSliceURI("3333")));
		assertNull(irmC.getSlice(NoviUris.createSliceURI("2222")));
		


	}
	
	@Test
	public void testGetResource()
	{
		IRMCallsImpl irmC = new IRMCallsImpl();
		irmC.setTestbed("PlanetLab");
		///irmC.setTestbedComm(mockTestbed);
		irmC.setUserFeedback(userFeedback);
		irmC.setRemoteRis(remoteRis);
		
		assertNotNull(irmC.getResource("http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl"));
		assertNull(irmC.getResource("http://fp7-novi.eu/im.owl#smilax13.man.poznan.pl"));

	}
	
	@Test
	public void testGetResources()
	{
		IRMCallsImpl irmC = new IRMCallsImpl();
		irmC.setTestbed("PlanetLab");
		///irmC.setTestbedComm(mockTestbed);
		irmC.setUserFeedback(userFeedback);
		irmC.setRemoteRis(remoteRis);
		
		Set<String> uris = new HashSet<String>();
		uris.add("http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl");
		uris.add("http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr");
		assertEquals(2, irmC.getResources(uris).size());
		
		uris.add("http://fp7-novi.eu/im.owl#smilax13.man.poznan.pl");
		assertEquals(2, irmC.getResources(uris).size());


	}
	
	@Test
	public void testFindPartCost() throws RepositoryException, IOException {
		
		//set up the mocks for monitoring
		//we don't care here about the average utilization from monitoring here
		FindLocalPartitioningCostTest.mockMonitoringForUtilValues();
		
		IRMCallsImpl irmC = new IRMCallsImpl();
		irmC.setTestbed("PlanetLab");
		///irmC.setTestbedComm(mockTestbed);
		irmC.setUserFeedback(userFeedback);
		irmC.setRemoteRis(remoteRis);
		RemoteRisDiscoveryImpl.staticSetRemoteRISList(null);
		List<FPartCostTestbedResponseImpl>  answer = irmC.findPartitioningCost(null,
				FindLocalPartitioningCostTest.createTopology("i386"));


		assertEquals(1, answer.size());
	
		assertEquals(1, answer.get(0).getLinkCosts().size());
		assertEquals(1, answer.get(0).getNodeCosts().size());
		assertEquals(2, answer.get(0).getNodeCosts().get(0).getResourceURIs().size());
		assertEquals(3, answer.get(0).getNodeCosts().get(0).takeAvailResNumber());
		//assertEquals(2, answer.get(0).getNodeCosts().get(0).takeFedeResNumb());
		//assertEquals(0, answer.get(0).getLinkCosts().get(0).takeAvailResNumber());
		//test type
		assertEquals("i386", answer.get(0).getNodeCosts().get(0).getHardwType());
		assertEquals("2.35", answer.get(0).getLinkCosts().get(0).getHardwType());
		
		
		//set the monitoring service again to null
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(null);
	}
	



	//for 1st year review demo
	//not applicable with the current implementation
/*	@Test
	public void testFindResource() {
		
		IRMCalls irmCalls = new IRMCallsImpl();
		Topology topol = irmCalls.findResources(testCreateTopology2Nodes());
		Set<Resource> resources = topol.getContains();
		//only for the specific OWL file in DB and requesting Topology
		assertTrue(resources.size() == 2);
		//some printing
		log.debug("The list of the returned Nodes in the topology");
		for (Resource r : resources)
		{
			log.debug("Node : "+r.toString()+"\nHas Component: "+ ((Node)r).getHasComponent()+"\n");
			//just testing
			Set<NodeComponent> components = ((Node)r).getHasComponent();
		}
		
		//irmCalls.findResources(testCreateTopology2());
		//LocalDbCalls.showAllContentOfDB();
	
	}*/
	
	/**
	 * it creates a HashSet and it adds one value 
	 * @param t the added value
	 * @return the HashSet object included the value t
	 */
	public static <T> Set<T> createSetWithOneValue(T t)
	{
		Set<T> returnSet = new HashSet<T>() ;
		returnSet.add(t);
		return returnSet;
	}
	
	
	/**
	 * it create an topology with a single node for testing
	 * @return
	 */
	public static Topology testCreateTopology2()
	{
		// Another example of creating a Node, and setting their Hardware Type and components
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		Topology myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#Topology", Topology.class);
		
        Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node",
                Node.class);

        //set hardware type
        myNode.setHardwareType("i386");
        
        //////////add my node to topology ///////////

		myTopology.setContains(createSetWithOneValue(myNode));
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
		//////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#CPU", CPU.class);
		myCPU.setHasCPUSpeed(2f);
		//myCPU.setHasCores(createSetWithOneValue(BigInteger.valueOf(8)));
		//myCPU.setHasAvailableCores(createSetWithOneValue(BigInteger.valueOf(8)));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
        //Memory myMemory = factory.createObject("http://fp7-novi.eu/im.owl#Memory", Memory.class);
        //myMemory.setHasMemorySize(createSetWithOneValue(100f));
        //myMemory.setHasAvailableMemorySize(createSetWithOneValue(100f));
        //nodeComponents.add(myMemory);
        
        //////////Storage component///////////
        //Storage myStorage = factory.createObject("http://fp7-novi.eu/im.owl#Storage1", Storage.class);
        //myStorage.setHasStorageSize(createSetWithOneValue(1000f));
        //myStorage.setHasAvailableStorageSize(createSetWithOneValue(1000f));
        //nodeComponents.add(myStorage);
    	////////////END OF COMPONENTS/////////////
        myNode.setHasComponent(nodeComponents);
        
        
/*	        Set<NodeComponent> ncRetrieve =myNode.getHasComponent();
        Iterator<NodeComponent> iterNc = ncRetrieve.iterator();
        while(iterNc.hasNext()){
        	CPU curComponent = (CPU)iterNc.next();
        	System.out.println(curComponent);
        }
        System.out.println("==============================================================");*/
        
        return myTopology;
		
	}

	
	
	/**
	 * it create an topology with a single node for testing
	 * @return
	 */
	public static Topology testCreateTopology()
	{
		// Another example of creating a Node, and setting their Hardware Type and components
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		Topology myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#Topology", Topology.class);
		
        Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node",
                Node.class);

        //set hardware type
        myNode.setHardwareType("i386");
        
        //////////add my node to topology ///////////

		myTopology.setContains(createSetWithOneValue(myNode));
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
		//////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#CPU", CPU.class);
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(8));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
        Memory myMemory = factory.createObject("http://fp7-novi.eu/im.owl#Memory", Memory.class);
        myMemory.setHasMemorySize(100f);
        myMemory.setHasAvailableMemorySize(100f);
        nodeComponents.add(myMemory);
        
        //////////Storage component///////////
        Storage myStorage = factory.createObject("http://fp7-novi.eu/im.owl#Storage", Storage.class);
        myStorage.setHasStorageSize(1000f);
        myStorage.setHasAvailableStorageSize(1000f);
        nodeComponents.add(myStorage);
    	////////////END OF COMPONENTS/////////////
        myNode.setHasComponent(nodeComponents);
        
        
/*	        Set<NodeComponent> ncRetrieve =myNode.getHasComponent();
        Iterator<NodeComponent> iterNc = ncRetrieve.iterator();
        while(iterNc.hasNext()){
        	CPU curComponent = (CPU)iterNc.next();
        	System.out.println(curComponent);
        }
        System.out.println("==============================================================");*/
        
        return myTopology;
		
	}
	
	
	/**
	 * it create a topology with two nodes for testing
	 * @return
	 */
	public static Topology testCreateTopology2Nodes()
	{
		// Another example of creating a Node, and setting their Hardware Type and components
		Topology myTopology = new TopologyImpl("Topology");
		
        Node myNode = new NodeImpl("Node");
        
        Node myNode2 = new NodeImpl("Node2");

        //set hardware type
        myNode.setHardwareType("i386");
        
        //////////add my node to topology ///////////
        Set<Node> nodes = new HashSet<Node>();
        nodes.add(myNode);
        nodes.add(myNode2);

		myTopology.setContains(nodes);
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
		//////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = new CPUImpl("CPU");
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(8));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
        Memory myMemory = new MemoryImpl("Memory");
        myMemory.setHasMemorySize(100f);
        myMemory.setHasAvailableMemorySize(100f);
        nodeComponents.add(myMemory);
        
        //////////Storage component///////////
        Storage myStorage = new StorageImpl("Storage");
        myStorage.setHasStorageSize(1000f);
        myStorage.setHasAvailableStorageSize(1000f);
        nodeComponents.add(myStorage);
    	////////////END OF COMPONENTS/////////////
        myNode.setHasComponent(nodeComponents);
        
        
/*	        Set<NodeComponent> ncRetrieve =myNode.getHasComponent();
        Iterator<NodeComponent> iterNc = ncRetrieve.iterator();
        while(iterNc.hasNext()){
        	CPU curComponent = (CPU)iterNc.next();
        	System.out.println(curComponent);
        }
        System.out.println("==============================================================");*/
        
        //NODE 2///////
        


        //set hardware type
        myNode2.setHardwareType("i386");
        

		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
		//////add node components to the node /////////
        Set<NodeComponent> nodeComponents2 = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU2 =  new CPUImpl("CPU2");
		myCPU2.setHasCPUSpeed(2f);
		myCPU2.setHasCores(BigInteger.valueOf(8));
		myCPU2.setHasAvailableCores(BigInteger.valueOf(8));
        nodeComponents2.add(myCPU2);
        
        /////////Memory Component///////////
        Memory myMemory2 = new MemoryImpl("Memory2");
        myMemory2.setHasMemorySize(100f);
        myMemory2.setHasAvailableMemorySize(100f);
        nodeComponents2.add(myMemory2);
        
        //////////Storage component///////////
        Storage myStorage2 = new StorageImpl("Storage2");
        myStorage2.setHasStorageSize(1000f);
        myStorage2.setHasAvailableStorageSize(1000f);
        nodeComponents2.add(myStorage2);
    	////////////END OF COMPONENTS/////////////
        myNode2.setHasComponent(nodeComponents2);
        
        
        return myTopology;
		
	}
	
	
	
	@Test
	public void shouldThrowExceptionWhenNodeIsNull(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getNSwitchAddress(null, "slice");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when Node is null. ", nodeIsNull);
		}
	}

	
	@Test
	public void shouldThrowExceptionWhenSliceNameIsNull(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getNSwitchAddress(new NodeImpl(""), null );		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when slice name is null. ", nodeIsNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceNameIsEmpty(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getNSwitchAddress(new NodeImpl(""), "");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when slice name is empty ", nodeIsNull);
		}
	}
	
	@Test
	public void shouldReturnNSwtichFedericaAddressWhenNodeIsRouter(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		Node node = new NodeImpl("");
		node.setHardwareType(Node.HARDWARE_TYPE_ROUTER);
		String nswitchAddress = "194.132.52.217";
		
		// when
		String result = "";
		try {
			result = irmCalls.getNSwitchAddress(node, "slice1");		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e){
			
		}
		
		//then
		finally{
			assertEquals(nswitchAddress, result);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenNodeIsNullWhenCallsForNswitchEndpoint(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getPlanetlabPrivateAddressForNSwitchEndpoint(null, "slice");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when Node is null. ", nodeIsNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceNameIsEmptyWhenCallsForNswitchEndpoint(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getPlanetlabPrivateAddressForNSwitchEndpoint(new NodeImpl(""), "");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when slice name is empty ", nodeIsNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenHardwareTypeIsEmptyWhenCallsForNswitchEndpoint(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getPlanetlabPrivateAddressForNSwitchEndpoint(new NodeImpl(""), "slice");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when hardwareType is empty ", nodeIsNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenNodeIsRouterWhenCallsForNswitchEndpoint(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		Node node = new NodeImpl("");
		node.setHardwareType(Node.HARDWARE_TYPE_ROUTER);
		boolean nodeIsNull = false;
		
		// when
		try {
			irmCalls.getPlanetlabPrivateAddressForNSwitchEndpoint(new NodeImpl(""), "slice");		
		} catch (IllegalArgumentException e) {
			nodeIsNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("IRM should throw exception when Node is null. ", nodeIsNull);
		}
	}
	
	
	@Test
	public void shouldReturnSliverPrivateAddressOfNSwitchEndpoint(){
		//given 
		IRMCalls irmCalls = new IRMCallsImpl();
		Node node = new NodeImpl("");
		node.setHardwareType(Node.HARDWARE_TYPE_PLANETLAB_NODE);
		String nswitchAddress = "192.168.0.1";
		
		// when
		String result = "";
		try {
			result = irmCalls.getPlanetlabPrivateAddressForNSwitchEndpoint(node, "slice1");		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e){
			
		}
		
		//then
		finally{
			assertEquals(nswitchAddress, result);
		}
	}
	
	@Test
	public void testFindResourcesFEDERICA(){
		//ConnectionClass.startStorageService(true);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		Topology testTopology = testCreateTopology2Nodes();
		ReportEvent userFeedback =  mock(ReportEvent.class);
		irmCalls.setUserFeedback(userFeedback);
		irmCalls.setTestbed(Testbeds.FEDERICA);
		NOVIUserImpl user = new NOVIUserImpl("sfademo@barcelona.com");
		FRResponse resp = irmCalls.getSubstrateAvailability(null);
		assertTrue(resp !=null);
		assertFalse(resp.hasError());
		assertEquals(33, resp.getTopology().getContains().size());
		//ConnectionClass.stopStorageService();
	}
	
	
	@Test
	public void testFindResources(){
		//ConnectionClass.startStorageService(true);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		Topology testTopology = testCreateTopology2Nodes();
		ReportEvent userFeedback =  mock(ReportEvent.class);
		irmCalls.setUserFeedback(userFeedback);
		NOVIUserImpl user = new NOVIUserImpl("sfademo@barcelona.com");
		FRResponse resp = irmCalls.findResources(null, testTopology, user);
		assertTrue(resp !=null);
		//ConnectionClass.stopStorageService();
	}
	
	
	@Test
	public void testReserveSlice(){
		//ConnectionClass.startStorageService(true);
		
		ReportEvent userFeedback =  mock(ReportEvent.class);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		irmCalls.setUserFeedback(userFeedback);
		RemoteRisDiscoveryImpl.staticSetRemoteRISList(createRemoteList());
		//irmCalls.setRemoteRISList(createRemoteList());
		
		Topology testTopology = testCreateTopology2Nodes();
		
		TestbedCommunication tbc = mock(TestbedCommunication.class);
		RHCreateDeleteSliceResponseImpl response = new RHCreateDeleteSliceResponseImpl();
		response.setHasError(true);
		
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.createSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).
		thenReturn(response);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		//when(tbc.reserveSlice(any(NOVIUserImpl.class), any(String.class), any(Topology.class))).thenReturn(response);
		//irmCalls.setTestbedComm(tbc);
		ReserveResponse resp = irmCalls.reserveSlice(null, testTopology, 1234, null);
		assertTrue(resp != null);
		//ConnectionClass.stopStorageService();
	}
	
	
	@Test
	public void testReserveSliceNoError(){
	//	ConnectionClass.startStorageService(false);
		
		ReportEvent userFeedback =  mock(ReportEvent.class);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		irmCalls.setUserFeedback(userFeedback);
		
		//irmCalls.setRemoteRISList(createRemoteList());
		RemoteRisDiscoveryImpl.staticSetRemoteRISList(createRemoteList());
		
		Topology testTopology = testCreateTopology2Nodes();
		
		//TestbedCommunication tbc = mock(TestbedCommunication.class);
		RHCreateDeleteSliceResponseImpl response = new RHCreateDeleteSliceResponseImpl();
		response.setHasError(false);
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.createSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).
		thenReturn(response);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		
		//when(tbc.reserveSlice(any(NOVIUserImpl.class), any(String.class), any(Topology.class))).thenReturn(response);
		//irmCalls.setTestbedComm(tbc);
		ReserveResponse resp = irmCalls.reserveSlice(null, testTopology, 1234, new NOVIUserImpl("noviUser1234"));
		assertTrue(resp != null);
	//	ConnectionClass.stopStorageService();
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
	
	@Test
	public void testGetSomeSlice(){
		//ConnectionClass.startStorageService(false);
		
		ReportEvent userFeedback =  mock(ReportEvent.class);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		irmCalls.setUserFeedback(userFeedback);
		RemoteRisDiscoveryImpl.staticSetRemoteRISList(createRemoteList());
		//irmCalls.setRemoteRISList(createRemoteList());
		
		Reservation r = irmCalls.getSlice("http://fp7-novi.eu/NOSLICE");
		//ConnectionClass.stopStorageService();
	}

	@Test
	public void testGetNSwitchAddressNoHardware(){
		//ConnectionClass.startStorageService(false);
		
		ReportEvent userFeedback =  mock(ReportEvent.class);
		IRMCallsImpl irmCalls = new IRMCallsImpl();
		Node n = new NodeImpl("test");
		n.setHardwareType(null);
		irmCalls.getNSwitchAddress(n, "someSlice");
		
		//ConnectionClass.stopStorageService();
	}
	
	
	@Test
	public void testGetResourcesFedSubstr()
	{
		
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadTripleOWLFile("fed.owl", ManipulateDB.TESTBED_CONTEXTS);
		//LocalDbCalls.showAllContentOfDB();
		IRMCallsImpl irmC = new IRMCallsImpl();
		irmC.setTestbed("PlanetLab");
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		//irmC.setTestbedComm(mockTestbed);
		irmC.setUserFeedback(userFeedback);
		irmC.setRemoteRis(remoteRis);
		
		Set<String> uris = new HashSet<String>();
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-dfn.erl.router1.ge-0/1/0");
		uris.add("urn:publicid:IDN+federica.eu+link+dfn.erl.vserver1.vmnic7-dfn.erl.router1.ge-0/2/2");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/2/2-psnc.poz.vserver1.vmnic3");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/2-psnc.poz.vserver1.vmnic4");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.vserver1.vmnic4-psnc.poz.router1.ge-0/0/2");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/3/2-psnc.poz.vserver1.vmnic7");
		uris.add("urn:publicid:IDN+federica.eu+link+dfn.erl.router1.ge-0/0/3-dfn.erl.vserver1.vmnic3");
		uris.add("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/1/2-out");
		uris.add("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/0/0-in");
		uris.add("urn:publicid:IDN+federica.eu+interface+dfn.erl.vserver1.vmnic3-out");
		uris.add("http://fp7-novi.eu/im.owl#dfn.erl.vserver1.mem");
		assertEquals(11, irmC.getResources(uris).size());
		
		assertEquals(11, irmC.getResources(uris).size());
		
		assertEquals(11, irmC.getResources(uris).size());
		
		//maybe other test after this one need this
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadOwlFileTestDB("PLEtopologyModified3.owl", ManipulateDB.TESTBED_CONTEXTS);
		ManipulateDB.loadOwlFileTestDB("FEDERICA_substrate.owl", ManipulateDB.TESTBED_CONTEXTS);
		
		
		

	}
	
}
