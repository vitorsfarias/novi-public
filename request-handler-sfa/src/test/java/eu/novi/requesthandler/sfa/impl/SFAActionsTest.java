package eu.novi.requesthandler.sfa.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Test;

import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAActions;
import eu.novi.requesthandler.sfa.clients.FedXMLRPCClient;
import eu.novi.requesthandler.sfa.clients.NoviplXMLRPCClient;
import eu.novi.requesthandler.sfa.exceptions.TestbedException;
import eu.novi.requesthandler.utils.RHUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class SFAActionsTest {
	
	@Test
	public void shouldReturnStringGetSelfCredentialPL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn("planetLabSelfCredential");
			String cred = sfa.getSelfCredentialPL();
			assertEquals("planetLabSelfCredential", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorGetSelfCredentialPL() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn("planetLabSelfCredential");
			String cred = sfa.getSelfCredentialPL();			
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to get PlanetLab Credentials without PlanetLab XMLRPC client.", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorGetSelfCredentialPL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn(null);
			String cred = sfa.getSelfCredentialPL();			
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: PlanetLab getSelfCredential returned null", e.toString());
		}	
	}
	
	@Test
	public void shouldReturnStringGetSelfCredentialFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn("planetLabSelfCredential");
			String cred = sfa.getSelfCredentialFed();
			assertEquals("planetLabSelfCredential", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorGetSelfCredentialFed() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn("planetLabSelfCredential");
			String cred = sfa.getSelfCredentialFed();			
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to get FEDERICA Credentials without FEDERICA XMLRPC client", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorGetSelfCredentialFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetSelfCredential"), any(Vector.class)))
				.thenReturn(null);
			String cred = sfa.getSelfCredentialFed();			
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: FEDERICA getSelfCredential returned null", e.toString());
		}	
	}	

	@Test
	public void shouldReturnMapListResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			HashMap<String, Object> result = new HashMap();
			result.put("value", "Return OK!");
			when(c.execXMLRPCAggregate(eq("ListResources"), any(Vector.class)))
				.thenReturn(result);
			Object r = sfa.listResources("");	
			assertTrue(r instanceof HashMap);
			HashMap rMap = (HashMap)r;
			assertEquals("Return OK!", rMap.get("value"));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchNullListResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			Vector<Serializable> v = new Vector();
			when(c.execXMLRPCAggregate(eq("ListResources"), any(Vector.class)))
				.thenReturn(null);
			Object r = sfa.listResources("");	
		} catch (Exception e) {
			assertNotNull(e);
			String error = e.toString();
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: List resources method returned null", error);
		}	
	}
	
	@Test
	public void shouldReturnGetCredential() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetCredential"), any(Vector.class)))
				.thenReturn("Credential");
			String r = sfa.getCredential("", "test", "");	
			assertEquals("Credential", r);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchNullGetCredential() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("GetCredential"), any(Vector.class)))
				.thenReturn(null);
			Object r = sfa.getCredential("", "test", "");	
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals(e.toString(), "eu.novi.requesthandler.sfa.exceptions.TestbedException: Get credential method returned null for test");
		}	
	}
	
	@Test
	public void shouldReturnStringAddRecordPL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), any(Vector.class)))
				.thenReturn("Record Added");
			String cred = (String) sfa.addRecordPL("", "", "", "");
			assertEquals("Record Added", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorAddRecordPL() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), (Vector<Serializable>) anyObject()))
				.thenReturn("Record Added");
			sfa.addRecordPL("", "", "", "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to add Record to PlanetLab without PlanetLab XMLRPC client.", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorAddRecordPL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), (Vector<Serializable>) anyObject()))
				.thenReturn(null);
			String cred = (String) sfa.addRecordPL("test", "", "", "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: PlanetLab addRecord returned null for test", e.toString());
		}	
	}

	@Test
	public void shouldReturnStringAddRecordFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), any(Vector.class)))
				.thenReturn("RecordAdded");
			String cred = (String) sfa.addRecordFed("test", "", "");
			assertEquals("RecordAdded", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorAddRecordFed() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), any(Vector.class)))
				.thenReturn("planetLabSelfCredential");
			String cred = (String) sfa.addRecordFed("test", "", "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to add Record to FEDERICA without FEDERICA XMLRPC client.", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorAddRecordFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Register"), any(Vector.class)))
				.thenReturn(null);
			String cred = (String) sfa.addRecordFed("test", "", "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: FEDERICA addRecord returned null for test", e.toString());
		}	

	}	
	
	@Test
	public void shouldReturnRemoveSliceResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			HashMap<String, Object> result = new HashMap();
			result.put("value", "Return OK!");
			when(c.execXMLRPCAggregate(eq("DeleteSliver"), any(Vector.class)))
				.thenReturn(result);
			Object r = sfa.removeSliceResources("test", "");	
			assertTrue(r instanceof HashMap);
			HashMap rMap = (HashMap)r;
			assertEquals("Return OK!", rMap.get("value"));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchNullRemoveSliceResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCAggregate(eq("DeleteSliver"), any(Vector.class)))
				.thenReturn(null);
			Object r = sfa.removeSliceResources("test", "");	
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: Remove slice resources method returned null for test", e.toString());
		}	
	}
	
	@Test
	public void shouldReturnRemoveSlice() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Remove"), any(Vector.class)))
				.thenReturn("Removed");
			Object r = sfa.removeSlice("test", "");	
			assertEquals("Removed", r);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchNullRemoveSlice() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCRegistry(eq("Remove"), any(Vector.class)))
				.thenReturn(null);
			Object r = sfa.removeSlice("test", "");	
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: Remove record method returned null for test", e.toString());
		}	
	}

	@Test
	public void shouldReturnListSliceResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			HashMap<String, Object> result = new HashMap();
			result.put("value", "Return OK!");
			when(c.execXMLRPCAggregate(eq("ListResources"), any(Vector.class)))
				.thenReturn(result);
			Object r = sfa.listSliceResources("", "test");	
			assertTrue(r instanceof HashMap);
			HashMap rMap = (HashMap)r;
			assertEquals("Return OK!", rMap.get("value"));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchNullListSliceResources() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		try {
			when(c.execXMLRPCAggregate(eq("ListResources"), any(Vector.class)))
				.thenReturn(null);
			Object r = sfa.listSliceResources("", "test");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: List Resources method returned null for test", e.toString());
		}	
	}
	
	@Test
	public void shouldReturnStringPopulateSlicePL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");

		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn("created");
			String cred = (String) sfa.populatePLSlice("test", "", user, "");
			assertEquals("created", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorPopulateSlicePL() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn("created");
			String cred = (String) sfa.populatePLSlice("test", "", user, "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to populate a slice in PlanetLab without PlanetLab XMLRPC client.", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorPopulateSlicePL() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn(null);
			String cred = (String) sfa.populatePLSlice("test", "", user, "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: PlanetLab Populate Slice method returned null for test", e.toString());
		}	
	}
	
	@Test
	public void shouldReturnStringPopulateSliceFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");

		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn("created");
			String cred = (String) sfa.populateFedSlice("test", "", user, "");
			assertEquals("created", cred);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void shouldCatchClientErrorPopulateSliceFed() {
		NoviplXMLRPCClient c = mock(NoviplXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn("created");
			String cred = (String) sfa.populateFedSlice("test", "", user, "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: You're trying to populate a slice in FEDERICA without FEDERICA XMLRPC client.", e.toString());
		}	
	}

	@Test
	public void shouldCatchNullErrorPopulateSliceFed() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		user.setFirstName("Celia");
		user.setLastName("Velayos");
		
		try {
			when(c.execXMLRPCAggregate(eq("CreateSliver"), any(Vector.class)))
				.thenReturn(null);
			String cred = (String) sfa.populateFedSlice("test", "", user, "");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("eu.novi.requesthandler.sfa.exceptions.TestbedException: FEDERICA Populate Slice method returned null for test", e.toString());
		}	
	}
	
	@Test
	public void shouldCatchNullErrorListSlices() {
		FedXMLRPCClient c = mock(FedXMLRPCClient.class);
		SFAActions sfa = new SFAActions();
		sfa.setClient(c);
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");

		try {
			when(c.execXMLRPCAggregate(eq("ListSlices"), any(Vector.class)))
				.thenReturn(null);
			Object result = (String) sfa.listAllSlices("cred");
		} catch (Exception e) {
			assertNotNull(e);
			assertEquals("SFA List Slices method returned null", e.getMessage());
		}	
	}

	@Test
	public void testIsValidSFAResponseErrorInResponse() throws TestbedException {
		Map<String, Object> mockResult = new HashMap<String, Object>();
		Map<String, Object> mockMapCode = new HashMap<String, Object>();
		mockMapCode.put("geni_code", 1);
		mockResult.put("code", mockMapCode);
		mockResult.put("output", "We are mocking SFA error response");
		
		assertFalse(SFAActions.isValidSFAResponse(mockResult));
	}
	
	@Test
	public void testIsValidSFAResponseGoodResponse() throws TestbedException {
		Map<String, Object> mockResult = new HashMap<String, Object>();
		Map<String, Object> mockMapCode = new HashMap<String, Object>();
		mockMapCode.put("geni_code", 0);
		mockResult.put("code", mockMapCode);
		mockResult.put("output", "We are mocking SFA good response");
		
		assertTrue(SFAActions.isValidSFAResponse(mockResult));		
	}
	
	@Test
	public void testIsValidSFAResponseEmptyMapCode(){
		Map<String, Object> mockResult = new HashMap<String, Object>();
		Map<String, Object> mockMapCode = new HashMap<String, Object>();
		mockResult.put("code", mockMapCode);
		
		try {
			assertFalse(SFAActions.isValidSFAResponse(mockResult));
		} catch (TestbedException e) {
			assertEquals("Testbed returned a not valid response.", e.getMessage());
		}
	}
	
	@Test
	public void testIsValidSFAResponseNullMapCode(){
		Map<String, Object> mockResult = new HashMap<String, Object>();
		mockResult.put("code", null);
		
		try {
			assertFalse(SFAActions.isValidSFAResponse(mockResult));
		} catch (TestbedException e) {
			assertEquals("Testbed returned a not valid response.", e.getMessage());
		}
	}
	
	@Test
	public void testGetUserIDFromUser() {
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		String userID = SFAActions.getSFAUserIDFromNOVIUser(user);
		assertEquals("celia_velayos", userID);
	}
	
	@Test
	public void testGetUserIDFromUserWithoutDots() {
		NOVIUserImpl user = new NOVIUserImpl("celiavelayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		String userID = SFAActions.getSFAUserIDFromNOVIUser(user);
		assertEquals("celiavelayos", userID);
	}
}
