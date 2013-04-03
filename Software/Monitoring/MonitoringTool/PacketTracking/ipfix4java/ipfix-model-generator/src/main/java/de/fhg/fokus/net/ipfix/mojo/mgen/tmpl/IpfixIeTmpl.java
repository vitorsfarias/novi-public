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

package de.fhg.fokus.net.ipfix.mojo.mgen.tmpl;

import java.io.File;
/**
 * Text template definitions for generating IPFIX information elements.
 * 
 * @author FhG-FOKUS NETwork Research
 *
 */
public class IpfixIeTmpl {
	/**
	 * Defined tokens in the template.
	 * 
	 * @author FhG-FOKUS NETwork Research
	 *
	 */
	public static enum Tokens {
		TARGET_PACKAGE,
		IMPORTS,
		CODEC_METHODS,
		CLASS_DOCUMENTATION,
		IE_ENTERPRISE_NUMBER,
		IE_ID,
		IE_CODEC,
		IE_CLASS_NAME,
		IE_DATA_TYPES,
		IE_SEMANTICS,
		IE_STATUS,
		IE_NAME,
		IE_UNITS
	}
	/**
	 * IpfixIe.tmpl
	 */
	public static File FILE = new File(IpfixIeTmpl.class.getResource(
			IpfixIeTmpl.class.getSimpleName() + ".tmpl").getFile());
}
