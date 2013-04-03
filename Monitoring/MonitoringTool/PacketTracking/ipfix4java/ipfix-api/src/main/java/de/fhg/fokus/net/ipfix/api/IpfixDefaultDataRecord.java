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

import java.nio.ByteBuffer;

import de.fhg.fokus.net.ipfix.util.HexDump;

/**
 * 
 * @author FhG-FOKUS NETwork Research
 *
 */
public class IpfixDefaultDataRecord {
	private final ByteBuffer byteBuffer;
	/**
	 * Slice the set buffer from the current position len bytes long.
	 * 
	 * @param setBuffer Origin byte buffer.
	 * @param len Size of record in octets.
	 */
	public IpfixDefaultDataRecord(ByteBuffer setBuffer, int len ) {
		this.byteBuffer = setBuffer.slice();
		this.byteBuffer.limit(len);
		setBuffer.position(setBuffer.position()+len );
	}
	@Override
	public String toString() {
		byte [] bb = new byte[byteBuffer.remaining()];
		byteBuffer.get(bb);
		String hexdump = HexDump.toHexString( bb, byteBuffer.limit()*2 );
		return String.format("drec:{ len:%d hex: %s }", byteBuffer.limit(),hexdump);
	}
	
}
