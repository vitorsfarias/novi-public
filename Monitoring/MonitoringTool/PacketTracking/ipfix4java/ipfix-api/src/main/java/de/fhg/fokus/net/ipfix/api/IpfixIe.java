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
public interface  IpfixIe {
	/**
	 * Value reserved for variable-length Information Elements.
	 */
	public int VARIABLE_LENGTH = 65535;
	public String getName();
	public IpfixIeDataTypes getDataType();
	public IpfixIeSemantics getSemantics();
	public IpfixIeStatus getStatus();
	public IpfixIeUnits getUnits();
	public IpfixFieldSpecifier getFieldSpecifier();
	
	/**
	 * Number of octets used for encoding this IPFIX IE.
	 * @return length or {@link IpfixIe#VARIABLE_LENGTH}
	 */
	public int getLength();

	public static class Util{
		public static String toString(IpfixIe ie) {
			return String.format("%s:%d", ie.getName(),ie.getLength());
		}
	}

}
