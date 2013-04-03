package eu.novi.policysender.requestToIRM;
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
 * 
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.mapping.IRMInterface;
import eu.novi.policysender.requestToIRM.RequestToIRM;

public class RequestToIRMTest {
	
	//IRMInterface MockIRM = mock(IRMInterface.class);
	private static final transient Logger log = LoggerFactory.getLogger(RequestToIRMTest.class);
	
/*	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing UpdateSliceTest...");
		RequestToIRM reqIRM= new RequestToIRM();
		reqIRM.setIrmMapperInterface(MockIRM);
		when(MockIRM.updateSlice(anyString(), anySet())).thenReturn(updateMockSlice());
	}*/
	@Test
	public void testupdateSlice() {
		RequestToIRM requestUpdate = new RequestToIRM();
		String SliceID="Slice0";
		Collection<String> failingResources = new HashSet<String>();
		failingResources.add("Node1null");
		requestUpdate.callUpdateSliceFP(SliceID,failingResources);
		//Collection<String> response = requestUpdate.callUpdateSliceFP(SliceID,failingResources);
		//System.out.println(response);
		//assertEquals("policy is null",response,1); 	
	}

}
