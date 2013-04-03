/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping;

import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.junit.Test;

import eu.novi.mapping.utils.IRMOperations;

public class UUIDTest {
	
	@Test
	public void testGetUUID() throws NoSuchAlgorithmException{
		int counter = 0;
		for(int i=0;i<10000;i++){
			String sessionID = UUID.randomUUID().toString();
			int test = IRMOperations.getUUID(sessionID);
			if(test < 0) counter++;
		}
		System.out.println(counter);
		assertTrue(counter == 0);
		
	}
	
}
