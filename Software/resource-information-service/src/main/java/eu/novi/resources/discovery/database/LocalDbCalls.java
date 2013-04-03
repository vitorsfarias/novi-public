package eu.novi.resources.discovery.database;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Resource;
import eu.novi.im.util.IMCopy;


/**
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
 * ******************************************************************************
 * 
 * A parent class of all the queries classes. 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class LocalDbCalls {
	

	

	private static final transient Logger log = 
			LoggerFactory.getLogger(LocalDbCalls.class);
	
	
	protected final static String PREFIXES =
			"PREFIX im:<http://fp7-novi.eu/im.owl#>\n "+
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n"+
			"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> \n";
	
	
	//////////////////////////////////////////////////////////
	////SIMPLE STATIC CALL TO THE REPOSITORY/////////////////
	//////////////////////////////////////////////////////////
	
	/**return a resource from the local DB;
	 * The recursive depth of the returned resource is 1.
	 * The context is the testbed context
	 * @param uri -- the URI of the resource
	 * @return the resource or null if it is not found. 
	 */
	public static Resource getLocalResource(final String uri)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Problem creating new connecion in the getLocalResource");
			return null;
		}
		try {
			//set the platfrom substrate context
			con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
			Resource resource= (Resource) con.getObject(uri);
			log.info("The Resource with URI: {}, was found in the db.", uri);
			
			IMCopy copier = new IMCopy();
			Resource memResource = (Resource) copier.copy(resource, 1);
			
			ConnectionClass.closeAConnection(con);
			return memResource;

		} catch (RepositoryException e) {
			log.error("Repository error in getLocalResource");
			ConnectionClass.logErrorStackToFile(e);
		}
		catch (ClassCastException e)
		{
			log.warn(e.getMessage());
			//ConnectionClass.logErrorStackToFile(e);
			
		}
		log.warn("The Resource with URI: {} was not found in the DB", uri);

		ConnectionClass.closeAConnection(con);
		return null;
		
	}
	
	
	/**check if a slice exist in the DB
	 * @param sliceURI
	 * @return
	 */
	public static Boolean checkSliceExist(String sliceURI)
	{
		if (execStatementReturnSum(NoviUris.createURI(sliceURI), null, null,
				NoviUris.createURI(sliceURI)) > 0)
		{
			log.info("The slice {} is in the DB", sliceURI);
			return true;
		}
		else
		{
			log.info("The slice {}, is not in the DB");
			return false;
		}
	}

	
	
	/**get all the types of the resource
	 * @param resourceURI the URI
	 * @return a Set containing all the types. ie. a CPU is also 
	 * a ComputerNode and a Resource
	 */
	public static Set<String> getResourceTypes(String resourceURI)
	{//TODO make Junit test
		ObjectConnection con = ConnectionClass.getNewConnection();
		Set<String> types = new HashSet<String>();
		log.debug("Finding the RDF types for {}", resourceURI);
		try {
			
			//log.debug("Executing the statement: " + subj + ", " + pred + ", " + obj);
			RepositoryResult<Statement> statements = con.
					getStatements(NoviUris.createURI(resourceURI), RDF.TYPE, null, true);
			
			while (statements.hasNext())
			{
				Statement st = statements.next();
				log.debug("The type is {}", st.getObject().toString());
				types.add(st.getObject().toString());
				
			         
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement : "
					+ NoviUris.createURI(resourceURI) + " RDF.TYPE, null");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return types;
		
	}
	
	

	/**print and return all the slices URI that are in the DB
	 * @return a Set containing all the slice URIs or an empty set
	 */
	public static Set<String> printGetCurrentSlices()
	{
		//LocalDbCalls.execPrintStatement(null, LocalDbCalls.createRdfURI("type"),
			//	LocalDbCalls.createNoviURI("Reservation"));
		ObjectConnection con = ConnectionClass.getNewConnection();
		Set<String> slices =  new HashSet<String>();
		log.info("The current slices in the DB are:");
		try {
			RepositoryResult<Statement> statements = 
					con.getStatements(null, NoviUris.createRdfURI("type"),
							NoviUris.createNoviURI("Reservation"), true);
			while (statements.hasNext())
			{
				Statement st = statements.next();
			    log.info(st.getSubject().toString() + ", context: {}", st.getContext());
			    slices.add(st.getSubject().toString());
			     
			     
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.warn("Error while executing a statement in printCurrentSlices");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return slices;
	}
	
	/**add a single statement to the DB, you can use the abbreviations im: rdf: and pl:
	 * the sub, pred and obj can not be null, the context can be null
	 * @param sub a URI
	 * @param pred a URI
	 * @param obj can be a URI or a value
	 * @param context can be null or URI
	 */
	protected static void addStatement(String sub, String pred, String obj, String context)
	{
		
		if (sub == null || obj == null || pred == null)
		{
			log.warn("I can not add the statement: {} , {} " + obj + ". All the argument should not be null",
					sub, pred);
			return ;
		}
		ObjectConnection con = ConnectionClass.getNewConnection();
		URI[] cont = null;
		if (context == null)
		{
			cont = new URI[0];
		}
		else
		{
			cont = new URI[1];
			cont[0] = NoviUris.createURI(NoviUris.checkAbbreviation(context));
		}
		Value object = null;
		if (NoviUris.checkAbbreviation(obj).startsWith("http://"))
		{
			//is URI
			object = NoviUris.createURI(NoviUris.checkAbbreviation(obj));
		}
		else 
		{
			//is a value
			object  = con.getValueFactory().createLiteral(NoviUris.checkAbbreviation(obj));
		}
		try {
			con.add(NoviUris.createURI(NoviUris.checkAbbreviation(sub)),
					NoviUris.createURI(NoviUris.checkAbbreviation(pred)),
					object, cont);
		} catch (RepositoryException e) {
			log.warn("An error occur when the statement {}, {}, " + obj + ", " + cont + " was storing",
					sub, pred);
			log.warn(e.getMessage());
		}
		ConnectionClass.closeAConnection(con);
		
	}
	
	/**get the hostname of a Node.
	 * The node should  have the testbed context
	 * @param nodeURI the node URI
	 * @return the hostname or null if it is not found
	 */
	public static String getNodeHostname(String nodeURI)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		String hstn = null;
		log.debug("Finding the hostname for {}", nodeURI);
		try {
			
			//log.debug("Executing the statement: " + subj + ", " + pred + ", " + obj);
			RepositoryResult<Statement> statements = con.
					getStatements(NoviUris.createURI(nodeURI), NoviUris.createNoviURI("hostname"), null, true,
							ManipulateDB.getTestbedContextURI());
			
			while (statements.hasNext())
			{
				Statement st = statements.next();
				log.debug("The hostname is {}", st.getObject().toString());
				hstn = st.getObject().toString();
				
			         
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement : "
					+ NoviUris.createURI(nodeURI) + " ," + NoviUris.createNoviURI("hostname") + " ,null");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return hstn;
		
	}
	

	/**
	 * use the getStatements to execute a statement
	 * and print the results.
	 * Can be null as many arguments you want.
	 * you can use the full URI or the abbreviation: im: for core IM, rdf: for RDF,
	 * unit: for unit IM and pl: for Policy IM
	 * @param subj - A string specifying the subject, or null for a wildcard.
	 * @param pred - A string specifying the predicate, or null for a wildcard.
	 * @param obj - A string specifying the object, or null for a wildcard.
	 * @param context the context. if you don't want to specify context you give null. 
	 * To search only information for a specific slice, give the URI of the slice, to search for 
	 * testbed information give
	 * im:testebedSubstrateConexts, to search all the DB give null
	 * @return it return a string that contain all the results as triples
	 */
	public static String execPrintStatement(String subj,
			String pred, String obj, String context)
	{
		String statement = subj + ", " + pred + ", " + obj + ", " + context;
		log.info("Executing the statement: {}", statement);
		String triples;
		if (context == null)
		{
			log.debug("The context is null");
			triples = execPrintStatement(NoviUris.createURI(NoviUris.checkAbbreviation(subj)),
					NoviUris.createURI(NoviUris.checkAbbreviation(pred)),
					NoviUris.createURI(NoviUris.checkAbbreviation(obj)), true);
		}
		else
		{
			log.debug("The context is {}", context);
			triples = execPrintStatement(NoviUris.createURI(NoviUris.checkAbbreviation(subj)),
					NoviUris.createURI(NoviUris.checkAbbreviation(pred)),
					NoviUris.createURI(NoviUris.checkAbbreviation(obj)), true,
					NoviUris.createURI(NoviUris.checkAbbreviation(context)));
		}
		
		return "#########The execution of the statement: " + statement + 
				", returned the following results: \n" + triples;
		
	}
	
	

	/**
	 * use the getStatements to execute a statement
	 * and print the results.
	 * @param subj - A Resource specifying the subject, or null for a wildcard.
	 * @param pred - A URI specifying the predicate, or null for a wildcard.
	 * @param obj - A Value specifying the object, or null for a wildcard.
	 * @param debug - if it is true then the print are debug level, otherwise info
	 * @param contexts the contexts
	 * @return it return a string that contain all the results as triples
	 */
	protected static String execPrintStatement(org.openrdf.model.Resource subj,
			URI pred, Value obj, boolean debug, URI... contexts)
	{

		ObjectConnection con = ConnectionClass.getNewConnection();
		String answer = "";

		try {
			if (debug)
			{
				log.debug("Executing the statement: " + subj + ", " + pred + ", " + obj);
			}
			else
			{
				log.info("Executing the statement: " + subj + ", " + pred + ", " + obj);
			}
			
			RepositoryResult<Statement> statements = 
					con.getStatements(subj, pred, obj, true, contexts);
			log.debug("read context :");
			for (URI u : contexts)
			{
				log.debug(u.stringValue());
				
			}
			while (statements.hasNext())
			{
				Statement st = statements.next();
				String line = st.getSubject() + ", "
						+ st.getPredicate() + ", " + st.getObject() + ", " + st.getContext() + "\n";
				answer += line;
				if (debug)
				{
				     log.debug(line);
				}
				else
				{
				     log.info(line);
				}

			     
			     
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement in execPrintStatement");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return answer;
	
	}
	
	
	
	
	/**execute the statements and return back the results. Can be null one and only one argument.
	 * It return back the result for the argument that was null (but the context can not be null).
	 * you can use the full URI or the abbreviation: im: for core IM, rdf: for RDF 
	 * , unit for the unit IM and pl: for Policy IM
	 * @param subject i.e. you can give a specific URI or null
	 * @param predicate the predicate. For instance for type use: 
	 * rdf:type , for implementBy use: 
	 * im:implementedBy
	 * @param object i.e. http://fp7-novi.eu/im.owl#Node or im:Node
	 * @param contexts if you don't want to specify context you can leave it empty (but NOT null). 
	 * To search only information for a specific slice, give the URI of the slice, to search for 
	 * testbed information give
	 * im:testebedSubstrateConexts, to search all the DB leave it empty
	 * @return the found URIs in a set. If an error occur, or some of the URIs you provided are invalid, 
	 * then it returns null. If nothing was found it return an empty Set
	 */
	public static Set<String> execStatementReturnRes(String subject,
			String predicate, String object,  String... contexts)
	{
		Set<String> results = null; //new HashSet<String>();

		int count = 0;
		int index = 0; //to know where is the null value
		//check how many null the user gave
		if (subject == null)
		{
			log.debug("The null value is the subject");
			count++;
			index = 0;
		}
		if (predicate == null)
		{
			log.debug("The null value is the predicate");
			count++;
			index = 1;
		}
		if (object == null)
		{
			log.debug("The null value is the object");
			count++;
			index = 2;
		}
		
		if (count != 1)
		{
			log.warn("execStatementReturnRes: you gave {} null values. You should give only 1", count);
			return results;
		}
		
		//create the URIs
		URI subjectURI = null;
		URI predicateURI = null;
		URI objectURI = null;
		
		if (subject != null)
		{
			subjectURI = NoviUris.createURI(NoviUris.checkAbbreviation(subject));
			if (subjectURI == null)
			{
				log.warn("The subject {} is invalid URI", subject);
				return results;
			}
		}
		
		if (predicate != null)
		{
			predicateURI = NoviUris.createURI(NoviUris.checkAbbreviation(predicate));
			if (predicateURI == null)
			{
				log.warn("The predicate {} is invalid URI", predicate);
				return results;
			}
		}
		
		if (object != null)
		{
			objectURI = NoviUris.createURI(NoviUris.checkAbbreviation(object));
			if (objectURI == null)
			{
				log.warn("The object {} is invalid URI", object);
				return results;
			}
		}
		
		URI[] contextsURI = new URI[contexts.length];
		for (int i = 0; i < contexts.length; i++)
		{
			URI uri = NoviUris.createURI(NoviUris.checkAbbreviation(contexts[i]));
			if (uri == null)
			{
				log.warn("The context URI {} is invalid", contexts[i]);
				return results;
			}
			contextsURI[i] = uri;
			
		}
		
		//execute the getStatements
		ObjectConnection con = ConnectionClass.getNewConnection();
		try {
			log.info("Executing the statement: " + subjectURI + ", " + predicateURI + ", " + objectURI);
			log.info("Contexts:");
			for (URI s : contextsURI)
			{
				log.info(s.stringValue());
				
			}
			
			RepositoryResult<Statement> statements = 
					con.getStatements(subjectURI, predicateURI, objectURI, true, contextsURI);
			
			results = new HashSet<String>();
			while (statements.hasNext())
			{
				Statement st = statements.next();
				
				if (index == 0)
				{
					log.debug("I found the subject: {}", st.getSubject());
					results.add(st.getSubject().stringValue());
				}
				else if (index == 1)
				{
					log.debug("I found the predicate: {}", st.getPredicate());
					results.add(st.getPredicate().stringValue());
				}
				else
				{
					log.debug("I found the object: {}", st.getObject());
					results.add(st.getObject().stringValue());
				}

			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement in execStatementReturnRes");
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return results;
	}
	
	
	/**it execute a statement and return the sum of results.
	 * It doesn't print the statements
	 * @param subj - A Resource specifying the subject, or null for a wildcard.
	 * @param pred - A URI specifying the predicate, or null for a wildcard.
	 * @param obj - A Value specifying the object, or null for a wildcard.
	 * @param contexts the contects
	 * @return the sum of founded statements
	 */
	protected static int execStatementReturnSum(org.openrdf.model.Resource subj,
			URI pred, Value obj, URI... contexts)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		int sum = 0;
		try {
			log.debug("Executing the statement: " + subj + ", " + pred + ", " + obj);
			RepositoryResult<Statement> statements = con.
					getStatements(subj, pred, obj, true, contexts);
			
			while (statements.hasNext())
			{
				sum++;
				statements.next();
				
			         
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement : "
					+ subj + ", " + pred + ", " + obj);
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		return sum;
		
	}


	

	
	
	/**
	 * It show all the contents of the DB
	 */
	protected static void showAllContentOfDB()
	{
		execPrintStatement(null, null, null, true);
	
	}


	
	//////ABOUT MEMORY REPOSITORY ////////////////////////
	

	/**
	 * show all the content of memory repository.
	 * 
	 */
	public final static void showAllContentOfMemoryRepos()
	{

		try {
			
			RepositoryResult<Statement> statements = ConnectionClass.getConnection2MemoryRepos().
					getStatements(null, null, null, true);
			while (statements.hasNext())
			{
				Statement st = statements.next();
			     log.debug(st.getSubject()+", "
				+ st.getPredicate()+", "+st.getObject());
			     
			     
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement in memory connetion");
			ConnectionClass.logErrorStackToFile(e);
		}
		
	}


	/**execute a getStatements in memory repository and print the results.
	 * @param subj - A Resource specifying the subject, or null for a wildcard.
	 * @param pred - A URI specifying the predicate, or null for a wildcard.
	 * @param obj - A Value specifying the object, or null for a wildcard.
	 */
	protected final static void execPrintStatementMemoryRepos(org.openrdf.model.Resource subj,
			URI pred, Value obj)
	{


		try {
			log.debug("Executing in memory repository the statement: " +
					"" + subj + ", " + pred + ", " + obj);
			RepositoryResult<Statement> statements = 
					ConnectionClass.getConnection2MemoryRepos().getStatements(subj, pred, obj, true);
			while (statements.hasNext())
			{
				Statement st = statements.next();
			     log.debug(st.getSubject()+", "
				+ st.getPredicate()+", "+st.getObject());
			     
			     
			}
			statements.close();
			
		} catch (RepositoryException e) {
			log.error("Error while executing a statement in memory repository");
			ConnectionClass.logErrorStackToFile(e);
		}
	
	}
	


	


	//////////////////////////////////////////////////////////
	////SIMPLE HELP CALLS FOR LIFETIMES AND CALANDER//////////
	//////////////////////////////////////////////////////////

	/**
	 * It check if a Lifetime is valid;
	 * That means that the current day is after or equal the startTime of
	 * lifetime, and is before the endTime
	 * of lifetime
	 * @param Lifetime -- the Lifetime to check 
	 * @return true if the lifetime is valid, otherwise false
	 */
	protected static final boolean checkIfLifetimeIsValid(Lifetime lifetime)
	{
		return checkIfLifetimeIsValid(lifetime, 0, 0, 0);

	}
	
	
	/**
	 * It check if a Lifetime is valid;
	 * That means that the given day (current day shifted by months days and minutes to the front)
	 * is after or equal the startTime of the
	 * lifetime, and is before the endTime of the lifetime
	 * @param Lifetime -- the Lifetime to check 
	 * @param month
	 * @param days
	 * @param minutes
	 * @return true if the lifetime is valid, otherwise false
	 */
	protected static final boolean checkIfLifetimeIsValid(Lifetime lifetime,
			int month, int days, int minutes)
	{

		XMLGregorianCalendar startTime = lifetime.getStartTime();
		XMLGregorianCalendar endTime = lifetime.getEndTime();
		XMLGregorianCalendar date = getDate(month, days, minutes);
		//XMLGregorianCalendar currentTime = currentDate();

		if(startTime == null || endTime == null)
		{
			log.warn("The start time or the end time of the lifetime is null." +
					"The lifetime will be consider not valid.\nLifetime: {}", lifetime.toString());
			return false;
		}
		if ((date.compare(startTime) == DatatypeConstants.GREATER || 
				date.compare(startTime) == DatatypeConstants.EQUAL) && 
				date.compare(endTime) == DatatypeConstants.LESSER)
		{
			return true;
		}

		return false;
	}
	
	/**
	 * Get the current time.
	 * @return the current time in GMT-0:00 zone
	 * in an XMLGregorianCalendar format
	 */
	public static final XMLGregorianCalendar currentDate()
	{
		XMLGregorianCalendar curDate = null;

		try {
			curDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(
							new GregorianCalendar(TimeZone.getTimeZone("GMT-0:00")));
		} catch (DatatypeConfigurationException e) {
			log.error("Error in creating the current date for XMLGregorianCalendar");
			e.printStackTrace();
		}
		return curDate;
	}


	/**it create the current day (GMT-0:00 zone) shifted by 'month' months and 'day' days
	 * @param month -- how many months the current day is shifted
	 * @param day -- how many days the current day is shifted
	 * @return the shifted day in XMLGregorianCalendar format
	 */
	public static final XMLGregorianCalendar getDate(int month, int day, int minutes)
	{
		XMLGregorianCalendar date = null;
		try {
			//new calendar with current time
			GregorianCalendar gregorianCal = 
					new GregorianCalendar(TimeZone.getTimeZone("GMT-0:00"));
			gregorianCal.add(Calendar.MONTH, month); //add or subtract months
			gregorianCal.add(Calendar.DAY_OF_MONTH, day); //add or subtract days
			gregorianCal.add(Calendar.MINUTE, minutes); //add or subtract minutes
			date = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCal);
		} catch (DatatypeConfigurationException e) {
			log.error("Error in creating the date for XMLGregorianCalendar");
			e.printStackTrace();
		}

		return date;
	}


	/**for 1st year DEMO;
	 * It check if a Lifetime is not valid and hence the resource that have the
	 * lifetime is not reserved.
	 * That means that the current day is after or equal the endTime of
	 * lifetime. OR the current day+one month is before the startTime
	 * of lifetime
	 * @param Lifetime -- the Lifetime to check 
	 * @return true if the lifetime is not valid (the resource is available),
	 * otherwise false
	 */
	protected static final boolean checkIfLifetimeIsNotValid(Lifetime lifetime)
	{

		XMLGregorianCalendar startTime = lifetime.getStartTime();
		XMLGregorianCalendar endTime = lifetime.getEndTime();
		XMLGregorianCalendar currentTime = currentDate();
		XMLGregorianCalendar oneMonthFromNow = getDate(1, 0, 0);

		if(startTime == null || endTime == null)
		{
			log.warn("The start time or the end time of the lifetime is null." +
					"The lifetime will be consider not valid");
			return true;
		}
		if ((currentTime.compare(endTime) == DatatypeConstants.GREATER ) || 
				(currentTime.compare(endTime) == DatatypeConstants.EQUAL) || 
				(oneMonthFromNow.compare(startTime) == DatatypeConstants.LESSER))
		{
			return true;

		}

		return false;
	}


	

	

}
