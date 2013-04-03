package eu.novi.monitoring.credential;

public class UsernameRSAKey extends Credential {
	public String username;
	public String password;
	public String RSAKey;

	public UsernameRSAKey(String username, String RSAKey, String password)
	{
	    this.username = username;
	    this.RSAKey = RSAKey;
	    this.password = password;
	}
	
	public String getType()
	{
	    return "UsernameRSAKey";
	}
}
