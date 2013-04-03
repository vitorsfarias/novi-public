/**
 * 
 */
package eu.novi.resources.discovery.database;



import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import eu.novi.im.core.Platform;


/**
 * 
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * test all the local calls
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class IRMLocalDbCallsTest {
	
	//private static final transient Logger log =
	//		LoggerFactory.getLogger(IRMLocalDbCallsTest.class);
	
	@BeforeClass 
	public static void setUpBeforeClass() 
	{
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("FEDERICA_substrate.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);
		ManipulateDB.loadOWLFile("PLEtopologyModified3.owl", "RDFXML",
				NoviUris.createNoviURI("someContext"));
		//LocalDbCalls.showAllContentOfDB();
		
	}

	@AfterClass 
	public static void tearDownAfterClass()
	{
		//LocalDbCalls.showAllContentOfDB();
		ConnectionClass.stopStorageService();
		
	}
	
	
	@Test
	public void testGetSubstrate()
	{
	

		Platform platform = IRMLocalDbCalls.getSubstrate("http://fp7-novi.eu/im.owl#FEDERICA");
		assertEquals(platform.getContains().size(), 33);
		
	}
	

	/**
	 * for 1st year Review DEMO
	 */
/*	@Test
	public void testReserveSlice() {
		IRMLocalDbCalls irmCalls = new IRMLocalDbCalls();

		Topology topol1 = irmCalls.findLocalResources(IrmCallsTest.testCreateTopology());
		assertEquals(topol1.getContains().size(), 2);

		Integer sliceId = irmCalls.reserveLocalSlice(topol1); //reserve the available resources
		log.debug("The Id of the slice is : " +sliceId);

		//LocalDbCalls.execPrintStatement(null, LocalDbCalls.createNoviURI("hasLifetime"), null);
		//LocalDbCalls.execPrintStatement(LocalDbCalls.createNoviURI("slice_888"), null, null);
		//LocalDbCalls.execPrintStatement(null, null, LocalDbCalls.createNoviURI("Lifetime"));

		//look again for that resources
		Topology topol = irmCalls.findLocalResources(IrmCallsTest.testCreateTopology());
		assertEquals(topol.getContains().size(), 0);

	}*/
	

}
