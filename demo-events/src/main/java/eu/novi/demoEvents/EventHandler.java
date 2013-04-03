package eu.novi.demoEvents;

import java.net.URISyntaxException;

import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/EventHandler")
public interface EventHandler {

	@POST
	@Path("/receiveEvent")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public int receiveEvent(@FormParam("deliver") String request);
		
	@GET
	@Path("/delete")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String deleteRequestHandler(@FormParam("deleteSlice") String sliceID) throws JMSException, URISyntaxException;
	
}
