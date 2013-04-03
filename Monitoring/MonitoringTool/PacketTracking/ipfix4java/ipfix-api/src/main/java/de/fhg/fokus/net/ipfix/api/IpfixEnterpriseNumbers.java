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
 * TODO auto generate from
 * http://www.iana.org/assignments/enterprise-numbers
 
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public enum IpfixEnterpriseNumbers {
	/**
	 * <pre>
	 * { Decimal:12325, 
	 *   Organization:"Fraunhofer FOKUS", 
	 *   Contact:"Hartmut Brandt", 
	 *   Email:"brandt&fokus.gmd.de" }
	 * </pre>
	 */
	FRAUNHOFER_FOKUS(12325, "Fraunhofer FOKUS", "Hartmut Brandt",
			"brandt&fokus.gmd.de"),
	/**
	 * <pre>
	 * { Decimal:29305,
	 *   Organization:"IPFIX Reverse Information Element Private Enterprise",
	 *   Contact:"RFC5103 Authors",
	 *   Email:"ipfix-biflow&cert.org" }
	 * </pre>
	 */
	IPFIX_REVERSE_INFORMATION_ELEMENT_PRIVATE_ENTERPRISE(29305,
			"IPFIX Reverse Information Element Private Enterprise",
			"RFC5103 Authors", "ipfix-biflow&cert.org");
	private final int decimal;
	private final String organization;
	private final String contact;
	private final String email;

	private IpfixEnterpriseNumbers(int decimal, String organization,
			String contact, String email) {
		this.decimal = decimal;
		this.organization = organization;
		this.contact = contact;
		this.email = email;
	}

	public int getDecimal() {
		return decimal;
	}

	public String getOrganization() {
		return organization;
	}

	public String getContact() {
		return contact;
	}

	public String getEmail() {
		return email;
	}

}
