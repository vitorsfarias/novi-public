package eu.novi.feedback.event;

import javax.jms.JMSException;

import eu.novi.feedback.queue.FeedbackQueueProducer;

/**
 * Normally ReportEvent is provided as a service for other components to use.
 * It's instance, implementations and initializations are  performed via blueprint container
 * But for using within NOVI-API itself I need to recreate what blueprint does, so I am providing this for NOVI-API itself.
 * @author wibisono
 *
 */
public class ReportEventFactory {
	static ReportEvent currentReportEvent=null;

	static private String CURRENT_BROKER_URL = "tcp://localhost:61616";

	public static ReportEvent getReportEvent(){
		if(currentReportEvent == null ){
			initializeReportEvent();
		}
		
		return currentReportEvent;
	}

	private static void initializeReportEvent() {
		ReportEventImpl reImpl = new ReportEventImpl();
		FeedbackQueueProducer producer = new FeedbackQueueProducer();
		producer.setBroker(CURRENT_BROKER_URL);
		try {
			producer.initialize();
		} catch (JMSException e) {

			e.printStackTrace();
		}
		reImpl.setEventQueueProducer(producer);
		currentReportEvent = reImpl;
	}
	
	/**
	 * For testing purposes, getting reportEvent for certain broker URL
	 * @param brokerURL
	 * @return
	 */
	public static ReportEvent getReportEventBrokerURL(String brokerURL){
		setBrokerURL(brokerURL);
		return getReportEvent();
	}
	
	public static void setBrokerURL(String brokerURL){
		if(!brokerURL.equals(CURRENT_BROKER_URL)){
			CURRENT_BROKER_URL = brokerURL;
			initializeReportEvent();
		}
	}

		
}
