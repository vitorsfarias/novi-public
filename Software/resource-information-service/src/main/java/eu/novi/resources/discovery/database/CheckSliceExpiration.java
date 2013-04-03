package eu.novi.resources.discovery.database;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Reservation;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;


/**
 *a class that check for slices that expired or they are close to expired and 
 *inform policy service
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class CheckSliceExpiration {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(CheckSliceExpiration.class);
	
	private static int days_to_notify = 3; //send notification for slices that will expire in 3 days.
	
	

	
	/**
	 * for all the slices in the DB, check each one if it will expire soon,  and 
	 * notify policy. Delete from the DB the expired slices
	 * @return return a list with two Sets, the first set (index 0) contain the expired slices and 
	 * the second Set the slices that we need to send notification
	 * 
	 */
	public static List<Set<String>> checkSlices()
	{
		Set<String> expireSlices = new HashSet<String>();
		log.info("Checking the slices for expiration...");
		Set<String> slices = LocalDbCalls.printGetCurrentSlices();
		//find the slices that already was expired 
		for (String sl : slices)
		{
			
			if (checkIfSliceExpi(sl, 0, 0))
			{//the slice is expired
				log.info("I found the expired slice {}", sl);
				callPolicy4SliceExp(sl);
				expireSlices.add(sl);
			}
			
		}
		
		//remove the already expired slices
		slices.removeAll(expireSlices);
		
		Set<String> needNotifSlices = new HashSet<String>();
		//check for the slices that are expired soon
		for (String sl : slices)
		{
			if (checkSliceExpiWindows(sl, days_to_notify, 0, days_to_notify - 1 , (60 * 12) - 15))
			{
				log.info("I found the slice {}, which the expiration day is soon", sl);
				callPolicy2Notify4SlExpir(sl);
				needNotifSlices.add(sl);

			}
			else
			{
				log.info("I don't need to do anything about slice {}", sl);
			}
			
		}
		
		List<Set<String>> answer = new Vector<Set<String>>();
		answer.add(expireSlices); //index 0
		answer.add(needNotifSlices); //index 1
		
		return answer;
	

	}
	
	
	public static String updateExpirationTime(NOVIUser user, String sliceURI, Date date)
	{
		log.info("Renew the slice {}, new expiration date: {}", sliceURI, date.toString());
		PolicyServCommun.call4SliceRenew((NOVIUserImpl)user, sliceURI, date);
		LifetimeResult lifetimeRes = getExpLifetime(sliceURI);
		if (!lifetimeRes.isSliceExist())
		{

			return "The slice " + sliceURI + " doesn't exist in the RIS DB";
		}
		
		if (lifetimeRes.getExpLifetime() == null)
		{
			return "I can not find the lifetimes for the slice: " + sliceURI;
		}
		
		
		//get the lifetime
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setReadContexts(NoviUris.createURI(sliceURI));
		con.setAddContexts(NoviUris.createURI(sliceURI));
		Lifetime lifetime = null;
		try {
			lifetime = con.getObject(Lifetime.class, lifetimeRes.getExpLifetime().toString());
		} catch (RepositoryException e) {
			log.warn(e.getMessage());
			log.warn("I can not retrieve the lifetime for the slice {}", sliceURI);
		} catch (QueryEvaluationException e) {
			log.warn(e.getMessage());
			log.warn("I can not retrieve the lifetime for the slice {}", sliceURI);
		} catch (ClassCastException e)
		{
			log.warn(e.getMessage());
			log.warn("I can not retrieve the lifetime for the slice {}", sliceURI);
		}
		
		if (lifetime == null)
		{
			ConnectionClass.closeAConnection(con);
			return "the lifetime was not found";
			
		}
		
		//update the date
		XMLGregorianCalendar gregDate = null;
		try {
			GregorianCalendar gregorianCal = 
					new GregorianCalendar(TimeZone.getTimeZone("GMT-0:00"));
			gregorianCal.setTime(date);
			gregDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCal);
		} catch (DatatypeConfigurationException e) {
			log.warn(e.getMessage());
			log.warn("Error in creating the date for XMLGregorianCalendar");
			
		}
		lifetime.setEndTime(gregDate);
		String st = "The slice " + sliceURI + " was renewed succesfully";
		log.info(st);
		ConnectionClass.closeAConnection(con);
		return st;
				
		
	}

	
	/**when a slice is expired.
	 * it delete it and it call policy
	 * @param sliceUri
	 */
	protected static void callPolicy4SliceExp(String sliceUri)
	{
		NOVIUserImpl user = NOVIUserClass.getNoviUserSlice(sliceUri);
		if (user == null)
		{
			log.warn("I can not find the user for the slice {}", sliceUri);
		}
		Date expDate = getExpiDate(sliceUri);
		log.info("Deleting the expired slice {}", sliceUri);
		DeleteSlice.deleteSlice(sliceUri, null, null, null);
		PolicyServCommun.call4SliceExpiration(user, sliceUri, expDate);
		
	}
	
	/**when a slice is expiring soon.
	 * It calls policy
	 * @param sliceUri
	 */
	protected static void callPolicy2Notify4SlExpir(String sliceUri)
	{
		NOVIUserImpl user = NOVIUserClass.getNoviUserSlice(sliceUri);
		if (user == null)
		{
			log.warn("I can not find the user for the slice {}", sliceUri);
			return ;
		}
		Date expDate = getExpiDate(sliceUri);
		PolicyServCommun.call4SliceExpirationNotif(user, sliceUri, expDate);
		
	}
	
	
	
	/**get the expiration date in Java.util.Date
	 * @param sliceUri
	 * @return the date or null if it is not found
	 */
	protected static Date getExpiDate(String sliceUri)
	{
		Lifetime lifetime = getExpLifetime(sliceUri).getExpLifetime();
		if (lifetime == null)
		{
			return null;
		}
	
		return lifetime.getEndTime().toGregorianCalendar().getTime();
	}
	
	/**check if the slice expiration is in the given window, 
	 * That means, it expires in less time than the given daysLess and minutesLess and 
	 * it needs more time to expires than the given daysMore and minutesMore
	 * @param sliceURI
	 * @param daysLess
	 * @param minutesLess
	 * @param daysMore
	 * @param minutesMore
	 * @return true if the slice is expiring in the given window
	 * if the slice was not found then it return false. 
	 * If the slice has no lifetimes, or the expiration lifetime was not found then it return true
	 */
	protected static boolean checkSliceExpiWindows(String sliceURI, int daysLess, int minutesLess, 
			int daysMore, int minutesMore)
	{
		
		log.info("Checking the slice  for notification {} ...", sliceURI);

		LifetimeResult lifetimeRes = getExpLifetime(sliceURI);
		if (!lifetimeRes.isSliceExist())
		{
			return false;

		}

		if (lifetimeRes.getExpLifetime() == null)
		{//the lifetime was not found, so the slice doesn't have expiration date
			return true;
		}
		Lifetime exp_life = lifetimeRes.getExpLifetime();


		if (LocalDbCalls.checkIfLifetimeIsValid(exp_life, 0, daysLess, minutesLess))
		{
			log.info("The slice {} will not expire in less time than {} days and " + minutesLess +
					" minutes", sliceURI, daysLess);
			return false;
		}
		else
		{
			log.info("The slice {} will expire in less than {} days and " + minutesLess + " minutes",
					sliceURI, daysLess);
			if (LocalDbCalls.checkIfLifetimeIsValid(exp_life, 0, daysMore, minutesMore))
			{
				log.info("The slice {} will expire in less time than: {} days and " + minutesLess +
						" minutes, and in more time than: " + daysMore + " days and " + minutesMore + " minutes",
						sliceURI, daysLess);
				return true;
			}
			else
			{
				log.info("The slice {} will expire in less time than {} days and " + minutesMore +
						" minutes", sliceURI, daysMore);
				return false;
			}


		}

	}

	
	
	/**check if the slice is expiring, in less time than the given days and minutes.
	 * if you give zero days and minutes then it check if the slice is expiring now
	 * @param sliceURI
	 * @param days
	 * @param minutes 
	 * @return true if the slice is expiring, in less than the given days and minutes. Otherwise false,
	 * if the slice was not found then it return false. 
	 * If the slice has no lifetimes, or the expiration lifetime was not found then it return true
	 */
	protected static boolean checkIfSliceExpi(String sliceURI, int days, int minutes)
	{
		
		log.info("Checking the slice for expiration {} ...", sliceURI);
		LifetimeResult lifetimeRes = getExpLifetime(sliceURI);
		if (!lifetimeRes.isSliceExist())
		{
			return false;
			
		}
		
		if (lifetimeRes.getExpLifetime() == null)
		{//the lifetime was not found, so the slice doesn't have expiration date
			return true;
		}
		Lifetime exp_life = lifetimeRes.getExpLifetime();

		if (LocalDbCalls.checkIfLifetimeIsValid(exp_life, 0, days, minutes))
		{
			log.info("The slice {} will not expire in {} days and " + minutes + " minutes",
					sliceURI, days);
			return false;
		}
		else
		{
			log.info("The slice {} will expire in less than {} days and " + minutes + " minutes",
					sliceURI, days);
			return true;

		}
			
	}
	
	
	/**it get the expiration lifetime for the slice
	 * @param sliceURI
	 * @return if the slice was not found then the isSliceExist() return false
	 * if the lifetime was not found then the getExpLifetime() returns null 
	 */
	protected static LifetimeResult getExpLifetime (String sliceURI)
	{
		CheckSliceExpiration checkSlEx = new CheckSliceExpiration();
		
		LifetimeResult result = checkSlEx.new LifetimeResult();
		log.debug("Getting the expiration lifetime for the slice {} ...", sliceURI);
		Reservation slice = IRMLocalDbCalls.getLocalSlice(sliceURI);
		if (slice == null)
		{
			log.warn("I can not find the slice {}", sliceURI);
			result.setSliceExist(false);
			return result;
		}
		else if (slice.getContains() == null || slice.getContains().isEmpty())
		{//have in mind that in federica is stored only the slice name and lifetimes
			log.info("The slice was found but the information is not stored here. " +
					"Probaply it is stored in the remote platform");
			result.setSliceExist(false);
			return result;
		}
		
		if (slice.getHasLifetimes() == null || slice.getHasLifetimes().isEmpty())
		{//well, this is not good
			log.warn("I can not find any lifetimes for the slice {}", sliceURI);
			return result;
			
		}
		
		
		Set<Lifetime> lifetimes = slice.getHasLifetimes();
		for (Lifetime exp_life : lifetimes)
		{
			if (exp_life.toString().equals(NoviUris.getSliceLifetimeURI(sliceURI).toString()))
			{
				log.debug("I found the expiration lifetime for the slice {}", sliceURI);
				result.setExpLifetime(exp_life);
				return result;
			}
			
		}
		log.warn("The slice expiration lifetime was not found for the slice {}", sliceURI);
		return result;
		
	}
	
	
	/**it contain the results for the expiration lifetime of a slice
	 * @author chariklis
	 *
	 */
	private class LifetimeResult
	{
		
		private Lifetime  expLifetime = null;
		private boolean sliceExist = true;
		
		public Lifetime getExpLifetime() {
			return expLifetime;
		}
		public void setExpLifetime(Lifetime expLifetime) {
			this.expLifetime = expLifetime;
		}
		public boolean isSliceExist() {
			return sliceExist;
		}
		public void setSliceExist(boolean sliceExist) {
			this.sliceExist = sliceExist;
		}

	
		
	}
	
	
}
