/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica.utils;

/**
 * Enbedding FEDERICA Constants
 */
public final class EmbeddingConstants {

	/**
	 * Private constructor
	 */
	private EmbeddingConstants(){}
	
	// Embedding Constants
	public static final String TESTBED_NAME = "FEDERICA";
	public static final String GSP_ALGORITHM_NAME = "GSP";
	public static final String NCM_ALGORITHM_NAME = "NCM";
	public static final String FED_ALGORITHM_NAME = "FEDERICAGENERIC";
	public static final String C = "FEDERICA";
	public static final String ROUTER_HW_TYPE = "genericNetworkDevice";
	public static final String ROUTER_VIRTUAL_ROLE = "router";
	public static final String VM_HW_TYPE_1 = "pc";
	public static final String VM_HW_TYPE_2 = "i386";
	public static final String VM_VIRTUAL_ROLE = "vm";
	public static final int NON_FUNCTIONAL_VALUES = 3;
	public static String LINK_MAPPING= "SHORTEST_PATH";
	public static final String EMBEDDING_FEEDBACK_URL = "http://wiki.fp7-novi.eu/";
	public static final String TOP_LEVEL_AUTHORITY= "FEDERICA";
	public static final int CPLEX_TIMEOUT= 600;
	public static final int NODE_MAX_CAPACITY_TYPES= 3;
	
}
