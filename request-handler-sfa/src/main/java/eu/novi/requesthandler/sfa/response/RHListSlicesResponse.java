/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

import java.util.List;

/**
 * Methods that will implement the response for the List Resources method in RH
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 */
public interface RHListSlicesResponse {

	/**
	 * Returns the list of user slices
	 */
	public List<String> getSlices();
	
	public void setSlices(List<String> slices);
	
	public void addSlice(String slice);
	
	public boolean hasError();

	public void setHasError(boolean hasError);
	
	public String getErrorMessage();
	
	public void setErrorMessage(String errorMessage);
	
	
}
