package eu.novi.policysender.monitoringevents;
/*package eu.novi.policy.monitoringevents;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.novi.im.core.Resource;
import eu.novi.policy.interfaces.InterfaceForMonitoring;
import eu.novi.policy.queue.Ponder2QueueProducer;

public class MonitoringEvents implements InterfaceForMonitoring{
     Ponder2QueueProducer ponder2Producer;
	
	public Ponder2QueueProducer getPonder2Producer()
	{
		return ponder2Producer;
	}
	
	public void setPonder2Producer ( Ponder2QueueProducer ponder2Producer)
	{
		this.ponder2Producer=ponder2Producer;
	}
	public MEReturn setMonitoringEvent(Resource resource, String event) {
		
		try {
			String queueProcessingID = ponder2Producer.pushRequest("This is the ponder2 Event");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			System.out.println("er");
			e.printStackTrace();
		}
		return null;		
	}
	public void setMonitoringEvent(TextMessage transmitEvent) {
		// TODO Auto-generated method stub
		
	}

}
*/