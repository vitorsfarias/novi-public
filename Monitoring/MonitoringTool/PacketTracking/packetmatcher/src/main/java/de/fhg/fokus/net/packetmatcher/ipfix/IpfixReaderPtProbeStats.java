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
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeObservationTimeMilliseconds;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtProcessCpuSys;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtProcessCpuUser;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtProcessMemRss;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtProcessMemVzs;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtSystemCpuIdle;
import de.fhg.fokus.net.packetmatcher.ipfix.ie.IpfixIePtSystemMemFree;
import de.fhg.fokus.net.ptapi.PtProbeStats;

/**
 * IPFIX record reader for packet tracking minimal templates .
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public final class IpfixReaderPtProbeStats implements IpfixDataRecordReader {
	private final IpfixIeObservationTimeMilliseconds ie1 = new IpfixIeObservationTimeMilliseconds(8);
	private final IpfixIePtSystemCpuIdle ie2 = new IpfixIePtSystemCpuIdle(4);
	private final IpfixIePtSystemMemFree ie3 = new IpfixIePtSystemMemFree(8);
	private final IpfixIePtProcessCpuUser ie4 = new IpfixIePtProcessCpuUser(4);
	private final IpfixIePtProcessCpuSys ie5 = new IpfixIePtProcessCpuSys(4);
	private final IpfixIePtProcessMemVzs ie6 = new IpfixIePtProcessMemVzs(8);
	private final IpfixIePtProcessMemRss ie7 = new IpfixIePtProcessMemRss(8);
	
	
	private final IpfixTemplateForDataReader template = new IpfixTemplateForDataReader(
			ie1, ie2, ie3, ie4, ie5, ie6, ie7);

	@Override
	public Object getRecord(IpfixMessage msg, ByteBuffer setBuffer) {
		if (!setBuffer.hasRemaining()) {
			return null;
		}
		return new PtProbeStats(msg.getObservationDomainID(),
				ie1.getBigInteger(setBuffer).longValue(),
				ie2.getFloat(setBuffer),
				ie3.getBigInteger(setBuffer),
				ie4.getFloat(setBuffer),
				ie5.getFloat(setBuffer),
				ie6.getBigInteger(setBuffer),
				ie7.getBigInteger(setBuffer)
		);
	}

	@Override
	public IpfixTemplateForDataReader getTemplate() {
		return template;
	}

	public String toString() {
		return getClass().getSimpleName();
	};

}
