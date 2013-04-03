package eu.novi.resources.discovery;


import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;


/**
 * 
 * 
 * 
 * an interface for the provided calls to the policy service
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface PolicyCalls {

	
	
	/**it calls PolicyAA and it sends to it all the slices currently in the RIS DB
	 */
	public void initPolicy();
	
	
	
	
	/**it gives the partitioning cost for the local testbed.
	 * It is called by a remote policy service
	 * @param requestedTopology the unbound requested topology in xml/rdf string format
	 * @return a FPartCostTestbedResponse object, that contain
	 * the results
	 */
	public FPartCostTestbedResponseImpl giveLocalPartitioningCost(
			String requestedTopology);
	
	
	/**get the novi user for a given slice
	 * @param sliceURI the slice URI
	 * @return the user URI or null of it is not found
	 */
	public String getNoviUser(String sliceURI);
}
