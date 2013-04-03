package eu.novi.resources.discovery.response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.util.IMCopy;

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
 * the implementation of the response from Find Resources call.
 * It contain all the result of find resources
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FRResponseImp implements FRResponse {
	
	//private static final transient Logger log = 
	//		LoggerFactory.getLogger(FRResponseImp.class);


	//private Map<Resource, Set<Resource>> succeedResources;
	

	private Map<Resource, FRFailedMess> failedResources;
	
	private Topology topology = null;
	
	private String userFeedback = "";
	


	public FRResponseImp()
	{
		failedResources = new HashMap<Resource, FRFailedMess>();
		
	}
	
/*	public void setSucceedResources(Map<Resource, Set<Resource>> succeedResources) {
		this.succeedResources = succeedResources;
	}*/

	public void setFailedResources(Map<Resource, FRFailedMess> failedResources) {
		this.failedResources = failedResources;
	}

	public void setTopology(Topology top){
		this.topology = top;
	}
	
	/**
	 * it creates
	 * a topology that contains the resources. It uses the IMCopy to translate
	 * the given alibaba objects to the implemented object
	 * you have to call this function after the setFailedResources
	 * @param resources
	 */
	public void setTopology(Set<Resource> resources)
	{

		
		Topology tempTopology = new TopologyImpl("returnTopology");
		Set<Resource> copiedRes = new HashSet<Resource>();
		IMCopy copier = new IMCopy();
		for (Resource r: resources)
		{ 
			//the depth is 2 so will create the resources and 
			//any objects that are directly attached to the resources
			copiedRes.add((Resource)copier.copy(r, -1));

		}
		tempTopology.setContains(copiedRes); 
		
		//the depth is 2 so will create the resources in the topology and 
		//any objects that are directly attached to the resources
		topology = tempTopology;
		
	}
	
	/**set the topology from a platform
	 * it is used for federica 
	 * @param platform
	 */
	public void setFedericaTopology(Platform platform)
	{
		if (platform != null)
		{
			topology = new TopologyImpl(platform.toString());
			topology.setContains(platform.getContains());
		}
	}

	
	@Override
	public boolean hasError() {
		if (!failedResources.isEmpty() || topology == null)
			return true;
		else return false;
	}

/*	@Override
	public Map<Resource, Set<Resource>> getSucceedResources() {
		return succeedResources;
	}*/

	@Override
	public Map<Resource, FRFailedMess> getFailedResources() {
		return failedResources;
	}

	@Override
	public Topology getTopology() {
		return topology;
	}
	
	@Override
	public String getUserFeedback() {
		return userFeedback;
	}

	public void setUserFeedback(String userFeedback) {
		this.userFeedback = userFeedback;
	}

}
