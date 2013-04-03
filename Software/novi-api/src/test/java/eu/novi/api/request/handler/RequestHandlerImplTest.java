package eu.novi.api.request.handler;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Null;

import edu.emory.mathcs.backport.java.util.TreeSet;
import eu.novi.api.request.handler.impl.RequestHandlerImpl;
import eu.novi.api.request.queue.RequestProcessor;
import eu.novi.api.request.queue.RequestQueueListener;
import eu.novi.api.request.queue.RequestQueueProducer;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.IRMInterface;
import eu.novi.policyAA.interfaces.InterfaceForAPI;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;
import eu.novi.resources.discovery.NoviApiCalls;

public class RequestHandlerImplTest {
	
	static final String VMBroker ="vm://localhost?broker.persistent=false"; 

	@BeforeClass
	public static void setup(){
		ReportEventFactory.setBrokerURL(VMBroker);
	}

	@Test
	public final void testGetSetRequestProducer() throws JMSException {
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
	
		rqp.initialize();
		rhi.setRequestProducer(rqp);
		
		assertNotNull(rhi.getRequestProducer());
	}

	
	@Test
	public final void testUnboundRequestHandlerNull() throws JMSException, URISyntaxException{
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		rhi.setRequestProducer(new RequestQueueProducer());
		
		assertEquals(RequestHandlerImpl.EMPTY_CREATE_REQUEST,rhi.unboundRequestHandler(null, null));
	}

	@Test
	public final void testUnboundRequestHandler() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();
		rhi.setRequestProducer(rqp);
		
		//Given
		final String TESTED_USER="testuser";
		final String PASSWD_USER="passwd";
		final String USER_SESSION_KEY="someKey";
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		NOVIUserImpl mockUser = new NOVIUserImpl(TESTED_USER);
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn(mockUser);
		rhi.setAuthenticationAPI(mockAuthentication);
	
		//When
		rhi.authenticateUser(TESTED_USER, PASSWD_USER);
		
		String request = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		
		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		RequestProcessor mockRequestProcessor = mock(RequestProcessor.class);
		rql.addRequestProcessor(mockRequestProcessor);
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
	
		IRMInterface mockIRMInterface = mock(IRMInterface.class);
		doNothing().when(mockIRMInterface).processGroups(anyCollection(), anyString(), any(NOVIUserImpl.class));
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		rql.setResourceDiscoveryAPI(mockRIS);
		rql.setResourceMappingAPI(mockIRMInterface);

		String redirectLocation = rhi.unboundRequestHandler(request, USER_SESSION_KEY);
		assert(redirectLocation.startsWith(RequestHandler.SYSTEM_INSTANCE_LOCATION));
	}
	
	@Test
	public final void testNamedRequestHandler() throws JMSException, URISyntaxException, IOException{
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		rhi.setRequestProducer(rqp);
		
		String request = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		assertNull(rhi.namedRequestHandler(request));
	}

	@Test
	public final void testDeleteRequestHandler() throws Exception{
		//Given
		final String TESTED_USER="testuser";
		final String PASSWD_USER="passwd";
		final String USER_SESSION_KEY="someKey";

		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestHandlerImpl rhi = new RequestHandlerImpl();
		rhi.setRequestProducer(rqp);
		
		RequestQueueListener rql = new RequestQueueListener();
		rql.setBrokerURL(VMBroker);
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.initialize();

		NOVIUser mockUser = new NOVIUserImpl(TESTED_USER);

		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		rql.setResourceDiscoveryAPI(mockRIS);
	
		
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn((NOVIUserImpl)mockUser);

		
		rhi.setAuthenticationAPI(mockAuthentication);
		rhi.setResourceDiscoveryAPI(mockRIS);
		
		//When
		rhi.authenticateUser(TESTED_USER, PASSWD_USER);
		
		String redirectLocation = rhi.deleteRequestHandler("1111", USER_SESSION_KEY);

		assert(redirectLocation.startsWith(RequestHandler.SYSTEM_INSTANCE_LOCATION));
		
	}
	@Test
	public final void testDeleteRequestHandlerNull() throws JMSException, URISyntaxException{
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();
		rhi.setRequestProducer(rqp);
		
		assertEquals(RequestHandlerImpl.EMPTY_DELETE_REQUEST,rhi.deleteRequestHandler(null, null));
		
	}
	
	
	@Test
	public final void shouldReturnOwlFile() throws JMSException, URISyntaxException, IOException{  
		//given
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = new RequestQueueProducer();
		
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();
		rhi.setRequestProducer(rqp);
		
		final String expectedOwlFileData = eu.novi.api.request.handler.helpers.FileReader.readFile("1link.owl");
		final String SLICE_URI="SLICE_URI";
		
		NoviApiCalls resourceDiscoveryAPI =mock(NoviApiCalls.class);
		when(resourceDiscoveryAPI.getSlice(SLICE_URI)).thenReturn(expectedOwlFileData);
		
		rhi.setResourceDiscoveryAPI(resourceDiscoveryAPI);
		
		//when
		String owlFileData= rhi.getOwlRequestHandler("");
		
		//then
		assertEquals(expectedOwlFileData, owlFileData);
	}
	
	@Test
	public final void testAuthenticateUser(){
		
		//Given
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		InterfaceForAPI authenticationAPI = mock(InterfaceForAPI.class);
		
		rhi.setAuthenticationAPI(authenticationAPI);
		
		// When
		String sessionKey = rhi.authenticateUser("nonexistent","user");
	
		//then
		assertEquals(RequestHandlerImpl.USER_DOES_NOT_EXIST, sessionKey);
		
	}
	
	@Test
	public final void testGetSlice() throws NoClassDefFoundError, IOException, JMSException, URISyntaxException{
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		final String expectedOwlFileData = eu.novi.api.request.handler.helpers.FileReader.readFile("1link.owl");
		
		//Given
		final String SLICE_URI="SLICE_URI";
		
		NoviApiCalls resourceDiscoveryAPI =mock(NoviApiCalls.class);
		when(resourceDiscoveryAPI.getSlice(SLICE_URI)).thenReturn(expectedOwlFileData);
		
		rhi.setResourceDiscoveryAPI(resourceDiscoveryAPI);
		
		// when
		String result = rhi.getSlice(SLICE_URI);
		
		//Then
		assertEquals(result, expectedOwlFileData);
		
	
	}
	
	@Test
	public void testSliceIDPart(){
		RequestHandlerImpl impl = new RequestHandlerImpl();
		String novipl = "novipl.novi.slice_12345";
		String result = impl.getSliceIDFromRequestHandler(novipl);
		assertEquals(result, "slice_12345");
	}
	
	@Test
	public void testSliceIDPart1(){
		RequestHandlerImpl impl = new RequestHandlerImpl();
		String novipl = "fireexp.novi.slice_12345";
		String result = impl.getSliceIDFromRequestHandler(novipl);
		assertEquals(result, "slice_12345");
	}
	
	@Test
	public void testEmptySessionStorage(){
		RequestHandlerImpl impl = new RequestHandlerImpl();
		assertEquals(RequestHandlerImpl.SESSION_MAP_EMPTY, impl.checkSession(""));
	}
	
	@Test
	public void testSessionKeyNotFound() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="testuser";
		final String PASSWD_USER="passwd";
		final String USER_SESSION_KEY="someKey";
		final String OTHER_SESSION_KEY="someOtherKey";
		
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		NOVIUserImpl mockUser = new NOVIUserImpl(TESTED_USER);
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn(mockUser);
		rhi.setAuthenticationAPI(mockAuthentication);
		
		rhi.authenticateUser(TESTED_USER, PASSWD_USER);
		
		assertEquals(RequestHandlerImpl.SESSION_KEY_NOT_FOUND, rhi.checkSession(OTHER_SESSION_KEY));
	}
	
	
	@Test
	public void testSessionKeyFound() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="testuser";
		final String PASSWD_USER="passwd";
		final String USER_SESSION_KEY="someKey";
		
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		NOVIUserImpl mockUser = new NOVIUserImpl(TESTED_USER);
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		mockUser.setFirstName("test");
		mockUser.setLastName("user");
		
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn(mockUser);
		rhi.setAuthenticationAPI(mockAuthentication);
		
		rhi.authenticateUser(TESTED_USER, PASSWD_USER);
		
		assertEquals(TESTED_USER, rhi.checkSession(USER_SESSION_KEY));
	}
	
	@Test
	public void testListSliceUser() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="testuser";
		final String SLICE1="slice_1";
		final String SLICE2="slice_2";
			
		
		NoviApiCalls mockRISWhereSliceAllExist = mock(NoviApiCalls.class);
		FederatedTestbed mockRequestHandler = mock(FederatedTestbed.class);
		RHListSlicesResponseImpl mockResponse = mock(RHListSlicesResponseImpl.class);
		
		ArrayList<String> slicesList = new ArrayList<String>();
		slicesList.add(SLICE1);
		slicesList.add(SLICE2);
		
		when(mockRISWhereSliceAllExist.checkSliceExist(anyString())).thenReturn(true);
		when(mockResponse.getSlices()).thenReturn(slicesList);
		when(mockRequestHandler.listUserSlices(TESTED_USER)).thenReturn(mockResponse);
		
		//when
		rhi.setRequestHandlerAPI(mockRequestHandler);
		rhi.setResourceDiscoveryAPI(mockRISWhereSliceAllExist);
		
		//We'll get a list separated slices
		assertEquals(SLICE1+","+SLICE2, rhi.listSliceByUser(TESTED_USER));
		
	}
	

	@Test
	public void testListSliceUserRIS() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="user@example.net1";
			
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		
		Set<String> slicesSet = new TreeSet();
		slicesSet.add("slice_1");
		slicesSet.add("slice_2");
		slicesSet.add("slice_3");
		
		
		Set<String> user1 = new HashSet<String>();
		user1.add("http://example.com#user@example.net1");
		
		Set<String> user2 = new HashSet<String>();
		user2.add("http://example.com#user@example.net2");
		
		when(mockRIS.execStatementReturnResults(anyString(), eq("rdf:type"), eq("im:Reservation"))).thenReturn(slicesSet);
		when(mockRIS.execStatementReturnResults(anyString(),  eq("rdf:type"), eq("pl:NOVIUser"), eq("slice_1"))).thenReturn(user1);
		when(mockRIS.execStatementReturnResults(anyString(),  eq("rdf:type"), eq("pl:NOVIUser"), eq("slice_2"))).thenReturn(user2);
		when(mockRIS.execStatementReturnResults(anyString(),  eq("rdf:type"), eq("pl:NOVIUser"), eq("slice_3"))).thenReturn(user1);
		
		//when
		rhi.setResourceDiscoveryAPI(mockRIS);
		
		//We'll get a list separated slices
		assertEquals("slice_3,slice_1", rhi.listSliceByUserRIS(TESTED_USER));
		
	}

	
	@Test // listing directly platform slices without checking if ris also have it.
	public void testListPlatformSliceUser() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="testuser";
		final String SLICE1="slice_1";
		final String SLICE2="slice_2";
			
		
		FederatedTestbed mockRequestHandler = mock(FederatedTestbed.class);
		RHListSlicesResponseImpl mockResponse = mock(RHListSlicesResponseImpl.class);
		
		ArrayList<String> slicesList = new ArrayList<String>();
		slicesList.add(SLICE1);
		slicesList.add(SLICE2);
		
		when(mockResponse.getSlices()).thenReturn(slicesList);
		when(mockRequestHandler.listUserSlices(TESTED_USER)).thenReturn(mockResponse);
		
		//when
		rhi.setRequestHandlerAPI(mockRequestHandler);
		
		//We'll get a list separated slices
		assertEquals(SLICE1+","+SLICE2, rhi.listPlatformSliceByUser(TESTED_USER));
		
	}
	
	@Test
	public void testListResourceUser() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();

		//Given
		final String TESTED_USER="testuser";
		
		FederatedTestbed mockRequestHandler = mock(FederatedTestbed.class);
		RHListResourcesResponseImpl mockResponse = mock(RHListResourcesResponseImpl.class);
		String platformString = "MyPlatform, which should be in OWL";
		
		when(mockResponse.getPlatformString()).thenReturn(platformString);
		when(mockRequestHandler.listResources(TESTED_USER)).thenReturn(mockResponse);
		
		//when
		rhi.setRequestHandlerAPI(mockRequestHandler);
	
		
		//We'll get the requested platform string
		assertEquals(platformString, rhi.listResourceByUser(TESTED_USER));
		
	}
	@Test
	public void testExecStatementRIS() throws Exception{
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		
		String[] results = {"Subject1", "Predicate2", "Object3"};
		Set<String> setResult = new HashSet<String>();
		setResult.addAll(Arrays.asList(results));
		
		NoviApiCalls risAPI = mock(NoviApiCalls.class);
		
		when(risAPI.execStatementReturnResults(anyString(), anyString(), anyString(), any(String [].class))).thenReturn(setResult);
		when(risAPI.execStatementReturnResults(anyString(), anyString(), anyString())).thenReturn(setResult);
		
		rhi.setResourceDiscoveryAPI(risAPI);
		
		String result = rhi.getStatement("SomeSubject", "SomePredicate", "SomeObject", "NoContext"); 
		assertNotNull(result);
		System.out.println(result);
		
	}


	
	@Test
	public final void testUpdateMappingSlice() throws Exception{
		//Given
		final String TESTED_USER="testuser";
		final String USER_SESSION_KEY ="SessionKey";
		final String PASSWD_USER ="Passwd";
		
		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = mock(RequestQueueProducer.class);
		rhi.setRequestProducer(rqp);
		
		NOVIUser mockUser = new NOVIUserImpl(TESTED_USER);
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn((NOVIUserImpl)mockUser);
		rhi.setAuthenticationAPI(mockAuthentication);
		
		//When
		rhi.authenticateUser(TESTED_USER, PASSWD_USER);
		assertNotNull(rhi.updateMappingSlice(USER_SESSION_KEY, "sliceID", "somerequest"));
	}

	@Test
	public final void testUpdateFailingSlice() throws Exception{
		//Given
		final String TESTED_USER="testuser";
		final String USER_SESSION_KEY ="SessionKey";
		final String PASSWD_USER ="Passwd";

		RequestHandlerImpl rhi = new RequestHandlerImpl();
		RequestQueueProducer rqp = mock(RequestQueueProducer.class);
		rhi.setRequestProducer(rqp);
		
		NOVIUser mockUser = new NOVIUserImpl(TESTED_USER);
		mockUser.setHasSessionKey(USER_SESSION_KEY);
		
		InterfaceForAPI mockAuthentication = mock(InterfaceForAPI.class);
		when(mockAuthentication.getAuth(TESTED_USER, PASSWD_USER)).thenReturn((NOVIUserImpl)mockUser);
		rhi.setAuthenticationAPI(mockAuthentication);
		
		
		List<String>failingResources = new ArrayList<String>();
		for(int i=0;i<5;i++){
			failingResources.add("resources"+i);
		}
		
		assertNotNull(rhi.updateFailingSlice(USER_SESSION_KEY, "SliceID", failingResources));
	}

	
	public String readFile(String path) throws IOException{
			File file = new File(path);
			StringBuffer result = new StringBuffer();
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String currentLine = reader.readLine();
			while(currentLine != null)
			{
					result.append(currentLine);
					currentLine = reader.readLine();
			}
			
			return result.toString();
	}
}
