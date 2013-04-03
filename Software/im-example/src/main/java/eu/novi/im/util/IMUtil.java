package eu.novi.im.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.repository.object.config.ObjectRepositoryConfig;
import org.openrdf.repository.object.exceptions.ObjectStoreConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.DiskImage;
import eu.novi.im.core.Group;
import eu.novi.im.core.GroupOrPath;
import eu.novi.im.core.GroupOrResource;
import eu.novi.im.core.Interface;
import eu.novi.im.core.InterfaceOrNode;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Location;
import eu.novi.im.core.LoginComponent;
import eu.novi.im.core.LoginComponentOrLoginService;
import eu.novi.im.core.LoginService;
import eu.novi.im.core.Memory;
import eu.novi.im.core.MemoryService;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.NSwitchService;
import eu.novi.im.core.NetworkElement;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.NodeOrService;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.ProcessingService;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Service;
import eu.novi.im.core.Storage;
import eu.novi.im.core.StorageService;
import eu.novi.im.core.SwitchingMatrix;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.policy.AuthorizationPolicy;
import eu.novi.im.policy.ECAPolicy;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.ManagedEntityMethod;
import eu.novi.im.policy.ManagedEntityProperty;
import eu.novi.im.policy.ManagementDomain;
import eu.novi.im.policy.MissionConroller;
import eu.novi.im.policy.MissionInterface;
import eu.novi.im.policy.MissionPolicy;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.Policy;
import eu.novi.im.policy.PolicyAction;
import eu.novi.im.policy.PolicyCondition;
import eu.novi.im.policy.PolicyEvent;
import eu.novi.im.policy.RBACPolicy;
import eu.novi.im.policy.Role;
import eu.novi.im.unit.IPAddress;
import eu.novi.im.unit.impl.IPAddressImpl;

/**
 * Contains utility functions that can help us to manipulate and process the IM
 * aliobaba java classes
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 * 
 */
public class IMUtil {

	static ObjectRepositoryConfig repConfig = null;
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(IMUtil.class);

	/**
	 * it creates a HashSet and it adds one value
	 * 
	 * @param t
	 *            the added value
	 * @return the HashSet object included the value t
	 */
	public static <T> Set<T> createSetWithOneValue(T t) {
		Set<T> returnSet = new HashSet<T>();
		returnSet.add(t);
		return returnSet;
	}

	/**
	 * return one value of the set
	 * 
	 * @param set
	 * @return the value or null if the set is empty
	 */
	public static <T> T getOneValueFromSet(Set<T> set) {
		if(set == null) return null;
		Iterator<T> it = set.iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}
	
	
	/**create a set containing monitoring unit IPAddress and set the given ipValue
	 * @param ipObject the name of the object, can have or not the UNIT base address
	 * @param ipValue the IP address
	 * @return a set containing this IPAddress object
	 */
	public static Set<IPAddress> createUnitIPAddressSet(String ipObject, String ipValue)
	{

		Set<IPAddress> set = new HashSet<IPAddress>();
		set.add(createUnitIPAddress(ipObject, ipValue));
		return set;
	}
	
	/**create a monitoring unit IPAddress and set the given ipValue
	 * @param ipObject the name of the object
	 * @param ipValue the IP address
	 * @return the IPAddress object
	 */
	public static IPAddress createUnitIPAddress(String ipObject, String ipValue)
	{
		IPAddress ip = new IPAddressImpl(ipObject);
		ip.setHasValue(ipValue);
		return ip;
	}

	public static ObjectRepositoryConfig getRepositoryConfig() {
		if (repConfig != null)
			return repConfig;
		else {
			repConfig = new ObjectRepositoryConfig();
			try {
				repConfig.addConcept(CPU.class, "http://fp7-novi.eu/im.owl#CPU");
				repConfig.addConcept(DiskImage.class, "http://fp7-novi.eu/im.owl#DiskImage");
				repConfig.addConcept(Group.class, "http://fp7-novi.eu/im.owl#Group");
				repConfig.addConcept(GroupOrPath.class, "http://fp7-novi.eu/im.owl#GroupOrPath");
				repConfig.addConcept(GroupOrResource.class, "http://fp7-novi.eu/im.owl#GroupOrResource");
				repConfig.addConcept(Interface.class, "http://fp7-novi.eu/im.owl#Interface");
				repConfig.addConcept(InterfaceOrNode.class, "http://fp7-novi.eu/im.owl#InterfaceOrNode");
				repConfig.addConcept(Lifetime.class, "http://fp7-novi.eu/im.owl#Lifetime");
				repConfig.addConcept(Link.class, "http://fp7-novi.eu/im.owl#Link");
				repConfig.addConcept(LinkOrPath.class, "http://fp7-novi.eu/im.owl#LinkOrPath");
				repConfig.addConcept(Location.class, "http://fp7-novi.eu/im.owl#Location");
				repConfig.addConcept(LoginComponent.class, "http://fp7-novi.eu/im.owl#LoginComponent");
				repConfig.addConcept(LoginComponentOrLoginService.class,
						"http://fp7-novi.eu/im.owl#LoginComponentOrLoginService");
				repConfig.addConcept(LoginService.class,"http://fp7-novi.eu/im.owl#LoginService");
				repConfig.addConcept(Memory.class, "http://fp7-novi.eu/im.owl#Memory");
				repConfig.addConcept(MemoryService.class, "http://fp7-novi.eu/im.owl#MemoryService");
				repConfig.addConcept(NetworkElement.class, "http://fp7-novi.eu/im.owl#NetworkElement");
				repConfig.addConcept(NetworkElementOrNode.class, 
						"http://fp7-novi.eu/im.owl#NetworkElementOrNode");
				repConfig.addConcept(Node.class, "http://fp7-novi.eu/im.owl#Node");
				repConfig.addConcept(NodeComponent.class, "http://fp7-novi.eu/im.owl#NodeComponent");
				repConfig.addConcept(NodeOrService.class, "http://fp7-novi.eu/im.owl#NodeOrService");
				repConfig.addConcept(NSwitch.class, "http://fp7-novi.eu/im.owl#NSwitch");
				repConfig.addConcept(Path.class, "http://fp7-novi.eu/im.owl#Path");
				repConfig.addConcept(Platform.class, "http://fp7-novi.eu/im.owl#Platform");
				repConfig.addConcept(ProcessingService.class, "http://fp7-novi.eu/im.owl#ProcessingService");
				repConfig.addConcept(Reservation.class, "http://fp7-novi.eu/im.owl#Reservation");
				repConfig.addConcept(Resource.class, "http://fp7-novi.eu/im.owl#Resource");
				repConfig.addConcept(Service.class, "http://fp7-novi.eu/im.owl#Service");
				repConfig.addConcept(Storage.class, "http://fp7-novi.eu/im.owl#Storage");
				repConfig.addConcept(StorageService.class, "http://fp7-novi.eu/im.owl#StorageService");
				repConfig.addConcept(SwitchingMatrix.class,	"http://fp7-novi.eu/im.owl#SwitchingMatrix");
				repConfig.addConcept(NSwitchService.class, "http://fp7-novi.eu/im.owl#NSwitchService");
				repConfig.addConcept(Topology.class, "http://fp7-novi.eu/im.owl#Topology");
				repConfig.addConcept(VirtualLink.class, "http://fp7-novi.eu/im.owl#VirtualLink");
				repConfig.addConcept(VirtualNode.class, "http://fp7-novi.eu/im.owl#VirtualNode");
				
				//policy IM
				repConfig.addConcept(AuthorizationPolicy.class, "http://fp7-novi.eu/NOVIPolicyService.owl#AuthorizationPolicy");
				repConfig.addConcept(ECAPolicy.class, "http://fp7-novi.eu/NOVIPolicyService.owl#ECAPolicy");
				repConfig.addConcept(ManagedEntity.class, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity");
				repConfig.addConcept(ManagedEntityMethod.class, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntityMethod");
				repConfig.addConcept(ManagedEntityProperty.class, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntityProperty");
				repConfig.addConcept(ManagementDomain.class, "http://fp7-novi.eu/NOVIPolicyService.owl#ManagementDomain");
				repConfig.addConcept(MissionConroller.class, "http://fp7-novi.eu/NOVIPolicyService.owl#MissionConroller");
				repConfig.addConcept(MissionInterface.class, "http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterface");
				repConfig.addConcept(MissionPolicy.class, "http://fp7-novi.eu/NOVIPolicyService.owl#MissionPolicy");
				repConfig.addConcept(NOVIUser.class, "http://fp7-novi.eu/NOVIPolicyService.owl#NOVIUser");
				repConfig.addConcept(Policy.class, "http://fp7-novi.eu/NOVIPolicyService.owl#Policy");
				repConfig.addConcept(PolicyAction.class, "http://fp7-novi.eu/NOVIPolicyService.owl#PolicyAction");
				repConfig.addConcept(PolicyCondition.class, "http://fp7-novi.eu/NOVIPolicyService.owl#PolicyCondition");
				repConfig.addConcept(PolicyEvent.class, "http://fp7-novi.eu/NOVIPolicyService.owl#PolicyEvent");
				repConfig.addConcept(RBACPolicy.class, "http://fp7-novi.eu/NOVIPolicyService.owl#RBACPolicy");
				repConfig.addConcept(Role.class, "http://fp7-novi.eu/NOVIPolicyService.owl#Role");
				
				//monitoring 
				repConfig.addConcept(IPAddress.class, "http://fp7-novi.eu/unit.owl#IPAddress");

				

				
			} catch (ObjectStoreConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return repConfig;
	}
	
	/**it set the reverse property. 
	 * @param newSet the new set that it will be assigned
	 * @param currentSet the existing set
	 * @param curObject the object that call the reverse, (use java this)
	 * @param reverseFunction the reverse function name, i.e. for the setIsSink is HasSource with out
	 * the get and set in front
	 */
	public static <A, R> void reverseProperty(Set<? extends A> newSet, Set<? extends A> currentSet, 
			R curObject, String reverseFunction) {


		//To make sure that this IsSource relation and link/hasSource it is synchronized
		//Whenever we set a new set here, 
		//we make sure the reverse is also filled with this value. Also it corrects the current set

		//fix the reverse property links of the old objects 
		if (currentSet != null)
		{//the set is not null, remove the links from the previous objects
			
			//System.out.println("Current set not null: " + currentSet.toString());
			for (A oldObj : currentSet)
			{
				//i.e. Set<Interface> interf = oldObj.getHasSource();
				Method getMethCur = null;
				try {
					getMethCur = oldObj.getClass().getMethod("get" + reverseFunction);
				} catch (SecurityException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							oldObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when getMethod for the previous objects");
					log.warn("SecurityException Message :{}", e.getMessage());
					continue;
				} catch (NoSuchMethodException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							oldObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when getMethod for the previous objects");
					log.warn("NoSuchMethodException Message :{}", e.getMessage());
					continue;
				}
				
				
				Set<R> oldR = null;
				try {
					oldR = (Set<R>)getMethCur.invoke(oldObj);
				} catch (IllegalArgumentException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							oldObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the previous objects");
					log.warn("IllegalArgumentException Message :{}", e.getMessage());
					continue;
				} catch (IllegalAccessException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							oldObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the previous objects");
					log.warn("IllegalAccessException Message :{}", e.getMessage());
					continue;
				} catch (InvocationTargetException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							oldObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the previous objects");
					log.warn("InvocationTargetException Message :{}", e.getMessage());
					continue;
				}
				
				if (oldR != null)
				{
					//remove the current object from the reverse property set of each one of the old objects
			
					//System.out.println("Remove from the old set : " + curObj.toString());
					oldR.remove(curObject);
				}
			}
		}

		//set the reverse property links for the new object
		if (newSet != null)
		{
			for(A newObj : newSet){
				//i.e Set<Interface> inter = newObj.getHasSource();
				Method getMethNew = null;
				try {
					getMethNew = newObj.getClass().getMethod("get" + reverseFunction);
				} catch (SecurityException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when getMethod for the current objects");
					log.warn("SecurityException Message :{}", e.getMessage());
					continue;
				} catch (NoSuchMethodException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when getMethod for the current objects");
					log.warn("NoSuchMethodException Message :{}", e.getMessage());
					continue;
				}
				Set<R> newR = null;
				try {
					newR = (Set<R>) getMethNew.invoke(newObj);
				} catch (IllegalArgumentException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the current objects");
					log.warn("IllegalArgumentException Message :{}", e.getMessage());
					continue;
				} catch (IllegalAccessException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the current objects");
					log.warn("IllegalAccessException Message :{}", e.getMessage());
					continue;
				} catch (InvocationTargetException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("problem when invoke the getMethod for the current objects");
					log.warn("InvocationTargetException Message :{}", e.getMessage());
					continue;
				}
				if(newR == null){
					newR = new HashSet<R>();
				}
				//add on the current set this new object
				newR.add(curObject);
				
				
				
				//i.e. newObj.setHasSource(inter);
				Method setMeth = null;
				try {
					setMeth = newObj.getClass().getMethod("set" + reverseFunction, Set.class);
				} catch (SecurityException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("Problem when getMethod set from the new objects");
					log.warn("SecurityException Message :{}", e.getMessage());
					continue;
				} catch (NoSuchMethodException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("Problem when getMethod set from the new objects");
					log.warn("NoSuchMethodException Message :{}", e.getMessage());
					continue;
				}
				
				
				try {
					//System.out.println("Calling lp : " + lp.toString() + ", : " + newR.toString()); 
					setMeth.invoke(newObj, newR);
				} catch (IllegalArgumentException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("Problem when invoke the set method from the new objects");
					log.warn("IllegalArgumentException Message :{}", e.getMessage());
					continue;
				} catch (IllegalAccessException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("Problem when invoke the set method from the new objects");
					log.warn("IllegalAccessException Message :{}", e.getMessage());
					continue;
				} catch (InvocationTargetException e) {
					log.warn("Problem in the reverse property of {}, function: {}, object: " + 
							newObj.toString(), curObject.toString(), reverseFunction);
					log.warn("Problem when invoke the set method from the new objects");
					log.warn("InvocationTargetException Message :{}", e.getMessage());
					continue;
				}

			}

		}


	}
}
