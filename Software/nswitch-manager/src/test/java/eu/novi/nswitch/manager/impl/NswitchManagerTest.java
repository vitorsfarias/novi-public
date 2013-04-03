package eu.novi.nswitch.manager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.nswitch.Nswitch;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;
import eu.novi.nswitch.manager.Federation;

public class NswitchManagerTest {

	
	
	
	@Test
	public void shouldThrowExceptionWhenNswitchFedericaIsNotSet(){
		//given 
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		boolean isNull = false;
		
		// when
		try {
			nswitchManager.createFederation(new TopologyImpl(), "slice1");		
		} catch (Exception e) {
			isNull = true;
		}
		
		//then
		finally{
			assertTrue("nswitch-manager doesn't throw exception when nswitch federica is not set", isNull);
		}
	}
	
	
	@Test
	public void shouldThrowExceptionWhenTopologyNswitchPlanetlabIsNotSet(){
		//given
		Nswitch nswitchFederica= mock(Nswitch.class);
		
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		
		boolean isNull = false;
		
		// when
		try {
			nswitchManager.createFederation(null, "slice1");		
		} catch (Exception e) {
			isNull = true;
		}
		
		//then
		finally{
			assertTrue("nswitch-manager doesn't throw exception when nswitch planetlab is not set", isNull);
		}
	}
	
	
	@Test
	public void shouldThrowExceptionWhenTopologyIsNull(){
		//given 
		Nswitch nswitchPlanetlab = mock(Nswitch.class);
		Nswitch nswitchFederica= mock(Nswitch.class);
		
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		nswitchManager.setNswitchPlanetlab(nswitchPlanetlab);
		
		boolean isNull = false;
		
		// when
		try {
			nswitchManager.createFederation(null, "slice1");		
		} catch (Exception e) {
			isNull = true;
		}
		
		//then
		finally{
			assertTrue("nswitch-manager doesn't throw exception when topology is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenTopologyIsEmpty(){
		//given 
		TopologyImpl slice= new TopologyImpl("");
		Nswitch nswitchPlanetlab = mock(Nswitch.class);
		Nswitch nswitchFederica= mock(Nswitch.class);	
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		nswitchManager.setNswitchPlanetlab(nswitchPlanetlab);
		
		boolean isEmpty = false;
		
		// when
		try {
			nswitchManager.createFederation(slice, "slice1");
		} catch (IncorrectTopologyException e) {
			isEmpty = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-manager does not throw exception when topology is empty", isEmpty);
			
		}
	}
	

	@Test
	public void shouldThrowExceptionWhenSliceNameIsNull(){
		//given 
		Nswitch nswitchPlanetlab = mock(Nswitch.class);
		Nswitch nswitchFederica= mock(Nswitch.class);
		
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		nswitchManager.setNswitchPlanetlab(nswitchPlanetlab);
		TopologyImpl t = new TopologyImpl();
		Set<Resource> setResources = new HashSet<Resource>(); 
		Resource r = new NodeImpl("");
		setResources.add(r);
		t.setContains(setResources);
		
		boolean isNull = false;
		
		// when
		try {
			nswitchManager.createFederation(t, null);		
		} catch (Exception e) {
			isNull = true;
		}
		
		//then
		finally{
			assertTrue("nswitch-manager doesn't throw exception when slice name is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceNameIsEmpty(){
		//given 
		Nswitch nswitchPlanetlab = mock(Nswitch.class);
		Nswitch nswitchFederica= mock(Nswitch.class);
		
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		nswitchManager.setNswitchPlanetlab(nswitchPlanetlab);
		
		TopologyImpl t = new TopologyImpl();
		Set<Resource> setResources = new HashSet<Resource>(); 
		Resource r = new NodeImpl("");
		setResources.add(r);
		t.setContains(setResources);
		
		
		
		boolean isNull = false;
		
		// when
		try {
			nswitchManager.createFederation(t, "");		
		} catch (Exception e) {
			isNull = true;
		}
		
		//then
		finally{
			assertTrue("nswitch-manager doesn't throw exception when slice name is empty", isNull);
		}
	}
	
	
	@Ignore //NOVI-288
	@Test
	public void fullTopologyTest(){
		//given 
		Nswitch nswitchPlanetlab = mock(Nswitch.class);
		Nswitch nswitchFederica= mock(Nswitch.class);
		SliceTagsManager sliceTagsManager = mock(SliceTagsManager.class);
		Federation federation = mock(Federation.class);
		
		NswitchManagerImpl nswitchManager = new NswitchManagerImpl();
		nswitchManager.setNswitchFederica(nswitchFederica);
		nswitchManager.setNswitchPlanetlab(nswitchPlanetlab);
		nswitchManager.setSliceTagsManager(sliceTagsManager);
		
		boolean error = false;
		
		
		try {
			when(nswitchPlanetlab.federate("x", "x", "x", "x", "x", "x")).thenReturn("");
			TopologyImpl t = getTopologyFromFile("/FullTopology.owl");
			nswitchManager.createFederation(t, "slice1");
			
		} catch (IOException e1) {
			error = true;
			e1.printStackTrace();		
		} catch (Exception e) {
			error = true;
			e.printStackTrace();
		}
		
		//then
		finally{
			assertFalse("nswitch-manager throws exception when full topology creation", error);
		}
	}
	
	
	@Test
	public void testFederationClass() throws IncorrectTopologyException{
		Federation f = new Federation();
		f.setFedericaConfigured(true);
		f.setPlanetlabConfigured(true);
		f.isFedericaConfigured();
		f.isPlanetlabConfigured();
		f.setSliverIp("192.168.36.4/24");
	}
	
	@Test
	public void setupTagsTest(){
		SliceTagsManager stl = new SliceTagsManager();
		String network = stl.getNetworkAddress("192.168.37.5");
		assertEquals("192.168.37.0", network);
	}
	
	
	
	private TopologyImpl getTopologyFromFile(String path) throws IOException{
		URL url = this.getClass().getResource(path);
		IMRepositoryUtilImpl im = new IMRepositoryUtilImpl();
		String owlFileContent;
		try {
			owlFileContent = readFile(new File(url.getPath()));
		} catch (IOException e) {
			throw e;
		}
		return (TopologyImpl)im.getTopologyFromFile(owlFileContent);
	}

	private String readFile(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String currentLine = reader.readLine();
		StringBuilder result = new StringBuilder();
		while(currentLine != null){
				result.append(currentLine);
				currentLine = reader.readLine();
		}
		return result.toString();
	}
	
	
}
