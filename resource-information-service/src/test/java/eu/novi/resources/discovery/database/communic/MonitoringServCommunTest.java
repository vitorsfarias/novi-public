package eu.novi.resources.discovery.database.communic;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import eu.novi.monitoring.MonSrv;
import eu.novi.monitoring.credential.Credential;
import eu.novi.monitoring.util.MonitoringQuery;
import eu.novi.resources.discovery.scheduler.PeriodicUpdate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class MonitoringServCommunTest {
	
	private  MonSrv monServ;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		PeriodicUpdate update = new PeriodicUpdate();
		update.setScheduler(Executors.newScheduledThreadPool(5));
	}
	
	@Test
	public void testMonitoringFindResou1() throws IOException
	{//test when the answer is an object
		//set up the mocks
		monServ = mock(MonSrv.class);
		when(monServ.substrate(any(Credential.class), any(String.class))).
		thenReturn(readFile("src/test/resources/monitoring2.json"));
		MonitoringQuery quer = mock(MonitoringQuery.class);
		when(monServ.createQuery()).thenReturn(quer);
		
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(monServ);
		
		Set<String> nodes = new HashSet<String>();
		nodes.add("planetlab1-novi.lab.netmode.ece.ntua.gr");
		MonitoringInfo monInfo = MonitoringServCommun.getNodesMonData(nodes).iterator().next();
		assertEquals(4, monInfo.getCpuCores());
		assertEquals(1.995, monInfo.getCpuSpeed(), 0.001);
		assertEquals(0.0082, monInfo.getMemory(), 0.0001);
		assertEquals(2.963952, monInfo.getStorage(), 0.000001);
		
		

	}
	
	@Test
	public void testMonitoringFindResou2() throws IOException
	{//test when the answer is a table
		//set up the mocks
		monServ = mock(MonSrv.class);
		when(monServ.substrate(any(Credential.class), any(String.class))).
		thenReturn(readFile("src/test/resources/monitoringFindResou.json"));
		MonitoringQuery quer = mock(MonitoringQuery.class);
		when(monServ.createQuery()).thenReturn(quer);
		
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(monServ);
		
		Set<String> nodes = new HashSet<String>();
		nodes.add("planetlab1-novi.lab.netmode.ece.ntua.gr");
		nodes.add("smilax4.psnc.pl");
		
		//it should get two  same answers
		//MonitoringInfo monInfo = MonitoringServCommun.getNodesMonData(nodes).iterator().next();
		
		for (MonitoringInfo monInfo : MonitoringServCommun.getNodesMonData(nodes))
		{
			assertEquals(2, monInfo.getCpuCores());
			assertEquals(2.3118, monInfo.getCpuSpeed(), 0.001);
			assertEquals(0.00741, monInfo.getMemory(), 0.0001);
			assertEquals(12.888536, monInfo.getStorage(), 0.000001);
			
		}
		
		//check the file
		when(monServ.substrate(any(Credential.class), any(String.class))).
		thenReturn(readFile("src/test/resources/monitoringFindResou2.json"));
		nodes = new HashSet<String>();
		nodes.add("planetlab1-novi.lab.netmode.ece.ntua.gr");
		for (MonitoringInfo monInfo : MonitoringServCommun.getNodesMonData(nodes))
		{
			assertEquals(0, monInfo.getCpuCores());
			assertEquals(2.311847, monInfo.getCpuSpeed(), 0.001);
			assertEquals(0.043944, monInfo.getMemory(), 0.0001);
			assertEquals(9.666508, monInfo.getStorage(), 0.000001);
			
		}

		
		//test a second file, missing values
		when(monServ.substrate(any(Credential.class), any(String.class))).
		thenReturn(readFile("src/test/resources/monitoringFindResMissVal.json"));

		MonitoringInfo monInfo = MonitoringServCommun.getNodesMonData(nodes).iterator().next();

		assertEquals(-1, monInfo.getCpuCores()); 
		assertEquals(-1, monInfo.getCpuSpeed(), 0.001);
		assertEquals(0.3264, monInfo.getMemory(), 0.0001);
		assertEquals(-1, monInfo.getStorage(), 0.000001);
	}
	
	
	@Test
	public void testGetValue() throws IOException
	{
		
		String arrayStringAnswer = readFile("src/test/resources/monitoringFindResou.json");
		JsonNode arrayAnswer = MonitoringServCommun.getJNodefromString(arrayStringAnswer);
		
		String stringAnswer = readFile("src/test/resources/monitoring2.json");
		JsonNode objectAnswer = MonitoringServCommun.getJNodefromString(stringAnswer);
		
		assertNotNull(MonitoringServCommun.getValue(objectAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeMemory"));
		assertNotNull(MonitoringServCommun.getValue(objectAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace"));
		assertNull(MonitoringServCommun.getValue(objectAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeMemoryWrong"));
		assertNull(MonitoringServCommun.getValue(objectAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#notExist"));
		
		
		assertNotNull(MonitoringServCommun.getValue(arrayAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeMemory"));
		assertNotNull(MonitoringServCommun.getValue(arrayAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace"));
		//assertNull(MonitoringServCommun.getValue(arrayAnswer, 
			//	"http://fp7-novi.eu/monitoring_features.owl#FreeMemory", "no Memory"));
		assertNull(MonitoringServCommun.getValue(arrayAnswer, 
				"http://fp7-novi.eu/monitoring_features.owl#notExist"));
		
		
		//test get the objcet from string
		assertNull(MonitoringServCommun.getJNodefromString(
				readFile("src/test/resources/monitoringFindResInvalid.json")));
		
	}
	
	@Test
	public void testMonitoringUtilValues() throws IOException
	{
		//cpuUtil 0.35, freeMem 400, totalMem 1000, usedDisk 550, freeDisk 650
		MonitoringAvarInfo ans = MonitoringServCommun.extractMonUtilValues("node uti",
				readFile("src/test/resources/monitoringUtil.json"));
		
		assertEquals(0.35, ans.getCpuUtil(), 0.01);
		assertEquals(0.6, ans.getMemoryUtil(), 0.01);
		assertEquals(0.45, ans.getStorageUtil(), 0.01);
		
	}
	
	@Test
	public void testMonitoringAverageUtilValues() throws IOException
	{
		//cpu: 0.35, 0.60 memory: 0.45, 0.58, disk: 0.77, 0.64
		MonitoringAvarInfo ans = MonitoringServCommun.extractMonAverUtilValue(
				readFile("src/test/resources/monitoringUtilAggr.json"));
		
		assertEquals(0.47, ans.getCpuUtil(), 0.01);
		assertEquals(0.51, ans.getMemoryUtil(), 0.01);
		assertEquals(0.70, ans.getStorageUtil(), 0.01);
		
		ans = MonitoringServCommun.extractMonAverUtilValue(
				readFile("src/test/resources/monitoringUtilAggr2.json"));
		
		assertEquals(0.875, ans.getCpuUtil(), 0.01);
		assertEquals(0.855, ans.getMemoryUtil(), 0.01);
		assertEquals(0.20, ans.getStorageUtil(), 0.01);
		
	}
	
	@Test
	public void testMonitoringNoDiscNoMem() throws IOException
	{
		//set up the mocks
		monServ = mock(MonSrv.class);
		when(monServ.substrate(any(Credential.class), any(String.class))).
		thenReturn(readFile("src/test/resources/monitoringNoDiscMem.json"));
		MonitoringQuery quer = mock(MonitoringQuery.class);
		when(monServ.createQuery()).thenReturn(quer);
		
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(monServ);
		
		Set<String> nodes = new HashSet<String>();
		nodes.add("planetlab1-novi.lab.netmode.ece.ntua.gr");
		MonitoringInfo monInfo = MonitoringServCommun.getNodesMonData(nodes).iterator().next();
		assertEquals(6, monInfo.getCpuCores());
		assertEquals(2.995, monInfo.getCpuSpeed(), 0.001);
		assertEquals(-1, monInfo.getMemory(), 0.001);
		assertEquals(-1, monInfo.getStorage(), 0.001);
	}

	@Test
	public void testHardCoded() {
		MonitoringServCommun mon = new MonitoringServCommun();
		mon.setMonServ(null);
		
		Set<String> nodes = new TreeSet<String>();
		Set<MonitoringInfo> monInfo;
		nodes.add("not_exist");
		nodes.add("novilab.elte.hu");
		nodes.add("planetlab1-novi.lab.netmode.ece.ntua.gr");
		nodes.add("planetlab2-novi.lab.netmode.ece.ntua.gr");
		nodes.add("smilax1.man.poznan.pl");
		// nodes.add("smilax2.man.poznan.pl");
		// nodes.add("smilax3.man.poznan.pl");
		// nodes.add("smilax4.man.poznan.pl");
		// nodes.add("smilax5.man.poznan.pl");
		
		monInfo = MonitoringServCommun.getNodesMonData(nodes);
		for (MonitoringInfo inf : monInfo)
		{
			if (inf.getNodeUri().equals("www.test.pl"))
			{
				assertEquals(4, inf.getCpuCores());
				assertEquals(3, inf.getCpuSpeed(),0.1);
				assertEquals(500, inf.getStorage(), 0.1);
				assertEquals(4, inf.getMemory(), 0.1);

			}
		}
	}
	
	public static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}


}
