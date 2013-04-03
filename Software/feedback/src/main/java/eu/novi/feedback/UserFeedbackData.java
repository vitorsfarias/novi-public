package eu.novi.feedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.Event;

/**
 * Probably this will be rewritten with better solution. The idea of this class
 * is the bridge between the user interface/rest endpoint/javascript timeline
 * and the activeMQ listener. The first one, retrieve appropriate JSON string
 * for a specific sessionID. The activeMQ listener updates the data for a
 * certain session ID.
 * 
 * How to make sure that the FeedbackHandler Rest endpoint and the activeMQ
 * listener are using the same UserFeedback ? It will be initialized in blue
 * print container, so we know both are using the same memory container.
 * 
 * In this class we will also deal with the starting and ending event.
 * 
 * Instantaneous events is straightforward, the JSON event string can easily be
 * generated, also the error. The start/stop event at the starting will only be
 * instantenous, and later on it will be updated/closed.
 * 
 * @author wibisono
 * 
 */
public class UserFeedbackData {
	private static final transient Logger log = 
			LoggerFactory.getLogger(UserFeedbackData.class);
	
	HashMap<String, ArrayList<Event>> sessionIDToEventListMap = new HashMap<String, ArrayList<Event>>();

	/* (non-Javadoc)
	 * @see eu.novi.feedback.display.UserFeedback#generateEventJSONForSessionID(java.lang.String)
	 */

	public String generateEventJSONForSessionID(String sessionID) {
		ArrayList<Event> sessionsEvent = getCurrentSessionsEventList(sessionID);
		StringBuffer result = new StringBuffer();
		Date current = new Date();
		for(Event e : sessionsEvent){
			// Crucial step to make the timeline more interesting
			// Don't show things in the future 
			if(current.before(e.getStartTime()))
				continue;
			// The JSON String should be separated with comma, so
			if(result.length() != 0) result.append(",");
			
			result.append(e.getJSONString());
		}
		// remove all new line and special characters.
		String jsonString = result.toString().replaceAll("\\s+", " ");
		return jsonString;
	}

	/* (non-Javadoc)
	 * @see eu.novi.feedback.display.UserFeedback#updateEventsForSession(java.lang.String, eu.novi.feedback.event.Event)
	 */

	public void updateEventsForSession(String sessionID, Event currentEvent) {
		ArrayList<Event> sessionsEvent = getCurrentSessionsEventList(sessionID);
		log.info("Updating events for session "+sessionID + " Event : " + currentEvent.getJSONString());
		// Only STOP event requires special handling. the rest we just add
		if(currentEvent.getEventType().equals(Event.STOP_EVENT)){
			// Need to find the corresponding start event, and update its stop time.
			for(Event e : sessionsEvent){
				if(e.getEventID().equals(currentEvent.getEventID())){
						e.setStopTime(currentEvent.getStopTime());
						break;
				}
			}
		}
		else // non stop event, we just add it to the list{
			// This is a hack to remove duplicated message.
			for(Event e : sessionsEvent){
				if(e.equals(currentEvent))
					return;
			}
			sessionsEvent.add(currentEvent);
		}

	/**
	 * Does what it said it does
	 * @param sessionID
	 * @return
	 */
	private ArrayList<Event> getCurrentSessionsEventList(String sessionID) {
		if (!sessionIDToEventListMap.containsKey(sessionID)) {
			sessionIDToEventListMap.put(sessionID, new ArrayList<Event>());
		}
		return sessionIDToEventListMap.get(sessionID);
	}

	public List<Event> getErrorEvents(String sessionID) {
		List<Event> result = getCurrentSessionsEventList(sessionID);
		List<Event> ret  = new ArrayList<Event>();
		
		for(Event e : result){
			if(e.getEventType().equals(Event.ERROR_EVENT))
				ret.add(e);
		}
		
		return ret;
	}

	public List<Event> getInstantEvents(String sessionID) {
		List<Event> result = getCurrentSessionsEventList(sessionID);
		List<Event> ret  = new ArrayList<Event>();
		
		for(Event e : result){
			if(e.getEventType().equals(Event.INSTANT_EVENT))
				ret.add(e);
		}
		
		return ret;	}
	
	/**
	 * Provide function that might be necessary for cleaning up event lists 
	 */
	public boolean removeEventListForSession(String sessionID){
		List<Event> associatedEvents = sessionIDToEventListMap.remove(sessionID);

		return associatedEvents != null;

	}	
		
}
