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
package de.fhg.fokus.net.netview.control;

import de.fhg.fokus.net.netview.model.db.TrackRepository;
import de.fhg.fokus.net.ptapi.PacketTrackRecord;
import de.fhg.fokus.net.ptapi.PtProbeStats;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default packet track collector implementation
 *
 * @author FhG-FOKUS NETwork Research
 *
 */
public class PacketTrackCollector {

    public static enum PtcEventType {

        /**
         * eventData: clientAddress
         */
        CLIENT_CONNECTED,
        /**
         * eventData: null
         */
        CLIENT_DISCONNECTED,
        /**
         * eventData: null
         */
        STARTED,
        STOPPED
    }

    public static class PtcEventData {

        public SocketAddress clientAddress;

        public PtcEventData() {
        }

        public PtcEventData(SocketAddress clientAddress) {
            this.clientAddress = clientAddress;
        }
    }

    private final ExecutorService executor;
    private final TrackRepository db;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // model
    private final List<Socket> clients = new CopyOnWriteArrayList<Socket>();
    private long numberOfRecords = 0;
    private long startedAt = 0;

    public PacketTrackCollector(TrackRepository db, ExecutorService executor) {
        this.executor = executor;
        this.db = db;
    }

    private void handleConnection(final Socket socket) {
        final SocketAddress remote = socket.getRemoteSocketAddress();
        clients.add(socket);

        try {
            InputStream inStream = socket.getInputStream();
            ObjectInput inObjectStream = new ObjectInputStream(inStream);

            Object obj;
//			socket.getRemoteSocketAddress();
            while (true) {
                obj = inObjectStream.readObject();
                if (obj != null) {
                    if (obj instanceof PacketTrackRecord) {
                        db.addPacketTrackRecord((PacketTrackRecord) obj);
                        numberOfRecords++;
                    } else if (obj instanceof PtProbeStats) {
                        db.addPtProbeStats((PtProbeStats) obj);
                    // } else if (obj instanceof PtNodeStats) {
                        // TODO implement handling of node stats later
                    } else {
                        //logger.warn("Unexpected object of class " + obj.getClass() + " received.");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
            e.printStackTrace();
            try {
                socket.getInputStream().close();
                socket.close();
            } catch (Exception e1) {
                logger.debug(e1.getMessage());
            }
            clients.remove(socket);
        }
    }
    private ServerSocket serverSocket;

    public boolean isBound() {
        if (serverSocket == null) {
            return false;
        }
        return serverSocket.isBound();
    }

    public String getLocalAddress() {
        if (isBound()) {
            return serverSocket.getLocalSocketAddress().toString();
        }
        return "";
    }
    private boolean shouldRun = true;

    /**
     * Bind collector to a port
     *
     * @param port
     * @throws Exception
     */
    public void bind(int port) {
        shouldRun = true;
        numberOfRecords = 0;
        try {
            serverSocket = new ServerSocket(port);
            startedAt = System.currentTimeMillis();
            while (shouldRun) {
                final Socket client = serverSocket.accept();
                executor.execute(
                        new Runnable() {

                            @Override
                            public void run() {
                                handleConnection(client);
                            }
                        });
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        } finally {
            serverSocket = null;
        }
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getNumberOfRecords() {
        return numberOfRecords;
    }

    /**
     * Stop collector
     */
    public void stop() {
        logger.debug("=> stopping collector...");
        shouldRun = false;
        for (Socket socket : clients) {
            logger.debug("Closing client: {}", socket);
            try {
                socket.getInputStream().close();
                socket.close();
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    public List<Socket> getClients() {
        return clients;
    }
}
