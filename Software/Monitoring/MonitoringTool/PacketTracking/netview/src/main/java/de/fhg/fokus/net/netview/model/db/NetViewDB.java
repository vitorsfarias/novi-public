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

import de.fhg.fokus.net.ptapi.PacketTrackRecord;
import de.fhg.fokus.net.ptapi.PtProbeStats;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class NetViewDB {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private TrackRepository trackRepository;
    
    public NetViewDB() {
	try {
            this.trackRepository = new TrackRepository();
	} catch (Exception ex) {
	    logger.error("failed to create trackRepository: " + ex);
	    ex.printStackTrace();
	}
    }

    public void shutdown() {
        this.trackRepository = null; // gc on this will trigger database connection close
    }

    public TrackRepository getTrackRepository() {
        return trackRepository;
    }

    public int importTracks(File file) {
        ObjectInputStream ois = null;
        Object obj = null;
        int count = 0;

        try {
            ois = new ObjectInputStream(new FileInputStream(file));

            while (true) {
                obj = ois.readObject();
                if (obj != null) {
                    if (obj instanceof PacketTrackRecord) {
                        trackRepository.addPacketTrackRecord((PacketTrackRecord) obj);
                        count++;
                    } else if (obj instanceof PtProbeStats) {
                        trackRepository.addPtProbeStats((PtProbeStats) obj);
                        count++;
                    } else {
                        logger.warn("Unexpected object of class " + obj.getClass() + " in object stream.");
                    }
                }

            }
        } catch (EOFException eof) {
            /* NOP */
        } catch (IOException ioe) {
            logger.warn("IO error while reading " + file.getAbsolutePath());
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            logger.warn("Unknown  class in object stream.");
            cnfe.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    /* NOP */
                }
            }
        }
        return count;
    }
}
