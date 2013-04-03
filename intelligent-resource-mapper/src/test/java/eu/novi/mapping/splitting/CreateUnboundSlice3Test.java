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

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.exceptions.MappingException;
import eu.novi.mapping.impl.IRMEngine;
import eu.novi.mapping.impl.SplittingAlgorithm;
import eu.novi.mapping.utils.GraphOperations;
import eu.novi.mapping.utils.IRMOperations;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveResponse;
public class CreateUnboundSlice3Test {

	IRMEngine plcIRM;
	IRMEngine fedeIRM;
	List<EmbeddingAlgorithmInterface> plcEmbeddingAlgorithms;
	List<EmbeddingAlgorithmInterface> fedeEmbeddingAlgorithms;
	LogService logService;
	ReportEvent userFeedback;
	IRMCalls plcMockRIS;
	IRMCalls fedeMockRIS;
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(CreateUnboundSlice3Test.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing MidtermWorkshopTest...");
		
		// Creating mock classes
		//ReportEvent users = mock(ReportEvent.class);
		logService = mock(LogService.class);
		// Creating mock classes for PLC
		plcMockRIS = mock(IRMCalls.class);
		FRResponse plcMockFRResponse = mock(FRResponse.class);
		ReserveResponse plcMockReserveResponse = mock(ReserveResponse.class);
		// Creating mock classes for FEDE
		fedeMockRIS = mock(IRMCalls.class);
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
//		plcIRM.setSessionID("temporarySessionIDforPL");
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
//		fedeIRM.setSessionID("temporarySessionIDforFEDE");
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
				
		//stubbing PLC mockRIS
		log.debug("stubbing PL mockRIS...");
		when(plcMockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				.thenReturn(plcMockFRResponse);
		when(plcMockRIS.reserveSlice(any(String.class), any(Topology.class),anyInt(),any(NOVIUserImpl.class)))
				.thenReturn(plcMockReserveResponse);
		
		//stubbing PLC mockFRResponse
		when(plcMockFRResponse.hasError())
				.thenReturn(false);
				
		//stubbing PLC plcMockFPartCostRecordImpl		
		log.debug("stubbing PL FPartCostRecordImpl for every server node requested in PlanetLab...");
		Set<String>  serverURIsPLC = new  HashSet<String>();
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#sliver1");
		serverURIsPLC.add("http://fp7-novi.eu/im.owl#sliver2");
		when (plcMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(8);
		when (plcMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.35);
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
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link1-lrouter-sliver1");
		linkURIsPLC.add("http://fp7-novi.eu/im.owl#link2-lrouter-sliver2");
		when (plcMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(150000);
		when (plcMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.0);
		when (plcMockFPartCostRecordImplLink.hasError()).thenReturn(false);
		when (plcMockFPartCostRecordImplLink.getResourceURIs()).thenReturn(linkURIsPLC);
	
		//stubbing FEDE FEDMockFPartCostRecordImpl		
		 log.debug("stubbing FED FPartCostRecordImpl for every server node requested in FEDERICA...");
		  Set<String>  serverURIsFED = new  HashSet<String>();
		// serverURIsFED.add("http://fp7-novi.eu/im.owl#sliver1");
		// serverURIsFED.add("http://fp7-novi.eu/im.owl#sliver2");
		  when (fedMockFPartCostRecordImplSer.takeAvailResNumber()).thenReturn(0);
		  when (fedMockFPartCostRecordImplSer.getAverUtil()).thenReturn(0.0);
		  when (fedMockFPartCostRecordImplSer.hasError()).thenReturn(false);
		  when (fedMockFPartCostRecordImplSer.getResourceURIs()).thenReturn(serverURIsFED);
		
		 log.debug("stubbing FED FPartCostRecordImpl for every router node requested in FEDERICA...");
		 Set<String>  routerURIsFED = new  HashSet<String>();
		// routerURIsFED.add("http://fp7-novi.eu/im.owl#lrouter");
		 when (fedMockFPartCostRecordImplRout.takeAvailResNumber()).thenReturn(0);
		 when (fedMockFPartCostRecordImplRout.getAverUtil()).thenReturn(0.0);
		 when (fedMockFPartCostRecordImplRout.hasError()).thenReturn(false);
		 when (fedMockFPartCostRecordImplRout.getResourceURIs()).thenReturn(routerURIsFED);
		
		// log.debug("stubbing FED FPartCostRecordImplLink for every link requested in FEDERICA...");
		Set<String>  linkURIsFED = new  HashSet<String>();
		// linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-lrouter-sliver1");
		// linkURIsFED.add("http://fp7-novi.eu/im.owl#link1-sliver1-lrouter");
		// linkURIsFED.add("http://fp7-novi.eu/im.owl#link2-lrouter-sliver2");
		// linkURIsFED.add("http://fp7-novi.eu/im.owl#link2-sliver2-lrouter");
		 when (fedMockFPartCostRecordImplLink.takeAvailResNumber()).thenReturn(0);
		 when (fedMockFPartCostRecordImplLink.getAverUtil()).thenReturn(0.0);
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
		
	//	testbedResponseVector.add(plcMockFPartCostTestbedResponse);
		//testbedResponseVector.add(fedMockFPartCostTestbedResponse);
		
		
		Vector<FPartCostTestbedResponseImpl> splittingCosts = new Vector<FPartCostTestbedResponseImpl>();
		splittingCosts.add(plcMockFPartCostTestbedResponse);
		splittingCosts.add(fedMockFPartCostTestbedResponse);
		
		when(plcMockRIS.findPartitioningCost(any(String.class), any(Topology.class))).thenReturn(splittingCosts);
		
	}

	/**
	 * Creates a slice with unbounded resources: 3 totally unbound resources
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
		public void createSliceTestUnbound6() throws IOException {
		

		log.debug("Running createSliceTestUnbound...");
		SplittingAlgorithm splitAlgo =  new SplittingAlgorithm();
		Topology newTop = (Topology)createRequestTopology("src/main/resources/MidtermWorkshopRequest_unbound.owl");
		
		Topology[] splitResources = IRMOperations.checkBoundUnboundRequest(newTop);
		Topology boundedTopology = splitResources[0];
		Topology unboundedTopology = splitResources[1];
		SparseMultigraph<NodeImpl, LinkImpl> virtualRequestToSplit =  GraphOperations.translateIMToGraph(newTop,true);

		try{
		splitAlgo.split(virtualRequestToSplit,boundedTopology.getContains(),unboundedTopology.getContains(),null,
					plcMockRIS.findPartitioningCost(null, newTop),plcIRM.getIrms());
		} catch (MappingException e){
		log.debug("Error in splitting request: " + e.getMessage());
		}
		
	}
	
	private Topology createRequestTopology(String path) throws IOException {
		// loading and getting imrespository util
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString(path);
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;

	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

