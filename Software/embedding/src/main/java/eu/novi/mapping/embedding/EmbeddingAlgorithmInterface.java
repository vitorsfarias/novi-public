/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding;

import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;

/**
 * This module provides an interface for the embedding algorithms that will be 
 * used for solving in the inter-domain VNE problem. It is testbed specific. 
 * 
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
public interface EmbeddingAlgorithmInterface {

	List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID,  SparseMultigraph<NodeImpl, LinkImpl> virtualTopology, 
			SparseMultigraph<NodeImpl, LinkImpl> resources);
			
	List<Map<ResourceImpl,ResourceImpl>> embed(String sessionID, GroupImpl partialTopology, NOVIUserImpl noviUsers);
    
	/**
	 * Gets the testbed name where the algorithm is located
	 * 
	 * @return Testbed name
	 */
	String getTestbedName();
	
	String getTestbedTopLeveAuthority();
	
	/**
	 * Gets the name of the algorithm
	 * 
	 * @return The name of the current Algorithm
	 */
	String getAlgorithmName();
	
	/** testing who are you **/
	String whoAreYou(String question);
	
}
