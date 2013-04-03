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

/**
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public enum IpfixSetType {
	INVALID(0,"invalidset"),
	TEMPLATE(2, "tset"),
	OPTIONS_TEMPLATE(3, "otset"),
	DATA(256, "dset");
	private final int value;
	private final String shortname;
	IpfixSetType(int value, String name) {
		this.value = value;
		this.shortname = name;
	}
	public int getValue() {
		return value;
	}
	public String getShortName() {
		return shortname;
	}
	/**
	 * Return respective set type.
	 * 
	 * @param setId
	 * @return
	 */
	public static IpfixSetType getSetType( int setId ){
		if( setId >= IpfixSetType.DATA.value){
			return IpfixSetType.DATA;
		}
		if( setId== IpfixSetType.TEMPLATE.value ){
			return IpfixSetType.TEMPLATE;
		} else if(setId == IpfixSetType.OPTIONS_TEMPLATE.value) {
			return IpfixSetType.OPTIONS_TEMPLATE;
		}
		return IpfixSetType.INVALID;
	}
}
