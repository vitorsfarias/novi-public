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


/**
 * Manages IPFIX template state.
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public interface IpfixTemplateManager {
	public class Statistics {
		public long numberOfMessages = 0;
		public long numberOfDataSets = 0;
		public long numberOfDataRecords = 0;
		public long numberOfTemplateSets = 0;
		public long numberOfTemplateRecords = 0;
		public long numberOfOptionTemplateSets = 0;
		public long numberOfOptionTemplateRecords = 0;
		// TODO review this in the context of a collector
		public long globalBufferPosition = 0;
		public long invalidBytes = 0;
		public int setBufferPosition = 0;

		@Override
		public String toString() {
			return String
					.format(
							"stats:{msgs:%d, data:{nsets:%d, nrecs:%d}, tmpl:{nsets:%d, nrecs:%d}, otmpl:{nsets:%d, nrecs:%d}, pos:{file:%d, set:%d}, invalidbytes:%d}",
							numberOfMessages, numberOfDataSets,
							numberOfDataRecords, numberOfTemplateSets,
							numberOfTemplateRecords,
							numberOfOptionTemplateSets,
							numberOfOptionTemplateRecords,
							globalBufferPosition,
							setBufferPosition,
							invalidBytes
					);
		}
	}

	/**
	 * Register template record so that subsequently data records can be
	 * decoded.
	 * 
	 * @param ipfixTemplateRecord
	 */
	public void registerTemplateRecord(IpfixTemplateRecord ipfixTemplateRecord);

	/**
	 * Register option template record so that subsequently records can be
	 * decoded.
	 * 
	 * @param ipfixOptionsTemplateRecord
	 */
	public void registerOptionsTemplateRecord(
			IpfixOptionsTemplateRecord ipfixOptionsTemplateRecord);

	/**
	 * Data record reader are responsible for interpreting IPFIX data sets.
	 * Information elements are also registered by this call;
	 * 
	 * @param reader
	 */
	public void registerDataRecordReader(IpfixDataRecordReader reader);

	/**
	 * 
	 * @param templateId
	 * @return the respective ipfix data record reader or null if setId is
	 *         unknown
	 */
	public IpfixDataRecordReader getDataRecordReader(int setId);

	public IpfixDataRecordSpecifier getDataRecordSpecifier(int setId);

	/**
	 * 
	 * @param fieldSpecifier
	 * @return information element
	 */
	public IpfixIe getInformationElement(IpfixFieldSpecifier fieldSpecifier);
	
	public Statistics getStatistics();

}
