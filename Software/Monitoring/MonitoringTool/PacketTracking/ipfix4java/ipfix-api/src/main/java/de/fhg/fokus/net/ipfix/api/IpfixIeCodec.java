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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.util.ByteBufferUtil;

/**
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public interface IpfixIeCodec {
	public static final Logger logger = LoggerFactory.getLogger(VariableLength.class);

	public IpfixIeDataTypes getDataType();

	/**
	 * Deals with variable length Information Elements. There are two cases, as
	 * shown in the figures below. <h3>1. Length < 255 octets</h3>
	 * 
	 * <pre>
	 * 
	 *     0                   1                   2                   3
	 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *    | Length (< 255)|          Information Element                  |
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *    |                      ... continuing as needed                 |
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * 
	 *    Figure R: Variable-Length Information Element (length < 255 octets)
	 * 
	 * </pre>
	 * 
	 * <h3>2. Length > 255</h3>
	 * 
	 * <pre>
	 *    0                   1                   2                   3
	 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *    |      255      |      Length (0 to 65535)      |       IE      |
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *    |                      ... continuing as needed                 |
	 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * 
	 * </pre>
	 * 
	 * @author FhG-FOKUS NETwork Research
	 * 
	 */
	public static class VariableLength {

		/**
		 * Reads variable length information elements.
		 * 
		 * @param setBuffer
		 * @return
		 */
		public static ByteBuffer getByteBuffer(ByteBuffer setBuffer) {
			int len = ByteBufferUtil.getUnsignedByte(setBuffer, setBuffer.position());
			setBuffer.position(setBuffer.position()+1);
			if( len == 0xff ){
				len = ByteBufferUtil.getUnsignedShort(setBuffer, setBuffer.position());
				setBuffer.position(setBuffer.position()+2);

			}
			return ByteBufferUtil.sliceAndSkip(setBuffer, len);
		}
	}
}
