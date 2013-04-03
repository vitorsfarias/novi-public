package eu.novi.api.request.handler.impl;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.api.request.handler.NoviApiFeedbackHandler;
import eu.novi.feedback.event.Event;
import eu.novi.feedback.FeedbackHandler;

public class NoviApiFeedbackHandlerImpl implements NoviApiFeedbackHandler{

	
	private FeedbackHandler feedbackHandler;
	
	@Override
	public Response getFeedbackByRequestID(String requestID) {
		return feedbackHandler.getFeedbackByRequestID(requestID);
	}

	@Override
	public String seeTimeline(String requestID, String topologyName) {
		return feedbackHandler.seeTimeline(requestID, topologyName);
	}

	@Override
	public String seeTimeline(String requestID) {
		return feedbackHandler.seeTimeline(requestID);
	}

	@Override
	public String getSessionData(String requestID) {
		return feedbackHandler.getSessionData(requestID);
	}

	@Override
	public String getTimelineSessionData(String requestID) {
		return feedbackHandler.getTimelineSessionData(requestID);
	}

	@Override
	public List<Event> getErrorEvent(String requestID) {
		return feedbackHandler.getErrorEvent(requestID);
	}

	@Override
	public List<Event> getInstantEvent(String requestID) {
		return feedbackHandler.getInstantEvent(requestID);
	}

	@Override
	public boolean removeEventListForSession(String sessionID) {
		return feedbackHandler.removeEventListForSession(sessionID);
	}


	public FeedbackHandler getFeedbackHandler() {
		return feedbackHandler;
	}

	public void setFeedbackHandler(FeedbackHandler feedbackHandler) {
		this.feedbackHandler = feedbackHandler;
	}

}
