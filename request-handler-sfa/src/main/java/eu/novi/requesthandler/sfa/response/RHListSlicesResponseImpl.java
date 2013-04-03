/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.response;

import java.util.ArrayList;
import java.util.List;

public class RHListSlicesResponseImpl implements RHListSlicesResponse {
	List<String> slices;
	boolean hasError;
	StringBuilder errorMessage;
	
	public RHListSlicesResponseImpl() {
		slices = new ArrayList<String>();
		errorMessage = new StringBuilder();
	}
	
	@Override
	public List<String> getSlices() {
		return slices;
	}
	
	public void setSlices(List<String> slices) {
		this.slices = slices;
	}
	
	public void addSlice(String slice) {
			slices.add(slice);
	}

	public boolean hasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public String getErrorMessage() {
		return errorMessage.toString();
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage.append(errorMessage);
	}
	
	public int getNumberOfSlices() {
		return slices.size();
	}

}
