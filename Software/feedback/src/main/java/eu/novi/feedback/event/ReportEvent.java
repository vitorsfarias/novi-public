package eu.novi.feedback.event;

import javax.jms.JMSException;

public interface ReportEvent {

	/**
	 *  Instant information about an event happening now without any starting and stopping time
	 * @param sessionID  			This is the session ID or the slice ID that will be used to uniquely identify timeline
	 * @param componentName			This is the current component reporting an information about an event
	 * @param eventInformation		This is the current information being reported.
	 * @param link					This is an additional link information that can be provided optionally
	 * @throws JMSException 
	 */
	
	public void instantInfo(String sessionID, String componentName, String eventInformation, String link) ;
	
	/**
	 * This starting event can be called to indicate an event happening to be reported in user feedback, which is ongoing.
	 * @param sessionID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @throws JMSException 
	 * @return A unique identifier which is associated with the current starting event.  This identifier should be used to report the stopping of this event.
	 */
	public String startInfo(String sessionID, String componentName, String eventInformation, String link) ;
	
	
	/**
	 * Stopping event should be called based on a corresponding start event info, for the timeline to know which of these starting event is being stopped.
	 * @param sessionID
	 * @param startID
	 * @param componentName
	 * @param eventInformation
	 * @param link
	 * @throws JMSException
	 */
	public void stopInfo(String sessionID, String eventID, String componentName, String eventInformation, String link) ;
	
	/**
	 * Instant error information just in case something went wrong.
	 * @param sessionID
	 * @param componentName
	 * @param eventError
	 * @param link
	 * @throws JMSException
	 */
	public void errorEvent(String sessionID, String componentName, String eventError, String link) ;

	
	/**
	 * 
	 * OK, we consider no concurrent session running.
	 * If we want to allow concurrent session, we have to pass around session ID to every component
	 * We don't want to do that yet.
	 * @return currentSessionID
	 */
	public String getCurrentSessionID();
}
