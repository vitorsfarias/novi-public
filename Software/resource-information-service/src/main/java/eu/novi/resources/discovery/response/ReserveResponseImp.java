package eu.novi.resources.discovery.response;

import eu.novi.im.core.Reservation;



/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ReserveResponseImp implements ReserveResponse {
	
	private Integer sliceID;
	private ReserveMess errorMessage;
	private String message;
	private Reservation limitedSliceInfo;
	private String userLoginInfo;
	
	






	@Override
	public Integer getSliceID() {
		return sliceID;
	}



	public void setSliceID(Integer sliceID) {
		this.sliceID = sliceID;
	}

	@Override
	public ReserveMess getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(ReserveMess errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean hasError() {
		if (errorMessage == ReserveMess.NO_ERROR)
			return false;
		else 
			return true;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**it is used to get the slice info that will be stored in
	 * the remote platform
	 * @return
	 */
	public Reservation getLimitedSliceInfo() {
		return limitedSliceInfo;
	}


	public void setLimitedSliceInfo(Reservation limitedSliceInfo) {
		this.limitedSliceInfo = limitedSliceInfo;
	}
	
	@Override
	public String getUserLoginInfo() {
		return userLoginInfo;
	}


	public void setUserLoginInfo(String userLoginInfo) {
		this.userLoginInfo = userLoginInfo;
	}


}