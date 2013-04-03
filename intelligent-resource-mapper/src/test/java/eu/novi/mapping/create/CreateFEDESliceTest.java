/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.create;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;

public class CreateFEDESliceTest {

	IRMEngine fedeIRM;
	List<EmbeddingAlgorithmInterface> fedeEmbeddingAlgorithms;
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	ScheduledExecutorService fedeScheduler = Executors.newScheduledThreadPool(2);
	
	//logging
    private static final transient Logger log =
    	LoggerFactory.getLogger(CreateFEDESliceTest.class);
	
	@SuppressWarnings("unchecked")
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing CreateSliceTest...");
		
		log.debug("Setting up local environment for FEDE IRM...");
		ReportEvent userFeedback = mock(ReportEvent.class);
		LogService logService = mock(LogService.class);
		IRMCalls mockRIS = mock(IRMCalls.class);
		FRResponse mockFRResponse = mock(FRResponse.class);
		ReserveResponse mockReserveResponse = mock(ReserveResponse.class);
		
		fedeIRM = new IRMEngine();
		fedeEmbeddingAlgorithms = new ArrayList<EmbeddingAlgorithmInterface>();
		EmbeddingAlgorithmFEDERICA fedAlgo =new EmbeddingAlgorithmFEDERICA();
		fedAlgo.setLogService(logService);
		fedAlgo.setUserFeedback(userFeedback);
		fedeEmbeddingAlgorithms.add(fedAlgo);
		fedeIRM.addEmbedding(fedeEmbeddingAlgorithms);
		fedeIRM.setIrmCallsFromRIS(mockRIS); 
		fedeIRM.setTestbed("FEDERICA");
//		irmGSP.setSessionID("temporarySessionIDforFEDE");
		fedeIRM.addIrms(new ArrayList<RemoteIRM>());
		fedeIRM.getIrms().add(fedeIRM);
		fedeIRM.setReportUserFeedback(userFeedback);
		fedeIRM.setLogService(logService);
		fedeIRM.setScheduler(fedeScheduler);
		
		//stubbing mockRIS
		log.debug("stubbing mockRIS...");
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				.thenReturn(mockFRResponse);
		when(mockRIS.getResource(any(String.class)))
				.thenReturn(getMockResource());
		when(mockRIS.getResources(any(Set.class)))
				.thenReturn(getMockResources());
		when(mockRIS.checkResources(any(String.class), any(Set.class),any(NOVIUserImpl.class)))
				.thenReturn(getMockResourceIDs());
		when(mockRIS.reserveSlice(any(String.class), any(Topology.class),anyInt(),any(NOVIUserImpl.class)))
				.thenReturn(mockReserveResponse);
		
		//stubbing mockFRResponse
		log.debug("stubbing mockFRResponse...");
		when(mockFRResponse.getTopology())
				.thenReturn(createMockAvailableResources());
		when(mockFRResponse.hasError())
				.thenReturn(false);
		
		//stubbing mockReserveResponse
		log.debug("stubbing mockReserveResponse...");
		when(mockReserveResponse.getSliceID())
				.thenReturn(100);
		when(mockReserveResponse.hasError())
				.thenReturn(false);
		when(mockReserveResponse.getErrorMessage())
				.thenReturn(ReserveMess.RESERVATION_TO_TESTEBED_FAILED);
		when(mockReserveResponse.getMessage())
				.thenReturn("Error reserving Slice");
		
		log.debug("CreateSliceTest initialized for FEDE");
		
	}

	@Test
	public void createSliceTest() throws IOException {
		log.debug("Running test...");
			
		log.debug("Creating slice platform bound to FEDERICA...");
		int result = fedeIRM.createSlice("sessionID",createRequestTopology(),createNoviUser());
		log.debug("Result of the createSlice: "+result);
		
		//assertFalse(result==-1);
		
		log.debug("CreateSliceTest successfully done");
	}
	
	private Set<GroupImpl> createRequestTopology() throws IOException {
		// loading and getting imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/platformBoundRequest.owl");
//		Set<Group> groups = imru.getGroupImplFromFile(stringOwl);
		Set<Group> groups = imru.getIMObjectsFromString(stringOwl, Group.class);
		Set<GroupImpl> groups1 = new HashSet<GroupImpl>();
		
		for (Group group : groups) {
			groups1.add((GroupImpl)group);
		}
		
		log.debug("Number of Groups in the request:" + groups1.size());
		
		return groups1;
	}
	
	private Topology createMockAvailableResources() throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/FEDERICATopology.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private Resource getMockResource() throws IOException {
		Topology substrate = createMockAvailableResources();
		return substrate.getContains().iterator().next();
	}
	
	private Set<Resource> getMockResources() throws IOException {
		Set<Resource> resourceSet = new HashSet<Resource>();
		Topology substrate = createMockAvailableResources();
		resourceSet.add(substrate.getContains().iterator().next());
		return resourceSet;
	}
	
	private Set<String> getMockResourceIDs() throws IOException {
		Set<String> idSet = new HashSet<String>();
		// Dummy addition of 1 resource ID
		idSet.add("dummyNode");
		return idSet;
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
	
	private static NOVIUserImpl createNoviUser() {
		NOVIUserImpl noviUser = new NOVIUserImpl("novi_user");
		noviUser.setFirstName("Novi");
		noviUser.setLastName("User");
		noviUser.setHasSessionKey("session_key");
		return noviUser;
	}
	
}