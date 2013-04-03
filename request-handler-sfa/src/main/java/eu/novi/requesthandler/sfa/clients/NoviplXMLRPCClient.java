/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.clients;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

import eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException;
import eu.novi.requesthandler.utils.Keystore;

/**
 * XMLRPC Client for communicating with FEDERICA. It sets the trustore and the keystore
 * to use. In addition, it has knowledge of the IPs where the AM and the R are placed.
 * 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 * 
 */
public class NoviplXMLRPCClient implements XMLRPCClient{
	private SSLContext scNoviPL = null;	
	private XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	private XmlRpcClient client = new XmlRpcClient();

	public NoviplXMLRPCClient() throws XMLRPCClientException{	
		try {
			configureSSLSession();
		} catch(Exception e) {
			throw new XMLRPCClientException("ERROR configuring SSL sessions in NOVIPL XMLRPC-Client: " + e.toString(), e.fillInStackTrace());
		}
	}

	private void configureSSLSession() throws KeyManagementException, NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
		final Keystore keys = Keystore.PLE_STORE;
		// Enable SSL debug:
		scNoviPL = SSLContext.getInstance("SSL");
		
		scNoviPL.init(keys.createKeyStore().getKeyManagers(), keys.createTrustStore().getTrustManagers(), null);

		scNoviPL.getClientSessionContext().setSessionTimeout(TIMEOUT);
		HttpsURLConnection.setDefaultSSLSocketFactory(scNoviPL.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(keys.createHostnameVerifier());
	}

	public  Object execXMLRPCAggregate(String commandName, List<Serializable> params) throws XMLRPCClientException {
		try {
			return execXMLRPC(commandName, params, "https://150.254.160.27:12347");
		} catch(Exception e) {
			throw new XMLRPCClientException("ERROR executing command in NOVIPL AM XMLRPC-Client: " + e.toString(), e.fillInStackTrace());
		}
	}

	public  Object execXMLRPCRegistry(String commandName, List<Serializable> params) throws XMLRPCClientException {
		try {
			return execXMLRPC(commandName, params, "https://150.254.160.27:12345");
		} catch (Exception e) {
			throw new XMLRPCClientException("ERROR executing command in NOVIPL Registry XMLRPC-Client: " + e.toString(), e.fillInStackTrace());
		}
	}

	protected  Object execXMLRPC(String commandName,
			List<Serializable> params, String url) throws MalformedURLException, XmlRpcException {

		invalidateSessions();
		config.setServerURL(new URL(url));
		config.setReplyTimeout(120000);
		config.setContentLengthOptional(true);
        config.setEnabledForExtensions(true);
		client.setConfig(config);
		client.setTypeFactory(new MyTypeFactory(client));

		return client.execute(commandName, params);
	}


	private void invalidateSessions() {
		scNoviPL.getClientSessionContext().setSessionTimeout(3);

		scNoviPL.getClientSessionContext().setSessionCacheSize(0);
		
		Enumeration<byte[]> a = scNoviPL.getClientSessionContext().getIds();
		if (a.hasMoreElements())
			scNoviPL.getClientSessionContext().getSession(a.nextElement())
			.invalidate();

	}
	
	protected void setClient(XmlRpcClient client) {
		this.client = client;
	}
}
