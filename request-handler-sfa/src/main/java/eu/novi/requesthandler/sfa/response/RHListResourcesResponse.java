/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

/**
 * Methods that will implement the response for the List Resources method in RH
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public interface RHListResourcesResponse {

	/**
	 * Returns the topology of the substrate that is in the layer underneeth 
	 * @return Topology
	 */
	public String getPlatformString();
	
	/**
	 * Return if an error occurred during the listing of resources.
	 * @return true if error, false otherwise
	 */
	public Boolean hasError();
	
	/**
	 * If hasError = true, then there will be an error message
	 * @return The error message obtained from the testbed.
	 */
	public String getErrorMessage();
	
	/**
	 * Set the error message that returns the platform
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage);
	
	/**
	 * Set if there was error or not during the list resources.
	 * @param error
	 */
	public void setHasError(Boolean error);
	
	/**
	 * Set the platform that the testbed returned. 
	 * @param platform
	 */
	public void setPlatformString (String platformString);
	
}
