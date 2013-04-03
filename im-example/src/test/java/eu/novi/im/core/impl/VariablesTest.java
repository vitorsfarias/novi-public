package eu.novi.im.core.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.novi.im.policy.MissionPolicy;
import eu.novi.im.policy.Policy;
import eu.novi.im.policy.impl.MissionPolicyImpl;
import eu.novi.im.policy.impl.PolicyImpl;

public class VariablesTest {

	@Test
	public void test() {
		assertEquals(Variables.NOVI_IM_BASE_ADDRESS + "some_name", Variables.checkURI("some_name"));
		assertEquals(Variables.NOVI_IM_BASE_ADDRESS + "some_name2", 
				Variables.checkURI(Variables.NOVI_IM_BASE_ADDRESS + "some_name2"));
		
		assertEquals(Variables.NOVI_POLICY_BASE_ADDRESS + "some_name2", 
				Variables.checkPolicyURI("some_name2"));
		
		

	}
	
	@Test
	public void testURIAssign()
	{
		Policy poli = new PolicyImpl("myPolicy");
		assertTrue(poli.toString().equals("http://fp7-novi.eu/NOVIPolicyService.owl#myPolicy"));
		
		MissionPolicy miss = new MissionPolicyImpl("http://fp7-novi.eu/NOVIPolicyService.owl#missi");
		assertTrue(miss.toString().equals("http://fp7-novi.eu/NOVIPolicyService.owl#missi"));
		
	}

}
