package eu.novi.feedback;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.novi.feedback.event.Event;

public class UserFeedbackDataTest {
	@Test
	public final void testGetErrorEventList() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		
		Date startTime = new Date(), stopTime = new Date();
		Event error1 = new Event(sessionID, requester, eventInfo+"1", link, eventID, Event.ERROR_EVENT, startTime, stopTime);
		Event error2 = new Event(sessionID, requester, eventInfo+"2", link, eventID, Event.ERROR_EVENT, startTime, stopTime);
		
		data.updateEventsForSession(sessionID, error1);
		data.updateEventsForSession(sessionID, error2);
		
		List<Event> errors = data.getErrorEvents(sessionID);
		assertEquals(errors.size() , 2);
	}
	
	@Test
	public final void testGetInstantEventList() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		
		Date startTime = new Date(), stopTime = new Date();
		Event instant1 = new Event(sessionID, requester, eventInfo+"1", link, eventID, Event.INSTANT_EVENT, startTime, stopTime);
		Event instant2 = new Event(sessionID, requester, eventInfo+"2", link, eventID, Event.INSTANT_EVENT, startTime, stopTime);
		
		data.updateEventsForSession(sessionID, instant1);
		data.updateEventsForSession(sessionID, instant2);
		
		List<Event> instants = data.getInstantEvents(sessionID);
		assertEquals(instants.size() , 2);
	}
	@Test
	public final void testGenerateEventJSONForSessionID() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, Event.START_EVENT, startTime, stopTime);
		Event nextEvent = new Event(sessionID, requester, "Something else", link, eventID, Event.STOP_EVENT, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
		data.updateEventsForSession("currentSession", nextEvent);
		
		data.generateEventJSONForSessionID("currentSession");
	}
	@Test
	public final void testGenerateDuplicateEventJSONForSessionID() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, Event.START_EVENT, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
		data.updateEventsForSession("currentSession", currentEvent);
			
		data.generateEventJSONForSessionID("currentSession");
	}

	@Test
	public final void testGenerateEventInTheFuture() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		long futureTime = System.currentTimeMillis()+20000l;
		Date startTime = new Date(futureTime), stopTime = new Date(futureTime);
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, eventType, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
		data.generateEventJSONForSessionID("currentSession");
	}
	
	@Test
	public final void testGenerateEventInThePast() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		long pastTime = System.currentTimeMillis()-20000l;
		Date startTime = new Date(pastTime), stopTime = new Date(pastTime);
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, eventType, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
		data.generateEventJSONForSessionID("currentSession");
	}
	
	
	
	@Test
	public final void testUpdateEventsForSession() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType="eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, eventType, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
	}
	
	@Test
	public final void testUpdateStopEventsForSession() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID",eventType=Event.STOP_EVENT;
		Date startTime = new Date(), stopTime = new Date();
		Event currentEvent = new Event(sessionID, requester, eventInfo, link, eventID, eventType, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", currentEvent);
	}

	@Test
	public final void testMatchingStartStopEvents() {
		UserFeedbackData data = new UserFeedbackData();
		String sessionID="SessionID", requester="requester", eventInfo="eventInfo",link="link",eventID="eventID";
		Date startTime = new Date(), stopTime = new Date(System.currentTimeMillis()+1000l);
		Event startEvent = new Event(sessionID, requester, eventInfo, link, eventID, Event.START_EVENT, startTime, stopTime);
		Event stopEvent = new Event(sessionID, requester, eventInfo, link, eventID, Event.STOP_EVENT, startTime, stopTime);
		
		data.updateEventsForSession("currentSession", startEvent);
		data.updateEventsForSession("currentSession", stopEvent);
		data.generateEventJSONForSessionID(sessionID);
	}

}
