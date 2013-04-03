/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.exceptions;

/**
 * Exception thrown when a testbed returns error
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 *
 */
public class TestbedException extends Exception {

	public TestbedException(String description) {
		super(description);
	}
}
