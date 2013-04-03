/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
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
import eu.novi.im.core.Reservation;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.mapping.RemoteIRM;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRResponse;

public class UserUpdateSliceTest {

	IRMEngine plcIRM;
	IRMEngine fedeIRM;
	List<EmbeddingAlgorithmInterface> plcEmbeddingAlgorithms;
	List<EmbeddingAlgorithmInterface> fedeEmbeddingAlgorithms;
	LogService logService;
	ReportEvent userFeedback;
	IRMCalls plcMockRIS;
	FRResponse plcMockFRResponse;
	FRResponse fedeMockFRResponse;
	IRMCalls fedeMockRIS;
	// schedulers
	ScheduledExecutorService plcScheduler = Executors.newScheduledThreadPool(2);
	ScheduledExecutorService fedeScheduler = Executors.newScheduledThreadPool(2);
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(UserUpdateSliceTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing UserUpdateSliceTest...");
		
		// Creating mock classes
		logService = mock(LogService.class);
		// Creating mock classes for PLC
		plcMockRIS = mock(IRMCalls.class);
		plcMockFRResponse = mock(FRResponse.class);
		// Creating mock classes for FEDE
		fedeMockRIS = mock(IRMCalls.class);
		fedeMockFRResponse = mock(FRResponse.class);
		userFeedback = mock(ReportEvent.class);

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
		plcIRM.setTestbed("PlanetLab");
		fedeIRM.setTestbed("FEDERICA");
		plcIRM.getIrms().add(fedeIRM);
		plcIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(fedeIRM);
			
		// Stubbing mock classes
		when(plcMockRIS.getSlice(anyString())).thenReturn(createPLMockSlice());
		
		
		
		log.debug("UserUpdateSliceTest initialized");
		
	}

	
	
	
	/**
	 * Updates a slice with new resources.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void updateSliceWithNewResourcesTest() throws IOException {
		log.debug("Running updateSliceWithNewResourcesTest...");
		
		log.debug("Creating slice from PLC...");
			
		int result = plcIRM.updateSlice("session_01","slice_101", createRequestTopology("src/main/resources/ManualUpdateSlice.owl"),createNoviUser());
		
		log.debug("Result of the createSlice: "+result);
		assertTrue(result==-1);
	}
	
	
	
	
	private Set<GroupImpl> createRequestTopology(String path) throws IOException {
		// loading and getting imrespository util
		imru = new IMRepositoryUtilImpl();
		// Unbound Request
		String stringOwl = readFileAsString(path);
//		Set<Group> groups = imru.getGroupImplFromFile(stringOwl);
		Set<Group> groups = imru.getIMObjectsFromString(stringOwl, Group.class);

		Set<GroupImpl> groups1 = new HashSet<GroupImpl>();
		log.debug("Number of Groups in the request:" + groups.size());
		 for (Group group : groups) {
				 groups1.add((GroupImpl)group);
		 }
		log.debug("Number of Groups in the request:" + groups1.size());
		return groups1;

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
	
	private Reservation createPLMockSlice() throws IOException {
		log.debug("creating mock Slice...");
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/CurrentSlice.owl");
		Reservation reservation = imru.getReservationFromFile(stringOwl);
		return reservation;
	}
	
}
