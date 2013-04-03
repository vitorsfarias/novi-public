package eu.novi.feedback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.Event;

/**
 * This is the rest end point responsible for handling feedback.
 * @author wibisono
 *
 */

public class FeedbackHandlerImpl implements FeedbackHandler {
	
		private static final transient Logger log = 
			LoggerFactory.getLogger(FeedbackHandlerImpl.class);
	
		//User feedback data, which bridge between the message queue and the timeline.
		//Will be initialized from the blue print;
		UserFeedbackData userFeedbackData ;
		
		
		/* (non-Javadoc)
		 * @see eu.novi.feedback.FeedbackHandlerServiceInterface#getFeedbackByRequestID(java.lang.String)
		 */
		@Override
		//PP@GET
		//PP@Path("req/{requestID}")
		public Response getFeedbackByRequestID(@PathParam("requestID") String requestID){
			return Response.status(200)
					.entity("getFeedbackByRequestID is called, processing request : " + requestID).build();
		}
		
		@Override
		//PP@GET
		//PP@Path("timeline/{requestID}")
		//PP@Produces(MediaType.TEXT_HTML)
		public String seeTimeline(@PathParam("requestID") String requestID) {
			   
			return generateTimelineHTML(requestID, null, "../data/"+requestID);
		
		}
		/* (non-Javadoc)
		 * @see eu.novi.feedback.FeedbackHandlerServiceInterface#seeTimeline(java.lang.String)
		 */
		@Override
		//PP@GET
		//PP@Path("timeline/{requestID}/{topologyName}")
		//PP@Produces(MediaType.TEXT_HTML)
		public String seeTimeline(@PathParam("requestID") String requestID, @PathParam("topologyName") String topologyName) {
			   
			return generateTimelineHTML(requestID, topologyName, "../../data/"+requestID);
		
		}

		private String generateTimelineHTML(String requestID,
				String topologyName, String dataLocation) {
			String Header="\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"+
					"<html> <head> <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />"+
					"<script src=\"http://static.simile.mit.edu/timeline/api-2.3.0/timeline-api.js?bundle=true\" type=\"text/javascript\"></script>" +
					"</head>";
			String Body = "<body onload=\"onLoad();\" onresize=\"onResize();\">";
			String Style = "<style> #timeline-event-bubble-wiki {display:none;} img {   display: block;   margin-left: auto;   margin-right: auto; }h3{text-align:center; }</style><img src=\"http://www.fp7-novi.eu/images/stories/novi-img.png\">";
			String RequestHeader ="<h3>User Feedback on Request ID : "+requestID+ (topologyName == null? "":"  based on topology: " + topologyName )+" </h3>";
			
			String Footer =	"</body> </html>";
			String DivTimeline= "\n<div id=\"my-timeline\" style=\"height: 350px; border: 1px solid #aaa\"></div>";
			String StopLoading = "<a href=\"#\" onclick=\"updateData=function(){}\">Stop Loading</a>";
			String NoScript= "\n<noscript>This page uses Javascript to show you a Timeline. Please enable Javascript in your browser to see the full page. Thank you.</noscript>";
			
			String currentTime = getCurrentTimeString();
			String onLoad="\n<script>\nvar tl;\nvar theme = Timeline.ClassicTheme.create(); \ntheme.event.bubble.width = 250; var updateInterval=null; "+
					"\nfunction onLoad() {"+
					"\n	var eventSource = new Timeline.DefaultEventSource();"+
					"\n	var bandInfos = ["+
					"\n	Timeline.createBandInfo({"+
					"\n		eventSource:    eventSource,"+
					"\n		date:           \""+currentTime+"\","+
					"\n		width:          \"70%\", "+
					"\n		theme:          theme," +
					"\n		intervalUnit:   Timeline.DateTime.SECOND, "+
					"\n		intervalPixels: 20"+
					"}),"+
					"\n	Timeline.createBandInfo({"+
					"\n		eventSource:    eventSource,"+
					"\n		date:           \""+currentTime+"\","+
					"\n		width:          \"15%\", "+
					"\n		theme:          theme," +
					"\n		intervalUnit:   Timeline.DateTime.MINUTE, "+
					"\n		intervalPixels: 300"+
					"}),"+
					"\n	Timeline.createBandInfo({"+
					"\n		eventSource:    eventSource,"+
					"\n		date:           \""+currentTime+"\","+
					"\n		width:          \"15%\", "+
					"\n		theme:          theme," +
					"\n		intervalUnit:   Timeline.DateTime.HOUR, "+
					"\n		intervalPixels: 1200"+
					"})"+				
					"\n	];"+
					"\n	bandInfos[1].syncWith=0;"+
					"\n	bandInfos[2].syncWith=0;"+
					"\n	bandInfos[0].highlight=true;   "+
					"\n	bandInfos[1].highlight=true;   "+
					"\n	bandInfos[2].highlight=true;   "+
					"\n	tl = Timeline.create(document.getElementById(\"my-timeline\"), bandInfos);"+
					"\n if(updateInterval == null) updateInterval = self.setInterval(\"updateData()\", 5000);"+
					"\n	console.log('onload called');"+
					"\n	\n} " + 
					"\n function updateData() { " + 
					"\n 	var eventSource = tl.getBand(0).getEventSource(); "+
				    "\n 	tl.loadJSON(\""+dataLocation+"?\"+(new Date().getTime()), "+
				    "\n 		function(json, url) {eventSource.clear();"+
				    "\n								 eventSource.loadJSON(json,url);"+"" +
				    "\n								 tl.layout(); "+
				  "\n								 console.log('Updating'); }); "+
					"\n }" + 
					"\n</script>";
			
			String onResize ="\n<script>\nvar resizeTimerID = null; "+
					"\n	console.log('onResize'); function onResize() {"+
					"\n		if (resizeTimerID == null) {"+
					"\n  	  resizeTimerID = window.setTimeout(function() {"+
												"resizeTimerID = null;"+
												"tl.layout();"+
												"}, 800);"+
					"\n 	}"+
					"\n } \n</script>";
			
			
			String result = Header+Body+Style+RequestHeader+onLoad+onResize+DivTimeline+StopLoading+NoScript+Footer;
			return result;
		}
		
		/* (non-Javadoc)
		 * @see eu.novi.feedback.FeedbackHandlerServiceInterface#getSessionData(java.lang.String)
		 */
		@Override
		//PP@GET
		//PP@Path("data/{requestID}")
		//PP@Produces(MediaType.APPLICATION_JSON)
		public String getSessionData(@PathParam("requestID")  String requestID){
			String mainHeader ="\n{"+
					"\n	\"dateTimeFormat\": \"iso8601\","+
					"\n	\"wikiURL\": \"http://wiki.fp7-novi.eu/bin/view/WP2/\","+
					"\n	\"wikiSection\": \"NOVI User feedback\","+
					"\n";
		

			String eventHeader = "\n	\"events\" : [";
			String eventFooter = "\n]";
			String mainFooter ="\n}";
			String events = "";
			
			events = userFeedbackData.generateEventJSONForSessionID(requestID);
			
			
			return mainHeader + eventHeader + events + eventFooter + mainFooter;
			
		}
		
		@Override
		//PP@GET
		//PP@Path("timeline/data/{requestID}")
		//PP@Produces(MediaType.APPLICATION_JSON)
		public String getTimelineSessionData(@PathParam("requestID")  String requestID){
			String mainHeader ="\n{"+
					"\n	'dateTimeFormat': 'iso8601',"+
					"\n	'wikiURL': \"http://wiki.fp7-novi.eu/bin/view/WP2/\","+
					"\n	'wikiSection': \"NOVI User feedback\","+
					"\n";
		

			String eventHeader = "\n	'events' : [";
			String eventFooter = "\n]";
			String mainFooter ="\n}";
			String events = "";
			
			events = userFeedbackData.generateEventJSONForSessionID(requestID);
			
			
			return mainHeader + eventHeader + events + eventFooter + mainFooter;
			
		}


		/**
		 * Remove list of events for current sessionID
		 * @param requestID
		 * @return
		 */
		public boolean removeEventListForSession(String sessionID){
			return userFeedbackData.removeEventListForSession(sessionID);
		}
	
		
		public static SimpleDateFormat ISO8601FORMAT    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		private String getCurrentTimeString(){
			Calendar cal = Calendar.getInstance();
			return ISO8601FORMAT.format(cal.getTime());
		}

		/* (non-Javadoc)
		 */
		public UserFeedbackData getUserFeedbackData() {
			return userFeedbackData;
		}

		/* (non-Javadoc)
		 */
		public void setUserFeedbackData(UserFeedbackData userFeedbackData) {
			log.info("Setting up user feedback data");
			this.userFeedbackData = userFeedbackData;
		}

		@Override
		public List<Event> getErrorEvent(String requestID) {
			
			return userFeedbackData.getErrorEvents(requestID);
		}

		@Override
		public List<Event> getInstantEvent(String requestID) {
			// TODO Auto-generated method stub
			return userFeedbackData.getInstantEvents(requestID);
		}
}

