/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

import eu.novi.im.core.impl.PlatformImpl;


/**
 * Implementation of the response for the List Resources method.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class RHListResourcesResponseImpl implements RHListResourcesResponse {
	
	private Boolean hasError;
	private String platformString;
	private StringBuilder errorMessage;
	
	public RHListResourcesResponseImpl() {
		errorMessage = new StringBuilder();
	}
	
	public String getPlatformString() {
		return platformString;
	}

	public void setPlatformString(String platformString) {
		this.platformString = platformString;
	}

	public Boolean hasError() {
		return hasError;
	}
	
	public void setHasError(Boolean error) {
		this.hasError = error;
	}

	public String getErrorMessage() {
		return errorMessage.toString();
	}
	
	public void setErrorMessage(String eMessage) {
		errorMessage.append(eMessage);
	}

}
