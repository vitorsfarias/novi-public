package eu.novi.mapping.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.LinkImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartitionedRequest {
	
	private Map<SparseMultigraph<Node,Link>, String> subGraphs;
	private Map<Link, LinkedHashMap<Node,String>> interLinks; 
	private Set<Platform> partialPlatforms;
	
	// Local logging
    private static final transient Logger log = LoggerFactory.getLogger(PartitionedRequest.class);
	
	public PartitionedRequest(){
		this.subGraphs =  new LinkedHashMap<SparseMultigraph<Node,Link>, String>();
		this.interLinks= new LinkedHashMap<Link, LinkedHashMap<Node,String>>();
		this.partialPlatforms = new HashSet<Platform>();
	}
	
	
	
	public  void createSubNets(Map<Node, Integer> mapping,
			Map<Integer, String> testbeds, SparseMultigraph<NodeImpl,LinkImpl> virtualTopology){

		
		// Get the key set of linkedHashMap
		Set<Integer> tesbeds = testbeds.keySet();
		// Get Iterator of keySet
		Iterator<Integer> iterator = tesbeds.iterator();
		log.info("in partitioned class");
		for (Node reqNode: virtualTopology.getVertices()){
		    Integer belongsTo = mapping.get(reqNode);
			log.info("Node: " +reqNode.toString()+ "  in testbed " + testbeds.get(belongsTo) );
		}
		
		
		while(iterator.hasNext())
		{
		
			Integer testbedID = iterator.next();
		    String testbedName = testbeds.get(testbedID);
			log.info("cheking testbed : " + testbedName);
		    SparseMultigraph<Node,Link> tmpGraph =  new SparseMultigraph<Node,Link>();
			
			Set<Resource> tmpTopologyResources = new HashSet<Resource>();
			PlatformImpl tmpTopology =  new PlatformImpl(testbedName);
		    

		    for (Node reqNode: virtualTopology.getVertices()){
		    	Integer belongsTo = mapping.get(reqNode);
		    	if (belongsTo.equals(testbedID)){
		    		tmpGraph.addVertex(reqNode);
					tmpTopologyResources.add(reqNode);
					//System.out.println(reqNode.toString());
		    	} 	
		    }
			
			
		    for (LinkImpl reqLink: virtualTopology.getEdges()){
		    	Pair<NodeImpl> linkNodes =virtualTopology.getEndpoints(reqLink);
				Node src =  linkNodes.getFirst();
				Integer belongsToSrc = mapping.get(src);
				Node dst =  linkNodes.getSecond();
				Integer belongsToDst = mapping.get(dst);
				
				if ((belongsToSrc.equals(belongsToDst)) && (belongsToSrc.equals(testbedID))){
					tmpGraph.addEdge(reqLink,linkNodes,null);
					tmpTopologyResources.add(reqLink);
				}
				else{
					LinkedHashMap<Node,String> tmpLink= new LinkedHashMap<Node,String>();
					tmpLink.put(src, testbeds.get(belongsToSrc));
					tmpLink.put(dst, testbeds.get(belongsToDst));
					this.interLinks.put(reqLink, tmpLink);
				}
				
		    }
			tmpTopology.setContains(tmpTopologyResources);
			
			if (tmpTopologyResources.size()!=0){
			log.info("create partialPlatforms for : " + testbedName);
			this.partialPlatforms.add(tmpTopology);
		    this.subGraphs.put(tmpGraph,testbedName);
			}
		}

	}
	
	public Map<SparseMultigraph<Node,Link>, String> getSubGraphs(){
		return this.subGraphs;
	}
	
		
	public Set<Platform> getPartialPlatforms(){
		return this.partialPlatforms;
	}
	
	public  Map<Link, LinkedHashMap<Node,String>> getInterLinks(){
		return this.interLinks;
	}


}
