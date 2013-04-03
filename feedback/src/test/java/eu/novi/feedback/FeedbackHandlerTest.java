package eu.novi.feedback;

import static org.junit.Assert.*;

import org.junit.Test;

public class FeedbackHandlerTest {

	@Test
	public final void testGetFeedbackByRequestID() {
		FeedbackHandlerImpl impl = new FeedbackHandlerImpl();
		assertNotNull(impl.getFeedbackByRequestID("randomRequest"));
	}

	@Test
	public final void testSeeTimeline() {
		FeedbackHandlerImpl impl = new FeedbackHandlerImpl();
		assertNotNull(impl.seeTimeline("randomRequest"));
	}

	@Test
	public final void testGetSessionData() {
		FeedbackHandlerImpl impl = new FeedbackHandlerImpl();
		impl.setUserFeedbackData(new UserFeedbackData());
		assertNotNull(impl.getSessionData("randomRequest"));
		
	}

	@Test
	public final void testGetSetUserFeedbackData() {
		FeedbackHandlerImpl impl = new FeedbackHandlerImpl();
		impl.setUserFeedbackData(new UserFeedbackData());
		assertNotNull(impl.getUserFeedbackData());
		
		
	}



}
