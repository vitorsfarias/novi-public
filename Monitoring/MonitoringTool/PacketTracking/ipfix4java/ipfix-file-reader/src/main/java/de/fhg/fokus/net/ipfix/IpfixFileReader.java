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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixDataRecordReader;
import de.fhg.fokus.net.ipfix.api.IpfixDefaultTemplateManager;
import de.fhg.fokus.net.ipfix.api.IpfixMessage;
import de.fhg.fokus.net.ipfix.api.IpfixTemplateManager.Statistics;

/**
 * IPFIX file reader implementation (on going). It uses memory mapped files and
 * is currently limited to files of size Integer.MAX_VALUE ( 2^31 -1) bytes.
 * 
 * TODO use IpfixDefaultTemplateManager
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class IpfixFileReader implements Iterable<IpfixMessage> {
	// -- sys --
	private final Logger logger = LoggerFactory.getLogger(getClass());
	// -- constants --
	// -- model --
	private final File file;
	private final RandomAccessFile raf;
	private final FileChannel fileChannel;
	private final MappedByteBuffer fileBuffer;
	private boolean autoDispose = true;
	// -- management --
	private final IpfixDefaultTemplateManager templateManager = new IpfixDefaultTemplateManager();
	private final Statistics stats = templateManager.getStatistics();
	
	public IpfixFileReader(File file) throws IOException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "r");
		this.fileChannel = this.raf.getChannel();
		this.fileBuffer = this.fileChannel.map(MapMode.READ_ONLY, 0, file
				.length());
		logger.debug("File length: {}", file.length());
	}
	/**
	 * Auto dispose resources (e.g. close underlying file) after finishing iterating over messages;
	 * 
	 * @param autoDispose
	 * @return
	 */
	public IpfixFileReader setAutoDispose( boolean autoDispose ){
		this.autoDispose = autoDispose;
		return this;
	}
	/**
	 * Close file and release any resources used by the reader
	 */
	public void dispose(){
		try {
			logger.debug("Releasing resources, file: {} ",file);
			this.raf.close();
		} catch (IOException e) {
			logger.error("Could not release resources!");
			logger.equals(e.toString());
		}
	}
	public File getFile() {
		return file;
	}

	/**
	 * Iterate over IPFIX messages in file.
	 */
	public Iterator<IpfixMessage> iterator() {

		return new Iterator<IpfixMessage>() {
			// iterators are independent
			private final ByteBuffer byteBuffer = fileBuffer.slice();
			private IpfixMessage next = null, last = null;

			public boolean hasNext() {
				if (next != null) {
					return true;
				}
				while (byteBuffer.hasRemaining()) {
					try {
						if (!IpfixMessage.align(byteBuffer )) {
							byte b = byteBuffer.get();
							stats.invalidBytes++;
							logger
							.warn(
									"Invalid data, skipping byte: 0x{}:0x{} ",
									Integer.toHexString(byteBuffer
											.position() - 1), Integer
											.toHexString(0xff & b));
							continue;
						}
						// TODO test this
						next = new IpfixMessage(IpfixFileReader.this.templateManager, byteBuffer);

					} catch (Exception e) {
						byte b = byteBuffer.get();
						stats.invalidBytes++;
						logger
						.warn(
								"Invalid/malformed data, skipping byte: 0x{}:0x{} ",
								Integer.toHexString(byteBuffer
										.position() - 1), Integer
										.toHexString(0xff & b));
						continue;
					}
					return true;
				}
				if(autoDispose){
					dispose();
				}
				return false;
			}

			public IpfixMessage next() {
				if (next == null && !hasNext()) {
					throw new NoSuchElementException();
				}
				last = next;
				next = null;
				stats.numberOfMessages++;
				return last;
			}

			public void remove() {
				throw new UnsupportedOperationException(
				"Cannot modify IPFIX file!");
			}

		};
	}


	@Override
	public String toString() {
		return String.format("%s:{ %s }", file.getName(), stats);
	}
	public void registerDataRecordReader(IpfixDataRecordReader reader) {
		templateManager.registerDataRecordReader(reader);
	}
	public Statistics getStatistics() {
		return stats;
	}


}
