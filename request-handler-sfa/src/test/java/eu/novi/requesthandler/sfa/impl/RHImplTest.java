/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.impl;



import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doNothing;

import org.junit.Test;
import org.junit.Ignore;
import org.osgi.service.log.LogService;


import eu.novi.nswitch.manager.NswitchManager;

import eu.novi.requesthandler.sfa.SFAActions;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.clients.XMLRPCClient;
import eu.novi.requesthandler.sfa.exceptions.RHBadInputException;
import eu.novi.requesthandler.sfa.impl.RHImpl;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponse;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;
import eu.novi.requesthandler.sfa.rspecs.FedericaRSpec;
import eu.novi.requesthandler.sfa.rspecs.RSpecSchema;


import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of the API offered to the rest of NOVI services by the RH.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 *
 */
public class RHImplTest {
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
	public void shouldReturnNullLogService(){
		RHImpl sfaf = new RHImpl();
		
		LogService ls = sfaf.getLogService();
		assertNull(ls);
	}
	
	@Test
	public void shouldReturnLogService(){
		RHImpl sfaf = new RHImpl();
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		LogService ls = sfaf.getLogService();
		assertNotNull(ls);
	}
	
	@Test
	public void shouldReturnNullSFAActions(){
		RHImpl sfaf = new RHImpl();
		
		SFAActions sfaa = sfaf.getSfaActions();
		assertNull(sfaa);
	}
	
	@Test
	public void shouldReturnSFAActions(){
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		
		SFAActions sfaa = sfaf.getSfaActions();
		assertNotNull(sfaa);
	}
	
	public void shouldReturnTestbed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("testbed");
		String testbed = sfaf.getTestbed();
		
		assertNotNull(testbed);
		assertTrue(testbed.equals("testbed"));
			
	}
		
	@Test
	public void shouldReturnNullErrorInRegistryInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab:smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn(null);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("RH - Error creating PlanetLab slice: java.lang.NullPointerException"));
	
	}

	@Test
	public void shouldReturnNullPointerExceptionInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("RH - Error creating PlanetLab slice: java.lang.NullPointerException"));
	}
	
	@Test
	public void shouldReturnErrorCreateSlicePLInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("RH - Error creating PlanetLab slice: java.lang.NullPointerException"));
	}
	
	@Test
	public void shouldCatchGetSelfCredentialInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			Exception e = new Exception("Exception getting self credential PL");
			when(mockActions.getSelfCredentialPL()).thenThrow(e);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertEquals("RH - Error creating PlanetLab slice: java.lang.NullPointerException", response.getErrorMessage());
	}
	
	@Test
	public void shouldCatchGetCredentialInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			Exception e = new Exception("Exception getting credential PL");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenThrow(e);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertEquals("RH - Error creating PlanetLab slice: java.lang.NullPointerException", response.getErrorMessage());
	}
	
	@Test
	public void shouldReturnErrorResultWhenListSliceResourcesInCreateSlicePL() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 1);
			mockResult.put("code", mockMapCode);
			mockResult.put("output", "We are mocking the result of listSliceResources");
			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("RH - PlanetLab RSpec created with ERROR: ERROR: We are mocking the result of listSliceResources"));
	}
	
	@Test
	public void shouldCatchNullPointerExceptionWhenPopulatingSliceInCreateSlicePL() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);
			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("RH - Error creating PlanetLab slice: java.lang.NullPointerException"));
	}
	
	@Test
	public void shouldGetErrorWhenPopulatingSliceInCreateSlicePL() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 1);
			mockResultPopulate.put("code", mockMapCodePopulate);
			mockResultPopulate.put("output", "We are mocking the error result of populate slice");
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertTrue(response.getErrorMessage().equals("We are mocking the error result of populate slice"));
	}

	
	@Test
	public void shouldWorkCreateSlicePL() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			mockResultPopulate.put("value", manifest);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertFalse(response.hasError());
		assertNotNull(response.getSliceID());
		assertTrue(response.getSliceID().equals("testRH05042012"));
		assertEquals(1, response.getListOfTestbedsWhereSliceIsCreated().size());
		assertEquals("http://fp7-novi.eu/im.owl#PlanetLab", response.getListOfTestbedsWhereSliceIsCreated().get(0));

	}
	
	@Test
	public void shouldWorkCreateSlicePLFixedVLAN1() {
		plTopology = new TopologyImpl("requestTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			mockResultPopulate.put("value", manifest);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertFalse(response.hasError());
		assertNotNull(response.getSliceID());
		assertTrue(response.getSliceID().equals("testRH05042012"));
		
	}
	
	@Test
	public void shouldWorkCreateSlicePLFixedVLAN2() {
		plTopology = new TopologyImpl("requestTopology2");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			mockResultPopulate.put("value", manifest);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertFalse(response.hasError());
		assertNotNull(response.getSliceID());
		assertTrue(response.getSliceID().equals("testRH05042012"));
		
	}
	
	@Test
	public void shouldNotCreateSlicePL2() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("OpenLab+smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getSliceID());
		assertTrue(response.getSliceID().equals("testRH05042012"));
		assertEquals("There were no Nodes to add in PlanetLab, neither FEDERICA", response.getErrorMessage());
		
	}
	
	@Test
	public void shouldNotCreateSlicePL() {
		plTopology = new TopologyImpl("testTopology");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getSliceID());
		assertTrue(response.getSliceID().equals("testRH05042012"));
		assertEquals("There were no Nodes to add in PlanetLab, neither FEDERICA", response.getErrorMessage());
		
	}

	
	@Test
	public void shouldCatchErrorAddRecordInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			
			Exception e = new Exception("Exception adding record");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
						.thenThrow(e);			

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error creating Federica slice record: java.lang.Exception: Exception adding record", response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldCatchErrorGetSelfCredentialFedInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			Exception e = new Exception("Exception getting selfCredential SFA");
			when(mockActions.getSelfCredentialFed())
						.thenThrow(e);			
			
			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error creating Federica slice record: java.lang.Exception: Exception getting selfCredential SFA",
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchErrorGetAuthorityCredentialInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			
			Exception e = new Exception("Exception getting authority credential");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
						.thenThrow(e);			

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error creating Federica slice record: java.lang.Exception: Exception getting authority credential",
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchErrorGetSliceCredentialInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			Exception e = new Exception("Exception getting slice credential");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
						.thenThrow(e);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error populating Federica slice: java.lang.Exception: Exception getting slice credential", response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void shouldCatchNullPointExceptionGetSliceCredentialInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenThrow(new NullPointerException());
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);
			
			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error populating Federica slice: java.lang.NullPointerException", response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldGetErrorPopulateSliceInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.setLogService(logService);
		r.createEmptyRequestRSpec();
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenReturn("");
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 1);
			mockResult.put("code", mockMapCode);
			mockResult.put("output", "We are mocking the result of populate federica slice");

			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);


			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: We are mocking the result of populate federica slice", 
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void shouldCatchNullPointExceptionPopulateSliceInCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenReturn("");
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(null);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);
			
			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error populating Federica slice: java.lang.NullPointerException", 
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void checkFedRSpec() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		user.setFirstName("h");
		user.setLastName("v");
		Set<String> pk = new HashSet<String>();
		pk.add("pkpkpk");
		user.setPublicKeys(pk);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
	
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("ada");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("fegd");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("gerdfe");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			mockResult.put("value", readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt"));

			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkCreateSliceFed() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenReturn("");
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			assertEquals(1, response.getListOfTestbedsWhereSliceIsCreated().size());
			assertEquals("http://fp7-novi.eu/im.owl#Federica", response.getListOfTestbedsWhereSliceIsCreated().get(0));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Test
	public void shouldWorkCreateSliceMix() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));			
			assertEquals(2, response.getListOfTestbedsWhereSliceIsCreated().size());
			assertEquals("http://fp7-novi.eu/im.owl#Federica", response.getListOfTestbedsWhereSliceIsCreated().get(0));
			assertEquals("http://fp7-novi.eu/im.owl#PlanetLab", response.getListOfTestbedsWhereSliceIsCreated().get(1));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Test
	public void shouldFailEmptyVLAN() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultiLinkInterdomain.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Resource> setContains = new HashSet<Resource>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add(resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifestEmptyVLAN.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.removeSliceResources(anyString(), anyString())).thenReturn(1);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			assertEquals("RH - Error with slice VLAN: there is no VLAN set by FED", response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldReturnErrorNoNodesInCreateSlice() {
		plTopology = new TopologyImpl("testTopology");
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);		
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertEquals("RH - Error in the topology: eu.novi.requesthandler.sfa.exceptions.RHBadInputException: Topology has no resources to create", response.getErrorMessage());
	
	}

	@Test
	public void shouldReturnErrorNoNodesInCreateSlice2() {
		plTopology = new TopologyImpl("testTopology");
		Set<Node> setContains = new HashSet<Node>();
		plTopology.setContains(setContains);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);		
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", plTopology);
		assertNotNull(response);
		assertTrue(response.hasError());
		assertNotNull(response.getErrorMessage());
		assertEquals("RH - Error in the topology: eu.novi.requesthandler.sfa.exceptions.RHBadInputException: Topology has no resources to create", response.getErrorMessage());
	
	}
		
	@Test
	public void shouldWorkCreateSliceFed2Routers() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"2Routers.owl");
			
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
						
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenReturn("");
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", fedTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	
	@Test
	public void shouldReturnErrorNullTestbedListResources() {
		RHImpl sfaf = new RHImpl();
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		when(userFeedback.getCurrentSessionID()).thenReturn("1223");
		
		RHListResourcesResponse r = sfaf.listResources("");
		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("RH - Error getting selfcredential null for listing resources: eu.novi.requesthandler.sfa.exceptions.RHBadInputException: The paramater testbed doesn't match with the testbeds RH has. Testbed has the following value: null", 
				r.getErrorMessage());
		
	}

	@Test
	public void shouldCatchNullExceptionTestbedListResourcesPlanetLab() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn(null);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertTrue(r.getErrorMessage().equals("RH - Error listing PlanetLab resources: testbed returned null"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldCatchGetSelfCredentialExceptionTestbedListResourcesPlanetLab() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			Exception e = new Exception("Exception getting self credential");
			when(mockActions.getSelfCredentialPL()).thenThrow(e);
			
			
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error getting selfcredential PlanetLab for listing resources: java.lang.Exception: Exception getting self credential", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchListPLResourcesExceptionTestbedListResourcesPlanetLab2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("selfCredentialPL");
			Exception e = new Exception("Exception listing PlanetLab resources");
			when(mockActions.listResources(anyString())).thenThrow(e);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing PlanetLab resources. java.lang.Exception: Exception listing PlanetLab resources", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNullExceptionTestbedListResourcesPlanetLab2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.listResources(anyString())).thenReturn(null);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertTrue(r.getErrorMessage().equals("RH - Error listing PlanetLab resources: testbed returned null"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNullExceptionMapCodeListResourcesPlanetLab() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> planetLabResourcesMap = new HashMap<String, Object>();			
			when(mockActions.listResources(anyString())).thenReturn(planetLabResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing PlanetLab resources: eu.novi.requesthandler.sfa.exceptions.TestbedException: Testbed returned a not valid response.", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNullExceptionMapCodeListResourcesPlanetLab2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> planetLabResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			planetLabResourcesMap.put("code", mapCode);
			when(mockActions.listResources(anyString())).thenReturn(planetLabResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing PlanetLab resources: eu.novi.requesthandler.sfa.exceptions.TestbedException: Testbed returned a not valid response.", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	@Test
	public void shouldGetSFAErrorListResourcesPlanetLab2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> planetLabResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			mapCode.put("geni_code", 1);
			planetLabResourcesMap.put("code", mapCode);
			planetLabResourcesMap.put("output", "We assume there was error in SFA");
			when(mockActions.listResources(anyString())).thenReturn(planetLabResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertTrue(r.getErrorMessage().equals("RH - Error listing PlanetLab resources: We assume there was error in SFA"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkListResourcesPlanetLab2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> planetLabResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");

			mapCode.put("geni_code", 0);
			planetLabResourcesMap.put("code", mapCode);
			planetLabResourcesMap.put("value", rspec);

			when(mockActions.listResources(anyString())).thenReturn(planetLabResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertFalse(r.hasError());
			assertNotNull(r.getPlatformString());
			assertTrue(r.getPlatformString() instanceof String);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldReturnErrorWrongTestbedListResources() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("whatever");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		when(userFeedback.getCurrentSessionID()).thenReturn("1223");
	
		RHListResourcesResponse r = sfaf.listResources("");

		assertNotNull(r);
		assertTrue(r.hasError());
		assertEquals("RH - Error getting selfcredential whatever for listing resources: eu.novi.requesthandler.sfa.exceptions.RHBadInputException: The paramater testbed doesn't match with the testbeds RH has. Testbed has the following value: whatever",
				r.getErrorMessage());
		
	}
	
	@Test
	public void shouldCatchNullPointerExceptionListResourcesFederica() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialFed()).thenReturn(null);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertTrue(r.getErrorMessage().equals("RH - Error listing federica resources: testbed returned null"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void shouldCatchNullPointerExceptionListResourcesFederica2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.listResources(anyString())).thenReturn(null);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertTrue(r.getErrorMessage().equals("RH - Error listing federica resources: testbed returned null"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void shouldCatchNullExceptionMapCodeListResourcesFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> fedResourcesMap = new HashMap<String, Object>();			
			when(mockActions.listResources(anyString())).thenReturn(fedResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing Federica resources: eu.novi.requesthandler.sfa.exceptions.TestbedException: Testbed returned a not valid response.", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNullExceptionMapCodeListResourcesFed2() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> fedResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			fedResourcesMap.put("code", mapCode);
			when(mockActions.listResources(anyString())).thenReturn(fedResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing Federica resources: eu.novi.requesthandler.sfa.exceptions.TestbedException: Testbed returned a not valid response.", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
		
	@Test
	public void shouldGetSFAErrorListResourcesFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> fedResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			mapCode.put("geni_code", 1);
			fedResourcesMap.put("code", mapCode);
			fedResourcesMap.put("output", "We assume there was error in SFA");
			when(mockActions.listResources(anyString())).thenReturn(fedResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error listing Federica resources: We assume there was error in SFA", r.getErrorMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkListResourceFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			HashMap<String, Object> fedResourcesMap = new HashMap<String, Object>();
			HashMap<String, Object> mapCode = new HashMap<String, Object>();
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"AdvRSpecFed.xml");

			mapCode.put("geni_code", 0);
			fedResourcesMap.put("code", mapCode);
			fedResourcesMap.put("value", rspec);

			when(mockActions.listResources(anyString())).thenReturn(fedResourcesMap);
				
			RHListResourcesResponse r = sfaf.listResources("");
			
			assertNotNull(r);
			assertFalse(r.hasError());
			assertNotNull(r.getPlatformString());
			assertTrue(r.getPlatformString() instanceof String);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNullPointerExceptionDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn(null);
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			platformURIs.add("http://fp7-novi.eu/im.owl#federica");
			Set<Resource> resources = new HashSet<Resource>();
			TopologyImpl t = new TopologyImpl();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"", platformURIs, t);
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error deleting slice: java.lang.NullPointerException", r.getErrorMessage());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchPlanetLabExceptionDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			Exception e = new Exception("Exception listing PlanetLab resources");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
				.thenThrow(e);
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			TopologyImpl t = new TopologyImpl();
			Set<Resource> resources = new HashSet<Resource>();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"", platformURIs, t);
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("RH - Error deleting slice: java.lang.Exception: Exception listing PlanetLab resources", r.getErrorMessage());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldFailFedericaDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialStringPL");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialStringFed");
			when(mockActions.getCredential(contains("credentialStringPL"), anyString(), anyString()))
				.thenReturn("PL");
			when(mockActions.getCredential(contains("credentialStringFed"), anyString(), anyString()))
			.thenReturn("Fed");
			when(mockActions.removeSlice(anyString(), contains("PL"))).thenReturn(1);
			when(mockActions.removeSlice(anyString(), contains("Fed"))).thenReturn(2);
			
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#federica");
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			Set<Resource> resources = new HashSet<Resource>();
			TopologyImpl t = new TopologyImpl();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"aaa", platformURIs, t);
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("FEDERICA response: 2\n PlanetLab response: 1", r.getErrorMessage());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldFailPlanetLabDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialStringPL");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialStringFed");
			when(mockActions.getCredential(contains("credentialStringPL"), anyString(), anyString()))
				.thenReturn("PL");
			when(mockActions.getCredential(contains("credentialStringFed"), anyString(), anyString()))
			.thenReturn("Fed");
			when(mockActions.removeSlice(anyString(), contains("PL"))).thenReturn(2);
			when(mockActions.removeSlice(anyString(), contains("Fed"))).thenReturn(1);
			
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			platformURIs.add("http://fp7-novi.eu/im.owl#federica");
			TopologyImpl t = new TopologyImpl();
			Set<Resource> resources = new HashSet<Resource>();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"", platformURIs, t);
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("FEDERICA response: 1\n PlanetLab response: 2", r.getErrorMessage());			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldFailPlanetLabAndFedericaDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialStringPL");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialStringFed");
			when(mockActions.getCredential(contains("credentialStringPL"), anyString(), anyString()))
				.thenReturn("PL");
			when(mockActions.getCredential(contains("credentialStringFed"), anyString(), anyString()))
			.thenReturn("Fed");
			when(mockActions.removeSlice(anyString(), contains("PL"))).thenReturn(2);
			when(mockActions.removeSlice(anyString(), contains("Fed"))).thenReturn(2);
			
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#federica");
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			Set<Resource> resources = new HashSet<Resource>();
			TopologyImpl t = new TopologyImpl();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"", platformURIs, t);
			assertNotNull(r);
			assertTrue(r.hasError());
			assertEquals("FEDERICA response: 2\n PlanetLab response: 2", r.getErrorMessage());		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkPlanetLabAndFedericaDeleteSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialStringPL");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialStringFed");
			when(mockActions.getCredential(contains("credentialStringPL"), anyString(), anyString()))
				.thenReturn("PL");
			when(mockActions.getCredential(contains("credentialStringFed"), anyString(), anyString()))
			.thenReturn("Fed");
			when(mockActions.removeSlice(anyString(), contains("PL"))).thenReturn(1);
			when(mockActions.removeSlice(anyString(), contains("Fed"))).thenReturn(1);
			
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("http://fp7-novi.eu/im.owl#planetlab");
			platformURIs.add("http://fp7-novi.eu/im.owl#federica");
			Set<Resource> resources = new HashSet<Resource>();
			TopologyImpl t = new TopologyImpl();
			t.setContains(resources);
			RHCreateDeleteSliceResponse r = sfaf.releaseFederationAndDeleteSlice(null,"unitTestSlice", platformURIs, t);
			assertNotNull(r);
			assertFalse(r.hasError());
			assertTrue(r.getSliceID().equals("unitTestSlice"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkUpdateSliceNoNodes() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("OpenLab+smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test", 
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			
			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", plTopology, plTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
	}
	
	@Test
	public void updateSliceEmtpyTopology() {
		plTopology = new TopologyImpl("testTopology");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 0);
			mockResultPopulate.put("code", mockMapCodePopulate);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);		
			TopologyImpl newTopology = new TopologyImpl("newTopology");
			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", plTopology, newTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertEquals("testRH05042012", response.getSliceID());
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	@Test
	public void shouldGetErrorWhenPopulatingSliceInUpdateSlice() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		// mock sfaactions
		try {
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResult.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResult);
			
			Map<String, Object> mockResultPopulate = new HashMap<String, Object>();
			Map<String, Object> mockMapCodePopulate = new HashMap<String, Object>();
			mockMapCodePopulate.put("geni_code", 1);
			mockResultPopulate.put("code", mockMapCodePopulate);
			mockResultPopulate.put("output", "We are mocking the error result of populate slice");
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResultPopulate);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);

			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", plTopology, plTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertTrue(response.getErrorMessage().equals("We are mocking the error result of populate slice"));			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldGetErrorPopulateSliceInUpdateSlice() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getCredential(anyString(), anyString(), contains("slice")))
					.thenReturn("");
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 1);
			mockResult.put("code", mockMapCode);
			mockResult.put("output", "We are mocking the result of populate federica slice");

			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);
			

			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", fedTopology, fedTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: We are mocking the result of populate federica slice", 
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void shouldCatchFedericaPopulateExceptionInUpdateSlice() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);

		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);

			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			Exception e = new Exception("Exception populating Federica slice");
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenThrow(e);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", mixTopology, mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating FEDERICA slice: RH - Error populating Federica slice: java.lang.Exception: Exception populating Federica slice", 
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Test
	public void shouldCatchPlanetLabPopulateExceptionInUpdateSlice() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);

			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			Exception e = new Exception("Exception populating PlanetLab slice");
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenThrow(e);

			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", mixTopology, mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getErrorMessage());
			assertEquals("RH - Error creating PlanetLab slice: java.lang.Exception: Exception populating PlanetLab slice", 
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkUpdateSlice() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		sfaf.setWaitingTime("20");
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.updateSlice(null, user, "testRH05042012", mixTopology, mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			assertEquals(2, response.getListOfTestbedsWhereSliceIsCreated().size());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldCatchNSwitchException() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		sfaf.setWaitingTime("20");
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);

		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultiLinkInterdomain.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Resource> setContains = new HashSet<Resource>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add(resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);	
			Exception e = new Exception("nswitch exception");
			// TODO: throw exception in nswitch
			doThrow(e).when(ns).createFederation((TopologyImpl) anyObject(), anyString());
		
			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));	
			assertTrue(response.getErrorMessage().startsWith("Error federating slices with nswtich Mock for NswitchManager"));
		} catch (Exception e) {
			assertEquals("java.lang.Exception: nswitch exception", e.toString());
		}
	}
	
	
	@Test
	public void shouldFailEmptyVLANAndExceptionDeletingSlice() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifestEmptyVLAN.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			
			Exception e = new Exception("Exception removing slice");
			when(mockActions.removeSlice(anyString(), anyString())).thenThrow(e);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			assertEquals("RH - Error with slice VLAN: there is no VLAN set by FED"
					+ "\nRH - Error deleting PlanetLab slice: java.lang.Exception: Exception removing slice",
					response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkCreateSliceMixFixPartiallyBoundRequest() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("fixPBoundRequest");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertEquals("testRH05042012", response.getSliceID());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldWorkCreateSliceMixFixBoundRequest() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("fixBoundRequest");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldFailSleepThreadCreateSliceMix() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("abc");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldDeletePlanetLabSliceWhenFedericaFails() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
			fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			TopologyImpl mixTopology = new TopologyImpl("mixTopology");

			Node parent = new NodeImpl("PlanetLab_smilax1.man.poznan.pl");
			VirtualNode vm = new VirtualNodeImpl("vm");
			Set<Node> setNodes = new HashSet<Node>();
			Set<Node> setContains = new HashSet<Node>();
			setNodes.add(parent);
			Set<Resource> federicaSetContains = fedTopology.getContains();
			for (Resource resource : federicaSetContains) {
				setContains.add((Node)resource);
			}
			
			vm.setImplementedBy(setNodes);
			setContains.add(vm);
			mixTopology.setContains(setContains);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
				.thenReturn(null);
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.removeSlice(anyString(), anyString())).thenReturn(1);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertTrue(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			assertEquals("RH - Error creating FEDERICA slice: RH - Error populating Federica slice: java.lang.NullPointerException", response.getErrorMessage());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void shouldWorkComplete() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
		
			TopologyImpl mixTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifest.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertTrue(response.getSliceID().equals("testRH05042012"));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void shouldWorkCompleteGetLoginInfo() {
		RHImpl sfaf = new RHImpl();
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		sfaf.setTestbed("PlanetLab");
		sfaf.setWaitingTime("20");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		RSpecSchema r = new FedericaRSpec();
		r.createEmptyRequestRSpec();
		r.setLogService(logService);
	
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		try {
			IMRepositoryUtil imru = new IMRepositoryUtilImpl();
			// Unbound Request
			String stringOwl;
			stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"2slivers_1router_bound_corr2.owl");
			
			TopologyImpl mixTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
			
			when(mockActions.addRecordPL(anyString(), anyString(), anyString(), anyString()))
					.thenReturn("");
			when(mockActions.addRecordFed(anyString(), anyString(), anyString()))
			.thenReturn("");
			when(mockActions.getSelfCredentialPL()).thenReturn("credentialString");
			when(mockActions.getSelfCredentialFed()).thenReturn("credentialString");
			when(mockActions.getCredential(anyString(), anyString(), anyString()))
					.thenReturn("");
			
							
			Map<String, Object> mockResult = new HashMap<String, Object>();
			Map<String, Object> mockMapCode = new HashMap<String, Object>();
			mockMapCode.put("geni_code", 0);
			mockResult.put("code", mockMapCode);
			String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifestRouterAndUAG.txt");
			mockResult.put("value", manifest);
			
			Map<String, Object> mockResultList = new HashMap<String, Object>();
			Map<String, Object> mockMapCodeList = new HashMap<String, Object>();
			mockMapCodeList.put("geni_code", 0);
			mockResultList.put("code", mockMapCodeList);
			String rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"PLnoviall.rspec");
			mockResultList.put("value", rspec);			
			when(mockActions.listResources(anyString()))
					.thenReturn(mockResultList);
			when(mockActions.populateFedSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);
			when(mockActions.populatePLSlice(anyString(), anyString(), (NOVIUserImpl) anyObject(), anyString()))
					.thenReturn(mockResult);

			RHCreateDeleteSliceResponse response = sfaf.createSlice(null, user, "testRH05042012", mixTopology);
			assertNotNull(response);
			assertFalse(response.hasError());
			assertNotNull(response.getSliceID());
			assertEquals("testRH05042012", response.getSliceID());
			assertNotNull(response.getTopologyCreated());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	
	@Test
	public void testListSlicesShouldFailBecauseSelfCredentialFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {

			Object[] mockRecordResultPL = new Object[1];
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			Object[] slicesPL = new Object[1];
			slicesPL[0] = "fakeSlice1";
			mockRecordPL.put("slices", slicesPL);

			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
			when(mockActions.getSelfCredentialFed()).thenThrow(new Exception("We're mocking error during getSelfCredentialFed"));
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertTrue(response.hasError());
			assertEquals("RH - Error listing slices of testbed FEDERICA" + 
					".\nError message: java.lang.Exception: We're mocking error during getSelfCredentialFed", response.getErrorMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldFailBecauseSelfCredentialPL() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			
			Object[] mockRecordResultPL = new Object[1];
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			Object[] slicesPL = new Object[1];
			slicesPL[0] = "fakeSlice1";
			mockRecordPL.put("slices", slicesPL);
			
			when(mockActions.resolve("fedCredential", "firexp.novi.celia")).thenReturn(mockRecordResultPL);
			when(mockActions.getSelfCredentialPL()).thenThrow(new Exception("We're mocking error during getSelfCredentialPL"));
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertTrue(response.hasError());
			assertEquals("RH - Error listing slices of testbed PlanetLab" + 
					".\nError message: java.lang.Exception: We're mocking error during getSelfCredentialPL", response.getErrorMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkPLandFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
					
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {"test+novipl.novi+fakeSlice1"};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential", "firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(2, response.getSlices().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testListSlicesShouldWorkPLandOtherAuthoritiesFed() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {"test+novipl.novi+fakeSlice1"};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+authority.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkFedandOtherAuthoritiesPL() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");		
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {"test+novipl.pl+fakeSlice1"};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
			
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkMixAuthoritiesPL() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {"test+novipl.novi+fakeSlice1", "test+lalala+fakeSlice1"};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);

			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkEmptyListPL() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);

			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkRecordNotFound() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenThrow(new Exception("novipl.novi.celia record not found"));

			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldCatchException() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {"test+federica.eu+fakeSlice2"};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenThrow(new Exception("error!"));
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenThrow(new Exception("error!"));

			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertTrue(response.hasError());
			assertNull(response.getSlices());
			assertEquals("RH - Error listing slices of testbed PlanetLab.\nError message: java.lang.Exception: error!", response.getErrorMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListSlicesShouldWorkEmptyListOfSlices() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
			when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
			
			Map<String, Object> mockRecordPL = new HashMap<String, Object>();
			String[] slicesPL = {};
			mockRecordPL.put("slices", slicesPL);
			Object[] mockRecordResultPL = {mockRecordPL};
				
			Map<String, Object> mockRecordFed = new HashMap<String, Object>();
			String[] slicesFed = {};
			mockRecordFed.put("slices", slicesFed);
			Object[] mockRecordResultFed = {mockRecordFed};
			
			when(mockActions.resolve("fedCredential","firexp.novi.celia")).thenReturn(mockRecordResultFed);
			when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
			
			RHListSlicesResponseImpl response = sfaf.listUserSlices("celia");
			assertFalse(response.hasError());
			assertNotNull(response.getSlices());
			assertEquals(response.getSlices().size(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void deleteStaticSlice(){
		Date d = new Date();
		System.out.println("Starting createSlice: " + d);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		SFAActions sfa = mock(SFAActions.class);
		NswitchManager mockNS = mock(NswitchManager.class);
		sfaf.setSfaActions(sfa);
		sfaf.setWaitingTime("20");		
		sfaf.setNswitchManager(mockNS);
		
		try {

			doNothing().when(mockNS).releaseFederation(anyString());
			doNothing().when(sfa).setClient((XMLRPCClient) anyObject());

			when(sfa.getSelfCredentialPL()).thenReturn("plCredential");
			when(sfa.getCredential(anyString(), anyString(), anyString())).thenReturn("sliceCredential");
			when(sfa.removeSliceResources(anyString(), anyString())).thenReturn("hola");
			when(sfa.removeSlice(anyString(), anyString())).thenReturn(1);
			when(sfa.getSelfCredentialFed()).thenReturn("fedCredential");
			
			List<String> staticSlices = new ArrayList<String>();
			staticSlices.add("testStaticSlice");
			staticSlices.add("hohohoho");
			
			sfaf.setStaticSlicesInFederica(staticSlices);
			
			Set<String> platformURIs = new HashSet<String>();
			platformURIs.add("federica");
			platformURIs.add("planetlab");
			TopologyImpl t = new TopologyImpl();
			Set<Resource> resources = new HashSet<Resource>();
			t.setContains(resources);
			RHCreateDeleteSliceResponseImpl r = sfaf.releaseFederationAndDeleteSlice(null,"testStaticSlice", platformURIs, t);
			
			if (r.hasError() == true) {
				System.out.println(r.getErrorMessage());
			}
			assertFalse(r.hasError());
			assertNotNull(r.getSliceID());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

	}
	
	@Test
	public void shouldFindNode() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab:smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		VirtualNode vmFound = sfaf.findNodeByName(plTopology, "vm");
		assertNotNull(vmFound);
		assertEquals("vm", vmFound.toString().replace(SFAConstants.NOVI_IM_URI_BASE, ""));
	}
	
	@Test
	public void shouldNotFindNode() {
		plTopology = new TopologyImpl("testTopology");
		Node parent = new NodeImpl("PlanetLab:smilax1.man.poznan.pl");
		VirtualNode vm = new VirtualNodeImpl("vm");
		Set<Node> setNodes = new HashSet<Node>();
		Set<Node> setContains = new HashSet<Node>();
		setNodes.add(parent);
		vm.setImplementedBy(setNodes);
		setContains.add(vm);
		plTopology.setContains(setContains);
		
		RHImpl sfaf = new RHImpl();
		VirtualNode vmFound = sfaf.findNodeByName(plTopology, "router");
		assertNull(vmFound);
	}
	
	@Test
	public void testAddLoginInfoToTopology() throws IOException {
		String manifest = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"manifestExample.xml");
		FedericaRSpec manifestRspec = new FedericaRSpec();
		manifestRspec.readRSpec(manifest);
		
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"FedericaReq.owl");
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		
		RHImpl sfaf = new RHImpl();
		sfaf.setManifest(manifestRspec);
		sfaf.setUserLoginInfo(new StringBuffer());
		Topology newTopology = sfaf.addUserInfoInTopology(fedTopology);
		assertNotNull(newTopology);
		assertNotNull(newTopology.getContains());
		for (Resource resource : newTopology.getContains()) {
			assertTrue(resource instanceof VirtualNode);
			assertNotNull(((VirtualNode)resource).getHasComponent());
		}
		
	}
	
	

	
	@Test
	public void testTranslateEmptyAdvertisementRSpecShouldFail() {
		RHImpl sfaf = new RHImpl();
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
												
		RHListResourcesResponse response = sfaf.translateAdvertisementRSpecToPlatform("");
		
		//check that it returns an error
		assert(response.hasError());// && response.getErrorMessage().equalsIgnoreCase("empty RSpec string"));

	}
	

	
	@Test
	public void testTranslateAdvertisementRSpecOtherTestbedShouldFail() {
		String rspec = "";
		try {
			rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"AdvRSpecFed.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert(rspec!="");
		
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("SomeTestbed");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
						
		RHListResourcesResponse response = sfaf.translateAdvertisementRSpecToPlatform(rspec);
		
		//check that it returns an error
		assert(response.hasError());// && response.getErrorMessage().equalsIgnoreCase("Incorrect testbed name"));

	}
	
	@Test
	public void testTranslateAdvertisementRSpecPL() {				
		String rspec = "";
		try {
			rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"AdvRSpecPL.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assert(rspec!="");
		
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("PlanetLab");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		RHListResourcesResponse r = sfaf.translateAdvertisementRSpecToPlatform(rspec);
		
		//check that the response has a platform that contains a Resource set of one element
		assertFalse(r.hasError());
		assertNotNull(r.getPlatformString());

	}
	
	@Test
	public void testTranslateAdvertisementRSpecFederica() {		
		String rspec = "";
		try {
			rspec = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"AdvRSpecFed.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert(rspec!="");
		
		
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("Federica");
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		RHListResourcesResponse response = sfaf.translateAdvertisementRSpecToPlatform(rspec);
		
		//check that the response has a platform that contains a resourceSet of six elements
		assert(!response.hasError());
		assertNotNull(response.getPlatformString());

	}
	
	@Test 
	public void testGetSlicesFromRequest(){
		RHImpl sfaf = new RHImpl();
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test", 
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		List<String> slices = sfaf.getSlicesFromResponse(responseFromTestbed);
		assertNotNull(slices);
		assertEquals(2, slices.size());
		assertEquals("urn:publicid:IDN+federica.eu+slice+testDfnPsnc", slices.get(0));
		assertEquals("urn:publicid:IDN+firexp:novi+slice+slice_971819752", slices.get(1));
	}
	
	@Test 
	public void testGetSlicesFromPlanetLab(){
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("planetlab");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test", 
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.getSelfCredentialPL()).thenReturn("");
			when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);

			List<String> slices = sfaf.getSlicesFromTestbed("planetlab");
			assertNotNull(slices);
			assertEquals(2, slices.size());
			assertEquals("urn:publicid:IDN+federica.eu+slice+testDfnPsnc", slices.get(0));
			assertEquals("urn:publicid:IDN+firexp:novi+slice+slice_971819752", slices.get(1));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test 
	public void testGetSlicesFromFederica(){
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("");
			when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);

			List<String> slices = sfaf.getSlicesFromTestbed("federica");
			assertNotNull(slices);
			assertEquals(2, slices.size());
			assertEquals("urn:publicid:IDN+federica.eu+slice+testDfnPsnc", slices.get(0));
			assertEquals("urn:publicid:IDN+firexp:novi+slice+slice_971819752", slices.get(1));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test 
	public void testSliceExistsInTestbed(){
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.getSelfCredentialFed()).thenReturn("");
			when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);

			assertTrue(sfaf.sliceExistsInTestbed("slice_971819752", "Federica"));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test 
	public void testSliceDoesntExistsInTestbed(){
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("federica");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.listAllSlices("Federica")).thenReturn(responseFromTestbed);

			assertFalse(sfaf.sliceExistsInTestbed("slice_Celia", "Federica"));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test 
	public void testGetSlicesNoTestbed(){
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		try {
			when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);

			List<String> slices = sfaf.getSlicesFromTestbed("");
			assertNull(slices);
		} catch (Exception e) {
			assertTrue(e.toString().contains("RH - Error listing slices, testbed is not set."));
			System.out.println(e.toString());
		}
	}
	
	@Test
	public void testGetUserSlicesFromRecordNullObject() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		ArrayList<String> userSlices = sfaf.getUserSlicesFromRecord(null);
		assertNotNull(userSlices);
		assertEquals(0, userSlices.size());
	}
	
	@Test
	public void testGetUserSlicesFromRecordEmptyList() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		String[] listReceived = new String[0];
		ArrayList<String> userSlices = sfaf.getUserSlicesFromRecord(listReceived);
		assertNotNull(userSlices);
		assertEquals(0, userSlices.size());
	}
	
	@Test
	public void testGetUserSlicesFromRecordWithoutMapsOnIt() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		String[] listReceived = new String[]{"hola"};
		ArrayList<String> userSlices = sfaf.getUserSlicesFromRecord(listReceived);
		assertNotNull(userSlices);
		assertEquals(0, userSlices.size());
	}
	
	@Test
	public void testGetUserSlicesFromRecordMapDoesntContainSlice() {
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		LogService logService = mock(LogService.class);
		sfaf.setLogService(logService);
		
		Map[] listReceived = new Map[1];
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("hola", "cualquier cosa");
		ArrayList<String> userSlices = sfaf.getUserSlicesFromRecord(listReceived);
		assertNotNull(userSlices);
		assertEquals(0, userSlices.size());
	}
	
	
	@Test
	public void addInterdomainMultiLink() throws IOException, RHBadInputException {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultiLinkInterdomain.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		sfaf.initCreateUpdateMethods(user);
		
		Set<Resource> resources = fedTopology.getContains();
		for (Resource resource : resources) {
			if(resource instanceof VirtualLink) {
 				sfaf.analyzeVirtualLink((VirtualLink)resource);
			}
		}
		FedericaRSpec rspec = sfaf.getFedericaRSpec();
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("link1"));
		assertEquals(2, rspec.getLinkListFromDocuemtn().getLength());
	}
	
	@Test
	public void addInterdomainMultiLinkCompleteRSpec() throws IOException, RHBadInputException {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MultiLinkInterdomain.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		sfaf.initCreateUpdateMethods(user);
		
		sfaf.analyzeTopologyReceived(fedTopology);
		
		FedericaRSpec rspec = sfaf.getFedericaRSpec();
		assertNotNull(rspec.toString());
		assertTrue(rspec.toString().contains("link1"));
		assertEquals(2, rspec.getLinkListFromDocuemtn().getLength());
	}
	
	@Test
	public void addInterdomainMultiLinkCompleteRSpec2() throws IOException, RHBadInputException {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		fedTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		sfaf.initCreateUpdateMethods(user);
		
		sfaf.analyzeTopologyReceived(fedTopology);
		
		FedericaRSpec rspec = sfaf.getFedericaRSpec();
		assertNotNull(rspec.toString());
		assertEquals(3, rspec.getLinkListFromDocuemtn().getLength());

	}
	
	@Test
	public void releaseFederationNSwitch() throws Exception {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"MidtermWorkshopRequest_bound_slice2_v8.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		TopologyImpl topology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
	
		sfaf.releaseFederation("test", topology);
	}
	
	@Test
	public void releaseFederationNoNSwitch() throws Exception {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		String stringOwl = readFileAsString("src"+sep+"test"+sep+"resources"+sep+"2Routers.owl");
		IMRepositoryUtil imru = new IMRepositoryUtilImpl();
		TopologyImpl topology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
	
		sfaf.releaseFederation("test", topology);
	}
	
	@Test
	public void updateFedericaNoFedericaResources() throws Exception {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
		when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
				
		Map<String, Object> mockRecordPL = new HashMap<String, Object>();
		String[] slicesPL = {"test+novipl.novi+fakeSlice1"};
		mockRecordPL.put("slices", slicesPL);
		Object[] mockRecordResultPL = {mockRecordPL};
			
		Map<String, Object> mockRecordFed = new HashMap<String, Object>();
		String[] slicesFed = {"test+federica.eu+fakeSlice2"};
		mockRecordFed.put("slices", slicesFed);
		Object[] mockRecordResultFed = {mockRecordFed};
		
		when(mockActions.resolve("fedCredential", "firexp.novi.celia")).thenReturn(mockRecordResultFed);
		when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
		
	
		sfaf.setNoFedericaNodesInRequest(true);
		RHCreateDeleteSliceResponse r = sfaf.updateFederica("test", "slice_test");
		assertFalse(r.hasError());
		assertEquals("", r.getErrorMessage());
		
	}
	
	@Test
	public void updateFedericaFedericaResourcesStaticSlice() throws Exception {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);
		
		when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);
		when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
		when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
				
		Map<String, Object> mockRecordPL = new HashMap<String, Object>();
		String[] slicesPL = {"test+novipl.novi+fakeSlice1"};
		mockRecordPL.put("slices", slicesPL);
		Object[] mockRecordResultPL = {mockRecordPL};
			
		Map<String, Object> mockRecordFed = new HashMap<String, Object>();
		String[] slicesFed = {"test+federica.eu+fakeSlice2"};
		mockRecordFed.put("slices", slicesFed);
		Object[] mockRecordResultFed = {mockRecordFed};
		
		when(mockActions.resolve("fedCredential", "firexp.novi.celia")).thenReturn(mockRecordResultFed);
		when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
		
	
		sfaf.setNoFedericaNodesInRequest(false);
		RHCreateDeleteSliceResponse r = sfaf.updateFederica("fixPBoundRequest", "slice_test");
		assertFalse(r.hasError());
		assertEquals("", r.getErrorMessage());
		
	}
	
	public void updateFedericaNoFedericaResourcesExistingStaticSlice() throws Exception {
		LogService logService = mock(LogService.class);
		RHImpl sfaf = new RHImpl();
		sfaf.setTestbed("");
		SFAActions mockActions = mock(SFAActions.class);
		sfaf.setSfaActions(mockActions);
		ReportEvent userFeedback = mock(ReportEvent.class);
		sfaf.setUserFeedback(userFeedback);
		sfaf.setLogService(logService);
		NswitchManager ns = mock(NswitchManager.class);
		sfaf.setNswitchManager(ns);
		
		Object[] urns = {"urn:publicid:IDN+federica.eu+slice+testDfnPsnc",
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test",  
				"urn:publicid:IDN+firexp:novi+slice+slice_971819752", 
				"urn:publicid:IDN+federica.eu+slice+BonFIRE_test2"};
		Map<String, Object> responseFromTestbed = new HashMap<String, Object>();
		responseFromTestbed.put("value", urns);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("geni_code", 0);
		codeMap.put("am_type", "sfa");
		responseFromTestbed.put("code", codeMap);
		responseFromTestbed.put("geni_api", 2);
		responseFromTestbed.put("output", null);

		when(mockActions.listAllSlices("")).thenReturn(responseFromTestbed);
		when(mockActions.getSelfCredentialFed()).thenReturn("fedCredential");
		when(mockActions.getSelfCredentialPL()).thenReturn("plCredential");
				
		Map<String, Object> mockRecordPL = new HashMap<String, Object>();
		String[] slicesPL = {"test+novipl.novi+slice_test"};
		mockRecordPL.put("slices", slicesPL);
		Object[] mockRecordResultPL = {mockRecordPL};
			
		Map<String, Object> mockRecordFed = new HashMap<String, Object>();
		String[] slicesFed = {"test+federica.eu+fakeSlice2"};
		mockRecordFed.put("slices", slicesFed);
		Object[] mockRecordResultFed = {mockRecordFed};
		
		when(mockActions.resolve("fedCredential", "firexp.novi.celia")).thenReturn(mockRecordResultFed);
		when(mockActions.resolve("plCredential", "novipl.novi.celia")).thenReturn(mockRecordResultPL);
		
	
		sfaf.setNoFedericaNodesInRequest(true);
		RHCreateDeleteSliceResponse r = sfaf.updateFederica("fixPBoundRequest", "slice_test");
		assertFalse(r.hasError());
		assertEquals("", r.getErrorMessage());
		
	}
}
