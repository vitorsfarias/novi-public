/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

import java.util.List;


/**
 * The response from the RH to the RIS when is requested creating a slice
 * It will have the following format:
 * SliceID: Defines the result
 * hasError: True when there is error, false otherwise
 * errorMessage: Contains the error if hasError = true.
 * 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class RHCreateDeleteSliceResponseImpl implements RHCreateDeleteSliceResponse {

	private String sliceID;
	private StringBuilder errorMessage;
	private Boolean hasError;
	private List<String> testbedsList;
	private String topologyCreated;
	private String userLogin;
	
	public RHCreateDeleteSliceResponseImpl() {
		errorMessage = new StringBuilder();
	}

	public String getSliceID() {
		return sliceID;
	}

	public void setSliceID(String sliceID) {
		this.sliceID = sliceID;
	}

	public String getErrorMessage() {
		return errorMessage.toString();
	}

	public void setErrorMessage(String eMessage) {
		errorMessage.append(eMessage);
	}

	public Boolean hasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}
	
	public void setListOfTestbedsWhereSliceIsCreated(List<String> testbeds){
		this.testbedsList = testbeds;
	}
		
	public List<String> getListOfTestbedsWhereSliceIsCreated(){
		return testbedsList;
	}

	public String getTopologyCreated() {
		return topologyCreated;
	}

	public void setTopologyCreated(String userLogin) {
		this.topologyCreated = userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserLogin() {
		return userLogin;
	}	
}
