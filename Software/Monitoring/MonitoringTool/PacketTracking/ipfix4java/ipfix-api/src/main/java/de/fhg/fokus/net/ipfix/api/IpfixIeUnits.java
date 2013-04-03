/**
*
* Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
* Copyright according to BSD License
* For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt
*
* @author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
* @author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
* @author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
* @author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
*
*/

package de.fhg.fokus.net.ipfix.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author FhG-FOKUS NETwork Research
 *
 */
public enum IpfixIeUnits {
	NONE(0),
	BITS(1),    
	OCTETS(2),    
	PACKETS(3),    
	FLOWS(4),    
	SECONDS(5),    
	MILLISECONDS(6),    
	MICROSECONDS(7),
	NANOSECONDS(8),
	FOUR_OCTET_WORDS(9),
	MESSAGES(10),
	HOPS(11),   
	ENTRIES(12),
	UNASSIGNED(13);
	private final static Logger logger = LoggerFactory.getLogger(IpfixIeUnits.class);
	private final int value;
	IpfixIeUnits(int value){
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	public static IpfixIeUnits getFromModelName( String  name ){
		if( name==null){
			return NONE;
		}
		try {
			String upper  = name.toUpperCase();
			if(upper.contentEquals("4-OCTET WORDS")){
				return FOUR_OCTET_WORDS;
			}
			return IpfixIeUnits.valueOf(name.toUpperCase());
		}catch (Exception e) {
			logger.debug("No units matched for \"{}\"",name);
			return IpfixIeUnits.NONE;
		}
	}


}
