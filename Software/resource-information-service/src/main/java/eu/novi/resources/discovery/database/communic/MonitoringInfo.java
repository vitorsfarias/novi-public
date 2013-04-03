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
 * contain the monitoring info for a single node
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class MonitoringInfo {
	
	private String nodeUri;
	private float cpuSpeed;
	private int cpuCores;
	private float storage;
	private float memory;
	
	private boolean hasError = false;
	private String errorMessage = "";
	
	public MonitoringInfo(String nodeUri, int cpuCores, float cpuSpeed, float storage, float memory) {
		super();
		this.nodeUri = nodeUri;
		this.cpuCores = cpuCores;
		this.cpuSpeed = cpuSpeed;
		this.storage = storage;
		this.memory = memory;
	}
	
	/**use this when the call to monitoring was failed
	 * @param errorMessage
	 */
	public MonitoringInfo(String nodeUri, String errorMessage) {
		this.hasError = true;
		this.nodeUri = nodeUri;
		this.errorMessage = errorMessage;
	}
	
	
	/**if true the the call to monitoring was failed
	 * @return
	 */
	public boolean isHasError() {
		return hasError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getNodeUri() {
		return nodeUri;
	}
	
	/**
	 * @return the CPU core number or -1 if it is not available
	 */
	public int getCpuCores() {
		return cpuCores;
	}
	
	/**
	 * @return the CPU speed in GHz or -1 if it is not available
	 */
	public float getCpuSpeed() {
		return cpuSpeed;
	}
	
	/**
	 * @return the free disk size in GB or -1 if it is not available
	 */
	public float getStorage() {
		return storage;
	}
	
	/**
	 * @return the free memory size in GB or -1 if it is not available
	 */
	public float getMemory() {
		return memory;
	}

}
