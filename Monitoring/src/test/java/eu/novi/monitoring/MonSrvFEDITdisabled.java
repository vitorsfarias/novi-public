package eu.novi.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.novi.monitoring.credential.*;
import eu.novi.monitoring.util.*;

import eu.novi.framework.IntegrationTesting;

/**
 * Integration tests for MonSrv.
 * 
 * @author <a href="mailto:lakis@inf.elte.hu">Sandor Laki</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class MonSrvFEDITdisabled {
	private static final String SAMPLE_SERVICE = "eu.novi.monitoring.MonSrv";
        private static final String TESTBED_FEDERICA = "(testbed=FEDERICA)";
        private static final String TESTBED_PLANETLAB = "(testbed=PlanetLab)";
        private static final String TESTBED_ALL = null;
	 											  
	
	@Configuration 
    public static Option[] configuration() throws Exception {
        return IntegrationTesting.createConfigurationWithBundles("org.apache.servicemix.bundles.jsch","information-model");
    }

	public static void waitSafe() {
		try {
			 Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	
	@Test
	public void queryBuilderExample(BundleContext ctx) throws Exception {
                waitSafe();
                ctx.getServiceReferences(null, null);
                ServiceReference[] serviceReferences = ctx.getServiceReferences(MonSrv.class.getName(), TESTBED_FEDERICA);
                assert(serviceReferences != null);
                assertEquals(1, serviceReferences.length);
                MonSrv service = (MonSrv) ctx.getService((ServiceReference) serviceReferences[0]);

                //final MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
                UsernameRSAKey up = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

		MonitoringQuery q = service.createQuery();
		q.addFeature("measureMemoryInfo", "MemoryUtilization");
	        q.addResource("measureMemoryInfo", "smilax1", "Node");
        	q.addInterface("smilax1", "ifin", "hasInboundInterface");
	        q.addInterface("smilax1", "ifout", "hasOutboundInterface");
        	q.defineInterface("ifin","194.132.52.2", "hasIPv4Address");
	        q.defineInterface("ifout","194.132.52.2", "hasIPv4Address");
        	String query = q.serialize();
		System.out.println(query);
		String res = service.substrate(up, query);
		System.out.println(res);
		assertEquals(true, 20 < res.split("\n").length);

	}

       
	public void risLikeQueryExample(BundleContext ctx) throws Exception {
        waitSafe();
        ctx.getServiceReferences(null, null);
        final MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
        UsernameRSAKey up = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

        MonitoringQuery q = service.createQuery();
        //free memory
		q.addFeature("measureMemoryInfo", "PartitioningCost");
		q.addResource("measureMemoryInfo", "smilax1", "Node");
		//free disk  space
		q.addFeature("measureDiskInfo", "FreeDiskSpace");
		q.addResource("measureDiskInfo", "smilax1", "Node");
		//CPU load
		q.addFeature("measureCPULoad", "CPULoad");
		q.addResource("measureCPULoad", "smilax1", "Node");
		//CPU cores
		q.addFeature("measureCPUcores", "CPUCores");
		q.addResource("measureCPUcores", "smilax1", "Node");
		
        //q.addFeature("measureMemoryInfo", "FreeMemory");
        //q.addResource("measureMemoryInfo", "smilax1", "Node");
        q.addInterface("smilax1", "ifin", "hasInboundInterface");
        q.addInterface("smilax1", "ifout", "hasOutboundInterface");
        q.defineInterface("ifin","150.254.160.19", "hasIPv4Address");
        q.defineInterface("ifout","150.254.160.19", "hasIPv4Address");
        String query = q.serialize();
        System.out.println(query);
        String res = service.substrate(up, query);
        System.out.println(res);
        assertEquals(true, 20 < res.split("\n").length);

	}
 
	

}
