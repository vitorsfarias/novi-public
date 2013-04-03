package eu.novi.nswitch.manager;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.nswitch.exceptions.FederationException;


/**
 * NSwitch manager that gets topology from the Request-Handler
 * @author pikusa
 *
 */
public interface NswitchManager {

	/**
	 * Create federation in the given topology
	 * @param topology
	 * @param sliceName TODO
	 * @throws Exception 
	 */
	public void createFederation(TopologyImpl topology, String sliceName) throws FederationException, Exception;
	
	/**
	 * Release the created federation
	 * @param topology
	 * @throws Exception
	 */
	public void releaseFederation(String sliceName) throws Exception;
	
}	
