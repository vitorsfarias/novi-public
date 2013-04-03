package eu.novi.resources.discovery.database;


import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.MemoryImpl;
import eu.novi.im.core.impl.StorageImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.util.IMCopy;
import eu.novi.resources.discovery.database.communic.MonitoringInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.util.NoviRisValues;
import eu.novi.resources.discovery.util.RisSystemVariables;

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
 * it finds the availability of the substrate resources and return it to NOVI API
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class UpdateAvailability {

	/**
	 * the cpu component for the monitoring cache is nodeURI + CPU_CACHE_SUFFIX
	 */
	protected static final String CPU_CACHE_SUFFIX = "-cpu";
	protected static final String MEMORY_CACHE_SUFFIX = "-mem";
	protected static final String STORAGE_CACHE_SUFFIX = "-sto";

	/**
	 * this is used when we want to store 
	 * the node monitoring data, from the monitoring service, as a cache.
	 * So the lifetime URI will be nodeURI + NODE_CACHE_SUFFIX
	 */
	private static final String NODE_CACHE_SUFFIX = "_monit_cache";

	/**
	 * the duration that the monitoring cache is valid in minutes.
	 * this is used only in the case where the periodic monitoring 
	 * update doesn't happen
	 */
	private final static int MONIT_CACHE_LIFETIME_MINUTES = 5;

	private static final transient Logger log = 
			LoggerFactory.getLogger(UpdateAvailability.class);

	/**
	 * for junit testing
	 */
	private Set<MonitoringInfo> monInfoTest = null;


	/**return all the physical nodes in the DB with the availability values, from monitoring
	 * @return a Set with the physical nodes and their availability
	 */
	public Set<Node> listResources()
	{
		return listResources(null);
	}


	/**
	 * it calls monitoring and it updates all the monitoring values in the DB, for servers 
	 */
	public void updateAllMonitoringValues()
	{
		log.info("Running the update all monitoring values...");
		//get all the servers, if in federica ignore routers becaouse we don't call monitoring
		//for routers
		Set<String> servers = new HashSet<String>(); //federica and plantlab servers
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection, I can not update the Mon values");
			return ; //an empty set
		}

		con.setReadContexts(NoviUris.getSubstrateURI());
		//get all the substrate physical nodes
		Result<Node> nodesTemp = null;
		try {
			nodesTemp = con.getObjects(Node.class);
			for (Node n : nodesTemp.asList())
			{
				if (!(n instanceof VirtualNode))
				{//virtual nodes are nodes
					log.debug("I found the physical node {}", n.toString());

					if (!NoviRisValues.isRouter(n.getHardwareType()))
					{
						log.debug("The machine {} is a physical server", n.toString());
						servers.add(n.toString());
					}


				}

			}
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		}

		ConnectionClass.closeAConnection(con);
		if (nodesTemp == null)
		{
			log.warn("I can not read the physical nodes from the DB");
			return ; //an empty set
		}
		else if (servers.isEmpty())
		{
			log.warn("There are no physical nodes in the DB");
			return ; //an empty set
		}

		log.info("I will update the monitoring values for the following servers: {}",
				servers.toString());
		//call monitoring and get the values
		//get the monitoring info for the servers
		Set<MonitoringInfo>  monValues = null;
		if (monInfoTest != null && MonitoringServCommun.isMonServiceNull())
		{
			log.warn("I am going to use ready data for monitoring values");
			monValues = monInfoTest; //FOR JUNIT TESTING

		}
		else
		{
			monValues = MonitoringServCommun.getNodesMonData(servers);
		}

		//update the monitoring values in the DB
		updateMonitoringValues(monValues);


	}


	/**return all the physical nodes for the given user with the availability values, from monitoring
	 * @param user if it is null, then it return all the nodes
	 * @return a Set with the physical nodes and their availability
	 */
	public Set<Node> listResources(NOVIUser user)
	{
		log.info("Getting the availability of physical resources");
		//Set<Node> allNodes = new HashSet<Node>(); //all the physical resources
		Set<Node> routers = new HashSet<Node>(); //federica routers
		Set<Node> servers = new HashSet<Node>(); //federica and plantlab servers
		Set<String> allNodeUris = new HashSet<String>();
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection...");
			return servers; //an empty set
		}

		con.setReadContexts(NoviUris.getSubstrateURI());
		//get all the substrate physical nodes
		Result<Node> nodesTemp = null;
		try {
			IMCopy copy = new IMCopy();
			nodesTemp = con.getObjects(Node.class);
			for (Node n : nodesTemp.asList())
			{
				if (!(n instanceof VirtualNode))
				{//virtual nodes are nodes
					log.debug("I found the physical node {}", n.toString());
					Node nodImp = (Node)copy.copy(n, 1);
					//allNodes.add(nodImp);
					allNodeUris.add(n.toString());
					if (NoviRisValues.isRouter(nodImp.getHardwareType()))
					{
						log.info("The machine {} is a federica router", nodImp.toString());
						routers.add(nodImp);
					}
					else 
					{
						log.info("The machine {} is a physical server", nodImp.toString());
						servers.add(nodImp);
					}

				}

			}
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		}

		if (nodesTemp == null)
		{
			log.warn("I can not read the physical nodes from the DB");
			ConnectionClass.closeAConnection(con);
			return servers; //an empty set
		}
		else if (servers.isEmpty() && routers.isEmpty())
		{
			log.warn("There are no physical nodes in the DB");
			ConnectionClass.closeAConnection(con);
			return servers; //an empty set
		}


		/////if a user is given , then call policy to get the authorize resources
		//check all the resources, routers and servers
		Set<Node> authorizedServers = new HashSet<Node>();
		Set<Node> authorizedRouters = new HashSet<Node>();
		if (user != null)
		{
			log.info("I will contact policy to get the authorized resources for the user {}", user);
			//Note: we actually need to pass sessionID to Policy instead of NULL
			Map<String, Boolean> policyAns = PolicyServCommun.getAuthorizedResources(null, user, allNodeUris, 0);
			if (policyAns == null)
			{
				log.warn("No response from policy. I will retrun back all the nodes");
				authorizedServers = servers;
				authorizedRouters = routers;
			}
			else
			{
				//check the servers
				for (Node nod : servers)
				{
					Boolean answer  = policyAns.get(nod.toString());
					if (answer == null)
					{
						log.warn("I have no anwer from policy for the node {}. " +
								"I will not include this node", nod.toString());
						answer = false;
					}
					else if (answer)
					{
						log.info("The resource {} is authorized for the user {}",
								nod.toString(), user.toString());
						authorizedServers.add(nod);

					}
					else
					{
						log.info("The resource {} is not authorized for the user {}",
								nod.toString(), user.toString());
					}
				}
				//check the routers
				for (Node rout : routers)
				{
					Boolean answer  = policyAns.get(rout.toString());
					if (answer == null)
					{
						log.warn("I have no anwer from policy for the router {}. " +
								"I will not include this router", rout.toString());
						answer = false;
					}
					else if (answer)
					{
						log.info("The router {} is authorized for the user {}",
								rout.toString(), user.toString());
						authorizedRouters.add(rout);

					}
					else
					{
						log.info("The router {} is not authorized for the user {}",
								rout.toString(), user.toString());
					}
				}
			}
		}
		else 
		{
			log.info("The user was not specified. I will get the availability for all the resources");
			authorizedServers = servers;
			authorizedRouters = routers;
		}

		Set<Node> results = new HashSet<Node>();
		if (RisSystemVariables.isUpdateMonValuesPeriodic())
		{
			log.info("The monitoring values are updated periodically. I will not call monitoring");
			ConnectionClass.closeAConnection(con);
			//return back the authorized routers and the authorized available servers
			results.addAll(authorizedRouters);
			results.addAll(removeOfflineNodes(authorizedServers));
			return results;

		}


		///////contact monitoring for the availability values of server
		Set<String> serversURIs = new HashSet<String>();
		Set<Node> authAvailServers = new HashSet<Node>(); //authorize and available servers
		//get the servers uris
		for (Node n: authorizedServers)
		{
			serversURIs.add(n.toString());
		}
		//get the monitoring info for the servers
		Set<MonitoringInfo> monInfo = null;
		if (monInfoTest == null )
		{
			monInfo = MonitoringServCommun.getNodesMonData(serversURIs);
		}
		else
		{
			log.warn("I am going to use ready data for monitoring values");
			monInfo = monInfoTest; //FOR JUNIT TESTING
		}

		for (Node node : authorizedServers)
		{
			boolean found = false;
			for (MonitoringInfo nodeInfo : monInfo)
			{
				if (nodeInfo.getNodeUri().equals(node.toString()))
				{
					log.debug("I found the monitoring info for the node {}", node.toString());
					found = true;
					if (updateNodeAvailability(node, nodeInfo, true))
					{
						log.warn("At least one value from monitoring was missing. I will ommit the " +
								"node {} from the results", node.toString());

					}
					else
					{
						authAvailServers.add(node);
					}
					break;
				}
			}

			if (!found)
			{
				log.warn("I did not find any monitoring info for the node {}", node.toString());
			}
		}
		ConnectionClass.closeAConnection(con);
		//return back the authorized routers and the authorized available servers
		results.addAll(authorizedRouters);
		results.addAll(removeOfflineNodes(authAvailServers));
		return results;
	}




	/**for each node, it query database and if it is offline (all the availability values are zero),
	 * then it remove it from the list
	 * @param servers
	 * @return a new list with no offline nodes
	 */
	protected static Set<Node> removeOfflineNodesQueries(Set<Node> servers)
	{
		log.info("I will check for offline servers: {}", servers.toString());
		Set<Node> notOffline = new HashSet<Node>();
		for (Node n : servers)
		{
			ConstructFindResQuery query = new ConstructFindResQuery(
					NoviUris.getSubstrateURI().toString());
			query.setCheck4OfflineNode(n.toString());
			query.finalizeQuery();
			if (query.execAskQuery())
			{
				log.warn("The node {} is offline, I will not include it", n.toString());
			}
			else
			{
				log.debug("The node {} is online", n.toString());
				notOffline.add(n);
			}
		}
		return notOffline;
	}
	
	/**for each node, it check the availability values, and if all of them are zero, 
	 * then it remove it from the list
	 * @param servers
	 * @return a new list with no offline nodes
	 */
	protected static Set<Node> removeOfflineNodes(Set<Node> servers)
	{
		log.info("I will check for offline servers: {}", servers.toString());
		Set<Node> online = new HashSet<Node>();
		for (Node n : servers)
		{
			Set<NodeComponent> nodComps = n.getHasComponent();
			if (nodComps == null)
			{
				log.warn("The server {} doens't have any components", n.toString());
			}
			else
			{
				for (NodeComponent nodCom : nodComps)
				{
					if (nodCom instanceof CPU)
					{
						if (((CPU)nodCom).getHasAvailableCores() != null 
								&& ((CPU)nodCom).getHasAvailableCores().intValue() != 0)
						{
							log.debug("The node {} is online", n.toString());
							online.add(n);
							break;
							
						}
					}
					else if (nodCom instanceof Memory)
					{
						if (((Memory)nodCom).getHasAvailableMemorySize() != null &&
								((Memory)nodCom).getHasAvailableMemorySize() != 0)
						{
							log.debug("The node {} is online", n.toString());
							online.add(n);
							break;
							
						}
						
					}
					else if (nodCom instanceof  Storage)
					{
						if (((Storage)nodCom).getHasAvailableStorageSize() != null &&
								((Storage)nodCom).getHasAvailableStorageSize() != 0)
						{
							log.debug("The node {} is online", n.toString());
							online.add(n);
							break;
							
						}
						
					}
					
				}// end for components
				
			}
			
			
			if (!online.contains(n))
			{
				log.warn("The node {} is offline, I will not include it", n.toString());
			}
			
			
		}
		
		return online;
	}



	/**For each node in the monitoringInfo,
	 * It updates the monitoring values in the DB using the information in the monitoringInfo. 
	 * Also it updates the cache lifetimes information
	 * @param monitoringInfo
	 */
	protected static void updateMonitoringValues(Set<MonitoringInfo> monitoringInfo)
	{
		log.info("Updating monitoring values and lifetimes...");
		ObjectConnection con = ConnectionClass.getNewConnection();
		//set the contexts
		con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
		//all the information from monitoring will be stored, in the 
		//monitoring cache with the testbed context
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);
		for (MonitoringInfo nodeInfo : monitoringInfo)
		{
			String nodeURI = nodeInfo.getNodeUri();
			log.info("Updating monitoring cache for: {}", nodeURI);
			//Node node = IRMLocalDbCalls.getNodefromDB(nodeURI, con,
			//ManipulateDB.TESTBED_CONTEXTS_STR);
			Node node = null;
			try {
				node = con.getObject(Node.class, nodeURI);
			} catch (RepositoryException e1) {
				log.warn(e1.getMessage());
			} catch (QueryEvaluationException e1) {

				log.warn(e1.getMessage());
			}
			catch (ClassCastException e)
			{
				log.warn(e.getMessage());
			}

			if (node == null)
			{
				log.warn("I can not find the node {} in the DB. " +
						"I can not update the monitoring cache", nodeURI);
				continue;
			}


			try {
				log.debug("Removing old cache lifetimes...");
				//remove all the monitoring cache lifetime, nodeURI + NODE_CACHE_SUFFIX
				//IRMLocalDbCalls.execPrintStatement( //for testing
				//		LocalDbCalls.createURI(nodeURI + NODE_CACHE_SUFFIX), null, null);
				con.remove(NoviUris.createURI(nodeURI + NODE_CACHE_SUFFIX), null, null);
				//IRMLocalDbCalls.execPrintStatement( //for testing
				//		LocalDbCalls.createURI(nodeURI + NODE_CACHE_SUFFIX), null, null);

				//remove the node has lifetime nodeURI + NODE_CACHE_SUFFIX
				con.remove(NoviUris.createURI(nodeURI), NoviUris.createNoviURI("hasLifetime"),
						NoviUris.createURI(nodeURI + NODE_CACHE_SUFFIX));
				log.debug("The old cache lifetime was removing\n");



			} catch (RepositoryException e) {
				log.error("Problem removing old cache in updateMonCache");
				ConnectionClass.logErrorStackToFile(e);

			}


			try {

				//store the new lifetime
				IRMLocalDbCalls.storeLifetimeObjectInDb(
						nodeURI + NODE_CACHE_SUFFIX, 0, 0, MONIT_CACHE_LIFETIME_MINUTES, con);
				IRMLocalDbCalls.execPrintStatement( //for testing
						NoviUris.createURI(nodeURI + NODE_CACHE_SUFFIX), null, null, true);
				con.add(NoviUris.createURI(nodeURI), NoviUris.createNoviURI("hasLifetime"),
						NoviUris.createURI(nodeURI + NODE_CACHE_SUFFIX));
				log.debug("The new lifetime was stored");

				//update the monitoring value ,  you have a node and a nodeInfo
				updateNodeAvailability(node, nodeInfo, false);

				/////////TESTING/////////////
				/*IRMLocalDbCalls.execPrintStatement( //for testing
						LocalDbCalls.createURI(nodeURI + CPU_CACHE_SUFFIX), null, null);
				IRMLocalDbCalls.execPrintStatement( //for testing
						LocalDbCalls.createURI(nodeURI + MEMORY_CACHE_SUFFIX), null, null);
				IRMLocalDbCalls.execPrintStatement( //for testing
						LocalDbCalls.createURI(nodeURI + STORAGE_CACHE_SUFFIX), null, null);
				IRMLocalDbCalls.execPrintStatement( //for testing
						LocalDbCalls.createURI(nodeURI), null, null);*/

			} catch (RepositoryException e) {
				log.error("Repository exception: problem storing in db in updateMonCache function");
				ConnectionClass.logErrorStackToFile(e);

			}



		}
		ConnectionClass.closeAConnection(con);


	}

	/**using the information from the nodeInfo, it updates the availability values of the node.
	 * it doesn't update the lifetimes
	 * @param node the node to be updated
	 * @param nodeInfo the availability values of the node, taken from monitoring
	 * @param onTheFly if true then it doesn't store anything in the DB. i.e. when it creates
	 * not existing components of the node
	 * @return true if at least one value from monitoring is missing, otherwise false
	 */
	protected static boolean updateNodeAvailability(Node node, MonitoringInfo nodeInfo, boolean onTheFly)
	{
		log.info("Updating the availability for the node {}", node.toString());
		log.info("Values from monitoring : cpu cores: {}, cpu speed: {} GHz, free memory: "
				+ nodeInfo.getMemory() + " GB, free storage: " + nodeInfo.getStorage() + " GB",
				nodeInfo.getCpuCores(), nodeInfo.getCpuSpeed());
		//update the cache ,  you have a node and a nodeInfo

		//if true then, for at least one value we did not get answer from monitoring
		boolean missingAnswer = false; 
		Set<NodeComponent> nodeComponents = node.getHasComponent();
		if (nodeComponents == null)
			nodeComponents = new HashSet<NodeComponent>();
		boolean cpuCheck = true;
		boolean memoryCheck = true;
		boolean storageCheck = true;
		if (nodeComponents.size() != 0)
		{
			//update the values of the cpu, memory and storage components
			for (NodeComponent comp : nodeComponents)
			{
				if (comp instanceof CPU)
				{
					log.debug("Updating available cpu cores, value: {}", nodeInfo.getCpuCores());
					if (nodeInfo.getCpuCores() == -1)
					{
						missingAnswer = true;
						log.warn("I have no CPU cores answer from monitoring. " +
								"I will set the value to 0");
						((CPU) comp).setHasAvailableCores(BigInteger.valueOf(0));
					}
					else
					{
						((CPU) comp).setHasAvailableCores(BigInteger.valueOf(nodeInfo.getCpuCores()));
						log.debug("Updating cpu cores, value: {}", nodeInfo.getCpuCores());

					}

					if (nodeInfo.getCpuSpeed() == -1)
					{
						missingAnswer = true;
						log.warn("I have no CPU speed answer from monitoring. " +
								"I will set the value to 0");
						((CPU) comp).setHasCPUSpeed(0f);
					}
					else
					{
						((CPU) comp).setHasCPUSpeed(nodeInfo.getCpuSpeed());
						log.debug("Updating cpu speed, value in GHz: {}", nodeInfo.getCpuSpeed());

					}
					cpuCheck = false;

				}
				else if (comp instanceof Memory)
				{
					log.debug("Updating available memory size, memory value in GB: {}",
							nodeInfo.getMemory() );
					if (nodeInfo.getMemory() == -1)
					{
						missingAnswer = true;
						log.warn("I have no memory answer from monitoring, I will set it to 0");
						((Memory) comp).setHasAvailableMemorySize(0f);
					}
					else
					{
						((Memory) comp).setHasAvailableMemorySize(nodeInfo.getMemory());
					}
					memoryCheck = false;
				} 
				else if (comp instanceof Storage)
				{
					log.debug("Updating available storage size, disc space value in GB: {}",
							nodeInfo.getStorage());
					if (nodeInfo.getStorage() == -1)
					{
						missingAnswer = true;
						log.warn("I have no storage answer from monitoring. I will set it to 0");
						((Storage) comp).setHasAvailableStorageSize(0f);

					}
					else
					{
						((Storage) comp).setHasAvailableStorageSize(nodeInfo.getStorage());
					}
					storageCheck = false;

				}
				else
				{
					log.warn("The node component {}, is not recognized", comp.toString());
				}

			}
		}


		// create and store the components that are missing
		String nodeURI = nodeInfo.getNodeUri();


		ObjectConnection con = ConnectionClass.getNewConnection();
		//set the contexts
		con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
		//all the information from monitoring will be stored, in the 
		//monitoring cache with the testbed context
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);

		try {

			//CPU
			if (cpuCheck)
			{
				log.info("Create new CPU component");
				////////CPU//////////////////
				//CPU myCPU = ConnectionClass.getObjectFactory().createObject(
				//		LocalDbCalls.createURI(nodeURI + CPU_CACHE_SUFFIX), CPU.class);
				CPU myCPU = new CPUImpl(nodeURI + CPU_CACHE_SUFFIX);

				if (nodeInfo.getCpuCores() != -1)
				{

					log.info("Setting the available cpu cores, value: {}", nodeInfo.getCpuCores());
					myCPU.setHasAvailableCores(BigInteger.valueOf(nodeInfo.getCpuCores()));
				}
				else 
				{
					missingAnswer = true;
					log.warn("No cpu cores answer from monitoring. I will set the value to 0");
					myCPU.setHasAvailableCores(BigInteger.valueOf(0));
				}

				if (nodeInfo.getCpuSpeed() != -1)
				{
					log.info("Setting the cpu speed, value in GHz: {}", nodeInfo.getCpuSpeed());
					myCPU.setHasCPUSpeed(nodeInfo.getCpuSpeed());
				}
				else
				{
					missingAnswer = true;
					log.warn("No cpu speed answer from monitoring. I will set the value to 0");
					myCPU.setHasCPUSpeed(0f);
				}

				if (!onTheFly)
					con.addObject(NoviUris.createURI(nodeURI + CPU_CACHE_SUFFIX), myCPU);

				nodeComponents.add(myCPU);
				//myCPU.setHasCores(IMUtil.createSetWithOneValue(BigInteger.valueOf(8)));
				//myCPU.setHasAvailableCores(IMUtil.createSetWithOneValue(BigInteger.valueOf(8)));



			}

			//Memory
			if (memoryCheck)
			{
				log.info("Create new memory component");
				/////////Memory Component///////////
				//Memory myMemory = ConnectionClass.getObjectFactory().createObject(
				//		LocalDbCalls.createURI(nodeURI + MEMORY_CACHE_SUFFIX), Memory.class);
				Memory myMemory = new MemoryImpl(nodeURI + MEMORY_CACHE_SUFFIX);

				if (nodeInfo.getMemory() == -1)
				{
					missingAnswer = true;
					log.warn("No free memory answer from monitoring. I will set the value to 0");
					myMemory.setHasAvailableMemorySize(0f);
				}
				else
				{
					log.info("Setting the AvailableMemorySize, memory value in GB: {}",
							nodeInfo.getMemory());
					myMemory.setHasAvailableMemorySize(nodeInfo.getMemory());
				}
				//myMemory.setHasAvailableMemorySize(IMUtil.createSetWithOneValue(100f));
				if (!onTheFly)
					con.addObject(myMemory);
				nodeComponents.add(myMemory);
			}



			//Storage
			if (storageCheck)
			{
				log.info("Create new storage component");
				//////////Storage component///////
				//Storage storage = ConnectionClass.getObjectFactory().createObject(
				//		LocalDbCalls.createURI(nodeURI + STORAGE_CACHE_SUFFIX), Storage.class);
				Storage storage = new StorageImpl(nodeURI + STORAGE_CACHE_SUFFIX);

				if (nodeInfo.getStorage() == -1)
				{
					missingAnswer = true;
					log.warn("No storage answer from monitoring. I will set the value to 0");
					storage.setHasAvailableStorageSize(0f);
				}
				else
				{
					log.info("Setting the AvailableStorageSize, disc space value in GB: {}",
							nodeInfo.getStorage());
					storage.setHasAvailableStorageSize(nodeInfo.getStorage());

				}

				//storage.setHasAvailableMemorySize(IMUtil.createSetWithOneValue(100f));
				if (!onTheFly)
					con.addObject(storage);
				nodeComponents.add(storage);
			}



		} catch (RepositoryException e) {
			log.warn("Problem to store the node components: {}", e.getMessage());
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);
		node.setHasComponent(nodeComponents);
		//con.commit();
		log.debug("The  CPU, memory and storage components were updated");

		return missingAnswer;

	}


	/**for junit testing. 
	 * set the monitoring data in the case of junit tests
	 * @param monInfoTest
	 */
	protected void setMonInfo(Set<MonitoringInfo> monInfoTest)
	{
		this.monInfoTest = monInfoTest;
	}


	/**return all the physical nodes in the DB
	 * @return a Set with the physical nodes 
	 */
	public Set<Node> listAllResources(){

		Set<Node> nodes = new HashSet<Node>();
		ObjectConnection con = ConnectionClass.getNewConnection();
		if (con == null)
		{
			log.warn("Unable to get a new connection...");
			return nodes;
		}

		con.setReadContexts(NoviUris.getSubstrateURI());
		//get all the substrate physical nodes
		Result<Node> nodesTemp = null;
		try {
			IMCopy copy = new IMCopy();
			nodesTemp = con.getObjects(Node.class);
			for (Node n : nodesTemp.asList())
			{
				if (!(n instanceof VirtualNode))
				{//virtual nodes are nodes
					log.debug("I found the physical node {}", n.toString());
					nodes.add((Node)copy.copy(n, 1));
				}

			}
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		}
		ConnectionClass.closeAConnection(con);

		return nodes;
	}


	/**
	 * 	 * this is used when we want to store 
	 * the node monitoring data, from the monitoring service, as a cache.
	 * So the lifetime URI will be nodeURI + NODE_CACHE_SUFFIX
	 * @return
	 */
	protected static String getNodeCacheSuffix()
	{
		return NODE_CACHE_SUFFIX;

	}
}
