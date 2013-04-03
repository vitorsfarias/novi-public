/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.update;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRFailedMess;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;

public class FailureUpdateSliceTest {

	IRMEngine plcIRM;
	IRMEngine fedeIRM;
	List<EmbeddingAlgorithmInterface> plcEmbeddingAlgorithms;
	List<EmbeddingAlgorithmInterface> fedeEmbeddingAlgorithms;
	
	FRResponse plcMockFRResponse;
	FRResponse fedeMockFRResponse;
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// schedulers
	ScheduledExecutorService plcScheduler = Executors.newScheduledThreadPool(2);
	ScheduledExecutorService fedeScheduler = Executors.newScheduledThreadPool(2);
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(FailureUpdateSliceTest.class);
	
	@SuppressWarnings("unchecked")
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing UpdateSliceTest...");
		
		// Creating mock classes
		ReportEvent userFeedback = mock(ReportEvent.class);
		LogService logService = mock(LogService.class);
		// Creating mock classes for PLC
		IRMCalls plcMockRIS = mock(IRMCalls.class);
		plcMockFRResponse = mock(FRResponse.class);
		ReserveResponse plcMockReserveResponse = mock(ReserveResponse.class);
		// Creating mock classes for FEDE
		IRMCalls fedeMockRIS = mock(IRMCalls.class);
		fedeMockFRResponse = mock(FRResponse.class);
		ReserveResponse fedeMockReserveResponse = mock(ReserveResponse.class);
		
		log.debug("Setting up local environment for PL IRM...");
		plcIRM = new IRMEngine();
		plcIRM.setTestbed("PlanetLab");
		plcIRM.setIrmCallsFromRIS(plcMockRIS); 
		plcEmbeddingAlgorithms = new ArrayList<EmbeddingAlgorithmInterface>();
		eu.novi.mapping.embedding.planetlab.EmbeddingAlgorithmGNM plEmbeddingAlgorithm = 
			new eu.novi.mapping.embedding.planetlab.EmbeddingAlgorithmGNM();
		plEmbeddingAlgorithm.setLogService(logService);
		plEmbeddingAlgorithm.setUserFeedback(userFeedback);
		plcEmbeddingAlgorithms.add(plEmbeddingAlgorithm);
		plcIRM.addEmbedding(plcEmbeddingAlgorithms);
		plcIRM.addIrms(new ArrayList<RemoteIRM>());
		plcIRM.setReportUserFeedback(userFeedback);
		plcIRM.setLogService(logService);
		plcIRM.setScheduler(plcScheduler);
		
		log.debug("Setting up local environment for FEDE IRM...");
		fedeIRM = new IRMEngine();
		fedeIRM.setTestbed("FEDERICA");
		fedeIRM.setIrmCallsFromRIS(fedeMockRIS); 
		fedeEmbeddingAlgorithms = new ArrayList<EmbeddingAlgorithmInterface>();
		EmbeddingAlgorithmFEDERICA fedAlgo =new EmbeddingAlgorithmFEDERICA();
		fedAlgo.setLogService(logService);
		fedAlgo.setUserFeedback(userFeedback);
		fedeEmbeddingAlgorithms.add(fedAlgo);
		fedeIRM.addEmbedding(fedeEmbeddingAlgorithms);
		fedeIRM.addIrms(new ArrayList<RemoteIRM>());
		fedeIRM.setReportUserFeedback(userFeedback);
		fedeIRM.setLogService(logService);
		fedeIRM.setScheduler(fedeScheduler);
		
		// Assigning IRMs
		plcIRM.getIrms().add(fedeIRM);
		plcIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(fedeIRM);
		
		//stubbing PLC mockRIS
		log.debug("stubbing mockRIS...");
		when(plcMockRIS.getSlice(anyString()))
				.thenReturn(createPLMockSlice());
		when(plcMockRIS.getResource(anyString()))
				.thenReturn(getPLCMockResource());
		when(plcMockRIS.findLocalResourcesUpdate(any(String.class), any(Reservation.class), any(Set.class)))
				.thenReturn(plcMockFRResponse);
		when(plcMockRIS.updateSlice(any(String.class), any(Topology.class),anyInt()))
				.thenReturn(plcMockReserveResponse);
		
		//stubbing PLC mockFRResponse
		log.debug("stubbing mockFRResponse...");
		when(plcMockFRResponse.getTopology())
				.thenReturn(createPLCMockAvailableResources());
		when(plcMockFRResponse.hasError())
				.thenReturn(false);
		
		//stubbing PLC mockReserveResponse
		log.debug("stubbing mockReserveResponse...");
		when(plcMockReserveResponse.getSliceID())
				.thenReturn(100);
		when(plcMockReserveResponse.hasError())
				.thenReturn(false);
		when(plcMockReserveResponse.getErrorMessage())
				.thenReturn(ReserveMess.RESERVATION_TO_TESTEBED_FAILED);
		when(plcMockReserveResponse.getMessage())
				.thenReturn("Error reserving Slice");
		
		//stubbing FEDE mockRIS
		log.debug("stubbing FEDE mockRIS...");
		when(fedeMockRIS.findLocalResourcesUpdate(any(String.class), any(Reservation.class), any(Set.class)))
				.thenReturn(fedeMockFRResponse);
		when(fedeMockRIS.getSlice(anyString()))
				.thenReturn(createFEDEMockSlice());
		when(fedeMockRIS.getResource(any(String.class)))
				.thenReturn(getFEDEMockResource());
		when(fedeMockRIS.updateSlice(any(String.class), any(Topology.class),anyInt()))
				.thenReturn(fedeMockReserveResponse);
		
		//stubbing FEDE mockFRResponse
		log.debug("stubbing FEDE mockFRResponse...");
		when(fedeMockFRResponse.getTopology())
				.thenReturn(createFEDEMockAvailableResources());
		when(fedeMockFRResponse.hasError())
				.thenReturn(false);
		
		//stubbing FEDE mockReserveResponse
		log.debug("stubbing FEDE mockReserveResponse...");
		when(fedeMockReserveResponse.getSliceID())
				.thenReturn(100);
		when(fedeMockReserveResponse.hasError())
				.thenReturn(false);
		when(fedeMockReserveResponse.getErrorMessage())
				.thenReturn(ReserveMess.RESERVATION_TO_TESTEBED_FAILED);
		when(fedeMockReserveResponse.getMessage())
				.thenReturn("Error reserving Slice");
		
		log.debug("UpdateSliceTest initialized");
	}

	@Test
	public void updateSliceFromPLCTest() {
		log.debug("Running test...");
		Set<String> failingResources = new HashSet<String>();
//		failingResources.add("http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr");
		failingResources.add("http://fp7-novi.eu/im.owl#sliver1");
		
		log.debug("Updating slice from PLC...");
		Collection<String> result = plcIRM.updateSlice("sessionID","midtermWorkshopSlice", failingResources);
//		assertFalse(result==-1);
		assertFalse(result.size()==0);
		
		log.debug("Result of the updateSlice method: "+result);
		log.debug("UpdateSliceTest successfully done");
	}
	
	@Test
	public void updateSliceFromFEDETest() {
		log.debug("Running test...");
		Set<String> failingResources = new HashSet<String>();
		failingResources.add("http://fp7-novi.eu/im.owl#vNode2");
		
		log.debug("Updating slice from FEDERICA...");
		Collection<String> result = fedeIRM.updateSlice("sessionID", "midtermWorkshopSlice", failingResources);
		assertFalse(result.size()==0);
		
		log.debug("Result of the updateSlice method: "+result);
		log.debug("UpdateSliceTest successfully done");
	}
	
	@Test
	public void findResourcesUpdateError() {
		log.debug("Running test...");
		Set<String> failingResources = new HashSet<String>();
		failingResources.add("http://fp7-novi.eu/im.owl#sliver1");
		
		// setting error in findResources response
		when(plcMockFRResponse.hasError()).thenReturn(true);
		when(plcMockFRResponse.getUserFeedback()).thenReturn("userFeedback message");
		Map<Resource,FRFailedMess> map = new HashMap<Resource,FRFailedMess>();
		map.put(new NodeImpl("MockResource"), FRFailedMess.WAS_NOT_FOUND_ANY_AVAILABLE_NODE_NON_FUNCTIONAL);
		when(plcMockFRResponse.getFailedResources()).thenReturn(map);
		
		log.debug("Updating slice from PLC...");
		Collection<String> result = plcIRM.updateSlice("sessionID","midtermWorkshopSlice", failingResources);
		
		assertTrue(result.size()==0);
		
		// setting again error to false in findResources
		when(plcMockFRResponse.hasError()).thenReturn(false);
	}
	
	private Reservation createPLMockSlice() throws IOException {
		log.debug("creating mock failing Slice...");
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/failingPLSlice.owl");
		Reservation reservation = imru.getReservationFromFile(stringOwl);
		return reservation;
	}
	
	private Reservation createFEDEMockSlice() throws IOException {
		log.debug("creating mock failing Slice...");
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/failingSlice.owl");
		Reservation reservation = imru.getReservationFromFile(stringOwl);
		return reservation;
	}
	
	private Resource getPLCMockResource() throws IOException {
		log.debug("creating PL mock failing Resource...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/failingPhysicalResource.owl");
		String resourceName = "urn:publicid:IDN+novipl:novi+node+planetlab2-novi.lab.netmode.ece.ntua.gr";
		Resource result = imru.getKnownResourceFromFile(stringOwl,resourceName);
		return result;
	}
	
	private Resource getFEDEMockResource() throws IOException {
		log.debug("creating FEDE mock failing Resource...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/failingSlice.owl");
		String resourceName = "urn:publicid:IDN+federica.eu+node+psnc.poz.vserver3";
		Resource result = imru.getKnownResourceFromFile(stringOwl,resourceName);
		return result;
	}
	
	private Topology createFEDEMockAvailableResources() throws IOException {
		log.debug("creating mock available Physical Resources...");
		imru = new IMRepositoryUtilImpl();
		// loading and getting from imrespository util
		String stringOwl = readFileAsString("src/main/resources/FEDERICATopology.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private Topology createPLCMockAvailableResources() throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/PLETopologyWithoutPlanetlab2.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
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

//	private String getId(Resource resource) {
//		String[] components = resource.toString().split("\\#");
//		return components[components.length-1];
//	}
	
}
