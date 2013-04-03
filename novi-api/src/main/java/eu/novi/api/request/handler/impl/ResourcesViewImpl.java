package eu.novi.api.request.handler.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.novi.api.request.handler.ResourcesView;
import eu.novi.api.request.handler.helpers.FileReader;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.resources.discovery.NoviApiCalls;


/**
 * 
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *******************************************************************************************
 * Resource view service implementation.
 * 
 * These are implementation of REST interfaces that will be used for resources visualization
 * 
 *  @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 *  
 *******************************************************************************************/

public class ResourcesViewImpl implements ResourcesView {
	private static final transient Logger log = 
			LoggerFactory.getLogger(ResourcesViewImpl.class);
	

	private NoviApiCalls resourceDiscoveryAPI;
	
	@Override
	public String getSlices() {
		
		Gson gson = new Gson();
		VizNodes resultVizNodes = new VizNodes();
		
		Set<String> slices = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_RESERVATION);
		
		for(String sliceID: slices){
			Set<String> contained = resourceDiscoveryAPI.execStatementReturnResults(sliceID, IM_CONTAINS, null, sliceID);
			
			VizNode aNode = new VizNode(sliceID,0,contained.toArray(new String[0]));
			resultVizNodes.addNode(aNode);
			
		}
		
		return gson.toJson(resultVizNodes);
	}

	@Override
	public String getSlivers() { 
		Gson gson = new Gson();
		VizNodes resultVizNodes = new VizNodes();
		
		Set<String> slices = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_RESERVATION);
		Set<String> virtualNodes = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_VIRTUALNODE);
		for(String sliceID: slices){
			Set<String> contained = resourceDiscoveryAPI.execStatementReturnResults(sliceID, IM_CONTAINS, null, sliceID);
			for(String containedID : contained){
				if(virtualNodes.contains(containedID)){
					Set<String> implementingNodes = 	resourceDiscoveryAPI.execStatementReturnResults(containedID, IM_IMPLEMENTEDBY, null, sliceID);
					VizNode aNode = new VizNode(containedID,1, implementingNodes.toArray(new String[0]));
					resultVizNodes.addNode(aNode);
				}
			}
		}
		
		
		return gson.toJson(resultVizNodes);
	}

	@Override
	public String getNodes() {

		Gson gson = new Gson();
		VizNodes resultVizNodes = new VizNodes();
		
		Set<String> nodes  = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_NODE);
		Set<String> virtualNodes = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_VIRTUALNODE);
		
		for(String nodeID:nodes){
			// We only want pure nodes not including virtual nodes (which is also a node sinc vnode is subclass of a node)
			if(virtualNodes.contains(nodeID)) continue;
			
			VizNode aNode = new VizNode(nodeID, 2, new String[0]);
			resultVizNodes.addNode(aNode);
		}
		return gson.toJson(resultVizNodes);
	}

	@Override
	public String getVLinks() {

		return getLinkOrVirtualLink(IM_VIRTUALLINK);
	}

	@Override
	public String getLinks() {
		return getLinkOrVirtualLink(IM_LINK);
	}
	
	private String getLinkOrVirtualLink(String LinkType){
		
		Gson gson = new Gson();
		
		VizLinks resultVizLinks = new VizLinks();
		
		Set<String> slices = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, IM_RESERVATION);
		Set<String> linkOrVirtualLinks = resourceDiscoveryAPI.execStatementReturnResults(null, RDF_TYPE, LinkType);
	
		for(String sliceID: slices){
			Set<String> containedVLink = resourceDiscoveryAPI.execStatementReturnResults(sliceID, IM_CONTAINS, null, sliceID);
			for(String curVlink : containedVLink){
				
			
				if(linkOrVirtualLinks.contains(curVlink)){
					String sinkNode = getSinkNode(sliceID, curVlink);
					String sourceNode = getSourceNode(sliceID, curVlink);
					if(sinkNode != null && sourceNode != null){
						resultVizLinks.addLink(new VizLink(curVlink, sinkNode, sourceNode));
					}
				}
			}
		}
			
		return gson.toJson(resultVizLinks);
	}
	
	private String  getSinkNode(String sliceID, String curVlink) {
		// Here actually we know we will only get one  sink interface.
		Set<String> sinkInterfaces = resourceDiscoveryAPI.execStatementReturnResults(null, IM_ISSINK, curVlink, sliceID);
		// Assuming we have only one sink Interface
		if(sinkInterfaces.isEmpty()) return null;
		String sinkInterface = sinkInterfaces.iterator().next();
		
		// From sink interface we are getting the sink node
		Set<String> sinkNodes = resourceDiscoveryAPI.execStatementReturnResults(null, IM_HASOUTBOUNDINTERFACE, sinkInterface, sliceID);
		if(sinkNodes.isEmpty()) return null;
		
		// Here is the node.
		return sinkNodes.iterator().next();
	}

	private String  getSourceNode(String sliceID, String curVlink) {
		// Here actually we know we will only get one  sink interface.
		Set<String> sourceInterfaces = resourceDiscoveryAPI.execStatementReturnResults(null, IM_ISSOURCE, curVlink, sliceID);
		// Assuming we have only one sink Interface
		if(sourceInterfaces.isEmpty()) return null;
		String sourceInterface = sourceInterfaces.iterator().next();
		
		// From sink interface we are getting the sink node
		Set<String> sourceNodes = resourceDiscoveryAPI.execStatementReturnResults(null, IM_HASINBOUNDINTERFACE, sourceInterface, sliceID);
		if(sourceNodes.isEmpty()) return null;
		// Here is the node.
		return sourceNodes.iterator().next();
	}
	


	/**
	 * @return the resourceDiscoveryAPI
	 */
	public NoviApiCalls getResourceDiscoveryAPI() {
		return resourceDiscoveryAPI;
	}

	/**
	 * @param resourceDiscoveryAPI the resourceDiscoveryAPI to set
	 */
	public void setResourceDiscoveryAPI(NoviApiCalls resourceDiscoveryAPI) {
		this.resourceDiscoveryAPI = resourceDiscoveryAPI;
	}

	
	@Override
	public String getInfraViz() {
		String fileContent = "";
		try {
			fileContent = FileReader.readFile("resource-view.html");
		} catch (NoClassDefFoundError e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return fileContent;
	}
	
	@Override
	public String getInstances() {
		Set<Node> nodes = resourceDiscoveryAPI.listAllResources();
		IMRepositoryUtil util = new IMRepositoryUtilImpl();
		Platform container = new PlatformImpl("http://fp7-novi.eu/im.owl#currentTestbed");
		String result = "";
		
		container.setContains(nodes);
		
		result = util.exportIMObjectToString(container);
		return result;
	}
	
	/**
	 * Internal class intended only as place holder for serializing into JSON String.
	 * Dumping this class using GSON will produce the JSON needed for visualization.
	 * @author wibisono
	 *
	 */
	public class VizNode {
		public VizNode(String id, int type, String[]  implementedBy){
			this.id = id; 
			this.type = type; 
			this.implementedBy = implementedBy;
		}
		String id;
		int type;
		String[] implementedBy;

	}
	
	public class VizNodes {
		ArrayList<VizNode> nodes = new ArrayList<ResourcesViewImpl.VizNode>();
		public void addNode(VizNode n){
			nodes.add(n);
		}
		public ArrayList<VizNode> getNodes(){
			return nodes;
		}
	}
	
	public class VizLink {
		public VizLink (String id, String sourcenode, String sinknode){
			this.id = id;
			this.sourcenode = sourcenode;
			this.sinknode = sinknode;
		}
		String id, sourcenode, sinknode;
	}
	
	public class VizLinks {
		ArrayList<VizLink> links = new ArrayList<ResourcesViewImpl.VizLink>();
		public void addLink(VizLink vl){
			links.add(vl);
		}
		public ArrayList<VizLink> getLinks(){
			return links;
		}
	}
	
	
	
	public static final String RDF_TYPE 				= "rdf:type";
	public static final String IM_RESERVATION 			= "im:Reservation";
	public static final String IM_CONTAINS 				= "im:contains";
	public static final String IM_NODE					= "im:Node";
	public static final String IM_LINK					= "im:Link";
	public static final String IM_VIRTUALNODE			= "im:VirtualNode";
	public static final String IM_VIRTUALLINK			= "im:VirtualLink";
	public static final String IM_IMPLEMENTEDBY			= "im:implementedBy";
	public static final String IM_ISSINK				= "im:isSink";
	public static final String IM_ISSOURCE				= "im:isSource";
	public static final String IM_HASINBOUNDINTERFACE  	= "im:hasInboundInterface";
	public static final String IM_HASOUTBOUNDINTERFACE 	= "im:hasOutboundInterface";
	
	
	
	
	
	
	
	
	
}
