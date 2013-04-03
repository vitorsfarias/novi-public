package eu.novi.authentication;

import java.util.Date;

import eu.novi.im.policy.impl.NOVIUserImpl;

 public interface InterfaceForPS {
	public NOVIUserImpl getAuth(String username, String password) throws Exception;
	public int updateExpirationTime(NOVIUserImpl user, String slice_id, Date date) throws Exception;

}
