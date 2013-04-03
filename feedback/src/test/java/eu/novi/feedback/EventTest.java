/**
 * 
 */
package eu.novi.feedback;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import eu.novi.feedback.event.Event;

/**
 * @author wibisono
 * 
 */
public class EventTest {

	/**
	 * Test method for {@link eu.novi.feedback.event.Event#Event()}.
	 */
	@Test
	public final void testEventConstructor() {
		Event e = new Event();
		assert (e != null);
	}

	/**
	 * Test method for
	 * {@link eu.novi.feedback.event.Event#Event(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)}
	 * .
	 */
	@Test
	public final void testEventConstructorWithParams() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event e = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);

		// Nothing magical and unexpected should happen
		assertEquals(e.getSessionID(), sessionID);

	}

	/**
	 * Test method for {@link eu.novi.feedback.event.Event#getJSONString()}.
	 */
	@Test
	public final void testGetJSONString() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "ERROR_EVENT";
		Date startTime = new Date(), stopTime = new Date();
		Event e = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);

		assertNotNull(e.getJSONString());

	}

	/**
	 * Test method for
	 * {@link eu.novi.feedback.event.Event#createEventsJSONString(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testCreateEventsJSONString() {
		Event e = new Event();
		assertNotNull(e.createEventsJSONString("start", "end",
				"Something happened", "link", "description"));

	}

	/**
	 * Test method for {@link eu.novi.feedback.event.Event#set
	 * /getSessionID(java.lang.String)}.
	 */
	@Test
	public final void testSetSessionID() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event e = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);
		assertEquals(sessionID, e.getSessionID());
		e.setSessionID("newSessionID");
		assertEquals("newSessionID", e.getSessionID());

	}

	/**
	 * Test method for {@link eu.novi.feedback.event.Event#set
	 * /getRequester()}.
	 */
	@Test
	public final void testGetRequester() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event e = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);
		assertEquals(requester, e.getRequester());

		e.setRequester("newRequester");
		assertEquals("newRequester", e.getRequester());
	}

	/**
	 * Test method for {@link eu.novi.feedback.event.Event#get
	 * /setEventInfo()}.
	 */
	@Test
	public final void testGetEventInfo() {

		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();
		Event e = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);
		assertEquals(eventInfo, e.getEventInfo());
		e.setEventInfo("newEventInfo");
		assertEquals("newEventInfo", e.getEventInfo());

		e.setEventType("newEventType");
		assertEquals("newEventType", e.getEventType());

		e.setStartTime(stopTime);
		assertEquals(stopTime, e.getStartTime());

		e.setStopTime(startTime);
		assertEquals(startTime, e.getStopTime());

		e.setEventID("newEventID");
		assertEquals(e.getEventID(), "newEventID");
	}

	@Test
	public final void testExactlyEquals() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();

		Event a = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);

		// First equal because they are exactly the same.
		assertEquals(a, a);

		Event b = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);
		assertEquals(a, b);

	}

	@Test
	public final void testNotEqualWithSomethingNotEvent() {
		Event a = new Event();
		Object somethingElse = new String("");
		boolean insaneCheck = a.equals(somethingElse);
		assert (insaneCheck != true);
	}

	@Test
	public final void testEqualsObject() {
		Event a = new Event();

		// Different object test
		String something = "Something";

		assert (!a.equals(something));
	}

	@Test
	public final void testEqualsEvent() {
		String sessionID = "SessionID", requester = "requester", eventInfo = "eventInfo", link = "link", eventID = "eventID", eventType = "eventType";
		Date startTime = new Date(), stopTime = new Date();

		Event a = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);
		Event b = new Event(sessionID, requester, eventInfo, link, eventID,
				eventType, startTime, stopTime);

		// Different event test
		b.setEventType("differentType");
		
		boolean equalsAB = a.equals(b);
		assert(!equalsAB);
		
		// Cancel previous test.
		b.setEventType(a.getEventType());

		// Different session test
		b.setSessionID("differentSessionID");
		equalsAB = a.equals(b);
		assert(!equalsAB);
		
		// cancel previous test
		a.setSessionID(b.getSessionID());
		b.setRequester("newRequester");
		equalsAB = a.equals(b);
		assert(!equalsAB);
		
		a.setRequester(b.getRequester());
		a.setLink("newLink");
		equalsAB = a.equals(b);
		assert(!equalsAB);
		
		a.setLink(b.getLink());
		a.setEventInfo("newEventInfo");
		equalsAB = a.equals(b);
		assert(!equalsAB);
		
	}
}
