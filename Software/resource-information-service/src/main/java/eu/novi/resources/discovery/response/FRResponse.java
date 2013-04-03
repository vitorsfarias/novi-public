package eu.novi.resources.discovery.response;

import java.util.Map;

import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.resources.discovery.response.FRFailedMess;


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
 * the response from Find Resources call.
 * It contain all the result of find resources
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface FRResponse {
	
	/**see if an error occurred during find resources
	 * @return true if something went wrong during
	 * the find resources call
	 */
	public boolean hasError();
	
	/**get the succeed resources
	 * @return for each succeed resource get the set of available
	 * resouces
	 */
	//public Map<Resource, Set<Resource>> getSucceedResources();
	
	/**get the failed resources
	 * @return for each failed resource get the error message
	 */
	public Map<Resource, FRFailedMess> getFailedResources();
	
	/**get the topology that contain all the resources
	 * @return the Topology. If there is an error then the topology is null
	 */
	public Topology getTopology();
	
	/**Return user feedback. This is not empty in the case that the
	 * user requirements are not met by any available machine. The system 
	 * suggest to user what to change in order his request to be succesful
	 * @return user feedback message or an empty string
	 */
	public String getUserFeedback();

}
