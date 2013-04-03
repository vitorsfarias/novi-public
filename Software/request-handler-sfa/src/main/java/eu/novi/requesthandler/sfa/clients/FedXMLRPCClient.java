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
 * XMLRPC Client for communicating with NOVI PlanetLab. It sets the trustore and the keystore
 * to use. In addition, it has knowledge of the IPs where the AM and the R are placed.
 * 
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 * 
 */
public class FedXMLRPCClient implements XMLRPCClient {
	private SSLContext scFederica = null;
	private XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	private XmlRpcClient client = new XmlRpcClient();

	public FedXMLRPCClient() throws XMLRPCClientException{	
		try {
			configureSSLSession();
		} catch(Exception e) {
			throw new XMLRPCClientException("ERROR configuring SSL sessions in FEDERICA XMLRPC-Client: " + e.toString(), e.fillInStackTrace());
		}
	}

	private void configureSSLSession() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		final Keystore keys = Keystore.FEDERICA_STORE;
		// Enable SSL debug:
		scFederica = SSLContext.getInstance("SSL");
		
		scFederica.init(keys.createKeyStore().getKeyManagers(), keys.createTrustStore().getTrustManagers(), null);
		scFederica.getClientSessionContext().setSessionTimeout(TIMEOUT);
		
		HttpsURLConnection.setDefaultSSLSocketFactory(scFederica.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(keys.createHostnameVerifier());
		
	}

	public Object execXMLRPCRegistry(String commandName, List<Serializable> params) throws XMLRPCClientException {
		try {
			return execXMLRPC(commandName, params, "https://sfa-registry.ict-openlab.eu:12345");
		} catch (Exception e) {
			throw new XMLRPCClientException("ERROR executing command in FEDERICA Registry XMLRPC-Client: " + e.toString(), e.fillInStackTrace());
		}
	}

	public Object execXMLRPCAggregate(String commandName, List<Serializable> params) throws XMLRPCClientException {
		try {
			return execXMLRPC(commandName, params, "https://sfa-federica.ict-openlab.eu:12346");
		} catch (Exception e) {
			throw new XMLRPCClientException("ERROR executing command in FEDERICA AM XMLRPC-Client: " + e.toString(), e.fillInStackTrace());

		}
	}


	protected  Object execXMLRPC(String commandName,
			List<Serializable> params, String url) throws MalformedURLException, XmlRpcException {

		invalidateSessions();

		config.setServerURL(new URL(url));
		config.setReplyTimeout(1800000);
		config.setContentLengthOptional(true);
        config.setEnabledForExtensions(true);

		client.setConfig(config);
		client.setTypeFactory(new MyTypeFactory(client));

		Object result;

		result = client.execute(commandName, params);
		return result;
	}


	private void invalidateSessions() {
		scFederica.getClientSessionContext().setSessionTimeout(3);
		scFederica.getClientSessionContext().setSessionCacheSize(0);

		Enumeration<byte[]> a = scFederica.getClientSessionContext().getIds();
		if (a.hasMoreElements())
			scFederica.getClientSessionContext().getSession(a.nextElement())
			.invalidate();
	}
	
	protected void setClient(XmlRpcClient client) {
		this.client = client;
	}
	
}
