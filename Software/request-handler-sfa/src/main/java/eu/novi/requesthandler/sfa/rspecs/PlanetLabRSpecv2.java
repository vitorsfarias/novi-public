/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.rspecs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.requesthandler.sfa.SFAConstants;

/**
 * Protogeni v2 schema class for PlanetLab
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public class PlanetLabRSpecv2 extends RSpecSchema {

	private Element root;
	
	public PlanetLabRSpecv2(){
		super();		
	}
	
	
	
	public void createEmptyRequestRSpec() {
		root  = doc.createElement(SFAConstants.RSPEC);
		root.setAttribute(SFAConstants.XMLNS, SFAConstants.FED_XMLNS);
		root.setAttribute(SFAConstants.XMLNS_XSI, SFAConstants.FED_XMLNS_XSI);
		root.setAttribute(SFAConstants.XSI_SCHEMA_LOCATION, SFAConstants.FED_XSI_SCHEMA_LOCATION_ADV);
		root.setAttribute(SFAConstants.TYPE, "request");
		doc.appendChild(root);		
	}
	
	/**
	 * gets the nodes from the RSpec as Resource objects
	 * @return the set of Resource objects
	 */	
	public Set<Resource> getResourceSet() {
		Set<Node> nodeSet = getNodeSet();
		
		Set<Resource> resources = new HashSet<Resource>();
		resources.addAll(nodeSet);
		return resources;		
	}
	
	/**
	 * returns the set of nodes from the RSpec
	 * @return set of Node objects
	 */
	private Set<Node> getNodeSet() {
		//get the list of nodes by tag
		NodeList nodesRSpec = doc.getElementsByTagName(SFAConstants.NODE);
				
		int nItems = nodesRSpec.getLength();
				
		
		Set<Node> nodeSet = new HashSet<Node>();
		//go through the list of nodes from the RSpec sequentially and add them to the list
		for (int i = 0; i < nItems; i++) {
		
			Element crtNode = (Element) nodesRSpec.item(i);
			
			Node nn = getNodeFromElement(crtNode);
						
			//add the node to the list
			nodeSet.add(nn); 
		}
		return nodeSet;
	}
	
	/**
	 * converts an RSpec Element to a Node object
	 * @param rspecElement: an element from a RSpec 
	 * @return Node object that contains the information extracted from the rspecElement
	 */
	private Node getNodeFromElement(Element rspecElement) {
		String componentName = rspecElement.getAttributes().getNamedItem(SFAConstants.COMPONENT_NAME).getNodeValue();
		String componentURN = getElementURN(rspecElement);
		
		Node nn = new NodeImpl(componentURN);
		
		nn.setLocatedAt(getNodeLocation(componentName, rspecElement));
		nn.setHardwareType(getHardwareType(rspecElement));
		nn.setHostname(componentName);
		nn.setHrn(getHRN(componentURN));
		
		String physicalInterfaceName = getNodeInterfaceName(rspecElement);
			
		Interface inboundInterface = new InterfaceImpl(physicalInterfaceName+"-in");
		inboundInterface.setCanFederate(true);
		Set<Interface> inboundSet = new HashSet<Interface>();
		inboundSet.add(inboundInterface);
		nn.setHasInboundInterfaces(inboundSet);
		
		Interface outboundInterface = new InterfaceImpl(physicalInterfaceName+"-out");
		outboundInterface.setCanFederate(true);
		Set<Interface> outboundSet = new HashSet<Interface>();
		outboundSet.add(outboundInterface);
		nn.setHasOutboundInterfaces(outboundSet);

		return nn;
	}

	/*public Node getNextNode() {
		// TODO Auto-generated method stub
		
		//doc.getElementsByTagName()
		return null;
	}*/
	
	private String getNodeInterfaceName(Element rspecElement) {
		String phInterfaceName = null;
		NodeList interfaces = rspecElement.getElementsByTagName("interface");
		if (interfaces != null && interfaces.getLength() != 0) {
			Element physicalInterface = (Element) interfaces.item(0);
			phInterfaceName = physicalInterface.getAttribute(SFAConstants.COMPONENT_ID);			
		}
		return phInterfaceName;
	}
	
	public void changeToRequestRSpec(List<String> nodeNameList) {
		NodeList nl = doc.getElementsByTagName("rspec");
		org.w3c.dom.Node rspecElement = nl.item(0);
		nl.item(0).getAttributes().getNamedItem("type").setNodeValue("request");
		
		org.w3c.dom.Node statictis = doc.getElementsByTagName("statistics").item(0).getAttributes().getNamedItem("call");
		statictis.setNodeValue("CreateSlice");
		
		NodeList nodes = doc.getElementsByTagName("node");
		for (int i=0; i<nodes.getLength(); i++) {
			org.w3c.dom.Node n = nodes.item(i);
			NamedNodeMap attributes = n.getAttributes();
			String componentName = attributes.getNamedItem("component_name").getNodeValue().toString();
			if (!nodeNameList.contains(componentName)) {
				rspecElement.removeChild(n);
				i--;
			}
		}
	}
}
