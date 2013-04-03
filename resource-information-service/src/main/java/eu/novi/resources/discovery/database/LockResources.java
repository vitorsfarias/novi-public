package eu.novi.resources.discovery.database;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.resources.discovery.database.LockSession.ServerLock;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscovery;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.NoviRisValues;
import eu.novi.resources.discovery.util.Testbeds;


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
 * In reserve slice, it locks the resources for not shared testbeds, before to call RH
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class LockResources {
    private static final transient Logger log = 
			LoggerFactory.getLogger(LockResources.class);
    
    private RemoteRisDiscovery remoteRis = new RemoteRisDiscoveryImpl();
    private final static Semaphore serverSemaphore;
    private final static Semaphore routerSemaphore;
    static {
    	serverSemaphore = new Semaphore(1);
    	routerSemaphore = new Semaphore(1);
    	};
    
    
    /**start a lock session and lock the resources in the NOVI federation (local and remote ris)
     * @param boundTopology the slice bound topology
     * @param sliceID the slice ID
     * @return a LockSession containing all the lock information
     */
    public LockSession startLockResources(Topology boundTopology, Integer sliceID)
    {
    	log.info("Locking the resources for the slice with ID {}...", sliceID);
    	final LockSession lockSession = createLockSession(boundTopology, sliceID);
    	Future<Boolean> remoteLock = null;
    	if (lockSession.getRemoteTestbed().isEmpty())
    	{
    		log.info("There is no resource to be lock, in the remote testbed");
    	}
    	else
    	{
    		log.info("There are resources to be lock, in the remote testbed");
    		////call the remote testbed to lock resources///
    		remoteLock = PeriodicUpdate.executeNewThread(
    				new Callable<Boolean>() {

    					@Override
    					public Boolean call() throws Exception {
    						log.info("Calling the remote testbed using new thread");
    						remoteRis.lockUnlockRemoteResources(lockSession, true);
    						log.info("The lock resources to the remote testbed was done");
    						return true;
    					}

    				});
    	}
    	
    	//lock local resources
    	lockUnlockLocalResources(lockSession, true, true);
    	if (remoteLock != null)
    	{
    		log.info("I am going to wait for the remote lock");
    		try {
				remoteLock.get();
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
			}
    		
    	}
    	return lockSession;
    	
    }
    
    
    /**start an unlock session.
     * Unlock the resources from the local and the remote testbeds
     * @param lockSession
     */
    public void startUnlockResources(final LockSession lockSession)
    {
    	log.info("Unlocking the resources for the slice with ID {}...", lockSession.getSessionID());
    	Future<Boolean> remoteUnLock = null;
    	if (lockSession.getRemoteTestbed().isEmpty())
    	{
    		log.info("There is no resource to be unlock, in the remote testbed");
    	}
    	else
    	{
    		log.info("There are resources to be unlock, in the remote testbed");
    		////call the remote testbed to lock resources///
    		remoteUnLock = PeriodicUpdate.executeNewThread(
    				new Callable<Boolean>() {

    					@Override
    					public Boolean call() throws Exception {
    						log.info("Calling the remote testbed using new thread");
    						remoteRis.lockUnlockRemoteResources(lockSession, false);
    						log.info("The unlock resources to the remote testbed was done");
    						return true;
    					}

    				});
    	}
    	
    	//unlock local resources
    	lockUnlockLocalResources(lockSession, false, true);
    	if (remoteUnLock != null)
    	{
    		log.info("I am going to wait for the remote unlock");
    		try {
				remoteUnLock.get();
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			} catch (ExecutionException e) {
				log.warn(e.getMessage());
			}
    		
    	}
    	
    	
    }
    
    
    
    /**it creates the lock session for this reservation
     * @param boundTopology 
     * @param sliceID
     * @return a LockSession object, containing all the lock information
     */
    protected LockSession createLockSession(Topology boundTopology, Integer sliceID)
    {
    	log.info("Creating the lock session");
    	LockSession lockSession = new LockSession(sliceID);
    	
    	for (Resource res : boundTopology.getContains())
    	{
    		if (res instanceof VirtualNode)
    		{//virtual node
    			VirtualNode vNode = (VirtualNode) res;
    			log.debug("Checking virtualNode {}", res.toString());
    			Set<Node> phNodes = vNode.getImplementedBy();
    			if (phNodes == null || phNodes.isEmpty())
    			{
    				log.warn("The virtual node {} doesn't have implemented by", res.toString());
    				continue;
    			}
    			
    			Node phN = phNodes.iterator().next();
    			if (Testbeds.isThisTestbedUri(phN.toString()))
    			{
    				log.info("The physical machine {} belongs to this testbed", phN.toString());
    				addMachine(phN, vNode, lockSession, true);
    			
    			}
    			else
    			{
    				log.info("The physical machine {} belongs to the remote testbed", phN.toString());
    				addMachine(phN, vNode, lockSession, false);
    			
    			}
    			
    			
    		}
    	}
    	
    	return lockSession;
    	
    }
    
    
    
    /**lock or unlock the local resources.
     * If the testbed is shared (like planetLab) then it doesn't lock or unlock
     * the resources
     * @param lockSession the lock information
     * @param isLock if true then it  lock, otherwise unlock
     * @param isLocal if true, then it read the local info from the lockSession, otherwise
     * it read the remote info form the lockSession
     */
    public void lockUnlockLocalResources(LockSession lockSession, boolean isLock, boolean isLocal)
    {
    	if (isLock)
    	{
    		log.info("Locking the local resources for session {}", lockSession.getSessionID());
    	}
    	else
    	{
    		log.info("Unlocking the local resources for session {}", lockSession.getSessionID());
    	}
    	
    	if (Testbeds.isSharedTestbed())
    	{
    		log.info("This testbed is shared tesbed, there is no need to lock resources");
    		return ;
    	}
    	
    	List<String> routers;
    	List<ServerLock> servers;
    	
    	if (isLocal)
    	{
    		log.info("This is a local lock or unlock");
    		routers = lockSession.getLocalTestebed().getRouters();
    		servers = lockSession.getLocalTestebed().getServers();
    	}
    	else
    	{
    		log.info("This is a remote lock or unlock");
    		routers = lockSession.getRemoteTestbed().getRouters();
    		servers = lockSession.getRemoteTestbed().getServers();
    	}
    	
    	ObjectConnection con = ConnectionClass.getNewConnection();
    	//lock/unlock routers
    	for (String rout : routers)
    	{
    		log.info("Processing router {}", rout);
    		log.debug("waiting for acquire router semaphore");
    		try {
				routerSemaphore.acquire();
				log.debug("The router semaphor was acquired");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Node router = IRMLocalDbCalls.getNodefromDB(rout, con, NoviUris.getSubstrateContextName());
    		if (router == null)
    		{
    			log.warn("The router was not found");
    			routerSemaphore.release();
    			log.debug("Release router semaphor");
    			continue;
    		}
    		else
    		{
    			Integer avLogR = router.getHasAvailableLogicalRouters();
				if (avLogR == null)
				{
					log.warn("The router {} doesn't have available logical router", rout);
	    			routerSemaphore.release();
	    			log.debug("Release router semaphor");
					continue;
				}
				
    			if (isLock)
    			{
    				
    				log.info("Lock: The router has {} logical router, and I will do it {}",
    						avLogR, avLogR -1);
    				router.setHasAvailableLogicalRouters(avLogR - 1);
    				
    			}
    			else
    			{
    				log.info("Unlock: The router has {} logical router, and I will do it {}",
    						avLogR, avLogR + 1);
    				router.setHasAvailableLogicalRouters(avLogR + 1);
    			}
    		}
    		
			routerSemaphore.release();
			log.debug("Release router semaphor");
    	}
    	
    	//lock/unlock servers
    	for (ServerLock servInf : servers)
    	{
    		if (isLock)
    		{
    			log.info("Locking the server {}", servInf.getServerURI());
    		}
    		else
    		{
    			log.info("Unlocking the server {}", servInf.getServerURI());
    		}
    		
    		log.debug("Waiting to acquire server semaphor");
    		try {
				serverSemaphore.acquire();
				log.debug("Acquire server semaphor");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Node server = IRMLocalDbCalls.getNodefromDB(servInf.getServerURI(), con, 
    				NoviUris.getSubstrateContextName());
    		if (server == null)
    		{
    			log.warn("The server {} was not found", servInf.getServerURI());
    			serverSemaphore.release();
    			log.debug("Release server semaphor");
    			continue;
    		}
    		
    		Set<NodeComponent> components = server.getHasComponent();
    		
    		if (components == null)
    		{
    			log.warn("The server {} doesn't have components", server.toString());
    			serverSemaphore.release();
    			log.debug("Release server semaphor");
    			continue;
    		}
    		
    		for (NodeComponent comp : components)
    		{
    			if (comp instanceof Memory)
    			{
    				Float mem = ((Memory) comp).getHasAvailableMemorySize();
    				if (mem != null)
    				{
    					if (isLock)
    					{
    						log.debug("Lock memory, for server {}", servInf.getServerURI());
    						((Memory) comp).setHasAvailableMemorySize(mem - servInf.getMemory());
    					}
    					else
    					{
    						log.debug("UnLock memory, for server {}", servInf.getServerURI());
    						((Memory) comp).setHasAvailableMemorySize(mem + servInf.getMemory());
    					}
    				}
    				
    			}
    			else if (comp instanceof Storage)
    			{
    				Float sto = ((Storage) comp).getHasAvailableStorageSize();
    				if (sto != null)
    				{
    					if (isLock)
    					{
    						log.debug("Lock storage, for server {}", servInf.getServerURI());
    						((Storage) comp).setHasAvailableStorageSize(sto - servInf.getStorage());
    					}
    					else
    					{
    						log.debug("UnLock storage, for server {}", servInf.getServerURI());
    						((Storage) comp).setHasAvailableStorageSize(sto + servInf.getStorage());
    						
    					}
    					
    				}
    				
    			}
    			else if (comp instanceof CPU)
    			{
    				BigInteger cpu = ((CPU) comp).getHasAvailableCores();
    				if (cpu != null)
    				{
    					if (isLock)
    					{
    						log.debug("Lock CPU cores, for server {}", servInf.getServerURI());
    						((CPU) comp).setHasAvailableCores(cpu.subtract(
    								BigInteger.valueOf(servInf.getCpu())));
    					}
    					else
    					{
    						log.debug("UnLock CPU cores, for server {}", servInf.getServerURI());
    						((CPU) comp).setHasAvailableCores(cpu.add(
    								BigInteger.valueOf(servInf.getCpu())));
    					}
    				}
    				
    			}
    			else
    			{
    				log.warn("No acceptable component {}", comp.toString());
    			}
    		}
			serverSemaphore.release();
			log.debug("Release server semaphor");
    			
    	}
    	ConnectionClass.closeAConnection(con);
    }
    
    
    /**add a machine to the lockSession
     * @param machine physical machine
     * @param vNode the virtualNode that implementedBy the physical machine
     * @param lockSession
     * @param isLocal if true, then the machine is in the local testbed
     */
    private void addMachine(Node machine, VirtualNode vNode, LockSession lockSession, boolean isLocal)
    {
    	if (NoviRisValues.isRouter(machine.getHardwareType()))
    	{//router
    		log.debug("The machine {} is a router", machine.toString());
    		if (isLocal)
    		{
    			lockSession.getLocalTestebed().addRouter(machine.toString());
    		}
    		else
    		{
    			lockSession.getRemoteTestbed().addRouter(machine.toString());
    		}
    	}
    	else
    	{//server
    		float mem = 0;
    		float sto = 0;
    		int cpu = 0;
    		log.debug("The machine {} is a server", machine.toString());
    		Set<NodeComponent> components = vNode.getHasComponent();
    		if (components != null)
    		{
    			for (NodeComponent comp : components)
        		{
        			if (comp instanceof Memory)
        			{
        				Float memory = ((Memory)comp).getHasMemorySize();
        				if (memory != null)
        				{
        					mem = memory;
        				}
        			
        			}
        			else if (comp instanceof Storage)
        			{
        				Float storage = ((Storage) comp).getHasStorageSize();
        				if (storage != null)
        				{
        					sto = storage;
        				}
        				
        			}
        			else if (comp instanceof CPU)
        			{
        				BigInteger cpuC = ((CPU) comp).getHasCores();
        				if (cpuC != null)
        				{
        					cpu = cpuC.intValue();
        				}
        				
        			}
        			else
        			{
        				log.warn("Wrong node component {}", comp.toString());
        			}
        		}
    		}
    		else
    		{
    			log.warn("The machine {} dosn't have any component", machine.toString());
    		}
    		
    		
    		if (isLocal)
    		{
    			lockSession.getLocalTestebed().addServer(
    					lockSession.new ServerLock(machine.toString(), mem, sto, cpu));
    		}
    		else
    		{
    			lockSession.getRemoteTestbed().addServer(
    					lockSession.new ServerLock(machine.toString(), mem, sto, cpu));
    		}
    		
    	}
    	
    }

}
