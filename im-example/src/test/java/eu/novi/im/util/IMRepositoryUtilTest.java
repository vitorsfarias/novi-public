package eu.novi.im.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;

public class IMRepositoryUtilTest {

	@Test
	public void testGroupExportToString() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Collection<Group> multipleGroups = repositoryUtil.getGroupImplFromFile(requestTest);
		
		for(Group g : multipleGroups){
			String stringOfGroup = repositoryUtil.exportIMObjectToString((GroupImpl)g);
			Collection<Group> resultGroup = repositoryUtil.getGroupImplFromFile(stringOfGroup);
			assertTrue(resultGroup.size()==1);
			
			GroupImpl gg = (GroupImpl) resultGroup.iterator().next();
			
			assertTrue(gg.getContains().size() == g.getContains().size());
		}
		
	}
	
	@Test
	public void testIPaddress() throws IOException{
		Interface inter = new InterfaceImpl("interface");
		inter.setHasIPv4Address(IMUtil.createUnitIPAddressSet("ip", "192.168.1.1"));
		IMCopy copy = new IMCopy();
		Interface intCopy = (Interface)copy.copy(inter, -1);
		assertTrue(intCopy.getHasIPv4Address().iterator().next().getHasValue().equals("192.168.1.1"));
		
		
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/ipAddressTest.owl");
		Node node = repositoryUtil.getIMObjectFromString(requestTest, Node.class);
		Interface in = node.getHasInboundInterfaces().iterator().next();
		System.out.println(in.getHasIPv4Address().iterator().next().toString());
		System.out.println(in.getHasIPv4Address().iterator().next().getHasValue());
		assertTrue(in.getHasIPv4Address().iterator().next().getHasValue().equals("192.168.1.1"));
		
		
	}
	
	@Test
	public void testFilterExportString() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Collection<Group> multipleGroups = repositoryUtil.getGroupImplFromFile(requestTest);
		
		for(Group g : multipleGroups){
			String stringOfGroup = repositoryUtil.exportIMObjectToStringWithFilter((GroupImpl)g, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity");
			Collection<Group> resultGroup = repositoryUtil.getGroupImplFromFile(stringOfGroup);
			assertTrue(resultGroup.size()==1);
			
			GroupImpl gg = (GroupImpl) resultGroup.iterator().next();
			
			assertTrue(gg.getContains().size() == g.getContains().size());
		}
		
		
	}
	
	@Test
	public void testInterfaceConnectedTo() throws IOException
	{
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/interfaceConnectedTo.owl");
		Topology top = imRep.getIMObjectFromString(requestTest, Topology.class);
		Resource res = top.getContains().iterator().next();
		Interface interf = ((Node) res).getHasOutboundInterfaces().iterator().next();
		Node nod = (Node) interf.getConnectedTo().iterator().next();
		System.out.println(nod.toString());
		
		Interface in = imRep.getIMObjectFromString(requestTest, Interface.class);
		System.out.println(in.getConnectedTo());
		
	}

	@Test
	public void testIndividualGroupExport(){
		
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
			GroupImpl aGroup = new GroupImpl("MyGroup");
			VirtualNodeImpl myVN = new VirtualNodeImpl("SomeVirtualNode");
			myVN.setHardwareType("A hardware type");
			aGroup.setContains(IMUtil.createSetWithOneValue(myVN));
			
			String testString = repositoryUtil.exportIMObjectToString(aGroup);
			System.out.println(testString);
			Set<Group> retrieved = repositoryUtil.getIMObjectsFromString(testString, Group.class);
			
			Group firstGroup = retrieved.iterator().next();
			assertTrue(firstGroup.toString().endsWith("MyGroup"));
			assertTrue(firstGroup.getContains().size() == 1);
			VirtualNode vn = (VirtualNode)firstGroup.getContains().iterator().next();
			assertTrue(vn.getHardwareType().equals("A hardware type"));
			
			Group retrTop = repositoryUtil.getIMObjectFromString(testString, Group.class);
			assertTrue(retrTop.toString().equals("http://fp7-novi.eu/im.owl#MyGroup"));
			assertTrue(retrTop.getContains().size() == 1);
			
			System.out.println(repositoryUtil.exportIMObjectToString(retrTop));
			
			
			//get a specific object
			VirtualNode vNode = repositoryUtil.getIMObjectFromString(testString, VirtualNode.class,
					"http://fp7-novi.eu/im.owl#SomeVirtualNode");
			assertNotNull(vNode);
			assertTrue(vNode.getHardwareType().equals("A hardware type"));
			
			Node node = repositoryUtil.getIMObjectFromString(testString, Node.class,
					"http://fp7-novi.eu/im.owl#SomeVirtualNode");
			assertNotNull(node);
			assertTrue(node instanceof VirtualNode);
			
			
	}
	@Test
	public void testIndividualExport(){
		
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
			TopologyImpl aTopology = new TopologyImpl("MyTopology");
			VirtualNodeImpl myVN = new VirtualNodeImpl("SomeVirtualNode");
			myVN.setHardwareType("A hardware type");
			Memory mem = new MemoryImpl("myMemory");
			mem.setHasMemorySize(23.0f);
			myVN.setHasComponent(IMUtil.createSetWithOneValue(mem));
			
			Interface interf = new InterfaceImpl("myInterface");
			interf.setHasIPv4Address(IMUtil.createUnitIPAddressSet("myIp", "192.168.12.5"));
			myVN.setHasInboundInterfaces(IMUtil.createSetWithOneValue(interf));
			
			aTopology.setContains(IMUtil.createSetWithOneValue(myVN));
			
			String testString = repositoryUtil.exportIMObjectToString(aTopology);
			
			Set<Topology> retrieved = repositoryUtil.getIMObjectsFromString(testString, Topology.class);
			
			Topology firstTopology = retrieved.iterator().next();
			assertTrue(firstTopology.toString().endsWith("MyTopology"));
			assertTrue(firstTopology.getContains().size() == 1);
			VirtualNode vn = (VirtualNode)firstTopology.getContains().iterator().next();
			assertTrue(vn.getHardwareType().equals("A hardware type"));
			
			Topology retrTop = repositoryUtil.getIMObjectFromString(testString, Topology.class);
			assertTrue(retrTop.toString().equals("http://fp7-novi.eu/im.owl#MyTopology"));
			assertTrue(retrTop.getContains().size() == 1);
			
			//get a specific object
			VirtualNode vNode = repositoryUtil.getIMObjectFromString(testString, VirtualNode.class,
					"http://fp7-novi.eu/im.owl#SomeVirtualNode");
			assertNotNull(vNode);
			assertTrue(vNode.getHardwareType().equals("A hardware type"));
			
			Node node = repositoryUtil.getIMObjectFromString(testString, Node.class,
					"http://fp7-novi.eu/im.owl#SomeVirtualNode");
			assertNotNull(node);
			assertTrue(node instanceof VirtualNode);
			
			System.out.println(IMUtil.getOneValueFromSet(IMUtil.getOneValueFromSet(
					node.getHasInboundInterfaces()).getHasIPv4Address()));
			System.out.println(IMUtil.getOneValueFromSet(
					node.getHasInboundInterfaces()));
			// TODO find the problem here
			assertTrue(IMUtil.getOneValueFromSet(IMUtil.getOneValueFromSet(
					node.getHasInboundInterfaces()).getHasIPv4Address()).getHasValue().equals("192.168.12.5"));
			
			System.out.println(repositoryUtil.exportIMObjectToString(retrTop));
			
			
	}
	@Test
	public void testGroupSetExportToString() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Set<Group> multipleGroups = repositoryUtil.getGroupImplFromFile(requestTest);
		repositoryUtil.exportGroupImplSetToString(multipleGroups);
	
	}
	
	
	
	@Test
	public final void testIMRepositoryUtilImpl() {
		IMRepositoryUtil util = new IMRepositoryUtilImpl();
		assert(util != null);
	}

	@Test
	public final void testDestroyMethod() {
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
		util.destroyMethod();
		assert(util != null);
	}
	
	@Test
	public final void testDumping() throws RepositoryException {
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
		util.dumpRepository();
		assert(util != null);
	}
	
	@Test
	public final void testDestroyMethodOnClosedConnection() throws RepositoryException {
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
		util.getConnection().close();
		util.destroyMethod();
		assert(!util.getConnection().isOpen());
	}
	
	@Test
	public final void testGetTopologyFromFile() throws IOException {
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
		util.getTopologyFromFile(readFile("src/test/resources/MidtermWorkshopRequest.owl"));
	}

	@Test
	public void testGetGroups() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Collection<Group> groupsFromRequest = repositoryUtil.getGroupImplFromFile(requestTest);
		assert(groupsFromRequest != null);
		requestTest = readFile("src/test/resources/failingSlice.owl");
		groupsFromRequest = repositoryUtil.getGroupImplFromFile(requestTest);
		assert(groupsFromRequest != null);
		
	}
	
	@Test
	public void testGetGroups1() throws IOException{
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Collection<Group> groupsFromRequest = repositoryUtil.getGroupsFromFile(requestTest);
		assert(groupsFromRequest != null);
	}
	
	@Test
	public void testGetKnownResourcesFromFile() throws IOException {

		IMRepositoryUtilImpl repositoryUtil= new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Resource res = repositoryUtil.getKnownResourceFromFile(requestTest, "Group");
		assert(requestTest!= null);
	}

	@Test
	public void testReservationFromFile() throws IOException {

		IMRepositoryUtilImpl repositoryUtil= new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Reservation res = repositoryUtil.getReservationFromFile(requestTest);
		assert(requestTest!= null);
		requestTest = readFile("src/test/resources/failingSlice.owl");
		res = repositoryUtil.getReservationFromFile(requestTest);
		assert(requestTest!= null);
	
	}
	@Test
	public void testResourceFromFile() throws IOException {

		IMRepositoryUtilImpl repositoryUtil= new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Resource res = repositoryUtil.getResourceFromFile(requestTest);
		assert(res!= null);
	}
	
	
	public String readFile(String path) throws IOException{
		File file = new File(path);
		StringBuffer result = new StringBuffer();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String currentLine = reader.readLine();
		while(currentLine != null)
		{
				result.append(currentLine);
				currentLine = reader.readLine();
		}
		
		return result.toString();
	}
	


}
