/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.impl.LoginComponentImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.LoginComponent;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Node;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.requesthandler.sfa.SFAActions;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;
import eu.novi.requesthandler.sfa.rspecs.FedericaRSpec;
import eu.novi.requesthandler.sfa.rspecs.PlanetLabRSpecv2;
import eu.novi.requesthandler.sfa.rspecs.RSpecSchema;
import eu.novi.requesthandler.sfa.clients.FedXMLRPCClient;
import eu.novi.requesthandler.sfa.clients.NoviplXMLRPCClient;
import eu.novi.requesthandler.sfa.exceptions.RHBadInputException;
import eu.novi.requesthandler.sfa.exceptions.TestbedException;
import eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException;
import eu.novi.requesthandler.utils.RHUtils;
import eu.novi.nswitch.exceptions.FederationException;
import eu.novi.nswitch.manager.NswitchManager;
/* Logging imports */
import org.osgi.service.log.LogService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Federation strategy for the SFA interface to the testbeds.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu -
 *         i2CAT</a>
 * 
 */
public class RHImpl {
	private final String STATIC_PARTIALLY_BOUND_SLICE = "fixPBoundRequest";
	private final String STATIC_VLAN1 = "3601";
	private final String STATIC_BOUND_SLICE = "fixBoundRequest";
	private final String STATIC_VLAN2 = "3701";

	private String selfCredentialPL = "";
	private String selfCredentialFed = "";
	private SFAActions sfaActions;

	private String testbed; 
	private NswitchManager nswitchManager; 
	private LogService logService; 
	private String waitingTime;
	private ReportEvent userFeedback;

	private boolean noFedericaNodesInRequest; 
	private List<String> nodeURNsToAddPlanetLab; 
	private FedericaRSpec fedRSpec; 
	private FedericaRSpec manifest;
	private StringBuffer userLoginInfo;
	private String vlan;
    private String sessionID=null;
    
	private NOVIUserImpl user;
	private List<String> staticSlicesInFederica = new ArrayList<String>();

	public RHCreateDeleteSliceResponseImpl createSlice(String sessionID, NOVIUserImpl user, String sliceName, TopologyImpl topology) {
		RHCreateDeleteSliceResponseImpl result = null;
		sendInfoFeedback(sessionID, "RH create slice", "Starting create slice for slice: " + sliceName);

		initCreateUpdateMethods(user);
		String topologyName = RHUtils.removeNOVIURIprefix(topology.toString());

		try {
			analyzeTopologyReceived(topology);
		} catch (Exception e) {
			result = getCreateDeleteResponse(true, "RH - Error in the topology: " + e.toString());
			return result;
		}
		
		result = createFederatedSlice(topologyName, sliceName);

		if (result == null) {
			result = getCreateDeleteResponse(true, "There were no Nodes to add in PlanetLab, neither FEDERICA");
			result.setSliceID(sliceName);
			return result;
		}
		
		TopologyImpl topologyWithVLAN = topology;
		if (!result.hasError() && hasNSwitch(topology)) {
			if (vlan.trim().equals("") || vlan.isEmpty()) {
				result = manageVLANError(sliceName);
				return result;
			} else {
				sleep();
				sendInfoFeedback(sessionID, "RH create slice", "Slice " + sliceName + " created successfully. Federating testbeds.");
				topologyWithVLAN = addVlanToNSwitchTopology(topology);
				federateSlices(sliceName, result, topologyWithVLAN);
			}
		}
		
		
		result = setTopologyInResult(result, topologyWithVLAN);
		result = setTestbedsToResponse(result);
		result.setSliceID(sliceName);
		return result;
	}
	
	private boolean hasNSwitch(TopologyImpl topology) {
		Set<Resource> resourcesRequested = topology.getContains();
		for (Resource resource : resourcesRequested) {
			if (resource instanceof Link) {
				Link l = (Link) resource;
				Set<LinkOrPath> paths = l.getProvisionedBy();
				if (paths != null) {
					Path path = (Path) paths.iterator().next();
					Set<Resource> resources = path.getContains();
					if (resources != null) {
						for (Resource res : resources) {
							if (res instanceof NSwitch) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Fills the federica RSpec schema if there is any node for federica. For
	 * planetlab, fills the list with the planetlab nodes to create.
	 * 
	 * @param resourcesRequested
	 *            : the set of resources from the Topology object
	 * @throws Exception
	 */
	protected void analyzeTopologyReceived(TopologyImpl topology) throws RHBadInputException {
		if(RHUtils.isSetEmpty(topology.getContains())) {
			logService.log(LogService.LOG_ERROR, "Topology has no resources to create");
			throw new RHBadInputException("Topology has no resources to create");
		}
		
		Set<Resource> resourcesRequested = topology.getContains();
		for (Resource resource : resourcesRequested) {
			if (resource instanceof VirtualNode) {
				analyzeNode((VirtualNode) resource);
			}else if (resource instanceof VirtualLink){
				 analyzeVirtualLink((VirtualLink)resource);
			}
		}
	}

	private void analyzeNode(VirtualNode node) throws RHBadInputException {
		logService.log(LogService.LOG_INFO, "PSNC Log: Analyzing Virtual node:" + node.toString());
		Iterator<Node> it = node.getImplementedBy().iterator();
		if (it.hasNext()) {
			NetworkElementOrNode nen = (Node) it.next();
			String componentID = RHUtils.removeNOVIURIprefix(nen.toString().toLowerCase());
			logService.log(LogService.LOG_INFO, "RH - Node in the topology: " + componentID);
			if (componentID.contains(SFAConstants.FEDERICA.toLowerCase())) {
				analyzeFedericaNode(node);
			} else if (componentID.contains(SFAConstants.PLANETLAB.toLowerCase())
					|| componentID.contains("novipl")) {
				String[] nodeSplit = componentID.split("\\+");
				String nodeName = nodeSplit[nodeSplit.length - 1];
				nodeURNsToAddPlanetLab.add(nodeName);
			}
		}
	}
	
	protected void analyzeVirtualLink(VirtualLink vLink) throws RHBadInputException {
		if(FedericaRSpec.isFedericaLink(vLink)) {
			addVirtualLinkToRSpec(vLink);		
		}
	}

	private void addVirtualLinkToRSpec(VirtualLink vLink)
			throws RHBadInputException {
		if (noFedericaNodesInRequest) {
			logService.log(LogService.LOG_INFO, "RH - First FEDERICA resource in the topology");
			prepareFedRSpec();
			noFedericaNodesInRequest = false;
		} 
		if(!fedRSpec.isExistingVirtualLink(vLink)) {
			fedRSpec.addLinkToRequestRSpec(vLink);
		}
	}
	
	private void analyzeFedericaNode(VirtualNode node) throws RHBadInputException {
		if (noFedericaNodesInRequest) {
			logService.log(LogService.LOG_INFO, "RH - First FEDERICA Node in the topology");
			prepareFedRSpec();
			noFedericaNodesInRequest = false;
		}
		fedRSpec.addNodeToRequestRSpec(node);
	}

	private RHCreateDeleteSliceResponseImpl createFederatedSlice(String topologyName, String sliceName) {
		RHCreateDeleteSliceResponseImpl result = null;
	
		if (nodeURNsToAddPlanetLab.size() != 0) {
			result = createPlanetLabSlice(sliceName);
			if (result.hasError()) {
				logService.log(LogService.LOG_INFO, "RH - create planetlab slice returned has error message: "
						+ result.getErrorMessage());
				return result;
			}
		}
	
		if (result == null || (result != null && !result.hasError())) {
			if (!noFedericaNodesInRequest) {
				result = checkStaticSlicesAndCreate(topologyName, sliceName);
			}
		}		
		return result;
	}
	
	protected RHCreateDeleteSliceResponseImpl updateFederatedSlice(String topologyName, String sliceName) {
		RHCreateDeleteSliceResponseImpl result = updatePlanetLab(sliceName);	
		
		if (result == null || (result != null && !result.hasError())) {
			result = updateFederica(topologyName, sliceName);		
		}

		return result;
	}

	protected RHCreateDeleteSliceResponseImpl updateFederica(String topologyName,
			String sliceName) {
		RHCreateDeleteSliceResponseImpl result = null;	
		
		if(sliceExistsInTestbed(sliceName, SFAConstants.FEDERICA)) {
			if (!noFedericaNodesInRequest) {
				result = checkStaticSlicesAndUpdate(topologyName, sliceName);
			} else {
				try {
					deleteFedericaSliceStaticOrReal(sliceName);
				} catch (Exception e) {
					logService.log(LogService.LOG_INFO, "RH - There was a problem listing slices to see if the slice to update is existing or not. There may be inconsistencies later. " + e.getMessage());
				} 
			}
		} else {
			if (!noFedericaNodesInRequest) {
				result = checkStaticSlicesAndCreate(topologyName, sliceName);
			} else {
				logService.log(LogService.LOG_INFO, "RH - No nodes to update in FEDERICA");
				result = getCreateDeleteResponse(false, "");
			}
		}

		return result;
	}

	protected boolean isNoFedericaNodesInRequest() {
		return noFedericaNodesInRequest;
	}

	protected void setNoFedericaNodesInRequest(boolean noFedericaNodesInRequest) {
		this.noFedericaNodesInRequest = noFedericaNodesInRequest;
	}

	protected List<String> getNodeURNsToAddPlanetLab() {
		return nodeURNsToAddPlanetLab;
	}

	protected void setNodeURNsToAddPlanetLab(List<String> nodeURNsToAddPlanetLab) {
		this.nodeURNsToAddPlanetLab = nodeURNsToAddPlanetLab;
	}

	protected RHCreateDeleteSliceResponseImpl updatePlanetLab(String sliceName) {		
		RHCreateDeleteSliceResponseImpl result = null;

		if(sliceExistsInTestbed(sliceName, SFAConstants.PLANETLAB)) {
			if (nodeURNsToAddPlanetLab.size() != 0) {
				result = updatePlanetLabSlice(sliceName);
				if (result.hasError()) {
					logService.log(LogService.LOG_INFO, "RH - create planetlab slice returned has error message: "
							+ result.getErrorMessage());
					return result;
				}
			} else {
				try {
					deletePlanetLabSlice(sliceName);
				} catch (Exception e) {
					logService.log(LogService.LOG_INFO, "RH - There was a problem listing slices to see if the slice to update is existing or not. There may be inconsistencies later. " + e.getMessage());
				} 
			}
		} else {
			if (nodeURNsToAddPlanetLab.size() != 0) {
				result = createPlanetLabSlice(sliceName);
			} else {
				logService.log(LogService.LOG_INFO, "RH - No nodes to update in FEDERICA");
				result = getCreateDeleteResponse(false, "");
			}
		}

		return result;
	}
	
	protected boolean sliceExistsInTestbed(String sliceID, String testbed) {
		List<String> slicesInTestbed;

		try {
			slicesInTestbed = getSlicesFromTestbed(testbed);
			for (String slice : slicesInTestbed) {
				if (slice.contains(sliceID)) {
					return true;
				}
			}
			
		} catch (Exception e) {
			logService.log(LogService.LOG_INFO, "RH - There was a problem listing slices to see if the slice to update is existing or not. There may be inconsistencies later. " + e.getMessage());
			return false;
		} 

		return false;
	}

	/**
	 * Creates the planetlab slice if there are resources for planetlab in the
	 * topology requested.
	 * 
	 * @param sliceName
	 *            : string containing the name of the slice
	 * @param nodeURNsToAdd
	 *            : list of strings containing the URNs of the nodes
	 * @return a RHCreateDeleteSliceResponseImpl object, containing the slice
	 *         id, if creation is successful, or an error otherwise
	 */
	private RHCreateDeleteSliceResponseImpl createPlanetLabSlice(String sliceName) {	
		logService.log(LogService.LOG_INFO, "RH - Starting createPlanetLabSlice");
	
		RHCreateDeleteSliceResponseImpl r;
		try {
			preparePlanetLabClient();
	
			String authorityCredential = sfaActions.getCredential(selfCredentialPL, SFAConstants.NOVI_PL_AUTHORITY,
					SFAConstants.AUTHORITY);
	
			sfaActions.addRecordPL(sliceName,
					SFAConstants.NOVI_PL_AUTHORITY,
					SFAActions.getSFAUserIDFromNOVIUser(user),
					authorityCredential);
		} catch (Exception e) {
			r = getCreateDeleteResponse(true, "RH - Error creating PlanetLab record: " + e.toString());
			return r;
		}
	
		r = updatePlanetLabSlice(sliceName);
	
		return r;
	}
	
	private RHCreateDeleteSliceResponseImpl updatePlanetLabSlice(String sliceName) {	
		logService.log(LogService.LOG_INFO, "RH - Starting createPlanetLabSlice");
	
		RHCreateDeleteSliceResponseImpl r;
		try {
			preparePlanetLabClient();
		} catch (Exception e) {
			r = getCreateDeleteResponse(true, "RH - Error getting PlanetLab selfCredential: " + e.toString());
			return r;
		}
	
		try {
			r = createRSpecAndPopulatePLSlice(sliceName);
		} catch (Exception e) {
			r = managePopulateError(e, sliceName);
		}
	
		return r;
	}

	/**
	 * Populates the slice that has been created with the resources requested.
	 * 
	 * @param sliceName
	 *            : a string containing the name of the slice
	 * @param plSliceHRN
	 *            : the HRN of the PlanetLab slice
	 * @return a RHCreateDeleteSliceResponseImpl object, containing the slice
	 *         id, or an error message, if an error occurred
	 * @throws TestbedException 
	 * @throws XMLRPCClientException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private RHCreateDeleteSliceResponseImpl createRSpecAndPopulatePLSlice(String sliceName) throws XMLRPCClientException, TestbedException, IOException  {
		RHCreateDeleteSliceResponseImpl r;
		String plSliceURN = SFAConstants.NOVI_PL_SLICE_URN_PREFIX + sliceName;
		String sliceHRNPL = SFAConstants.NOVI_PL_HRN_PREFIX + sliceName;

		String rspecResult = createPLRequestRSpec(plSliceURN, nodeURNsToAddPlanetLab);
		logService.log(LogService.LOG_INFO, "RH - Nodes to add in the RSpec: " + nodeURNsToAddPlanetLab.toString());
		logService.log(LogService.LOG_INFO, "RH - RSpec created: " + rspecResult);
	
		if (rspecResult.contains("ERROR:")) {
			deleteRecord(selfCredentialPL, SFAConstants.NOVI_PL_AUTHORITY, sliceHRNPL);
			r = getCreateDeleteResponse(true, "RH - PlanetLab RSpec created with ERROR: " + rspecResult);
		} else {
			r = populatePlanetLabSlice(rspecResult, sliceName);
		}
		return r;
	}

	private RHCreateDeleteSliceResponseImpl populatePlanetLabSlice(String rspecResult, String sliceName) throws XMLRPCClientException, TestbedException, IOException  {
		RHCreateDeleteSliceResponseImpl r;
		Map<String, Object> plResultPopulate;
		String plSliceURN = SFAConstants.NOVI_PL_SLICE_URN_PREFIX + sliceName;
		String sliceHRNPL = SFAConstants.NOVI_PL_HRN_PREFIX + sliceName;

		preparePlanetLabClient();

		String sliceCredential = sfaActions.getCredential(selfCredentialPL, sliceHRNPL, SFAConstants.SLICE);

		plResultPopulate = (HashMap<String, Object>) sfaActions.populatePLSlice(plSliceURN, sliceCredential, user,
				rspecResult);

		if (!SFAActions.isValidSFAResponse(plResultPopulate)) {
			deleteRecord(selfCredentialPL, SFAConstants.NOVI_PL_AUTHORITY, sliceHRNPL);

			r = getCreateDeleteResponse(true, plResultPopulate.get(SFAConstants.OUTPUT).toString());
			logService.log(LogService.LOG_INFO,
					"RH - PL slice created with ERROR: " + plResultPopulate.get(SFAConstants.OUTPUT).toString());
		} else {
			r = getCreateDeleteResponse(false, "");
			logService.log(LogService.LOG_INFO, "RH - PL slice created succefully ");
		}
		return r;
	}

	/**
	 * Creates the RSpec for NOVI PlanetLab
	 * 
	 * @param sliceURN
	 *            : string containing the URN of the slice
	 * @param nodeNameList
	 *            :
	 * @return the RSpec as a String, or a string communicating the error.
	 */
	private String createPLRequestRSpec(String sliceURN, List<String> nodeNameList) {
		try {
			preparePlanetLabClient();
	
			Map<String, Object> listPlanetLabResources = (HashMap<String, Object>) sfaActions.listResources(selfCredentialPL);
	
			// Check the result:
			if (SFAActions.isValidSFAResponse(listPlanetLabResources)) {
				String advRSpec = listPlanetLabResources.get(SFAConstants.VALUE).toString();
	
				PlanetLabRSpecv2 rspec = new PlanetLabRSpecv2();
				rspec.setLogService(logService);
				rspec.readRSpec(advRSpec);
				rspec.changeToRequestRSpec(nodeNameList);
	
				return rspec.toString();
			} else {
				logService.log(LogService.LOG_INFO,
						"RH: Error creating Planetlab RSpec. " + listPlanetLabResources.get(SFAConstants.OUTPUT));
				return "ERROR: " + listPlanetLabResources.get(SFAConstants.OUTPUT);
			}
	
		} catch (Exception e) {
			logService.log(LogService.LOG_ERROR, "RH - Error creating PL request RSpec: " + e.toString());
			return "ERROR creating PL request RSpec: " + e.toString();
		}
	}

	private RHCreateDeleteSliceResponseImpl managePopulateError(Exception e, String sliceName) {
		logService.log(LogService.LOG_ERROR, "RH - Error creating PlanetLab slice: ", e);
		RHCreateDeleteSliceResponseImpl r = null;
		try {
			deleteRecord(selfCredentialPL, SFAConstants.NOVI_PL_AUTHORITY, SFAConstants.NOVI_PL_HRN_PREFIX
					+ sliceName);
		} catch (Exception e1) {
			e1.printStackTrace();
			logService.log(	LogService.LOG_WARNING,
					"RH - Error deleting Federica slice record because create FEDERICA slice failed.\n"
							+ e1.toString());
		}
		r = getCreateDeleteResponse(true, "RH - Error creating PlanetLab slice: " + e.toString());
		return r;
	}

	private RHCreateDeleteSliceResponseImpl setTopologyInResult(
			RHCreateDeleteSliceResponseImpl result, Topology topology) {
		String topologyString = "";
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();		
			
	
		if (manifest != null && topology != null) {
			userLoginInfo = new StringBuffer(); 
			Topology newTopology = addUserInfoInTopology(topology);
			result.setUserLogin(userLoginInfo.toString());
			topologyString = repositoryUtil.exportIMObjectToString(newTopology);
		} else {
			topologyString = repositoryUtil.exportIMObjectToString(topology);
			result.setUserLogin("There are no nodes in FEDERICA to log in");

		}
		result.setTopologyCreated(topologyString);
		return result;
	}
	
	public void setUserLoginInfo(StringBuffer userLoginInfo) {
		this.userLoginInfo = userLoginInfo;
	}

	public FedericaRSpec getManifest() {
		return manifest;
	}

	public void setManifest(FedericaRSpec manifest) {
		this.manifest = manifest;
	}

	protected Topology addUserInfoInTopology(Topology topology) {

		if(!RHUtils.isSetEmpty(manifest.getNodesFromElement())) {
			LoginComponentImpl loginInfo = null;
			for (Element node : manifest.getNodesFromElement()) {
				 loginInfo = manifest.getLoginFromElement(node) ;
				 String nodeName = node.getAttribute(SFAConstants.CLIENT_ID);
				 nodeName = nodeName.substring(0, nodeName.length()-4);
				 
				 VirtualNode nodeTopology = findNodeByName(topology, nodeName);
				 if(nodeTopology != null) {
					 
					 Set<NodeComponent> componentSet = nodeTopology.getHasComponent();
					 if (RHUtils.isSetEmpty(componentSet)) {
						 componentSet = new HashSet<NodeComponent>();;
					 }
					 componentSet.add(loginInfo);
					 nodeTopology.setHasComponent(componentSet);
				 }
				 setLoginInfoAsString(loginInfo);
			}
		} 
		
		
		return topology;
	}
	
	/*
	 protected Topology addUserInfoInRouters(Topology topology) {
		if(!RHUtils.isSetEmpty(manifest.getRoutersFromElement())) {
			LoginComponentImpl loginInfo = null;
			for (Element router : manifest.getRoutersFromElement()) {
				 loginInfo = manifest.getLoginFromElement(router) ;
				 String routerName = router.getAttribute(SFAConstants.CLIENT_ID);
				 routerName = routerName.substring(0, routerName.length()-4);			 
				 VirtualNode routerTopology = findNodeByName(topology, routerName);
				 if(routerTopology != null) {
					 Set<NodeComponent> componentSet = new HashSet<NodeComponent>();
					 componentSet.add(loginInfo);
					 routerTopology.setHasComponent(componentSet);
				 }
				 setLoginInfoAsString(routerName, loginInfo);
			}
		} 
		return topology;
	}
	*/
	
	private void setLoginInfoAsString(LoginComponent loginComponent) {
		userLoginInfo.append(" HOST: " + loginComponent.getHasLoginIPv4Address().getHasValue());
		userLoginInfo.append(" PORT: " + loginComponent.getHasLoginPort());
		userLoginInfo.append(" USER " + loginComponent.getHasLoginUsername());
		userLoginInfo.append(" PASSWORD: " + loginComponent.getHasLoginPassword());
	}
	
	/*
	protected Topology addUserInfoInComputes(Topology topology) {
		if(!RHUtils.isSetEmpty(manifest.getComputesFromElement())) {
			LoginComponentImpl loginInfo = null;
			for (Element compute : manifest.getComputesFromElement()) {
				 loginInfo = manifest.getLoginFromElement(compute) ;
				 String computeName = compute.getAttribute(SFAConstants.CLIENT_ID);
				 computeName = computeName.substring(0, computeName.length()-4);
				 
				 VirtualNode nodeTopology = findNodeByName(topology, computeName);
				 if(nodeTopology != null) {
					 Set<NodeComponent> componentSet = new HashSet<NodeComponent>();
					 componentSet.add(loginInfo);
					 nodeTopology.setHasComponent(componentSet);
				 }
				 setLoginInfoAsString(computeName, loginInfo);
			}
		} 
		return topology;
	}
	*/
	
	protected VirtualNode findNodeByName(Topology topology, String nodeNameToFind) {
		for (Resource resource : topology.getContains()) {
			if (resource instanceof VirtualNode) {
				String nodeName = RHUtils.removeNOVIURIprefix(resource.toString());
				if (nodeNameToFind.equals(nodeName)) {
					return (VirtualNode)resource;
				}
			}
		}
		return null;
	}

	private RHCreateDeleteSliceResponseImpl federateSlices(String sliceName, RHCreateDeleteSliceResponseImpl result, TopologyImpl topologyWithVLAN) {
		logService.log(LogService.LOG_INFO, "Name of slice provided to nswitch: " + sliceName);
		if (nswitchManager == null) {
			result = getResultAfterNswitchFails("RH - Create slice: nswitch-manager is NULL! ", result);
			deleteSlicesAfterNswitchFails(sliceName);
		} else {
			try {
				nswitchManager.createFederation(topologyWithVLAN, sliceName);
			}catch(FederationException ex){
				String cause = createErrorMessageFromException(ex);
				result = getResultAfterNswitchFails("RH - Create slice: nswitch throws the following FederationExcepion " + cause,
						result);
				ex.printStackTrace();
				deleteSlicesAfterNswitchFails(sliceName);
			} catch (Exception e) {
				e.printStackTrace();
				result = getResultAfterNswitchFails("RH - Create slice: nswitch throws the following Excepion " + e.toString(),
						result);
				deleteSlicesAfterNswitchFails(sliceName);
			}
		}
		return result;
	}

	private void sleep() {
		logService.log(LogService.LOG_INFO, "RH - Sleeping for: " + waitingTime + " miliseconds");
		try {
			Thread.sleep(new Long(waitingTime));
		} catch (NumberFormatException e) {
			logService.log(LogService.LOG_INFO,
					"RH - NumberFormatException during sleeping call: " + e.toString()
							+ "We continue to see if it works");
		} catch (InterruptedException e) {
			logService.log(LogService.LOG_INFO,
					"RH - InterruptedException during sleeping call: " + e.toString()
							+ "We continue to see if it works");
		}		
	}

	private RHCreateDeleteSliceResponseImpl manageVLANError(String sliceName) {
		RHCreateDeleteSliceResponseImpl result = new RHCreateDeleteSliceResponseImpl();
		result.setSliceID(sliceName);
		result.setHasError(true);
		result.setErrorMessage("RH - Error with slice VLAN: there is no VLAN set by FED");
		sendErrorFeedback(sessionID, "RH: Error federating slices", "Error with slice VLAN: there is no VLAN set by FED. Deleting slices because they have not been federated");

		try {
			deleteFedericaSlice(sliceName);
		} catch (Exception e) {
			logService.log(LogService.LOG_INFO, "RH - Error with slice VLAN: there is no VLAN set by FED"
					+ "\nRH - Error deleting FEDERICA slice: " + e.toString());
			result.setErrorMessage("RH - Error with slice VLAN: there is no VLAN set by FED"
					+ "\nRH - Error deleting FEDERICA slice: " + e.toString());
		}

		try {
			deletePlanetLabSlice(sliceName);
		} catch (Exception e) {
			logService.log(LogService.LOG_INFO, "RH - Error with slice VLAN: there is no VLAN set by FED"
					+ "\nRH - Error deleting PlanetLab slice: " + e.toString());
			result.setErrorMessage("RH - Error with slice VLAN: there is no VLAN set by FED"
					+ "\nRH - Error deleting PlanetLab slice: " + e.toString());
		}
		return result;
	}

	private RHCreateDeleteSliceResponseImpl checkStaticSlicesAndCreate(String topologyName, String sliceName) {
		RHCreateDeleteSliceResponseImpl result = null;

		if (topologyName.equals(STATIC_PARTIALLY_BOUND_SLICE)) {
			vlan = STATIC_VLAN1;
			logService.log(LogService.LOG_INFO, "RH - Manifest with VLAN: " + vlan + " " + topologyName);
			staticSlicesInFederica.add(sliceName);
			result = getCreateDeleteResponse(false, "");
		} else if (topologyName.equals(STATIC_BOUND_SLICE)) {
			vlan = STATIC_VLAN2;
			logService.log(LogService.LOG_INFO, "RH - Manifest with VLAN: " + vlan + " " + topologyName);
			staticSlicesInFederica.add(sliceName);
			result = getCreateDeleteResponse(false, "");
		} else {
			result = createFedericaSlice(sliceName);
		}	
		
		return result;
	}
	
	private RHCreateDeleteSliceResponseImpl checkStaticSlicesAndUpdate(String topologyName, String sliceName) {
		RHCreateDeleteSliceResponseImpl result = null;

		if (topologyName.equals(STATIC_PARTIALLY_BOUND_SLICE)) {
			vlan = STATIC_VLAN1;
			logService.log(LogService.LOG_INFO, "RH - Manifest with VLAN: " + vlan + " " + topologyName);
			staticSlicesInFederica.add(sliceName);
			result = getCreateDeleteResponse(false, "");
		} else if (topologyName.equals(STATIC_BOUND_SLICE)) {
			vlan = STATIC_VLAN2;
			logService.log(LogService.LOG_INFO, "RH - Manifest with VLAN: " + vlan + " " + topologyName);
			staticSlicesInFederica.add(sliceName);
			result = getCreateDeleteResponse(false, "");
		} else {
			result = populateFedSliceHandlingExceptions(sliceName);
		}	
		
		return result;
	}



	private String createErrorMessageFromException(Exception ex){
		StackTraceElement[] st = ex.getStackTrace();
		String cause = ex.toString()+ "\n is thrown by the following methods:";
		for (int i = 0; i < st.length; i++) {
			cause = cause + "\n" + st[i].getClassName() + ": " +st[i].getMethodName();
		}
		return cause;
	}
	
	private void deleteSlicesAfterNswitchFails(String sliceName) {
		try {
			deleteFedericaSlice(sliceName);
			deletePlanetLabSlice(sliceName);
		} catch (Exception e1) {
			e1.printStackTrace();
			logService.log(LogService.LOG_ERROR, "RH - Error deleting slices after NSwitch fail during the federation "
					+ e1.toString());
		}

	}

	private RHCreateDeleteSliceResponseImpl getResultAfterNswitchFails(String errorMessage, RHCreateDeleteSliceResponseImpl result) {
		logService.log(LogService.LOG_ERROR, errorMessage);
		result.setHasError(true);
		result.setErrorMessage("Error federating slices with nswtich " + nswitchManager.toString()
				+ ".\nError message: " + errorMessage);
		return result;
	}

	protected void initCreateUpdateMethods(NOVIUserImpl user) {
		vlan = "";
		noFedericaNodesInRequest = true;
		nodeURNsToAddPlanetLab = new ArrayList<String>();
		this.user = user;

	}

	private void prepareFedRSpec() {
		fedRSpec = new FedericaRSpec();
		fedRSpec.setUser(user);
		fedRSpec.setLogService(logService);
		fedRSpec.createEmptyRequestRSpec();		
	}

	/**
	 * Creates the federica slice if there are federica resources in the
	 * topology
	 * 
	 * @param fedRSpec
	 *            : the RSpec schema
	 * @param sliceName
	 *            : a string containing the name of the slice
	 * @return a RHCreateDeleteSliceResponse containing the slice id, if
	 *         creation is successful, or an error otherwise
	 */
	private RHCreateDeleteSliceResponseImpl createFedericaSlice(String sliceName) {
		RHCreateDeleteSliceResponseImpl r;

		try {
			prepareFedericaClient();

			String authorityCredential = sfaActions.getCredential(selfCredentialFed, SFAConstants.FEDERICA_AUTHORITY,
					SFAConstants.AUTHORITY);

			sfaActions.addRecordFed(sliceName, SFAConstants.FEDERICA_AUTHORITY, authorityCredential);
		} catch (Exception e) {
			r = rollbackSlice(sliceName, "RH - Error creating Federica slice record: " + e.toString());
			return r;
		}
		
		r = populateFedSliceHandlingExceptions(sliceName);

		return r;
	}

	private RHCreateDeleteSliceResponseImpl populateFedSliceHandlingExceptions(
			String sliceName) {
		RHCreateDeleteSliceResponseImpl r;
		try {
			fedRSpec.addUAG();
			r = populateFedericaSlice(sliceName, fedRSpec);
		} catch (Exception e) {
			try {
				r = rollbackSliceCreation("RH - Error populating Federica slice: " + e.toString(), sliceName);
			} catch (Exception e1) {
				logService.log(	LogService.LOG_WARNING,
						"RH - Error deleting Federica slice record because create FEDERICA slice failed.\n"	+ e1.toString());
				r = getCreateDeleteResponse(true, "RH - Error deleting Federica slice record because create FEDERICA slice failed.\n"
						+ e1.toString());
			}
		}
		return r;
	}

	private RHCreateDeleteSliceResponseImpl rollbackSlice(String sliceName, String errorMessage) {
		RHCreateDeleteSliceResponseImpl r;

		// If there was a PlanetLab slice, delete it:
		if (nodeURNsToAddPlanetLab.size() != 0) {
			try {
				deletePlanetLabSlice(sliceName);
			} catch (Exception e1) {
				logService.log(LogService.LOG_ERROR, "RH - Error creating FEDERICA slice: " + sliceName
						+ "\n and deleting PlanetLab slice: " + errorMessage);
			}
		}

		r = getCreateDeleteResponse(true, "RH - Error creating FEDERICA slice: " + errorMessage);

		return r;

	}

	/**
	 * Populates the federica slice that has been created. Same method used for
	 * the update slice.
	 * 
	 * @param sliceName
	 *            : string containing the name of the slice
	 * @param fedRSpec
	 *            : the RSpec
	 * @param fedSliceHRN
	 *            : string with the HRN of the slice
	 * @return a RHCreateDeleteSliceResponseImpl object containing the slice id,
	 *         if the operation is successfull, or an error message otherwise
	 * @throws XMLRPCClientException 
	 * @throws TestbedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private RHCreateDeleteSliceResponseImpl populateFedericaSlice(String sliceName, RSpecSchema fedRSpec) throws TestbedException, XMLRPCClientException, IOException
			 {
		RHCreateDeleteSliceResponseImpl r;
		String fedSliceURN = SFAConstants.FED_SLICE_URN_PREFIX + sliceName;
		String fedReqRspec = fedRSpec.toString();

		if (fedRSpec.validateSchema(fedReqRspec)) {
			prepareFedericaClient();

			String sliceCredential = sfaActions.getCredential(selfCredentialFed, SFAConstants.NOVI_FED_HRN_PREFIX
					+ sliceName, SFAConstants.SLICE);

			Map<String, Object> fedResultPopulate = (HashMap<String, Object>) sfaActions.populateFedSlice(fedSliceURN,
					sliceCredential, user, fedReqRspec);

			if (!SFAActions.isValidSFAResponse(fedResultPopulate)) {
				r = rollbackSliceCreation(fedResultPopulate.get(SFAConstants.OUTPUT).toString(), sliceName);
			} else {
				r = getCreateDeleteResponse(false, "");
				getVlanFromManifest((String) fedResultPopulate.get(SFAConstants.VALUE));
				logService.log(LogService.LOG_INFO, "RH - FEDERICA slice created correctly");
			}
		} else {
			r = rollbackSlice(sliceName, "RH - Error validating created FEDERICA RSpec");
		}
		return r;
	}

	private RHCreateDeleteSliceResponseImpl rollbackSliceCreation(
			String errorMessage, String sliceName) throws XMLRPCClientException, TestbedException, IOException  {
		RHCreateDeleteSliceResponseImpl r = rollbackSlice(sliceName, errorMessage);
		
		prepareFedericaClient();
		String sliceHRN = SFAConstants.NOVI_FED_HRN_PREFIX + sliceName;
		deleteRecord(selfCredentialFed, SFAConstants.FEDERICA_AUTHORITY, sliceHRN);
		return r;
	}

	/**
	 * The manifest returned by the SFA wrapper of FEDERICA contains the VLAN in
	 * the node names. In order to know the VLAN, we read a node from the
	 * manifest and set the value indicated in it, as the nodes in FEDERICA are
	 * call nodeID+VLAN.
	 * 
	 * @param manifestRSpec
	 */
	private void getVlanFromManifest(String manifestRSpec) {
		manifest = new FedericaRSpec();
		manifest.readRSpec(manifestRSpec);
		NodeList nodes = manifest.getNodeListFromDocument();
		if (nodes != null && nodes.getLength() > 0) {
			Element n = (Element) nodes.item(0);
			String completeName = n.getAttributeNode("client_id").getValue();
			vlan = completeName.substring(completeName.length() - 4);
			logService.log(LogService.LOG_INFO, "RH - Manifest with VLAN: " + vlan);
		}
	}

	private TopologyImpl addVlanToNSwitchTopology(TopologyImpl topology) {
		Set<Resource> resourcesRequested = topology.getContains();
		for (Resource resource : resourcesRequested) {
			if (resource instanceof Link) {
				Link l = (Link) resource;
				Set<LinkOrPath> paths = l.getProvisionedBy();
				if (paths != null) {
					Path path = (Path) paths.iterator().next();
					Set<Resource> resources = path.getContains();
					if (resources != null) {
						for (Resource res : resources) {
							if (res instanceof NSwitch) {
								logService.log(LogService.LOG_INFO, "RH - Adding VLAN " + vlan + " to NSwitch");
								NSwitch nSwitch = (NSwitch) res;
								Set<String> hasVLANID = new HashSet<String>();
								hasVLANID.add(vlan);
								nSwitch.setHasVLANID(hasVLANID);
							}
						}
					}
				}
			}
		}
		return topology;
	}

	private RHCreateDeleteSliceResponseImpl setTestbedsToResponse(RHCreateDeleteSliceResponseImpl result) {
		ArrayList<String> testbedList = new ArrayList<String>();

		if (!noFedericaNodesInRequest) {
			testbedList.add(SFAConstants.NOVI_IM_URI_BASE + SFAConstants.FEDERICA);
		}

		if (nodeURNsToAddPlanetLab.size() != 0) {
			testbedList.add(SFAConstants.NOVI_IM_URI_BASE + SFAConstants.PLANETLAB);
		}

		result.setListOfTestbedsWhereSliceIsCreated(testbedList);

		return result;
	}

	public RHListResourcesResponseImpl listResources(String user) {
		RHListResourcesResponseImpl r;
		sendInfoFeedback(sessionID, "RH list resources", "List resources from testbed " + testbed);

		try {
			prepareClientsAccordingToTestbed();
		} catch (Exception e) {
			logService.log(LogService.LOG_INFO, "RH - Error getting selfcredential " + testbed
					+ " for listing resources: " + e.toString());
			r = getListResourcesResponse(true, "RH - Error getting selfcredential " + testbed
					+ " for listing resources: " + e.toString());
			return r;
		}

		r = getResourcesFromTestbed();
		return r;
	}

	/**
	 * Gets the resource from the testbed and analyzes the result.
	 * 
	 * @return
	 */
	private RHListResourcesResponseImpl getResourcesFromTestbed() {
		RHListResourcesResponseImpl r;

		// Get the resource from testbed:
		Map<String, Object> resourcesMap = null;
		try {
			if (testbed.equalsIgnoreCase(SFAConstants.PLANETLAB)) {
				resourcesMap = (HashMap<String, Object>) sfaActions.listResources(selfCredentialPL);
			} else if (testbed.equalsIgnoreCase(SFAConstants.FEDERICA)) {
				resourcesMap = (HashMap<String, Object>) sfaActions.listResources(selfCredentialFed);
			}
		} catch (Exception e) {
			logService.log(LogService.LOG_DEBUG, "RH - Error listing " + testbed + " resources. " + e.toString());
			r = getListResourcesResponse(true, "RH - Error listing " + testbed + " resources. " + e.toString());
			return r;
		}

		r = checkListResourcesResult(resourcesMap);

		return r;
	}

	/**
	 * Checks the result got from testbed, and if correct, translates it to a IM
	 * Platform.
	 * 
	 * @param resourcesMap
	 *            : Map obtained from listing testbed resources
	 * @return
	 */
	private RHListResourcesResponseImpl checkListResourcesResult(Map<String, Object> resourcesMap) {
		RHListResourcesResponseImpl r;
		// Check the results
		try {
			if (resourcesMap == null) {
				logService.log(LogService.LOG_DEBUG, "RH - Error listing " + testbed
						+ " resources: testbed returned null");
				r = getListResourcesResponse(true, "RH - Error listing " + testbed
						+ " resources: testbed returned null");
			} else if (SFAActions.isValidSFAResponse(resourcesMap)) {
				String rspecString = (String) resourcesMap.get(SFAConstants.VALUE);
				r = translateAdvertisementRSpecToPlatform(rspecString);
			} else {
				logService.log(LogService.LOG_DEBUG,
						"RH - Error listing " + testbed + " resources: " + resourcesMap.get(SFAConstants.OUTPUT));
				r = getListResourcesResponse(true,
						"RH - Error listing " + testbed + " resources: " + resourcesMap.get(SFAConstants.OUTPUT));
			}
		} catch (Exception e) {
			logService.log(LogService.LOG_DEBUG, "RH - Error listing " + testbed + " resources: " + e.toString());
			r = getListResourcesResponse(true, "RH - Error listing " + testbed + " resources: " + e.toString());
		}

		return r;
	}

	/*
	 * public RHListResourcesResponseImpl listSliceResources(String user, String
	 * sliceHRN) { //TODO: To fix RHListResourcesResponseImpl r = new
	 * RHListResourcesResponseImpl();
	 * 
	 * try { preparePlanetLabClient();
	 * 
	 * String sliceCredential = sfaActions.getCredential(selfCredentialPL,
	 * sliceHRN, sliceString, testbed); String sliceURN =
	 * SFAConstants.NOVI_PL_SLICE_URN_PREFIX + sliceHRN;
	 * 
	 * Object response = sfaActions.listSliceResources(sliceCredential,
	 * sliceURN, testbed); Map<String, Object> responseMap = (HashMap<String,
	 * Object>) response; Map<String, Object> codeMap = (HashMap<String,
	 * Object>)responseMap.get(codeString); if
	 * ((Integer)codeMap.get(geniCodeString) == 0) {
	 * logService.log(LogService.LOG_DEBUG, "RH: List slice OK");
	 * r.setHasError(false); } } catch (Exception e) {
	 * logService.log(LogService.LOG_ERROR, "Error listing Resources: " +
	 * e.toString()); r.setHasError(true); r.setErrorMessage(e.toString()); }
	 * finally { return r; } }
	 */

	/**
	 * Translates Advertisement RSpec to NOVI IM
	 * 
	 * @param rspecString
	 *            : Advertisement RSpec as a String
	 * @return RHListResourceResponse object containing a platform with the
	 *         resources (nodes and links), or an error
	 */
	protected RHListResourcesResponseImpl translateAdvertisementRSpecToPlatform(String rspecString) {

		if (rspecString.equals("")) {
			return getListResourcesResponse(true, "Empty RSpec string");
		}

		RSpecSchema rspec = null;
		if (testbed.equalsIgnoreCase(SFAConstants.PLANETLAB)) {
			rspec = new PlanetLabRSpecv2();
		} else if (testbed.equalsIgnoreCase(SFAConstants.FEDERICA)) {
			rspec = new FedericaRSpec();
		} else {
			return getListResourcesResponse(true, "Incorrect testbed name");
		}

		rspec.setLogService(logService);
		return getListResourcesResponse(rspecString, rspec);
	}

	private RHListResourcesResponseImpl getListResourcesResponse(String rspecString, RSpecSchema rspecSchema) {
		RHListResourcesResponseImpl r = getListResourcesResponse(false, "");
		PlatformImpl platform = new PlatformImpl(testbed);
		
		rspecSchema.readRSpec(rspecString);
		Set<Resource> resources = rspecSchema.getResourceSet();
		platform.setContains(resources);

		logService.log(LogService.LOG_DEBUG, "Translating the slice information to string format...");
		IMRepositoryUtil repositoryUtil = new IMRepositoryUtilImpl();
		String platformString = repositoryUtil.exportIMObjectToString(platform);
		r.setPlatformString(platformString);

		return r;
	}

	private RHListResourcesResponseImpl getListResourcesResponse(boolean error, String message) {
		RHListResourcesResponseImpl r = new RHListResourcesResponseImpl();
		if(error) {
			sendErrorFeedback(sessionID, "RH list resources", message);
		}
		r.setHasError(error);
		r.setErrorMessage(message);
		return r;
	}

	public RHCreateDeleteSliceResponseImpl updateSlice(String sessionID, NOVIUserImpl user, String sliceName, TopologyImpl oldTopology, TopologyImpl newTopology) {
		RHCreateDeleteSliceResponseImpl r = null;
		String topologyName = oldTopology.toString();
		initCreateUpdateMethods(user);

		try {
			releaseFederation(sliceName, oldTopology);
			analyzeTopologyReceived(newTopology);
		} catch (RHBadInputException e) {
			if (RHUtils.isSetEmpty(newTopology.getContains())) {
				deleteSlicesIfExistInTestbed(sliceName);
				r = getCreateDeleteResponse(false, "");
				r.setSliceID(sliceName);
				return r;
			}
			r = getCreateDeleteResponse(true, "RH - Error in the topology: " + e.toString() + " The slice keeps the same.");
			r.setSliceID(sliceName);
			return r;
		} catch (Exception e) {
			r = getCreateDeleteResponse(true, "RH - Error releasing the federation: " + e.toString() + " The slice keeps the same.");
			r.setSliceID(sliceName);
			return r;
		}
			
		r = updateFederatedSlice(topologyName, sliceName);

		TopologyImpl topologyWithVLAN = newTopology;
		if (!r.hasError() && hasNSwitch(newTopology)) {
			if (vlan.trim().equals("") || vlan.isEmpty()) {
				r = manageVLANError(sliceName);
				return r;
			} else {
				sleep();
				sendInfoFeedback(sessionID, "RH create slice", "Slice " + sliceName + " created successfully. Federating testbeds.");
				topologyWithVLAN = addVlanToNSwitchTopology(newTopology);
				federateSlices(sliceName, r, topologyWithVLAN);
			}
		}
		
		r = setTopologyInResult(r, topologyWithVLAN);
		r = setTestbedsToResponse(r);
		r.setSliceID(sliceName);
		return r;
	}

	private void deleteSlicesIfExistInTestbed(String sliceName) {
		if(sliceExistsInTestbed(sliceName, SFAConstants.FEDERICA)) {
			try {
				deleteFedericaSliceStaticOrReal(sliceName);
			} catch (Exception e1) {
				logService.log(LogService.LOG_INFO, "RH - There was a problem listing slices to see if the slice to update is existing or not. There may be inconsistencies later. " + e1.getMessage());
			} 
		}
		if(sliceExistsInTestbed(sliceName, SFAConstants.PLANETLAB)) {
			try {
				deletePlanetLabSlice(sliceName);
			} catch (Exception e1) {
				logService.log(LogService.LOG_INFO, "RH - There was a problem listing slices to see if the slice to update is existing or not. There may be inconsistencies later. " + e1.getMessage());
			} 
		}
	}


	public RHCreateDeleteSliceResponseImpl releaseFederationAndDeleteSlice(String sessionID, String sliceID, Set<String> platformURIs, TopologyImpl topology) {
		RHCreateDeleteSliceResponseImpl r;
		try {
			releaseFederation(sliceID, topology);
			r = deleteSlicesAndAnalizeResults(sliceID, platformURIs);

		} catch (Exception e) {
			r = getCreateDeleteResponse(true, "RH - Error deleting slice: " + e.toString());
			return r;
		}

		r.setSliceID(sliceID);
		return r;
	}
	
	protected void releaseFederation(String sliceID, TopologyImpl topology) throws Exception {
		if (hasNSwitch(topology)) {
			nswitchManager.releaseFederation(sliceID);
		}
	}

	private RHCreateDeleteSliceResponseImpl deleteSlicesAndAnalizeResults(String sliceID, Set<String> platformURIs) throws XMLRPCClientException, TestbedException, IOException  {
		RHCreateDeleteSliceResponseImpl r;
		Integer responseDeletePL = 0;
		Integer responseDeleteFed = 0;
		boolean fedResources = false;
		boolean plResources = false;
		
		for (String platform : platformURIs) {
			if (RHUtils.removeNOVIURIprefix(platform).equalsIgnoreCase("planetlab")) {
				plResources = true;
				responseDeletePL = deletePlanetLabSlice(sliceID);
			} else if (RHUtils.removeNOVIURIprefix(platform).equalsIgnoreCase("federica")) {
				fedResources = true;
				responseDeleteFed = deleteFedericaSliceStaticOrReal(sliceID);
			} else {
				logService.log(LogService.LOG_DEBUG, "RH: delete slice for unknown platform " + platform);
			}
		}

		r = analizeResults(responseDeletePL, responseDeleteFed, fedResources, plResources);
		return r;
	}

	private Integer deleteFedericaSliceStaticOrReal(String sliceID) throws XMLRPCClientException, TestbedException, IOException  {
		Integer responseDeleteFed;
		if (!staticSlicesInFederica.contains(sliceID)) {
			responseDeleteFed = deleteFedericaSlice(sliceID);
		} else {
			staticSlicesInFederica.remove(staticSlicesInFederica.indexOf(sliceID));
			responseDeleteFed = 1;
		}
		
		return responseDeleteFed;
	}

	/**
	 * Check the results: the slice is correctly deleted if: 1. there
	 * are resources in both PL and FED and they were all correctly
	 * removed, 2. there are only FED resources and they were correctly
	 * removed 3. there are only PL resources and they were correctly
	 * removed
	 **/
	private RHCreateDeleteSliceResponseImpl analizeResults(Integer responseDeletePL, Integer responseDeleteFed,
			boolean fedResources, boolean plResources) {
		RHCreateDeleteSliceResponseImpl r = new RHCreateDeleteSliceResponseImpl();
		if ((fedResources && responseDeleteFed == 1 && plResources && responseDeletePL == 1)
				|| (fedResources && responseDeleteFed == 1 && !plResources)
				|| (!fedResources && plResources && responseDeletePL == 1)) {
			r = getCreateDeleteResponse(false, "");
		} else {
			r = getCreateDeleteResponse(true, "FEDERICA response: " + responseDeleteFed.toString()
					+ "\n PlanetLab response: " + responseDeletePL.toString());
		}
		return r;
	}

	/**
	 * Method for deleting a FEDERICA slice: - Set federica XMLRPC client - Get
	 * federica self credential - Get slice credential - Remove slice resources
	 * - Get Authority credential. - Remove record.
	 * @throws TestbedException 
	 * @throws XMLRPCClientException 
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	private int deleteFedericaSlice(String sliceID) throws XMLRPCClientException, TestbedException, IOException  {
		logService.log(LogService.LOG_INFO, "RH: calling delete slice for FEDERICA resources");

		prepareFedericaClient();

		String sliceHRNFED = SFAConstants.NOVI_FED_HRN_PREFIX + sliceID;
		String sliceURNFED = SFAConstants.FED_SLICE_URN_PREFIX + sliceID;

		deleteSliceResources(selfCredentialFed, sliceURNFED);

		int responseDeleteFed = deleteRecord(selfCredentialFed, SFAConstants.FEDERICA_AUTHORITY, sliceHRNFED);

		return responseDeleteFed;
	}

	/**
	 * This method deletes a PlanetLab slice, for that we do - Set NOVI
	 * PlanetLab XMLRPC client - Get NOVI PlanetLab self credential - Get slice
	 * credential - Remove slice resources - Get Authority credential - Remove
	 * record.
	 * @throws TestbedException 
	 * @throws XMLRPCClientException 
	 * @throws IOException 
	 */
	private int deletePlanetLabSlice(String sliceID) throws XMLRPCClientException, TestbedException, IOException  {
		logService.log(LogService.LOG_INFO, "RH: calling delete slice for PlanetLab resources");

		preparePlanetLabClient();

		String sliceHRNPL = SFAConstants.NOVI_PL_HRN_PREFIX + sliceID;
		String sliceURNPL = SFAConstants.NOVI_PL_SLICE_URN_PREFIX + sliceID;

		deleteSliceResources(selfCredentialPL, sliceURNPL);

		int responseDeletePL = deleteRecord(selfCredentialPL, SFAConstants.NOVI_PL_AUTHORITY, sliceHRNPL);

		return responseDeletePL;
	}

	/**
	 * Deletes the resources of a slice.
	 * 
	 * @param selfCredential
	 * @param sliceURN
	 * @throws XMLRPCClientException 
	 * @throws TestbedException 
	 * @throws Exception
	 */
	private void deleteSliceResources(String selfCredential, String sliceURN) throws TestbedException, XMLRPCClientException  {
		String sliceCredential = sfaActions.getCredential(selfCredential, sliceURN, SFAConstants.SLICE);

		sfaActions.removeSliceResources(sliceURN, sliceCredential);
	}

	/**
	 * Private method that deletes the record of a slice from the registry
	 * 
	 * @param selfCredential
	 *            - RH credential
	 * @param authorityName
	 *            - Authority of the testbed
	 * @param sliceHRN
	 *            - Identifies the slice to delete
	 * @return
	 * @throws XMLRPCClientException 
	 * @throws TestbedException 
	 * @throws Exception
	 */
	private int deleteRecord(String selfCredential, String authorityName, String sliceHRN) throws TestbedException, XMLRPCClientException  {
		String authorityCredential = sfaActions.getCredential(selfCredential, authorityName, SFAConstants.AUTHORITY);

		Integer responseDelete = (Integer) sfaActions.removeSlice(sliceHRN, authorityCredential);
		return responseDelete;
	}

	/**
	 * Creates the response for methods createSlice, updateSlice and deleteSlice
	 * 
	 * @param error: boolean that indicates if there is error or not
	 * @param message: message error
	 * @return RHCreateDeleteSliceResponseImpl to be returned to RIS
	 */
	private RHCreateDeleteSliceResponseImpl getCreateDeleteResponse(boolean error, String message) {
		if(error) {
			sendErrorFeedback(sessionID, "Request Handler error", message);
		}
		RHCreateDeleteSliceResponseImpl response = new RHCreateDeleteSliceResponseImpl();
		response.setErrorMessage(message);
		response.setHasError(error);
		return response;
	}

	public RHListSlicesResponseImpl listUserSlices(String user) {
		RHListSlicesResponseImpl r = new RHListSlicesResponseImpl();
		List<String> slices;

		for (String testbedName : SFAConstants.FEDERATED_TESTBEDS) {
			try {
				slices = getSlicesFromRegistryDependingTestbedAndUser(testbedName, user);
				r = analizeSlicesReceivedAndAddThemToResult(slices, r);
			} catch (Exception e) {
				if (e.toString().toLowerCase().contains("record not found")) {
					logService.log(LogService.LOG_INFO, "RH - User " + user + " doesn't exist in testbed " + testbedName + ".\nError message: " + e.toString());
				} else {
					r = getListSliceResponse(true, null, "RH - Error listing slices of testbed " + testbedName + ".\nError message: " + e.toString());
					return r;
				}
			}
		}

		r.setHasError(false);
		return r;
	}
	
	private RHListSlicesResponseImpl analizeSlicesReceivedAndAddThemToResult(List<String> slices, RHListSlicesResponseImpl r) {
		if (slices != null && !slices.isEmpty()) {
			for (String slice : slices) {
				r = addSliceToResponseIfValid(r, slice);
			}
		}
		return r;
	}

	private RHListSlicesResponseImpl addSliceToResponseIfValid(RHListSlicesResponseImpl r, String slice) {		
		if (slice.toLowerCase().contains("novipl.novi") || slice.toLowerCase().contains("federica.eu") 
				|| (slice.toLowerCase().contains("firexp.novi") && slice.toLowerCase().contains("bonfire"))) {
			r.addSlice(slice);
			logService.log(LogService.LOG_INFO, "RH - Found slice " + slice + " for user " + user);
		}
		logService.log(LogService.LOG_INFO, "RH - Found slice " + slice + " for user " + user
				+ " but is not in novipl.novi or federica.eu domains. Not added to slice list.");
		return r;
	}

	private ArrayList<String> getSlicesFromRegistryDependingTestbedAndUser(String testbed, String user) throws XMLRPCClientException, TestbedException, RHBadInputException, IOException  {
		ArrayList<String> slices = null;
		String credential;
		String userHrn;

		if (testbed.equalsIgnoreCase(SFAConstants.PLANETLAB)) {
			preparePlanetLabClient();
			credential = selfCredentialPL;
			userHrn = SFAConstants.NOVI_PL_HRN_PREFIX + user;
		} else if (testbed.equalsIgnoreCase(SFAConstants.FEDERICA)) {
			prepareFedericaClient();
			credential = selfCredentialFed;
			userHrn = SFAConstants.NOVI_FED_HRN_PREFIX + user;
		} else {
			throw new RHBadInputException("Testbed is not valid for listing slices");
		}

		Object[] record = sfaActions.resolve(credential, userHrn);

		slices = getUserSlicesFromRecord(record);

		return slices;
	}

	protected ArrayList<String> getUserSlicesFromRecord(Object[] result) {
		ArrayList<String> userSlices = new ArrayList<String>();
		if (result != null && result.length > 0) {
			for (Object object : result) {
				if (object instanceof Map) {
					Map<String, Object> objectMap = (Map<String, Object>) object;
					if (objectMap.containsKey("slices")) {
						Object[] slices = (Object[]) objectMap.get("slices");
						for (Object slice : slices) {
							String sliceID = (String) slice;
							userSlices.add(sliceID);
						}
					}
				}
			}
		}
		return userSlices;
	}

	private RHListSlicesResponseImpl getListSliceResponse(boolean hasError, List<String> slices,
			String errorMessage) {
		if(hasError) {
			sendErrorFeedback(sessionID, "RH, list slices", errorMessage);
		}
		RHListSlicesResponseImpl r = new RHListSlicesResponseImpl();
		r.setErrorMessage(errorMessage);
		r.setHasError(hasError);
		r.setSlices(slices);
		return r;
	}
	
	public RHListSlicesResponseImpl listSlices() {
		RHListSlicesResponseImpl response;
		try {
			prepareClientsAccordingToTestbed();
			List<String> slices = getSlicesFromTestbed(testbed);
			response = getListSliceResponse(false, slices, "");
		} catch (Exception e) {
			response = getListSliceResponse(true, null, "RH - Error listing slices. " + e.toString());	
			return response;
		}
		return response;
	}
	
	protected List<String> getSlicesFromTestbed(String testbed) throws TestbedException, XMLRPCClientException, IOException  {
		Map<String, Object> slicesMap = null;
		if (testbed.equalsIgnoreCase(SFAConstants.PLANETLAB)) {
			preparePlanetLabClient();
			slicesMap = (HashMap<String, Object>) sfaActions.listAllSlices(selfCredentialPL);
		} else if (testbed.equalsIgnoreCase(SFAConstants.FEDERICA)) {
			prepareFedericaClient();
			slicesMap = (HashMap<String, Object>) sfaActions.listAllSlices(selfCredentialFed);
		} else {
			throw new TestbedException("RH - Error listing slices, testbed is not set.");
		}

		List<String> slices;
		if (SFAActions.isValidSFAResponse(slicesMap)) {
			slices = getSlicesFromResponse(slicesMap);		
		} else {
			throw new TestbedException("RH - Error listing slices. " + slicesMap.get(SFAConstants.OUTPUT));
		}
		return slices;
	}

	protected List<String> getSlicesFromResponse(Map<String, Object> slicesMap) {
		List<String> sliceList = new ArrayList<String>();
		Object[] slices = (Object[])slicesMap.get(SFAConstants.VALUE);
		for (Object slice : slices) {
			String sliceURN = (String)slice;
			if(!sliceURN.contains("BonFIRE")) {
				sliceList.add(sliceURN);
			}
		}

		return sliceList;
	}

	private void sendErrorFeedback(String sessionID, String shortMessage, String errorMessage) {
		logService.log(LogService.LOG_ERROR, errorMessage);

		if(sessionID == null)
			sessionID = userFeedback.getCurrentSessionID();
		userFeedback.errorEvent(sessionID, shortMessage, errorMessage, "http://fp7-novi.eu");		
	}
	
	private void sendInfoFeedback(String sessionID, String shortMessage, String longMessage) {
		logService.log(LogService.LOG_INFO, longMessage);

		if(sessionID == null)
			sessionID = userFeedback.getCurrentSessionID();
		userFeedback.instantInfo(sessionID, shortMessage, longMessage, "http://fp7-novi.eu");			
	}

	private void prepareClientsAccordingToTestbed() throws XMLRPCClientException, TestbedException, RHBadInputException, IOException  {
		if (testbed != null && testbed.equalsIgnoreCase(SFAConstants.PLANETLAB)) {
			preparePlanetLabClient();
		} else if (testbed != null && testbed.equalsIgnoreCase(SFAConstants.FEDERICA)) {
			prepareFedericaClient();
		} else {
			logService.log(LogService.LOG_INFO,
					"RH - The paramater testbed doesn't match with the testbeds RH has. Testbed has the following value: "
							+ testbed);
			throw new RHBadInputException(
					"The paramater testbed doesn't match with the testbeds RH has. Testbed has the following value: "
							+ testbed);
		}
	}

	/**
	 * Set the NOVI FEDERICA XMLRPC client for the SFAActions and gets the
	 * selfCredential for FEDERICA.
	 * @throws XMLRPCClientException 
	 * @throws TestbedException 
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	private void prepareFedericaClient() throws XMLRPCClientException, TestbedException, IOException  {
		FedXMLRPCClient fedClient = new FedXMLRPCClient();
		sfaActions.setClient(fedClient);

		selfCredentialFed = sfaActions.getSelfCredentialFed();
	}

	/**
	 * Set the NOVI PlanetLab XMLRPC client for the SFAActions and gets the
	 * selfCredential for PlanetLab.
	 * @throws XMLRPCClientException 
	 * @throws TestbedException 
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	private void preparePlanetLabClient() throws XMLRPCClientException, TestbedException, IOException  {
		NoviplXMLRPCClient client = new NoviplXMLRPCClient();
		sfaActions.setClient(client);
		
		selfCredentialPL = sfaActions.getSelfCredentialPL();
	}



	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}

	public NswitchManager getNswitchManager() {
		return nswitchManager;
	}

	public void setNswitchManager(NswitchManager nswitchManager) {
		this.nswitchManager = nswitchManager;
	}

	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSfaActions(SFAActions sfa) {
		this.sfaActions = sfa;
	}

	public SFAActions getSfaActions() {
		return sfaActions;
	}

	public String getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(String waitingTime) {
		this.waitingTime = waitingTime;
	}

	public NOVIUserImpl getUser() {
		return user;
	}

	public void setUser(NOVIUserImpl user) {
		this.user = user;
	}

	public List<String> getStaticSlicesInFederica() {
		return staticSlicesInFederica;
	}

	public void setStaticSlicesInFederica(List<String> staticSlicesInFederica) {
		this.staticSlicesInFederica = staticSlicesInFederica;
	}

	public void setUserFeedback(ReportEvent userFeedback) {
		this.userFeedback = userFeedback;
	}

	public ReportEvent getUserFeedback() {
		return userFeedback;
	}

	protected FedericaRSpec getFedericaRSpec() {
		return fedRSpec;
	}

}
