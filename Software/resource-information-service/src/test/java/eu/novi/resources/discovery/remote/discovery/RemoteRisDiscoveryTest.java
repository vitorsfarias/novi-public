package eu.novi.resources.discovery.remote.discovery;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.novi.im.core.Node;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.ManipulateDB;
import eu.novi.resources.discovery.database.NoviUris;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.remote.serve.RemoteRisServeImpl;
import eu.novi.resources.discovery.util.Testbeds;



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
public class RemoteRisDiscoveryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);

	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Test
	public void testSlice() {
		RemoteRisDiscoveryImpl remoteRis = new RemoteRisDiscoveryImpl("PlanetLab");
		remoteRis.setRemoteRISList(createRemoteList());
		
		ReservationImpl slice = new ReservationImpl("slice222");
		remoteRis.storeRemoteSlice(slice);
		assertNotNull(remoteRis.getRemoteSlice(
				NoviUris.NOVI_IM_BASE_ADDRESS + "slice222"));
		assertEquals(1, 
				remoteRis.findRemotePartitioningCost(new TopologyImpl("topologyURI")).size());
		

		
		remoteRis.storeRemoteSlice(slice);
		assertNotNull(remoteRis.getRemoteSlice(
				NoviUris.NOVI_IM_BASE_ADDRESS + "slice222"));
		
	}
	
	
	public static List<RemoteRisServe> createRemoteList() {
		RemoteRisServe plRIS = new RemoteRisServeImpl();
		plRIS.setTestbed(Testbeds.PLANETLAB);
		RemoteRisServe fdRIS = new RemoteRisServeImpl();
		fdRIS.setTestbed(Testbeds.FEDERICA);
		
		List<RemoteRisServe> remotes = new ArrayList<RemoteRisServe>();
		remotes.add(plRIS); 
		remotes.add(fdRIS);
		return remotes;
	}
	
	@Test
	public void testGetResource()
	{

		RemoteRisDiscoveryImpl remoteRisDiscovery = new RemoteRisDiscoveryImpl("PlanetLab");
		remoteRisDiscovery.setRemoteRISList(createRemoteList());

		
		Node node = new NodeImpl("myNode");
		node.setHardwareType("x86");
		ManipulateDB.addObjectToTestDB(node, ManipulateDB.TESTBED_CONTEXTS);

		Node getNode = (Node) remoteRisDiscovery.getRemoteResource(NoviUris.NOVI_IM_BASE_ADDRESS + "myNode");
		assertNotNull(getNode);
		assertTrue(getNode.getHardwareType().equals("x86"));
		assertNull(remoteRisDiscovery.getRemoteResource(NoviUris.NOVI_IM_BASE_ADDRESS + "wrongNode"));
		
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadOwlFileTestDB("FEDERICATopology.owl", ManipulateDB.TESTBED_CONTEXTS);
		assertNotNull(remoteRisDiscovery.getRemoteResource(
				"http://fp7-novi.eu/im.owl#FEDERICA_dfn.erl.router1"));
		

	}
	
	@Test
	public void testGetAndCheckResources()
	{
		//we ignore policy in the first checks
		PolicyServCommun.setPolicyServiceCallsStatic(null);

		RemoteRisDiscoveryImpl remoteRisDiscovery = new RemoteRisDiscoveryImpl("PlanetLab");
		remoteRisDiscovery.setRemoteRISList(createRemoteList());
		

	
		
		ManipulateDB.clearTribleStoreTestDB();
		ManipulateDB.loadTripleOWLFile("fed.owl", ManipulateDB.TESTBED_CONTEXTS);
		Set<String> uris = new HashSet<String>();
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-dfn.erl.router1.ge-0/1/0");
		uris.add("http://fp7-novi.eu/im.owl#dfn.notExist1");
		uris.add("urn:publicid:IDN+federica.eu+link+dfn.erl.vserver1.vmnic7-dfn.erl.router1.ge-0/2/2");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/2/2-psnc.poz.vserver1.vmnic3");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/2-psnc.poz.vserver1.vmnic4");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.vserver1.vmnic4-psnc.poz.router1.ge-0/0/2");
		uris.add("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/3/2-psnc.poz.vserver1.vmnic7");
		uris.add("urn:publicid:IDN+federica.eu+link+dfn.erl.router1.ge-0/0/3-dfn.erl.vserver1.vmnic3");
		uris.add("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/1/2-out");
		uris.add("urn:publicid:IDN+federica.eu+interface+psnc.poz.router1.ge-0/0/0-in");
		uris.add("urn:publicid:IDN+federica.eu+interface+dfn.erl.vserver1.vmnic3-out");
		uris.add("http://fp7-novi.eu/im.owl#dfn.erl.vserver1.mem");
		uris.add("http://fp7-novi.eu/im.owl#dfn.notExist2");
		uris.add("http://fp7-novi.eu/im.owl#dfn.notExist3");
		assertEquals(11, remoteRisDiscovery.getRemoteResources(uris).size());
		
		
		///////TEST checkResources////////////////
	//	NOVIUserImpl user = new NOVIUserImpl("noviUser34");
		String user = "noviUser34";
		Set<String> foundRes = remoteRisDiscovery.checkRemoteResources(uris, user);
		assertEquals(11, foundRes.size());
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist2"));
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist3"));
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist1"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-dfn.erl.router1.ge-0/1/0"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+dfn.erl.vserver1.vmnic7-dfn.erl.router1.ge-0/2/2"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/2/2-psnc.poz.vserver1.vmnic3"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/2-psnc.poz.vserver1.vmnic4"));
		assertTrue(foundRes.contains(
				"http://fp7-novi.eu/im.owl#dfn.erl.vserver1.mem"));

		
		///TEST AUTHORIZED RESOURCES////
		InterfaceForRIS policyMock = mock(InterfaceForRIS.class);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		map.put("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-dfn.erl.router1.ge-0/1/0",
				true);
		map.put("urn:publicid:IDN+federica.eu+link+dfn.erl.vserver1.vmnic7-dfn.erl.router1.ge-0/2/2",
				true);
		map.put("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/2/2-psnc.poz.vserver1.vmnic3",
				false);
		map.put("urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/2-psnc.poz.vserver1.vmnic4",
				false);
		when(policyMock.AuthorizedForResource(any(String.class), any(NOVIUserImpl.class), any(Set.class), 
				any(Integer.class))).thenReturn(map);
		PolicyServCommun.setPolicyServiceCallsStatic(policyMock);
		
		foundRes = remoteRisDiscovery.checkRemoteResources(uris, user);
		//TODO enable check authorize resources
		assertEquals(2, foundRes.size());
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist2"));
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist3"));
		assertFalse(foundRes.contains("http://fp7-novi.eu/im.owl#dfn.notExist1"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-dfn.erl.router1.ge-0/1/0"));
		assertTrue(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+dfn.erl.vserver1.vmnic7-dfn.erl.router1.ge-0/2/2"));
		assertFalse(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/2/2-psnc.poz.vserver1.vmnic3"));
		assertFalse(foundRes.contains(
				"urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/2-psnc.poz.vserver1.vmnic4"));

	}
	
	

}
