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

package de.fhg.fokus.net.ipfix.model.ie;

import de.fhg.fokus.net.ipfix.api.IpfixFieldSpecifier;
import de.fhg.fokus.net.ipfix.api.IpfixIe;
import de.fhg.fokus.net.ipfix.api.IpfixIeSemantics;
import de.fhg.fokus.net.ipfix.api.IpfixIeStatus;
import de.fhg.fokus.net.ipfix.api.IpfixIeUnits;
import de.fhg.fokus.net.ipfix.api.codec.IpfixIeCodecUnsigned32;



/**
 * <pre>
ipv6ExtensionHeaders:{ 
  elementId:64, 
  dataType:unsigned32, 
  dataTypeSemantis:flags, 
  units:null 
  status:current 
  en: 0 
}
</pre> 
 * 
 */
public final class IpfixIeIpv6ExtensionHeaders extends IpfixIeCodecUnsigned32 implements IpfixIe {
	// -- model --
	private final IpfixFieldSpecifier fieldSpecifier;

	@Override
	public IpfixFieldSpecifier getFieldSpecifier() {
		return fieldSpecifier;
	}

	public IpfixIeIpv6ExtensionHeaders() {
		this.fieldSpecifier = new IpfixFieldSpecifier(0).setId(64)
				.setFieldLength(this.fieldLength);
	}
	public IpfixIeIpv6ExtensionHeaders( int length ) {
		this.fieldLength = length;
		this.fieldSpecifier = new IpfixFieldSpecifier(0).setId(64)
				.setFieldLength(this.fieldLength);
	}
	public IpfixIeIpv6ExtensionHeaders( int length, long enterpriseNumber, boolean isScope ) {
		this.fieldLength = length;
		this.fieldSpecifier = new IpfixFieldSpecifier(enterpriseNumber).setId(64)
				.setFieldLength(this.fieldLength).setScope(isScope);
	}


	@Override
	public IpfixIeSemantics getSemantics() {
		return IpfixIeSemantics.FLAGS;
	}

	@Override
	public IpfixIeStatus getStatus() {
		return IpfixIeStatus.CURRENT;
	}

	@Override
	public String getName() {
		return "ipv6ExtensionHeaders";
	}

	@Override
	public int getLength() {
		return fieldSpecifier.getIeLength();
	}

	@Override
	public IpfixIeUnits getUnits() {
		return IpfixIeUnits.NONE;
	}
}