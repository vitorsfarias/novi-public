package eu.novi.policysender.requestToRIS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.resources.discovery.PolicyCalls;

public class RequestToInit {
	private static PolicyCalls policyCallsToRIS;
	private static final transient Logger log = 
			LoggerFactory.getLogger(RequestToInit.class);
	public static void InitPolicy()
	{
		if (policyCallsToRIS==null)
		{
			log.warn("The object from RIS service is null");
		}
		else
		{
			log.info("Policy Manager is sending the query to RIS to init the database in Ponder2");
			policyCallsToRIS.initPolicy();		
		}
	}
	public PolicyCalls getPolicyCallsToRIS() {
		return policyCallsToRIS;
	}

	public void setPolicyCallsToRIS(PolicyCalls policyCallsToRIS) {
		this.policyCallsToRIS = policyCallsToRIS;
	}

	
}
