package eu.novi.resources.discovery.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.resources.discovery.database.communic.MonitoringAvarInfo;
import eu.novi.resources.discovery.database.communic.MonitoringServCommun;
import eu.novi.resources.discovery.response.FPartCostRecordImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.util.NoviRisValues;


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
 * It implement the find Partitioning Cost for the IRM but only for the
 * local testbed
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FindLocalPartitioningCost {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(FindLocalPartitioningCost.class);
	
	private FPartCostTestbedResponseImpl fPartCostResponse;
	/**
	 * key is the query and the value is the resources uri
	 */
	private Map<String, String> resourceTypes = new HashMap<String, String>();
	
	private ObjectConnection con;

	
	/**
	 * @param testbed the name of the testbed that the current ris run
	 */
	public FindLocalPartitioningCost(String testbed)
	{
		fPartCostResponse = new FPartCostTestbedResponseImpl(); 
		fPartCostResponse.setTestbedURI(NoviUris.createNoviURI(testbed).toString());
	}
	
	
	/**the find local partitioning cost for the local testbed.
	 * @param requestedTopology the unbound topology
	 * @return an FPartCostTestbedResponse object that contain the costs
	 * for all the virtual nodes and links in the topology
	 */
	public FPartCostTestbedResponseImpl findLocalPartitioningCost(Topology requestedTopology)
	{
		log.info("Running the findLocalPartitioningCost...");
		if (requestedTopology == null || requestedTopology.getContains() == null
				|| requestedTopology.getContains().isEmpty())
		{
			log.warn("Local partitionig cost :  the requested topology is null or empty");
			return fPartCostResponse;
			
		}
		con = ConnectionClass.getNewConnection();
		//set the contexts
		con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
		
		Set<Resource> resources = requestedTopology.getContains();
		for (Resource resource : resources)
		{
			if (resource instanceof VirtualNode)
			{
				log.debug("FLPC: the resource {} is virtual node", resource.toString());
				//process virtual node
				processVirtualNode((VirtualNode) resource);
			}
			else if (resource instanceof Link)
			{
				log.debug("FLPC: the resource {} is link", resource.toString());
				//process link
				processLink((Link) resource);
			}
			else
			{
				log.warn("The resource: {} is not acceptable for the find partitioning cost request",
						resource.toString());
			}
		}
		//reset the contexts
		ConnectionClass.closeAConnection(con);
		return fPartCostResponse;
		
	}
	

	/**it check if there is already a node/link with the same 
	 * functional characteristics. If exist then it adds the new 
	 * virtual node URI to the existing FPartCostRecord
	 * @param query the functional characteristics query of the node
	 * @param uri the URI of the new node
	 * @return true if the type already exist otherwise false
	 */
	private boolean checkIfTypeExist(String query, String uri)
	{
		if (resourceTypes.containsKey(query))
		{
			log.debug("The node/link {} is the same type as the node/link {}", 
					uri, resourceTypes.get(query));
			fPartCostResponse.addNewURI(resourceTypes.get(query), uri);
			return true;
			
		}
		//add the new type to the list
		resourceTypes.put(query, uri);
		return false;
	}
	
	
	protected void processVirtualNode(VirtualNode vNode)
	{
		FPartCostRecordImpl virtualNodeCost = new FPartCostRecordImpl();
		virtualNodeCost.addURI(vNode.toString());
		log.info("FLPC: processing virtual node {}", vNode.toString());
		//make a query using the functional characteristics of the request
		ConstructFindResQuery query = new ConstructFindResQuery(1, 
				ManipulateDB.TESTBED_CONTEXTS_STR);
		query.setRdfType(1, "Node");
		query.setFunctionalChar(1, vNode);
		query.finalizeQuery();
		log.debug("Set the hardware type {}", vNode.getHardwareType());
		virtualNodeCost.setHardwType(vNode.getHardwareType());
		if (checkIfTypeExist(query.toString(), vNode.toString()))
		{//this hardware type already exist
			//just add the virtual node uri to the exissting record
			return ;
		}

		
		
		Set<String> physicalNodes = query.execQueryPrintGetResults();
		if (physicalNodes == null)
		{
			virtualNodeCost.setError(true);
			log.warn("Problem executing a query in find partitioning cost for virtual node {}", vNode.toString());
			fPartCostResponse.setNodeCosts(virtualNodeCost);
			return ;
			
		}
		else if (physicalNodes.size() == 0)
		{
			log.info("None node is found for this resource {}, hardware type: {}.",
					vNode.toString(), vNode.getHardwareType());
			fPartCostResponse.setNodeCosts(virtualNodeCost);
			return ;
		}
		else
		{
			
			log.info("{} nodes are found for this resource {}.",
					physicalNodes.size(), vNode.toString());
			virtualNodeCost.setPhysicalNodeURIs(physicalNodes);
			
		}
		
		
		//find and set the federable nodes
		setFederatableNodes(vNode, virtualNodeCost);
		
		
		setNodesAvarUtil(virtualNodeCost);
		fPartCostResponse.setNodeCosts(virtualNodeCost);
		
	}
	
	
	/**find the federable nodes for this specific virtual nodes and update the 
	 * FPartCostRecordImpl record
	 * @param vNode
	 * @param virtualNodeRecord the record to be updated
	 */
	protected void setFederatableNodes(VirtualNode  vNode, FPartCostRecordImpl virtualNodeRecord)
	{
		
		ConstructFindResQuery query2 = new ConstructFindResQuery(1, true,
				ManipulateDB.TESTBED_CONTEXTS_STR);
		query2.setRdfType(1, "Node");
		query2.setFunctionalChar(1, vNode);
		query2.setCanFederadedAttrib(1);
		query2.finalizeQuery();
		Set<String> canFeder = query2.execQueryPrintGetResults();
		if (canFeder == null)
		{
			virtualNodeRecord.setError(true);
			log.warn("Problem executing a query for federable nodes in find partitioning cost");
			
		}
		else if (canFeder.size() == 0)
		{
			log.warn("None federated node is found for this resource: {}.", vNode.toString());
		}
		else
		{//nodes are found
			log.info("{} federate nodes are found for this resource : {}.", canFeder.size(), vNode.toString());
			virtualNodeRecord.setFederableResourceURIs(canFeder);
			
		}
		
	}
	
	
	protected void processLink(Link link)
	{
		
		FPartCostRecordImpl virtualLinkCost = new FPartCostRecordImpl();
		virtualLinkCost.addURI(link.toString());
		log.info("FLPC: processing link {}", link.toString());
		//make a query using the functional characteristics of the request
		ConstructFindResQuery query = new ConstructFindResQuery(1, 
				ManipulateDB.TESTBED_CONTEXTS_STR);
		query.setRdfType(1, "Link");
		query.setFunctionalCharLink(1, link);
		query.finalizeQuery();
		
		log.debug("Set the capacity type : ", link.getHasCapacity());
		virtualLinkCost.setHardwType(link.getHasCapacity().toString());

		if (checkIfTypeExist(query.toString(), link.toString()))
		{
			return ;
		}
		
		query.execQueryPrintResults();
		Set<String> physicalLinks = query.execQueryPrintGetResults();
		///
		if (physicalLinks == null)
		{
			virtualLinkCost.setError(true);
			log.warn("Problem executing a query in find partitioning cost");
			fPartCostResponse.addLinkCosts(virtualLinkCost);
			return ;
			
		}
		else if (physicalLinks.size() == 0)
		{
			log.info("None link is found for this resource {}.", link.toString());
			fPartCostResponse.addLinkCosts(virtualLinkCost);
			return ;
		}
		else
		{
			
			log.info("{} links are found for this resource {}.",
					physicalLinks.size(), link.toString());
			virtualLinkCost.setPhysicalNodeURIs(physicalLinks);
			
		}
	
		//TODO : contact monitoring for the average utilization
		virtualLinkCost.setAverUtil(0.25);
		fPartCostResponse.addLinkCosts(virtualLinkCost);
		
	}
	
	
	/**find the average utilization of the nodes (computation nodes or router) 
	 * and update the FPartCostRecordImpl record.
	 * also it deletes from the record the nodes that the utilization was not found.
	 * For the computation nodes It call the monitoring service to get the util values
	 * @param nodesRec
	 * @return the average utilization or -1 if it is not found
	 */
	protected float setNodesAvarUtil(FPartCostRecordImpl nodesRec)
	{
		log.info("Finding the avarage utilization for the nodes : {}",
				nodesRec.getPhysicalNodeURIs().toString());
		
		if (NoviRisValues.isRouter(nodesRec.getHardwType()))
		{
			log.info("The nodes are routers. The hardware type is: {}", nodesRec.getHardwType());
			
			return calculateRoutersAvarUtil(nodesRec);
		}
		else
		{
			log.info("The nodes are computational nodes. The hardware type is: {}", nodesRec.getHardwType());
			//get the utilization values from monitoring service
			/*Set<MonitoringAvarInfo>  monAvarInfo = MonitoringServCommun.
					getNodesMonUtilData(nodesRec.getPhysicalNodeURIs());
			return calculateComputNodesAvarUtil(nodesRec, monAvarInfo);*/
			
			//USE THE AGGREGATED CALL TO MONITORING
			MonitoringAvarInfo  monAvarInfo = MonitoringServCommun.getNodesMonAverageUtilData(
					nodesRec.getPhysicalNodeURIs());
			if (monAvarInfo == null)
			{
				log.warn("There was a problem to call monitoring");
				return -1;
			}
			
			
			
			float avarUtil = calculateComputNodesAvarAggregUtil(monAvarInfo);
			
			nodesRec.setAverUtil(avarUtil);
			return avarUtil;
		
			
		}
		

		
		
	}
	
	
	/**calculate the routers average utilization and update the FPartCostRecordImpl.
	 * It read the available and total logical routers of the router from the RIS DB.
	 * also it deletes from the record the routers that the utilization was not found
	 * @param nodesRec
	 * @return
	 */
	protected float calculateRoutersAvarUtil(FPartCostRecordImpl nodesRec)
	{
		Set<String> validRouters = new HashSet<String>();
		float avarUtil = 0;
		for (String s: nodesRec.getPhysicalNodeURIs())
		{
			log.debug("Calculating the average utilization for router {}", s);
			try {
				Node node = (Node)LocalDbCalls.getLocalResource(s);
				if (node == null)
				{
					log.warn("Well, this is emparassing! I can not find the router {} in the DB", s);
					continue;
					
				}
				Integer totalLogRout = node.getHasLogicalRouters();
				Integer availLogRout = node.getHasAvailableLogicalRouters();
				
				if (totalLogRout == null || availLogRout == null)
				{
					log.warn("The router {}, doesn't have LogicalRouters value: {} " +
							"or AvailableLogicalRouters value :" + availLogRout, s, totalLogRout);
					continue;
				}
				avarUtil += (totalLogRout - availLogRout) / (float)totalLogRout;
				validRouters.add(s);

			} catch (ClassCastException e) {
				log.warn(e.getMessage());
				log.warn("Well, this is emparassing! I can not find the router {} in the DB", s);
			}
			
		}
		
		if (validRouters.size() > 0)
		{
			avarUtil = avarUtil / (float) validRouters.size();
			log.debug("The average util is {}", avarUtil);
		}
		else
		{
			log.warn("I was not able to calculate the avarage util for the routers {}",
					nodesRec.getPhysicalNodeURIs());
		}
		nodesRec.setPhysicalNodeURIs(validRouters);
		nodesRec.setAverUtil(avarUtil);
		
		return avarUtil;
	}
	
	
	
	/**calculate the avarage utilization for the computational nodes.
	 * @param monAvarInfo the monitoring average utilization values for all the nodes
	 * @return the average utilization or -1 if the answer is ivalid
	 */
	protected float calculateComputNodesAvarAggregUtil(MonitoringAvarInfo  monAvarInfo)
	{
		if (monAvarInfo.getCpuUtil() == -1 || monAvarInfo.getMemoryUtil() == -1
				|| monAvarInfo.getStorageUtil() == -1)
		{
			log.warn("Was not possible to get utilization value for at least one metric");
			return -1;
		}
		
		float cpuUtil = monAvarInfo.getCpuUtil();
		float memUtil = monAvarInfo.getMemoryUtil();
		float diskUtil = monAvarInfo.getStorageUtil();
		
		
		float avarUtil = ( (cpuUtil + memUtil + diskUtil) / (float)3 );
		
		//nodesRec.setAverUtil(avarUtil);
		log.info("The average utilization one value is {}", avarUtil);
		return avarUtil;
		
	}
	
	/**calculate the avarage utilization for computational nodes and update the FPartCostRecordImpl record.
	 * also it deletes from the record the nodes that the utilization was not found.
	 * NOT USED, WE USE THE AGGREGATED CALL TO MONITIRNG
	 * @param nodesRec the FPartCostRecordImpl
	 * @param monAvarInfo the monitoring utilization info for that record
	 * @return the average utilization or -1 if it is not found
	 */
	protected float calculateComputNodesAvarUtil(FPartCostRecordImpl nodesRec, 
			Set<MonitoringAvarInfo>  monAvarInfo)
	{
		Set<MonitoringAvarInfo> validMonAvarInfo = removeNotValidAnswers(monAvarInfo);
		if (validMonAvarInfo.size() != nodesRec.getPhysicalNodeURIs().size())
		{
			log.warn("There are {} nodes, but we found the avarage utilization for {} nodes",
					nodesRec.getPhysicalNodeURIs().size(), validMonAvarInfo.size());
			//update the physical nodes list
			//remove those that the average utilization was not found
			Set<String> newPhysicalNodeList = new HashSet<String>();
			for (String nod : nodesRec.getPhysicalNodeURIs())
			{//for all the physical nodes
				boolean found = false;
				for (MonitoringAvarInfo val : validMonAvarInfo)
				{
					if (val.getNodeUri().equals(nod))
					{
						newPhysicalNodeList.add(nod);
						log.debug("The average utilization for the node {} was found", nod);
						found = true;
						break;
					}
				}
				if (!found)
				{
					log.warn("Was not found the avarage utilization for the node {}", nod);
				}
			}
			//update the list with the new one
			nodesRec.setPhysicalNodeURIs(newPhysicalNodeList);
		}
		
		//calculate the aggregation value
		float cpuUtil = 0;
		float memUtil = 0;
		float diskUtil = 0;
		for (MonitoringAvarInfo value : validMonAvarInfo)
		{
			cpuUtil += value.getCpuUtil();
			memUtil += value.getMemoryUtil();
			diskUtil += value.getStorageUtil();
		}
		int size = validMonAvarInfo.size();
		float avarUtil = ( ((cpuUtil/(float)size) + (memUtil/(float)size) + (diskUtil/(float)size)) 
				/ (float)3 );
		
		nodesRec.setAverUtil(avarUtil);
		return avarUtil;
		
	}
	
	
	/**if a node record has an invalid monitoring value (-1) then remove this record
	 * @param monAvarInfo
	 * @return the list containing only the valid records
	 */
	private Set<MonitoringAvarInfo>  removeNotValidAnswers(Set<MonitoringAvarInfo>  monAvarInfo)
	{
		Set<MonitoringAvarInfo>  validMonAvarInfo = new HashSet<MonitoringAvarInfo>();
		for (MonitoringAvarInfo info: monAvarInfo)
		{
			if (info.getCpuUtil() != -1 && info.getMemoryUtil() != -1 && info.getStorageUtil() != -1)
			{
				log.debug("The monitoring info for node {} is valid", info.getNodeUri());
				validMonAvarInfo.add(info);
			}
			else
			{
				log.warn("The monitoring info for node {} is not valid", info.getNodeUri());
			}
		}

		return validMonAvarInfo;
	}
	
}
