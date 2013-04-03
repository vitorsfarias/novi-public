package eu.novi.resources.discovery;

import java.util.Date;
import java.util.Set;

import eu.novi.im.core.Node;
import eu.novi.im.policy.NOVIUser;


/**
 * The calls thar RIS provide to NOVI-API
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface NoviApiCalls {
	
	
	/**get the slice information in OWL format
	 * @param uri -- the URI of the slice
	 * @return an rdf/xml string containing the slice information.
	 * Or null if it is not found
	 */
	public String getSlice( String uri);
	

	/**delete a slice.
	 * First RIS try to delete the slice in the testebeds using request handler.
	 * if this fail, then RIS quit the operation and it doesn't delete the slice
	 * information in the RIS DB
	 * @param user the user that request the deletion
	 * @param sliceID  the ID of the slice to be deleted
	 * @param sessionID
	 * @return a feedback message
	 */
	public String deleteSlice(NOVIUser user, String sliceID, String sessionID);

	/**Store a NOVI User
	 * @param userObject the NOVI User object to be stored
	 * @return 0 if the store was OK, otherwise -1
	 */
	public int storeNoviUser(NOVIUser userObject);
	
	
	/**it renew the slice (update expiration date). It calls also policy to update the 
	 * slice to the testbeds
	 * @param user
	 * @param sliceURI
	 * @param date
	 * @return a message
	 */
	public String updateExpirationTime(NOVIUser user, String sliceURI, Date date);
	
	
	/**it checks if a slice is in the local NOVI DB
	 * @param sliceURI the slice URI in the format noviBaseAddress#slice_id
	 * @return true if the slice is in the DB otherwise false
	 */
	public Boolean checkSliceExist(String sliceURI);
	
	
	/**return all the physical nodes in the DB with the availability values, from monitoring
	 * @return a Set with the physical nodes and their availability
	 */
	public Set<Node> listResources();
	
	
	/**return all the physical nodes in the DB 
	 * @return a Set with the physical nodes 
	 */
	public Set<Node> listAllResources();
	
	/**return all the physical nodes for the given user with the availability values, from monitoring
	 * @param user if it is null, then it return all the nodes
	 * @return a Set with the physical nodes and their availability
	 */
	public Set<Node> listResources(NOVIUser user);
	
	
	
	/**execute the statements and return back the results. Can be null one and only one argument.
	 * It return back the result for the argument that was null (but the context can not be null).
	 * you can use the full URI or the abbreviation: im: for core IM, rdf: for RDF,
	 * unit: for unit IM and pl: for Policy IM
	 * @param subject i.e. you can give a specific URI or null
	 * @param predicate the predicate. For instance for type use: 
	 * rdf:type , for implementBy use: 
	 * im:implementedBy
	 * @param object the object, i.e. http://fp7-novi.eu/im.owl#Node or im:Node
	 * @param contexts if you don't want to specify context you can leave it empty (but NOT null). 
	 * To search only information for a specific slice, give the URI of the slice, to search for 
	 * testbed information give
	 * im:testebedSubstrateConexts, to search all the DB leave it empty
	 * @return the found URIs in a set. If an error occur, or some of the URIs you provided are invalid, 
	 * then it returns null. If nothing was found it returns an empty Set
	 */
	public Set<String> execStatementReturnResults(String subject,
			String predicate, String object,  String... contexts);
	
	
	/**execute the statements and get back the results in a single string. 
	 * Can be null as many arguments you want.
	 * you can use the full URI or the abbreviation: im: for core IM, rdf: for RDF,
	 * unit: for unit IM and pl: for Policy IM
	 * @param subject i.e. you can give a specific URI or null
	 * @param predicate the predicate. For instance for type use: 
	 * rdf:type , for implementBy use: 
	 * im:implementedBy
	 * @param object the object, i.e. http://fp7-novi.eu/im.owl#Node or im:Node
	 * @param context if you don't want to specify context you give null. 
	 * To search only information for a specific slice, give the URI of the slice, to search for 
	 * testbed information give
	 * im:testebedSubstrateConexts, to search all the DB give null
	 * @return it return a string that contain all the results as triples
	 */
	public String execStatementPrintResults(String subject,
			String predicate, String object,  String context);


}
