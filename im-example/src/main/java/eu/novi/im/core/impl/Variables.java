package eu.novi.im.core.impl;

/**it contains some global variables and also some static simple functions that is used
 * from all the implemented classes
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class Variables {
	
	public static final String NOVI_IM_BASE_ADDRESS = "http://fp7-novi.eu/im.owl#";
	public static final String NOVI_POLICY_BASE_ADDRESS = 
			"http://fp7-novi.eu/NOVIPolicyService.owl#";
	public static final String NOVI_UNIT_ADDRESS = "http://fp7-novi.eu/unit.owl#";
	
	
	/**it ckeck the URI.
	 * If the uri contains the novi base address then it returns the uri as it is.
	 * if the novi base address is not contained in the uri then it add it. 
	 * @param uri the uri to check
	 * @return the processed uri
	 */
	public static String checkURI(String uri)
	{
		return checkURI(uri, NOVI_IM_BASE_ADDRESS);
	}
	
	
	/**it ckeck the URI.
	 * If the uri contains the novi policy base address then it returns the uri as it is.
	 * if the novi policy base address is not contained in the uri then it add it. 
	 * @param uri the uri to check
	 * @return the processed uri
	 */
	public static String checkPolicyURI(String uri)
	{
		return checkURI(uri, NOVI_POLICY_BASE_ADDRESS);
	}
	
	/**it ckeck the URI.
	 * If the uri contains the novi unit base address then it returns the uri as it is.
	 * if the novi unit base address is not contained in the uri then it add it. 
	 * @param uri the uri to check
	 * @return the processed uri
	 */
	public static String checkUnitURI(String uri)
	{
		return checkURI(uri, NOVI_UNIT_ADDRESS);
	}
	
	
	private static String checkURI(String uri, String baseAddress)
	{
		if (uri.contains(baseAddress))
		{
			return uri;
		}
		else if (uri.startsWith("urn") || uri.startsWith("http"))
		{
			return uri;
		}
		else 
		{
			return baseAddress + uri;
		}
		
	}

}
