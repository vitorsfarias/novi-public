/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;

//SUCCESFUL EMBEDDING REQUESTS
public class EmbeddingTest {
	EmbeddingAlgorithmGSP embeddingAlgorithmGSP;
	EmbeddingAlgorithmNCM embeddingAlgorithmNCM;
	EmbeddingAlgorithmFEDERICA embeddingAlgorithmFEDERICA;
	SparseMultigraph<NodeImpl, LinkImpl> request;
	SparseMultigraph<NodeImpl, LinkImpl> substrate;
	LogService logService;
	ReportEvent userFeedback;
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(EmbeddingTest.class);
	
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
		request = createGraph("src/main/resources/FEDERICARequest.owl", true);
		substrate = createGraph("src/main/resources/FEDERICATopology.owl", true);

		log.info("FEDERICA Embedding test initialized");
	}
	
	@Test
	public void embeddingSelectNCMAlgoTest() throws IOException {
		request = createGraph("src/main/resources/FEDERICARequest.owl", true);
		log.info("Calling select method ...");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",request, substrate);
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
			
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
			
		}
		// We have 2 link mapped
		assertEquals(2,result.get(1).size());	
		

	}
	
	@Test
	public void embeddingSelectGSPAlgoTest() throws IOException {
		request = createGraph("src/main/resources/DisconnectedRequest.owl", true);
		log.info("Calling select method ...");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmFEDERICA.embed("sid100",request, substrate);
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// We have no links mapped
		assertEquals(0,result.get(1).size());	
	}
	
	@Test
	public void embeddingTest() throws IOException {
	
		/** GSP **/
		log.info("Calling embed method for embeddingAlgorithmGSP...");
		log.info("Algorithm's name: "+embeddingAlgorithmGSP.getAlgorithmName());
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		//We have node & link mapping
		assertEquals(2,result.size());
		//We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		//Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		//We have 2 link mapped
		assertEquals(2,result.get(1).size());
		
		/** MP-SP **/
		log.info("Calling embed method for embeddingAlgorithmNCM...");
		log.info("Algorithm's name: "+embeddingAlgorithmNCM.getAlgorithmName());	
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		}
		// We have 2 link mapped
		assertEquals(2,result.get(1).size());
		
		
		/** NCM **/
		EmbeddingConstants.LINK_MAPPING="MCF";
		log.info("Calling embed method for embeddingAlgorithmNCM...");
		log.info("Algorithm's name: "+embeddingAlgorithmNCM.getAlgorithmName());	
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		}
		// We have 2 link mapped
		assertEquals(2,result.get(1).size());
		EmbeddingConstants.LINK_MAPPING="SHORTEST_PATH";
		log.info("FEDERICA Embedding test successfully done");

		
	}
	
	@Test
	public void correctBidirectionalTest() throws IOException {
		
		request = createGraph("src/main/resources/CorrectBidirectionalRequest.owl", true);
		
		//** GSP **/
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
				assertEquals(1, iface.getImplementedBy().size());
			}
			for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
				assertEquals(1, iface.getImplementedBy().size());
			}
		}
		// We have 2 links mapped
		assertEquals(2,result.get(1).size());
		
		/** NCM **/
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// Sometimes the node mapping is done (sometimes fails)
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
				log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
				assertEquals(1, iface.getImplementedBy().size());
			}
			for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
				assertEquals(1, iface.getImplementedBy().size());
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		}
		
		// We have 2 links mapped
		assertEquals(2,result.get(1).size());
		
	}
	
	
	@Test
	public void vmRouterVmTest() throws IOException {
		
		request = createGraph("src/main/resources/VmRouterVmRequest.owl", true);
		
		/** GSP **/
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 3 nodes mapped
		assertEquals(3,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		// We have 4 links mapped
		assertEquals(4,result.get(1).size());
		
		/** NCM **/
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 3 nodes mapped
		assertEquals(3,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		}
		
		// We have 4 links mapped
		assertEquals(4,result.get(1).size());

	}
	
	@Test
	public void noComponentsRequestTest() throws IOException {
		
		request = createGraph("src/main/resources/NoComponentsRequest.owl", true);
		
		/** GSP **/
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 3 nodes mapped
		assertEquals(3,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		// We have 4 links mapped
		assertEquals(4,result.get(1).size());
		
		/** NCM **/
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 3 nodes mapped
		assertEquals(3,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		}
		// We have 4 links mapped
		assertEquals(4,result.get(1).size());
	}

	@Test
	public void disconnectedRequestTest() throws IOException {
		
		request = createGraph("src/main/resources/DisconnectedRequest.owl", true);
		
		//** GSP **//
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// We have no links mapped
		assertEquals(0,result.get(1).size());
		
		/** NCM **/
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// RVINE algorithm is based on request links. 
		// If no links, there are no mapping.
		assertNull(result);
				
	}
	
	@Test @Ignore
	public void federationVmRequestTest() throws IOException {
			
		request = createGraph("src/main/resources/FederationVmRequest.owl", true);
		
		/** GSP **/
		log.info("Calling embed method for embeddingAlgorithmGSP...");
		log.info("Algorithm's name: "+ embeddingAlgorithmGSP.getAlgorithmName());
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request,
				substrate,userFeedback,null,logService,"FEDERICA");
		//We have node & link mapping
		assertEquals(2,result.size());
		//We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		//Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		//We have 2 link mapped
		assertEquals(2,result.get(1).size());
		
		/** NCM **/
		log.info("Calling embed method for embeddingAlgorithmNCM...");
		log.info("Algorithm's name: "+embeddingAlgorithmNCM.getAlgorithmName());	
		result = embeddingAlgorithmNCM.embedNCM(request,substrate,userFeedback,
				null,logService,"FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		
		}
		// We have 2 link mapped
		assertEquals(2,result.get(1).size());
		System.exit(0);
	}
	
	@Test
	public void complexRequestTest() throws IOException {
		
		request = createGraph("src/main/resources/ComplexRequest.owl", true);
		
		/** GSP **/
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithmGSP.embedGSP(request, substrate, userFeedback, null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 5 nodes mapped
		assertEquals(5,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		// We have 8 links mapped
		assertEquals(8,result.get(1).size());
		
		/** NCM **/
		result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
		// We have node & link mapping
		assertEquals(2,result.size());
		// We have 5 nodes mapped
		assertEquals(5,result.get(0).size());
		// Virtual interfaces has implementedBy set
		for (ResourceImpl node : result.get(0).keySet()) {
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasOutboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasOutboundInterfaces()) {
					log.info(((Node) node).toString()+" mapped to: "+ result.get(0).get(node));
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
			if (!EmbeddingOperations.isSetEmpty(((Node) node).getHasInboundInterfaces())) {
				for (Interface iface : ((Node) node).getHasInboundInterfaces()) {
					assertEquals(1, iface.getImplementedBy().size());
				}
			}
		}
		
		for (ResourceImpl link : result.get(1).keySet()) {
			log.info(link+" mapped to: "+ result.get(1).get(link)+ " that contains");
			Set<Resource>  resources = ((PathImpl)result.get(1).get(link)).getContains();
			for (Resource res: resources) {
				log.info("element " + res);
			}
		
		}
		// We have 8 links mapped
		assertEquals(8,result.get(1).size());
		
		

	}
	
	private SparseMultigraph<NodeImpl, LinkImpl> createGraph(String path, boolean bidirectional) throws IOException {
		// loading and getting imrespository util
		imru = new IMRepositoryUtilImpl();
		// Unbound Request
		String stringOwl = readFileAsString(path);
		TopologyImpl requestTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		return translateIMToGraph(requestTopology, bidirectional);
	}
	
	/**
	 * Graph 
	 * 
	 * @param toTranslate
	 * @param bidirectional 
	 * @return
	 */
	private SparseMultigraph<NodeImpl, LinkImpl> translateIMToGraph(GroupImpl toTranslate, boolean bidirectional) {
		// Graph to return
		SparseMultigraph<NodeImpl, LinkImpl> graph = new SparseMultigraph<NodeImpl, LinkImpl>();
		// HashMap for links and its endpoints
		HashMap<Link,Node[]> linksAndEndpoints = new HashMap<Link, Node[]>();
		
		try {
			Iterator<Resource> resourceIt = ((Group) toTranslate).getContains().iterator();
			while (resourceIt.hasNext()) {
				Resource resource = resourceIt.next();
				if (resource instanceof Node) {
					// Add vertex to the graph
					graph.addVertex((NodeImpl) resource);
					Set<Interface> outIfaces = new HashSet<Interface>();
					Set<Interface> inIfaces = new HashSet<Interface>();
					// go through out-bound interfaces
					if (((Node) resource).getHasOutboundInterfaces()!=null) {
						outIfaces.addAll(((Node) resource).getHasOutboundInterfaces());
					}
					for (Interface iface : outIfaces) {
						Set<LinkOrPath> links = new HashSet<LinkOrPath>();
						if (iface.getIsSource()!=null) {
							links.addAll(iface.getIsSource());
						}
						for (LinkOrPath link : links) {
							if (link instanceof Link) {
								if (linksAndEndpoints.containsKey(link)) {
									Node[] pair = linksAndEndpoints.get(link);
									pair[0] = (Node)resource;
								} else {
									Node pair[] = new Node[2];
									pair[0] = (Node)resource;
									linksAndEndpoints.put((Link)link, pair);
								}
							}
						}
					}
					// go through in-bound interfaces
					if (((Node) resource).getHasInboundInterfaces()!=null) {
						inIfaces.addAll(((Node) resource).getHasInboundInterfaces());
					}
					for (Interface iface : inIfaces) {
						Set<LinkOrPath> links = new HashSet<LinkOrPath>();
						if (iface.getIsSink()!=null) {
							links.addAll(iface.getIsSink());
						}
						for (LinkOrPath link : links) {
							if (link instanceof Link) {
								if (linksAndEndpoints.containsKey(link)) {
									Node[] pair = linksAndEndpoints.get(link);
									pair[1] = (Node)resource;
								} else {
									Node pair[] = new Node[2];
									pair[1] = (Node)resource;
									linksAndEndpoints.put((Link)link, pair);
								}
							}
						}
					}
				}
				else if (resource instanceof Link) {
					// Nothing to do. No nodes or interfaces reachable
					log.debug("Nothing to do for Links");
				}
			}
			
			// Add edges to the graph
			Set<Entry<Link,Node[]>> set = linksAndEndpoints.entrySet();
			for (Entry<Link,Node[]> entry : set) {
				// Discard link if some endpoint is null 
				// or if there is already set the inverse link in the graph
				if (entry.getValue()[0]!=null && entry.getValue()[1]!=null) {
					if (bidirectional) {
						if (!existsInverseEdge(graph, entry.getValue()[0], entry.getValue()[1])) {
							graph.addEdge((LinkImpl)entry.getKey(),
									(NodeImpl)entry.getValue()[0],(NodeImpl)entry.getValue()[1],
									EdgeType.UNDIRECTED);
						}
						else {
							log.debug("Link "+entry.getKey().toString()+" discarded");
						}
					} else {
						graph.addEdge((LinkImpl)entry.getKey(),
								(NodeImpl)entry.getValue()[0],(NodeImpl)entry.getValue()[1],
								EdgeType.UNDIRECTED);
					}
				}
				else {
					log.debug("Link "+entry.getKey().toString()+" discarded");
				}
			}
			
		} catch (Exception e) {
			log.error("Error translating to graph", e);
		}
		
		return graph;
	}
	
	private boolean existsInverseEdge(SparseMultigraph<NodeImpl, LinkImpl> graph, Node first, Node second) {
		for (LinkImpl link : graph.getEdges()) {
			Pair<NodeImpl> pair = graph.getEndpoints(link);
			if (pair.getFirst().equals(second) && pair.getSecond().equals(first)) {
				return true;
			}
		}
		return false;
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
