package eu.novi.nswitch.federica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.connection.SSHConnection;
import eu.novi.nswitch.Nswitch;
import eu.novi.nswitch.exceptions.FederationException;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;

/**
 * Class implements Nswitch interface. It is used to call NSwitch-driver on
 * planetlab-testbed
 * 
 * @author pikusa
 * 
 */
public class NswitchFederica implements Nswitch {

	private String user;
	private final String KEY_FILE = "/home/novi/.ssh/nswitch.key";
	private SSHConnection ssh = new SSHConnection();; 
	private final String NSWITCH_ADDRESS = "194.132.52.217";
	private final Logger logger = LoggerFactory.getLogger(NswitchFederica.class);
	/**
	 * Method called by nswitch-manager in order to create federation on
	 * federica testbed
	 * 
	 * @param nodeIp
	 *            - The ip of physicall node to federate
	 * @param sliceId
	 *            - slice Id to federate
	 * @param vlanId
	 *            - vlan id used in federated slice
	 * @param sliceName
	 *            - name of the slice to federate. It must be the same as for
	 *            federica
	 * @param privateIp
	 *            - Ip of sliver
	 * @param netmask
	 *            - Netmask in slicer
	 * @throws Exception
	 */
	@Override
	public String federate(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp,
			String netmask) throws FederationException, Exception {

		checkParameters(nodeIp, sliceId, vlanId, sliceName, privateIp, netmask);
		user="novi";
		String command = "sudo -S -p '' /root/l2_ovs_add_intf.sh " + sliceId + " " + nodeIp + " " + vlanId;
		String result = sshCommunication(command, NSWITCH_ADDRESS);
		if (!result.contains("successfully")) {
			throw new FederationException("Tunnel is not created succesfuly on host " + NSWITCH_ADDRESS
					+ " Returned message:" + result);
		}

		return result;
	}

	/**
	 * Method called by nswitch-manager in order to remove federation from
	 * fedeica testbed
	 * 
	 * @param nodeIp
	 *            - The ip of physicall node to federate
	 * @param sliceId
	 *            - slice Id to federate
	 * @param vlanId
	 *            - vlan id used in federated slice
	 * @param sliceName
	 *            - name of the slice to federate. It must be the same as for
	 *            federica
	 * @param privateIp
	 *            - Ip of sliver
	 * @param netmask
	 *            - Netmask in slicer
	 * @throws Exception
	 */
	@Override
	public void defederate(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp,
			String netmask) throws Exception {

		checkParameters(nodeIp, sliceId, vlanId, sliceName, privateIp, netmask);
		user="novi";
		String command = "sudo -S -p '' /root/l2_ovs_del_intf.sh " + sliceId + " " + nodeIp + " " + vlanId;
		String result = sshCommunication(command, NSWITCH_ADDRESS);
		if (!result.contains("removed") && !result.contains("Removed") && !result.contains("There is no")) {
			throw new FederationException("Tunnel was not removed succesfuly on host " + NSWITCH_ADDRESS
					+ " Returned message:" + result);
		}

	}


	private String sshCommunication(String command, String host) throws Exception {
		ssh.executeCommandOnHostWithKey(user, host, KEY_FILE, command);
		String result = "";
		while (ssh.connected()) {
			result = result + ssh.readOutputLine() + "\n";
		}
		logger.info(result);
		return result;
	}

	private void checkParameters(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp,
			String netmask) throws IncorrectTopologyException {

		checkParameterCorecntess(nodeIp, "nodeIp");
		checkParameterCorecntess(sliceId, "sliceId");
		checkParameterCorecntess(vlanId, "vlanId");
		checkParameterCorecntess(sliceName, "sliceName");
		checkParameterCorecntess(privateIp, "privateIp");
		checkParameterCorecntess(netmask, "netmask");
	}

	private void checkParameterCorecntess(String param, String name) throws IncorrectTopologyException {
		if (param == null || param.trim().equals("")) {
			throw new IncorrectTopologyException("Parameter " + name + "is null or empty!");
		}
	}
	
	
	public void setSsh(SSHConnection ssh) {
		this.ssh = ssh;
	}
	
}
