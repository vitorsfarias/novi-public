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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
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
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;

public class EmbeddingTest {
	
	EmbeddingAlgorithmInterface embeddingAlgorithm;
	SparseMultigraph<NodeImpl, LinkImpl> request;
	SparseMultigraph<NodeImpl, LinkImpl> substrate;
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(EmbeddingTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.info("Initializing PLC Embedding test...");
		
		embeddingAlgorithm = new EmbeddingAlgorithmGNM();
		// Creating mock classes
		LogService logService = mock(LogService.class);
		ReportEvent userFeedback = mock(ReportEvent.class);
		((EmbeddingAlgorithmGNM) embeddingAlgorithm).setLogService(logService);
		((EmbeddingAlgorithmGNM) embeddingAlgorithm).setUserFeedback(userFeedback);
			
		request = createGraph("src/main/resources/PLCRequest.owl");
		substrate = createGraph("src/main/resources/PLETopology.owl");

		log.info("PLC Embedding test initialized");
	}

	@Test
	public void embeddingTest() throws IOException {
		
		log.info("Running Embedding test for "+embeddingAlgorithm.getTestbedName());
		log.info("Algorithm's name: "+embeddingAlgorithm.getAlgorithmName());
		
		// correct embedding
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
		
		log.info("PLC Embedding test successfully done");
		
	}
	
	@Test
	public void noSubstrateTest() {
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, new SparseMultigraph<NodeImpl, LinkImpl>());
		assertNull(result);
	}
	
	@Test
	public void noRequestTest() {
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",new SparseMultigraph<NodeImpl, LinkImpl>(), substrate);
		// We have only node mapping
		assertEquals(1,result.size());
		// We have no nodes mapped
		assertEquals(0,result.get(0).size());
	}
	
	@Test
	public void noComponentsRequestTest() throws IOException {
		request = createGraph("src/main/resources/NoComponentsRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
	}
	
	@Test
	public void tooBigRequestTest() throws IOException {
		request = createGraph("src/main/resources/BigRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	@Test
	public void tooPowerfulRequestTest() throws IOException {
		request = createGraph("src/main/resources/PowerfulRequest.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	//@Test
	public void invalidTopologyCoresTest() throws IOException {
		substrate = createGraph("src/main/resources/PLETopologyInvalidCores.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	//@Test
	public void invalidTopologySpeedTest() throws IOException {
		substrate = createGraph("src/main/resources/PLETopologyInvalidSpeed.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	//@Test
	public void invalidTopologyStorageTest() throws IOException {
		substrate = createGraph("src/main/resources/PLETopologyInvalidStorage.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	//@Test
	public void invalidTopologyMemoryTest() throws IOException {
		substrate = createGraph("src/main/resources/PLETopologyInvalidMemory.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	@Test
	public void noComponentsTest() throws IOException {
		request = createGraph("src/main/resources/NoComponentsRequest.owl");
		substrate = createGraph("src/main/resources/NoComponentsTopology.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	@Test
	public void partialSubstrateComponentsTest() throws IOException {
		request = createGraph("src/main/resources/PLCRequest.owl");
		substrate = createGraph("src/main/resources/PartialComponentsTopology.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	@Test
	public void embeddingPartialBoundRequestTest() throws IOException {
		request = createGraph("src/main/resources/PartialBoundRequest.owl");
		substrate = createGraph("src/main/resources/PLETopology.owl");
		
		// correct embedding
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		
		log.info("Result: "+result.size());
		// We have only node mapping
		assertEquals(1,result.size());
		// We have 2 nodes mapped
		assertEquals(2,result.get(0).size());
	}
	
	@Test
	public void invalidPartialBoundRequestTest1() throws IOException {
		request = createGraph("src/main/resources/PartialBoundRequestInvalid1.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	@Test
	public void invalidPartialBoundRequestTest2() throws IOException {
		request = createGraph("src/main/resources/PartialBoundRequestInvalid2.owl");
		List<Map<ResourceImpl,ResourceImpl>> result = embeddingAlgorithm.embed("sid100",request, substrate);
		assertNull(result);
	}
	
	private SparseMultigraph<NodeImpl, LinkImpl> createGraph(String path) throws IOException {
		// loading and getting imrespository util
		imru = new IMRepositoryUtilImpl();
		// Unbound Request
		String stringOwl = readFileAsString(path);
		TopologyImpl requestTopology = (TopologyImpl) imru.getTopologyFromFile(stringOwl);
		return translateIMToGraph(requestTopology);
	}
	
	private SparseMultigraph<NodeImpl, LinkImpl> translateIMToGraph(GroupImpl toTranslate) {
		// Graph to return
		SparseMultigraph<NodeImpl, LinkImpl> graph = new SparseMultigraph<NodeImpl, LinkImpl>();
		// HashMap for links and its endpoints
		HashMap<Link,ArrayList<Node>> linksAndEndpoints = new HashMap<Link, ArrayList<Node>>();
		
		try {
			Iterator<Resource> resourceIt = ((Group) toTranslate).getContains().iterator();
			while (resourceIt.hasNext()) {
				Resource resource = resourceIt.next();
				if (resource instanceof Node) {
					// Add vertex to the graph
					graph.addVertex((NodeImpl) resource);
					Set<Interface> ifaces = new HashSet<Interface>();
					if (((Node) resource).getHasInboundInterfaces()!=null) {
						ifaces.addAll(((Node) resource).getHasInboundInterfaces());
					}
					if (((Node) resource).getHasOutboundInterfaces()!=null) {
						ifaces.addAll(((Node) resource).getHasOutboundInterfaces());
					}
					// go through the interfaces
					for (Interface iface : ifaces) {
						Set<LinkOrPath> links = new HashSet<LinkOrPath>();
						if (iface.getIsSink()!=null) {
							links.addAll(iface.getIsSink());
						}
						if (iface.getIsSource()!=null) {
							links.addAll(iface.getIsSource());
						}
						for (LinkOrPath link : links) {
							if (link instanceof Link) {
								if (linksAndEndpoints.containsKey(link)) {
									ArrayList<Node> pair = linksAndEndpoints.get(link);
									pair.add((Node)resource);
								} else {
									ArrayList<Node> pair = new ArrayList<Node>(2);
									pair.add((Node)resource);
									linksAndEndpoints.put((Link)link, pair);
								}
							}
						}
					}
				}
				if (resource instanceof Link) {
					// Nothing to do. No nodes or interfaces reachable
					log.debug("Nothing to do for Links");
				}
			}
			
			// Add edges to the graph
			Set<Entry<Link,ArrayList<Node>>> set = linksAndEndpoints.entrySet();
			for (Entry<Link,ArrayList<Node>> entry : set) {
				if (entry.getValue().size()==2) {
					graph.addEdge((LinkImpl)entry.getKey(),
							(NodeImpl)entry.getValue().get(0),(NodeImpl)entry.getValue().get(1),
							EdgeType.UNDIRECTED);
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
