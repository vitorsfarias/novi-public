package eu.novi.resources.discovery.database.communic;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponse;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.UrisUtil;

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
 * implement the communication with the testbed using the request handler
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class TestbedCommunication {

	private static final transient Logger log = 
			LoggerFactory.getLogger(TestbedCommunication.class);


	private static FederatedTestbed calls2TestbedFromRH;


	public FederatedTestbed getCalls2TestbedFromRH() {
		return TestbedCommunication.calls2TestbedFromRH;
	}

	public  void setCalls2TestbedFromRH(FederatedTestbed calls2TestbedFromRH) {
		TestbedCommunication.calls2TestbedFromRH = calls2TestbedFromRH;
	}
	
	
	/**for junit testing
	 * @param calls2TestbedFromRH
	 */
	public  static void assignCalls2TestbedFromRHStatic(FederatedTestbed calls2TestbedFromRH) {
		TestbedCommunication.calls2TestbedFromRH = calls2TestbedFromRH;
	}



	/**delete the slice to the testbeds
	 * @param uri
	 * @return true if the deletion was succesfull otherwise false
	 */
	public static boolean deleteSlice(String uri, Set<String> platformURIs, TopologyImpl topology)
	{
		if (calls2TestbedFromRH == null)
		{
			log.warn("The Request Handler service is null, I can not delete the slice {}", uri);
			return false;
		}
		RHCreateDeleteSliceResponse response = calls2TestbedFromRH.deleteSlice(null, 
				UrisUtil.getURNfromURI(uri), platformURIs, topology); //TODO add the set here
		if (response == null)
		{
			log.warn("Problem delete the slice {} from the testbed! " +
					"No response from the testbed", uri);
			return false;
		}
		else if (response.hasError())
		{
			log.warn("Problem delete the slice {} from the testbed!", uri);
			log.warn("Message from testbed: {}",response.getErrorMessage());
			return false;
		}
		else
		{
			log.info("The slice {} was succesfully deleted from the testbed", uri);
			return true;
		}

	}




	/**reserve the slice to the testbed
	 * @param sliceURN , the slice URN
	 * @param topology
	 * @return the RHCreateSliceResponse object from the testbed
	 */
	public static RHCreateDeleteSliceResponse reserveSlice(NOVIUserImpl currentUser, 
			String sliceURN, Topology topology)
	{

		// Testing logs added by Alvaro
		//log.info("Analyzing the topology in reserveSlice BEFORE copy");
		//printImplementBy(topology);


		/*IMCopy copier = new IMCopy();
		copier.enableLogs();
		TopologyImpl top = (TopologyImpl) copier.copy(topology, -1);*/

		// Testing logs added by Alvaro
		//log.info("Analyzing the topology in reserveSlice AFTER copy");
		//printImplementBy(topology);


		log.debug("The topology was translated to the implemented classes");
		log.debug("Calling Request Handler...");
		RHCreateDeleteSliceResponse respo = calls2TestbedFromRH.createSlice(null, currentUser, sliceURN, (TopologyImpl)topology);
		//	calls2TestbedFromRH.createSlice(currentUser, sliceURN, top);
		if (respo == null)
		{
			log.warn("The response from Request Handler is null");
		}
		else if (respo.hasError())
		{
			log.warn("The response from Request Handler has error");
		}
		else
		{
			log.debug("I got the response from Request Handler");
		}
		return respo;

	}


	/**it prints the ralation node implementedBy and interface implementedBy
	 * @param topology
	 */
	protected static void printImplementBy(Topology topology)
	{
		for (Resource res : topology.getContains()) {
			if (res instanceof Node) {
				if (((Node) res).getImplementedBy()==null) {
					log.info(  ((Node) res).toString()+ "	Implemented by: -");
				}
				else {
					Node n = ((Node) res).getImplementedBy().iterator().next();
					log.info(  ((Node) res).toString()+ "	Implemented by: " + n.toString());
				}
				if (((Node) res).getHasOutboundInterfaces()!=null) {
					for (Interface iface : ((Node) res).getHasOutboundInterfaces()) {
						if (iface.getImplementedBy()!=null && !iface.getImplementedBy().isEmpty()) {
							log.info("Iface "+iface+" implemented by "+iface.getImplementedBy().toString());
						} else {
							log.info("Iface "+iface+" has no implemented by set");
						}
					}
				}
				if (((Node) res).getHasInboundInterfaces()!=null) {
					for (Interface iface : ((Node) res).getHasInboundInterfaces()) {
						if (iface.getImplementedBy()!=null && !iface.getImplementedBy().isEmpty()) {
							log.info("Iface "+iface+" implemented by "+iface.getImplementedBy().toString());
						} else {
							log.info("Iface "+iface+" has no implemented by set");
						}
					}
				}
			}
		}
	}


	/**update the slice to the testbed
	 * @param sliceURN , the slice URN, should exist in the tesbed
	 * @param newTopology the new slice topology
	 * @param oldTopology the old manifest topology, in implemented classes
	 * @return the RHCreateSliceResponse object from the testbed
	 */
	public static RHCreateDeleteSliceResponse updateSlice(NOVIUserImpl currentUser,
			String sliceURN, Topology newTopology, TopologyImpl oldTopology)
	{
		IMCopy copier = new IMCopy();
		return calls2TestbedFromRH.updateSlice(null, currentUser, sliceURN,
				oldTopology, (TopologyImpl)copier.copy(newTopology,-1));

	}



	/**
	 * get the testbed substrate using request handler
	 * @return the substrate topology or null if an error occurred
	 */
	public static Platform getTestbedSubstrate()
	{
		if (calls2TestbedFromRH == null)
		{
			log.warn("The request handler service is null, I can not get the substrate");
			return null;
		}
		log.info("Calling Request handler listResources ...");
		RHListResourcesResponse testbedResponse = calls2TestbedFromRH.listResources("");
		log.info("I got back the answer from request handler");
		if (testbedResponse == null)
		{
			log.warn("The answer from Request handler is null");
			return null;
		}
		if (testbedResponse.hasError())
		{
			log.error("There is a problem getting the supstrate from the testbed");
			log.warn("Request Handler response hasError. The DB can not be updated");
			//log.warn("Request handler message : {}",testbedResponse.getErrorMessage());
			return null;

		}
		else
		{
			String rdfPlatformString = testbedResponse.getPlatformString();

			if (rdfPlatformString == null)
			{
				log.warn("The string topology that I got from the testbed is  null. " +
						"I can not update the DB");
				return null;
			}
			log.info("The supstrate was retrieved successfully using Request Handler");
			//log.info("I will store the substrate description to the server");
			//OwlCreator owlCr = new OwlCreator();
			//owlCr.createStoreFile("testbedSubstratefromRH.owl", rdfPlatformString);

			log.info("Translating rdf/xml string to NOVI IM objects...");
			IMRepositoryUtil repUtil = new IMRepositoryUtilImpl();
			Platform platformSubs = repUtil.getIMObjectFromString(rdfPlatformString, Platform.class);
			if (platformSubs == null)
			{
				log.warn("Not able to translate the rdf/xml string to objects");
				return null;
			}
			log.info("The rdf string was successfully translated to objects");
			if (platformSubs.getContains() == null || platformSubs.getContains().size() == 0)
			{
				log.warn("The returned platform is empty");
				return null;
			}
			else
			{
				log.debug("Platform name : {}", platformSubs.getContains().toString());
				return platformSubs;

			}

		}

	}



}
