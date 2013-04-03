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

/* Netview - a software component to visualize packet tracks, hop-by-hop delays,
 *           sampling stats and resource consumption. Netview requires the deployment of
 *           distributed probes (impd4e) and a central packet matcher to correlate the
 *           obervations.
 *
 *           The probe can be obtained at http://impd4e.sourceforge.net/downloads.html
 *
 * Copyright (c) 2011
 *
 * Fraunhofer FOKUS
 * www.fokus.fraunhofer.de
 *
 * in cooperation with
 *
 * Technical University Berlin
 * www.av.tu-berlin.de
 *
 * Ramon Masek <ramon.masek@fokus.fraunhofer.de>
 * Christian Henke <c.henke@tu-berlin.de>
 * Carsten Schmoll <carsten.schmoll@fokus.fraunhofer.de>
 * Julian Vetter <julian.vetter@fokus.fraunhofer.de>
 * Jens Krenzin <jens.krenzin@fokus.fraunhofer.de>
 * Michael Gehring <michael.gehring@fokus.fraunhofer.de>
 * Tacio Grespan Santos
 * Fabian Wolff
 *
 * For questions/comments contact packettracking@fokus.fraunhofer.de
 *
 */

package de.fhg.fokus.net.netview.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import de.fhg.fokus.net.ptapi.PacketTrackRecord;

@Entity
public class RawTrackData {

	
	public RawTrackData() {

	}

	public RawTrackData(int uID, int trackID, int packetID, int recordID, int probeID,
			long ts, int ttl, int nextHop, int delay, int hopNumber) {
		this.uid = uID;
		this.trackID = trackID;
		this.packetID = packetID;
		this.recordID = recordID;
		this.probeID = probeID;
		this.ts = ts;
		this.ttl = ttl;
		this.nextHop = nextHop;
		this.delay = delay;
		this.hopNumber = hopNumber;
	}

	
	public RawTrackData(PacketTrackRecord record, int i, long recordID){
		this.trackID= record.trackid;
		this.packetID = record.pktid;
		this.probeID = record.oids[i];
		this.recordID = recordID;
		this.ttl = record.ttl[i];
		this.ts = record.ts[i];
		this.hopNumber = i;
		if (i<record.oids.length-1){
			this.nextHop = record.oids[i+1];
			this.delay = record.ts[i+1]-record.ts[i];
		}

                // Not all records contain source addresses
                if(record.sourceAddress != null){
                        this.sourceAddress = record.sourceAddress.toString().substring(1);
                }
                else{
                        this.sourceAddress = "unkown source";
                }

                // Not all records contain destination addresses
                if(record.destinationAddress != null){
                        this.destinationAddress = record.destinationAddress.toString().substring(1);
                }
                else{
                        this.destinationAddress = "unkown destination";
                }
                
                this.sourcePort = record.sourcePort;
                this.destinationPort = record.destinationPort;
	}
	
	/**
	 * Unique Id
	 */
	@Id
	private int uid;

	@Column
	private int trackID;

	@Column
	private int packetID;

	@Column
	private long recordID;

	@Column
	private long probeID;

	@Column
	private long ts;

	@Column
	private int ttl;
	
	@Column
	private long nextHop;

	@Column
	private long delay;
	
	@Column
	private int hopNumber;

        @Column
	private String sourceAddress;

        @Column
        private int sourcePort;

        @Column
        private String destinationAddress;

        @Column
        private int destinationPort;
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getTrackID() {
		return trackID;
	}

	public void setTrackID(int trackID) {
		this.trackID = trackID;
	}

	public int getPacketID() {
		return packetID;
	}

	public void setPacketID(int packetID) {
		this.packetID = packetID;
	}

	public long getRecordID() {
		return recordID;
	}

	public void setRecordID(long recordID) {
		this.recordID = recordID;
	}

	public long getProbeID() {
		return probeID;
	}

	public void setProbeID(long probeID) {
		this.probeID = probeID;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public long getNextHop() {
		return nextHop;
	}

	public void setNextHop(long nextHop) {
		this.nextHop = nextHop;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public int getHopNumber() {
		return hopNumber;
	}

	public void setHopNumber(int hopNumber) {
		this.hopNumber = hopNumber;
	}

        public String getDestinationAddress() {
                return destinationAddress;
        }

        public void setDestinationAddress(String destinationAddress) {
                this.destinationAddress = destinationAddress;
        }

        public int getDestinationPort() {
                return destinationPort;
        }

        public void setDestinationPort(int destinationPort) {
                this.destinationPort = destinationPort;
        }

        public String getSourceAddress() {
                return sourceAddress;
        }

        public void setSourceAddress(String sourceAddress) {
                this.sourceAddress = sourceAddress;
        }

        public int getSourcePort() {
                return sourcePort;
        }

        public void setSourcePort(int sourcePort) {
                this.sourcePort = sourcePort;
        }

}
