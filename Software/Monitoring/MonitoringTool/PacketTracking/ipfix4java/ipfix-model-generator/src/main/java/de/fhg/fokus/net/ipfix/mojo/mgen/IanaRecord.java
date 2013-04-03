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

package de.fhg.fokus.net.ipfix.mojo.mgen;

import java.util.List;

/**
 * Iana record representation for parsing IPFIX xml models.
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public class IanaRecord {
	public String name;
	public String dataType;
	public String group;
	public String dataTypeSemantics;
	public String elementId;
	public String applicability;
	public String status;
	public String description;
	public String units;
	public List<String> xrefs;
	public String registration_rule;
	public String reserved;
	public String references;
	public String note;
	public String range;
	public String unassigned;
	public String value;
	public String comments;
	public int enterpriseNumber = 0;

	@Override
	public String toString() {
		return String.format("%s:{ \n" + "  elementId:%s, \n"
				+ "  dataType:%s, \n" + "  dataTypeSemantis:%s, \n"
				+ "  units:%s \n" + "  status:%s \n" + "  en: %s \n}", name,
				elementId, dataType, dataTypeSemantics, units, status,
				enterpriseNumber);
	}

	public IanaRecord() {
	}

	public IanaRecord(String name) {
		this.name = name;
	}

	public boolean isValid() {

		boolean notNull = (name != null) && (elementId != null)
		&& (dataType != null) && (status != null);

		return notNull;
	}
	/**
	 * Clean up some manually written notes from ipfix.xml.
	 */
	public void cleanup(){
		if( dataType!=null && name!=null && name.startsWith("messageMD5Checksum") ){
			dataType = dataType.replace("(16 bytes)","");
		}

	}
}
