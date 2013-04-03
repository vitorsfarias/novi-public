package eu.novi.demoEventsImpl;

import java.net.URISyntaxException;
import java.util.UUID;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.feedback.event.ReportEvent;
import eu.novi.feedback.event.ReportEventFactory;
import eu.novi.api.request.handler.impl.RequestHandlerImpl;
import eu.novi.api.request.queue.RequestQueueProducer;
import eu.novi.demoEvents.EventHandler;
import eu.novi.policyAA.interfaces.InterfaceForMonitoring;
import eu.novi.policyAA.monitoringInteractions.MonitoringEvents;

public class EventHandlerImpl implements EventHandler {
	InterfaceForMonitoring interfaceForMonitor;
	private static final transient Logger log = 
			LoggerFactory.getLogger(EventHandlerImpl.class);
	
	private static final String SYSTEM_INSTANCE_LOCATION = "http://localhost:8080";
	
	
	
	
	@Override
	public int receiveEvent(String event){

		log.info("CXF REST endpoint Accepting Event from the demo-events component");
		String topo="";
		interfaceForMonitor.ResourceFailure(topo, event);
		return 0;
	}


	public InterfaceForMonitoring getInterfaceForMonitor() {
		return interfaceForMonitor;
	}


	public void setInterfaceForMonitor(InterfaceForMonitoring interfaceForMonitor) {
		this.interfaceForMonitor = interfaceForMonitor;
	}


	@Override
	public String deleteRequestHandler(String sliceID) throws JMSException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

}
