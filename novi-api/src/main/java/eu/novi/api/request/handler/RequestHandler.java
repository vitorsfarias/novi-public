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
 * Request handler service interface. These are the interfaces provided by NOVI-API which will be exposed to 
 * the clients outside NOVI service layer
 * @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 *
 ********************************************************************************************/

@Path("/handler")
public interface RequestHandler {


	/**
	 * This interface process OLW file attached in the request for slice creation. Originally designed only for unbound.
	 * But currently determination whether request is bound or not would be done in IRM, so this is the main entry for request
	 * 
	 * @param request
	 * @param sessionKey
	 * @throws JMSException
	 * @throws URISyntaxException
	 * @return URL to the feedback component which will show progress of request handling.
	 **/
	@POST
	@Path("/unbound")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String unboundRequestHandler(@FormParam("request") String request, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException ;
	

	@POST
	@Path("/bound")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String boundRequestHandler(@FormParam("request") String request);
	
	@GET
	@Path("/named")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String namedRequestHandler(@FormParam("request") String request);

	
	@POST
	@Path("/delete")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String deleteRequestHandler(@FormParam("deleteSlice") String sliceID, @FormParam("sessionKey") String sessionKey)  throws JMSException, URISyntaxException;;
	

	/**
	 * Provides OWL file content for the given slice id.
	 * @param sliceId
	 * @return OWL file content
	 */
	@POST
	@Path("/getowl")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String getOwlRequestHandler(@FormParam("getOwlFile") String sliceID);
	
	@POST
	@Path("/getslice")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String getSlice(@FormParam("getSlice") String sliceID) throws JMSException, URISyntaxException;
	
	/**
	 * Authorization interface which would authenticate username and password, and return a token/session key to the client (GUI)
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
	
	
	/**
	 * List slice owned by current SFA user. 
	 * This will first call request handler, and then filtered, returned only slices which also exist in RIS.
	 * 
	 * @param user The SFA user, not the email address of a user/NOVI user
	 * @return The format of the list of slices as expected by Monitoring is comma separated value of slice IDs (without URI prefix)
	 * @throws JMSException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("/listSliceUser")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String listSliceByUser(@FormParam("user") String user) throws JMSException, URISyntaxException;
	

	
	
	@POST
	@Path("/listSliceUserRIS")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String listSliceByUserRIS(@FormParam("user") String user) ;
	

	/***
	 * Listing resource available to a certain user, who had authenticated himself and own a sessionKey.
	 * Assumes that the user had already authenticated himself
	 * @param sessionKey
	 * @return
	 */
	@POST
	@Path("/listAvailableResourceUser")
	public String listAvailableResourceUser(@FormParam("sessionKey") String sessionKey);


	/**
	 * List slice owned by current SFA user. 
	 * In this interface we will not filter the result with what is existed on RIS, we will just return whatever we got from platform.
	 * 
	 * 
	 * @param user The SFA user, not the mail user
	 * @return The format of the list of slices as expected by Monitoring is comma separated value of slice IDs (without URI prefix)
	 * @throws JMSException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("/listPlatformSliceUser")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String listPlatformSliceByUser(@FormParam("user") String user) throws JMSException, URISyntaxException;

	
	
	/**
	 * Calls request handler to obtain physical substrate. The calls will be performed on each platform. 
	 * @param user
	 * @return
	 * @throws JMSException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("/listResourceUser")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String listResourceByUser(@FormParam("user") String user) throws JMSException, URISyntaxException;
	
	@GET
	@Path("/session/{key}")
	public String checkSession(@PathParam("key") String key);
	
	/**
	 * Exposing get statement provided by RIS. Provide two out (Subject| Predicate | Object), and optionally Context.
	 * This get statement will return the comma separated value of the missing element from (Subject|Predicate|Object).
	 * i.e giving S|P will get comma separated values of O, giving S|O will get comma separated value of P
	 * @param Subject
	 * @param Predicate
	 * @param Object
	 * @param Context
	 * @return Comma separated values of missing element requested, 
	 */
	
	@GET
	@Path("/getstatement")
	public String getStatement(
			@DefaultValue("NoSubject") @QueryParam("Subject") String Subject, 
			@DefaultValue("NoPredicate") @QueryParam("Predicate") String Predicate, 
			@DefaultValue("NoObject") @QueryParam("Object") String Object,
			@DefaultValue("NoContext") @QueryParam("Context") String Context);
	
	
	/**
	 	Exposing RIS's new interface to execute the statements and print the results in the logs. 
	  * Accept multiple null arguments 
	  * you can use the full URI or the abbreviation: im: for core IM, rdf: for RDF,
	  * unit: for unit IM and pl: for Policy IM
	  * @param subject i.e. you can give a specific URI or null
	  * @param predicate the predicate. For instance for type use: 
	  * rdf:type , for implementBy use: 
	  * im:implementedBy
	  * @param object the object, i.e. http://fp7-novi.eu/im.owl#Node or im:Node
	  * @param context if you don't want to specify context you give null. 
	  * To search only information for a specific slice, give the URI of the slice, to search for 
	  * testbed information give im:testebedSubstrateConexts, to search all the DB give null
	  * 
	  * @return result of the query
	  * 
	  * */
	
	@GET
	@Path("/getprintstatement")
	public String getPrintStatement(
			@DefaultValue("NoSubject") @QueryParam("Subject") String Subject, 
			@DefaultValue("NoPredicate") @QueryParam("Predicate") String Predicate, 
			@DefaultValue("NoObject") @QueryParam("Object") String Object,
			@DefaultValue("NoContext") @QueryParam("Context") String Context);
	/**
	 * 
	 * An update slice interface for failing resources. Given list of failing resources id, and a slice ID, with current session
	 * this request will be handled by IRM. Normally this will be triggered by Policy Service directly within NOVI Service layer.
	 * 
	 * @param sessionKey for currently authenticated user. User should be authenticated, and should be authorized to update this slice
	 * @param sliceID of slice which will be updated
	 * @param failingResources collection of failing resources 
	 * @return a feedback URL where we can see how this update is being processed
	 */
	@POST
	@Path("/updateFailing")
	public String updateFailingSlice(@QueryParam("sessionKey") String sessionKey, @QueryParam("sliceID") String sliceID, @QueryParam("failingResources") List<String> failingResources) throws JMSException, URISyntaxException;


	/**
	 * An update slice interface for updating mapping of resources within NOVI Service layer.
	 * 
	 * @param sessionKey for currently authenticated user. 
	 * @param sliceID of slice which will be updated
	 * @param updatedRequest update slice request in OWL format
	 * @return
	 * @throws JMSException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("/updateMapping")
	public String updateMappingSlice(@QueryParam("sessionKey") String sessionKey, @QueryParam("sliceID") String sliceID, @FormParam("updatedSlice") String updatedRequest) throws JMSException, URISyntaxException;
	
	
	/** This interface renew the slice (update expiration date). It calls also policy to update the  slice to the testbeds
	 * @param user
	 * @param sliceURI
	 * @param date
	 * @return a message
	 */
	
	@POST
	@Path("/updateSliceExpiration")
	public String updateSliceExpiration(@QueryParam("sessionKey") String sessionKey, @QueryParam("sliceID") String sliceID, @QueryParam("date") String date);
	
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
	
}
