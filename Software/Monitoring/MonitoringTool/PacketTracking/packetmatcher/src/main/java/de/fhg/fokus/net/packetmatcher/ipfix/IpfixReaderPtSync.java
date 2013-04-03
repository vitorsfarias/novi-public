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
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixDataRecordReader;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateForDataReader;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeObservationTimeMilliseconds;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtMessage;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtMessageId;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtMessageValue;

/**
 * Minimal IPFIX record reader for packet tracking.
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public final class IpfixReaderPtSync implements IpfixDataRecordReader {
	private final static Logger logger = LoggerFactory
			.getLogger(IpfixReaderPtSync.class);
	private final IpfixIeObservationTimeMilliseconds ie1 = new IpfixIeObservationTimeMilliseconds(
			8);
	private final IpfixIePtMessageId ie2 = new IpfixIePtMessageId(4);
	private final IpfixIePtMessageValue ie3 = new IpfixIePtMessageValue(4);
	private final IpfixIePtMessage ie4 = new IpfixIePtMessage();
	private final IpfixTemplateForDataReader template = new IpfixTemplateForDataReader(
			ie1, ie2, ie3, ie4);

	@Override
	public Object getRecord(IpfixMessage msg, ByteBuffer setBuffer) {
		if (!setBuffer.hasRemaining()) {
			return null;
		}
		long ie1_d = ie1.getBigInteger(setBuffer).longValue();
		long ie2_d = ie2.getLong(setBuffer);
		long ie3_d = ie3.getLong(setBuffer);

		String ie4_d = "";
		try {
			ie4_d = ie4.getString(setBuffer, "ascii");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
		return new PtSync(msg.getObservationDomainID(), ie1_d, ie2_d, ie3_d,
				ie4_d);
	}

	public String toString() {
		return "PtSync";

	};

	@Override
	public IpfixTemplateForDataReader getTemplate() {
		return template;
	}

}
