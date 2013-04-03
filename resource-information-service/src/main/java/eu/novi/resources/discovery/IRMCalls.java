package eu.novi.resources.discovery;


import java.util.Set;
import java.util.Vector;

import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.resources.discovery.response.FRResponse;
import eu.novi.resources.discovery.response.ReserveResponse;

/**
 * 
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
 * 
 * 
 * an interface for the provided calls to the IRM
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface IRMCalls {
	
	/**
	 * the find resources method.
	 * It accepts unbound, bound and partially bound request.
	 * For bound request actually it checks if the bound nodes are available.
	 * If not then the hasError in the response will be true and also
	 * the not-available nodes will not be in the return topology
	 * @param topology The given topology that contain the requirements
	 * @param noviUser the NOVI user requesting this request
	 * @param sessionID for user feedback
	 * @return a FRResponce object contain the find resources results
	 */
	public FRResponse findResources(String sessionID, Topology topology,  NOVIUserImpl noviUser);
	
	
	/**find new available resources for some failed resources in an existing slice
	 * @param slice the reservation group contain information about the slice
	 * @param failedMachinesURIs the URIs of the physical machine that are failed
	 * @param sessionID for user feedback
	 * @return a FRResponce object contain the find resources results.
	 * The results are only for the virtual
	 * machines that was failed before
	 */
	public FRResponse findLocalResourcesUpdate(String sessionID, Reservation slice, 
			Set<String> failedMachinesURIs);
	
	
	/**it returns the current substrate topology, with the availability values
	 * @param sessionID for user feedback
	 * @return a FRResponce answer containing the substrate
	 */
	public FRResponse getSubstrateAvailability(String sessionID);
	
	
	/**the find local partitioning cost. It gets the partitioning cost
	 * info from all the testbed. It ask all the testbed for all the
	 * resources in the query. If a testbed doesn't have any available or if it
	 * doesn't have this kind of resource then the number values of these resources 
	 * will be zero in the specific testbed.
	 * CURRENTLY THERE IS A FAKE RESPONSE FROM FEDERICA TESTBED
	 * @param requestedTopology the unbound requested topology
	 * @param sessionID for user feedback
	 * @return a list of FPartCostTestbedResponse objects, each one for each one testbed
	 */
	public Vector<FPartCostTestbedResponseImpl> findPartitioningCost(String sessionID, Topology requestedTopology);
	
	
	/**
	 * It reserve the slice to the local testped (bound request).
	 * @param boundRequest the topology for reserved
	 * @param sliceID the slice ID, created by NOVI-API
	 * @param sessionID for user feedback
	 * @return a ReserveResponse object that contain the
	 * information about the reservation
	 */
	public ReserveResponse reserveSlice( String sessionID, final Topology boundRequest, Integer sliceID, NOVIUserImpl user);
	
	/**
	 * It updates an existing slice to the testbed (using Request Handler) 
	 * and in the local NOVI DB. It updates only the information in this local
	 *  NOVI layer
	 * @param boundRequest the updated topology
	 * @param sliceID the slice ID
	 * @param sessionID for user feedback
	 * @return a ReserveResponse object that contain the
	 * information about the reservation
	 */
	public ReserveResponse updateSlice(String sessionID, final Topology boundRequest, Integer sliceID);


	/**get the slice information.
	 * If the slice is not stored locally, then it contacts the remote platform
	 * @param uri -- the URI of the slice
	 * @return the reservation group containing the virtual nodes of the slice.
	 * Or null if it is not found
	 */
	public Reservation getSlice(String uri);
	
	
	/**get the substrate (i.e. planetlab) resources. 
	 * @param uri -- the URI of the substrate
	 * @return the Platform group containing the resources of the substrate.
	 * Or null if it is not found
	 */
	public Platform getSubstrate(String uri);
	


	/**return a resource from the local or from a remote DB;
	 * @param uri -- the URI of the resource
	 * @return the resource or null if it is not found. 
	 */
	public Resource getResource(String uri);
	
	
	/**return the resources with the given URIs from the local or from a remote DB;
	 * @param uris -- the URIs of the resources
	 * @return The founded resources 
	 */
	public Set<Resource> getResources(Set<String> uris);
	
	
	/**check if this resources are in the local or remote RIS DB
	 * @param uris the URIs of the resources
	 * @param user the NOVI user who make the request
	 * @return return the URIs of the resources that were found locally or remotly
	 */
	public Set<String> checkResources(String sessionID, Set<String> uris, NOVIUserImpl user);

	/**
	 * returns the ip address of the nswitch host that the Node is conneected to 
	 * @param node - the Node for which the nswitch host ip address is  requested
	 * @param sliceName - name of the slice that contains the node.
	 * @return ip address 
	 * @throws IllegalArgumentException when node does not fulfil the requirements to find nswitch
	 */
	public String getNSwitchAddress(Node node, String sliceName) throws IllegalArgumentException;


	/**
	 * returns the private ip address of the nswitch endpoint in planetlab sliver 
	 * @param node - the palnetlab node which contains the sliver
	 * @param string - the slice name
	 * @return the private ip address of the nswitch endpoint in planetlab sliver
	 * @throws IllegalArgumentException when node does not fulfil the requiremets to find this private ip
	 */
	public String getPlanetlabPrivateAddressForNSwitchEndpoint(Node node, String string)throws IllegalArgumentException;
	
	
}
