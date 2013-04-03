package eu.novi.authentication;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;


public class CoreTest {

@Ignore
@Test
public void testUpdate() throws MalformedURLException
{
	Core cor=new Core();
	String sliceName = "novi_slice_699986568";
	String sessionk = "IMDciUUdS+oh+3Gsflo4tR9qiPBARJA3Hr+mBubflXo=";
	int newexpirationTime = 1362057869;
	System.out.println(cor.call3(sessionk, sliceName, newexpirationTime));
}

@Test
@Ignore
public void TestOnAuth0() throws Exception
{
	Core bauth= new Core();
	//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
	//NOVIUser theUser=bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
	NOVIUser theUser=bauth.getAuth("chrisap@noc.ntua.gr","novi1");
	
	System.out.println(theUser.getHasSessionKey());
	System.out.println(theUser.toString());
	System.out.println("OK about Authentication without public key");
}


@Test
@Ignore
public void TestOnAuth() throws Exception
{
	Core core=new Core();
	NOVIUser theUser=core.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi");
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
	Core bauth= new Core();
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
	Core bauth= new Core();
	//assertEquals(0,bauth.getAuth("ykryftis@netmode.ece.ntua.gr","netmodenovi"));
	NOVIUser theUser=bauth.getAuth("notexisting@nothing.eu","password");
	if (theUser.getHasSessionKey() == null)
	{
		System.out.println("OK SessionKey is null");
	}
	System.out.println("OK about NOT Existing User");
}

@Test
@Ignore
public void UpdateSliceExpTest() throws Exception
{
	Core ex=new Core();
	Date date=new Date();
	date.setMonth(3);
	date.setDate(5);
	date.setYear(113);
	//date.setTime(1362057870);
	System.out.println(date.toString());
	NOVIUserImpl user=new NOVIUserImpl("ykryftis@netmode.ece.ntua.gr");
	user.setHasSessionKey("DaR7A9fn/8kSOV474Xd+JNCyBAo9Viea9MueJXa1cO0=");
	String slice_id="http://fp7-novi.eu/im.owl#slice_699986568";
	ex.updateExpirationTime(user, slice_id, date);
}
}
