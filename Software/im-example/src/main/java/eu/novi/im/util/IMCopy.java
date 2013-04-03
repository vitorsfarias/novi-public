package eu.novi.im.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.DiskImage;
import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Link;
import eu.novi.im.core.Location;
import eu.novi.im.core.LoginComponent;
import eu.novi.im.core.LoginService;
import eu.novi.im.core.Memory;
import eu.novi.im.core.MemoryService;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.NSwitchService;
import eu.novi.im.core.NetworkElement;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
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
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.DiskImageImpl;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.LocationImpl;
import eu.novi.im.core.impl.LoginComponentImpl;
import eu.novi.im.core.impl.LoginServiceImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.MemoryServiceImpl;
import eu.novi.im.core.impl.NSwitchImpl;
import eu.novi.im.core.impl.NSwitchServiceImpl;
import eu.novi.im.core.impl.NetworkElementImpl;
import eu.novi.im.core.impl.NodeComponentImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.ProcessingServiceImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.ServiceImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.core.impl.StorageServiceImpl;
import eu.novi.im.core.impl.SwitchingMatrixImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualLinkImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
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
import eu.novi.im.policy.impl.AuthorizationPolicyImpl;
import eu.novi.im.policy.impl.ECAPolicyImpl;
import eu.novi.im.policy.impl.ManagedEntityImpl;
import eu.novi.im.policy.impl.ManagedEntityMethodImpl;
import eu.novi.im.policy.impl.ManagedEntityPropertyImpl;
import eu.novi.im.policy.impl.ManagementDomainImpl;
import eu.novi.im.policy.impl.MissionConrollerImpl;
import eu.novi.im.policy.impl.MissionInterfaceImpl;
import eu.novi.im.policy.impl.MissionPolicyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.policy.impl.PolicyActionImpl;
import eu.novi.im.policy.impl.PolicyConditionImpl;
import eu.novi.im.policy.impl.PolicyEventImpl;
import eu.novi.im.policy.impl.PolicyImpl;
import eu.novi.im.policy.impl.RBACPolicyImpl;
import eu.novi.im.policy.impl.RoleImpl;
import eu.novi.im.unit.IPAddress;
import eu.novi.im.unit.impl.IPAddressImpl;

/**
 * A class that make the transformation of the objects created by 
 * alibaba object factory to the implemented java objects
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class IMCopy {

	private static final transient Logger log = LoggerFactory.getLogger(IMCopy.class);

	private boolean enable_logs = false;

	private static final Map<Class<?>, Class<?>> classMap = new HashMap<Class<?>, Class<?>>();
	static {
		//CORE IM
		classMap.put(Resource.class, ResourceImpl.class);
		classMap.put(VirtualNode.class, VirtualNodeImpl.class);
		classMap.put(Node.class, NodeImpl.class);
		classMap.put(Location.class, LocationImpl.class);
		classMap.put(Interface.class, InterfaceImpl.class);
		classMap.put(Path.class, PathImpl.class);
		classMap.put(Topology.class, TopologyImpl.class);
		classMap.put(CPU.class, CPUImpl.class);
		classMap.put(DiskImage.class, DiskImageImpl.class);
		classMap.put(Group.class, GroupImpl.class);
		classMap.put(Lifetime.class, LifetimeImpl.class);
		classMap.put(Link.class, LinkImpl.class);
		classMap.put(LoginComponent.class, LoginComponentImpl.class);
		classMap.put(LoginService.class, LoginServiceImpl.class);
		classMap.put(Memory.class, MemoryImpl.class);
		classMap.put(MemoryService.class, MemoryServiceImpl.class);
		classMap.put(NetworkElement.class, NetworkElementImpl.class);
		classMap.put(NodeComponent.class, NodeComponentImpl.class);
		classMap.put(NSwitch.class, NSwitchImpl.class);
		classMap.put(Platform.class, PlatformImpl.class);
		classMap.put(ProcessingService.class, ProcessingServiceImpl.class);
		classMap.put(Reservation.class, ReservationImpl.class);
		classMap.put(Service.class, ServiceImpl.class);
		classMap.put(Storage.class, StorageImpl.class);
		classMap.put(StorageService.class, StorageServiceImpl.class);
		classMap.put(SwitchingMatrix.class, SwitchingMatrixImpl.class);
		classMap.put(NSwitchService.class, NSwitchServiceImpl.class);
		classMap.put(VirtualLink.class, VirtualLinkImpl.class);

		//POLICY IM
		classMap.put(AuthorizationPolicy.class, AuthorizationPolicyImpl.class);
		classMap.put(ECAPolicy.class, ECAPolicyImpl.class);
		classMap.put(ManagedEntity.class, ManagedEntityImpl.class);
		classMap.put(ManagedEntityMethod.class, ManagedEntityMethodImpl.class);
		classMap.put(ManagedEntityProperty.class, ManagedEntityPropertyImpl.class);
		classMap.put(ManagementDomain.class, ManagementDomainImpl.class);
		classMap.put(MissionPolicy.class, MissionPolicyImpl.class);
		classMap.put(NOVIUser.class, NOVIUserImpl.class);
		classMap.put(PolicyAction.class, PolicyActionImpl.class);
		classMap.put(PolicyCondition.class, PolicyConditionImpl.class);
		classMap.put(PolicyEvent.class, PolicyEventImpl.class);
		classMap.put(Policy.class, PolicyImpl.class);
		classMap.put(MissionConroller.class, MissionConrollerImpl.class);
		classMap.put(MissionInterface.class, MissionInterfaceImpl.class);
		classMap.put(RBACPolicy.class, RBACPolicyImpl.class);
		classMap.put(Role.class, RoleImpl.class);
		
		//monitoring unit IM
		classMap.put(IPAddress.class, IPAddressImpl.class);
		



	};

	private Map<String, Object> createdObjects;

	/**get a NOVI IM object (can be complex topology with interconnected objects)
	 * that is created by Alibaba and transfer it to the implemented NOVI IM objects
	 * @param sourceObject
	 * @param recursive set the recursive depth. ie.If it 0 then all the novi IM objects
	 * that are connected to this object will be set to null. If you give negative
	 * number then you don't specify the depth, and all the objects will be created
	 * @return the transformation to the NOVI IM core.impl java classes
	 */
	public Object copy(Object sourceObject, int recursive)
	{
		if (enable_logs)
			log.info("STARTING THE COPY FOR THE RESOURCE {}, RECURSIVE {}",
					sourceObject.toString(), recursive);
		createdObjects = new HashMap<String, Object>();
		return copyInternal(sourceObject, recursive);


	}

	/**the internal copy function
	 * @param sourceObject
	 * @return
	 */
	private Object copyInternal(Object sourceObject, int recursive)
	{

		if (enable_logs)
			log.info("######## Start #############");

		if (sourceObject == null)
		{
			if (enable_logs)
				log.warn("The object you gave as input is null");
			return null;
		}
		if (enable_logs)
			log.info("I got the object {}", sourceObject.toString());


		Class<?> sourceClass = getSourceClass(sourceObject);
		if (sourceClass == null)
		{
			if (enable_logs)
				log.warn("I can not find the class of the object {}", sourceObject.toString());
			return null;
		}

		if (enable_logs)
			log.info("The class of the object {} is: {}", sourceObject.toString(), sourceClass.toString());
		if (createdObjects.containsKey(sourceObject.toString()))
		{
			if (enable_logs)
				log.info("The object {} was created before", sourceObject.toString());
			return createdObjects.get(sourceObject.toString());
		}
		//get the target class
		Class<?> targetClass = getImplementedClass(sourceClass);
		if (targetClass == null)
		{
			log.warn("I can not transform the object {} to an implemented class", sourceObject.toString());
			return null;
		}
		if (enable_logs)
			log.debug("I found the target class {}, for the source class {}", targetClass.toString(), sourceClass.toString());

		//create the target object 
		Constructor<?> constr = null;
		Object targetObject = null;
		try {
			constr = targetClass.getConstructor(String.class);
			//give as input the URI
			targetObject = constr.newInstance(sourceObject.toString());
			if (targetObject != null)
			{//add the object to the map, for loop checking
				createdObjects.put(targetObject.toString(), targetObject);
				if (enable_logs)
					log.debug("I added the object {}", targetObject.toString());
			}
		} catch (SecurityException e1) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e1.printStackTrace();
			return null;
		} catch (NoSuchMethodException e1) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e1.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			if (enable_logs)
				log.error("IM Copy: error in creating the target object for the {}", sourceObject.toString());
			e.printStackTrace();
			return null;
		}


		Method[] targetMethods = targetClass.getMethods();
		Method[] sourceMethods = sourceClass.getMethods();
		//set the set methods of the target object
		for (Method mySetterMethod : targetMethods) {
			////log.debug("Check the method {}", mySetterMethod.getName());
			String setterName = mySetterMethod.getName();
			if (setterName.startsWith("set")) {
				if (enable_logs)
					log.debug("Setting the set method : {}", mySetterMethod.getName());
				// find matching getter
				for (Method myGetterMethod : sourceMethods) {
					String getterName = myGetterMethod.getName();
					if (getterName.equals("get"+(setterName.substring(3)))) {
						//I found the source get method for the target set method
						if (enable_logs)
							log.debug("Match!" + setterName +", " + getterName);
						try {
							Object result = myGetterMethod.invoke(sourceObject);
							if (result != null) {
								if (enable_logs)
									log.debug("The results of the {} is {}", 
											myGetterMethod.getName(), result.toString());
								if (result instanceof Set) {
									Set<?> setResult = (Set<?>) result;
									Iterator<?> iter = setResult.iterator();
									if (iter.hasNext())
									{
										Object firstObject = iter.next();
										Class<?> setClass = getSourceClass(firstObject);
										if (setClass == null)
										{//the Set is not contain  NOVI IM objects


											// copy the objects (e.g. Set<String>)
											if (setResult.size() > 0)
											{//the set is not empty

												if (enable_logs)
													log.info("The Set is not NOVI IM object set for {}", 
															myGetterMethod.getName());
												Object firstCloneObject = cloneNotIMObject(firstObject);
												if (firstCloneObject == null)
												{
													if (enable_logs)
														log.warn("I can not clone the object. I will link to the given set");
													if (enable_logs)
														log.info("Calling {}, arguments : {}", mySetterMethod.getName(), result.toString());
													mySetterMethod.invoke(targetObject, result);

												}
												else
												{//clone all the objects in the Set
													Set<Object> newSet = new HashSet<Object>();
													for (Object obj : setResult) {

														if (enable_logs)
															log.info("Clone not IM object {}", obj.toString());
														Object newObject = cloneNotIMObject(obj);
														if (newObject != null)
														{
															newSet.add(newObject);
														}
														else
														{
															if (enable_logs)
																log.warn("The returning object is null");
														}

													}

													if (!newSet.isEmpty())
													{
														if (enable_logs)
															log.info("Calling {}, arguments : {}",
																	mySetterMethod.getName(), newSet.toString());
														mySetterMethod.invoke(targetObject, newSet);

													}

												}

											}
											else
											{//the set is empty
												if (enable_logs)
													log.debug("The Set of the method {} is empty", mySetterMethod.getName());
											}

										}
										else
										{//the Set contain NOVI IM objects

											//call recursive (e.g. Set<NodeComponent>)
											if (recursive != 0)
											{
												Set<Object> newSet = new HashSet<Object>();
												for (Object obj : setResult) {


													if (enable_logs)
														log.info("Calling recursive...");
													int redRecurs = recursive - 1;
													Object newObject = copyInternal(obj, redRecurs);

													if (enable_logs)
														log.info("########## Return back ##############  to {}",
																sourceObject.toString());
													if (newObject != null)
													{
														newSet.add(newObject);
													}
													else
													{
														if (enable_logs)
															log.warn("The returning object is null");
													}

												}

												if (!newSet.isEmpty())
												{
													if (enable_logs)
														log.info("Calling {}, arguments : {}",
																mySetterMethod.getName(), newSet.toString());
													mySetterMethod.invoke(targetObject, newSet);

												}

											}
											else
											{
												if (enable_logs)
													log.info("The recursive is 0. " +
															"I will not create the Set of NOVI IM objects");

											}



										}
									}
									else
									{
										if (enable_logs)
											log.debug("The set is empty. I will not copy it");
									}


								}//end of Set if 
								else {
									if (getSourceClass(result) == null )
									{//is not a novi IM object
										if (enable_logs)
											log.info("Calling {}, arguments {}",
													mySetterMethod.getName(), result.toString());
										mySetterMethod.invoke(targetObject, result);
									}
									else
									{//the object is NOVI IM object

										if (enable_logs)
											log.info("The {} is novi IM object.",	result.toString());
										if (recursive != 0)
										{
											if (enable_logs)
												log.info(" Calling recursive...");
											int redRecurs = recursive - 1;
											Object newResult = copyInternal(result, redRecurs);
											if (newResult != null)
											{
												if (enable_logs)
													log.info("########### Return back #############  to {}",
															sourceObject.toString());
												if (enable_logs)
													log.info("I got the new novi IM implemented object {}",
															newResult.toString());
												mySetterMethod.invoke(targetObject, newResult);

											}
											else
											{
												if (enable_logs)
													log.warn("The NOVI object that I got back for the {} is null",
															result.toString());

											}

										}
										else
										{
											if (enable_logs)
												log.info("The recursive is 0. " +
														"I will not create the NOVI IM object");

										}

									}
								}//end of if not set
							}
							else
							{
								if (enable_logs)
									log.debug("The results of the {} is null", 
											myGetterMethod.getName());

							}
						} catch (IllegalArgumentException e) {
							if (enable_logs)
								log.warn("Problem calling {}, or {} method in the object " + sourceObject.toString(),
										myGetterMethod.getName(), mySetterMethod.getName());
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							if (enable_logs)
								log.warn("Problem calling {}, or {} method in the object " + sourceObject.toString(),
										myGetterMethod.getName(), mySetterMethod.getName());
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							if (enable_logs)
								log.warn("Problem calling {}, or {} method in the object " + sourceObject.toString(),
										myGetterMethod.getName(), mySetterMethod.getName());
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}

		return targetObject;

	}

	/**get the implemented class of the given class
	 * @param sourceClass
	 * @return the implemented class or null
	 */
	private Class<?> getImplementedClass(Class<?> sourceClass)
	{
		Class<?> returnClass = classMap.get(sourceClass);
		if (returnClass == null)
		{
			if (enable_logs)
				log.warn("There is no implemented class for the class : {}", sourceClass.toString());
		}
		return returnClass;
	}



	protected Object cloneNotIMObject(Object object)
	{
		if (enable_logs)
			log.debug("Creating the non IM mobject {}", object.toString());
		if (object instanceof String)
		{
			return new String(object.toString());
		}
		if (object instanceof Float)
		{
			Float fl = (Float) object;
			return new Float(fl);
		}
		if (object instanceof Double)
		{
			Double fl = (Double) object;
			return new Double(fl);
		}
		if (object instanceof Integer)
		{
			Integer fl = (Integer) object;
			return new Integer(fl);
		}
		if (object instanceof BigInteger)
		{
			BigInteger fl = (BigInteger) object;
			return new BigInteger(fl.toString());
		}
		else
		{
			if (enable_logs)
				log.warn("I can not clone the object {}", object.toString());
			return null;
		}


	}



	/**it return the class (exact class not any parent class) of the source object,
	 * @param object 
	 * @return the class or null if the class was not found
	 */
	protected Class<?> getSourceClass(Object object)
	{

		Iterator<Entry<Class<?>, Class<?>>> it = classMap.entrySet().iterator();

		Class<?> succedClass = null;
		while (it.hasNext())
		{
			Entry<Class<?>, Class<?>> pair = it.next();
			Class<?> currentClass = pair.getKey();

			if (currentClass.isInstance(object))
			{
				if (succedClass == null)
				{//the first matched class
					succedClass = currentClass;
				}
				else
				{
					if (succedClass.isAssignableFrom(currentClass))
					{//currentClass is subclass of succedClass
						succedClass = currentClass;
					}

				}
			}
		}

		return succedClass;


	}
	
	/**
	 * enable the logs
	 */
	public void enableLogs()
	{
		this.enable_logs = true;
	}
	
	/**
	 * disable the logs
	 */
	public void disableLogs()
	{
		this.enable_logs = false;
	}

}
