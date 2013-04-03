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
        import java.lang.StackOverflowError;

	/**
	 * Integration tests for MonSrv.
	 * 
	 * @author <a href="mailto:lakis@inf.elte.hu">Sandor Laki</a>
	 *
	 */
	@RunWith(JUnit4TestRunner.class)
	public class MonSrvIT {
		private static final String SAMPLE_SERVICE = "eu.novi.monitoring.MonSrv";
													  
		
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

		
		
		public void echo(BundleContext ctx) throws Exception {
			waitSafe();
			ctx.getServiceReferences(null, null);
			final MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));				
			assertEquals(Arrays.asList("NOVI Monitoring Service v0.0", "mon"), service.echo("mon"));
		}

	        @Test	
		public void queryBuilderExample(BundleContext ctx) throws Exception {
			waitSafe();
			ctx.getServiceReferences(null, null);
			final MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
			UsernameRSAKey up = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");

			MonitoringQuery q = service.createQuery();
			q.addFeature("measureMemoryInfo", "FreeMemory");
			q.addResource("measureMemoryInfo", "smilax1", "Node");
			q.addInterface("smilax1", "ifin", "hasInboundInterface");
			q.addInterface("smilax1", "ifout", "hasOutboundInterface");
			q.defineInterface("ifin","150.254.160.19", "hasIPv4Address");
			q.defineInterface("ifout","150.254.160.19", "hasIPv4Address");
			String query = q.serialize();
                        try {
				System.out.println(query);
				String res = service.substrate(up, query);
				System.out.println(res);
				assertEquals(true, 20 < res.split("\n").length);
                        } catch(java.lang.StackOverflowError se) {
				se.printStackTrace();
                        } catch(Error e) {
                                e.printStackTrace();
                        }

	}

	@Test
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
        try {
       		String res = service.substrate(up, query);
        	System.out.println(res);
        	assertEquals(true, 20 < res.split("\n").length);
	} catch(java.lang.StackOverflowError se) {
		se.printStackTrace();
        } catch(Error e) {
                e.printStackTrace();
        }
	}
 
	
	
        public void substrate(BundleContext ctx) throws Exception {
		waitSafe();
                ctx.getServiceReferences(null, null);
                final MonSrv service = (MonSrv) ctx.getService(ctx.getServiceReference(MonSrv.class.getName()));
                UsernameRSAKey up = new UsernameRSAKey("novi_novi","/home/jenkins/.ssh/sfademo_key","");
                final String query = "<!DOCTYPE rdf:RDF [\r\n" + 
	 		"    <!ENTITY im \"http://fp7-novi.eu/im.owl#\" >\r\n" + 
	 		"    <!ENTITY unit \"http://fp7-novi.eu/unit.owl#\" >\r\n" + 
	 		"    <!ENTITY owl \"http://www.w3.org/2002/07/owl#\" >\r\n" + 
	 		"    <!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\r\n" + 
	 		"    <!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\r\n" + 
	 		"    <!ENTITY monitoring_stat \"http://fp7-novi.eu/monitoring_stat.owl#\" >\r\n" + 
	 		"    <!ENTITY monitoring_query \"http://fp7-novi.eu/monitoring_query.owl#\" >\r\n" + 
	 		"    <!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\r\n" + 
	 		"    <!ENTITY monitoring_features \"http://fp7-novi.eu/monitoring_features.owl#\" >\r\n" + 
	 		"    <!ENTITY monitoring_parameter \"http://fp7-novi.eu/monitoring_parameter.owl#\" >\r\n" + 
	 		"    <!ENTITY smilax1 \"http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl:\" >\r\n" + 
	 		"]>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"<rdf:RDF xmlns=\"http://fp7-novi.eu/monitoringQuery_example.owl#\"\r\n" + 
	 		"     xml:base=\"http://fp7-novi.eu/monitoringQuery_example.owl\"\r\n" + 
	 		"     xmlns:smilax1=\"&im;smilax1.man.poznan.pl:\"\r\n" + 
	 		"     xmlns:monitoring_features=\"http://fp7-novi.eu/monitoring_features.owl#\"\r\n" + 
	 		"     xmlns:im=\"http://fp7-novi.eu/im.owl#\"\r\n" + 
	 		"     xmlns:unit=\"http://fp7-novi.eu/unit.owl#\"\r\n" + 
	 		"     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n" + 
	 		"     xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
	 		"     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\r\n" + 
	 		"     xmlns:monitoring_stat=\"http://fp7-novi.eu/monitoring_stat.owl#\"\r\n" + 
	 		"     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
	 		"     xmlns:monitoring_query=\"http://fp7-novi.eu/monitoring_query.owl#\"\r\n" + 
	 		"     xmlns:monitoring_parameter=\"http://fp7-novi.eu/monitoring_parameter.owl#\">\r\n" + 
	 		"    <owl:Ontology rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl\">\r\n" + 
	 		"        <owl:imports rdf:resource=\"http://fp7-novi.eu/monitoring_query.owl\"/>\r\n" + 
	 		"    </owl:Ontology>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!--\r\n" + 
	 		"    ///////////////////////////////////////////////////////////////////////////////////////\r\n" + 
	 		"    //\r\n" + 
	 		"    // Datatypes\r\n" + 
	 		"    //\r\n" + 
	 		"    ///////////////////////////////////////////////////////////////////////////////////////\r\n" + 
	 		"     -->\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!--\r\n" + 
	 		"    ///////////////////////////////////////////////////////////////////////////////////////\r\n" + 
	 		"    //\r\n" + 
	 		"    // Individuals\r\n" + 
	 		"    //\r\n" + 
	 		"    ///////////////////////////////////////////////////////////////////////////////////////\r\n" + 
	 		"     -->\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#PlanetLab_smilax1.man.poznan.pl -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;PlanetLab_smilax1.man.poznan.pl\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Node\"/>\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Resource\"/>\r\n" + 
	 		"        <im:hasLogicalRouters rdf:datatype=\"&xsd;integer\">0</im:hasLogicalRouters>\r\n" + 
	 		"        <im:hasAvailableLogicalRouters rdf:datatype=\"&xsd;integer\">0</im:hasAvailableLogicalRouters>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"        <im:hrn></im:hrn>\r\n" + 
	 		"        <im:hostname>smilax1.man.poznan.pl</im:hostname>\r\n" + 
	 		"        <im:hardwareType>plab-pc</im:hardwareType>\r\n" + 
	 		"        <im:hasInboundInterface rdf:resource=\"&im;smilax1.man.poznan.pl:eth0-in\"/>\r\n" + 
	 		"        <im:hasOutboundInterface rdf:resource=\"&im;smilax1.man.poznan.pl:eth0-out\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#boundResources -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;boundResources\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Group\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#reqCPU -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;reqCPU\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;CPU\"/>\r\n" + 
	 		"        <im:hasAvailableCores rdf:datatype=\"&xsd;integer\">0</im:hasAvailableCores>\r\n" + 
	 		"        <im:hasCores rdf:datatype=\"&xsd;integer\">2</im:hasCores>\r\n" + 
	 		"        <im:hasCPUSpeed rdf:datatype=\"&xsd;float\">2.0</im:hasCPUSpeed>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#reqDisk -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;reqDisk\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Storage\"/>\r\n" + 
	 		"        <im:hasAvailableStorageSize rdf:datatype=\"&xsd;float\">0.0</im:hasAvailableStorageSize>\r\n" + 
	 		"        <im:hasStorageSize rdf:datatype=\"&xsd;float\">10.0</im:hasStorageSize>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#reqMem -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;reqMem\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Memory\"/>\r\n" + 
	 		"        <im:hasAvailableMemorySize rdf:datatype=\"&xsd;float\">0.0</im:hasAvailableMemorySize>\r\n" + 
	 		"        <im:hasMemorySize rdf:datatype=\"&xsd;float\">1.0</im:hasMemorySize>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#slice_1992460072 -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;slice_1992460072\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Group\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl:eth0-in -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;smilax1.man.poznan.pl:eth0-in\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Interface\"/>\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;NetworkElement\"/>\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Resource\"/>\r\n" + 
	 		"        <im:hasAvailableCapacity rdf:datatype=\"&xsd;float\">0.0</im:hasAvailableCapacity>\r\n" + 
	 		"        <im:hasCapacity rdf:datatype=\"&xsd;float\">0.0</im:hasCapacity>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"        <im:hasIPv4Address>150.254.160.19</im:hasIPv4Address>\r\n" + 
	 		"        <im:hasNetmask></im:hasNetmask>\r\n" + 
	 		"        <im:connectedTo rdf:resource=\"&im;PlanetLab_smilax1.man.poznan.pl\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/im.owl#smilax1.man.poznan.pl:eth0-out -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"&im;smilax1.man.poznan.pl:eth0-out\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Interface\"/>\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;NetworkElement\"/>\r\n" + 
	 		"        <rdf:type rdf:resource=\"&im;Resource\"/>\r\n" + 
	 		"        <im:hasCapacity rdf:datatype=\"&xsd;float\">0.0</im:hasCapacity>\r\n" + 
	 		"        <im:hasAvailableCapacity rdf:datatype=\"&xsd;float\">0.0</im:hasAvailableCapacity>\r\n" + 
	 		"        <im:exclusive rdf:datatype=\"&xsd;boolean\">false</im:exclusive>\r\n" + 
	 		"        <im:hasNetmask></im:hasNetmask>\r\n" + 
	 		"        <im:hasIPv4Address>150.254.160.19</im:hasIPv4Address>\r\n" + 
	 		"        <im:connectedTo rdf:resource=\"&im;PlanetLab_smilax1.man.poznan.pl\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/monitoringQuery_example.owl#last3 -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#last3\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&monitoring_stat;Tail\"/>\r\n" + 
	 		"        <monitoring_stat:sampleSize>3</monitoring_stat:sampleSize>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/monitoringQuery_example.owl#last3samples_largerthan_averageplusvariance -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#last3samples_largerthan_averageplusvariance\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&monitoring_stat;IsPositive\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/monitoringQuery_example.owl#measureMemoryInformation -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#measureMemoryInformation\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&monitoring_query;BundleQuery\"/>\r\n" + 
	 		"        <monitoring_query:hasResource rdf:resource=\"&im;PlanetLab_smilax1.man.poznan.pl\"/>\r\n" + 
	 		"        <monitoring_query:hasFeature rdf:resource=\"&monitoring_features;FreeMemory\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"\r\n" + 
	 		"    <!-- http://fp7-novi.eu/monitoringQuery_example.owl#minoflast3 -->\r\n" + 
	 		"\r\n" + 
	 		"    <owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#minoflast3\">\r\n" + 
	 		"        <rdf:type rdf:resource=\"&monitoring_stat;Minimum\"/>\r\n" + 
	 		"        <monitoring_stat:hasSample rdf:resource=\"http://fp7-novi.eu/monitoringQuery_example.owl#last3\"/>\r\n" + 
	 		"    </owl:NamedIndividual>\r\n" + 
	 		"</rdf:RDF>\r\n"; 
                assertEquals(true, 26 < service.substrate(up, query).split("\n").length);
        }


}
