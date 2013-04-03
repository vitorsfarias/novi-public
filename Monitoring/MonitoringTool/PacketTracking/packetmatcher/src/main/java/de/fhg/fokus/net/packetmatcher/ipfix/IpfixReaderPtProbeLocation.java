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

import java.math.BigInteger;

import de.fhg.fokus.net.ipfix.api.IpfixIe;
import de.fhg.fokus.net.ipfix.api.IpfixDataRecordReader;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateForDataReader;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeLatitude;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeLongitude;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeObservationTimeMilliseconds;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeProbeLocationName;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeProbeName;
import de.fhg.fokus.net.ipfix.model.ie.IpfixIeSourceIPv4Address;
import de.fhg.fokus.net.ptapi.PtProbeLocation;
import java.net.Inet4Address;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author FhG-FOKUS NETwork Research
 */
public final class IpfixReaderPtProbeLocation implements IpfixDataRecordReader{
    
        private final IpfixIeObservationTimeMilliseconds ie1 = new IpfixIeObservationTimeMilliseconds(
			8);
        private final IpfixIeSourceIPv4Address ie2 = new IpfixIeSourceIPv4Address(4);
	private final IpfixIeLatitude ie3 = new IpfixIeLatitude(IpfixIe.VARIABLE_LENGTH);
	private final IpfixIeLongitude ie4 = new IpfixIeLongitude(IpfixIe.VARIABLE_LENGTH);
        private final IpfixIeProbeName ie5 = new IpfixIeProbeName(IpfixIe.VARIABLE_LENGTH);
        private final IpfixIeProbeLocationName ie6 = new IpfixIeProbeLocationName(IpfixIe.VARIABLE_LENGTH);
	private final IpfixTemplateForDataReader template = new IpfixTemplateForDataReader(
			ie1,ie2,ie3, ie4,ie5, ie6);

	@Override
	public Object getRecord(IpfixMessage msg, ByteBuffer setBuffer) {
		if (!setBuffer.hasRemaining()) {
			return null;
		}

                // Observation Time Milliseconds
                long observationTimeMilliseconds = ie1.getBigInteger(setBuffer).longValue();

                // Source Ipv4 Address
                Inet4Address ipv4SourceAddess = ie2.getAddress(setBuffer);

                // Latitude
                String latitude = "undefined";
                try {
                    latitude = ie3.getString(setBuffer);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(IpfixReaderPtProbeLocation.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Longitude
                String longitude = "undefined";
                try {
                    longitude = ie4.getString(setBuffer);
                } catch (UnsupportedEncodingException ex) {
                     Logger.getLogger(IpfixReaderPtProbeLocation.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Probe Name
                String probeName = "undefined";
                try {
                    probeName = ie5.getString(setBuffer);
                } catch (UnsupportedEncodingException ex) {
                     Logger.getLogger(IpfixReaderPtProbeLocation.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Probe Location Name
                String probeLocationName = "undefined";
                try {
                    probeLocationName = ie6.getString(setBuffer);
                } catch (UnsupportedEncodingException ex) {
                     Logger.getLogger(IpfixReaderPtProbeLocation.class.getName()).log(Level.SEVERE, null, ex);
                }
		return new PtProbeLocation(msg.getObservationDomainID(),
				observationTimeMilliseconds,
                                ipv4SourceAddess,
                                latitude,
				longitude,
                                probeName,
                                probeLocationName
                                );
	}

        @Override
	public String toString() {
		return "PtLocation";

	};

	@Override
	public IpfixTemplateForDataReader getTemplate() {
		return template;
	}
}
