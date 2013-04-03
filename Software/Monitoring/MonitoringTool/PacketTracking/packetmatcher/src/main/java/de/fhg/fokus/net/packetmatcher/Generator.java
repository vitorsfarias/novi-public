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

import java.io.IOException;
import java.net.InetAddress;
import java.util.SortedSet;
import java.util.concurrent.ArrayBlockingQueue;

public class Generator implements Runnable{
	
	class PacketSchedule implements Runnable {
		private int pktid;
		Thread thread;
		
		public PacketSchedule(int pktid) {
			this.pktid = pktid;
			thread = new Thread(this);
		    thread.start();
			
		}

		@Override
		public void run() {
			for (int j = 0; j < probeIDs.length; j++) {
				if (Math.random() < 0.5D) {
					try {
						Thread.sleep(Math.round(Math.random()*15));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					PacketIdRecord rec = new PacketIdRecord(pktid, System.currentTimeMillis(), probeIDs[j], (short)100, 0 );
					pktidmap.addPacketIdRecord(rec);
				}
			}
		}
		
	}
	
	private PacketIdMatcher pktidmap;
    private int [] probeIDs = { -1062731518, -1062731514, -1062731510, -1062731506, 
		                        -1062731502, -1062731515, -1062731511, -1062731503, 
								-1062731519, -1062731507 };

	
	public Generator(PacketIdMatcher pktidmap)  {
		this.pktidmap = pktidmap;
	}


	// Socket client = collector.setUpClient();
	
	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			PacketSchedule sched = new PacketSchedule(i);
		}
	}
	
	public static void main(String[] args) throws IOException {
		ArrayBlockingQueue<SortedSet<PacketIdRecord>> queue = new ArrayBlockingQueue<SortedSet<PacketIdRecord>>(10000);
		PacketIdMatcher pktidmap = new PacketIdMatcher(1000, 1000, queue);
		Generator ger = new Generator(pktidmap);
		Thread generatorThread = new Thread(ger);
		generatorThread.start();
		ExportTask export = new ExportTask(null,queue, InetAddress.getLocalHost(), 12345, pktidmap.getPktIdMap());
		export.start();
	}
}
