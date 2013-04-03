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

package de.fhg.fokus.net.ptapi;


/**
 * Data exported from probes must implement this interface. 
 * 
 * @author FhG-FOKUS NETwork Research
 *
 */
public interface ProbeRecord extends BaseRecord {
	/**
	 * @return observation domain id.
	 */
	public long getOid();
	/**
	 * @param oid
	 */
	public void setOid( long oid);
	/**
	 * Time in seconds since 0000 UTC Jan 1st 1970, at which the 
	 * IPFIX Message Header leaves the Exporter.
	 * 
	 * @return export time 
	 */
	public long getExportTime();
	/**
	 * 
	 * @param exportTime
	 */
	public void setExportTime( long exportTime );
	/**
	 * Milliseconds since 0000 UTC Jan 1st 1970 data was observation.
	 * 
	 * @return observation time in milliseconds
	 */
	public void setObservationTimeMilliseconds( long observationTimeMilliseconds );
}
