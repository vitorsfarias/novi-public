package eu.novi.monitoring;

import eu.novi.monitoring.MonSrv;
import java.util.List;


public interface MonDiscovery {

	public void setMonSrvList(List<MonSrv> monSrvList);

	public List<MonSrv> getMonSrvList();

        public MonSrv getInterface(String testbed);

        public MonSrv getService(String testbed);

	public void setTestbed(String testbed);

	public String getTestbed();

}

