package eu.novi.monitoring;

import java.util.Map;
//import eu.novi.feedback.event.ReportEvent;

public interface Wiring {
	void setTestbed(String name);
	String getTestbed();
//	void setUserFeedback(ReportEvent userFeedback);
//	ReportEvent getUserFeedback();
	void addService(MonSrv service, Map<String, Object> properties);
	void removeService(MonSrv service);

}
