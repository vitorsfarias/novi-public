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
package eu.novi.policylistener.ponder2comms;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Ignore;
import org.mockito.stubbing.OngoingStubbing;

import eu.novi.policylistener.ponder2comms.TelnetClient;

import static org.mockito.Mockito.mock;

public class TelnetClientTest {

	
//		Piotr Pikusa: I have disabled this test because integration test must not try to connect system-tests instance 
//		since on the latter there is nothing installed before successful finish Integration on CI.
	
	
	TelnetClient telc= new TelnetClient();
	Socket Mocksoc= mock(Socket.class);
	DataInputStream MockDin= mock(DataInputStream.class);
	//BufferedReader MockDin= mock(BufferedReader.class);
	DataOutputStream MockDout= mock(DataOutputStream.class);
	
	@Test
	@Ignore
	public void test() {
		String result = "";
		result = telc.TelnetPonder2("true.\r\n");
		System.out.println("So we can comminicate with ponder2 with telnet");
		System.out.println(result);
		if (result.equals("false")) 
			{
			fail("No connection with Ponder2 (telnet 150.254.160.28 13570)");
			}
		if (result.equals("true"))
		{
			System.out.println("We passed the test for the connection");
		}
	}
}
/*	@Test
	public void testMock() {
		System.out.println("TTTTTest");
		String result = "";
		result = telc.TelnetPonder2("true.\r\n");
		System.out.println("So we can comminicate with ponder2 with telnet");
	}

	@SuppressWarnings("deprecation")
	@Before
	public <T> void initialize() throws IOException {
		System.out.println("HERE");
		//log.debug("Initializing AuthorizationSearchTest...");
		
		telc.setSocket(Mocksoc);
	//telc.setDataInputStream(MockDin);
		//telc.setDataOutputStream(MockDout);
		//when(Mocksoc.getInputStream()).thenReturn(MockDin);
		telc.setDataInputStream(MockDin);
		//when(Mocksoc.getOutputStream()).thenReturn(MockDout);
		System.out.println("no1");
		//when(MockDin.readLine()).thenReturn("ds");
		//when(MockDin.readLine()).thenReturn(mockreadLine());
	    when(MockDin.readLine()).thenReturn("akslfjlksa");
		
		System.out.println("no2");
	//	when(MockDout.writeBytes(anyString())).thenReturn();
		System.out.println("no3");
	}
	
	//private  mockflush()
	//{
		
	//}
	
	public String mockreadLine()
	{
		return "ab";
	}

}
*/