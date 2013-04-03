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

package de.fhg.fokus.net.ipfix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixCollectorListener;
import de.fhg.fokus.net.ipfix.api.IpfixConnectionHandler;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixSet;
import de.fhg.fokus.net.ipfix.record.IpfixRecordSourceIpv4PacketDeltaCount;

/**
 * 
 *  @author FhG-FOKUS NETwork Research
 */
public class IpfixCollectorTest {
	private final static Logger logger = LoggerFactory.getLogger(IpfixCollectorTest.class);
	@Test
	public void tcpCollector() throws Exception {
		int sleep = 50;
		logger.debug("--- IPFIX Collector test ---");
		logger.debug("Shutting down in {}s",sleep);
		
		IpfixCollector ic = new IpfixCollector();
		ic.registerDataRecordReader(IpfixRecordSourceIpv4PacketDeltaCount.getReader());
		ic.addEventListener(new IpfixCollectorListener() {
			@Override
			public void onMessage( IpfixConnectionHandler handler, IpfixMessage msg) {
				logger.debug(msg+"");
				for(IpfixSet set: msg){
					for(Object rec: set){
						logger.debug(rec+"");
					}
				}
			}
			@Override
			public void onConnect(IpfixConnectionHandler hander) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onDisconnect(IpfixConnectionHandler hander) {
				// TODO Auto-generated method stub
			}
		});
		
		//ic.bind(4739);
		//Thread.sleep(sleep*1000);
		ic.shutdow();
		
		
	}
	
}
