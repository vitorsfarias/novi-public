package eu.novi.api.request.handler;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * 
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 ******************************************************************************************** 
 * 
 * Monitoring service interface. These are the interfaces provided by NOVI-API which will be exposed to 
 * the clients outside NOVI service layer
 * @author <a href="mailto:lakis@inf.elte.hu">Sandor Laki</a>
 *
 ********************************************************************************************/

@Path("/monitoring")
public interface MonitoringService {


	/**
	 * 
	 * 
	 * @param request
	 * @param sessionKey
	 * @throws JMSException
	 * @throws URISyntaxException
	 * @return URL to the feedback component which will show progress of request handling.
	 **/
	@POST
	@Path("/echo")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String echo(@FormParam("testparam") String testparam)  throws JMSException, URISyntaxException ;
	

	@POST
	@Path("/sliceTasks")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String sliceTasks(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;
	
	@POST
	@Path("/addTask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String addTask(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;
	
	@POST
	@Path("/describeTaskData")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String describeTaskData(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;
	
	@POST
	@Path("/fetchTaskData")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String fetchTaskData(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/modifyTask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String modifyTask(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/removeTask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String removeTask(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/enableTask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String enableTask(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/disableTask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String disableTask(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/getTaskStatus")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String getTaskStatus(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/addAggregator")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String addAggregator(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/removeAggregator")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String removeAggregator(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/fetchAggregator")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String fetchAggregator(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/addCondition")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String addCondition(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/removeCondition")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String removeCondition(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	@POST
	@Path("/modifyCondition")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String modifyCondition(@FormParam("sliceid") String sliceID, @FormParam("query") String query, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;

	/**
	 * Authorization interface which would authenticate username and password, and return a token/session key to the client (GUI)
	 * TODO: Use the original auth from request/handler path...
	 * @param userName
	 * @param password
	 * @return
	 * @throws JMSException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("/auth")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String authenticateUser(@FormParam("username") String userName, @FormParam("password") String password) throws JMSException, URISyntaxException; 
		
	/*
	public static final String SYSTEM_INSTANCE_LOCATION 		= "http://localhost:8080";
	public static final String USER_DOES_NOT_EXIST 		 		= "User does not exist";
	public static final String USER_IS_NOT_AUTHENTICATED 		= "User is not authenticated";
	public static final String SESSION_KEY_NOT_FOUND 	 		= "Session key not found";
	public static final String SESSION_MAP_EMPTY 	 	 		= "Session map is empty";
	public static final String EMPTY_CREATE_REQUEST	 	 		= "Nothing is requested";
	public static final String EMPTY_DELETE_REQUEST	 	 		= "Nothing is deleted";
	public static final String EMPTY_UPDATE_REQUEST	 	 		= "Nothing is updated";
	public static final String CREATE_SLICE_REQUEST		 		= "CREATE_SLICE_REQUEST";
	public static final String UPDATE_MAPPING_SLICE_REQUEST		= "UPDATE_MAPPING_SLICE_REQUEST";
	public static final String UPDATE_FAILING_SLICE_REQUEST		= "UPDATE_FAILING_SLICE_REQUEST";
	public static final String DELETE_SLICE_REQUEST		 		= "DELETE_SLICE_REQUEST";
	*/
}
