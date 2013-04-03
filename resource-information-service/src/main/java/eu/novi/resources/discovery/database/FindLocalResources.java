package eu.novi.resources.discovery.database;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Node;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.util.IMUtil;

import eu.novi.resources.discovery.database.communic.MonitoringInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.response.FRFailedMess;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.FRResponseImp;
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
 * It implement the find local resources call
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FindLocalResources {
	

	private static final transient Logger log = 
			LoggerFactory.getLogger(FindLocalResources.class);
	
	

	//contain the requested Resource (key) and the available
	//resources (value) for that resource
	private Map<Resource, Set<Resource>> succeedResources = 
			new HashMap<Resource, Set<Resource>>();

	//contain all the failed resources. (was not found any available resources).
	//Every Resource (key) has a value the failed message
	private Map<Resource, FRFailedMess> failedResources = 
			new HashMap<Resource, FRFailedMess>();
	
	private String userFeedback = "";
	//shortcut to the connection
	private ObjectConnection con;
	
	//private PolicyServCommun policyService; //get from IRMCallsImpl
	
	private NOVIUser noviUser; //the novi user asking the request
	private int numberOfReqResources = 0; //the number of request that the request contain
	
	/**
	 * if something goes wrong set it true
	 */
	private boolean hasError = false;
	
	
	
	
	
	/**find the local resources that fill the requirements
	 * @param requestTop -- the topology contain the requested resources
	 * @param noviUser the NOVI user asking the request
	 * @return a FRResponce object contain the find resources results
	 */
	public FRResponse findLocalResources(final Topology requestTop,  NOVIUser noviUser)
	{
		this.noviUser = noviUser;
		con = ConnectionClass.getNewConnection();
		//set the contexts
		con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
		//all the information from monitoring will be stored, in the 
		//monitoring cache with the testbed context
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);
		
		Set<Resource> resources = requestTop.getContains();
		for (Resource resource : resources)
		{
			if (resource instanceof VirtualNode)
			{
				numberOfReqResources++; //increase the number of requested resources
				//process virtual node
				processVirtualNode((VirtualNode) resource, null);
			}
			else 
			{
				log.warn("The resource: {} is not acceptable for the find resources request",
						resource);
			}
		}
		
		FRResponse response = constructResponce();
		//reset the contexts
		//ConnectionClass.getConnection2DB().setAddContexts();
		//ConnectionClass.getConnection2DB().setReadContexts();
		ConnectionClass.closeAConnection(con);
		return response;
		
	}
	

	/**find new local available resources for some failed resources in an existing slice
	 * @param slice the reservation group contain information about the slice
	 * @param failedMachinesURIs the URIs of the physical machine that are failed
	 * @return a FRResponce object contain the find resources results.
	 * The results are only for the virtual
	 * machines that was failed before
	 */
	public FRResponse findLocalResourcesUpdate(final Reservation slice, 
			Set<String> failedMachinesURIs)
	{
		log.info("Running findLocalResourcesUpdate for slice {}...", slice.toString());
		

		//all the physical machines in the slice
		Set<String> allPhysicalMachinesURIs = new HashSet<String>();
		//the virtual machines that have failed physical machines implement by
		Set<Resource> failedVirNodes = new HashSet<Resource>();
		if (slice.getContains() == null || slice.getContains().isEmpty())
		{
			log.warn("The slice topology doesn't contain anything");
			hasError = true;
			userFeedback = "The slice topology doesn't contain anything";
			FRResponse response = constructResponce();
			return response;
		}
		
		Iterator<Resource> resources = slice.getContains().iterator();
		con = ConnectionClass.getNewConnection();
		//set the contexts
		con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
		//all the information from monitoring will be stored, in the 
		//monitoring cache with the testbed context
		con.setAddContexts(ManipulateDB.TESTBED_CONTEXTS);
		//find all the physical machines in the slice
		//and the virtual machines that failed
		while (resources.hasNext())
		{
			Resource res = resources.next();
			if (res instanceof VirtualNode)
			{
				log.info("The resources {} is a virtual node", res.toString());
				Set<Node> boundNodes = 
						((VirtualNode) res).getImplementedBy();
				if (boundNodes == null || boundNodes.isEmpty())
				{
					log.warn("The virtual node {}, in the slice {} does not have implement by node",
							res.toString(), slice.toString());
					continue;
				}

				for (Node boundNode : boundNodes)
				{
					//a physical machine that implement a virtual node in the slice
					allPhysicalMachinesURIs.add(boundNode.toString()); 
					log.debug("The virtual node {} has implement by {}",
							res.toString(), boundNode.toString());
					if (failedMachinesURIs.contains(boundNode.toString()))
					{
						log.info("I need to find new available resources for virtual node {}",
								res.toString());
						//a virtual machines that have failed physical machines implement by
						Set<Node> netNod = new HashSet<Node>();
						//we don't want the case to be consider as bound
						((VirtualNode) res).setImplementedBy(netNod); 
						failedVirNodes.add(res);
					}
					else
					{
						log.info("The virtual node {} doesn't need remapping.", res.toString());
					}
				}

			}
			else
			{
				log.warn("The resource {} is not a virtual node", res.toString());
			}
		} //end of while

		//find new resources for the failed virtual machines
		for (Resource r : failedVirNodes)
		{
			processVirtualNode( (VirtualNode) r, allPhysicalMachinesURIs);
		}
		
		//reset the contexts
		//ConnectionClass.getConnection2DB().setAddContexts();
		//ConnectionClass.getConnection2DB().setReadContexts();
		this.noviUser = NOVIUserClass.getNoviUserSlice(slice.toString());
		FRResponse response = constructResponce();
		ConnectionClass.closeAConnection(con);
		return response;
	}
	
	
	/**it take a virtual node that is in request topology and it find 
	 *the available resources for that.
	 * @param vNode the Virtual Node
	 * @param exceptNodes physical machines that you want to be except from the results.
	 * Null if it is not applicable
	 */
	protected void processVirtualNode(final VirtualNode vNode, Set<String> exceptNodes)
	{
		//the physical machine using functional characteristics
		//that is available for the virtual node
		int excNodNumber = 0;
		if (exceptNodes != null)
			excNodNumber = exceptNodes.size();
		log.info("I will proccess the find resource request for the virtual node: {}.\n" +
				"There are {} nodes that will be excepted from the results",
				vNode.toString(), excNodNumber);
		if (exceptNodes != null)
			for (String s : exceptNodes)
				log.debug(s);

		Set<String> physicalMachines = findPhysicalMachinesUsingFunctChar(vNode, exceptNodes);
		if (physicalMachines == null)
			return;

		//you have to get the availability of the nodes in physicalMachines list,
		//and update the DB
		if (physicalMachines.size() == 0)
		{
			log.warn("Using functional characteristics, none physical machine was found for the virtual node {}. " +
					"I will not proceed for that virtual node", vNode.toString());
			failedResources.put(vNode, FRFailedMess.WAS_NOT_FOUND_ANY_AVAILABLE_NODE_FUNCTIONAL);
			return;
		}
		log.debug("I have to use the cache or call monitoring service for {} Nodes",
				physicalMachines.size());
		for (String s : physicalMachines)
		{
			log.debug(s);
		}

		if (RisSystemVariables.isUpdateMonValuesPeriodic())
		{
			log.info("The monitoring values are updated periodically. I will not call monitoring");
		}
		else 
		{
			//call monitoring
			////////////////check the cache first/////////////////

			//to be sent to monitoring
			Set<String> needUpdate = checkCacheNodesAvail(physicalMachines);
			if (!needUpdate.isEmpty())
			{
				log.info("I have to get the monitoring data, from monitoring service, " +
						"for the nodes below :");
				for (String n : needUpdate)
				{
					log.info(n);
				}
				///////////////contact monitoring service and get the mon info/////////
				Set<MonitoringInfo> monInfo = MonitoringServCommun.getNodesMonData(needUpdate);

				///update the monitoring cache
				UpdateAvailability.updateMonitoringValues(monInfo);
			}
			else
			{
				log.info("The cache is up-to-date. It doesn't need to call monitoring service");
			}
		}

		//the db is now up-to-date with the availability of the nodes
		////////make a query using all the constrains/////////////////////
		Node boundNode = IMUtil.getOneValueFromSet(vNode.getImplementedBy());
		String nodeUri;
		if (boundNode == null)
			nodeUri = null;
		else 
			nodeUri = boundNode.toString();
		//make the final query and update the lists succeedResources and failedResources
		findAvailResForVNodeInDB(vNode, nodeUri, exceptNodes);
		
	}
	
	
	


	/**using the functional characteristics of a virtual node, or the
	 * bound node if exist, it looks in the DB for appropriate nodes (physical machines).
	 * It doesn't take into account non functional characteristics like cpu, memory...
	 * If a problem occur it update the failedResources list
	 * @param vNode the virtual node
	 * @param exceptNodes physical machines that you want to be except from the results.
	 * Null if it is not applicable. If it is not null then we consider the case unbound
	 * @return a list of the URI of the appropriate nodes, or null if a problem occur and
	 * the vNod is added in the failedResources
	 */
	private Set<String> findPhysicalMachinesUsingFunctChar(final VirtualNode vNode,
			Set<String> exceptNodes)
	{
		//the physical machine using functional characteristics
		//that is available for the virtual node
		Set<String> physicalMachines = new HashSet<String>();

		Node boundNode =  IMUtil.getOneValueFromSet(vNode.getImplementedBy());
		//if the exceptNodes list is not empty then we consider the case as unbound
		if (boundNode == null || exceptNodes != null)
		{ /////////proceed with unbound request//////////////////
			log.info("Find Resources: The resource {} is unbound", vNode.toString());
			//make a query using the functional characteristics of the request
			ConstructFindResQuery query = new ConstructFindResQuery(1, 
					ManipulateDB.TESTBED_CONTEXTS_STR);
			query.setRdfType(1, "Node");
			query.setFunctionalChar(1, vNode);
			query.setExceptNodes(1, exceptNodes);
			query.finalizeQuery();
			//log.debug("sum {}", query.execQueryPrintResults());
			try {
				log.debug("Executing query...");
				query.printQuery(true);
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query.getQuery());
				TupleQueryResult result = tupleQuery.evaluate();
				List<String> bindingNames = result.getBindingNames();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value value = bindingSet.getValue(bindingNames.get(0));
						physicalMachines.add(value.stringValue());
					}
				}
				finally {
					result.close();
				}
			}
			catch (OpenRDFException e) {
				log.warn("Problem executing a query in find local resource :");
				log.warn(query.getQuery());
				ConnectionClass.logErrorStackToFile(e);
			}

		}
		else
		{ ///////////proceed with bound request///////////////

			if (!(boundNode instanceof Node))
			{
				log.warn("The bound object {} is not Node. I can't procceed the request " +
						"for virtual node {}", boundNode.toString(), vNode.toString());
				failedResources.put(vNode, FRFailedMess.BOUND_NODE_INVALID);
				return null;
			}

			log.info("Find Resources: The resource {} is bound. The bound node is : {}",
					vNode.toString(), boundNode.toString());

			int i = IRMLocalDbCalls.execStatementReturnSum(
					NoviUris.createURI(boundNode.toString()),
					NoviUris.createRdfURI("type"),
					NoviUris.createNoviURI("Node"));

			if (i == 0)
			{
				log.warn("Find Resources: I can not find in the DB the Node {}\n." +
						"I can't procceed the request for virtual node {}",
						boundNode.toString(), vNode.toString());
				failedResources.put(vNode, FRFailedMess.BOUND_NODE_WAS_NODE_FOUND);
				return null;

			}

			physicalMachines.add(boundNode.toString());
		}
		
		return physicalMachines;
	}// end of findPhysicalMachinesUsingFunctChar


	/**make a query to database using the functional, non functional (cpu, memory...)
	 * characteristics and the bound requirement if exist.
	 * Add the results to the succedResources or failedResources list
	 * @param vNode the virtual node contain the requirement
	 * @param boundNode the bound node or null if doesn't exist
	 * @param exceptNodes physical machines that you want to be except from the results.
	 * Null if it is not applicable
	 */
	private void findAvailResForVNodeInDB(final VirtualNode vNode, String boundNode,
			Set<String> exceptNodes)
	{
		//construct the query
		ConstructFindResQuery query = new ConstructFindResQuery(1, 
				ManipulateDB.TESTBED_CONTEXTS_STR);
		query.setRdfType(1, "Node"); //looking for nodes
		query.setFunctionalChar(1, vNode);
		if (boundNode != null)
			query.setBoundConstrain(1, boundNode);
		query.setExceptNodes(1, exceptNodes);
		// TODO check if the cpu memory is empty set the minimal values
		query.setNodeComponents(1, vNode.getHasComponent());
		query.finalizeQuery();
		//execute the query
		try {
			log.info("Executing query...");
			query.printQuery(false);
			ObjectQuery objQuery = con.prepareObjectQuery(query.getQuery());
			Result<Node> nodes = objQuery.evaluate(Node.class);

			if (!nodes.hasNext())
			{ //was not found any resources
				log.warn("Using the functional and non-functinal characteristics, was not found any " +
						"available resources for node {}", vNode.toString());
				failedResources.put(vNode, FRFailedMess.WAS_NOT_FOUND_ANY_AVAILABLE_NODE_NON_FUNCTIONAL);
				UserFeedback feedback = new UserFeedback();
				userFeedback = feedback.createFeedback(
						findPhysicalMachinesUsingFunctChar(vNode, exceptNodes), vNode);
				return;
			}
			
			//resources were found
			List<Node> nodesList = nodes.asList();
			log.info("Were found {} available resources for virtual node {}",
					nodesList.size(), vNode.toString());
			Set<Resource> foundResources = new HashSet<Resource>();
			for (Node n : nodesList)
			{
				foundResources.add(n);
				log.debug("The node {} was found", n.toString());

			}
			succeedResources.put(vNode, foundResources);
			log.info("The resources were succesfully added to the list");

		}
		catch (QueryEvaluationException e)
		{
			log.warn("Problem evaluating a query in find local resource");
			ConnectionClass.logErrorStackToFile(e);
			failedResources.put(vNode, FRFailedMess.ERROR_EXECUTING_QUERY);
		}
		catch (OpenRDFException e) {
			log.warn("Problem executing a query in find local resource");
			ConnectionClass.logErrorStackToFile(e);
			failedResources.put(vNode, FRFailedMess.ERROR_EXECUTING_QUERY);
		}
		
	}
	
	
	
	/**it checks for the set of nodes if the cache about the monitoring data is
	 * valid or not
	 * @param nodes -- the nodes to check
	 * @return return the nodes that the cache is NOT valid
	 */
	protected Set<String> checkCacheNodesAvail(final Set<String> nodes)
	{

		//the node that the cache in the db is not valid
		Set<String> invalidCacheNodes =  new HashSet<String>();

		for (String node : nodes)
		{
			if (!checkCacheNodeAvail(node))
				invalidCacheNodes.add(node);
		}

		return invalidCacheNodes;

	} //end of checkCacheNodesAvail
	
	
	
	/**it checks for that nodes if the cache about the monitoring data is
	 * valid or not. It checks the monitoring cache lifetime for that node
	 * @param node -- the node URI to check
	 * @return true if the cache is valid otherwise false
	 */
	protected boolean checkCacheNodeAvail(final String node)
	{
		log.info("Checking the monitoring cache for node {}", node.toString());
		String queryString = 
				LocalDbCalls.PREFIXES +
				"SELECT ?lifetime where { \n" +
				" ?lifetime rdf:type im:Lifetime .\n" +
				"<" + node + ">" + " im:hasLifetime ?lifetime  .\n" +
				" FILTER regex(str(?lifetime), \"" + UpdateAvailability.getNodeCacheSuffix() + "\") . }\n ";

		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = con.prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return false;
		} catch (RepositoryException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return false;
		}

		Result<Lifetime> lifetimes;
		try {
			lifetimes = query.evaluate(Lifetime.class);
			if (!lifetimes.hasNext())
			{
				log.info("I can not find cache lifetimes for node {}", node);
				return false;
			}

			boolean valid = false;
			while (lifetimes.hasNext())
			{
				Lifetime current = lifetimes.next();
				log.debug("I found the cache lifetime {}", current);
				if (IRMLocalDbCalls.checkIfLifetimeIsValid(current))
				{
					log.debug("The lifetime is valid");
					valid = true;
				}
				else
				{
					log.debug("The lifetime is not valid");
				}
			}

			if (!valid)
			{
				log.info("The cache is not valid");
				return false;
			}

		} catch (QueryEvaluationException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return false;
		} catch (ClassCastException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return false;
		}
		log.info("The cache is valid for node: {}", node);
		return true;

	} //end of checkCacheNodeAvail
	
	
	/**construct the responce from the results of the
	 * current find resources call
	 * @return
	 */
	private FRResponse constructResponce()
	{
		FRResponseImp response = new FRResponseImp();
		response.setFailedResources(failedResources);
		//responce.setSucceedResources(succeedResources);
		response.setUserFeedback(userFeedback);
		
		if (hasError)
		{
			log.warn("There was an error in the findResources/findResourcesUpdate");
			return response;
			//the topology in the response is null, so the hasError return true
		}
		
		
		Set<Resource> returnResources = new HashSet<Resource>();
		if (!failedResources.isEmpty())
		{
			log.warn("There are failed resources, however I will create the return topology" +
					" for findResources. The failed resources are : \n{}", failedResources.toString());
			//return response;

		}


		Iterator it = succeedResources.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry)it.next();
			returnResources.addAll((Set<Resource>) pairs.getValue());

		}


		//////REMOVE THE OFFLINE NODES
		Set<Node> servers = new HashSet<Node>();
		Set<Resource> notServers = new HashSet<Resource>();
		for (Resource r : returnResources)
		{
			if (r instanceof Node)
			{
				
				if (!NoviRisValues.isRouter(((Node) r).getHardwareType()))
				{
					log.debug("The resource {} is a server", r.toString());
					servers.add((Node) r);
					
				}
				else
				{
					log.debug("The resource {} is not a server", r.toString());
					notServers.add(r);
				}
				
			}
			else
			{
				log.debug("The resource {} is not a server", r.toString());
				notServers.add(r);
				
			}
		}
		//reset the list and add them again
		returnResources = new HashSet<Resource>();
		returnResources.addAll(notServers);
		returnResources.addAll(UpdateAvailability.removeOfflineNodes(servers));

		///call policy service
		Set<String> resources2Check = new HashSet<String>();
		for (Resource r : returnResources)
		{
			resources2Check.add(r.toString());
		}
		

		Map<String, Boolean> policyAnswer = PolicyServCommun.
				getAuthorizedResources(null, noviUser, resources2Check, numberOfReqResources);
		if (policyAnswer == null)
		{
			log.warn("The policy Service answer is null, I will consider all resources as authorized");
			response.setTopology(returnResources);
			return response;
		}
		else
		{
			Set<Resource> validResources = new HashSet<Resource>();
			for (Resource r : returnResources)
			{
				Boolean answer = policyAnswer.get(r.toString());
				if (answer == null)
				{
					log.warn("I don't have answer from policy for the resource : {}." +
							"I will add it to the response", r.toString());
					validResources.add(r);
				}
				else
				{
					if (answer)
					{
						log.info("The resources {} is authorized", r.toString());
						validResources.add(r);
					}
					else
					{
						log.info("The resource {}, is not authorized", r.toString());
					}
				}
			}
			response.setTopology(validResources);
			return response;
		}




	}

	
	/**
	 * for Junit testing.
	 * start the connection
	 */
	protected void startConnection()
	{
		con = ConnectionClass.getNewConnection();
		con.setAddContexts( ManipulateDB.getTestbedContextURI());
		con.setReadContexts( ManipulateDB.getTestbedContextURI());
	}
	
	/**
	 * for junit testing
	 * stop the connection
	 */
	protected void stopConnection()
	{
		ConnectionClass.closeAConnection(con);
	}
	////////////////////////////////////////////////////////////////
	////////////GETTERS AND SETTERS////////////////////////////////
	////////////////////////////////////////////////////////////////
	
	protected int getNumberOfReqResources()
	{
		return numberOfReqResources;
	}

	
	
	/*public PolicyServCommun getPolicyService() {
		return policyService;
	}


	public void setPolicyService(PolicyServCommun policyService) {
		this.policyService = policyService;
	}*/



}
