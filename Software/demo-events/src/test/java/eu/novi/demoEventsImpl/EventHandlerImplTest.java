package eu.novi.demoEventsImpl;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.junit.Ignore;
import org.junit.Test;

import eu.novi.api.request.handler.impl.RequestHandlerImpl;
import eu.novi.api.request.queue.RequestQueueProducer;
import eu.novi.demoEventsImpl.EventHandlerImpl;
import eu.novi.framework.RESTCommunication;

public class EventHandlerImplTest {
	@Ignore
	@Test
	public void eventHandlerTest(){
		EventHandlerImpl ev=new EventHandlerImpl();
		String event="tesst";
		System.out.println("Calling the receive Event");
		//ev.setIrmMapperInterface(interfaceForMonitor);
	   ev.receiveEvent(event);
		
	}
	

	@Test
	@Ignore
	public void eventRESTcall(){
		String address="";
		String failureEvent="http://fp7-novi.eu/im.owl#slice_1355389980http://fp7-novi.eu/im.owl#sliver1";
		try {
			HashMap<String, String> requestParameters = new HashMap<String, String>();
			requestParameters.put("deliver", failureEvent);
			new RESTCommunication().executePostMethod("http://150.254.160.28:8080/deliver/EventHandler/receiveEvent",requestParameters);
			
			//new RESTCommunication().executePostMethod("http://150.254.160.28:8080/deliver/EventHandler/receiveEvent", "deliver", failureEvent);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
