/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

import java.util.List;

/**
 * API offered in the response class when creation of a slice is requested.
 * 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public interface RHCreateDeleteSliceResponse {

	/**
	 * Returns the slice ID of the created Slice
	 * @return: SliceID in a String form.
	 */
	public String getSliceID();
	
	/**
	 * Sets the slice ID of the response
	 */
	public void setSliceID(String slice);
	
	/**
	 * Returns if there was error creating the slice
	 * @return: Boolean saying if there is error or not.
	 */
	public Boolean hasError();
	
	/**
	 * Sets the if exists error
	 */
	public void setHasError(Boolean hasError);
	
	/**
	 * Returns the Error message returned by the testbed
	 * @return: Error message if exists.
	 */
	public String getErrorMessage();
	
	/**
	 * Sets the Error when it exists.
	 */
	public void setErrorMessage(String errorMessage);
	
	/**
	 * Sets the List of testbeds where there are resources in the slice created.
	 */
	public void setListOfTestbedsWhereSliceIsCreated(List<String> testbeds);
	
	/**
	 * Gets the List of testbeds where there are resources in the slice created.
	 */
	public List<String> getListOfTestbedsWhereSliceIsCreated();
	
	public String getTopologyCreated();
	
	public void setTopologyCreated(String userLogin);
	
	public void setUserLogin(String userLogin);

	public String getUserLogin();
	
}
