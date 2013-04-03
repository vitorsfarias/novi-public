package eu.novi.feedback.event;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
	//OK this should have been using enum, 
	public static final String INSTANT_EVENT = "INSTANT_EVENT";
	public static final String ERROR_EVENT = "ERROR_EVENT";
	public static final String START_EVENT = "START_EVENT";
	public static final String STOP_EVENT = "STOP_EVENT";
	
	//The session ID, unique identifier which will also be used for sliceID
	String sessionID="";
	
	// The component generating the event
	String requester="";
	
	// The information about the event
	String eventInfo="";	
	
	// Additional link if necessary
	String link="";
	
	// Event ID in case of starting/stopping event
	String eventID="";
	
	// Type of the event, one of the value above
	String eventType="";
	
	// Isn't these obvious
	Date startTime;
	Date stopTime;
	
	public Event(){}
	
	public Event(String sessionID, String requester, String eventInfo,
			String link, String eventID, String eventType, Date startTime,
			Date stopTime) {
		super();
		this.sessionID = sessionID;
		this.requester = requester;
		this.eventInfo = eventInfo;
		this.link = link;
		this.eventID = eventID;
		this.eventType = eventType;
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

	public String getJSONString(){
			String result="";
	
			//TODO: create different event for error, update with 
			result = createEventsJSONString(ISO8601FORMAT.format(startTime), ISO8601FORMAT.format(stopTime), 
										requester, link, eventInfo);
	
			return result;
	}

	//Note that these create event JSON String does not end with comma, so any user of this creating a list is responsible for it.
	public String createEventsJSONString(String start, String end, String title, String link, String description){
		String result="\n	{";
		result += "\n 	\"start\" : \""+start+"\",";
		result += "\n 	\"end\"   : \""+end+"\",";
		result += "\n 	\"title\"   : \""+title+"\",";
		result += "\n 	\"image\"   : \"https://chart.googleapis.com/chart?chst=d_fnote_title&chld=pinned_c|1|004400|l|"+title+"\",";
		result += "\n 	\"description\"   : \""+description+"\",";

		if(eventType.equals(ERROR_EVENT)){
			result += "\n	\"color\"	: \"red\",";
		}
		
		result += "\n 	\"link\"   : \""+link +"\"";
		result += "\n	}";
		return result;
	}
	
	public static SimpleDateFormat ISO8601FORMAT    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * @return the sessionID
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * @param sessionID the sessionID to set
	 */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * @return the requester
	 */
	public String getRequester() {
		return requester;
	}

	/**
	 * @param requester the requester to set
	 */
	public void setRequester(String requester) {
		this.requester = requester;
	}

	/**
	 * @return the eventInfo
	 */
	public String getEventInfo() {
		return eventInfo;
	}

	/**
	 * @param eventInfo the eventInfo to set
	 */
	public void setEventInfo(String eventInfo) {
		this.eventInfo = eventInfo;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the eventID
	 */
	public String getEventID() {
		return eventID;
	}

	/**
	 * @param eventID the eventID to set
	 */
	public void setEventID(String eventID) {
		this.eventID = eventID;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Date getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	@Override
	public boolean equals(Object other) {
		
		if(this == other) return true;
		
		if(!(other instanceof Event)) 	return false;
			
		Event o = (Event) other;
		
		if(o.getEventType() == null && eventType != null) return false;
		if(o.getSessionID() == null && sessionID != null) return false;
		if(o.getRequester() == null && requester != null) return false;
		if(o.getLink() == null && link != null) return false;
		if(o.getEventInfo() == null && eventInfo != null) return false;
		
		
		if(o.getEventType() != null && !o.getEventType().equals(eventType)) return false;
		if(o.getSessionID() != null && !o.getSessionID().equals(sessionID)) return false;
		if(o.getRequester() != null && !o.getRequester().equals(requester)) return false;
		if(o.getLink() != null && !o.getLink().equals(link)) return false;
		if(o.getEventInfo() != null && !o.getEventInfo().equals(eventInfo )) return false;
		
		return true;
			
	}
	
	@Override
	public int hashCode(){
		return getJSONString().hashCode();
	}
	

}
