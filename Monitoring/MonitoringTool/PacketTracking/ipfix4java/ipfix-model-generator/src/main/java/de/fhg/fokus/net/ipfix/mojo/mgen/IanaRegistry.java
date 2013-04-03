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
 * Iana registry representation for parsing IPFIX xml models.
 * 
 * @author FhG-FOKUS NETwork Research
 *
 */
public class IanaRegistry {
	// -- sys --
//	private final static Logger logger = LoggerFactory.getLogger(IanaRegistry.class);
	// -- model --
	public String id;
	public String title;
	public String created;
	public String updated;
	public String xref;
	public String note;
	public String registration_rule;
	public String people;
	public List<IanaRecord> records;
	public List<IanaRegistry> children;
	
	/**
	 * Recursively find registry with given id. 
	 * @param registry
	 * @param id
	 * @param maxDepth Maximum nesting depth.
	 * @return
	 */
	public static IanaRegistry find( IanaRegistry registry, String id, int maxDepth  ){
		return find(registry, id, 0, maxDepth);
	}
	
	/**
	 * Find the first registry that matches id. 
	 * (recursive depth first search)
	 * @param id
	 * @return registry or null if not found
	 */
	public static IanaRegistry find( IanaRegistry registry, String id, int depth, int maxDepth ){
		if(registry==null || depth > maxDepth ){
			return null;
		}
		if(registry.id!=null && id.contentEquals(registry.id)){
			return registry;
		}
		if( registry.children!=null ){
			for( IanaRegistry child: registry.children ){
				IanaRegistry reg = find(child, id, depth+1, maxDepth);
				if( reg!=null){
					return reg;
				}
			}
		} 
		return null;
	}
	@Override
	public String toString() {
		int nchildren = children!=null?children.size():0;
		int nrecords = records!=null?records.size():0;
		return String.format("registry:{ id:\"%s\", nchildren:%d, nrecords:%d }  ", id, nchildren, nrecords);
	}
}
