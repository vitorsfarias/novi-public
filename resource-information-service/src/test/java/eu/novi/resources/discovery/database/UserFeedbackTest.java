package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;

public class UserFeedbackTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		//ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);


	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Before
	public void setUp() throws Exception {
		ManipulateDB.clearTripleStore();
		
		
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		//             cores, speed, mem, sto
		storeNode("node1", 2, 2, 4, 100);
		storeNode("node2", 4, 3, 2, 200);
		storeNode("node3", 4, 5, 2, 200);
		storeNode("node4", 5, 5, 3, 150);

		//create a node with null values
		String name = "nullValuesNode";
		Node node = new NodeImpl(name);
		node.setHardwareType("x86");
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		//CPU
		CPU myCPU = new CPUImpl(name + "-cpu");
		nodeComponents.add(myCPU);
		//memory
		Memory mem = new MemoryImpl(name + "-mem");
		nodeComponents.add(mem);
		//storage
		Storage sto = new StorageImpl(name+ "-sto");
		nodeComponents.add(sto);
		node.setHasComponent(nodeComponents);
		storeNodeDB(node);

		Set<String> uris = new HashSet<String>();
		uris.add(NoviUris.createNoviURI("node1").toString());
		uris.add(NoviUris.createNoviURI("node2").toString());
		uris.add(NoviUris.createNoviURI("node3").toString());
		uris.add(NoviUris.createNoviURI("node4").toString());
		uris.add(NoviUris.createNoviURI(name).toString());

		UserFeedback feedback = new UserFeedback();
		System.out.println(feedback.createFeedback(uris, createVirtualNode("vNode", 4, 3, 3, 160)));

	}
	
	@Test
	public void test2() {
		//             cores, speed, mem, sto
		storeNode("node1", 1, 1, 1, 100);
		storeNode("node2", 2, 2, 2, 200);
		storeNode("node3", 4, 4, 1.9f, 400);
		storeNode("node4", 5, 5, 5, 300);
		
		
		
		Set<String> uris = new HashSet<String>();
		uris.add(NoviUris.createNoviURI("node1").toString());
		uris.add(NoviUris.createNoviURI("node2").toString());
		uris.add(NoviUris.createNoviURI("node3").toString());
		uris.add(NoviUris.createNoviURI("node4").toString());
		
		
		UserFeedback feedback = new UserFeedback();
		System.out.println(feedback.createFeedback(uris, createVirtualNode("vNode", 2, 2, 2, 400)));
		
	}
	
	
	
	/**create and store a physical node
	 * @param name
	 * @param cpuCore
	 * @param cpuSpeed
	 * @param freeMem
	 * @param freeSto
	 */
	protected static void storeNode(String name, int cpuCore, float cpuSpeed, float freeMem, float freeSto)
	{
		Node node = new NodeImpl(name);

		node.setHardwareType("x86");
		
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		////////CPU//////////////////
		CPU myCPU = new CPUImpl(name + "-cpu");
		myCPU.setHasCPUSpeed(cpuSpeed);
		//myCPU.setHasCores(BigInteger.valueOf(cpuCore));
		myCPU.setHasAvailableCores(BigInteger.valueOf(cpuCore));
		nodeComponents.add(myCPU);
		
		//memory
		Memory mem = new MemoryImpl(name + "-mem");
		mem.setHasAvailableMemorySize(freeMem);
		nodeComponents.add(mem);
		//storage
		Storage sto = new StorageImpl(name+ "-sto");
		sto.setHasAvailableStorageSize(freeSto);
		nodeComponents.add(sto);
		
		node.setHasComponent(nodeComponents);
		
		storeNodeDB(node);
		
		
	}
	
	
	/**store a node to the DB
	 * @param node
	 */
	protected static void storeNodeDB(Node node)
	{
		//store the node
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(ManipulateDB.getTestbedContextURI());
		try {
			con.addObject(node);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionClass.closeAConnection(con);
	}
	
	
	
	/**create a virtual node with the fiven characteristics
	 * @param name
	 * @param cpuCore
	 * @param cpuSpeed
	 * @param freeMem
	 * @param freeSto
	 * @return
	 */
	public static VirtualNode createVirtualNode(String name, int cpuCore, float cpuSpeed, float freeMem, float freeSto)
	{
		VirtualNode node = new VirtualNodeImpl(name);

		node.setHardwareType("x86");
		
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		////////CPU//////////////////
		CPU myCPU = new CPUImpl(name + "-cpu");
		myCPU.setHasCPUSpeed(cpuSpeed);
		//myCPU.setHasCores(BigInteger.valueOf(cpuCore));
		myCPU.setHasCores(BigInteger.valueOf(cpuCore));
		nodeComponents.add(myCPU);
		
		//memory
		Memory mem = new MemoryImpl(name + "-mem");
		mem.setHasMemorySize(freeMem);
		nodeComponents.add(mem);
		//storage
		Storage sto = new StorageImpl(name+ "-sto");
		sto.setHasStorageSize(freeSto);
		nodeComponents.add(sto);
		
		node.setHasComponent(nodeComponents);
		return node;
	
	}
	


}
