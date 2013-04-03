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

package de.fhg.fokus.net.packetmatcher;

import java.util.SortedSet;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class MatchingTask extends TimerTask {
	ConcurrentSkipListSet<PacketIdRecord> pktidRecords; 
	private final BlockingQueue<SortedSet<PacketIdRecord>> queue;
	
	public MatchingTask(ConcurrentSkipListSet<PacketIdRecord> pktidRecords, BlockingQueue<SortedSet<PacketIdRecord>> queue) {
		this.pktidRecords = pktidRecords;
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			// System.out.println("TimeOut: recordSize = " + pktidRecords.size());
			if (pktidRecords.size() > 1) {
				queue.put(pktidRecords);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
};
