/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa;

/**
 * Different constants that are needed for SFA.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class SFAConstants {

	// RSpec common tags and constants:
	public static final String XMLNS = "xmlns";
	public static final String XMLNS_CC ="xmlns:cc";
	public static final String XMLNS_XSI = "xmlns:xsi";
	public static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
	public static final String TYPE = "type";
	public static final String REQUEST_RSPEC_TYPE = "request";
	
	// PlanetLab tags:
	public static final String NOVI_PL = "novipl";
	public static final String NOVI_PL_SLICE_URN_PREFIX = "urn:publicid:IDN+novipl:novi+slice+";
	public static final String NOVI_PL_HRN_PREFIX = "novipl.novi.";
	public static final String NOVI_PL_AUTHORITY = "novipl.novi";
	public static final String NOVI_PL_USER_URN_PREFIX = "urn:publicid:IDN+novipl:novi+user+";
	
	// NOVI - SFA values:
	public static final String NOVI_IM_URI_BASE = "http://fp7-novi.eu/im.owl#";
	public static final String NOVI_POLICY_URI_BASE = "http://fp7-novi.eu/NOVIPolicyService.owl#";
	public static final String NOVI_FED_HRN_PREFIX = "firexp.novi.";
	public static final String FEDERICA_AUTHORITY = "firexp.novi";
	public static final String FEDERICA_USER_URN_PREFIX = "urn:publicid:IDN+firexp:novi+user+";
	
	// Values that may take the testbed parameter with the service mix.
	public static final String PLANETLAB = "PlanetLab";
	public static final String[] FEDERATED_TESTBEDS = {"PlanetLab", "FEDERICA"};

	// SFA types of registries
	public static final String AUTHORITY = "authority";
	public static final String SLICE = "slice";
	
	// RSpec v1 tags and constants:
	public static final String PL_XMLNS = "http://www.protogeni.net/resources/rspec/3";
	public static final String PL_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String PL_XSI_SCHEMA_LOCATION = "http://www.protogeni.net/resources/rspec/3 " +
		"http://www.protogeni.net/resources/rspec/3/request.xsd";
	public static final String NOVI_PL_COMPONENT_MANAGER_ID = "urn:publicid:IDN+novipl+authority+sa";
	public static final String SITE_ID = "site_id";
	public static final String HRN = "hrn";
	public static final String LOCATION = "location";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
		
	// FEDERICA RSpec v2 tags and constants:
	public static final String FEDERICA = "Federica";
	public static final String EXCLUSIVE = "exclusive";
	public static final String CC_EXCLUSIVE = "cc:exclusive";
	public static final String INTERNET_ID = "urn:publicid:internet";
	public static final String INTERNET_MANAGER_ID = "urn:publicid:authority+internet";
	public static final String SLIVER_ID = "sliver_id";
	public static final String INTERNET_INTERFACE_ID = "urn:publicid:internet:interface";
	public static final String NSWITCH_ID = "urn:publicid:IDN+novi.eu+nswitch";
	public static final String NSWITCH_MANAGER_ID = "urn:publicid:IDN+novi.eu+authority+cm";
	public static final String NSWITCH_INTERFACE_ID = "urn:publicid:IDN+novi.eu+nswitch:interface";
	public static final String MANIFEST = "manifest";
	public static final String SLIVER_TYPE = "sliver_type";
	public static final String VM = "vm";
	public static final String ROUTER = "router";
	public static final String ETHERNET = "ethernet";
	public static final String COMPUTE_CAPACITY = "cc:compute_capacity";
	public static final String DISK_IMAGE = "disk_image";
	public static final String CLIENT_ID = "client_id";
	
	public static final int MAX_LOGICAL_ROUTERS = 15;
	
	public static final String COMPONENT_MANAGER_ID = "component_manager_id";
	public static final String COMPONENT_MANAGER = "component_manager";	
	public static final String INTERFACE = "interface";
	public static final String IP = "ip";
	public static final String ADDRESS = "address";
	public static final String DISK_SIZE = "diskSize";
	public static final String STORAGE = "storage";
	public static final String RAM_SIZE = "ramSize";
	public static final String MEM = "mem";
	public static final String CPU = "cpu";
	public static final String CPU_SPEED = "cpuSpeed";
	public static final String NUM_CPUS = "numCPUCores";
	public static final String GUEST_OS = "guestOS";
	public static final String NAME = "name";
	public static final String COMPONENT = "component";
	
	public static final String USER = "user";
	public static final String NODE = "node";
	public static final String LINK = "link";
	public static final String HW_TYPE = "hardware_type";
	public static final String SERVICES = "services";
	public static final String LOGIN = "login";
	public static final String AUTHENTICATION = "authentication";
	public static final String HOSTNAME = "hostname";
	public static final String PORT = "port";
	public static final String NETMASK = "netmask";
	public static final String RSPEC = "rspec";
	public static final String MAC_ADDRESS = "mac_address";
	public static final String VLAN = "vlantag";
	public static final String COMPONENT_ID = "component_id";
	public static final String COMPONENT_NAME = "component_name";
	public static final String AVAILABLE = "available";
	public static final String AVAILABLE_LOGICAL_ROUTERS = "availableLogicalRouters";
	public static final String ROUTER_CONFIGURATION = "cc:router_configuration";
	
	public static final String FEDERICA_ADVERTISEMENT = "fedad";
	public static final String CAN_FEDERATE = "fedad:canFederate";

	public static final String COMPONENT_HOP = "component_hop";
	public static final String INTERFACE_REF = "interface_ref";
	
	// FEDERICA URN and prefixes
	public static final String FED_COMPONENT_MANAGER_ID = "urn:publicid:IDN+federica.eu+authority+cm";
	public static final String FED_COMPONENT_INTERFACE_PREFIX = "urn:publicid:IDN+federica.eu+interface+";
	public static final String FED_COMPONENT_LINK_PREFIX = "urn:publicid:IDN+federica.eu+link+";
	public static final String FED_COMPONENT_IMAGE_PREFIX = "urn:publicid:IDN+federica.eu+image+";
	public static final String FED_COMPONENT_NODE_PREFIX = "urn:publicid:IDN+federica.eu+node+";
	public static final String FED_COMPONENT_SLIVER_PREFIX = "urn:publicid:IDN+federica.eu+sliver+";
	public static final String FED_SLICE_URN_PREFIX = "urn:publicid:IDN+firexp:novi+slice+";
	
	// FEDERICA RSpec schemas:
	public static final String FED_XMLNS = "http://sorch.netmode.ntua.gr/ws/RSpec";
	public static final String FED_XMLNS_CC = "http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica";
	public static final String FED_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String FED_XSI_SCHEMA_LOCATION_ADV = "http://sorch.netmode.ntua.gr/ws/RSpec " +
		"http://sorch.netmode.ntua.gr/ws/RSpec/ad.xsd " +
		"http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica " +
		"http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica/federica.xsd";
	public static final String FED_XSI_SCHEMA_LOCATION_REQ = "http://sorch.netmode.ntua.gr/ws/RSpec " +
	"http://sorch.netmode.ntua.gr/ws/RSpec/request.xsd " +
	"http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica " +
	"http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica/federica.xsd";	
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"; 
	
	// GENI result parameters:
	public static final String CODE = "code";
	public static final String GENI_CODE = "geni_code";
	public static final String OUTPUT = "output";
	public static final String VALUE = "value";
	
}
