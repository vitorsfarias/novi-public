package eu.novi.authentication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Platform;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.policy.impl.RoleImpl;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
public class Core implements InterfaceForPS{
	private static final transient Logger log = LoggerFactory.getLogger(Core.class);
	private RoleImpl plPI=new RoleImpl("PlanetLabPI");
	private RoleImpl plUser=new RoleImpl("PlanetLabUser");
	
	public NOVIUserImpl getAuth(String username, String password) throws Exception {
	        Core core = new Core();
	    	core.install();
	        NOVIUserImpl theUser= core.call2(username,password);
	        log.info("The User "+username + "returned the NOVIUser object with the session key "+ theUser.getHasSessionKey());
	        System.out.println("Finished.");
	        return theUser;
	   }
	
	
	public void install() throws Exception {
        // Create a trust manager that does not validate certificate chains
 System.setProperty("jsse.enableSNIExtension","false");
 //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
    	TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
 
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    // Trust always
                }
 
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    // Trust always
                }

            }
        };
 
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        // Create empty HostnameVerifier
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
                    return true;
            }
        };
 
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
    
    public NOVIUserImpl call2(String username, String password) throws MalformedURLException
    {
    	log.info("Received the auth call for user: "+ username );
    	NOVIUserImpl theUser= new NOVIUserImpl(username);
    	Set<String> hasRoleInPlatform=new HashSet<String>();
    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
         config.setServerURL(new URL("https://myplc.fp7-novi.eu/PLCAPI/"));
         config.isEnabledForExtensions();
         config.setEnabledForExtensions(true);
		 config.setEnabledForExceptions(false);
         XmlRpcClient client = new XmlRpcClient();
         client.setConfig(config);
         client.setTypeFactory(new MyTypeFactory(client));
    	 Map m = new HashMap();
	        m.put("AuthMethod", "password");
	        m.put("Username", username);
	        m.put("AuthString", password);
		String n = username;
		Object[] params = new Object[]{m};
	    Object[] params2 = new Object[]{m,n};
		Object response;
		 try {
	//		if(userFeedback != null)
		//		userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "Checking the Authentication Status of user "+username +".","http://www.fp7-novi.eu");
				
				
			response= client.execute(config, "AuthCheck", params);
			System.out.println(response.toString());
			log.info(response.toString());
			Object Session = client.execute(config,"GetSession",params);
			System.out.println(Session.toString());
			log.info(Session.toString());
			theUser.setHasSessionKey(Session.toString());
			Platform hasUserPlatform = new PlatformImpl("PlanetLab");
			theUser.setHasUserPlatform(hasUserPlatform);
			Object[] person= (Object[]) client.execute(config,"GetPersons",params2);
			Map p = (Map) person[0];
			System.out.println(p.get("first_name"));
			log.info((String) p.get("first_name"));
			System.out.println(p.get("last_name"));
			try {
			Object[] keys= (Object[]) p.get("key_ids");
			System.out.println(keys[0]);
			//I am going to add the first key of the user in the list of public keys.
			Object[] params3 = new Object[]{m,keys};
			Set<String> publicKeys = new HashSet<String>();
			Object[] pubkeys =(Object[]) client.execute(config,"GetKeys",params3);
            Map pkey= (Map) pubkeys[0];
            String theKey=(String) pkey.get("key");
			System.out.println(theKey);
			log.info(theKey);
			publicKeys.add(theKey);
			theUser.setPublicKeys(publicKeys);
			//OK about the public Key
			theUser.setFirstName((String )p.get("first_name"));
			theUser.setLastName((String )p.get("last_name"));
			Object[] roles = (Object[]) p.get("roles");
			
			for (Object object : roles) {
			    System.out.println(object.toString());
			    hasRoleInPlatform.add(object.toString());
			    }
			if (hasRoleInPlatform.contains("user"))
			{
			theUser.setHasNoviRole(plUser);
			log.info("The User "+username + " has Role planetlabUser");
			log.info("The role is stored in the NOVIUser object");
			if (theUser.getHasNoviRole() != null)
			{
				log.info("Print what we stored about the User"+theUser.getHasNoviRole().toString());
			}
			else {
				log.info("Just stored it and still NULL!!!! Error error");
			}
			
			}
			if (hasRoleInPlatform.contains("pi"))
				{
				theUser.setHasNoviRole(plPI);
				log.info("The User "+username + " has Role planetlabPI");
				
				}
			theUser.setHasRoleInPlatform(hasRoleInPlatform);
			
			//if(userFeedback != null)
			//	userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "The User "+username+ " is authenticated.","http://www.fp7-novi.eu");
			log.info("The User "+username + " is authenticated .");
				
			} catch (Exception en) 
			{
				log.info("No public key! Please upload througt myplc!!");
				//if(userFeedback != null)
				//	userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "The User "+username+ " does not have a public key.","http://www.fp7-novi.eu");
				return theUser;
			}
			Object[] sites= (Object[]) p.get("site_ids");
			System.out.println(sites[0]);
			theUser.setBelongsToDomain(sites[0].toString());			
			
				
	} catch (XmlRpcException e) {
		if (e.getMessage().equals(": AuthCheck: Failed to authenticate call: PasswordAuth: Password verification failed"))
		{
			System.out.println("Not authenticated");
			log.info("The User "+username + " is not authenticated .");
		//	if(userFeedback != null)
		//		userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "The User "+username+ " is NOT authenticated.","http://www.fp7-novi.eu");
		}
		else if (e.getMessage().equals(": AuthCheck: Failed to authenticate call: PasswordAuth: No such account"))
		{
			System.out.println("Not existing User");
			log.info("The User "+username + " does not exists.");
		//	if(userFeedback != null)
		//		userFeedback.instantInfo(userFeedback.getCurrentSessionID(), "Policy Manager AA", "The User "+username + " does NOT exist.","http://www.fp7-novi.eu");
		}
		else {
			log.info("e.printStackTrace();");
			System.out.println("Exception here "+e.getMessage());
			e.printStackTrace();
			log.info(e.getMessage());
		}	
		}
		 return theUser;
    }
    public int call3(String sessionkey, String sliceName, int newexpirationTime) throws MalformedURLException
    {
    	log.info("Received the call for update the expiration time of slice: "+ sliceName +" with new expiration time "+ newexpirationTime );
    //	String theSessionKey=user.getHasSessionKey();
    	log.info("The session key of the User is:" + sessionkey);
    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("https://myplc.fp7-novi.eu/PLCAPI/"));
        config.isEnabledForExtensions();
        config.setEnabledForExtensions(true);
		config.setEnabledForExceptions(false);
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        client.setTypeFactory(new MyTypeFactory(client));
    	Map m = new HashMap();
	        m.put("AuthMethod", "session");
	        m.put("session", sessionkey);
	    Map up = new HashMap();
	    	up.put("expires", newexpirationTime);
	    Object[] params2 = new Object[]{m,sliceName,up};
		Object re;
		 try {
			re= client.execute(config,"UpdateSlice",params2);
			log.info("result ="+re.toString());			
	} catch (XmlRpcException e) {

			log.info("e.printStackTrace();");
			System.out.println("Exception here "+e.getMessage());
			e.printStackTrace();
			log.info(e.getMessage());
		}	
		 return 0;

}
    
    
	 public int updateExpirationTime(NOVIUserImpl user, String slice_id, Date date) throws Exception {
		log.info("Update Slice expiration time");
		String usersession="";
		if (user !=null)
		{
	    	if (user.getHasSessionKey()!=null)
	    	{
	    		usersession= user.getHasSessionKey();
	    	}
	    	else
	    	{
	    		log.info("No session key");
	    		return 1;
	    	}
		}
		else
		{
			log.info("User is null");
			return 2;
		}
		if (date!= null)
		{
			log.info("The date is "+ date.getDate() +"/"+date.getMonth()+"/"+1900+date.getYear());
		}
		else
		{
			log.info("No date provided");
			return 3;
		}
		if (slice_id==null)
		{
			log.info("No slice provided");
			return 4;
		}
		log.info("Update Slice request for "+slice_id + "for "+ date.getTime());
		String pl_slice_id=slice_id.replaceFirst("http://fp7-novi.eu/im.owl#", "novi_");
		log.info("Novi slice is :"+pl_slice_id);
		Core core = new Core();
	    core.install();
	    int dateInformat=(int) (date.getTime()/1000);
	    System.out.println(dateInformat);
	    int success= core.call3(usersession, pl_slice_id, dateInformat);
		return 0;	    
	}
}
