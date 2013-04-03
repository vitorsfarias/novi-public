package eu.novi.ponder2.managedobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.policy.RequestToIRM.RequestToIRM2;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.StartStopPonder2SMC;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.interactions.CallstoIRM;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.queue.PolicyQueueProducer;

public class TopologyMO extends TopologyImpl implements ManagedObject {
	private static final transient Logger log = 
			LoggerFactory.getLogger(TopologyMO.class);
/*	PolicyQueueProducer policyProducer;
	
	public PolicyQueueProducer getPolicyProducer()
	{
		return policyProducer;
	}
	
	public void setPolicyProducer ( PolicyQueueProducer policyProducer)
	{
		this.policyProducer=policyProducer;
	}*/
	
	private boolean updatePermission=true;
	@Ponder2op("create:")
	public TopologyMO(String uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}
	
	@Ponder2op("remove:")
	public void TopologyMO(String uri) {
		try {
			this.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}
	
	/*@Ponder2op("search")
	public void search() {
		System.out.println("Searching");
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
		
		
	}*/

	@Ponder2op("getUpdatePermission:")
	 boolean getUpdatePermission(String vnode) {
		return this.updatePermission;
		}
	
	@Ponder2op("setUpdatePermission:status:")
	 void setUpdatePermission(String vnode,boolean status) {
		 this.updatePermission=status;
		}
	
	 @Ponder2op("topologyfailure:resource:")
	    public int topologyfailure(String uri, String resourceuri) {
	      	
		 //Should communicate with policy-Manager to execute the IRM-call
		 
		 Collection<String> failingResources= new HashSet<String>();
		 String currentTopology=uri;
		 String resourceuriV2=resourceuri.replace(uri,"");
		 failingResources.add(resourceuriV2);
		 System.out.println("In the topologyMO I am handling the failure");
		 log.info("In the topologyMO I am handling the failure");
		 log.info("The failure was in slice "+ uri +" for resource "+ resourceuriV2);
		 log.info("The updatePermission value is:"+this.updatePermission);
		 CallstoIRM callIRM=new CallstoIRM();
		 Collection<String> failed=callIRM.updateSliceinIRM(currentTopology ,  failingResources);
	     return 1;
		 /*if (failed!=null)
	    	 {
	    	 log.info("DONE the update");
	    	 return failed.size();
	    	 }
	     else 
	    	 {
	    	 log.info("DONE the update (No-success)");
	    	 return 0;
	    	 }
		 */
		 /*System.out.println("I sending a message with activemq to Policy-Manager to update the Slice");
	    	
	    	
	    	try {
				String queueProcessingID = policyProducer.pushRequest("fdskl");
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    	
	    }

}
