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
public enum IpfixIeSemantics {
	DEFAULT(0), 
	QUANTITY(1), 
	TOTALCOUNTER(2), 
	DELTACOUNTER(3), 
	IDENTIFIER(4), 
	FLAGS(5),
	UNASSIGNED(6);

	private final int value;
	private static final Logger logger = LoggerFactory.getLogger(IpfixIeSemantics.class);

	IpfixIeSemantics(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	public static IpfixIeSemantics getFromModelName( String  name ){
		if( name==null){
			return DEFAULT;
		}
		try {
			return IpfixIeSemantics.valueOf(name.toUpperCase());
		}catch (Exception e) {
			logger.debug("No ie semantics  matched for \"{}\"",name);
			return IpfixIeSemantics.DEFAULT;
		}
	}

}
