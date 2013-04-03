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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Link;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.exceptions.MappingException;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.mapping.impl.PartitionedRequest;
import eu.novi.mapping.impl.SplittingAlgorithm;
import eu.novi.mapping.utils.GraphOperations;
import eu.novi.mapping.utils.IMOperations;
import eu.novi.mapping.utils.IRMOperations;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;


public class createSlicePboundTest {

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
	
	// logging
	/*								VM2 (PL)
									| (LINK2)
	 PARTIAL BOUND REQ: VM1-(link1)- LR1 - (Link4) - LR2 (dfn.erl.router1)  - (link5: 0/1/4,vmnic1) - VM4(dfn.erl.vserver2)
									|(LINK3)
								    VM3(planetlab2-novi.lab.netmode.ece.ntua.gr)

	Result VM1, VM2, VM3 PL REST FEDERICA
	*/
	
    private static final transient Logger log = 
    	LoggerFactory.getLogger(createSlicePboundTest.class);
	
	@SuppressWarnings("unchecked")
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing createSlicePboundTest...");
		
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
		plcEmbeddingAlgorithms.add(plEmbeddingAlgorithm);
		plcIRM.addEmbedding(plcEmbeddingAlgorithms);
		plcIRM.addIrms(new ArrayList<RemoteIRM>());
		plcIRM.setReportUserFeedback(userFeedback);
		plcIRM.setLogService(logService);
		
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
		
		// Assigning IRMs
		plcIRM.setTestbed("PlanetLab");
		fedeIRM.setTestbed("FEDERICA");
		plcIRM.getIrms().add(fedeIRM);
		plcIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(plcIRM);
		fedeIRM.getIrms().add(fedeIRM);

		
		//stubbing PLC mockFRResponse
		log.debug("stubbing PL mockFRResponse...");
		when(plcMockFRResponse.getTopology())
				.thenReturn(createPLCMockAvailableResources());
		when(plcMockFRResponse.hasError())
				.thenReturn(false);
				
		//stubbing PLC plcMockFPartCostRecordImpl		
		log.debug("stubbing PL FPartCostRecordImpl for every server node requested in PlanetLab...");
		Set<String>  serverURIsPLC = new  HashSet<String>();
		Set<String>  pserverURIsPLC = new  HashSet<String>();
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#vm1");
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#vm2");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+dfn-novi-ple1.x-win.dfn.de");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+novilab.elte.hu");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+planetlab2-novi.lab.netmode.ece.ntua.gr");
	    pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+smilax2.man.poznan.pl");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+planetlab1-novi.lab.netmode.ece.ntua.gr");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+smilax1.man.poznan.pl");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+smilax3.man.poznan.pl");
		pserverURIsPLC.add("urn:publicid:IDN+novipl:novi+node+smilax4.man.poznan.pl");
		when (plcMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(9);
		when (plcMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.15);
		when (plcMockFPartCostRecordImplSer.takeFedeResNumb()).thenReturn(9);
		when (plcMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsPLC);
		//when (fedMockFPartCostRecordImplRout.getFederableResourceURIs()).thenReturn(pserverURIsPLC);
			
		log.debug("stubbing PL FPartCostRecordImpl for every router node requested in PlanetLab...");
		Set<String>  routerURIsPLC = new  HashSet<String>();
		routerURIsPLC.add("http://fp7-novi.eu/im.owl#lrouter1");
		when (plcMockFPartCostRecordImplRout.takeAvailResNumber()).thenReturn(0);
		when (plcMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplRout.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsPLC);
		
		log.debug("stubbing PL FPartCostRecordImplLink for every link requested in PlanetLab...");
		Set<String>  linkURIsPLC = new  HashSet<String>();
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link1-lrouter1-vm1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link1-vm1-lrouter1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link2-vm2-lrouter1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link2-lrouter1-vm2");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link3-lrouter1-vm3");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link3-vm3-lrouter1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link4-lrouter1-lrouter2");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link4-lrouter2-lrouter1");
		when (plcMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(0);
		when (plcMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplLink.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplLink.getResourceURIs()).thenReturn(linkURIsPLC);
	
		//stubbing FEDE FEDMockFPartCostRecordImpl		
		log.debug("stubbing FED FPartCostRecordImpl for every server node requested in FEDERICA...");
		Set<String>  serverURIsFED = new  HashSet<String>();
		serverURIsFED.add("http://fp7-novi.eu/im.owl#vm1");
		serverURIsFED.add("http://fp7-novi.eu/im.owl#vm2");
		when (fedMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(3);
		when (fedMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.55);
		when (fedMockFPartCostRecordImplSer.takeFedeResNumb()).thenReturn(0);
		when (fedMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsFED);
		
	    log.debug("stubbing FED FPartCostRecordImpl for every router node requested in FEDERICA...");
		Set<String>  routerURIsFED = new  HashSet<String>();
		Set<String>  prouterURIsFED = new  HashSet<String>();
		routerURIsFED.add("http://fp7-novi.eu/im.owl#lrouter1");
		prouterURIsFED.add("urn:publicid:IDN+federica.eu+node+psnc.poz.router1");
		when (fedMockFPartCostRecordImplRout.takeAvailResNumber()).thenReturn(3);
		when (fedMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.1);
		when (fedMockFPartCostRecordImplRout.takeFedeResNumb()).thenReturn(1);
		when (fedMockFPartCostRecordImplRout.hasError()).thenReturn(false);
		when (fedMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsFED);
		//when (fedMockFPartCostRecordImplRout.getFederableResourceURIs()).thenReturn(prouterURIsFED);
	
		log.debug("stubbing FED FPartCostRecordImplLink for every link requested in FEDERICA...");
		Set<String>  linkURIsFED = new  HashSet<String>();
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-lrouter1-vm1");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-vm1-lrouter1");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link2-vm2-lrouter1");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link2-lrouter1-vm2");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link3-lrouter1-vm3");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link3-vm3-lrouter1");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link4-lrouter1-lrouter2");
		linkURIsFED.add("http://fp7-novi.eu/im.owl#link4-lrouter2-lrouter1");
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
		when(fedeMockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				.thenReturn(fedeMockFRResponse);
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
		
		log.debug("createSlicePboundTest initialized");
		
	}


	@Test
		public void splitPartialBound() throws IOException {
		log.debug("Running splitPartialBoundTest...");
	
		SplittingAlgorithm splitAlgo =  new SplittingAlgorithm();
		Collection<GroupImpl> groups = createRequestTopology("src/main/resources/PartialBoundRequest.owl");
		Topology newTop =  IMOperations.getVirtualRequest(groups);
		Set<Platform> platforms  = IMOperations.getPlatforms(groups, newTop);
		
		Topology[] splitResources = IRMOperations.checkBoundUnboundRequest(newTop);		
		Topology boundedTopology = splitResources[0];
		Topology unboundedTopology = splitResources[1];

		Topology platformUnboundTopology = new TopologyImpl("platformUnboundResources");
		Set<Topology> partialTopologies = checkPlatformBoundResources(unboundedTopology,platforms,platformUnboundTopology);
				
		
		SparseMultigraph<NodeImpl, LinkImpl> virtualRequestToSplit =  GraphOperations.translateIMToGraph(newTop,true);
		PartitionedRequest subGraphs = new PartitionedRequest();
		
		try{
		subGraphs = splitAlgo.split(virtualRequestToSplit,boundedTopology.getContains(),unboundedTopology.getContains(),partialTopologies,
									plcMockRIS.findPartitioningCost(null, newTop),plcIRM.getIrms());	
					
		Set<Platform> partialSplitPlatforms  =  subGraphs.getPartialPlatforms();
	//	printPlatform(partialSplitPlatforms);
			Iterator<Platform> iteratorTop = partialSplitPlatforms.iterator();
			
			while(iteratorTop.hasNext()) {
			Platform platform = iteratorTop.next();
			Set<Resource> pboundResources  = platform.getContains();
					Iterator<Resource> iteratorInner = pboundResources.iterator();
					while(iteratorInner.hasNext()) {
					Resource res = iteratorInner.next();
						if (res instanceof VirtualNode){
							// if (res.toString().toLowerCase().contains("vm1")) {
							// assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#PlanetLab");
							// } 
							// else if (res.toString().toLowerCase().contains("vm2")){ 
							// assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#PlanetLab");
							// } 
							// else 
							if (res.toString().toLowerCase().contains("vm3")){
							assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#PlanetLab");
							} 
							else if (res.toString().toLowerCase().contains("vm4")){
							assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#FEDERICA");
							} 
							else if  (res.toString().toLowerCase().contains("lrouter1")){ 
							assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#FEDERICA");
							} 
							else if (res.toString().toLowerCase().contains("lrouter2")){
							assertEquals(platform.toString(),"http://fp7-novi.eu/im.owl#FEDERICA");
							} 
						}
					}
					
			}
				
	
		} catch (MappingException e){
		log.debug("Error in splitting request: " + e.getMessage());
		}
		
	}

	
	/**
	 * Creates a slice with unbounded resources: 3 totally unbound resources
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	//@Test
		@SuppressWarnings("unchecked")
		public void createSlicePbound() throws IOException {
		log.debug("Running createSlicePboundTest...");
		
		log.debug("Creating slice from PLC...");
		when(plcMockRIS.checkResources(any(String.class), any(Set.class),any(NOVIUserImpl.class)))
			.thenReturn(getPLCMockResourceIDs(9));
		int result = plcIRM.createSlice("sessionID",createRequestTopology("src/main/resources/PartialBoundRequest.owl"),createNoviUser());
		log.debug("Result of the createSlice: "+ result);
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
	
	private Set<Topology> checkPlatformBoundResources(
			Topology unboundedTopology, Set<Platform> platforms, Topology platformUnboundTopology) {
		
		Set<Topology> result = new HashSet<Topology>();
		
		// Create one Topology for each Platform in the request
		for (Platform platform : platforms) {
			if (!IMOperations.isSetEmpty(platform.getContains())) {
				Topology t = new TopologyImpl(IMOperations.getId(platform.toString()));
				Set<Resource> resToAdd = new HashSet<Resource>();
				for (Resource r : platform.getContains()) {
					resToAdd.add(r);
					// remove the resource from the unboundedTopology
					unboundedTopology.getContains().remove(r);
				}
				t.setContains(resToAdd);
				result.add(t);
			}
		}
		
		// Add no platform resources to platformUnboundTopology
		Set<Resource> resToAdd = new HashSet<Resource>();
		for (Resource res : unboundedTopology.getContains()) {
			boolean found=false;
			for (Topology platformT : result) {
				for (Resource platformR : platformT.getContains()) {
					if (IMOperations.getId(res.toString())
							.equals(IMOperations.getId(platformR.toString()))) {
						found=true;
						break;
					}		
				}
			}
			// Links should not be considered as platform unbound resources
			if (!found && !(res instanceof Link)) {
				resToAdd.add(res);
			}
		}
		platformUnboundTopology.setContains(resToAdd);
		
		return result;


	}
	
	
}
