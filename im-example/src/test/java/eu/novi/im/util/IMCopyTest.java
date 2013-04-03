package eu.novi.im.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jline.internal.Log;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.*;
import eu.novi.im.core.impl.*;
import eu.novi.im.policy.ECAPolicy;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.impl.ECAPolicyImpl;
import eu.novi.im.policy.impl.ManagedEntityImpl;
import eu.novi.im.unit.impl.IPAddressImpl;
import eu.novi.im.unit.IPAddress;

public class IMCopyTest {
	static ObjectConnection connection;
	static Repository rdfRepository;
	static final String NOVI_ADD = "http://fp7-novi.eu/im.owl#";
	private static final transient Logger log =
			LoggerFactory.getLogger(IMCopyTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		MemoryStore memoryStore = new MemoryStore();
		rdfRepository = new SailRepository(memoryStore);
		System.out.println("Setting up");
		try {
			rdfRepository.initialize();
			ObjectRepositoryFactory objectRepositoryFactory = new ObjectRepositoryFactory();
			ObjectRepository objectRepository = objectRepositoryFactory
					.createRepository(rdfRepository);
			connection = objectRepository.getConnection();

		} catch (RepositoryConfigException e) {

			e.printStackTrace();
		} catch (RepositoryException e) {

			e.printStackTrace();
		} 
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		connection.close();
		rdfRepository.shutDown();
		System.out.println("Tear down ");
	}
	
	
	@Test
	public void testRecursiveDepth() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();
		Node node1 = factory.createObject(NOVI_ADD + "node1", Node.class);
		connection.addObject(node1.toString(), node1);
		Node node2 = factory.createObject(NOVI_ADD + "node2", Node.class);
		connection.addObject(node2.toString(), node2);
		Node node3 = factory.createObject(NOVI_ADD + "node3", Node.class);
		connection.addObject(node3.toString(), node3);
		Node node4 = factory.createObject(NOVI_ADD + "node4", Node.class);
		connection.addObject(node4.toString(), node4);
		
		Set<Node> nodeSet = new HashSet<Node>();
		nodeSet.add(node2);
		nodeSet.add(node3);
		
		node1.setConnectedTo(nodeSet);
		
		
		node2.setConnectedTo(IMUtil.createSetWithOneValue(node4));
		node3.setConnectedTo(IMUtil.createSetWithOneValue(node4));

		
		IMCopy copy = new IMCopy();
		NodeImpl nodeImp = (NodeImpl) copy.copy(node1, 0);
		assertNull(nodeImp.getConnectedTo());
		nodeImp = (NodeImpl) copy.copy(node1, 1);
		assertEquals(2 , nodeImp.getConnectedTo().size());
		System.out.println(IMUtil.getOneValueFromSet(nodeImp.getConnectedTo()).getConnectedTo());
		assertNull(IMUtil.getOneValueFromSet(nodeImp.getConnectedTo()).getConnectedTo());
		
		nodeImp = (NodeImpl) copy.copy(node1, 2);
		assertNotNull(IMUtil.getOneValueFromSet(nodeImp.getConnectedTo()).getConnectedTo());
		assertEquals(1 , IMUtil.getOneValueFromSet(nodeImp.getConnectedTo()).getConnectedTo().size());

		//assertEquals(NOVI_ADD + "node3" , IMUtil.getOneValueFromSet(nodeImp.getConnectedTo()).
		//		getConnectedTo().toArray()[0].toString());

		
	}
	
	//@Test
	public void testLogicalRouters()
	{
		Node node = new NodeImpl("anode");
		node.setHasAvailableLogicalRouters(4);
		node.setHasLogicalRouters(6);
		
		IMCopy copy = new IMCopy();
		copy.enableLogs();
		copy.copy(node, -1);
		
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();
		String rdfSt = imRep.exportIMObjectToString(node);
		log.debug("this is the string: \n{}", rdfSt);
		imRep.getIMObjectFromString(rdfSt, Node.class);
		
	}
	
	@Test
	public void testCloneObject()
	{
		IMCopy copy = new IMCopy();
		String st = (String) copy.cloneNotIMObject("stringTest");
		assertNotNull(st);
		assertTrue(st.equals("stringTest"));
		
		Integer in = (Integer) copy.cloneNotIMObject(new Integer(4));
		assertNotNull(in);
		assertEquals(4, in.intValue());
		
		Float fl = (Float) copy.cloneNotIMObject(new Float(4.3));
		assertNotNull(fl);
		assertEquals(0, fl.compareTo(4.3f));
		
		Double dl = (Double) copy.cloneNotIMObject(new Double(4.3));
		assertNotNull(dl);
		assertEquals(0, dl.compareTo(4.3));
		
	}
	
	@Test
	public void testTopology() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();

		Topology top = factory.createObject(NOVI_ADD + "topology", Topology.class);
		connection.addObject(top.toString(), top);
		
		Node node1 = factory.createObject(NOVI_ADD + "node1", Node.class);
		connection.addObject(node1.toString(), node1);
		
		top.setContains(IMUtil.createSetWithOneValue(node1));

		
		IMCopy copy = new IMCopy();
		TopologyImpl topImp = (TopologyImpl) copy.copy(top, 10);

		Resource res = IMUtil.getOneValueFromSet(topImp.getContains());

		assertEquals(NOVI_ADD + "node1" , res.toString());
		assertEquals(NOVI_ADD + "topology" , IMUtil.getOneValueFromSet(res.getIsContainedIn()).toString());
		//assertEquals(NOVI_ADD + "topology" , IMUtil.getOneValueFromSet(node1.getIsContainedIn()));

		
	}

	@Test
	public void testInterface() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();
		
		Interface int1 = factory.createObject("http://fp7-novi.eu/im.owl#lrouter.if0-out", Interface.class);
		Interface int2 = factory.createObject("http://fp7-novi.eu/im.owl#lrouter.if1-out", Interface.class);
		Interface intReal = 
				factory.createObject("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/2/9-out", Interface.class);

		Link link = factory.createObject("http://fp7-novi.eu/im.owl#theLink", Link.class);
		link.setHasSink(IMUtil.createSetWithOneValue(int1));
		connection.addObject(link.toString(), link);
		
		Topology top = factory.createObject(NOVI_ADD + "topology", Topology.class);
		connection.addObject(top.toString(), top);

		connection.addObject(int1.toString(), int1);
		connection.addObject(int2.toString(), int2);
		connection.addObject(intReal.toString(), intReal);
		
		//set the implement by
		int1.setImplementedBy(IMUtil.createSetWithOneValue(intReal));
		int2.setImplementedBy(IMUtil.createSetWithOneValue(intReal));
		
		//add the  interfaces and link to the topology
		Set<Resource> res = new HashSet<Resource>();
		//res.add(int1);
		res.add(int2);
		//res.add(intReal);
		res.add(link);
		top.setContains(res);
		


		
		IMCopy copy = new IMCopy();
		//copy.enableLogs();
		TopologyImpl topImpl = (TopologyImpl) copy.copy(top, -1);

		assertEquals(2, topImpl.getContains().size());
		
		for (Resource r : topImpl.getContains())
		{
			if (r.toString().equals("http://fp7-novi.eu/im.owl#lrouter.if0-out"))
			{
				log.debug("Interface 1");
				assertNotNull( ((Interface) r).getImplementedBy());
				assertEquals(1, ((Interface) r).getImplementedBy().size());
				
			}
			else if (r.toString().equals("http://fp7-novi.eu/im.owl#lrouter.if1-out"))
			{//in the topology
				log.debug("Interface 2");
				assertNotNull( ((Interface) r).getImplementedBy());
				assertEquals(1, ((Interface) r).getImplementedBy().size());
				assertEquals(2, //the physical interface implements
						IMUtil.getOneValueFromSet(((Interface) r).getImplementedBy()).getImplements().size());
				
			}
			else if (r.toString().equals("http://fp7-novi.eu/im.owl#theLink"))
			{//the link, in the topology 
				log.debug("Link");
				assertNotNull( ((Link) r).getHasSink());
				assertEquals(1, ((Link) r).getHasSink().size());
				//interface1 implementBy physicalInterface
				assertEquals(1, IMUtil.getOneValueFromSet(((Link) r).getHasSink()).getImplementedBy().size());
				
			}
			else
			{//the physical interface
				log.debug("Physical interface");
				assertNull( ((Interface) r).getImplementedBy());
				assertNotNull( ((Interface) r).getImplements());
				assertEquals(2, ((Interface) r).getImplements().size());
			}
		}
		
		
		log.debug("Run again the copy");
		//run again the copy in a topoogy with implemented objects
		
		Set<Resource> res1 = topImpl.getContains();//the current resources in the topology
		Set<Resource> res2 = new HashSet<Resource>();
		for (Resource r : res1)
		{
			if (r.toString().equals("http://fp7-novi.eu/im.owl#lrouter.if1-out"))
			{//in the topology
				log.debug("Interface 2 found");
				//the physical interface
				res2.add(IMUtil.getOneValueFromSet(((Interface) r).getImplementedBy())); 
				assertNotNull( ((Interface) r).getImplementedBy());
				assertEquals(1, ((Interface) r).getImplementedBy().size());
				assertEquals(2, //the physical interface implements
						IMUtil.getOneValueFromSet(((Interface) r).getImplementedBy()).getImplements().size());
				
			}
			else if (r.toString().equals("http://fp7-novi.eu/im.owl#theLink"))
			{//the link, in the topology 
				log.debug("Link found");
				res2.add(r);
			}
			
		}
		//now in the topology add the link and the physical interface
		topImpl.setContains(res2);
		
		TopologyImpl topImpl2 = (TopologyImpl) copy.copy(topImpl, -1);

		assertEquals(2, topImpl2.getContains().size());
		
		for (Resource r : topImpl2.getContains())
		{
			if (r.toString().equals("http://fp7-novi.eu/im.owl#lrouter.if0-out"))
			{
				log.debug("second copy: Interface 1");
				assertNotNull( ((Interface) r).getImplementedBy());
				assertEquals(1, ((Interface) r).getImplementedBy().size());
				
			}
			else if (r.toString().equals("http://fp7-novi.eu/im.owl#lrouter.if1-out"))
			{//in the topology
				log.debug("second copy: Interface 2");
				assertNotNull( ((Interface) r).getImplementedBy());
				assertEquals(1, ((Interface) r).getImplementedBy().size());
				assertEquals(2, //the physical interface implements
						IMUtil.getOneValueFromSet(((Interface) r).getImplementedBy()).getImplements().size());
				
			}
			else if (r.toString().equals("http://fp7-novi.eu/im.owl#theLink"))
			{//the link, in the topology 
				log.debug("second copy: Link");
				assertNotNull( ((Link) r).getHasSink());
				assertEquals(1, ((Link) r).getHasSink().size());
				//interface1 implementBy physicalInterface
				assertEquals(1, IMUtil.getOneValueFromSet(((Link) r).getHasSink()).getImplementedBy().size());
				
			}
			else
			{//the physical interface
				log.debug("second copy: Physical interface");
				assertNull( ((Interface) r).getImplementedBy());
				assertNotNull( ((Interface) r).getImplements());
				assertEquals(2, ((Interface) r).getImplements().size());
				for (Interface in: ((Interface) r).getImplements())
				{
					assertEquals(1, in.getImplementedBy().size());
					assertEquals("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/2/9-out",
							IMUtil.getOneValueFromSet(in.getImplementedBy()).toString());
					
					
				}
			}
		}



		
	}
	
	
	@Test
	public void simpleTest() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();
		VirtualNode vNode = factory.createObject(NOVI_ADD + "myVNode", VirtualNode.class);
		connection.addObject(vNode.toString(), vNode);
		vNode.setHardwareType("x86");
		vNode.setLocatedAt(new LocationImpl("mylocation"));
		
		IMCopy copy = new IMCopy();
		VirtualNodeImpl vNodeIm = (VirtualNodeImpl) copy.copy(vNode, 1);
		assertEquals("x86", vNodeIm.getHardwareType());
		assertEquals(NOVI_ADD + "mylocation", vNodeIm.getLocatedAt().toString());
		System.out.println(vNodeIm.toString());
		
	}
	
	@Test
	public void testLoops() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();
		Node node1 = factory.createObject(NOVI_ADD + "node1", Node.class);
		connection.addObject(node1.toString(), node1);
		Node node2 = factory.createObject(NOVI_ADD + "node2", Node.class);
		connection.addObject(node2.toString(), node2);
		
		node1.setConnectedTo(IMUtil.createSetWithOneValue(node2));
		node2.setConnectedTo(IMUtil.createSetWithOneValue(node1));

		
		IMCopy copy = new IMCopy();
		NodeImpl nodeImp = (NodeImpl) copy.copy(node1, -1);
		assertEquals(1, nodeImp.getConnectedTo().size());
		assertEquals(NOVI_ADD + "node2", nodeImp.getConnectedTo().toArray()[0].toString());

		
	}
	

	@Test
	public void testSetsAndIPAddress() throws RepositoryException {
		ObjectFactory factory = connection.getObjectFactory();

		IMCopy copy = new IMCopy();

		
		///check  interface//
		Interface interf = factory.createObject(NOVI_ADD + "myInterface", Interface.class);
		connection.addObject(interf.toString(), interf);
		IPAddress ip1 = new IPAddressImpl("ip1");
		ip1.setHasValue("192.168.1.1");
		connection.addObject(ip1.toString(), ip1);
		
		IPAddress ip2 = new IPAddressImpl("ip2");
		ip2.setHasValue("192.168.1.2");
		connection.addObject(ip2);
		
		interf.setHasIPv4Address(new HashSet<IPAddress>(Arrays.asList(ip1, ip2)));
		Path path1 = factory.createObject(NOVI_ADD + "path1", Path.class);
		connection.addObject(path1.toString(), path1);
		Path path2 = factory.createObject(NOVI_ADD + "path2", Path.class);
		connection.addObject(path2.toString(), path2);
		Set<Path> paths = new HashSet<Path>();
		paths.add(path1);
		paths.add(path2);
		interf.setInPaths(paths);
		interf.setIsSource(paths);
		
		Resource res = factory.createObject(NOVI_ADD + "myresource", Resource.class);
		connection.addObject(res.toString(), res);
		path1.setContains(IMUtil.createSetWithOneValue(res));
		
		for (Statement st: connection.getStatements(null, null, null).asList())
		{
			System.out.println(st.toString());
		}
		
		
		InterfaceImpl intImpl = (InterfaceImpl) copy.copy(interf, -1);
		assertEquals(2, intImpl.getHasIPv4Address().size());
		for (IPAddress st : intImpl.getHasIPv4Address())
		{
			if (st.toString().endsWith("ip1"))
			{
				assertTrue(st.getHasValue().equals("192.168.1.1"));
			}
			else if (st.toString().endsWith("ip2"))
			{
				assertTrue(st.getHasValue().equals("192.168.1.2"));
			}
			
		}
		assertEquals(2, intImpl.getInPaths().size());
		assertTrue(intImpl.getInPaths().toArray()[0] instanceof PathImpl);
		assertEquals(2, intImpl.getIsSource().size());
		assertTrue(intImpl.getIsSource().toArray()[0] instanceof PathImpl);
		System.out.println(intImpl.getHasIPv4Address().toString());
		System.out.println(intImpl.getInPaths().toString());
		
		
		
	}
	
	@Test
	public void someAdditionalCopyTest(){
			IMCopy cp = new IMCopy();
			Lifetime oldLifetime = new LifetimeImpl("Lifetime");
			Lifetime newLifetime = (Lifetime) cp.copy(oldLifetime, 0);
			assert(newLifetime != null);
			
			Group oldGroup = new GroupImpl("Group");
			Group newGroup = (Group) cp.copy(oldGroup, 0);
			assert(newGroup != null);
			
			Link oldLink = new LinkImpl("Link");
			Link newLink = (Link) cp.copy(oldLink, 0);
			assert(newLink != null);
			
			NetworkElement oldNetworkElement = new NetworkElementImpl("NetworkElement");
			NetworkElement newNetworkElement = (NetworkElement) cp.copy(oldNetworkElement, 0);
			assert(newNetworkElement != null);
			
			DiskImage oldDiskImage = new DiskImageImpl("DiskImage");
			DiskImage newDiskImage = (DiskImage) cp.copy(oldDiskImage, 0);
			assert(newDiskImage != null);
			
			SwitchingMatrix oldSwitchingMatrix = new SwitchingMatrixImpl("SwitchingMatrix");
			SwitchingMatrix newSwitchingMatrix = (SwitchingMatrix) cp.copy(oldSwitchingMatrix, 0);
			assert(newSwitchingMatrix != null);
			
			LoginComponent oldLoginComponent = new LoginComponentImpl("LoginComponent");
			LoginComponent newLoginComponent = (LoginComponent) cp.copy(oldLoginComponent, 0);
			assert(newLoginComponent != null);
			
			NodeComponent oldNodeComponent = new NodeComponentImpl("NodeComponent");
			NodeComponent newNodeComponent = (NodeComponent) cp.copy(oldNodeComponent, 0);
			assert(newNodeComponent != null);
		
			
			Service oldService = new ServiceImpl("Service");
			Service newService = (Service) cp.copy(oldService, 0);
			assert(newService != null);
			
			MemoryService oldMemoryService = new MemoryServiceImpl("MemoryService");
			MemoryService newMemoryService = (MemoryService) cp.copy(oldMemoryService, 0);
			assert(newMemoryService != null);
			
			LoginService oldLoginService = new LoginServiceImpl("LoginService");
			LoginService newLoginService = (LoginService) cp.copy(oldLoginService, 0);
			assert(newLoginService != null);
			
			ProcessingService oldProcessingService = new ProcessingServiceImpl("ProcessingService");
			ProcessingService newProcessingService = (ProcessingService) cp.copy(oldProcessingService, 0);
			assert(newProcessingService != null);
			
			StorageService oldStorageService = new StorageServiceImpl("StorageService");
			StorageService newStorageService = (StorageService) cp.copy(oldStorageService, 0);
			assert(newStorageService != null);
			
			NSwitchService oldSwitchingService = new NSwitchServiceImpl("SwitchingService");
			NSwitchService newSwitchingService = (NSwitchService) cp.copy(oldSwitchingService, 0);
			assert(newSwitchingService != null);
			
	}
	
	
	@Test
	public void testGetSourceClass()
	{
		IMCopy copy = new IMCopy();
		Resource res = new ResourceImpl("resource");
		assertTrue(copy.getSourceClass(res) == Resource.class);
		assertFalse(copy.getSourceClass(res) == Node.class);
		
		Node nod = new NodeImpl("node");
		assertTrue(copy.getSourceClass(nod) == Node.class);
		
		VirtualLink vlink = new VirtualLinkImpl("link");
		assertTrue(copy.getSourceClass(vlink) == VirtualLink.class);
		
		ManagedEntity manEnt = new ManagedEntityImpl("managedEntity");
		assertTrue(copy.getSourceClass(manEnt) == ManagedEntity.class);
		
		ECAPolicy polic = new ECAPolicyImpl("policy");
		assertTrue(copy.getSourceClass(polic) == ECAPolicy.class);
		System.out.println(polic.toString());
		
		
	}
	

	
	@Test
	public void testGetGroupBound() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest =
				readFile("src/test/resources/SIMPLE_BOUND_CHRYSA_TEST.owl");
		Collection<Group> groupsFromRequest =
				repositoryUtil.getIMObjectsFromString(requestTest, Group.class);
		assertTrue(groupsFromRequest != null);
		assertEquals(1, groupsFromRequest.size());
	}
	
	
	//@Test
	public void testBoundRequestFile() throws IOException
	{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest =
				readFile("src/test/resources/example_bound.owl");
		Collection<Topology> groupsFromRequest =
				repositoryUtil.getIMObjectsFromString(requestTest, Topology.class);
		assertTrue(groupsFromRequest != null);
		assertEquals(1, groupsFromRequest.size());
		
	}
	
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	
}
