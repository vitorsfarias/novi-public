package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.im.util.UrisUtil;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;

public class CheckSliceExpirationTest {
	static String sl0 ;
	static String sl1 ;
	static String sl2 ;
	static String sl3 ;
	static String sl4 ;
	static String sl5 ;
	static String sl6 ;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		sl0 = NoviUris.createNoviURI("slice0").toString();
		sl1 = NoviUris.createNoviURI("slice1").toString();
		sl2 = NoviUris.createNoviURI("slice2").toString();
		sl3 = NoviUris.createNoviURI("slice3").toString();
		sl4 = NoviUris.createNoviURI("slice4").toString();
		sl5 = NoviUris.createNoviURI("slice5").toString();
		sl6 = NoviUris.createNoviURI("sliceNoLif").toString();

		storeSlice(sl0, 0, -10);
		storeSlice(sl1, 0, 0);
		storeSlice(sl2, 0, 10);
		storeSlice(sl3, 4, 0);
		storeSlice(sl4, 2, 10);
		storeSlice(sl5, 2, 14*60);
		storeSlice(sl6);
		
		//mock policy
		PolicyServCommun policy = new PolicyServCommun();
		InterfaceForRIS policyService = mock(InterfaceForRIS.class);
		policy.setPolicyServiceCalls(policyService);
		when(policyService.InformExpirationHappened(any(NOVIUserImpl.class), any(String.class), any(Date.class))).
		thenReturn(0);
		when(policyService.InformExpirationTime(any(NOVIUserImpl.class), any(String.class), any(Date.class))).
		thenReturn(0);
		when(policyService.UpdateExpirationTime(any(NOVIUserImpl.class), any(String.class), any(Date.class))).
		thenReturn(0);
		when(policyService.AuthorizedForDeletion(any(String.class), any(NOVIUserImpl.class), any(Set.class))).
		thenReturn(true);
		
		//mock RH
		RHCreateDeleteSliceResponseImpl mockCreateSliceResp = 
				mock(RHCreateDeleteSliceResponseImpl.class);
		when(mockCreateSliceResp.hasError()).thenReturn(false);
		//when(mockCreateSliceResp.getSliceID()).thenReturn("sliceID1213");
		
		FederatedTestbed calls2TestbedFromRHMock = mock(FederatedTestbed.class);
		when(calls2TestbedFromRHMock.deleteSlice(any(String.class), any(String.class), any(Set.class),
				any(TopologyImpl.class))).thenReturn(mockCreateSliceResp);
		TestbedCommunication.assignCalls2TestbedFromRHStatic(calls2TestbedFromRHMock);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Test
	public void testChecks() {
		
		
		assertTrue(CheckSliceExpiration.checkIfSliceExpi(sl0, 0, 0));
		assertTrue(CheckSliceExpiration.checkIfSliceExpi(sl1, 0, 0));
		assertTrue(CheckSliceExpiration.checkIfSliceExpi(sl2, 0, 11));
		assertTrue(CheckSliceExpiration.checkIfSliceExpi(sl2, 0, 10));
		assertFalse(CheckSliceExpiration.checkIfSliceExpi(sl2, 0, 9));
		assertTrue(CheckSliceExpiration.checkIfSliceExpi(sl3, 4, 10));
		
		assertTrue(CheckSliceExpiration.checkSliceExpiWindows(sl3, 4, 1, 3, 0));
		assertTrue(CheckSliceExpiration.checkSliceExpiWindows(sl3, 4, 0, 3, 0));
		assertTrue(CheckSliceExpiration.checkSliceExpiWindows(sl3, 5, 0, 3, 0));
		assertFalse(CheckSliceExpiration.checkSliceExpiWindows(sl3, 3, 60, 3, 0));
		assertFalse(CheckSliceExpiration.checkSliceExpiWindows(sl3, 5, 0, 4, 1));
		assertFalse(CheckSliceExpiration.checkSliceExpiWindows(sl4, 3, 0, 2, (60*12)-15));
		assertTrue(CheckSliceExpiration.checkSliceExpiWindows(sl5, 3, 0, 2, (60*12)-15));
	}
	
	@Test
	public void testRunRenew() {
		
		
		List<Set<String>> answer = CheckSliceExpiration.checkSlices();
		
		assertEquals(3, answer.get(0).size());//expired
		assertEquals(1, answer.get(1).size());//need notify
		//expired
		assertTrue(answer.get(0).contains(sl0));
		assertTrue(answer.get(0).contains(sl1));
		assertTrue(answer.get(0).contains(sl6));
		//expired in 3 days
		assertTrue(answer.get(1).contains(sl5));
		
		//test renew
		CheckSliceExpiration.updateExpirationTime(new NOVIUserImpl("user"), sl5, 
				LocalDbCalls.getDate(0,10,0).toGregorianCalendar().getTime());
		LocalDbCalls.showAllContentOfDB();
		answer = CheckSliceExpiration.checkSlices();

		assertEquals(0, answer.get(0).size());//expired, was deleted before
		assertEquals(0, answer.get(1).size());//need notify

	}
	

	
	
	/**create and store a slice, 
	 * it store also a user and a platform for the slice
	 * @param uri uri of the slice
	 * @param days expiration in days
	 * @param minutes expiration in minutes
	 */
	protected static void storeSlice(String uri, int days, int minutes)
	{
		Reservation slice = new ReservationImpl(uri); 
		slice.setContains(IMUtil.createSetWithOneValue(new VirtualNodeImpl(uri + "-vnode")));
		Lifetime lifetime = new LifetimeImpl(
				NoviUris.getSliceLifetimeURI(uri).toString());
		lifetime.setStartTime(LocalDbCalls.getDate(-1, 0, 0));
		lifetime.setEndTime(LocalDbCalls.getDate(0, days, minutes));
		slice.setHasLifetimes(IMUtil.createSetWithOneValue(lifetime));

		//store the slice
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(NoviUris.createURI(uri));
		try {
			NOVIUser user = new NOVIUserImpl(UrisUtil.getURNfromURI(uri) + "_user");
			con.addObject(slice);
			con.addObject(new PlatformImpl("Planetlab"));
			con.addObject(user);
			NOVIUserClass.storeNoviUser(user);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionClass.closeAConnection(con);

	}
	
	
	/**store a slice with out lifetime
	 * @param uri
	 */
	protected static void storeSlice(String uri)
	{
		Reservation slice = new ReservationImpl(uri); 
		slice.setContains(IMUtil.createSetWithOneValue(new VirtualNodeImpl(uri + "-vnode")));
		
		//store the slice
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setAddContexts(NoviUris.createURI(uri));
		try {
			
			NOVIUser user = new NOVIUserImpl(UrisUtil.getURNfromURI(uri) + "_user");
			con.addObject(slice);
			con.addObject(new PlatformImpl("Planetlab"));
			con.addObject(user);
			NOVIUserClass.storeNoviUser(user);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionClass.closeAConnection(con);

	}

}
