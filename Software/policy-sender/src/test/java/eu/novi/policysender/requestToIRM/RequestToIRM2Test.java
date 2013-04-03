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
import static org.junit.Assert.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

import eu.novi.im.core.Topology;
import eu.novi.mapping.RemoteIRM;
import eu.novi.policysender.requestToIRM.RequestToIRM2;
import eu.novi.resources.discovery.IRMCalls;
import eu.novi.resources.discovery.PolicyCalls;

public class RequestToIRM2Test {
	RemoteIRM MockIRM = mock(RemoteIRM.class);
	PolicyCalls MockRIS= mock(PolicyCalls.class);
	
	private static final transient Logger log = LoggerFactory.getLogger(RequestToIRM2Test.class);

	@Test
	@Ignore
	public void testUpdateMock() {
		Set<String> failingResources= new HashSet<String>();
		failingResources.add("planetlab1");	
		String currentTopology = "Topo1";
		System.out.println("test");
		Collection<String> res=RequestToIRM2.callUpdateSliceFP2(currentTopology, failingResources);
		System.out.println(res);
		//assertEquals("should be 1",res,1); 	
							
	}
	
	@Before
	public <T> void initialize() throws IOException {
		log.debug("Initializing UpdateSliceTest...");
		RequestToIRM2 reqIRM= new RequestToIRM2();
		reqIRM.setIrmMapperInterface(MockIRM);
		reqIRM.setPolicyCallsToRIS(MockRIS);
		when(MockIRM.updateSlice(anyString(),anyString(), anySet())).thenReturn(updateMockSlice());
		when(MockRIS.getNoviUser(anyString())).thenReturn(getMockUser());
	}
	

	private Collection<String> updateMockSlice() {
		// TODO Auto-generated method stub
		Collection<String> toreturn=new HashSet<String>();
		toreturn.add("resource1");
		toreturn.add("resource2");
		//return toreturn;
		return null;
	}
	
	private String getMockUser()
	{
		return "ykryftis@netmode.ece.ntua.gr";
	}
	
	
	

}
