package eu.novi.resources.discovery.database.communic;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.ManipulateDB;
import eu.novi.resources.discovery.database.NoviUris;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.NoviRisValues;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



/**
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
 * ******************************************************************************
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class TestbedCommunicationTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		PeriodicUpdate update = new PeriodicUpdate();
		update.setScheduler(Executors.newScheduledThreadPool(5));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		TestbedCommunication testb = new TestbedCommunication();
		
		FederatedTestbed rh = mock(FederatedTestbed.class);
		testb.setCalls2TestbedFromRH(rh);
		
		//answer is null
		when(rh.deleteSlice(any(String.class), any(String.class), any(Set.class), 
				any(TopologyImpl.class))).thenReturn(null);
		testb.deleteSlice("urn", new HashSet<String>(), null);
		
		//has error
		RHCreateDeleteSliceResponseImpl rhRespo = mock(RHCreateDeleteSliceResponseImpl.class);
		when(rhRespo.hasError()).thenReturn(true);
		when(rh.deleteSlice(any(String.class), any(String.class), any(Set.class), 
				any(TopologyImpl.class))).thenReturn(rhRespo);
		testb.deleteSlice("urn", new HashSet<String>(), null);
		
		//no error
		rhRespo = mock(RHCreateDeleteSliceResponseImpl.class);
		when(rhRespo.hasError()).thenReturn(false);
		when(rh.deleteSlice(any(String.class), any(String.class), any(Set.class), 
				any(TopologyImpl.class))).thenReturn(rhRespo);
		testb.deleteSlice("urn", new HashSet<String>(), null);
	
		
		//get substrate
		RHListResourcesResponseImpl rhListRe = mock(RHListResourcesResponseImpl.class);
		when(rhListRe.hasError()).thenReturn(true);
		when(rh.listResources(any(String.class))).thenReturn(rhListRe);
		assertNull(testb.getTestbedSubstrate());
		
		when(rhListRe.hasError()).thenReturn(false);
		when(rhListRe.getPlatformString()).thenReturn(null);
		when(rh.listResources(any(String.class))).thenReturn(rhListRe);
		assertNull(testb.getTestbedSubstrate());
		
		when(rhListRe.hasError()).thenReturn(false);
		when(rhListRe.getPlatformString()).thenReturn(
				translateToRdfString(new PlatformImpl("platform")));
		when(rh.listResources(any(String.class))).thenReturn(rhListRe);
		assertNull(testb.getTestbedSubstrate());
		
		when(rhListRe.hasError()).thenReturn(false);
		when(rhListRe.getPlatformString()).thenReturn("not valid rdf string");
		when(rh.listResources(any(String.class))).thenReturn(rhListRe);
		assertNull(testb.getTestbedSubstrate());
		
		when(rhListRe.hasError()).thenReturn(false);
		PlatformImpl pl = new PlatformImpl("platform");
		pl.setContains(IMUtil.createSetWithOneValue(new NodeImpl("node")));
		when(rhListRe.getPlatformString()).thenReturn(translateToRdfString(pl));
		when(rh.listResources(any(String.class))).thenReturn(rhListRe);
		assertNotNull(testb.getTestbedSubstrate());
		
		
	}
	
	
	@Test
	public void testPrintImplemented()
	{
		TestbedCommunication.printImplementBy(createTopology());
		
	}
	
	@Test
	public void testReserveSlice()
	{
		TestbedCommunication testb = new TestbedCommunication();

		FederatedTestbed rh = mock(FederatedTestbed.class);
		testb.setCalls2TestbedFromRH(rh);

		Topology top = new TopologyImpl("myTopology");
		top.setContains(new HashSet<Resource>());
		
		//null response

		when(rh.createSlice(any(String.class), any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).thenReturn(null);
		testb.reserveSlice(null, null, top);
		
		//has error
		RHCreateDeleteSliceResponseImpl rhRespo = mock(RHCreateDeleteSliceResponseImpl.class);
		when(rhRespo.hasError()).thenReturn(true);
		when(rh.createSlice(any(String.class), any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).thenReturn(rhRespo);
		testb.reserveSlice(null, null, top);
		
		//no error
		when(rhRespo.hasError()).thenReturn(false);
		testb.reserveSlice(null, null, top);
		
	}
	
	private String translateToRdfString(Platform platform)
	{
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		return imRepo.exportIMObjectToString(platform);
		
	}
	
	private Topology createTopology()
	{
		Topology top = new TopologyImpl("boundTopology");
		//Platform plat = new PlatformImpl("PlanetLab");
		//virtual node 1
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		Interface in1 = new InterfaceImpl("sliver1-if0-in");
		Interface out1 = new InterfaceImpl("sliver1-if0-out");
		out1.setImplementedBy(IMUtil.createSetWithOneValue(new InterfaceImpl("aRealInterface")));
		vNode1.setHasInboundInterfaces(IMUtil.createSetWithOneValue(in1));
		vNode1.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(out1));
		
		Node node1 = new NodeImpl("smilax3.novipl.man.poznan.pl");
		node1.setHostname("smilax3.novipl.man.poznan.pl");
		Interface n_in1 = new InterfaceImpl("node-if0-in");
		Interface n_out1 = new InterfaceImpl("node-if0-out");
		node1.setHasInboundInterfaces(IMUtil.createSetWithOneValue(n_in1));
		node1.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(n_out1));
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(node1));
		
		//virtual node 1
		VirtualNode vNode2 = new VirtualNodeImpl("vnode2");
		Interface in2 = new InterfaceImpl("sliver2-if0-in");
		Interface out2 = new InterfaceImpl("sliver2-if0-out");
		vNode2.setHasInboundInterfaces(IMUtil.createSetWithOneValue(in2));
		vNode2.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(out2));
		
		Node node2 = new NodeImpl("novipl.planetlab2-novi.lab.netmode.ece.ntua.gr");
		node2.setHostname("novipl.planetlab2-novi.lab.netmode.ece.ntua.gr");
		Interface n_in2 = new InterfaceImpl("node-if0-in");
		Interface n_out2 = new InterfaceImpl("node-if0-out");
		node2.setHasInboundInterfaces(IMUtil.createSetWithOneValue(n_in2));
		node2.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(n_out2));
		
		node2.setHostname(null);
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(node2));
		
		
		Set<Resource> nodes = new HashSet<Resource>();
		nodes.add(vNode1);
		nodes.add(vNode2);
		nodes.add(node1);
		top.setContains(nodes);
		//plat.setContains(nodes);
		
		return top;
	}


}
