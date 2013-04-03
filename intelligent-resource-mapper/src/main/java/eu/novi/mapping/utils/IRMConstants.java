/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.utils;

/**
 * IRM Constants
 */
public final class IRMConstants {

	/**
	 * Private constructor
	 */
	private IRMConstants(){}
	
	// IRM Constants
	//public static final String EMBEDDING_SERVICE = "eu.novi.mapping.embedding.EmbeddingAlgorithmInterface";
	public static final String PATH_PREFIX = "urn:publicid:IDN+novi.eu+path+";
	public static final String LINK_PREFIX = "urn:publicid:IDN+novi.eu+link+";
	public static final String IRM_FEEDBACK_URL = "http://www.fp7-novi.eu/";
	public static final String URI_PREFIX = "http://fp7-novi.eu/im.owl#";
	
	//ILS Constants
	public static final int IRM_PARAMETER_ILS = 5;
	public static final int IRM_PARAMETER_LS = 10;
	public static final double IRM_PARAMETER_ILS_NODE_PENALTY = 1000000;
	public static final double IRM_PARAMETER_ILS_LINK_PENALTY = 1000000;
	
	////    FEDERICA Constants
    public static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
    public static final String FEDERICA = "FEDERICA";
    public static final String FEDERICA_HW_TYPE_ROUTER = "genericNetworkDevice";
    public static final String FEDERICA_HW_TYPE_SERVER = "pc";
    public static final String RVINE_ALGORITHM = "RVINE";
    public static final String GSP_ALGORITHM = "GSP";
    public static final String FEDERICA_VNODE_ROLE_ROUTER = "router";
    
	//PlanetLab Constants
    public static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
    public static final String PLANETLAB = "PlanetLab";
    public static final String PLANETLAB_HW_TYPE = "plab-pc";
    public static final String GNM_ALGORITHM = "GNM";
	public static final String PLANETLAB_VSERVER_ROLE = "plab-vserver";
	
}
