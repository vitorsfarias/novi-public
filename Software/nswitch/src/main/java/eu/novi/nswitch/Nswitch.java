package eu.novi.nswitch;


/**
 * Interfaces created for nswitch-testbed components. Nswitch-manager finds all the components
 * that implement this interface in order to call federation and defederation methods.
 * @author pikusa
 *
 */
public interface Nswitch {

	/**
	 * Method called by nswitch-manager in order to create federation on particular testbed
	 * @param nodeIp - The ip of physicall node to federate
	 * @param sliceId - slice Id to federate
	 * @param vlanId - vlan id used in federated slice
	 * @param sliceName - name of the slice to federate. It must be the same as for planetlab
	 * @param privateIp - Ip of sliver 
	 * @param netmask - Netmask in slicer
	 * @return TODO
	 * @throws Exception
	 */
	String federate(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp, String netmask) throws Exception;
	
	/**
	 * Method called by nswitch-manager in order to remove federation from particular testbed
	 * @param nodeIp - The ip of physicall node to federate
	 * @param sliceId - slice Id to federate
	 * @param vlanId - vlan id used in federated slice
	 * @param sliceName - name of the slice to federate. It must be the same as for planetlab
	 * @param privateIp - Ip of sliver 
	 * @param netmask - Netmask in slicer
	 * @throws Exception
	 */
	void defederate(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp, String netmask) throws Exception;
	

}
