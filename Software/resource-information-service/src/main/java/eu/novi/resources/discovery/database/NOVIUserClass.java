package eu.novi.resources.discovery.database;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.Role;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;

/**
 *a class for storing, retrieving and manipulate the NOVI USers in the RIS DB
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class NOVIUserClass {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(NOVIUserClass.class);
	
	/**
	 * the URI context that is used for NOVI Users
	 */
	public static final URI USER_CONTEXT_URI = NoviUris.createNoviURI("novi_users");
	
	
	
	/**get the user object for the slice
	 * @param sliceURI the slice URI
	 * @return the NOVIUserImpl object or null
	 */
	public static NOVIUserImpl getNoviUserSlice(String sliceURI)
	{
		log.info("Getting the user for the slice {}",sliceURI);
		LocalDbCalls.printGetCurrentSlices();
		printCurrentUsers();
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection...");
			return null;
		}
		con.setReadContexts(NoviUris.createURI(sliceURI));

		//NOVIUser user = null;
		Set<String> userURIs = new HashSet<String>();


		try {
			log.debug("Executing the statement: null, rdf:type, policyIM:NOVIUser,  " + sliceURI);
			RepositoryResult<Statement> statements = con.
					getStatements(null,
							NoviUris.createRdfURI("type"),
							NoviUris.createPolicyURI("NOVIUser"), 
							true, NoviUris.createURI(sliceURI));

			while (statements.hasNext())
			{
				String st = statements.next().getSubject().toString();
				log.info("I found the user: {}", st);
				userURIs.add(st);

			}
			statements.close();

		} catch (RepositoryException e) {
			log.warn("Error while executing a statement : " +
					"Executing the statement: null, rdf:type, policyIM:NOVIUser,  " + sliceURI);
			ConnectionClass.logErrorStackToFile(e);
		}

		ConnectionClass.closeAConnection(con);
		
		if (userURIs.isEmpty())
		{
			log.warn("I did not find any user object for the slice {}", sliceURI);
			return null;
		}



		String us = userURIs.iterator().next();
		//get the user stored in the slice
		return getNoviUser(us);

	}
	
	
	/**get the NOVI user with specific context
	 * @param userURI the user URI
	 * @param contextURI the context URI
	 * @return the NOVIUserImpl object or null
	 */
	private static NOVIUserImpl getNoviUser(String userURI, String contextURI)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection...");
			return null;
		}
		con.setReadContexts(NoviUris.createURI(contextURI));

		NOVIUser user = null;
		try {
			log.info("I will retrieved the NOVI user : {}, with context: {}", userURI, contextURI);
			user = con.getObject(NOVIUser.class, userURI);

		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (ClassCastException e)
		{
			log.warn("Class cast exception, the user can not be retrieved");
		}

		NOVIUserImpl userImp = null;
		if (user == null)
		{
			log.warn("I failed to retreive the user  {} from the DB", userURI);


		} else
		{
			IMCopy copy = new IMCopy();
			log.info("I retreived the novi user {}", user.toString());
			userImp =  (NOVIUserImpl)copy.copy(user, -1);
		}
		ConnectionClass.closeAConnection(con);
		return userImp;

	}
	
	/**get the NOVI user, which is stored in the RIS DB. 
	 * @param userURI the user URI
	 * @return the NOVIUserImpl object or null
	 */
	public static NOVIUserImpl getNoviUser(String userURI)
	{
		return getNoviUser(userURI, USER_CONTEXT_URI.toString());
	}
	
	
	/**store the novi user object using a given context
	 * @param user the NOVIUser object to be stored
	 * @param context the context to store the user
	 * @return 0 if the store was OK, otherwise -1
	 */
	private static int storeNoviUser(NOVIUser user, URI context)
	{
		if (user == null)
		{
			log.warn("The given user object with the context {} is null", context);
			return -1;
		}
		log.info("I will store the NOVI user {}, with the context {}", user.toString(), context);
		/*ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection...");
			return ;
		}
		con.setAddContexts(context);*/
		//try {
			//IMCopy copy = new IMCopy();
			//NOVIUser userImpl = (NOVIUser)copy.copy(user, -1);
			/*NOVIUserImpl userImpl = createUserMan(user);
			log.info("I translated the user to the implemented classes, user URI: {}", userImpl.toString());
			con.addObject(userImpl.toString(), userImpl);
			con.commit();*/
			storeNoviUserAsTRiples(user, context.toString());
			LocalDbCalls.execPrintStatement(null, null, null, true, context);
			
			//check if the user was stored
			int sum  = LocalDbCalls.execStatementReturnSum(
					NoviUris.createURI(user.toString()),
					NoviUris.createRdfURI("type"),
					NoviUris.createPolicyURI("NOVIUser"), context);
			if (sum == 0)
			{
				log.warn("The novi user was not stored in the DB");
				return -1;
			}
			else
			{
				log.info("The NOVI user was stored successfuly in the RIS DB");
				return 0;
			}
		//} catch (RepositoryException e) {
		//	log.error("Error in storing the user object in the DB");
		//	ConnectionClass.logErrorStackToFile(e);
		//}
		//ConnectionClass.closeAConnection(con);
	}
	
	
	/**Store a NOVI User, it uses the default NOVI user context.
	 *  if the user exist, then it deletes the old information and it stores the new
	 * @param userObject the NOVI User object to be stored
	 * @return 0 if the store was OK, otherwise -1
	 */
	public static int storeNoviUser(NOVIUser userObject)
	{
		
		return storeNoviUser(userObject, USER_CONTEXT_URI);

	}
	
	/**Store a NOVI User, for a slice. If the user does not exist then it stored
	 * in the RIS DB and a reference is added to the slice information.
	 * If exist it just adds a reference to the slice information
	 * @param userObject the NOVI User object to be stored
	 * @param sliceURI the slice URI
	 */
	public static void storeNoviUserSlice(NOVIUser userObject, String sliceURI)
	{
		
		if (getNoviUser(userObject.toString()) == null)
		{
			log.info("The user {} is not stored in the NOVI DB, I will store it.",
					userObject.toString());
			storeNoviUser(userObject);
		}
		else
		{
			log.info("The user {} is already stored in the NOVI DB, I will not store it again.",
					userObject.toString());
			
		}
		
		log.info("I will add a reference for the user {} to the slice {}",
				userObject.toString(), sliceURI);
		LocalDbCalls.addStatement(userObject.toString(), "rdf:type", "pl:NOVIUser", sliceURI);
		
		

	}
	
	
	/**Store a novi user manually, storing one by one the triples.
	 * if the user exist, then it deletes the old information and it stores the new one
	 * @param user
	 * @param context
	 */
	private static void storeNoviUserAsTRiples(NOVIUser user, String context)
	{
		log.info("I will store the novi user {} manually as triples", user.toString());
		//check if it stored already
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setReadContexts(NoviUris.createURI(context));
		con.setRemoveContexts(NoviUris.createURI(context));

		NOVIUser dbUser = null;
		try {
			log.debug("I will check if the NOVI user : {}, with context: {} exist",
					user.toString(), context);
			dbUser = con.getObject(NOVIUser.class, user.toString());

		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (ClassCastException e)
		{
			log.debug("Class cast exception, the user {} can not be retrieved", user.toString());
		}
		if (dbUser != null)
		{
			log.info("The user {} already exist. I will update it", user.toString());
			//delete the stored user information
			
			dbUser.setHasSessionKey(null);
			dbUser.setPublicKeys(null);
			Role role = dbUser.getHasNoviRole();
			if (role != null)
			{
				dbUser.setHasNoviRole(null);
				try {
					con.removeDesignation(role, Role.class);
				} catch (RepositoryException e) {
					ConnectionClass.logErrorStackToFile(e);
				}
			}
			
		}
		ConnectionClass.closeAConnection(con);
		
		LocalDbCalls.addStatement(user.toString(), "rdf:type", "pl:NOVIUser", context);
		//SessionKey
		if (user.getHasSessionKey() != null)
		{
			log.info("Storing the session key {} for the user {}", user.getHasSessionKey(), user.toString());
			LocalDbCalls.addStatement(user.toString(), "pl:hasSessionKey", user.getHasSessionKey(), context);
		}
		else
		{
			log.warn("The user {} doesn't have session key", user.toString());
		}
		
		//PublicKeys
		if (user.getPublicKeys() != null && user.getPublicKeys().size() != 0)
		{
			log.info("Storing the public keys {} for the user {}", user.getPublicKeys().toString(),
					user.toString());
			for (String key : user.getPublicKeys())
			{
				LocalDbCalls.addStatement(user.toString(), "pl:publicKeys", key, context);
			}
		}
		else
		{
			log.warn("The user {} doesn't have public keys", user.toString());
		}
		
		//NOVI role
		if (user.getHasNoviRole() != null)
		{
			log.info("Storing the NOVI role {} for the user {}", user.getHasNoviRole(), user.toString());
			LocalDbCalls.addStatement(user.toString(), "pl:hasNoviRole",
					user.getHasNoviRole().toString(), context);
			LocalDbCalls.addStatement(user.getHasNoviRole().toString(), "rdf:type",
					"pl:Role", context);
			
		}
		else
		{
			log.warn("The user {} doesn't have NOVI role", user.toString());
			
		}
	}
	
	

	
	
	/**
	 * print all the NOVI User URIs that are in the DB
	 */
	public static void printCurrentUsers()
	{
		//LocalDbCalls.execPrintStatement(null, LocalDbCalls.createRdfURI("type"),
			//	LocalDbCalls.createNoviURI("Reservation"));
		ObjectConnection con = ConnectionClass.getNewConnection();
		log.info("The current users in the DB are:");
		try {
			RepositoryResult<Statement> statements = 
					con.getStatements(null, NoviUris.createRdfURI("type"),
							NoviUris.createPolicyURI("NOVIUser"), true);
			while (statements.hasNext())
			{
				Statement st = statements.next();
			     log.info(st.getSubject().toString() + ", context: {}", st.getContext());
			     
			     
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.warn("Error while executing a statement in printCurrentUsers");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
	}
	
	
	/**create the novi user manually
	 * @param user
	 * @return
	 */
	/*private static NOVIUserImpl createUserMan(NOVIUser user)
	{
		NOVIUserImpl userImpl = new NOVIUserImpl(user.toString());
		String st = user.getHasSessionKey();
		if (st == null)
		{
			log.warn("The session key is null");
		}
		else
		{
			log.info("The session key is : {}", st);
			userImpl.setHasSessionKey(st);
		}
		
		Set<String> pub = user.getPublicKeys();
		if (pub == null)
		{
			log.warn("The public keys are null");
		}
		else if (pub.size() == 0)
		{
			log.warn("The public keys list is empty");
		}
		else
		{
			log.info("The public keys are : {}", pub.toString());
			userImpl.setPublicKeys(pub);
		}
		
		String name = user.getFirstName();
		if (name == null)
		{
			log.warn("The first name is null");
		}
		else
		{
			log.info("The first name is : {}", name);
			userImpl.setFirstName(name);
		}
		
		
		return userImpl;
		
	}*/

}
