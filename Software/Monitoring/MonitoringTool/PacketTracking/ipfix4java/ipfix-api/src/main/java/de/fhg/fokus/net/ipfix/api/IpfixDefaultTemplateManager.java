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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IPFIX Template Manager
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class IpfixDefaultTemplateManager implements IpfixTemplateManager {
	// -- sys --
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Statistics stats = new Statistics();
	// -- management --
	private final Map<String, IpfixIe> mapFieldIe = new ConcurrentHashMap<String, IpfixIe>();
	private final Map<Integer, IpfixDataRecordReader> mapSetIdRecordReader = new ConcurrentHashMap<Integer, IpfixDataRecordReader>();
	private final Map<String, IpfixDataRecordReader> mapTemplateUidRecordReader = new ConcurrentHashMap<String, IpfixDataRecordReader>();
	private final Map<Integer, IpfixDataRecordSpecifier> mapSetId2DataRecordSpecifier = new ConcurrentHashMap<Integer, IpfixDataRecordSpecifier>();

	// TODO unify this
	@Override
	public void registerOptionsTemplateRecord(
			IpfixOptionsTemplateRecord ipfixOptionsTemplateRecord) {
		stats.numberOfOptionTemplateRecords++;
		int setId = ipfixOptionsTemplateRecord.getTemplateId();
		IpfixDataRecordReader reader = mapTemplateUidRecordReader
				.get(ipfixOptionsTemplateRecord.getUid());
		mapSetId2DataRecordSpecifier.put(setId, ipfixOptionsTemplateRecord);
		if (reader == null) {
			logger.debug(
					"No IPFIX (options) data record reader was found for {}.",
					ipfixOptionsTemplateRecord.getUid());
			return;
		}
		logger.debug(
				"registering ipfix options template record: {}, reader:{}",
				ipfixOptionsTemplateRecord, reader);
		mapSetIdRecordReader.put(setId, reader);
	}

	@Override
	public IpfixDataRecordSpecifier getDataRecordSpecifier(int setId) {
		return mapSetId2DataRecordSpecifier.get(setId);
	}

	@Override
	public IpfixDataRecordReader getDataRecordReader(int setId) {
		IpfixDataRecordReader reader = mapSetIdRecordReader.get(setId);
		// logger.debug("Getting data record reader:{} setId:{}",reader,setId);
		return reader;
	}

	@Override
	public IpfixIe getInformationElement(IpfixFieldSpecifier fieldSpecifier) {
		return mapFieldIe.get(fieldSpecifier.toString());
	}

	@Override
	public Statistics getStatistics() {
		return stats;
	}

	@Override
	public void registerDataRecordReader(IpfixDataRecordReader reader) {
		logger.debug("registering data record reader: {}", reader.getTemplate()
				.getUid());
		for (IpfixIe ie : reader.getTemplate().getInformationElements()) {
			logger.debug(" +-registering ie: {}, {}", ie.getName(), ie
					.getFieldSpecifier());
			mapFieldIe.put(ie.getFieldSpecifier().toString(), ie);
		}
		mapTemplateUidRecordReader.put(reader.getTemplate().getUid(), reader);

	}

	@Override
	public void registerTemplateRecord(IpfixTemplateRecord ipfixTemplateRecord) {
		stats.numberOfTemplateRecords++;
		IpfixDataRecordReader reader = mapTemplateUidRecordReader
				.get(ipfixTemplateRecord.getUid());
		int setId = ipfixTemplateRecord.getTemplateId();
		mapSetId2DataRecordSpecifier.put(setId, ipfixTemplateRecord);
		if (reader == null) {
			logger.debug("No IPFIX data record reader was found for {}.",
					ipfixTemplateRecord.getUid());
			return;
		}
		logger.debug("registering ipfix template record: {}, reader:{}",
				ipfixTemplateRecord, reader);
		mapSetIdRecordReader.put(setId, reader);

	}

}
