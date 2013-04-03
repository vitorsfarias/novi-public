package eu.novi.ponder2.interactions;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.novi.policy.interfaces.interfaceforponder2;


public class CallstoIRM {
	private static final transient Logger log = 
			LoggerFactory.getLogger(CallstoIRM.class);
	private static interfaceforponder2 policyServiceCalls;
	
	
		
	
	public Collection<String> updateSliceinIRM(String currentTopology,Collection<String> failingResources) {
		
		if (policyServiceCalls == null)
		{
			log.warn("The object from policy service is null");
			return null;
		}
		else
		{
		//failingResources.add("sliver1");
		//RequestToIRM req= new RequestToIRM();
		log.info("Calling policy manager to initiate the updateSlice");
		Collection<String> failed=policyServiceCalls.callUpdateSliceFP(currentTopology, failingResources);
		log.info("DONE the update: CallstoIRM");
		//log.info(failed.toString());
		//log.info("DONE the update: CallstoIRM");
		return failed;
		}
	}
	
	public static void initThePonder()
	{
		if (policyServiceCalls == null)
		{
			log.warn("The object from policy service is null");
		}
		else
		{
	    log.info("Calling policy manager to initiate the init of Ponder2");
		policyServiceCalls.initPonder();
		log.info("DONE the init");
		}
	}
	
	public interfaceforponder2 getPolicyServiceCalls() {
		return policyServiceCalls;
	}
	public void setPolicyServiceCalls(interfaceforponder2 policyServiceCalls) {
		this.policyServiceCalls = policyServiceCalls;
	}	

}
