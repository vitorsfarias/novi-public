package eu.novi.monitoring.credential;

public class UsernamePassword extends Credential {
    public String username;
    public String password;
    
    public UsernamePassword(String username, String password)
    {
	this.username = username;
	this.password = password;
    }
    
    public String getType()
    {
	return "UsernamePassword";
    }
}
