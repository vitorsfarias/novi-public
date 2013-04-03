package eu.novi.resources.discovery.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.resources.discovery.database.CheckSliceExpiration;
import eu.novi.resources.discovery.database.ManipulateDB;


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
 * This class will periodically call monitoring to update the resources.
 * 
 *Also provide threads
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class PeriodicUpdate {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(PeriodicUpdate.class);
	
	
	//in minutes. By default periodic monitoring update will be performed every 5 minutes
	private static int periodicMonInterval = 5;
	private static int monInitialDelay = 0;
	
	//request handler for substrate topology, in hours
	private static int periodicRHInterval = 24;
	private static int rhInitialDelay = 12;
	
	//check  for slice expiration, in hours
	private static int periodicSlExpirInterval = 12;
	private static int slExpirInitialDelay = 1;
	



	/**get from blue print.
	 *  The Interface for calling scheduler service.
	 */
	private static ScheduledExecutorService scheduler;// = Executors.newScheduledThreadPool(4);
	
	//private static ScheduledExecutorService myScheduler = Executors.newScheduledThreadPool(1);
	


	private static MonitoringTask monitoringTask = new MonitoringTask();
	
	
	/** 
	 * Triggers periodic updates of monitoring values
	 */
	public static void startMonitoringUpdating() {
		
		
		
		 scheduler.scheduleWithFixedDelay( monitoringTask, monInitialDelay, periodicMonInterval, TimeUnit.MINUTES);
	}
	
	/**
	 * triggers periodic update of the substrate physical topology, calling request handler
	 */
	public static void startSubstrateRHUpdating() {
		
		
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				//the actual code
				log.info("Periodic running of update substrate topology");
				ManipulateDB.updateDBfromTestbed();
			}
		}, rhInitialDelay, periodicRHInterval, TimeUnit.HOURS);
		
		
		
	}
	
	
	/**
	 * triggers periodic checks for slice expiration
	 */
	public static void startExpirationSliceChecks()
	{
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				// the real code to run
				log.info("Periodic running of slice expiration check");
				CheckSliceExpiration.checkSlices();
			}
		}, periodicSlExpirInterval, slExpirInitialDelay, TimeUnit.HOURS);
	}
	
	
	public static <T> Future<T> executeNewThread(Callable<T> task)
	{
		return scheduler.submit(task);
	}
	
	public static void executeNewThread(Runnable task)
	{
		scheduler.submit(task);
	}
	
	///////////SETTERS ///////////
	
	/**set how often the substrate topology will be updated
	 * @param hours
	 */
	public static void setPeriodicRHInterval(int hours)
	{
		periodicRHInterval = hours;
	}
	
	
	/**
	 * Set how long we want to wait between updates of monitoring values
	 * @param minutes
	 */
	public static void setMonPeriodicInterval(int minutes){
			periodicMonInterval = minutes;
	}
	
	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	
	public void setScheduler(ScheduledExecutorService scheduler) {
		PeriodicUpdate.scheduler = scheduler;
	}
	
	/**a static method to get the  ScheduledExecutorService
	 * @return
	 */
	public static ScheduledExecutorService takeSchedulerStatic() {
		return scheduler;
	}
	
	
	
}
