package eu.novi.resources.discovery.database.communic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.novi.im.core.Reservation;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.policyAA.interfaces.InterfaceForRIS;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;


/**
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
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 *junit test for policy communication
 */
public class PolicyCommTest {
	PolicyServCommun policy ;
	
	@Before
	public void setUp() throws Exception {
		policy = new PolicyServCommun();
		InterfaceForRIS policyService = mock(InterfaceForRIS.class);
		policy.setPolicyServiceCalls(policyService);
		when(policyService.RemoveTopology(any(String.class), any(Topology.class), any(String.class))).
		thenReturn(0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullTop() {
		
		policy.deleteSlicePolicy(null, null, "uri");
	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullUri() {
		
		policy.deleteSlicePolicy(null, new ReservationImpl("uri"), null);
	
	}
	
	@Test
	public void test() {
		Reservation res = new ReservationImpl("uri");
		res.setContains(IMUtil.createSetWithOneValue(new VirtualNodeImpl("myVnode")));
		policy.deleteSlicePolicy(null, res, "uri");
	
	}


}
