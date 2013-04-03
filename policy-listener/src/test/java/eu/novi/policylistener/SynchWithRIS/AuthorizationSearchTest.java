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
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */
package eu.novi.policylistener.SynchWithRIS;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualLinkImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.Role;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.policy.impl.RoleImpl;
import eu.novi.im.util.IMUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.policylistener.ponder2comms.TelnetClient;
import eu.novi.policylistener.synchWithRIS.AuthorizationSearch;

public class AuthorizationSearchTest {
	TelnetClient MockTelCli = mock(TelnetClient.class);
	AuthorizationSearch auths= new AuthorizationSearch();
	private static final transient Logger log = LoggerFactory.getLogger(AuthorizationSearchTest.class);
	
	@Test
	//@Ignore
	public void testAuthorizedForResource() {
		//auths.setTelnetClient(new TelnetClient());
		//String User="User1";
		NOVIUserImpl User=new NOVIUserImpl("testUser");
		RoleImpl plrole= new RoleImpl("PlanetLabUser");
		User.setHasNoviRole(plrole);
		User.setBelongsToDomain("4");
		Set<String> Resources= new HashSet<String>();
		Resources.add("planetlab1");
		//Resources.add("planetlab2");
		Resources.add("http://fp7-novi.eu/im.owl#PlanetLab+planetlab1-novi.lab.netmode.ece.ntua.gr");
		Resources.add("http://fp7-novi.eu/im.owl#PlanetLab+planetlab2-novi.lab.netmode.ece.ntua.gr");
		Resources.add("http://fp7-novi.eu/im.owl#PlanetLab+smilax1.man.poznan.pl");
		Map<String, Boolean> authRes;
		//AuthorizationSearch auths= new AuthorizationSearch();
		authRes=auths.AuthorizedForResource(null, User, Resources,0);
		System.out.println(authRes);
		//assertEquals(authRes,"{http://fp7-novi.eu/im.owl#PlanetLab+planetlab2-novi.lab.netmode.ece.ntua.gr=true, http://fp7-novi.eu/im.owl#PlanetLab+planetlab1-novi.lab.netmode.ece.ntua.gr=true, http://fp7-novi.eu/im.owl#PlanetLab+planetds2-novi.lab.netmode.ece.ntua.gr=false, planetlab1=true}");
		//removed it for the mock. With the mock we can't check the results

	}
	
	@Test
	//@Ignore
	public void testAuthorizedForResource2() {
		NOVIUserImpl User=new NOVIUserImpl("testUser");
		RoleImpl plrole= new RoleImpl("PlanetLabUser");
		User.setHasNoviRole(plrole);
		User.setBelongsToDomain("4");
		Set<String> Resources= new HashSet<String>();
		Resources.add("planetlab1");
		Map<String, Boolean> authRes;
		authRes=auths.AuthorizedForResource(null, User, Resources,0);
		System.out.println(authRes);
		
	}
	
	@Test
	//@Ignore
	public void testAuthorizedForResourced() {
		String User="User1";
		String Resources= "Resource";
		//AuthorizationSearch auths= new AuthorizationSearch();
		String authRes=auths.AuthorizedForResourced(null, User, Resources);
		if (authRes=="AUTH")
		{
			System.out.println("OK");
		}

	}
	
	@Test
	//@Ignore
	public void testAddTopologyOneVnode(){
		Topology topology = new TopologyImpl("myTopology");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		vNode1.setHardwareType("x86");
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		topology.setContains(IMUtil.createSetWithOneValue(vNode1));
	//	System.out.println(topology.getContains().size());
		System.out.println("OK so far");
		//AuthorizationSearch auths= new AuthorizationSearch();
		auths.AddTopology(null, topology, "mytopology");
	}
	
	@Test
	//@Ignore
	public void testAddMultibleTopologies(){
		log.info("testAddMultibleTopologies");
		/*Topology topology1 = new TopologyImpl("myTopology1");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		vNode1.setHardwareType("x86");
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		topology1.setContains(IMUtil.createSetWithOneValue(vNode1));
		Reservation reservation1= (Reservation) topology1;
		Topology topology12 = new TopologyImpl("myTopology12");
		VirtualNode vNode12 = new VirtualNodeImpl("vnode12");
		vNode12.setHardwareType("x86");
		vNode12.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab12.ntua.gr")));
		topology12.setContains(IMUtil.createSetWithOneValue(vNode12));
		Reservation reservation12= (Reservation) topology12;*/
	//	System.out.println(topology.getContains().size());
		System.out.println("OK so far");
		//AuthorizationSearch auths= new AuthorizationSearch();
		ReservationImpl reservation1 = new ReservationImpl("myReservation11");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		vNode1.setHardwareType("x86");
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		reservation1.setContains(IMUtil.createSetWithOneValue(vNode1));
		ReservationImpl reservation12 = new ReservationImpl("myReservation12");
		VirtualNode vNode12 = new VirtualNodeImpl("vnode12");
		vNode12.setHardwareType("x86");
		vNode12.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab12.ntua.gr")));
		reservation12.setContains(IMUtil.createSetWithOneValue(vNode12));
		Set<Reservation> res=new HashSet<Reservation>();
		res.add(reservation1);
		res.add(reservation12);
		auths.AddAllTopologies(null,res);
		
	}
	
	@Test
	//@Ignore
	public void testAddMultibleTopologies2(){
		log.info("testAddMultibleTopologies2");
		System.out.println("OK so far");
	/*	ReservationImpl reservation1 = new ReservationImpl("myReservation11");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		vNode1.setHardwareType("x86");
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		reservation1.setContains(IMUtil.createSetWithOneValue(vNode1));
		ReservationImpl reservation12 = new ReservationImpl("myReservation12");
		VirtualNode vNode12 = new VirtualNodeImpl("vnode12");
		vNode12.setHardwareType("x86");
		vNode12.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab12.ntua.gr")));
		reservation12.setContains(IMUtil.createSetWithOneValue(vNode12));
	*/	Set<Reservation> res=new HashSet<Reservation>();
	//	res.add(reservation1);
	//	res.add(reservation12);
		auths.AddAllTopologies(null,res);
		
	}
	@Test
	//@Ignore
	public void testAddMultibleTopologies3(){
		log.info("testAddMultibleTopologies3");
		auths.AddAllTopologies(null,null);
		
	}
	
/*	@Test
	public void testAddTopologyOneVnodeandOneVLink(){
		Topology topology = new TopologyImpl("myTopology2");
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		VirtualNode vNode2 = new VirtualNodeImpl("vnode2");
		VirtualLink vLink1= new VirtualLinkImpl("vlink1");
		//vNode1.setHardwareType("x86");
		//NodeImpl node1 = IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")
		vNode1.setImplementedBy(node1));
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab2.ntua.gr")));
		vLink1.setImplementedBy()));
		topology.setContains(IMUtil.createSetWithOneValue(vNode1));
	//	System.out.println(topology.getContains().size());
		System.out.println("OK so far");
		//AuthorizationSearch auths= new AuthorizationSearch();
		auths.AddTopology(topology, "mytopology2");
	}*/
	
	@Test
	//@Ignore
	public void testAddEmptyTopology(){
		System.out.println("testAddEmptyTopology()");
		Topology topology = new TopologyImpl("myemptyTopology");
		//VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		//vNode1.setHardwareType("x86");
		//vNode1.setImplementedBy(IMUtil.createSetWithOneValue(new NodeImpl("planetlab1.ntua.gr")));
		System.out.println("TESTING THE EMPTY TOPO");
	//	topology.setContains(null);
		//System.out.println(topology.getContains().size());
		
		System.out.println("OK so far");
		//AuthorizationSearch auths= new AuthorizationSearch();
		auths.AddTopology(null, topology, "myemptyTopology");
		
		
	}
	
	@Before
	public <T> void initialize() throws IOException {
		System.out.println("HERE");
		log.debug("Initializing AuthorizationSearchTest...");
		
		auths.setTelnetClient(MockTelCli);
		when(MockTelCli.TelnetPonder2(anyString())).thenReturn(telClientMock());
		System.out.println("EDO EFTASE");
		
	}

	private String telClientMock() {
		// TODO Auto-generated method stub
		System.out.println("telnet mock");
		Random ran= new Random();
		//System.out.println("Tiponno tous booleans: "+ran.nextBoolean());
		boolean kat= ran.nextBoolean();
		if (kat==true)
		{
			return "true";
		}
		else 
		return "false";
		
	}
	
	@Test
//	@Ignore
	public void setandgetUserfeedback() {
		auths.setUserFeedback(null);
		System.out.println(auths.getUserFeedback());

	}
	
	@Test
	//@Ignore
	public void testAuthorizedForDeletion(){
		System.out.println("Test AuthorizedForDeletion");
		NOVIUserImpl caller= new NOVIUserImpl("Yiannos");
		NOVIUserImpl owner= new NOVIUserImpl("Yiannos");
		Set<NOVIUserImpl> owners= new HashSet<NOVIUserImpl>();
		owners.add(owner);
		boolean res=auths.AuthorizedForDeletion(null, caller, owners);
		assertEquals(res,true);
		System.out.println("OK");
	}
	
	@Test
	//@Ignore
	public void testAuthorizedForDeletionPI(){
		System.out.println("Test AuthorizedForDeletionPI");
		NOVIUserImpl caller= new NOVIUserImpl("Yiannos");
		RoleImpl plrole= new RoleImpl("PlanetLabPI");
		caller.setHasNoviRole(plrole);
		caller.setBelongsToDomain("4");
		NOVIUserImpl owner= new NOVIUserImpl("Kostis");
		owner.setBelongsToDomain("4");
		Set<NOVIUserImpl> owners= new HashSet<NOVIUserImpl>();
		owners.add(owner);
		boolean res=auths.AuthorizedForDeletion(null, caller, owners);
		//assertEquals(res,true);
		System.out.println("OK");
		
	}
	
	@Test
	@Ignore
	public void testInfoaboutExpirationTIme()
	{
		System.out.println("Slice about to expire");
		auths.InformExpirationTime(null, null, null);
	}
	
	@Test
	@Ignore
	public void testInfoaboutExpirationTIme2()
	{
		System.out.println("Slice about to expire");
		NOVIUserImpl caller= new NOVIUserImpl("ykryftis@netmode.ece.ntua.gr");
		Date date=new Date("5/18/2013");
		auths.InformExpirationTime(caller, "topoID", date);
	}
	
	@Test
	@Ignore
	public void testInfoaboutExpiration()
	{
		System.out.println("Slice expired");
		auths.InformExpirationHappened(null, null, null);
	}
	
	@Test
	@Ignore
	public void testUpdateExpiration()
	{
		System.out.println("Slice expired");
		NOVIUserImpl caller= new NOVIUserImpl("ykryftis@netmode.ece.ntua.gr");
		caller.setHasSessionKey("IMDciUUdS+oh+3Gsflo4tR9qiPBARJA3Hr+mBubflXo=");
		Date date=new Date("3/18/2013");
		String sliceName = "novi_slice_699986568";
		auths.UpdateExpirationTime(caller, sliceName, date);
	}
	
}
