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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
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
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmFEDERICA;
import eu.novi.mapping.embedding.federica.EmbeddingAlgorithmNCM;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;

public class DiscoveryEmbeddingSimulationsTest {
	
	LogService logService;
	ReportEvent userFeedback ;
	EmbeddingAlgorithmNCM embeddingAlgorithmNCM;
	EmbeddingAlgorithmInterface embeddingAlgorithm;
	List<Map<SparseMultigraph<NodeImpl, LinkImpl>,
			List<Map<ResourceImpl,ResourceImpl>>>> requests;
	SparseMultigraph<NodeImpl, LinkImpl> substrate;
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(DiscoveryEmbeddingSimulationsTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		log.info("Initializing FEDERICA Embedding test...");
		
		embeddingAlgorithm = new EmbeddingAlgorithmFEDERICA();
		embeddingAlgorithmNCM = new EmbeddingAlgorithmNCM();
		// Creating mock classes
		logService = mock(LogService.class);
		userFeedback = mock(ReportEvent.class);
		((EmbeddingAlgorithmFEDERICA) embeddingAlgorithm).setLogService(logService);
		((EmbeddingAlgorithmFEDERICA) embeddingAlgorithm).setUserFeedback(userFeedback);
		
		substrate = createGraph("src/main/resources/FEDERICAExtendedTopology.owl", true);

		requests = new ArrayList<Map<SparseMultigraph<NodeImpl, LinkImpl>,
				List<Map<ResourceImpl,ResourceImpl>>>>();
		
		log.info("FEDERICA Embedding test initialized");
	}

	@Test
	public void embeddingSimulationsTest() throws IOException {
		
		int accepted = 0;
		int denied = 0;
		int released = 0;
		
		for (int i=0; i<5; i++) {
			
			SparseMultigraph<NodeImpl, LinkImpl> request = createGraph("src/main/resources/simulations/request"+i+".owl", true);
			
			log.info("Calling embed method for request: request"+i+"...");
			List<Map<ResourceImpl,ResourceImpl>>  result = embeddingAlgorithmNCM.embedNCM(request, substrate, userFeedback, 
															null, logService, "FEDERICA");
			
			
			
			if (result!=null && result.size()==2) {
				
				showNodeMapping(result.get(0));
				showLinkMapping(result.get(1), result.get(0), request);
				
				reserveReleaseRequest(request,substrate,
						result.get(0),result.get(1),true);
				
				Map<SparseMultigraph<NodeImpl, LinkImpl>,
				List<Map<ResourceImpl,ResourceImpl>>> toAdd = new HashMap<SparseMultigraph<NodeImpl, LinkImpl>,
					List<Map<ResourceImpl,ResourceImpl>>>();
				toAdd.put(request, result);
				requests.add(toAdd);
				
				accepted++;
				
			} else {
				denied++;
			}

			// Release slice with a probability of 50%
//			if (Math.random()<.5)  {
//				if (requests.size()>released) {
//					Map<SparseMultigraph<NodeImpl, LinkImpl>, 
//						List<Map<ResourceImpl, ResourceImpl>>> toRelease = requests.get(released);
//					reserveReleaseRequest(toRelease.keySet().iterator().next(), 
//							substrate, 
//							toRelease.get(toRelease.keySet().iterator().next()).get(0), 
//							toRelease.get(toRelease.keySet().iterator().next()).get(1),
//							false);
//					
//					released++;
//				}
//			}
			
		}
		
		/** TODO show results: denials, accepted, released, utilization,
		 * cost (revenew), hops.
		 */
		log.info("Accepted requests: "+accepted);
		log.info("Denied requests: "+denied);
		log.info("Released requests: "+released);
		
		assertEquals(5,accepted);
		assertEquals(0,denied);
		assertEquals(0,released);
		
		log.info("FEDERICA Embedding simulations successfully done");
		
	}

	private void showLinkMapping(Map<ResourceImpl, ResourceImpl> linkMapping, Map<ResourceImpl, ResourceImpl> nodeMapping, SparseMultigraph<NodeImpl, LinkImpl> request) {
		
		log.info("");
		log.info("**************** Link Mapping **********************");
		log.info("");
		
		for (Entry<ResourceImpl, ResourceImpl> toShow : linkMapping.entrySet()) {
			
			// Do not show reverse paths
			if (toShow.getValue().toString().contains("reverse")) {continue;}
			
			log.info("VirtualLink: "+toShow.getKey().toString());
			
			Pair<NodeImpl> endpoints = request.getEndpoints((LinkImpl) toShow.getKey());
			NodeImpl sourceVNode = endpoints.getFirst();
			NodeImpl targetVNode = endpoints.getSecond();
			log.info("--> Source Substrate Node: "+nodeMapping.get(sourceVNode).toString());
			log.info("--> Target Substrate Node: "+nodeMapping.get(targetVNode).toString());
			
			PathImpl path = (PathImpl) toShow.getValue();
			log.info("--> Provisioned by: "+path.toString());
			for (Resource pathElement : path.getContains()) {
				// Do not show interfaces
				if (!(pathElement instanceof InterfaceImpl)) {
					log.info("----> Path Element: "+pathElement.toString());
				}
			}
		}
		
	}

	private void showNodeMapping(Map<ResourceImpl, ResourceImpl> nodeMapping) {
		
		log.info("");
		log.info("**************** Node Mapping **********************");
		log.info("");
		
		for (Entry<ResourceImpl, ResourceImpl> toShow : nodeMapping.entrySet()) {
			log.info("VirtualNode: "+toShow.getKey().toString());
			log.info("--> Implemented by: "+toShow.getValue().toString());
		} 
		
	}

	/**
	 * Update the substrate values with the values of the request
	 * @param request
	 * @param substrate
	 * @param map2 
	 * @param nodeMapping 
	 */
	private void reserveReleaseRequest(SparseMultigraph<NodeImpl, LinkImpl> request,
			SparseMultigraph<NodeImpl, LinkImpl> substrate, 
			Map<ResourceImpl, ResourceImpl> nodeMapping, 
			Map<ResourceImpl, ResourceImpl> linkMapping,
			boolean reserve) {
		
		if (reserve) {
			log.info("Reserving request...");
		} else {
			log.info("Releasing request...");
		}
		
		for (ResourceImpl vNode : nodeMapping.keySet()) {
			
			// getting substrate node
			NodeImpl pNode = (NodeImpl) nodeMapping.get(vNode);
			
			if (EmbeddingOperations.isVirtualMachine((VirtualNode) vNode)) {
				if (!EmbeddingOperations.isSetEmpty(((NodeImpl) vNode).getHasComponent())) {
					// getting request valies
//					float reqCpu=0;
					float reqMem=0;
					float reqSto=0;
					int reqCores=0;
					for (NodeComponent nc : ((NodeImpl) vNode).getHasComponent()) {
						if (nc instanceof CPU) {
//							reqCpu = ((CPU) nc).getHasCPUSpeed();
							reqCores = ((CPU) nc).getHasCores().intValue();
						} else if (nc instanceof Memory) {
							reqMem = ((Memory) nc).getHasMemorySize();
						} else if (nc instanceof Storage) {
							reqSto = ((Storage) nc).getHasStorageSize();
						}
					}
					// reserving request values in the substrate
					if (!EmbeddingOperations.isSetEmpty(pNode.getHasComponent())) {
						for (NodeComponent nc : pNode.getHasComponent()) {
							if (nc instanceof CPU) {
								if (reserve) {
									((CPU) nc).setHasAvailableCores(
											new BigInteger(Integer.toString(((CPU) nc).getHasAvailableCores().intValue()-reqCores)));
								} else {
									((CPU) nc).setHasAvailableCores(
											new BigInteger(Integer.toString(((CPU) nc).getHasAvailableCores().intValue()+reqCores)));
								}
							} else if (nc instanceof Memory) {
								if (reserve) {
									((Memory) nc).setHasAvailableMemorySize(
											((Memory) nc).getHasAvailableMemorySize()-reqMem);
								} else {
									((Memory) nc).setHasAvailableMemorySize(
											((Memory) nc).getHasAvailableMemorySize()+reqMem);
								}
							} else if (nc instanceof Storage) {
								if (reserve) {
									((Storage) nc).setHasAvailableStorageSize(
											((Storage) nc).getHasAvailableStorageSize()-reqSto);
								} else {
									((Storage) nc).setHasAvailableStorageSize(
											((Storage) nc).getHasAvailableStorageSize()+reqSto);
								}
							}
						}
						
					}
				}
			}
			else if (EmbeddingOperations.isVirtualRouter((VirtualNode) vNode)) {
				// reserving/releasing logical router in the substrate
				if (reserve) {
					pNode.setHasAvailableLogicalRouters(pNode.getHasAvailableLogicalRouters()-1);
				} else {
					pNode.setHasAvailableLogicalRouters(pNode.getHasAvailableLogicalRouters()+1);
				}
			}
		}
		for (LinkImpl vLink : request.getEdges()) {
			// getting request link bw
			float reqBw=0;
			reqBw = vLink.getHasCapacity();
			// reserving/releasing bw in the substrate link
			PathImpl path = (PathImpl) linkMapping.get(vLink);
			for (Link sLink : substrate.getEdges()) {
				if (!EmbeddingOperations.isSetEmpty(sLink.getInPaths())) {
					if (sLink.getInPaths().contains(path)) {
						if (reserve) {
							sLink.setHasAvailableCapacity(
									sLink.getHasAvailableCapacity()-reqBw);
						} else {
							sLink.setHasAvailableCapacity(
									sLink.getHasAvailableCapacity()+reqBw);
						}
					}
				}
			}
		}
		
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
