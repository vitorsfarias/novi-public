package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryTest;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.Testbeds;

public class LockResourcesTest {
	
	String phMachName = "physNode-novipl:novi";
	String base = "http://fp7-novi.eu/im.owl#";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		Testbeds.setIsSharedTestbed(false);
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
	public void testLockSession() {
		Testbeds testbe = new Testbeds();
		testbe.setTestbed(Testbeds.PLANETLAB);
		
		LockResources lockRes = new LockResources();
		LockSession session = lockRes.createLockSession(createTopology(4, 2), 1111);
		assertEquals(1111, session.getSessionID().intValue());
		
		assertEquals(2, session.getLocalTestebed().getRouters().size());
		assertTrue(session.getLocalTestebed().getRouters().contains(
				base + phMachName+"2"));
		assertTrue(session.getLocalTestebed().getRouters().contains(
				base + phMachName+"3"));
		
		assertEquals(2, session.getLocalTestebed().getServers().size());
		//System.out.println(session.getLocalTestebed().getServers().toString());
		List<String> serv = new Vector<String>();
		serv.add(session.getLocalTestebed().getServers().get(0).getServerURI());
		serv.add(session.getLocalTestebed().getServers().get(1).getServerURI());
		assertTrue(serv.contains(base + phMachName+"0"));
		assertTrue(serv.contains(base + phMachName+"1"));
		
		assertEquals(0, session.getRemoteTestbed().getRouters().size());
		assertEquals(0, session.getRemoteTestbed().getServers().size());


		testbe.setTestbed(Testbeds.FEDERICA);


		session = lockRes.createLockSession(createTopology(4, 2), 1112);
		assertEquals(1112, session.getSessionID().intValue());

		assertEquals(0, session.getLocalTestebed().getRouters().size());
		assertEquals(0, session.getLocalTestebed().getServers().size());

		assertEquals(2, session.getRemoteTestbed().getRouters().size());
		assertEquals(2, session.getRemoteTestbed().getServers().size());

	}
	
	
	@Test
	public void testLocalLockUnlock() throws RepositoryException, QueryEvaluationException {
		ManipulateDB.clearTripleStore();
		Testbeds testbe = new Testbeds();
		testbe.setTestbed(Testbeds.PLANETLAB);
		storePhyMachines(4, 2);
		
		//LOCK
		LockResources lockRes = new LockResources();
		ObjectConnection con = ConnectionClass.getNewConnection();
		
		LockSession session = lockRes.startLockResources(createTopology(4, 2), 1111);
		for (int i = 0; i<4; i++)
		{
			if (i<2)
			{//servers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				Set<NodeComponent> comps = nod.getHasComponent();
				int j =0;
				for (NodeComponent cmp : comps)
				{
					if (cmp instanceof Memory)
					{
						assertEquals(4f, ((Memory) cmp).getHasAvailableMemorySize(), 0.01);
						j++;
					}
					else if (cmp instanceof Storage)
					{
						assertEquals(5f, ((Storage) cmp).getHasAvailableStorageSize(), 0.01);
						j++;
					}
					else if (cmp instanceof CPU)
					{
						assertEquals(2, ((CPU) cmp).getHasAvailableCores().intValue());
						j++;
					}
				}

				assertEquals(3, j);

			}
			else
			{//routers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				assertEquals(5+i-1, nod.getHasAvailableLogicalRouters().intValue());

			}


		}

		//UNLOCK/////////////////////////////////////////////////////////////////////////////
		lockRes.startUnlockResources(session);
		for (int i = 0; i<4; i++)
		{
			if (i<2)
			{//servers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				Set<NodeComponent> comps = nod.getHasComponent();
				int j =0;
				for (NodeComponent cmp : comps)
				{
					if (cmp instanceof Memory)
					{
						assertEquals(4  + 1.5 + i, ((Memory) cmp).getHasAvailableMemorySize(), 0.01);
						j++;
					}
					else if (cmp instanceof Storage)
					{
						assertEquals(5 + 2 + i, ((Storage) cmp).getHasAvailableStorageSize(), 0.01);
						j++;
					}
					else if (cmp instanceof CPU)
					{
						assertEquals(2 + 1 + i, ((CPU) cmp).getHasAvailableCores().intValue());
						j++;
					}
				}

				assertEquals(3, j);

			}
			else
			{//routers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				assertEquals(5+i, nod.getHasAvailableLogicalRouters().intValue());

			}


		}

		
		ConnectionClass.closeAConnection(con);
	}
	
	
	@Test
	public void testLFalseActions() throws RepositoryException, QueryEvaluationException {
		ManipulateDB.clearTripleStore();
		Testbeds testbe = new Testbeds();
		testbe.setTestbed(Testbeds.PLANETLAB);
		storePhyMachinesNoComp(3, 2);
		
		
		//LOCK
		LockResources lockRes = new LockResources();
		ObjectConnection con = ConnectionClass.getNewConnection();
		
		LockSession session = lockRes.startLockResources(createTopology(5, 2), 1111);
		assertNotNull(session);
		

		ConnectionClass.closeAConnection(con);
	}
	
	
	
	
	
	@Test
	public void testRemoteLockUnlock() throws RepositoryException, QueryEvaluationException {
		//initialize
		ManipulateDB.clearTripleStore();
		PeriodicUpdate sched = new PeriodicUpdate();
		sched.setScheduler(Executors.newScheduledThreadPool(4));
		Testbeds testbe = new Testbeds();
		testbe.setTestbed(Testbeds.FEDERICA);
		RemoteRisDiscoveryImpl.staticSetRemoteRISList(RemoteRisDiscoveryTest.createRemoteList());
		
		
		storePhyMachines(4, 2);
		
		//LOCK
		LockResources lockRes = new LockResources();
		ObjectConnection con = ConnectionClass.getNewConnection();
		
		LockSession session = lockRes.startLockResources(createTopology(4, 2), 1111);
		for (int i = 0; i<4; i++)
		{
			if (i<2)
			{//servers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				Set<NodeComponent> comps = nod.getHasComponent();
				int j =0;
				for (NodeComponent cmp : comps)
				{
					if (cmp instanceof Memory)
					{
						assertEquals(4f, ((Memory) cmp).getHasAvailableMemorySize(), 0.01);
						j++;
					}
					else if (cmp instanceof Storage)
					{
						assertEquals(5f, ((Storage) cmp).getHasAvailableStorageSize(), 0.01);
						j++;
					}
					else if (cmp instanceof CPU)
					{
						assertEquals(2, ((CPU) cmp).getHasAvailableCores().intValue());
						j++;
					}
				}

				assertEquals(3, j);

			}
			else
			{//routers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				assertEquals(5+i-1, nod.getHasAvailableLogicalRouters().intValue());

			}


		}

		//UNLOCK/////////////////////////////////////////////////////////////////////////////
		lockRes.startUnlockResources(session);
		for (int i = 0; i<4; i++)
		{
			if (i<2)
			{//servers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				Set<NodeComponent> comps = nod.getHasComponent();
				int j =0;
				for (NodeComponent cmp : comps)
				{
					if (cmp instanceof Memory)
					{
						assertEquals(4  + 1.5 + i, ((Memory) cmp).getHasAvailableMemorySize(), 0.01);
						j++;
					}
					else if (cmp instanceof Storage)
					{
						assertEquals(5 + 2 + i, ((Storage) cmp).getHasAvailableStorageSize(), 0.01);
						j++;
					}
					else if (cmp instanceof CPU)
					{
						assertEquals(2 + 1 + i, ((CPU) cmp).getHasAvailableCores().intValue());
						j++;
					}
				}

				assertEquals(3, j);

			}
			else
			{//routers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				assertEquals(5+i, nod.getHasAvailableLogicalRouters().intValue());

			}


		}

		
		ConnectionClass.closeAConnection(con);
	}
	
	
	@Test
	public void testMultiThreadLocalLock() throws RepositoryException, QueryEvaluationException, InterruptedException, ExecutionException {
		ManipulateDB.clearTripleStore();
		Testbeds testbe = new Testbeds();
		testbe.setTestbed(Testbeds.PLANETLAB);
		PeriodicUpdate sched = new PeriodicUpdate();
		sched.setScheduler(Executors.newScheduledThreadPool(4));
		
		
		storePhyMachines(4, 2);
		
		final LockResources lockRes = new LockResources();
		final LockResources lockRes2 = new LockResources();
		ObjectConnection con = ConnectionClass.getNewConnection();
		//create two threads
		Future<Boolean>  threadAns = PeriodicUpdate.executeNewThread(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				lockRes2.startLockResources(createTopology(4, 2), 1111);
				return true;
			}
		});
		
		
		lockRes.startLockResources(createTopology(4, 2), 1112);
		threadAns.get(); //wait the thread to finish
		
		for (int i = 0; i<4; i++)
		{
			if (i<2)
			{//servers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				Set<NodeComponent> comps = nod.getHasComponent();
				int j =0;
				for (NodeComponent cmp : comps)
				{
					if (cmp instanceof Memory)
					{
						assertEquals(2.5f-i, ((Memory) cmp).getHasAvailableMemorySize(), 0.01);
						j++;
					}
					else if (cmp instanceof Storage)
					{
						assertEquals(3f-i, ((Storage) cmp).getHasAvailableStorageSize(), 0.01);
						j++;
					}
					else if (cmp instanceof CPU)
					{
						assertEquals(1 - i, ((CPU) cmp).getHasAvailableCores().intValue());
						j++;
					}
				}

				assertEquals(3, j);

			}
			else
			{//routers
				Node nod = con.getObject(Node.class, base + phMachName + String.valueOf(i));
				assertEquals(5+i-2, nod.getHasAvailableLogicalRouters().intValue());

			}


		}

		ConnectionClass.closeAConnection(con);
	}
	
	
	
	
	public Topology createTopology(int siz, int serv)
	{
		if (serv > siz)
		{
			System.out.println("The number of the server should be equal or less the total size");
			return null;
		}
		
		Topology top = new TopologyImpl("myTopology");
		Set<Resource> contain = new HashSet<Resource>();
		
		
		Node[] nodes = new Node[siz];
		for (int i=0; i<siz; i++)
			nodes[i] = new NodeImpl(phMachName + i);
		
		VirtualNode[] vNodes = new VirtualNode[siz];
		for (int i=0; i<siz; i++)
		{
			vNodes[i] = new VirtualNodeImpl("virtualNode"+i);
			vNodes[i].setImplementedBy(IMUtil.createSetWithOneValue(nodes[i]));
			contain.add(vNodes[i]);
		}
		top.setContains(contain);
		
		for (int i=0; i<siz; i++)
		{
			if (i < serv)
			{
				nodes[i].setHardwareType("pc");
				
				//assign the requirements
				Set<NodeComponent> nodComp = new HashSet<NodeComponent>();
				Memory mem = new MemoryImpl("mem" + i);
				mem.setHasMemorySize(1.5f + i);  //memory 1.5 + i
				nodComp.add(mem);
				
				Storage sto = new StorageImpl("sto" + i);
				sto.setHasStorageSize(2f + i);    //storage 2 + i
				nodComp.add(sto);
				
				CPU cpu = new CPUImpl("cpu" + i);
				cpu.setHasCores(BigInteger.valueOf(1 + i));  //cpu cores 1 + i
				nodComp.add(cpu);
				
				vNodes[i].setHasComponent(nodComp);
				
				
			}
			else
			{
				nodes[i].setHardwareType("genericNetworkDevice");
				//nodes[i].setHasAvailableLogicalRouters(2 + i);
			}
			
		}
		
		return top;
	}
	
	
	public void storePhyMachines(int siz, int router) 
	{
		Node[] nodes = new Node[siz];
		for (int i=0; i<siz; i++)
			nodes[i] = new NodeImpl(phMachName + i);
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(NoviUris.getSubstrateURI());

		for (int i=0; i<siz; i++)
		{

			if (i < router)
			{
				//assign the requirements
				Set<NodeComponent> nodComp = new HashSet<NodeComponent>();
				Memory mem = new MemoryImpl("mem" + i);
				mem.setHasAvailableMemorySize(5.5f + i);  //memory 5.5 + i
				nodComp.add(mem);

				Storage sto = new StorageImpl("sto" + i);
				sto.setHasAvailableStorageSize(7f + i);    //storage 7 + i
				nodComp.add(sto);

				CPU cpu = new CPUImpl("cpu" + i);
				cpu.setHasAvailableCores(BigInteger.valueOf(3 + i));  //cpu cores 3 + i
				nodComp.add(cpu);

				nodes[i].setHasComponent(nodComp);
				
			}
			else
			{
				nodes[i].setHasAvailableLogicalRouters(5 + i); // avail logical router 5 + i
			}

			
			
			try {
				con.addObject(nodes[i]);
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ConnectionClass.closeAConnection(con);

	}
	
	public void storePhyMachinesNoComp(int siz, int router) 
	{
		Node[] nodes = new Node[siz];
		for (int i=0; i<siz; i++)
			nodes[i] = new NodeImpl(phMachName + i);
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(NoviUris.getSubstrateURI());

		for (int i=0; i<siz; i++)
		{

			
			
			try {
				con.addObject(nodes[i]);
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ConnectionClass.closeAConnection(con);

	}

}
