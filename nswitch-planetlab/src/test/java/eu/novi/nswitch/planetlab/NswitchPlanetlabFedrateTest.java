package eu.novi.nswitch.planetlab;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcraft.jsch.JSchException;

import eu.novi.connection.SSHConnection;
import eu.novi.nswitch.exceptions.FederationException;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;



public class NswitchPlanetlabFedrateTest {
	
	@Test
	public void shouldThrowExceptionWhenNodeIpIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate(null, "x", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when nodeIp is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenNodeIpIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("", "x", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when nodeIp is empty", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceIdIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", null, "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when sliceId is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceIdIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when sliceId is empty", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenVlanIdIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", null, "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when vlanId is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenVlanIdIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when vlanId is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenSliceNameIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", null, "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when sliceName is empty", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenSliceNameIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when sliceName is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenPrivateIpIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "x", null, "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when privateIp is empty", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenPrivateIpIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "x", "", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when privateIp is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenNetmaskIsNull(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", null);
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when netmask is null", isNull);
		}
	}
	
	@Test
	public void shouldThrowExceptionWhenNetmaskIsEmpty(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		boolean isNull = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", "");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//then
		finally{
			assertTrue("nswitch-Planetlab doesn't throw exception when netmask is empty", isNull);
		}
	}
	
	
	
	
	
	@Test
	public void shouldNotThrowExceptionWhenTryToMakeSSHConnection(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		SSHConnection ssh = mock(SSHConnection.class);
		nswitch.setSsh(ssh);
		when(ssh.connected()).thenReturn(false);
		boolean error = false;
		
		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", "x");
		} catch (JSchException e){
			error = true;
		} catch (Exception e){
		}
		
		//then
		finally{
			assertFalse("nswitch-Planetlab throws exception when try to connect via ssh", error);
		}
	}

		@Test
	public void shouldReturnTapInterfaceString(){
		//given 
		NswitchPlanetlab nswitch = new NswitchPlanetlab();
		String logs = "Creating GRE interface tap520-1 192.168.37.4/29 gre=3701 remote=194.132.52.217 pointopoint=192.168.37.0";
		
		//when
		String result = nswitch.findTapInterfaceInLogs(logs);
		
		//then
		assertEquals("tap520-1", result);
		
		
	}
}

