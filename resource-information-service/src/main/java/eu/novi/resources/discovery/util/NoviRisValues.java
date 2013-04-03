package eu.novi.resources.discovery.util;



/**
 *a class that have various NOVI and RIS values. For instance the router hardware type
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class NoviRisValues {
	
	private final static String ROUTER_HARDW_TYPE = "genericNetworkDevice"; //"genericNetworkdevice";

	
	
	/**get the hardware type of a router
	 * @return
	 */
	public static String getRouterHardType()
	{
		return ROUTER_HARDW_TYPE;
	}
	
	
	/**given a hardware type it checks if the node is router
	 * @param hardwType
	 * @return if router then true
	 */
	public static boolean isRouter(String hardwType)
	{
		if (ROUTER_HARDW_TYPE.equalsIgnoreCase(hardwType))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
