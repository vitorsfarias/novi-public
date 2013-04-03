package eu.novi.requesthandler.sfa.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.log.LogService;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Node;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.nswitch.manager.NswitchManager;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponse;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;

public class SFAFederatedTestbedTest {
	String sep = System.getProperty("file.separator");

	@Test
	public void testDeleteSliceShouldFailBecausePlatformListIsEmpty() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);		

		when(userFeedback.getCurrentSessionID()).thenReturn("1223");
		
		Set<String> platformUriList = new HashSet<String>();
		RHCreateDeleteSliceResponseImpl r = sfaf.deleteSlice(null, "slice.test", platformUriList, null);
	
		assertTrue(r.hasError());
		assertNotNull(r.getErrorMessage());
		assertEquals("Error deleting the slice slice.test: there are no platforms specified in the call. We can't delete slice if we don´t know which platform(s) it pertains to.", r.getErrorMessage());
	}
	
	@Test
	public void testPrepareRH(){
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHImpl rh = sfaf.prepareRH();
		assertNotNull(rh);
		assertNotNull(rh.getSfaActions());
		assertNotNull(rh.getNswitchManager());
		assertNotNull(rh.getLogService());
		assertNotNull(rh.getTestbed());
		assertEquals("Federica", rh.getTestbed());
		assertNotNull(rh.getWaitingTime());
		assertEquals("120", rh.getWaitingTime());
		assertNotNull(rh.getUserFeedback());
	}
	
	@Test
	public void testListResponseWithErrors() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHListResourcesResponseImpl response = sfaf.listResponseWithError("mock error");
		assertNotNull(response);
		assertTrue(response.hasError());
		assertEquals("mock error", response.getErrorMessage());
	}
	
	@Test
	public void testGetUserFeedback() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		ReportEvent uf = sfaf.getReportUserFeedback();
		assertNotNull(uf);
	}
	
	@Test
	public void testCreateUnknownError() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHCreateDeleteSliceResponseImpl response = sfaf.createUnknownErrorResponse("test slice");
		assertNotNull(response);
		assertTrue(response.hasError());
		assertEquals("Not known error: method returned null", response.getErrorMessage());
		assertEquals("test slice", response.getSliceID());
	}
	
	@Test
	public void testSetGetWaitingTime() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		String wt = sfaf.getWaitingTime();
		assertNotNull(wt);
		assertEquals("120", wt);
	}
	
	@Test
	public void testSetGetLogService() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		LogService ls = sfaf.getLogService();
		assertNotNull(ls);
		assertEquals(logService, ls);
	}
	
	@Test
	public void testSetGetNSwitchManager() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		NswitchManager nswitch = sfaf.getNswitchManager();
		assertNotNull(nswitch);
		assertEquals(ns, nswitch);
	}
	
	@Test
	public void testSetGetTestbed() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		String testbed = sfaf.getTestbed();
		assertNotNull(testbed);
		assertEquals("Federica", testbed);
	}
	
	@Test
	public void listUserSlicesNullUser() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHListSlicesResponseImpl r = sfaf.listUserSlices(null);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("User can't be null or empty.", r.getErrorMessage());
	}
	
	@Test
	public void listUserSlicesEmptyUser() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHListSlicesResponseImpl r = sfaf.listUserSlices("");
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("User can't be null or empty.", r.getErrorMessage());
	}
	
	@Test
	public void updateSliceNullUser() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		TopologyImpl topology = new TopologyImpl("test");
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, null, "testSlice", topology, topology);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user null sliceName testSlice topology http://fp7-novi.eu/im.owl#test",
				r.getErrorMessage());
		assertEquals("testSlice", r.getSliceID());
	}
	
	@Test
	public void updateSliceEmptySliceName() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		TopologyImpl topology = new TopologyImpl("test");
		NOVIUserImpl user = new NOVIUserImpl("user");
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, user, "", topology, topology);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user http://fp7-novi.eu/NOVIPolicyService.owl#user sliceName  topology http://fp7-novi.eu/im.owl#test",
				r.getErrorMessage());
		assertEquals("", r.getSliceID());
	}
	
	@Test
	public void updateSliceEmptySliceNameNullTopology() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		NOVIUserImpl user = new NOVIUserImpl("user");
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, user, "", null, null);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user http://fp7-novi.eu/NOVIPolicyService.owl#user sliceName  topology null",
				r.getErrorMessage());
		assertEquals("", r.getSliceID());
	}
	
	@Test
	public void updateSliceNullTopology() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		NOVIUserImpl user = new NOVIUserImpl("user");
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, user, "testSlice", null, null);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user http://fp7-novi.eu/NOVIPolicyService.owl#user sliceName testSlice topology null",
				r.getErrorMessage());
		assertEquals("testSlice", r.getSliceID());
	}	
	
	@Test
	public void updateSliceNullUserAndTopology() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, null, "testSlice", null, null);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user null sliceName testSlice topology null",
				r.getErrorMessage());
		assertEquals("testSlice", r.getSliceID());
	}
	
	@Test
	public void updateSliceEmptySliceNameNullUser() {
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);	
		sfaf.setWaitingTime("120");
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		TopologyImpl topology = new TopologyImpl("test");
		
		RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, null, "", topology, topology);
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("Some of the inputs for updating slice are invalid. Received: user null sliceName  topology http://fp7-novi.eu/im.owl#test",
				r.getErrorMessage());
		assertEquals("", r.getSliceID());
	}
	
	@Test
	@Ignore
	public void testListPlanetLabResources() {
		Date d = new Date();
		System.out.println("Starting list PlanetLab resources test: " + d);

		LogService logService = mock(LogService.class);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("PlanetLab");
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		RHListResourcesResponse r = sfaf.listResources("");

		if(r.hasError()) {
			System.out.println(r.getErrorMessage());
		}
		assertFalse(r.hasError());  
		assertNotNull(r.getPlatformString());
	
	}

	@Test
	@Ignore
	public void testListFedericaResources() {
		Date d = new Date();
		System.out.println("Starting list federica resources test: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		RHListResourcesResponse r = sfaf.listResources("");
		
		if(r.hasError()) {
			System.out.println(r.getErrorMessage());
		}
		assertFalse(r.hasError());
		assertNotNull(r.getPlatformString());
				
		long difference = (new Date()).getTime() - d.getTime();
		System.out.println("End of list federica resources test. It took: " + difference);
	}
	
	
	@Test
	@Ignore
	public void testCreateSlice() {
		Date d = new Date();
		System.out.println("Starting createSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NswitchManager mockNswitch = mock(NswitchManager.class);
		sfaf.setNswitchManager(mockNswitch);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA8xAK/Xjdpfy1Ic32b3eHl9K3djMpZ9Uff7berl8KqYEv0i1xTOdMpL9qDi80dwtcxlZXH7ChlYaDBZuLQ0v6k2l2pKr4juvcbkZevPttHRhCAZ6+x8zvG4DoDgVn6eR6uypgL1//HNGaZI3y7OSZB8lUQ5DBbf/jlxazLoT6mfzDT3Iff5dYOBLPr8GryAQmtyo3zAFEuLoft3DUc/7scsPslZpPXq7F8w86tO2w3fridCif6zluZiua6JOxSHBc1JDGUJtfIIDHZjxCnMa/L9Kuzbh5K1NDt02pGraceUgrfRl1LGqy6MNocm1+dgSuSMFsLxGD8+A+UfPrlrqnow== root@federica");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		String stringOwl;
		try {
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"bound2NodesConstraints1router.owl");
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			TopologyImpl topology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);

			RHCreateDeleteSliceResponseImpl r = sfaf.createSlice(null, user, "celia_27032013", topology);
			
			long difference = (new Date()).getTime() - d.getTime();
			System.out.println("End of createSlice test. It took: " + difference);		
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());
			assertNotNull(r.getSliceID());

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	@Ignore
	public void testUpdateSlice() {
		Date d = new Date();
		System.out.println("Starting createSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NswitchManager mockNswitch = mock(NswitchManager.class);
		sfaf.setNswitchManager(mockNswitch);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA8xAK/Xjdpfy1Ic32b3eHl9K3djMpZ9Uff7berl8KqYEv0i1xTOdMpL9qDi80dwtcxlZXH7ChlYaDBZuLQ0v6k2l2pKr4juvcbkZevPttHRhCAZ6+x8zvG4DoDgVn6eR6uypgL1//HNGaZI3y7OSZB8lUQ5DBbf/jlxazLoT6mfzDT3Iff5dYOBLPr8GryAQmtyo3zAFEuLoft3DUc/7scsPslZpPXq7F8w86tO2w3fridCif6zluZiua6JOxSHBc1JDGUJtfIIDHZjxCnMa/L9Kuzbh5K1NDt02pGraceUgrfRl1LGqy6MNocm1+dgSuSMFsLxGD8+A+UfPrlrqnow== root@federica");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		String stringOwl;
		try {
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"bound2NodesConstraints1router.owl");
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			TopologyImpl oldTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"ChrysaExample.owl");
			TopologyImpl newTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);

			RHCreateDeleteSliceResponseImpl r = sfaf.updateSlice(null, user, "celia_27032013", oldTopology, newTopology);
			
			long difference = (new Date()).getTime() - d.getTime();
			System.out.println("End of createSlice test. It took: " + difference);		
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());
			assertNotNull(r.getSliceID());

		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	@Ignore
	public void testCreateSlicePlanetLab() {
		Date d = new Date();
		System.out.println("Starting createSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NswitchManager mockNswitch = mock(NswitchManager.class);
		sfaf.setNswitchManager(mockNswitch);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA8xAK/Xjdpfy1Ic32b3eHl9K3djMpZ9Uff7berl8KqYEv0i1xTOdMpL9qDi80dwtcxlZXH7ChlYaDBZuLQ0v6k2l2pKr4juvcbkZevPttHRhCAZ6+x8zvG4DoDgVn6eR6uypgL1//HNGaZI3y7OSZB8lUQ5DBbf/jlxazLoT6mfzDT3Iff5dYOBLPr8GryAQmtyo3zAFEuLoft3DUc/7scsPslZpPXq7F8w86tO2w3fridCif6zluZiua6JOxSHBc1JDGUJtfIIDHZjxCnMa/L9Kuzbh5K1NDt02pGraceUgrfRl1LGqy6MNocm1+dgSuSMFsLxGD8+A+UfPrlrqnow== root@federica");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		TopologyImpl plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("planetlab1-novi.lab.netmode.ece.ntua.gr");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);


		RHCreateDeleteSliceResponseImpl r = sfaf.createSlice(null, user, "CeliaVL1131", plTopology);

		long difference = (new Date()).getTime() - d.getTime();
		System.out.println("End of createSlice test. It took: " + difference);

		if (r.hasError() == true) {
			System.out.println(r.getErrorMessage());
		}
		assertFalse(r.hasError());
		assertNotNull(r.getSliceID());


	}


	@Test
	@Ignore
	public void testDeleteSlices() {
		Date d = new Date();
		System.out.println("Starting createSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NswitchManager mockNswitch = mock(NswitchManager.class);
		sfaf.setNswitchManager(mockNswitch);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA8xAK/Xjdpfy1Ic32b3eHl9K3djMpZ9Uff7berl8KqYEv0i1xTOdMpL9qDi80dwtcxlZXH7ChlYaDBZuLQ0v6k2l2pKr4juvcbkZevPttHRhCAZ6+x8zvG4DoDgVn6eR6uypgL1//HNGaZI3y7OSZB8lUQ5DBbf/jlxazLoT6mfzDT3Iff5dYOBLPr8GryAQmtyo3zAFEuLoft3DUc/7scsPslZpPXq7F8w86tO2w3fridCif6zluZiua6JOxSHBc1JDGUJtfIIDHZjxCnMa/L9Kuzbh5K1NDt02pGraceUgrfRl1LGqy6MNocm1+dgSuSMFsLxGD8+A+UfPrlrqnow== root@federica");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("PlanetLab");
			platformURIs.add("Federica");
			RHCreateDeleteSliceResponseImpl r = sfaf.deleteSlice(null, "CeliaVL1131", platformURIs, null);
			boolean error = r.hasError();  
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());

					
			long difference = (new Date()).getTime() - d.getTime();
			System.out.println("End of deleteSlice test. It took: " + difference);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	@Ignore
	public void testListSlices() {
		Date d = new Date();
		System.out.println("Starting ListSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {

			RHListSlicesResponseImpl r = sfaf.listUserSlices("celia_velayos");
			boolean error = r.hasError();  
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());
			System.out.println(r.getSlices());
					
			long difference = (new Date()).getTime() - d.getTime();
			System.out.println("End of list slices test. It took: " + difference);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	@Ignore
	public void testListAllSlices() {
		Date d = new Date();
		System.out.println("Starting ListSlice: " + d);
		SFAFederatedTestbedImpl sfaf = new SFAFederatedTestbedImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setReportUserFeedback(userFeedback);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {

			RHListSlicesResponseImpl r = sfaf.listAllSlices();
			boolean error = r.hasError();  
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());
			System.out.println(r.getSlices());
					
			long difference = (new Date()).getTime() - d.getTime();
			System.out.println("End of list slices test. It took: " + difference);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

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
}
