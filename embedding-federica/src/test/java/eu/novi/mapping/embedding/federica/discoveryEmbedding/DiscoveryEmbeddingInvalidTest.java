/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica.discoveryEmbedding;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmGSP;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmNCM;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.im.policy.impl.NOVIUserImpl;

//UNSUCCESFUL  EMBEDDING REQUESTS
public class DiscoveryEmbeddingInvalidTest {
	EmbeddingAlgorithmGSP embeddingAlgorithmGSP;
	EmbeddingAlgorithmNCM embeddingAlgorithmNCM;
	EmbeddingAlgorithmFEDERICA embeddingAlgorithmFEDERICA;
	SparseMultigraph<NodeImpl, LinkImpl> request;
	SparseMultigraph<NodeImpl, LinkImpl> substrate;
	LogService logService;
	ReportEvent userFeedback;
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	IRMCalls mockRIS;
	FRResponse mockFRResponse;
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(DiscoveryEmbeddingInvalidTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.info("Initializing FEDERICA Embedding test...");
		
		embeddingAlgorithmFEDERICA =  new EmbeddingAlgorithmFEDERICA();
		embeddingAlgorithmGSP = new EmbeddingAlgorithmGSP();
		embeddingAlgorithmNCM = new EmbeddingAlgorithmNCM();
		// Creating mock classes
		logService = mock(LogService.class);
		userFeedback = mock(ReportEvent.class);
		((EmbeddingAlgorithmFEDERICA) embeddingAlgorithmFEDERICA).setLogService(logService);
		((EmbeddingAlgorithmFEDERICA) embeddingAlgorithmFEDERICA).setUserFeedback(userFeedback);
		
			//stubbing mockRIS
		mockRIS = mock(IRMCalls.class);
		mockFRResponse = mock(FRResponse.class);
		((EmbeddingAlgorithmFEDERICA) embeddingAlgorithmFEDERICA).setResourceDiscovery(mockRIS); 	
		when(mockRIS.getSubstrateAvailability(any(String.class)))
				.thenReturn(mockFRResponse);
		log.debug("stubbing mockFRResponse...");
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/FEDERICATopology.owl"));
		when(mockFRResponse.hasError())
				.thenReturn(false);
		log.info("FEDERICA Embedding test initialized");
	}
	@Test
	public void incorrectResourceDiscoveryTest() throws IOException {
			log.debug("stubbing mockFRResponse...");
			when(mockFRResponse.hasError()).thenReturn(true);
			when(mockFRResponse.getUserFeedback()).thenReturn("component constraints are not met");
			Topology req = createTopology("src/main/resources/FEDERICARequest.owl");
			List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100", (GroupImpl) req, createNoviUser());
			assertNull(result);
	}
	

	@Test
	public void incorrectBidirectionalTest() throws IOException {
		
		Topology req = createTopology ("src/main/resources/IncorrectBidirectionalRequest.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
		
	}
	

	@Test
	public void noRequestTest() {
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",null, createNoviUser());
		assertNull(result);
		

	}

	@Test
	public void vmVirtualRoleTest() throws IOException {
		
		Topology req = createTopology("src/main/resources/IncorrectVmVirtualRoleRequest.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	
	@Test
	public void disconnectedSubstrateTest() throws IOException {
		when(mockFRResponse.getTopology())
				.thenReturn(createTopology("src/main/resources/DisconnectedSubstrate.owl"));
		when(mockRIS.findResources(any(String.class), any(Topology.class),any(NOVIUserImpl.class)))
				 .thenReturn(mockFRResponse);
				 				
		Topology req = createTopology("src/main/resources/FEDERICARequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
				
	}
	
	@Test
	public void invalidPartialBoundRequestTest1() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid1.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
		
	}
	
	@Test
	public void invalidPartialBoundRequestTest2() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid2.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
		
	}
	
	@Test
	public void invalidPartialBoundRequestTest3() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid3.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
		
	}
	
	@Test
	public void invalidPartialBoundRequestTest4() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid4.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
		

	}
	
	
	@Test
	public void invalidPartialBoundRequestTest5() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid5.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);

	}
	
	@Test
	public void invalidPartialBoundRequestTest6() throws IOException {
		
		Topology req = createTopology("src/main/resources/PartialBoundRequestInvalid6.owl");
		
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",(GroupImpl)req, createNoviUser());
		assertNull(result);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
		// loading and getting from imrespository util
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
