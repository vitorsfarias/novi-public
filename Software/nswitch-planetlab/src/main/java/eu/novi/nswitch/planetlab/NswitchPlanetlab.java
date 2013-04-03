package eu.novi.nswitch.planetlab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import eu.novi.connection.SSHConnection;
import eu.novi.nswitch.Nswitch;
import eu.novi.nswitch.exceptions.FederationException;
import eu.novi.nswitch.exceptions.IncorrectTopologyException;

public class NswitchPlanetlab implements Nswitch {

	private String user;
	private SSHConnection ssh = new SSHConnection();
	
	private final String nswitchAddress = "194.132.52.217";
	private final String KEY_FILE = "/home/novi/nswitch/root_ssh_key.rsa";

	private final Logger logger = LoggerFactory.getLogger(NswitchPlanetlab.class);


	@Override
	/**
	 * Method called by nswitch-manager in order to create federation on federica testbed
	 * @param nodeIp - The ip of physicall node to federate
	 * @param sliceId - slice Id to federate
	 * @param vlanId - vlan id used in federated slice
	 * @param sliceName - name of the slice to federate. It must be the same as for planetlab
	 * @param privateIp - Ip of sliver 
	 * @param netmask - Netmask in slicer
	 * @throws Exception
	 */
	public String federate(String nodeIp, String sliceId, String vlanId, String sliceName, String privateIp,
			String netmask) throws JSchException, Exception, FederationException {
		String result = "";

		checkParameters(nodeIp, sliceId, vlanId, sliceName, privateIp, netmask);

		logger.info("Calling nswitch-driver for slice " + sliceName + " on node " + nodeIp);

		user = "root";
		String command = "./l2_over_gre_up.sh " + sliceId + " " + sliceName + " " + nswitchAddress + " " + privateIp
				+ " " + netmask;
		result = sshCommunication(command, nodeIp);
		if (result.toLowerCase().contains("error")) {
			throw new FederationException("nswitch-planetlab-driver: federation error on host " + nodeIp + " : "
					+ result);
		} else if (result.contains("not in netblock")) {
			throw new FederationException("nswitch-planetlab-driver: federation error on host " + nodeIp + " : "
					+ result);
		}
		return result;
	}

	protected String findTapInterfaceInLogs(String logs) {
		String temp = logs.substring(logs.indexOf("tap"));
		return temp.substring(0, temp.indexOf(" "));
	}

	/**
	 * Method called by nswitch-manager in order to remove federation from
	 * planetlab testbed
	 * 
	 * @param nodeIp
	 *            - The ip of physicall node to federate
	 * @param sliceId
	 *            - slice Id to federate
	 * @param vlanId
	 *            - vlan id used in federated slice
	 * @param sliceName
	 *            - name of the slice to federate. It must be the same as for
	 *            planetlab
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

		user = "root";
		String command = "./l2_over_gre_down.sh " + sliceName + " " + privateIp;
		String result = sshCommunication(command, nodeIp);
		if (result.toLowerCase().contains("error")) {
			throw new FederationException("nswitch-planetlab dirver returned error during defederation on host "
					+ nodeIp + " : " + result);
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
