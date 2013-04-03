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

package de.fhg.fokus.net.packetmatcher.ipfix;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixDataRecordReader;
import de.fhg.fokus.net.ipfix.api.IpfixIe;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateForDataReader;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeObservationTimeMilliseconds;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIePacketDeltaCount;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeSamplingSize;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtInterfaceDescription;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtInterfaceName;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtPcapStatDrop;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtPcapStatRecv;
import de.fhg.fokus.net.ptapi.PtInterfaceStats;

/**
 * Minimal IPFIX record for packet tracking.
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public final class IpfixReaderPtInterfaceStats implements IpfixDataRecordReader {
	private static final Logger logger = LoggerFactory.getLogger(IpfixReaderPtInterfaceStats.class);
	private static final String DECODE_ERROR_STRING = "";
	
	private final IpfixIeObservationTimeMilliseconds ie1 = new  IpfixIeObservationTimeMilliseconds(8);
	private final IpfixIeSamplingSize ie2 = new IpfixIeSamplingSize(4);
	private final IpfixIePacketDeltaCount ie3 = new IpfixIePacketDeltaCount(8);
	private final IpfixIePtPcapStatRecv ie4 = new IpfixIePtPcapStatRecv(4);
	private final IpfixIePtPcapStatDrop ie5 = new IpfixIePtPcapStatDrop(4);
	private final IpfixIePtInterfaceName ie6 = new IpfixIePtInterfaceName(IpfixIe.VARIABLE_LENGTH);
	private final IpfixIePtInterfaceDescription ie7 = new IpfixIePtInterfaceDescription(IpfixIe.VARIABLE_LENGTH);


	private final IpfixTemplateForDataReader template = new IpfixTemplateForDataReader(
			ie1, ie2, ie3, ie4, ie5, ie6, ie7);

	@Override
	public Object getRecord(IpfixMessage msg, ByteBuffer setBuffer) {
		if (!setBuffer.hasRemaining()) {
			return null;
		}
		long ie1_d = ie1.getBigInteger(setBuffer).longValue();
		long ie2_d = ie2.getLong(setBuffer);
		BigInteger ie3_d = ie3.getBigInteger(setBuffer);
		long ie4_d = ie4.getLong(setBuffer);
		long ie5_d = ie5.getLong(setBuffer);
		
		String ie6_d = DECODE_ERROR_STRING;
		String ie7_d = DECODE_ERROR_STRING;
		try {
			ie6_d=ie6.getString(setBuffer);
			ie7_d=ie7.getString(setBuffer);
		} catch (UnsupportedEncodingException e) {
			logger.debug(e.getMessage());
		}
		return new PtInterfaceStats(
				msg.getObservationDomainID(),
				ie1_d, ie2_d, ie3_d, ie4_d, ie5_d, ie6_d, ie7_d 				
		);
	}

	@Override
	public IpfixTemplateForDataReader getTemplate() {
		return template;
	}

	public String toString() {
		return "PtSampling";
	};

}
