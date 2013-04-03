package eu.novi.nswitch.manager.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.connection.SSHConnection;
import eu.novi.nswitch.manager.Federation;
import eu.novi.nswitch.manager.NswitchManager;

public class SliceTagsManager {

	
	private final String KEY_FILE = "/home/novi/nswitch/root_ssh_key.rsa";
	
	private final Logger logger = LoggerFactory.getLogger(NswitchManager.class);
	
	
	/**
	 * Calls the script on the pl node, which setup vsys tags needed by
	 * 
	 * @param federation
	 *            object which stores all the parameters needed for the
	 *            federation creation
	 * @throws Exception
	 *             when it fails to create ssh communication or setup the tags
	 */
	protected String setupTags(Federation federation) throws Exception {
		SSHConnection ssh = new SSHConnection();
		String networkAddress = getNetworkAddress(federation.getSliverIp()) + "/" + federation.getNetmask();
		logger.info("Setting network tag:+ " + networkAddress + " for slice " + federation.getSliceName() + " on node "
				+ federation.getNodeIp());
		String command = "python vsys_tag_0.5.py " + federation.getSliceName() + " " + networkAddress;
		ssh.executeCommandOnHostWithKey("root", federation.getNodeIp(), KEY_FILE, command);
		String result="";
		while(ssh.connected()){
			result = result + ssh.readOutputLine() + "\n";
		}
		logger.info(result);
		logger.info("Sleepping for 70000ms after tags setup");
		Thread.sleep(70000);
		return result;
	}
	
	/**
	 * Primitive method to create network address. Gets the IP and replace the
	 * last value with "0". To be changed
	 * 
	 * @param sliverIp
	 * @return
	 */
	protected String getNetworkAddress(String sliverIp) {
		return sliverIp.substring(0, sliverIp.lastIndexOf(".")) + ".0";
	}


}
