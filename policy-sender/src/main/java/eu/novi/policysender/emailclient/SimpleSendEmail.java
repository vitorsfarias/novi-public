package eu.novi.policysender.emailclient;
/**
 * 
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
 * 
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */

//import org.mortbay.log.Log;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.connection.RESTCommunication;
//import eu.novi.mail.mailclient.*;

public class SimpleSendEmail {
	//private static InterfaceForMail callsToMail;
	private static final transient Logger log = 
			LoggerFactory.getLogger(SimpleSendEmail.class);
/*public int SendEmail(String receiver,String information ) {
	
	System.out.println("Calling the mail component");
	log.info("Calling the mail component");
	if (callsToMail == null)
	{
		log.warn("The object from Mail service is null");
		return -2;
	}
	else
	{
		int wassent=callsToMail.SendEmail(receiver, information);
		if (wassent==0)
		{
			log.info("We got error");
			return 0;
		}
		if (wassent==1)
		{
			log.info("Email was sent!!!");
			return 1;
		}
		log.info("Called the mail component and got unexpected error");
		return -1;
	}
	

}*/
public void sentEmailWithREST(String receiver, String information){
	//String address="";
	//String failureEvent="Text for the User";
	try {
		HashMap<String, String> requestParameters = new HashMap<String, String>();
		requestParameters.put("receiver", receiver);
		requestParameters.put("information", information);
		new RESTCommunication().executePostMethod("http://150.254.160.28:8080/forthemail/TempHandler/sentEmailEvent",requestParameters);
	} catch (HttpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
//public InterfaceForMail getcallsToMail() {
//	return callsToMail;
//}
//public void setcallsToMail(InterfaceForMail callsToMail) {
//	this.callsToMail = callsToMail;
//}
}