package eu.novi.nswitch.federica;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.jcraft.jsch.JSchException;

import eu.novi.connection.SSHConnection;

import eu.novi.nswitch.exceptions.IncorrectTopologyException;

public class NswitchFedericaFedrateTest {

	@Test
	public void shouldThrowExceptionWhenNodeIpIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate(null, "x", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when nodeIp is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenNodeIpIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("", "x", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when nodeIp is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenSliceIdIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", null, "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when sliceId is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenSliceIdIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "", "x", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when sliceId is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenVlanIdIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", null, "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when vlanId is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenVlanIdIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "", "x", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when vlanId is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenSliceNameIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", null, "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when sliceName is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenSliceNameIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "", "x", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when sliceName is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenPrivateIpIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "x", null, "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when privateIp is empty", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenPrivateIpIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "x", "", "x");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when privateIp is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenNetmaskIsNull() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", null);
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when netmask is null", isNull);
		}
	}

	@Test
	public void shouldThrowExceptionWhenNetmaskIsEmpty() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		boolean isNull = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", "");
		} catch (IncorrectTopologyException e) {
			isNull = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// then
		finally {
			assertTrue("nswitch-federica doesn't throw exception when netmask is empty", isNull);
		}
	}

	@Test
	public void shouldNotThrowExceptionWhenTryToMakeSSHConnection() {
		// given
		NswitchFederica nswitch = new NswitchFederica();
		SSHConnection ssh = mock(SSHConnection.class);
		nswitch.setSsh(ssh);
		when(ssh.connected()).thenReturn(false);
		boolean error = false;

		// when
		try {
			nswitch.federate("x", "x", "x", "x", "x", "x");
		} catch (JSchException e) {
			error = true;
		} catch (Exception e) {
		}

		// then
		finally {
			assertFalse("nswitch-federica throws exception when try to connect via ssh", error);
		}
	}

}
