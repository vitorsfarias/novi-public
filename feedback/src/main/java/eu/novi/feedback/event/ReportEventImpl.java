package eu.novi.feedback.event;

import java.util.UUID;

import javax.jms.JMSException;

import eu.novi.feedback.queue.FeedbackQueueProducer;

public class ReportEventImpl implements ReportEvent {
	
	// Initialized from blueprint
	FeedbackQueueProducer eventQueueProducer;
	
	String currentSessionID = "DEFAULT_SESSION";
	/**
	 * This interface should be used when user only wanted to inform an instantaneous event.
	 */
	@Override
	public void instantInfo(String sessionID, String componentName,
			String eventInformation, String link) {
			currentSessionID = sessionID;
			try {
				eventQueueProducer.reportInstantEvent(sessionID, componentName, eventInformation, link);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * This reporting method will indicate the start of an event, and this will return back the eventID which should be used 
	 * to stop again this event when it is finished.
	 */
	@Override
	public String startInfo(String sessionID, String componentName,
			String eventInformation, String link) {
			currentSessionID = sessionID;
		String eventID = UUID.randomUUID().toString();
		try {
			eventQueueProducer.reportStartEvent(sessionID, eventID, componentName, eventInformation, link);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return eventID;
	}

	/**
	 * This reporting method will stop the event being started earlier, given that the user 
	 * knows which one he wanted to stop.  Not sure if componentName, eventInformation and link parameter
	 * is still necessary. I think now let's agree that if there is no information update it should be let to null.
	 * @throws JMSException 
	 */
	@Override
	public void stopInfo(String sessionID, String eventID, String componentName,
			String eventInformation, String link) {
			currentSessionID = sessionID;
			try {
				eventQueueProducer.reportStopEvent(sessionID, eventID, componentName, eventInformation, link);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void errorEvent(String sessionID, String componentName,
			String eventError, String link) {
			currentSessionID = sessionID;
			try {
				eventQueueProducer.reportErrorEvent(sessionID, componentName, eventError, link);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * @return the eventQueue
	 */
	public FeedbackQueueProducer getEventQueueProducer() {
		return eventQueueProducer;
	}

	/**
	 * @param eventQueue the eventQueue to set
	 */
	public void setEventQueueProducer(FeedbackQueueProducer eventQueueProducer) {
		this.eventQueueProducer = eventQueueProducer;
	}

	@Override
	public String getCurrentSessionID() {
		// TODO Auto-generated method stub
		return currentSessionID;
	}

}
