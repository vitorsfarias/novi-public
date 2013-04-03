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

import java.nio.ByteBuffer;

import de.fhg.fokus.net.ipfix.api.IpfixDataRecordReader;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateForDataReader;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeDigestHashValue;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeIpTTL;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeIpVersion;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeObservationTimeMicroseconds;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeProtocolIdentifier;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeTotalLengthIPv4;

/**
 * Minimal IPFIX record for packet tracking.
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public final class IpfixReaderPtTsTtl implements IpfixDataRecordReader {
	private final IpfixIeObservationTimeMicroseconds ie1 = new IpfixIeObservationTimeMicroseconds(
			8);
	private final IpfixIeDigestHashValue ie2 = new IpfixIeDigestHashValue(4);
	private final IpfixIeIpTTL ie3 = new IpfixIeIpTTL(1);
	private final IpfixIeTotalLengthIPv4 ie4 = new IpfixIeTotalLengthIPv4(2);
	private final IpfixIeProtocolIdentifier ie5 = new IpfixIeProtocolIdentifier(
			1);
	private final IpfixIeIpVersion ie6 = new IpfixIeIpVersion(1);
	private final IpfixTemplateForDataReader template = new IpfixTemplateForDataReader(
			ie1, ie2, ie3, ie4, ie5, ie6);

	@Override
	public Object getRecord(IpfixMessage msg, ByteBuffer setBuffer) {
		if (!setBuffer.hasRemaining()) {
			return null;
		}
		return new PtTsTtl(ie1.getBigInteger(setBuffer),
				ie2.getLong(setBuffer), ie3.getShort(setBuffer),
				ie4.getInt(setBuffer), ie5.getShort(setBuffer),
				ie6.getShort(setBuffer));
	}

	public String toString() {
		return "PtTsTtl";

	};

	@Override
	public IpfixTemplateForDataReader getTemplate() {
		return template;
	}

}
