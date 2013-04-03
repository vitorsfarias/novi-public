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

package de.fhg.fokus.net.ipfix.api.codec;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixIeCodec;
import de.fhg.fokus.net.ipfix.api.IpfixIeDataTypes;

/**
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class IpfixIeCodecIpv4Address implements IpfixIeCodec {
	// -- sys --
	private Logger logger = LoggerFactory.getLogger(getClass());
	// -- model --
	protected int fieldLength = getDataType().getDefaultLength();

	public Inet4Address getAddress(ByteBuffer setBuffer) {
		byte[] ba = new byte[4];
		setBuffer.get(ba);
		Inet4Address addr = null;
		try {
			addr = (Inet4Address) Inet4Address.getByAddress(ba);
		} catch (UnknownHostException e) {
			logger.warn("Invalid IPv4 address.", e.toString());
		}
		return addr;
	}

	@Override
	public IpfixIeDataTypes getDataType() {
		return IpfixIeDataTypes.IPV4ADDRESS;
	}
}
