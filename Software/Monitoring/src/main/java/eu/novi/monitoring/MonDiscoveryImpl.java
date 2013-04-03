package eu.novi.monitoring;

import eu.novi.monitoring.MonSrv;
import java.util.List;
import java.util.ArrayList;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonDiscoveryImpl implements MonDiscovery {

	private static final transient Logger log =
		LoggerFactory.getLogger(MonDiscoveryImpl.class);	


	private List<MonSrv> monSrvList = new ArrayList<MonSrv>();

	private String testbed = "Unknown";

	public MonDiscoveryImpl() {

	}

	@Override
	public void setMonSrvList(List<MonSrv> monSrvList) {
		this.monSrvList = monSrvList;
	}

	@Override
	public List<MonSrv> getMonSrvList() {
		return monSrvList;
	}

	@Override
	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}

	@Override
	public String getTestbed() {
		return testbed;
	}

	@Override
	public MonSrv getInterface(String testbed) {
		return this.getService(testbed);
	}

	@Override
        public MonSrv getService(String testbed) {
		MonSrv result = null;

		if ((monSrvList !=null) && (monSrvList.size() > 0)) {
			for (MonSrv msrv : monSrvList) {
				if (msrv != null) {
					if (msrv.getPlatform().equals(testbed)) return msrv;
				}
			}
		}
		log.warn("The monSrvList does not contain MonSrv on the testbed:" + testbed);
		return getServiceBundle(testbed);
	}

	public MonSrv getServiceBundle(String testbed)
	{
	  try {
		Bundle bundle = null;
		bundle = FrameworkUtil.getBundle(MonDiscoveryImpl.class);
		BundleContext ctx = bundle.getBundleContext();
		ctx.getServiceReferences(null, null);
		ServiceReference [] monSrvs = ctx.getServiceReferences(MonSrv.class.getName(), null);
		if(monSrvs == null || monSrvs.length == 0){
			log.error("Cannot get MonSrv from bundle context. MonSrv is null or empty");
			return null;
		}else{
			for(int i = 0; i < monSrvs.length; i++){
				ServiceReference serviceReference = (ServiceReference)monSrvs[i];
				MonSrv msrv = (MonSrv) ctx.getService(serviceReference);
				if( msrv.getPlatform().equals(testbed) ) {
					return msrv;
				}
			}
			log.error("Cannot get MonSrv. There is no service on testbed:" + testbed);
		}
	  } catch(NoClassDefFoundError e1) {
		log.error("Problem to get the bundle of class: "+MonSrv.class.getName());
		e1.printStackTrace();
		return null;
	  } catch (InvalidSyntaxException e) {
		log.error("Problem to get service reference from context");
		e.printStackTrace();
		return null;
	  }
	  return null;
	}

}

