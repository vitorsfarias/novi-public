package eu.novi.resources.discovery.remote.discovery;

import java.util.List;
import java.util.Set;
import java.util.Vector;


import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.NOVIUser;
import eu.novi.resources.discovery.database.LockSession;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.response.FPartCostTestbedResponse;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;



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
 * All the calls that the RIS provide for the remote discovery
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface RemoteRisDiscovery {
	
	
	
	
	
	/**lock or unlock the resources in the remote testbed
	 * @param lockSession the LockSession that contain the lock information
	 * @param isLock if true then it lock, otherwise unlock
	 */
	public void lockUnlockRemoteResources(LockSession lockSession, boolean isLock);
	
	/**store to the remote platform the slice uri and the remote platform
	 * (this platform). Not used
	 * @param slice 
	 */
	public void storeRemoteSlice(ReservationImpl slice);

	
	/**get a slice that is stored in the remote platforms.
	 * It checks all the remote platforms
	 * @param uri the uri of the slice
	 * @return the reservation object or null if an error occur
	 */
	public Reservation getRemoteSlice(String uri);
	
	
	/**get the slice manifest that is stored in the remote platform in an rdf/xml format
	 * @param uri the uri of the slice
	 * @return the slice in an rdf/xml description, or null if it is not found
	 */
	public String getRemoteSliceOwl(String uri);

	
	/**it find the partitioning cost for all the remote testbeds.
	 * @param requestedTopology the unbound requested topology
	 * @return a list of FPartCostTestbedResponse objects, each one for each 
	 * remote one testbed. if an error occur then the vector is empty
	 */
	public Vector<FPartCostTestbedResponseImpl> findRemotePartitioningCost(
			TopologyImpl requestedTopology);
	
	
	
	/**get a resource that is stored in a remote platform.
	 * It looks at all the remote testbeds
	 * @param uri the URI of the resource
	 * @return the resource object or null
	 */
	public ResourceImpl getRemoteResource(String uri);
	
	
	/**get resources that are stored in a remote platform
	 * @param uris the URIs of the resources
	 * @return a Set containing the resources object.
	 * For the resources that are not found nothing is returned.
	 * if a problem occur, it return null
	 */
	public Set<ResourceImpl> getRemoteResources(Set<String> uris);
	
	
	/**check if these resources = are stored in a remote platform
	 * @param uris the URIs of the resources
	 * @param user the NOVI  user asking for the bound request
	 * @return a Set containing the URIs of the found resources.
	 * if a problem occur, it return null
	 */
	public Set<String> checkRemoteResources(Set<String> uris, String user);
	
	
	
	/**Call the remote RIS to execute delete slice.
	 * It calls all the remote RISs
	 * @param sliceURI the slice URI
	 * @param user
	 * @param userFeedback
	 * @param sessionID
	 * @return true if the information was deleted, false if was not found
	 */
	public boolean deleteRemoteSlice(String sliceURI, NOVIUser user, 
			ReportEvent userFeedback, String sessionID);


	public void setTestbed(String testbed);
	
	public String getTestbed();
	
	public List<RemoteRisServe> getRemoteRISList();

	public void setRemoteRISList(List<RemoteRisServe> remoteRISList);

	public List<String> getRemoteRisServeTestbed();
}
