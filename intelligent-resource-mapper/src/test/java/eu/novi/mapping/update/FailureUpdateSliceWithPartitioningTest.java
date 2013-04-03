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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;

public class FailureUpdateSliceWithPartitioningTest {

	IRMEngine plcIRM;
	IRMEngine fedeIRM;
	List<EmbeddingAlgorithmInterface> plcEmbeddingAlgorithms;
	List<EmbeddingAlgorithmInterface> fedeEmbeddingAlgorithms;
	
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
		Vector<FPartCostTestbedResponseImpl> testbedResponseVector =  new  Vector<FPartCostTestbedResponseImpl>();
		// Creating mock classes for PLC
		IRMCalls plcMockRIS = mock(IRMCalls.class);
		FRResponse plcMockFRResponse = mock(FRResponse.class);
		ReserveResponse plcMockReserveResponse = mock(ReserveResponse.class);
		FPartCostTestbedResponseImpl  plcMockFPartCostTestbedResponse =  mock(FPartCostTestbedResponseImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplSer = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplRout = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplLink = mock(FPartCostRecordImpl.class);
		Vector<FPartCostRecordImpl> plcTesbedNodeRecordVector =  new  Vector<FPartCostRecordImpl>();
		Vector<FPartCostRecordImpl> plcTesbedLinkRecordVector =  new  Vector<FPartCostRecordImpl>();
		// Creating mock classes for FEDE
		IRMCalls fedeMockRIS = mock(IRMCalls.class);
		FRResponse fedeMockFRResponse = mock(FRResponse.class);
		ReserveResponse fedeMockReserveResponse = mock(ReserveResponse.class);
		FPartCostTestbedResponseImpl  fedMockFPartCostTestbedResponse =  mock(FPartCostTestbedResponseImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplSer = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplRout = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplLink = mock(FPartCostRecordImpl.class);
		Vector<FPartCostRecordImpl> fedTesbedNodeRecordVector =  new  Vector<FPartCostRecordImpl>();
		Vector<FPartCostRecordImpl> fedTesbedLinkRecordVector =  new  Vector<FPartCostRecordImpl>();
		
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
		/**
		 * First call to getTopology returns an Empty topology
		 * Second call to getTopology returns the correct topology
		 */
		when(plcMockFRResponse.getTopology())
				.thenReturn(createPLCMockAvailableResources(), 
						createSecondPLCMockAvailableResources());
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
		
		/** SUBBING Partitioning calls **/
		
		//stubbing PLC plcMockFPartCostRecordImpl		
		log.debug("stubbing PL FPartCostRecordImpl for every server node requested in PlanetLab...");
		Set<String>  serverURIsPLC = new  HashSet<String>();
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#sliver1");
		when (plcMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(8);
		when (plcMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.35);
		when (plcMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsPLC);
		
//		log.debug("stubbing PL FPartCostRecordImpl for every router node requested in PlanetLab...");
//		Set<String>  routerURIsPLC = new  HashSet<String>();
//		routerURIsPLC.add("http://fp7-novi.eu/im.owl#lrouter");
//		when (plcMockFPartCostRecordImplRout.getAvailResNumber()).thenReturn(0);
//		when (plcMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.0);
//		when (plcMockFPartCostRecordImplRout.hasError()).thenReturn(false);
//		when (plcMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsPLC);
		
		log.debug("stubbing PL FPartCostRecordImplLink for every link requested in PlanetLab...");
		Set<String>  linkURIsPLC = new  HashSet<String>();
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link1-lrouter-sliver1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link1-sliver1-lrouter");
		when (plcMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(150000);
		when (plcMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplLink.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplLink.getResourceURIs()).thenReturn(linkURIsPLC);
	
		//stubbing FEDE FEDMockFPartCostRecordImpl		
		log.debug("stubbing FED FPartCostRecordImpl for every server node requested in FEDERICA...");
		Set<String>  serverURIsFED = new  HashSet<String>();
		serverURIsFED.add("http://fp7-novi.eu/im.owl#sliver1");
		when (fedMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(3);
		when (fedMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.45);
		when (fedMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsFED);
		
//		log.debug("stubbing FED FPartCostRecordImpl for every router node requested in FEDERICA...");
//		Set<String>  routerURIsFED = new  HashSet<String>();
//		routerURIsFED.add("http://fp7-novi.eu/im.owl#lrouter");
//		when (fedMockFPartCostRecordImplRout.getAvailResNumber()).thenReturn(3);
//		when (fedMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.1);
//		when (fedMockFPartCostRecordImplRout.hasError()).thenReturn(false);
//		when (fedMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsFED);
		
		log.debug("stubbing FED FPartCostRecordImplLink for every link requested in FEDERICA...");
		Set<String>  linkURIsFED = new  HashSet<String>();
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-lrouter-sliver1");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-sliver1-lrouter");
		when (fedMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(6);
		when (fedMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.25);
		when (fedMockFPartCostRecordImplLink.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplLink.getResourceURIs()).thenReturn(linkURIsFED);
			
		//stubbing PLC plcMockFPartCostTestbedResponse	
		log.debug("stubbing PL FPartCostTestbedResponse for  PlanetLab...");
		plcTesbedNodeRecordVector.add(plcMockFPartCostRecordImplSer);
		plcTesbedNodeRecordVector.add(plcMockFPartCostRecordImplRout);
		plcTesbedLinkRecordVector.add(plcMockFPartCostRecordImplLink);
		when (plcMockFPartCostTestbedResponse.getNodeCosts()).thenReturn(plcTesbedNodeRecordVector);
		when (plcMockFPartCostTestbedResponse.getLinkCosts()).thenReturn(plcTesbedLinkRecordVector);
		when (plcMockFPartCostTestbedResponse.getTestbedURI()).thenReturn("http://fp7-novi.eu/im.owl#PlanetLab");

		//stubbing FEDE fedMockFPartCostTestbedResponse	
		log.debug("stubbing FEDE FPartCostTestbedResponse for FEDERICA...");
		fedTesbedNodeRecordVector.add(fedMockFPartCostRecordImplSer);
		fedTesbedNodeRecordVector.add(fedMockFPartCostRecordImplRout);
		fedTesbedLinkRecordVector.add(fedMockFPartCostRecordImplLink);
		when (fedMockFPartCostTestbedResponse.getNodeCosts()).thenReturn(fedTesbedNodeRecordVector);
		when (fedMockFPartCostTestbedResponse.getLinkCosts()).thenReturn(fedTesbedLinkRecordVector);
		when (fedMockFPartCostTestbedResponse.getTestbedURI()).thenReturn("http://fp7-novi.eu/im.owl#FEDERICA");
		
		testbedResponseVector.add(plcMockFPartCostTestbedResponse);
		testbedResponseVector.add(fedMockFPartCostTestbedResponse);
		
		when(plcMockRIS.findPartitioningCost(any(String.class), any(Topology.class)))
				.thenReturn(testbedResponseVector);
		when(fedeMockRIS.findPartitioningCost(any(String.class), any(Topology.class)))
				.thenReturn(testbedResponseVector);
		
		/** END SUBBING Partitioning calls **/
		
		log.debug("UpdateSliceTest initialized");
	}

//	@Test
	public void updateSliceFromPLCTest() {
		log.debug("Running test...");
		Set<String> failingResources = new HashSet<String>();
		failingResources.add("http://fp7-novi.eu/im.owl#sliver1");
		
		log.debug("Updating slice from PLC...");
		Collection<String> result = plcIRM.updateSlice("sessionID","midtermWorkshopSlice", failingResources);
//		assertFalse(result==-1);
		assertFalse(result.size()==0);
		
		log.debug("Result of the updateSlice method: "+result);
		log.debug("UpdateSliceTest successfully done");
	}
	
//	@Test
	public void updateSliceFromFEDETest() {
		log.debug("Running test...");
		Set<String> failingResources = new HashSet<String>();
		failingResources.add("http://fp7-novi.eu/im.owl#sliver1");
		
		log.debug("Updating slice from FEDERICA...");
		Collection<String> result = fedeIRM.updateSlice("sessionID","midtermWorkshopSlice", failingResources);
//		assertFalse(result==-1);
		assertFalse(result.size()==0);
		
		log.debug("Result of the updateSlice method: "+result);
		log.debug("UpdateSliceTest successfully done");
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
		String resourceName = "http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr";
		Resource result = imru.getKnownResourceFromFile(stringOwl,resourceName);
		return result;
	}
	
	private Resource getFEDEMockResource() throws IOException {
		log.debug("creating FEDE mock failing Resource...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/failingPhysicalResource.owl");
		String resourceName = "http://fp7-novi.eu/im.owl#planetlab2-novi.lab.netmode.ece.ntua.gr";
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
		String stringOwl = readFileAsString("src/main/resources/PLEEmptyTopology.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private Topology createSecondPLCMockAvailableResources() throws IOException {
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
	
}
