/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa;

import java.util.Set;

import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;

/**
 * API offered to the rest of NOVI services by the RH.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public interface FederatedTestbed {

	/**
	 * Creates the slice with slice_urn = sliceURN with the topology
	 * described by the user
	 * This method will be called by the RIS
	 * @param user that request the slice
	 * @param sliceURN slice_urn to fulfill
	 * @param topology topology to create in the slice_urn. The nodes where the vnodes are implemented,
	 * should have the following property:
	 * - nodeURN (node.toString() result): testbed+nodeName (e.g: PlanetLab+smilax1.man.poznan.pl)
	 * @return a RHCreateDeleteSliceResponse object containing 
	 * the slice id, if the creation was successful, 
	 * or an error, otherwise
	 */
	public RHCreateDeleteSliceResponseImpl createSlice(String sessionID, NOVIUserImpl user, String sliceURN, TopologyImpl topology);
	
	/**
	 * Changes the resources the slice with slice_urn = sliceURN with the topology
	 * described by the user. The slice sliceURN should exist.
	 * This method will be called by the RIS
	 * @param user that request the slice
	 * @param sliceURN slice_urn to fulfill
	 * @param oldTopology the topology that was previously created
	 * @param newTopology topology to create in the slice_urn
	 * @return a RHCreateDeleteSliceResponse object containing 
	 * the slice id, if the update was successful, 
	 * or an error, otherwise
	 */
	public RHCreateDeleteSliceResponseImpl updateSlice(String sessionID, NOVIUserImpl user, String sliceURN, TopologyImpl oldTopology, TopologyImpl newTopology);
	
	
	/**
	 * list the physical substrate of the testbed. The nodes will have the next information
	 * with the following format:
	 * - nodeURN (node.toString() result): testbed+nodeName (e.g: PlanetLab+smilax1.man.poznan.pl)
	 * - Location (node.getLocation() result): locationFromRSpec-location (e.g: poznan-location)
	 * - hostName as from the RSpec (e.g:  smilax1.man.poznan.pl)
	 * @param user: info needed for obtaining user credentials.
	 * @return a RHListResourcesResponse object containing 
	 * a Platform object containing the resources, if the listing was successful, 
	 * or an error, otherwise
	 */
	public RHListResourcesResponseImpl listResources(String user);
	

	/**
	 * List all the slices that are created on the testbeds under the federation.
	 * 
	 * @return RHListSlicesResponseImpl: boolean hasError, errorMessage if there is error
	 *  and List of Slices if no error.
	 */
	public RHListSlicesResponseImpl listAllSlices();
	
	/**
	 * Delete the slice specified by the sliceURN
	 * @param sliceURN identifies the slice to delete
	 * @param platformURIs a set containing the URIs of the platforms to which the resources in the slice belong to
	 * @param topology TODO
	 * @return a RHCreateDeleteSliceResponse object containing 
	 * the slice id, if the deletion was successful, 
	 * or an error, otherwise
	 */
	public RHCreateDeleteSliceResponseImpl deleteSlice(String sessionID, String sliceURN, Set<String> platformURIs, TopologyImpl topology);
	
	/**
	 * List slices that the user has. 
	 * @param user
	 * @return
	 */
	public RHListSlicesResponseImpl listUserSlices(String user);
	
}
