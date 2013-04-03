package eu.novi.mapping.impl;

import java.util.Collection;
import java.util.concurrent.Callable;

import eu.novi.mapping.RemoteIRM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateOnTestbedCallable implements Callable<String> {

	private int id;
	private String slice;
	private String partialTopology;
	private String sliceID;
	private String partialTopologyID;
	private Collection<String> failingResourceIDs;
	private RemoteIRM irm;
	private String sessionID;
	
	// Local logging
    private static final transient Logger log = LoggerFactory.getLogger(UpdateOnTestbedCallable.class);
	
	public UpdateOnTestbedCallable(String sessionID, String slice, String partialTopology, String sliceID, 
			String partialTopologyID, Collection<String> failingResourceIDs, 
			RemoteIRM irm, int id) {
		
		this.id = id;
		this.partialTopology = partialTopology;
		this.slice = slice;
		this.sliceID = sliceID;
		this.partialTopologyID = partialTopologyID;
		this.failingResourceIDs = failingResourceIDs;
		this.irm = irm;
		this.sessionID=sessionID;
	}
	
	@Override
	public String call() throws Exception {
		log.debug("Mapping Task with ID "+id+" started");
		String partialBound = null;
		partialBound = irm.updateOnTestbed(sessionID, slice,partialTopology,sliceID,
				partialTopologyID,failingResourceIDs);
		log.debug("Mapping Task "+id+" done");
		return  partialBound;
	}

}
