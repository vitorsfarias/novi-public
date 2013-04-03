package eu.novi.resources.discovery.remote.serve;



import java.util.Set;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.policy.NOVIUser;
import eu.novi.resources.discovery.database.LockSession;
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
 * All the calls that the RIS offer to the remote RISs
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface RemoteRisServe {
	
	
	
	
	/**lock or unlock the resources in the local testbed
	 * @param lockSession the LockSession that contain the lock information
	 * @param isLock if true then it lock, otherwise unlock
	 */
	public void lockUnlockLocalResources(LockSession lockSession, boolean isLock);
	
	/**it gives the partitioning cost for the local testbed.
	 * It is called by a remote RIS
	 * @param requestedTopology the unbound requested topology in xml/rdf string format
	 * @return a FPartCostTestbedResponse object, that contain
	 * the results
	 */
	public FPartCostTestbedResponseImpl giveLocalPartitioningCost(
			String requestedTopology);

	
	
	/**it return the slice information that is stored locally
	 * @param uri the uri of the slice
	 * @param getManifest if true the it returns the manifest description,
	 * if it false, then it returns the normal slice description
	 * @return  xml/rdf string that contain the Reservation object that
	 *  contain the slice info. Or null if the slice was not found or
	 *  a problem occured
	 */
	public String getLocalSlice(String uri, boolean getManifest);
	

	/**store locally the slice uri and the remote platform
	 * for this slice
	 * @param slice the slice object (Reservation) transformed to xml/rdf string
	 * @param testbed the remote platform that the slice information is stored
	 */
	public void storeLocallyRemoteSliceInfo(String slice, String testbed);
	
	
	/**return a resource that is stored locally
	 * @param uri the URI of the resource
	 * @return the resource or null if it is not found.
	 * The resource object is in xml/rdf string format
	 */
	public String getLocalResource(String uri);
	
	
	/**return a resource that is stored locally
	 * @param uris the URIs of the resources
	 * @return A set contained the founded resources
	 * The resource objects are in xml/rdf string format. 
	 * For not found resources are not sent anything back
	 */
	public Set<String> getLocalResources(Set<String> uris);
	
	
	/**check if the resources are stored locally
	 * @param uris the URIs of the resources
	 * @param user the NOVI user
	 * @return A set contained the URI of the founded resources
	 */
	public Set<String> checkLocalResources(Set<String> uris, String user);
	

	
	/**execute delete slice. Delete the slice from the novi layer and from the testbeds
	 * @param sliceURI  the slice URI
	 * @param user the user who request the deletion
	 * @param userFeedback
	 * @param sessionID
	 * @return true if the information was deleted, false if was not found
	 */
	public boolean deleteLocalSlice(String sliceURI, NOVIUser user, 
			ReportEvent userFeedback, String sessionID);
	
	
	/**dymmy call for testing, if the input is "letSee" then it return "good" otherwise "bad"
	 * @param input
	 * @return
	 */
	public String dummyCall(String input);
	
	public String getTestbed();
	
	public void setTestbed(String testbed);

}
