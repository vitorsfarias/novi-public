package eu.novi.resources.discovery.impl;





import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.resources.discovery.NoviApiCalls;

import eu.novi.im.core.Node;
import eu.novi.im.policy.NOVIUser;

import eu.novi.resources.discovery.database.CheckSliceExpiration;
import eu.novi.resources.discovery.database.DeleteSlice;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.NOVIUserClass;
import eu.novi.resources.discovery.database.NoviUris;
import eu.novi.resources.discovery.database.OwlCreator;
import eu.novi.resources.discovery.database.UpdateAvailability;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscovery;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;

public class NoviApiCallsImpl implements NoviApiCalls {

	private static final transient Logger log = 
			LoggerFactory.getLogger(NoviApiCallsImpl.class);



	/**
	 * Testbed name on which the service is running e.g. PlanetLab, FEDERICA.
	 */
	private String testbed; //get for service mix blueprint

	//private TestbedCommunication testbedCommun; //get for service mix blueprint

	private RemoteRisDiscovery remoteRis = new RemoteRisDiscoveryImpl(testbed);
	List<RemoteRisServe> remoteRISList; // get from blueprint/service mix

	private ReportEvent userFeedback;



	@Override
	public String getSlice(String uri){

		log.info("I will get the slice {}", uri);
		//LocalDbCalls.printGetCurrentSlices();
		//return owlCrea.getSliceInfoToString(uri);
		if (LocalDbCalls.checkSliceExist(uri))
		{
			log.info("The slice is stored locally");
			return OwlCreator.getSliceManifestInfoToString(uri);
		}
		else
		{
			log.info("I can not find the slice {}. I will contact remote platform", uri);
			return remoteRis.getRemoteSliceOwl(uri);

		}

	}


	@Override
	public String updateExpirationTime(NOVIUser user, String sliceURI, Date date)
	{
		return CheckSliceExpiration.updateExpirationTime(user, sliceURI, date);
	}

	
	
	@Override
	public String deleteSlice(NOVIUser user, String sliceID, String sessionID) {
		//this method is called in PlanetLab and PL next call FEDERICA site

		log.info("I got a delete slice call for {} by the user {}", sliceID, user.toString());
		userFeedback.instantInfo(sessionID, "RIS", 
				"Accepting delete slice request for "+sliceID, "http://fp7-novi.eu");
		String st = null;
		String sliceUri = NoviUris.createSliceURI(sliceID);
		//check platform
		if (LocalDbCalls.checkSliceExist(sliceUri))
		{//delete the slice
			log.info("The slice is in {} site, I will delete it", testbed);

			boolean ans = DeleteSlice.deleteSlice(sliceUri, user, userFeedback, sessionID);
			if (ans)
			{
				return "The slice was deleted succesfully";
			}
			else
			{
				return "The slice deletion was failed";
			}
			
		}
		else
		{//This ris doesn't contain the slice info
			
			st = "The slice is not in the " + testbed + " site. I will call remote RIS";
			log.info(st);
			userFeedback.instantInfo(sessionID, "RIS", 
					st, "http://fp7-novi.eu");
			boolean ans = remoteRis.deleteRemoteSlice(sliceUri, user, userFeedback, sessionID);
			if (ans)
			{
				st = "The slice was deleted succesfully remotetly";
				log.info(st);
				userFeedback.instantInfo(sessionID, "RIS", 
						st, "http://fp7-novi.eu");
				return st;
			}
			else
			{
				st = "The slice deletion was failed";;
				log.warn(st);
				userFeedback.errorEvent(sessionID, "RIS", 
						st, "http://fp7-novi.eu");
				return st;
			}

		}

	}



	@Override
	public Boolean checkSliceExist(String sliceURI) {
		log.info("I got a checkSliceExist for slice: {}", sliceURI);
		return LocalDbCalls.checkSliceExist(sliceURI);
	}

	@Override
	public int storeNoviUser(NOVIUser userObject) {
		log.info("I got a store novi user call from NOVI API");
		return NOVIUserClass.storeNoviUser(userObject);
	}



	@Override
	public Set<String> execStatementReturnResults(String subject,
			String predicate, String object, String... contexts) {
		return LocalDbCalls.execStatementReturnRes(subject, predicate, object, contexts);
	}
	
	@Override
	public String execStatementPrintResults(String subject,
			String predicate, String object, String context) {
		return LocalDbCalls.execPrintStatement(subject, predicate, object, context);
		
	}



	@Override
	public Set<Node> listResources() {
		log.info("I got a list resources call from NOVI API");
		UpdateAvailability update = new UpdateAvailability();
		return update.listResources();
	}
	
	@Override
	public Set<Node> listAllResources() {
		log.info("I got a list all resources call from NOVI API, returning all resources regardless of availability	 ");
		UpdateAvailability update = new UpdateAvailability();
		return update.listAllResources();
	}
	
	@Override
	public Set<Node> listResources(NOVIUser user) {
		log.info("I got a list resources call for user {}, from NOVI API", user.toString());
		UpdateAvailability update = new UpdateAvailability();
		return update.listResources(user);
	}

	////////////////GETTERS AND SETTERS////////////////////////////

/*	public TestbedCommunication getTestbedCommun() {
		return testbedCommun;
	}

	public void setTestbedCommun(TestbedCommunication testbedCommun) {
		this.testbedCommun = testbedCommun;
	}
*/
	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}


	public ReportEvent getUserFeedback() {
		return userFeedback;
	}

	public void setUserFeedback(ReportEvent userFeedback) {
		this.userFeedback = userFeedback;
	}

	/**
	 * @return the remoteRISList
	 */
	public List<RemoteRisServe> getRemoteRISList() {
		return remoteRISList;
	}


	/**
	 * @param remoteRISList the remoteRISList to set
	 */
	public void setRemoteRISList(List<RemoteRisServe> remoteRISList) {
		this.remoteRISList = remoteRISList;
		remoteRis.setRemoteRISList(remoteRISList);
	}


	/**
	 * this is not used from service mix, is for JUnit tests
	 * @param remoteRis
	 */
	public void setRemoteRis(RemoteRisDiscovery remoteRis) {
		this.remoteRis = remoteRis;
	}








}
