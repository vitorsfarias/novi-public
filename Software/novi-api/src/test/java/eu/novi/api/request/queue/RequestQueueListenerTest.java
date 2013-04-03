package eu.novi.api.request.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.novi.api.request.handler.RequestHandler;
import eu.novi.api.request.handler.impl.RequestHandlerImpl;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.im.core.Group;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.IRMInterface;
import eu.novi.mapping.RemoteIRM;
import eu.novi.resources.discovery.NoviApiCalls;

public class RequestQueueListenerTest {
	static final String VMBroker = "vm://localhost?broker.persistent=false";

	@BeforeClass
	public static void setup() {
		ReportEventFactory.setBrokerURL(VMBroker);
	}

	@Test
	public void testGetGroupMidTerm() throws IOException {
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		Collection<Group> groupsFromRequest = repositoryUtil
				.getIMObjectsFromString(requestTest, Group.class);
		assert (groupsFromRequest != null);
	}

	@Test
	public void testGetGroupBound() throws IOException {
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/SIMPLE_BOUND_CHRYSA_TEST.owl");
		Collection<Group> groupsFromRequest = repositoryUtil
				.getIMObjectsFromString(requestTest, Group.class);
		assertNotNull(groupsFromRequest);
	}

	@Test
	public final void testRequestQueueListenerInitialization()
			throws JMSException {
		RequestQueueListener rql = new RequestQueueListener();
		rql.setBrokerURL(VMBroker);
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.initialize();
		assertEquals(rql.getBrokerURL(), VMBroker);
	}

	@Test
	public final void testOnDeleteSlice() throws JMSException,
			InterruptedException {
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setBrokerURL(VMBroker);
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.initialize();


		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		
		rql.setResourceDiscoveryAPI(mockRIS);

		
		NOVIUser user = new NOVIUserImpl("testUser");
		for(int i=0;i<10;i++){
			rqp.pushDeleteRequest("MYSessionID"+i, "sliceID1", user);
		}
		Thread.sleep(1000);
		assertNotNull(rql);
	}

	@Test
	public final void testOnCreateSliceWithNoProcessor() throws JMSException,
			InterruptedException, IOException {
		String requestText = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		rql.setRepositoryUtil(new IMRepositoryUtilImpl());

		NOVIUser user = new NOVIUserImpl("testUser");
		TextMessage createSlice = rqp.createRequestMessage("SessionID"+System.currentTimeMillis(), "CreateSlice", requestText, user);

		IRMInterface mockIRMInterface = mock(IRMInterface.class);
		doNothing().when(mockIRMInterface).processGroups(anyCollection(), anyString(), any(NOVIUserImpl.class));
		
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		rql.setResourceDiscoveryAPI(mockRIS);
		
		rql.setResourceMappingAPI(mockIRMInterface);
		
		rql.onMessage(createSlice);
		
		assertNotNull(rql);
	}

	@Test
	public final void testOnCreateSlice() throws JMSException,
			InterruptedException, IOException {
		String requestText = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		RequestProcessor mockRequestProcessor = mock(RequestProcessor.class);
		rql.addRequestProcessor(mockRequestProcessor);

		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
	
		NOVIUser user = new NOVIUserImpl("testUser");
		TextMessage createSlice = rqp.createRequestMessage("SessionID"+System.currentTimeMillis(),
				"CreateSlice", requestText, user);
		
		IRMInterface mockIRMInterface = mock(IRMInterface.class);
		doNothing().when(mockIRMInterface).processGroups(anyCollection(), anyString(), any(NOVIUserImpl.class));
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		rql.setResourceDiscoveryAPI(mockRIS);
		
		rql.setResourceMappingAPI(mockIRMInterface);
		rql.handleCreateSlice(createSlice);

		assertNotNull(rql);
	}


	@Test
	public final void testOnUpdateMapping() throws JMSException,
			InterruptedException, IOException {
		String requestText = readFile("src/test/resources/MidtermWorkshopRequest.owl");
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		RequestProcessor mockRequestProcessor = mock(RequestProcessor.class);
		rql.addRequestProcessor(mockRequestProcessor);

		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
	
		NOVIUser user = new NOVIUserImpl("testUser");
		TextMessage updateMappingSlice = rqp.createRequestMessage("SessionID"+System.currentTimeMillis(),
				RequestHandler.UPDATE_MAPPING_SLICE_REQUEST, requestText, user);
		
		IRMInterface mockIRMInterface = mock(IRMInterface.class);
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		
		rql.setResourceDiscoveryAPI(mockRIS);
		rql.setResourceMappingAPI(mockIRMInterface);
		rql.handleUpdateMappingSlice(updateMappingSlice);

		assertNotNull(rql);

	}

	@Test
	public final void testOnUpdateFailingResources() throws JMSException,
			InterruptedException, IOException {
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		RequestProcessor mockRequestProcessor = mock(RequestProcessor.class);
		rql.addRequestProcessor(mockRequestProcessor);

		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
	
		NOVIUser user = new NOVIUserImpl("testUser");
		List<String> failingResources = new ArrayList<String>();
		for(int i=0;i<5;i++) {
			failingResources.add("resoruces"+i);
		}
		TextMessage updateFailingSlice = rqp.pushUpdateFailingRequest("sessionID", user, "sliceID", failingResources);
		
		RemoteIRM mockIRMInterface = mock(RemoteIRM.class);
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		rql.setResourceDiscoveryAPI(mockRIS);
		rql.setRemoteResourceMappingAPI(mockIRMInterface);
		rql.handleUpdateFailingSlice(updateFailingSlice);

		assertNotNull(rql);

	}
	
	@Test
	public final void testInvalidLinkCreateSlice() throws JMSException,
			InterruptedException, IOException {
		String requestText = readFile("src/test/resources/MidtermWorkshopRequest_bound_slice2_v8.owl");
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.setBrokerURL(VMBroker);
		rql.initialize();

		RequestProcessor mockRequestProcessor = mock(RequestProcessor.class);
		rql.addRequestProcessor(mockRequestProcessor);

		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
	
		NOVIUser user = new NOVIUserImpl("testUser");
		TextMessage createSlice = rqp.createRequestMessage("SessionID"+System.currentTimeMillis(),
				"CreateSlice", requestText, user);
		
		IRMInterface mockIRMInterface = mock(IRMInterface.class);
		doNothing().when(mockIRMInterface).processGroups(anyCollection(), anyString(), any(NOVIUserImpl.class));
		
		NoviApiCalls mockRIS = mock(NoviApiCalls.class);
		stub(mockRIS.deleteSlice(any(NOVIUser.class), anyString(), anyString())).toReturn("deleted");
		rql.setResourceDiscoveryAPI(mockRIS);
		
		rql.setResourceMappingAPI(mockIRMInterface);
		
		// slice creation should fail
		assertFalse(rql.handleCreateSlice(createSlice));
	}
	@Test
	public final void testOnInvalidRequest() throws JMSException,
			InterruptedException {
		RequestQueueProducer rqp = new RequestQueueProducer();
		rqp.setBrokerURL(VMBroker);
		rqp.initialize();

		RequestQueueListener rql = new RequestQueueListener();
		rql.setBrokerURL(VMBroker);
		rql.setRepositoryUtil(new IMRepositoryUtilImpl());
		rql.initialize();

		TextMessage invalidRequest = rqp.createRequestMessage(
				"currentSessionID", "InvalidRequest", "");
		rql.onMessage(invalidRequest);

		// Allow purging feedback queue messages
		Thread.sleep(500);
		assertNotNull(rql);
	}

	public String readFile(String path) throws IOException {
		File file = new File(path);
		StringBuffer result = new StringBuffer();

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String currentLine = reader.readLine();
		while (currentLine != null) {
			result.append(currentLine);
			currentLine = reader.readLine();
		}

		return result.toString();

	}
	
	
}
