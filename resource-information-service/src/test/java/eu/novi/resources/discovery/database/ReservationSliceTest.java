package eu.novi.resources.discovery.database;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.im.util.Validation;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;
import eu.novi.resources.discovery.impl.IRMCallsImpl;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;



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
public class ReservationSliceTest {
	private static final transient Logger log =
			LoggerFactory.getLogger(ReservationSliceTest.class);
	
	//TestbedCommunication mockTestbed;
	RHCreateDeleteSliceResponseImpl mockCreateSliceResp;
	ReserveSlice reserve;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);


	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestbedCommunication.assignCalls2TestbedFromRHStatic(null);
		ConnectionClass.stopStorageService();
	}

	@Before
	public void setUp() throws Exception {
		log.debug("Initializing ReservationSliceTest...");
		reserve = new ReserveSlice();

		// Setting up local environment
		log.debug("Setting up local environment...");
		//mockTestbed = mock(TestbedCommunication.class);
		//reserve.setTestbedComm(mockTestbed);
		
		PeriodicUpdate sched = new PeriodicUpdate();
		sched.setScheduler(Executors.newScheduledThreadPool(4));

		//stubbing mockRIS
		log.debug("stubbing mockRIS...");
		mockCreateSliceResp = mock(RHCreateDeleteSliceResponseImpl.class);
		when(mockCreateSliceResp.hasError()).thenReturn(false);
		when(mockCreateSliceResp.getSliceID()).thenReturn("sliceID1213");
		ArrayList plList = new ArrayList<String>();
		plList.add("http://fp7-novi.eu/im.owl#PlanetLab");
		when(mockCreateSliceResp.getListOfTestbedsWhereSliceIsCreated()).
		thenReturn(plList);
		//create rdf topology from java objects
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();
		
		when(mockCreateSliceResp.getTopologyCreated()).
		thenReturn(imRep.exportIMObjectToString(createTopology("theNodeManifest")));
		
		//
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.createSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), any(TopologyImpl.class))).
		thenReturn(mockCreateSliceResp);
		when(calls2TestbedFromRHMock.updateSlice(any(String.class),
				any(NOVIUserImpl.class), any(String.class), 
				any(TopologyImpl.class), any(TopologyImpl.class))).
		thenReturn(mockCreateSliceResp);
		
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
		///
		/*when(mockTestbed.reserveSlice(any(NOVIUserImpl.class), any(String.class), any(Topology.class))).
		thenReturn(mockCreateSliceResp);
		when(mockTestbed.updateSlice(any(NOVIUserImpl.class), any(String.class), any(Topology.class))).
		thenReturn(mockCreateSliceResp);*/
		
		
	}

	@After
	public void tearDown() throws Exception {
	}
	


	@Test
	public void testHasSourceHasSink()
	{
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();

		String requestTest = FindResourcesDemoTest.readFile("src/test/resources/2slivers_1router_bound.owl");
		Topology reqTopo = imRep.getIMObjectFromString(requestTest, Topology.class);
		Validation validation = new Validation();
		String st = validation.checkLinksForSinkSource(reqTopo);
		System.out.println(st);
		assertTrue(st.equals("")); //valid
		
		int id = 47346;
		//reserve slice
		NOVIUserImpl user =  new NOVIUserImpl("user-novi-1");
		user.setHasSessionKey("sessionKey33");
		reserve.reserveLocalSlice(reqTopo, id, user);
		String uri = ReserveSlice.createSliceURI(String.valueOf(id));
		st = validation.checkLinksForSinkSource(IRMLocalDbCalls.getLocalSlice(uri));
		System.out.println(st);
		assertTrue(st.equals("")); //valid
		
		OwlCreator owlCrea = new OwlCreator();
		String rdfStr = owlCrea.getSliceInfoToString(uri);
		//System.out.println(rdfStr);
		
		IMRepositoryUtil imRepos = new IMRepositoryUtilImpl();
		st = validation.checkLinksForSinkSource(imRepos.getIMObjectFromString(rdfStr, Reservation.class));
		System.out.println(st);
		assertTrue(st.equals("")); //valid
		
		
	}

	
	@Test
	public void testRemoteSlice()
	{
		ReserveSlice reserve = new ReserveSlice();
		Reservation slice = new ReservationImpl("slice_4343");
		reserve.storeRemoteSliceInfo(slice, "PlanetLab");
		//ConnectionClass.getConnection2DB().setReadContexts(LocalDbCalls.createNoviURI("slice_4343"));
		//LocalDbCalls.execPrintStatement(slice.toString(), null, null, null);
		assertEquals(4, LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(slice.toString()), null, null));
		Reservation retSlice = IRMLocalDbCalls.getLocalSlice(slice.toString());
		assertEquals(slice.toString(), retSlice.toString());
		assertNull(retSlice.getContains());
		//ConnectionClass.getConnection2DB().setReadContexts();
	}
	
	
	
	/**test also some methods in OwlCreator class
	 * @throws RepositoryException
	 */
	@Test
	public void testReserveUpdateSlice() throws RepositoryException {
		
		IRMCalls irmCalls = new IRMCallsImpl();
		NOVIUserImpl user =  new NOVIUserImpl("user-novi-1");
		user.setHasSessionKey("sessionKey33");
		
		ReserveResponse response = reserve.reserveLocalSlice(
				createTopology("smilax1.man.poznan.pl"), 8898, user);

		Integer sliceID = response.getSliceID();
		String sliceURI = ReserveSlice.createSliceURI(sliceID.toString());
		//LocalDbCalls.execPrintStatement(null, null, null);
		//test getSlice
		Reservation storedSlice = irmCalls.getSlice(sliceURI);
		assertFalse(response.hasError());
		assertEquals(ReserveMess.NO_ERROR, response.getErrorMessage());
		assertNotNull(storedSlice);
		assertEquals(1, storedSlice.getContains().size());
		assertTrue(storedSlice.getContains().iterator().next().toString().
				equals("http://fp7-novi.eu/im.owl#virtualNode12"));
		log.debug(storedSlice.getContains().iterator().next().getIsContainedIn().toString());
		//in slice reservation
		assertEquals(1, storedSlice.getContains().iterator().next().getIsContainedIn().size());

		//check the reservation group, must be 5 statements
		//type reservation and group, contains (1 virtual node), hasLifetime, autoUpdate

		assertEquals(5,LocalDbCalls.execStatementReturnSum(NoviUris.createNoviURI(
				ReserveSlice.SLICE_PREFIX + sliceID), null, null));

		//LocalDbCalls.showAllContentOfDB();
		//3 statements
		//type, startTime, endTime
		assertEquals(3, LocalDbCalls.execStatementReturnSum(
				NoviUris.getSliceLifetimeURI(sliceURI), null, null));

		VirtualNode vNo = (VirtualNode)IMUtil.getOneValueFromSet(storedSlice.getContains());
		for (NodeComponent comp : vNo.getHasComponent())
		{
			log.debug(comp.toString());
			if (comp instanceof CPU)
			{
				log.debug(((CPU)comp).getHasCores().toString());
			}
		}
		//Reservation slice = (Reservation) ConnectionClass.getConnection2DB().
		//		getObject(LocalDbCalls.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID));
		assertEquals(1, storedSlice.getContains().size()); //one virtual node
		
		String virtualNodeUri = IMUtil.getOneValueFromSet(storedSlice.getContains()).toString();
		
		//test user
		assertNotNull(NOVIUserClass.getNoviUserSlice(sliceURI.toString()));
		System.out.println(NOVIUserClass.getNoviUserSlice(sliceURI.toString()));
		
		assertTrue(NOVIUserClass.getNoviUserSlice(
				sliceURI.toString()).toString().equals("http://fp7-novi.eu/NOVIPolicyService.owl#user-novi-1"));
		assertTrue(NOVIUserClass.getNoviUserSlice(
				sliceURI.toString()).getHasSessionKey().equals("sessionKey33"));
		
		LocalDbCalls.execPrintStatement(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"), true);
		//virtual node, one in the testbed context and one in the manifest
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"),
				NoviUris.getSliceManifestContextUri(sliceURI)));
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"),
				NoviUris.createURI(sliceURI)));
		//virtual node implement by
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createNoviURI("implementedBy"), 
				NoviUris.createNoviURI("smilax1.man.poznan.pl")));
		//slice reservation has lifetime
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID),
				NoviUris.createNoviURI("hasLifetime"), 
				NoviUris.getSliceLifetimeURI(sliceURI)));
		
		
		//////////TEST OwlCreator/////////////////////////////////////////////////
		OwlCreator owlCreator = new OwlCreator();
		String sliceStr = owlCreator.getSliceInfoToString(sliceURI);
		assertNotNull(sliceStr);
		//log.debug("\n%%%%%%%%%%%%%%%%SLICE OWL%%%%%%%%%%%%%%%%%%%");
		//System.out.println(sliceStr);
		//owlCreator.storeSliceInfoToFile(sliceURI);
		String owlStr = OwlCreator.exportDBinOwl(NoviUris.createNoviURI("not_exist_uri").toString());
		assertNull(owlStr);
		owlStr = OwlCreator.getSliceManifestInfoToString(sliceURI);
		assertNotNull(owlStr);
		log.debug("THE MANIFEST IS : :\n{}", owlStr);
		//log.debug(OwlCreator.getSliceInfoToString(sliceURI));
		//log.debug(OwlCreator.exportDBinOwl(sliceURI));
		
		Set<String> vNodes = owlCreator.getVNodes4SliceToString(sliceURI);
		assertTrue(vNodes.size() == 1);
		for(String vNod : vNodes)
		{
			//log.debug("\n%%%%%%%%%%%%%%%%VIRTUAL NODE%%%%%%%%%%%%%%%%%%%");
			//System.out.println(vNod);
		}
		//owlCreator.storeVNodesToFiles(sliceURI);
		
		//test get platforms
		Set<String> platforms = ReserveSlice.getPlatformsFromSlice(NoviUris.
				createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID).toString());
		assertEquals(1, platforms.size());
		assertTrue(platforms.contains("http://fp7-novi.eu/im.owl#PlanetLab"));
		
		
		
		////TEST UPDATE/////////////////////////////////
		assertEquals(ReserveMess.SLICE_NOT_EXIST,
				reserve.updateLocalSlice(createTopology("smilax1.man.poznan.pl"), 1111).getErrorMessage());

		sliceID = 8898;
		assertNotNull(NOVIUserClass.getNoviUserSlice(sliceURI));
		response = reserve.updateLocalSlice(createTopology("smilax3.man.poznan.pl"), sliceID);
		storedSlice = irmCalls.getSlice(
				ReserveSlice.createSliceURI(sliceID.toString()));
		assertNotNull(NOVIUserClass.getNoviUserSlice(sliceURI.toString()));
		
		assertFalse(response.hasError());
		assertEquals( ReserveMess.NO_ERROR, response.getErrorMessage());
		assertNotNull(storedSlice);
		assertEquals(1, storedSlice.getContains().size());

		//check the reservation group, must be 4 statements
		//type reservation and group, contains (1 virtual node), hasLifetime, autoUpdate
		assertEquals(5,LocalDbCalls.execStatementReturnSum(NoviUris.createNoviURI(
				ReserveSlice.SLICE_PREFIX + sliceID), null, null));

		//LocalDbCalls.showAllContentOfDB();
		//3 statements
		//type, startTime, endTime
		assertEquals(3, LocalDbCalls.execStatementReturnSum(
				NoviUris.getSliceLifetimeURI(sliceURI), null, null));

		vNo = (VirtualNode)IMUtil.getOneValueFromSet(storedSlice.getContains());

		//Reservation slice = (Reservation) ConnectionClass.getConnection2DB().
		//		getObject(LocalDbCalls.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID));
		assertEquals(1, storedSlice.getContains().size()); //one virtual node

		virtualNodeUri = IMUtil.getOneValueFromSet(storedSlice.getContains()).toString();

		//virtual node
		//virtual node, one in the testbed context and one in the manifest
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"),
				NoviUris.getSliceManifestContextUri(sliceURI)));
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"),
				NoviUris.createURI(sliceURI)));
		//virtual node implement by
		assertEquals(1,
				LocalDbCalls.execStatementReturnSum(
						NoviUris.createURI(virtualNodeUri),
						NoviUris.createNoviURI("implementedBy"), 
						NoviUris.createNoviURI("smilax3.man.poznan.pl")));
		
		assertEquals(0,
				LocalDbCalls.execStatementReturnSum(
						NoviUris.createURI(virtualNodeUri),
						NoviUris.createNoviURI("implementedBy"), 
						NoviUris.createNoviURI("smilax1.man.poznan.pl")));
		
		//slice reservation has lifetime
		assertEquals(1,
				LocalDbCalls.execStatementReturnSum(
						NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID),
						NoviUris.createNoviURI("hasLifetime"), 
						NoviUris.getSliceLifetimeURI(sliceURI)));

		//test get platforms
		platforms = ReserveSlice.getPlatformsFromSlice(NoviUris.
				createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID).toString());
		assertEquals(1, platforms.size());
		assertTrue(platforms.contains("http://fp7-novi.eu/im.owl#PlanetLab"));

		/*ReserveResponse response2 = reserve.reserveLocalSlice(createTopology("not_exist_node"), 777);
		log.debug(response2.getErrorMessage() + ", message: " + response2.getMessage());
		assertTrue(response2.hasError());
		assertEquals(response2.getErrorMessage(), ReserveMess.NODE_NOT_IN_DB);*/


	}
	
	
	
	
	

	
	@Test
	public void testReserveDeleteSlice() throws RepositoryException {
		//TODO if I enable this also I have error
		IRMCalls irmCalls = new IRMCallsImpl();
		
		NOVIUserImpl user =  new NOVIUserImpl("user-novi-2");
		ReserveResponse response = reserve.reserveLocalSlice(
				createTopology("smilax1.man.poznan.pl"), 7777, user);

		Integer sliceID = response.getSliceID();
		URI sliceURI = NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID);
		//LocalDbCalls.execPrintStatement(null, null, null);
		//test getSlice
		Reservation getSlice = irmCalls.getSlice(sliceURI.toString());
		assertFalse(response.hasError());
		assertEquals(response.getErrorMessage(), ReserveMess.NO_ERROR);
		assertNotNull(getSlice);
		assertEquals(1, getSlice.getContains().size());
		log.debug(getSlice.getContains().toString());
		
		//test user
		assertNotNull(NOVIUserClass.getNoviUserSlice(sliceURI.toString()));

		//check the reservation group, must be 5 statements
		//type reservation and group, contains (1 virtual node), hasLifetime, autoUpdate

		assertEquals(5,LocalDbCalls.execStatementReturnSum(NoviUris.createNoviURI(
				ReserveSlice.SLICE_PREFIX + sliceID), null, null));

		//3 statements
		//type, startTime, endTime
		assertEquals(3, LocalDbCalls.execStatementReturnSum(
				NoviUris.getSliceLifetimeURI(sliceURI.toString()), null, null));
		//Reservation slice = (Reservation) ConnectionClass.getConnection2DB().
		//		getObject(LocalDbCalls.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID));
		assertEquals(1, getSlice.getContains().size()); //one virtual node
		
		String virtualNodeUri = IMUtil.getOneValueFromSet(getSlice.getContains()).toString();
		
		//virtual node
		assertEquals(1, 
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createRdfURI("type"), 
				NoviUris.createNoviURI("VirtualNode"),
				NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID)));
		//virtual node implement by
		assertEquals(1, 
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createURI(virtualNodeUri),
				NoviUris.createNoviURI("implementedBy"), 
				NoviUris.createNoviURI("smilax1.man.poznan.pl"),
				NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID)));
		//slice reservation has lifetime
		assertEquals(1,
		LocalDbCalls.execStatementReturnSum(
				NoviUris.createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID),
				NoviUris.createNoviURI("hasLifetime"), 
				NoviUris.getSliceLifetimeURI(sliceURI.toString())));
		
		//slice reservation has novi user, context novi_user and sliceURI
				assertEquals(2,
				LocalDbCalls.execStatementReturnSum(
						NoviUris.createPolicyURI("user-novi-2"),
						NoviUris.createRdfURI("type"), 
						NoviUris.createPolicyURI("NOVIUser")));
		
		//test get platforms
		Set<String> platforms = ReserveSlice.getPlatformsFromSlice(NoviUris.
				createNoviURI(ReserveSlice.SLICE_PREFIX + sliceID).toString());
		assertEquals(1, platforms.size());
		assertTrue(platforms.contains("http://fp7-novi.eu/im.owl#PlanetLab"));
		
		//create some slices
		//create 2222
		URI uri2 = NoviUris.createNoviURI("slice_2222");
		assertFalse(LocalDbCalls.checkSliceExist(uri2.toString()));
		NOVIUserImpl user2 =  new NOVIUserImpl("user-novi-3");
		reserve.reserveLocalSlice(
				createTopology("smilax1.man.poznan.pl"), 2222, user2);
		assertTrue(LocalDbCalls.checkSliceExist(uri2.toString()));
		//test user
		NOVIUserImpl retrUser = null;
		retrUser = NOVIUserClass.getNoviUserSlice(uri2.toString());
		assertNotNull(retrUser);
		assertTrue(retrUser.toString().equals(user2.toString()));
		
		//create 3333
		URI uri3 = NoviUris.createNoviURI("slice_3333");
		assertFalse(LocalDbCalls.checkSliceExist(uri3.toString()));
		NOVIUserImpl user3 =  new NOVIUserImpl("user-novi-4");
		reserve.reserveLocalSlice(
				createTopology("smilax1.man.poznan.pl"), 3333, user3);
		assertTrue(LocalDbCalls.checkSliceExist(uri3.toString()));

		//test user
		retrUser = null;
		retrUser = NOVIUserClass.getNoviUserSlice(uri3.toString());
		assertNotNull(retrUser);
		assertTrue(retrUser.toString().equals(user3.toString()));
		
		
		
		//delete a slices
		
		//delete 3333
		assertTrue(LocalDbCalls.execStatementReturnSum(null, null, null, uri3) > 0);
		assertNotNull(NOVIUserClass.getNoviUserSlice(uri3.toString()));
		assertNotNull(IRMLocalDbCalls.getLocalSlice(uri3.toString()));
		assertTrue(DeleteSlice.deleteLocalSliceInfo(uri3.toString()));
		assertEquals(0, LocalDbCalls.execStatementReturnSum(null, null, null, uri3));
		assertNull(NOVIUserClass.getNoviUserSlice(uri3.toString()));
		assertFalse(LocalDbCalls.checkSliceExist(uri3.toString()));
		
		//delete 2222
		assertTrue(LocalDbCalls.execStatementReturnSum(null, null, null, uri2) > 0);
		assertNotNull(NOVIUserClass.getNoviUserSlice(uri2.toString()));
		assertNotNull(IRMLocalDbCalls.getLocalSlice(uri2.toString()));
		assertTrue(DeleteSlice.deleteLocalSliceInfo(uri2.toString()));
		assertEquals(0, LocalDbCalls.execStatementReturnSum(null, null, null, uri2));
		assertNull(NOVIUserClass.getNoviUserSlice(uri2.toString()));
		assertFalse(LocalDbCalls.checkSliceExist(uri2.toString()));
		
		//delete 7777
		URI uri = NoviUris.createNoviURI("slice_7777");
		//LocalDbCalls.execPrintStatement(null, null, null, uri);
		//LocalDbCalls.showAllContentOfDB();
		//LocalDbCalls.execPrintStatement(null, null, null, true,uri);
		assertEquals(38, LocalDbCalls.execStatementReturnSum(null, null, null, uri));
		//check if the manifest was stored
		assertEquals(26, LocalDbCalls.execStatementReturnSum(null, null, null,
				NoviUris.getSliceManifestContextUri(uri.toString())));
		assertNotNull(IRMLocalDbCalls.getLocalSlice(uri.toString()));
		//delete slice
		assertTrue(DeleteSlice.deleteLocalSliceInfo("http://fp7-novi.eu/im.owl#slice_7777"));
		//LocalDbCalls.execPrintStatement(null, null, null, uri);
		assertEquals(0, LocalDbCalls.execStatementReturnSum(null, null, null, uri));
		assertEquals(0, LocalDbCalls.execStatementReturnSum(null, null, null,
				NoviUris.getSliceManifestContextUri(uri.toString())));
		assertNull(NOVIUserClass.getNoviUserSlice(sliceURI.toString()));
		

		

		
		/*ReserveResponse response2 = reserve.reserveLocalSlice(createTopology("not_exist_node"), 777);
		log.debug(response2.getErrorMessage() + ", message: " + response2.getMessage());
		assertTrue(response2.hasError());
		assertEquals(response2.getErrorMessage(), ReserveMess.NODE_NOT_IN_DB);*/
		
		
	}
	
	
	@Test
	public void testIllegalActions()
	{
		NOVIUserImpl user =  new NOVIUserImpl("user-novi-1");
		ReserveResponse response = reserve.reserveLocalSlice(null, 8888, user);
		assertTrue(response.hasError());
		

		response = reserve.reserveLocalSlice(new TopologyImpl("topology"), 8888, user);
		assertTrue(response.hasError());
		
		Topology top = new TopologyImpl("topoogy");
		top.setContains(new HashSet<Node>());
		response = reserve.reserveLocalSlice(top, 8888, user);
		assertTrue(response.hasError());
		
		//mockTestbed = mock(TestbedCommunication.class);
		//reserve.setTestbedComm(mockTestbed);
		log.debug("stubbing mockRIS...");
		RHCreateDeleteSliceResponse mockCreateSliceResp = mock(RHCreateDeleteSliceResponse.class);
		when(mockCreateSliceResp.hasError()).thenReturn(true);
		Set<Resource> res = new HashSet<Resource>();
		res.add(new ResourceImpl("resource"));
		assertTrue(response.hasError());
		
	
		
	}
	
	
	@Test
	public void testGetSliceManifestOwl() throws RepositoryException
	{
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();
		//mock the RH manifest
		Topology manifTop = createTopologyForIpTest();
		reserve.setPlanetLabIPs(manifTop);
		when(mockCreateSliceResp.getTopologyCreated()).
		thenReturn(imRep.exportIMObjectToString(manifTop));


		//reserve.setTestbedComm(mockTestbed);
		reserve.reserveLocalSlice(createTopologyForIpTest(), 7777, new NOVIUserImpl("aNoviUSer"));
		String sliceOwl = OwlCreator.getSliceManifestInfoToString(NoviUris.createSliceURI("7777"));
		assertFalse(sliceOwl.isEmpty());

		log.debug("#####MANIFEST#####\n" + sliceOwl); 
		//log.debug("#####SLICE INFO USING EXPORT#####\n" + OwlCreator.exportDBinOwl(NoviUris.createSliceURI("7777")));
		//log.debug("#####GET SLICE INFO USING OLD VERSION#####\n" + OwlCreator.getSliceInfoToString(NoviUris.createSliceURI("7777"))); 
		//log.debug("$$$$$EXPORT ALL THE DB$$$$$$\n" + OwlCreator.exportDBinOwl(null));


		String st1 = 
				"<hasIPv4Address rdf:resource=\"http://fp7-novi.eu/unit.owl#node1-if0-in-ip\"/>";
		String st2 = "<hasValue xmlns=\"http://fp7-novi.eu/unit.owl#\">147.102.22.67</hasValue>";
		String st3 = "<hasValue xmlns=\"http://fp7-novi.eu/unit.owl#\">150.254.160.21</hasValue>";
		String st4 = "<owl:Ontology rdf:about=\"http://fp7-novi.eu/imcore.owl\">";
		String st5 = "<rdf:type rdf:resource=\"http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity\"/>";
		assertTrue(sliceOwl.contains(st1));
		assertTrue(sliceOwl.contains(st2));
		assertTrue(sliceOwl.contains(st3));
		assertTrue(sliceOwl.contains(st4));
		assertFalse(sliceOwl.contains(st5));
		
		String st6 = "<rdf:Description rdf:about=\"http://fp7-novi.eu/im.owl#smilax3.novipl.man.poznan.pl\">";
		assertTrue(sliceOwl.contains(st6));
		String st7 = "<rdf:Description rdf:about=\"http://fp7-novi.eu/im.owl#node1-if0-in\">";
		assertTrue(sliceOwl.contains(st7));
		String st8 = "<rdf:Description rdf:about=\"http://fp7-novi.eu/im.owl#vnode2\">";
		assertTrue(sliceOwl.contains(st8));
		


	}
	
	
	@Test
	public void testIPassignment() throws RepositoryException
	{
		ReserveSlice reserve = new ReserveSlice();
		
		Topology top = createTopologyForIpTest();
		VirtualNode vN = (VirtualNode) IMUtil.getOneValueFromSet(top.getContains());
		assertNull(IMUtil.getOneValueFromSet(vN.getHasInboundInterfaces()).getHasIPv4Address());
		assertNull(IMUtil.getOneValueFromSet(vN.getHasOutboundInterfaces()).getHasIPv4Address());
		
		reserve.setPlanetLabIPs(top);
		Set<Resource> vNodes = top.getContains();
		for (Resource r : vNodes)
		{
			vN = (VirtualNode) r;

			assertNotNull(IMUtil.getOneValueFromSet(vN.getHasInboundInterfaces()).getHasIPv4Address());
			//assertEquals("192.168.101.2/24", IMUtil.getOneValueFromSet(
					//vN.getHasInboundInterfaces()).getHasIPv4Address().iterator().next().getHasValue());
			assertNotNull(IMUtil.getOneValueFromSet(vN.getHasOutboundInterfaces()).getHasIPv4Address());
			
			Node n = IMUtil.getOneValueFromSet(vN.getImplementedBy());
			if (n.toString().contains("smilax3"))
			{
				assertEquals("150.254.160.21",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address())).getHasValue());
				assertEquals("150.254.160.21",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasOutboundInterfaces()).getHasIPv4Address())).getHasValue());
				
			}
			else
			{
				System.out.println(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address());
				assertEquals("147.102.22.67",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address())).getHasValue());
				assertEquals("147.102.22.67",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasOutboundInterfaces()).getHasIPv4Address())).getHasValue());
			}
		}
		
		
		//TEST AGAIN IF THE NODES DOESN'T HAVE INTERFACES
		top = createTopologyForIpTestWithOutInterf();
		vN = (VirtualNode) IMUtil.getOneValueFromSet(top.getContains());
		assertNull(vN.getHasInboundInterfaces());
		assertNull(vN.getHasOutboundInterfaces());
		assertNull(vN.getImplementedBy().iterator().next().getHasInboundInterfaces());
		
		reserve.setPlanetLabIPs(top);
		vNodes = top.getContains();
		for (Resource r : vNodes)
		{
			vN = (VirtualNode) r;
	
			assertNull(vN.getHasInboundInterfaces());
			assertNull(vN.getHasOutboundInterfaces());
			
			Node n = IMUtil.getOneValueFromSet(vN.getImplementedBy());
			if (n.toString().contains("smilax3"))
			{
				assertEquals("150.254.160.21",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address())).getHasValue());
				assertEquals("150.254.160.21",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasOutboundInterfaces()).getHasIPv4Address())).getHasValue());
				
			}
			else
			{
				System.out.println(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address());
				assertEquals("147.102.22.67",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasInboundInterfaces()).getHasIPv4Address())).getHasValue());
				assertEquals("147.102.22.67",
						IMUtil.getOneValueFromSet(
								(IMUtil.getOneValueFromSet(n.getHasOutboundInterfaces()).getHasIPv4Address())).getHasValue());
			}
		}
		
		
	}
	
	
	protected static Topology createTopologyForIpTest() throws RepositoryException
	{
		Topology top = new TopologyImpl("boundTopology");
		//Platform plat = new PlatformImpl("PlanetLab");
		//virtual node 1
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		Interface in1 = new InterfaceImpl("sliver1-if0-in");
		Interface out1 = new InterfaceImpl("sliver1-if0-out");
		vNode1.setHasInboundInterfaces(IMUtil.createSetWithOneValue(in1));
		vNode1.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(out1));
		
		Node node1 = new NodeImpl("smilax3.novipl.man.poznan.pl");
		node1.setHostname("smilax3.novipl.man.poznan.pl");
		Interface n_in1 = new InterfaceImpl("node1-if0-in");
		Interface n_out1 = new InterfaceImpl("node1-if0-out");
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
		Interface n_in2 = new InterfaceImpl("node2-if0-in");
		Interface n_out2 = new InterfaceImpl("node2-if0-out");
		node2.setHasInboundInterfaces(IMUtil.createSetWithOneValue(n_in2));
		node2.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(n_out2));
		//store the node2
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(ManipulateDB.getTestbedContextURI());
		con.addObject(node2);
		ConnectionClass.closeAConnection(con);
		node2.setHostname(null);
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(node2));
		
		
		Set<Resource> nodes = new HashSet<Resource>();
		nodes.add(vNode1);
		nodes.add(vNode2);
		top.setContains(nodes);
		//plat.setContains(nodes);
		
		return top;
	}
	
	private Topology createTopologyForIpTestWithOutInterf() throws RepositoryException
	{
		Topology top = new TopologyImpl("boundTopology2");
		//Platform plat = new PlatformImpl("PlanetLab");
		//virtual node 1
		VirtualNode vNode1 = new VirtualNodeImpl("vnode1");
		
		
		Node node1 = new NodeImpl("smilax3.novipl.man.poznan.pl");
		node1.setHostname("smilax3.novipl.man.poznan.pl");
		
		vNode1.setImplementedBy(IMUtil.createSetWithOneValue(node1));
		
		//virtual node 1
		VirtualNode vNode2 = new VirtualNodeImpl("vnode2");
		
		
		Node node2 = new NodeImpl("novipl.planetlab2-novi.lab.netmode.ece.ntua.gr");
		node2.setHostname("novipl.planetlab2-novi.lab.netmode.ece.ntua.gr");
		
	
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(node2));
		
		
		Set<Resource> nodes = new HashSet<Resource>();
		nodes.add(vNode1);
		nodes.add(vNode2);
		top.setContains(nodes);
		//plat.setContains(nodes);
		
		return top;
	}
	
	
	

	
	/**
	 * it create a topology with a single virtual node for testing
	 * @return
	 * @throws RepositoryException 
	 */
	protected static Topology createTopology(String nodeID) throws RepositoryException
	{
		// Another example of creating a Node, and setting their Hardware Type and components
		Topology myTopology = new TopologyImpl("topology123");
		
		//Platform platf = new PlatformImpl("planetlab");
		
        VirtualNode myVNode = new VirtualNodeImpl("virtualNode12");
        
       // platf.setContains(IMUtil.createSetWithOneValue(myVNode));
        
        Node node = new NodeImpl(nodeID);
        

        //set hardware type
        myVNode.setHardwareType("i386");
        
        //////////add my node to topology ///////////

		myTopology.setContains(IMUtil.createSetWithOneValue(myVNode));
		
		
		/*//////////////////////////////////////////////////////////////////////
		YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
		NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
		RUNTIME
		*///////////////////////////////////////////////////////////////////////
		
		//////add node components to the node /////////
        Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
        ////////CPU//////////////////
        CPU myCPU = new CPUImpl("cpu1");
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(8));
        nodeComponents.add(myCPU);
        
        /////////Memory Component///////////
        Memory myMemory = new MemoryImpl("Memory1");
        myMemory.setHasMemorySize(100f);
        myMemory.setHasAvailableMemorySize(100f);
        nodeComponents.add(myMemory);
        

    	////////////END OF COMPONENTS/////////////
        myVNode.setHasComponent(nodeComponents);
        myVNode.setImplementedBy(IMUtil.createSetWithOneValue(node));
        
        
        
        return myTopology;
		
	}
	

}
