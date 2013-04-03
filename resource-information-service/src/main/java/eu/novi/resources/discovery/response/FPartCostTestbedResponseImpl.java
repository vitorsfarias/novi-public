package eu.novi.resources.discovery.response;

import java.util.LinkedList;
import java.util.List;


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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FPartCostTestbedResponseImpl {

	private String testbedURI;
	private List<FPartCostRecordImpl> nodeCosts = new LinkedList<FPartCostRecordImpl>();
	private List<FPartCostRecordImpl> linkCosts = new LinkedList<FPartCostRecordImpl>();
	
	
	/*public FPartCostTestbedResponseImpl(String testbedURI)
	{
		this.testbedURI = testbedURI;
	}*/
	
	public FPartCostTestbedResponseImpl()
	{
	}
	
	/**it add a new URI to an existing FPartCostRecord
	 * @param existingURI 
	 * @param newURI
	 * @return true if the existing URI was found otherwise false
	 */
	public boolean addNewURI(String existingURI, String newURI)
	{
		for (FPartCostRecordImpl rec : nodeCosts)
		{
			if (rec.getResourceURIs().contains(existingURI))
			{
				((FPartCostRecordImpl) rec).addURI(newURI);
				return true;
			}
			
		}
		
		for (FPartCostRecordImpl rec : linkCosts)
		{
			if (rec.getResourceURIs().contains(existingURI))
			{
				((FPartCostRecordImpl) rec).addURI(newURI);
				return true;
			}
			
		}
		
		return false;
	}
	
	public void addLinkCosts(FPartCostRecordImpl linkCost) {
		this.linkCosts.add(linkCost);
	}
	
	//////SETTERS GETTERS////
	
	public void setTestbedURI(String testbedURI) {
		this.testbedURI = testbedURI;
	}

	public void setNodeCosts(List<FPartCostRecordImpl> nodeCosts) {
		this.nodeCosts = nodeCosts;
	}

	//@Override
	public String getTestbedURI() {
		return this.testbedURI;
	}

	//@Override
	public List<FPartCostRecordImpl> getNodeCosts() {
		return this.nodeCosts;
	}


	//@Override
	public List<FPartCostRecordImpl> getLinkCosts() {
		return this.linkCosts;
	}

	public void setNodeCosts(FPartCostRecordImpl nodeCost) {
		this.nodeCosts.add(nodeCost);
	}


	
	public void setLinkCosts(List<FPartCostRecordImpl> linkCosts) {
		this.linkCosts = linkCosts;
	}

}
