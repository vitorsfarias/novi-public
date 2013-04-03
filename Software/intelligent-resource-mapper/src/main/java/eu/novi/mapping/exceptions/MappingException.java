/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.exceptions;

/**
 * Exceptions that is thrown whenever an error during mapping occurs.
 */
public class MappingException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MappingException(String desc) {
		super(desc);
	}
	
}
