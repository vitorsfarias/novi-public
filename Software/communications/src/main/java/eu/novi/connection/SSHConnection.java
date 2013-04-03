package eu.novi.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class SSHConnection {

	private static final Logger logger = LoggerFactory.getLogger(SSHConnection.class);

	private BufferedReader br;
	private InputStream in;
	private Session session;
	private Channel channel;
	private boolean connectedStatus = false;
	private static  final int SSHPORT = 22;
	private static final int TIMEOUT = 30000;
	private static final int AVAILABLE_BYTES = 1024;

	/**
	 * 
	 * @return
	 */
	public boolean connected() {
		return connectedStatus;
	}

	/**
	 * 
	 * @param user
	 * @param host
	 * @param password
	 * @param command
	 * @throws JSchException
	 * @throws Exception
	 */
	public void executeCommandOnHostWithPassword(String user, String host, String password, String command)
			throws JSchException, Exception {

		JSch jsch = new JSch();
		session = jsch.getSession(user, host, SSHPORT);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(TIMEOUT);
		executeCommandInSession(session, command);
	}

	/**
	 * 
	 * @param user
	 * @param host
	 * @param sshKeyLocation
	 * @param command
	 * @param
	 * @throws JSchException
	 * @throws Exception
	 */
	public void executeCommandOnHostWithKey(String user, String host, String sshKeyLocation, String command)
			throws JSchException, Exception {
		JSch jsch = new JSch();
		jsch.addIdentity(sshKeyLocation);
		session = jsch.getSession(user, host, SSHPORT);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(TIMEOUT);
		executeCommandInSession(session, command);
	}

	private void executeCommandInSession(Session session, String command) throws JSchException, Exception {
		channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		((ChannelExec) channel).setErrStream(System.out);
		in = channel.getInputStream();
		br = new BufferedReader(new InputStreamReader(in));
		channel.connect();
		connectedStatus = true;
	}

	/**
	 * Reads the output stream that. It always reads the InputStream.available() amount of bytes. If there is expected more
	 * (for example when "tail -f" command is executed, than this method should be called many times.
	 *  
	 * 
	 * @param bytes
	 *            = number of bytes to read. If bytes<0, then default value is
	 *            InputStream.available()
	 * @return output If there is no mroe bytes that can be read the output=""
	 */
	public String readOutput(int bytes) throws IOException {
		String result = "";
		if (in.available() > 0) {
			byte[] tmp = new byte[AVAILABLE_BYTES];
			int i = in.read(tmp, 0, AVAILABLE_BYTES);
			if (i > 0){
				result = (new String(tmp, 0, i));
			}
		}
		if (channel.isClosed() || !channel.isConnected()) {
			disconnect();
		}
		return result;

	}

	
	public String readOutputLine() throws IOException {
		String result = "";
		try{
			result = br.readLine(); 	
			if (result == null) { 
			   disconnect(); 
			   return ""; 
			} 
		} catch (IOException e) { 
			//disconnect();
			throw e; 
			} 
		if (channel.isClosed()) {
			logger.info("exit-status: " + channel.getExitStatus());
			disconnect(); 
		}
		
		return result;
	}
	
	public String readOutputLineWhenBufferReady() throws IOException {
		String result = "";
		try{
			if(!br.ready()){
				disconnect();
				return "";
			}
			result = br.readLine(); 	
			if (result == null) { 
			   disconnect(); 
			   return ""; 
			} 
		} catch (IOException e) { 
			disconnect();
			throw e; 
			} 
		if (channel.isClosed()) {
			logger.info("exit-status: " + channel.getExitStatus());
			disconnect(); 
		}
		
		return result;
	}
	
	/**
	 * Close the connection that is hosted by the object of this class.
	 * @throws IOException 
	 */
	public void disconnect() throws IOException {
		in.close();
		br.close();
		channel.disconnect();
		session.disconnect();
		connectedStatus = false;
	}
}
