/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping;

import java.util.Collection;

import eu.novi.im.core.Group;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;

/**
 * The IRM interface will attend the incoming calls to the IRM. 
 * CreateSlice() operation will inquire the RIS interface 
 * (findResources()), in order to obtain a resource set of physical 
 * resources mapping the description of the resources in the virtual 
 * topology provided to the IRM based on functional attributes. 
 */
public interface IRMInterface {
    
	/**
	 * This process topology is what will be called whenever a message arrived.
	 * The session ID is 128 bit UUID generated, which uniquely identifies the
	 * timeline for user feedback.
	 *
	 * @param groups the groups to be processed
	 * @param sessionID the session ID
	 * @param noviUser the novi user
	 */
	void processGroups(Collection<Group> groups, String sessionID, NOVIUserImpl noviUser);
	
	/**
	 * Maps topology requirements into physical resources in the federated testbed.
	 *
	 * @param groups the groups containing the Topology and Platforms (if any)
	 * @param noviUser the NOVI user creating the slice
	 * @return ID of the slice with the mapped physical resources
	 */
	int createSlice(String sessionID, Collection<GroupImpl> groups, NOVIUserImpl noviUser);	
	
	/**
	 * Re-maps an already existing slice into available physical resources in the
	 * federated testbed. It is triggered manually by a NOVI user.
	 *
	 * @param sliceID the ID of the slice to modify
	 * @param updatedSliceGroups the groups containing the new Topology and Platforms (if any)
	 * @param noviUser the NOVI user creating the slice
	 * @return ID of the updated slice
	 */
	int updateSlice(String sessionID, String sliceID, Collection<GroupImpl> updatedSliceGroups, NOVIUserImpl noviUser);
	
	/**
	 * Allocate virtual topology into physical resources in the LOCAL testbed.
	 *
	 * @param partialTopology virtual resources to allocate in the local testbed
	 * @param noviUser the novi user
	 * @return bounded topology in the local testbed
	 */
	GroupImpl mapOnTestbed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUser);
	
	/**
	 * Re-allocate virtual topology into physical resources in the LOCAL testbed.
	 * 
	 * @param slice slice to reallocate
	 * @param partialTopology virtual resource to reallocate in the local testbed
	 * @param failingResources failing physical resources
	 * @return updated bounded topology in the local testbed
	 */
	GroupImpl updateOnTestbed(String sessionID, GroupImpl slice, GroupImpl partialTopology,
			Collection<String> failingResources);
	
	/**
	 * Dummy mapOnTestbed for distributed testing.
	 *
	 * @param req the request
	 * @param subs the substrate
	 * @return the string with the result
	 */
	String mapOnTestbed(TopologyImpl req, PlatformImpl subs);
	
}