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

package de.fhg.fokus.net.ipfix.api;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.api.IpfixTemplateManager.Statistics;
import de.fhg.fokus.net.ipfix.util.ByteBufferUtil;
import de.fhg.fokus.net.ipfix.util.HexDump;

/**
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class IpfixSet implements Iterable<Object> {
	// -- sys --
	private static final Logger logger = LoggerFactory
			.getLogger(IpfixSet.class);
	// -- model --
	private final IpfixSetHeader header;
	private ByteBuffer setBuffer;
	private IpfixSetType type;
	private final IpfixMessage msg; // won't be used here, just passed through to delegated decoding
	// -- management --
	private final IpfixTemplateManager templateManager;
	private final Statistics stats;
	private Iterator<Object> iterator = new Iterator<Object>() {
		@Override
		public void remove() {
		}

		@Override
		public Object next() {
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasNext() {
			logger.warn("Trying to iterate over an invalid set: {}",
					IpfixSet.this.toString());
			return false;
		}
	};
	public IpfixSet(IpfixMessage msg, IpfixTemplateManager templateManager,
			IpfixSetHeader header, ByteBuffer setsBuffer) {
		this.msg = msg;
		this.header = header;
		this.templateManager = templateManager;
		this.stats = templateManager.getStatistics();
		if(this.header.getLength()==0){
			throw new RuntimeException("Set length is 0! At "+this.stats.globalBufferPosition);
		}
		
		this.setBuffer = ByteBufferUtil.sliceAndSkip(setsBuffer, this.header.getLength() -IpfixSetHeader.SIZE_IN_OCTETS);
		stats.setBufferPosition = setsBuffer.position();
		//
		this.type = IpfixSetType.getSetType(this.header.getSetId());
		// Setting up record iterator
		switch (type) {
		// -------------------------------------------------------------------
		// Reading data records
		// -------------------------------------------------------------------
		case DATA:
			stats.numberOfDataSets++;
			final int setId = this.header.getSetId();
                        logger.debug("set id: " + setId);
			final IpfixDataRecordReader recordReader = templateManager
					.getDataRecordReader(setId);
                        logger.debug("reader: "+recordReader);
			final IpfixDataRecordSpecifier recordSpecifier = templateManager
					.getDataRecordSpecifier(setId);
                        logger.debug("record spec: " + recordSpecifier);
			iterator = new RecordIterator() {

				@Override
				public boolean hasNext() {
					if (next != null) {
						return true;
					}
					if (setBuffer.hasRemaining()) {
						if (recordReader == null) {
							if( recordSpecifier==null){
								IpfixSet.this.msg.incNumberOfunknownSets();
								// Skipping unknown set
								logger.debug("Got unknown set, did the exporter " +
										"send all template records? setid: {}, hexdump:{}",
										setId,
										HexDump.toHexString(setBuffer.slice()));
								
								return false;
							}
							if (  !recordSpecifier.isVariableLength()) {
								next = new IpfixDefaultDataRecord(setBuffer,
										recordSpecifier.getDataRecordLength());
							} else {
								logger
										.debug("Skipping unknown variable length set.");
								return false;
							}
						} else {
							next = recordReader.getRecord(IpfixSet.this.msg, setBuffer);
							stats.numberOfDataRecords++;
							return true;
						}
					}
					return false;
				}
			};
			break;
		// -------------------------------------------------------------------
		// Reading template records
		// -------------------------------------------------------------------
		case TEMPLATE:
			stats.numberOfTemplateSets++;
			iterator = new RecordIterator() {

				@Override
				public boolean hasNext() {
					if (next != null) {
						return true;
					}
					if (setBuffer.hasRemaining()) {
						try {
							next = new IpfixTemplateRecord(
									IpfixSet.this.templateManager, setBuffer);
							return true;
						} catch (Exception e) {
							logger.debug(e.getMessage());
						}
					}
					return false;
				}

			};

			break;
		// -------------------------------------------------------------------
		// Reading option template records
		// -------------------------------------------------------------------

		case OPTIONS_TEMPLATE:
			stats.numberOfOptionTemplateSets++;
			iterator = new RecordIterator() {
				@Override
				public boolean hasNext() {
					if (next != null) {
						return true;
					}
					if (setBuffer.hasRemaining()) {
						try {
							next = new IpfixOptionsTemplateRecord(
									IpfixSet.this.templateManager, setBuffer);
							return true;
						} catch (Exception e) {
							logger.debug(e.getMessage());
						}
					}
					return false;
				}
			};
			break;
		default:
			break;
		}
	}

	public IpfixSetType getType() {
		return type;
	}

	@Override
	public Iterator<Object> iterator() {
		return iterator;
	}

	public IpfixSetHeader getHeader() {
		return header;
	}

	/**
	 * Generic abstract ipfix record iterator
	 */
	private static abstract class RecordIterator implements Iterator<Object> {
		protected Object last = null, next = null;

		@Override
		public final Object next() {
			if (next == null && !hasNext()) {
				throw new NoSuchElementException();
			}
			last = next;
			next = null;
			return last;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove records from set!");
		}
	}

	@Override
	public String toString() {
		return String.format("%s:{id:%d, len:%d}", type.getShortName(), header
				.getSetId(), header.getLength());
	}

}
