/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.impl;

import java.util.Set;

import org.osgi.service.log.LogService;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAActions;
import eu.novi.requesthandler.sfa.FederatedTestbed;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListResourcesResponseImpl;
import eu.novi.requesthandler.sfa.response.RHListSlicesResponseImpl;
import eu.novi.nswitch.manager.NswitchManager;


/**
 * Federation strategy for the SFA interface to the testbeds.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 *
 */
public class SFAFederatedTestbedImpl implements FederatedTestbed {
    private String testbed; //get for service mix blueprint
	private NswitchManager nswitchManager; //get for service mix blueprint   
    private LogService logService;// LogService to get for service mix blueprint
    private String waitingTime;
	private ReportEvent userFeedback;
    private String sessionID;

    public RHCreateDeleteSliceResponseImpl createSlice(String sessionID, NOVIUserImpl user, String sliceName, TopologyImpl topology) {
    	RHImpl rh = prepareRH();
    	
    	if (user == null || sliceName.isEmpty() || topology == null) {
    		RHCreateDeleteSliceResponseImpl response = createResponseWithError(sliceName, 
    				"Some of the inputs for creating slice are invalid. Received: user " + user 
    				+ " sliceName " + sliceName + " topology " + topology);
    		return response;
    	}
    	
    	RHCreateDeleteSliceResponseImpl response = rh.createSlice(sessionID, user, sliceName, topology);
    	
    	if (response == null) {
    		response = createUnknownErrorResponse(sliceName);
    	}
    	
    	response.setSliceID(sliceName);
    	
    	return response;
    }	
    
    public RHCreateDeleteSliceResponseImpl updateSlice(String sessionID, NOVIUserImpl user, String sliceName, TopologyImpl oldTopology, TopologyImpl newTopology) {
    	RHImpl rh = prepareRH();

    	if (user == null || sliceName.isEmpty() || newTopology == null) {
    		RHCreateDeleteSliceResponseImpl response = createResponseWithError(sliceName, 
    				"Some of the inputs for updating slice are invalid. Received: user " + user 
    				+ " sliceName " + sliceName + " topology " + newTopology);
    		return response;
    	}
    	
    	RHCreateDeleteSliceResponseImpl response = rh.updateSlice(sessionID, user, sliceName, oldTopology, newTopology);
    	
    	if (response == null) {
    		response = createUnknownErrorResponse(sliceName);
    	}

    	return response;
    }	

	public RHListResourcesResponseImpl listResources(String user) {
		
		RHImpl rh = prepareRH();
		
    	RHListResourcesResponseImpl response = rh.listResources(user);
    	
    	if (response == null) {
    		response = listResponseWithError("Not known error: method returned null");
    	}
    	
    	return response;

	}
		
	public RHCreateDeleteSliceResponseImpl deleteSlice(String sessionID, String sliceName, Set<String> platformURIs, TopologyImpl topology) {	
		RHCreateDeleteSliceResponseImpl response;
		
		if (platformURIs.size() < 1) {
			response = createResponseWithError(sliceName, 
					"Error deleting the slice "+ sliceName +": there are no platforms specified in the call." +
							" We can't delete slice if we don´t know which platform(s) it pertains to.");
			return response;
		}
		RHImpl rh = prepareRH();

    	response = rh.releaseFederationAndDeleteSlice(sessionID, sliceName, platformURIs, topology);
    	
    	if (response == null) {
    		response = createUnknownErrorResponse(sliceName);
    	}
    	
    	return response;
		
	}
	
	public RHListSlicesResponseImpl listUserSlices(String user) {
		logService.log(LogService.LOG_INFO, "RH - Starting list user slices");
		RHListSlicesResponseImpl response;
		
		if (user == null || user.isEmpty()) {
			response = new RHListSlicesResponseImpl();
			response.setHasError(true);
			response.setErrorMessage("User can't be null or empty.");
			sendErrorFeedback("User can't be null or empty.");
			logService.log(LogService.LOG_ERROR, "RH - User is null or empty.");
			return response;
		}
		
		RHImpl rh = prepareRH();
		
		response = rh.listUserSlices(user);
		
		return response;
	}
	

	public RHListSlicesResponseImpl listAllSlices() {
		
		RHImpl rh = prepareRH();
		RHListSlicesResponseImpl response = rh.listSlices();
		return response;
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

	public String getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(String waitingTime) {
		this.waitingTime = waitingTime;
	}
	
	protected RHCreateDeleteSliceResponseImpl createUnknownErrorResponse(String sliceName) {
		String message = "Not known error: method returned null";
		RHCreateDeleteSliceResponseImpl response = new RHCreateDeleteSliceResponseImpl();
		response.setSliceID(sliceName);
		response.setHasError(true);
		response.setErrorMessage(message);
		sendErrorFeedback(message);
		return response;
	}
	
	private void sendErrorFeedback(String message) {
		if(sessionID == null) {
			sessionID = userFeedback.getCurrentSessionID();
		}
		
		userFeedback.errorEvent(sessionID, "Error in Request Handler", message, "http://fp7-novi.eu");		
	}

	protected RHCreateDeleteSliceResponseImpl createResponseWithError(String sliceName, String error) {
		RHCreateDeleteSliceResponseImpl response = new RHCreateDeleteSliceResponseImpl();
		response.setSliceID(sliceName);
		response.setHasError(true);
		response.setErrorMessage(error);
		sendErrorFeedback(error);
		return response;
	}
	
	protected RHListResourcesResponseImpl listResponseWithError(String error) {
		RHListResourcesResponseImpl response = new RHListResourcesResponseImpl();
		response.setHasError(true);
		response.setErrorMessage(error);
		sendErrorFeedback(error);
		return response;
	}
	
	protected RHImpl prepareRH() {
		RHImpl rh = new RHImpl();
    	
    	SFAActions sfaActions = new SFAActions();
    	rh.setSfaActions(sfaActions);
    	rh.setNswitchManager(nswitchManager);
    	rh.setLogService(logService);
    	rh.setTestbed(testbed);
    	rh.setWaitingTime(waitingTime);
    	rh.setUserFeedback(userFeedback);
    	
    	return rh;
	}

	public void setReportUserFeedback(ReportEvent reportUserFeedback) {
		this.userFeedback = reportUserFeedback;
	}

	public ReportEvent getReportUserFeedback() {
		return userFeedback;
	}




}
