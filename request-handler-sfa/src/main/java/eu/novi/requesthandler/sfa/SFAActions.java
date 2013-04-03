/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.clients.FedXMLRPCClient;
import eu.novi.requesthandler.sfa.clients.NoviplXMLRPCClient;
import eu.novi.requesthandler.sfa.clients.XMLRPCClient;
import eu.novi.requesthandler.sfa.exceptions.TestbedException;
import eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException;
import eu.novi.requesthandler.utils.RHUtils;

/**
 * This class in is charge of fulling all parameters needed for the SFA calls. There are 
 * methods for specific testbed, or some others that are generic.
 * The XMLRPC client should be set in advance.
 * 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class SFAActions {
	private XMLRPCClient client;
	private static String protogeni = "ProtoGENI";


	/**
	 * Get's the self credential of PlanetLab for the RH user (sfa_test)
	 * @throws TestbedException,XMLRPCClientException 
	 * @throws IOException 
	 */
	public String getSelfCredentialPL() throws TestbedException,XMLRPCClientException, IOException{
		List<Serializable> params = new ArrayList<Serializable>();

		params.add("-----BEGIN CERTIFICATE-----\n"+
				"MIIC8jCCAdoCCQDEz5TbZWEdDjANBgkqhkiG9w0BAQUFADA8MQswCQYDVQQGEwJH\n"+
				"UjEOMAwGA1UECAwFaTJjYXQxHTAbBgNVBAMMFG5vdmlwbC5ub3ZpLnNmYV90ZXN0\n"+
				"MB4XDTEyMDIyNjE2MzE0NloXDTE3MDIyNDE2MzE0NlowPDELMAkGA1UEBhMCR1Ix\n"+
				"DjAMBgNVBAgMBWkyY2F0MR0wGwYDVQQDDBRub3ZpcGwubm92aS5zZmFfdGVzdDCC\n"+
				"ASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAODwB7LsfVMzqyU8x39zR1Ri\n"+
				"8be5wfy6+hvppxzsp2pkzbr8Fe7ATyOFnklanH1hF51yOUF2xK8Au9AzGO507ZRr\n"+
				"CQVpBC0soumqJVO4TvtXuJ5lpUhvdHy7NKHRu3y4v5Uap/D+6QpJfm6t/OwfNcLy\n"+
				"fB0Hon1FuluLNklRxlKxy/KIJcD8dW4SZNrpdVrumdB5QM35OoJvNiQh1jcv4gmt\n"+
				"pEZLU+yqeceSzp7ADwMG/ZszkdNzJFpWU6sX/ML1AxjJ/n6EwXeOhYVRox81cmIx\n"+
				"5BIHB+K9aYfAzpVD1wxBzypydFDcFeSZAue7bclhu/wgxBcG4r4DwOKs6Rv2tk0C\n"+
				"ASMwDQYJKoZIhvcNAQEFBQADggEBAKCqybl0yyPnuMjj6NqT0XIHE9UgO4ZUOJ5d\n"+
				"YCzkWhFgVBHxmPEld89kZJ6h1oP9Beg9M5duBtYGlfYMbuOekrnTTrCLaC8RDZSN\n"+
				"BgVorfBN3RObZx6koF6DICO6FejkV+/dS9L/SgDUK9srmHD3q0Dl0cKQ1vT8B4ft\n"+
				"F1acoq8gc2NjBeok2yyU3RoouFIq73qW8oyssDH+VbDDJFT4beKY2UTEsfnuydXl\n"+
				"6YiVZ6xqQQ1LuqOdga3ehcPhRH3OyTByVZJcaT5ZUci8eCgBcsue4TzeTOVhJ627\n"+
				"ibA9KMTf9MVRK4ggIGBE7WZkDFzOGvS5g09PLEhpFg0/TgsIJ6Y=\n"+
		"-----END CERTIFICATE-----");

		params.add(SFAConstants.NOVI_PL_HRN_PREFIX + "sfa_test");
		params.add("user");

		if (client instanceof NoviplXMLRPCClient) {
			Object credentialObject = client.execXMLRPCRegistry("GetSelfCredential", params);
			if (credentialObject != null) {
				return credentialObject.toString();
			} else {
				throw(new TestbedException("PlanetLab getSelfCredential returned null"));
			}			
		} else {
			throw new XMLRPCClientException("You're trying to get PlanetLab Credentials without PlanetLab XMLRPC client.", new IllegalStateException().fillInStackTrace());
		}
	}


	/**
	 * Get's the selfCredential of FEDERICA for RH user (celia_velayos) 
	 * @throws TestbedException,XMLRPCClientException 
	 * @throws IOException 
	 */
	public String getSelfCredentialFed() throws TestbedException,XMLRPCClientException, IOException {
		List<Serializable> params = new ArrayList<Serializable>();

		params.add("-----BEGIN CERTIFICATE-----\n" +
				"MIIDBDCCAewCCQCB98ZJ1lwveTANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJH\n" +
				"UjESMBAGA1UECAwJQmFyY2Vsb25hMSIwIAYDVQQDDBlmaXJleHAubm92aS5jZWxp\n" +
				"YV92ZWxheW9zMB4XDTEyMDYyMDE0MTM1MVoXDTE3MDYxOTE0MTM1MVowRTELMAkG\n" +
				"A1UEBhMCR1IxEjAQBgNVBAgMCUJhcmNlbG9uYTEiMCAGA1UEAwwZZmlyZXhwLm5v\n" +
				"dmkuY2VsaWFfdmVsYXlvczCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEB\n" +
				"AK/PvWOxPqSXKfvfJdrJ5w+rdLzBKf0brLXboWkaLw47dupmjSezx1AdZKr0CxDr\n" +
				"XMc6z/V7ZcUF4fFk/tC0bJNjsTm9DCc0xU1NRgs+ItTwJZUAcjeebEHxzswK7Ye4\n" +
				"/KHXAdYnX9wefPqP92/lub8SMYy50WnSCtNSZKJ1KydiCsfHOSFQ4g+U5Pw5n1Vb\n" +
				"IrmbWtiJZ8xbwuN+dT8e0xjVAYb8Hm+0nubPkE6+a0pfn/7FK/feXOFsUHEky4Zn\n" +
				"LSfc9zyKe0pIgBVbk5a0fpINPhhAk3nELAj/7HwnpXs5js2bwibu8TBYYgLMfWQ8\n" +
				"Gw+ObPAZ4wIwfMOlydTvspECASMwDQYJKoZIhvcNAQEFBQADggEBADipoOs0fd23\n" +
				"rRWtfj28K0YT+AyiWPWYSHDKL7f+Z1ncgMZAkJHS668DKcHXaWOD59Fca0iVYGU0\n" +
				"BMCune7iS7PP5nEMkra8zF6PBaMnK7cG7NEdYri4xy3NhkbPWXBwzdS0OtCuIb4X\n" +
				"PxzYd39DSQbuZXfLKIuMX/ZbR8y0v0lxfdkrBEQDY451/6DOxDAXzPgMHiLSFje7\n" +
				"zSrjT5+LhbiUWa6u0K+R3mYriCMR19HLmmw6J+litJvUSd8kHC381Ha5cedpM4YS\n" +
				"xh2ygHTB/sy1fI45veblFk3upJVYTnn6O211aLThEfNUJ65XCufAAOJ4gwORjGQL\n" +
				"FHTZ6zEXiMk=\n" +
		"-----END CERTIFICATE-----");
		
		params.add(SFAConstants.NOVI_FED_HRN_PREFIX + "celia_velayos");
		params.add("user");

		if (client instanceof FedXMLRPCClient) {
			Object credentialObject = client.execXMLRPCRegistry("GetSelfCredential", params);
			if (credentialObject != null) {
				return credentialObject.toString();
			} else {
				throw(new TestbedException("FEDERICA getSelfCredential returned null"));
			}			
		} else {
			throw(new XMLRPCClientException("You're trying to get FEDERICA Credentials without FEDERICA XMLRPC client", new IllegalStateException().fillInStackTrace()));
		}
	}
	
	
	/**
	 * Gets the record from the registry.
	 * @param selfCredential
	 * @param hrnToResolve
	 * @return
	 * @throws TestbedException,XMLRPCClientException
	 * @throws XMLRPCClientException 
	 */
	public Object[] resolve(String selfCredential, String hrnToResolve) throws TestbedException, XMLRPCClientException {
		List<Serializable> paramsCredentials = new ArrayList<Serializable>();
		paramsCredentials.add(hrnToResolve);
		paramsCredentials.add(selfCredential);
		Object result = client.execXMLRPCRegistry("Resolve", paramsCredentials);
		return (Object[])result;
	}

	/**
	 * List resources of specific testbed (till both are federated via SFA)
	 * @param SFACredential of RH in testbed to list
	 * @param testbed which we want to list
	 * @return The result obtained from SFA: Map<String, Objec>
	 * @throws TestbedException 
	 * @throws XMLRPCClientException
	 */
	public Object listResources(String SFACredential) throws TestbedException, XMLRPCClientException {

		Object result = null;
		List<Serializable> params = new ArrayList<Serializable>();
		params.add(SFACredential);
		
		HashMap<String, Object> geniRspecVersion = createRSpecVersion();
		params.add(geniRspecVersion);

		result = client.execXMLRPCAggregate("ListResources", params);

		if (result != null) {
			return result;
		} else {
			throw(new TestbedException("List resources method returned null"));
		}	
	}

	/**
	 * Get's the credential of other SFA element (slice, user or authority)
	 * @param SFACredential the self-credential, needed for asking other credential
	 * @param elementHRN, HRN of the element which credential we are requesting
	 * @param elementType, type of this element (slice, user or authority)
	 * @param testbed - federica or novipl
	 * @return
	 * @throws TestbedException 
	 * @throws XMLRPCClientException 
	 */
	public String getCredential(String SFACredential, String elementHRN, String elementType) throws TestbedException, XMLRPCClientException {
		Object result = null;
		List<Serializable> paramsCredentials = new ArrayList<Serializable>();

		paramsCredentials.add(SFACredential);
		paramsCredentials.add(elementHRN);
		paramsCredentials.add(elementType);

		result = client.execXMLRPCRegistry("GetCredential", paramsCredentials);
		//		}
		if (result != null) {
			return result.toString();
		} else {
			throw(new TestbedException("Get credential method returned null for " + elementHRN));
		}

	}

	/**
	 * Add slice record to the PL registry. For doing this is needed the authority credentials.
	 * @param sliceHRN - slice HRN that is going to be created
	 * @param sliceName
	 * @param authorityHRN - authority HRN that own the slice
	 * @param credential - authority credential
	 * @return
	 * @throws TestbedException,XMLRPCClientException 
	 */
	public Object addRecordPL(String sliceName, String authorityHRN, String username, String credential) throws TestbedException,XMLRPCClientException {
		Object result = null;

		Map<String, Object> recordToAdd = new HashMap<String, Object>();
		recordToAdd.put("authority", SFAConstants.NOVI_PL_AUTHORITY);
		recordToAdd.put("description","NOVI slice");
		recordToAdd.put("expires", "2012-06-15T16:00:00Z");
		recordToAdd.put("hrn", SFAConstants.NOVI_PL_HRN_PREFIX + sliceName);
		recordToAdd.put("name", sliceName);
		recordToAdd.put("type", "slice");
		recordToAdd.put("url", "http://novi_"+sliceName);

		recordToAdd.put("researcher", SFAConstants.NOVI_PL_HRN_PREFIX + username);
		recordToAdd.put("PI", SFAConstants.NOVI_PL_HRN_PREFIX + "sfa_test");

		List<Serializable> params = new ArrayList<Serializable>();
		params.add((Serializable) recordToAdd);
		params.add(credential);

		if (client instanceof NoviplXMLRPCClient) {
			result = client.execXMLRPCRegistry("Register", params);
			if (result != null) {
				return result;
			} else {
				throw(new TestbedException("PlanetLab addRecord returned null for " + sliceName));
			}
		} else {
			throw(new XMLRPCClientException("You're trying to add Record to PlanetLab without PlanetLab XMLRPC client.", new IllegalStateException().fillInStackTrace()));
		}
	}

	/**
	 * Add slice record to the registry in Federica. For doing this is needed the authority credentials.
	 * @param sliceHRN - slice HRN that is going to be created
	 * @param sliceName
	 * @param authorityHRN - authority HRN that own the slice
	 * @param credential - authority credential
	 * @return
	 * @throws TestbedException,XMLRPCClientException 
	 */
	public Object addRecordFed(String sliceName, String authorityHRN, String credential) throws TestbedException,XMLRPCClientException {
		Object result = null;

		Map<String, Object> recordToAdd = new HashMap<String, Object>();
		recordToAdd.put("authority", SFAConstants.FEDERICA_AUTHORITY);
		recordToAdd.put("description","Testing SFA as PI");
		recordToAdd.put("expires", "2013-06-15T16:00:00Z");
		recordToAdd.put("hrn", SFAConstants.NOVI_FED_HRN_PREFIX +  sliceName);
		recordToAdd.put("name", sliceName);
		recordToAdd.put("type", "slice");
		recordToAdd.put("url", "http://novi_"+sliceName);

		List<Serializable> userList = new ArrayList<Serializable>();
		userList.add(SFAConstants.NOVI_FED_HRN_PREFIX +  "celia_velayos");
		recordToAdd.put("researcher", userList);

		List<Serializable> piList = new ArrayList<Serializable>();
		piList.add(SFAConstants.NOVI_FED_HRN_PREFIX + "celia_velayos");
		recordToAdd.put("PI", piList);

		List<Serializable> params = new ArrayList<Serializable>();
		params.add((Serializable) recordToAdd);
		params.add(credential);

		if (client instanceof FedXMLRPCClient) {
			result = client.execXMLRPCRegistry("Register", params);
			if (result != null) {
				return result;
			} else {
				throw(new TestbedException("FEDERICA addRecord returned null for " + sliceName));
			}
		} else {
			throw(new XMLRPCClientException("You're trying to add Record to FEDERICA without FEDERICA XMLRPC client.", new IllegalStateException().fillInStackTrace()));
		}
	}


	/**
	 * Remove all the resources from a slice
	 * @param sliceURN
	 * @param sliceCredentials
	 * @param testbed
	 * @return
	 * @throws TestbedException,XMLRPCClientException 
	 */
	public Object removeSliceResources(String sliceURN, String sliceCredentials) throws TestbedException,XMLRPCClientException {
		List<Serializable> params = new ArrayList<Serializable>();
		Object result = null;

		HashMap<String, Object> geniRspecVersion = createRSpecVersion();

		params.add(sliceURN);
		params.add(sliceCredentials);
		params.add(geniRspecVersion);

		result = client.execXMLRPCAggregate("DeleteSliver", params);

		if (result != null) {
			return result;
		} else {
			throw(new TestbedException("Remove slice resources method returned null for " + sliceURN));
		}
	}

	/**
	 * Remove slice from the registry
	 * @param sliceHRN
	 * @param authorityCredentials
	 * @param testbed
	 * @return
	 * @throws TestbedException,XMLRPCClientException 
	 */
	public Object removeSlice(String sliceHRN, String authorityCredentials) throws TestbedException,XMLRPCClientException {
		List<Serializable> params = new ArrayList<Serializable>();
		Object result = null;

		params.add(sliceHRN);
		params.add(authorityCredentials);
		params.add("slice");		

		result = client.execXMLRPCRegistry("Remove", params);

		if (result != null) {
			return result;
		} else {
			throw(new TestbedException("Remove record method returned null for " + sliceHRN));
		}
	}

	/**
	 * List the resources that pertain to a slice.
	 * @param sliceCredential
	 * @param sliceURN
	 * @param testbed
	 * @return
	 * @throws TestbedException,XMLRPCClientException 
	 */
	public Object listSliceResources(String sliceCredential, String sliceURN/*, String testbed*/) throws TestbedException,XMLRPCClientException{
		List<Serializable> params = new ArrayList<Serializable>();
		Object result = null;

		params.add(sliceCredential);

		HashMap<String, Object> options = createRSpecVersion();

		options.put("geni_slice_urn", sliceURN);
		params.add(options);

		result = client.execXMLRPCAggregate("ListResources", params);

		if (result != null) {
			return result;
		} else {
			throw(new TestbedException("List Resources method returned null for " + sliceURN));
		}

	}

	/**
	 * Populates the slice given by the sliceURN and the resources specified in reqRSpec. Needs the sliceCredentials for working.
	 * @param sliceURN
	 * @param sliceCred
	 * @param reqRSpec
	 * @return
	 * @throws TestbedException,XMLRPCClientException
	 */
	public Object populatePLSlice(String sliceURN, String sliceCred, NOVIUserImpl user, String reqRSpec) throws TestbedException,XMLRPCClientException {
		Object result;
		List<Serializable> paramsResources = new ArrayList<Serializable>();

		// 3rd, create slice resources:
		paramsResources.add(sliceURN);
		paramsResources.add(sliceCred);
		paramsResources.add(reqRSpec);

		Map<String, Object> userMap = createSFAUserFromNOVIUser(user, SFAConstants.NOVI_PL_HRN_PREFIX);
		List<Map<String, Object>> listUsers = new ArrayList<Map<String, Object>>();
		listUsers.add(userMap);
		paramsResources.add((Serializable) listUsers);

		Map<Object, Object> callId = new HashMap<Object, Object>();
		paramsResources.add((Serializable) callId);

		if (client instanceof NoviplXMLRPCClient) {
			result = client.execXMLRPCAggregate("CreateSliver", paramsResources);
			if (result != null) {
				return result;
			} else {
				throw(new TestbedException("PlanetLab Populate Slice method returned null for " + sliceURN));
			}
		} else {
			throw(new XMLRPCClientException("You're trying to populate a slice in PlanetLab without PlanetLab XMLRPC client.", new IllegalStateException().fillInStackTrace()));
		}
	}

	public Object populateFedSlice(String sliceURN, String sliceCred, NOVIUserImpl user, String reqRSpec) throws TestbedException,XMLRPCClientException {
		Object result;
		List<Serializable> paramsResources = new ArrayList<Serializable>();

		// create slice resources:
		paramsResources.add(sliceURN);
		paramsResources.add(sliceCred);
		paramsResources.add(reqRSpec);

		Map<String, Object> userMap = createSFAUserFromNOVIUser(user, SFAConstants.NOVI_FED_HRN_PREFIX);
		List<Serializable> listUsers = new ArrayList<Serializable>();

		listUsers.add((Serializable)userMap);

		paramsResources.add((Serializable) listUsers);
		Map<Object, Object> callId = new HashMap<Object, Object>();
		paramsResources.add((Serializable) callId);

		if (client instanceof FedXMLRPCClient) {
			result = client.execXMLRPCAggregate("CreateSliver", paramsResources);
			if (result != null) {
				return result;
			} else {
				throw(new TestbedException("FEDERICA Populate Slice method returned null for " + sliceURN));
			}
		} else {
			throw new XMLRPCClientException("You're trying to populate a slice in FEDERICA without FEDERICA XMLRPC client.", new IllegalStateException().fillInStackTrace());
		}
	}
	
	/**
	 * 
	 * @param cred
	 * @return Object: (Map<String, Object>)
	 * @throws TestbedException,XMLRPCClientException
	 */
	public Object listAllSlices(String cred) throws TestbedException,XMLRPCClientException {
		Object result;
		List<Serializable> params = new ArrayList<Serializable>();
		params.add(cred);

		HashMap<String, Object> geniRspecVersion = createRSpecVersion();
	
		params.add(geniRspecVersion);
	
		result = client.execXMLRPCAggregate("ListSlices", params);
		if (result != null) {
			return result;
		} else {
			throw(new TestbedException("SFA List Slices method returned null"));
		}
	}
	

	public void validatePopulateResponse(Map<String, Object> fedResultPopulate) throws TestbedException {
		if (isValidSFAResponse(fedResultPopulate)) {
			throw new TestbedException("RH - Error populating slice: " + fedResultPopulate.get(SFAConstants.OUTPUT));
		}
	}
	
	private HashMap<String, Object> createRSpecVersion() {
		HashMap<String, String> auth = new HashMap<String, String>();
		auth.put("type", protogeni);
		auth.put("version", "2");
		HashMap<String, Object> geniRspecVersion = new HashMap<String, Object>();
		geniRspecVersion.put("geni_rspec_version", auth);
		return geniRspecVersion;
	}


	private Map<String, Object> createSFAUserFromNOVIUser(NOVIUserImpl user, String hrnPrefix) {
		Map<String, Object> userMap = new HashMap<String, Object>();
		
		String userID = getSFAUserIDFromNOVIUser(user);
		String userHrn = hrnPrefix + userID;
		Set<String> userKeys = user.getPublicKeys();
		
		userMap.put("urn", userHrn);	
		userMap.put("keys", new ArrayList<String>(userKeys));
		userMap.put("email", RHUtils.getUserMailFromNOVIUser(user));
		userMap.put("person_id", userID);
		userMap.put("first_name", user.getFirstName());
		userMap.put("last_name", user.getLastName());

		return userMap;
	}


	public void setClient(XMLRPCClient client) {
		this.client = client;
	}

	/**
	 * Checks that the code returned by SFA Action is 0 or not, so we know if
	 * there was error or not during the SFA action.
	 * 
	 * @param response
	 *            returned by the SFA action
	 * @return true when code = 0; false otherwise
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static boolean isValidSFAResponse(Map<String, Object> response) throws TestbedException {
		Map<String, Object> mapCode = (Map<String, Object>) response.get(SFAConstants.CODE);

		if (mapCode == null || mapCode.isEmpty()) {
			throw new TestbedException("Testbed returned a not valid response.");
		} else if ((Integer) mapCode.get(SFAConstants.GENI_CODE) == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getSFAUserIDFromNOVIUser(NOVIUserImpl user) {
		String userMail = RHUtils.getUserMailFromNOVIUser(user);
		return userMail.split("@")[0].replaceAll("\\.", "_");
	}
	

}

