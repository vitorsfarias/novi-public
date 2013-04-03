package eu.novi.im.util;



/**
 * Contains the NOVI base address and some utility functions 
 * that can help us to manipulate NOVI URIs
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 * 
 */
public class UrisUtil {
	
	private static final String NOVI_IM_BASE_ADDRESS = "http://fp7-novi.eu/im.owl#";
	private static final String NOVI_POLICY_BASE_ADDRESS = 
			"http://fp7-novi.eu/NOVIPolicyService.owl#";
	private static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String NOVI_UNIT_ADDRESS = "http://fp7-novi.eu/unit.owl#";
	
	
	/**remove from the URI the novi IM base address :
	 * http://fp7-novi.eu/im.owl#, or the policy IM base or the unit IM base address
	 * @param uri
	 * @return the id of the URI
	 */
	public static final String getURNfromURI(String uri)
	{
		if (uri.contains(NOVI_IM_BASE_ADDRESS))
		{
			return uri.replaceAll(NOVI_IM_BASE_ADDRESS, "");
		}
		else if (uri.contains(NOVI_POLICY_BASE_ADDRESS))
		{
			return uri.replaceAll(NOVI_POLICY_BASE_ADDRESS, "");
		}
		else if (uri.contains(NOVI_UNIT_ADDRESS))
		{
			return uri.replaceAll(NOVI_UNIT_ADDRESS, "");
		}
		else	
		{
			return uri;
		}
	
	}
	
	
	
	/////GETTERS/////
	public static String getNoviImBaseAddress() {
		return NOVI_IM_BASE_ADDRESS;
	}
	public static String getNoviPolicyBaseAddress() {
		return NOVI_POLICY_BASE_ADDRESS;
	}
	public static String getRdfPrefix() {
		return RDF_PREFIX;
	}
	public static String getNoviUnitAddress() {
		return NOVI_UNIT_ADDRESS;
	}
	

}
