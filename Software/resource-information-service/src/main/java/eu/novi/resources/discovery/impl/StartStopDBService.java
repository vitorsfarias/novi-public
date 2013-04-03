package eu.novi.resources.discovery.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Reservation;
import eu.novi.resources.discovery.database.ConnectionClass;
import eu.novi.resources.discovery.database.IRMLocalDbCalls;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.ManipulateDB;
import eu.novi.resources.discovery.database.ReserveSlice;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;
import eu.novi.resources.discovery.remote.discovery.RemoteRisDiscoveryImpl;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;
import eu.novi.resources.discovery.util.RisSystemVariables;
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
 * For the service mix. Is the bean that start and stop the DB engine.
 * It initializes the systems and it hold some system variables
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 * 
 */
public class StartStopDBService {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(StartStopDBService.class);
	
    /**
     * Testbed name on which the service is running e.g. PlanetLab, FEDERICA.
     */
    private String testbed;
    
    private List<RemoteRisServe> remoteRISList ; //get for blueprint
    //public static final String PLANETLAB = "PlanetLab";
    //public static final String FEDERICA = "FEDERICA";
    //private TestbedCommunication testbedComm; //get for service mix
	



	/**
	 * Initialize default Database.
	 */
	public void initDefaultDatabase(){
		ConnectionClass.startStorageService(true);
	}
	
	
	/**
	 * Initialize testing Database.
	 * Clean the db and Load the
	 * PLEtopologyModified.owl file 
	 */
	public void initTestDatabase() {
		log.info("Initializing RIS ...");
		ConnectionClass.startStorageService(false);
		ReserveSlice.storeOwlFiles = true;

		RemoteRisDiscoveryImpl.staticSetRemoteRISList(remoteRISList);

		//call Request Handler

		log.info("I will call the Request Handler to Update the DB with the testbed advertisment");
		boolean answer = ManipulateDB.updateDBfromTestbed();
		if (!answer)
		{
			log.warn("Fail to update the substrate information from the request handler. I will use" +
					"hard coded values");
			//hardCodeUpdate();

		}



		log.info("Start the periodic update of monitoring values");
		RisSystemVariables.setUpdateMonValuesPeriodic(true); //if false then it run but it does nothing
		PeriodicUpdate.setMonPeriodicInterval(8); //run every 8 minutes
		PeriodicUpdate.startMonitoringUpdating();
		
		PeriodicUpdate.startExpirationSliceChecks();
		PeriodicUpdate.startSubstrateRHUpdating();
		

	}
	

	
	
	/**
	 * update the DB from owl files
	 */
	/*private void hardCodeUpdate()
	{
		log.warn("I will update the DB from OWL files");
		////hard coded values//////
		ManipulateDB.clearTribleStoreTestDB();
		if (testbed == null)
		{
			log.warn("The testbed values is null. I will not populate the DB");
		}
		else if (testbed.equals(Testbeds.PLANETLAB))
		{
			log.info("RIS is running on Planetlab site");
			ManipulateDB.loadOwlFileTestDB("PLETopologyNew.owl", 
					ManipulateDB.getTestbedContextURI());
		}
		else if (testbed.equals(Testbeds.FEDERICA))
		{
			log.info("RIS is running on FEDERICA instance");
			ManipulateDB.loadOwlFileTestDB("FEDERICATopologyNew.owl", 
					ManipulateDB.getTestbedContextURI());

		}
		else
		{
			log.warn("The testbed values is {}. I will not populate the DB", testbed);
		}
		
	}*/
	
	/**
	 *Stop the database service. 
	 */
	public void destroyDatabase()
	{
		ConnectionClass.stopStorageService();
	}
	
	/////////////////////////////////////////////////////////////
	/////GETTERS AND SETTERS////////////////////////////////////
	/////////////////////////////////////////////////////////////
	
	public String getTestbed() {
		return testbed;
	}


	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}
	
	public List<RemoteRisServe> getRemoteRISList() {
		return remoteRISList;
	}


	public void setRemoteRISList(List<RemoteRisServe> remoteRISList) {
		this.remoteRISList = remoteRISList;
	}
	
	/*public TestbedCommunication getTestbedComm() {
		return testbedComm;
	}


	public void setTestbedComm(TestbedCommunication testbedComm) {
		this.testbedComm = testbedComm;
	}
*/

}
