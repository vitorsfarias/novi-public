package eu.novi.ponder2;

import org.junit.Ignore;
import org.junit.Test;

import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.StaticAuthPolicySearch;

public class StartStopPonder2SMCTest {
	@Ignore
	@Test
	public void testsearchauth() {
	
		AuthPolicyHolder holder = new AuthPolicyHolder();
		short pepType = AuthorisationModule.PEP1;
		P2Object subject = P2Object.create();
		P2Object target = P2Object.create();
		String action = "*";
		P2Object[] args = new P2Object[] {};
		P2Object result =P2Object.create("Foo");
		char focus ='t';
		StartStopPonder2SMC smc= new StartStopPonder2SMC(); 
		Boolean authornot =smc.searchauth(holder, pepType, subject, target, action, focus, args, result);
		if (authornot == true)
		{
			System.out.println("OK it is auth as it should");
		}
		else {
			System.out.println("failure or the auth result is : " + authornot);
		}
	}
	@Ignore
	@Test
	public void testsearchauth2() {
		StartStopPonder2SMC smc= new StartStopPonder2SMC(); 
				System.out.println(smc.searchauth(new AuthPolicyHolder(),
				AuthorisationModule.PEP1, P2Object.create(), P2Object.create(),
				"*", 't', new P2Object[] {}, P2Object.create("Foo")));

	}

}
