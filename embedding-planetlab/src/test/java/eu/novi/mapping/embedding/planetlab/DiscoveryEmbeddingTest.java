/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.planetlab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.Topology;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.resources.discovery.response.FRFailedMess;

public class DiscoveryEmbeddingTest {
	
	EmbeddingAlgorithmInterface embeddingAlgorithm;
	SparseMultigraph<NodeImpl, LinkImpl> request;
	SparseMultigraph<NodeImpl, LinkImpl> substrate;
	IRMCalls mockRIS;
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	FRResponse mockFRResponse;
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(DiscoveryEmbeddingTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.info("Initializing PLC Embedding test...");
		
		embeddingAlgorithm = new EmbeddingAlgorithmGNM();
		// Creating mock classes
		LogService logService = mock(LogService.class);
		ReportEvent userFeedback = mock(ReportEvent.class);
		((EmbeddingAlgorithmGNM) embeddingAlgorithm).setLogService(logService);
		((EmbeddingAlgorithmGNM) embeddingAlgorithm).setUserFeedback(userFeedback);
		
		//stubbing mockRIS
		mockRIS = mock(IRMCalls.class);
		mockFRResponse = mock(FRResponse.class);
		((EmbeddingAlgorithmGNM) embeddingAlgorithm).setResourceDiscovery(mockRIS); 	
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				.thenReturn(mockFRResponse);
		log.debug("stubbing mockFRResponse...");
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/PLETopology.owl"));
		when(mockFRResponse.hasError())
				.thenReturn(false);
		when(mockFRResponse.getUserFeedback()).thenReturn("Error Response from RIS");
				
		log.info("PLC Embedding test initialized");
	}

	@Test
	public void embeddingTest() throws IOException {

		log.info("Running Embedding test for "+embeddingAlgorithm.getTestbedName());
		log.info("Algorithm's name: "+embeddingAlgorithm.getAlgorithmName());
		Topology req = createTopology("src/main/resources/PLCRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100", (GroupImpl) req, createNoviUser());
		
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		
		log.info("PLC Embedding test successfully done");
		
	}
	
	@Test
	public void noSubstrateTest() {
		// setting error in findResources response
		
		when(mockFRResponse.hasError()).thenReturn(true);
		when(mockFRResponse.getUserFeedback()).thenReturn("userFeedback message");
		Map<Resource,FRFailedMess> map = new HashMap<Resource,FRFailedMess>();
		map.put(new NodeImpl("MockResource"), FRFailedMess.WAS_NOT_FOUND_ANY_AVAILABLE_NODE_NON_FUNCTIONAL);
		when(mockFRResponse.getFailedResources()).thenReturn(map);
		when(mockFRResponse.getTopology()).thenReturn(null);
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				 .thenReturn(mockFRResponse);
				
		Topology req = null;
		try {
		req = createTopology("src/main/resources/PLCRequest.owl");

		} catch (Exception e) {
			log.error("Concert exception caught: " + e.getMessage());
		}
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl) req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void noRequestTest() {
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100", null, createNoviUser());
		assertNull(result);
	}
	

	 
	@Test
	public void noComponentsRequestTest() throws IOException {
		Topology req = null;
		try {
		req = createTopology("src/main/resources/NoComponentsRequest.owl");

		} catch (Exception e) {
			log.error("Concert exception caught: " + e.getMessage());
		}
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl) req, createNoviUser());
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
	}
	 
	@Test
	public void tooBigRequestTest() throws IOException {
		Topology req = createTopology("src/main/resources/BigRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void tooPowerfulRequestTest() throws IOException {
		Topology req = createTopology("src/main/resources/PowerfulRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidTopologyCoresTest() throws IOException {
		Topology req = createTopology("src/main/resources/PLETopologyInvalidCores.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	//@Test
	public void invalidTopologySpeedTest() throws IOException {
		Topology req = createTopology("src/main/resources/PLETopologyInvalidSpeed.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidTopologyStorageTest() throws IOException {
		Topology req = createTopology("src/main/resources/PLETopologyInvalidStorage.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidTopologyMemoryTest() throws IOException {
		Topology req = createTopology("src/main/resources/PLETopologyInvalidMemory.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidTopologyHasLinksTest() throws IOException {
		Topology req = createTopology("src/main/resources/unboundRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidTopologyHasLinks2Test() throws IOException {
		Topology req = createTopology("src/main/resources/unBound2NodesConstraints1router.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	
	@Test
	public void noComponentsTest() throws IOException {
		Topology req = createTopology("src/main/resources/NoComponentsRequest.owl");
		
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/NoComponentsTopology.owl"));
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				 .thenReturn(mockFRResponse);
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
 	@Test
	public void partialSubstrateComponentsTest() throws IOException {
		Topology req = createTopology("src/main/resources/PLCRequest.owl");
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/PartialComponentsTopology.owl"));
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				 .thenReturn(mockFRResponse);
				 
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	 
 	@Test
	public void embeddingPartialBoundRequestTest() throws IOException {
		Topology req = createTopology("src/main/resources/PartialBoundRequest.owl");
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/PLETopology.owl"));
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				 .thenReturn(mockFRResponse);
				 
		
		// correct embedding
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
	} 
	
	@Test
	public void invalidPartialBoundRequestTest1() throws IOException {
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid1.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	@Test
	public void invalidPartialBoundRequestTest2() throws IOException {
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid2.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	private Topology createTopology(String file) throws IOException {
		log.debug("creating mock available Physical Resources...");
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString(file);
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private static NOVIUserImpl createNoviUser() {
		NOVIUserImpl noviUser = new NOVIUserImpl("novi_user");
		noviUser.setFirstName("Novi");
		noviUser.setLastName("User");
		noviUser.setHasSessionKey("session_key");
		return noviUser;
	}
	
}
