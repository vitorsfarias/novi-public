package eu.novi.im.core.impl;


import eu.novi.im.core.LoginService;
import eu.novi.im.unit.IPAddress;

public class LoginServiceImpl extends ServiceImpl implements LoginService {


	private IPAddress hasLoginIPv4Address;
	private String hasLoginProtocol;
	private String hasLoginPassword;
	private Integer hasLoginPort;
	private String hasLoginUsername;

	public LoginServiceImpl(String uri)
	{
		super(uri);
	}
	
	@Override
	public IPAddress getHasLoginIPv4Address() {
		return this.hasLoginIPv4Address;
	}

	@Override
	public void setHasLoginIPv4Address(IPAddress hasLoginIPv4Address) {
		this.hasLoginIPv4Address = hasLoginIPv4Address;

	}

	@Override
	public String getHasLoginProtocol() {
		return  this.hasLoginProtocol;
	}

	@Override
	public void setHasLoginProtocol(String hasLoginProtocol) {
		this.hasLoginProtocol = hasLoginProtocol;

	}



	@Override
	public String getHasLoginPassword() {
		return this.hasLoginPassword;
	}

	@Override
	public void setHasLoginPassword(String hasLoginPassword) {
		this.hasLoginPassword = hasLoginPassword;
		
	}

	@Override
	public Integer getHasLoginPort() {
		return this.hasLoginPort;
	}

	@Override
	public void setHasLoginPort(Integer hasLoginPort) {

		this.hasLoginPort = hasLoginPort;
		
	}

	@Override
	public String getHasLoginUsername() {
		return this.hasLoginUsername;
	}

	@Override
	public void setHasLoginUsername(String hasLoginUsername) {
		this.hasLoginUsername = hasLoginUsername;
		
	}
	

}
