package eu.novi.resources.discovery.response;

import java.util.HashSet;
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
 * implementation of the interface FPartCostRecord
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FPartCostRecordImpl {

	/**
	 * the virtual nodes URIs that have the same type and consists this record
	 */
	private Set<String> resourceURIs = new HashSet<String>();
	
	/**
	 * the physical nodes URIs that meet this type
	 */
	private Set<String> physicalNodeURIs = new HashSet<String>();
	
	/**
	 * nodes that can be federated
	 */
	private Set<String> federableResourceURIs = new HashSet<String>();






	//private int availResNumber;
	private double averUtil = 0;
	private boolean error = false;
	private String hardwType;
	//private int fedeResNumb = 0;
	
	
	/**create the resourceURIs list and add the first URI
	 * @param resourceURIs
	 */
	/*public FPartCostRecordImpl(String resourceURIs)
	{
		
		this.resourceURIs.add(resourceURIs);
	}*/
	
	public FPartCostRecordImpl()
	{
		
	}
	
	public Set<String> getPhysicalNodeURIs() {
		return physicalNodeURIs;
	}

	public void setPhysicalNodeURIs(Set<String> physicalNodeURIs) {
		this.physicalNodeURIs = physicalNodeURIs;
	}
	
	public Set<String> getFederableResourceURIs() {
		return this.federableResourceURIs;
	}

	public void setFederableResourceURIs(Set<String> federableResourceURIs) {
		this.federableResourceURIs = federableResourceURIs;
	}
	
	//@Override
	/**
	 *  the virtual nodes URIs that have the same type and consists this record
	 * @return
	 */
	public Set<String> getResourceURIs() {
		return resourceURIs;
	}
	
	/** a virtual nodes URI that have the same type with this record
	 * @param uri
	 */
	public void addURI(String uri)
	{
		this.resourceURIs.add(uri);
	}
	
	/** the virtual nodes URIs that have the same type and consists this record
	 * @param resourceURIs
	 */
	public void setResourceURIs(Set<String> resourceURIs) {
		this.resourceURIs = resourceURIs;
	}

	//@Override
	public int takeAvailResNumber() {
		return physicalNodeURIs.size();
	}

	//@Override
	public double getAverUtil() {
		return averUtil;
	}
	
	public int takeFedeResNumb() {
		return federableResourceURIs.size();
	}
	
	/*public void setFedeResNumb(int fedeResNumb) {
		this.fedeResNumb = fedeResNumb;
	}*/
	
	//@Override
	public boolean hasError() {
		return this.error;
	}

	/*public void setAvailResNumber(int availResNumber) {
		this.availResNumber = availResNumber;
	}*/

	public void setAverUtil(double averUtil) {
		this.averUtil = averUtil;
	}
	
	public void setError(boolean hasError)
	{
		this.error = hasError;
	}

	public boolean getError() {
		return this.error;
	}
	
	public String getHardwType() {
		return hardwType;
	}

	public void setHardwType(String hardwType) {
		this.hardwType = hardwType;
	}

	
}
