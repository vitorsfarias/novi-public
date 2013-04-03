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
package eu.novi.policylistener.authentication;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import eu.novi.im.policy.NOVIUser;
import eu.novi.policylistener.authentication.Basic;
public class BasicTest {
	
	@Test
	@Ignore
	public void TestOnAuth0() throws Exception
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		//NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
		NOVIUser theUser=bauth.getAuth("chrisap@noc.ntua.gr","novi1");
		
		System.out.println(theUser.getHasSessionKey());
		System.out.println(theUser.toString());
		System.out.println("OK about Authentication without public key");
	}
	
	@Test
	@Ignore
	public void TestOnSendPolicy()
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		//NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
		bauth.addPolicyFromFile("http://www.netmode.ntua.gr/~ykryftis/ex1.p2");
		System.out.println("OK provided the file");
	}
	
	@Test
	@Ignore
	public void TestOnManualPolicy()
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		//NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
		System.out.println("So the result of the ls is: \n"+bauth.mCallToPonder2("ls")+".THE END");
		System.out.println("So the result of the ls is: \n"+bauth.mCallToPonder2("cd event")+".THE END");
		System.out.println("So the result of the ls is: \n"+bauth.mCallToPonder2("ls /root/event")+".THE END");
	}
	
	@Test
	@Ignore
	public void TestOnAuth() throws Exception
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
		//NOVIUser theUser=bauth.getAuth("chrisap@noc.ntua.gr","novi1");
		
		System.out.println(theUser.getHasSessionKey());
		System.out.println(theUser.toString());
		Set<String> theRoles = theUser.getHasRoleInPlatform();
		for (Object object : theRoles) {
		    System.out.println(object.toString());
		}
		System.out.println(theUser.getHasUserPlatform());
	    assertEquals("http://fp7-novi.eu/im.owl#PlanetLab",theUser.getHasUserPlatform().toString());
		for (Object object : theUser.getPublicKeys()) {
		    System.out.println(object.toString());
		   // assertEquals("ssh-rsa root@snf-965",object.toString());
		    }
		System.out.println(theUser.getBelogsToDomain());
		System.out.println("theUser.getHasNoviRole()"+ theUser.getHasNoviRole());
		System.out.println("OK about Authentication");
	}
	
	@Test
	@Ignore
	public void TestOnAuth2() throws Exception
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmoden");
		if (theUser.getHasSessionKey() == null)
		{
			System.out.println("OK SessionKey is null");
		}
		System.out.println("OK about NOT Authentication");
	}
	
	@Test
    @Ignore
	public void TestOnAuth3() throws Exception
	{
		Basic bauth= new Basic();
		//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
		NOVIUser theUser=bauth.getAuth("notexisting@nothing.eu","password");
		if (theUser.getHasSessionKey() == null)
		{
			System.out.println("OK SessionKey is null");
		}
		System.out.println("OK about NOT Existing User");
	}
	
}
