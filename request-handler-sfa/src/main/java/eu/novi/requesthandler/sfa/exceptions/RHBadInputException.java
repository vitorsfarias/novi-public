/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.exceptions;

/**
 * Exception thrown when there is some missmatching in the expected input to RH
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 *
 */
public class RHBadInputException extends Exception {

	public RHBadInputException (String description) {
		super(description);
	}
}
