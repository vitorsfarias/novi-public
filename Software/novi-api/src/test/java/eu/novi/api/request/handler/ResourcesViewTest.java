package eu.novi.api.request.handler;

import static org.easymock.EasyMock.anyObject;

import static org.easymock.EasyMock.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import static eu.novi.api.request.handler.impl.ResourcesViewImpl.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;


import eu.novi.api.request.handler.impl.ResourcesViewImpl;
import eu.novi.api.request.handler.impl.ResourcesViewImpl.VizLinks;
import eu.novi.api.request.handler.impl.ResourcesViewImpl.VizNodes;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Node;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.resources.discovery.NoviApiCalls;

public class ResourcesViewTest {

	@Test
	public void testGetSlices(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String resultJSONString = rv.getSlices();
		Gson gson = new Gson();
		VizNodes nodes = gson.fromJson(resultJSONString, VizNodes.class);
		assertEquals(nodes.getNodes().size(), 2);
	}
	
	@Test
	public void testGetSlivers(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String resultJSONString = rv.getSlivers();
		Gson gson = new Gson();
		VizNodes nodes = gson.fromJson(resultJSONString, VizNodes.class);
		assertEquals(nodes.getNodes().size(), 3);
	}
	
	@Test
	public void testGetNodes(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String resultJSONString = rv.getNodes();
		Gson gson = new Gson();
		VizNodes nodes = gson.fromJson(resultJSONString, VizNodes.class);
		assertEquals(nodes.getNodes().size(), 3);
	}
	
	@Test
	public void testGetLinks(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String resultJSONString = rv.getLinks();
		System.out.println(resultJSONString);
		Gson gson = new Gson();
		VizLinks links = gson.fromJson(resultJSONString, VizLinks.class);
		assertEquals(links.getLinks().size(),4);
	}
	
	@Test
	public void testGetVLinks(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String resultJSONString = rv.getVLinks();
		System.out.println(resultJSONString);
		Gson gson = new Gson();
		VizLinks links = gson.fromJson(resultJSONString, VizLinks.class);
		assertEquals(links.getLinks().size(),4);
	}
	
	@Test
	public void testInstances(){
		ResourcesViewImpl rv = new ResourcesViewImpl();
		NoviApiCalls resourceDiscovery = getRISMock();
		rv.setResourceDiscoveryAPI(resourceDiscovery);
		String result = rv.getInstances();
		assertNotNull(result);
	}
	
	
	private NoviApiCalls getRISMock(){
		NoviApiCalls result = mock(NoviApiCalls.class);
		
		HashMap<String, Set <String>> sliceVirtualNodes = new HashMap<String, Set<String>>();
		
		sliceVirtualNodes.put("http://fp7-novi.eu/im.owl#slice_1", 
							  toStringSet("http://fp7-novi.eu/im.owl#sliver1"));
		sliceVirtualNodes.put("http://fp7-novi.eu/im.owl#slice_2", 
							 toStringSet("http://fp7-novi.eu/im.owl#sliver2", 
									 	 "http://fp7-novi.eu/im.owl#sliver3"));
		
		
		HashMap<String, Set <String>> virtualToPhysical = new HashMap<String, Set<String>>();
		
		virtualToPhysical.put("http://fp7-novi.eu/im.owl#sliver1", toStringSet("urn:publicid:IDN+novipl:novi+node+smilax1.man.poznan.pl"));
		virtualToPhysical.put("http://fp7-novi.eu/im.owl#sliver2", toStringSet("urn:publicid:IDN+novipl:novi+node+smilax2.man.poznan.pl"));
		virtualToPhysical.put("http://fp7-novi.eu/im.owl#sliver3", toStringSet("urn:publicid:IDN+novipl:novi+node+smilax3.man.poznan.pl"));
			
		
		when(result.execStatementReturnResults(null, RDF_TYPE, IM_RESERVATION))
			.thenReturn(sliceVirtualNodes.keySet());
		
		when(result.execStatementReturnResults(null, RDF_TYPE, IM_VIRTUALNODE))
		.thenReturn(virtualToPhysical.keySet());
		
		when(result.execStatementReturnResults(null, RDF_TYPE, IM_NODE))
		.thenReturn(toStringSet(
				"urn:publicid:IDN+novipl:novi+node+smilax1.man.poznan.pl",
				"urn:publicid:IDN+novipl:novi+node+smilax2.man.poznan.pl",
				"urn:publicid:IDN+novipl:novi+node+smilax3.man.poznan.pl")	);
		
		for(String sliceID: sliceVirtualNodes.keySet()){
			Set<String> nodes = sliceVirtualNodes.get(sliceID);
			nodes.addAll(toStringSet("link1","link2","virtualLink1","virtualLink2"));
			
			when(result.execStatementReturnResults(sliceID, IM_CONTAINS, null, sliceID))
				.thenReturn(nodes);
			for(String nodeID : sliceVirtualNodes.get(sliceID)){
				when(result.execStatementReturnResults(nodeID, IM_IMPLEMENTEDBY, null, sliceID))
				.thenReturn(virtualToPhysical.get(nodeID));
			}
		}
		
		when(result.execStatementReturnResults(anyString(), eq(RDF_TYPE), eq(IM_VIRTUALLINK)))
				.thenReturn(toStringSet("virtualLink1", "virtualLink2"));
		
		when(result.execStatementReturnResults(anyString(), eq(RDF_TYPE), eq(IM_LINK)))
		.thenReturn(toStringSet("link1", "link2"));

		when(result.execStatementReturnResults(anyString(), eq(IM_ISSINK), anyString(), anyString()))
		.thenReturn(toStringSet("aSinkInterface"));

		when(result.execStatementReturnResults(anyString(), eq(IM_ISSOURCE), anyString(), anyString()))
		.thenReturn(toStringSet("aSourceInterface"));

		when(result.execStatementReturnResults(anyString(), eq(IM_HASINBOUNDINTERFACE), anyString(), anyString()))
		.thenReturn(toStringSet("anInboundNode1"));

		
		when(result.execStatementReturnResults(anyString(), eq(IM_HASOUTBOUNDINTERFACE), anyString(), anyString()))
		.thenReturn(toStringSet("anOutboundNode2"));

		Set<Node> nodes = new HashSet<Node>();
		Node node1 =  new NodeImpl("http://fp7-novi.eu/im.owl#node1");
		
		Set<Interface> interfaceSet = new HashSet<Interface>();
		interfaceSet.add(new InterfaceImpl("http://fp7-novi.eu/im.owl#interfaceX"));
		
		node1.setHasOutboundInterfaces(interfaceSet);
		nodes.add(node1);
		
		Node node2 = new NodeImpl("http://fp7-novi.eu/im.owl#node2");
		
		node2.setHasInboundInterfaces(interfaceSet);
		
		nodes.add(node2);
		nodes.add(new NodeImpl("http://fp7-novi.eu/im.owl#node3"));
			
		when(result.listResources()).thenReturn(nodes);
		
		return result;
	}
	
	private Set <String> toStringSet(String... elements){
			Set<String> result = new HashSet<String>();
			for(String s : elements){
				result.add(s);
			}
			return result;
	}
}
