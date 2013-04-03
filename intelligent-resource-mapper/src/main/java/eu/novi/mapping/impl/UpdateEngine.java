/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.osgi.service.log.LogService;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.embedding.EmbeddingAlgorithmInterface;
import eu.novi.mapping.exceptions.MappingException;
import eu.novi.mapping.utils.IMOperations;
import eu.novi.resources.discovery.IRMCalls;

/**
 * The Class responsible for handling updating of slices triggered
 * by users. 
 * @see IRMEngine
 */
public class UpdateEngine extends IRMEngine {

	/** The NOVI user. */
    private NOVIUserImpl noviUser;
	
	/**
     * Constructor of UpdateEngine. It sets all the attributes of the superclass (IRMEngine)
     */
	public UpdateEngine(IRMCalls irmCallsFromRIS, List<RemoteIRM> irms, 
			List<EmbeddingAlgorithmInterface> embeddingAlgorithms, 
			ReportEvent userFeedback, String sessionID, 
			LogService logService, NOVIUserImpl noviUser, 
			String testbed, ScheduledExecutorService scheduler) {
		this.irmCallsFromRIS = irmCallsFromRIS;
		this.irms = irms;
		this.userFeedback = userFeedback;
		this.logService = logService;
		this.testbed = testbed;
		this.embeddingAlgorithms = embeddingAlgorithms;
		this.noviUser = noviUser;
		this.scheduler = scheduler;
	}

	/**
	 * Update slice triggered by NOVI user through NOVI-API.
	 * @param platforms 
	 * @param virtualTopology 
	 * @param currentSlice 
	 *
	 * @return the updated slice topology
	 * @throws MappingException the mapping exception
	 */
	public Topology updateSlice(String sessionID, Reservation currentSlice, Topology virtualTopology, Set<Platform> platforms) throws MappingException {
		
		logService.log(LogService.LOG_DEBUG, 
				"Update Slice triggered by NOVI user "+this.noviUser.getFirstName());
		
		// get new resources
		Topology newResources = getNewResources(currentSlice, virtualTopology);
		
		if (!IMOperations.isSetEmpty(newResources.getContains())) {
			IMOperations.analyzeGroup(newResources, logService);
		}
		
		// continue here 
		
		throw new MappingException("This method is not implemented yet");
	}

	/**
	 * Identifies the new resources in the currentSlice.
	 *
	 * @param currentSlice the current slice
	 * @param virtualTopology the virtual topology
	 * @return the new resources
	 */
	private Topology getNewResources(Reservation currentSlice,
			Topology virtualTopology) {
		
		Topology toReturn = new TopologyImpl("newResources");
		Set<Resource> newResources = new HashSet<Resource>();
		
		for (Resource res : virtualTopology.getContains()) {
			boolean found = false;
			for (Resource currentRes : currentSlice.getContains()) {
				if (res.toString().equals(currentRes.toString())) {
					found=true;
					break;
				}
			}
			if (!found) {newResources.add(res);}
		}
		
		toReturn.setContains(newResources);
		return toReturn;
	}
	
	
	
}
