package eu.novi.ponder2.managedobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.jms.JMSException;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.StartStopPonder2SMC;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.interactions.CallstoIRM;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.queue.PolicyQueueProducer;

public class PlatformMO extends PlatformImpl implements ManagedObject {
	private static final transient Logger log = LoggerFactory.getLogger(PlatformMO.class);
	private int NumberOfRemoteVNodesProvidedToRemote=0;
	private int NumberOfRemoteVNodesIgotFromRemote=0;
	@Ponder2op("create:")
	public PlatformMO(String PlatformName) {
		super(PlatformName);
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
	
	
	@Ponder2op("findpartitioningcost:")
	    public void findpartitioningcost(String topo) {
	      	
		 //Should communicate with policy-Manager to execute the IRM-call
		 
		 //Collection<String> failingResources= new HashSet<String>();
		 //String currentTopology=uri;
		 //failingResources.add(resourceuri);
		 System.out.println("I am trying to find the partitioning cost");
		 log.info("I am trying to find the partitioning cost");
		 log.info("At this point we must call RIS");
		// CallstoIRM.findpartitioningcost(topo);
		 
		 /*System.out.println("I sending a message with activemq to Policy-Manager to update the Slice");
	    	
	    	
	    	try {
				String queueProcessingID = policyProducer.pushRequest("fdskl");
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    	
	    }
	
	@Ponder2op("addToNumberOfRemoteVNodesIprovide")
    public void addToNumberOfRemoteVNodesIprovide() {
      	
	 System.out.println("I am providing a VNode to the remote Platform");
	 log.info("I am providing a VNode to the remote Platform");
	 log.info("Number of VNodes I provide: "+NumberOfRemoteVNodesProvidedToRemote);
	 NumberOfRemoteVNodesProvidedToRemote++;
	 log.info("Number of VNodes I provide: "+NumberOfRemoteVNodesProvidedToRemote);    	
    }
	
	@Ponder2op("addToNumberOfRemoteVNodesIgot")
	public void addToNumberOfRemoteVNodesIgot()
	{
		System.out.println("I am adding one to the number of RemoteVnodes I got from Remote Platform");
		log.info("I am adding one to the number of RemoteVnodes I got from Remote Platform");
		this.NumberOfRemoteVNodesIgotFromRemote++;
	}
	
	@Ponder2op("getquota")
	public int getquota()
	{
		log.info("We want to see the quota of the VNodes we can deliver to the other Platform");
		log.info("We have: "+ this.NumberOfRemoteVNodesProvidedToRemote +" of VNodes that we provided and: " + this.NumberOfRemoteVNodesIgotFromRemote + " of VNodes that we got.");
		int quota= this.NumberOfRemoteVNodesProvidedToRemote-this.NumberOfRemoteVNodesIgotFromRemote+100;
		log.info("So we have quota" + quota);
		return quota;
	}
	
	@Ponder2op("initMissions")
	public void initMissions()
	{
		log.info("Ponder2:Manually init Missions");
		CallstoIRM.initThePonder();
		log.info("Ponder2: Called init");
	}
	/*@Ponder2op("returnNumberOfRemoteVNodes")
    public int returnNumberOfRemoteVNodes() {
      	
	 System.out.println("I am returning the Number of Remote VNodes I am providing");
	 log.info("I am returning the Number of Remote VNodes.");
	 log.info("Number of VNodes: "+this.NumberOfRemoteVNodesProvidedToFED);
	 return this.NumberOfRemoteVNodesProvidedToFED;
    }*/
	
	

}
