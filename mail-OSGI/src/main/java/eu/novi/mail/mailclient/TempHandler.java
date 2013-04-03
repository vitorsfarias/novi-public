package eu.novi.mail.mailclient;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/TempHandler")
public interface TempHandler {

		@POST
		@Path("/sentEmailEvent")
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
		@Produces(MediaType.TEXT_HTML)
		public int sentEmailEvent(@FormParam("receiver") String receiver,@FormParam("information") String information);
		
		
}
