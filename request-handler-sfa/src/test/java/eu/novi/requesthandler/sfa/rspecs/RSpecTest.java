/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.rspecs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.osgi.service.log.LogService;
import org.w3c.dom.Element;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Memory;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.exceptions.RHBadInputException;
import eu.novi.requesthandler.sfa.rspecs.FedericaRSpec;
import eu.novi.requesthandler.sfa.rspecs.PlanetLabRSpecv2;
import eu.novi.requesthandler.utils.RHUtils;
import eu.novi.im.unit.IPAddress;
import eu.novi.im.unit.impl.IPAddressImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;

public class RSpecTest {
	String sep = System.getProperty("file.separator");

	TopologyImpl fedTopology;
	TopologyImpl plTopology;
	
	private static String readFileAsString(String filePath)
	throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead=0;
		while((numRead=reader.read(buf)) != -1){
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
		

	@Test
	public void addMultiLinkPath() throws IOException, RHBadInputException {
		FedericaRSpec rspec = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		rspec.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		rspec.setUser(user);
		rspec.createEmptyRequestRSpec();
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PartialBoundRequest.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
				if(!rspec.isExistingVirtualLink((VirtualLink)resource))
					rspec.addLinkToRequestRSpec((VirtualLink)resource);
			}
		}
		
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("<component_manager name=\"urn:publicid:IDN+federica.eu+authority+cm\"/>"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"sliver1-if0"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"lrouter.if0"));
	}
	
	@Test
	public void addMultiLinkPathMixTestbeds() throws IOException, RHBadInputException {
		FedericaRSpec rspec = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		rspec.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		rspec.setUser(user);
		rspec.createEmptyRequestRSpec();
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultiLinkPathPlanetLabFederica.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
				if(!rspec.isExistingVirtualLink((VirtualLink)resource))
					rspec.addLinkToRequestRSpec((VirtualLink)resource);
			}
		}
		
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("<component_manager name=\"urn:publicid:IDN+federica.eu+authority+cm\"/>"));
		assertTrue(rspec.toString().contains("garr.mil.router1.ge-0/0/0"));
		assertTrue(rspec.toString().contains("psnc.poz.router1.ge-0/1/0"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"nswitch:if0"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"lrouter.if0"));
	}
	
	@Test
	public void addInterdomainLink() throws IOException, RHBadInputException {
		FedericaRSpec rspec = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		rspec.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		rspec.setUser(user);
		rspec.createEmptyRequestRSpec();
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
				if(!rspec.isExistingVirtualLink((VirtualLink)resource))
					rspec.addLinkToRequestRSpec((VirtualLink)resource);
			}
		}
		
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("link4"));
		assertTrue(rspec.toString().contains("link3"));
		assertTrue(!rspec.toString().contains("link1"));
		assertTrue(!rspec.toString().contains("link2"));
	}
	

	
	
	
	@Test
	public void addMultiLinkPathMixTestbedsNSwitch() throws IOException, RHBadInputException {
		FedericaRSpec rspec = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		rspec.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		rspec.setUser(user);
		rspec.createEmptyRequestRSpec();
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultilinkNSwitch.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
				if(!rspec.isExistingVirtualLink((VirtualLink)resource))
					rspec.addLinkToRequestRSpec((VirtualLink)resource);
			}
		}
		
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("<component_manager name=\"urn:publicid:IDN+federica.eu+authority+cm\"/>"));
		assertTrue(rspec.toString().contains("garr.mil.router1.ge-0/0/0"));
		assertTrue(rspec.toString().contains("psnc.poz.router1.ge-0/1/0"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"nswitch:if0"));
		assertTrue(rspec.toString().contains("<interface_ref client_id=\"lrouter.if0"));
	}
	
	@Test
	public void testTranslateFedericaVM(){
		VirtualNode vm = new VirtualNodeImpl("fedVM");
		Node phyServer = new NodeImpl("physicalVM");
		phyServer.setHostname("physicalHostName");
		
		Set<Node> implementedBySet = new HashSet<Node>();
		implementedBySet.add(phyServer);
		vm.setImplementedBy(implementedBySet);
		vm.setHostname("physicalVM");
		vm.setHrn("VM_hrn");
		vm.setHasOS("operating system");
		vm.setVirtualRole("vm");
		
		Set<NodeComponent> nodeComponentSet = new HashSet<NodeComponent>();
		
		CPU cpu = new CPUImpl("cpu");
		cpu.setHasCPUSpeed((float) 2);
		cpu.setHasCores(new BigInteger("3"));		
		nodeComponentSet.add(cpu);
		
		Memory mem = new MemoryImpl("memory");
		mem.setHasMemorySize((float) 2500);
		nodeComponentSet.add(mem);
		
		Storage storage = new StorageImpl("storage");
		storage.setHasStorageSize((float)1300);
		nodeComponentSet.add(storage);
		
		vm.setHasComponent(nodeComponentSet);
		
		Set<Interface> interfaces = new HashSet<Interface>();
		Interface ifa = new InterfaceImpl("if1");
		Interface phyIface = new InterfaceImpl("phyIf-in");
		Set<Interface> phyInterfaces = new HashSet<Interface>();
		phyInterfaces.add(phyIface);
		ifa.setImplementedBy(phyInterfaces);
		IPAddress ip = new IPAddressImpl("ip");
		ip.setHasValue("1.1.1.1/24");
		Set<IPAddress> ips = new HashSet<IPAddress>();
		ips.add(ip);
		ifa.setHasIPv4Address(ips);
		interfaces.add(ifa);
		vm.setHasInboundInterfaces(interfaces);
		
		FedericaRSpec rspec = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		rspec.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		rspec.setUser(user);
		rspec.createEmptyRequestRSpec();
		
		try {
			rspec.addNodeToRequestRSpec(vm);
			String finalRSpec = rspec.toString();
			System.out.println(rspec.toString());
			assertTrue(finalRSpec.contains("<hardware_type name=\"pc\"/>"));
			assertTrue(finalRSpec.contains("<sliver_type name=\"vm\">"));
			assertTrue(finalRSpec.contains("<node client_id=\"fedVM\""));
			assertTrue(finalRSpec.contains("<cc:compute_capacity cpuSpeed=\"2\" diskSize=\"1300\" guestOS=\"operating system\" numCPUCores=\"3\" ramSize=\"2500\"/>"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void shouldNotAddExistingInterfaceToLinkInFederica() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);
		
		Link link = new LinkImpl("uri");
		
		Interface ifc = new InterfaceImpl("uri");
		Set<Interface> hasSink = new HashSet<Interface>();
		hasSink.add(ifc);
		
		link.setHasSink(hasSink);
		
		fedSchema.addInterfaceToLinkSinks(link, ifc);
		
		assert(link.getHasSink().size() == 1);		
		
		link.setHasSource(hasSink);
		
		fedSchema.addInterfaceToLinkSources(link, ifc);
		
		assert(link.getHasSource().size() == 1);
	}
		
	@Test
	public void shouldCreateEmptyRSpecPlanetLab() {
		PlanetLabRSpecv2 rspecPL = new PlanetLabRSpecv2();
		
		rspecPL.createEmptyRequestRSpec();
		
		String rspecString = rspecPL.toString();
		
		assert(rspecPL.validateSchema(rspecString));
	}
	


	@Test
	public void shouldFailFedericaRSpecNodeNoImplemetedBy() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		
				
		VirtualNode node = new VirtualNodeImpl("urn:publicid:IDN+novipl:novi+node+novilab.elte.hu");
		node.setImplementedBy(null);
		
		node.setVirtualRole("router");
		
		try {
			fedSchema.addNodeToRequestRSpec(node);
		} catch (Exception e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.RHBadInputException: Node urn:publicid:IDN+novipl:novi+node+novilab.elte.hu has implementedBy = null. Only bound request can arrive to FEDERICA.", 
					e.toString());
		}	

	}
	
	@Test
	public void shouldFailFedericaRSpecInterfaceNoImplemetedBy() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		fedSchema.setUser(user);
				
		VirtualNode node = new VirtualNodeImpl("vm");
		VirtualNode parentNode = new VirtualNodeImpl("urn:publicid:IDN+novipl:novi+node+novilab.elte.hu");
		parentNode.setHostname("urn:publicid:IDN+novipl:novi+node+novilab.elte.hu");
		Set<Node> parents = new HashSet<Node>();
		parents.add(parentNode);
		node.setImplementedBy(parents);
		
		Interface iface = new InterfaceImpl("if1");
		Set<Interface> interfaces = new HashSet<Interface>();
		interfaces.add(iface);		
		node.setHasOutboundInterfaces(interfaces);
		
		node.setVirtualRole("router");
		
		try {
			fedSchema.addNodeToRequestRSpec(node);
		} catch (Exception e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.RHBadInputException: Logical interface if1 doesn't have any physical interface." +
					"\n logicalInterface.getImplementedBy() returned null or empty",
					e.toString());
		}	
	}
	
	@Test
	public void shouldCreateFedericaRSpec() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		NOVIUserImpl user = new NOVIUserImpl("celia.ABC@test.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		fedSchema.setUser(user);
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		
				
		VirtualNode node = new VirtualNodeImpl("lr");
		VirtualNode parentNode = new VirtualNodeImpl("urn:publicid:IDN+novipl:novi+node+psnc.poz.router1");
		parentNode.setHostname("urn:publicid:IDN+novipl:novi+node+psnc.poz.router1");
		Set<Node> parents = new HashSet<Node>();
		parents.add(parentNode);
		node.setImplementedBy(parents);
		
		Interface parentInterface = new InterfaceImpl("interfaceParent");
		Set<Interface> parentInterfaces = new HashSet<Interface>();
		parentInterfaces.add(parentInterface);
		
		Interface iface = new InterfaceImpl("if1");
		iface.setImplementedBy(parentInterfaces);
		Set<IPAddress> ipv4Set = new HashSet<IPAddress>();
		IPAddress ip = new IPAddressImpl("1.1.1.1/24");
		ip.setHasValue("1.1.1.1/24");
		ipv4Set.add(ip);
		iface.setHasIPv4Address(ipv4Set);
		Set<Interface> interfaces = new HashSet<Interface>();
		interfaces.add(iface);		
		node.setHasOutboundInterfaces(interfaces);
		
		node.setVirtualRole("router");
		
		try {
			fedSchema.addNodeToRequestRSpec(node);
		} catch (Exception e) {
			assertTrue(false);
		}	
		
		assertTrue(fedSchema.validateSchema(fedSchema.toString()));
		assertTrue(fedSchema.toString().contains("address=\"1.1.1.1\""));
		System.out.println(fedSchema.toString());
	}
	
	@Test
	public void shouldCreateFedericaRSpecFromOwl() throws IOException, RHBadInputException {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		NOVIUserImpl user = new NOVIUserImpl("celia.ABC@test.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		fedSchema.setUser(user);
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		

		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"bound2NodesConstraints1router.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);	
		
		for (Resource resource : fedTopology.getContains()) {
			if (resource instanceof VirtualNode) {
				Iterator<Node> it = ((VirtualNode)resource).getImplementedBy().iterator();
				if (it.hasNext()) {
					NetworkElementOrNode nen = (Node) it.next();
					String componentID = RHUtils.removeNOVIURIprefix(nen.toString().toLowerCase());
					logService.log(LogService.LOG_INFO, "RH - Node in the topology: " + componentID);
					if (componentID.contains(SFAConstants.FEDERICA.toLowerCase())) {
						fedSchema.addNodeToRequestRSpec((VirtualNode)resource);
					}
				}
			}else if (resource instanceof VirtualLink){
				if(FedericaRSpec.isFedericaLink((VirtualLink)resource) && !fedSchema.isExistingVirtualLink((VirtualLink)resource)) {
					fedSchema.addLinkToRequestRSpec((VirtualLink)resource);
				}
			}
		}
		System.out.println(fedSchema.toString());

		assertTrue(fedSchema.toString().contains("<cc:compute_capacity cpuSpeed=\"2\" diskSize=\"10\" guestOS=\"otherLinuxGuest\" numCPUCores=\"1\" ramSize=\"2048\"/>"));
	
	}
	
	@Test
	public void shouldCreateFedericaRSpecPSNCnameChanged() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		NOVIUserImpl user = new NOVIUserImpl("celia.ABC@test.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		fedSchema.setUser(user);
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		
				
		VirtualNode node = new VirtualNodeImpl("lr");
		VirtualNode parentNode = new VirtualNodeImpl("urn:publicid:IDN+novipl:novi+node+router1.poz.pl");
		parentNode.setHostname("urn:publicid:IDN+novipl:novi+node+router1.poz.pl");
		Set<Node> parents = new HashSet<Node>();
		parents.add(parentNode);
		node.setImplementedBy(parents);
		
		Interface parentInterface = new InterfaceImpl("interfaceParent");
		Set<Interface> parentInterfaces = new HashSet<Interface>();
		parentInterfaces.add(parentInterface);
		
		Interface iface = new InterfaceImpl("if1");
		iface.setImplementedBy(parentInterfaces);
		Set<IPAddress> ipv4Set = new HashSet<IPAddress>();
		IPAddress ip = new IPAddressImpl("1.1.1.1");
		ip.setHasValue("1.1.1.1");
		ipv4Set.add(ip);
		iface.setHasIPv4Address(ipv4Set);
		Set<Interface> interfaces = new HashSet<Interface>();
		interfaces.add(iface);		
		node.setHasOutboundInterfaces(interfaces);
		
		node.setVirtualRole("router");
		
		try {
			fedSchema.addNodeToRequestRSpec(node);
		} catch (Exception e) {
			assertTrue(false);
		}	
		
		assertTrue(fedSchema.validateSchema(fedSchema.toString()));
	}
	
	@Test
	public void shouldReturnEmptyStringAsSliverType() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Element rspecNodeElement = fedSchema.doc.createElement(SFAConstants.NODE);
		String sliverType = fedSchema.getSliverType(rspecNodeElement);
		
		assert(sliverType == "");
	}
	
	@Test
	public void shouldReturnNoLogicalRouters() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Element rspecNodeElement = fedSchema.doc.createElement(SFAConstants.NODE);
		int noRouters = fedSchema.getHasAvailableLogicalRouters(rspecNodeElement);
		
		assert(noRouters == 0);
	}
	
	@Test
	public void shouldReturnNullCPU() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Node availableElement = fedSchema.doc.createElement(SFAConstants.NODE);
		
		CPU cpu = fedSchema.getCPU("componentName",availableElement);
		
		assert(cpu == null);
	}
	
	@Test
	public void shouldReturnNullMemory() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Node availableElement = fedSchema.doc.createElement(SFAConstants.NODE);
		
		Memory mem = fedSchema.getMemory("componentName",availableElement);
		
		assert(mem == null);		
	}
	
	@Test
	public void shouldReturnNullStorage() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Node availableElement = fedSchema.doc.createElement(SFAConstants.NODE);
		
		Storage storage = fedSchema.getStorage("componentName",availableElement);
		
		assert(storage == null);

	}
	
	@Test
	public void shouldReturnEmptyStringAsHardwareType() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Element rspecNodeElement = fedSchema.doc.createElement(SFAConstants.NODE);
		String hardwareType = fedSchema.getHardwareType(rspecNodeElement);
		
		assert(hardwareType == "");
	}
	
	@Test 
	public void shouldGetNoNodeComponentsForFederica() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		org.w3c.dom.Element someElement = fedSchema.doc.createElement(SFAConstants.NODE);
		//add some component
		Element someComponent = fedSchema.doc.createElement("testComponent");
		someComponent.setAttribute("toAdd", "false");
		
		someElement.appendChild(someComponent);
		
		Set <NodeComponent> nodeComponents = fedSchema.getNodeComponents("componentName", someElement);
		
		assert(nodeComponents.size() == 0);

	}
	
	@Test
	public void shouldGetNoLinksFromElementForFederica() {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		
		org.w3c.dom.Element linkElement = fedSchema.doc.createElement(SFAConstants.LINK);	
		linkElement.setAttribute(SFAConstants.COMPONENT_NAME, "componentName");
		linkElement.setAttribute(SFAConstants.COMPONENT_ID, "component_id");
		org.w3c.dom.Element capacityElement = fedSchema.doc.createElement(SFAConstants.FEDERICA_ADVERTISEMENT+":"+"linkAvailability");
		capacityElement.setAttribute("capacity", "1000");
		linkElement.appendChild(capacityElement);
		
		Element interfaceElement = fedSchema.doc.createElement(SFAConstants.INTERFACE);
		interfaceElement.setAttribute(SFAConstants.CLIENT_ID, "myInterface");
		//add just one interface to the link
		linkElement.appendChild(interfaceElement);
		
		Set<Link> links = fedSchema.getLinksFromElement(linkElement, new HashSet<Node>());
		
		assert(links.size() == 0);
	}
	
	@Test
	public void shouldNotValidateRSpecSchema(){
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);		
		
		boolean valid = fedSchema.validateSchema("someInvalidRSpec");
		
		assert(valid == false);
	}

	@Test
	public void testIsFedericaLink() throws IOException, RHBadInputException {
		FedericaRSpec fedSchema = new FedericaRSpec();
		
		NOVIUserImpl user = new NOVIUserImpl("celia.ABC@test.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		fedSchema.setUser(user);
		
		//create empty rspec
		fedSchema.createEmptyRequestRSpec();
		
		LogService logService = mock(LogService.class);
		fedSchema.setLogService(logService);	
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
				boolean isFedericaLink = fedSchema.isFedericaLink((VirtualLink) resource);
				assertTrue(isFedericaLink);
			}
		}
	}
}
