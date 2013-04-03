package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.Credential;
import eu.novi.monitoring.util.MonitoringQuery;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.resources.discovery.database.communic.MonitoringInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.NoviRisValues;
import eu.novi.resources.discovery.util.RisSystemVariables;


public class UpdateAvailabilityTest {
	private static final transient Logger log = 
			LoggerFactory.getLogger(UpdateAvailabilityTest.class);

	static URI s1 ;
	static String s2;
	static String s3 ;
	static String r1 ;
	static String r2 ;
	static String r3 ;
	static String nodInv; 
	static String offline; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		//store the nodes
		s1 = NoviUris.createNoviURI("testNodeForMonitoring2343");
		s2 = NoviUris.createNoviURI("testNodeForMonitoring2376").toString();
		s3 = NoviUris.createNoviURI("testNodeForMonitoring2385").toString();
		r1 = NoviUris.createNoviURI("router1").toString();
		r2 = NoviUris.createNoviURI("router2").toString();
		r3 = NoviUris.createNoviURI("router3").toString();
		nodInv = NoviUris.createNoviURI("node2-missingValues").toString();
		offline = NoviUris.createNoviURI("offline-node").toString();

		Node node = createNode(s1.toString());
		Node node2 = createNode(s2.toString());
		Node node3 = createNode(s3.toString());
		Node router1 = createNode(r1.toString());
		Node router2 = createNode(r2.toString());
		Node router3 = createNode(r3.toString());
		router1.setHardwareType(NoviRisValues.getRouterHardType());
		router2.setHardwareType(NoviRisValues.getRouterHardType());
		router3.setHardwareType(NoviRisValues.getRouterHardType());
		Node nodeInvalid = createNode(nodInv);
		Node nodeOffline = createNode(offline);
		VirtualNode vNode = new VirtualNodeImpl("vNode"); //virtual node, should not be considered
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts( ManipulateDB.getTestbedContextURI());
		try {
			con.addObject(node);
			con.addObject(node2);
			con.addObject(node3);
			con.addObject(router1);
			con.addObject(router2);
			con.addObject(router3);
			con.addObject(nodeInvalid);
			con.addObject(nodeOffline);
			con.addObject(vNode);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		ConnectionClass.closeAConnection(con);
		
		PeriodicUpdate update = new PeriodicUpdate();
		update.setScheduler(Executors.newScheduledThreadPool(5));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}




	@Test
	public void listResources() {

		//ManipulateDB.clearTripleStore();


		///CREATE THE mon values//////
		Set<MonitoringInfo> monitoring = new HashSet<MonitoringInfo>();
		MonitoringInfo mon = new MonitoringInfo(s1.toString(), 2, 2.1f, 30.5f, 4.5f);
		MonitoringInfo mon2 = new MonitoringInfo(s2.toString(), 1, 2.4f, 30.5f, 6.5f);
		MonitoringInfo mon3 = new MonitoringInfo(s3.toString(), 2, 4.1f, 35.5f, 8.5f);
		MonitoringInfo monInvalid = new MonitoringInfo(nodInv, -1, 2.1f, 30.5f, 4.5f);
		monitoring.add(mon);
		monitoring.add(mon2);
		monitoring.add(mon3);
		monitoring.add(monInvalid);

		//LocalDbCalls.showAllContentOfDB();


		UpdateAvailability update = new UpdateAvailability();
		//set the monitoring data
		update.setMonInfo(monitoring);
		Set<Node> answer = update.listResources();

		assertEquals(6, answer.size());
		assertTrue(answer.toString().contains(s1.toString()));
		assertTrue(answer.toString().contains(s2.toString()));
		assertTrue(answer.toString().contains(s3.toString()));
		assertTrue(answer.toString().contains(r1.toString()));
		assertTrue(answer.toString().contains(r2.toString()));
		assertTrue(answer.toString().contains(r3.toString()));
		//check that they have interfaces
		for (Node n : answer)
		{
			assertNotNull(n.getHasInboundInterfaces());
			assertNotNull(n.getHasOutboundInterfaces());

			//just for one check the component values
			if(n.toString().equals(s1.toString()))
			{
				Set<NodeComponent> nodComp = n.getHasComponent();
				assertEquals(3, nodComp.size());
				for (NodeComponent comp: nodComp)
				{
					if (comp instanceof CPU)
					{
						log.debug("cpu component");
						assertEquals(2, ((CPU)comp).getHasAvailableCores().intValue());
					} else if (comp instanceof Memory)
					{
						log.debug("Memory component");
						assertEquals(4.5, ((Memory)comp).getHasAvailableMemorySize(),0.1);
					} else if (comp instanceof Storage)
					{
						log.debug("Storage component");
						assertEquals(30.5, ((Storage)comp).getHasAvailableStorageSize(),0.1);
					}
				}

			}

		}

		///UPDATE THE COMPONENTS with -1////
		/*log.debug("UPDATE THE COMPONENTS with -1");


		mon = new MonitoringInfo(s.toString(), -1, -1f, -1f, -1f);
		monitoring = new HashSet<MonitoringInfo>();
		monitoring.add(mon);*/

	}

	@Test
	public void listResourcesUser() {

		//ManipulateDB.clearTripleStore();


		///CREATE THE mon values//////
		Set<MonitoringInfo> monitoring = new HashSet<MonitoringInfo>();
		MonitoringInfo mon = new MonitoringInfo(s1.toString(), 2, 2.1f, 30.5f, 4.5f);
		//MonitoringInfo mon2 = new MonitoringInfo(s2.toString(), 1, 2.4f, 30.5f, 6.5f);
		MonitoringInfo mon3 = new MonitoringInfo(s3.toString(), 2, 4.1f, 35.5f, 8.5f);
		//MonitoringInfo monInvalid = new MonitoringInfo(nodInv, -1, 2.1f, 30.5f, 4.5f);
		monitoring.add(mon);
		//monitoring.add(mon2);
		monitoring.add(mon3);
		//monitoring.add(monInvalid);

		///mock policy answer
		InterfaceForRIS policyMock = mock(InterfaceForRIS.class);
		//PolicyServCommun policyMock = mock(PolicyServCommun.class);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		map.put(s1.toString(), true);
		map.put(s2, false);
		map.put(s3, true);
		map.put(r1, false);
		map.put(r2, true);
		//miss value for r3
		//miss value for the node nodInv
		when(policyMock.AuthorizedForResource(any(String.class), any(NOVIUserImpl.class), any(Set.class), 
				any(Integer.class))).thenReturn(map);

		PolicyServCommun.setPolicyServiceCallsStatic(policyMock);


		UpdateAvailability update = new UpdateAvailability();
		//set the monitoring data
		update.setMonInfo(monitoring);
		NOVIUser user = new NOVIUserImpl("noviUSer13");
		Set<Node> answer = update.listResources(user);

		assertEquals(3, answer.size());
		assertTrue(answer.toString().contains(s1.toString()));
		//assertTrue(answer.toString().contains(s2.toString()));
		assertTrue(answer.toString().contains(s3.toString()));
		assertTrue(answer.toString().contains(r2.toString()));
		//check that they have interfaces
		for (Node n : answer)
		{
			assertNotNull(n.getHasInboundInterfaces());
			assertNotNull(n.getHasOutboundInterfaces());

			//just for one check the component values
			if(n.toString().equals(s1.toString()))
			{
				Set<NodeComponent> nodComp = n.getHasComponent();
				assertEquals(3, nodComp.size());
				for (NodeComponent comp: nodComp)
				{
					if (comp instanceof CPU)
					{
						log.debug("cpu component");
						assertEquals(2, ((CPU)comp).getHasAvailableCores().intValue());
					} else if (comp instanceof Memory)
					{
						log.debug("Memory component");
						assertEquals(4.5, ((Memory)comp).getHasAvailableMemorySize(),0.1);
					} else if (comp instanceof Storage)
					{
						log.debug("Storage component");
						assertEquals(30.5, ((Storage)comp).getHasAvailableStorageSize(),0.1);
					}
				}

			}

		}



	}
	
	
	@Test
	public void updateAllMonValuesTest() {

		RisSystemVariables.setUpdateMonValuesPeriodic(true);
		///CREATE THE mon values//////
		Set<MonitoringInfo> monitoring = new HashSet<MonitoringInfo>();
		MonitoringInfo mon = new MonitoringInfo(s1.toString(), 3, 4.2f, 52.5f, 3.7f);
		MonitoringInfo mon2 = new MonitoringInfo(s2.toString(), 2, 2.8f, 38.5f, 6.5f);
		MonitoringInfo mon3 = new MonitoringInfo(s3.toString(), 3, 4.1f, 35.5f, 8.5f);
		MonitoringInfo monInvalid = new MonitoringInfo(nodInv, -1, 3.1f, 36.5f, 3.5f);
		MonitoringInfo monOffl = new MonitoringInfo(offline, -1, -1, -1, -1);
		monitoring.add(mon);
		monitoring.add(mon2);
		monitoring.add(mon3);
		monitoring.add(monInvalid);
		monitoring.add(monOffl);
		
		UpdateAvailability update = new UpdateAvailability();
		
		//set the monitoring data
		update.setMonInfo(monitoring);

		//update the values
		update.updateAllMonitoringValues();

		
		Set<Node> answer = update.listResources();
		RisSystemVariables.setUpdateMonValuesPeriodic(false);

		//System.out.println(answer.toString());
		assertEquals(7, answer.size()); //the nodInv is included also
		assertTrue(answer.toString().contains(s1.toString()));
		assertTrue(answer.toString().contains(s2.toString()));
		assertTrue(answer.toString().contains(s3.toString()));
		assertTrue(answer.toString().contains(nodInv.toString()));
		assertTrue(answer.toString().contains(r1.toString()));
		assertTrue(answer.toString().contains(r2.toString()));
		assertTrue(answer.toString().contains(r3.toString()));
		//check that they have interfaces
		for (Node n : answer)
		{
			assertNotNull(n.getHasInboundInterfaces());
			assertNotNull(n.getHasOutboundInterfaces());

			//just for one check the component values
			if(n.toString().equals(s1.toString()))
			{
				Set<NodeComponent> nodComp = n.getHasComponent();
				assertEquals(3, nodComp.size());
				for (NodeComponent comp: nodComp)
				{
					if (comp instanceof CPU)
					{
						log.debug("cpu component");
						assertEquals(3, ((CPU)comp).getHasAvailableCores().intValue());
					} else if (comp instanceof Memory)
					{
						log.debug("Memory component");
						assertEquals(3.7, ((Memory)comp).getHasAvailableMemorySize(),0.1);
					} else if (comp instanceof Storage)
					{
						log.debug("Storage component");
						assertEquals(52.5, ((Storage)comp).getHasAvailableStorageSize(),0.1);
					}
				}

			}
			
			if(n.toString().equals(nodInv.toString()))
			{
				Set<NodeComponent> nodComp = n.getHasComponent();
				assertEquals(3, nodComp.size());
				for (NodeComponent comp: nodComp)
				{
					if (comp instanceof CPU)
					{
						log.debug("cpu component");
						assertEquals(0, ((CPU)comp).getHasAvailableCores().intValue());
					} else if (comp instanceof Memory)
					{
						log.debug("Memory component");
						assertEquals(3.5, ((Memory)comp).getHasAvailableMemorySize(),0.1);
					} else if (comp instanceof Storage)
					{
						log.debug("Storage component");
						assertEquals(36.5, ((Storage)comp).getHasAvailableStorageSize(),0.1);
					}
				}

			}

		}

		///UPDATE THE COMPONENTS with -1////
		/*log.debug("UPDATE THE COMPONENTS with -1");


		mon = new MonitoringInfo(s.toString(), -1, -1f, -1f, -1f);
		monitoring = new HashSet<MonitoringInfo>();
		monitoring.add(mon);*/

	}


	/**create a node with an inound and outbound interface
	 * @param uri
	 * @return
	 */
	private static Node createNode(String uri)
	{
		Node node = new NodeImpl(uri);
		Interface intIn = new InterfaceImpl(node.toString() + "interface-in");
		Interface intOut = new InterfaceImpl(node.toString() + "interface-out");
		node.setHasInboundInterfaces(IMUtil.createSetWithOneValue(intIn));
		node.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(intOut));


		return node;

	}

}
