package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.novi.resources.discovery.util.NoviIPs;

public class NoviIPsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testPublicIps() {
		assertEquals("194.132.52.190",
				NoviIPs.getPublicIP("urn:publicid:IDN+federica.eu+node+garr.mil.vserver2"));
		assertEquals("194.132.52.3",
				NoviIPs.getPublicIP("urn:publicid:IDN+federica.eu+node+garr.mil.router1"));
		assertEquals("150.254.160.22",
				NoviIPs.getPublicIP("urn:publicid:IDN+novipl:novi+node+smilax4.man.poznan.pl"));
		assertEquals("147.102.22.66",
				NoviIPs.getPublicIP("urn:publicid:IDN+novipl:novi+node+planetlab1-novi.lab.netmode.ece.ntua.gr"));
		
		assertNull(NoviIPs.getPublicIP("notExist"));
		
		
	}

}
