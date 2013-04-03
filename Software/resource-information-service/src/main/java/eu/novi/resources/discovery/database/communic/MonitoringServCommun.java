package eu.novi.resources.discovery.database.communic;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.novi.im.util.UrisUtil;
import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.*;
import eu.novi.monitoring.util.MonitoringQuery;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.NoviIPs;
import eu.novi.resources.discovery.util.Testbeds;



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
 * the communication with the monitoring service
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class MonitoringServCommun {

	private static final transient Logger log = 
			LoggerFactory.getLogger(MonitoringServCommun.class);


	private static MonSrv monServ;
	
	/**
	 * Path to the private key for plabetlab user sfademo 
	 */
	private static String plSfademoKeyPath;
	/**
	 * Path to the private key for planetlab user root
	 */
	private static String plRootKeyPath;
	
    /**
     * Testbed name on which the service is running e.g. PlanetLab, FEDERICA.
     */
    private static String testbed; //get for service mix blueprint

	//the free memory and disc space are in kilo bytes, so we devide them by
	//1 000 000 to get GB
	private static final float DEVIDE_FACTOR = 1000000;



	/**get the public IP  of the physical node
	 * @param nodeURI
	 * @return the IP  or null if was found
	 */
	protected static String getPublicIP(String nodeURI)
	{

		String ip = NoviIPs.getPublicIP(nodeURI);
		if (ip != null)
		{
			log.info("I found the public IP {} for the node : {}", ip, nodeURI);

		}

		return ip;

		/*log.info("I will use the hostname as the public IP of the {}", nodeURI);
		String hostn = LocalDbCalls.getNodeHostname(nodeURI);
		if (hostn == null)
		{
			log.warn("I did not found any hostname for the node {}", nodeURI);

		}
		else
		{
			log.info("I found the hostname {}", hostn);
		}

		return hostn;*/
	}


	/**call monitoring and get the availability values for the node
	 * @param nodeUri
	 * @return the mon information
	 */
	public static MonitoringInfo getNodeMonData(String nodeUri)
	{


		if (monServ == null)
		{
			String ms = "I can not call monitoring service. The monitoring service is null";
			log.warn(ms);
			return new MonitoringInfo(nodeUri, ms);
		}
		//do a real call to monitoring service


		//constract and send the query
		log.info("Quering monitoring for node : {}", nodeUri);
		String node = UrisUtil.getURNfromURI(nodeUri); //"smilax1";
		String inIp = getPublicIP(nodeUri); //"150.254.160.19";
		if (inIp == null)
		{
			String ms = "I will not make the query to monitoring. The public IP was not found";
			log.warn(ms);
			return new MonitoringInfo(nodeUri, ms);
		}
		String outIp = inIp; //"150.254.160.19";
		log.info("PSNC DEBUG: plSfaDemoKeyPath is: " + plSfademoKeyPath);
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi",plSfademoKeyPath,"");

		MonitoringQuery q = monServ.createQuery();
		//free memory
		q.addFeature("measureMemoryInfo", "FreeMemory");
		q.addResource("measureMemoryInfo", node, "Node");
		//free disk  space
		q.addFeature("measureDiskInfo", "FreeDiskSpace");
		q.addResource("measureDiskInfo", node, "Node");
		//CPU speed
		q.addFeature("measureCPUSpeed", "CPUClockRate");
		q.addResource("measureCPUSpeed", node, "Node");

		//CPU cores
		q.addFeature("measureCPUSockets", "CPUSockets");
		q.addResource("measureCPUSockets", node, "Node");

		q.addFeature("measureCPUcores", "CPUCores");
		q.addResource("measureCPUcores", node, "Node");

		q.addInterface(node, "ifin", "hasInboundInterface");
		q.addInterface(node, "ifout", "hasOutboundInterface");
		q.defineInterface("ifin", inIp, "hasIPv4Address");
		q.defineInterface("ifout", outIp, "hasIPv4Address");
		String query = q.serialize();

		log.info("The query to monitoring is:\n" + query);
		String res = monServ.substrate(cred, query);
		log.info("Results from Monitoring for node: {} : \n{}\n", nodeUri, res);


		return extractMonValues(nodeUri, res);

	}

	/**
	 * get for monitoring service the monitoring data for the given nodes.
	 * It use threads to speed up the queries
	 * i.e. the cpu load, free memory, free storage size
	 * @param nodes
	 * @return a set of MonitoringInfo objects contain the information
	 * from monitoring service
	 */
	public static Set<MonitoringInfo> getNodesMonData(Set<String> nodes)
	{
		log.info("Calling Monitoring Service...");
		Set<MonitoringInfo> monInfo = new HashSet<MonitoringInfo>();

		if (monServ != null)
		{//do a real call to monitoring service

			List<Future<MonitoringInfo>> threadAnswers = new Vector<Future<MonitoringInfo>>();
			//do the calls
			for (String nodeUri : nodes)
			{
				//use multi threading
				final String nodeUrif = nodeUri;
				Future<MonitoringInfo> newThreadAns = PeriodicUpdate.executeNewThread(
						new Callable<MonitoringInfo>() {

							@Override
							public MonitoringInfo call() throws Exception {
								//the actual parallel code
								return getNodeMonData(nodeUrif);
							}

						});
				threadAnswers.add(newThreadAns);

			}

			log.info("Now I am going to wait until I will get back the results from the threads");
			try {
				//to be sure that all the threads are started
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				log.warn(e1.getMessage());
				log.warn("Was not able to sleep");
			}
			
			//gather the results
			for (Future<MonitoringInfo> threadAnsw : threadAnswers)
			{
				try {
					MonitoringInfo nodeInfo = threadAnsw.get();
					if (nodeInfo.isHasError())
					{
						log.warn("There was a problem to get a monitoring answer for the node {}.\n" +
								"The error message is: {}", nodeInfo.getNodeUri(),
								nodeInfo.getErrorMessage());
					}
					else
					{
						log.info("I got the results for the node: {}", nodeInfo.getNodeUri());
						monInfo.add(nodeInfo);
					}

				} catch (InterruptedException e) {
					log.warn(e.getMessage());
					log.warn("There was a problem to read the answer from a thread");
				} catch (ExecutionException e) {
					log.warn(e.getMessage());
					log.warn("There was a problem to read the answer from a thread");
				}

			}

		}
		else
		{//provide some hard coded values
			log.warn("The monitoring service is null, I will add some hard coded values");
			for (String nodeUri : nodes)
			{


				if (nodeUri.contains("novilab.elte.hu")){
					log.info("Adding monitoring info to novilab.elte.hu");
					monInfo.add(new MonitoringInfo(nodeUri, 2, 2.5f, 800, 3));
				}
				// else if (nodeUri.contains("smilax5.man.poznan.pl")){
				// log.info("Adding monitoring info to smilax5.man.poznan.pl");
				// monInfo.add(new MonitoringInfo(nodeUri, 2, 3, 800, 3));
				// }
				// else if (nodeUri.contains("smilax2.man.poznan.pl")){
				// log.info("Adding monitoring info to smilax2.man.poznan.pl");
				// monInfo.add(new MonitoringInfo(nodeUri, 4, 2.5f, 1000, 4));
				// }
				else if (nodeUri.contains("planetlab1-novi.lab.netmode.ece.ntua.gr")){
					log.info("Adding monitoring info to planetlab1-novi.lab.netmode.ece.ntua.gr");
					monInfo.add(new MonitoringInfo(nodeUri, 2, 4, 1010, 4));
				}
				else if (nodeUri.contains("planetlab2-novi.lab.netmode.ece.ntua.gr")){
					log.info("Adding monitoring info to planetlab2-novi.lab.netmode.ece.ntua.gr");
					monInfo.add(new MonitoringInfo(nodeUri, 4, 2, 1024, 8));
				}
				else if (nodeUri.contains("smilax1.man.poznan.pl")){
					log.info("Adding monitoring info to smilax1.man.poznan.pl");
					monInfo.add(new MonitoringInfo(nodeUri, 6, 2, 2048, 8));
				}
				// else if (nodeUri.contains("smilax3.man.poznan.pl")){
				// log.info("Adding monitoring info to smilax3.man.poznan.pl");
				// monInfo.add(new MonitoringInfo(nodeUri, 4, 3, 500, 4));
				// }
				// else if (nodeUri.contains("smilax4.man.poznan.pl")){
				// log.info("Adding monitoring info to smilax4.man.poznan.pl");
				// monInfo.add(new MonitoringInfo(nodeUri, 4, 2, 500, 4));
				// }
				else {
					log.info("Adding default monitoring info");
					monInfo.add(new MonitoringInfo(nodeUri, 2, 2, 500, 2));
				}
			}


		}//end of hard coded

		return monInfo;

	}


	/**
	 * get for monitoring service the static monitoring data for the given nodes.
	 * It use threads to speed up the queries
	 * i.e. the cpu cores and speed, total memory, total storage size
	 * @param nodes
	 * @return a set of MonitoringInfo objects contain the information
	 * from monitoring service
	 */
	public static Set<MonitoringInfo> getNodesStaticMonData(Set<String> nodes)
	{
		log.info("Calling Monitoring Service for static information...");
		Set<MonitoringInfo> monInfo = new HashSet<MonitoringInfo>();

		if (monServ != null)
		{
			log.warn("The monitoring service is null. I can not call monitoring");
			return monInfo;
		}

		List<Future<MonitoringInfo>> threadAnswers = new Vector<Future<MonitoringInfo>>();
		//do the calls
		for (String nodeUri : nodes)
		{
			//use multi threading
			final String nodeUrif = nodeUri;
			Future<MonitoringInfo> newThreadAns = PeriodicUpdate.executeNewThread(
					new Callable<MonitoringInfo>() {

						@Override
						public MonitoringInfo call() throws Exception {
							//the actual parallel code
							return getNodeMonData(nodeUrif);//TODO change that
						}

					});
			threadAnswers.add(newThreadAns);

		}

		log.info("Now I am going to wait until I will get back the results from the threads");
		//gather the results
		for (Future<MonitoringInfo> threadAnsw : threadAnswers)
		{
			try {
				MonitoringInfo nodeInfo = threadAnsw.get();
				if (nodeInfo.isHasError())
				{
					log.warn("There was a problem to get a monitoring answer for the node {}.\n" +
							"The error message is: {}", nodeInfo.getNodeUri(),
							nodeInfo.getErrorMessage());
				}
				else
				{
					log.info("I got the results for the node: {}", nodeInfo.getNodeUri());
					monInfo.add(nodeInfo);
				}

			} catch (InterruptedException e) {
				log.warn(e.getMessage());
				log.warn("There was a problem to read the answer from a thread");
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
				log.warn("There was a problem to read the answer from a thread");
			}

		}
		return monInfo;
	}
	
	
	/**call monitoring and get the static values for the node.
	 *  the cpu cores and speed, total memory, total storage size
	 * @param nodeUri
	 * @return the mon information 
	 */
	public static MonitoringInfo getNodeStaticMonData(String nodeUri)
	{


		if (monServ == null)
		{
			String ms = "I can not call monitoring service. The monitoring service is null";
			log.warn(ms);
			return new MonitoringInfo(nodeUri, ms);
		}
		//do a real call to monitoring service


		//constract and send the query
		log.info("Quering monitoring for static info for node : {}", nodeUri);
		String node = UrisUtil.getURNfromURI(nodeUri); //"smilax1";
		String inIp = getPublicIP(nodeUri); //"150.254.160.19";
		if (inIp == null)
		{
			String ms = "I will not make the query to monitoring. The public IP was not found";
			log.warn(ms);
			return new MonitoringInfo(nodeUri, ms);
		}
		String outIp = inIp; //"150.254.160.19";
		log.info("PSNC DEBUG: plSfaDemoKeyPath is: " + plSfademoKeyPath);
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi",plSfademoKeyPath,"");

		MonitoringQuery q = monServ.createQuery();
		//TODO update the info
		//total memory
		q.addFeature("measureMemoryInfo", "FreeMemory");
		q.addResource("measureMemoryInfo", node, "Node");
		//total disk  space
		q.addFeature("measureDiskInfo", "FreeDiskSpace");
		q.addResource("measureDiskInfo", node, "Node");
		//CPU speed
		q.addFeature("measureCPUSpeed", "CPUClockRate");
		q.addResource("measureCPUSpeed", node, "Node");

		//CPU cores
		q.addFeature("measureCPUSockets", "CPUSockets");
		q.addResource("measureCPUSockets", node, "Node");

		q.addFeature("measureCPUcores", "CPUCores");
		q.addResource("measureCPUcores", node, "Node");

		q.addInterface(node, "ifin", "hasInboundInterface");
		q.addInterface(node, "ifout", "hasOutboundInterface");
		q.defineInterface("ifin", inIp, "hasIPv4Address");
		q.defineInterface("ifout", outIp, "hasIPv4Address");
		String query = q.serialize();

		log.info("The query to monitoring is:\n" + query);
		String res = monServ.substrate(cred, query);
		log.info("Results from Monitoring for node: {} : \n{}\n", nodeUri, res);


		//TODO extract the correct values
		return extractMonValues(nodeUri, res);

	}

	
	
	/**from the monitoring response it extracts the availability values of 
	 * cpu, memory and storage
	 * @param nodeUri 
	 * @param response the monitoring JSon response
	 * @return
	 */
	protected static MonitoringInfo extractMonValues(String nodeUri, String response)
	{
		//extract the information from the answer
		int cpuCores = -1;
		float cpuSpeed = -1;
		float discSpace = -1;
		float freeMemory = -1;

		JsonNode jsonRootNode = getJNodefromString(response);
		if (jsonRootNode == null)
		{//the answer is invalid
			
			return new MonitoringInfo(nodeUri, "The Json answer is invalid");
		}

		//get cpu cores
		JsonNode jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#CPUSockets");
		if (jNode != null)
		{
			int cpuSockets = jNode.asInt();
			JsonNode jNode2 = getValue(jsonRootNode, 
					"http://fp7-novi.eu/monitoring_features.owl#CPUCores");
			if (jNode2 != null)
			{
				cpuCores = cpuSockets * jNode2.asInt();
			}
			else
			{
				//log.warn("I have no CPU core, value. I will assign the mock value 4");
				
				cpuCores = -1;
			}
		}
		else
		{
			//log.warn("I have no CPU core, value. I will assign the mock value 4");
		
			cpuCores = -1;
		}

		//get cpu speed
		jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#CPUClockRate");
		if (jNode != null)
		{
			cpuSpeed = (jNode.asInt()/1000f); //transform it to GHz
		}
		else
		{
			cpuSpeed = -1;
		}

		//get free memory
		jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeMemory");
		if (jNode != null)
		{
			freeMemory = jNode.asInt()/DEVIDE_FACTOR;
		}
		else
		{
			freeMemory = -1;
		}

		//get free disc space
		jNode = getValue(jsonRootNode,
				"http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace");
		if(jNode != null)
		{
			discSpace = jNode.asInt()/DEVIDE_FACTOR;

		}
		else
		{
			discSpace = -1;
		}

		return new MonitoringInfo(nodeUri, cpuCores, cpuSpeed, discSpace, freeMemory);
	}


	

	/**get 3  average utilization values  for all the nodes. 
	 * Make an aggregation call to monitoring.
	 * @param nodes
	 * @return the average utilization value for all the nodes. Or null if a problem happen
	 */
	public static MonitoringAvarInfo getNodesMonAverageUtilData(Set<String> nodes)
	{
		log.info("Calling Monitoring Service for average utilization values...");
		
		//we have a monitoring problem in federica, so we mock the values for the moment
		if (Testbeds.isFederica(testbed))
		{
			log.warn("This is FEDERICA testbed, I will mock the utilization values");
			//TODO remove the mock
			return new MonitoringAvarInfo("averageUtilValues", 0.6f, 0.7f, 0.4f);
		}

		if (monServ == null)
		{
			log.warn("The monitoring service reference is null. I can not get the avarage util");
			return null;
		}

		//create the query
		log.info("PSNC DEBUG: plSfaDemoKeyPath is: " + plSfademoKeyPath);
		UsernameRSAKey cred = new UsernameRSAKey("novi_novi",plSfademoKeyPath,"");
		MonitoringQuery q = monServ.createQuery();

		//do one aggregated call to monitoring service 
		//include all the nodes to the query
		int index = 0;
		String res = null;
		for (String nodeUri : nodes)
		{

			//constract  the query
			log.info("Include the node to the monitoring query : {}", nodeUri);
			String node = UrisUtil.getURNfromURI(nodeUri); //"smilax1";
			
			String inIp = getPublicIP(nodeUri); //"150.254.160.19";
			if (inIp == null)
			{
				log.warn("I can not find the IP address for the node {}", nodeUri);
				continue;
			}
			index++;
			String outIp = inIp; //"150.254.160.19";

			//memory
			q.addFeature("measureMemoryUtilInfo" + index, "MemoryUtilization");
			q.addResource("measureMemoryUtilInfo" + index, node, "Node");


			//disk  space
			q.addFeature("measureDiskUtilInfo" + index, "DiskUtilization");
			q.addResource("measureDiskUtilInfo" + index, node, "Node");


			//CPU load
			q.addFeature("measureCPUUtil" +index, "CPUUtilization");
			q.addResource("measureCPUUtil" + index, node, "Node");


			//interfaces
			q.addInterface(node, "ifin" + index, "hasInboundInterface");
			q.addInterface(node, "ifout" + index, "hasOutboundInterface");
			q.defineInterface("ifin" + index, inIp, "hasIPv4Address");
			q.defineInterface("ifout" + index, outIp, "hasIPv4Address");


		}




		String query = q.serialize();
		if (index == 0)
		{
			log.warn("The query {} doesn't contain any nodes, I will not send it to monitoring", query);
			return null;
		}
		//make the call to monitoring
		log.info("The aggregated query to monitoring is:\n" + query);
		res = monServ.substrate(cred, query);
		log.info("Results from Monitoring: \n{}\n", res);

		if (res == null)
		{
			log.warn("The results from monitoring is null");
			return null;
		}

		//extract the result and calculate the average utilization
		return extractMonAverUtilValue(res);

	}


	/**from the monitoring response it extracts and calculate the average utilization values
	 * for cpu, memory  and disk
	 * @param monResponse the monitoring JSon response
	 * @return the values in a MonitoringAvarInfo class or null if there is a problem
	 */
	protected static MonitoringAvarInfo extractMonAverUtilValue(String monResponse)
	{


		JsonNode rootNode = getJNodefromString(monResponse);
		if (rootNode == null)
		{//the answer is invalid
			return null;
		}



		int size =  rootNode.size();
		if (size == 0 )
		{
			log.warn("The monitoring answer is empty");
			return null;
		}

		//read all the values
		float cpuUtil = 0;
		float diskUtil = 0;
		float memoryUtil = 0;

		int cpuCount = 0;
		int memCount = 0;
		int diskCount = 0;
		for (int i=0; i < size; i++)
		{
			JsonNode jNode = rootNode.get(i);

			if (jNode == null)
			{
				log.warn("The monitoring answer is not an array. I can not proccess it");
				return null;
			}
			String diskUtilFea = "http://fp7-novi.eu/monitoring_features.owl#DiskUtilization";
			String memUtilFea = "http://fp7-novi.eu/monitoring_features.owl#MemoryUtilization";
			String cpuUtilfea = "http://fp7-novi.eu/monitoring_features.owl#CPUUtilization";
			if (jNode.get(diskUtilFea) != null)
			{
				log.debug("The element is Disk util");
				JsonNode val = getValueFromJsonNode(
						jNode.get(diskUtilFea), diskUtilFea);
				if (val == null)
				{
					log.warn("I can not find the value in the DiskUtilization JNode component:\n{}",
							jNode.toString()); 
				}
				else
				{
					diskCount++;
					diskUtil += val.asDouble();
					log.info("The DiskUtilization value is {}", val.asDouble());

				}

			}
			else if (jNode.get(memUtilFea) != null)
			{
				log.debug("The element is Memory util");
				JsonNode val = getValueFromJsonNode(
						jNode.get(memUtilFea), memUtilFea);
				if (val == null)
				{
					log.warn("I can not find the value in the MemoryUtilization JNode component:\n{}",
							jNode.toString()); 
				}
				else
				{
					memCount++;
					memoryUtil += val.asDouble();
					log.info("The MemoryUtilization value is {}", val.asDouble());

				}

			}
			else if (jNode.get(cpuUtilfea) != null)
			{
				log.debug("The element is CPU util");
				JsonNode val = getValueFromJsonNode(
						jNode.get(cpuUtilfea), cpuUtilfea);
				if (val == null)
				{
					log.warn("I can not find the value in the CPUUtilization JNode component:\n{}",
							jNode.toString()); 
				}
				else
				{
					cpuCount++;
					cpuUtil += val.asDouble();
					log.info("The CPUUtilization value is {}", val.asDouble());

				}

			}
			else
			{
				log.warn("The json node doesn't contain any cpu, memory or disk utilization component:\n{}",
						jNode.toString());
			}

		}


		if ((cpuCount != memCount) || (memCount != diskCount))
		{//I should find the same number of values
			log.warn("Well something odd is going on here! " +
					"I did not get the same number of values for all the metrics.\n" +
					"CPU count = {}, Memory Count = {} and Disk count = " + diskCount, cpuCount, memCount);
		}

		//calculate the average
		//cpu
		if (cpuCount == 0)
		{
			log.warn("I did not found any cpu value");
			cpuUtil = -1;
		}
		else 
		{
			cpuUtil = cpuUtil / (float)cpuCount;
			log.info("I found {} cpu util values and the average value is {}", cpuCount, cpuUtil);

		}
		//memory
		if (memCount == 0)
		{
			log.warn("I did not found any memory value");
			memoryUtil = -1;
		}
		else
		{
			memoryUtil = memoryUtil / (float)memCount;
			log.info("I found {} memory util values and the average value is {}", memCount, memoryUtil);
		}
		//disk
		if (diskCount == 0)
		{
			log.warn("I did not found any disk value");
			diskUtil = -1;
		}
		else
		{
			diskUtil = diskUtil / (float) diskCount;
			log.info("I found {} disk util values and the average value is {}", diskCount, diskUtil);
		}

		return new MonitoringAvarInfo("averageUtilValues", cpuUtil, memoryUtil, diskUtil);
	}



	/**from the monitoring response it extracts and calculate the utilization values for the 
	 * given node
	 * @param nodeUri the node URI
	 * @param monResponse the monitoring JSon response
	 * @return the values in a MonitoringAvarInfo class
	 */
	protected static MonitoringAvarInfo extractMonUtilValues(String nodeUri, String monResponse)
	{
		//extract the information from the answer
		float cpuLoad = -1;
		float diskUtil = -1;
		float memoryUtil = -1;
		JsonNode jsonRootNode = getJNodefromString(monResponse);
		if (jsonRootNode == null)
		{//the answer is invalid
			return new MonitoringAvarInfo(nodeUri, cpuLoad, memoryUtil, diskUtil);
		}

		//get cpu load
		JsonNode jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#CPUUtilization");
		if (jNode != null)
		{
			cpuLoad = (float)jNode.asDouble();

		}
		else
		{
			log.warn("I can not found the CPU load value");
			//TODO remove the mock
			log.warn("I will assign a mock value 0.5");
			cpuLoad = 0.5f;
		}

		//MEMORY
		float freeMem = -1;
		float totalMem = -1;

		//get free memory
		jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeMemory");
		if (jNode != null)
		{
			freeMem = (float)jNode.asDouble();
		}
		else
		{
			log.warn("I can not found the free memory");
		}

		//get available memory
		jNode = getValue(jsonRootNode, 
				"http://fp7-novi.eu/monitoring_features.owl#AvailableMemory");
		if (jNode != null)
		{
			totalMem = (float)jNode.asDouble();
		}
		else
		{
			log.warn("I can not found the available memory");
		}

		if (freeMem != -1 && totalMem != -1)
		{//if I got both values
			memoryUtil = (totalMem - freeMem) / totalMem;

		}

		//DISK SPACE
		float diskFree = -1;
		float diskUsed = -1;
		//get free disc space
		jNode = getValue(jsonRootNode,
				"http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace");
		if(jNode != null)
		{
			diskFree = (float)jNode.asDouble();

		}
		else
		{
			log.warn("I can not found the free disk space");
		}

		//get used disc space
		jNode = getValue(jsonRootNode,
				"http://fp7-novi.eu/monitoring_features.owl#UsedDiskSpace");
		if(jNode != null)
		{
			diskUsed = (float)jNode.asDouble();

		}
		else
		{
			log.warn("I can not found the used disk space");
		}

		if (diskFree != -1 && diskUsed != -1)
		{//if I got both values
			diskUtil = diskUsed / (diskUsed + diskFree);
		}

		return new MonitoringAvarInfo(nodeUri, cpuLoad, memoryUtil, diskUtil);
	}


	/**it return back the value for a specific type and field
	 * @param jsonNode a Json node, can be object or array
	 * @param type URL of the type, i.e. http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace
	 * @param field the required field, i.e. Available Disk Size
	 * @return a JsonNode or null, if the field was not found, is null or the response is invalid
	 */
	protected static JsonNode getValue (JsonNode jsonNode, String type)  {
		//String type = "http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace";
		//String getField = "Available Disk Size";



		JsonNode typeNode = jsonNode.path(type);

		if (typeNode.isMissingNode() && jsonNode.get(0) == null)//not found and not array
		{
			log.warn("The type {} doesnt exist in the json node", type);
			return null;

		}

		if (typeNode.isMissingNode() && jsonNode.get(0) != null)//not found but it is an array
		{//consider the case the rootNode to be an array
			log.debug("The json answer from monitoring is an array");
			typeNode = getObjectFromJsonArray(jsonNode, type);
			//log.info("test \n{}",typeNode.toString());
			if (typeNode == null)
				return null;
		}

		return getValueFromJsonNode(typeNode, type);
	}



	/**from a Json node it find and retrun the value for the given field
	 * @param jNode Json node
	 * @param field
	 * @return the value in a Json node or null
	 */
	private static JsonNode getValueFromJsonNode(JsonNode jNode, String field)
	{
		for (int i = 0 ; ;i++ )
		{
			if (jNode.path("HDR").path("HDRINFO").get(i) == null)
			{
				log.warn("I reach the end of the table, the field \"{}\" was not found", field);
				break;

			}
			else if (jNode.path("HDR").path("HDRINFO").get(i).path("FEATURE").asText().equals(field))
			{
				log.debug("I found the index, for {}, and the value is: {}", field,
						jNode.path("DATA").get(0).get(i));

				if (jNode.path("DATA").get(0).get(i).isNull())
				{
					log.warn("The answer for {} is invalid or null, I will return null", field);
					return null;
				}

				return  jNode.path("DATA").get(0).get(i);
			}
		}

		return null;

	}


	/**get the Json root node of this string
	 * @param jSonString the json string
	 * @return the json node or null if it is invalid
	 */
	protected static JsonNode getJNodefromString(String jSonString)
	{
		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(JsonParser), or bind to JsonNode
		JsonNode rootNode = null;
		try {
			rootNode = m.readValue(jSonString, JsonNode.class);
		} catch (JsonParseException e) {
			log.warn("The following answer from monitoring service is invalid:\n{}", jSonString);
			log.warn(e.getMessage());
			return null;
		} catch (JsonMappingException e) {
			log.warn("The following answer from monitoring service is invalid:\n{}", jSonString);
			log.warn(e.getMessage());
			return null;
		} catch (IOException e) {
			log.warn("The following answer from monitoring service is invalid:\n{}", jSonString);
			log.warn(e.getMessage());
			return null;
		}

		return rootNode;

	}


	/**find in the Json array and return  the object with the given name
	 * @param jArray a Json array 
	 * @param name
	 * @return the Json object or null if it is not found
	 */
	private static JsonNode getObjectFromJsonArray(JsonNode jArray, String name)
	{
		int size = jArray.size();
		for (int i=0; i < size; i++)
		{
			if (jArray.get(i).get(name) != null)
			{
				log.debug("I found the object {} in the Json array", name);
				return jArray.get(i).get(name);

			}

		}
		log.warn("I did not found the object {} in the Json array", name);
		return null;
	}


	/**get the utilization values for the nodes.
	 * For each resources it send seperate query
	 * NOT USED
	 * @param nodes
	 * @return
	 */
	public static Set<MonitoringAvarInfo> getNodesMonUtilData(Set<String> nodes)
	{
		log.info("Calling Monitoring Service...");
		Set<MonitoringAvarInfo> monInfo = new HashSet<MonitoringAvarInfo>();

		if (monServ == null)
		{
			log.warn("The monitoring service reference is null. I can not get the avarage util");
			return monInfo;
		}

		//do a real call to monitoring service
		for (String nodeUri : nodes)
		{

			//constract and send the query
			log.info("Quering monitoring for util for node : {}", nodeUri);
			String node = UrisUtil.getURNfromURI(nodeUri); //"smilax1";

			String inIp = getPublicIP(nodeUri); //"150.254.160.19";
			if (inIp == null)
			{
				log.warn("I will not make the query to monitoring. The public IP was not found");
				continue;
			}
			String outIp = inIp; //"150.254.160.19";
			log.info("PSNC DEBUG: plSfaDemoKeyPath is: " + plSfademoKeyPath);
			UsernameRSAKey cred = new UsernameRSAKey("novi_novi",plSfademoKeyPath,"");

			MonitoringQuery q = monServ.createQuery();
			//memory
			q.addFeature("measureFreeMemoryInfo", "FreeMemory");
			q.addResource("measureFreeMemoryInfo", node, "Node");

			q.addFeature("measureAvailMemoryInfo", "AvailableMemory");
			q.addResource("measureAvailMemoryInfo", node, "Node");

			//disk  space
			q.addFeature("measureFreeDiskInfo", "FreeDiskSpace");
			q.addResource("measureFreeDiskInfo", node, "Node");

			q.addFeature("measureUsedDiskInfo", "UsedDiskSpace");
			q.addResource("measureUsedDiskInfo", node, "Node");


			//CPU load
			q.addFeature("measureCPU", "CPUUtilization");
			q.addResource("measureCPU", node, "Node");


			q.addInterface(node, "ifin", "hasInboundInterface");
			q.addInterface(node, "ifout", "hasOutboundInterface");
			q.defineInterface("ifin", inIp, "hasIPv4Address");
			q.defineInterface("ifout", outIp, "hasIPv4Address");
			String query = q.serialize();

			log.info("The query to monitoring is:\n" + query);
			String res = monServ.substrate(cred, query);
			log.info("Results from Monitoring: \n{}\n", res);

			monInfo.add(extractMonUtilValues(nodeUri, res));

		}

		return monInfo;

	}


	///////////////	SETTERS GETTERS////////////

	public MonSrv getMonServ() {
		return monServ;
	}



	public void setMonServ(MonSrv monServ) {
		MonitoringServCommun.monServ = monServ;
	}

	public String getPlSfademoKeyPath() {
		return plSfademoKeyPath;
	}


	public void setPlSfademoKeyPath(String plSfademoKeyPath1) {
		plSfademoKeyPath = plSfademoKeyPath1;
	}


	public String getPlRootKeyPath() {
		return plRootKeyPath;
	}

	public void setPlRootKeyPath(String plRootKeyPath1) {
		plRootKeyPath = plRootKeyPath1;
	}

	/**if the monitoring service is null, it returns true
	 * @return
	 */
	public static boolean isMonServiceNull()
	{
		if (monServ == null)
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	public String getTestbed() {
		return testbed;
	}


	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}



}
