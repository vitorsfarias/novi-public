package eu.novi.feedback;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.novi.feedback.event.Event;

public interface FeedbackHandler {

	/**
	 * Just a place holder, for testing purposes only.
	 * @param requestID
	 * @return
	 */
	//PP@GET
	//PP@Path("req/{requestID}")
	public Response getFeedbackByRequestID(
			@PathParam("requestID") String requestID);

	/**
	 * This function generates the HTML and javascript necessary to generate timeline 
	 * visualizing the user feedback.
	 * @param requestID
	 * @return
	 */
	//PP@GET
	//PP@Path("timeline/{requestID}/{topologyName}")
	//PP@Produces(MediaType.TEXT_HTML)
	public String seeTimeline(@PathParam("requestID") String requestID, @PathParam("topologyName") String topologyName);

	/**
	 * This function generates the HTML and javascript necessary to generate timeline 
	 * visualizing the user feedback. This is for case where topology Name is not given
	 * @param requestID
	 * @return
	 */
	//PP@GET
	//PP@Path("timeline/{requestID}")
	//PP@Produces(MediaType.TEXT_HTML)
	public String seeTimeline(@PathParam("requestID") String requestID) ;

	/**
	 * This is the data of current event related to @param requestID
	 * @param requestID
	 * @return
	 */
	//PP@GET
	//PP@Path("data/{requestID}")
	//PP@Produces(MediaType.APPLICATION_JSON)
	public String getSessionData(@PathParam("requestID") String requestID);

	/**
	 * Anticipating changes in where the data was requested from generated html
	 * @param requestID
	 * @return
	 */
	//PP@GET
	//PP@Path("timeline/data/{requestID}")
	//PP@Produces(MediaType.APPLICATION_JSON)
	public String getTimelineSessionData(@PathParam("requestID") String requestID);

	
	/**
	 * Returns list of recent error events
	 * @param requestID
	 * @return
	 */
	public List<Event> getErrorEvent(String requestID);
	
	/**
	 * Return list of recent informational events
	 * @param requestID
	 * @return
	 */
	public List<Event> getInstantEvent(String requestID);

	/**
	 * Remove list of events for current sessionID
	 * @param requestID
	 * @return
	 */
	public boolean removeEventListForSession(String sessionID);
	
	
}
