package eu.novi.nswitch.exceptions;

/**
 * Exceptions that is thrown whenever the error during federation occurs
 * @author pikusa
 *
 */
public class FederationException extends Exception{

	
	public FederationException() {
		
	}
	
	public FederationException(String desc){
		super(desc);
	}
}
