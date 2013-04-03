/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.splitting;

import static org.junit.Assert.assertFalse;
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
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
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
import eu.novi.resources.discovery.response.FRResponseImp;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;
public class createSliceUnbound0aTest {

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
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	Topology fedsubstrate;
	// schedulers
	ScheduledExecutorService plcScheduler = Executors.newScheduledThreadPool(2);
	ScheduledExecutorService fedeScheduler = Executors.newScheduledThreadPool(2);
	
	// logging
	/* Two slivers connected via one link - fully unbound->should be all in federica*/
    private static final transient Logger log = 
    	LoggerFactory.getLogger(createSliceUnbound0aTest.class);
	
	@SuppressWarnings("unchecked")
	@Before
	public <T> void initialize() throws IOException {
	
		fedsubstrate = createTopology("src/main/resources/FEDERICATopology.owl");
		log.debug("Initializing createSliceUnboundTest...");
		// Creating mock classes
		logService = mock(LogService.class);
		// Creating mock classes for PLC
		plcMockRIS = mock(IRMCalls.class);
		plcMockFRResponse = mock(FRResponse.class);
		ReserveResponse plcMockReserveResponse = mock(ReserveResponse.class);
		// Creating mock classes for FEDE
		fedeMockRIS = mock(IRMCalls.class);
		fedeMockFRResponse = mock(FRResponse.class);
		ReserveResponse fedeMockReserveResponse = mock(ReserveResponse.class);
		userFeedback = mock(ReportEvent.class);
		FPartCostTestbedResponseImpl  plcMockFPartCostTestbedResponse =  mock(FPartCostTestbedResponseImpl.class);
		FPartCostTestbedResponseImpl  fedMockFPartCostTestbedResponse =  mock(FPartCostTestbedResponseImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplSer = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplRout = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl plcMockFPartCostRecordImplLink = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplSer = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplRout = mock(FPartCostRecordImpl.class);
		FPartCostRecordImpl fedMockFPartCostRecordImplLink = mock(FPartCostRecordImpl.class);
		Vector<FPartCostRecordImpl> plcTesbedNodeRecordVector =  new  Vector<FPartCostRecordImpl>();
		Vector<FPartCostRecordImpl> plcTesbedLinkRecordVector =  new  Vector<FPartCostRecordImpl>();
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
		plEmbeddingAlgorithm.setResourceDiscovery(plcMockRIS);
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
		fedAlgo.setResourceDiscovery(fedeMockRIS);
		fedeEmbeddingAlgorithms.add(fedAlgo);
		fedeIRM.addEmbedding(fedeEmbeddingAlgorithms);
		fedeIRM.addIrms(new ArrayList<RemoteIRM>());
		fedeIRM.setReportUserFeedback(userFeedback);
		fedeIRM.setLogService(logService);
		fedeIRM.setScheduler(plcScheduler);
		
		// Assigning IRMs
		plcIRM.setTestbed("PlanetLab");
		fedeIRM.setTestbed("FEDERICA");
		plcIRM.getIrms().add(fedeIRM);
		plcIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(fedeIRM);
				
		//stubbing PLC mockRIS
		log.debug("stubbing PL mockRIS...");
		when(plcMockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				.thenReturn(plcMockFRResponse);
		when(plcMockRIS.getResource(any(String.class)))
				.thenReturn(getPLCMockResource());
		when(plcMockRIS.getResources(any(Set.class)))
				.thenReturn(getPLCMockResources());
		when(plcMockRIS.checkResources(any(String.class), any(Set.class),any(NOVIUserImpl.class)))
				.thenReturn(getPLCMockResourceIDs(23));
		when(plcMockRIS.reserveSlice(any(String.class), any(Topology.class),anyInt(),any(NOVIUserImpl.class)))
				.thenReturn(plcMockReserveResponse);
		
		//stubbing PLC mockFRResponse
		log.debug("stubbing PL mockFRResponse...");
		when(plcMockFRResponse.getTopology())
				.thenReturn(createPLCMockAvailableResources());
		when(plcMockFRResponse.hasError())
				.thenReturn(false);
				
		//stubbing PLC plcMockFPartCostRecordImpl		
		log.debug("stubbing PL FPartCostRecordImpl for every server node requested in PlanetLab...");
		Set<String>  serverURIsPLC = new  HashSet<String>();
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#sliver1");
		when (plcMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(8);
		when (plcMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.35);
		when (plcMockFPartCostRecordImplSer.takeFedeResNumb()).thenReturn(8);
		when (plcMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsPLC);
		
		log.debug("stubbing PL FPartCostRecordImpl for every router node requested in PlanetLab...");
		Set<String>  routerURIsPLC = new  HashSet<String>();
		routerURIsPLC.add("http://fp7-novi.eu/im.owl#lrouter");
		when (plcMockFPartCostRecordImplRout.takeAvailResNumber()).thenReturn(0);
		when (plcMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplRout.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsPLC);
		
	
		log.debug("stubbing PL FPartCostRecordImplLink for every link requested in PlanetLab...");
		Set<String>  linkURIsPLC = new  HashSet<String>();
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#virtualLink1-sliver1-lrouter");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#virtualLink1-lrouter-sliver1");
		when (plcMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(0);
		when (plcMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplLink.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplLink.getResourceURIs()).thenReturn(linkURIsPLC);
	
		//stubbing FEDE FEDMockFPartCostRecordImpl		
		log.debug("stubbing FED FPartCostRecordImpl for every server node requested in FEDERICA...");
		Set<String>  serverURIsFED = new  HashSet<String>();
		serverURIsFED.add("http://fp7-novi.eu/im.owl#sliver1");
		when (fedMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(0);
		when (fedMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.0);
		when (fedMockFPartCostRecordImplSer.takeFedeResNumb()).thenReturn(0);
		when (fedMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsFED);
		
		log.debug("stubbing FED FPartCostRecordImpl for every router node requested in FEDERICA...");
		Set<String>  routerURIsFED = new  HashSet<String>();
		routerURIsFED.add("http://fp7-novi.eu/im.owl#lrouter");
		when (fedMockFPartCostRecordImplRout.takeAvailResNumber()).thenReturn(1);
		when (fedMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.1);
		when (fedMockFPartCostRecordImplRout.takeFedeResNumb()).thenReturn(1);
		when (fedMockFPartCostRecordImplRout.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsFED);
		when (fedMockFPartCostRecordImplRout.takeFedeResNumb()).thenReturn(1);
		
	
		log.debug("stubbing FED FPartCostRecordImplLink for every link requested in FEDERICA...");
		Set<String>  linkURIsFED = new  HashSet<String>();
		linkURIsFED.add("http://fp7-novi.eu/im.owl#virtualLink1-sliver1-lrouter");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#virtualLink1-lrouter-sliver1");
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
		
		Vector<FPartCostTestbedResponseImpl> splittingCosts = new Vector<FPartCostTestbedResponseImpl>();
		splittingCosts.add(plcMockFPartCostTestbedResponse);
		splittingCosts.add(fedMockFPartCostTestbedResponse);
				
		when(plcMockRIS.findPartitioningCost(any(String.class), any(Topology.class)))
				 .thenReturn(splittingCosts);	
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//stubbing PLC mockReserveResponse
		log.debug("stubbing PL mockReserveResponse...");
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
		when(fedeMockRIS.getSubstrateAvailability(any(String.class)))
				.thenReturn(createRISresp(fedsubstrate));
		when(fedeMockRIS.getResource(any(String.class)))
				.thenReturn(getFEDEMockResource());
		when(fedeMockRIS.getResources(any(Set.class)))
				.thenReturn(getFEDEMockResources());
		when(fedeMockRIS.reserveSlice(any(String.class), any(Topology.class),anyInt(),any(NOVIUserImpl.class)))
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
		
		log.debug("createSliceTestUnbound initialized");
		
	}


	
	/**
	 * Creates a slice with unbounded resources: 3 totally unbound resources
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
		public void createSliceTest0Unbound() throws IOException {
		log.debug("Running createSliceTestUnbound...");
		
		log.debug("Creating slice from PLC...");
		int result = plcIRM.createSlice("sessionID",createRequestTopology("src/main/resources/unBound1NodesConstraints1router.owl"),createNoviUser());
		log.debug("Result of the createSlice: "+result);
		assertFalse(result==-1);
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
	
	private Topology createPLCMockAvailableResources() throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/PLETopology.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private Topology createFEDEMockAvailableResources() throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString("src/main/resources/FEDERICATopology.owl");
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private Resource getPLCMockResource() throws IOException {
		Topology substrate = createPLCMockAvailableResources();
		return substrate.getContains().iterator().next();
	}
	
	private Set<Resource> getPLCMockResources() throws IOException {
		Set<Resource> resourceSet = new HashSet<Resource>();
		// Dummy addition of 23 resources
		for (int i=0; i<23; i++) {
			Node res = (Node) new NodeImpl("dummyNode"+i);
			resourceSet.add(res);
		}
		return resourceSet;
	}
	
	private Set<String> getPLCMockResourceIDs(int num) throws IOException {
		Set<String> idSet = new HashSet<String>();
		// Dummy addition of 23 resource ID's
		for (int i=0; i<num; i++) {
			idSet.add("dummyNode"+i);
		}
		return idSet;
	}
	
	private Resource getFEDEMockResource() throws IOException {
		Topology substrate = createFEDEMockAvailableResources();
		return substrate.getContains().iterator().next();
	}
	
	private Set<Resource> getFEDEMockResources() throws IOException {
		Set<Resource> resourceSet = new HashSet<Resource>();
		Topology substrate = createFEDEMockAvailableResources();
		resourceSet.addAll(substrate.getContains());
		return resourceSet;
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
	
	private Topology createTopology(String file) throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString(file);
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private FRResponseImp createRISresp(Topology top){
		FRResponseImp response = new FRResponseImp();
		response.setTopology(top);
		return response;
	}
}
