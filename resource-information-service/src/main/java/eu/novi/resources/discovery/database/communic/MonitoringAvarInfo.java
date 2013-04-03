package eu.novi.resources.discovery.database.communic;

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
 * contain the monitoring avarage utilization info for a single node
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class MonitoringAvarInfo {
	
	private String nodeUri;
	private float cpuUtil;
	private float memoryUtil;
	private float storageUtil;
	
	
	public MonitoringAvarInfo(String nodeURI)
	{
		this.nodeUri = nodeURI;
	}
	
	public MonitoringAvarInfo(String nodeURI, float cpuUtil, float memUtil, float stoUtil)
	{
		this(nodeURI);
		this.cpuUtil = cpuUtil;
		this.memoryUtil = memUtil;
		this.storageUtil = stoUtil;
	}
	
	
	public float getCpuUtil() {
		return cpuUtil;
	}
	
	public void setCpuUtil(float cpuUtil) {
		this.cpuUtil = cpuUtil;
	}
	
	public float getMemoryUtil() {
		return memoryUtil;
	}
	
	public void setMemoryUtil(float memoryUtil) {
		this.memoryUtil = memoryUtil;
	}
	
	public float getStorageUtil() {
		return storageUtil;
	}
	
	public void setStorageUtil(float storageUtil) {
		this.storageUtil = storageUtil;
	}
	
	public String getNodeUri() {
		return nodeUri;
	}

}
