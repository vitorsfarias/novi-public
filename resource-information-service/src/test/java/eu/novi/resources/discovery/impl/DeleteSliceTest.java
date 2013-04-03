package eu.novi.resources.discovery.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.IRMLocalDbCalls;
import eu.novi.resources.discovery.database.NoviUris;
import eu.novi.resources.discovery.database.ReserveSlice;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.remote.serve.RemoteRisServeImpl;


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
public class DeleteSliceTest {
	
	private static final transient Logger log = LoggerFactory.getLogger(DeleteSliceTest.class);

	//TestbedCommunication mockTestbed;
	FederatedTestbed calls2TestbedFromRHMock;
	ReportEvent userFeedback;
	RemoteRisDiscoveryImpl remoteRis;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestbedCommunication.assignCalls2TestbedFromRHStatic(null);
		ConnectionClass.stopStorageService();
	}
	
	@Before
	public void setUp() throws Exception {
		log.debug("Initializing DeleteSliceTest...");

		// Setting up local environment
		log.debug("Setting up local environment...");
		/*mockTestbed = mock(TestbedCommunication.class);

		//stubbing mockRIS
		log.debug("stubbing mockRIS...");
		RHCreateDeleteSliceResponse mockCreateSliceResp = mock(RHCreateDeleteSliceResponse.class);
		when(mockCreateSliceResp.hasError()).thenReturn(false);
		when(mockCreateSliceResp.getSliceID()).thenReturn("sliceID1213");
		
		when(mockTestbed.reserveSlice(any(NOVIUserImpl.class), any(String.class), any(Topology.class))).
		thenReturn(mockCreateSliceResp);*/
		
		////
		RHCreateDeleteSliceResponseImpl mockCreateSliceResp = 
				mock(RHCreateDeleteSliceResponseImpl.class);
		when(mockCreateSliceResp.hasError()).thenReturn(false);
		when(mockCreateSliceResp.getSliceID()).thenReturn("sliceID1213");
		
		calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.createSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).
		thenReturn(mockCreateSliceResp);
		
		
		//RHCreateDeleteSliceResponse mockCreateSliceResp = mock(RHCreateDeleteSliceResponse.class);
		//when(mockCreateSliceResp.hasError()).thenReturn(false);
		when(calls2TestbedFromRHMock.deleteSlice(any(String.class), any(String.class), any(Set.class),
				any(TopologyImpl.class))).thenReturn(mockCreateSliceResp);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		//user feedback
		userFeedback =  mock(ReportEvent.class);
		remoteRis = new RemoteRisDiscoveryImpl("PlanetLab");
		RemoteRisServeImpl remoteServe = new RemoteRisServeImpl();
		remoteServe.setTestbed("FEDERICA");
		List<RemoteRisServe> remoteRISList = new ArrayList<RemoteRisServe>();
		remoteRISList.add(remoteServe);
		remoteRis.setRemoteRISList(remoteRISList);
	}

	@Test
	public void testDeleteSlice() {
		//we ignore policy in these tests
		PolicyServCommun.setPolicyServiceCallsStatic(null);
		
		NoviApiCallsImpl delete = new NoviApiCallsImpl();
		delete.setRemoteRis(remoteRis);
		delete.setUserFeedback(userFeedback);
		delete.setTestbed("PlanetLab");
		//delete.setTestbedCommun(mockTestbed);
		
		ReserveSlice reserve = new ReserveSlice();
		//reserve.setTestbedComm(mockTestbed);
		NOVIUserImpl user = new NOVIUserImpl("novi-user-33");
		reserve.reserveLocalSlice(createTestSlice(), 7777, user);
		String uri = NoviUris.createSliceURI("7777");
		
		assertNotNull(IRMLocalDbCalls.getLocalSlice(uri));
		assertTrue(IRMLocalDbCalls.checkSliceExist(uri));
		NOVIUserImpl user2 = new NOVIUserImpl("novi-user-55");
		//use wrong user
		delete.deleteSlice(user2, "7777","sessionID");
		assertNotNull(IRMLocalDbCalls.checkSliceExist(uri));
		assertTrue(IRMLocalDbCalls.checkSliceExist(uri));
		//delete the slice
		delete.deleteSlice(user, "7777","sessionID");
		assertNull(IRMLocalDbCalls.getLocalSlice(uri));
		assertFalse(IRMLocalDbCalls.checkSliceExist(uri));
		
	}
	
	
	private Topology createTestSlice()
	{
		Topology top = new TopologyImpl("boundTopology");
		Platform pl = new PlatformImpl("PlanetLab");
		VirtualNode vNode = new VirtualNodeImpl("vNode");
		pl.setContains(IMUtil.createSetWithOneValue(vNode));
		//Platform platf = new PlatformImpl("planetlab");
		top.setContains(IMUtil.createSetWithOneValue(vNode));
		//platf.setContains(IMUtil.createSetWithOneValue(vNode));
		return top;
	}

}
