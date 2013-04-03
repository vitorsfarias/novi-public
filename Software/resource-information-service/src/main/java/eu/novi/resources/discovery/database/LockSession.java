package eu.novi.resources.discovery.database;

import java.util.List;
import java.util.Vector;


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
 * It contains all the information about a lock session for all the platforms
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class LockSession {
	
   // private static final transient Logger log = 
	//		LoggerFactory.getLogger(LockSession.class);
    
    private Integer sessionID;
    private TestbedLock localTestebed = new TestbedLock();
    private TestbedLock remoteTestbed = new TestbedLock();

    public LockSession(Integer sessionID)
    {
    	this.sessionID = sessionID;
    	
    }
    

	public TestbedLock getLocalTestebed() {
		return localTestebed;
	}



	public TestbedLock getRemoteTestbed() {
		return remoteTestbed;
	}

    public Integer getSessionID() {
		return sessionID;
	}
    
    
    /**contain the lock information for one testbed
     * @author chariklis
     *
     */
    public class TestbedLock {
    	private List<String> routers = new Vector<String>();
    	private List<LockSession.ServerLock> servers = new Vector<LockSession.ServerLock>();
    	
    	public void addRouter(String uri)
    	{
    		routers.add(uri);
    	}
    	
    	public void addServer(ServerLock server)
    	{
    		servers.add(server);
    	}
    	
    	/**check if this tesbed lock doesn't contain any information
    	 * @return true if it is empty
    	 */
    	public boolean isEmpty()
    	{
    		if (routers.isEmpty() && servers.isEmpty())
    		{
    			return true;
    		}
    		else
    		{
    			return false;
    		}
    	}
    	
		public List<String> getRouters() {
			return routers;
		}
		public List<LockSession.ServerLock> getServers() {
			return servers;
		}
    	
    }
    
    /**it contains information for a server lock
     * @author chariklis
     *
     */
    public class ServerLock {
    	
    	private String serverURI;
    	private float memory;
    	private float  storage;
    	private int cpu;
    	
    	public ServerLock(String serverURI, float memory, float storage, int cpu)
    	{
    		this.serverURI = serverURI;
    		this.memory = memory;
    		this.storage = storage;
    		this.cpu = cpu;
    	}

		public String getServerURI() {
			return serverURI;
		}

		public float getMemory() {
			return memory;
		}

		public float getStorage() {
			return storage;
		}

		public int getCpu() {
			return cpu;
		}
    	
    	
    	
    }

}
