package eu.novi.monitoring;

import eu.novi.monitoring.credential.*;
import eu.novi.monitoring.util.*;
import java.util.List;
//import eu.novi.resources.Resource;
//import eu.novi.policy.interfaces.InterfaceForMonitoring;


public interface MonSrv {
	// Tester function
	public List<String> echo(String message);
	
	public MonitoringQuery createQuery();

	public String getPlatform();

//	public InterfaceForMonitoring getPolicy();

//	public void setPolicy(InterfaceForMonitoring monevents);

	public String measure(Credential credential, String query);

	public String substrate(Credential credential, String query);

//	public String substrateFB(Credential credential, String query, String sessionID);

	public String sliceTasks(Credential credential, String query);

	public String addTask(Credential credential, String query);

	public String describeTaskData(Credential credential, String query);

	public String fetchTaskData(Credential credential, String query);

	public String modifyTask(Credential credential, String query);

	public String removeTask(Credential credential, String query);

	public String enableTask(Credential credential, String query);

	public String disableTask(Credential credential, String query);

	public boolean getTaskStatus(Credential credential, String query);

	public String addAggregator(Credential credential, String query);

	public String removeAggregator(Credential credential, String query);

	public String fetchAggregatorData(Credential credential, String query);

	public String addCondition(Credential credential, String query);

	public String removeCondition(Credential credential, String query);

	public String modifyCondition(Credential credential, String query);


    	// Substrate monitoring function
//	public String measure(Scredential, query):
//        pass
//
//    # Slice monitoring functions
//    def addTask(self, credential, query):
//        pass
//
//    def modifyTask(self, credential, query):
//        pass
//
//    def removeTask(self, credential, query):
//        pass
//
//    def getTaskStatus(self, credential, query):
//        pass
//
//    def addCondition(self, credential, query):
//        pass
//
//    def modifyCondition(self, credential, query):
//        pass
//
//    def removeCondition(self, credential, query):
//        pass
//

}
