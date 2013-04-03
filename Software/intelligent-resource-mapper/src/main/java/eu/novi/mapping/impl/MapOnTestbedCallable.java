package eu.novi.mapping.impl;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.mapping.RemoteIRM;
import eu.novi.mapping.exceptions.MappingException;

public class MapOnTestbedCallable implements Callable<String> {

	private int id;
	private String partialTopology;
	private String topologyID;
	private String noviUser;
	private RemoteIRM irm;
	private String sessionID;
	
	// Local logging
    private static final transient Logger LOG = LoggerFactory.getLogger(MapOnTestbedCallable.class);
	
	public MapOnTestbedCallable(String sessionID, String partialTopology, String topologyID, 
			String noviUser, RemoteIRM irm, int id) {
		this.id = id;
		this.partialTopology = partialTopology;
		this.topologyID = topologyID;
		this.noviUser = noviUser;
		this.irm = irm;
		this.sessionID = sessionID;
	}
	
	@Override
	public String call() throws MappingException {
		LOG.debug("Mapping Task with ID "+id+" started");
		String partialBound = null;
		partialBound = irm.mapOnTestbed(sessionID, partialTopology,topologyID,noviUser);
		LOG.debug("Mapping Task "+id+" done");
		return  partialBound;
	}

}
