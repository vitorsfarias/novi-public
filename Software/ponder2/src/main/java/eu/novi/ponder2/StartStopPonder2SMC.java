package eu.novi.ponder2;
import java.rmi.RMISecurityManager;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.ponder2.P2ObjectAdaptor.InstanceOperation;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.interactions.CallstoIRM;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.Event;
import eu.novi.ponder2.policy.StaticAuthPolicySearch;
//import eu.novi.resources.discovery.remote.discovery.Testbeds;
//import eu.novi.resources.discovery.util.Testbeds;

public class StartStopPonder2SMC implements SMCStartInterface{
	private static String testbed;
	private static final transient Logger log = LoggerFactory.getLogger(StartStopPonder2SMC.class);
		/**
		 * Initialize SMC
		 */
		public void start(){
		//	System.setProperty("java." );
			//System.setProperty("java.rmi.server.codebase", "..\\lib\\ponder2.jar c:\\Users\\Administrator\\Documents\\gitPSNC\\novi\\Software\\ponder2\\lib\\ponder2comms.jar");
			//System.setProperty("address", "rmi://localhost/MyPonderPL");
		/*	if (System.getSecurityManager()!=null){
				log.info("O Security Manager einai: "+System.getSecurityManager().toString());
			}
			else 
				{
				log.info("O Security Manager einai: NULLLLLLL");
				RMISecurityManager SecMan= new RMISecurityManager();
				System.setSecurityManager(SecMan);
				log.info("O Security Manager einai: "+System.getSecurityManager().toString());
				}
			log.info(System.getProperties().toString());*/
			//System.ge
			if (testbed == null)
			{
				log.warn("The testbed values is null.");
			}
			else if (testbed.contains("PlanetLab"))
			{
			String[] argsPL={"-multiple","-port","13570","-auth","allow","-boot","boot.p2,nodemo.p2,linkmo.p2,topologymo.p2,virtualnodemo.p2,users.p2,roles.p2,authpolicy.p2,talkPL.p2,platformmoPL.p2,bootmissionNOVI.p2,planetlabmission2.p2","-address","rmi://150.254.160.28:1113/Ponder2PL"};
			//,"-address","rmi://localhost/Ponder2PL"
			log.info("StartStopPonder2SMC.start(): Starting SelfManagedCell on PlanetLab side");
			SelfManagedCell.start(argsPL);
			try {
				log.info("Sleeping for 100 seconds to give time for the init Policy Manager");
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Calling Policy Manager for the init");
			CallstoIRM.initThePonder();
			}
			else if (testbed.contains("FEDERICA"))
			{
			String[] argsFED={"-multiple","-port","13570","-auth","allow","-boot","boot.p2,nodemo.p2,linkmo.p2,topologymo.p2,virtualnodemo.p2,roles.p2,users.p2,authpolicy.p2,talkFED.p2,platformmoFED.p2,bootmissionNOVI.p2,federicamission2.p2","-address","rmi://194.132.52.214:1113/Ponder2FED"};
			//,"-address","rmi://localhost/Ponder2FED"
			log.info("StartStopPonder2SMC.start(): Starting SelfManagedCell on FEDERICA side");
			SelfManagedCell.start(argsFED);
			}
			else
			{
				log.warn("The testbed values is {}. It is not accepted by Ponder2", testbed);
			}
		}
		
		public void stop(){
			log.info("StartStopPonder2SMC.stop(): Stop SelfManagedCell");
			//SelfManagedCell.exit();
		}
		
		
		/*public Node createNode(String aName, String aSlice)
		{
			String aPlatform="PlanetLab";
			Node newNode = new Node(aName,aPlatform,aSlice);
			return newNode;
			
		}*/
		
	/*	// Ponder2 call to inject an event
		operation.put("Ponder2.event:", new InstanceOperation() {

					@Override
					public P2Object call(P2Object thisObj, ManagedObject obj,
							P2Object source, String operation, P2Object... args)
							throws Ponder2Exception {
						P2Object event = args[0];
						if (event instanceof Event) {
							thisObj.getManagedObject().sendEvent((Event) event);
						} else
							throw new Ponder2ArgumentException(
									"Ponder2.event: Object is not of type Event - "
											+ event.getClass());
						return thisObj;
					}
				});*/

		public P2Object createEvent(P2Object thisObj, ManagedObject obj,
				P2Object source, String operation, P2Object... args)
				throws Ponder2Exception {
			P2Object event = args[0];
			if (event instanceof Event) {
				thisObj.getManagedObject().sendEvent((Event) event);
			} else
				throw new Ponder2ArgumentException(
						"Ponder2.event: Object is not of type Event - "
								+ event.getClass());
			return thisObj;
			
		}
		/**
		 *Stop the SMC service. 
		 */
		/*public void destroyDatabase()
		{
			SelfManagedCell.stop());
		}*/
		
		public Boolean searchauth(P2Object myP2Object, P2Object resource)
		{
			
			try {
				resource.operation(myP2Object, "access");
				return true;
				
			} catch (Ponder2AuthorizationException e) {
				return false;
			} catch (Ponder2Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("We shouldn't be here");
			return false;
			
			
			
		}

		public Boolean searchauth(AuthPolicyHolder holder, short pepType,
				P2Object subject, P2Object target, String action, char focus,
				P2Object[] args, P2Object result) {
			AuthPolicySearch aps = new StaticAuthPolicySearch();
			short auth = aps.search(holder, pepType, subject, target, action, focus, args, result);
			if (auth == AuthPolicySearch.AUTH)
			{
				return true;
			}
			if (auth ==AuthPolicySearch.NOTAUTH)
			{
				return false;
			}
			if (auth ==AuthPolicySearch.POL_NOT_DEFINED)
			{
				System.out.println("The policy is not defined so we supposed it is true");
				return true;
			}
			return null;
			
		}
		public String getTestbed() {
			return testbed;
		}

		public void setTestbed(String testbed) {
			this.testbed = testbed;
		}


		
}
