package eu.novi.mapping;

import java.util.Collection;

public interface RemoteIRM {
	
	/**
	 * Gets the testbed under the IRM.
	 *
	 * @return the testbed
	 */
	String getTestbed();
	
	/**
	 * Re-maps an already existing slice into available physical resources in
	 * the federated testbed. It is triggered by resource failures.
	 * 
	 * @param currentSlice
	 *            the slice to be updated
	 * @param failingResources
	 *            the ID's of the failing virtual resources to be replaced
	 * @return IDs of the new physical resources of the slice. Empty collection
	 *         if there is a failure
	 */
	Collection<String> updateSlice(String sessionID, String currentSlice,
			Collection<String> failingResources);
	
	/**
	 * Allocate virtual topology into physical resources in the REMOTE testbed.
	 *
	 * @param partialTopology virtual resources to allocate in the testbed in xml format
	 * @param id the ID of the partialTopology
	 * @param stringNoviUser the string novi user
	 * @return (string) bounded topology in the testbed
	 */
	String mapOnTestbed(String sessionID, String partialTopology, String id, String stringNoviUser);
	
	String updateOnTestbed(String sessionID, String slice, String partial, String sliceID, String partialTopologyID,
			Collection<String> failingResourceIDs);
	
	String getTestbedTopLeveAuthority();
}
