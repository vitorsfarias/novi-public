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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.IpfixCollector;
import de.fhg.fokus.net.ipfix.api.IpfixCollectorListener;
import de.fhg.fokus.net.ipfix.api.IpfixConnectionHandler;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixSet;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateRecord;
import de.fhg.fokus.net.ipfix.util.HexDump;
import de.fhg.fokus.net.packetmatcher.ctrl.Console;
import de.fhg.fokus.net.packetmatcher.ctrl.Probe;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtProbeLocation;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtInterfaceStats;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtMin;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtProbeStats;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtSync;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtTsTtl;
import de.fhg.fokus.net.packetmatcher.ipfix.IpfixReaderPtTsTtlIP;
import de.fhg.fokus.net.packetmatcher.ipfix.PtMin;
import de.fhg.fokus.net.packetmatcher.ipfix.PtSync;
import de.fhg.fokus.net.packetmatcher.ipfix.PtTsTtl;
import de.fhg.fokus.net.packetmatcher.ipfix.PtTsTtlIP;
import de.fhg.fokus.net.ptapi.ProbeRecord;
import de.fhg.fokus.net.ptapi.PtInterfaceStats;
import de.fhg.fokus.net.ptapi.PtProbeLocation;
import de.fhg.fokus.net.ptapi.PtProbeStats;

/**
 * Packet Matcher
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class Matcher {
	// -- constants --
	public static final String VERSION = "v.2010-11";
	// -- config --
	
	@Option(name="-exportHost",usage="host to send packet records to (localhost)")
	private String exportHost = "localhost";
	
	@Option(name="-exportPort", usage="export port (40123)")
	private int exportPort = 40123;
	
	@Option(name="-exportReconnectInterval",usage="export reconnect interval in second (7)")
	private int exportReconnectTimeSeconds = 7;
	
	@Option(name="-consolePort", usage="console port (4000)")
	private int consolePort = 4000;

	@Option(name="-listenPort", usage="IPFIX listen port (4739)")
	private int listenPort = 4739;

	@Option(name="-ttlcheck", usage="perform ttl checks by matching (no)")
	private boolean ttlcheck = false;

	@Option(name="-verbose", usage="verbose log output (no)")
	private boolean verbose = false;
	
	@Option(name="-csv", usage="export data to csv files (no)")
	private boolean fileExportEnableCsv = false;
	
	@Option(name="-obj", usage="export data to java obj files (no)")
	private boolean fileExportEnableObj = false;
	
	@Option(name="-exportFolder", usage="export Folder (matcher_data)")
	private String fileExportFolder = "matcher_data";
	
	// -- sys --
	private static final Logger logger = LoggerFactory.getLogger(Matcher.class);
	private static final ScheduledExecutorService scheduler = Executors
	.newScheduledThreadPool(2);
	// -- model --
	private final IpfixCollector ipfixCollector = new IpfixCollector();
	private int matchingTimeoutMillis = 5000;
	private int matchingPeriodMillis = 5000;

	
	// TODO add support for stopping/starting export task
	private ExportTask exportTask;
	private PacketIdMatcher packetIdMatcher;
	
	private int initialQueueSize = 20000;
	
	private final FileExporter fileExporter = new FileExporter();

	private interface RecordHandler {
		public void onRecord(IpfixMessage msg, Probe probe, Object rec);
	}

	private Map<Class<?>, RecordHandler> recordHandlerMap = new HashMap<Class<?>, Matcher.RecordHandler>();

	// -- probe sync --
	private  Console consoleSync;


	private Thread shutdownHook= new Thread ( "ShutdownHook"){
		@Override
		public void run() {
			fileExporter.stop();
			shutdown();
			
			logger.info("bye");
		}
	};
	public Matcher() {
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public void shutdown(){
		logger.info("shutting down..");
		if(consoleSync!=null){
		   consoleSync.shutdown();
		}
		scheduler.shutdown();
		try {
			if(exportTask!=null){
				exportTask.close();
			}
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
	}
	/**
	 * Depending on the record received various actions are taken. These actions
	 * are implemented via callbacks defined in this function. Dispatching
	 * callbacks is done via recordHandlerMap.
	 */
	private void setupRecordHandlers() {
            
                // Probe location
                recordHandlerMap.put(PtProbeLocation.class, new RecordHandler() {
                        @Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtProbeLocation r = (PtProbeLocation) rec;
                                /*logger.debug("-------------------------------------------------");
                                logger.debug("Observation Time: " + r.getObservationTimeMilliseconds());
                                logger.debug("Source IPv4: " + r.getSourceIpv4Address());
                                logger.debug("Latitude: " + r.getLatitude());
                                logger.debug("Longitude: " + r.getLongitude());
                                logger.debug("Probe Name: " + r.getProbeName());
                                logger.debug("Probe Location Name: " + r.getProbeLocationName());
                                logger.debug("-------------------------------------------------");*/
				if (probe != null) {
					probe.setLocation(r.getLatitude(),r.getLongitude());
				}
                                if (!exportTask.putProbeRecord(r)) {
					logger.warn("export queue not ready, dropping record: {}",
							rec);
				}
			}
                });

                // Pktid records
		// - create PacketIdRecord and
		// - forward it to matcher
		recordHandlerMap.put(PtMin.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtMin r = (PtMin) rec;
				if (probe != null) {
					probe.incPktIdRecords();
				}
				// TODO add support to packet size
				PacketIdRecord pir = new PacketIdRecord((int) r
						.getDigestHashValue(), r
						.getObservationTimeMicroseconds().longValue(),
						(int) msg.getObservationDomainID(), 0, r.getIpTTL());
				packetIdMatcher.addPacketIdRecord(pir);

			}
		});
                // Pktid records
		// - create PacketIdRecord with IP-Addresses and Ports and
		// - forward it to matcher
		recordHandlerMap.put(PtTsTtl.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtTsTtl r = (PtTsTtl) rec;
				if (probe != null) {
					probe.incPktIdRecords();
				}
				PacketIdRecord pir = new PacketIdRecord((int) r
						.getDigestHashValue(), r
						.getObservationTimeMicroseconds().longValue(),
						(int) msg.getObservationDomainID(), r.getTotalLengthIPv4(), r.getIpTTL());
				pir.setVersion((byte)r.getIpVersion());
				pir.setProtocol(r.getProtocolIdentifier());
				packetIdMatcher.addPacketIdRecord(pir);
			}
		});
                recordHandlerMap.put(PtTsTtlIP.class, new RecordHandler() {
                        @Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtTsTtlIP r = (PtTsTtlIP) rec;
				if (probe != null) {
					probe.incPktIdRecords();
				}
				// TODO add support to packet size
				PacketIdRecord pir = new PacketIdRecord((int) r
						.getDigestHashValue(), r
						.getObservationTimeMicroseconds().longValue(),
						(int) msg.getObservationDomainID(), r.getTotalLengthIPv4(), r.getIpTTL(),
                                                r.sourceAddress, r.sourcePort, r.destinationAddress, r.destinationPort);
				pir.setVersion((byte)r.getIpVersion());
				pir.setProtocol(r.getProtocolIdentifier());
				packetIdMatcher.addPacketIdRecord(pir);
			}
		});
		// Probe statistics
		// - forward to output queue
		recordHandlerMap.put(PtProbeStats.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtProbeStats r = (PtProbeStats) rec;
				if (probe != null) {
					probe.incProbeStatsRecords();
					probe.setLastProbeStatsRecord(r);
				}

				if (!exportTask.putProbeRecord(r)) {
					logger.warn("export queue not ready, dropping record: {}",
							rec);
				}
			}
		});
		// Sampling statistics
		// - forward to output queue
		recordHandlerMap.put(PtInterfaceStats.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				PtInterfaceStats r = (PtInterfaceStats) rec;
				if (probe != null) {
					probe.setLastInterfaceStatsRecord(r);
				}
				if (!exportTask.putProbeRecord(r)) {
					logger.warn("export queue not ready, dropping record: {}",
							rec);
				}
			}
		});
		// Template records
		// - add to statistics
		recordHandlerMap.put(IpfixTemplateRecord.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				if (probe != null) {
					probe.incTemplateRecords();
				}
			}
		});

		// PTSync responses
		//
		recordHandlerMap.put(PtSync.class, new RecordHandler() {
			@Override
			public void onRecord(IpfixMessage msg, Probe probe, Object rec) {
				consoleSync.receive(probe, (PtSync) rec);
				if (probe != null) {
					probe.incSyncResponseRecords();
				}
			}
		});

	}

	private void init() throws UnknownHostException, IOException {
		BlockingQueue<SortedSet<PacketIdRecord>> queue = new ArrayBlockingQueue<SortedSet<PacketIdRecord>>(
				initialQueueSize);
		
		packetIdMatcher = new PacketIdMatcher(matchingTimeoutMillis,
				matchingPeriodMillis, queue);
		exportTask = new ExportTask(this.fileExporter, queue, InetAddress.getByName(exportHost),
				exportPort, packetIdMatcher.getPktIdMap());

	}

	public int getInitialQueueSize() {
		return initialQueueSize;
	}

	/**
	 * @param initialQueueSize
	 *            initial size of internal queue used for matching packets.
	 */
	public Matcher setInitialQueueSize(int initialQueueSize) {
		this.initialQueueSize = initialQueueSize;
		return this;
	}
	public void startFileExporter(){
		if(fileExportEnableCsv||fileExportEnableObj){
			fileExporter.setEnableCsv(fileExportEnableCsv);
			fileExporter.setEnableObj(fileExportEnableObj);
			fileExporter.start(new File(fileExportFolder));
		}
	}
	public FileExporter getFileExporter() {
		return fileExporter;
	}

	public void stopFileExporter(){
		fileExporter.stop();
	}
	/**
	 * start matcher
	 * 
	 * @return
	 * @throws IOException
	 */
	public Matcher start() throws IOException {
		init();
		startFileExporter();
		System.out.println(toString());

		ipfixCollector.registerDataRecordReader(new IpfixReaderPtMin());
		ipfixCollector.registerDataRecordReader(new IpfixReaderPtProbeStats());
		ipfixCollector.registerDataRecordReader(new IpfixReaderPtInterfaceStats());
		ipfixCollector.registerDataRecordReader(new IpfixReaderPtTsTtl());
		ipfixCollector.registerDataRecordReader(new IpfixReaderPtSync());
                ipfixCollector.registerDataRecordReader(new IpfixReaderPtProbeLocation());
                ipfixCollector.registerDataRecordReader(new IpfixReaderPtTsTtlIP());

                logger.debug("adding EventListener ... ");

		ipfixCollector.addEventListener(new IpfixCollectorListener() {

			@Override
			public void onMessage(IpfixConnectionHandler handler,
					IpfixMessage msg) {
				try {
					logger.debug(msg+"");
					if (msg == null) {
						logger.warn("Strange, msg is null, this might be a bug in the IPFIX collector");
						return;
					}
					Probe probe = (Probe) handler.getAttachment();
					if (probe == null) { // this should never happen
						logger.warn("onMessage dispatched before onConnect, you may lose some probe statistics");
					}
					// reading records and dispatching dispatching record handlers
					for (IpfixSet set : msg) {
						for (Object rec : set) {
							RecordHandler recordHandler = recordHandlerMap.get(rec
									.getClass());
							if (recordHandler != null) {
								if( verbose ){
									logger.debug("No record handler for: {}", rec);
								}
								// This might have been set in the respective data record reader,
								// let's assure it here anyway
								if (rec instanceof ProbeRecord) {
									ProbeRecord prec = (ProbeRecord) rec;
									prec.setOid(msg.getObservationDomainID());
									prec.setExportTime(msg.getExportTime());
									fileExporter.putReceivedRecord(prec);
								}
								recordHandler.onRecord(msg, probe, rec);
							} else {
								logger.warn("unknown record received:{}, {}", rec
										.getClass().getSimpleName(), rec);
							}
						}
					}
					if (probe != null) {
						probe.incUnknownSets(msg.getNumberOfunknownSets());
					}
				} catch (Exception e) {
					logger.debug(e.getMessage());
					logger.debug(HexDump.toHexString(msg.getMessageBuffer()));
//					try {
//						handler.getSocket().close();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}

				}
				
			}

			@Override
			public void onConnect(IpfixConnectionHandler handler) {
				logger.debug("handler: {}", handler.hashCode());
				Probe probe;
				try {
					probe = new Probe(handler);
				} catch (IOException e) {
					logger.warn(
							"Could not initialize probe, dropping connection: {}",
							handler.getSocket().getRemoteSocketAddress());
					logger.debug(e.getMessage());
					try {
						handler.getSocket().close();
					} catch (IOException e1) {
						logger.debug(e.getMessage());
					}
					return;
				}
				handler.setAttachment(probe);
				consoleSync.addProbe(probe);
				logger.debug("Exporter CONNECTED from {} ", handler.getSocket()
						.getRemoteSocketAddress());
			}

			@Override
			public void onDisconnect(IpfixConnectionHandler handler) {
				Probe probe = (Probe) handler.getAttachment();
				if (probe != null) {
					consoleSync.removeProbe(probe);

				} else {
					logger.warn("strange, attachment should not be null.");
				}

				logger.debug("Exporter DISCONNECTED from {}", handler
						.getSocket().getRemoteSocketAddress());
			}
		});
		// Debug timer
		if( verbose ){
			scheduler.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					if(consoleSync!=null){
						for( Probe probe: consoleSync.getProbeList()){
							if(probe!=null){
								logger.debug("PROBE_DETAILED_STATS: {}",probe.getStats().replaceAll("\\s+|\n", " "));
								logger.debug("MATCHER_STATS: {}",toString().replaceAll("\\s+|\n", " ") );
							}
						}
					}
				}
			} , 0,
			10, TimeUnit.SECONDS);
		}

		// Reconnect timer
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Socket client = exportTask.getClient();
				if (client == null || !client.isConnected()) {
					try {
						exportTask.start();
					} catch (IOException e) {
						logger.error("ExportTask: could not connect to: {}",
								exportTask.getRemoteSocketAddr());
					}
				}
			}
		}, 0, exportReconnectTimeSeconds, TimeUnit.SECONDS);
		ipfixCollector.bind(listenPort);
		return this;
	}

	public int getMatchingTimeoutMillis() {
		return matchingTimeoutMillis;
	}

	public Matcher setMatchingTimeoutMillis(int matchingTimeoutMillis) {
		this.matchingTimeoutMillis = matchingTimeoutMillis;
		return this;
	}

	public int getMatchingPeriodMillis() {
		return matchingPeriodMillis;
	}

	public Matcher setMatchingPeriodMillis(int matchingPeriodMillis) {
		this.matchingPeriodMillis = matchingPeriodMillis;
		return this;
	}

	public int getExportPort() {
		return exportPort;
	}

	/**
	 * @param exportPort
	 *            port GUI is listening to
	 * @return
	 */
	public Matcher setExportPort(int exportPort) {
		this.exportPort = exportPort;
		return this;
	}

	public String getExportHost() {
		return exportHost;
	}

	/**
	 * @param exportHost
	 *            host where GUI is running
	 */
	public Matcher setExportHost(String exportHost) {
		this.exportHost = exportHost;
		return this;
	}

	public IpfixCollector getIpfixCollector() {
		return ipfixCollector;
	}

	public ExportTask getExportTask() {
		return exportTask;
	}

	public void setExportTask(ExportTask exportTask) {
		this.exportTask = exportTask;
	}
	@Override
	public String toString() {
		String exportTaskStr = "ExportTask:{\"not available\"}";
		if(exportTask!=null){
			exportTaskStr = exportTask.toString();
		}
		return String.format(
				"Matcher { export: \"%s:%d\", console:\"localhost:%d\"  \n" +
				"          ttlcheck: %s, verbose: %s, \n" +
				"   %s \n" +
				"   %s \n" +
				"} ",
				exportHost, exportPort, consolePort,
				ttlcheck , verbose ,exportTaskStr, fileExporter.toString() );
	}
	// used by args4j
	public void run(){
		System.out.println("starting Packet Matcher, logging to matcher_debug.log");
		logger.info("=== Matcher ===");
		try {
			this.consoleSync = new Console(this,consolePort);
			setupRecordHandlers();
			this.consoleSync.start();
			start();
			// FIXME review
			if(ttlcheck){
				exportTask.setTtlcheck(ttlcheck);
			}
			

		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
	}
	// -- main --
	public static void main(String[] args) throws IOException {
		logger.info("== Packet Matcher ==");
		new Matcher().run();

	}
}
