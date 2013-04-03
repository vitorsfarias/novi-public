package eu.novi.nswitch.manager;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.novi.nswitch.exceptions.IncorrectTopologyException;

public class FederationTest {

	private final Federation federation = new Federation();
	
	@Before
	public void whenGivenIpAddressAndNetmask() throws IncorrectTopologyException {
		federation.setSliverIp("192.168.106.2/24");
	}
	
	@Test
	public void shouldSetUpIPaddress() {
		assertEquals("192.168.106.2", federation.getSliverIp());
	}
	
	@Test
	public void shouldSetUpNetmask() {
		assertEquals("24", federation.getNetmask());
	}

}
