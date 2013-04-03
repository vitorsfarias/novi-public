package eu.novi.resources.discovery.response;

import java.util.Set;


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
 * 
 * a single record for Find Partitioning Cost. It contains the node or link name
 * and the number of of matching resources in Testbed X and the  
 * average (weighted) utilization on testbed X
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public interface FPartCostRecord {
	
	/**get the resource URIs of all the resources of this type.
	 * This are the resources that is in the requested topology
	 * @return
	 */
	public Set<String> getResourceURIs();
	
	/**get the number of matching resources for this specific
	 * resources in the current Testbed
	 * @return
	 */
	public int getAvailResNumber();
	
	/**get the average (weighted) utilization of this 
	 * specific resource on the current testbed X
	 * @return
	 */
	public double getAverUtil();
	
	
	/**see if something went wrong with this resource.
	 * if true then the lists probably will be empty
	 * @return true if something went wrong
	 */
	public boolean hasError();
	
	
	/**get the type of this record.
	 * For a virtual node is the hardwareType, for a link is the capacity in a string format
	 * @return
	 */
	public String getType();

}
